/*
 Copyright (C) 2022, Martin Drößler <m.droessler@handelsblattgroup.com>
 Copyright (C) 2022, Handelsblatt GmbH

 This file is part of account-permission-management tool

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package dev.hmg.apm.integration.ado.service;

import dev.hmg.apm.db.ProductCategoryDAO;
import dev.hmg.apm.db.ProductDAO;
import dev.hmg.apm.db.entity.ProductCategoryEntity;
import dev.hmg.apm.db.entity.ProductEntity;
import org.azd.core.types.Project;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AdoProjectSyncServiceTest {
	private AdoProjectSyncService sut;
	
	@Mock
	private AdoProjectFetchService adoProjectFetchService;
	@Mock
	private ProductCategoryDAO productCategoryDao;
	@Mock
	private ProductDAO productDAO;
	@Captor
	private ArgumentCaptor<ProductCategoryEntity> categoryCaptor;
	
	@Before
	public void init() {
		sut = new AdoProjectSyncService();
		sut.setAdoProjectFetchService(adoProjectFetchService);
		sut.setProductCategoryDao(productCategoryDao);
		sut.setProductDAO(productDAO);
	}
	
	@Test
	public void testSyncProjectsFromAdoToDatabase_forUnexpectedState() {
		given(adoProjectFetchService.fetchProjects()).willReturn(Collections.emptyList());
		
		try {
			sut.syncProjectsFromAdoToDatabase();
			fail();
		} catch (IllegalStateException e) {
			assertNotNull(e.getMessage());
		}
		
		verify(adoProjectFetchService, times(1)).fetchProjects();
		verifyNoInteractions(productDAO, productCategoryDao);
	}
	
	@Test
	public void testSyncProjectsFromAdoToDatabase() {
		String expectedProductKey = "azure-devops";
		int expectedNewCategories = 2;
		ProductEntity dummyProduct = mock(ProductEntity.class);
		Project p1 = createDummyProject("project-1", "Dummy Project 1");
		Project p2 = createDummyProject("Project.2", "Dummy Project 2");
		Project p3 = createDummyProject("Pröjèct 3", "Dummy Project 3");
		List<Project> dummyProjects = Arrays.asList(p1, p2, p3);
		List<String> dummyCategoryNames = Arrays.asList("project-1", "CUSTOM");
		
		given(adoProjectFetchService.fetchProjects()).willReturn(dummyProjects);
		given(productCategoryDao.findCategoryNamesForProductKey(anyString())).willReturn(dummyCategoryNames);
		given(productDAO.findProductByKey(expectedProductKey)).willReturn(dummyProduct);
		
		int result = sut.syncProjectsFromAdoToDatabase();
		assertEquals(expectedNewCategories, result);
		
		verify(adoProjectFetchService, times(1)).fetchProjects();
		verify(productCategoryDao, times(1)).findCategoryNamesForProductKey(expectedProductKey);
		verify(productDAO, times(1)).findProductByKey(expectedProductKey);
		verify(productCategoryDao, times(expectedNewCategories)).save(categoryCaptor.capture());
		
		List<ProductCategoryEntity> resultCategories = categoryCaptor.getAllValues();
		assertNotNull(resultCategories);
		assertEquals(expectedNewCategories, resultCategories.size());
		assertExpectedCategory(resultCategories.get(0), p2.getName(), "project-2", p2.getDescription(), dummyProduct);
		assertExpectedCategory(resultCategories.get(1), p3.getName(), "project-3", p3.getDescription(), dummyProduct);
	}
	
	private Project createDummyProject(final String name, final String description) {
		Project p = new Project();
		p.setName(name);
		p.setDescription(description);
		
		return p;
	}
	
	private void assertExpectedCategory(final ProductCategoryEntity cat, final String expectedName, final String expectedKey, 
										final String expectedDesc, final ProductEntity expectedProduct) {
		assertNotNull(cat);
		assertTrue(cat.isAutomated());
		assertEquals(expectedName, cat.getName());
		assertEquals(expectedKey, cat.getKey());
		assertEquals(expectedDesc, cat.getDescription());
		assertEquals(expectedProduct, cat.getProduct());
	}
}

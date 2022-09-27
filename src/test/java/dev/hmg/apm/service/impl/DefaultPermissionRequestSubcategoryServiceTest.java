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
package dev.hmg.apm.service.impl;

import dev.hmg.apm.db.PermissionRequestSubcategoryDAO;
import dev.hmg.apm.db.ProductCategoryDAO;
import dev.hmg.apm.db.ProductDAO;
import dev.hmg.apm.db.entity.PermissionRequestEntity;
import dev.hmg.apm.db.entity.PermissionRequestSubcategoryEntity;
import dev.hmg.apm.db.entity.ProductCategoryEntity;
import dev.hmg.apm.db.entity.ProductEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultPermissionRequestSubcategoryServiceTest {
	private DefaultPermissionRequestSubcategoryService sut;
	
	@Mock
	private ProductDAO productDAO;
	@Mock
	private ProductCategoryDAO productCategoryDAO;
	@Mock
	private PermissionRequestSubcategoryDAO permissionRequestSubcategoryDAO;
	
	@Before
	public void init() {
		sut = new DefaultPermissionRequestSubcategoryService();
		sut.setProductDAO(productDAO);
		sut.setProductCategoryDAO(productCategoryDAO);
		sut.setPermissionRequestSubcategoryDAO(permissionRequestSubcategoryDAO);
	}
	
	@Test
	public void testCreateRequestSubcategoriesFromFormData_forNullFormData() {
		List<PermissionRequestSubcategoryEntity> result = sut.createRequestSubcategoriesFromFormData(null, null, null);
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}
	
	@Test
	public void testCreateRequestSubcategoriesFromFormData_forEmptyFormData() {
		String[] categories = new String[]{};
		List<PermissionRequestSubcategoryEntity> result = sut.createRequestSubcategoriesFromFormData(categories, null, null);
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}
	
	@Test
	public void testCreateRequestSubcategoriesFromFormData() {
		String[] categories = new String[] {"invalid", "missing_mode", "product1_category1_read", "product1_category2_write", "product1_categoryinvalid_write"};
		int expectedResultSize = 3;
		ProductEntity dummyProduct = mock(ProductEntity.class);
		int dummyProductId = 21;
		ProductCategoryEntity dummyCategory1 = mock(ProductCategoryEntity.class);
		ProductCategoryEntity dummyCategory2 = mock(ProductCategoryEntity.class);
		PermissionRequestEntity dummyPermissionRequest = mock(PermissionRequestEntity.class);
		Map<String,String> allParams = new HashMap<>();
		allParams.put("invalid", "TEST");
		allParams.put("product1_category1_read", "true");
		allParams.put("product1_category1_comment", "Test comment");
		allParams.put("invalid_category1_comment", "Should not appear");
		allParams.put("product1_category2_comment", "");
		
		given(productDAO.findProductByKey("product1")).willReturn(dummyProduct);
		given(dummyProduct.getId()).willReturn(dummyProductId);
		given(productCategoryDAO.findProductCategoryByProductAndKey(dummyProductId, "category1")).willReturn(dummyCategory1);
		given(productCategoryDAO.findProductCategoryByProductAndKey(dummyProductId, "category2")).willReturn(dummyCategory2);
		given(permissionRequestSubcategoryDAO.save(any(PermissionRequestSubcategoryEntity.class))).willAnswer(new Answer<PermissionRequestSubcategoryEntity>() {
			@Override
			public PermissionRequestSubcategoryEntity answer(final InvocationOnMock invocationOnMock) throws Throwable {
				return invocationOnMock.getArgument(0);
			}
		});
		
		List<PermissionRequestSubcategoryEntity> result = sut.createRequestSubcategoriesFromFormData(categories, dummyPermissionRequest, allParams);
		assertNotNull(result);
		assertEquals(expectedResultSize, result.size());
		PermissionRequestSubcategoryEntity result1 = result.get(0);
		PermissionRequestSubcategoryEntity result2 = result.get(1);
		PermissionRequestSubcategoryEntity result3 = result.get(2);
		assertNotNull(result1);
		assertEquals(dummyCategory1, result1.getProductCategory());
		assertEquals("read", result1.getAccessMode());
		assertEquals(dummyPermissionRequest, result1.getPermissionRequest());
		assertNotNull(result2);
		assertEquals(dummyCategory2, result2.getProductCategory());
		assertEquals("write", result2.getAccessMode());
		assertEquals(dummyPermissionRequest, result2.getPermissionRequest());
		assertNotNull(result3);
		assertEquals(dummyCategory1, result3.getProductCategory());
		assertEquals("comment", result3.getAccessMode());
		assertEquals(dummyPermissionRequest, result3.getPermissionRequest());
		assertEquals("Test comment", result3.getComment());
		
		verify(productDAO, atLeastOnce()).findProductByKey("product1");
		verify(productCategoryDAO, times(4)).findProductCategoryByProductAndKey(anyInt(), anyString());
		verify(permissionRequestSubcategoryDAO, times(expectedResultSize)).save(any(PermissionRequestSubcategoryEntity.class));
	}
}

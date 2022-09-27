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
package dev.hmg.apm.integration.ado;

import dev.hmg.apm.db.ProductCategoryDAO;
import dev.hmg.apm.integration.ado.service.AdoProjectFetchService;
import dev.hmg.apm.integration.ado.service.AdoProjectSyncService;
import org.azd.core.types.Project;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AdoSettingsControllerTest {
	private AdoSettingsController sut;
	
	@Mock
	private Model model;
	@Mock
	private Locale dummyLocale;
	@Mock
	private MessageSource messageSource;
	@Mock
	private RedirectAttributes redirectAttributes;
	@Mock
	private AdoProjectFetchService adoProjectFetchService;
	@Mock
	private ProductCategoryDAO productCategoryDao;
	@Mock
	private AdoProjectSyncService adoProjectSyncService;
	
	@Before
	public void init() {
		sut = new AdoSettingsController();
		sut.setAdoProjectFetchService(adoProjectFetchService);
		sut.setAdoProjectSyncService(adoProjectSyncService);
		sut.setProductCategoryDao(productCategoryDao);
		sut.setMessageSource(messageSource);
	}
	
	@Test
	public void testSettings() {
		List<Project> dummyProjects = Collections.emptyList();
		List<String> dummyCategoryNames = Collections.emptyList();
		String productKey = "azure-devops";
		given(adoProjectFetchService.fetchProjects()).willReturn(dummyProjects);
		given(productCategoryDao.findCategoryNamesForProductKey(productKey)).willReturn(dummyCategoryNames);
		
		String result = sut.settings(model);
		assertEquals("adoSettings", result);
		
		verify(adoProjectFetchService, times(1)).fetchProjects();
		verify(productCategoryDao, times(1)).findCategoryNamesForProductKey(productKey);
		verify(model, times(1)).addAttribute("adoProjects", dummyProjects);
		verify(model, times(1)).addAttribute("adoCategories", dummyCategoryNames);
	}
	
	@Test
	public void testSyncProjects_forAdoConnectionIssues() {
		String dummyMessage = "TEST";
		doThrow(new IllegalStateException("TEST")).when(adoProjectSyncService).syncProjectsFromAdoToDatabase();
		given(messageSource.getMessage(anyString(), any(), any(Locale.class))).willReturn(dummyMessage);
		
		String result = sut.syncProjects(redirectAttributes, dummyLocale);
		assertEquals("redirect:/adoSettings", result);
		
		verify(messageSource, times(1)).getMessage("integration.ado.settings.sync_failed", null, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", dummyMessage);
	}
	
	@Test
	public void testSyncProjects() {
		String dummyMessage = "TEST";
		int expectedChanges = 21;
		given(messageSource.getMessage(anyString(), any(), any(Locale.class))).willReturn(dummyMessage);
		given(adoProjectSyncService.syncProjectsFromAdoToDatabase()).willReturn(expectedChanges);
		
		String result = sut.syncProjects(redirectAttributes, dummyLocale);
		assertEquals("redirect:/adoSettings", result);
		
		verify(messageSource, times(1)).getMessage("integration.ado.settings.sync_success", new Object[]{expectedChanges}, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("message", dummyMessage);
	}
}

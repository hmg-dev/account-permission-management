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

import dev.hmg.apm.config.AppConfig;
import dev.hmg.apm.model.PaginationData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

@RunWith(MockitoJUnitRunner.class)
public class DefaultPaginationServiceTest {
	private DefaultPaginationService sut;
	
	@Mock
	private AppConfig appConfig;
	
	@Before
	public void init() {
		sut = new DefaultPaginationService();
		sut.setAppConfig(appConfig);
	}
	
	@Test
	public void testDeterminePaginationData() {
		int totalEntries = 42;
		int currentPage = 6;
		int entriesPerPage = 2;
		int visiblePageNumbers = 5;
		int expectedPageCount = 21;
		int expectedFirstVisiblePageNumber = 4;
		
		given(appConfig.getEntriesPerPage()).willReturn(entriesPerPage);
		given(appConfig.getPageNumbersInPaginationBar()).willReturn(visiblePageNumbers);
		
		PaginationData result = sut.determinePaginationData(totalEntries, currentPage);
		assertNotNull(result);
		assertEquals(currentPage, result.getCurrentPage());
		assertEquals(expectedPageCount, result.getPageCount());
		assertEquals(visiblePageNumbers, result.getVisiblePageNumberAmount());
		assertEquals(expectedFirstVisiblePageNumber, result.getFirstVisiblePage());
		assertEquals(entriesPerPage, result.getEntriesPerPage());
		
		verify(appConfig, atLeastOnce()).getEntriesPerPage();
		verify(appConfig, atLeastOnce()).getPageNumbersInPaginationBar();
	}
	
	@Test
	public void testDeterminePaginationData_forOnlyOnePage() {
		int totalEntries = 42;
		int currentPage = 0;
		int entriesPerPage = 42;
		int visiblePageNumbers = 5;
		
		int expectedVisiblePageNumbers = 1;
		int expectedPageCount = 1;
		int expectedFirstVisiblePageNumber = 0;
		
		given(appConfig.getEntriesPerPage()).willReturn(entriesPerPage);
		given(appConfig.getPageNumbersInPaginationBar()).willReturn(visiblePageNumbers);
		
		PaginationData result = sut.determinePaginationData(totalEntries, currentPage);
		assertNotNull(result);
		assertEquals(currentPage, result.getCurrentPage());
		assertEquals(expectedPageCount, result.getPageCount());
		assertEquals(expectedVisiblePageNumbers, result.getVisiblePageNumberAmount());
		assertEquals(expectedFirstVisiblePageNumber, result.getFirstVisiblePage());
		assertEquals(entriesPerPage, result.getEntriesPerPage());
		
		verify(appConfig, atLeastOnce()).getEntriesPerPage();
		verify(appConfig, atLeastOnce()).getPageNumbersInPaginationBar();
	}
	
	@Test
	public void testDeterminePaginationData_forUnevenPageEntryMatching() {
		int totalEntries = 42;
		int currentPage = 0;
		int entriesPerPage = 40;
		int visiblePageNumbers = 5;
		
		int expectedVisiblePageNumbers = 2;
		int expectedPageCount = 2;
		int expectedFirstVisiblePageNumber = 0;
		
		given(appConfig.getEntriesPerPage()).willReturn(entriesPerPage);
		given(appConfig.getPageNumbersInPaginationBar()).willReturn(visiblePageNumbers);
		
		PaginationData result = sut.determinePaginationData(totalEntries, currentPage);
		assertNotNull(result);
		assertEquals(currentPage, result.getCurrentPage());
		assertEquals(expectedPageCount, result.getPageCount());
		assertEquals(expectedVisiblePageNumbers, result.getVisiblePageNumberAmount());
		assertEquals(expectedFirstVisiblePageNumber, result.getFirstVisiblePage());
		assertEquals(entriesPerPage, result.getEntriesPerPage());
		
		verify(appConfig, atLeastOnce()).getEntriesPerPage();
		verify(appConfig, atLeastOnce()).getPageNumbersInPaginationBar();
	}
	
	@Test
	public void testDeterminePaginationData_forLastPageEntry() {
		int totalEntries = 42;
		int currentPage = 8;
		int entriesPerPage = 5;
		int visiblePageNumbers = 5;
		
		int expectedVisiblePageNumbers = 3;
		int expectedPageCount = 9;
		int expectedFirstVisiblePageNumber = 6;
		
		given(appConfig.getEntriesPerPage()).willReturn(entriesPerPage);
		given(appConfig.getPageNumbersInPaginationBar()).willReturn(visiblePageNumbers);
		
		PaginationData result = sut.determinePaginationData(totalEntries, currentPage);
		assertNotNull(result);
		assertEquals(currentPage, result.getCurrentPage());
		assertEquals(expectedPageCount, result.getPageCount());
		assertEquals(expectedVisiblePageNumbers, result.getVisiblePageNumberAmount());
		assertEquals(expectedFirstVisiblePageNumber, result.getFirstVisiblePage());
		assertEquals(entriesPerPage, result.getEntriesPerPage());
		
		verify(appConfig, atLeastOnce()).getEntriesPerPage();
		verify(appConfig, atLeastOnce()).getPageNumbersInPaginationBar();
	}
}

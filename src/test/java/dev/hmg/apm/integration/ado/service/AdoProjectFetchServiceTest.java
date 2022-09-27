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

import org.azd.core.CoreApi;
import org.azd.core.types.Project;
import org.azd.core.types.Projects;
import org.azd.exceptions.AzDException;
import org.azd.interfaces.AzDClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AdoProjectFetchServiceTest {
	private AdoProjectFetchService sut;
	
	@Mock
	private AdoConnectionService adoConnectionService;
	@Mock
	private AzDClient adoClient;
	@Mock
	private CoreApi coreApi;
	
	@Before
	public void init() {
		sut = new AdoProjectFetchService();
		sut.setAdoConnectionService(adoConnectionService);
		sut.setAdoClient(adoClient);
		
		given(adoClient.getCoreApi()).willReturn(coreApi);
	}
	
	@Test
	public void testInit() {
		sut.init();
		verify(adoConnectionService, times(1)).connectionData();
	}
	
	@Test
	public void testFetchProjects_forException() throws AzDException {
		doThrow(new AzDException("TEST")).when(coreApi).getProjects();
		
		List<Project> result = sut.fetchProjects();
		assertNotNull(result);
		assertTrue(result.isEmpty());
		
		verify(adoClient, times(1)).getCoreApi();
		verify(coreApi, times(1)).getProjects();
	}
	
	@Test
	public void testFetchProjects() throws AzDException {
		Project p1 = mock(Project.class);
		Project p2 = mock(Project.class);
		List<Project> dummyResults = Arrays.asList(p1, p2);
		Projects dummyProjects = mock(Projects.class);
		
		given(coreApi.getProjects()).willReturn(dummyProjects);
		given(dummyProjects.getProjects()).willReturn(dummyResults);
		
		List<Project> result = sut.fetchProjects();
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(dummyResults, result);
		
		verify(adoClient, times(1)).getCoreApi();
		verify(coreApi, times(1)).getProjects();
		verify(dummyProjects, times(1)).getProjects();
	}
}

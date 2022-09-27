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
package dev.hmg.apm.util;

import dev.hmg.apm.db.entity.PermissionRequestEntity;
import dev.hmg.apm.db.entity.ProductEntity;
import dev.hmg.apm.model.RequestStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SupportUtilsTest {
	
	private SupportUtils sut;
	
	@Mock
	private OAuth2User user;
	
	@Before
	public void init() {
		sut = new SupportUtils();
	}
	
	@Test
	public void testCurrentTimeMillis() {
		long result = sut.currentTimeMillis();
		assertTrue(result > 0);
	}
	
	@Test
	public void testIsAdmin() {
		OAuth2User user = mock(OAuth2User.class);
		GrantedAuthority authority1 = mock(GrantedAuthority.class);
		GrantedAuthority authority2 = mock(GrantedAuthority.class);
		Collection<GrantedAuthority> authorities = Arrays.asList(authority1, authority2);
		Collection<GrantedAuthority> userAuthorities = Arrays.asList(authority1);
		
		willReturn(authorities).given(user).getAuthorities();
		given(authority1.getAuthority()).willReturn("ROLE_company.domain");
		given(authority2.getAuthority()).willReturn(SupportUtils.ADMIN_GROUP);
		
		assertTrue(sut.isAdmin(user));
		
		willReturn(userAuthorities).given(user).getAuthorities();
		assertFalse(sut.isAdmin(user));
		
		verify(user, atLeastOnce()).getAuthorities();
	}
	
	@Test
	public void testFormatAuditLogDetails() {
		String requestFor = "test, user";
		String requestFrom = "other, user";
		long requestTimestamp = 123456789;
		Instant requestDateTime = Instant.ofEpochMilli(requestTimestamp);
		String expectedFormatedDateTime =  DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault()).format(requestDateTime);
		PermissionRequestEntity request = mock(PermissionRequestEntity.class);
		ProductEntity p1 = mock(ProductEntity.class);
		ProductEntity p2 = mock(ProductEntity.class);
		String expectedResult = "permission-request for target-user '" + requestFor + "' initially requested at '" + 
				expectedFormatedDateTime +"' from '" + requestFrom + "' for products: product1, product2";
		
		given(request.getRequestFor()).willReturn(requestFor);
		given(request.getRequestFrom()).willReturn(requestFrom);
		given(request.getRequestDateTime()).willReturn(requestDateTime);
		given(request.getProducts()).willReturn(Arrays.asList(p1, p2));
		given(p1.getName()).willReturn("product1");
		given(p2.getName()).willReturn("product2");
		
		String result = sut.formatAuditLogDetails(request);
		assertNotNull(result);
		assertEquals(expectedResult, result);
	}
	
	@Test
	public void testParseDateTimestamp() {
		assertEquals((Long)86400000L, sut.parseDateTimestamp("1970-01-02", "y-M-d", 0L));
		assertEquals((Long)0L, sut.parseDateTimestamp(null, "yyyy-MM-dd", 0L));
		assertNull(sut.parseDateTimestamp("invalid-01-01", "yyyy-MM-dd", null));
		assertEquals((Long)1643673600000L, sut.parseDateTimestamp("2022-02-01", "yyyy-MM-dd", 0L));
	}
	
	@Test
	public void testCreateSubcategoryMappingWithoutAccessMode_forNullInput() {
		Map<String, String> result = sut.createSubcategoryMappingWithoutAccessMode(null);
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}
	
	@Test
	public void testCreateSubcategoryMappingWithoutAccessMode() {
		String[] input = new String[]{
				"product1_category1_read", "", "product1_category2_write", "product1_category3_write", "product1_category4_admin", 
				"product2_category1_write", "", "product2_category2_read", "product2_category3_read", ""
		};
		Map<String, String> expectedResult = new HashMap<>();
		expectedResult.put("product1_category1", "product1_category1_read");
		expectedResult.put("product1_category2", "product1_category2_write");
		expectedResult.put("product1_category3", "product1_category3_write");
		expectedResult.put("product1_category4", "product1_category4_admin");
		expectedResult.put("product2_category1", "product2_category1_write");
		expectedResult.put("product2_category2", "product2_category2_read");
		expectedResult.put("product2_category3", "product2_category3_read");
		
		Map<String, String> result = sut.createSubcategoryMappingWithoutAccessMode(input);
		assertNotNull(result);
		assertEquals(expectedResult, result);
	}
	
	@Test
	public void testIsUserCanEditRequest_forNoSuchRequest() {
		assertFalse(sut.isUserCanEditRequest(user, Optional.empty()));
	}
	
	@Test
	public void testIsUserCanEditRequest_forInsufficientPrivileges() {
		PermissionRequestEntity dummyRequest = new PermissionRequestEntity();
		dummyRequest.setRequestFrom("test.user@hcompany.localdomain");
		
		given(user.getAttribute("email")).willReturn("someone.else@company.localdomain");
		
		assertFalse(sut.isUserCanEditRequest(user, Optional.of(dummyRequest)));
	}
	
	@Test
	public void testIsUserCanEditRequest_forRejectedRequest() {
		PermissionRequestEntity dummyRequest = new PermissionRequestEntity();
		dummyRequest.setRequestFrom("test.user@company.localdomain");
		dummyRequest.setResolution(RequestStatus.REJECTED.name());
		
		given(user.getAttribute("email")).willReturn("test.user@company.localdomain");
		
		assertFalse(sut.isUserCanEditRequest(user, Optional.of(dummyRequest)));
	}
	
	@Test
	public void testIsUserCanEditRequest_forAcceptedRequest() {
		PermissionRequestEntity dummyRequest = new PermissionRequestEntity();
		dummyRequest.setRequestFrom("test.user@company.localdomain");
		dummyRequest.setResolution(RequestStatus.ACCEPTED.name());
		
		given(user.getAttribute("email")).willReturn("test.user@company.localdomain");
		
		assertFalse(sut.isUserCanEditRequest(user, Optional.of(dummyRequest)));
	}
	
	@Test
	public void testIsUserCanEditRequest() {
		PermissionRequestEntity dummyRequest = new PermissionRequestEntity();
		dummyRequest.setRequestFrom("test.user@company.localdomain");
		dummyRequest.setResolution(RequestStatus.OPEN.name());
		
		given(user.getAttribute("email")).willReturn("test.user@company.localdomain");
		
		assertTrue(sut.isUserCanEditRequest(user, Optional.of(dummyRequest)));
		
		verify(user, times(1)).getAttribute("email");
	}
	
	@Test
	public void testIsAdminCanEditRequest_forNoSuchRequest() {
		assertFalse(sut.isAdminCanEditRequest(Optional.empty()));
	}
	
	@Test
	public void testIsAdminCanEditRequest_forRejectedRequest() {
		PermissionRequestEntity dummyRequest = new PermissionRequestEntity();
		dummyRequest.setResolution(RequestStatus.REJECTED.name());
		
		assertFalse(sut.isAdminCanEditRequest(Optional.of(dummyRequest)));
	}
	
	@Test
	public void testIsAdminCanEditRequest_forAcceptedRequest() {
		PermissionRequestEntity dummyRequest = new PermissionRequestEntity();
		dummyRequest.setResolution(RequestStatus.ACCEPTED.name());
		
		assertFalse(sut.isAdminCanEditRequest(Optional.of(dummyRequest)));
	}
	
	@Test
	public void testIsAdminCanEditRequest() {
		PermissionRequestEntity dummyRequest = new PermissionRequestEntity();
		dummyRequest.setResolution(RequestStatus.OPEN.name());
		
		assertTrue(sut.isAdminCanEditRequest(Optional.of(dummyRequest)));
	}
}

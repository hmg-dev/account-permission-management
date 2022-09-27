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

import dev.hmg.apm.db.entity.PermissionRequestEntity;
import dev.hmg.apm.db.entity.UserEntity;
import dev.hmg.apm.service.PermissionRequestService;
import dev.hmg.apm.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAdminHelperServiceTest {
	private DefaultAdminHelperService sut;
	
	@Mock
	private OAuth2AuthenticationToken auth;
	@Mock
	private OAuth2User oauthUser;
	@Mock
	private PermissionRequestService permissionRequestService;
	@Mock
	private UserService userService;
	
	@Before
	public void init() {
		sut = new DefaultAdminHelperService();
		sut.setPermissionRequestService(permissionRequestService);
		sut.setUserService(userService);
		
		given(auth.getPrincipal()).willReturn(oauthUser);
	}
	
	@Test
	public void testAcceptRequest() {
		int requestId = 42;
		UserEntity user = mock(UserEntity.class);
		String targetUserMail = "test@user";
		PermissionRequestEntity request = mock(PermissionRequestEntity.class);
		
		given(request.getRequestFor()).willReturn(targetUserMail);
		given(userService.findOrCreateUser(anyString(), anyString())).willReturn(user);
		given(permissionRequestService.acceptRequest(anyInt(), any(OAuth2User.class))).willReturn(request);
		
		PermissionRequestEntity result = sut.acceptRequest(requestId, auth);
		assertNotNull(result);
		assertEquals(request, result);
		
		verify(userService, times(1)).findOrCreateUser(eq(targetUserMail), anyString());
		verify(permissionRequestService, times(1)).acceptRequest(requestId, oauthUser);
		verify(userService, times(1)).assignRequestedProductsToUser(user, request);
		verify(userService, times(1)).assignRequestedCategoriesToUser(user, request);
	}
}

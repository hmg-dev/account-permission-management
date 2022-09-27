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
package dev.hmg.apm;

import dev.hmg.apm.db.UserDetailsDAO;
import dev.hmg.apm.db.entity.UserDetailsEntity;
import dev.hmg.apm.util.SupportUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserUIControllerTest {
	private UserUIController sut;
	
	@Mock
	private Model model;
	@Mock
	private OAuth2AuthenticationToken auth;
	@Mock
	private OAuth2User user;
	@Mock
	private UserDetailsDAO userDetailsDAO;
	@Mock
	private SupportUtils supportUtils;
	
	@Before
	public void init() {
		sut = new UserUIController();
		sut.setUserDetailsDAO(userDetailsDAO);
		sut.setSupportUtils(supportUtils);
		
		given(auth.getPrincipal()).willReturn(user);
	}
	
	@Test
	public void testUserPermissions() {
		String userMail = "test.user@company.tld";
		UserDetailsEntity userDetails = mock(UserDetailsEntity.class);
		
		given(user.getAttribute("email")).willReturn(userMail);
		given(userDetailsDAO.findUserByMail(userMail)).willReturn(userDetails);
		given(supportUtils.isAdmin(user)).willReturn(true);
		
		String result = sut.userPermissions(model, auth);
		assertEquals("myPermissions", result);
		
		verify(auth, atLeastOnce()).getPrincipal();
		verify(user, times(1)).getAttribute("email");
		verify(userDetailsDAO, times(1)).findUserByMail(userMail);
		verify(model, times(1)).addAttribute("user", userDetails);
		verify(model, times(1)).addAttribute("isAdmin", true);
		verify(supportUtils, times(1)).isAdmin(user);
	}
}

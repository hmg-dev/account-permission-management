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

import dev.hmg.apm.db.UserCategoryAssignmentDAO;
import dev.hmg.apm.db.UserDAO;
import dev.hmg.apm.db.UserProductAssignmentDAO;
import dev.hmg.apm.db.entity.*;
import dev.hmg.apm.util.SupportUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultUserServiceTest {
	private DefaultUserService sut;
	
	@Mock
	private OAuth2User user;
	@Mock
	private UserDAO userDAO;
	@Mock
	private UserCategoryAssignmentDAO userCategoryAssignmentDAO;
	@Mock
	private UserProductAssignmentDAO userProductAssignmentDAO;
	@Mock
	private SupportUtils supportUtils;
	
	@Captor
	private ArgumentCaptor<UserProductAssignment> productAssignmentCaptor;
	
	@Before
	public void init() {
		sut = new DefaultUserService();
		sut.setSupportUtils(supportUtils);
		sut.setUserDAO(userDAO);
		sut.setUserCategoryAssignmentDAO(userCategoryAssignmentDAO);
		sut.setUserProductAssignmentDAO(userProductAssignmentDAO);
	}
	
	@Test
	public void testFindOrCreateUser_forExistingUser() {
		String userMail = "testmail";
		UserEntity existingUser = mock(UserEntity.class);
		
		given(userDAO.findUserByMail(anyString())).willReturn(existingUser);
		
		UserEntity result = sut.findOrCreateUser(userMail, "");
		assertNotNull(result);
		assertEquals(existingUser, result);
		
		verify(userDAO, times(1)).findUserByMail(userMail);
	}

	@Test
	public void testFindOrCreateUser_forExistingAuthUser() {
		String userMail = "testmail";
		UserEntity existingUser = mock(UserEntity.class);

		given(user.getAttribute("email")).willReturn(userMail);
		given(userDAO.findUserByMail(anyString())).willReturn(existingUser);

		UserEntity result = sut.findOrCreateUser(user, Locale.ENGLISH);
		assertNotNull(result);
		assertEquals(existingUser, result);

		verify(user, times(1)).getAttribute("email");
		verify(userDAO, times(1)).findUserByMail(userMail);
	}
	
	@Test
	public void testFindOrCreateUser_forCreateUser() {
		String userMail = "testmail";
		given(userDAO.findUserByMail(anyString())).willReturn(null);
		given(userDAO.save(any(UserEntity.class))).willAnswer((Answer<UserEntity>) invocationOnMock -> invocationOnMock.getArgument(0));
		
		UserEntity result = sut.findOrCreateUser(userMail, "");
		assertNotNull(result);
		assertEquals(userMail, result.geteMail());
		assertEquals("", result.getName());
		assertEquals("de", result.getLocale());
		
		verify(userDAO, times(1)).findUserByMail(userMail);
		verify(userDAO, times(1)).save(any(UserEntity.class));
	}

	@Test
	public void testFindOrCreateUser_forCreateAuthUser() {
		String userMail = "testmail";
		String givenName = "Test";
		String familyName = "User";

		given(user.getAttribute("email")).willReturn(userMail);
		given(user.getAttribute("given_name")).willReturn(givenName);
		given(user.getAttribute("family_name")).willReturn(familyName);
		given(userDAO.findUserByMail(anyString())).willReturn(null);
		given(userDAO.save(any(UserEntity.class))).willAnswer((Answer<UserEntity>) invocationOnMock -> invocationOnMock.getArgument(0));

		UserEntity result = sut.findOrCreateUser(user, Locale.ENGLISH);
		assertNotNull(result);
		assertEquals(userMail, result.geteMail());
		assertEquals(givenName + " " + familyName, result.getName());
		assertEquals(Locale.ENGLISH.getLanguage(), result.getLocale());

		verify(userDAO, times(1)).findUserByMail(userMail);
		verify(userDAO, times(1)).save(any(UserEntity.class));
		verify(user, atLeastOnce()).getAttribute("email");
		verify(user, times(1)).getAttribute("given_name");
		verify(user, times(1)).getAttribute("family_name");
	}
	
	@Test
	public void testAssignRequestedProductsToUser() {
		long dummyTimestamp = 42;
		ProductEntity p1 = mock(ProductEntity.class);
		ProductEntity pExists = mock(ProductEntity.class);
		ProductEntity p2 = mock(ProductEntity.class);
		List<ProductEntity> dummyProducts = Arrays.asList(p1, pExists, p2);
		PermissionRequestEntity dummyRequest = mock(PermissionRequestEntity.class);
		UserEntity dummyUser = new UserEntity();
		
		given(supportUtils.currentTimeMillis()).willReturn(dummyTimestamp);
		given(dummyRequest.getProducts()).willReturn(dummyProducts);
		given(dummyRequest.getValidToTimestamp()).willReturn(dummyTimestamp);
		given(userProductAssignmentDAO.existsAssignment(dummyUser, pExists)).willReturn(true);
		
		sut.assignRequestedProductsToUser(dummyUser, dummyRequest);
		
		verify(dummyRequest, times(1)).getProducts();
		verify(userProductAssignmentDAO, times(dummyProducts.size())).existsAssignment(eq(dummyUser), any(ProductEntity.class));
		verify(userProductAssignmentDAO, times(2)).save(productAssignmentCaptor.capture());
		
		List<UserProductAssignment> assignments = productAssignmentCaptor.getAllValues();
		assertEquals(2, assignments.size());
		UserProductAssignment a1 = assignments.get(0);
		assertNotNull(a1);
		assertEquals((Long)dummyTimestamp, a1.getValidToTimestamp());
		assertEquals(dummyUser, a1.getUser());
		assertEquals(p1, a1.getProduct());
		assertEquals(dummyTimestamp, a1.getAssignmentDateTimestamp());
		
		UserProductAssignment a2 = assignments.get(1);
		assertNotNull(a2);
		assertEquals((Long)dummyTimestamp, a2.getValidToTimestamp());
		assertEquals(dummyUser, a2.getUser());
		assertEquals(p2, a2.getProduct());
		assertEquals(dummyTimestamp, a2.getAssignmentDateTimestamp());
	}
	
	@Test
	public void testAssignRequestedCategoriesToUser_forEmptyCategories() {
		PermissionRequestEntity dummyRequest = mock(PermissionRequestEntity.class);
		UserEntity dummyUser = new UserEntity();
		
		sut.assignRequestedCategoriesToUser(dummyUser, dummyRequest);
		
		verifyNoInteractions(userCategoryAssignmentDAO);
	}
	
	@Test
	public void testAssignRequestedCategoriesToUser_forCategoryProductNotInList() {
		ProductEntity detachedProduct = mock(ProductEntity.class);
		ProductCategoryEntity cat = mock(ProductCategoryEntity.class);
		PermissionRequestSubcategoryEntity requestedCat = mock(PermissionRequestSubcategoryEntity.class);
		PermissionRequestEntity dummyRequest = mock(PermissionRequestEntity.class);
		UserEntity dummyUser = new UserEntity();
		
		given(dummyRequest.getProducts()).willReturn(Collections.emptyList());
		given(dummyRequest.getRequestSubcategories()).willReturn(Collections.singletonList(requestedCat));
		given(requestedCat.getProductCategory()).willReturn(cat);
		given(cat.getProduct()).willReturn(detachedProduct);
		
		sut.assignRequestedCategoriesToUser(dummyUser, dummyRequest);
		
		verifyNoInteractions(userCategoryAssignmentDAO);
	}
	
	@Test
	public void testAssignRequestedCategoriesToUser() {
		long dummyTimestamp = 42;
		ProductEntity product1 = mock(ProductEntity.class);
		ProductEntity product2 = mock(ProductEntity.class);
		PermissionRequestSubcategoryEntity requestedCat1 = mock(PermissionRequestSubcategoryEntity.class);
		PermissionRequestSubcategoryEntity requestedCat2 = mock(PermissionRequestSubcategoryEntity.class);
		ProductCategoryEntity cat1 = mock(ProductCategoryEntity.class);
		ProductCategoryEntity cat2 = mock(ProductCategoryEntity.class);
		PermissionRequestEntity dummyRequest = mock(PermissionRequestEntity.class);
		UserEntity dummyUser = new UserEntity();
		UserCategoryAssignment existingAssignment = mock(UserCategoryAssignment.class);
		
		given(userCategoryAssignmentDAO.findByUserAndCategory(dummyUser, cat1)).willReturn(existingAssignment);
		given(dummyRequest.getProducts()).willReturn(Arrays.asList(product1, product2));
		given(dummyRequest.getRequestSubcategories()).willReturn(Arrays.asList(requestedCat1, requestedCat2));
		given(dummyRequest.getValidToTimestamp()).willReturn(dummyTimestamp);
		given(cat1.getProduct()).willReturn(product1);
		given(cat2.getProduct()).willReturn(product2);
		given(requestedCat1.getProductCategory()).willReturn(cat1);
		given(requestedCat2.getProductCategory()).willReturn(cat2);
		
		sut.assignRequestedCategoriesToUser(dummyUser, dummyRequest);
		
		verify(userCategoryAssignmentDAO, times(2)).findByUserAndCategory(eq(dummyUser), any(ProductCategoryEntity.class));
		verify(existingAssignment, times(1)).setComment(any());
		verify(existingAssignment, never()).setAssignmentDateTimestamp(anyLong());
		verify(existingAssignment, times(1)).setUpdateDateTimestamp(anyLong());
		verify(existingAssignment, times(1)).setAccessMode(any());
		verify(existingAssignment, times(1)).setValidToTimestamp(eq(dummyTimestamp));
		verify(supportUtils, times(2)).currentTimeMillis();
		verify(userCategoryAssignmentDAO, times(2)).save(any(UserCategoryAssignment.class));
	}
	
	@Test
	public void testRevokePermissions_forInvalidUser() {
		String userMail = "test@narf.zort";
		given(userDAO.findUserByMail(anyString())).willReturn(null);
		
		try {
			sut.revokePermissions(userMail);
			fail();
		} catch (IllegalStateException e) {
			assertNotNull(e);
		}
		
		verify(userDAO, times(1)).findUserByMail(userMail);
		verifyNoInteractions(userCategoryAssignmentDAO, userProductAssignmentDAO);
	}
	
	@Test
	public void testRevokePermissions() {
		String userMail = "test@narf.zort";
		int userId = 21;
		UserEntity existingUser = mock(UserEntity.class);
		UserProductAssignment a1 = mock(UserProductAssignment.class);
		UserProductAssignment a2 = mock(UserProductAssignment.class);
		UserCategoryAssignment ac1 = mock(UserCategoryAssignment.class);
		UserCategoryAssignment ac2 = mock(UserCategoryAssignment.class);
		int expectedAssignmentUpdates = 1; // update only those without validTo-Date
		
		given(a2.getValidToTimestamp()).willReturn(42L);
		given(a1.getValidToTimestamp()).willReturn(null);
		given(ac2.getValidToTimestamp()).willReturn(36L);
		given(ac1.getValidToTimestamp()).willReturn(null);
		given(existingUser.getId()).willReturn(userId);
		given(userDAO.findUserByMail(anyString())).willReturn(existingUser);
		given(userProductAssignmentDAO.findAssignmentsForUserId(anyInt())).willReturn(Arrays.asList(a1, a2));
		given(userCategoryAssignmentDAO.findAssignmentsByUserId(anyInt())).willReturn(Arrays.asList(ac1, ac2));
		
		sut.revokePermissions(userMail);
		
		verify(userDAO, times(1)).findUserByMail(userMail);
		verify(existingUser, atLeastOnce()).getId();
		verify(userProductAssignmentDAO, times(1)).findAssignmentsForUserId(userId);
		verify(userProductAssignmentDAO, times(expectedAssignmentUpdates)).save(any(UserProductAssignment.class));
		verify(a1, times(1)).setValidToTimestamp(anyLong());
		verify(userCategoryAssignmentDAO, times(1)).findAssignmentsByUserId(userId);
		verify(userCategoryAssignmentDAO, times(expectedAssignmentUpdates)).save(any(UserCategoryAssignment.class));
		verify(ac1, times(1)).setValidToTimestamp(anyLong());
	}
	
}

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
import dev.hmg.apm.db.*;
import dev.hmg.apm.db.entity.*;
import dev.hmg.apm.model.*;
import dev.hmg.apm.service.NotificationService;
import dev.hmg.apm.service.PermissionRequestSubcategoryService;
import dev.hmg.apm.util.SupportUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultPermissionRequestServiceTest {
	private DefaultPermissionRequestService sut;
	
	@Mock
	private AppConfig appConfig;
	@Mock
	private AuditLogDAO auditLogDAO;
	@Mock
	private OAuth2User user;
	@Mock
	private PermissionRequestDAO permissionRequestDAO;
	@Mock
	private PermissionRequestSubcategoryService permissionRequestSubcategoryService;
	@Mock
	private ProductDAO productDAO;
	@Mock
	private SupportUtils supportUtils;
	@Mock
	private UserDAO userDAO;
	@Mock
	private MessageSource messageSource;
	@Mock
	private NotificationService notificationService;
	@Mock
	private PermissionRequestSubcategoryDAO permissionRequestSubcategoryDAO;
	
	@Captor
	private ArgumentCaptor<AuditLogEntity> auditLogCaptor;
	
	@Before
	public void setup() {
		sut = new DefaultPermissionRequestService();
		sut.setAppConfig(appConfig);
		sut.setAuditLogDAO(auditLogDAO);
		sut.setPermissionRequestDAO(permissionRequestDAO);
		sut.setPermissionRequestSubcategoryService(permissionRequestSubcategoryService);
		sut.setProductDAO(productDAO);
		sut.setSupportUtils(supportUtils);
		sut.setUserDAO(userDAO);
		sut.setMessageSource(messageSource);
		sut.setNotificationService(notificationService);
		sut.setPermissionRequestSubcategoryDAO(permissionRequestSubcategoryDAO);
	}
	
	@Test
	public void testIsValidEMail() {
		given(appConfig.getRegularAllowedMailSuffixes()).willReturn(Arrays.asList("@company.localdomain", "@extern.company.localdomain"));
		
		assertTrue(sut.isValidEMail("test.user@company.localdomain"));
		assertTrue(sut.isValidEMail("test.user@extern.company.localdomain"));
		
		assertFalse(sut.isValidEMail("zort.narf@ey-hallo.localdomain"));
		assertFalse(sut.isValidEMail("zort.narf_company.localdomain"));
		assertFalse(sut.isValidEMail("zort.narf@company.localdomain@ey-hallo.localdomain"));
		assertFalse(sut.isValidEMail("zort.narf@invalid"));
		
		verify(appConfig, atLeastOnce()).getRegularAllowedMailSuffixes();
	}
	
	@Test
	public void testIsValidEMail_forDevMode() {
		given(appConfig.getRegularAllowedMailSuffixes()).willReturn(Arrays.asList("@company.localdomain", "@extern.company.localdomain"));
		given(appConfig.isDevMode()).willReturn(true);
		given(appConfig.getDevModeMailSuffixes()).willReturn(Arrays.asList("@ey-hallo.localdomain", "@dev.company.localdomain"));
		
		assertTrue(sut.isValidEMail("zort.narf@ey-hallo.localdomain"));
		assertTrue(sut.isValidEMail("zort.narf@dev.company.localdomain"));
		assertTrue(sut.isValidEMail("test.user@company.localdomain"));
		assertTrue(sut.isValidEMail("test.user@extern.company.localdomain"));
		
		assertFalse(sut.isValidEMail("zort.narf_company.localdomain"));
		assertFalse(sut.isValidEMail("zort.narf@company.localdomain@ey-hallo.localdomain"));
		assertFalse(sut.isValidEMail("zort.narf@invalid"));
		
		verify(appConfig, atLeastOnce()).getRegularAllowedMailSuffixes();
		verify(appConfig, atLeastOnce()).isDevMode();
		verify(appConfig, atLeastOnce()).getDevModeMailSuffixes();
	}
	
	@Test
	public void testIsValidPrivilegedMail_forNonAdmin() {
		given(supportUtils.isAdmin(any(OAuth2User.class))).willReturn(false);
		
		assertFalse(sut.isValidPrivilegedMail("test.user@dev.company.localdomain", user));
		
		verify(supportUtils, times(1)).isAdmin(user);
		verifyNoInteractions(appConfig);
	}
	
	@Test
	public void testIsValidPrivilegedMail() {
		given(supportUtils.isAdmin(any(OAuth2User.class))).willReturn(true);
		given(appConfig.getPrivilegedMailSuffixes()).willReturn(Arrays.asList("@dev.company.localdomain", "@ey-hallo.localdomain"));
		
		assertTrue(sut.isValidPrivilegedMail("test.user@dev.company.localdomain", user));
		assertTrue(sut.isValidPrivilegedMail("test.user@ey-hallo.localdomain", user));
		assertFalse(sut.isValidPrivilegedMail("test.user@invalid", user));
		assertFalse(sut.isValidPrivilegedMail("test.user@other.localdomain", user));
		assertFalse(sut.isValidPrivilegedMail("test.user@company.localdomain", user));
		
		verify(supportUtils, atLeastOnce()).isAdmin(user);
		verify(appConfig, atLeastOnce()).getPrivilegedMailSuffixes();
	}
	
	@Test
	public void testDetermineTargetEMail_forUserTypeOther() {
		String otherUserMail = "test.user@company.localdomain";
		String targetUserType = "other";
		
		assertEquals(otherUserMail, sut.determineTargetEMail(targetUserType, otherUserMail, user));
		verify(user, times(1)).getAttribute(anyString());
	}
	
	@Test
	public void testDetermineTargetEMail_forUserTypeMyself() {
		String otherUserMail = "test.user@company.localdomain";
		String dummyUserEmail = "test.user@company.localdomain";
		String targetUserType = "myself";
		
		given(user.getAttribute("email")).willReturn(dummyUserEmail);
		
		assertEquals(dummyUserEmail, sut.determineTargetEMail(targetUserType, otherUserMail, user));
		verify(user, times(1)).getAttribute(anyString());
	}
	
	@Test
	public void testCreatePermissionRequest() {
		String targetUserMail = "test.user@ey-hallo.localdomain";
		String[] products = new String[] {"azure", "", "", "elk", ""};
		String[] categories = new String[] {"comment", "dummy"};
		long changeTimestamp = 42;
		String dummyUserEmail = "test.user@company.localdomain";
		List<ProductEntity> dummyProducts = Collections.singletonList(mock(ProductEntity.class));
		List<PermissionRequestSubcategoryEntity> dummySubcategories = Collections.singletonList(mock(PermissionRequestSubcategoryEntity.class));
		Map<String,String> allParams = Collections.emptyMap();
		
		given(supportUtils.currentTimeMillis()).willReturn(changeTimestamp);
		given(productDAO.findProductsByKeys(anyList())).willReturn(dummyProducts);
		given(permissionRequestSubcategoryService.createRequestSubcategoriesFromFormData(eq(categories), any(PermissionRequestEntity.class), anyMap())).willReturn(dummySubcategories);
		given(user.getAttribute("email")).willReturn(dummyUserEmail);
		given(permissionRequestDAO.save(any(PermissionRequestEntity.class))).willAnswer(new Answer<PermissionRequestEntity>() {
			@Override
			public PermissionRequestEntity answer(final InvocationOnMock invocationOnMock) throws Throwable {
				return invocationOnMock.getArgument(0);
			}
		});
		
		PermissionRequestEntity result = sut.createPermissionRequest(user, targetUserMail, products, categories, allParams);
		assertNotNull(result);
		assertEquals(targetUserMail, result.getRequestFor());
		assertEquals(RequestStatus.OPEN.name(), result.getResolution());
		assertEquals(changeTimestamp, result.getRequestDateTimestamp());
		assertEquals(dummyUserEmail, result.getRequestFrom());
		assertEquals(dummyProducts, result.getProducts());
		//assertEquals(dummySubcategories, result.getRequestSubcategories()); // FIXME: jpa hibernate session bullshit
		
		verify(supportUtils, times(1)).currentTimeMillis();
		verify(supportUtils, atLeastOnce()).parseDateTimestamp(any(), eq("yyyy-MM-dd"), eq(null));
		verify(productDAO, times(1)).findProductsByKeys(anyList());
		verify(permissionRequestSubcategoryService, times(1)).createRequestSubcategoriesFromFormData(eq(categories), any(PermissionRequestEntity.class), anyMap());
		verify(user, times(1)).getAttribute(anyString());
	}
	
	@Test
	public void testEditPermissionRequest() {
		String targetUserMail = "test.user@ey-hallo.localdomain";
		String[] products = new String[] {"azure", "", "", "elk", ""};
		String[] categories = new String[] {"comment", "dummy"};
		Map<String,String> allParams = Collections.emptyMap();
		PermissionRequestEntity request = mock(PermissionRequestEntity.class);
		List<PermissionRequestSubcategoryEntity> requestSubCats = new ArrayList<>();
		requestSubCats.add(mock(PermissionRequestSubcategoryEntity.class));
		List<ProductEntity> dummyProducts = Collections.singletonList(mock(ProductEntity.class));
		
		given(request.getRequestSubcategories()).willReturn(requestSubCats);
		given(productDAO.findProductsByKeys(anyList())).willReturn(dummyProducts);
		
		PermissionRequestEntity result = sut.editPermissionRequest(request, targetUserMail, products, categories, allParams);
		assertNotNull(request);
		assertSame(request, result);
		
		verify(request, times(1)).setRequestFor(targetUserMail);
		verify(request, times(1)).setProducts(dummyProducts);
		verify(request, times(1)).setValidToTimestamp(any());
		verify(productDAO, times(1)).findProductsByKeys(anyList());
		verify(supportUtils, atLeastOnce()).parseDateTimestamp(any(), eq("yyyy-MM-dd"), eq(null));
		verify(permissionRequestSubcategoryDAO, times(1)).deleteAll(requestSubCats);
		verify(permissionRequestDAO, times(1)).save(request);
		verify(permissionRequestSubcategoryService, times(1)).createRequestSubcategoriesFromFormData(eq(categories), eq(request), anyMap());
	}
	
	@Test
	public void testRejectRequest_forInvalidId() {
		int requestId = 21;
		String comment = "TEST";
		given(permissionRequestDAO.findById(requestId)).willReturn(Optional.empty());
		
		PermissionRequestEntity result = sut.rejectRequest(requestId, user, comment);
		assertNull(result);
		
		verify(permissionRequestDAO, times(1)).findById(requestId);
		verify(permissionRequestDAO, never()).save(any());
		verifyNoInteractions(auditLogDAO, user);
	}
	
	@Test
	public void testRejectRequest() {
		int requestId = 42;
		String comment = "TEST";
		long timestamp = 666;
		String username = "Admin, User";
		String targetUser = "user@test";
		String dummyAuditLogDescriptionDetails = " changes for user from user at time xy with products.";
		String expectedAuditLogDescription = "Rejected " + dummyAuditLogDescriptionDetails;
		PermissionRequestEntity request = mock(PermissionRequestEntity.class);
		
		given(permissionRequestDAO.findById(requestId)).willReturn(Optional.of(request));
		given(supportUtils.currentTimeMillis()).willReturn(timestamp);
		given(supportUtils.formatAuditLogDetails(request)).willReturn(dummyAuditLogDescriptionDetails);
		given(user.getName()).willReturn(username);
		given(request.getRequestFor()).willReturn(targetUser);
		
		PermissionRequestEntity result = sut.rejectRequest(requestId, user, comment);
		assertNotNull(result);
		assertEquals(request, result);
		
		verify(permissionRequestDAO, times(1)).findById(requestId);
		verify(request, times(1)).setResolution(RequestStatus.REJECTED.name());
		verify(request, times(1)).setComment(comment);
		verify(permissionRequestDAO, times(1)).save(request);
		verify(user, times(1)).getName();
		verify(auditLogDAO, times(1)).save(auditLogCaptor.capture());
		
		AuditLogEntity auditLog = auditLogCaptor.getValue();
		assertNotNull(auditLog);
		assertEquals(timestamp, auditLog.getEntryTimestamp());
		assertEquals(RequestStatus.REJECTED.name(), auditLog.getAction());
		assertEquals(username, auditLog.getUser());
		assertEquals(targetUser, auditLog.getTargetUser());
		assertEquals(expectedAuditLogDescription, auditLog.getDescription());
		assertEquals(comment, auditLog.getComment());
	}
	
	@Test
	public void testSendRejectRequestNotification_forExistingUser() {
		UserEntity userEntity = mock(UserEntity.class);
		PermissionRequestEntity request = new PermissionRequestEntity();
		request.setRequestFor("pinky@acme.narf");
		request.setRequestFrom("narf@zort.poit");
		request.setComment("Au Junge!");
		String dummySubject = "Test";
		String dummyMessage = "Notification";
		Locale userLocale = Locale.GERMAN;
		
		given(userDAO.findUserByMail(request.getRequestFrom())).willReturn(userEntity);
		given(userEntity.getUserLocale()).willReturn(userLocale);
		given(messageSource.getMessage("notification.request.rejected.subject", new String[]{request.getRequestFor()}, userLocale)).willReturn(dummySubject);
		given(messageSource.getMessage("notification.request.rejected.text", new String[]{request.getRequestFor(), request.getComment()}, userLocale)).willReturn(dummyMessage);
		
		sut.sendRejectRequestNotification(request);
		
		verify(userDAO, times(1)).findUserByMail(request.getRequestFrom());
		verify(userEntity, atLeastOnce()).getUserLocale();
		verify(messageSource, times(1)).getMessage("notification.request.rejected.subject", new String[]{request.getRequestFor()}, userLocale);
		verify(messageSource, times(1)).getMessage("notification.request.rejected.text", new String[]{request.getRequestFor(), request.getComment()}, userLocale);
		verify(notificationService, times(1)).sendNotificationAsync(dummySubject, dummyMessage, request.getRequestFrom());
	}
	
	@Test
	public void testSendRejectRequestNotification_forUnknownUser() {
		PermissionRequestEntity request = new PermissionRequestEntity();
		request.setRequestFor("pinky@acme.narf");
		request.setRequestFrom("narf@zort.poit");
		request.setComment("Au Junge!");
		String dummySubject = "Test";
		String dummyMessage = "Notification";
		
		given(userDAO.findUserByMail(request.getRequestFrom())).willReturn(null);
		given(messageSource.getMessage("notification.request.rejected.subject", new String[]{request.getRequestFor()}, Locale.ENGLISH)).willReturn(dummySubject);
		given(messageSource.getMessage("notification.request.rejected.text", new String[]{request.getRequestFor(), request.getComment()}, Locale.ENGLISH)).willReturn(dummyMessage);
		
		sut.sendRejectRequestNotification(request);
		
		verify(userDAO, times(1)).findUserByMail(request.getRequestFrom());
		verify(messageSource, times(1)).getMessage("notification.request.rejected.subject", new String[]{request.getRequestFor()}, Locale.ENGLISH);
		verify(messageSource, times(1)).getMessage("notification.request.rejected.text", new String[]{request.getRequestFor(), request.getComment()}, Locale.ENGLISH);
		verify(notificationService, times(1)).sendNotificationAsync(dummySubject, dummyMessage, request.getRequestFrom());
	}
	
	@Test
	public void testAcceptRequest_forInvalidId() {
		int requestId = 21;
		given(permissionRequestDAO.findById(requestId)).willReturn(Optional.empty());
		
		PermissionRequestEntity result = sut.acceptRequest(requestId, user);
		assertNull(result);
		
		verify(permissionRequestDAO, times(1)).findById(requestId);
		verify(permissionRequestDAO, never()).save(any());
		verifyNoInteractions(auditLogDAO, user);
	}
	
	@Test
	public void testAcceptRequest() {
		int requestId = 42;
		long timestamp = 666;
		String username = "Admin, User";
		String targetUser = "test@user";
		String dummyAuditLogDescriptionDetails = " changes for user from user at time xy with products.";
		String expectedAuditLogDescription = "Accepted " + dummyAuditLogDescriptionDetails;
		PermissionRequestEntity request = mock(PermissionRequestEntity.class);
		
		given(permissionRequestDAO.findById(requestId)).willReturn(Optional.of(request));
		given(supportUtils.currentTimeMillis()).willReturn(timestamp);
		given(supportUtils.formatAuditLogDetails(request)).willReturn(dummyAuditLogDescriptionDetails);
		given(user.getName()).willReturn(username);
		given(request.getRequestFor()).willReturn(targetUser);
		
		PermissionRequestEntity result = sut.acceptRequest(requestId, user);
		assertNotNull(result);
		
		verify(permissionRequestDAO, times(1)).findById(requestId);
		verify(request, times(1)).setResolution(RequestStatus.ACCEPTED.name());
		verify(permissionRequestDAO, times(1)).save(request);
		verify(user, times(1)).getName();
		verify(auditLogDAO, times(1)).save(auditLogCaptor.capture());
		
		AuditLogEntity auditLog = auditLogCaptor.getValue();
		assertNotNull(auditLog);
		assertEquals(timestamp, auditLog.getEntryTimestamp());
		assertEquals(RequestStatus.ACCEPTED.name(), auditLog.getAction());
		assertEquals(username, auditLog.getUser());
		assertEquals(targetUser, auditLog.getTargetUser());
		assertEquals(expectedAuditLogDescription, auditLog.getDescription());
	}
	
	@Test
	public void testSendAcceptRequestNotification_forExistingUser() {
		UserEntity userEntity = mock(UserEntity.class);
		PermissionRequestEntity request = new PermissionRequestEntity();
		request.setRequestFor("pinky@acme.narf");
		request.setRequestFrom("narf@zort.poit");
		String dummySubject = "Test";
		String dummyMessage = "Notification";
		Locale userLocale = Locale.GERMAN;
		
		given(userDAO.findUserByMail(request.getRequestFrom())).willReturn(userEntity);
		given(userEntity.getUserLocale()).willReturn(userLocale);
		given(messageSource.getMessage("notification.request.accepted.subject", new String[]{request.getRequestFor()}, userLocale)).willReturn(dummySubject);
		given(messageSource.getMessage("notification.request.accepted.text", new String[]{request.getRequestFor()}, userLocale)).willReturn(dummyMessage);
		
		sut.sendAcceptRequestNotification(request);
		
		verify(userDAO, times(1)).findUserByMail(request.getRequestFrom());
		verify(userEntity, atLeastOnce()).getUserLocale();
		verify(messageSource, times(1)).getMessage("notification.request.accepted.subject", new String[]{request.getRequestFor()}, userLocale);
		verify(messageSource, times(1)).getMessage("notification.request.accepted.text", new String[]{request.getRequestFor()}, userLocale);
		verify(notificationService, times(1)).sendNotificationAsync(dummySubject, dummyMessage, request.getRequestFrom());
	}
	
	@Test
	public void testSendAcceptRequestNotification_forUnkownUser() {
		PermissionRequestEntity request = new PermissionRequestEntity();
		request.setRequestFor("pinky@acme.narf");
		request.setRequestFrom("narf@zort.poit");
		String dummySubject = "Test";
		String dummyMessage = "Notification";
		
		given(userDAO.findUserByMail(request.getRequestFrom())).willReturn(null);
		given(messageSource.getMessage("notification.request.accepted.subject", new String[]{request.getRequestFor()}, Locale.ENGLISH)).willReturn(dummySubject);
		given(messageSource.getMessage("notification.request.accepted.text", new String[]{request.getRequestFor()}, Locale.ENGLISH)).willReturn(dummyMessage);
		
		sut.sendAcceptRequestNotification(request);
		
		verify(userDAO, times(1)).findUserByMail(request.getRequestFrom());
		verify(messageSource, times(1)).getMessage("notification.request.accepted.subject", new String[]{request.getRequestFor()}, Locale.ENGLISH);
		verify(messageSource, times(1)).getMessage("notification.request.accepted.text", new String[]{request.getRequestFor()}, Locale.ENGLISH);
		verify(notificationService, times(1)).sendNotificationAsync(dummySubject, dummyMessage, request.getRequestFrom());
	}
}

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

import dev.hmg.apm.config.AppConfig;
import dev.hmg.apm.db.PermissionRequestDAO;
import dev.hmg.apm.db.entity.PermissionRequestEntity;
import dev.hmg.apm.service.NotificationService;
import dev.hmg.apm.service.PermissionRequestService;
import dev.hmg.apm.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PermissionRequestControllerTest extends AbstractControllerTest {
	
	private PermissionRequestController sut;
	
	@Mock
	private AppConfig appConfig;
	@Mock
	private Model model;
	@Mock
	private OAuth2AuthenticationToken auth;
	@Mock
	private OAuth2User user;
	@Mock
	private Locale dummyLocale;
	@Mock
	private PermissionRequestService permissionRequestService;
	@Mock
	private PermissionRequestDAO permissionRequestDAO;
	@Mock
	private MessageSource messageSource;
	@Mock
	private NotificationService notificationService;
	@Mock
	private UserService userService;
	
	private String dummyUID = "T.User@company.domain";
	private String expectedUID = "t.user_company.domain";
	private String[] products = new String[] {"", ""};
	private String[] categories = new String[] {""};
	private String dummyErrorMessage = "TEST";
	private Map<String,String> allParams = Collections.emptyMap();
	
	@Before
	public void setUp() {
		sut = new PermissionRequestController();
		sut.setAppConfig(appConfig);
		sut.setProductDAO(productDAO);
		sut.setPermissionRequestService(permissionRequestService);
		sut.setMessageSource(messageSource);
		sut.setPermissionRequestDAO(permissionRequestDAO);
		sut.setSupportUtils(supportUtils);
		sut.setNotificationService(notificationService);
		sut.setUserService(userService);
		
		given(auth.getPrincipal()).willReturn(user);
	}
	
	@Test
	public void testIndexPage() {
		given(supportUtils.isAdmin(user)).willReturn(true);
		given(productDAO.findAll()).willReturn(Collections.emptyList());
		
		String result = sut.indexPage(model, auth);
		assertNotNull(result);
		assertEquals("indexPage", result);
		
		verify(auth, atLeastOnce()).getPrincipal();
		verify(model, times(1)).addAttribute("user", user);
		verify(model, times(1)).addAttribute("isAdmin", true);
		verify(model, times(1)).addAttribute(eq("products"), anyList());
		verify(model, times(1)).addAttribute("devMode", false);
		verify(appConfig, times(1)).isDevMode();
		verify(productDAO, times(1)).findAll();
		verify(supportUtils, times(1)).isAdmin(user);
	}
	
	@Test
	public void testRequestPermissions_forInvalidMail() {
		String targetUserType = "other";
		String targetUserMail = "test.user@ey-hallo.localdomain";
		
		given(permissionRequestService.determineTargetEMail(targetUserType, targetUserMail, user)).willReturn(targetUserMail);
		given(permissionRequestService.isValidEMail(anyString())).willReturn(false);
		given(messageSource.getMessage("request.error.mail.invalid", null, dummyLocale)).willReturn(dummyErrorMessage);
		given(supportUtils.createSubcategoryMappingWithoutAccessMode(any())).willReturn(dummyCategoriesNoAM);
		
		String result = sut.requestPermissions(targetUserType, targetUserMail, products, categories, allParams, dummyLocale, redirectAttributes, auth);
		assertNotNull(result);
		assertEquals("redirect:/", result);
		
		verify(permissionRequestService, times(1)).determineTargetEMail(targetUserType, targetUserMail, user);
		verify(permissionRequestService, times(1)).isValidEMail(targetUserMail);
		verify(messageSource, times(1)).getMessage("request.error.mail.invalid", null, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", dummyErrorMessage);
		verifyParametersAsFlashAttributes(targetUserType, targetUserMail, products, categories);
		verifyNoInteractions(permissionRequestDAO, notificationService, userService);
		verify(permissionRequestService, never()).createPermissionRequest(any(), anyString(), any(), any(), any());
	}
	
	@Test
	public void testRequestPermissions_forInvalidPrivilegedMail() {
		String targetUserType = "other";
		String targetUserMail = "test.user@ey-hallo.localdomain";
		
		given(permissionRequestService.determineTargetEMail(targetUserType, targetUserMail, user)).willReturn(targetUserMail);
		given(permissionRequestService.isValidEMail(anyString())).willReturn(false);
		given(permissionRequestService.isValidPrivilegedMail(anyString(), any(OAuth2User.class))).willReturn(false);
		given(messageSource.getMessage("request.error.mail.invalid", null, dummyLocale)).willReturn(dummyErrorMessage);
		given(supportUtils.createSubcategoryMappingWithoutAccessMode(any())).willReturn(dummyCategoriesNoAM);
		
		String result = sut.requestPermissions(targetUserType, targetUserMail, products, categories, allParams, dummyLocale, redirectAttributes, auth);
		assertNotNull(result);
		assertEquals("redirect:/", result);
		
		verify(permissionRequestService, times(1)).determineTargetEMail(targetUserType, targetUserMail, user);
		verify(permissionRequestService, times(1)).isValidEMail(targetUserMail);
		verify(permissionRequestService, times(1)).isValidPrivilegedMail(targetUserMail, user);
		verify(messageSource, times(1)).getMessage("request.error.mail.invalid", null, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", dummyErrorMessage);
		verifyParametersAsFlashAttributes(targetUserType, targetUserMail, products, categories);
		verifyNoInteractions(permissionRequestDAO, notificationService, userService);
		verify(permissionRequestService, never()).createPermissionRequest(any(), anyString(), any(), any(), any());
	}
	
	@Test
	public void testRequestPermissions_forExistingRequest() {
		String targetUserType = "other";
		String targetUserMail = "test.user@company.tld";
		
		given(permissionRequestService.determineTargetEMail(targetUserType, targetUserMail, user)).willReturn(targetUserMail);
		given(permissionRequestService.isValidEMail(anyString())).willReturn(true);
		given(permissionRequestDAO.existsOpenPermissionRequestForUser(targetUserMail)).willReturn(true);
		given(messageSource.getMessage("request.error.open.request.exists", null, dummyLocale)).willReturn(dummyErrorMessage);
		given(supportUtils.createSubcategoryMappingWithoutAccessMode(any())).willReturn(dummyCategoriesNoAM);
		
		String result = sut.requestPermissions(targetUserType, targetUserMail, products, categories, allParams, dummyLocale, redirectAttributes, auth);
		assertNotNull(result);
		assertEquals("redirect:/", result);
		
		verify(permissionRequestService, times(1)).determineTargetEMail(targetUserType, targetUserMail, user);
		verify(permissionRequestService, times(1)).isValidEMail(targetUserMail);
		verify(permissionRequestDAO, times(1)).existsOpenPermissionRequestForUser(targetUserMail);
		verify(messageSource, times(1)).getMessage("request.error.open.request.exists", null, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", dummyErrorMessage);
		verifyParametersAsFlashAttributes(targetUserType, targetUserMail, products, categories);
		verify(permissionRequestService, never()).createPermissionRequest(any(), anyString(), any(), any(), any());
		verifyNoInteractions(notificationService, userService);
	}
	
	@Test
	public void testRequestPermissions() {
		String targetUserType = "other";
		String targetUserMail = "test.user@company.tld";
		String requestingUserMail = "req.user@company.tld";
		String dummyMessage = "TEST CREATED";
		String dummyNotificationSubject = "permission request subject";
		String dummyNotificationText = "permission requested text";

		given(user.getAttribute("email")).willReturn(requestingUserMail);
		given(permissionRequestService.determineTargetEMail(targetUserType, targetUserMail, user)).willReturn(targetUserMail);
		given(permissionRequestService.isValidEMail(anyString())).willReturn(true);
		given(permissionRequestDAO.existsOpenPermissionRequestForUser(targetUserMail)).willReturn(false);
		given(messageSource.getMessage("request.success.created", new Object[]{targetUserMail}, dummyLocale)).willReturn(dummyMessage);
		given(messageSource.getMessage("notification.request.created.subject", new Object[]{targetUserMail}, Locale.GERMAN)).willReturn(dummyNotificationSubject);
		given(messageSource.getMessage("notification.request.created.text", new Object[]{requestingUserMail, targetUserMail}, Locale.GERMAN)).willReturn(dummyNotificationText);
		
		String result = sut.requestPermissions(targetUserType, targetUserMail, products, categories, allParams, dummyLocale, redirectAttributes, auth);
		assertNotNull(result);
		assertEquals("redirect:/", result);
		
		verify(permissionRequestService, times(1)).determineTargetEMail(targetUserType, targetUserMail, user);
		verify(permissionRequestService, times(1)).isValidEMail(targetUserMail);
		verify(permissionRequestDAO, times(1)).existsOpenPermissionRequestForUser(targetUserMail);
		verify(messageSource, times(1)).getMessage("request.success.created", new Object[]{targetUserMail}, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("message", dummyMessage);
		verify(permissionRequestService, times(1)).createPermissionRequest(user, targetUserMail, products, categories, allParams);
		verify(messageSource, times(1)).getMessage("notification.request.created.subject", new Object[]{targetUserMail}, Locale.GERMAN);
		verify(messageSource, times(1)).getMessage("notification.request.created.text", new Object[]{requestingUserMail, targetUserMail}, Locale.GERMAN);
		verify(notificationService, times(1)).sendAdminNotificationAsync(dummyNotificationSubject, dummyNotificationText);
		verify(userService, times(1)).findOrCreateUser(user, dummyLocale);
	}
	
	@Test
	public void testUserRequests() {
		String userMail = "dummyMail";
		given(user.getAttribute("email")).willReturn(userMail);
		given(supportUtils.isAdmin(user)).willReturn(true);
		given(permissionRequestDAO.findRequestsForUser(userMail)).willReturn(Collections.emptyList());
		
		String result = sut.userRequests(model, auth);
		assertNotNull(result);
		assertEquals("myRequests", result);
		
		verify(auth, atLeastOnce()).getPrincipal();
		verify(user, times(1)).getAttribute("email");
		verify(model, times(1)).addAttribute("user", user);
		verify(model, times(1)).addAttribute("isAdmin", true);
		verify(model, times(1)).addAttribute(eq("requests"), anyList());
		verify(supportUtils, times(1)).isAdmin(user);
		verify(permissionRequestDAO, times(1)).findRequestsForUser(userMail);
	}
	
	@Test
	public void testEditForm_forInvalidRequestId() {
		int invalidRequestId = -1;
		String dummyErrorMessage = "Computer says: NO!";
		
		given(supportUtils.isUserCanEditRequest(eq(user), any(Optional.class))).willReturn(false);
		given(permissionRequestDAO.findById(anyInt())).willReturn(Optional.empty());
		given(messageSource.getMessage("request.edit.error.requestid.invalid", null, dummyLocale)).willReturn(dummyErrorMessage);
		
		String result = sut.editForm(invalidRequestId, model, auth, dummyLocale, redirectAttributes);
		assertNotNull(result);
		assertEquals("redirect:/", result);
		
		verify(permissionRequestDAO, times(1)).findById(invalidRequestId);
		verify(messageSource, times(1)).getMessage("request.edit.error.requestid.invalid", null, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", dummyErrorMessage);
		verify(supportUtils, times(1)).isUserCanEditRequest(user, Optional.empty());
	}

	@Test
	public void testEditForm() {
		int dummyRequestId = 42;
		String expectedTargetUserType = "other";
		PermissionRequestEntity dummyRequest = createDummyPermissionRequestEntity();
		List<String> expectedSelectedProducts = Arrays.asList("product-1", "product-2");
		Map<String,String> expectedSelectedSubCats = Map.of("product-1_cat1", "product-1_cat1_read", "product-1_cat2", 
				"product-1_cat2_write", "product-2_cat1", "product-2_cat1_admin");
		Map<String,String> expectedAllParams = Map.of("product-1_cat1_read", "product-1_cat1_read", 
				"product-1_cat2_write", "product-1_cat2_write", 
				"product-2_cat1_admin", "product-2_cat1_admin",
				"product-2_CUSTOM_comment", "Dummy Comment NARF", "cancellationDate", "1970-01-01");
		
		given(supportUtils.isUserCanEditRequest(eq(user), any(Optional.class))).willReturn(true);
		given(permissionRequestDAO.findById(anyInt())).willReturn(Optional.of(dummyRequest));
		given(supportUtils.isAdmin(user)).willReturn(true);
		given(productDAO.findAll()).willReturn(Collections.emptyList());
		
		String result = sut.editForm(dummyRequestId, model, auth, dummyLocale, redirectAttributes);
		assertNotNull(result);
		assertEquals("editPage", result);
		
		verify(permissionRequestDAO, times(1)).findById(dummyRequestId);
		verify(auth, atLeastOnce()).getPrincipal();
		verify(model, times(1)).addAttribute("user", user);
		verify(model, times(1)).addAttribute("isAdmin", true);
		verify(model, times(1)).addAttribute(eq("products"), anyList());
		verify(model, times(1)).addAttribute("devMode", false);
		verify(model, times(1)).addAttribute("requestId", dummyRequestId);
		verify(model, times(1)).addAttribute("targetUserType", expectedTargetUserType);
		verify(model, times(1)).addAttribute("targetUserMail", dummyRequest.getRequestFor());
		verify(model, times(1)).addAttribute("selectedProducts", expectedSelectedProducts);
		verify(model, times(1)).addAttribute("selectedProductCategories", expectedSelectedSubCats);
		verify(model, times(1)).addAttribute("allParams", expectedAllParams);
		verify(appConfig, times(1)).isDevMode();
		verify(productDAO, times(1)).findAll();
		verify(supportUtils, times(1)).isAdmin(user);
		verify(supportUtils, times(1)).isUserCanEditRequest(eq(user), any(Optional.class));
	}
	
	@Test
	public void testEditRequest_forInvalidRequest() {
		int invalidRequestId = -1;
		String targetUserType = "other";
		String targetUserMail = "test.user@company.tld";
		String dummyErrorMessage = "Computer says: NO!";
		
		given(supportUtils.isUserCanEditRequest(eq(user), any(Optional.class))).willReturn(false);
		given(permissionRequestDAO.findById(anyInt())).willReturn(Optional.empty());
		given(messageSource.getMessage("request.edit.error.requestid.invalid", null, dummyLocale)).willReturn(dummyErrorMessage);
		
		String result = sut.editRequest(invalidRequestId, targetUserType, targetUserMail, products, categories, allParams, dummyLocale, redirectAttributes, auth);
		assertNotNull(result);
		assertEquals("redirect:/", result);
		
		verify(permissionRequestDAO, times(1)).findById(invalidRequestId);
		verify(messageSource, times(1)).getMessage("request.edit.error.requestid.invalid", null, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", dummyErrorMessage);
		verify(supportUtils, times(1)).isUserCanEditRequest(user, Optional.empty());
	}
	
	@Test
	public void testEditRequest_forInvalidMail() {
		int requestId = 21;
		String targetUserType = "other";
		String targetUserMail = "test.user@ey-hallo.localdomain";
		PermissionRequestEntity dummyRequest = createDummyPermissionRequestEntity();
		
		given(supportUtils.isUserCanEditRequest(eq(user), any(Optional.class))).willReturn(true);
		given(permissionRequestDAO.findById(anyInt())).willReturn(Optional.of(dummyRequest));
		given(permissionRequestService.determineTargetEMail(targetUserType, targetUserMail, user)).willReturn(targetUserMail);
		given(permissionRequestService.isValidEMail(anyString())).willReturn(false);
		given(messageSource.getMessage("request.error.mail.invalid", null, dummyLocale)).willReturn(dummyErrorMessage);
		given(supportUtils.createSubcategoryMappingWithoutAccessMode(any())).willReturn(dummyCategoriesNoAM);
		
		String result = sut.editRequest(requestId, targetUserType, targetUserMail, products, categories, allParams, dummyLocale, redirectAttributes, auth);
		assertNotNull(result);
		assertEquals("redirect:/edit?requestId=21", result);
		
		verify(permissionRequestDAO, times(1)).findById(requestId);
		verify(permissionRequestService, times(1)).determineTargetEMail(targetUserType, targetUserMail, user);
		verify(permissionRequestService, times(1)).isValidEMail(targetUserMail);
		verify(messageSource, times(1)).getMessage("request.error.mail.invalid", null, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", dummyErrorMessage);
		verifyParametersAsFlashAttributes(targetUserType, targetUserMail, products, categories);
		verifyNoInteractions(notificationService, userService);
		verify(permissionRequestService, never()).createPermissionRequest(any(), anyString(), any(), any(), any());
		verify(permissionRequestService, never()).editPermissionRequest(any(), anyString(), any(), any(), any());
		verify(supportUtils, times(1)).isUserCanEditRequest(eq(user), any(Optional.class));
	}
	
	@Test
	public void testEditRequest() {
		int requestId = 21;
		String targetUserType = "other";
		String targetUserMail = "test.user@company.tld";
		String dummyMessage = "TEST CREATED";
		PermissionRequestEntity dummyRequest = createDummyPermissionRequestEntity();
		
		given(supportUtils.isUserCanEditRequest(eq(user), any(Optional.class))).willReturn(true);
		given(permissionRequestDAO.findById(anyInt())).willReturn(Optional.of(dummyRequest));
		given(permissionRequestService.determineTargetEMail(targetUserType, targetUserMail, user)).willReturn(targetUserMail);
		given(permissionRequestService.isValidEMail(anyString())).willReturn(true);
		given(messageSource.getMessage("request.success.edited", new Object[]{targetUserMail}, dummyLocale)).willReturn(dummyMessage);
		
		String result = sut.editRequest(requestId, targetUserType, targetUserMail, products, categories, allParams, dummyLocale, redirectAttributes, auth);
		assertNotNull(result);
		assertEquals("redirect:/myRequests", result);
		
		verify(permissionRequestDAO, times(1)).findById(requestId);
		verify(permissionRequestService, times(1)).determineTargetEMail(targetUserType, targetUserMail, user);
		verify(permissionRequestService, times(1)).isValidEMail(targetUserMail);
		verify(messageSource, times(1)).getMessage("request.success.edited", new Object[]{targetUserMail}, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("message", dummyMessage);
		verify(supportUtils, times(1)).isUserCanEditRequest(eq(user), any(Optional.class));
		verify(permissionRequestService, times(1)).editPermissionRequest(dummyRequest, targetUserMail, products, categories, allParams);
	}
}

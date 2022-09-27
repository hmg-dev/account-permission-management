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
import dev.hmg.apm.db.AuditLogDAO;
import dev.hmg.apm.db.PermissionRequestDAO;
import dev.hmg.apm.db.UserDetailsDAO;
import dev.hmg.apm.db.UserProductAssignmentDAO;
import dev.hmg.apm.db.entity.AuditLogEntity;
import dev.hmg.apm.db.entity.PermissionRequestEntity;
import dev.hmg.apm.db.entity.UserDetailsEntity;
import dev.hmg.apm.db.entity.UserProductAssignment;
import dev.hmg.apm.model.PaginationData;
import dev.hmg.apm.service.AdminHelperService;
import dev.hmg.apm.service.PaginationService;
import dev.hmg.apm.service.PermissionRequestService;
import dev.hmg.apm.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AdminUIControllerTest extends AbstractControllerTest {
	private AdminUIController sut;
	
	@Mock
	private OAuth2AuthenticationToken auth;
	@Mock
	private Model model;
	@Mock
	private OAuth2User user;
	@Mock
	private Locale dummyLocale;
	@Mock
	private AppConfig appConfig;
	@Mock
	private AuditLogDAO auditLogDAO;
	@Mock
	private MessageSource messageSource;
	@Mock
	private PermissionRequestDAO permissionRequestDAO;
	@Mock
	private PermissionRequestService permissionRequestService;
	@Mock
	private UserDetailsDAO userDAO;
	@Mock
	private UserService userService;
	@Mock
	private AdminHelperService adminHelperService;
	@Mock
	private UserProductAssignmentDAO userProductAssignmentDAO;
	@Mock
	private PaginationService paginationService;
	@Captor
	private ArgumentCaptor<Pageable> pageableCaptor;
	
	private String[] products = new String[] {"", ""};
	private String[] categories = new String[] {""};
	private String dummyErrorMessage = "TEST";
	private Map<String,String> allParams = Collections.emptyMap();
	
	@Before
	public void init() {
		sut = new AdminUIController();
		sut.setAppConfig(appConfig);
		sut.setAuditLogDAO(auditLogDAO);
		sut.setMessageSource(messageSource);
		sut.setPermissionRequestDAO(permissionRequestDAO);
		sut.setPermissionRequestService(permissionRequestService);
		sut.setUserDAO(userDAO);
		sut.setAdminHelperService(adminHelperService);
		sut.setUserProductAssignmentDAO(userProductAssignmentDAO);
		sut.setPaginationService(paginationService);
		sut.setUserService(userService);
		sut.setSupportUtils(supportUtils);
		sut.setProductDAO(productDAO);
		
		given(auth.getPrincipal()).willReturn(user);
	}
	
	@Test
	public void testAdminPage() {
		List<PermissionRequestEntity> dummyRequests = Collections.emptyList();
		
		given(permissionRequestDAO.findOpenPermissionRequests()).willReturn(dummyRequests);
		
		String result = sut.adminPage(model, auth);
		assertNotNull(result);
		assertEquals("adminPage", result);
		
		verify(permissionRequestDAO, times(1)).findOpenPermissionRequests();
		verify(auth, atLeastOnce()).getPrincipal();
		verify(model, times(1)).addAttribute("user", user);
		verify(model, times(1)).addAttribute("requests", dummyRequests);
	}
	
	@Test
	public void testRejectRequest_forException() {
		int requestId = 42;
		String reason = "NARF";
		String dummyErrorMessage = "TEST ERROR";
		given(messageSource.getMessage(eq("admin.reject.processing_error"), any(), eq(dummyLocale))).willReturn(dummyErrorMessage);
		doThrow(new IllegalStateException("TEST")).when(permissionRequestService).rejectRequest(anyInt(), any(), any());
		
		String result = sut.rejectRequest(requestId, reason, redirectAttributes, auth, dummyLocale);
		assertEquals("redirect:/admin", result);
		
		verify(messageSource, times(1)).getMessage("admin.reject.processing_error", new Object[]{"TEST"}, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", dummyErrorMessage);
		verify(permissionRequestService, times(1)).rejectRequest(anyInt(), any(), any());
		verify(permissionRequestService, never()).sendRejectRequestNotification(any());
	}
	
	@Test
	public void testRejectRequest() {
		int requestId = 42;
		String reason = "NARF";
		String dummyMessage = "Success!";
		PermissionRequestEntity request = mock(PermissionRequestEntity.class);
		given(messageSource.getMessage(eq("admin.reject.success"), any(), eq(dummyLocale))).willReturn(dummyMessage);
		given(permissionRequestService.rejectRequest(requestId, user, reason)).willReturn(request);
		
		String result = sut.rejectRequest(requestId, reason, redirectAttributes, auth, dummyLocale);
		assertEquals("redirect:/admin", result);
		
		verify(messageSource, times(1)).getMessage("admin.reject.success", null, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("message", dummyMessage);
		verify(permissionRequestService, times(1)).rejectRequest(requestId, user, reason);
		verify(permissionRequestService, times(1)).sendRejectRequestNotification(request);
	}
	
	@Test
	public void testAuditLog() {
		AuditLogEntity al1 = mock(AuditLogEntity.class);
		AuditLogEntity al2 = mock(AuditLogEntity.class);
		List<AuditLogEntity> auditLogs = Arrays.asList(al1, al2);
		int totalAmount = 42;
		int page = 0;
		PaginationData paginationData = new PaginationData.Builder().setPageCount(21).setEntriesPerPage(2).setCurrentPage(page).build();
		Page<AuditLogEntity> resultPage = mock(Page.class);
		
		given(auditLogDAO.findTotalAmount()).willReturn(totalAmount);
		given(auditLogDAO.findAll(any(Pageable.class))).willReturn(resultPage);
		given(paginationService.determinePaginationData(totalAmount, page)).willReturn(paginationData);
		given(resultPage.getContent()).willReturn(auditLogs);
		
		String result = sut.auditLog(model, 0);
		assertEquals("auditLog", result);
		
		verify(auditLogDAO, times(1)).findTotalAmount();
		verify(auditLogDAO, times(1)).findAll(pageableCaptor.capture());
		verify(model, times(1)).addAttribute("auditLogEntries", auditLogs);
		verify(model, times(1)).addAttribute("paging", paginationData);
		verify(paginationService, times(1)).determinePaginationData(totalAmount, page);
		verify(resultPage, times(1)).getContent();
		
		Pageable pageable = pageableCaptor.getValue();
		assertNotNull(pageable);
		assertEquals(page, pageable.getPageNumber());
		assertEquals(2, pageable.getPageSize());
		assertNotNull(pageable.getSort());
	}
	
	@Test
	public void testAcceptRequest_forException() {
		int requestId = 42;
		String dummyErrorMessage = "TEST ERROR";
		given(messageSource.getMessage(eq("admin.accept.processing_error"), any(), eq(dummyLocale))).willReturn(dummyErrorMessage);
		doThrow(new IllegalStateException("TEST")).when(adminHelperService).acceptRequest(anyInt(), any());
		
		String result = sut.acceptRequest(requestId, redirectAttributes, auth, dummyLocale);
		assertEquals("redirect:/admin", result);
		
		verify(messageSource, times(1)).getMessage("admin.accept.processing_error", new Object[]{"TEST"}, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", dummyErrorMessage);
		verify(adminHelperService, times(1)).acceptRequest(anyInt(), any());
		verifyNoInteractions(permissionRequestService);
	}
	
	@Test
	public void testAcceptRequest() {
		int requestId = 42;
		String dummyMessage = "Success!";
		PermissionRequestEntity request = mock(PermissionRequestEntity.class);
		
		given(messageSource.getMessage(eq("admin.accept.success"), any(), eq(dummyLocale))).willReturn(dummyMessage);
		given(adminHelperService.acceptRequest(requestId, auth)).willReturn(request);
		
		String result = sut.acceptRequest(requestId, redirectAttributes, auth, dummyLocale);
		assertEquals("redirect:/admin", result);
		
		verify(messageSource, times(1)).getMessage("admin.accept.success", null, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("message", dummyMessage);
		verify(adminHelperService, times(1)).acceptRequest(anyInt(), any());
		verify(permissionRequestService, times(1)).sendAcceptRequestNotification(request);
	}
	
	@Test
	public void testUserPermissions() {
		int totalAmount = 42;
		int page = 0;
		List<UserDetailsEntity> users = Collections.emptyList();
		PaginationData paginationData = new PaginationData.Builder().setPageCount(21).setEntriesPerPage(2).setCurrentPage(page).build();
		Page<UserDetailsEntity> resultPage = mock(Page.class);
		
		given(userDAO.findTotalAmount()).willReturn(totalAmount);
		given(userDAO.findAll(any(Pageable.class))).willReturn(resultPage);
		given(paginationService.determinePaginationData(totalAmount, page)).willReturn(paginationData);
		given(resultPage.getContent()).willReturn(users);
		
		String result = sut.userPermissions(model, page);
		assertEquals("userPermissions", result);
		
		verify(userDAO, times(1)).findTotalAmount();
		verify(userDAO, times(1)).findAll(pageableCaptor.capture());
		verify(model, times(1)).addAttribute("users", users);
		verify(model, times(1)).addAttribute("paging", paginationData);
		verify(paginationService, times(1)).determinePaginationData(totalAmount, page);
		verify(resultPage, times(1)).getContent();
		
		Pageable pageable = pageableCaptor.getValue();
		assertNotNull(pageable);
		assertEquals(page, pageable.getPageNumber());
		assertEquals(2, pageable.getPageSize());
		assertNotNull(pageable.getSort());
	}
	
	@Test
	public void testSoonExpiringPermissions() {
		UserProductAssignment ua1 = mock(UserProductAssignment.class);
		List<UserProductAssignment> assignments = Collections.singletonList(ua1);
		int showDays = 14;
		
		given(appConfig.getShowSoonExpiringPermissionsInNextDays()).willReturn(showDays);
		given(userProductAssignmentDAO.findAssignmentsExpiringInDays(anyInt())).willReturn(assignments);
		
		String result = sut.soonExpiringPermissions(model);
		assertEquals("soonExpiringPermissions", result);
		
		verify(appConfig, times(1)).getShowSoonExpiringPermissionsInNextDays();
		verify(userProductAssignmentDAO, times(1)).findAssignmentsExpiringInDays(showDays);
		verify(model, times(1)).addAttribute("assignments", assignments);
		verify(model, times(1)).addAttribute("days", showDays);
	}
	
	@Test
	public void testRevokePermissions_forError() {
		String userMail = "test@narf.zort";
		String dummyMessage = "TEST";
		
		doThrow(new IllegalStateException("TEST")).when(userService).revokePermissions(anyString());
		given(messageSource.getMessage(eq("admin.user_permissions.revoke.error"), any(), eq(dummyLocale))).willReturn(dummyMessage);
		
		String result = sut.revokePermissions(userMail, redirectAttributes, dummyLocale);
		assertEquals("redirect:/userPermissions", result);
		
		verify(userService, times(1)).revokePermissions(userMail);
		verify(messageSource, times(1)).getMessage(anyString(), any(), eq(dummyLocale));
		verify(messageSource, never()).getMessage(eq("admin.user_permissions.revoke.success"), any(), eq(dummyLocale));
		verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", dummyMessage);
	}
	
	@Test
	public void testRevokePermissions_forSuccess() {
		String userMail = "test@narf.zort";
		String dummyMessage = "TEST";
		
		given(messageSource.getMessage(eq("admin.user_permissions.revoke.success"), any(), eq(dummyLocale))).willReturn(dummyMessage);
		
		String result = sut.revokePermissions(userMail, redirectAttributes, dummyLocale);
		assertEquals("redirect:/userPermissions", result);
		
		verify(userService, times(1)).revokePermissions(userMail);
		verify(messageSource, times(1)).getMessage(anyString(), any(), eq(dummyLocale));
		verify(messageSource, never()).getMessage(eq("admin.user_permissions.revoke.error"), any(), eq(dummyLocale));
		verify(redirectAttributes, times(1)).addFlashAttribute("message", dummyMessage);
	}
	
	@Test
	public void testEditForm_forInvalidRequestId() {
		int invalidRequestId = -1;
		String dummyErrorMessage = "Computer says: NO!";
		
		given(supportUtils.isAdminCanEditRequest(any(Optional.class))).willReturn(false);
		given(permissionRequestDAO.findById(anyInt())).willReturn(Optional.empty());
		given(messageSource.getMessage("request.edit.error.requestid.invalid", null, dummyLocale)).willReturn(dummyErrorMessage);
		
		String result = sut.editForm(invalidRequestId, model, auth, dummyLocale, redirectAttributes);
		assertNotNull(result);
		assertEquals("redirect:/admin", result);
		
		verify(permissionRequestDAO, times(1)).findById(invalidRequestId);
		verify(messageSource, times(1)).getMessage("request.edit.error.requestid.invalid", null, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", dummyErrorMessage);
		verify(supportUtils, times(1)).isAdminCanEditRequest(Optional.empty());
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
		
		given(supportUtils.isAdminCanEditRequest(any(Optional.class))).willReturn(true);
		given(permissionRequestDAO.findById(anyInt())).willReturn(Optional.of(dummyRequest));
		given(supportUtils.isAdmin(user)).willReturn(true);
		given(productDAO.findAll()).willReturn(Collections.emptyList());
		
		String result = sut.editForm(dummyRequestId, model, auth, dummyLocale, redirectAttributes);
		assertNotNull(result);
		assertEquals("adminEditPage", result);
		
		verify(permissionRequestDAO, times(1)).findById(dummyRequestId);
		verify(auth, atLeastOnce()).getPrincipal();
		verify(model, times(1)).addAttribute("user", user);
		verify(model, times(1)).addAttribute("isAdmin", true);
		verify(model, times(1)).addAttribute(eq("products"), anyList());
		verify(model, times(1)).addAttribute("devMode", false);
		verify(model, times(1)).addAttribute("requestId", dummyRequestId);
		verify(model, times(1)).addAttribute("targetUserType", expectedTargetUserType);
		verify(model, times(1)).addAttribute("targetUserMail", dummyRequest.getRequestFor());
		verify(model, times(1)).addAttribute("requestUserMail", dummyRequest.getRequestFrom());
		verify(model, times(1)).addAttribute("selectedProducts", expectedSelectedProducts);
		verify(model, times(1)).addAttribute("selectedProductCategories", expectedSelectedSubCats);
		verify(model, times(1)).addAttribute("allParams", expectedAllParams);
		verify(appConfig, times(1)).isDevMode();
		verify(productDAO, times(1)).findAll();
		verify(supportUtils, times(1)).isAdmin(user);
		verify(supportUtils, times(1)).isAdminCanEditRequest(any(Optional.class));
	}
	
	
	@Test
	public void testEditRequest_forInvalidRequest() {
		int invalidRequestId = -1;
		String targetUserType = "other";
		String targetUserMail = "test.user@company.tld";
		String requestUserMail = targetUserMail;
		String dummyErrorMessage = "Computer says: NO!";
		
		given(supportUtils.isAdminCanEditRequest(any(Optional.class))).willReturn(false);
		given(permissionRequestDAO.findById(anyInt())).willReturn(Optional.empty());
		given(messageSource.getMessage("request.edit.error.requestid.invalid", null, dummyLocale)).willReturn(dummyErrorMessage);
		
		String result = sut.editRequest(invalidRequestId, targetUserType, targetUserMail, requestUserMail, products, categories, allParams, dummyLocale, redirectAttributes, auth);
		assertNotNull(result);
		assertEquals("redirect:/admin", result);
		
		verify(permissionRequestDAO, times(1)).findById(invalidRequestId);
		verify(messageSource, times(1)).getMessage("request.edit.error.requestid.invalid", null, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", dummyErrorMessage);
		verify(supportUtils, times(1)).isAdminCanEditRequest(Optional.empty());
	}
	
	@Test
	public void testEditRequest_forInvalidMail() {
		int requestId = 21;
		String targetUserType = "other";
		String targetUserMail = "test.user@ey-hallo.localdomain";
		String requestUserMail = "test.user@company.tld";
		PermissionRequestEntity dummyRequest = createDummyPermissionRequestEntity();
		
		given(supportUtils.isAdminCanEditRequest(any(Optional.class))).willReturn(true);
		given(permissionRequestDAO.findById(anyInt())).willReturn(Optional.of(dummyRequest));
		given(permissionRequestService.determineTargetEMail(targetUserType, targetUserMail, requestUserMail)).willReturn(targetUserMail);
		given(permissionRequestService.isValidEMail(anyString())).willReturn(false);
		given(messageSource.getMessage("request.error.mail.invalid", null, dummyLocale)).willReturn(dummyErrorMessage);
		given(supportUtils.createSubcategoryMappingWithoutAccessMode(any())).willReturn(dummyCategoriesNoAM);
		
		String result = sut.editRequest(requestId, targetUserType, targetUserMail, requestUserMail, products, categories, allParams, dummyLocale, redirectAttributes, auth);
		assertNotNull(result);
		assertEquals("redirect:/adminEdit?requestId=21", result);
		
		verify(permissionRequestDAO, times(1)).findById(requestId);
		verify(permissionRequestService, times(1)).determineTargetEMail(targetUserType, targetUserMail, requestUserMail);
		verify(permissionRequestService, times(1)).isValidEMail(targetUserMail);
		verify(messageSource, times(1)).getMessage("request.error.mail.invalid", null, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", dummyErrorMessage);
		verifyParametersAsFlashAttributes(targetUserType, targetUserMail, products, categories);
		verifyNoInteractions(userService);
		verify(permissionRequestService, never()).createPermissionRequest(any(), anyString(), any(), any(), any());
		verify(permissionRequestService, never()).editPermissionRequest(any(), anyString(), any(), any(), any());
		verify(supportUtils, times(1)).isAdminCanEditRequest(any(Optional.class));
	}
	
	@Test
	public void testEditRequest() {
		int requestId = 21;
		String targetUserType = "other";
		String targetUserMail = "test.user@company.tld";
		String requestUserMail = "test.user@company.tld";
		String dummyMessage = "TEST CREATED";
		PermissionRequestEntity dummyRequest = createDummyPermissionRequestEntity();
		
		given(supportUtils.isAdminCanEditRequest(any(Optional.class))).willReturn(true);
		given(permissionRequestDAO.findById(anyInt())).willReturn(Optional.of(dummyRequest));
		given(permissionRequestService.determineTargetEMail(targetUserType, targetUserMail, requestUserMail)).willReturn(targetUserMail);
		given(permissionRequestService.isValidEMail(anyString())).willReturn(true);
		given(messageSource.getMessage("admin.request.edit.success", new Object[]{targetUserMail}, dummyLocale)).willReturn(dummyMessage);
		
		String result = sut.editRequest(requestId, targetUserType, targetUserMail, requestUserMail, products, categories, allParams, dummyLocale, redirectAttributes, auth);
		assertNotNull(result);
		assertEquals("redirect:/admin", result);
		
		verify(permissionRequestDAO, times(1)).findById(requestId);
		verify(permissionRequestService, times(1)).determineTargetEMail(targetUserType, targetUserMail, requestUserMail);
		verify(permissionRequestService, times(1)).isValidEMail(targetUserMail);
		verify(messageSource, times(1)).getMessage("admin.request.edit.success", new Object[]{targetUserMail}, dummyLocale);
		verify(redirectAttributes, times(1)).addFlashAttribute("message", dummyMessage);
		verify(supportUtils, times(1)).isAdminCanEditRequest(any(Optional.class));
		verify(permissionRequestService, times(1)).editPermissionRequest(dummyRequest, targetUserMail, products, categories, allParams);
	}
}

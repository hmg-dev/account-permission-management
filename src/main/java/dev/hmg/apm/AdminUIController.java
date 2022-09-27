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
import dev.hmg.apm.db.entity.*;
import dev.hmg.apm.model.PaginationData;
import dev.hmg.apm.service.AdminHelperService;
import dev.hmg.apm.service.PaginationService;
import dev.hmg.apm.service.PermissionRequestService;
import dev.hmg.apm.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class AdminUIController extends AbstractRequestController {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private AppConfig appConfig;
	@Autowired
	private AuditLogDAO auditLogDAO;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private PermissionRequestDAO permissionRequestDAO;
	@Autowired
	private PermissionRequestService permissionRequestService;
	@Autowired
	private UserDetailsDAO userDAO;
	@Autowired
	private AdminHelperService adminHelperService;
	@Autowired
	private UserProductAssignmentDAO userProductAssignmentDAO;
	@Autowired
	private PaginationService paginationService;
	@Autowired
	private UserService userService;
	
	@GetMapping("/admin")
	@PreAuthorize("hasRole('DevOps')")
	public String adminPage(final Model model, final OAuth2AuthenticationToken auth) {
		model.addAttribute("user", auth.getPrincipal());
		model.addAttribute("requests", permissionRequestDAO.findOpenPermissionRequests());
		
		return "adminPage";
	}
	
	@PostMapping("/rejectRequest")
	@PreAuthorize("hasRole('DevOps')")
	public String rejectRequest(@RequestParam("requestId") final int requestId, @RequestParam("reason") final String reason,
								final RedirectAttributes redirectAttributes, final OAuth2AuthenticationToken auth, final Locale locale) {
		try {
			PermissionRequestEntity request = permissionRequestService.rejectRequest(requestId, auth.getPrincipal(), reason);
			permissionRequestService.sendRejectRequestNotification(request);
		} catch (RuntimeException e) {
			log.error("Unable to reject request!", e);
			redirectAttributes.addFlashAttribute("errorMessage", 
					messageSource.getMessage("admin.reject.processing_error", new Object[]{e.getMessage()}, locale));
			return "redirect:/admin";
		}
		
		redirectAttributes.addFlashAttribute("message",
				messageSource.getMessage("admin.reject.success", null, locale));
		return "redirect:/admin";
	}
	
	@PostMapping("/acceptRequest")
	@PreAuthorize("hasRole('DevOps')")
	public String acceptRequest(@RequestParam("requestId")final int requestId, final RedirectAttributes redirectAttributes, 
								final OAuth2AuthenticationToken auth, final Locale locale) {
		try {
			PermissionRequestEntity request = adminHelperService.acceptRequest(requestId, auth);
			permissionRequestService.sendAcceptRequestNotification(request);
		} catch (RuntimeException e) {
			log.error("Unable to accept request!", e);
			redirectAttributes.addFlashAttribute("errorMessage",
					messageSource.getMessage("admin.accept.processing_error", new Object[]{e.getMessage()}, locale));
			return "redirect:/admin";
		}
		
		redirectAttributes.addFlashAttribute("message",
				messageSource.getMessage("admin.accept.success", null, locale));
		return "redirect:/admin";
	}
	
	@PostMapping("/revokePermissions")
	@PreAuthorize("hasRole('DevOps')")
	public String revokePermissions(@RequestParam("userMail") final String targetUser, final RedirectAttributes redirectAttributes, final Locale locale) {
		try {
			userService.revokePermissions(targetUser);
			
			String msg = messageSource.getMessage("admin.user_permissions.revoke.success", new Object[]{targetUser}, locale);
			redirectAttributes.addFlashAttribute("message", msg);
		} catch (RuntimeException e) {
			String msg = messageSource.getMessage("admin.user_permissions.revoke.error", new Object[]{targetUser}, locale);
			redirectAttributes.addFlashAttribute("errorMessage", msg);
		}
		
		return "redirect:/userPermissions";
	}
	
	@GetMapping("/auditLog")
	@PreAuthorize("hasRole('DevOps')")
	public String auditLog(final Model model, @RequestParam(value = "page", required = false, defaultValue = "0") final int page) {
		int totalEntries = auditLogDAO.findTotalAmount();
		PaginationData paginationData = paginationService.determinePaginationData(totalEntries, page);
		
		Page<AuditLogEntity> result = auditLogDAO.findAll(PageRequest.of(page, paginationData.getEntriesPerPage(), Sort.by(Sort.Direction.DESC, "entryTimestamp")));
		model.addAttribute("auditLogEntries", result.getContent());
		model.addAttribute("paging", paginationData);
		
		return "auditLog";
	}
	
	@GetMapping("/userPermissions")
	@PreAuthorize("hasRole('DevOps')")
	public String userPermissions(final Model model, @RequestParam(value = "page", required = false, defaultValue = "0") final int page) {
		int totalEntries = userDAO.findTotalAmount();
		PaginationData paginationData = paginationService.determinePaginationData(totalEntries, page);
		
		Page<UserDetailsEntity> result = userDAO.findAll(PageRequest.of(page, paginationData.getEntriesPerPage(), Sort.by("eMail")));
		model.addAttribute("users", result.getContent());
		model.addAttribute("paging", paginationData);
		
		return "userPermissions";
	}
	
	@GetMapping("/soonExpiringPermissions")
	@PreAuthorize("hasRole('DevOps')")
	public String soonExpiringPermissions(final Model model) {
		int nextDays = appConfig.getShowSoonExpiringPermissionsInNextDays();
		List<UserProductAssignment> assignments = userProductAssignmentDAO.findAssignmentsExpiringInDays(nextDays);
		
		model.addAttribute("assignments", assignments);
		model.addAttribute("days", nextDays);
		
		return "soonExpiringPermissions";
	}
	
	@GetMapping("/adminEdit")
	@PreAuthorize("hasRole('DevOps')")
	public String editForm(@RequestParam final int requestId, final Model model, final OAuth2AuthenticationToken auth, final Locale locale,
						   final RedirectAttributes redirectAttributes) {
		OAuth2User user = auth.getPrincipal();
		Optional<PermissionRequestEntity> possibleRequest = permissionRequestDAO.findById(requestId);
		if(!supportUtils.isAdminCanEditRequest(possibleRequest)) {
			redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("request.edit.error.requestid.invalid", null, locale));
			return "redirect:/admin";
		}
		
		PermissionRequestEntity request = possibleRequest.get();
		model.addAttribute("user", user);
		model.addAttribute("isAdmin", supportUtils.isAdmin(user));
		model.addAttribute("products", productDAO.findAll());
		model.addAttribute("devMode", appConfig.isDevMode());
		model.addAttribute("requestId", requestId);
		model.addAttribute("requestUserMail", request.getRequestFrom());
		
		if(!model.containsAttribute("allParams")) {
			model.addAttribute("targetUserType", StringUtils.equalsIgnoreCase(request.getRequestFor(), request.getRequestFrom()) ? "myself" : "other");
			model.addAttribute("targetUserMail", request.getRequestFor());
			model.addAttribute("selectedProducts", request.getProducts().stream().map(ProductEntity::getKey).collect(Collectors.toList()));
			model.addAttribute("selectedProductCategories", buildSelectedProductCategoriesFromRequest(request));
			Map<String, String> allParams = buildAllParamsFromRequest(request);
			if (request.getValidToTimestamp() != null) {
				allParams.put("cancellationDate", DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC).format(request.getValidToDateTime()));
			}
			model.addAttribute("allParams", allParams);
		}
		
		return "adminEditPage";
	}
	
	@PostMapping("/adminEditRequest")
	@PreAuthorize("hasRole('DevOps')")
	public String editRequest(@RequestParam final int requestId, @RequestParam(required = false) final String targetUserType,
							  @RequestParam(required = false) final String targetUserMail,
							  @RequestParam(required = false) final String requestUserMail,
							  @RequestParam(required = false, value="products[]") final String[] products,
							  @RequestParam(required = false, value="categories[]") final String[] categories,
							  @RequestParam Map<String,String> allParams,
							  final Locale locale,
							  final RedirectAttributes redirectAttributes,
							  final OAuth2AuthenticationToken auth) {
		Optional<PermissionRequestEntity> possibleRequest = permissionRequestDAO.findById(requestId);
		if(!supportUtils.isAdminCanEditRequest(possibleRequest)) {
			redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("request.edit.error.requestid.invalid", null, locale));
			return "redirect:/admin";
		}
		String targetMail = permissionRequestService.determineTargetEMail(targetUserType, targetUserMail, requestUserMail);
		if(!permissionRequestService.isValidEMail(targetMail) && !permissionRequestService.isValidPrivilegedMail(targetMail, auth.getPrincipal())) {
			log.debug("User entered invalid eMail.");
			redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("request.error.mail.invalid", null, locale));
			addRequestParamFlashAttributes(redirectAttributes, targetUserType, targetUserMail, products, categories, allParams);
			return "redirect:/adminEdit?requestId="+requestId;
		}
		
		permissionRequestService.editPermissionRequest(possibleRequest.get(), targetUserMail, products, categories, allParams);
		
		redirectAttributes.addFlashAttribute("message",
				messageSource.getMessage("admin.request.edit.success", new Object[]{targetMail}, locale));
		
		return "redirect:/admin";
	}
	
	public void setAppConfig(final AppConfig appConfig) {
		this.appConfig = appConfig;
	}
	
	public void setAdminHelperService(final AdminHelperService adminHelperService) {
		this.adminHelperService = adminHelperService;
	}
	
	public void setAuditLogDAO(final AuditLogDAO auditLogDAO) {
		this.auditLogDAO = auditLogDAO;
	}
	
	public void setMessageSource(final MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	public void setPermissionRequestDAO(final PermissionRequestDAO permissionRequestDAO) {
		this.permissionRequestDAO = permissionRequestDAO;
	}
	
	public void setPermissionRequestService(final PermissionRequestService permissionRequestService) {
		this.permissionRequestService = permissionRequestService;
	}
	
	public void setUserDAO(final UserDetailsDAO userDAO) {
		this.userDAO = userDAO;
	}
	
	public void setUserProductAssignmentDAO(final UserProductAssignmentDAO userProductAssignmentDAO) {
		this.userProductAssignmentDAO = userProductAssignmentDAO;
	}
	
	public void setPaginationService(final PaginationService paginationService) {
		this.paginationService = paginationService;
	}
	
	public void setUserService(final UserService userService) {
		this.userService = userService;
	}
}

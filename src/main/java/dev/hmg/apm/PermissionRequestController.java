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
import dev.hmg.apm.db.entity.ProductEntity;
import dev.hmg.apm.service.NotificationService;
import dev.hmg.apm.service.PermissionRequestService;
import dev.hmg.apm.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.transaction.Transactional;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class PermissionRequestController extends AbstractRequestController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private AppConfig appConfig;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private PermissionRequestService permissionRequestService;
	@Autowired
	private PermissionRequestDAO permissionRequestDAO;
	@Autowired
	private UserService userService;
	
	@GetMapping("/")
	public String indexPage(final Model model, final OAuth2AuthenticationToken auth) {
		OAuth2User user = auth.getPrincipal();
		
		model.addAttribute("user", user);
		model.addAttribute("isAdmin", supportUtils.isAdmin(user));
		model.addAttribute("products", productDAO.findAll());
		model.addAttribute("devMode", appConfig.isDevMode());
		
		return "indexPage";
	}
	
	@GetMapping("/edit")
	public String editForm(@RequestParam final int requestId, final Model model, final OAuth2AuthenticationToken auth, final Locale locale,
						   final RedirectAttributes redirectAttributes) {
		OAuth2User user = auth.getPrincipal();
		Optional<PermissionRequestEntity> possibleRequest = permissionRequestDAO.findById(requestId);
		if(!supportUtils.isUserCanEditRequest(user, possibleRequest)) {
			redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("request.edit.error.requestid.invalid", null, locale));
			return "redirect:/";
		}
		
		PermissionRequestEntity request = possibleRequest.get();
		model.addAttribute("user", user);
		model.addAttribute("isAdmin", supportUtils.isAdmin(user));
		model.addAttribute("products", productDAO.findAll());
		model.addAttribute("devMode", appConfig.isDevMode());
		model.addAttribute("requestId", requestId);
		
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
		
		return "editPage";
	}
	
	@PostMapping("/requestPermissions")
	@Transactional
	public String requestPermissions(@RequestParam(required = false) final String targetUserType,
									 @RequestParam(required = false) final String targetUserMail,
									 @RequestParam(required = false, value="products[]") final String[] products,
									 @RequestParam(required = false, value="categories[]") final String[] categories, 
									 @RequestParam Map<String,String> allParams,
									 final Locale locale,
									 final RedirectAttributes redirectAttributes, 
									 final OAuth2AuthenticationToken auth) {
		OAuth2User user = auth.getPrincipal();
		String targetMail = permissionRequestService.determineTargetEMail(targetUserType, targetUserMail, user);
		if(!permissionRequestService.isValidEMail(targetMail) && !permissionRequestService.isValidPrivilegedMail(targetMail, user)) {
			log.debug("User entered invalid eMail.");
			redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("request.error.mail.invalid", null, locale));
			addRequestParamFlashAttributes(redirectAttributes, targetUserType, targetUserMail, products, categories, allParams);
			return "redirect:/";
		}
		if(permissionRequestDAO.existsOpenPermissionRequestForUser(targetMail)) {
			log.debug("Encountered still open request for target-user.");
			redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("request.error.open.request.exists", null, locale));
			addRequestParamFlashAttributes(redirectAttributes, targetUserType, targetUserMail, products, categories, allParams);
			return "redirect:/";
		}

		userService.findOrCreateUser(user, locale);
		permissionRequestService.createPermissionRequest(user, targetMail, products, categories, allParams);
		notificationService.sendAdminNotificationAsync(
			messageSource.getMessage("notification.request.created.subject", new Object[]{targetMail}, Locale.GERMAN),
			messageSource.getMessage("notification.request.created.text", new Object[]{user.getAttribute("email"), targetMail}, Locale.GERMAN)
		);
		
		redirectAttributes.addFlashAttribute("message", 
				messageSource.getMessage("request.success.created", new Object[]{targetMail}, locale));
		return "redirect:/";
	}
	
	
	@PostMapping("/editRequest")
	public String editRequest(@RequestParam final int requestId, @RequestParam(required = false) final String targetUserType,
							  @RequestParam(required = false) final String targetUserMail,
							  @RequestParam(required = false, value="products[]") final String[] products,
							  @RequestParam(required = false, value="categories[]") final String[] categories,
							  @RequestParam Map<String,String> allParams,
							  final Locale locale,
							  final RedirectAttributes redirectAttributes,
							  final OAuth2AuthenticationToken auth) {
		OAuth2User user = auth.getPrincipal();
		Optional<PermissionRequestEntity> possibleRequest = permissionRequestDAO.findById(requestId);
		if(!supportUtils.isUserCanEditRequest(user, possibleRequest)) {
			redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("request.edit.error.requestid.invalid", null, locale));
			return "redirect:/";
		}
		String targetMail = permissionRequestService.determineTargetEMail(targetUserType, targetUserMail, user);
		if(!permissionRequestService.isValidEMail(targetMail) && !permissionRequestService.isValidPrivilegedMail(targetMail, user)) {
			log.debug("User entered invalid eMail.");
			redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("request.error.mail.invalid", null, locale));
			addRequestParamFlashAttributes(redirectAttributes, targetUserType, targetUserMail, products, categories, allParams);
			return "redirect:/edit?requestId="+requestId;
		}
		
		permissionRequestService.editPermissionRequest(possibleRequest.get(), targetUserMail, products, categories, allParams);
		
		redirectAttributes.addFlashAttribute("message",
				messageSource.getMessage("request.success.edited", new Object[]{targetMail}, locale));
		return "redirect:/myRequests";
	}
	
	@GetMapping("/myRequests")
	public String userRequests(final Model model, final OAuth2AuthenticationToken auth) {
		OAuth2User user = auth.getPrincipal();
		List<PermissionRequestEntity> requests = permissionRequestDAO.findRequestsForUser(user.getAttribute("email"));
		
		model.addAttribute("user", user);
		model.addAttribute("isAdmin", supportUtils.isAdmin(user));
		model.addAttribute("requests", requests);
		
		return "myRequests";
	}
	
	public void setAppConfig(final AppConfig appConfig) {
		this.appConfig = appConfig;
	}
	
	public void setMessageSource(final MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	public void setPermissionRequestService(final PermissionRequestService permissionRequestService) {
		this.permissionRequestService = permissionRequestService;
	}
	
	public void setPermissionRequestDAO(final PermissionRequestDAO permissionRequestDAO) {
		this.permissionRequestDAO = permissionRequestDAO;
	}

	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}

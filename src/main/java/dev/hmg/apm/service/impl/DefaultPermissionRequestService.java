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
import dev.hmg.apm.service.PermissionRequestService;
import dev.hmg.apm.service.PermissionRequestSubcategoryService;
import dev.hmg.apm.util.SupportUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class DefaultPermissionRequestService implements PermissionRequestService {
	
	@Autowired
	private AppConfig appConfig;
	@Autowired
	private AuditLogDAO auditLogDAO;
	@Autowired
	private PermissionRequestDAO permissionRequestDAO;
	@Autowired
	private PermissionRequestSubcategoryService permissionRequestSubcategoryService;
	@Autowired
	private ProductDAO productDAO;
	@Autowired
	private SupportUtils supportUtils;
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private PermissionRequestSubcategoryDAO permissionRequestSubcategoryDAO;

	private boolean allowLocalAsTLD = true;

	@Override
	public boolean isValidEMail(final String email) {
		if(!EmailValidator.getInstance(allowLocalAsTLD).isValid(email)) {
			return false;
		}
		return appConfig.getRegularAllowedMailSuffixes().stream().anyMatch(s -> StringUtils.endsWithIgnoreCase(email, s)) ||
				isValidEMailInDevMode(email);
	}
	
	private boolean isValidEMailInDevMode(final String email) {
		if(!appConfig.isDevMode() || CollectionUtils.isEmpty(appConfig.getDevModeMailSuffixes())) {
			return false;
		}
		
		return appConfig.getDevModeMailSuffixes().stream().anyMatch(s -> StringUtils.endsWithIgnoreCase(email, s));
	}
	
	@Override
	public boolean isValidPrivilegedMail(final String email, final OAuth2User user) {
		if(!supportUtils.isAdmin(user)) {
			return false;
		}
		return appConfig.getPrivilegedMailSuffixes().stream().anyMatch(s -> StringUtils.endsWithIgnoreCase(email, s));
	}
	
	@Override
	public String determineTargetEMail(final String targetUserType, final String otherUserMail, final OAuth2User user) {
		return determineTargetEMail(targetUserType, otherUserMail, (String)user.getAttribute("email"));
	}
	
	@Override
	public String determineTargetEMail(final String targetUserType, final String otherUserMail, final String userMail) {
		if("other".equalsIgnoreCase(targetUserType)) {
			return otherUserMail;
		}
		return userMail;
	}
	
	@Override
	public PermissionRequestEntity createPermissionRequest(final OAuth2User user, final String targetUserMail, final String[] products, 
														   final String[] categories, final Map<String,String> allParams) {
		List<ProductEntity> productList = productDAO.findProductsByKeys(Arrays.asList(products));
		PermissionRequestEntity pr = new PermissionRequestEntity();
		pr.setRequestFrom(user.getAttribute("email"));
		pr.setRequestFor(targetUserMail);
		pr.setResolution(RequestStatus.OPEN.name());
		pr.setRequestDateTimestamp(supportUtils.currentTimeMillis());
		pr.setProducts(productList);
		pr.setValidToTimestamp(supportUtils.parseDateTimestamp(allParams.get("cancellationDate"), "yyyy-MM-dd", null));
		
		pr = permissionRequestDAO.save(pr);
		List<PermissionRequestSubcategoryEntity> subcategories = permissionRequestSubcategoryService.createRequestSubcategoriesFromFormData(categories, pr, allParams);
		// TODO: does pr contain the subcategories at this point? How to "reload" the entity?
		
		return pr;
	}
	
	@Override
	public PermissionRequestEntity editPermissionRequest(final PermissionRequestEntity request, final String targetUserMail,
														 final String[] products, final String[] categories, final Map<String, String> allParams) {
		List<ProductEntity> productList = productDAO.findProductsByKeys(Arrays.asList(products));
		request.setRequestFor(targetUserMail);
		request.setProducts(productList);
		request.setValidToTimestamp(supportUtils.parseDateTimestamp(allParams.get("cancellationDate"), "yyyy-MM-dd", null));
		
		List<PermissionRequestSubcategoryEntity> subcategories = request.getRequestSubcategories();
		permissionRequestSubcategoryDAO.deleteAll(subcategories);
		request.getRequestSubcategories().clear();
		permissionRequestDAO.save(request);
		
		permissionRequestSubcategoryService.createRequestSubcategoriesFromFormData(categories, request, allParams);
		
		return request;
	}
	
	@Override
	@Transactional
	public PermissionRequestEntity rejectRequest(final int requestId, final OAuth2User user, final String comment) {
		Optional<PermissionRequestEntity> requestOpt = permissionRequestDAO.findById(requestId);
		if(requestOpt.isEmpty()) {
			return null;
		}
		PermissionRequestEntity request = requestOpt.get();
		request.setResolution(RequestStatus.REJECTED.name());
		request.setComment(comment);
		
		AuditLogEntity auditLog = new AuditLogEntity();
		auditLog.setEntryTimestamp(supportUtils.currentTimeMillis());
		auditLog.setAction(RequestStatus.REJECTED.name());
		auditLog.setUser(user.getName());
		auditLog.setTargetUser(request.getRequestFor());
		auditLog.setDescription("Rejected " + supportUtils.formatAuditLogDetails(request));
		auditLog.setComment(comment);
		
		permissionRequestDAO.save(request);
		auditLogDAO.save(auditLog);
		
		return request;
	}
	
	@Override
	public void sendRejectRequestNotification(final PermissionRequestEntity request) {
		UserEntity user = userDAO.findUserByMail(request.getRequestFrom());
		Locale locale = user != null ? user.getUserLocale() : Locale.ENGLISH;
		
		String subject = messageSource.getMessage("notification.request.rejected.subject", new String[]{request.getRequestFor()}, locale);
		String message = messageSource.getMessage("notification.request.rejected.text", new String[]{request.getRequestFor(), request.getComment()}, locale);
		
		notificationService.sendNotificationAsync(subject, message, request.getRequestFrom());
	}
	
	@Override
	public PermissionRequestEntity acceptRequest(final int requestId, final OAuth2User user) {
		Optional<PermissionRequestEntity> requestOpt = permissionRequestDAO.findById(requestId);
		if(requestOpt.isEmpty()) {
			return null;
		}
		PermissionRequestEntity request = requestOpt.get();
		request.setResolution(RequestStatus.ACCEPTED.name());
		
		AuditLogEntity auditLog = new AuditLogEntity();
		auditLog.setEntryTimestamp(supportUtils.currentTimeMillis());
		auditLog.setAction(RequestStatus.ACCEPTED.name());
		auditLog.setUser(user.getName());
		auditLog.setTargetUser(request.getRequestFor());
		auditLog.setDescription("Accepted " + supportUtils.formatAuditLogDetails(request));
		
		permissionRequestDAO.save(request);
		auditLogDAO.save(auditLog);
		
		return request;
	}
	
	@Override
	public void sendAcceptRequestNotification(final PermissionRequestEntity request) {
		UserEntity user = userDAO.findUserByMail(request.getRequestFrom());
		Locale locale = user != null ? user.getUserLocale() : Locale.ENGLISH;
		
		String subject = messageSource.getMessage("notification.request.accepted.subject", new String[]{request.getRequestFor()}, locale);
		String message = messageSource.getMessage("notification.request.accepted.text", new String[]{request.getRequestFor()}, locale);
		
		notificationService.sendNotificationAsync(subject, message, request.getRequestFrom());
	}
	
	public void setAuditLogDAO(final AuditLogDAO auditLogDAO) {
		this.auditLogDAO = auditLogDAO;
	}
	
	public void setPermissionRequestDAO(final PermissionRequestDAO permissionRequestDAO) {
		this.permissionRequestDAO = permissionRequestDAO;
	}
	
	public void setPermissionRequestSubcategoryService(final PermissionRequestSubcategoryService permissionRequestSubcategoryService) {
		this.permissionRequestSubcategoryService = permissionRequestSubcategoryService;
	}
	
	public void setProductDAO(final ProductDAO productDAO) {
		this.productDAO = productDAO;
	}
	
	public void setSupportUtils(final SupportUtils supportUtils) {
		this.supportUtils = supportUtils;
	}
	
	public void setAppConfig(final AppConfig appConfig) {
		this.appConfig = appConfig;
	}
	
	public void setUserDAO(final UserDAO userDAO) {
		this.userDAO = userDAO;
	}
	
	public void setMessageSource(final MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	public void setNotificationService(final NotificationService notificationService) {
		this.notificationService = notificationService;
	}
	
	public void setPermissionRequestSubcategoryDAO(final PermissionRequestSubcategoryDAO permissionRequestSubcategoryDAO) {
		this.permissionRequestSubcategoryDAO = permissionRequestSubcategoryDAO;
	}
}

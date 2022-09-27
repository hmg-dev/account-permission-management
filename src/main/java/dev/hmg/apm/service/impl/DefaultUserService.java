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
import dev.hmg.apm.service.UserService;
import dev.hmg.apm.util.SupportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Locale;

@Service
public class DefaultUserService implements UserService {
	@Autowired
	private SupportUtils supportUtils;
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private UserCategoryAssignmentDAO userCategoryAssignmentDAO;
	@Autowired
	private UserProductAssignmentDAO userProductAssignmentDAO;
	
	@Override
	public UserEntity findOrCreateUser(final String email, final String name) {
		UserEntity u = userDAO.findUserByMail(email);
		if(u != null) {
			return u;
		}
		
		u = new UserEntity();
		u.seteMail(email);
		u.setName(name);
		u.setLocale("de");
		
		return userDAO.save(u);
	}

	@Override
	public UserEntity findOrCreateUser(final OAuth2User user, final Locale locale) {
		UserEntity u = userDAO.findUserByMail(user.getAttribute("email"));
		if(u != null) {
			return u;
		}

		u = new UserEntity();
		u.seteMail(user.getAttribute("email"));
		u.setName(user.getAttribute("given_name") + " " + user.getAttribute("family_name"));
		u.setLocale(locale.getLanguage());

		return userDAO.save(u);
	}
	
	@Override
	public void assignRequestedProductsToUser(final UserEntity user, final PermissionRequestEntity request) {
		for(ProductEntity p : request.getProducts()) {
			if(userProductAssignmentDAO.existsAssignment(user, p)) {
				continue;
			}
			UserProductAssignment assignment = new UserProductAssignment();
			assignment.setProduct(p);
			assignment.setUser(user);
			assignment.setAssignmentDateTimestamp(supportUtils.currentTimeMillis());
			assignment.setValidToTimestamp(request.getValidToTimestamp());
			
			userProductAssignmentDAO.save(assignment);
		}
	}
	
	@Override
	public void assignRequestedCategoriesToUser(final UserEntity user, final PermissionRequestEntity request) {
		if(CollectionUtils.isEmpty(request.getRequestSubcategories())) {
			return;
		}
		for(PermissionRequestSubcategoryEntity subcat : request.getRequestSubcategories()) {
			if(!CollectionUtils.containsInstance(request.getProducts(), subcat.getProductCategory().getProduct())) {
				continue;
			}
			UserCategoryAssignment assignment = userCategoryAssignmentDAO.findByUserAndCategory(user, subcat.getProductCategory());
			if(assignment == null) {
				assignment = new UserCategoryAssignment();
				assignment.setUser(user);
				assignment.setCategory(subcat.getProductCategory());
				assignment.setAssignmentDateTimestamp(supportUtils.currentTimeMillis());
			} else {
				assignment.setUpdateDateTimestamp(supportUtils.currentTimeMillis());
			}
			assignment.setComment(subcat.getComment());
			assignment.setAccessMode(subcat.getAccessMode());
			assignment.setValidToTimestamp(request.getValidToTimestamp());
			
			userCategoryAssignmentDAO.save(assignment);
		}
	}
	
	@Override
	@Transactional
	public void revokePermissions(final String userMail) {
		UserEntity u = userDAO.findUserByMail(userMail);
		if(u == null) {
			throw new IllegalStateException("No such user for eMail " + userMail);
		}
		
		List<UserProductAssignment> userProductAssignments = userProductAssignmentDAO.findAssignmentsForUserId(u.getId());
		userProductAssignments.stream().filter(a -> a.getValidToTimestamp() == null).forEach(a -> {
			a.setValidToTimestamp(System.currentTimeMillis());
			userProductAssignmentDAO.save(a);
		});
		
		List<UserCategoryAssignment> userCategoryAssignments = userCategoryAssignmentDAO.findAssignmentsByUserId(u.getId());
		userCategoryAssignments.stream().filter(a -> a.getValidToTimestamp() == null).forEach(a -> {
			a.setValidToTimestamp(System.currentTimeMillis());
			userCategoryAssignmentDAO.save(a);
		});
	}
	
	public void setSupportUtils(final SupportUtils supportUtils) {
		this.supportUtils = supportUtils;
	}
	
	public void setUserDAO(final UserDAO userDAO) {
		this.userDAO = userDAO;
	}
	
	public void setUserCategoryAssignmentDAO(final UserCategoryAssignmentDAO userCategoryAssignmentDAO) {
		this.userCategoryAssignmentDAO = userCategoryAssignmentDAO;
	}
	
	public void setUserProductAssignmentDAO(final UserProductAssignmentDAO userProductAssignmentDAO) {
		this.userProductAssignmentDAO = userProductAssignmentDAO;
	}
	
}

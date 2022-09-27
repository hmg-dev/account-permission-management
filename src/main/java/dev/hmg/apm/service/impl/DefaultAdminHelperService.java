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
import dev.hmg.apm.service.AdminHelperService;
import dev.hmg.apm.service.PermissionRequestService;
import dev.hmg.apm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class DefaultAdminHelperService implements AdminHelperService {
	
	@Autowired
	private PermissionRequestService permissionRequestService;
	@Autowired
	private UserService userService;
	
	@Override
	@Transactional
	public PermissionRequestEntity acceptRequest(final int requestId, final OAuth2AuthenticationToken auth) {
		PermissionRequestEntity request = permissionRequestService.acceptRequest(requestId, auth.getPrincipal());
		UserEntity user = userService.findOrCreateUser(request.getRequestFor(), ""); // FIXME: find name
		userService.assignRequestedProductsToUser(user, request);
		userService.assignRequestedCategoriesToUser(user, request);
		
		return request;
	}
	
	public void setPermissionRequestService(final PermissionRequestService permissionRequestService) {
		this.permissionRequestService = permissionRequestService;
	}
	
	public void setUserService(final UserService userService) {
		this.userService = userService;
	}
}

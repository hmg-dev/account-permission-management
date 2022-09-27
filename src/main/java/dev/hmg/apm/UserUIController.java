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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserUIController {
	@Autowired
	private UserDetailsDAO userDetailsDAO;
	@Autowired
	private SupportUtils supportUtils;
	
	@GetMapping("/myPermissions")
	public String userPermissions(final Model model, final OAuth2AuthenticationToken auth) {
		String userMail = auth.getPrincipal().getAttribute("email");
		UserDetailsEntity userDetails = userDetailsDAO.findUserByMail(userMail);
		
		model.addAttribute("user", userDetails);
		model.addAttribute("isAdmin", supportUtils.isAdmin(auth.getPrincipal()));
		
		return "myPermissions";
	}
	
	public void setUserDetailsDAO(final UserDetailsDAO userDetailsDAO) {
		this.userDetailsDAO = userDetailsDAO;
	}
	
	public void setSupportUtils(final SupportUtils supportUtils) {
		this.supportUtils = supportUtils;
	}
}

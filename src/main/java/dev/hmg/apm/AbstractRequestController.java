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

import dev.hmg.apm.db.ProductDAO;
import dev.hmg.apm.db.entity.PermissionRequestEntity;
import dev.hmg.apm.model.ProductAccessLevel;
import dev.hmg.apm.util.SupportUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractRequestController {
	
	@Autowired
	protected SupportUtils supportUtils;
	@Autowired
	protected ProductDAO productDAO;
	
	protected void addRequestParamFlashAttributes(final RedirectAttributes redirectAttributes, final String targetUserType,
												final String targetUserMail, final String[] products, final String[] categories, final Map<String,String> allParams) {
		redirectAttributes.addFlashAttribute("targetUserType", targetUserType);
		redirectAttributes.addFlashAttribute("targetUserMail", targetUserMail);
		redirectAttributes.addFlashAttribute("selectedProducts", products);
		redirectAttributes.addFlashAttribute("selectedProductCategories", supportUtils.createSubcategoryMappingWithoutAccessMode(categories));
		redirectAttributes.addFlashAttribute("allParams", allParams);
	}
	
	protected Map<String,String> buildSelectedProductCategoriesFromRequest(final PermissionRequestEntity request) {
		return request.getRequestSubcategories().stream().filter(rsc->!StringUtils.equalsIgnoreCase("comment", rsc.getAccessMode())).collect(
				Collectors.toMap(
						rsc->StringUtils.join(rsc.getProductCategory().getProduct().getKey(), "_", rsc.getProductCategory().getKey()),
						rsc->StringUtils.join(rsc.getProductCategory().getProduct().getKey(), "_", rsc.getProductCategory().getKey(), "_", rsc.getAccessMode())));
	}
	
	protected Map<String,String> buildAllParamsFromRequest(final PermissionRequestEntity request) {
		return request.getRequestSubcategories().stream().collect(
				Collectors.toMap(
						rsc->StringUtils.join(rsc.getProductCategory().getProduct().getKey(), "_", rsc.getProductCategory().getKey(), "_", rsc.getAccessMode()),
						rsc->(ProductAccessLevel.COMMENT.key().equals(rsc.getAccessMode())) ? rsc.getComment() : StringUtils.join(rsc.getProductCategory().getProduct().getKey(), "_", rsc.getProductCategory().getKey(), "_", rsc.getAccessMode())
				)
		);
	}
	
	public void setSupportUtils(final SupportUtils supportUtils) {
		this.supportUtils = supportUtils;
	}
	
	public void setProductDAO(final ProductDAO productDAO) {
		this.productDAO = productDAO;
	}
}

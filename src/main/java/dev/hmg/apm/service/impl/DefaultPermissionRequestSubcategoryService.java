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

import dev.hmg.apm.db.PermissionRequestSubcategoryDAO;
import dev.hmg.apm.db.ProductCategoryDAO;
import dev.hmg.apm.db.ProductDAO;
import dev.hmg.apm.db.entity.PermissionRequestEntity;
import dev.hmg.apm.db.entity.PermissionRequestSubcategoryEntity;
import dev.hmg.apm.db.entity.ProductCategoryEntity;
import dev.hmg.apm.db.entity.ProductEntity;
import dev.hmg.apm.service.PermissionRequestSubcategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class DefaultPermissionRequestSubcategoryService implements PermissionRequestSubcategoryService {
	
	@Autowired
	private ProductDAO productDAO;
	@Autowired
	private ProductCategoryDAO productCategoryDAO;
	@Autowired
	private PermissionRequestSubcategoryDAO permissionRequestSubcategoryDAO;
	
	@Override
	public List<PermissionRequestSubcategoryEntity> createRequestSubcategoriesFromFormData(final String[] categories, final PermissionRequestEntity permissionRequest,
																						   final Map<String,String> allParams) {
		if(categories == null || categories.length == 0) {
			return Collections.emptyList();
		}
		
		// FIXME: introduce product-list param and only create category if its product is in this list! 
		List<PermissionRequestSubcategoryEntity> subcategories = new ArrayList<>();
		for(String category : categories) {
			extractCreateAndAddSubcategoryIfValid(category, subcategories, permissionRequest, null);
		}
		
		if(allParams != null && !allParams.isEmpty()) {
			allParams.forEach((k, v) -> {
				if(StringUtils.endsWithIgnoreCase(k, "_comment") && v != null && !v.isEmpty()){
					extractCreateAndAddSubcategoryIfValid(k, subcategories, permissionRequest, v);
				} 
			});
		}
		
		return subcategories;
	}
	
	private void extractCreateAndAddSubcategoryIfValid(final String category, final List<PermissionRequestSubcategoryEntity> subcategories, 
													   final PermissionRequestEntity permissionRequest, final String comment) {
		String[] parts = category.split("_");
		if(parts.length != 3) {
			return;
		}
		PermissionRequestSubcategoryEntity cat = createPermissionRequestSubcategory(parts[0], parts[1], parts[2], permissionRequest, comment);
		if(cat != null) {
			subcategories.add(permissionRequestSubcategoryDAO.save(cat));
		}
	}
	
	private PermissionRequestSubcategoryEntity createPermissionRequestSubcategory(final String productKey, final String categoryKey, final String accessMode,
																			final PermissionRequestEntity permissionRequest, final String comment) {
		ProductEntity product = productDAO.findProductByKey(productKey);
		if(product == null) {
			return null;
		}
		ProductCategoryEntity productCategory = productCategoryDAO.findProductCategoryByProductAndKey(product.getId(), categoryKey);
		if(productCategory == null) {
			return null;
		}
		PermissionRequestSubcategoryEntity cat = new PermissionRequestSubcategoryEntity();
		cat.setProductCategory(productCategory);
		cat.setAccessMode(accessMode);
		cat.setPermissionRequest(permissionRequest);
		cat.setComment(comment);
		
		return cat;
	}
	
	public void setProductDAO(final ProductDAO productDAO) {
		this.productDAO = productDAO;
	}
	
	public void setProductCategoryDAO(final ProductCategoryDAO productCategoryDAO) {
		this.productCategoryDAO = productCategoryDAO;
	}
	
	public void setPermissionRequestSubcategoryDAO(final PermissionRequestSubcategoryDAO permissionRequestSubcategoryDAO) {
		this.permissionRequestSubcategoryDAO = permissionRequestSubcategoryDAO;
	}
}

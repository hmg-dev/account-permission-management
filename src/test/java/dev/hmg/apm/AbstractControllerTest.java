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
import dev.hmg.apm.db.entity.PermissionRequestSubcategoryEntity;
import dev.hmg.apm.db.entity.ProductCategoryEntity;
import dev.hmg.apm.db.entity.ProductEntity;
import dev.hmg.apm.model.ProductAccessLevel;
import dev.hmg.apm.model.RequestStatus;
import dev.hmg.apm.util.SupportUtils;
import org.mockito.Mock;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public abstract class AbstractControllerTest {
	
	@Mock
	protected SupportUtils supportUtils;
	@Mock
	protected RedirectAttributes redirectAttributes;
	@Mock
	protected ProductDAO productDAO;
	
	protected Map<String,String> dummyCategoriesNoAM;
	
	protected PermissionRequestEntity createDummyPermissionRequestEntity() {
		ProductEntity dummyProduct1 = new ProductEntity();
		ProductEntity dummyProduct2 = new ProductEntity();
		dummyProduct1.setKey("product-1");
		dummyProduct2.setKey("product-2");
		PermissionRequestSubcategoryEntity subcat1 = createDummySubCat("cat1", ProductAccessLevel.READ, dummyProduct1);
		PermissionRequestSubcategoryEntity subcat2 = createDummySubCat("cat2", ProductAccessLevel.WRITE, dummyProduct1);
		PermissionRequestSubcategoryEntity subcat3 = createDummySubCat("cat1", ProductAccessLevel.ADMIN, dummyProduct2);
		PermissionRequestSubcategoryEntity subcat4 = createDummySubCat("CUSTOM", ProductAccessLevel.COMMENT, dummyProduct2);
		PermissionRequestEntity dummyRequest = new PermissionRequestEntity();
		dummyRequest.setRequestFor("test.user2@company.tld");
		dummyRequest.setRequestFrom("test.me@company.tld");
		dummyRequest.setResolution(RequestStatus.OPEN.name());
		dummyRequest.setRequestDateTimestamp(42);
		dummyRequest.setProducts(Arrays.asList(dummyProduct1, dummyProduct2));
		dummyRequest.setRequestSubcategories(Arrays.asList(subcat1, subcat2, subcat3, subcat4));
		dummyRequest.setValidToTimestamp(42L);
		
		return dummyRequest;
	}
	
	private PermissionRequestSubcategoryEntity createDummySubCat(final String key, final ProductAccessLevel mode, final ProductEntity product) {
		ProductCategoryEntity cat = new ProductCategoryEntity();
		cat.setKey(key);
		cat.setProduct(product);
		PermissionRequestSubcategoryEntity subcat = new PermissionRequestSubcategoryEntity();
		subcat.setAccessMode(mode.key());
		subcat.setProductCategory(cat);
		subcat.setComment("Dummy Comment NARF");
		
		return subcat;
	}
	
	protected void verifyParametersAsFlashAttributes(final String targetUserType, final String targetUserMail, final String[] products, final String[] categories) {
		verify(supportUtils, times(1)).createSubcategoryMappingWithoutAccessMode(categories);
		verify(redirectAttributes, times(1)).addFlashAttribute("targetUserType", targetUserType);
		verify(redirectAttributes, times(1)).addFlashAttribute("targetUserMail", targetUserMail);
		verify(redirectAttributes, times(1)).addFlashAttribute("selectedProducts", products);
		verify(redirectAttributes, times(1)).addFlashAttribute("selectedProductCategories", dummyCategoriesNoAM);
		verify(redirectAttributes, times(1)).addFlashAttribute(eq("allParams"), anyMap());
	}
	
}

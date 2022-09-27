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
package dev.hmg.apm.integration.ado.service;

import dev.hmg.apm.db.ProductCategoryDAO;
import dev.hmg.apm.db.ProductDAO;
import dev.hmg.apm.db.entity.ProductCategoryEntity;
import dev.hmg.apm.db.entity.ProductEntity;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.azd.core.types.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

@Service
public class AdoProjectSyncService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private AdoProjectFetchService adoProjectFetchService;
	@Autowired
	private ProductCategoryDAO productCategoryDao;
	@Autowired
	private ProductDAO productDAO;
	
	public int syncProjectsFromAdoToDatabase() {
		List<Project> projects = adoProjectFetchService.fetchProjects();
		if(CollectionUtils.isEmpty(projects)) {
			throw new IllegalStateException("Unexpected Input. Project list is empty. Check the connection to ADO!");
		}
		
		List<String> categoryNames = productCategoryDao.findCategoryNamesForProductKey("azure-devops");
		ProductEntity product = productDAO.findProductByKey("azure-devops");
		
		int syncedCategories = 0;
		for(Project p : projects) {
			if(categoryNames.contains(p.getName())) {
				log.debug("Category for Project {} already exists - skipping.", p.getName());
				continue;
			}
			ProductCategoryEntity cat = createCategoryFromProject(p, product);
			productCategoryDao.save(cat);
			++syncedCategories;
		}
		
		return syncedCategories;
	}
	
	private ProductCategoryEntity createCategoryFromProject(final Project p, final ProductEntity product) {
		ProductCategoryEntity cat = new ProductCategoryEntity();
		cat.setName(p.getName());
		cat.setKey(generateCategoryKey(p.getName()));
		cat.setDescription(p.getDescription());
		cat.setProduct(product);
		cat.setAutomated(true);
		
		return cat;
	}
	
	private String generateCategoryKey(final String name) {
		return StringUtils.lowerCase(Normalizer.normalize(StringUtils.strip(name), Normalizer.Form.NFD)
				.replaceAll("[^\\p{ASCII}]", "").replaceAll("[ \\._]", "-"));
	}
	
	public void setAdoProjectFetchService(final AdoProjectFetchService adoProjectFetchService) {
		this.adoProjectFetchService = adoProjectFetchService;
	}
	
	public void setProductCategoryDao(final ProductCategoryDAO productCategoryDao) {
		this.productCategoryDao = productCategoryDao;
	}
	
	public void setProductDAO(final ProductDAO productDAO) {
		this.productDAO = productDAO;
	}
}

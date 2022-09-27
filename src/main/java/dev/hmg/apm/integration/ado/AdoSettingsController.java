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
package dev.hmg.apm.integration.ado;

import dev.hmg.apm.db.ProductCategoryDAO;
import dev.hmg.apm.integration.ado.service.AdoProjectFetchService;
import dev.hmg.apm.integration.ado.service.AdoProjectSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
public class AdoSettingsController {
	
	@Autowired
	private AdoProjectFetchService adoProjectFetchService;
	@Autowired
	private AdoProjectSyncService adoProjectSyncService;
	@Autowired
	private ProductCategoryDAO productCategoryDao;
	@Autowired
	private MessageSource messageSource;
	
	@GetMapping("/adoSettings")
	@PreAuthorize("hasRole('DevOps')")
	public String settings(final Model model) {
		model.addAttribute("adoProjects", adoProjectFetchService.fetchProjects());
		model.addAttribute("adoCategories", productCategoryDao.findCategoryNamesForProductKey("azure-devops"));
		
		return "adoSettings";
	}
	
	@PostMapping("/adoSyncProjects")
	@PreAuthorize("hasRole('DevOps')")
	public String syncProjects(final RedirectAttributes redirectAttributes, final Locale locale) {
		int importedCategories = 0;
		try {
			importedCategories = adoProjectSyncService.syncProjectsFromAdoToDatabase();
		} catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("integration.ado.settings.sync_failed", null, locale));
			return "redirect:/adoSettings";
		}
		
		redirectAttributes.addFlashAttribute("message", 
				messageSource.getMessage("integration.ado.settings.sync_success", new Object[]{importedCategories}, locale));
		return "redirect:/adoSettings";
	}
	
	public void setAdoProjectFetchService(final AdoProjectFetchService adoProjectFetchService) {
		this.adoProjectFetchService = adoProjectFetchService;
	}
	
	public void setAdoProjectSyncService(final AdoProjectSyncService adoProjectSyncService) {
		this.adoProjectSyncService = adoProjectSyncService;
	}
	
	public void setProductCategoryDao(final ProductCategoryDAO productCategoryDao) {
		this.productCategoryDao = productCategoryDao;
	}
	
	public void setMessageSource(final MessageSource messageSource) {
		this.messageSource = messageSource;
	}
}

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

import org.azd.core.types.Project;
import org.azd.exceptions.AzDException;
import org.azd.interfaces.AzDClient;
import org.azd.utils.AzDClientApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Service
public class AdoProjectFetchService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private AzDClient adoClient;
	
	@Autowired
	private AdoConnectionService adoConnectionService;
	
	@PostConstruct
	public void init() {
		adoClient = new AzDClientApi(adoConnectionService.connectionData());
	}
	
	public List<Project> fetchProjects() {
		try {
			return adoClient.getCoreApi()
					.getProjects()
					.getProjects();
		} catch (AzDException e) {
			log.error("Unable to fetch ADO-Projects: ", e);
		}
		return Collections.emptyList();
	} 
	
	public void setAdoConnectionService(final AdoConnectionService adoConnectionService) {
		this.adoConnectionService = adoConnectionService;
	}
	
	protected void setAdoClient(final AzDClient adoClient) {
		this.adoClient = adoClient;
	}
}

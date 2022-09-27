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
import dev.hmg.apm.model.PaginationData;
import dev.hmg.apm.service.PaginationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultPaginationService implements PaginationService {
	@Autowired
	private AppConfig appConfig;
	
	@Override
	public PaginationData determinePaginationData(final int totalEntries, final int currentPage) {
		int pageCount = (int) Math.ceil((double)totalEntries / (double)appConfig.getEntriesPerPage());
		PaginationData.Builder b = new PaginationData.Builder();
		b.setCurrentPage(currentPage);
		b.setPageCount(pageCount);
		b.setEntriesPerPage(appConfig.getEntriesPerPage());
		b.setVisiblePageNumberAmount(visiblePageNumberAmount(appConfig.getPageNumbersInPaginationBar(), pageCount, currentPage));
		b.setFirstVisiblePage(firstVisiblePage(currentPage, appConfig.getPageNumbersInPaginationBar()));
		
		return b.build();
	}
	
	private int visiblePageNumberAmount(final int defaultAmount, final int pageCount, final int currentPage) {
		double half = ((double)defaultAmount / 2.0);
		int pagesUntilLast = (pageCount - 1 - currentPage);
		if(pageCount > defaultAmount && pagesUntilLast < half) {
			return (int) Math.round(half+pagesUntilLast);
		}
		
		return Math.min(defaultAmount, pageCount);
	}
	
	private int firstVisiblePage(final int currentPage, final int pageNumberAmount) {
		double half = ((double)pageNumberAmount / 2.0);
		if(currentPage > half) {
			return (int) (currentPage - Math.floor(half));
		}
		return 0;
	}
	
	public void setAppConfig(final AppConfig appConfig) {
		this.appConfig = appConfig;
	}
}

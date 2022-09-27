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
package dev.hmg.apm.model;

public class PaginationData {
	private final int pageCount;
	private final int currentPage;
	private final int visiblePageNumberAmount;
	private final int firstVisiblePage;
	private final int entriesPerPage;
	
	private PaginationData(final Builder b) {
		pageCount = b.pageCount;
		currentPage = b.currentPage;
		visiblePageNumberAmount = b.visiblePageNumberAmount;
		firstVisiblePage = b.firstVisiblePage;
		entriesPerPage = b.entriesPerPage;
	}
	
	public static class Builder {
		private int pageCount;
		private int currentPage;
		private int visiblePageNumberAmount;
		private int firstVisiblePage;
		private int entriesPerPage;
		
		public PaginationData build() {
			return new PaginationData(this);
		}
		
		public Builder setPageCount(final int pageCount) {
			this.pageCount = pageCount;
			return this;
		}
		
		public Builder setCurrentPage(final int currentPage) {
			this.currentPage = currentPage;
			return this;
		}
		
		public Builder setVisiblePageNumberAmount(final int visiblePageNumberAmount) {
			this.visiblePageNumberAmount = visiblePageNumberAmount;
			return this;
		}
		
		public Builder setFirstVisiblePage(final int firstVisiblePage) {
			this.firstVisiblePage = firstVisiblePage;
			return this;
		}
		
		public Builder setEntriesPerPage(final int entriesPerPage) {
			this.entriesPerPage = entriesPerPage;
			return this;
		}
	}
	
	public int getPageCount() {
		return pageCount;
	}
	
	public int getCurrentPage() {
		return currentPage;
	}
	
	public int getVisiblePageNumberAmount() {
		return visiblePageNumberAmount;
	}
	
	public int getFirstVisiblePage() {
		return firstVisiblePage;
	}
	
	public int getEntriesPerPage() {
		return entriesPerPage;
	}
}

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
package dev.hmg.apm.db.entity;

import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "permission_request_subcategories")
public class PermissionRequestSubcategoryEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INTEGER")
	private int id;
	
	@ManyToOne
	@JoinColumn(name = "product_category_id")
	private ProductCategoryEntity productCategory;
	
	@ManyToOne
	@JoinColumn(name = "permission_request_id")
	private PermissionRequestEntity permissionRequest;
	
	@Column(name = "access_mode")
	private String accessMode;
	
	@Column(columnDefinition = "text")
	@Type(type = "text")
	private String comment;
	
	public PermissionRequestSubcategoryEntity() {}
	
	
	public int getId() {
		return id;
	}
	
	public ProductCategoryEntity getProductCategory() {
		return productCategory;
	}
	
	public PermissionRequestEntity getPermissionRequest() {
		return permissionRequest;
	}
	
	public String getAccessMode() {
		return accessMode;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setId(final int id) {
		this.id = id;
	}
	
	public void setProductCategory(final ProductCategoryEntity productCategory) {
		this.productCategory = productCategory;
	}
	
	public void setPermissionRequest(final PermissionRequestEntity permissionRequest) {
		this.permissionRequest = permissionRequest;
	}
	
	public void setAccessMode(final String accessMode) {
		this.accessMode = accessMode;
	}
	
	public void setComment(final String comment) {
		this.comment = comment;
	}
}

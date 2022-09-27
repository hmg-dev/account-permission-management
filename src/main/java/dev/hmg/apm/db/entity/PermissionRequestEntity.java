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
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "permission_requests")
public class PermissionRequestEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INTEGER")
	private int id;
	
	@Column(name = "request_from")
	private String requestFrom;
	
	@Column(name = "request_for")
	private String requestFor;
	
	@Column(name = "request_date", columnDefinition = "INTEGER")
	private long requestDateTimestamp;
	
	private String resolution;
	
	@Column(name = "resolution_comment", columnDefinition = "text")
	@Type(type = "text")
	private String comment;
	
	@Column(name = "validto_date")
	private Long validToTimestamp;
	
	@OneToMany
	@JoinColumn(name = "permission_request_id")
	private List<PermissionRequestSubcategoryEntity> requestSubcategories;
	
	@ManyToMany
	@JoinTable(name = "permission_request_to_product", 
			joinColumns = @JoinColumn(name = "permission_request_id"),
			inverseJoinColumns = @JoinColumn(name = "product_id"))
	private List<ProductEntity> products;
	
	public PermissionRequestEntity() {}
	
	public int getId() {
		return id;
	}
	
	public String getRequestFrom() {
		return requestFrom;
	}
	
	public String getRequestFor() {
		return requestFor;
	}
	
	public long getRequestDateTimestamp() {
		return requestDateTimestamp;
	}
	
	public Instant getRequestDateTime() {
		return Instant.ofEpochMilli(requestDateTimestamp);
	}
	
	public String getResolution() {
		return resolution;
	}
	
	public String getComment() {
		return comment;
	}
	
	public List<ProductEntity> getProducts() {
		return products;
	}
	
	public List<PermissionRequestSubcategoryEntity> getRequestSubcategories() {
		return requestSubcategories;
	}
	
	public void setId(final int id) {
		this.id = id;
	}
	
	public void setRequestFrom(final String requestFrom) {
		this.requestFrom = requestFrom;
	}
	
	public void setRequestFor(final String requestFor) {
		this.requestFor = requestFor;
	}
	
	public void setRequestDateTimestamp(final long requestDateTimestamp) {
		this.requestDateTimestamp = requestDateTimestamp;
	}
	
	public void setResolution(final String resolution) {
		this.resolution = resolution;
	}
	
	public void setComment(final String comment) {
		this.comment = comment;
	}
	
	public void setRequestSubcategories(final List<PermissionRequestSubcategoryEntity> requestSubcategories) {
		this.requestSubcategories = requestSubcategories;
	}
	
	public void setProducts(final List<ProductEntity> products) {
		this.products = products;
	}
	
	public Long getValidToTimestamp() {
		return validToTimestamp;
	}
	
	public Instant getValidToDateTime() {
		return validToTimestamp!=null?Instant.ofEpochMilli(validToTimestamp):null;
	}
	
	public void setValidToTimestamp(final Long validToTimestamp) {
		this.validToTimestamp = validToTimestamp;
	}
}

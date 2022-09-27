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
@Table(name = "product_categories")
public class ProductCategoryEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INTEGER")
	private int id;
	
	private String name;
	
	@ManyToOne
	@JoinColumn(name = "product_id")
	private ProductEntity product;
	
	@Column(name = "category_key")
	private String key;
	
	@Column(columnDefinition = "text")
	@Type(type = "text")
	private String description;
	
	private boolean active = true;
	
	private boolean automated = false;
	
	public ProductCategoryEntity() {}
	
	
	public boolean isAutomated() {
		return automated;
	}
	
	public void setAutomated(final boolean automated) {
		this.automated = automated;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(final boolean active) {
		this.active = active;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public ProductEntity getProduct() {
		return product;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setId(final int id) {
		this.id = id;
	}
	
	public void setName(final String name) {
		this.name = name;
	}
	
	public void setProduct(final ProductEntity product) {
		this.product = product;
	}
	
	public void setKey(final String key) {
		this.key = key;
	}
	
	public void setDescription(final String description) {
		this.description = description;
	}
}

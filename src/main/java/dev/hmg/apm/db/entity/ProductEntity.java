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
import java.util.List;

@Entity
@Table(name = "products")
public class ProductEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INTEGER")
	private int id;
	
	private String name;
	
	@Column(name = "product_key")
	private String key;
	
	@Column(columnDefinition = "text")
	@Type(type = "text")
	private String description;
	
	@OneToMany
	@JoinColumn(name = "product_id")
	@OrderBy("name ASC")
	private List<ProductCategoryEntity> categories;
	
	public ProductEntity() {}
	
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getDescription() {
		return description;
	}
	
	public List<ProductCategoryEntity> getCategories() {
		return categories;
	}
	
	public void setId(final int id) {
		this.id = id;
	}
	
	public void setName(final String name) {
		this.name = name;
	}
	
	public void setKey(final String key) {
		this.key = key;
	}
	
	public void setDescription(final String description) {
		this.description = description;
	}
	
	public void setCategories(final List<ProductCategoryEntity> categories) {
		this.categories = categories;
	}
}

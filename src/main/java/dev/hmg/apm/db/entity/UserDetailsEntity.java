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

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class UserDetailsEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INTEGER")
	private int id;
	
	private String name;
	
	@Column(name = "email")
	private String eMail;
	
	@OneToMany
	@JoinColumn(name = "user_id")
	private List<UserProductAssignment> productAssignments;
	
	@OneToMany
	@JoinColumn(name = "user_id")
	private List<UserCategoryAssignment> categoryAssignments;
	
	public int getId() {
		return id;
	}
	
	public void setId(final int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(final String name) {
		this.name = name;
	}
	
	public String geteMail() {
		return eMail;
	}
	
	public void seteMail(final String eMail) {
		this.eMail = eMail;
	}
	
	public List<UserProductAssignment> getProductAssignments() {
		return productAssignments;
	}
	
	public void setProductAssignments(final List<UserProductAssignment> productAssignments) {
		this.productAssignments = productAssignments;
	}
	
	public List<UserCategoryAssignment> getCategoryAssignments() {
		return categoryAssignments;
	}
	
	public void setCategoryAssignments(final List<UserCategoryAssignment> categoryAssignments) {
		this.categoryAssignments = categoryAssignments;
	}
}

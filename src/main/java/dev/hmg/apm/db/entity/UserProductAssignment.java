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
import java.time.Instant;

@Entity
@Table(name = "user_to_product")
@IdClass(UserProductAssignmentId.class)
public class UserProductAssignment {
	
	@Id
	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserEntity user;
	
	@Id
	@ManyToOne
	@JoinColumn(name = "product_id")
	private ProductEntity product;
	
	@Column(name = "assignment_date", columnDefinition = "INTEGER")
	private long assignmentDateTimestamp;
	
	@Column(name = "validto_date")
	private Long validToTimestamp;
	
	public UserEntity getUser() {
		return user;
	}
	
	public void setUser(final UserEntity user) {
		this.user = user;
	}
	
	public ProductEntity getProduct() {
		return product;
	}
	
	public void setProduct(final ProductEntity product) {
		this.product = product;
	}
	
	public long getAssignmentDateTimestamp() {
		return assignmentDateTimestamp;
	}
	
	public Instant getAssignmentDateTime() {
		return Instant.ofEpochMilli(assignmentDateTimestamp);
	}
	
	public void setAssignmentDateTimestamp(final long assignmentDateTimestamp) {
		this.assignmentDateTimestamp = assignmentDateTimestamp;
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

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

import java.io.Serializable;

public class UserProductAssignmentId implements Serializable {
	private int user;
	private int product;
	
	public UserProductAssignmentId(){}
	
	public UserProductAssignmentId(final int userId, final int productId) {
		this.user = userId;
		this.product = productId;
	}
	
	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		
		UserProductAssignmentId that = (UserProductAssignmentId) o;
		
		if (user != that.user) return false;
		return product == that.product;
	}
	
	@Override
	public int hashCode() {
		int result = user;
		result = 31 * result + product;
		return result;
	}
}

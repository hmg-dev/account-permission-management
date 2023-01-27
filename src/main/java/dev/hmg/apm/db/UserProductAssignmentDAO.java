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
package dev.hmg.apm.db;

import dev.hmg.apm.db.entity.ProductEntity;
import dev.hmg.apm.db.entity.UserEntity;
import dev.hmg.apm.db.entity.UserProductAssignment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProductAssignmentDAO extends CrudRepository<UserProductAssignment, Integer> {
	
	@Query("select case when count(a) > 0 then true else false end " +
			"from UserProductAssignment a where a.user = :user and a.product = :product")
	boolean existsAssignment(@Param("user") final UserEntity user, @Param("product") final ProductEntity product);
	
	@Query("select a from UserProductAssignment a " +
			"where a.validToTimestamp >= (UNIX_TIMESTAMP() * 1000) " +
			"and a.validToTimestamp <= (UNIX_TIMESTAMP() + (:days * 24*60*60))*1000 " +
			"order by a.validToTimestamp")
	List<UserProductAssignment> findAssignmentsExpiringInDays(@Param("days") final int days);
	
	@Query("select a from UserProductAssignment a " +
			"where a.validToTimestamp <= (UNIX_TIMESTAMP() * 1000) " +
			"and a.validToTimestamp >= (UNIX_TIMESTAMP() - (:days * 24*60*60))*1000 " +
			"order by a.validToTimestamp")
	List<UserProductAssignment> findAssignmentsExpiredForDays(@Param("days") final int days);
	
	@Query("select a from UserProductAssignment a where a.user.id = :userId")
	List<UserProductAssignment> findAssignmentsForUserId(@Param("userId") final int userId);
}

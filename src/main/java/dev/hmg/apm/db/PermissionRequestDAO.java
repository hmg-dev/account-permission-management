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

import dev.hmg.apm.db.entity.PermissionRequestEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRequestDAO extends CrudRepository<PermissionRequestEntity, Integer> {
	
	@Query("select case when count(r) > 0 then true else false end " +
			"from PermissionRequestEntity r " +
			"where r.requestFor = :user and r.resolution = 'OPEN'")
	boolean existsOpenPermissionRequestForUser(@Param("user") final String targetUser);
	
	@Query("select pr from PermissionRequestEntity pr where pr.resolution = 'OPEN'")
	List<PermissionRequestEntity> findOpenPermissionRequests();
	
	@Query("select pr from PermissionRequestEntity pr where pr.requestFrom = :user order by pr.requestDateTimestamp desc")
	List<PermissionRequestEntity> findRequestsForUser(@Param("user") final String user);
}

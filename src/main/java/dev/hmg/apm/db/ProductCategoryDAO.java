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

import dev.hmg.apm.db.entity.ProductCategoryEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryDAO extends CrudRepository<ProductCategoryEntity, Integer> {
	
	@Query("select p from ProductCategoryEntity p where p.product.id = :productId and p.key = :categoryKey")
	ProductCategoryEntity findProductCategoryByProductAndKey(@Param("productId") int productId, @Param("categoryKey") String categoryKey);
	
	@Query("select pc.name from ProductCategoryEntity pc where pc.product.key = :productKey")
	List<String> findCategoryNamesForProductKey(@Param("productKey") String productKey);
}

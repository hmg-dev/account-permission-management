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
package dev.hmg.apm.model;

import java.util.Arrays;
import java.util.Optional;

public enum ProductAccessLevel {
	READ("read"),
	WRITE("write"),
	ADMIN("admin"),
	STAKEHOLDER("stakeholder"),
	COMMENT("comment");
	
	private final String key;
	
	ProductAccessLevel(final String key) {
		this.key = key;
	}
	
	public String key() {
		return this.key;
	}
	
	ProductAccessLevel fromKey(final String key) {
		Optional<ProductAccessLevel> match = Arrays.stream(ProductAccessLevel.values()).filter(pal -> pal.key().equals(key)).findFirst();
		return match.orElse(null);
	}
}

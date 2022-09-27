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
package dev.hmg.apm.util;

import dev.hmg.apm.db.entity.PermissionRequestEntity;
import dev.hmg.apm.db.entity.ProductEntity;
import dev.hmg.apm.model.RequestStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SupportUtils {
	
	protected static final String ADMIN_GROUP = "ROLE_DevOps";
	
	/**
	 * Wrapper for System.currentTimeMillis() for easier mocking.
	 * @return
	 */
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}
	
	public boolean isAdmin(final OAuth2User user) {
		return user.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority).anyMatch(a -> ADMIN_GROUP.equals(a));
	}
	
	public String formatAuditLogDetails(final PermissionRequestEntity request) {
		return "permission-request for target-user '" + request.getRequestFor() + "' initially requested at '" +
				DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault()).format(request.getRequestDateTime()) 
				+ "' from '" + request.getRequestFrom() + 
				"' for products: " + request.getProducts().stream().map(ProductEntity::getName).collect(Collectors.joining(", "));
	}
	
	public Long parseDateTimestamp(final String value, final String pattern, final Long defaultValue) {
		try {
			try {
				return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(pattern)).toInstant(ZoneOffset.UTC).toEpochMilli();
			} catch (DateTimeParseException e) { // wasn't java.time supposed to make things easier? -.-
				// LocalDateTime.parse INSISTS on having a time-component in the string and pattern - you can't parse a date with it!!!
				return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern)).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
			}
		} catch (RuntimeException r) {
			return defaultValue;
		}
	}
	
	public Map<String, String> createSubcategoryMappingWithoutAccessMode(final String[] productCategories) {
		if(productCategories == null) {
			return Collections.emptyMap();
		}
		return Arrays.stream(productCategories).filter(StringUtils::isNotBlank).collect(Collectors.toMap(s->StringUtils.substringBeforeLast(s, "_"), s->s));
	}
	
	public boolean isUserCanEditRequest(final OAuth2User user, final Optional<PermissionRequestEntity> possibleRequest) {
		if(possibleRequest.isEmpty()) {
			return false;
		}
		return StringUtils.equalsIgnoreCase(user.getAttribute("email"), possibleRequest.get().getRequestFrom())
				&& RequestStatus.OPEN.name().equals(possibleRequest.get().getResolution());
	}
	
	public boolean isAdminCanEditRequest(final Optional<PermissionRequestEntity> possibleRequest) {
		if(possibleRequest.isEmpty()) {
			return false;
		}
		return RequestStatus.OPEN.name().equals(possibleRequest.get().getResolution());
	}
}

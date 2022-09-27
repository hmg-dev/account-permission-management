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
package dev.hmg.apm.integration.ado.service;

import dev.hmg.apm.db.entity.ConnectionDataEntity;
import org.azd.oauth.types.AuthorizedToken;
import org.springframework.stereotype.Service;

@Service
public class AdoConnectionSupportService {
	
	public AuthorizedToken createAuthorizedTokenFromConnectionData(final ConnectionDataEntity connectionData) {
		AuthorizedToken token = new AuthorizedToken();
		token.setTokenType(connectionData.getTokenType());
		token.setAccessToken(connectionData.getAccessToken());
		token.setRefreshToken(connectionData.getRefreshToken());
		token.setExpiresIn(connectionData.getExpiresIn());
		token.setReceivedTimestamp(connectionData.getCreationTimestamp());
		
		return token;
	}
	
}

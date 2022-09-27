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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AdoConnectionSupportServiceTest {
	private AdoConnectionSupportService sut;
	
	@Before
	public void init() {
		sut = new AdoConnectionSupportService();
	}
	
	@Test
	public void testCreateAuthorizedTokenFromConnectionData() {
		ConnectionDataEntity connectionData = new ConnectionDataEntity();
		connectionData.setRefreshToken("REFRESH_TOKEN");
		connectionData.setAccessToken("ACCESSS_TOKEN");
		connectionData.setKey(DatabaseTokenRefreshHandler.CONNECTION_KEY);
		connectionData.setExpiresIn(42);
		connectionData.setTokenType("jwt-bearer");
		connectionData.setCreationTimestamp(21);
		
		AuthorizedToken result = sut.createAuthorizedTokenFromConnectionData(connectionData);
		
		assertNotNull(result);
		assertEquals(connectionData.getTokenType(), result.getTokenType());
		assertEquals(connectionData.getAccessToken(), result.getAccessToken());
		assertEquals(connectionData.getRefreshToken(), result.getRefreshToken());
		assertEquals(connectionData.getExpiresIn(), result.getExpiresIn());
		assertEquals(connectionData.getCreationTimestamp(), result.getReceivedTimestamp());
	}
}

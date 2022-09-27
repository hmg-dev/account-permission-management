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

import dev.hmg.apm.db.ConnectionDataDAO;
import dev.hmg.apm.db.entity.ConnectionDataEntity;
import org.azd.oauth.types.AuthorizedToken;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseTokenRefreshHandlerTest {
    private DatabaseTokenRefreshHandler sut;

    @Mock
    private ConnectionDataDAO connectionDataDAO;
    @Captor
    private ArgumentCaptor<ConnectionDataEntity> connectionDataArgumentCaptor;

    @Before
    public void setUp() {
        sut = new DatabaseTokenRefreshHandler();
        sut.setConnectionDataDAO(connectionDataDAO);
    }
    
    @Test
    public void testTokenRefreshed_forEmptyRefreshToken() {
        AuthorizedToken authorizedToken = new AuthorizedToken();
        sut.tokenRefreshed(authorizedToken);
        
        verifyNoInteractions(connectionDataDAO);
    }
    
    @Test
    public void testTokenRefreshed() {
        ConnectionDataEntity connectionData = new ConnectionDataEntity();
        connectionData.setRefreshToken("REFRESH_TOKEN");
        connectionData.setAccessToken("ACCESSS_TOKEN");
        connectionData.setKey(DatabaseTokenRefreshHandler.CONNECTION_KEY);
        connectionData.setExpiresIn(42);
        connectionData.setTokenType("jwt-bearer");

        AuthorizedToken authorizedToken = new AuthorizedToken();
        authorizedToken.setAccessToken("NEW_ACCESS_TOKEN");
        authorizedToken.setRefreshToken("NEW_REFRESH_TOKEN");
        authorizedToken.setTokenType("jwt-bearer");
        authorizedToken.setExpiresIn(666);
        authorizedToken.setReceivedTimestamp(1729);

        given(connectionDataDAO.findByKey(DatabaseTokenRefreshHandler.CONNECTION_KEY)).willReturn(connectionData);

        sut.tokenRefreshed(authorizedToken);

        verify(connectionDataDAO, times(1)).findByKey(DatabaseTokenRefreshHandler.CONNECTION_KEY);
        verify(connectionDataDAO, times(1)).save(connectionDataArgumentCaptor.capture());

        ConnectionDataEntity result = connectionDataArgumentCaptor.getValue();
        assertNotNull(result);
        assertEquals(authorizedToken.getAccessToken(), result.getAccessToken());
        assertEquals(authorizedToken.getRefreshToken(), result.getRefreshToken());
        assertEquals(connectionData.getTokenType(), result.getTokenType());
        assertEquals(authorizedToken.getExpiresIn(), result.getExpiresIn());
        assertEquals(connectionData.getKey(), result.getKey());
        assertEquals(authorizedToken.getReceivedTimestamp(), result.getCreationTimestamp());
    }
}

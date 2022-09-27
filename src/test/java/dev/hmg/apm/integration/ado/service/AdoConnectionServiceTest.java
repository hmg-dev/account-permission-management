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

import dev.hmg.apm.config.AppConfig;
import dev.hmg.apm.db.ConnectionDataDAO;
import dev.hmg.apm.db.entity.ConnectionDataEntity;
import org.azd.connection.Connection;
import org.azd.oauth.types.AuthorizedToken;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AdoConnectionServiceTest {
    private AdoConnectionService sut;
    
    @Mock
    private AdoConnectionSupportService connectionSupportService;
    @Mock
    private ConnectionDataDAO connectionDataDAO;
    @Mock
    private AppConfig appConfig;

    @Before
    public void setUp() {
        sut = new AdoConnectionService();
        sut.setAppConfig(appConfig);
        sut.setConnectionSupportService(connectionSupportService);
        sut.setConnectionDataDAO(connectionDataDAO);
    }

    @Test
    public void testConnectionData() {
        String dummyOrgURL = "NARF";
        String dummyCallbackURL = "ZORT";
        ConnectionDataEntity connectionData = new ConnectionDataEntity();
        AuthorizedToken authToken = new AuthorizedToken();

        given(connectionSupportService.createAuthorizedTokenFromConnectionData(connectionData)).willReturn(authToken);
        given(appConfig.getAdoOrganizationUrl()).willReturn(dummyOrgURL);
        given(appConfig.getAdoCallbackUrl()).willReturn(dummyCallbackURL);
        given(connectionDataDAO.findByKey(DatabaseTokenRefreshHandler.CONNECTION_KEY)).willReturn(connectionData);

        Connection result = sut.connectionData();
        assertNotNull(result);
        assertEquals(dummyOrgURL, result.getOrganization());

        verify(connectionSupportService, times(1)).createAuthorizedTokenFromConnectionData(connectionData);
        verify(connectionDataDAO, times(1)).findByKey(DatabaseTokenRefreshHandler.CONNECTION_KEY);
        verify(appConfig, times(1)).getAdoOrganizationUrl();
        verify(appConfig, times(1)).getAdoCallbackUrl();
        verify(appConfig, times(1)).getAdoClientSecret();

    }
}

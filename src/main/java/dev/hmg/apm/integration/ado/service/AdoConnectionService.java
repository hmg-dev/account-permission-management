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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdoConnectionService {

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private AdoConnectionSupportService connectionSupportService;
    @Autowired
    private ConnectionDataDAO connectionDataDAO;
    @Autowired
    private DatabaseTokenRefreshHandler databaseTokenRefreshHandler;

    public Connection connectionData() {
        ConnectionDataEntity connectionData = connectionDataDAO.findByKey(DatabaseTokenRefreshHandler.CONNECTION_KEY);
        AuthorizedToken authorizedToken = connectionSupportService.createAuthorizedTokenFromConnectionData(connectionData);
        
        return new Connection(appConfig.getAdoOrganizationUrl(), authorizedToken,
                appConfig.getAdoClientSecret(), appConfig.getAdoCallbackUrl(), databaseTokenRefreshHandler);
    }

    public void setAppConfig(final AppConfig appConfig) {
        this.appConfig = appConfig;
    }
    
    public void setConnectionSupportService(final AdoConnectionSupportService connectionSupportService) {
        this.connectionSupportService = connectionSupportService;
    }
    
    public void setConnectionDataDAO(final ConnectionDataDAO connectionDataDAO) {
        this.connectionDataDAO = connectionDataDAO;
    }
    
    public void setDatabaseTokenRefreshHandler(final DatabaseTokenRefreshHandler databaseTokenRefreshHandler) {
        this.databaseTokenRefreshHandler = databaseTokenRefreshHandler;
    }
}

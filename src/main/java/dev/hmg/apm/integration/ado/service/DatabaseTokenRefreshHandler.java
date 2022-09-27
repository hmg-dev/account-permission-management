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
import org.apache.commons.lang3.StringUtils;
import org.azd.connection.Connection;
import org.azd.oauth.types.AuthorizedToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatabaseTokenRefreshHandler implements Connection.TokenRefreshedHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public static final String CONNECTION_KEY = "ADO_TOKEN";

    @Autowired
    private ConnectionDataDAO connectionDataDAO;

    @Override
    public void tokenRefreshed(final AuthorizedToken authorizedToken) {
        if(StringUtils.isBlank(authorizedToken.getRefreshToken())) {
            log.warn("received empty refresh-token - will not update DB!");
            return;
        }
        
        ConnectionDataEntity connectionData = connectionDataDAO.findByKey(CONNECTION_KEY);
        connectionData.setRefreshToken(authorizedToken.getRefreshToken());
        connectionData.setAccessToken(authorizedToken.getAccessToken());
        connectionData.setExpiresIn(authorizedToken.getExpiresIn());
        connectionData.setCreationTimestamp(authorizedToken.getReceivedTimestamp());

        connectionDataDAO.save(connectionData);
    }

    public void setConnectionDataDAO(final ConnectionDataDAO connectionDataDAO) {
        this.connectionDataDAO = connectionDataDAO;
    }
}

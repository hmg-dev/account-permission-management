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
package dev.hmg.apm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@Configuration
@ConfigurationProperties("data")
@EnableScheduling
public class AppConfig {
    private String adoCallbackUrl;
    private String adoClientSecret;
    private String adoOrganizationUrl;
    
    private boolean devMode;
    private List<String> devModeMailSuffixes;
    private List<String> privilegedMailSuffixes;
    private List<String> regularAllowedMailSuffixes;
    
    private List<String> notificationRecipients;
    private String notificationSender;
    private boolean notificationsEnabled;
    
    private int showSoonExpiringPermissionsInNextDays;
    private int entriesPerPage;
    private int pageNumbersInPaginationBar;

    public String getAdoCallbackUrl() {
        return adoCallbackUrl;
    }

    public void setAdoCallbackUrl(final String adoCallbackUrl) {
        this.adoCallbackUrl = adoCallbackUrl;
    }

    public String getAdoClientSecret() {
        return adoClientSecret;
    }

    public void setAdoClientSecret(final String adoClientSecret) {
        this.adoClientSecret = adoClientSecret;
    }

    public String getAdoOrganizationUrl() {
        return adoOrganizationUrl;
    }

    public void setAdoOrganizationUrl(final String adoOrganizationUrl) {
        this.adoOrganizationUrl = adoOrganizationUrl;
    }
    
    public boolean isDevMode() {
        return devMode;
    }
    
    public void setDevMode(final boolean devMode) {
        this.devMode = devMode;
    }
    
    public List<String> getDevModeMailSuffixes() {
        return devModeMailSuffixes;
    }
    
    public void setDevModeMailSuffixes(final List<String> devModeMailSuffixes) {
        this.devModeMailSuffixes = devModeMailSuffixes;
    }
    
    public List<String> getNotificationRecipients() {
        return notificationRecipients;
    }
    
    public void setNotificationRecipients(final List<String> notificationRecipients) {
        this.notificationRecipients = notificationRecipients;
    }
    
    public String getNotificationSender() {
        return notificationSender;
    }
    
    public void setNotificationSender(final String notificationSender) {
        this.notificationSender = notificationSender;
    }
    
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
    
    public void setNotificationsEnabled(final boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
    
    public int getShowSoonExpiringPermissionsInNextDays() {
        return showSoonExpiringPermissionsInNextDays;
    }
    
    public void setShowSoonExpiringPermissionsInNextDays(final int showSoonExpiringPermissionsInNextDays) {
        this.showSoonExpiringPermissionsInNextDays = showSoonExpiringPermissionsInNextDays;
    }
    
    public int getEntriesPerPage() {
        return entriesPerPage;
    }
    
    public void setEntriesPerPage(final int entriesPerPage) {
        this.entriesPerPage = entriesPerPage;
    }
    
    public int getPageNumbersInPaginationBar() {
        return pageNumbersInPaginationBar;
    }
    
    public void setPageNumbersInPaginationBar(final int pageNumbersInPaginationBar) {
        this.pageNumbersInPaginationBar = pageNumbersInPaginationBar;
    }
    
    public void setPrivilegedMailSuffixes(final List<String> privilegedMailSuffixes) {
        this.privilegedMailSuffixes = privilegedMailSuffixes;
    }
    
    public List<String> getPrivilegedMailSuffixes() {
        return privilegedMailSuffixes;
    }
    
    public List<String> getRegularAllowedMailSuffixes() {
        return regularAllowedMailSuffixes;
    }
    
    public void setRegularAllowedMailSuffixes(final List<String> regularAllowedMailSuffixes) {
        this.regularAllowedMailSuffixes = regularAllowedMailSuffixes;
    }
}

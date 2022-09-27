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
package dev.hmg.apm.cron;

import dev.hmg.apm.config.AppConfig;
import dev.hmg.apm.db.UserProductAssignmentDAO;
import dev.hmg.apm.db.entity.UserProductAssignment;
import dev.hmg.apm.service.NotificationService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ExpiringPermissionsNotificationTask {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public static final int DAYS_UNTIL_EXPIRATION = 1;
	
	@Autowired
	private AppConfig appConfig;
	@Autowired
	private UserProductAssignmentDAO userProductAssignmentDAO;
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private MessageSource messageSource;
	
	@Scheduled(cron = "0 0 8,15 * * *")
	public void checkAndNotify() {
		if(!appConfig.isNotificationsEnabled()) {
			log.debug("Notifications are disabled.");
			return;
		}
		List<UserProductAssignment> matches = userProductAssignmentDAO.findAssignmentsExpiringInDays(DAYS_UNTIL_EXPIRATION);
		if(CollectionUtils.isEmpty(matches)) {
			log.debug("No expiring permissions in the next {} days", DAYS_UNTIL_EXPIRATION);
			return;
		}
		String userListing = matches.stream().map(upa -> upa.getUser().geteMail()).distinct().collect(Collectors.joining("\n"));
		String subject = messageSource.getMessage("notification.permissions.expire.subject", null, Locale.GERMAN);
		String message = messageSource.getMessage("notification.permissions.expire.text", new Object[]{userListing}, Locale.GERMAN);
		
		log.debug("Found {} expiring permissions in the next {} days", matches.size(), DAYS_UNTIL_EXPIRATION);
		notificationService.sendAdminNotificationAsync(subject, message);
	}
	
	public void setAppConfig(final AppConfig appConfig) {
		this.appConfig = appConfig;
	}
	
	public void setNotificationService(final NotificationService notificationService) {
		this.notificationService = notificationService;
	}
	
	public void setUserProductAssignmentDAO(final UserProductAssignmentDAO userProductAssignmentDAO) {
		this.userProductAssignmentDAO = userProductAssignmentDAO;
	}
	
	public void setMessageSource(final MessageSource messageSource) {
		this.messageSource = messageSource;
	}
}

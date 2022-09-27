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
import dev.hmg.apm.db.entity.UserEntity;
import dev.hmg.apm.db.entity.UserProductAssignment;
import dev.hmg.apm.service.NotificationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExpiringPermissionsNotificationTaskTest {
	private ExpiringPermissionsNotificationTask sut;
	
	@Mock
	private AppConfig appConfig;
	@Mock
	private UserProductAssignmentDAO userProductAssignmentDAO;
	@Mock
	private NotificationService notificationService;
	@Mock
	private MessageSource messageSource;
	
	@Before
	public void init() {
		sut = new ExpiringPermissionsNotificationTask();
		sut.setAppConfig(appConfig);
		sut.setUserProductAssignmentDAO(userProductAssignmentDAO);
		sut.setNotificationService(notificationService);
		sut.setMessageSource(messageSource);
	}
	
	@Test
	public void testCheckAndNotify_forNotificationsDisabled() {
		given(appConfig.isNotificationsEnabled()).willReturn(false);
		
		sut.checkAndNotify();
		
		verify(appConfig, times(1)).isNotificationsEnabled();
		verifyNoInteractions(userProductAssignmentDAO, notificationService, messageSource);
	}
	
	@Test
	public void testCheckAndNotify_forNoMatchingPermissions() {
		given(appConfig.isNotificationsEnabled()).willReturn(true);
		
		sut.checkAndNotify();
		
		verify(appConfig, times(1)).isNotificationsEnabled();
		verify(userProductAssignmentDAO, times(1)).findAssignmentsExpiringInDays(ExpiringPermissionsNotificationTask.DAYS_UNTIL_EXPIRATION);
		verifyNoInteractions(notificationService, messageSource);
	}
	
	@Test
	public void testCheckAndNotify() {
		List<UserProductAssignment> assignments = createDummyUserProductAssignments();
		String messageSubject = "Test";
		String messageText = "Message";
		String expectedMessageParam = "test.user@company.tld\nother.user@company.tld";
		
		given(appConfig.isNotificationsEnabled()).willReturn(true);
		given(userProductAssignmentDAO.findAssignmentsExpiringInDays(ExpiringPermissionsNotificationTask.DAYS_UNTIL_EXPIRATION)).willReturn(assignments);
		given(messageSource.getMessage("notification.permissions.expire.subject", null, Locale.GERMAN)).willReturn(messageSubject);
		given(messageSource.getMessage(eq("notification.permissions.expire.text"), any(), eq(Locale.GERMAN))).willReturn(messageText);
		
		sut.checkAndNotify();
		
		verify(appConfig, times(1)).isNotificationsEnabled();
		verify(userProductAssignmentDAO, times(1)).findAssignmentsExpiringInDays(ExpiringPermissionsNotificationTask.DAYS_UNTIL_EXPIRATION);
		verify(notificationService, times(1)).sendAdminNotificationAsync(messageSubject, messageText);
		verify(messageSource, times(1)).getMessage("notification.permissions.expire.subject", null, Locale.GERMAN);
		verify(messageSource, times(1)).getMessage("notification.permissions.expire.text", new Object[]{expectedMessageParam}, Locale.GERMAN);
	}
	
	private List<UserProductAssignment> createDummyUserProductAssignments() {
		UserProductAssignment a1 = new UserProductAssignment();
		UserProductAssignment a2 = new UserProductAssignment();
		UserProductAssignment a3 = new UserProductAssignment();
		UserEntity u1 = new UserEntity();
		UserEntity u2 = new UserEntity();
		UserEntity u3 = new UserEntity();
		
		u1.seteMail("test.user@company.tld");
		u2.seteMail("other.user@company.tld");
		u3.seteMail("test.user@company.tld");
		
		a1.setUser(u1);
		a2.setUser(u2);
		a3.setUser(u3);
		
		return Arrays.asList(a1, a2, a3);
	}
}

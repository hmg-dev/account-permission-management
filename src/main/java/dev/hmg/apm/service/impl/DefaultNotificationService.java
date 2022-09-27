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
package dev.hmg.apm.service.impl;

import dev.hmg.apm.config.AppConfig;
import dev.hmg.apm.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Service
public class DefaultNotificationService implements NotificationService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private AppConfig appConfig;
	@Autowired
	private JavaMailSender javaMailSender;
	@Autowired
	private ExecutorService executorService;

	@Override
	public void sendAdminNotificationAsync(final String subject, final String text) {
		sendNotificationAsync(subject, text, appConfig.getNotificationRecipients().toArray(new String[]{}));
	}
	
	@Override
	public void sendNotificationAsync(final String subject, final String text, final String... recipients) {
		executorService.execute(() -> {
			try {
				sendNotification(subject, text, recipients);
			} catch (Exception e) {
				log.error("Sending async notification failed!", e);
			}
		});
	}
	
	@Override
	public void sendAdminNotification(final String subject, final String text) {
		sendNotification(subject, text, appConfig.getNotificationRecipients().toArray(new String[]{}));  // FIXME: verify
	}
	
	@Override
	public void sendNotification(final String subject, final String text, final String... recipients) {
		if(!appConfig.isNotificationsEnabled()) {
			log.debug("Notifications are disabled.");
			return;
		}

		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setFrom(appConfig.getNotificationSender());
		msg.setTo(recipients);
		msg.setSubject(subject);
		msg.setText(text);

		log.info("Sending notification...");
		javaMailSender.send(msg);
	}
	
	public void setAppConfig(final AppConfig appConfig) {
		this.appConfig = appConfig;
	}

	public void setJavaMailSender(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}
}

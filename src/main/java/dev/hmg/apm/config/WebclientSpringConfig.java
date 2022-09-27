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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;

import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@ComponentScan({"dev.hmg.apm"})
@EnableConfigurationProperties
public class WebclientSpringConfig implements WebMvcConfigurer {
	
	@Value("${spring.mail.host}")
	private String mailServerHost;
	
	@Value("${spring.mail.port}")
	private Integer mailServerPort;
	
	@Value("${spring.mail.username}")
	private String mailServerUsername;
	
	@Value("${spring.mail.password}")
	private String mailServerPassword;
	
	@Value("${spring.mail.properties.mail.smtp.starttls.enable}")
	private String mailServerStartTls;
	
	@Value("${spring.mail.properties.mail.debug}")
	private String mailDebug;
	
	@Bean
	public Java8TimeDialect java8TimeDialect() {
		return new Java8TimeDialect();
	}
	
	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver slr = new SessionLocaleResolver();
		slr.setDefaultLocale(Locale.GERMAN);
		return slr;
	}
	
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
		lci.setParamName("lang");
		return lci;
	}
	
	@Bean
	public JavaMailSender javaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(mailServerHost);
		mailSender.setPort(mailServerPort);
		
		mailSender.setUsername(mailServerUsername);
		mailSender.setPassword(mailServerPassword);
		
		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", mailServerStartTls);
		props.put("mail.debug", mailDebug);
		
		return mailSender;
	}

	@Bean
	public ExecutorService executorService() {
		return Executors.newFixedThreadPool(5);
	}

	@Override
	public void addInterceptors(final InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
	}
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/login").setViewName("login");
		registry.addViewController("/error").setViewName("error");
	}
	
	public void setMailServerHost(final String mailServerHost) {
		this.mailServerHost = mailServerHost;
	}
	
	public void setMailServerPort(final Integer mailServerPort) {
		this.mailServerPort = mailServerPort;
	}
	
	public void setMailServerUsername(final String mailServerUsername) {
		this.mailServerUsername = mailServerUsername;
	}
	
	public void setMailServerPassword(final String mailServerPassword) {
		this.mailServerPassword = mailServerPassword;
	}
	
	public void setMailServerStartTls(final String mailServerStartTls) {
		this.mailServerStartTls = mailServerStartTls;
	}
	
	public void setMailDebug(final String mailDebug) {
		this.mailDebug = mailDebug;
	}
}

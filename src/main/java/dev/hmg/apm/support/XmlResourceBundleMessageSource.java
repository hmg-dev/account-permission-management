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
package dev.hmg.apm.support;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

@Component("messageSource")
public class XmlResourceBundleMessageSource extends ResourceBundleMessageSource {
	
	public XmlResourceBundleMessageSource() {
		this.setBasename("messages");
		this.setDefaultEncoding("UTF-8");
	}
	
	@Nullable
	private volatile XmlResourceBundleMessageSource.XMLResourceBundleControl control = new XMLResourceBundleControl();
	
	@Override
	protected ResourceBundle loadBundle(final Reader reader) throws IOException {
		return new XmlPropertyResourceBundle(reader);
	}
	
	@Override
	protected ResourceBundle loadBundle(final InputStream inputStream) throws IOException {
		return new XmlPropertyResourceBundle(inputStream);
	}
	
	@Override
	protected ResourceBundle doGetBundle(final String basename, final Locale locale) throws MissingResourceException {
		ClassLoader classLoader = this.getBundleClassLoader();
		Assert.state(classLoader != null, "No bundle ClassLoader set");
		XmlResourceBundleMessageSource.XMLResourceBundleControl control = this.control;
		if (control != null) {
			try {
				return ResourceBundle.getBundle(basename, locale, classLoader, control);
			} catch (UnsupportedOperationException var7) {
				this.control = null;
				String encoding = this.getDefaultEncoding();
				if (encoding != null && this.logger.isInfoEnabled()) {
					this.logger.info("ResourceBundleMessageSource is configured to read resources with encoding '" + encoding + 
							"' but ResourceBundle.Control not supported in current system environment: " + var7.getMessage() + 
							" - falling back to plain ResourceBundle.getBundle retrieval with the platform default encoding. " +
							"Consider setting the 'defaultEncoding' property to 'null' for participating in the platform default and therefore avoiding this log message.");
				}
			}
		}
		
		return ResourceBundle.getBundle(basename, locale, classLoader);
	}
	
	private class XMLResourceBundleControl extends ResourceBundle.Control {
		final String XML = "xml";
		final List<String> SINGLETON_LIST = Collections.singletonList(XML);
		
		@Override
		public ResourceBundle newBundle(final String baseName, final Locale locale, final String format, final ClassLoader loader, final boolean reload)
				throws IllegalAccessException, InstantiationException, IOException {
			
			if ((baseName == null) || (locale == null) || (format == null) || (loader == null)) {
				throw new IllegalArgumentException("baseName, locale, format and loader cannot be null");
			}
			if (!format.equals(XML)) {
				throw new IllegalArgumentException("format must be xml");
			}
			
			final String bundleName = toBundleName(baseName, locale);
			final String resourceName = toResourceName(bundleName, format);
			final URL url = loader.getResource(resourceName);
			if (url == null) {
				return null;
			}
			
			final URLConnection urlconnection = url.openConnection();
			if (urlconnection == null) {
				return null;
			}
			if (reload) {
				urlconnection.setUseCaches(false);
			}
			
			try (final InputStream stream = urlconnection.getInputStream()) {
				return loadBundle(stream);
			}
		}
		
		@Override
		public List<String> getFormats(String baseName) {
			return SINGLETON_LIST;
		}
		
	}
}

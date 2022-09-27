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

import org.apache.commons.io.input.ReaderInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class XmlPropertyResourceBundle extends ResourceBundle {
	
	private final Map<String,Object> lookup;
	
	@SuppressWarnings("unchecked")
	public XmlPropertyResourceBundle(final InputStream inputStream) throws IOException {
		Properties properties = new Properties();
		properties.loadFromXML(inputStream);
		lookup = new HashMap(properties);
	}
	
	@SuppressWarnings("unchecked")
	public XmlPropertyResourceBundle(final Reader reader) throws IOException {
		Properties properties = new Properties();
		properties.loadFromXML(new ReaderInputStream(reader, StandardCharsets.UTF_8));
		lookup = new HashMap(properties);
	}
	
	@Override
	protected Object handleGetObject(final String key) {
		if (key == null) {
			throw new NullPointerException();
		}
		return lookup.get(key);
	}
	
	@Override
	public Enumeration<String> getKeys() {
		Set<String> handleKeys = lookup.keySet();
		return Collections.enumeration(handleKeys);
	}
	
	protected Set<String> handleKeySet() {
		return lookup.keySet();
	}
}

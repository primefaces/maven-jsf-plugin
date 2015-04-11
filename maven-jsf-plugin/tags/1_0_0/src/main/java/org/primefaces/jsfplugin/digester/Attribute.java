/*
 * Copyright 2009 Prime Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.jsfplugin.digester;

import org.apache.commons.lang.StringUtils;

public class Attribute {

	private String name;
	private String required;;
	private String type;
	private String description;
	private String defaultValue;
	private String ignoreInComponent = "false";
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getRequired() {
		return required;
	}
	public void setRequired(String required) {
		this.required = required;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getDefaultValue() {
		if(StringUtils.isBlank(defaultValue))
			return "null";
		
		if(type.equals("java.lang.String"))
			return "\"" + defaultValue + "\"";
			
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public String getIgnoreInComponent() {
		return ignoreInComponent;
	}
	public void setIgnoreInComponent(String ignoreInComponent) {
		this.ignoreInComponent = ignoreInComponent;
	}
	
	/**
	 * Gives the short name of the attribute
	 * e.g. java.lang.String will return String
	 */
	public String getShortTypeName() {
		String[] list = type.split("\\.");
		return list[list.length-1];
	}
	
	public String getCapitalizedName() {
		return StringUtils.capitalize(name);
	}
	
	public String getCapitalizedType() {
		return StringUtils.capitalize(getShortTypeName());
	}
	
	public boolean isIgnored() {
		return Boolean.valueOf(ignoreInComponent).booleanValue();
	}
}
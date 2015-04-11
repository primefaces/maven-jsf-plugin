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
package org.primefaces.jsfplugin.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.primefaces.jsfplugin.digester.Attribute;
import org.primefaces.jsfplugin.digester.Component;

import org.apache.commons.lang.StringUtils;

public class FacesMojoUtils {

	public static Map<String,String> wrapperMap;					//primitives and corresponding wrappers
	
	static {
		wrapperMap = new HashMap<String,String>();
		wrapperMap.put("java.lang.Integer", "int");
		wrapperMap.put("java.lang.Double", "double");
		wrapperMap.put("java.lang.Boolean", "boolean");
	}
	
	public static String getWrapperType(String type) {
		String wrapperType = (String) wrapperMap.get(type);
		
		if(StringUtils.isBlank(wrapperType))
			return type;					//if none found just return the same type
		else
			return wrapperType;
	}
	
	public static boolean shouldWrap(String attributeType) {
		return wrapperMap.containsKey(attributeType);
	}
	
	/**
	 * Calculates array size to be allocated when saving the state
	 * 
	 * @param component
	 * @return Array size to be used for state saving
	 */
	public static int getStateAllocationSize(Component component) {
		int size = 0;
		
		for (Iterator<Attribute> iterator = component.getAttributes().iterator(); iterator.hasNext();) {
			Attribute attribute= iterator.next();
			
			if(!attribute.isIgnored())
				size++;
		}
		
		return size;
	}
}
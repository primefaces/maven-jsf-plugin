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
package org.primefaces.jsfplugin.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;

import org.primefaces.jsfplugin.digester.Attribute;
import org.primefaces.jsfplugin.digester.Component;
import org.primefaces.jsfplugin.util.FacesMojoUtils;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal generate-tld
 */
public class TLDMojo extends BaseFacesMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Generating TLD");
		
		writeTLD(getComponents());
		
		getLog().info("TLD Generated successfully");
	}
	
	private void writeTLD(List components) {
		FileWriter fileWriter;
		BufferedWriter writer;
		String outputPath = project.getBuild().getOutputDirectory() + File.separator + "META-INF";
		String outputFile =  "primefaces-ui.tld";
		
		try {
			File tldDirectory = new File(outputPath);
			tldDirectory.mkdirs();
			
			fileWriter = new FileWriter(outputPath + File.separator + outputFile);	
			writer = new BufferedWriter(fileWriter);
			
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			writer.write("<taglib xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-jsptaglibrary_2_1.xsd\" xmlns=\"http://java.sun.com/xml/ns/javaee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"2.1\">\n");
			writer.write("\t<tlib-version>1.2</tlib-version>\n");
			writer.write("\t<short-name>p</short-name>\n");
			writer.write("\t<uri>http://primefaces.prime.com.tr/ui</uri>\n");
			
			for (Iterator iterator = components.iterator(); iterator.hasNext();) {
				Component component = (Component) iterator.next();
				writer.write("\t<tag>\n");
				writer.write("\t\t<name>" + component.getTag() + "</name>\n");
				writer.write("\t\t<tag-class>" + component.getTagClass() + "</tag-class>\n");
				writer.write("\t\t<body-content>JSP</body-content>\n");
				
				for (Iterator iterator2 = component.getAttributes().iterator(); iterator2.hasNext();) {
					Attribute attribute = (Attribute) iterator2.next();
					
					writer.write("\t\t<attribute>\n");
					writer.write("\t\t\t<name>" + attribute.getName() + "</name>\n");
					writer.write("\t\t\t<required>" + attribute.isRequired() + "</required>\n");
					if(attribute.getDescription() != null)
						writer.write("\t\t\t<description><![CDATA[" + attribute.getDescription() + "]]></description>\n");
					
					if(attribute.isLiteral()) {
						 writer.write("\t\t\t<rtexprvalue>false</rtexprvalue>\n");
					} else {
						if(attribute.isDeferredValue()) {
							writer.write("\t\t\t<deferred-value>\n");
							writer.write("\t\t\t\t<type>");
							writer.write(FacesMojoUtils.toPrimitive(attribute.getType()));
							writer.write("</type>\n");
							writer.write("\t\t\t</deferred-value>\n");
						} else {
							writer.write("\t\t\t<deferred-method>\n");
							writer.write("\t\t\t\t<method-signature>");
							writer.write(attribute.getMethodSignature());
							writer.write("</method-signature>\n");
							writer.write("\t\t\t</deferred-method>\n");
						}
					}

					writer.write("\t\t</attribute>\n");
				}
				writer.write("\t</tag>\n");
			}
			writer.write("</taglib>");
			writer.close();
		}catch(Exception exception) {
			getLog().error( exception.getMessage() );
		}
	}	
}
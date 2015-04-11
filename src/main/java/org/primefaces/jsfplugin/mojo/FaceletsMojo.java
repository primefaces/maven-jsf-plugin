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
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.primefaces.jsfplugin.digester.Component;

/**
 * @goal generate-facelets-taglib
 */
public class FaceletsMojo extends BaseFacesMojo{

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Generating facelets-taglib");
		
		try {
			writeFaceletsTaglib(getComponents());
			getLog().info("facelets-taglib generated successfully");
		} catch (Exception e) {
			getLog().info("Exception in generating facelets-taglib:");
			getLog().info(e.toString());
		}
	}

	private void writeFaceletsTaglib(List components) throws IOException{
		FileWriter fileWriter;
		BufferedWriter writer;
		String outputPath = project.getBuild().getOutputDirectory() + File.separator + "META-INF";
		String outputFile =  "primefaces-ui.taglib.xml";
		
		File outputDirectory = new File(outputPath);
		if(!outputDirectory.exists())
			outputDirectory.mkdirs();
		
		fileWriter = new FileWriter(outputPath + File.separator + outputFile);	
		writer = new BufferedWriter(fileWriter);
		
		writer.write("<?xml version=\"1.0\"?>\n");
		writer.write("<!DOCTYPE facelet-taglib PUBLIC \"-//Sun Microsystems, Inc.//DTD Facelet\n");
		writer.write("Taglib 1.0//EN\" \"facelet-taglib_1_0.dtd\">\n");
		writer.write("<facelet-taglib>\n\n");
		writer.write("\t<namespace>http://primefaces.prime.com.tr/ui</namespace>\n\n");
		
		for (Iterator iterator = components.iterator(); iterator.hasNext();) {
			Component component = (Component) iterator.next();
			writer.write("\t<tag>\n");
			writer.write("\t\t<tag-name>");
			writer.write(component.getTag());
			writer.write("</tag-name>\n");
			writer.write("\t\t<component>\n");
			writer.write("\t\t\t<component-type>");
			writer.write(component.getComponentType());
			writer.write("</component-type>\n");
			
			if(component.getRendererType() != null) {
				writer.write("\t\t\t<renderer-type>");
				writer.write(component.getRendererType());
				writer.write("</renderer-type>\n");
			}
			
			if(component.getComponentHandlerClass() != null) {
				writer.write("\t\t\t<handler-class>");
				writer.write(component.getComponentHandlerClass());
				writer.write("</handler-class>\n");
			}
			writer.write("\t\t</component>\n");
			writer.write("\t</tag>\n");
		}
		
		writer.write("</facelet-taglib>\n");
		
		writer.close();
		fileWriter.close();
	}
}
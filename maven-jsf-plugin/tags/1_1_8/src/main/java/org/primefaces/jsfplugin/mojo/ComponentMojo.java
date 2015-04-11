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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.primefaces.jsfplugin.digester.Attribute;
import org.primefaces.jsfplugin.digester.Component;
import org.primefaces.jsfplugin.digester.Interface;
import org.primefaces.jsfplugin.digester.Resource;
import org.primefaces.jsfplugin.util.FacesMojoUtils;

/**
 * @goal generate-components
 */
public class ComponentMojo extends BaseFacesMojo{
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Generating Components");
		
		try {
			writeComponents(getComponents());
			getLog().info("Components Generated successfully");
		} catch (Exception e) {
			getLog().info("Error occured in component generation:");
			getLog().info(e.toString());
		}
	}

	private void writeComponents(List<Component> components) throws Exception{
		
		String outputPath = getCreateOutputDirectory();
		
		for (Iterator<Component> iterator = components.iterator(); iterator.hasNext();) {
			Component component = (Component) iterator.next();
			getLog().info("Generating Component Source for:" + component.getComponentClass());
			
			String packagePath = createPackageDirectory(outputPath, component);
			
			FileWriter fileWriter = new FileWriter(packagePath + File.separator +  component.getComponentShortName() + ".java");	
			BufferedWriter writer = new BufferedWriter(fileWriter);
		
			writeComponent(writer, component);
			
			writer.close();
			fileWriter.close();
		}
	}

	private void writeComponent(BufferedWriter writer, Component component) throws IOException {
		writeLicense(writer);
		writePackage(writer, component);
		writeImports(writer, component);
		writeClassDeclaration(writer, component);
		writeComponentProperties(writer, component);
		writeAttributesDeclarations(writer, component);
		writeConstructor(writer, component);
		writeComponentFamily(writer, component);
		writeAttributes(writer, component);
		writeTemplate(writer, component);
		writeFacesContextGetter(writer);

        if(component.isWidget()) {
            writeWidgetVarResolver(writer);
        }
		
		if(isJSF2()) {
			writerAttributeHandler(writer);
		} else {
			writeSaveState(writer, component);
			writeRestoreState(writer, component);
			writeResourceHolderGetter(writer);
		}
		writer.write("}");
	}
	
	private void writerAttributeHandler(BufferedWriter writer) throws IOException {
		writer.write("\n\tpublic void handleAttribute(String name, Object value) {\n");
		writer.write("\t\tList<String> setAttributes = (List<String>) this.getAttributes().get(\"javax.faces.component.UIComponentBase.attributesThatAreSet\");\n");
		writer.write("\t\tif(setAttributes == null) {\n");
		writer.write("\t\t\tString cname = this.getClass().getName();\n");
		writer.write("\t\t\tif(cname != null && cname.startsWith(OPTIMIZED_PACKAGE)) {\n");
		writer.write("\t\t\t\tsetAttributes = new ArrayList<String>(6);\n");
		writer.write("\t\t\t\tthis.getAttributes().put(\"javax.faces.component.UIComponentBase.attributesThatAreSet\", setAttributes);\n");
		writer.write("\t\t\t}\n");
		writer.write("\t\t}\n");
		
		writer.write("\t\tif(setAttributes != null) {\n");
		writer.write("\t\t\tif(value == null) {\n");
		writer.write("\t\t\t\tValueExpression ve = getValueExpression(name);\n");
		writer.write("\t\t\t\tif(ve == null) {\n");
		writer.write("\t\t\t\t\tsetAttributes.remove(name);\n");
		writer.write("\t\t\t\t}");
		writer.write(" else if(!setAttributes.contains(name)) {\n");
		writer.write("\t\t\t\t\tsetAttributes.add(name);\n");
		writer.write("\t\t\t\t}\n");
		writer.write("\t\t\t}\n");
		writer.write("\t\t}\n");
		
		writer.write("\t}\n");
	}

	private void writeImports(BufferedWriter writer, Component component) throws IOException {
		writer.write("import " + component.getParent() + ";\n");
		writer.write("import javax.faces.context.FacesContext;\n");
        writer.write("import javax.faces.component.UINamingContainer;\n");
		writer.write("import javax.el.ValueExpression;\n");
		writer.write("import javax.el.MethodExpression;\n");
		writer.write("import javax.faces.render.Renderer;\n");
		writer.write("import java.io.IOException;\n");
		
		if(isJSF2()) {
			writer.write("import javax.faces.component.UIComponent;\n");
			writer.write("import javax.faces.event.AbortProcessingException;\n");
            writer.write("import javax.faces.application.ResourceDependencies;\n");
            writer.write("import javax.faces.application.ResourceDependency;\n");
            writer.write("import java.util.List;\n");
            writer.write("import java.util.ArrayList;\n");
		} else {
			writer.write("import org.primefaces.resource.ResourceHolder;\n");
		}
		
		String templateImports = getTemplateImports(component);
		
		if (StringUtils.isNotEmpty(templateImports)) {
			writer.write(templateImports);
		}
		writer.write("\n");
	}
	
	private void writeComponentProperties(BufferedWriter writer, Component component) throws IOException {
		writer.write("\tpublic static final String COMPONENT_TYPE = \"" + component.getComponentType() + "\";\n");
		writer.write("\tpublic static final String COMPONENT_FAMILY = \"" + component.getComponentFamily() + "\";\n");
		
		if(component.getRendererType() != null)
			writer.write("\tprivate static final String DEFAULT_RENDERER = \"" + component.getRendererType() + "\";\n");
		
		if(isJSF2()) {
			writer.write("\tprivate static final String OPTIMIZED_PACKAGE = \"org.primefaces.component.\";\n");
		}
		
		writer.write("\n");
	}
	
	private void writeAttributesDeclarations(BufferedWriter writer, Component component) throws IOException {
		boolean firstWritten = false;
		if(isJSF2()) {
			writer.write("\tprotected enum PropertyKeys {\n");
		}
		
		for(Iterator<Attribute> attributeIterator = component.getAttributes().iterator(); attributeIterator.hasNext();) {
			Attribute attribute = attributeIterator.next();
			if(attribute.isIgnored())
				continue;
			
			if(isJSF2()) {
				writer.write("\n");
				
				if(!firstWritten) {
					writer.write("\t\t");
					firstWritten = true;
				}
				else
					writer.write("\t\t,");
					
				if(attribute.getName().equals("for"))
					writer.write("forValue(\"for\")");
				else
					writer.write(attribute.getName());

			} else {
				writer.write("\tprivate " + attribute.getType() + " _" + attribute.getName() + ";\n");
			}
		}
		
		if(isJSF2()) {
			writer.write(";\n");
			
			writer.write("\n\t\tString toString;\n\n");
			 
			writer.write("\t\tPropertyKeys(String toString) {\n");
			writer.write("\t\t\tthis.toString = toString;\n");
			writer.write("\t\t}\n\n");
			
			writer.write("\t\tPropertyKeys() {}\n\n");
			
			writer.write("\t\tpublic String toString() {\n");
			writer.write("\t\t\treturn ((this.toString != null) ? this.toString : super.toString());");
			writer.write("\n}\n");

			writer.write("\t}\n\n");
		}
	}
	
	private void writeClassDeclaration(BufferedWriter writer, Component component) throws IOException {
		if(isJSF2()) {
            writer.write("@ResourceDependencies({\n");
            
            for (Iterator iterator = component.getResources().iterator(); iterator.hasNext();) {
                Resource resource = (Resource) iterator.next();

                writer.write("\t@ResourceDependency(library=\"primefaces\", name=\"" + resource.getName() + "\")");

                if(iterator.hasNext())
                    writer.write(",\n");
            }
            writer.write("\n})");
		}
		writer.write("\npublic class " + component.getComponentShortName() + " extends " + component.getParentShortName());
		
		if(!component.getInterfaces().isEmpty()) {
			writer.write(" implements ");
			
			for(Iterator<Interface> iterator = component.getInterfaces().iterator(); iterator.hasNext();) {
				Interface _interface = iterator.next();
				writer.write(_interface.getName());
				
				if(iterator.hasNext())
					writer.write(",");
			}
		}
		
		writer.write(" {\n");
		writer.write("\n\n");
	}
	
	private void writeConstructor(BufferedWriter writer, Component component) throws IOException {
		writer.write("\tpublic " + component.getComponentShortName() + "() {\n");
		
		if(component.getRendererType() != null)
			writer.write("\t\tsetRendererType(DEFAULT_RENDERER);\n");
		else
			writer.write("\t\tsetRendererType(null);\n");
		
		if(!isJSF2()) {
			writer.write("\t\tResourceHolder resourceHolder = getResourceHolder();\n");
			writer.write("\t\tif(resourceHolder != null) {\n");
			
			for (Iterator iterator = component.getResources().iterator(); iterator.hasNext();) {
				Resource resource = (Resource) iterator.next();
				
				writer.write("\t\t\tresourceHolder.addResource(\"" + resource.getName() + "\");\n");
			}
			
			writer.write("\t\t}\n");
		}
		
		writer.write("\t}");
		writer.write("\n\n");
	}
	
	private void writeComponentFamily(BufferedWriter writer, Component component) throws IOException {
		writer.write("\tpublic String getFamily() {\n");
		writer.write("\t\treturn COMPONENT_FAMILY;\n");
		writer.write("\t}");
		writer.write("\n\n");
	}
	
	private void writeAttributes(BufferedWriter writer, Component component) throws IOException {
		for (Iterator attributeIterator = component.getAttributes().iterator(); attributeIterator.hasNext();) {
			Attribute attribute = (Attribute) attributeIterator.next();
			if(attribute.isIgnored())
				continue;
			
			if(isJSF2()) {
				String propertyKeyName = attribute.getName().equalsIgnoreCase("for") ? "forValue" : attribute.getName();
				//getter
				if(FacesMojoUtils.shouldPrimitize(attribute.getType()))
					writer.write("\tpublic " + FacesMojoUtils.toPrimitive(attribute.getType()) + " " + resolveGetterPrefix(attribute) + attribute.getCapitalizedName() + "() {\n");
				else
					writer.write("\tpublic " + attribute.getType() + " " + resolveGetterPrefix(attribute) + attribute.getCapitalizedName() + "() {\n");
				
				if(attribute.getDefaultValue() == null)
					writer.write("\t\treturn (" + attribute.getType() + ") getStateHelper().eval(PropertyKeys." + propertyKeyName + ");\n");
				else
					writer.write("\t\treturn (" + attribute.getType() + ") getStateHelper().eval(PropertyKeys." + propertyKeyName + ", " + attribute.getDefaultValue() + ");\n");
				
				writer.write("\t}\n");
				
				//setter
				if(FacesMojoUtils.shouldPrimitize(attribute.getType()))
					writer.write("\tpublic void set" + attribute.getCapitalizedName() + "(" + FacesMojoUtils.toPrimitive(attribute.getType()) + " _" + attribute.getName() + ") {\n");
				else
					writer.write("\tpublic void set" + attribute.getCapitalizedName() + "(" + attribute.getType() + " _" + attribute.getName() + ") {\n");
				
				writer.write("\t\tgetStateHelper().put(PropertyKeys." + propertyKeyName + ", _" + attribute.getName() + ");\n");
				writer.write("\t\thandleAttribute(\"" + propertyKeyName + "\", _" + attribute.getName() + ");\n");
				
				writer.write("\t}\n\n");
			} else {
				if(isMethodExpression(attribute)) {
					writeMethodExpressionAttribute(writer, attribute);
				} else {
					if(FacesMojoUtils.shouldPrimitize(attribute.getType()))
						writer.write("\tpublic " + FacesMojoUtils.toPrimitive(attribute.getType()) + " " + resolveGetterPrefix(attribute) + attribute.getCapitalizedName() + "() {\n");
					else
						writer.write("\tpublic " + attribute.getType() + " " + resolveGetterPrefix(attribute) + attribute.getCapitalizedName() + "() {\n");
						
					writer.write("\t\tif(_" + attribute.getName() + " != null )\n");
					writer.write("\t\t\treturn _" + attribute.getName() + ";\n"); 
					writer.write("\n");
					
					writer.write("\t\tValueExpression ve = getValueExpression(\"" + attribute.getName() + "\");\n");
					writer.write("\t\treturn ve != null ? (" + attribute.getType() + ") ve.getValue(getFacesContext().getELContext())  : " + attribute.getDefaultValue() + ";\n");
					writer.write("\t}\n");
					
					if(FacesMojoUtils.shouldPrimitize(attribute.getType()))
						writer.write("\tpublic void set" + attribute.getCapitalizedName() + "(" + FacesMojoUtils.toPrimitive(attribute.getType()) + " _" + attribute.getName() + ") {\n");
					else
						writer.write("\tpublic void set" + attribute.getCapitalizedName() + "(" + attribute.getType() + " _" + attribute.getName() + ") {\n");
					
					writer.write("\t\tthis._" + attribute.getName() + " = _" + attribute.getName() + ";\n");
					
					writer.write("\t}\n\n");
				}
			}
		}
	}
	
	private void writeMethodExpressionAttribute(BufferedWriter writer, Attribute attribute) throws IOException {
		writer.write("\tpublic javax.el.MethodExpression get" + attribute.getCapitalizedName() + "() {\n");
		writer.write("\t\treturn this._" + attribute.getName() + ";\n");
		writer.write("\t}\n\n");
		
		writer.write("\tpublic void set" + attribute.getCapitalizedName() + "(javax.el.MethodExpression _" + attribute.getName() + ") {\n");
		writer.write("\t\tthis._" + attribute.getName() + " = _" + attribute.getName() + ";\n");
		writer.write("\t}\n");
	}
	
	private void writeSaveState(BufferedWriter writer, Component component) throws IOException {
		//ignore id,rendered,binding  
		int attributesToSave = FacesMojoUtils.getStateAllocationSize(component) + 1;
		int attributeNo = 1;
		
		writer.write("\tpublic Object saveState(FacesContext context) {\n");
		writer.write("\t\tObject values[] = new Object[" + attributesToSave + "];\n");
		writer.write("\t\tvalues[0] = super.saveState(context);\n");
		
		for (Iterator attributeIterator = component.getAttributes().iterator(); attributeIterator.hasNext();) {
			Attribute attribute = (Attribute) attributeIterator.next();
			if(attribute.isIgnored())
				continue;
			
			if(!isMethodBinding(attribute))
				writer.write("\t\tvalues[" + attributeNo + "] = _" + attribute.getName() +";\n");
			else
				writer.write("\t\tvalues[" + attributeNo + "] = saveAttachedState(context, _" + attribute.getName() + ");\n");
				
			attributeNo++;
		}
		
		writer.write("\t\treturn ((Object) values);\n");
		writer.write("\t}\n");
	}
	
	private void writeRestoreState(BufferedWriter writer, Component component) throws IOException {
		int attributeNo = 1;
		
		writer.write("\tpublic void restoreState(FacesContext context, Object state) {\n");
		writer.write("\t\tObject values[] = (Object[]) state;\n");
		writer.write("\t\tsuper.restoreState(context, values[0]);\n");
		
		for (Iterator attributeIterator = component.getAttributes().iterator(); attributeIterator.hasNext();) {
			Attribute attribute = (Attribute) attributeIterator.next();
			if(attribute.isIgnored())
				continue;
			
			if(!isMethodBinding(attribute))
				writer.write("\t\t_" + attribute.getName() + " = (" + attribute.getType() + ") values[" + attributeNo + "];\n");
			else
				writer.write("\t\t_" + attribute.getName() + " = (MethodBinding) restoreAttachedState(context, values[" + attributeNo + "]);\n");
				
			attributeNo++;
		}
		writer.write("\t}\n");
	}
	
	private void writePackage(BufferedWriter writer, Component component) throws IOException {
		writer.write("package " + component.getPackage() + ";\n\n");
	}
	
	private void writeTemplate(BufferedWriter writer, Component component) throws IOException{
		try {
			File template = getTemplate(component);
			FileReader fileReader = new FileReader(template);
			BufferedReader reader = new BufferedReader(fileReader);
			String line = null;
			
			getLog().info("Writing template for " + component.getComponentShortName());
			while((line = reader.readLine()) != null) {
				if (line.startsWith("import ")) continue;
				writer.write(line);
				writer.write("\n");
				
			}
		}catch(FileNotFoundException fileNotFoundException) {
			return;
		}
	}
	
	private String getTemplateImports(Component component) throws IOException {
		try {
			StringBuffer buf = new StringBuffer();
			File template = getTemplate(component);
			FileReader fileReader = new FileReader(template);
			BufferedReader reader = new BufferedReader(fileReader);
			String line = null;
			
			getLog().info("Looking for template imports of " + component.getComponentShortName());
			while(((line = reader.readLine()) != null) && (line.startsWith("import "))) {
				buf.append(line).append("\n");
			}
			
			return buf.toString();
		}catch(FileNotFoundException fileNotFoundException) {
		}
		return null;
	}
	
	protected boolean isBoolean(Attribute attribute) {
		return attribute.getType().equals("java.lang.Boolean");
	}
	
	protected boolean isMethodBinding(Attribute attribute) {
		return attribute.getType().equals("javax.faces.el.MethodBinding");
	}
	
	protected String resolveGetterPrefix(Attribute attribute) {
		if(isBoolean(attribute))
			return "is";
		else
			return "get";
	}
	
	protected File getTemplate(Component component) {
		String templatePath = project.getBasedir() + File.separator + templatesDir;
		String[] packagePath = component.getPackage().split("\\.");
		String templateFileName = component.getComponentShortName() + "Template.java";
		
		for (int i = 0; i < packagePath.length; i++) {
			templatePath = templatePath + File.separator + packagePath[i];
		}
		
		return new File(templatePath + File.separator + templateFileName);
	}

    protected void writeWidgetVarResolver(BufferedWriter writer) throws IOException {
        writer.write("\tpublic String resolveWidgetVar() {\n");
        writer.write("\t\tFacesContext context = FacesContext.getCurrentInstance();\n");
        writer.write("\t\tString userWidgetVar = (String) getAttributes().get(\"widgetVar\");\n\n");
        writer.write("\t\tif(userWidgetVar != null)\n");
        writer.write("\t\t\treturn userWidgetVar;\n");
        writer.write("\t\t");
        writer.write(" else\n");
        writer.write("\t\t\treturn \"widget_\" + getClientId(context).replaceAll(\"-|\" + UINamingContainer.getSeparatorChar(context), \"_\");\n");
        writer.write("\t}\n");
    }
}
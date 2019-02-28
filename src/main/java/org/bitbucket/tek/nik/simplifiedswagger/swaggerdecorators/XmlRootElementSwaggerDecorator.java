package org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.models.AbstractModel;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Xml;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public class XmlRootElementSwaggerDecorator implements ISwaggerDecorator {

	@Override
	public void decorateProperty(Property property, Annotation annotation,Class propertyType) {

		
	}

	

	@Override
	public void decorateParameter(Parameter parameter, Annotation annotation,java.lang.reflect.Parameter methodParameter) {
		
		
	}
	
	@Override
	public void decorateModel(Model model, Annotation annotation, Class modelClass) {
		XmlRootElement xmlRootElement=(XmlRootElement) annotation;
		if(model instanceof AbstractModel)
		{
			AbstractModel abstractModel=(io.swagger.models.AbstractModel) model;
			Xml xml= new Xml();
			if(xmlRootElement.name()!=null)
			{
				xml.name(xmlRootElement.name());
			}
			abstractModel.setXml(xml);
		}
		
	}



	@Override
	public void decorateOperation(Operation operation, Annotation annotation, Method method) {
		
		
	}
	
	

}

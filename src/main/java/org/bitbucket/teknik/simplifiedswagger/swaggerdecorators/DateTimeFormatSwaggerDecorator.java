package org.bitbucket.teknik.simplifiedswagger.swaggerdecorators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public class DateTimeFormatSwaggerDecorator implements ISwaggerDecorator {

	@Override
	public void decorateProperty(Property property, Annotation annotation,Class propertyType) {

		//can this be used

	}

	@Override
	public void decorateParameter(Parameter parameter, Annotation annotation,java.lang.reflect.Parameter methodParameter) {
		
		DateTimeFormat dateTimeFormat= (DateTimeFormat) annotation;
		populateVendorExtension(parameter.getVendorExtensions(), dateTimeFormat);
		
		
	}
	
	@Override
	public void decorateModel(Model model, Annotation annotation, Class modelClass) {
		
		
	}

	private void populateVendorExtension(Map<String, Object> vendorExtensions, DateTimeFormat dateTimeFormat) {
		String pattern = dateTimeFormat.pattern();
		ISO iso = dateTimeFormat.iso();
		String style = dateTimeFormat.style();
		if(pattern.length()>0)
		{
			//will first need to generate a regexp
			//vendorExtensions.put("pattern", pattern);
			vendorExtensions.put("dtf", pattern);
		}
		if(iso!=ISO.NONE)
		{
			vendorExtensions.put("iso", iso.name());
		}
		if(!style.equals("SS"))
		{
			vendorExtensions.put("style", style);
		}
	}

	@Override
	public void decorateOperation(Operation operation, Annotation annotation, Method method) {
		
		
	}

}

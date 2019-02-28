package org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import javax.validation.constraints.Pattern;

import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public class PatternSwaggerDecorator implements ISwaggerDecorator {

	@Override
	public void decorateProperty(Property property, Annotation annotation,Class propertyType) {
		Pattern pattern = (Pattern) annotation;
		
		populateVendorExtensions(pattern, property.getVendorExtensions());

	}

	

	@Override
	public void decorateParameter(Parameter parameter, Annotation annotation,java.lang.reflect.Parameter methodParameter) {
Pattern pattern = (Pattern) annotation;
		
		populateVendorExtensions(pattern, parameter.getVendorExtensions());
		
	}
	
	@Override
	public void decorateModel(Model model, Annotation annotation, Class modelClass) {
		
		
	}
	
	private void populateVendorExtensions(Pattern pattern, Map<String, Object> vendorExtensions) {
		vendorExtensions.put("pattern", pattern.regexp());
	}



	@Override
	public void decorateOperation(Operation operation, Annotation annotation, Method method) {
		
	}

}

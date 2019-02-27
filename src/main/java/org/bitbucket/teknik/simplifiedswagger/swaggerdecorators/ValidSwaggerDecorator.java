package org.bitbucket.teknik.simplifiedswagger.swaggerdecorators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public class ValidSwaggerDecorator implements ISwaggerDecorator {

	@Override
	public void decorateProperty(Property property, Annotation annotation,Class propertyType) {

		// dont need to use this

	}

	@Override
	public void decorateParameter(Parameter parameter, Annotation annotation,java.lang.reflect.Parameter methodParameter) {
		Map<String, Object> vendorExtensions = parameter.getVendorExtensions();
		vendorExtensions.put("validationsOn", Boolean.TRUE);
		
	}
	
	@Override
	public void decorateModel(Model model, Annotation annotation, Class modelClass) {
		
		

	}

	@Override
	public void decorateOperation(Operation operation, Annotation annotation, Method method) {
		
		
	}

}

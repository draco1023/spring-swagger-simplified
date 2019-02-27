package org.bitbucket.teknik.simplifiedswagger.swaggerdecorators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import io.swagger.annotations.ApiOperation;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public class ApiOperationSwaggerDecorator implements ISwaggerDecorator {

	@Override
	public void decorateProperty(Property property, Annotation annotation,Class propertyType) {

		
	}

	

	@Override
	public void decorateParameter(Parameter parameter, Annotation annotation,java.lang.reflect.Parameter methodParameter) {
		
		
	}
	
	@Override
	public void decorateModel(Model model, Annotation annotation, Class modelClass) {
		
	}



	@Override
	public void decorateOperation(Operation operation, Annotation annotation, Method method) {
		/*
		 * have lots of other attributes.
		 * will use when needed.
		 */
		ApiOperation apiOperation=(ApiOperation) annotation;
		String value = apiOperation.value();
		String notes = apiOperation.notes();
		operation.setSummary(value);
		operation.setDescription(notes);
		
	}
	
	

}

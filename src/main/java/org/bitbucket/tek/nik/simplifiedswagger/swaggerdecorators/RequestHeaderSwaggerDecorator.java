package org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.web.bind.annotation.RequestHeader;

import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public class RequestHeaderSwaggerDecorator implements ISwaggerDecorator {

	@Override
	public void decorateProperty(Property property, Annotation annotation,Class propertyType) {
		
		
	}
	
	@Override
	public void decorateParameter(Parameter parameter, Annotation annotation,java.lang.reflect.Parameter methodParameter) {
		RequestHeader requestParam=(RequestHeader) annotation;
		boolean required = requestParam.required();
		if(required)
		{
			parameter.setRequired(true);
		}
		
	}
	
	@Override
	public void decorateModel(Model model, Annotation annotation, Class modelClass) {
		
		
	}

	@Override
	public void decorateOperation(Operation operation, Annotation annotation, Method method) {
		
	}

	

	

}

package org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.bitbucket.tek.nik.simplifiedswagger.newmodels.NewModelCreator;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public class RequestParamSwaggerDecorator implements ISwaggerDecorator {

	@Override
	public void decorateProperty(Property property, Annotation annotation,Class propertyType) {
		
		
	}
	
	@Override
	public void decorateParameter(Parameter parameter, Annotation annotation,java.lang.reflect.Parameter methodParameter) {
		RequestParam requestParam=(RequestParam) annotation;
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
	public void decorateOperation(Operation operation, Annotation annotation, Method method, NewModelCreator newModelCreator) {
		
		
	}

	

	

}

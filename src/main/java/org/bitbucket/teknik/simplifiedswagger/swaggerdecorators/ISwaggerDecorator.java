package org.bitbucket.teknik.simplifiedswagger.swaggerdecorators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public interface ISwaggerDecorator {

	void decorateModel(Model model, Annotation annotation, Class modelClass);
	void decorateProperty(Property property, Annotation annotation, Class propertyType);
	void decorateParameter(Parameter parameter , Annotation annotation, java.lang.reflect.Parameter methodParameter);
	void decorateOperation(Operation operation, Annotation annotation, Method method);

}
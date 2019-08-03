package org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.Size;

import org.bitbucket.tek.nik.simplifiedswagger.newmodels.NewModelCreator;

import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public class SizeSwaggerDecorator implements ISwaggerDecorator {

	@Override
	public void decorateProperty(Property property, Annotation annotation,Class propertyType) {
		Size size = (Size) annotation;
		populateVendorExtensions(size, property.getVendorExtensions(), treatAsArray(propertyType));
		
	}
	@Override
	public void decorateParameter(Parameter parameter, Annotation annotation,java.lang.reflect.Parameter methodParameter) {
		Size size = (Size) annotation;
		
		populateVendorExtensions(size, parameter.getVendorExtensions(), treatAsArray(methodParameter.getType()));
	}
	private boolean treatAsArray(Class<?> type) {
		return type.isArray()||(List.class.isAssignableFrom(type)||Set.class.isAssignableFrom(type));
	}
	
	@Override
	public void decorateModel(Model model, Annotation annotation, Class modelClass) {
		
		
	}

	private void populateVendorExtensions(Size size, Map<String, Object> vendorExtensions, boolean isForArray) {
		
		vendorExtensions.put(isForArray?"minItems":"minLength", size.min());
		if (size.max() != Integer.MAX_VALUE) {
			vendorExtensions.put(isForArray?"maxItems":"maxLength", size.max());
		}
	}
	@Override
	public void decorateOperation(Operation operation, Annotation annotation, Method method, NewModelCreator newModelCreator) {
		
		
	}

	

}

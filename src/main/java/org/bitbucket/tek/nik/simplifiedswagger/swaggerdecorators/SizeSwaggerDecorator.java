package org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import javax.validation.constraints.Size;

import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public class SizeSwaggerDecorator implements ISwaggerDecorator {

	@Override
	public void decorateProperty(Property property, Annotation annotation,Class propertyType) {
		Size size = (Size) annotation;
		populateVendorExtensions(size, property.getVendorExtensions());
		
	}
	@Override
	public void decorateParameter(Parameter parameter, Annotation annotation,java.lang.reflect.Parameter methodParameter) {
		Size size = (Size) annotation;
		populateVendorExtensions(size, parameter.getVendorExtensions());
	}
	
	@Override
	public void decorateModel(Model model, Annotation annotation, Class modelClass) {
		
		
	}

	private void populateVendorExtensions(Size size, Map<String, Object> vendorExtensions) {
		vendorExtensions.put("minLength", size.min());
		if (size.max() != Integer.MAX_VALUE) {
			vendorExtensions.put("maxLength", size.max());
		}
	}
	@Override
	public void decorateOperation(Operation operation, Annotation annotation, Method method) {
		
		
	}

	

}

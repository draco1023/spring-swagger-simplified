package org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public class NotNullSwaggerDecorator implements ISwaggerDecorator {

	@Override
	public void decorateProperty(Property property, Annotation annotation,Class propertyType) {

		if(!propertyType.isArray())
		{
			property.setRequired(true);
		}
		
		populateVendorExtensions(property.getVendorExtensions());
	}

	

	@Override
	public void decorateParameter(Parameter parameter, Annotation annotation,java.lang.reflect.Parameter methodParameter) {
		if(!methodParameter.getType().isArray())
		{
			parameter.setRequired(true);
		}
		
		populateVendorExtensions(parameter.getVendorExtensions());
		
	}
	
	@Override
	public void decorateModel(Model model, Annotation annotation, Class modelClass) {
		
		
	}
	
	private void populateVendorExtensions(Map<String, Object> vendorExtensions) {
		vendorExtensions.put("notNull", Boolean.TRUE);
	}



	@Override
	public void decorateOperation(Operation operation, Annotation annotation, Method method) {
		
		
	}

}

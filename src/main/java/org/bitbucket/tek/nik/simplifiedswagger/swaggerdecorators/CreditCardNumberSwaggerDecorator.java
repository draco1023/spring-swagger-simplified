package org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import org.hibernate.validator.constraints.CreditCardNumber;

import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public class CreditCardNumberSwaggerDecorator implements ISwaggerDecorator {

	@Override
	public void decorateProperty(Property property, Annotation annotation,Class propertyType) {
		CreditCardNumber creditCardNumber = (CreditCardNumber) annotation;
		populateVendorExtension(property.getVendorExtensions());

	}

	@Override
	public void decorateParameter(Parameter parameter, Annotation annotation,java.lang.reflect.Parameter methodParameter) {
		CreditCardNumber creditCardNumber = (CreditCardNumber) annotation;
		populateVendorExtension(parameter.getVendorExtensions());
		
	}
	

	private Object populateVendorExtension(Map<String, Object> vendorExtensions) {
		return vendorExtensions.put("isCreditCardNumber", Boolean.TRUE);
	}

	@Override
	public void decorateModel(Model model, Annotation annotation, Class modelClass) {
		
		
	}

	@Override
	public void decorateOperation(Operation operation, Annotation annotation, Method method) {
		
		
	}


}

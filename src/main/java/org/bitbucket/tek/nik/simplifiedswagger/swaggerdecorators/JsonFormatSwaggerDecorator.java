package org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import org.bitbucket.tek.nik.simplifiedswagger.newmodels.NewModelCreator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.OptBoolean;

import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public class JsonFormatSwaggerDecorator implements ISwaggerDecorator {

	@Override
	public void decorateProperty(Property property, Annotation annotation,Class propertyType) {

		
		JsonFormat jsonFormat= (JsonFormat) annotation;
		Map<String, Object> vendorExtensions = property.getVendorExtensions();
		populateVendorExtensions(jsonFormat, vendorExtensions);
		

	}
	
	@Override
	public void decorateParameter(Parameter parameter, Annotation annotation,java.lang.reflect.Parameter methodParameter) {
		
		
		
		
	}
	
	@Override
	public void decorateModel(Model model, Annotation annotation, Class modelClass) {
		
		
	}

	private void populateVendorExtensions(JsonFormat jsonFormat, Map<String, Object> vendorExtensions) {
		String pattern = jsonFormat.pattern();
		OptBoolean lenient = jsonFormat.lenient();
		String locale = jsonFormat.locale();
		Shape shape = jsonFormat.shape();
		if(pattern!=null && pattern.length()>0)
		{
			vendorExtensions.put("pattern", pattern);
		}
		if(lenient.asBoolean()!=null)
		{
			vendorExtensions.put("lenient", lenient);
		}
		if(locale!=null && locale.length()>0)
		{
			vendorExtensions.put("locale", locale);
		}
	}

	@Override
	public void decorateOperation(Operation operation, Annotation annotation, Method method, NewModelCreator newModelCreator) {
		
		
	}

	

	

}

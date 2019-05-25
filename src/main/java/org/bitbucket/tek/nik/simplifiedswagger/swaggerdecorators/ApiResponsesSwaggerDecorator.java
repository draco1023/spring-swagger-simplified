package org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestHeader;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.ResponseHeader;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public class ApiResponsesSwaggerDecorator implements ISwaggerDecorator {

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
		
		ApiResponses apiResponses=(ApiResponses) annotation;
		if(apiResponses!=null)
		{
			Map<String, Response> responses= new LinkedHashMap<>();
			final ApiResponse[] value = apiResponses.value();
			for (ApiResponse apiResponse : value) {
				
				Response response= new Response();
				response.setDescription(apiResponse.message());
				apiResponse.responseHeaders();
				apiResponse.responseContainer();
				String reference = apiResponse.reference();//ignore
				Class<?> respType = apiResponse.response();//ignore
				apiResponse.examples();//ignore
				//we are going to ignore apiResponse.responseHeaders() and apiResponse.responseContainer()because we will be relying only on @RequestHeader
					responses.put(String.valueOf(apiResponse.code()), response);
			}
			operation.setResponses(responses);
		}
		
		
		
	}
	
	
	






	
	
	

}

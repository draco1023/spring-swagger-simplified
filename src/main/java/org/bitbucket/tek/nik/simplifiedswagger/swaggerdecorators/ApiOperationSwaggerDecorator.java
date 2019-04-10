package org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
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


	interface SendStringToOperation 
    { 
        void apply(String message, Operation operation); 
    } 
	@Override
	/**
	 * 
	 * ApiOperation attributes that have been ignored are:
	 * nickname - (cant use nickname to set operatiinId, its already set and need it)
	 * cant use below to change what is sensibly set already
	 * httpMethod
	 * response
	 * responseContainer
	 * The following are ignored because original spring fox also is ignoring
	 * ignoreJsonView
	 * protocols
	 * responseHeaders
	 * responseReference
	 * 
	 */
	public void decorateOperation(Operation operation, Annotation annotation, Method method) {
		
		ApiOperation apiOperation=(ApiOperation) annotation;
		String value = apiOperation.value();
		String notes = apiOperation.notes();
		operation.setSummary(value);
		operation.setDescription(notes);
		Authorization[] authorizations = apiOperation.authorizations();
		for (Authorization authorization : authorizations) {
			AuthorizationScope[] authScopes = authorization.scopes();
			List<String> scopes= new ArrayList<>();
			for (AuthorizationScope authorizationScope : authScopes) {
				scopes.add(authorizationScope.scope());
				//ignoring description as in original
			}
			operation.addSecurity(authorization.value(), scopes);
		}
		//in addConsumesOrProduces deviating from spring fox original behaviour slightly
		addConsumesOrProduces(operation, apiOperation.consumes(),(message, operation1) ->operation1.addConsumes(message), (operation1)->operation1.getConsumes()==null||operation1.getConsumes().size()==0);
		addConsumesOrProduces(operation, apiOperation.produces(),(message, operation1) ->operation1.addProduces(message),  (operation1)->operation1.getProduces()==null||operation1.getProduces().size()==0);
		
		if(apiOperation.hidden())
		{
			operation.getVendorExtensions().put("hidden", apiOperation.hidden());
		}
		String[] tags=apiOperation.tags();
		if(tags!=null && tags.length>0)
		{
			for (String tag : tags) {
				if(tag!=null && tag.length()!=0)
				{
					operation.addTag(tag);
				}
				
			}
		}
		Extension[] extensions = apiOperation.extensions();
		for (Extension extension : extensions) {
			String extensionName = extension.name();
			ExtensionProperty[] properties = extension.properties();
			for (ExtensionProperty extensionProperty : properties) {
				String extensionPropertyName = extensionProperty.name();
				String extensionPropertyValue = extensionProperty.value();
				Map<String, String> map = new HashMap<>();
				if(extensionPropertyName!=null && extensionPropertyName.length()!=0)
				{
					map.put(extensionPropertyName, extensionPropertyValue);
				}
				
				operation.getVendorExtensions().put("x-"+extensionName, map);;
			}
		}
		
		
		
		
	}


	private void addConsumesOrProduces(Operation operation, String inputToAdd, SendStringToOperation fobj, Function<Operation, Boolean> previouslyEmpty) {
		
		if(inputToAdd!=null )
		{
			if(previouslyEmpty.apply(operation))
			{
				inputToAdd=inputToAdd.trim();
				if(inputToAdd.length()>0)
				{
					String[] inputToAddArr = inputToAdd.split(",");
					for (String inputToAddArrRow : inputToAddArr) {
						inputToAddArrRow=inputToAddArrRow.trim();
						if(inputToAddArrRow.length()>0)
						{
							fobj.apply(inputToAddArrRow, operation);
						}
					}
				}
			}
			
		
		}
	}



	
	
	

}

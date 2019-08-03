package org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bitbucket.tek.nik.simplifiedswagger.newmodels.NewModelCreator;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiModelProperty.AccessMode;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;

public class ApiModelPropertySwaggerDecorator implements ISwaggerDecorator {
	/**
	 * <p>
	 * Even though trying to ignore some attributes as shown below 
	 * its possible that they might not be really ignored.
	 * This is because unless its a generic bean we are not creating
	 * the actual bean model with the properties. 
	 * We are using the already built models and properties for 
	 * non generic beans. Will fix this properly when we do the swagger 3 version
	 * </p>
	 * ApiModelProperty  attributes that we are trying to ignore are as below:
	 * <p>
	 * <ol>
	 * <li>
	 * reference() - cant  change what is sensibly set already
	 * </li>
	 * <li>
	 * required - cant  change what is sensibly set already
	 * </li>

	 * 
	 * <li>
	 * access - ignored because original spring fox  is ignoring
	 * </li> 
	 * <li>
	 * allowEmptyValue - ignored because original spring fox  is ignoring
	 * </li> 
	 * <li>
	 * dataType - ignored because original spring fox  is ignoring
	 * </li> 
	 * <li>
	 * name - ignored because original spring fox  is ignoring
	 * </li> 
	 * </ol>
	 * </p>
	 */
	@Override
	public void decorateProperty(Property property, Annotation annotation,Class propertyType) {

		ApiModelProperty apiModelProperty=(ApiModelProperty) annotation;

		String example=apiModelProperty.example();
		if(example!=null && example.length()>0)
		{
			//its only an example let it override even if provided
			property.setExample(example);
		}
		String vaue=apiModelProperty.value();
		if(vaue!=null)
		{
			vaue=vaue.trim();
		}
		String notes = apiModelProperty.notes();
		if(notes!=null)
		{
			notes=notes.trim();
		}
		boolean useValue=vaue!=null && vaue.length()>0;
		boolean useNotes=notes!=null && notes.length()>0;
		if(useValue && useNotes)
		{
			property.setDescription(vaue+" notes- "+notes);
		}
		else if(useValue )
		{
			property.setDescription(vaue);
		}
		else if( useNotes)
		{
			property.setDescription("notes- "+notes);
		}
		
		
		Extension[] extensions = apiModelProperty.extensions();
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
				
				property.getVendorExtensions().put("x-"+extensionName, map);;
			}
		}
		
		
		property.setReadOnly(apiModelProperty.readOnly());
		//since above apiModelProperty.readOnly() is deprecated
		//allow below to override
		AccessMode accessMode = apiModelProperty.accessMode();
		if(accessMode!=null )
		{
			property.setReadOnly(accessMode==AccessMode.READ_ONLY);
		}
			
		
		property.setPosition(apiModelProperty.position());
		if(property instanceof StringProperty)
		{
			StringProperty stringProperty=(io.swagger.models.properties.StringProperty) property;
			List<String> existingEnum = stringProperty.getEnum();
			//dont change from already set sensibles
			if(existingEnum==null|| existingEnum.size()==0)
			{
				String allowableValues = apiModelProperty.allowableValues();
				if(allowableValues!=null)
				{
					allowableValues=allowableValues.trim();
					if(allowableValues.length()>0)
					{
						//range concept is not being used by original spring fox
						String[] enumValues = allowableValues.split(",");
						
						List<String> newEnum = new ArrayList<>();
						for (String string : enumValues) {
							if(string!=null )
							{
								string=string.trim();
								if(string.length()>0)
								{
									newEnum.add(string);
								}
							}
						}
						stringProperty.setEnum(newEnum);
					}
				}
			}

		}
		
		if(apiModelProperty.hidden())
		{
			if(!property.getRequired())
			{
				property.getVendorExtensions().put("hidden", true);
			}
			
		}
		
			
		
	}

	

	@Override
	public void decorateParameter(Parameter parameter, Annotation annotation,java.lang.reflect.Parameter methodParameter) {
		
		
	}
	
	@Override
	public void decorateModel(Model model, Annotation annotation, Class modelClass) {
		
	}



	@Override
	public void decorateOperation(Operation operation, Annotation annotation, Method method, NewModelCreator newModelCreator) {
		
	
		
		
	}


	

}

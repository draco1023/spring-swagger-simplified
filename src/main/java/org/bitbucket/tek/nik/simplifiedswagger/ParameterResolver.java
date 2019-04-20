package org.bitbucket.tek.nik.simplifiedswagger;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bitbucket.tek.nik.simplifiedswagger.exception.SimplifiedSwaggerException;
import org.bitbucket.tek.nik.simplifiedswagger.modelbuilder.ParameterizedComponentKeySymbols;

import io.swagger.annotations.ApiParam;
import io.swagger.models.Model;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

public class ParameterResolver {
	
	private SimplifiedSwaggerServiceModelToSwagger2MapperImpl maper;
	
	public ParameterResolver(SimplifiedSwaggerServiceModelToSwagger2MapperImpl maper) {
		super();
		this.maper = maper;
	}

	List<Parameter> buildNewResolvedParameters(ApiParam[] apiParams, String prefix, Map<String, Model> definitions, String simpleRef, boolean parentIsRequired) {
		
		List<Parameter> resolvedNewParmeters= new ArrayList<>();
		
		Class modelClazz=null;
		Type modelClazzType = maper.getClassDefinition(simpleRef);
		if(simpleRef.contains(ParameterizedComponentKeySymbols.LEFT))
		{
			if(modelClazzType instanceof ParameterizedType)
			{
			ParameterizedType ParameterizedType=(java.lang.reflect.ParameterizedType) modelClazzType;
			modelClazz = (Class) ParameterizedType.getRawType();
			}
		}
		else if(modelClazzType instanceof Class)
		{
			modelClazz=(Class) modelClazzType;
		}
		
		//we dont want to go into what we consider basic types
		//for all the basic types we should have a mapping
		if(BasicMappingHolder.INSTANCE.getMappedByType(modelClazz.getName())==null)
		{
			Model model = definitions.get(simpleRef);
			Map<String, Property> properties = model.getProperties();
			Set<String> keySet = properties.keySet();
			
			List<String> keyList=new ArrayList<>();
			for (String key : keySet) {
				keyList.add(key);
			}
			for (int i = 0; i < keyList.size(); i++) 
			{
				String key=keyList.get(i);
			
				Property property=properties.get(key);
				ApiParam apiParamFromPrperty = getApiParamFromProperty(modelClazz, key);
				ApiParam[] apiParams1= new ApiParam[apiParams.length+1];
				System.arraycopy(apiParams, 0, apiParams1, 0, apiParams.length);
				apiParams1[apiParams1.length-1]=apiParamFromPrperty;
				apiParams=apiParams1;
				
				{//just limiting the variable name space //curly can be removed without any efefct
					
					if(property instanceof ArrayProperty)
					{
						ArrayProperty arrayProperty=(ArrayProperty) property;
						Property items = arrayProperty.getItems();
					
						if(items instanceof RefProperty)
						{
							RefProperty itemsRefProperty=(RefProperty) items;
							List<Parameter> resolvedParmeters = buildNewResolvedParameters(apiParams, prefix+key+"[0].", definitions, itemsRefProperty.getSimpleRef(), parentIsRequired && property.getRequired() );
							//no need to remove before adding because nothing has been added yet
							
							resolvedNewParmeters.addAll(i, resolvedParmeters);
							i=i+resolvedParmeters.size()-1;
						}
						else
						{
							buildQueryParam(apiParams, prefix, parentIsRequired, resolvedNewParmeters, property, items);
							
							
						}
						
						
						
						
					}
					else if(property instanceof RefProperty)
					{
						RefProperty refProperty=(RefProperty) property;
						List<Parameter> resolvedParmeters = buildNewResolvedParameters(apiParams, prefix+key+".", definitions, refProperty.getSimpleRef(), parentIsRequired && refProperty.getRequired());
						//no need to remove before adding because nothing has been added yet
						
						resolvedNewParmeters.addAll(i, resolvedParmeters);
						i=i+resolvedParmeters.size()-1;	
					}
					else if(!property.getType().equals("ref"))
					{
						buildQueryParam(apiParams, prefix, parentIsRequired, resolvedNewParmeters, property, null);
					}
					else
					{
							throw new SimplifiedSwaggerException("unexpected see what aelse and if needed impmrove logic");
						
					}
					
					
				}
				
				
				
			}
			
		
		}
		
		
		
		return resolvedNewParmeters;
	}

	private void buildQueryParam(ApiParam[] apiParams, String prefix, boolean parentIsRequired,
			List<Parameter> resolvedNewParmeters, Property property, Property items) {
		QueryParameter queryParameter= new QueryParameter();
		queryParameter.setName(prefix+property.getName());
		queryParameter.setType(property.getType());
		queryParameter.setFormat(property.getFormat());
		if(items!=null)
		{
			queryParameter.items(items);
		}
		
		queryParameter.required(parentIsRequired && property.getRequired());
		
		Map<String, Object> propertyVendorExtensions = property.getVendorExtensions();
		Set<String> proertyVendorExtensionskeySet = propertyVendorExtensions.keySet();
		for (String proertyVendorExtensionskey : proertyVendorExtensionskeySet) {
			queryParameter.getVendorExtensions().put(proertyVendorExtensionskey, propertyVendorExtensions.get(proertyVendorExtensionskey));
		}
		
		for (ApiParam apiParam : apiParams) 
		{
			applyApiParamOnQueryParam(queryParameter, apiParam, property, items!=null);
		}
		describeParameter(queryParameter);
		Boolean hidden=(Boolean) queryParameter.getVendorExtensions().get("hidden");
		if(hidden==null || (!hidden.booleanValue()))
		{
			resolvedNewParmeters.add(queryParameter);
		}
		
		
	}

	private void applyApiParamOnQueryParam(QueryParameter queryParameter, ApiParam apiParam, Property property, boolean forArray) 
	
	{
		if(apiParam!=null)
		{
			queryParameter.setAccess(apiParam.access());
			
			String example=apiParam.example();
			if(example!=null && example.length()>0)
			{
				//its only an example let it override even if provided
				queryParameter.setExample(example);
			}
			
			String vaue=apiParam.value();
			if(vaue!=null)
			{
				vaue=vaue.trim();
				queryParameter.setDescription(vaue);
			}
			
			queryParameter.setReadOnly(apiParam.readOnly());
			queryParameter.setDefaultValue(apiParam.defaultValue());
			
			if(property instanceof StringProperty)
			{
				StringProperty stringProperty=(io.swagger.models.properties.StringProperty) property;
				List<String> existingEnum = stringProperty.getEnum();
				//dont change from already set sensibles
				if(existingEnum==null|| existingEnum.size()==0)
				{
					String allowableValues = apiParam.allowableValues();
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
							queryParameter.setEnum(newEnum);
						}
					}
				}

			}
			if(!queryParameter.getRequired())
			{
				//if we have it set as required we cant allow it to be hidden
				queryParameter.getVendorExtensions().put("hidden", true);
			}
			
			
			
			apiParam.examples();
			apiParam.format();
			
			apiParam.name();
			apiParam.required();
			apiParam.type();
			apiParam.allowEmptyValue();
			apiParam.allowMultiple();
			apiParam.collectionFormat();
		}

		
		
		
		
		
		
		
	}
	
	void describeParameter(Parameter parameter) {
		String existingParameterDescription = parameter.getDescription();
		existingParameterDescription=existingParameterDescription!=null?existingParameterDescription:parameter.getName();
		Map<String, Object> vendorExtensions = parameter.getVendorExtensions();
		StringBuilder sb= new StringBuilder();
		sb.append(existingParameterDescription);
		maper.addDescriptionUsingVendorExtensions( vendorExtensions, sb);
		//sb.append("</h6>");
		//sb.append("</table>");
		parameter.setDescription(sb.toString());
	}
	
	
	
	private ApiParam getApiParamFromProperty(Class modelClazz, String key) {
		Method getter=maper.getDeclaredGetter(modelClazz, key);
		Field field = maper.getFieldAfterCheckingWithGetter(modelClazz, key,  getter);
		Class fieldMethodType = maper.getFieldMethodType(field, getter);
		ApiParam apiParam=null;
		if(field!=null)
		{
			apiParam=field.getAnnotation(ApiParam.class);
			
		}
		if(getter!=null)
		{
			apiParam=getter.getAnnotation(ApiParam.class);
			
		}
		return apiParam;
		
	}

}

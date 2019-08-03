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
import org.bitbucket.tek.nik.simplifiedswagger.newmodels.NewModelCreator;
import org.springframework.http.MediaType;

import io.swagger.annotations.ApiParam;
import io.swagger.models.Model;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.FormParameter;
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

	List<Parameter> buildNewResolvedParameters(String prefix, Map<String, Model> definitions, String simpleRef, 
			boolean parentIsRequired, boolean preferQueryToFormParam, NewModelCreator newModelCreator) {
		
		List<Parameter> resolvedNewParmeters= new ArrayList<>();
		
		Class modelClazz=null;
		Type modelClazzType = maper.getClassDefinition(simpleRef, newModelCreator);
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
			if(model!=null)
			{
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
					
					
					{//just limiting the variable name space //curly can be removed without any efefct
						
						if(property instanceof ArrayProperty)
						{
							ArrayProperty arrayProperty=(ArrayProperty) property;
							Property items = arrayProperty.getItems();
						
							if(items instanceof RefProperty)
							{
								RefProperty itemsRefProperty=(RefProperty) items;
								List<Parameter> resolvedParmeters = buildNewResolvedParameters( prefix+key+"[0].", definitions, itemsRefProperty.getSimpleRef(), parentIsRequired && property.getRequired(), preferQueryToFormParam ,newModelCreator);
								//no need to remove before adding because nothing has been added yet
								
								resolvedNewParmeters.addAll(resolvedParmeters);
								//i=i+resolvedParmeters.size()-1;
							}
							else
							{
								buildQueryParam(apiParamFromPrperty, prefix, parentIsRequired, resolvedNewParmeters, property, items, preferQueryToFormParam);
								
								
							}
							
							
							
							
						}
						else if(property instanceof RefProperty)
						{
							RefProperty refProperty=(RefProperty) property;
							List<Parameter> resolvedParmeters = buildNewResolvedParameters( prefix+key+".", definitions, refProperty.getSimpleRef(), parentIsRequired && refProperty.getRequired(), preferQueryToFormParam, newModelCreator);
							//no need to remove before adding because nothing has been added yet
							
							resolvedNewParmeters.addAll(resolvedParmeters);
							//i=i+resolvedParmeters.size()-1;	
						}
						else if(!property.getType().equals("ref"))
						{
							buildQueryParam(apiParamFromPrperty, prefix, parentIsRequired, resolvedNewParmeters, property, null, preferQueryToFormParam);
						}
						else
						{
								throw new SimplifiedSwaggerException("unexpected see what aelse and if needed impmrove logic");
							
						}
						
						
					}
					
					
					
				}
			}
			else
			{
				throw new SimplifiedSwaggerException("why is model null for "+simpleRef );
			}

			
		
		}
		
		
		
		return resolvedNewParmeters;
	}

	
	
	
	public AbstractSerializableParameter buildQueryOrFormParameter(boolean preferQueryToFormParam) {
		AbstractSerializableParameter param;
		if(preferQueryToFormParam)
		{
			param= new QueryParameter();
		}
		else
		{
			FormParameter formParameter=new FormParameter();
			formParameter.setIn("formData");
			param=formParameter;
		}
		return param;
	}
	private void buildQueryParam(ApiParam apiParam, String prefix, boolean parentIsRequired,
			List<Parameter> resolvedNewParmeters, Property property, Property items, boolean preferQueryToFormParam) {
		
		AbstractSerializableParameter queryParameter= buildQueryOrFormParameter(preferQueryToFormParam);
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
		
		if(apiParam!=null)
		{
			applyApiParamOnQueryParam(queryParameter, apiParam, property, items!=null);
		}
		describeParameter(queryParameter);
		Boolean hidden=(Boolean) queryParameter.getVendorExtensions().get("hidden");
		boolean show=hidden==null || (!hidden.booleanValue());
		
		if(show)
		{
			resolvedNewParmeters.add(queryParameter);
		}
		
		
	}

	private void applyApiParamOnQueryParam(AbstractSerializableParameter queryParameter, ApiParam apiParam, Property property, boolean forArray) 
	
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
			//does not matter
			//cannot carry out implied removal from response if part of response
			queryParameter.setReadOnly(apiParam.readOnly());
			queryParameter.setDefaultValue(apiParam.defaultValue());
			
			setEnumValues(queryParameter, apiParam, property);
			if(!queryParameter.getRequired())
			{
				//if we have it set as required we cant allow it to be hidden
				if(apiParam.hidden())
				{
					queryParameter.getVendorExtensions().put("hidden", true);
				}
				
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

	private void setEnumValues(AbstractSerializableParameter queryParameter, ApiParam apiParam, Property property) {
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
	}
	
	void describeParameter(Parameter parameter) {
		String existingParameterDescription = parameter.getDescription();
		existingParameterDescription=existingParameterDescription!=null && existingParameterDescription.length()>0?existingParameterDescription:parameter.getName();
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
		if(getter!=null && apiParam==null)
		{
			apiParam=getter.getAnnotation(ApiParam.class);
			
		}
		return apiParam;
		
	}

}

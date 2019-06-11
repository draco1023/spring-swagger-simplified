package org.bitbucket.tek.nik.simplifiedswagger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.bitbucket.tek.nik.simplifiedswagger.exception.SimplifiedSwaggerException;
import org.bitbucket.tek.nik.simplifiedswagger.modelbuilder.ModelOrRefBuilder;
import org.bitbucket.tek.nik.simplifiedswagger.modelbuilder.OuterContainer;
import org.bitbucket.tek.nik.simplifiedswagger.modelbuilder.ParameterContainer;
import org.bitbucket.tek.nik.simplifiedswagger.modelbuilder.ParameterizedComponentKeyBuilder;
import org.bitbucket.tek.nik.simplifiedswagger.modelbuilder.ParameterizedComponentKeySymbols;
import org.bitbucket.tek.nik.simplifiedswagger.modelbuilder.ResponseContainer;
import org.bitbucket.tek.nik.simplifiedswagger.newmodels.NewModelCreator;
import org.bitbucket.tek.nik.simplifiedswagger.optracker.OperationTracker;
import org.bitbucket.tek.nik.simplifiedswagger.optracker.OperationTrackerData;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.ISwaggerDecorator;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiParam;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.RefResponse;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.CookieParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.service.Documentation;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2MapperImpl;

public class SimplifiedSwaggerServiceModelToSwagger2MapperImpl extends ServiceModelToSwagger2MapperImpl {

	
	
	private String[] constrollersToIgnore= {"org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController",
			springfox.documentation.swagger.web.ApiResourceController.class.getName()};
	private static final String[] NOBODYMETHODTYPES= sortArray(new String[]{"get", "delete"});
	private ParameterResolver parameterResolver= new ParameterResolver(this);
	@Autowired
	private ApplicationContext context;
	
	
	@Autowired( )
	Docket docket;
	
	private boolean applyDefaultResponseMessages;

	@Autowired
	private ListableBeanFactory listableBeanFactory;
	
	@Value("${showUnMappedAnnotations:false}")
	boolean showUnMappedAnnotations;
	
	private static final Class[] requestMappingTypes= {RequestMapping.class, GetMapping.class, PostMapping.class, PutMapping.class, PatchMapping.class, DeleteMapping.class};
	private Map<RequestMethod, List<ResponseMessage>> customGlobalResponseMessages;

	@PostConstruct
	private void init()
	{
		applyDefaultResponseMessages = applyDefaultResponseMessages();
		customGlobalResponseMessages = globalResponseMessages();
	}

	private Map<RequestMethod, List<ResponseMessage>> globalResponseMessages(){
		
		
		Map<RequestMethod, List<ResponseMessage>> ret=null;
		if(docket!=null)
		{
			try {
				Field field = docket.getClass().getDeclaredField("responseMessages");
				field.setAccessible(true);
				ret = (Map<RequestMethod, List<ResponseMessage>>) field.get(docket);
				field.setAccessible(false);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new SimplifiedSwaggerException("could not introspect docket", e);
			}
		}
		return ret;
		
		}

	@Override
	public Swagger mapDocumentation(Documentation from) {
		
		OperationTracker operationTracker= new OperationTracker();
		
		
			
			Swagger swagger = super.mapDocumentation(from);
			Info info = swagger.getInfo();
			String basePath = swagger.getBasePath();
			Map<String, Model> definitions = swagger.getDefinitions();
			
			SimplifiedSwaggerData simplifiedSwaggerData = new SimplifiedSwaggerData(definitions);
			ExternalDocs externalDocs = swagger.getExternalDocs();
			String host = swagger.getHost();
			List<String> consumes = swagger.getConsumes();
			List<String> produces = swagger.getProduces();
			Map<String, Parameter> parameters = swagger.getParameters();
			Map<String, Path> paths = swagger.getPaths();
			Map<String, Response> responses = swagger.getResponses();
			List<Scheme> schemes = swagger.getSchemes();
			List<SecurityRequirement> security = swagger.getSecurity();
			Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();
			List<SecurityRequirement> securityRequirement = swagger.getSecurityRequirement();
			String swagger2 = swagger.getSwagger();
			List<Tag> tags = swagger.getTags();
			Map<String, Object> vendorExtensions = swagger.getVendorExtensions();
			paths.clear();
			tags.clear();
			removeGenricModels(definitions);
			introspectConrollerAdvices(simplifiedSwaggerData.getNewModelCreator());
			Map<String, List<MethodAndTag>> pathToMethodListMap = buildPathToMethodAndTagMap(tags);
			Set<String> keySet = pathToMethodListMap.keySet();
			for (String key : keySet) {
				Path path= new Path();
				swagger.getPaths().put(key, path);
				
				List<MethodAndTag> list = pathToMethodListMap.get(key);
				for (MethodAndTag methdoAndTag : list) {
					 buildOperation(path, methdoAndTag, key, definitions, operationTracker, simplifiedSwaggerData);
					
					
				}
			}
			
			fixGenericReferencesInNonGenericBeans(definitions, simplifiedSwaggerData.getNewModelCreator());
			simplifiedSwaggerData.getNewModelCreator().build();
			transformDefinitions(definitions, simplifiedSwaggerData);
			adjustExamples(definitions, simplifiedSwaggerData.getNewModelCreator());
			
			expandResolvableParameters(paths, definitions, operationTracker, simplifiedSwaggerData.getNewModelCreator());
			removeHiddentParameters(paths, definitions, operationTracker);
			operationTracker.cleanup();
			transformDefinitionsUsingApi(definitions, simplifiedSwaggerData);
			
			
			
			if(showUnMappedAnnotations)
			{
				System.err.println("unMappedAnnotations=" + simplifiedSwaggerData.getUnMappedAnnotations());
			}
			//unused code below will remove later. commenting out for now
			//newModelCreator.tempShowBlocked();
			return swagger;

		
				
		
		
		
	}


	


	/**
	 * no need for this method to rcurse diurectly
	 */

	private void fixGenericReferencesInNonGenericBeans(Map<String, Model> definitions, NewModelCreator newModelCreator) {
		Set<String> keySet = definitions.keySet();
		for (String definitionsKey : keySet) 
		{
			if(!definitionsKey.contains(ParameterizedComponentKeySymbols.LEFT))
			{
				Type modelClazzType = getClassDefinition(definitionsKey, newModelCreator);
				Class modelClazz=(Class) modelClazzType;
				//we dont want to go into what we consider basic types
				//for all the basic types we should have a mapping
				if(BasicMappingHolder.INSTANCE.getMappedByType(modelClazz.getName())==null)
				{
					Model model = definitions.get(definitionsKey);
					Map<String, Property> properties = model.getProperties();
					if(properties!=null)
					{
						Set<String> propertiesKeySet = properties.keySet();
						for (String propertiesKey : propertiesKeySet) 
						{
							Property property = properties.get(propertiesKey);
							if(property instanceof RefProperty)
							{
								RefProperty refProperty=(RefProperty) property;
								
								Method getter=getDeclaredGetter(modelClazz, property.getName());
								Field field = getFieldAfterCheckingWithGetter(modelClazz, propertiesKey, getter);
								Class fieldMethodType = getFieldMethodType(field, getter);
								Type fieldMethodGenericType = getFieldMethodGenericType(field, getter);
								
								if(fieldMethodGenericType instanceof ParameterizedType)
								{
									ParameterizedType parameterizedType= (ParameterizedType) fieldMethodGenericType;
									Model parameterizedPropertiesModel = definitions.get(refProperty.getSimpleRef());
									if(parameterizedPropertiesModel==null)
									{
										//compute new 
										String newKey = ParameterizedComponentKeyBuilder.buildKeyForParameterizedComponentType(parameterizedType);
										refProperty.set$ref(newKey);
										newModelCreator.addIfParemeterizedType(parameterizedType, false);

									}
								}
								
								
								
							}
							else if(property instanceof ArrayProperty)
							{
								ArrayProperty arrayProperty=(ArrayProperty) property;
								Property items = arrayProperty.getItems();
								
								if(items instanceof RefProperty)
								{
									RefProperty refProperty=(RefProperty) items;
									Method getter=getDeclaredGetter(modelClazz, property.getName());
									Field field = getFieldAfterCheckingWithGetter(modelClazz, propertiesKey,  getter);
									Class fieldMethodType = getFieldMethodType(field, getter);
									Type fieldMethodGenericType1=getFieldMethodGenericType(field, getter);
									Type compaonentType=null;
									if(fieldMethodGenericType1 instanceof GenericArrayType)
									{
										GenericArrayType genericArrayType=(java.lang.reflect.GenericArrayType) fieldMethodGenericType1;
										compaonentType=genericArrayType.getGenericComponentType();
									}
									else if(fieldMethodType.isArray())
									{
										compaonentType=fieldMethodType.getComponentType();
									}
									else //must be list or set
									{
										compaonentType=getParameteerizedTypeIfFieldMethodTypeListOrSet(
												field, getter, fieldMethodType);
									}
									if(compaonentType!=null)
									{
										if(compaonentType instanceof ParameterizedType)
										{
											ParameterizedType parameterizedType= (ParameterizedType) compaonentType;
											Model parameterizedPropertiesModel = definitions.get(refProperty.getSimpleRef());
											//if(parameterizedPropertiesModel==null)//its having wrong assignments//best to replace
											{
												//compute new 
												String newKey = ParameterizedComponentKeyBuilder.buildKeyForParameterizedComponentType(parameterizedType);
												refProperty.set$ref(newKey);
												newModelCreator.addIfParemeterizedType(parameterizedType, false);

											}
										}
									}
									else
									{
										throw new SimplifiedSwaggerException(propertiesKey+" is an array  in "+modelClazz.getName() +" but could not find type of row");
									}
									
								}
								else
								{
									//if not RefProperty we need not do
								}
								
								
							
								
								
							}
						}
					}

						

			
							
				}
			}
				
				
			
		}
		
	}



	private void expandResolvableParameters(Map<String, Path> paths, 
			Map<String, Model> definitions, OperationTracker operationTracker, NewModelCreator newModelCreator) {
		Set<String> pathKeys = paths.keySet();
		for (String pathKey : pathKeys) {
			Path path = paths.get(pathKey);
			List<Operation> operations = path.getOperations();
			for (Operation operation : operations) {
				final OperationTrackerData operationTrackerData = operationTracker.get(operation);
				final ApiParam[] originalMethodApiParams = operationTrackerData.getApiParams();
				List<Parameter> opParameters = operation.getParameters();
				boolean preferQueryToFormParameter=operationTrackerData.preferQueryToFormParameter();
				
				for (int i = 0; i < opParameters.size(); i++) 
				{
					Parameter parameter=opParameters.get(i);
					ApiParam originalMethodApiParam=originalMethodApiParams[i];
					
					if(parameter instanceof BodyParameter)
					{
						BodyParameter tempBodyParameter=(BodyParameter) parameter;
						Boolean needsResolving=(Boolean) tempBodyParameter.getVendorExtensions().get("toresolve");
						if(needsResolving!=null && needsResolving.booleanValue())
						{
							List<Parameter> resolvedParmeters = expandTempBodyParameters(originalMethodApiParam, 
									tempBodyParameter, definitions, preferQueryToFormParameter, newModelCreator);
							opParameters.remove(i);
							
							opParameters.addAll(i, resolvedParmeters);
							i=i+resolvedParmeters.size();
							
							
						}
					}
					else
					{
						if(originalMethodApiParam!=null)
						{
							parameter.setDescription(originalMethodApiParam.value());
							parameter.setAccess(originalMethodApiParam.access());
							//calling readonly on a simple parameter is of no benefit 
							//cannot carry out implied removal from response if part of response
							parameter.setReadOnly(originalMethodApiParam.readOnly());
							
							if(parameter instanceof AbstractSerializableParameter)
							{
								AbstractSerializableParameter asp=(AbstractSerializableParameter) parameter;
								asp.setDefaultValue(originalMethodApiParam.defaultValue());
								asp.setExample(originalMethodApiParam.example());
								setEnumValues(asp, originalMethodApiParam);
							}
							if(!parameter.getRequired() && originalMethodApiParam.hidden())
							{
								parameter.getVendorExtensions().put("hidden", true);
							}
														
							
							//
							
							
							originalMethodApiParam.allowEmptyValue();
							originalMethodApiParam.collectionFormat();
							
							
							originalMethodApiParam.examples();
							originalMethodApiParam.format();
							
							originalMethodApiParam.required();
							originalMethodApiParam.type();
							
						}
						parameterResolver.describeParameter(parameter);
					}
					/*
					 * For now because we are using only vendor extensions this will work.
					 * Will improvise later when we stop using vendor extensions
					 */
					
					
				}
				
			}
		}
	}






	
	private void removeHiddentParameters(Map<String, Path> paths, 
			Map<String, Model> definitions, OperationTracker operationTracker) {
		Set<String> pathKeys = paths.keySet();
		for (String pathKey : pathKeys) {
			Path path = paths.get(pathKey);
			List<Operation> operations = path.getOperations();
			for (Operation operation : operations) {
				final OperationTrackerData operationTrackerData = operationTracker.get(operation);
				final ApiParam[] originalMethodApiParams = operationTrackerData.getApiParams();
				List<Parameter> opParameters = operation.getParameters();
				for (int i = 0; i < opParameters.size(); i++) 
				{
					Parameter parameter=opParameters.get(i);
					Boolean hidden=(Boolean) parameter.getVendorExtensions().get("hidden");
					if(hidden!=null && hidden.booleanValue())
					{
						opParameters.remove(i);
						i--;
					}
				}
			}
		}
		
	}



	private void setEnumValues(AbstractSerializableParameter asp, ApiParam apiParam) {
		
		
		final List existingEnum = asp.getEnum();
		
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
					asp.setEnum(newEnum);
				}
			}
		}
		
	}





	


//here must add drill logic which uses .
	private List<Parameter> expandTempBodyParameters(ApiParam originalMethodApiParam, 
			BodyParameter tempBodyParameter, Map<String, Model> definitions, boolean preferQueryToFormParam, NewModelCreator newModelCreator) {
		
		RefModel schema = (RefModel) tempBodyParameter.getSchema();
		String simpleRef = schema.getSimpleRef();
		//makes sense to treat outermost object as required
		//hence true
		//if its not true each field within it is not there when the outermost object is not there
		return parameterResolver.buildNewResolvedParameters( "", definitions, simpleRef, true, preferQueryToFormParam, newModelCreator);
	}


private void removeGenricModels(Map<String, Model> definitions) {
		Set<String> keySet = definitions.keySet();
		Set<String> keySetToRemove=new HashSet<>();
		for (String key : keySet) {
			if(key.contains(ParameterizedComponentKeySymbols.LEFT))
			{
				keySetToRemove.add(key);
				
			}
		}
		for (String key : keySetToRemove) {
			definitions.remove(key);
		}
		
		
	}
	
	private void adjustExamples(Map<String, Model> definitions,NewModelCreator newModelCreator) {
		Set<String> definitionsKeySet = definitions.keySet();
		for (String definitionsKey : definitionsKeySet) {
			if(definitionsKey.contains(ParameterizedComponentKeySymbols.LEFT))
			{
				continue;
			}
			Class modelClazz=null;
			Type modelClazzType = getClassDefinition(definitionsKey, newModelCreator);
			if(definitionsKey.contains(ParameterizedComponentKeySymbols.LEFT))
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
			Model model = definitions.get(definitionsKey);
			
			Map<String, Property> properties = model.getProperties();
			if(properties!=null)
			{
				Set<String> propertiesKeySet = properties.keySet();
				for (String propertiesKey : propertiesKeySet) {
					Property property = properties.get(propertiesKey);
					
					String name = property.getName();
					//if(!modelClazz.isEnum())
					{
						//you cant put contraints on a setter only getetr or fioeld
						Method getter=getDeclaredGetter(modelClazz, property.getName());
						Field field = getFieldAfterCheckingWithGetter(modelClazz, propertiesKey,  getter);
						Class fieldMethodType = getFieldMethodType(field, getter);
						String parameteerizedTypeIfFieldMethodTypeListOrSet = getParameteerizedTypeNameIfFieldMethodTypeListOrSet(
								field, getter, fieldMethodType);
						String mappedType = BasicMappingHolder.INSTANCE.getMappedByType(fieldMethodType.getName());
						createExampleForBasicTypes(property, fieldMethodType.getName(), mappedType);
						
						createExamplesForBasicInArrayIfNeeded(property, 
								mappedType, 
								fieldMethodType, 
								parameteerizedTypeIfFieldMethodTypeListOrSet);
					}
					
				}
			}
			
			
			
			
		}
	}

	
	private void createExamplesForBasicInArrayIfNeeded(Property property, 
			String mappedType, 
			Class fieldMethodType, 
			String parameteerizedTypeIfFieldMethodTypeListOrSet) {
	
		if(property instanceof ArrayProperty )
		{
			String typeNameOfItems=null;
			ArrayProperty arrayProperty=(ArrayProperty) property;
			Property items = arrayProperty.getItems();
			String mappedComponentType=null;
			if(fieldMethodType.isArray())
			{
				Class componentType = fieldMethodType.getComponentType();
				if(componentType!=null)
				{
					mappedComponentType = BasicMappingHolder.INSTANCE.getMappedByType(componentType.getName());
					typeNameOfItems=componentType.getName();
				}
				
			}
			else if(List.class.isAssignableFrom(fieldMethodType)||Set.class.isAssignableFrom(fieldMethodType))
			{
				if(parameteerizedTypeIfFieldMethodTypeListOrSet!=null)
				{
					typeNameOfItems=parameteerizedTypeIfFieldMethodTypeListOrSet;
					mappedComponentType = BasicMappingHolder.INSTANCE.getMappedByType(parameteerizedTypeIfFieldMethodTypeListOrSet);	
				}
				
			}
			
			if(!(items instanceof RefProperty) && mappedComponentType!=null)
			{
				if(typeNameOfItems!=null)
				{
					
					createExampleForBasicTypes( items, typeNameOfItems, mappedComponentType);
				}
				
				
			}
			
		}
	}
	private void createExampleForBasicTypes(Property property, String fieldMethodTypeName, String mappedType) {
		if(mappedType!=null)
		{
			if(fieldMethodTypeName.equals(char.class.getName())||fieldMethodTypeName.equals(Character.class.getName()))
			{
				property.setExample("c");
				property.getVendorExtensions().put("maxLength", 1);
			}
			if(mappedType.equals("date-time")||mappedType.equals("time")||mappedType.equals("date"))
			{
				if(property.getType().equals("string"))//it should be
				{
					
					String pattern = (String) property.getVendorExtensions().get("pattern");
					if(property.getFormat().equals("date-time"))
					{
						if(fieldMethodTypeName.equals(java.util.Date.class.getName()))
						{//yyyy-MM-dd'T'HH:mm:ss.SSSZ
							setExampleFor(property, pattern, 
									()->new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new java.util.Date()),
									()->new SimpleDateFormat(pattern).format(new java.util.Date()));
						}
						else if(fieldMethodTypeName.equals(java.sql.Timestamp.class.getName()))
						{
							//yyyy-MM-dd'T'HH:mm:ss.SSSZ
							setExampleFor(property, pattern, 
									()->new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new java.util.Date()),
									()->new SimpleDateFormat(pattern).format(new java.util.Date()));
							
						}
						else if(fieldMethodTypeName.equals(java.time.LocalDateTime.class.getName()))
						{
							
							setExampleFor(property, pattern, 
									()->java.time.LocalDateTime.now().toString(),
									()->DateTimeFormatter.ofPattern(pattern).format(LocalDateTime.now()));
							
						}
						else if(fieldMethodTypeName.equals(java.time.ZonedDateTime.class.getName()))
						{
							setExampleFor(property, pattern, 
									()->java.time.ZonedDateTime.now().toString(),
									()->DateTimeFormatter.ofPattern(pattern).format(java.time.ZonedDateTime.now()));
							
						}
						else if(fieldMethodTypeName.equals(java.time.OffsetDateTime.class.getName()))
						{
							setExampleFor(property, pattern, 
									()->java.time.OffsetDateTime.now().toString(),
									()->DateTimeFormatter.ofPattern(pattern).format(java.time.OffsetDateTime.now()));
							
						}
					}
					else if(property.getFormat().equals("time"))
					{
						if(fieldMethodTypeName.equals(java.sql.Time.class.getName()))
						{
							setExampleFor(property, pattern, 
									()->new java.sql.Time(System.currentTimeMillis()).toString(),
									()->new SimpleDateFormat(pattern).format(new java.util.Date()));
						}
						else if(fieldMethodTypeName.equals(java.time.LocalTime.class.getName()))
						{
							setExampleFor(property, pattern, 
									()->java.time.LocalTime.now().toString(),
									()->DateTimeFormatter.ofPattern(pattern).format(java.time.LocalTime.now()));
						}
						else if(fieldMethodTypeName.equals(java.time.OffsetTime.class.getName()))
						{
							setExampleFor(property, pattern, 
									()->java.time.OffsetTime.now().toString(),
									()->DateTimeFormatter.ofPattern(pattern).format(java.time.OffsetTime.now()));
						}
						
					}
					else if(property.getFormat().equals("date"))
					{
						if(fieldMethodTypeName.equals(java.sql.Date.class.getName()))
						{
							setExampleFor(property, pattern, 
									()->new java.sql.Date(System.currentTimeMillis()).toString(),
									()->new SimpleDateFormat(pattern).format(new java.util.Date()));
						}
						else if(fieldMethodTypeName.equals(java.time.LocalDate.class.getName()))
						{
							setExampleFor(property, pattern, 
									()->java.time.LocalDate.now().toString(),
									()->DateTimeFormatter.ofPattern(pattern).format(java.time.LocalDate.now()));
						}
					}
				}
			}
		}
	}
	
	

	private void setExampleFor(Property property, String pattern, Supplier<String> withoutPattern,
			Supplier<String> withPattern) {
		if(pattern!=null)
		{
			try
			{
			property.setExample(withPattern.get());
			}
			catch(Exception e)
			{
				property.setExample(withoutPattern.get());
			}
		}
		else
		{
			property.setExample(withoutPattern.get());
		}
	}


	private void transformDefinitions(Map<String, Model> definitions, SimplifiedSwaggerData simplifiedSwaggerData) {
		Set<String> definitionsKeySet = definitions.keySet();
		for (String definitionsKey : definitionsKeySet) {
			
			Class modelClazz=null;
			Type modelClazzType = getClassDefinition(definitionsKey, simplifiedSwaggerData.getNewModelCreator());
			if(definitionsKey.contains(ParameterizedComponentKeySymbols.LEFT))
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
				Model model = definitions.get(definitionsKey);
				Map<String, Property> properties = model.getProperties();
				
				
				Annotation[] declaredClassAnnotations = modelClazz.getDeclaredAnnotations();
				for (Annotation declaredClassAnnotation : declaredClassAnnotations) {
					handleAnnotatedModel(model, declaredClassAnnotation, modelClazz, simplifiedSwaggerData);
				}
				Map<String, Object> modelVendorExtensions = model.getVendorExtensions();
				if(modelVendorExtensions.size()>0)
				{
					String existingModelDescription = model.getDescription();
					existingModelDescription=existingModelDescription!=null&&existingModelDescription.length()>0?existingModelDescription:"a "+modelClazz.getName();
					
					
					StringBuilder sb= new StringBuilder();
					sb.append(existingModelDescription);
					sb.append("<hr/>");
					addDescriptionUsingVendorExtensions( modelVendorExtensions, sb);
					model.setDescription(sb.toString());
				}
			
				if(properties!=null)
				{
					Set<String> propertiesKeySet = properties.keySet();
					for (String propertiesKey : propertiesKeySet) {
						Property property = properties.get(propertiesKey);
						String name = property.getName();
						
					
						
						
						//if(!modelClazz.isEnum())
						{
							//you cant put contraints on a setter only getetr or fioeld
							Method getter=getDeclaredGetter(modelClazz, property.getName());
							Field field = getFieldAfterCheckingWithGetter(modelClazz, propertiesKey,  getter);
							Class fieldMethodType = getFieldMethodType(field, getter);
							String parameteerizedTypeIfFieldMethodTypeListOrSet = getParameteerizedTypeNameIfFieldMethodTypeListOrSet(
									field, getter, fieldMethodType);
							String mappedType = BasicMappingHolder.INSTANCE.getMappedByType(fieldMethodType.getName());
							refToBasicIfNeeded(property, mappedType, properties, fieldMethodType,  simplifiedSwaggerData);
							refToBasicInArrayIfNeeded(property, mappedType, properties, fieldMethodType, 
									parameteerizedTypeIfFieldMethodTypeListOrSet, simplifiedSwaggerData);
							if(field!=null)
							{
								Annotation[] annotations = field.getAnnotations();
								//annotationSort(annotations);
								
								for (Annotation annotation : annotations) {
									
									handleAnnotatedProperty(property, annotation, fieldMethodType, simplifiedSwaggerData);
								}
							}
							if(getter!=null)
							{
								Annotation[] annotations = getter.getAnnotations();
								for (Annotation annotation : annotations) {
									
									handleAnnotatedProperty(property, annotation, fieldMethodType, simplifiedSwaggerData);
								}
							}
						}
						
						if(property instanceof ArrayProperty)
						{
							ArrayProperty arrayProperty=(ArrayProperty) property;
							String pattern = (String) arrayProperty.getVendorExtensions().get("pattern");
							if(pattern!=null)
							{
								arrayProperty.getItems().getVendorExtensions().put("pattern", pattern);
							}
							Boolean notNullArray = (Boolean) arrayProperty.getVendorExtensions().get("notNull");
							Integer minLengthOfArray=(Integer) arrayProperty.getVendorExtensions().get("minItems");
							if(notNullArray!=null && notNullArray.booleanValue() && minLengthOfArray!=null && minLengthOfArray.intValue()> 0)
							{
								arrayProperty.setRequired(true);
							}
							
						}
						
						
						
					}
				}
				else
				{
					//throw new SimplifiedSwaggerException("why null");
				}
			
			}
		
		}
		
		
		for (String definitionsThatCanBeRemovedKey : simplifiedSwaggerData.getDefinitionsThatCanBeRemoved()) {
			definitions.remove(definitionsThatCanBeRemovedKey);
		}
		
	}
	
	void addDescriptionUsingVendorExtensions(
			Map<String, Object> vendorExtensions, StringBuilder sb) {
		
		
		Set<String> vendorExtensionKeySet = vendorExtensions.keySet();
		sb.append("                              ");
		sb.append("                              ");
		sb.append("                              ");
		//<h5>more description</h5>");
		//sb.append("<table><tr><td><h6>more description</h6></td></tr>");
		//sb.append("<h6>");
		sb.append("<p><span style='color: green; font-size: 10pt'>");
		for (String  vendorExtensionKey : vendorExtensionKeySet) {
			Object object = vendorExtensions.get(vendorExtensionKey);
			//sb.append("<tr><td><h6>");
			
			sb.append(vendorExtensionKey);
			sb.append(":");
			sb.append(object.toString());
			sb.append(", ");
			//sb.append("</h6></td></tr>");
			
		}
		sb.append("</span></p>");
	}
	
	private void transformDefinitionsUsingApi(Map<String, Model> definitions, SimplifiedSwaggerData simplifiedSwaggerData) {
		Set<String> definitionsKeySet = definitions.keySet();
		for (String definitionsKey : definitionsKeySet) {
			
			Class modelClazz=null;
			Type modelClazzType = getClassDefinition(definitionsKey, simplifiedSwaggerData.getNewModelCreator());
			if(definitionsKey.contains(ParameterizedComponentKeySymbols.LEFT))
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
				Model model = definitions.get(definitionsKey);
				Map<String, Property> properties = model.getProperties();
				
				
				Annotation[] declaredClassAnnotations = modelClazz.getDeclaredAnnotations();
				for (Annotation declaredClassAnnotation : declaredClassAnnotations) {
					//might need a api handling
					//handleAnnotatedModel(model, declaredClassAnnotation, modelClazz);
				}
			
			
				if(properties!=null)
				{
					Set<String> propertiesKeySet = properties.keySet();
					String[] propertiesArr= new String[propertiesKeySet.size()];
					propertiesKeySet.toArray(propertiesArr);
					for (String propertiesKey : propertiesArr) {
						Property property = properties.get(propertiesKey);
						String name = property.getName();
						
					
						
						
						//if(!modelClazz.isEnum())
						{
							//you cant put contraints on a setter only getetr or fioeld
							Method getter=getDeclaredGetter(modelClazz, property.getName());
							Field field = getFieldAfterCheckingWithGetter(modelClazz, propertiesKey,  getter);
							Class fieldMethodType = getFieldMethodType(field, getter);
							//String parameteerizedTypeIfFieldMethodTypeListOrSet = getParameteerizedTypeIfFieldMethodTypeListOrSet(
							//		field, getter, fieldMethodType);
							//String mappedType = BasicMappingHolder.INSTANCE.getMappedByType(fieldMethodType.getName());
							//refToBasicIfNeeded(property, mappedType, properties, fieldMethodType);
							//refToBasicInArrayIfNeeded(property, mappedType, properties, fieldMethodType, 
									//parameteerizedTypeIfFieldMethodTypeListOrSet);
							if(field!=null)
							{
								Annotation[] annotations = field.getAnnotations();
								//annotationSort(annotations);
								
								for (Annotation annotation : annotations) {
									
									handleAnnotatedApiProperty(property, annotation, fieldMethodType, simplifiedSwaggerData);
								}
							}
							if(getter!=null)
							{
								Annotation[] annotations = getter.getAnnotations();
								for (Annotation annotation : annotations) {
									
									handleAnnotatedApiProperty(property, annotation, fieldMethodType, simplifiedSwaggerData);
								}
							}
						}
						
						/*not needed here
						 * if(property instanceof ArrayProperty)
						{
							ArrayProperty arrayProperty=(ArrayProperty) property;
							String pattern = (String) arrayProperty.getVendorExtensions().get("pattern");
							if(pattern!=null)
							{
								arrayProperty.getItems().getVendorExtensions().put("pattern", pattern);
							}
						}*/
						
						Boolean hidden=(Boolean) property.getVendorExtensions().get("hidden");
						if(hidden!=null && hidden)
						{
							properties.remove(propertiesKey);
						}
						
						
					}

				}
				else
				{
					//throw new SimplifiedSwaggerException("why null");
				}
				
			}
		
		}
		
		
		/*not needed here
		 * for (String definitionsThatCanBeRemovedKey : definitionsThatCanBeRemoved) {
			definitions.remove(definitionsThatCanBeRemovedKey);
		}*/
		
	}



	private void annotationSort(Annotation[] annotations) {
		Arrays.sort(annotations, new Comparator<Annotation>() {

			@Override
			public int compare(Annotation o1, Annotation o2) {
				boolean o1IsSwaggerAnnotation=o1.annotationType().getPackage().getName().equals(SwaggerDecoratorConstants.SWAGGER_ANNOTATION_PACKAGE);
				boolean o2IsSwaggerAnnotation=o2.annotationType().getPackage().getName().equals(SwaggerDecoratorConstants.SWAGGER_ANNOTATION_PACKAGE);
				int ret=0;
				if(o1IsSwaggerAnnotation && (!o2IsSwaggerAnnotation))
				{
					ret=1;
				}
				else if(o2IsSwaggerAnnotation && (!o1IsSwaggerAnnotation))
				{
					ret=-1;
				}
				return ret;
			}
			
		});
	}

	



	public Field getFieldAfterCheckingWithGetter(Class modelClazz, String propertiesKey, 
			Method getter) {
		Field field = getDeclaredField(modelClazz, propertiesKey);
		if(field==null && getter==null)
		{
			throw new SimplifiedSwaggerException("could not find getter or field for "+propertiesKey+" in  "+modelClazz.getName());
		}
		if(field!=null && getter!=null)
		{
			if(field.getType()!=getter.getReturnType())
			{
				//if this happens prefer getter and ignore field
				field=null;
			}
		}
		return field;
	}

	
	private Type getParameteerizedTypeIfFieldMethodTypeListOrSet(Field field, Method getter, Class fieldMethodType) {
		Type parameteerizedTypeIfFieldMethodTypeListOrSet =null;
		if(field!=null)
		{

			parameteerizedTypeIfFieldMethodTypeListOrSet = 
					getParameterizedTypIfListOrSet(field.getGenericType(),
					fieldMethodType);
			
			
		}
		if(getter!=null)
		{

			parameteerizedTypeIfFieldMethodTypeListOrSet = 
					getParameterizedTypIfListOrSet(getter.getGenericReturnType(),
					fieldMethodType);
		}
		return parameteerizedTypeIfFieldMethodTypeListOrSet;
	}
	private String getParameteerizedTypeNameIfFieldMethodTypeListOrSet(Field field, Method getter, Class fieldMethodType) {
		String parameteerizedTypeIfFieldMethodTypeListOrSet =null;
		if(field!=null)
		{

			parameteerizedTypeIfFieldMethodTypeListOrSet = 
					getParameterizedTypeNameIfListOrSet(field.getGenericType(),
					fieldMethodType);
			
			
		}
		if(getter!=null)
		{

			parameteerizedTypeIfFieldMethodTypeListOrSet = 
					getParameterizedTypeNameIfListOrSet(getter.getGenericReturnType(),
					fieldMethodType);
		}
		return parameteerizedTypeIfFieldMethodTypeListOrSet;
	}

	public Class getFieldMethodType(Field field, Method getter) {
		Class fieldMethodType=null;
		
		if(field!=null)
		{
			fieldMethodType=field.getType();
			
			
			
		}
		if(getter!=null)
		{
			fieldMethodType=getter.getReturnType();
			
		}
		return fieldMethodType;
	}
	
	private Type getFieldMethodGenericType(Field field, Method getter) {
		Type fieldMethodType=null;
		
		if(field!=null)
		{
			fieldMethodType=field.getGenericType();
		}
		if(getter!=null)
		{
			fieldMethodType=getter.getGenericReturnType();
			
		}
		return fieldMethodType;
	}
	
	


	private String getParameterizedTypeNameIfListOrSet(Type genericType, Class fieldMethodType) {
		String parameteerizedTypeIfFieldMethodTypeListOrSet=null;
		Type type = getParameterizedTypIfListOrSet(genericType, fieldMethodType);
		if(type!=null)
		{
			parameteerizedTypeIfFieldMethodTypeListOrSet=type.getTypeName();
		}
		return parameteerizedTypeIfFieldMethodTypeListOrSet;
	}
	
	private Type getParameterizedTypIfListOrSet(Type genericType, Class fieldMethodType) {
		Type parameteerizedTypeIfFieldMethodTypeListOrSet=null;
		if(List.class.isAssignableFrom(fieldMethodType)||Set.class.isAssignableFrom(fieldMethodType))
		{
			
			if(genericType instanceof ParameterizedType)
			{
				ParameterizedType pt=(ParameterizedType) genericType;
				Type[] actualTypeArguments = pt.getActualTypeArguments();
				if(actualTypeArguments.length==1)
				{
					parameteerizedTypeIfFieldMethodTypeListOrSet=actualTypeArguments[0];
				}
				
			}
		}
		return parameteerizedTypeIfFieldMethodTypeListOrSet;
	}


	


	
	private void refToBasicInArrayIfNeeded(Property property, 
			String mappedType, Map<String, Property> properties, 
			Class fieldMethodType, 
			String parameteerizedTypeIfFieldMethodTypeListOrSet,
			SimplifiedSwaggerData simplifiedSwaggerData) {
		if(property instanceof ArrayProperty )
		{
			ArrayProperty arrayProperty=(ArrayProperty) property;
			Property items = arrayProperty.getItems();
			String mappedComponentType=null;
			if(fieldMethodType.isArray())
			{
				Class componentType = fieldMethodType.getComponentType();
				if(componentType!=null)
				{
					mappedComponentType = BasicMappingHolder.INSTANCE.getMappedByType(componentType.getName());	
				}
				
			}
			else if(List.class.isAssignableFrom(fieldMethodType)||Set.class.isAssignableFrom(fieldMethodType))
			{
				if(parameteerizedTypeIfFieldMethodTypeListOrSet!=null)
				{
					mappedComponentType = BasicMappingHolder.INSTANCE.getMappedByType(parameteerizedTypeIfFieldMethodTypeListOrSet);	
				}
				
			}
			
			
			if(items instanceof RefProperty && mappedComponentType!=null)
			{
				Property changed = getChanged(mappedComponentType, (RefProperty) items, simplifiedSwaggerData);
				if(changed!=null)
				{
					arrayProperty.setItems(changed);
				}
				
			}
			
			
		}
	}
	
	private Property getChanged(String mappedType, RefProperty refProperty, 
			SimplifiedSwaggerData simplifiedSwaggerData)
	{
		Property chnaged=null;
		
		if(mappedType.equals("date-time")||mappedType.equals("time"))
		{
			DateTimeProperty change=new DateTimeProperty();
			change.setAllowEmptyValue(refProperty.getAllowEmptyValue());
			change.setExample(refProperty.getExample());
			change.setAccess(refProperty.getAccess());
			change.setDescription(refProperty.getDescription());
			
			change.setName(refProperty.getName());
			change.setPosition(refProperty.getPosition());
			change.setReadOnly(refProperty.getReadOnly());
			change.setRequired(refProperty.getRequired());
			change.setTitle(refProperty.getTitle());
			change.getVendorExtensions().putAll(refProperty.getVendorExtensions());
			change.setXml(refProperty.getXml());
			
			change.setFormat(mappedType);
			change.setType("string");
			
			
			chnaged=change;
			
		}
		else if(mappedType.equals("date"))
		{
			DateProperty change= new DateProperty();
			change.setAllowEmptyValue(refProperty.getAllowEmptyValue());
			change.setExample(refProperty.getExample());
			change.setAccess(refProperty.getAccess());
			change.setDescription(refProperty.getDescription());
			
			change.setName(refProperty.getName());
			change.setPosition(refProperty.getPosition());
			change.setReadOnly(refProperty.getReadOnly());
			change.setRequired(refProperty.getRequired());
			change.setTitle(refProperty.getTitle());
			change.getVendorExtensions().putAll(refProperty.getVendorExtensions());
			change.setXml(refProperty.getXml());
			
			change.setFormat(mappedType);
			change.setType("string");
			
			
			chnaged=change;
		}
		else if(mappedType.equals("string"))
		{
			StringProperty change= new StringProperty();
			change.setAllowEmptyValue(refProperty.getAllowEmptyValue());
			change.setExample(refProperty.getExample());
			change.setAccess(refProperty.getAccess());
			change.setDescription(refProperty.getDescription());
			
			change.setName(refProperty.getName());
			change.setPosition(refProperty.getPosition());
			change.setReadOnly(refProperty.getReadOnly());
			change.setRequired(refProperty.getRequired());
			change.setTitle(refProperty.getTitle());
			change.getVendorExtensions().putAll(refProperty.getVendorExtensions());
			change.setXml(refProperty.getXml());
			
			change.setFormat(null);
			change.setType(mappedType);
			
			
			chnaged=change;
		}
		if(chnaged!=null)
		{
			String get$ref = refProperty.get$ref();
			simplifiedSwaggerData.getDefinitionsThatCanBeRemoved().add(get$ref.substring("#/definitions/".length()));
			
		}
		return chnaged;
	}
	private void refToBasicIfNeeded(Property property, String mappedType, Map<String, Property> properties, 
			Class fieldMethodType, SimplifiedSwaggerData simplifiedSwaggerData) {
		if(property instanceof RefProperty && mappedType!=null)
		{
			
			RefProperty refProperty=(RefProperty) property;
			Property chnaged = getChanged(mappedType, refProperty,  simplifiedSwaggerData);
			if(chnaged!=null)
			{
				properties.put(chnaged.getName(), chnaged);
			}
		}
	}
	



	private Field getDeclaredField(Class modelClazz, String propertyName)  {
		Field ret=null;
		try {
			ret= modelClazz.getDeclaredField(propertyName);
		} catch (NoSuchFieldException | SecurityException e) {
			//do nothing here
		}
		return ret;
	}
	
	public Method getDeclaredGetter(Class modelClazz, String propertyName)  {
		
		Method ret=null;
		try {
			
			String methodName="get"+ propertyName.substring(0, 1).toUpperCase()+(propertyName.length()>1?propertyName.substring(1):"");
			ret= modelClazz.getMethod(methodName);
		} catch (NoSuchMethodException | SecurityException e) {
			String methodName="is"+ propertyName.substring(0, 1).toUpperCase()+(propertyName.length()>1?propertyName.substring(1):"");
			try {
				ret= modelClazz.getMethod(methodName);
			} catch (NoSuchMethodException | SecurityException e1) {
				//do nothing here
			}
		}
		if(ret!=null)
		{
			if(ret.getReturnType()==null)
			{
				ret=null;
			}
		}
		return ret;
	}


	public Type getClassDefinition(String definitionsKey, NewModelCreator newModelCreator)  {
		try {
			
			Type ret=null;
			if(definitionsKey.contains(ParameterizedComponentKeySymbols.LEFT))
			{
				ret=newModelCreator.getParameterizedModelType(definitionsKey);
			}
			else
			{
				ret=Class.forName(definitionsKey);
			}
			return ret;
		} catch (ClassNotFoundException e) {
			throw new SimplifiedSwaggerException("unable to get class defintion for "+definitionsKey, e);
		}
	}


private List<String> buildList(String... args)
{
	
	List<String> ret= new ArrayList<>();
	if(args!=null)
	{
		for (String row : args) {
			ret.add(row);
		}
	}
	
	return ret;
	
}



private static String[] sortArray(String[] input) {
	Arrays.sort(input);
	return input;
}

	private void buildOperation(Path path, 
			MethodAndTag methdoAndTag, 
			String key, Map<String, Model> definitions,
			OperationTracker operationTracker,
			SimplifiedSwaggerData simplifiedSwaggerData) 
	{
		
		Method method = methdoAndTag.getMethod();
		List<String>  methodTypes=getMethodType(method, key);
		for (String methodType : methodTypes) 
		{
			Operation op= new Operation();
			final OperationTrackerData operationTrackerData = new OperationTrackerData(method, op, methodType);
			operationTracker.add(operationTrackerData);
			Annotation matchedRequestMapping = methdoAndTag.getMatchedRequestMapping();
			
			op.setTags(buildList(methdoAndTag.getTag().getName()));
			
			op.setOperationId(method.getName()+"-"+methodType);
			String[] consumes = (String[]) getAnnotationAttribute(matchedRequestMapping, "consumes");
			String[] produces = (String[]) getAnnotationAttribute(matchedRequestMapping, "produces");
			//op.setConsumes((consumes==null||consumes.length==0)?buildList("application/json"):buildList(consumes));
			//op.setProduces((produces==null||produces.length==0)?buildList("*/*"):buildList(produces));
			op.setConsumes(buildList(consumes));
			op.setProduces(buildList(produces));
			Annotation[] methodAnnotations = method.getDeclaredAnnotations();
			for (Annotation methodAnnotation : methodAnnotations) {
				handleAnnotatedMethod(methodAnnotation, op, method, simplifiedSwaggerData);
			}
			if(op.getConsumes()==null || op.getConsumes().size()==0)
			{
				if(Arrays.binarySearch(NOBODYMETHODTYPES, methodType)<0)
				{
					op.setConsumes(buildList("application/json"));
				}
				
			}
			if(op.getProduces()==null || op.getProduces().size()==0)
			{
				
				op.setProduces(buildList("*/*"));
			}
			boolean preferQueryToFormParameter=operationTrackerData.preferQueryToFormParameter();
			java.lang.reflect.Parameter[] parameters = method.getParameters();
			
			Type[] genericParameterTypes = method.getGenericParameterTypes();
			List<Parameter> opParams = new ArrayList<>();
			for (int i = 0; i < parameters.length; i++) {
				java.lang.reflect.Parameter parameter=parameters[i];
				Type genericParameterType = genericParameterTypes[i];
				
				
				Parameter param = buildOpParameter(parameter, genericParameterType, definitions, preferQueryToFormParameter, simplifiedSwaggerData.getNewModelCreator());
				if(param==null)
				{
					//then paramter is not a basic type
					//lets get paramter array
					//annotations must be handled from within buildOpParameters
					//using the properties
					
					//List<Parameter> params = buildOpParameters(parameter, genericParameterType);
					//opParams.addAll(params);
					//lets instead defer the above resolution
					//treat temprarily like a body paramter
					//will change it later
					ParameterContainer parameterContainer = new ParameterContainer();
					ModelOrRefBuilder bodyParameterBuilder= new ModelOrRefBuilder(genericParameterType, parameterContainer, simplifiedSwaggerData.getNewModelCreator());
					OuterContainer built = bodyParameterBuilder.build();
					BodyParameter bodyParameter = parameterContainer.getBodyParameter();
					bodyParameter.setRequired(true);
					bodyParameter.setName(parameter.getName());
					bodyParameter.getVendorExtensions().put("toresolve", true);
					param=bodyParameter;
					final RefModel schema = (RefModel) bodyParameter.getSchema();
					final Model found = definitions.get(schema.getSimpleRef());
					if(found==null)//this can happen if the actual object is not needed after converting to parameters
					{
						//TODO add a removal logic later
						simplifiedSwaggerData.getNewModelCreator().addIfParemeterizedType(genericParameterType, false);
					}
					opParams.add(bodyParameter);
				}
					
				else
				{
					Annotation[] declaredAnnotations = parameter.getDeclaredAnnotations();
					for (Annotation declaredAnnotation : declaredAnnotations) {
						handleAnnotatedParameter( declaredAnnotation,
								param, parameter, simplifiedSwaggerData);
					}
					opParams.add(param);
				}
				
				
			}
			op.setParameters(opParams);
			Class<?> returnType = method.getReturnType();
			Type genericReturnType = method.getGenericReturnType();
			Map<String, Response> responses = op.getResponses();
			boolean responsesExist=false;
			if(responses==null)
			{
				responses= new LinkedHashMap<>();
			}
			else
			{
				if(responses.size()>0)
				{
					responsesExist=true;
				}
			}
			
			
			if(!responsesExist)
			{
				if(returnType==void.class)
				{
					addResponse(responses, HttpStatus.OK);

				}
				else
				{
					addRefResponse(responses, HttpStatus.OK, returnType, genericReturnType, simplifiedSwaggerData.getNewModelCreator());
				}
				if(applyDefaultResponseMessages)
				{
				addResponse(responses, HttpStatus.CREATED);
				addResponse(responses, HttpStatus.UNAUTHORIZED);
				addResponse(responses, HttpStatus.FORBIDDEN);
				addResponse(responses, HttpStatus.NOT_FOUND);
				}
				else
				{
					if(customGlobalResponseMessages!=null)
					{
						RequestMethod methodTypeRequestMethod=RequestMethod.valueOf(methodType.toUpperCase());
						List<ResponseMessage> responseMessages = customGlobalResponseMessages.get(methodTypeRequestMethod);
						if(responseMessages!=null)
						{
							for (ResponseMessage responseMessage : responseMessages) {
								int httpStatusCode = responseMessage.getCode();
								String message = responseMessage.getMessage();
								ModelReference responseModel = responseMessage.getResponseModel();
								if(responseModel==null)
								{
									addResponse( responses, httpStatusCode, message);
								}
								else
								{
									addRefResponse( responses, httpStatusCode, message, responseModel, simplifiedSwaggerData.getNewModelCreator());
									
								}
							}
						}
						
					}
				}
			}
			
			
			op.setResponses(responses);
			Boolean hidden=(Boolean) op.getVendorExtensions().get("hidden");
			if(hidden!=null && hidden.booleanValue())
			{
				operationTrackerData.setHiddenOperation(true);
			}
			else
			{
				path.set(methodType, op);
			}
			
			
		}
		
		
		
		
	}





	private boolean applyDefaultResponseMessages(){
		boolean ret=true;
		if(docket!=null)
		{
			try {
				Field field = docket.getClass().getDeclaredField("applyDefaultResponseMessages");
				field.setAccessible(true);
				ret = field.getBoolean(docket);
				field.setAccessible(false);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new SimplifiedSwaggerException("could not introspect docket", e);
			}
		}
		return ret;
		
	}


	

	private void addResponse(Map<String, Response> responses, HttpStatus httpStatus) {
		Response response= new Response();
		response.setDescription(httpStatus.name());
		
		responses.put(String.valueOf(httpStatus.value()), response);
	}
	
	private void addRefResponse(Map<String, Response> responses, int  httpStatusCode, String message, ModelReference modelReference,
			 NewModelCreator newModelCreator) {
		ResponseContainer responseContainer = new ResponseContainer();
		ModelOrRefBuilder bodyParameterBuilder;
		try {
			bodyParameterBuilder = new ModelOrRefBuilder(Class.forName(modelReference.getType()), responseContainer, newModelCreator);
			OuterContainer built = bodyParameterBuilder.build();
			Response response=responseContainer.getResponse();
			if(message==null)
			{
				HttpStatus httpStatus=HttpStatus.resolve(httpStatusCode);
				message=httpStatus!=null?httpStatus.name():String.valueOf(httpStatusCode);
			}
			response.setDescription(message);
			responses.put(String.valueOf(httpStatusCode), response);
		} catch (ClassNotFoundException e) {
			throw new SimplifiedSwaggerException("Unable to load class of "+modelReference.getType(), e);
		}
		
	}
	
	private void addResponse(Map<String, Response> responses, int httpStatusCode, String message) {
		Response response= new Response();
		if(message==null)
		{
			HttpStatus httpStatus=HttpStatus.resolve(httpStatusCode);
			message=httpStatus!=null?httpStatus.name():String.valueOf(httpStatusCode);
		}
		response.setDescription(message);
		
		responses.put(String.valueOf(httpStatusCode), response);
	}
	//MAIN CHANGE
	//this method will change
	private void addRefResponse(Map<String, Response> responses, HttpStatus httpStatus, Class<?> returnType,
			Type genericReturnType, NewModelCreator newModelCreator) {
		ResponseContainer responseContainer = new ResponseContainer();
		ModelOrRefBuilder bodyParameterBuilder= new ModelOrRefBuilder(genericReturnType, responseContainer, newModelCreator);
		OuterContainer built = bodyParameterBuilder.build();
		Response response=responseContainer.getResponse();
		//RefResponse response= new RefResponse();
		response.setDescription(httpStatus.name());
		
		
		
		//response.set$ref("#/definitions/"+returnType.getName());
		responses.put(String.valueOf(httpStatus.value()), response);
	}


/*
	private List<Parameter> buildOpParameters(java.lang.reflect.Parameter parameter,
			Type genericParameterType) {
	
		List<Parameter> params= new ArrayList<>();
				
		MultiParameterContainer multiParameterContainer = new MultiParameterContainer();
			ModelOrRefBuilder bodyParameterBuilder= new ModelOrRefBuilder(genericParameterType, multiParameterContainer, newModelCreator);
			OuterContainer built = bodyParameterBuilder.build();
			RefModel schema = (RefModel) multiParameterContainer.getSchema();
			String simpleRef = schema.getSimpleRef();
			Class modelClazz=null;
			Type modelClazzType = getClassDefinition(simpleRef);
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
			
			}
			
	
		return params;
	}
	

*/
	private Parameter buildOpParameter(java.lang.reflect.Parameter parameter,
			Type genericParameterType, Map<String, Model> definitions, boolean preferQueryToFormParam,
			NewModelCreator newModelCreator) {
		Annotation[] annotations = parameter.getDeclaredAnnotations();
		
		
		
		Parameter param= null;
		if(findAnnotations(parameter, PathVariable.class))
		{
			
			param= new PathParameter();
		}
		else if(findAnnotations(parameter, RequestBody.class))
		{
			
			ParameterContainer parameterContainer = new ParameterContainer();
			ModelOrRefBuilder bodyParameterBuilder= new ModelOrRefBuilder(genericParameterType, parameterContainer, newModelCreator);
			OuterContainer built = bodyParameterBuilder.build();
			BodyParameter bodyParameter = parameterContainer.getBodyParameter();
			param=bodyParameter;
			
		
			
			
		}
		else if(findAnnotations(parameter, RequestParam.class))
		{
			param = this.parameterResolver.buildQueryOrFormParameter(preferQueryToFormParam);
			
			
		}
		else if(findAnnotations(parameter, RequestHeader.class))
		{
			param= new HeaderParameter();
		}
		else if(findAnnotations(parameter, CookieValue.class))
		{
			param= new CookieParameter();
		}
		else if(findAnnotations(parameter, RequestPart.class))
		{
			param= new FormParameter();
		}
		else
		{
			//default should be QueryParameter if its basic
			//else must introspect and build array of paramters
			//here will only handle for basic
			//can also handle for lsist, set and maybe array
			//of basic types
			boolean isBasic=false;
			if(genericParameterType instanceof ParameterizedType)
			{
				ParameterizedType parameterizedType=(ParameterizedType) genericParameterType;
				Type rawType = parameterizedType.getRawType();
				if(rawType instanceof Class)
				{
					Class clazz=(Class) rawType;
					if(List.class.isAssignableFrom(clazz)||Set.class.isAssignableFrom(clazz))
					{
						Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
						if(actualTypeArguments.length==1)
						{
							Type actualTypeArgument=actualTypeArguments[0];
							if(actualTypeArgument instanceof Class)
							{
								Class actualTypeArgumentAsClass=(Class) actualTypeArgument; 
								String mappedType = BasicMappingHolder.INSTANCE.getMappedByType(actualTypeArgumentAsClass.getName());
								if(mappedType!=null)
								{
									isBasic=true;
								}
							}
						}
					}
				}
			}
			if(!isBasic)
			{
				Class clazz=parameter.getType();
				if(clazz.isArray())
				{
					Class componentType = clazz.getComponentType();
					String mappedType = BasicMappingHolder.INSTANCE.getMappedByType(componentType.getName());
					if(mappedType!=null)
					{
						isBasic=true;
					}
				}
				else
				{
					//ignore
					//must be a complext type without the annoations
					//we are checking for earlier
				}
			}
			if(!isBasic)
			{
				String mappedType = BasicMappingHolder.INSTANCE.getMappedByType(parameter.getType().getName());
				if(mappedType!=null)
				{
					isBasic=true;
				}
			}
			if(isBasic)
			{
				param = this.parameterResolver.buildQueryOrFormParameter(preferQueryToFormParam);
			}
			
		}
		
		if(param!=null)
		{
			param.setName(parameter.getName());
			
			if(param instanceof SerializableParameter)
			{
				SerializableParameter sparam=(SerializableParameter) param;
				//sparam.setType(parameter.getType().getName());
				BasicMappingHolder.INSTANCE.setTypeAndFormat(sparam, parameter.getType());
				
			}
		}
		
		
		
		
		return param;
	}





	



	private boolean findAnnotations(java.lang.reflect.Parameter parameter, Class<? extends Annotation> type) {
		boolean found=false;
		Annotation[] declaredAnnotationsByType = parameter.getDeclaredAnnotationsByType(type);
		
		if(declaredAnnotationsByType!=null && declaredAnnotationsByType.length>0)
		{
			found=true;
		}
		return found;
	}



	private List<String> getMethodType(Method method, String key) 
	{
		List<String> methodTypes=new ArrayList<>();
		Annotation[] annotations = method.getAnnotations();
		if(annotations!=null)
		{
			for (Annotation annotation : annotations) 
			{
				Annotation matchedRequestMapping=null; 
				for (Class requestMappingType : requestMappingTypes) 
				{
					
					if(requestMappingType==annotation.annotationType())
					{
						matchedRequestMapping=annotation;
						break;
					}
				}
				if(matchedRequestMapping!=null)
				{
					if(matchedRequestMapping.annotationType()!=RequestMapping.class)
					{
						String simpleName = matchedRequestMapping.annotationType().getSimpleName();
						int index=simpleName.indexOf("Mapping");
						if(index!=-1)
						{
							methodTypes.add(simpleName.substring(0, index).toLowerCase());
						}
					}
					else
					{
						RequestMethod[] methods=(RequestMethod[]) getAnnotationAttribute(matchedRequestMapping, "method");
						for (RequestMethod httpMethod : methods) {
							methodTypes.add(httpMethod.name().toLowerCase());	
						}
					}
				}
			}
		}
		
		return methodTypes;
	}


	private void introspectConrollerAdvices(NewModelCreator newModelCreator)
	{
		Map<String, Object> controllerAdvices = listableBeanFactory.getBeansWithAnnotation(ControllerAdvice.class);
		Set<String> keySet = controllerAdvices.keySet();
		for (String key : keySet) 
		{
			
			if(key.equals("repositoryRestExceptionHandler"))
			{
				continue;
			}
			Object controllerAdvice = controllerAdvices.get(key);
			Class controllerAdviceClass=null;
			if(ClassUtils.isCglibProxy(controllerAdvice))
			{
				controllerAdviceClass=ClassUtils.getUserClass(controllerAdvice);
			}
			else
			{
				controllerAdviceClass=controllerAdvice.getClass();
			}
			
			Method[] declaredMethods = controllerAdviceClass.getDeclaredMethods();
			
			for (Method declaredMethod : declaredMethods) 
			{
				if(declaredMethod.isAnnotationPresent(ExceptionHandler.class))
				{
					Class<?> returnType = declaredMethod.getReturnType();
					Type genericReturnType = declaredMethod.getGenericReturnType();
					if(returnType!=void.class ||returnType!=Void.class)
					{
						ResponseContainer responseContainer = new ResponseContainer();
						ModelOrRefBuilder bodyParameterBuilder= new ModelOrRefBuilder(genericReturnType, responseContainer, newModelCreator);
						OuterContainer built = bodyParameterBuilder.build();
						//we only want the response model registered
						
						
					}
				}
							
			}
		}
	}
	private Map<String, List<MethodAndTag>> buildPathToMethodAndTagMap(List<Tag> tags) {
		
		
		Map<String, List<MethodAndTag>> pathToMethodListMap= new HashMap<>();
		Map<String, Object> controllers = listableBeanFactory.getBeansWithAnnotation(Controller.class);
		Map<String, Object> restControllers = listableBeanFactory.getBeansWithAnnotation(RestController.class);
		Map<String, Object> merged= new HashMap<>();
		merged.putAll(controllers);
		merged.putAll(restControllers);
		Set<String> keySet = merged.keySet();
		for (String key : keySet) 
		{
			
			Object controller = merged.get(key);
			Class controllerClass=null;
			if(ClassUtils.isCglibProxy(controller))
			{
				controllerClass=ClassUtils.getUserClass(controller);
			}
			else
			{
				controllerClass=controller.getClass();
			}
			
			if(arraysContains(controllerClass))
			{
				continue;
			}
			
			Tag tag= new Tag();
			tag.setName(key);
			tag.setDescription(controllerClass.getName());
			Set<String> basesSet= new HashSet<>();
			
			if(controllerClass.isAnnotationPresent(RequestMapping.class))
			{
				RequestMapping controllerRequestMapping = (RequestMapping) controllerClass.getAnnotation(RequestMapping.class);	
				if(controllerRequestMapping!=null)
				{
					String[] bases = controllerRequestMapping.value();
					for (String base : bases) {
						basesSet.add(base);
					}
					bases=controllerRequestMapping.path();
					for (String base : bases) {
						basesSet.add(base);
					}
					
				}
			}
			
			
			
			tags.add(tag);
			Method[] declaredMethods = controllerClass.getDeclaredMethods();
			
			for (Method declaredMethod : declaredMethods) 
			{
				Annotation[] annotations = declaredMethod.getAnnotations();
				for (Annotation annotation : annotations) 
				{
					Annotation matchedRequestMapping=null; 
					for (Class requestMappingType : requestMappingTypes) 
					{
						
						if(requestMappingType==annotation.annotationType())
						{
							matchedRequestMapping=annotation;
							break;
						}
					}
					if(matchedRequestMapping!=null)
					{
						Set<String> pathsSet= new HashSet<>();
						if(basesSet.size()>0)
						{
							for (String base : basesSet) {
								
								addPathsToPathSetForABase(matchedRequestMapping, pathsSet, base);
							}
						}
						else
						{
							addPathsToPathSetForABase(matchedRequestMapping, pathsSet, "");
						}
						
						for (String path : pathsSet) 
						{
							List<MethodAndTag> list = pathToMethodListMap.get(path);
							if(list==null)
							{
								list= new ArrayList<MethodAndTag>();
								pathToMethodListMap.put(path, list);
							}
							MethodAndTag methodAndTag= new MethodAndTag(declaredMethod, tag, matchedRequestMapping);
							list.add(methodAndTag);
						}
						
					}
					
				}
				
			}
			
			
		}
		return pathToMethodListMap;
	}

	private void addPathsToPathSetForABase(Annotation matchedRequestMapping, Set<String> pathsSet, String base) {
		if(base.length()>0 && !base.startsWith("/"))
		{
			base="/"+base;
		}
		if(base.endsWith("/"))
		{
			base=base.substring(0, base.length()-1);
		}
		String[] valuesInAnnotation=(String[]) getAnnotationAttribute(matchedRequestMapping, "value");
		for (String string : valuesInAnnotation) {
			if(!string.startsWith("/"))
			{
				string="/"+string;
			}
			string=base+string;
			pathsSet.add(string);
		}
		String[] pathsInAnnotation=(String[]) getAnnotationAttribute(matchedRequestMapping, "path");
		for (String string : pathsInAnnotation) {
			if(!string.startsWith("/"))
			{
				string="/"+string;
			}
			string=base+string;
			pathsSet.add(string);
		}
	}


	private boolean arraysContains(Class controllerClass) {
		boolean found=false;
		for (String check : constrollersToIgnore) {
			if(check.equals(controllerClass.getName()))
			{
				found=true;
				break;
			}
		}
		return found;
	}



	


	

	//private Properties propertyTypeMappingProps;
	
	private Object getAnnotationAttribute(Annotation matchedRequestMapping, String attributeName) {
		
		
		try {
			Method method = matchedRequestMapping.annotationType().getMethod(attributeName, new Class[] {});
			Object path = method.invoke(matchedRequestMapping, new Object[] {});
			return  path;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new SimplifiedSwaggerException("could not get path", e);
		}
		
	
	
}
	
	private void handleAnnotatedProperty(Property property, Annotation annotation, Class propertyType, SimplifiedSwaggerData simplifiedSwaggerData) 
	{

		if(!annotation.annotationType().getPackage().getName().equals(SwaggerDecoratorConstants.SWAGGER_ANNOTATION_PACKAGE))
		{
			String beanName = annotation.annotationType().getName() + SwaggerDecoratorConstants.DECORATOR_SUFFIX;
			if (context.containsBean(beanName)) 
			{
				ISwaggerDecorator bean = context.getBean(beanName, ISwaggerDecorator.class);
				bean.decorateProperty(property, annotation, propertyType);
			} else {
				simplifiedSwaggerData.getUnMappedAnnotations().add(annotation.annotationType());
	
			}
		}

	}
	
	private void handleAnnotatedApiProperty(Property property, Annotation annotation, Class propertyType, SimplifiedSwaggerData simplifiedSwaggerData) {
		if((!(annotation instanceof ApiParam))  && annotation.annotationType().getPackage().getName().equals(SwaggerDecoratorConstants.SWAGGER_ANNOTATION_PACKAGE))
		{
			String beanName = annotation.annotationType().getName() + SwaggerDecoratorConstants.DECORATOR_SUFFIX;
			if (context.containsBean(beanName)) 
			{
				ISwaggerDecorator bean = context.getBean(beanName, ISwaggerDecorator.class);
				bean.decorateProperty(property, annotation, propertyType);
			} else {
				simplifiedSwaggerData.getUnMappedAnnotations().add(annotation.annotationType());
	
			}
		}
		
	}
	
	private void handleAnnotatedParameter(
	
			Annotation annotation, Parameter matchedOperationParameter,
			java.lang.reflect.Parameter methodParameter, SimplifiedSwaggerData simplifiedSwaggerData) {
		
		if(!(annotation instanceof ApiParam))
		{
			String beanName = annotation.annotationType().getName() + SwaggerDecoratorConstants.DECORATOR_SUFFIX;
			if (context.containsBean(beanName)) 
			{
				ISwaggerDecorator bean = context.getBean(beanName, ISwaggerDecorator.class);
				bean.decorateParameter(matchedOperationParameter, annotation, methodParameter);
			} else {
				simplifiedSwaggerData.getUnMappedAnnotations().add(annotation.annotationType());
	
			}
		}
	}
	
	private void handleAnnotatedMethod(
			Annotation annotation, Operation operation,
			Method method, SimplifiedSwaggerData simplifiedSwaggerData) {
		
		//for methods lets handle both swagger and validation annotations togather
		String beanName = annotation.annotationType().getName() + SwaggerDecoratorConstants.DECORATOR_SUFFIX;
		if (context.containsBean(beanName)) 
		{
			ISwaggerDecorator bean = context.getBean(beanName, ISwaggerDecorator.class);
			
			bean.decorateOperation(operation, annotation, method, simplifiedSwaggerData.getNewModelCreator());
		} else {
			simplifiedSwaggerData.getUnMappedAnnotations().add(annotation.annotationType());

		}
		
		
	}
	
	private void handleAnnotatedModel(Model model, Annotation annotation, Class modelClazz,SimplifiedSwaggerData simplifiedSwaggerData) {
		if(!annotation.annotationType().getPackage().getName().equals(SwaggerDecoratorConstants.SWAGGER_ANNOTATION_PACKAGE))
		{
			String beanName = annotation.annotationType().getName() + SwaggerDecoratorConstants.DECORATOR_SUFFIX;
			if (context.containsBean(beanName)) 
			{
				ISwaggerDecorator bean = context.getBean(beanName, ISwaggerDecorator.class);
				bean.decorateModel(model, annotation, modelClazz);
			} else {
				simplifiedSwaggerData.getUnMappedAnnotations().add(annotation.annotationType());
	
			}
		}
	}


	

}

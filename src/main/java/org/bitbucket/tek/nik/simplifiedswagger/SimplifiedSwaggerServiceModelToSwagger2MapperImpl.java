package org.bitbucket.tek.nik.simplifiedswagger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.bitbucket.tek.nik.simplifiedswagger.exception.SimplifiedSwaggerException;
import org.bitbucket.tek.nik.simplifiedswagger.modelbuilder.ModelOrRefBuilder;
import org.bitbucket.tek.nik.simplifiedswagger.modelbuilder.OuterContainer;
import org.bitbucket.tek.nik.simplifiedswagger.modelbuilder.ParameterContainer;
import org.bitbucket.tek.nik.simplifiedswagger.modelbuilder.ParameterizedComponentKeySymbols;
import org.bitbucket.tek.nik.simplifiedswagger.modelbuilder.ResponseContainer;
import org.bitbucket.tek.nik.simplifiedswagger.newmodels.NewModelCreator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.ISwaggerDecorator;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.auth.SecuritySchemeDefinition;
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
import springfox.documentation.service.Documentation;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2MapperImpl;

public class SimplifiedSwaggerServiceModelToSwagger2MapperImpl extends ServiceModelToSwagger2MapperImpl {

	@Autowired
	private ApplicationContext context;
	


	
	@Autowired
	private ListableBeanFactory listableBeanFactory;
	
	@Value("${showUnMappedAnnotations:false}")
	boolean showUnMappedAnnotations;
	
	private static final Class[] requestMappingTypes= {RequestMapping.class, GetMapping.class, PostMapping.class, PutMapping.class, PatchMapping.class, DeleteMapping.class};

	private NewModelCreator newModelCreator;

	@Override
	public Swagger mapDocumentation(Documentation from) {
		
		
			
			Swagger swagger = super.mapDocumentation(from);
			Info info = swagger.getInfo();
			String basePath = swagger.getBasePath();
			Map<String, Model> definitions = swagger.getDefinitions();
			newModelCreator= new NewModelCreator(definitions);
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
			Map<String, List<MethodAndTag>> pathToMethodListMap = buildPathToMethodAndTagMap(tags);
			Set<String> keySet = pathToMethodListMap.keySet();
			for (String key : keySet) {
				Path path= new Path();
				swagger.getPaths().put(key, path);
				
				List<MethodAndTag> list = pathToMethodListMap.get(key);
				for (MethodAndTag methdoAndTag : list) {
					 buildOperation(path, methdoAndTag, key);
					
					
				}
			}
			newModelCreator.build();
			transformDefinitions(definitions);
			adjustExamples(definitions);
			
			if(showUnMappedAnnotations)
			{
				System.err.println("unMappedAnnotations=" + unMappedAnnotations);
			}
			//unused code below will remove later. commenting out for now
			//newModelCreator.tempShowBlocked();
			return swagger;

		
				
		
		
		
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
	
	private void adjustExamples(Map<String, Model> definitions) {
		Set<String> definitionsKeySet = definitions.keySet();
		for (String definitionsKey : definitionsKeySet) {
			if(definitionsKey.contains(ParameterizedComponentKeySymbols.LEFT))
			{
				continue;
			}
			Class modelClazz=null;
			Type modelClazzType = getClassDefinition(definitionsKey);
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
			Set<String> propertiesKeySet = properties.keySet();
			for (String propertiesKey : propertiesKeySet) {
				Property property = properties.get(propertiesKey);
				
				String name = property.getName();
				//if(!modelClazz.isEnum())
				{
					//you cant put contraints on a setter only getetr or fioeld
					Method getter=getDeclaredGetter(modelClazz, property);
					Field field = getFieldAfterCheckingWithGetter(modelClazz, propertiesKey, property, getter);
					Class fieldMethodType = getFieldMethodType(field, getter);
					String parameteerizedTypeIfFieldMethodTypeListOrSet = getParameteerizedTypeIfFieldMethodTypeListOrSet(
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


	private void transformDefinitions(Map<String, Model> definitions) {
		Set<String> definitionsKeySet = definitions.keySet();
		for (String definitionsKey : definitionsKeySet) {
			
			Class modelClazz=null;
			Type modelClazzType = getClassDefinition(definitionsKey);
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
					handleAnnotatedModel(model, declaredClassAnnotation, modelClazz);
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
			
				
				Set<String> propertiesKeySet = properties.keySet();
				for (String propertiesKey : propertiesKeySet) {
					Property property = properties.get(propertiesKey);
					String name = property.getName();
					
				
					
					
					//if(!modelClazz.isEnum())
					{
						//you cant put contraints on a setter only getetr or fioeld
						Method getter=getDeclaredGetter(modelClazz, property);
						Field field = getFieldAfterCheckingWithGetter(modelClazz, propertiesKey, property, getter);
						Class fieldMethodType = getFieldMethodType(field, getter);
						String parameteerizedTypeIfFieldMethodTypeListOrSet = getParameteerizedTypeIfFieldMethodTypeListOrSet(
								field, getter, fieldMethodType);
						String mappedType = BasicMappingHolder.INSTANCE.getMappedByType(fieldMethodType.getName());
						refToBasicIfNeeded(property, mappedType, properties, fieldMethodType);
						refToBasicInArrayIfNeeded(property, mappedType, properties, fieldMethodType, 
								parameteerizedTypeIfFieldMethodTypeListOrSet);
						if(field!=null)
						{
							Annotation[] annotations = field.getAnnotations();
							for (Annotation annotation : annotations) {
								handleAnnotatedProperty(property, annotation, fieldMethodType);
							}
						}
						if(getter!=null)
						{
							Annotation[] annotations = getter.getAnnotations();
							for (Annotation annotation : annotations) {
								handleAnnotatedProperty(property, annotation, fieldMethodType);
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
					}
					
					
					
				}
			}
		
		}
		
		
		for (String definitionsThatCanBeRemovedKey : definitionsThatCanBeRemoved) {
			definitions.remove(definitionsThatCanBeRemovedKey);
		}
		
	}

	private Field getFieldAfterCheckingWithGetter(Class modelClazz, String propertiesKey, Property property,
			Method getter) {
		Field field = getDeclaredField(modelClazz, property);
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

	private String getParameteerizedTypeIfFieldMethodTypeListOrSet(Field field, Method getter, Class fieldMethodType) {
		String parameteerizedTypeIfFieldMethodTypeListOrSet =null;
		if(field!=null)
		{

			parameteerizedTypeIfFieldMethodTypeListOrSet = 
					getParameterizedTypeIfListOrSet(field.getGenericType(),
					fieldMethodType);
			
			
		}
		if(getter!=null)
		{

			parameteerizedTypeIfFieldMethodTypeListOrSet = 
					getParameterizedTypeIfListOrSet(getter.getGenericReturnType(),
					fieldMethodType);
		}
		return parameteerizedTypeIfFieldMethodTypeListOrSet;
	}

	private Class getFieldMethodType(Field field, Method getter) {
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


	private String getParameterizedTypeIfListOrSet(Type genericType, Class fieldMethodType) {
		String parameteerizedTypeIfFieldMethodTypeListOrSet=null;
		if(List.class.isAssignableFrom(fieldMethodType)||Set.class.isAssignableFrom(fieldMethodType))
		{
			
			if(genericType instanceof ParameterizedType)
			{
				ParameterizedType pt=(ParameterizedType) genericType;
				Type[] actualTypeArguments = pt.getActualTypeArguments();
				if(actualTypeArguments.length==1)
				{
					parameteerizedTypeIfFieldMethodTypeListOrSet=actualTypeArguments[0].getTypeName();
				}
				
			}
		}
		return parameteerizedTypeIfFieldMethodTypeListOrSet;
	}


	


	
	private void refToBasicInArrayIfNeeded(Property property, 
			String mappedType, Map<String, Property> properties, 
			Class fieldMethodType, 
			String parameteerizedTypeIfFieldMethodTypeListOrSet) {
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
				Property changed = getChanged(mappedComponentType, (RefProperty) items);
				if(changed!=null)
				{
					arrayProperty.setItems(changed);
				}
				
			}
			
			
		}
	}
	
	private Property getChanged(String mappedType, RefProperty refProperty)
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
			definitionsThatCanBeRemoved.add(get$ref.substring("#/definitions/".length()));
			
		}
		return chnaged;
	}
	private void refToBasicIfNeeded(Property property, String mappedType, Map<String, Property> properties, Class fieldMethodType) {
		if(property instanceof RefProperty && mappedType!=null)
		{
			
			RefProperty refProperty=(RefProperty) property;
			Property chnaged = getChanged(mappedType, refProperty);
			if(chnaged!=null)
			{
				properties.put(chnaged.getName(), chnaged);
			}
		}
	}
	
	private Set<String> definitionsThatCanBeRemoved= new HashSet<>();


	private Field getDeclaredField(Class modelClazz, Property property)  {
		Field ret=null;
		try {
			ret= modelClazz.getDeclaredField(property.getName());
		} catch (NoSuchFieldException | SecurityException e) {
			//do nothing here
		}
		return ret;
	}
	
	private Method getDeclaredGetter(Class modelClazz, Property property)  {
		String propertyName = property.getName();
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


	private Type getClassDefinition(String definitionsKey)  {
		try {
			
			Type ret=null;
			if(definitionsKey.contains(ParameterizedComponentKeySymbols.LEFT))
			{
				ret=this.newModelCreator.getParameterizedModelType(definitionsKey);
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
	for (String row : args) {
		ret.add(row);
	}
	return ret;
	
}
	private void buildOperation(Path path, MethodAndTag methdoAndTag, String key) 
	{
		
		Method method = methdoAndTag.getMethod();
		List<String>  methodTypes=getMethodType(method, key);
		for (String methodType : methodTypes) 
		{
			Operation op= new Operation();
			Annotation matchedRequestMapping = methdoAndTag.getMatchedRequestMapping();
			
			op.setTags(buildList(methdoAndTag.getTag().getName()));
			path.set(methodType, op);
			op.setOperationId(method.getName()+"-"+methodType);
			String[] consumes = (String[]) getAnnotationAttribute(matchedRequestMapping, "consumes");
			String[] produces = (String[]) getAnnotationAttribute(matchedRequestMapping, "produces");
			op.setConsumes((consumes==null||consumes.length==0)?buildList("application/json"):buildList(consumes));
			op.setProduces((produces==null||produces.length==0)?buildList("*/*"):buildList(produces));
			
			
			Annotation[] methodAnnotations = method.getDeclaredAnnotations();
			for (Annotation methodAnnotation : methodAnnotations) {
				handleAnnotatedMethod(methodAnnotation, op, method);
			}
			
			java.lang.reflect.Parameter[] parameters = method.getParameters();
			
			Type[] genericParameterTypes = method.getGenericParameterTypes();
			List<Parameter> opParams = new ArrayList<>();
			for (int i = 0; i < parameters.length; i++) {
				java.lang.reflect.Parameter parameter=parameters[i];
				Type genericParameterType = genericParameterTypes[i];
				
			
				Parameter param = buildOpParameter(parameter, genericParameterType);
				Annotation[] declaredAnnotations = parameter.getDeclaredAnnotations();
				for (Annotation declaredAnnotation : declaredAnnotations) {
					handleAnnotatedParameter( declaredAnnotation,
							param, parameter);
				}
				opParams.add(param);
				/*
				 * For now because we are using only vendor extensions this will work.
				 * Will improvise later when we stop using vendor extensions
				 */
				
				String existingParameterDescription = param.getDescription();
				existingParameterDescription=existingParameterDescription!=null?existingParameterDescription:param.getName();
				Map<String, Object> vendorExtensions = param.getVendorExtensions();
				StringBuilder sb= new StringBuilder();
				sb.append(existingParameterDescription);
				addDescriptionUsingVendorExtensions( vendorExtensions, sb);
				//sb.append("</h6>");
				//sb.append("</table>");
				param.setDescription(sb.toString());
			}
			op.setParameters(opParams);
			Class<?> returnType = method.getReturnType();
			Type genericReturnType = method.getGenericReturnType();
			Map<String, Response> responses= new LinkedHashMap<>();
			if(returnType==void.class)
			{
				addResponse(responses, HttpStatus.OK);

			}
			else
			{
				addRefResponse(responses, HttpStatus.OK, returnType, genericReturnType);
			}
			
			addResponse(responses, HttpStatus.CREATED);
			addResponse(responses, HttpStatus.UNAUTHORIZED);
			addResponse(responses, HttpStatus.FORBIDDEN);
			addResponse(responses, HttpStatus.NOT_FOUND);
			
			op.setResponses(responses);
			
			
			
			
		}
		
		
	}


	private void addDescriptionUsingVendorExtensions(
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

	private void addResponse(Map<String, Response> responses, HttpStatus httpStatus) {
		Response response= new Response();
		response.setDescription(httpStatus.name());
		
		responses.put(String.valueOf(httpStatus.value()), response);
	}
	//MAIN CHANGE
	//this method will change
	private void addRefResponse(Map<String, Response> responses, HttpStatus httpStatus, Class<?> returnType,
			Type genericReturnType) {
		ResponseContainer responseContainer = new ResponseContainer();
		ModelOrRefBuilder bodyParameterBuilder= new ModelOrRefBuilder(genericReturnType, responseContainer, newModelCreator);
		OuterContainer built = bodyParameterBuilder.build();
		Response response=responseContainer.getResponse();
		//RefResponse response= new RefResponse();
		response.setDescription(httpStatus.name());
		
		
		
		//response.set$ref("#/definitions/"+returnType.getName());
		responses.put(String.valueOf(httpStatus.value()), response);
	}



	
	


	private Parameter buildOpParameter(java.lang.reflect.Parameter parameter,
			Type genericParameterType) {
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
			
			param= new QueryParameter();
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
		
		param.setName(parameter.getName());
		
		if(param instanceof SerializableParameter)
		{
			SerializableParameter sparam=(SerializableParameter) param;
			//sparam.setType(parameter.getType().getName());
			BasicMappingHolder.INSTANCE.setTypeAndFormat(sparam, parameter.getType());
			
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

private String[] constrollersToIgnore= {"org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController",
		springfox.documentation.swagger.web.ApiResourceController.class.getName()};

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
						
						String[] valuesInAnnotation=(String[]) getAnnotationAttribute(matchedRequestMapping, "value");
						for (String string : valuesInAnnotation) {
							if(!string.startsWith("/"))
							{
								string="/"+string;
							}
							pathsSet.add(string);
						}
						String[] pathsInAnnotation=(String[]) getAnnotationAttribute(matchedRequestMapping, "path");
						for (String string : pathsInAnnotation) {
							if(!string.startsWith("/"))
							{
								string="/"+string;
							}
							pathsSet.add(string);
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



	private Set<Class> unMappedAnnotations = new HashSet<>();

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
	
	private void handleAnnotatedProperty(Property property, Annotation annotation, Class propertyType) 
	{
		String beanName = annotation.annotationType().getName() + SwaggerDecoratorConstants.DECORATOR_SUFFIX;
		if (context.containsBean(beanName)) 
		{
			ISwaggerDecorator bean = context.getBean(beanName, ISwaggerDecorator.class);
			bean.decorateProperty(property, annotation, propertyType);
		} else {
			unMappedAnnotations.add(annotation.annotationType());

		}

	}
	
	private void handleAnnotatedParameter(
			Annotation annotation, Parameter matchedOperationParameter,
			java.lang.reflect.Parameter methodParameter) {
		String beanName = annotation.annotationType().getName() + SwaggerDecoratorConstants.DECORATOR_SUFFIX;
		if (context.containsBean(beanName)) 
		{
			ISwaggerDecorator bean = context.getBean(beanName, ISwaggerDecorator.class);
			bean.decorateParameter(matchedOperationParameter, annotation, methodParameter);
		} else {
			unMappedAnnotations.add(annotation.annotationType());

		}
	}
	
	private void handleAnnotatedMethod(
			Annotation annotation, Operation operation,
			Method method) {
		String beanName = annotation.annotationType().getName() + SwaggerDecoratorConstants.DECORATOR_SUFFIX;
		if (context.containsBean(beanName)) 
		{
			ISwaggerDecorator bean = context.getBean(beanName, ISwaggerDecorator.class);
			bean.decorateOperation(operation, annotation, method);
		} else {
			unMappedAnnotations.add(annotation.annotationType());

		}
	}
	
	private void handleAnnotatedModel(Model model, Annotation annotation, Class modelClazz) {
		String beanName = annotation.annotationType().getName() + SwaggerDecoratorConstants.DECORATOR_SUFFIX;
		if (context.containsBean(beanName)) 
		{
			ISwaggerDecorator bean = context.getBean(beanName, ISwaggerDecorator.class);
			bean.decorateModel(model, annotation, modelClazz);
		} else {
			unMappedAnnotations.add(annotation.annotationType());

		}
	}


	

}

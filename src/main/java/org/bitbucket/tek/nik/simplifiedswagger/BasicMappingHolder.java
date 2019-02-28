package org.bitbucket.tek.nik.simplifiedswagger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
/*
 * will later make it a spring type of singleton bean if needed
 * for now using this

 */
public class BasicMappingHolder {
	
	private static final String PARAM_FORMAT_MAPPING_PROPERTIES_FILE_NAME = "param-format-mapping.properties";
	private static final String PARAM_TYPE_MAPPING_PROPERTIES_FILE_NAME = "param-type-mapping.properties";
	private static final String PROPERTY_TYPE_MAPPING_PROPERTIES_FILE_NAME = "property-type-mapping.properties";
	public static final BasicMappingHolder INSTANCE= new BasicMappingHolder();
	/*
	 * should later shift this mapping to xml
	 */
	private final Properties propertyTypeMappingProps;
	private final Properties paramTypeMappingProps;
	private final Properties paramFormatMappingProps;

	private BasicMappingHolder()  {
		super();
			this.propertyTypeMappingProps=buildProperties(PROPERTY_TYPE_MAPPING_PROPERTIES_FILE_NAME);
			this.paramTypeMappingProps=buildProperties(PARAM_TYPE_MAPPING_PROPERTIES_FILE_NAME);
			this.paramFormatMappingProps=buildProperties(PARAM_FORMAT_MAPPING_PROPERTIES_FILE_NAME);
	}

	private Properties buildProperties(String fileName) {
		URL resource = getResourceInternal(fileName);
		if(resource==null)
		{
			resource = getResourceInternal("/"+fileName);
		}
		if(resource==null)
		{
			resource = getResourceInternal("\\"+fileName);
		}
		
		if(resource==null)
		{
			throw new RuntimeException("could not load "+fileName);
		}
		Properties propertyTypeMappingProps = new Properties();
		try(InputStream is = resource.openStream();)
		{
			propertyTypeMappingProps.load(is);
		}
		
		catch (IOException e) {
			throw new RuntimeException("could not load ", e);
		}
		return propertyTypeMappingProps;
	}

	private URL getResourceInternal(String fileName) {
		URL resource=null;
		
		resource = BasicMappingHolder.class.getResource(fileName);
		if(resource == null)
		{
			resource = BasicMappingHolder.class.getClassLoader().getResource(fileName);
		}
		return resource;
	}
	/*
	 * must depercate this
	 * will try deprecating again
	 */
	public String getMappedByType(String className)
	{
		String mappedType = propertyTypeMappingProps.getProperty(className);
		return mappedType;
	}
	/*
	 * must depercate this
	 */
	public String getMappedByPropertyType(Class clazz)
	{
		return getMappedByInternal(clazz, propertyTypeMappingProps);
	}
	
	private String getMappedByParamFormat(Class clazz)
	{
		return paramFormatMappingProps.getProperty(clazz.getName());
	}
	
	private String getMappedByParamType(Class clazz)
	{
		return getMappedByInternal(clazz, paramTypeMappingProps);
	}

	private String getMappedByInternal(Class clazz, Properties properties) {
		String mappedType = properties.getProperty(clazz.getName());
		if(clazz.isEnum())
		{
			mappedType="string";
		}
		return mappedType;
	}
	
	
	public void setTypeAndFormat(SerializableParameter serializableParameter, Class parameterType)
	{
		String paramType=getMappedByParamType(parameterType);
		if(paramType==null)
		{
			throw new RuntimeException("need a mapping for "+parameterType.getName() +" in "+PARAM_TYPE_MAPPING_PROPERTIES_FILE_NAME);
		}
		serializableParameter.setType(paramType);
		//see https://swagger.io/docs/specification/data-models/data-types/
		//should i convert all the types
		String paramFormat=getMappedByParamFormat(parameterType);
		if(paramFormat!=null)
		{
			serializableParameter.setFormat(paramFormat);
		}
	}
	public  Property buildBasicProperty(  Class clazz) {
		String mappedByType = getMappedByPropertyType(clazz);
		return buildBasicProperty(mappedByType, clazz);
	}
	/**
	 * must deperecate this also
	 * @param mappedType
	 * @param clazz
	 * @return
	 */
	private  Property buildBasicProperty( String mappedType , Class clazz) {
		Property property=null;
		if(mappedType!=null)
		{
		if(mappedType.equals("string"))
		{
			StringProperty strProperty= new StringProperty();
			if(clazz.isEnum())
			{
				Object[] enumConstants = clazz.getEnumConstants();
				
				List<String> strings=new ArrayList<>();
				for (Object object : enumConstants) {
					strings.add(object.toString());
				}
				strProperty.setEnum(strings);
			}
			property=strProperty;
		}
		else if(mappedType.equals("date-time"))
		{
			property= new DateTimeProperty();
		}
		else if(mappedType.equals("date"))
		{
			property= new DateProperty();
		}
		else if(mappedType.equals("time"))
		{
			property= new DateTimeProperty();
		}
		else if(mappedType.equals("boolean"))
		{
			property= new BooleanProperty();
		}
		else if(mappedType.equals("float"))
		{
			property= new FloatProperty();
		}
		else if(mappedType.equals("double"))
		{
			property= new DoubleProperty();
		}
		else if(mappedType.equals("int32"))
		{
			property= new IntegerProperty();
		}
		else if(mappedType.equals("int64"))
		{
			property= new LongProperty();
		}
		else if(mappedType.equals("object"))
		{
			property= new ObjectProperty();
		}
		else 
		{
			
			throw new RuntimeException("Unknown mapped type of "+mappedType);
		}
		}
		return property;
		
	}
	
	

}

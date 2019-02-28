package org.bitbucket.tek.nik.simplifiedswagger.newmodels;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bitbucket.tek.nik.simplifiedswagger.BasicMappingHolder;

import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;


public class NonParameterizedPropertyHandler {
	
	private final ChainControl chainControl;
	public NonParameterizedPropertyHandler(Map<String, Model> definitions, 
		
			NewModelCreator newModelCreator) {
		super();
		this.chainControl = new ChainControl(definitions, newModelCreator);
	}
	
	
	public void handleNonParameterizedProperty(HashMap<String, Property> modelProperties,
			String propertyName, Class type) {
		
		
		
		
		if(type.isArray())
		{
			Class componentType = type.getComponentType();
			ArrayProperty arrayProperty= new ArrayProperty();
			arrayProperty.setName(propertyName);
			modelProperties.put(propertyName, arrayProperty);
			//build chain till it ends without a list, set, arry,map
			//there invoke addIf
			new Chain(chainControl, arrayProperty, componentType).chain();
		}
		else if(List.class.isAssignableFrom(type)||Set.class.isAssignableFrom(type))
		{
			
			Type componentType=Object.class;
			
			ArrayProperty arrayProperty= new ArrayProperty();
			arrayProperty.setName(propertyName);
			modelProperties.put(propertyName, arrayProperty);
			new Chain(chainControl, arrayProperty, componentType).chain();
		}
		else if(Map.class.isAssignableFrom(type))
		{
			Type valueType=Object.class;
			
			MapProperty mapProperty = new MapProperty();
			mapProperty.setName(propertyName);
			modelProperties.put(propertyName, mapProperty);
			new Chain(chainControl, mapProperty, valueType).chain();
			
		}
		else
		{
			//improve handling
			//what of list arry,map,set
			Property property = BasicMappingHolder.INSTANCE.buildBasicProperty(type);
			if(property==null)
			{
				RefProperty refProperty= new RefProperty();
				refProperty.set$ref("#/definitions/"+type.getName());
				if(chainControl.getDefinitions().get("#/definitions/"+type.getName())==null)
				{
					chainControl.getNewModelCreator().addIfParemeterizedType(type, false);
				}
				property=refProperty;
			}
			property.setName(propertyName);
			modelProperties.put(propertyName, property);
			

		}
		
	}
	

	

}

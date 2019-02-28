package org.bitbucket.tek.nik.simplifiedswagger.newmodels;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

public class ParameterizedPropertyHandler {
	
	private final ChainControl chainControl;
	public ParameterizedPropertyHandler(Map<String, Model> definitions, 
			
			NewModelCreator newModelCreator) {
		super();
		this.chainControl = new ChainControl(definitions,  newModelCreator);
	}
	
	
	public void handleParameterizedProperty(HashMap<String, Property> modelProperties,
			String propertyName, ParameterizedType type, Map<String, Type> typeVariableToActualTypeMapFromParentClass) {
		
		Type rawType = type.getRawType();
		/**
		 * assume this casting will always work else will have to solve it
		 * and use an else
		 */
		Class rawTypeClass=(Class) rawType;
		if(rawTypeClass.isArray())
		{
			Type componentType = rawTypeClass.getComponentType();
			componentType = convertTypeVariableComponentsToActual(typeVariableToActualTypeMapFromParentClass,
					componentType);
			ArrayProperty arrayProperty= new ArrayProperty();
			arrayProperty.setName(propertyName);
			modelProperties.put(propertyName, arrayProperty);
			//build chain till it ends without a list, set, arry,map
			//there invoke addIf
			
			new Chain(chainControl, arrayProperty, componentType).chain();
		}
		else if(List.class.isAssignableFrom(rawTypeClass)||Set.class.isAssignableFrom(rawTypeClass))
		{
			Type[] actualTypeArguments = type.getActualTypeArguments();
			Type componentType=Object.class;
			if(actualTypeArguments.length==1)
			{
				componentType=actualTypeArguments[0];
				componentType = convertTypeVariableComponentsToActual(typeVariableToActualTypeMapFromParentClass,
						componentType);
			}
			ArrayProperty arrayProperty= new ArrayProperty();
			arrayProperty.setName(propertyName);
			modelProperties.put(propertyName, arrayProperty);
			new Chain(chainControl, arrayProperty, componentType).chain();
		}
		else if(Map.class.isAssignableFrom(rawTypeClass))
		{
			Type valueType=Object.class;
			Type[] actualTypeArguments = type.getActualTypeArguments();
			if(actualTypeArguments!=null && actualTypeArguments.length==2)
			{
				Type keyType = actualTypeArguments[0];
				valueType = actualTypeArguments[1];
				
				
			}
			MapProperty mapProperty = new MapProperty();
			mapProperty.setName(propertyName);
			modelProperties.put(propertyName, mapProperty);
			
			valueType = convertTypeVariableComponentsToActual(typeVariableToActualTypeMapFromParentClass, valueType);
			new Chain(chainControl, mapProperty, valueType).chain();
			
		}
		else
		{
			//improve handling
			//what of list arry,map,set
			String ref = type.toString();
			ref=ref.replace('<', '«');
			ref=ref.replace('>', '»');
			RefProperty refProperty= new RefProperty();
			refProperty.set$ref("#/definitions/"+ref);
			if(chainControl.getDefinitions().get(ref)==null)
			{
				chainControl.getNewModelCreator().addIfParemeterizedType(type, false);
			}
			
			refProperty.setName(propertyName);
			modelProperties.put(propertyName, refProperty);
		}
		
	}


	private Type convertTypeVariableComponentsToActual(Map<String, Type> typeVariableToActualTypeMapFromParentClass,
			Type componentType) {
		if(componentType instanceof TypeVariable)
		{
			if(typeVariableToActualTypeMapFromParentClass!=null)
			{
				componentType=typeVariableToActualTypeMapFromParentClass.get(((TypeVariable) componentType).getName());
			}
		}
		return componentType;
	}
	

	

}

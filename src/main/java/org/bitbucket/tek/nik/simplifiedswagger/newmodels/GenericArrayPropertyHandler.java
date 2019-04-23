package org.bitbucket.tek.nik.simplifiedswagger.newmodels;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bitbucket.tek.nik.simplifiedswagger.modelbuilder.ParameterizedComponentKeySymbols;

import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

public class GenericArrayPropertyHandler {
	
	private final ChainControl chainControl;
	public GenericArrayPropertyHandler(Map<String, Model> definitions, 
			
			NewModelCreator newModelCreator) {
		super();
		this.chainControl = new ChainControl(definitions,  newModelCreator);
	}
	
	
	public void handleGenericArrayProperty(HashMap<String, Property> modelProperties,
			String propertyName, GenericArrayType type, Map<String, Type> typeVariableToActualTypeMapFromParentClass) {
		final Type genericComponentType = type.getGenericComponentType();
		
	
			Type componentType = genericComponentType;
			componentType = convertTypeVariableComponentsToActual(typeVariableToActualTypeMapFromParentClass,
					componentType);
			ArrayProperty arrayProperty= new ArrayProperty();
			arrayProperty.setName(propertyName);
			modelProperties.put(propertyName, arrayProperty);
			//build chain till it ends without a list, set, arry,map
			//there invoke addIf
			
			new Chain(chainControl, arrayProperty, componentType).chain();
		
				
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

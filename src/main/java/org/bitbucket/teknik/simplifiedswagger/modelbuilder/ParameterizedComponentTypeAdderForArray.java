package org.bitbucket.teknik.simplifiedswagger.modelbuilder;

import java.lang.reflect.ParameterizedType;

import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

public class ParameterizedComponentTypeAdderForArray extends ParameterizedComponentTypeAddingTemplate {

	public ParameterizedComponentTypeAdderForArray(BuilderCurrentState builderCurrentState) {
		super(builderCurrentState);
		
	}

	


	
	@Override
	protected Model newModelWithNonBasicPropertyOrRefOrItemForOuterContainer(ParameterizedType componentType) {
		ArrayModel model= new ArrayModel();
		RefProperty refProperty=new RefProperty();
		String keyForParameterizedComponentType = ParameterizedComponentKeyBuilder.buildKeyForParameterizedComponentType(componentType);
		refProperty.set$ref(keyForParameterizedComponentType);
		model.setItems(refProperty);
		return model;
	}
	
	

	@Override
	protected Model newModelForOuterContainer() {
		
		return new ArrayModel();
	}
	
	@Override
	protected Property usualNonBasicProperty(ParameterizedType componentType) {
		ArrayProperty arrayProperty= new ArrayProperty();
		RefProperty refProperty=new RefProperty();
		String keyForParameterizedComponentType = ParameterizedComponentKeyBuilder.buildKeyForParameterizedComponentType(componentType);
		refProperty.set$ref(keyForParameterizedComponentType);
		arrayProperty.setItems(refProperty);
		return arrayProperty;
	}
	
	@Override
	protected Property usualPostponingProperty() {
		return new ArrayProperty();
	}

	
}

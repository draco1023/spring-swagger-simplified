package org.bitbucket.tek.nik.simplifiedswagger.modelbuilder;

import java.lang.reflect.ParameterizedType;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

public class ParameterizedComponentTypeAdderForMap extends ParameterizedComponentTypeAddingTemplate {

	public ParameterizedComponentTypeAdderForMap(BuilderCurrentState builderCurrentState) {
		super(builderCurrentState);
		
	}

	

	
	
	@Override
	protected Model newModelWithNonBasicPropertyOrRefOrItemForOuterContainer(ParameterizedType componentType) {
		RefModel model=new RefModel();
		String keyForParameterizedComponentType = ParameterizedComponentKeyBuilder.buildKeyForParameterizedComponentType(componentType);
		model.set$ref(keyForParameterizedComponentType);
		return model;
	}


	@Override
	protected Model newModelForOuterContainer() {
		Model model=new ModelImpl();
		ModelImpl modelImpl=(ModelImpl) model;
		(( ModelImpl)model).setType("object");
		return model;
	}
	
	@Override
	protected Property usualNonBasicProperty(ParameterizedType componentType) {
		RefProperty additionalProperties= new RefProperty();
		String keyForParameterizedComponentType = ParameterizedComponentKeyBuilder.buildKeyForParameterizedComponentType(componentType);
		additionalProperties.set$ref(keyForParameterizedComponentType);
		
		return additionalProperties;
	}
	
	@Override
	protected Property usualPostponingProperty() {
		return new MapProperty();
	}


}

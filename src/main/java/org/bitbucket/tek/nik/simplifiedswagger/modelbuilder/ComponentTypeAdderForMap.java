package org.bitbucket.tek.nik.simplifiedswagger.modelbuilder;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

public class ComponentTypeAdderForMap extends ComponentTypeAddingTemplate {

	public ComponentTypeAdderForMap(BuilderCurrentState builderCurrentState) {
		super(builderCurrentState);
		
	}

	


	@Override
	protected Model newModelWithNonBasicPropertyOrRefOrItemForOuterContainer(Class componentType) {
		ModelImpl model= new ModelImpl();
		RefProperty refModel=new RefProperty();
		refModel.set$ref(componentType.getName());
		model.setAdditionalProperties(refModel);
		
		return model;
	}

	@Override
	protected Model newModelWithBasicPropertyOrRefOrItemForOuterContainer(Property basicComponentTypeProperty) {
		ModelImpl model= new ModelImpl();
		model.setAdditionalProperties(basicComponentTypeProperty);
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
	protected Property usualBasicProperty(Property basicComponentTypeProperty) {
		MapProperty mapProperty = new MapProperty();
		mapProperty.setAdditionalProperties(basicComponentTypeProperty);
		return mapProperty;
	}
	
	@Override
	protected Property usualNonBasicProperty(Class componentType) {
		RefProperty additionalProperties= new RefProperty();
		additionalProperties.set$ref(componentType.getName());
		
		return additionalProperties;
	}
	
	@Override
	protected Property usualPostponingProperty() {
		return new MapProperty();
	}



}

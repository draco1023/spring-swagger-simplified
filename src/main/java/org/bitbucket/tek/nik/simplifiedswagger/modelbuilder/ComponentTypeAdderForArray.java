package org.bitbucket.tek.nik.simplifiedswagger.modelbuilder;

import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

public class ComponentTypeAdderForArray extends ComponentTypeAddingTemplate {

	public ComponentTypeAdderForArray(BuilderCurrentState builderCurrentState) {
		super(builderCurrentState);
		
	}


	@Override
	protected Model newModelWithNonBasicPropertyOrRefOrItemForOuterContainer(Class componentType) {
		ArrayModel model= new ArrayModel();
		RefProperty refProperty=new RefProperty();
		refProperty.set$ref(componentType.getName());
		
		model.setItems(refProperty);
		return model;
	}
	
	@Override
	protected Model newModelWithBasicPropertyOrRefOrItemForOuterContainer(Property basicComponentTypeProperty) {
		ArrayModel model= new ArrayModel();
		model.setItems(basicComponentTypeProperty);
		return model;
	}
	


	@Override
	protected Model newModelForOuterContainer() {
	
		return new ArrayModel();
	}

	
	@Override
	protected Property usualBasicProperty(Property basicComponentTypeProperty) {
		ArrayProperty arrayProperty= new ArrayProperty();
		
		arrayProperty.setItems(basicComponentTypeProperty);
		return arrayProperty;
	}
	
	@Override
	protected Property usualNonBasicProperty(Class componentType) {
		ArrayProperty arrayProperty= new ArrayProperty();
		RefProperty refProperty=new RefProperty();
		refProperty.set$ref(componentType.getName());
		
		arrayProperty.setItems(refProperty);
		return arrayProperty;
	}
	
	@Override
	protected Property usualPostponingProperty() {
		return new ArrayProperty();
	}


}

package org.bitbucket.teknik.simplifiedswagger.modelbuilder;

import java.lang.reflect.ParameterizedType;

import io.swagger.models.ArrayModel;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

public  class NonSetListOrMapButParameterizedClassAdder {

	private BuilderCurrentState builderCurrentState;
	

	public NonSetListOrMapButParameterizedClassAdder(BuilderCurrentState builderCurrentState) {
		super();
		this.builderCurrentState = builderCurrentState;
		
	}

	public void add() {
		ParameterizedType clazz=(ParameterizedType) this.builderCurrentState.getCurrentGenericType();
		if (this.builderCurrentState.getCurrentContainer() instanceof OuterContainer) {
			OuterContainer outerContainer = (OuterContainer) this.builderCurrentState.getCurrentContainer();
			
				RefModel model=new RefModel();
				String keyForParameterizedComponentType = ParameterizedComponentKeyBuilder.buildKeyForParameterizedComponentType(clazz);
				model.set$ref(keyForParameterizedComponentType);
				
				outerContainer.setSchema(model);
			
				
			this.builderCurrentState.setCurrentContainer(null);//no more//might not be null for parameterized
			this.builderCurrentState.setCurrentGenericType(null);

		} else if (this.builderCurrentState.getCurrentContainer() instanceof ModelImpl) {
			ModelImpl modelImplConatiner = (ModelImpl) this.builderCurrentState.getCurrentContainer();
			Property property = usual(clazz);
			modelImplConatiner.setAdditionalProperties(property);

		} else if (this.builderCurrentState.getCurrentContainer() instanceof MapProperty) {
			MapProperty mapPropertyConatiner = (MapProperty) this.builderCurrentState.getCurrentContainer();
			Property property = usual(clazz);
			mapPropertyConatiner.setAdditionalProperties(property);
			

		} else if (this.builderCurrentState.getCurrentContainer() instanceof ArrayModel) {
			ArrayModel arrayModelConatiner = (ArrayModel) this.builderCurrentState.getCurrentContainer();
			Property property = usual(clazz);
			arrayModelConatiner.setItems(property);

			

		} else if (this.builderCurrentState.getCurrentContainer() instanceof ArrayProperty) {
			ArrayProperty arrayPropertyConatiner = (ArrayProperty) this.builderCurrentState.getCurrentContainer();
			Property property = usual(clazz);
			arrayPropertyConatiner.setItems(property);

		}
	}

	private Property usual(ParameterizedType  clazz) {
		Property property=null;
		
		RefProperty refProperty=new RefProperty();
		String keyForParameterizedComponentType = ParameterizedComponentKeyBuilder.buildKeyForParameterizedComponentType(clazz);
		refProperty.set$ref(keyForParameterizedComponentType);
		property=refProperty;
			
			
		
		
		this.builderCurrentState.setCurrentContainer(null);//no more//might not be null for parameterized
		this.builderCurrentState.setCurrentGenericType(null);
		return property;
	}



}

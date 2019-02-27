package org.bitbucket.teknik.simplifiedswagger.modelbuilder;

import io.swagger.models.ArrayModel;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;

public  class SimpleMapAdder {

	private BuilderCurrentState builderCurrentState;
	

	public SimpleMapAdder(BuilderCurrentState builderCurrentState) {
		super();
		this.builderCurrentState = builderCurrentState;
		
	}

	public void add() {
		if (this.builderCurrentState.getCurrentContainer() instanceof OuterContainer) {
			OuterContainer outerContainer = (OuterContainer) this.builderCurrentState.getCurrentContainer();
			ModelImpl model=new ModelImpl();
			model.setType("object");//since its non parameterized cant tell what is the type of key, value
			
			outerContainer.setSchema(model);
			this.builderCurrentState.setCurrentContainer(null);
			this.builderCurrentState.setCurrentGenericType(null);

		} else if (this.builderCurrentState.getCurrentContainer() instanceof ModelImpl) {
			ModelImpl modelImplConatiner = (ModelImpl) this.builderCurrentState.getCurrentContainer();
			MapProperty mapProperty=new MapProperty();
			
			mapProperty.setType("object");
			modelImplConatiner.setAdditionalProperties(mapProperty);
			
			this.builderCurrentState.setCurrentContainer(null);
			this.builderCurrentState.setCurrentGenericType(null);

		} else if (this.builderCurrentState.getCurrentContainer() instanceof MapProperty) {
			MapProperty mapPropertyConatiner = (MapProperty) this.builderCurrentState.getCurrentContainer();
			
			MapProperty mapProperty=new MapProperty();
			
			mapProperty.setType("object");
			mapPropertyConatiner.setAdditionalProperties(mapProperty);
			
			this.builderCurrentState.setCurrentContainer(null);
			this.builderCurrentState.setCurrentGenericType(null);

		} else if (this.builderCurrentState.getCurrentContainer() instanceof ArrayModel) {
			ArrayModel arrayModelConatiner = (ArrayModel) this.builderCurrentState.getCurrentContainer();
			MapProperty mapProperty=new MapProperty();
			
			mapProperty.setType("object");
			arrayModelConatiner.setItems(mapProperty);
			this.builderCurrentState.setCurrentContainer(null);
			this.builderCurrentState.setCurrentGenericType(null);

		} else if (this.builderCurrentState.getCurrentContainer() instanceof ArrayProperty) {
			ArrayProperty arrayPropertyConatiner = (ArrayProperty) this.builderCurrentState.getCurrentContainer();
			MapProperty mapProperty=new MapProperty();
			
			mapProperty.setType("object");//s
			arrayPropertyConatiner.setItems(mapProperty);
			this.builderCurrentState.setCurrentContainer(null);//no more//might not be null for parameterized
			this.builderCurrentState.setCurrentGenericType(null);

		}
	}



}

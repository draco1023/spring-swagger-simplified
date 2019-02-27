package org.bitbucket.teknik.simplifiedswagger.modelbuilder;

import org.bitbucket.teknik.simplifiedswagger.BasicMappingHolder;

import io.swagger.models.ArrayModel;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

public  class SimpleNonSetListOrMapButClassAdder {

	private BuilderCurrentState builderCurrentState;
	

	public SimpleNonSetListOrMapButClassAdder(BuilderCurrentState builderCurrentState) {
		super();
		this.builderCurrentState = builderCurrentState;
		
	}

	public void add() {
		Class clazz=(Class) this.builderCurrentState.getCurrentGenericType();
		if (this.builderCurrentState.getCurrentContainer() instanceof OuterContainer) {
			OuterContainer outerContainer = (OuterContainer) this.builderCurrentState.getCurrentContainer();
			String mappedTo = BasicMappingHolder.INSTANCE.getMappedByType(clazz.getName());
			
			if(mappedTo!=null)
			{
				ModelImpl model= new ModelImpl();
				model.setType(mappedTo);
				outerContainer.setSchema(model);
			}
			else
			{
				RefModel model=new RefModel();
				model.set$ref(clazz.getName());
				
				outerContainer.setSchema(model);
			}
				
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

	private Property usual(Class clazz) {
		Property property=null;
		Property basicProperty = BasicMappingHolder.INSTANCE.buildBasicProperty(clazz);
		if(basicProperty!=null)
		{
			property=basicProperty;
		}
		else
		{
			RefProperty refProperty=new RefProperty();
			refProperty.set$ref(clazz.getName());
			property=refProperty;
		}
		
		
		
		this.builderCurrentState.setCurrentContainer(null);//no more//might not be null for parameterized
		this.builderCurrentState.setCurrentGenericType(null);
		return property;
	}



}

package org.bitbucket.tek.nik.simplifiedswagger.modelbuilder;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;

public abstract class ParameterizedComponentTypeAddingTemplate {
	
	private BuilderCurrentState builderCurrentState;


	public ParameterizedComponentTypeAddingTemplate(BuilderCurrentState builderCurrentState) {
		super();
		this.builderCurrentState = builderCurrentState;
		
	}
	
	public void addComponentType(ParameterizedType  componentType)
	{
		Class classOfComponentType=(Class) componentType.getRawType();
		if(this.builderCurrentState.getCurrentContainer() instanceof OuterContainer)
		{
			OuterContainer outerContainer=(OuterContainer) this.builderCurrentState.getCurrentContainer();
			if(classOfComponentType.isArray()||
					List.class.isAssignableFrom(classOfComponentType)||
					Set.class.isAssignableFrom(classOfComponentType)||
					Map.class.isAssignableFrom(classOfComponentType))
			{
				Model model = newModelForOuterContainer();//model for a map or array
				
				outerContainer.setSchema(model);
				this.builderCurrentState.setCurrentGenericType(componentType);//to use in next build as needed

				this.builderCurrentState.setCurrentContainer(model);
			}
			else
			{
				Model model = newModelWithNonBasicPropertyOrRefOrItemForOuterContainer(componentType);
				outerContainer.setSchema(model);
				this.builderCurrentState.setCurrentContainer(null);
				this.builderCurrentState.setCurrentGenericType(null);
			}

		}
		else if(this.builderCurrentState.getCurrentContainer() instanceof ModelImpl)
		{
			ModelImpl modelImplConatiner=(ModelImpl) this.builderCurrentState.getCurrentContainer();
			if(classOfComponentType.isArray()||
					List.class.isAssignableFrom(classOfComponentType)||
					Set.class.isAssignableFrom(classOfComponentType)||
					Map.class.isAssignableFrom(classOfComponentType))
			{
				/*
				 * when this condition happens
				 * want to avoid deciding what to do.
				 * differ to next build iteration
				 */
				Property newPropertyForModelImpl= usualPostponingProperty();
				modelImplConatiner.setAdditionalProperties(newPropertyForModelImpl);
				this.builderCurrentState.setCurrentGenericType(componentType);//to use in next build as needed
				this.builderCurrentState.setCurrentContainer(newPropertyForModelImpl);
			}
			else
			{
				Property additionalProperties= usualNonBasicProperty(componentType);
				modelImplConatiner.setAdditionalProperties(additionalProperties);
				this.builderCurrentState.setCurrentContainer(null);
				this.builderCurrentState.setCurrentGenericType(null);
			}
		}
		else if(this.builderCurrentState.getCurrentContainer() instanceof MapProperty)
		{
			MapProperty mapPropertyConatiner=(MapProperty) this.builderCurrentState.getCurrentContainer();
			if(classOfComponentType.isArray()||
					List.class.isAssignableFrom(classOfComponentType)||
					Set.class.isAssignableFrom(classOfComponentType)||
					Map.class.isAssignableFrom(classOfComponentType))
			{
				Property additionalProperties = usualPostponingProperty();
				/*
				 * when this condition happens
				 * want to avoid deciding what to do.
				 * differ to next build iteration
				 */
				
				mapPropertyConatiner.setAdditionalProperties(additionalProperties);
				
				this.builderCurrentState.setCurrentGenericType(componentType);//to use in next build as needed

				this.builderCurrentState.setCurrentContainer(additionalProperties);
			}
			else
			{
				Property additionalProperties= usualNonBasicProperty(componentType);
				mapPropertyConatiner.setAdditionalProperties(additionalProperties);
				this.builderCurrentState.setCurrentContainer(null);
				this.builderCurrentState.setCurrentGenericType(null);
			}
		
		}
		else if(this.builderCurrentState.getCurrentContainer() instanceof ArrayModel)
		{
			ArrayModel arrayModelConatiner=(ArrayModel) this.builderCurrentState.getCurrentContainer();
			
			//needs more 
			//whqat if classOfComponentType is an array, list, set or map
			if(classOfComponentType.isArray()||
					List.class.isAssignableFrom(classOfComponentType)||
					Set.class.isAssignableFrom(classOfComponentType)||
					Map.class.isAssignableFrom(classOfComponentType))
			{
				/*
				 * when this condition happens
				 * want to avoid deciding what to do.
				 * differ to next build iteration
				 */
				Property arrayProperty= usualPostponingProperty();
				arrayModelConatiner.setItems(arrayProperty);
				this.builderCurrentState.setCurrentGenericType(componentType);//to use in next build as needed
				this.builderCurrentState.setCurrentContainer(arrayProperty);
			}
			else
			{
				
				Property newArrayModelsNonBasicProperty = usualNonBasicProperty(componentType);
				arrayModelConatiner.setItems(newArrayModelsNonBasicProperty);
				this.builderCurrentState.setCurrentContainer(null);
				this.builderCurrentState.setCurrentGenericType(null);
			}

		}
		else if(this.builderCurrentState.getCurrentContainer() instanceof ArrayProperty)
		{
			ArrayProperty arrayPropertyConatiner=(ArrayProperty) this.builderCurrentState.getCurrentContainer();
			
			//needs more 
			//whqat if classOfComponentType is an array, list, set or map
			if(classOfComponentType.isArray()||
					List.class.isAssignableFrom(classOfComponentType)||
					Set.class.isAssignableFrom(classOfComponentType)||
					Map.class.isAssignableFrom(classOfComponentType))
			{
				/*
				 * when this condition happens
				 * want to avoid deciding what to do.
				 * differ to next build iteration
				 */
				Property newPropertyForArrayProperty= usualPostponingProperty();
				arrayPropertyConatiner.setItems(newPropertyForArrayProperty);
				this.builderCurrentState.setCurrentGenericType(componentType);//to use in next build as needed

				this.builderCurrentState.setCurrentContainer(newPropertyForArrayProperty);
			}
			else
			{
				Property newArrayPropertiesNonBasicProperty=usualNonBasicProperty(componentType);
				arrayPropertyConatiner.setItems(newArrayPropertiesNonBasicProperty);
				this.builderCurrentState.setCurrentContainer(null);
				this.builderCurrentState.setCurrentGenericType(null);
			}

		}
		}

	

	
	protected abstract Model newModelWithNonBasicPropertyOrRefOrItemForOuterContainer(ParameterizedType componentType);


	
	/**
	 * this could be an arrayModel or a ModelImpl
	 * depending on whether we targetting array or map.
	 * We dont set the component type.
	 * we dont want to do taht because it might be array orlist//set or map
	 * @return
	 */
	protected abstract  Model newModelForOuterContainer();

	protected abstract Property usualNonBasicProperty(ParameterizedType componentType) ;

	protected abstract Property usualPostponingProperty() ;
	
	

}

package org.bitbucket.tek.nik.simplifiedswagger.modelbuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bitbucket.tek.nik.simplifiedswagger.BasicMappingHolder;

import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;

public abstract class ComponentTypeAddingTemplate {
	
	private BuilderCurrentState builderCurrentState;
	

	public ComponentTypeAddingTemplate(BuilderCurrentState builderCurrentState) {
		super();
		this.builderCurrentState = builderCurrentState;
		
	}
	
	public void addComponentType(Class componentType)
	{
		if(this.builderCurrentState.getCurrentContainer() instanceof OuterContainer)
		{
			OuterContainer outerContainer=(OuterContainer) this.builderCurrentState.getCurrentContainer();
			if(componentType.isArray()||
					List.class.isAssignableFrom(componentType)||
					Set.class.isAssignableFrom(componentType)||
					Map.class.isAssignableFrom(componentType))
			{
				Model model = newModelForOuterContainer();//model for a map or array
				
				outerContainer.setSchema(model);
				this.builderCurrentState.setCurrentGenericType(componentType);//to use in next build as needed

				this.builderCurrentState.setCurrentContainer(model);
			}
			else
			{
				Property basicComponentTypeProperty = BasicMappingHolder.INSTANCE.buildBasicProperty(componentType);
				if(basicComponentTypeProperty!=null)
				{
					Model model = newModelWithBasicPropertyOrRefOrItemForOuterContainer(basicComponentTypeProperty);
					outerContainer.setSchema(model);
				}
				else
				{
					Model model = newModelWithNonBasicPropertyOrRefOrItemForOuterContainer(componentType);
					outerContainer.setSchema(model);
				}
				
				this.builderCurrentState.setCurrentContainer(null);
				this.builderCurrentState.setCurrentGenericType(null);
			}

		}
		else if(this.builderCurrentState.getCurrentContainer() instanceof ModelImpl)
		{
			ModelImpl modelImplConatiner=(ModelImpl) this.builderCurrentState.getCurrentContainer();
			if(componentType.isArray()||
					List.class.isAssignableFrom(componentType)||
					Set.class.isAssignableFrom(componentType)||
					Map.class.isAssignableFrom(componentType))
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
				Property basicComponentTypeProperty = BasicMappingHolder.INSTANCE.buildBasicProperty(componentType);
				if(basicComponentTypeProperty!=null)
				{
					Property additionalProperties= usualBasicProperty(basicComponentTypeProperty);
					modelImplConatiner.setAdditionalProperties(additionalProperties);
				}
				else
				{
					Property additionalProperties= usualNonBasicProperty(componentType);
					modelImplConatiner.setAdditionalProperties(additionalProperties);
				}
				
				
				this.builderCurrentState.setCurrentContainer(null);
				this.builderCurrentState.setCurrentGenericType(null);
			}
		}
		else if(this.builderCurrentState.getCurrentContainer() instanceof MapProperty)
		{
			MapProperty mapPropertyConatiner=(MapProperty) this.builderCurrentState.getCurrentContainer();
			if(componentType.isArray()||
					List.class.isAssignableFrom(componentType)||
					Set.class.isAssignableFrom(componentType)||
					Map.class.isAssignableFrom(componentType))
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
				Property basicComponentTypeProperty = BasicMappingHolder.INSTANCE.buildBasicProperty(componentType);
				if(basicComponentTypeProperty!=null)
				{
					Property additionalProperties= usualBasicProperty(basicComponentTypeProperty);
					mapPropertyConatiner.setAdditionalProperties(additionalProperties);
				}
				else
				{
					Property additionalProperties= usualNonBasicProperty(componentType);
					mapPropertyConatiner.setAdditionalProperties(additionalProperties);
					
				}
				
				this.builderCurrentState.setCurrentContainer(null);
				this.builderCurrentState.setCurrentGenericType(null);
			}
		
		}
		else if(this.builderCurrentState.getCurrentContainer() instanceof ArrayModel)
		{
			ArrayModel arrayModelConatiner=(ArrayModel) this.builderCurrentState.getCurrentContainer();
			
			//needs more 
			//whqat if classOfComponentType is an array, list, set or map
			if(componentType.isArray()||
					List.class.isAssignableFrom(componentType)||
					Set.class.isAssignableFrom(componentType)||
					Map.class.isAssignableFrom(componentType))
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
				Property basicComponentTypeProperty = BasicMappingHolder.INSTANCE.buildBasicProperty(componentType);
				if(basicComponentTypeProperty!=null)
				{
					Property newArrayModelsBasicProperty = usualBasicProperty(basicComponentTypeProperty);
					arrayModelConatiner.setItems(newArrayModelsBasicProperty);
				}
				else
				{
					Property newArrayModelsNonBasicProperty = usualNonBasicProperty(componentType);
					arrayModelConatiner.setItems(newArrayModelsNonBasicProperty);
				}
				this.builderCurrentState.setCurrentContainer(null);
				this.builderCurrentState.setCurrentGenericType(null);
			}

		}
		else if(this.builderCurrentState.getCurrentContainer() instanceof ArrayProperty)
		{
			ArrayProperty arrayPropertyConatiner=(ArrayProperty) this.builderCurrentState.getCurrentContainer();
			
			//needs more 
			//whqat if classOfComponentType is an array, list, set or map
			if(componentType.isArray()||
					List.class.isAssignableFrom(componentType)||
					Set.class.isAssignableFrom(componentType)||
					Map.class.isAssignableFrom(componentType))
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
				Property basicComponentTypeProperty = BasicMappingHolder.INSTANCE.buildBasicProperty(componentType);
				if(basicComponentTypeProperty!=null)
				{
					Property newArrayPropertiesNonBasicProperty=usualBasicProperty(basicComponentTypeProperty);
					arrayPropertyConatiner.setItems(newArrayPropertiesNonBasicProperty);
				}
				else
				{
					Property newArrayPropertiesNonBasicProperty=usualNonBasicProperty(componentType);
					arrayPropertyConatiner.setItems(newArrayPropertiesNonBasicProperty);
				}
				
				this.builderCurrentState.setCurrentContainer(null);
				this.builderCurrentState.setCurrentGenericType(null);
			}

		}
		}
	



	
	protected abstract Model newModelWithNonBasicPropertyOrRefOrItemForOuterContainer(Class componentType);
	protected abstract Model newModelWithBasicPropertyOrRefOrItemForOuterContainer(Property basicComponentTypeProperty);

	
	protected abstract  Model newModelForOuterContainer();

	protected abstract Property usualBasicProperty(Property basicComponentTypeProperty) ;

	protected abstract Property usualNonBasicProperty(Class componentType) ;

	protected abstract Property usualPostponingProperty() ;
	
	

}

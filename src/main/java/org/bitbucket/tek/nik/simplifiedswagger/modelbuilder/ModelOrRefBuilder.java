package org.bitbucket.tek.nik.simplifiedswagger.modelbuilder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bitbucket.tek.nik.simplifiedswagger.newmodels.NewModelCreator;

public class ModelOrRefBuilder {
	
	private Type rootGenericType;
	
	
	
	private final OuterContainer outerContainer;
	private NewModelCreator newModelCreator;
	
	private BuilderCurrentState builderCurrentState;
	
	public List<Object> getCurrentContainerStack() {
		return builderCurrentState.getCurrentContainerStack();
	}

	public ModelOrRefBuilder(Type rootGenericType, OuterContainer outerContainer, NewModelCreator newModelCreator)
	{
		
		this.outerContainer=outerContainer;
		this.builderCurrentState= new BuilderCurrentState(outerContainer, rootGenericType);
		
	
		this.rootGenericType=rootGenericType;
		this.newModelCreator=newModelCreator;
		
	}
	public ModelOrRefBuilder(Type rootGenericType, OuterContainer outerContainer)
	{
		this(rootGenericType, outerContainer, null);
		
	}
	
	public boolean isBuilt()
	{
		return this.builderCurrentState.getCurrentContainer()==null;
	}
	
	public OuterContainer build()
	{
		OuterContainer built =null;
		for (boolean isbuilt = this.isBuilt(); !isbuilt; isbuilt = this.isBuilt()) 
		{
			built = this.buildIteration();
			
		}
		return this.outerContainer;
	}
	public OuterContainer buildIteration()
	{
		
		if(this.builderCurrentState.getCurrentContainer()==null)
		{
			throw new RuntimeException("no more build");
		}
		if(this.builderCurrentState.getCurrentContainerStack().size() > 0 && this.builderCurrentState.getCurrentContainer()==this.builderCurrentState.getCurrentContainerStack().get(this.builderCurrentState.getCurrentContainerStack().size()-1))
		{
			throw new RuntimeException("Check logic. Container is not changing");
		}
		this.builderCurrentState.getCurrentContainerStack().add(this.builderCurrentState.getCurrentContainer());
		
		if(this.builderCurrentState.getCurrentGenericType() instanceof ParameterizedType)
		{
			ParameterizedType parameterizedType=(ParameterizedType) this.builderCurrentState.getCurrentGenericType();
			Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
			Class clazz=(Class) parameterizedType.getRawType();
			TypeVariable[] typeParameters = clazz.getTypeParameters();
			
			Map<String, Type> typeVariableToActualTypeMap= new HashMap<>();
			for (int i = 0; i < typeParameters.length; i++) {
				TypeVariable typeVariable = typeParameters[i];
				typeVariableToActualTypeMap.put(typeVariable.getName(), actualTypeArguments[i]);
			}
			
			if(clazz.isArray())
			{
				
				new ComponentTypeAdderForArray(this.builderCurrentState).addComponentType(clazz.getComponentType());
				
			}
			else if(List.class.isAssignableFrom(clazz)||Set.class.isAssignableFrom(clazz))
			{
				
				if(actualTypeArguments!=null && actualTypeArguments.length==1)
				{
					Type type = actualTypeArguments[0];
					if(type instanceof TypeVariable)
					{
						type = typeVariableToActualTypeMap.get(((TypeVariable) type).getName());
						
					}
					
					if(type instanceof Class)
					{
						new ComponentTypeAdderForArray(this.builderCurrentState).addComponentType((Class)type);
						
					}
					else if(type instanceof ParameterizedType)
					{
						new ParameterizedComponentTypeAdderForArray(this.builderCurrentState).addComponentType((ParameterizedType)type);
						
					}
					else if(type instanceof WildcardType)
					{
						this.builderCurrentState.setCurrentContainer(null);
						this.builderCurrentState.setCurrentGenericType(null);
					}
					else
					{
						this.builderCurrentState.setCurrentContainer(null);
						this.builderCurrentState.setCurrentGenericType(null);
					}
				}
				else
				{
					//must detect when this happens
					throw new RuntimeException("havent handled this");
				}
			}
			else if(Map.class.isAssignableFrom(clazz))
			{
				
				if(actualTypeArguments!=null && actualTypeArguments.length==2)
				{
					Type type = actualTypeArguments[1];
					if(type instanceof TypeVariable)
					{
						type = typeVariableToActualTypeMap.get(((TypeVariable) type).getName());
						
					}
					
					if(type instanceof Class)
					{
						new ComponentTypeAdderForMap(this.builderCurrentState).addComponentType((Class)type);
						
					}
					
					else if(type instanceof ParameterizedType)
					{
						new ParameterizedComponentTypeAdderForMap(this.builderCurrentState).addComponentType((ParameterizedType)type);
						
					}
					else if(type instanceof WildcardType)
					{
						this.builderCurrentState.setCurrentContainer(null);
						this.builderCurrentState.setCurrentGenericType(null);
					}
					else
					{
						this.builderCurrentState.setCurrentContainer(null);
						this.builderCurrentState.setCurrentGenericType(null);
					}
				}
				else
				{
					//must detect when this happens
					throw new RuntimeException("havent handled this");
				}
				
				
			}
			else
			{
				if(newModelCreator!=null)
				{
					new NonSetListOrMapButParameterizedClassAdder(this.builderCurrentState).add();
					newModelCreator.addIfParemeterizedType(parameterizedType, true);
				}
				
			}
			
		}
		/*else if(this.builderCurrentState.getCurrentGenericType() instanceof WildcardType)
		{
			WildcardType wildcardType=(WildcardType) this.builderCurrentState.getCurrentGenericType();
			Class clazz=Object.class;
			Type[] upperBounds = wildcardType.getUpperBounds();
			if(upperBounds.length==1)
			{
				clazz=(Class) upperBounds[0];
			}
			System.out.println("***wildcard");
			this.builderCurrentState.setCurrentContainer(null);
			this.builderCurrentState.setCurrentGenericType(null);
		}*/
		else if(this.builderCurrentState.getCurrentGenericType() instanceof Class)
		{
			buildForSimpleClass();
		}
		else
		{
			throw new RuntimeException(" got type of "+this.builderCurrentState.getCurrentGenericType().getClass().getName());
		}
		return outerContainer;
	}

	private void buildForSimpleClass() {
		Class clazz=(Class) this.builderCurrentState.getCurrentGenericType();
		if(clazz.isArray())//even though its non parameterized for ararys we can use comepoennt type
		{
			new ComponentTypeAdderForArray(this.builderCurrentState).addComponentType(clazz.getComponentType());
			
			
			
		}//stopping here for array
		else if(List.class.isAssignableFrom(clazz)||Set.class.isAssignableFrom(clazz))
		{
			//since list is not parameterized cant know the type
			//must treat type as object
			new ComponentTypeAdderForArray(this.builderCurrentState).addComponentType(Object.class);
		}//stopping here for array as itslist or set
		else if(Map.class.isAssignableFrom(clazz))
		{
			new SimpleMapAdder(this.builderCurrentState).add();
			
		}
		else
		{
			//so whats letft
			//an actual classthats not map,set orlist
			
			new SimpleNonSetListOrMapButClassAdder(this.builderCurrentState).add();
		}
	}

	

	
	

}

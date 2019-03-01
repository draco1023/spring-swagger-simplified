package org.bitbucket.tek.nik.simplifiedswagger.newmodels;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bitbucket.tek.nik.simplifiedswagger.BasicMappingHolder;
import org.bitbucket.tek.nik.simplifiedswagger.modelbuilder.ParameterizedComponentKeySymbols;

import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;


public class Chain {
	
	private  final ChainControl chainControl;
	private final Property property;
	private final Type componentOrValueType;
	public Chain(ChainControl chainControl, Property property, Type componentOrValueType) {
		super();
		this.chainControl = chainControl;
		this.property = property;
		this.componentOrValueType = componentOrValueType;
	}
	
	public void chain()
	{
		
		/*componentOrValueType can be Parmeterized, nonParemeterized
		 * in any case array, list, set,or map, or not
		 * if arry,list,set add a new arrayPropert, set in this.property
		 * if map add a new mapProperty set in this.property
		 * edlse set is the property a refpropety and addIfPara...
		 */
		Type nextComponentOrValueType=null;
		Property newProperty=null;
		if(componentOrValueType instanceof ParameterizedType)
		{
			
			ParameterizedType parameterizedType=(java.lang.reflect.ParameterizedType) componentOrValueType;
			Class clazz = (Class) parameterizedType.getRawType();
			if(clazz.isArray())
			{
				nextComponentOrValueType=clazz.getComponentType();
				ArrayProperty arrayProperty= new ArrayProperty();
				arrayProperty.setName("items");
				newProperty=arrayProperty;
			}
			else if(
					List.class.isAssignableFrom(clazz)||
					Set.class.isAssignableFrom(clazz))
			{
				nextComponentOrValueType=Object.class;
				Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
				if(actualTypeArguments.length==1)
				{
					nextComponentOrValueType=actualTypeArguments[0];
				}
				ArrayProperty arrayProperty= new ArrayProperty();
				arrayProperty.setName("items");
				newProperty=arrayProperty;
			}
			else if(
					Map.class.isAssignableFrom(clazz))
			{
				nextComponentOrValueType=Object.class;
				Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
				if(actualTypeArguments.length==2)
				{
					nextComponentOrValueType=actualTypeArguments[1];
				}
			}
			else
			{
				String ref = parameterizedType.toString();
				ref=ref.replace('<', ParameterizedComponentKeySymbols.LEFTCHAR);
				ref=ref.replace('>', ParameterizedComponentKeySymbols.RIGHTCHAR);
				RefProperty refProperty= new RefProperty();
				refProperty.set$ref("#/definitions/"+ref);
				if(chainControl.getDefinitions().get(ref)==null)
				{
					chainControl.getNewModelCreator().addIfParemeterizedType(parameterizedType, false);
				}
				
			}
		}
		else if(componentOrValueType instanceof WildcardType)
		{
			WildcardType wildcardType=(java.lang.reflect.WildcardType) componentOrValueType;
			nextComponentOrValueType=Object.class;
			Type[] upperBounds = wildcardType.getUpperBounds();
			if(upperBounds.length==1)
			{
				nextComponentOrValueType=upperBounds[0];
			}
			throw new RuntimeException("improve wildcard handling");
		}
		else if(componentOrValueType instanceof Class)
		{
			Class clazz=(Class) componentOrValueType;
			if(clazz.isArray())
			{
				nextComponentOrValueType=clazz.getComponentType();
				ArrayProperty arrayProperty= new ArrayProperty();
				arrayProperty.setName("items");
				newProperty=arrayProperty;
			}
			else if(
					List.class.isAssignableFrom(clazz)||
					Set.class.isAssignableFrom(clazz))
			{
				nextComponentOrValueType=Object.class;
				ArrayProperty arrayProperty= new ArrayProperty();
				arrayProperty.setName("items");
				newProperty=arrayProperty;
			}
			else if(
					Map.class.isAssignableFrom(clazz))
			{
				nextComponentOrValueType=Object.class;
				MapProperty mapProperty=new MapProperty();
				newProperty=mapProperty;
				
			}
			else
			{
				Property basicProperty = BasicMappingHolder.INSTANCE.buildBasicProperty(clazz);
				if(basicProperty!=null)
				{
					newProperty=basicProperty;
				}
				else
				{
					RefProperty refProperty= new RefProperty();
					refProperty.set$ref("#/definitions/"+clazz.getName());
					newProperty=refProperty;
					
					if(chainControl.getDefinitions().get("#/definitions/"+clazz.getName())==null)
					{
						chainControl.getNewModelCreator().addIfParemeterizedType(clazz, false);
					}
				}
				
				
			}
		}
		else
		{
			throw new RuntimeException("unexpected else "+ componentOrValueType.getClass().getName());
		}
		
		if(property instanceof ArrayProperty)
		{
			ArrayProperty arrayProperty=(ArrayProperty) property;
			arrayProperty.setItems(newProperty);
		}
		else if(property instanceof MapProperty)
		{
			MapProperty mapProperty=(MapProperty) property;
			mapProperty.setAdditionalProperties(newProperty);
		}
		else
		{
			throw new RuntimeException("unexpected else");
		}
		if(nextComponentOrValueType!=null)
		{
			new Chain(this.chainControl, newProperty, nextComponentOrValueType).chain();
		}
	}
	

}

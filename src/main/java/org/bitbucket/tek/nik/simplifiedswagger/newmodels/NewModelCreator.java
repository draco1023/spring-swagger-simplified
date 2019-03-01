package org.bitbucket.tek.nik.simplifiedswagger.newmodels;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bitbucket.tek.nik.simplifiedswagger.BasicMappingHolder;
import org.bitbucket.tek.nik.simplifiedswagger.modelbuilder.ParameterizedComponentKeySymbols;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;


public class NewModelCreator {
	
	
	public void build()
	{
		//showNewTypes();
		for (int lastCounter = counter, i=0; lastCounter < counter||i==0;i++ ) {
			lastCounter=counter;
			//System.out.println("loop "+i);
			this.addGenericModels(definitions);
			this.addWildCardModels(definitions);
			this.addNonGenericModels(definitions);
		}
	}
	
	private final Map<String, Model> definitions;
	private final ParameterizedPropertyHandler parameterizedPropertyHandler;
	private final NonParameterizedPropertyHandler nonParameterizedPropertyHandler ;
	
	public NewModelCreator(Map<String, Model> definitions) {
		super();
		this.definitions = definitions;
		
		this.parameterizedPropertyHandler = new ParameterizedPropertyHandler(definitions, this);
		this.nonParameterizedPropertyHandler = new NonParameterizedPropertyHandler(definitions,  this);
	}

	private int counter=0;
	
	private Set<ParameterizedType> parameterizedTypes= new HashSet<>();
	private Map<String, ParameterizedType> parameterizedTypesLookup= new HashMap<>();

	private Set<WildcardType> wildcardTypes= new HashSet<>();
	private Map<String, WildcardType> wildcardTypesLookup= new HashMap<>();

	private Set<Class> nonParameterizedTypes= new HashSet<>();
	
	


	

	private void showNewTypes() {
		System.out.println("parameterizedTypes="+parameterizedTypes);
		System.out.println("wildcardTypes="+wildcardTypes);
		System.out.println("nonParameterizedTypes="+nonParameterizedTypes);
		
	}
	
	public void addIfParemeterizedType(Type genericType, boolean first) {
		if(genericType instanceof ParameterizedType)
		{
			/**
			 * Hope this next contains check is enough to prevent infinite recursions in drill
			 */
			ParameterizedType parameterizedType=(ParameterizedType)genericType;
			Type rawType=parameterizedType.getRawType();
			if(rawType instanceof Class)
			{
				prevent((Class) rawType, first);
			}
			
			
			if(parameterizedTypes.add((ParameterizedType)genericType))
			{
				counter++;
			}
			
			
		}
		else if(genericType instanceof WildcardType)
		{
			WildcardType wildcardType=(WildcardType) genericType;
			Type type = wildcardType.getUpperBounds()[0];
			if(type instanceof Class)
			{
				prevent((Class) type, first);
			}
			
			if(wildcardTypes.add(wildcardType))
			{
				counter++;
			}
				
			
		}
		else if(genericType instanceof TypeVariable)
		{
			throw new RuntimeException("Resolve the typevariables. Dont send typeVariables");
			
		}
		else if(genericType instanceof Class)
		{
			if(!first)
			{
				/*
				 * dont need to do on first as firsttime the models get created as a result of
				 * the buildOperation
				 */
				
				/*
				 * We should not be getting any Array, List, or Set here.
				 * */
				Class clazz=(Class) genericType;
				
				prevent(clazz, first);
				
				
					
				if(nonParameterizedTypes.add(clazz))
				{
					counter++;
				}
					
				
				
			}
			
			
		}

		else
		{
			throw new RuntimeException("got "+genericType.getClass().getName());
		}
		
	}
	
	
	private void prevent(Class clazz, boolean first) {
		if(!first)
		{
			if(clazz.isArray()||List.class.isAssignableFrom(clazz)||Set.class.isAssignableFrom(clazz))
			{
				throw new RuntimeException("Not expecting arrays/lists/sets here. Use ArrayProperty when building model fields");
			}
		}
		
		if(clazz.isEnum())
		{
			throw new RuntimeException("Not expecting enum types here. Use proper property/type when building model fields .offending type is "+clazz.getName());
		}
		
		
		if(BasicMappingHolder.INSTANCE.getMappedByType(clazz.getName())!=null)
		{
			throw new RuntimeException("Not expecting basic types here. Use proper property/type when building model fields .offending type is "+clazz.getName());
		}
		
		if(clazz.getName().contains("$"))
		{
			// we can pursue enclosing class
			//clazz.getEnclosingClass()
			//for now do nothing
		}
	}

private void addWildCardModels(Map<String, Model> definitions) {
	HashSet<WildcardType> hashSet = new HashSet<WildcardType>();
	hashSet.addAll(wildcardTypes);
	for (WildcardType wildcardType : hashSet) 
	{
		String key = wildcardType.toString();
		if(wildcardTypesLookup.containsKey(key))
		{
			continue;
		}
		if(key.startsWith(java.lang.Class.class.getName())||key.startsWith(java.lang.ref.SoftReference.class.getName())||key.startsWith(java.lang.reflect.Constructor.class.getName())||key.startsWith(java.lang.reflect.Type.class.getName()))
		{
			continue;
		}
		key=key.replace('<', ParameterizedComponentKeySymbols.LEFTCHAR);
		key=key.replace('>', ParameterizedComponentKeySymbols.RIGHTCHAR);
		wildcardTypesLookup.put(key, wildcardType);
		String originalKey=key;
		ModelImpl model= new ModelImpl();
		model.setTitle(originalKey);
		
		Type type = wildcardType.getUpperBounds()[0];
		model.setType(type==Object.class?"object":type.getClass().getName());
		addIfParemeterizedType(type, false);
		
		
		definitions.put(key, model);
		
	}
}

private void addNonGenericModels(Map<String, Model> definitions) {
	HashSet<Class> hashSet = new HashSet<Class>();
	hashSet.addAll(nonParameterizedTypes);
	
	for (Class clazz : hashSet) 
	{
		String key = clazz.getName();
		
		if(clazz.isPrimitive())
		{
			continue;
		}
		if(definitions.containsKey(key))
		{
			continue;
		}
		
		/**
		 * realize this in a betetr way
		 */
		if(key.startsWith(java.lang.Class.class.getName())||key.startsWith(java.lang.ref.SoftReference.class.getName())||
				key.startsWith(java.lang.reflect.Constructor.class.getName())||key.startsWith(java.lang.reflect.Type.class.getName())
				||key.startsWith(java.util.HashSet.class.getName())||key.startsWith(java.util.LinkedHashMap.class.getName())
				||key.startsWith(java.util.HashMap.class.getName()))
		{
			continue;
		}
		ModelImpl model= new ModelImpl();
		model.setTitle(key);
	
		model.setType("object");
		HashMap<String, Property> modelProperties = new HashMap<String, Property>();
		
		Map<String, Type> propertiesMap= new HashMap<>();
		//must build all the getters
		Method[] declaredMethods = clazz.getMethods();
		for (Method declaredMethod : declaredMethods) {
			if(declaredMethod.getParameterTypes().length==0 && declaredMethod.getReturnType()!=void.class)
			{
				String declaredMethodName = declaredMethod.getName();
				if(declaredMethodName.startsWith("get") && declaredMethodName.length()>"get".length())
				{
					char[] charArray = declaredMethodName.substring("get".length()).toCharArray();
					charArray[0]=Character.toLowerCase(charArray[0]);
					String propName = String.valueOf(charArray);
					if(propName.equals("class")||propName.equals("type"))
					{
						continue;
					}
					Type genericReturnType = declaredMethod.getGenericReturnType();
					
					 if(genericReturnType instanceof Class)
					{
						propertiesMap.put(propName,genericReturnType);
					}
					 else if(genericReturnType instanceof ParameterizedType)
					 {
						 propertiesMap.put(propName,genericReturnType);
					 }
					
					else
					{
						throw new RuntimeException("handle "+genericReturnType.getClass().getName()+" method="+propName+", in "+key);
					}
					/*else
					{
						propertiesMap.put(String.valueOf(charArray),declaredMethod.getReturnType());
					}*/
				}
				else if(declaredMethodName.startsWith("is") && declaredMethodName.length()>"is".length())
				{
					char[] charArray = declaredMethodName.substring("is".length()).toCharArray();
					charArray[0]=Character.toLowerCase(charArray[0]);
					String propName = String.valueOf(charArray);
					if(propName.equals("class")||propName.equals("type"))
					{
						continue;
					}
					Type genericReturnType = declaredMethod.getGenericReturnType();
					if(genericReturnType instanceof Class)
					{
						propertiesMap.put(propName,genericReturnType);
					}
					else if(genericReturnType instanceof ParameterizedType)
					 {
						 propertiesMap.put(propName,genericReturnType);
					 }
					else
					{
						throw new RuntimeException("handle "+genericReturnType.getClass().getName()+" method="+propName+", in "+key);
					}
				}
			}
		}
		
		Field[] declaredFields = clazz.getDeclaredFields();
		for (Field field : declaredFields) {
			if(field.getName().equals("class")||field.getName().equals("type"))
			{
				continue;
			}
			if(propertiesMap.get(field.getName())==null)
			{
				Type genericType = field.getGenericType();
				if(genericType instanceof Class)
				{
					propertiesMap.put(field.getName(),field.getType());
				}
				else if(genericType instanceof ParameterizedType)
				 {
					 propertiesMap.put(field.getName(),genericType);
				 }
				else
				{
					throw new RuntimeException("handle "+genericType.getClass().getName()+" field="+field.getName()+", in "+key);
				}
			}
		}
		Set<String> keySet = propertiesMap.keySet();
		for (String key2 : keySet) {
			if(key2.equals("class")||key2.equals("type"))
			{
				continue;
			}
			Type type = propertiesMap.get(key2);
			if(type==null)
			{
				throw new RuntimeException("unexpected if for property "+key2+ " in generic model "+key+" was actual of null type");
			}
			if(type instanceof ParameterizedType)
			{
				handleParameterizedProperty(definitions, modelProperties, key2, (ParameterizedType)type, null);
			}
			else if(type instanceof WildcardType)
			{
				WildcardType wildcardType=(WildcardType) type;
				
				{
					Class propertyType=(Class) wildcardType.getUpperBounds()[0];
					Property basicProperty = BasicMappingHolder.INSTANCE.buildBasicProperty(propertyType);
					if(basicProperty!=null)
					{
						basicProperty.setName(key2);
						modelProperties.put(key2, basicProperty);
					}
					else
					{
						if(propertyType.isArray())
						{
							ArrayProperty arrayProperty= new ArrayProperty();
							RefProperty refProperty= new RefProperty();
							refProperty.set$ref("#/definitions/"+propertyType.getName());
							refProperty.setName("items");
							arrayProperty.setItems(refProperty);
							modelProperties.put(key2, refProperty);
							if(definitions.get(propertyType.getName())==null)
							{
								addIfParemeterizedType(propertyType, false);
							}
						}
						else
						{
							RefProperty refProperty= new RefProperty();
							refProperty.set$ref("#/definitions/"+propertyType.getName());
							refProperty.setName(key2);
							modelProperties.put(key2, refProperty);
							if(definitions.get(propertyType.getName())==null)
							{
								addIfParemeterizedType(propertyType, false);
							}
						}
					}

				}
			}
			//
			else if(type instanceof Class)
			{
				
				handleNonParameterizedProperty(definitions, modelProperties, key2, (Class)type);
				
			}
			else
			{
				throw new RuntimeException("unexpected else for property "+key2+ " in generic model "+key+" was actual of "+type.getClass().getName());
			}
			
			
			
		}
		model.setProperties(modelProperties);
		definitions.put(key, model);

			
	}		
		
						
	
}

private void handleNonParameterizedProperty(Map<String, Model> definitions, HashMap<String, Property> modelProperties,
		String key2, Class propertyType) {
	nonParameterizedPropertyHandler.handleNonParameterizedProperty(modelProperties, key2, propertyType);
	
	}

private void handleParameterizedProperty(Map<String, Model> definitions, HashMap<String, Property> modelProperties,
		String propertyName, ParameterizedType type, Map<String, Type> typeVariableToActualTypeMapFromParentClass ) {
	
	parameterizedPropertyHandler.handleParameterizedProperty(modelProperties, propertyName, type,
			typeVariableToActualTypeMapFromParentClass);

}
/*
 * unused code will remove later.
 * commenting out for now
private Set<String> blockedKeys= new HashSet<>();
private Set<Type> blockedRawTypes= new HashSet<>();

public void tempShowBlocked()
{
	System.err.println("blocked keys are:");
	for (String blockedKey : blockedKeys) {
		System.err.println(blockedKey);
	}
	System.err.println("blocked blockedRawTypes are:");
	for (Type blockedRawType : blockedRawTypes) {
		System.err.println(blockedRawType);
	}
}

private boolean notNeededCheck(Type rawType, String key) {

	boolean check1=key.startsWith(java.lang.Class.class.getName())||key.startsWith(java.lang.ref.SoftReference.class.getName())||
	key.startsWith(java.lang.reflect.Constructor.class.getName())||key.startsWith(java.lang.reflect.Type.class.getName())
	||key.startsWith(java.util.HashSet.class.getName())||key.startsWith(java.util.LinkedHashMap.class.getName())
	||key.startsWith(java.util.HashMap.class.getName());
	
	
	
	if(check1)
	{
		blockedKeys.add(key);
		blockedRawTypes.add(rawType);
		
	}
	return check1;
}
*/
private void addGenericModels(Map<String, Model> definitions) {
	HashSet<ParameterizedType> hashSet = new HashSet<ParameterizedType>();
	hashSet.addAll(parameterizedTypes);
	for (ParameterizedType parameterizedType : hashSet) 
	{
		Type rawType = parameterizedType.getRawType();
		String key = parameterizedType.toString();
		if(parameterizedTypesLookup.containsKey(key))
		{
			continue;
		}
		/*
		 * unused code will remove later.
		 * commenting out for now
		 * boolean check1 = notNeededCheck(rawType, key);
		if(check1)
		{
			continue;
		}*/
		key=key.replace('<', ParameterizedComponentKeySymbols.LEFTCHAR);
		key=key.replace('>', ParameterizedComponentKeySymbols.RIGHTCHAR);
		parameterizedTypesLookup.put(key, parameterizedType);
		String originalKey=key;
		ModelImpl model= new ModelImpl();
		model.setTitle(originalKey);
		model.setType("object");
		HashMap<String, Property> modelProperties = new HashMap<String, Property>();
		
		
		
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		for (Type type2 : actualTypeArguments) {
			//registerTheTypeIfNeeded(type2);
			
		}
		if(rawType instanceof Class)
		{
			
			Class clazz = (Class) rawType;
			
			TypeVariable[] typeParameters = clazz.getTypeParameters();
			
			Map<String, Type> typeVariableToActualTypeMap= new HashMap<>();
			for (int i = 0; i < typeParameters.length; i++) {
				TypeVariable typeVariable = typeParameters[i];
				typeVariableToActualTypeMap.put(typeVariable.getName(), actualTypeArguments[i]);
			}
			Map<String, Type> propertiesMap= new HashMap<>();
			//must build all the getters
			Method[] declaredMethods = clazz.getMethods();
			for (Method declaredMethod : declaredMethods) {
				if(declaredMethod.getParameterTypes().length==0 && declaredMethod.getReturnType()!=void.class)
				{
					String declaredMethodName = declaredMethod.getName();
					if(declaredMethodName.startsWith("get") && declaredMethodName.length()>"get".length())
					{
						char[] charArray = declaredMethodName.substring("get".length()).toCharArray();
						charArray[0]=Character.toLowerCase(charArray[0]);
						String propName = String.valueOf(charArray);
						if(propName.equals("class")||propName.equals("type"))
						{
							continue;
						}
						Type genericReturnType = declaredMethod.getGenericReturnType();
						
						if(genericReturnType instanceof TypeVariable)
						{
							Type type = typeVariableToActualTypeMap.get(((TypeVariable)genericReturnType).getName());
							propertiesMap.put(propName,type);
						}
						else if(genericReturnType instanceof ParameterizedType)
						{
							propertiesMap.put(propName,genericReturnType);
						}
						else if(genericReturnType instanceof Class)
						{
							propertiesMap.put(propName,genericReturnType);
						}
						
						else
						{
							throw new RuntimeException("handle "+genericReturnType.getClass().getName()+" method="+propName+", in "+key);
						}
						/*else
						{
							propertiesMap.put(String.valueOf(charArray),declaredMethod.getReturnType());
						}*/
					}
					else if(declaredMethodName.startsWith("is") && declaredMethodName.length()>"is".length())
					{
						char[] charArray = declaredMethodName.substring("is".length()).toCharArray();
						charArray[0]=Character.toLowerCase(charArray[0]);
						String propName = String.valueOf(charArray);
						if(propName.equals("class")||propName.equals("type"))
						{
							continue;
						}
						Type genericReturnType = declaredMethod.getGenericReturnType();
						if(genericReturnType instanceof TypeVariable)
						{
							Type type = typeVariableToActualTypeMap.get((TypeVariable)genericReturnType);
							propertiesMap.put(propName,type);
							
						}
						else if(genericReturnType instanceof ParameterizedType)
						{
							propertiesMap.put(propName,genericReturnType);
						}
						else if(genericReturnType instanceof Class)
						{
							propertiesMap.put(propName,genericReturnType);
						}
						else
						{
							throw new RuntimeException("handle "+genericReturnType.getClass().getName()+" method="+propName+", in "+key);
						}
					}
				}
			}
			
			Field[] declaredFields = clazz.getDeclaredFields();
			for (Field field : declaredFields) {
				if(field.getName().equals("class")||field.getName().equals("type"))
				{
					continue;
				}
				if(propertiesMap.get(field.getName())==null)
				{
					Type genericType = field.getGenericType();
					if(genericType instanceof TypeVariable)
					{
						Type type = typeVariableToActualTypeMap.get((TypeVariable)genericType);
						propertiesMap.put(field.getName(),type);
					}
					else if(genericType instanceof Class)
					{
						propertiesMap.put(field.getName(),field.getType());
					}
					else
					{
						throw new RuntimeException("handle "+genericType.getClass().getName()+" field="+field.getName()+", in "+key);
					}
				}
			}
			Set<String> keySet = propertiesMap.keySet();
			for (String key2 : keySet) {
				if(key2.equals("class")||key2.equals("type"))
				{
					continue;
				}
				Type type = propertiesMap.get(key2);
				if(type==null)
				{
					throw new RuntimeException("unexpected if for property "+key2+ " in generic model "+key+" was actual of null type");
				}
				if(type instanceof ParameterizedType)
				{
					ParameterizedType parameterizedType1=(ParameterizedType) type;
					
					handleParameterizedProperty(definitions, modelProperties, key2, parameterizedType1, typeVariableToActualTypeMap);
				}
				else if(type instanceof WildcardType)
				{
					WildcardType wildcardType=(WildcardType) type;
					
					{
						Class propertyType=(Class) wildcardType.getUpperBounds()[0];
						
						Property basicProperty = BasicMappingHolder.INSTANCE.buildBasicProperty(propertyType);
						if(basicProperty!=null)
						{
							basicProperty.setName(key2);
							modelProperties.put(key2, basicProperty);
						}
						else
						{
							RefProperty refProperty= new RefProperty();
							refProperty.set$ref("#/definitions/"+propertyType.getName());
							refProperty.setName(key2);
							modelProperties.put(key2, refProperty);
							if(definitions.get(propertyType.getName())==null)
							{
								
								addIfParemeterizedType(propertyType, false);
							}
						}
						
					}
				}
				//
				else if(type instanceof Class)
				{
					
					handleNonParameterizedProperty(definitions, modelProperties, key2, (Class)type);
					
				}
				else
				{
					throw new RuntimeException("unexpected else for property "+key2+ " in generic model "+key+" was actual of "+type.getClass().getName());
				}
				
				
				
			}
			
		}
		else
		{
			throw new RuntimeException("key="+key+" did not map to a Class. check");
		}
		
		model.setProperties(modelProperties);
		definitions.put(originalKey, model);
		
	}
}



public ParameterizedType getParameterizedModelType(String key)
{
	return parameterizedTypesLookup.get(key);
}



}

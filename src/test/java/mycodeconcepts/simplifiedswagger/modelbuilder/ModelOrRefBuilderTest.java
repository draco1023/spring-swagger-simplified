package mycodeconcepts.simplifiedswagger.modelbuilder;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.bitbucket.teknik.simplifiedswagger.modelbuilder.ModelOrRefBuilder;
import org.bitbucket.teknik.simplifiedswagger.modelbuilder.OuterContainer;
import org.bitbucket.teknik.simplifiedswagger.modelbuilder.ParameterContainer;
import org.junit.BeforeClass;
import org.junit.Test;

import io.swagger.models.Model;

public class ModelOrRefBuilderTest {
	private static Method[] declaredMethods;

	@BeforeClass
	public static void setupOnce() throws IOException
	{
		Check check= new Check();
		
		declaredMethods = check.getClass().getDeclaredMethods();
	}
	
	private Method findFirstMethodOfSpecifiedName(String name)
	{
		Method method=null;
		for (Method declaredMethod : declaredMethods) {
			if(declaredMethod.getName().equals(name))
			{
				method=declaredMethod;
				break;
			}
		}
		return method;
	}

	@Test
	public void test1() {
		System.out.println("sampleMethod1");
		Method method=findFirstMethodOfSpecifiedName("sampleMethod1");
		Parameter parameter = method.getParameters()[0];
		Type genericParameterType = method.getGenericParameterTypes()[0];
		String parameterName = parameter.getName();
		ModelOrRefBuilder bodyParameterBuilder= new ModelOrRefBuilder(genericParameterType, new ParameterContainer());
		/*OuterContainer built =null;
		for (boolean isbuilt = bodyParameterBuilder.isBuilt(); !isbuilt; isbuilt = bodyParameterBuilder.isBuilt()) 
		{
			built = bodyParameterBuilder.buildIteration();
			System.out.println(bodyParameterBuilder.getCurrentContainerStack());
		}*/
		OuterContainer built=bodyParameterBuilder.build();
		
		Model schema = built.getSchema();
		List<Object> currentContainerStack = bodyParameterBuilder.getCurrentContainerStack();
		System.out.println(currentContainerStack);
	}
	
	@Test
	public void test2() {
		System.out.println("sampleMethod2");
		Method method=findFirstMethodOfSpecifiedName("sampleMethod2");
		Parameter parameter = method.getParameters()[0];
		Type genericParameterType = method.getGenericParameterTypes()[0];
		String parameterName = parameter.getName();
		ModelOrRefBuilder bodyParameterBuilder= new ModelOrRefBuilder(genericParameterType, new ParameterContainer());
		OuterContainer built =null;
		for (boolean isbuilt = bodyParameterBuilder.isBuilt(); !isbuilt; isbuilt = bodyParameterBuilder.isBuilt()) 
		{
			built = bodyParameterBuilder.buildIteration();
			System.out.println(bodyParameterBuilder.getCurrentContainerStack());
		}
		
		Model schema = built.getSchema();
		List<Object> currentContainerStack = bodyParameterBuilder.getCurrentContainerStack();
		System.out.println(currentContainerStack);
	}


}

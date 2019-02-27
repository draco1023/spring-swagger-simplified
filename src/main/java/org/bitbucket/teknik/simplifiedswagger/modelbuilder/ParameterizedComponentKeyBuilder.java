package org.bitbucket.teknik.simplifiedswagger.modelbuilder;

import java.lang.reflect.ParameterizedType;

public class ParameterizedComponentKeyBuilder {
	
	public static String buildKeyForParameterizedComponentType(ParameterizedType parameterizedComponentType) {
		
		String ref = parameterizedComponentType.toString();
		ref=ref.replace('<', '«');
		ref=ref.replace('>', '»');
		return ref;
	}

}

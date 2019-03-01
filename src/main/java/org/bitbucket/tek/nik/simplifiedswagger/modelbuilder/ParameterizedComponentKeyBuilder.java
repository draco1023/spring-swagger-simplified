package org.bitbucket.tek.nik.simplifiedswagger.modelbuilder;

import java.lang.reflect.ParameterizedType;

public class ParameterizedComponentKeyBuilder {
	
	public static String buildKeyForParameterizedComponentType(ParameterizedType parameterizedComponentType) {
		
		String ref = parameterizedComponentType.toString();
		ref=ref.replace('<', ParameterizedComponentKeySymbols.LEFTCHAR);
		ref=ref.replace('>', ParameterizedComponentKeySymbols.RIGHTCHAR);
		return ref;
	}

}

package org.bitbucket.tek.nik.simplifiedswagger.optracker;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import io.swagger.annotations.ApiParam;
import io.swagger.models.Operation;

/*
 * the goal of this class as of now is mainly to get at the ApiParam later
 * If needed can add more fields
 */

public class OperationTrackerData {
	
	private Method method;
	private Operation operation;
	private boolean hiddenOperation;
	
	public OperationTrackerData(Method method, Operation operation) {
		super();
		this.method = method;
		this.operation = operation;
		
		
	}
	
	public ApiParam[] getApiParams() {
		final Parameter[] parameters = method.getParameters();
		
		ApiParam[] apiParams= new ApiParam[parameters.length];
		for (int i = 0; i < parameters.length; i++) 
		{
			Parameter parameter=parameters[i];
			final ApiParam apiParam = parameter.getAnnotation(ApiParam.class);
			apiParams[i]=apiParam;
		}
		return apiParams;
		
	}
	
	
	public boolean isHiddenOperation() {
		return hiddenOperation;
	}

	public void setHiddenOperation(boolean hiddenOperation) {
		this.hiddenOperation = hiddenOperation;
	}

	public Method getMethod() {
		return method;
	}

	public Operation getOperation() {
		return operation;
	}

	

	
	
	

}

package org.bitbucket.tek.nik.simplifiedswagger.optracker;

import java.lang.reflect.Method;

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

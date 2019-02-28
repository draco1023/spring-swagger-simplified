package org.bitbucket.tek.nik.simplifiedswagger.modelbuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BuilderCurrentState {

	
	private Object currentContainer;
	private List<Object> currentContainerStack;
	private Type currentGenericType;
	public BuilderCurrentState(Object currentContainer, Type currentGenericType) {
		super();
		this.currentContainer = currentContainer;
		this.currentContainerStack = new ArrayList<Object>();
		this.currentGenericType = currentGenericType;
	}
	public Object getCurrentContainer() {
		return currentContainer;
	}
	public List<Object> getCurrentContainerStack() {
		return currentContainerStack;
	}
	public Type getCurrentGenericType() {
		return currentGenericType;
	}
	public void setCurrentContainer(Object currentContainer) {
		this.currentContainer = currentContainer;
	}
	public void setCurrentGenericType(Type currentGenericType) {
		this.currentGenericType = currentGenericType;
	}
	
	

}

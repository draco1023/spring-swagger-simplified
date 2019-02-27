package org.bitbucket.teknik.simplifiedswagger.newmodels;

import java.util.Map;

import io.swagger.models.Model;

public class ChainControl {
	
	private final Map<String, Model> definitions;

	private final NewModelCreator newModelCreator;
	public ChainControl(Map<String, Model> definitions, 
			NewModelCreator newModelCreator) {
		super();
		this.definitions = definitions;
		
		this.newModelCreator = newModelCreator;
	}
	public Map<String, Model> getDefinitions() {
		return definitions;
	}
	
	public NewModelCreator getNewModelCreator() {
		return newModelCreator;
	}
	

}

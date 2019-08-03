package org.bitbucket.tek.nik.simplifiedswagger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bitbucket.tek.nik.simplifiedswagger.newmodels.NewModelCreator;

import io.swagger.models.Model;

public class SimplifiedSwaggerData {
	
	
	
	public SimplifiedSwaggerData(Map<String, Model> definitions) {
		this.newModelCreator= new NewModelCreator(definitions);
	}
	private final NewModelCreator newModelCreator;
	private final Set<Class> unMappedAnnotations = new HashSet<>();
	private final Set<String> definitionsThatCanBeRemoved= new HashSet<>();
	
	public Set<Class> getUnMappedAnnotations() {
		return unMappedAnnotations;
	}
	public Set<String> getDefinitionsThatCanBeRemoved() {
		return definitionsThatCanBeRemoved;
	}
	public NewModelCreator getNewModelCreator() {
		return newModelCreator;
	}

}

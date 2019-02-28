package org.bitbucket.tek.nik.simplifiedswagger.modelbuilder;

import io.swagger.models.Model;

public abstract class OuterContainer {
	boolean loaded=false;
	private String ref;
	private Model schema;
	private Object actual;
	private boolean builtSchema;
	public boolean isBuiltSchema() {
		return builtSchema;
	}

	public boolean isBuiltRef() {
		return builtRef;
	}

	private boolean builtRef;
	public Object getActual() {
		return actual;
	}

	public String get$ref() {
		return ref;
	}

	public Model getSchema() {
		return schema;
	}

	public void set$ref(String ref)
	{
		prventDoubleLoading();
		this.loaded=true;
		this.ref=ref;
		this.actual=ref;
		this.builtRef=true;
		
		
	}

	private void prventDoubleLoading() {
		if(this.loaded)
		{
			throw new RuntimeException("container already loaded");
		}
	}
	
	public void setSchema(Model schema)
	{
		prventDoubleLoading();
		this.loaded=true;
		this.schema=schema;
		this.actual=schema;
		this.builtSchema=true;
	}
	
	
	

}

package org.bitbucket.teknik.simplifiedswagger.modelbuilder;

import io.swagger.models.Model;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.RefParameter;

public class ParameterContainer extends OuterContainer{
	private BodyParameter bodyParameter;
	public BodyParameter getBodyParameter() {
		return bodyParameter;
	}

	private RefParameter refParameter;
	@Override
	public void set$ref(String ref) {
		
		super.set$ref(ref);
		refParameter= new RefParameter(ref);
		
	}

	@Override
	public void setSchema(Model schema) {
		
		super.setSchema(schema);
		bodyParameter=new BodyParameter();
		bodyParameter.setSchema(schema);
	}

}

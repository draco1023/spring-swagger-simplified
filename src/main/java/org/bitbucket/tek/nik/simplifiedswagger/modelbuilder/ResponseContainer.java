package org.bitbucket.tek.nik.simplifiedswagger.modelbuilder;

import io.swagger.models.Model;
import io.swagger.models.RefResponse;
import io.swagger.models.Response;

public class ResponseContainer  extends OuterContainer{
	private Response response;
	public Response getResponse() {
		return response;
	}

	@Override
	public void set$ref(String ref) {
		super.set$ref(ref);
		response= new RefResponse(ref);
	}

	@Override
	public void setSchema(Model schema) {
		super.setSchema(schema);
		response= new Response();
		response.setResponseSchema(schema);
	}

}

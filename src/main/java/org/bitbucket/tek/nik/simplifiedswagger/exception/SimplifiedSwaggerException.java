package org.bitbucket.tek.nik.simplifiedswagger.exception;

public class SimplifiedSwaggerException extends RuntimeException {

	public SimplifiedSwaggerException() {
		
	}

	public SimplifiedSwaggerException(String message) {
		super(message);
		
	}

	public SimplifiedSwaggerException(Throwable cause) {
		super(cause);
	
	}

	public SimplifiedSwaggerException(String message, Throwable cause) {
		super(message, cause);
	
	}

	public SimplifiedSwaggerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

}

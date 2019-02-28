package org.bitbucket.tek.nik.simplifiedswagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
@Configuration
public class ServiceModelConfiguration {
	@Bean()
	@Primary
	SimplifiedSwaggerServiceModelToSwagger2MapperImpl myServiceModelToSwagger2MapperImpl() {
		return new SimplifiedSwaggerServiceModelToSwagger2MapperImpl();
	}
	
	
}

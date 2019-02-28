package org.bitbucket.tek.nik.simplifiedswagger;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.TypeNameProviderPlugin;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimplifiedSwaggerModelTypeNameProvider implements TypeNameProviderPlugin {
	@Override
	public boolean supports(DocumentationType delimiter) {
		return true;
	}

	@Override
	public String nameFor(Class<?> type) {

		return type.getName();
	}
}

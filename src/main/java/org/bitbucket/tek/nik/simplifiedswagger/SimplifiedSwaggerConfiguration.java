package org.bitbucket.tek.nik.simplifiedswagger;

import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.ApiOperationSwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.CreditCardNumberSwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.DateTimeFormatSwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.EmailSwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.ISwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.JsonFormatSwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.MaxSwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.MinSwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.NotBlankSwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.NotNullSwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.PathVariableSwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.PatternSwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.RequestBodySwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.RequestHeaderSwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.RequestParamSwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.SizeSwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.ValidSwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.ValidatedSwaggerDecorator;
import org.bitbucket.tek.nik.simplifiedswagger.swaggerdecorators.XmlRootElementSwaggerDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimplifiedSwaggerConfiguration {
	@Bean("javax.validation.constraints.Size"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator sizePropertyDecorator() {
		return new SizeSwaggerDecorator();
	}

	@Bean("javax.validation.constraints.Max"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator maxPropertyDecorator() {
		return new MaxSwaggerDecorator();
	}

	@Bean("javax.validation.constraints.Min"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator minPropertyDecorator() {
		return new MinSwaggerDecorator();
	}

	@Bean("javax.validation.constraints.NotBlank"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator notBlankPropertyDecorator() {
		return new NotBlankSwaggerDecorator();
	}

	@Bean("javax.validation.constraints.Pattern"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator patternPropertyDecorator() {
		return new PatternSwaggerDecorator();
	}

	@Bean("javax.validation.constraints.NotNull"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator notNullPropertyDecorator() {
		return new NotNullSwaggerDecorator();
	}

	@Bean("javax.validation.constraints.Email"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator emailPropertyDecorator() {
		return new EmailSwaggerDecorator();
	}

	@Bean("org.hibernate.validator.constraints.CreditCardNumber"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator CreditCardNumberPropertyDecorator() {
		return new CreditCardNumberSwaggerDecorator();
	}

	@Bean("org.springframework.web.bind.annotation.PathVariable"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator pathVariableSwaggerDecorator() {
		return new PathVariableSwaggerDecorator();
	}
	
	@Bean("org.springframework.web.bind.annotation.RequestBody"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator requestBodySwaggerDecorator() {
		return new RequestBodySwaggerDecorator();
	}
	
	@Bean("org.springframework.web.bind.annotation.RequestParam"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator requestParamSwaggerDecorator() {
		return new RequestParamSwaggerDecorator();
	}
	
	@Bean("org.springframework.web.bind.annotation.RequestHeader"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator requestHeaderSwaggerDecorator() {
		return new RequestHeaderSwaggerDecorator();
	}
	
	@Bean("org.springframework.validation.annotation.Validated"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator validatedPropertyDecorator() {
		return new ValidatedSwaggerDecorator();
	}
	
	@Bean("org.springframework.format.annotation.DateTimeFormat"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator dateTimeFormatSwaggerDecorator() {
		return new DateTimeFormatSwaggerDecorator();
	}
	
	@Bean("com.fasterxml.jackson.annotation.JsonFormat"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator jsonFormatSwaggerDecorator() {
		return new JsonFormatSwaggerDecorator();
	}
	
	@Bean("javax.validation.Valid"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator validPropertyDecorator() {
		return new ValidSwaggerDecorator();
	}
	
	@Bean("javax.xml.bind.annotation.XmlRootElement"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator xmlRootElementSwaggerDecorator() {
		return new XmlRootElementSwaggerDecorator();
	}
	
	@Bean("io.swagger.annotations.ApiOperation"+SwaggerDecoratorConstants.DECORATOR_SUFFIX)
	ISwaggerDecorator apiOperationSwaggerDecorator() {
		return new ApiOperationSwaggerDecorator();
	}

	
}

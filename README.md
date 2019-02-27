# README #

This README documents whatever steps are necessary to use swagger more effectively.
Following the steps here should help demonstrate that goal.

### What is this repository for? ###

* In references section in this readme I refer to two excellent tutorial approaches for using swagger with spring boot. For convenience I refer to them as approach A and approach B.  

| Requirement/Issue        | Approach A        | Approach B  |
| ------------- |:-------------:| -----:|
| Centralised approach      | Yes | No |
| Demonstrates validation      | No      |   Yes |
| javax.validation.constraints will cause automatic documentation | NA      |    No see note on minLength in references |

* While swagger yaml/json supports the notions of minLength, maxLength there is something wanting in swagger integration with spring boot rest services when using @Valid.  
* The developer must not only fill his code with javax.validation.constraints but must also offset with additional swagger annotations and hope that the generated documentation is adequately illustrative about the constraints. Any mismatch between javax.validation.constraints and swagger annotations will imply miscommunication.   Considering that most controllers would invoke a service it will make you wonder whether you are writing more code or annotations. :)
* This here is an effort to avoid that duplication of concerns in validation implementation and its documentation communication.
* Had two choices- Make a complete implementation from scratch i.e generate a swagger yaml specification using reflection and spring metadata vs use existing implementation and add value to it. Initially taken this second approach for quicker completion. Didnt like the results. Then took a middle path for now. Use the original swagger as much as possible. But refresh the actual operations and definitions using spring metadatada and reflection.

### what does this really look like
##### Enriched model definitions  
![Enriched swagger model](images/new1.png "Enriched swagger model")
##### Regular model definitions
![usual swagger model](images/regular1.png "Regular swagger model")

##### Enriched parameter definitions
![Enriched parameters](images/params1.png)

Quick related note: While the swagger specifications resulting from this enriching library have all the information like the minLength etc for the parameters, only in case of parameters (unlike with the model definitions) the swagger ui needs a little help in rendering the same information. That is taken care of additionally by this library. 

##### General Note 
* The library only tries to ensure the annotations get used properly in the generated swagger specs. The actual validation is executed by the underlying framework that processes the annotations at runtime. That said there are adequate illustrative examples provided.

### How do I get set up? ###

* clone the repository https://tek-nik@bitbucket.org/tek-nik/spring-swagger-simplified.git.
* For now Run mvn clean install on the project in master branch.
* Will be deploying same in maven central and previous step can be avoided after that.
* clone the repository git clone https://tek-nik@bitbucket.org/tek-nik/simplified-swagger-examples.git
* Run mvn clean package  in master branch.
* run java -jar simplified-swagger-demo\target\simplified-swagger-demo-0.1.0.jar
* check using http://localhost:8080/api/swagger-ui.html
* run java -jar regular-swagger-demo\target\regular-swagger-demo-0.1.0.jar
* check using http://localhost:8081/api/swagger-ui.html
* Compare the two and hope you will find the simplified-swagger-demo useful
* Note: spring-swagger-simplified Organizes the beans using the spring bean names in the sawgger ui.


### References ###

* [Approach A- Swagger tutorial](https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api)
* [Approach B- Swagger tutorial](https://dzone.com/articles/spring-boot-swagger-ui)
* [Min Length](https://stackoverflow.com/questions/33753340/is-there-a-way-to-indicate-that-a-string-model-property-has-a-maximum-length-in/51935210#51935210)
* [Data Types](https://swagger.io/docs/specification/data-models/data-types/)




### TODO Next ###


* Maybe provide support for spring rest data jpa.
* Provide swagger 3 spring fox implementation



### Housekeeping Notes ###

* For Examples branch although there is a parent pom the child modules are not going to inherit much from it. Aim is to ensure each child module is independent. The parent pom project will be used only as a convenience to build all the projects together.
* spring-swagger-simplified has some dependencies in provided scope. This is just to ensure it compiles. Actual dependencies will have to be provided by the project that uses it -e.g simplified-swagger-demo in the examples branch. 
* In our ISwaggerDecorator implementations we could do checks for whether an io.swagger.models.properties.Property instance is actually an instance of StringProperty etc and use the attributes therein. But for now deferring this and using vendorExtensions approach. We should try to ensure that the result is same even when using vendorExtensions. 
* Similarly in our ISwaggerDecorator implementations we could do checks for whether an io.swagger.models.parameters.Parameter
 instance is actually an instance of PathParameter etc and use the attributes therein. But for now deferring this and using vendorExtensions approach. We should try to ensure that the result is same even when using vendorExtensions. 


 
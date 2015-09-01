# Restify
--------------

The purpose of [Restify](https://github.com/42BV/restify) is to dynamically generate REST endpoints for entity CRUD. In contrast to Spring DATA REST, Restify follows the Controller-Service-Repository  architectural pattern. Restify is build on convention over configuration, resulting in very minimal amounts of code with optimal functionality. But we are also very flexible, enabling simple overwrites in each architural layer.

## Features ##

* REST endpoints for entities, for one line of code
* Dynamic CRUD service- and repository implementations
* Overwrite posibilities on each layer: Controller, Service, Repository

## Quick Start ##

Clone the restify project and install with Maven.

Add the dependency to your own project:

```xml
<dependency>
  <groupId>io.restify</groupId>
  <artifactId>restify</artifactId>
  <version>1.1-SNAPSHOT</version>
</dependency>
```

Other dependencies:

* Spring MVC (v4+)
* Jackson (v2+)
* Java 1.6+

Create the handler resolver in your web configuration:

```java
@EnableWebMvc
public class WebMvcConfig extends WebMvcConfigurerAdapter {

 @Bean
 public CrudHandlerMappingFactoryBean crudHandlerMapping() {
  CrudHandlerMappingFactoryBean crudHandlerMapping = new CrudHandlerMappingFactoryBean();
  crudHandlerMapping.setBasePackageClass(WebMvcConfig.class);
  return crudHandlerMapping;
 }

}
```

Annotate your entities with one simple annotation:

```java
@Entity
@EnableRest
public class User {

 @Id
 private Long id;
 private String name;

}
```

That's it! Restify will now automatically generate a repository, service and controller. Where the controller will handle the following requests:

* GET    /user
* GET    /user/{id}
* POST   /user
* PUT    /user/{id}
* DELETE /user/{id}

## Customize body ##

## Customize functionality ##

### Repository ###
### Service ###
### Controller ###

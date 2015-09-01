# Restify

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

* Spring MVC (4.0+)
* Spring Data JPA (1.8+)
* Jackson (2.0+)
* Java (1.6+)

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

Sometimes you want to return the entity in a different format. For example, return a user without it's password for security reasons. Also, in some cases the create- or update request varies from the entity. Restify allows you to specify certain custom types:

```java
@Entity
@EnableRest(
 resultType = UserModel.class,
 createType = CreateUserModel.class,
 updateType = UpdateUserModel.class
)
public class User {

 @Id
 private Long id;
 private String name;

}
```

Requests will now be unmarshalled to our custom type. Restify then maps the custom type back on our entity and performs the action.

When using custom types, you will need the following dependency:

```xml
<dependency>
  <groupId>io.beanmapper</groupId>
  <artifactId>beanmapper</artifactId>
  <version>0.2.14</version>
</dependency>
```

And register a custom converter in your web configuration:

```java
@Override
public void addFormatters(FormatterRegistry registry) {
 registry.addConverter(new BeanMapperConverterAdapter(new BeanMapper()));
}
```

## Customize functionality ##

Logic can be overwritten on each architecural layer. This is particulary handy when domain specific logic is needed.

### Repository ###

Restify relies heavily on Spring Data JPA. Whenever a Spring Data repository is created, that repository bean will automatically be used:

```java
public interface UserRepository extends CrudRepository<User, Long> {
}
```

### Service ###

It is also possible to create a custom service implementation by implementing the CrudService interface. Or you could simply overwrite the AbstractCrudService class:

```java
@Service
@Transactional
@Scope(proxyMode = TARGET_CLASS)
public interface UserService extends AbstractCrudService<User, Long> {
 
 @Autowired
 public UserService(UserRepository userRepository) {
  super(User.class, userRepository);
 }
 
}
```

### Controller ###

To create a custom REST endpoint, just define the regular Spring MVC request mapping:

```java
@RestController
@RequestMapping("/user")
public interface UserController {

 @RequestMapping(method = POST)
 public UserModel create(CreateUserModel model) {
 }
 
}
```

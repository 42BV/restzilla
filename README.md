# Restzilla

The purpose of [Restzilla](https://github.com/42BV/restzilla) is to dynamically generate REST endpoints for entity CRUD. In contrast to Spring DATA REST, Restzilla follows the Controller-Service-Repository architectural pattern. Restzilla is build on convention over configuration, resulting in very minimal amounts of code with optimal functionality. It is also possible to overwrite functionality in each architectural layer.

## Features ##

* Generates (CRUD) REST endpoints, requiring only a JPA entity class
* Overwrite posibilities on each layer: Controller, Service, Repository
* Custom finder queries
* Swagger support

## Quick Start ##

Clone the Restzilla project and install locally with Maven.

Add the dependency to your own project:

```xml
<dependency>
  <groupId>nl.42.restzilla</groupId>
  <artifactId>restzilla</artifactId>
  <version>2.0.0-SNAPSHOT</version>
</dependency>
```

We are currently working on publishing the project to Maven Central.

Other dependencies:

* Spring MVC (5.1+)
* Spring Data JPA (2.1+)
* Jackson (2.9+)
* Java (1.8+)

Annotate your Spring Configuration with @EnableRest:

```java
@EnableWebMvc
@EnableRest(basePackageClass = WebMvcConfig.class)
public class WebMvcConfig extends WebMvcConfigurerAdapter {
}
```

Then annotate the entities that should have REST endpoints with @RestResource:

```java
@Entity
@RestResource
public class User {

 @Id
 private Long id;
 private String name;

}
```

That's it! Restzilla will now automatically inject a repository, service and controller. Where the controller will handle the following requests:

* `GET    /user`
* `GET    /user/{id}`
* `POST   /user`
* `PUT    /user/{id}`
* `DELETE /user/{id}`

## Pagination and sorting ##

The GET requests by default return a collection of entities. But it is also possible to retrieve the entities as page:

* `GET     /user?page=0&size=10`

Also, the entities can be retrieved with a specific order:

* `GET     /user?page=0&size=10&sort=id,[asc|desc]`
* `GET     /user?sort=id,[asc|desc]`

You can also specify default orders per entity, as fallback when no orders are specified in the request:

```java
@Entity
@RestResource
@SortingDefault("name")
public class User {

 @Id
 private Long id;
 private String name;

}
```

More complicated orders are also possible:

```java
@Entity
@RestResource
@SortingDefaults({
 @SortingDefault("name"),
 @SortingDefault(value = "id", direction = Direction.DESC)
})
public class User {

 @Id
 private Long id;
 private String name;

}
```

## Partial request body ##

Restzilla provides native support for "patch" requests, where you only update a fragment of the entity. For example, if we have a model with multiple properties:

```java
@Entity
@RestResource
public class User {

 @Id
 private Long id;
 private String name;
 private String email;

}
```

And in our request we only want to update the name:

```json
{
  "name": "New name"
}
```

Our original email remains unchanged, where normally it would have been reset to null. When you do want to clear the email adress, just provide it in the body:

```json
{
  "name": "New name",
  "email": 
}
```

## Customize body (request and response) ##

Sometimes you want to return the entity in a different format. For example, return a user without it's password for security reasons. Also, in some cases the create/update request bodies vary from the entity. Custom types are specified as follows:

```java
@Entity
@RestResource(
 create = @RestConfig(inputType = CreateUserForm.class, resultType = CreateUserResult.class)
)
public class User {

 @Id
 private Long id;
 private String name;

}
```

With the following plain old java object:

```java
public class CreateUserForm {

 public String name;
 
}
```

Create request bodies will now be unmarshalled into a CreateUserForm object. The form is mapped to a User entity and persisted in the database. After persistence our entity is mapped to the result type and placed in the response body.

Mapping between beans is automatically handled by the [BeanMapper](https://github.com/42BV/beanmapper) dependency.

## Security ##

It's also possible to secure the endpoints, making them only accessable to certain roles:

```java
@Entity
@RestResource(
 create = @RestConfig(secured = "hasRole('ROLE_ADMIN')")
)
public class User {

 @Id
 private Long id;
 private String name;

}
```

When the user does not have any of the roles we will throw a SecurityException. Note that Spring Security must be on the classpath for this functionality to work.

## Customize beans ##

Logic can be customized on each of the architecural layers: Repository, Service and Controller. This is particulary handy when domain specific functionality is desired.

### Repository ###

Restzilla relies heavily on Spring Data JPA. Each Spring Data repository will automatically be detected and used:

```java
public interface UserRepository extends CrudRepository<User, Long> {
   
   List<User> findAllByActive(boolean active);
   
}
```

#### Customer finders ####

With the @RestQuery annotation it is also possible to configure custom finder queries. Whenever a GET request matches our specified parameters, the call will be delegated to the selected method in our repository or service bean. Paging and sorting is also supported by default, whenever the target method has a Sort or Pageable parameter type. Also, when a result type is specified the resulting array or page will automatically be mapped to that type.

```java
@Entity
@RestResource(
  queries = @RestQuery(
    parameters = { "active", "version=1" }, 
    method = "findAllByActive"
  )
)
public class User {

 @Id
 private Long id;
 private String name;
 private boolean active;
 
}
```

To query all active users, we perform the following request:
* GET /user?version=1&active=true

Whenever the query always returns a single result, it can be marked as unique. This way the result will be an object, rather than an array or page.

```java
@RestQuery(parameters="active", method="findAllByActive", unique=true))
```

### Service ###

Services can have a custom implementation. By implementing the CrudService interface your services will automatically be detected. In the lines of convention of configuration we offer an extendable template class:

```java
@Service
@Transactional
@Scope(proxyMode = TARGET_CLASS)
public class UserService extends DefaultCrudService<User, Long> {
 
 @Autowired
 public UserService(UserRepository userRepository) {
  super(userRepository);
 }
 
}
```

### Controller ###

To customize the REST endpoint, just define a regular Spring MVC request mapping:

```java
@RestController
@RequestMapping("/user")
public class UserController {

 @RequestMapping(method = POST)
 public UserModel create(CreateUserModel model) {
 }
 
}
```

REST controllers can also use the @RestResource annotation to place have all endpoint logic in one class. In this case we also scan the controller class for the base path:

```java
@RestController
@RequestMapping("/user")
@RestResource(entityType = User.class)
public class UserController {

 @RequestMapping(method = POST)
 public UserModel create(CreateUserModel model) {
 }
 
}
```

## Swagger ##

Swagger is also supported, allowing you to automatically generate the API documentation. To activate Swagger, just configure the provided plugin as follows:

```java
@EnableWebMvc
@EnableSwagger
@EnableRest(basePackageClass = WebMvcConfig.class)
public class WebMvcConfig extends WebMvcConfigurerAdapter {

 private SpringSwaggerConfig springSwaggerConfig;

 @Bean
 public SwaggerRestPlugin swaggerRestPlugin(CrudHandlerMapping crudHandlerMapping) {
  return new SwaggerRestPlugin(springSwaggerConfig, crudHandlerMapping);
 }

 @Autowired
 public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
  this.springSwaggerConfig = springSwaggerConfig;
 }
 
}

```

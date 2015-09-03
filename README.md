# Restify

The purpose of [Restify](https://github.com/42BV/restify) is to dynamically generate REST endpoints for entity CRUD. In contrast to Spring DATA REST, Restify follows the Controller-Service-Repository architectural pattern. Restify is build on convention over configuration, resulting in very minimal amounts of code with optimal functionality. But we are also very flexible, enabling simple overwrites in each architural layer.

## Features ##

* Generates (CRUD) REST endpoints, requiring only a JPA entity class
* Overwrite posibilities on each layer: Controller, Service, Repository

## Quick Start ##

Clone the restify project and install with Maven.

Add the dependency to your own project:

```xml
<dependency>
  <groupId>io.restify</groupId>
  <artifactId>restify</artifactId>
  <version>1.3-SNAPSHOT</version>
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

That's it! Restify will now automatically inject a repository, service and controller. Where the controller will handle the following requests:

* GET    /user
* GET    /user/{id}
* POST   /user
* PUT    /user/{id}
* DELETE /user/{id}

## Pagination and sorting ##

The GET requests by default return a collection of entities. But it is also possible to retrieve the entities as page:

* GET     /user?page=0&size=10

Also, the entities can be retrieved with a specific order:

* GET     /user?page=0&size=10&sort=id,[asc|desc]
* GET     /user?sort=id,[asc|desc]

You can also specify default orders per entity, as fallback when no orders are specified in the request:

```java
@Entity
@EnableRest
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
@EnableRest
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

## Customize body ##

Sometimes you want to return the entity in a different format. For example, return a user without it's password for security reasons. Also, in some cases the create- or update request varies from the entity. Restify allows you to specify custom types:

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

Requests will now be unmarshalled to their custom type. The custom types are then mapped back on our entity as we perform our logic.

Mapping between beans is automatically handled by the [BeanMapper](https://github.com/42BV/beanmapper) dependency.

## Customize functionality ##

Logic can be overwritten on each of the architecural layers: Repository, Service, Controller. This is particulary handy when domain specific functionality is required.

### Repository ###

Restify relies heavily on Spring Data JPA. Spring Data repositories will automatically be used:

```java
public interface UserRepository extends CrudRepository<User, Long> {
}
```

### Service ###

It is also possible to create a custom service, just implement the CrudService interface. Or you could overwrite the AbstractCrudService template class:

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

To create a custom REST endpoint, just define a regular Spring MVC request mapping:

```java
@RestController
@RequestMapping("/user")
public interface UserController {

 @RequestMapping(method = POST)
 public UserModel create(CreateUserModel model) {
 }
 
}
```

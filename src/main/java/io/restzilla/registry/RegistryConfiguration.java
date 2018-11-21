package io.restzilla.registry;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegistryConfiguration {

  /**
   * Build a registry with references to each entity service and repository.
   *
   * @return the service registry
   */
  @Bean
  public CrudServiceRegistry crudServiceRegistry(CrudServiceFactory factory) {
    return new CachingServiceRegistry(factory);
  }

  /**
   * Build a factory with references to each entity service and repository.
   *
   * @return the service registry
   */
  @Bean
  public CrudServiceFactory crudServiceFactory(ApplicationContext applicationContext) {
    CrudServiceFactory delegate = new DefaultServiceFactory(applicationContext);
    return new LookupServiceFactory(delegate);
  }

}

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
  public CrudServiceRegistry crudServiceRegistry(ApplicationContext applicationContext) {
    CrudServiceFactory factory = new CrudServiceFactory(applicationContext);
    return new DefaultServiceRegistry(factory);
  }

}

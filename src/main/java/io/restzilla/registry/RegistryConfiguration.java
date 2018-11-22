package io.restzilla.registry;

import io.restzilla.repository.CrudRepositoryFactory;
import io.restzilla.repository.DefaultRepositoryFactory;
import io.restzilla.service.CrudServiceFactory;
import io.restzilla.service.DefaultServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class RegistryConfiguration {

  private final ApplicationContext applicationContext;

  @Autowired
  public RegistryConfiguration(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  /**
   * Build a registry with references to each entity service and repository.
   *
   * @return the service registry
   */
  @Bean
  public CrudServiceRegistry crudServiceRegistry(
    final Optional<CrudRepositoryFactory> repositoryFactory,
    final Optional<CrudServiceFactory> serviceFactory
  ) {
    return new DefaultServiceRegistry(
      repositoryFactory.orElseGet(this::newRepositoryFactory),
      serviceFactory.orElseGet(this::newServiceFactory)
    );
  }

  private DefaultRepositoryFactory newRepositoryFactory() {
    return new DefaultRepositoryFactory(applicationContext);
  }

  private DefaultServiceFactory newServiceFactory() {
    return new DefaultServiceFactory(applicationContext);
  }

}

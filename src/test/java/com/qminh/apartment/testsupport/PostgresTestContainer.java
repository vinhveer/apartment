package com.qminh.apartment.testsupport;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class PostgresTestContainer {

  private static final boolean USE_LOCAL_DB =
      System.getenv("DB_URL") != null || System.getProperty("DB_URL") != null;

  private static final PostgreSQLContainer<?> CONTAINER = initContainer();

  @SuppressWarnings("resource")
  private static PostgreSQLContainer<?> initContainer() {
    if (USE_LOCAL_DB) {
      return null;
    }
    PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:17-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
    container.start();
    Runtime.getRuntime().addShutdownHook(new Thread(container::stop));
    return container;
  }

  @DynamicPropertySource
  static void registerProps(DynamicPropertyRegistry r) {
    if (USE_LOCAL_DB) {
      r.add("spring.datasource.url", () ->
          System.getenv().getOrDefault("DB_URL",
              System.getProperty("DB_URL", "jdbc:postgresql://localhost:5432/apartment")));
      r.add("spring.datasource.username", () ->
          System.getenv().getOrDefault("DB_USERNAME",
              System.getProperty("DB_USERNAME", "postgres")));
      r.add("spring.datasource.password", () ->
          System.getenv().getOrDefault("DB_PASSWORD",
              System.getProperty("DB_PASSWORD", "postgres")));
    } else {
      r.add("spring.datasource.url", CONTAINER::getJdbcUrl);
      r.add("spring.datasource.username", CONTAINER::getUsername);
      r.add("spring.datasource.password", CONTAINER::getPassword);
    }
    r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    r.add("spring.flyway.enabled", () -> "true");
    r.add("spring.flyway.locations", () -> "classpath:db/migration");
  }
}

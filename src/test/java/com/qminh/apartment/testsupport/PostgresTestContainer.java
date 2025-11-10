package com.qminh.apartment.testsupport;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class PostgresTestContainer {

  @Container
  protected static final PostgreSQLContainer<?> POSTGRES = createContainer();

  private static PostgreSQLContainer<?> createContainer() {
    PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:17-alpine");
    container.withDatabaseName("testdb");
    container.withUsername("test");
    container.withPassword("test");
    return container;
  }

  @DynamicPropertySource
  static void registerProps(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    r.add("spring.datasource.username", POSTGRES::getUsername);
    r.add("spring.datasource.password", POSTGRES::getPassword);
    r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    r.add("spring.flyway.enabled", () -> "true");
    r.add("spring.flyway.locations", () -> "classpath:db/migration");
  }
}

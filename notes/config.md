# Configuration properties of Spring

There are two different kinds of configurations in Spring:
- Bean wiring: this declares applicaiton components to be created as beans in the Spring application context and how they should be injected into each other.
- Property injection: this is what sets values on beans in the Spring application context. 

In Java configuration, an `@Bean` instantiate a bean and then set values to its properties. For example, following declares a DataSource for an embedded H2 database. The `addScript()` and `addScripts()` methods set name of SQL scripts that should be applied to the database once the data source is ready. With Spring Boot, autoconfiguration makes it unnecessary if H2 dependency is available in the run-time classpath. The bean by default applies the SQL scripts `schema.sql` and `data.sql`.

```java
@Bean
public DataSource dataSource() {
  return new EmbeddedDataSourceBuilder()
    .setType(H2)
    .addScript("taco_schema.sql")
    .addScripts("user_data.sql", "ingredient_data.sql")
    .build();
}
```

The Spring environment pulls properties from several sources including JVM system properties, OS environment variables, command-line arguments and application property configuration files. Suppose you wanted application's servlet container to listen for requests on port other than 8080. To do that we can set `server.port` property in `src/main/resources/application.properties`. The same can be done using `src/main/resources/application.yml` with YAML format.

```yml
server:
  port: 9090
```

The same could be configured externally using `java -jar tacocloud-0.0.0-SNAPSHOT.jar --server.port=9090`.
Similarly using environment variables as `export SERVER_PORT=9090`. The naming style is all uppercase.

Similarly, instead of configuring DataSource bean, it's easy to configure URL and credentials for database via configuration properties. Spring Boot will automatically figure out the JDBC driver classname, but could also be set using `spring.datasource.driver-class-name` property.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost/tacocloud
    username: tacodb
    password: tacopassword
    driver-class-name: com.mysql.jdbc.Driver
```

The DataSource bean will be pooled using Tomcat's JDBC connection pool if it's available on the classpath. Spring boot looks for and uses one of the connnection pool implementations on the class path either HikariCP or Commons DBCP2. The connection pool could also be configured using DataSource bean. We can also `spring.datasource.schema` to load schemas and `spring.datasource.data` to load schema and data when application starts.

```yaml
spring:
  datasource:
    schema:
    - order-schema.sql
    - ingredient-schema.sql
    - taco-schema.sql
    - user-schema.sql
    data:
    -ingredients.sql
```

We can also set `server.port=0` so that application starts on a randomly chosen available ports. It's useful when it's a microservice that will be looked up from a service registry in which we don't care what port application starts on.

To **handle HTTPS requests**, we can use JDK's keytool utility to create Java key storage.

```shell
keytool -keystore mykeys.jks -genkey -alias tomcat -keyalg RSA
```

When password is asked, entered `letmein`.

```yaml
server:
  port: 8443
  ssl:
    key-store: file://path/to/mykeys.jks
    key-store-password: letmein
    key-password: letmein
```

### Configuring logging

By default, Spring Boot configures logging via Logback to write to the console at an INFO level. For full control of logging configuration, you can create a `logback.xml` file at the root of classpath (`src/main/resources`).

```xml
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>
        %d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
      </pattern>
    </encoder>
  </appender>
  <logger name="root" level="INFO"/>
  <root level="INFO">
    <appender-ref ref="STDOUT" />
  </root>
</configuration>
```

With Spring Boot configuration properties, we can change logging level by using `logging.level`.

```yaml
logging:
  # specify the path and filename
  path: /var/logs/
  file: TacoCloud.log
  level:
    root: WARN
    org:
      springframework:
        security: DEBUG
```

```yaml
logging:
  level:
    root: WARN
    org.springframework.security: DEBUG
```

By default, Spring Boot will rotate the log files once they reach 10MB in size.
When setting properties in configuration properties, we can derive values using `${}`.

```yaml
greeting:
  welcome: You are using ${spring.application.name}.
```

We can **create new custom properties** using `@ConfigurationProperties` annotation. When placed on any Spring bean, it specifies that the properties of that bean can be injected from properties in the Spring environment. Suppose we add functionality to list authenticated user's past orders in OrderController.

If user has placed too many orders, it would be nice to show limited orders and displayed paginated content of 20 orders only. We can change `ordersForUser()` method in `OrderController`.

```java
@GetMapping
public String ordersForUser(
    @AuthenticationPrincipal User user, Model model) {

  Pageable pageable = PageRequest.of(0, 20);
  model.addAttribute("orders",
      orderRepo.findByUserOrderByPlacedAtDesc(user, pageable));

  return "orderList";
}
```

However, here we hard-coded the page size. Rather than hardcode the page size, we can set it with a custom configuration property. First, add a property called `pageSize` to `OrderController` and then annotated `OrderController`  with `@ConfigurationProperties`.

```java
@ConfigurationProperties(prefix="taco.orders")
public class OrderController {

  private int pageSize = 20;

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }
  ... ...
}
```

Now, the `taco.orders.pageSize` defaults to 20, but can be easily changed using `taco.orders.pageSize` property or using `export TACO_ORDERS_PAGESIZE=10`.

`@ConfigurationProperties` can be set on any beans. We can set such on a bean whose sole purpose is to hold configuration data as shown in `tacos.web.OrderProps` class. It is annotated with `@Component` for Spring component scanning to automatically discover it. We inject this in OrderController to use it. Having a separate bean allows to keep controller code cleaner and reuse the properties in any other bean that may need them. All the properties pertaining to orders is in one place. This makes it easy to write validation code for these props easier as we can annotate with `@Min` or `@Max` in one place.

When we define a new property, we need to configure property metadata so that IDEs can help us suggesting properties. Metadata also provides minimal documentation. To create metadata for custom configuration properties, you'll need to create a file udner `src/main/resources/META-INF` named `additional-spring-configuration-metadata.json`. This could be created using IDE when we hover over these property in configuration file. Once we define description for the file, hovering this property will show description summary.

### Profiles

Different environments have different configurations for database and many others. We can use Spring profiles for conditional configuration where different beans, configuration classes and configuration properties are applied or ignored based on what profiles are active at runtime.

One way to define profile-specific properties is to create another YAML or properties file containing only the properties for production. The name of the file should be like `application-<profile name>.yml` or `application-<profile name>.properties`. For example, application-prod.yml for production.

Another way works only with YAML configuration. It involves placing profile-specific properties alongside non-profiled properties in `application.yml` separated by three hyphens and the `spring.profiles` property to name the profile.

```yaml
# for development
logging:
  level:
    tacos: DEBUG

---
spring:
  profiles: prod
  
  datasource:
    url: jdbc:mysql://localhost/tacocloud
    username: tacouser
    password: tacopassword
 
logging:
  level:
    tacos: WARN
```

First section doesn't specify any `spring.profiles` and therefore its properties are common to all profiels or are defaults if the active profiel doesn't otherwise have the properties set. If profile named `prod` is active, then the `logging.level.tacos` property will be overridden with WARN.

The profile can be activated using profile name given to `spring.profiles.active` property in `application.yml` file. Instead, better options is to set environment variable as `export SPRING_PROFILES_ACTIVE=prod`. From then on, any applications deployed to that machine will have the prod profile active and the corresponding configuration properties would take precedence over the properties in the default profile. We can also pass configuration using `java -jar taco-cloud.jar --spring.profiles.active=prod`.

We can also have more than one profiles active using `export SPRING_PROFILES_ACTIVE=prod,audit,ha`. In YAML it can be done as below.

```yaml
spring:
  profiles:
    active:
    - prod
    - audit
    - ha
```

We can also **conditionally create beans with profiles**. Normally, any bean declared in a Java configuration class is created regardless of which profile is active. If we need some beans be created if a certain profile is active, we can use `@Profile` to designate beans as only being applicable to a given profile. For example, `CommandLineRunner` bean is used to load embedded database with ingredeitn data when application starts. It would be unnecessary in a production application. To prevent this data loading, we can use `@Profile("dev")`.

```java
@Bean
@Profile({"dev", "qa"})
public CommandLineRunner dataLoader(IngredientRepository repo,
      UserRepository userRepo, PasswordEncoder encoder) {

  ...

}
```

There can be another option that we can enable `CommandLineRunner` if profile is not prod using `@Profile("!prod")`. The exclamation mark (!) negates the profile name. We can even extract such code into separate configuration class named `DevelopmentConfig`.


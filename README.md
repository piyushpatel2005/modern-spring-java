# Spring Tutorial from Spring in Action

`@SpringBootApplication` is a composite that combinies three annotations:

1. `@SpringBootConfiguration`: designates this class as a configuration class. This is specialized form of `@Configuration` annotation. We can add Java based configuration to this class.
2. `@EnableAutoConfiguration`: enables Spring Boot automatic configuration.
3. `@ComponentScan`: enables component scanning. This lets you declare other classes with annotations like `@Component`, `@Controller`, `@Service` and others to have Spring auto-discover them and register them as components in application context. 

Spring Boot application also comes with tests. Tests are annotated with `@RunWith` to provide the class runner.

For mapping different types of HTTP methods, we have `@GetMapping`, `@PostMapping` and so on annotations. `@RequestMapping` specifies the path to map for any controller class.

Java Bean validation API helps us validate user input using simple annotations like `@NotNull`, `@NotBlank`, etc.

Any configuration class can implement `WebMvcConfigurer` and override `addViewController` method. So, we can define home page view controller in the `TacoCloudApplication` class as below.

```java
@SpringBootApplication
public class TacoCloudApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(TacoCloudApplication.class, args);
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("home");
	}
}
```

Usually, each kind of configuration are defined in separate configuration class to keep application bootstrap configuration class clean.

There are few template options supported by Spring Boot. Below table gives the template with its starter library

| Table | Spring boot starter library |
|:---------:|:-------:|
|FreeMarker | spring-boot-starter-freemarker |
| Groovy Templates | spring-boot-starter-groovy-templates |
| JavaServer Pages (JSP) | None |
| Mustache | spring-boot-starter-mustache |
| Thymeleaf | spring-boot-starter-thymeleaf |

Spring Boot will detect your chosen template library and automatically configure the components for it to serve views. JSP is only an option if you're building your application as a WAR file and deploying it in a traditional servlet container. By default, templates are only parsed once, when we first used and the results of that parse are cached for subsequent use. There's a way to disable caching for development. For that, we need to set a template-appropriate caching property to `false`. By default these are true, we can disable using `application.properties` file.

```
spring.thymeleaf.cache=false
spring.groovy.template.cache=false
spring.mustache.cache=false
spring.freemarker.cache=false
```

For production, we need to remove this one, better way is to set it in profiles or use DevTools for development.

## JDBC support

JDBC support is in `JdbcTemplate` class. To work with JdbcTemplate, we need to add `spring-boot-starter-jdbc` dependency to build file. Check `JdbcIngredientRepository` for example of this. For RowMapper, we use `this::mapRowToIngredient` Java 8's method references and lambdas.

[Data models](notes/models.md)

`@SessionAttribute` specifies any model objets like the order attribute that should be kept in session and available across multiple requests. `@ModelAttribute` annotation on `order()` ensures that an Order object will be created in the model. The Order object remains in the sesion and isn't saved to database until the user completes and submits the order form. At the end, `OrderController` will save the order. `SimpleJdbcInsert` wraps `JdbcTemplate` to make it easier to insert data into a table. Check `JdbcOrderRepository` for example.


## JPA Support using Spring Data

[Spring Data](notes/spring-data.md)

## Spring Security

[Spring Security](notes/spring-security.md)

## Configuring properties

[Customizing Spring application with configuration](notes/config.md)

## REST services

[REST Services with Spring](notes/rest-services.md)
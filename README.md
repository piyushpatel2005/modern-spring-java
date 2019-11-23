# Spring Tutorial from Spring in Action

`@SpringBootApplication` is a composite that combinies three annotations:

1. `@SpringBootConfiguration`: designates this class as a configuration class. This is specialized form of `@Configuration` annotation. We can add Java based configuration to this class.
2. `@EnableAutoConfiguration`: enables Spring Boot automatic configuration.
3. `@ComponentScan`: enables component scanning. This lets you declare other classes with annotations like `@Component`, `@Controller`, `@Service` and others to have Spring auto-discover them and register them as components in application context. 

Spring Boot application also comes with tests. Tests are annotated with `@RunWith` to provide the class runner.

For mapping different types of HTTP methods, we have `@GetMapping`, `@PostMapping` and so on annotations. `@RequestMapping` specifies the path to map for any controller class.

Java Bean validation API helps us validate user input using simple annotations like `@NotNull`, `@NotBlank`, etc.
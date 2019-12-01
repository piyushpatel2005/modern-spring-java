# Spring Security

To enable Spring security in Spring boot application, we have to add spring-boot-starter-security dependency to Maven file. As soon as this dependency is added, all endpoints are blocked and asks for password. The default username is `user` and the password is randomly generated and written to application log file with `Using default security password:`.

## Configure Security

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
}
```

Above configuration will provide HTTP basic authentication with login form. Spring Security offers several options for configuration a user store. For each store, we can configure it by overriding a `configure()` method defined in `WebSecurityConfigurerAdapter` base class.

In **in-memory user store**, we configure each user in code. Each call to `withUser()` starts configuration for a user. The `authorities()` method grants `ROLE_USER` authority.

```java
@Override
protected void configure(AuthenticationManagerBuilder auth)
    throws Exception {

  auth
    .inMemoryAuthentication()
      .withUser("buzz")
        .password("infinity")
        .authorities("ROLE_USER")
      .and()
      .withUser("woody")
        .password("bullseye")
        .authorities("ROLE_USER");

}
```

For **JDBC-based user store**, we can setup a data source and use `jdbcAuthentication()` method to provide JDBC user store.

```java
@Autowired
DataSource dataSource;

@Override
protected void configure(AuthenticationManagerBuilder auth)
    throws Exception {

  auth
    .jdbcAuthentication()
      .dataSource(dataSource);

}
```


This makes assumption about the database schema with specific tablename and property names. 

```java
public static final String DEF_USERS_BY_USERNAME_QUERY =
        "select username,password,enabled " +
        "from users " +
        "where username = ?";
public static final String DEF_AUTHORITIES_BY_USERNAME_QUERY =
        "select username,authority " +
        "from authorities " +
        "where username = ?";
public static final String DEF_GROUP_AUTHORITIES_BY_USERNAME_QUERY =
        "select g.id, g.group_name, ga.authority " +
        "from groups g, group_members gm, group_authorities ga " +
        "where gm.username = ? " +
        "and g.id = ga.group_id " +
        "and g.id = gm.group_id";
```

We can customize this using following code snippet.

```java
auth
  .jdbcAuthentication()
  .dataSource(dataSource)
  .userByUsernameQuery(
    "select username, password, enabled from Users where username=?")
  .authoritiesByUsernameQuery(
    "select username, authority from UserAuthorities where username=?"
  ).passwordEncoder(new StandardPasswordEncoder("53cr3t");
```

We can also override group authorities query by calling `groupAuthoritiesByUsername()` with a custom query. To protect password, we use `passwordEncoder()` so that we don't save the password as a plain text. Spring Security incldues several implementations.

- `BCryptPasswordEncoder`: bcrypt hashing encryption
- `NoOpPasswordEncoder`: applies no encoding
- `Pbkdf2PasswordEncoder`: applies PBKDF2 encryption
- `StandardPasswordEncoder`: applies SHA-256 hashing

PasswordEncoder interface has two methods. The `encode()` method encodes the plain text password to encoded password and `matches()` method matches the provided password with saved password.

```java
public interface PasswordEncoder {
  String encode(CharSequence rawPassword);
  boolean matches(CharSequence rawPassword, String encodedPassword);
}
```

To configure Spring Security for **LDAP authentication**, we can use `ldapAuthentication()` method.

```java
@Override
protected void configure(AuthenticationManagerBuilder auth)
    throws Exception {
  auth
    .ldapAuthentication()
      .userSearchBase("ou=people")
      .userSearchFilter("(uid={0})")
      .groupSearchBase("ou=groups")
      .groupSearchFilter("member={0}")
      .passwordCompare()
      .passwordEncoder(new BCryptPasswordEncoder())
      .passwordAttribute("passcode")
      .contextSource()
        .url("ldap://tacocloud.com:389/dc=tacocloud,dc=com");
}
```

The `userSearchFilter()` and `groupSearchFilter()` methods provide filters for the base LDAP queries used to search for users and groups. By default, the base queries for both are empty, indicating that search will be done from the root of the LDAP hierarchy. That can be changed using `userSearchBase()` and `groupSearchBase()` methods. Here groups should be searched for where the organizational unit is `groups`. If we'd authenticate by doing a password comparison, we can declare that using `passwordCompare()` method. By default, the password in login form will be compared with the value of `userPassword` attribute in the user's LDAP entry. If password is kept in a different attribute, we can specify password attribute with `passwordAttribute()` method. We can use `passwordEncoder()` to avoid sending password in plain text. This also assumes that the passwords are also encrypted using bcrypt in the LDAP server. If LDAP server is on another machine, we can use `contextSource()` to configure remote location.

Spring security provides an embedded LDAP server for development. Instead of setting the URL to remote LDAP, we can specify the root suffix for embedded server via the `root()` method as shown below. When LDAP server starts, it will attempt to load data from LDIF files that it can find in the classpath. LDIF (LDAP Data Interchange Format) is a way of representing LDAP data in a plain text file and is composed of lines with name-value pairs. Records are separated by blank lines. We can be explicit about which LDIF file gets loaded using `ldif()` method.

```java
auth
    .ldapAuthentication()
      .userSearchBase("ou=people")
      .userSearchFilter("(uid={0})")
      .groupSearchBase("ou=groups")
      .groupSearchFilter("member={0}")
      .passwordCompare()
      .passwordEncoder(new BCryptPasswordEncoder())
      .passwordAttribute("passcode")
      .contextSource()
        .root("dc=tacocloud,dc=com")
        .ldif("classpath:users.ldif");
```

To customize user data we can create `User` class with required fields. In `UserRepository`, we defined `findByUsername()` that will be used to look up a user by their username.

To get user details, we can use `UserDetailsService` which is a simple interface with only one method `loadUserByUsername(String username)` that can be used to fetch user details. To configure user details service with Spring security we need to define `configure()` method. As with JDBC-based authentication, you can also configure a password encoder so that the password can be encoded in the database. Check the bean of type `PasswordEncoder` and inject that using `passwordEncoder()` method call.

For registering a user, we created RegistrationController class where we display registration form and when user submits registration form, we process registration using `processRegistration()` method. The `RegistrationForm` object passed to `processRegistration()` is bound to the request data.

In this app, we block only authenticated users to design a taco and place an order. To configure these security rules, we need to use `WebSecurityConfigurerAdapter`'s `configure()` method with `HttpSecurity`.

This could be configured as below.

```java
protected void configure(HttpSecurity http) throws Exception {
  http
    .authorizeRequests()
      .antMatchers("/design", "/orders")
        .hasRole("ROLE_USER")
      .antMatchers("/", "/**").permitAll();
}
```

Requests for `/design` and `/orders` should be for users with granted authority of ROLE_USER and all requests should be permitted to all users. Here, the order of rules is very important. We can also use the `access()` method to provide SpEL expression to declare richer security rules.

| Security Expression | What it evalutes to |
|:--------------------|:--------------------|
| authentication | user's authentication object |
| denyAll | always evaluates to false |
| hasAnyRole(list of roles) | true if the user has an of the roles |
| hasRole(role) | true if user has the given role |
| hasIpAddress(IP address) | true if request comes from given IP |
| isAnonymous() | true if the user is anonymous |
| isAuthenticated() | true if the user is authenticated |
| isFullyAuthenticated() | true if the user is fully authenticated (not with remember-me) |
| isRememberMe() | true if the user was authenticated via remember-me |
| permitAll | always evaluates to true |
| principal | the user's principal object |

With such expression language, we configure complicated rules. For example if we wanted to allow user with ROLE_USER authority to create new Tacos on Tuesdays, we can write like this.

```java
http
  .authorizeRequests()
    .antMatchers("/design", "/orders")
      .access("hasRole('ROLE_USER') && T(java.util.Calendar).getInstance().get(" +
        "T(java.util.Calendar).DAY_OF_WEEK) == T(java.util.Calendar).TUESDAY")
    .antMatchers("/", "/**").access("permitAll");
```

We can replace provided login page by calling `formLogin()` in `configure()` method. For serving this login page, we only need a view controller which we can add in the `WebConfig` class. 

If in the login form, our username and password are with different name attributes than `username` and `password`, we can configure field names using following snippet.

```java
.and()
  .formLogin()
    .loginPage("/login")
    .loginProcessingUrl("/authenticate")
    .usernameParameter("user")
    .passwordParameter("pwd")
```

By default, a successful login will take the user directly to the page that they were navigating to when Spring Security determined that they needed to login. If the user were to directly navigate to the login page, a successful login would take them to the root path. We can change that by specifying default success page.

Optionally, we can force the user to the design page after login even if they were navigatin elsewhere prior to logging in, by passing true as second parameter to `defaultSuccessUrl`.

```java
.and()
  .formLogin()
    .loginPage("/login")
    .defaultSuccessUrl("/design")
```

To enable user to logout, we use `logout()` method and add a snippet of code with form which posts to `/logout` path.

**Cross-site request forgery (CSRF)** can be blocked by generating CSRF token upon displaying a form. Spring securiyt makes it easy by placing the CSRF token in a request attribute with the name `_csrf`. WE can disable CSRF using `.and().csrf().disable()`. 

It's important that when user is logged in, they don't have to fill up their details again if the details are already present in their account information. It would be nice if we could prepopulate `Order` with user's name and address so that they don't have to reenter it for each order. We should associate the ORder with the User that created the order. We add `@ManyToOne` relationship with User in Order domain class.

There are several ways to determine who the logged in user is.

1. Inject `Principal` object into the controller method.

```java
public String processOrder(@Valid Order order, Errors errors, Sessionstatus sessionStatus, Principal principal) {
... ....
User user = userRepository.findByUsername(principal.getName());
order.setUser(user);
...
}
```

2. Inject `Authentication` object into the controller method

```java
public String processOrder(@Valid Order order, Errors errors, SessionStatus sessionStatus, Authentication authentication) {
...
User user = (User) authentication.getPrincipal();
order.setUser(user);
...
}
```

3. User `SecurityContextHolder` to get the security context.

This can be used anywhere but its heavy with security specific code.

```java
Authentation authentication = SecurityContextHolder.getContext().getAuthentication();
User user = (User) authentication.getPrincipal();
```

4. Use an `@AuthenticationPrincipal` annotated method. (best option)

```java
public String processOrder(@Valid Order order, Errors errors, SessionStatus sessionStatus, @AuthenticatedPrincipal User user) {
  if (errors.hasErrors()) {
    return "orderForm";
  }
  order.setUser(user);
  
  orderRepo.save(order);
  sessionStatus.setComplete();
  return "redirect:/";
}
```
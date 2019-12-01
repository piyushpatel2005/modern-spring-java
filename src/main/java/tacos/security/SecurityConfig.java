package tacos.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.userDetailsService(userDetailsService)
			.passwordEncoder(encoder());
	}

	@Override
	protected void configure(HttpSecurity http)  throws Exception {
		http
			.authorizeRequests()
			.antMatchers("/design", "/orders")
				.access("hasRole('ROLE_USER')")
			.antMatchers("/**")
				.access("permitAll")
			// specify form login page to use for logging in
			.and()
				.formLogin()
					.loginPage("/login")
			// specify logout and redirection url after logging out
			.and()
				.logout()
					.logoutSuccessUrl("/")
			// Make H2 console non-secured for debug purposes
			.and()
				.csrf()
				 .ignoringAntMatchers("/h2-console/**")
			// Allow pages to be loaded in frames from the same origin, for H2 console 
			.and()
				.headers()
				 	.frameOptions()
				   	.sameOrigin();
	}
	
	@Bean
	public PasswordEncoder encoder() {
		return new StandardPasswordEncoder("53cr3t");
	}

	
	// LDAP authentication
//	@Override
//	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//		auth
//			.ldapAuthentication()
//			.userSearchFilter("(uid={0})")
//			.groupSearchFilter("member={0}");
//	}
	
//	@Autowired
//	DataSource dataSource;
//	
//	@Override 
//	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//		auth
//			.jdbcAuthentication()
//			.dataSource(dataSource)
//			.userByUsernameQuery(
//					"select username, password, enabled from Users where username=?"
//			).authoritiesByUsernameQuery(
//					"select username, authority from UserAuthorities where username=?"
//			).passwordEncoder(new StandardPasswordEncoder("53cr3t"));
//			
//			
//	}

}

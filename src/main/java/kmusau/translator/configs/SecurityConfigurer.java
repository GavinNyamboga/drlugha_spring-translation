package kmusau.translator.configs;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import kmusau.translator.filter.JwtRequestFilter;
import kmusau.translator.service.MyUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfigurer extends WebSecurityConfigurerAdapter {

	@Autowired
	private MyUserDetailsService myUserDetailsService;
	
	@Autowired
	private JwtRequestFilter jwtRequestFilter;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(myUserDetailsService);
	}

	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests()
		.antMatchers("/fetch/users", "/delete/user/{user_id}", "/delete/sentence/{id}", "/stats", "/languages/*", "/batch").hasRole("ADMIN")
		.antMatchers("/update/sentence/{id}", "/update/translatedsentencestatus/{id}", "/update/voicestatus/{voiceId}").hasAnyRole("ADMIN","MODERATOR")
		.antMatchers("/authenticate", "/create/user", "/hash-passwords").permitAll()
		.anyRequest().authenticated()
		.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.cors().configurationSource(request-> {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(Arrays.asList("*"));
            configuration.setAllowedMethods(Arrays.asList("GET","POST", "PUT", "DELETE", "PATCH"));
            configuration.setAllowedHeaders(List.of("*"));
            return configuration;
        }).and().exceptionHandling().authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
		http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
		
	}

	@Bean
	public AuthenticationProvider authenticationProvider(){
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder());
		authenticationProvider.setUserDetailsService(myUserDetailsService);
		return authenticationProvider;
	}

	@Bean public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public PasswordEncoder passwordencoder() {
		return new BCryptPasswordEncoder();
	}
}
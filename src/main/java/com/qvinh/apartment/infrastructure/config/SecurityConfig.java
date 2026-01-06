package com.qvinh.apartment.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qvinh.apartment.infrastructure.security.JwtAuthFilter;
import com.qvinh.apartment.infrastructure.security.RestAccessDeniedHandler;
import com.qvinh.apartment.infrastructure.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;
	private final ObjectMapper objectMapper;

	public SecurityConfig(JwtAuthFilter jwtAuthFilter, ObjectMapper objectMapper) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.objectMapper = objectMapper;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.cors(Customizer.withDefaults())
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint(new RestAuthenticationEntryPoint(objectMapper))
				.accessDeniedHandler(new RestAccessDeniedHandler(objectMapper))
			)
			.authorizeHttpRequests(reg -> reg
				// CORS preflight
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

				// Files
				.requestMatchers(HttpMethod.POST, "/api/files").authenticated()
				.requestMatchers(HttpMethod.GET, "/api/files/**").permitAll()

				// Static files (public uploads)
				.requestMatchers(HttpMethod.GET, "/public/**").permitAll()

				// Auth
				.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()

				// Account creation (requires auth)
				.requestMatchers(HttpMethod.POST, "/api/create-sale").authenticated()
				.requestMatchers(HttpMethod.POST, "/api/create-admin").authenticated()

				// Property - public read/search
				.requestMatchers(HttpMethod.GET, "/api/properties").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/properties/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/properties/search").permitAll()

				// Property Type - public read
				.requestMatchers(HttpMethod.GET, "/api/property-types").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/property-types/**").permitAll()

				// Property Area - public read
				.requestMatchers(HttpMethod.GET, "/api/areas").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/areas/**").permitAll()

				// Property Define Details - public read
				.requestMatchers(HttpMethod.GET, "/api/property-define-details").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/property-define-details/**").permitAll()

				// All other requests require authentication
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("http://localhost:3000"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}

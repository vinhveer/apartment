package com.qvinh.apartment.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qvinh.apartment.features.auth.api.AuthController;
import com.qvinh.apartment.features.files.api.FilesController;
import com.qvinh.apartment.features.properties.api.PropertyAreaController;
import com.qvinh.apartment.features.properties.api.PropertyController;
import com.qvinh.apartment.features.properties.api.PropertyDefineDetailsController;
import com.qvinh.apartment.features.properties.api.PropertyTypeController;
import com.qvinh.apartment.infrastructure.security.JwtAuthFilter;
import com.qvinh.apartment.infrastructure.security.RestAccessDeniedHandler;
import com.qvinh.apartment.infrastructure.security.RestAuthenticationEntryPoint;
import com.qvinh.apartment.shared.constants.WebPaths;
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
				.requestMatchers(HttpMethod.POST, FilesController.BASE_PATH).authenticated()
				.requestMatchers(HttpMethod.GET, FilesController.BASE_PATH_ALL).permitAll()

				// Static files (public uploads)
				.requestMatchers(HttpMethod.GET, WebPaths.PUBLIC_FILES).permitAll()

				// Auth
				.requestMatchers(HttpMethod.POST, AuthController.LOGIN_PATH).permitAll()
				.requestMatchers(HttpMethod.POST, AuthController.REFRESH_PATH).permitAll()
				.requestMatchers(HttpMethod.POST, AuthController.LOGOUT_PATH).permitAll()

				// Property - public read/search
				.requestMatchers(HttpMethod.GET, PropertyController.BASE_PATH).permitAll()
				.requestMatchers(HttpMethod.GET, PropertyController.BASE_PATH_ALL).permitAll()
				.requestMatchers(HttpMethod.POST, PropertyController.SEARCH_PATH).permitAll()

				// Property Type - public read
				.requestMatchers(HttpMethod.GET, PropertyTypeController.BASE_PATH).permitAll()
				.requestMatchers(HttpMethod.GET, PropertyTypeController.BASE_PATH_ALL).permitAll()

				// Property Area - public read
				.requestMatchers(HttpMethod.GET, PropertyAreaController.BASE_PATH).permitAll()
				.requestMatchers(HttpMethod.GET, PropertyAreaController.BASE_PATH_ALL).permitAll()

				// Property Define Details - public read
				.requestMatchers(HttpMethod.GET, PropertyDefineDetailsController.BASE_PATH).permitAll()
				.requestMatchers(HttpMethod.GET, PropertyDefineDetailsController.BASE_PATH_ALL).permitAll()

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

package com.qvinh.apartment.infrastructure.config;

import com.qvinh.apartment.features.accounts.domain.Role;
import com.qvinh.apartment.features.accounts.domain.User;
import com.qvinh.apartment.features.accounts.persistence.RoleRepository;
import com.qvinh.apartment.features.accounts.persistence.UserRepository;
import com.qvinh.apartment.shared.constants.RoleNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

	private static final String BOOTSTRAP_ADMIN_USERNAME_PROP = "app.bootstrap.admin.username";
	private static final String BOOTSTRAP_ADMIN_PASSWORD_PROP = "app.bootstrap.admin.password";
	private static final String DEFAULT_ADMIN_USERNAME = "admin";

	@Bean
	public CommandLineRunner initAdminUser(RoleRepository roleRepository,
	                                      UserRepository userRepository,
	                                      PasswordEncoder passwordEncoder,
	                                      Environment env) {
		return args -> {
			// Ensure base roles
			Role adminRole = ensureRole(roleRepository, RoleNames.ADMIN);
			ensureRole(roleRepository, RoleNames.SALE);
			ensureRole(roleRepository, RoleNames.USER);

			// Ensure default admin account
			String adminUsername = env.getProperty(BOOTSTRAP_ADMIN_USERNAME_PROP, DEFAULT_ADMIN_USERNAME);
			String rawPassword = env.getProperty(BOOTSTRAP_ADMIN_PASSWORD_PROP);
			if (rawPassword == null || rawPassword.isBlank()) {
				log.warn("Skipping bootstrap admin user creation; provide `{}` to enable it.", BOOTSTRAP_ADMIN_PASSWORD_PROP);
				return;
			}

			if (userRepository.findByUsername(adminUsername).isEmpty()) {
				User admin = new User();
				admin.setRole(adminRole);
				admin.setDisplayName("System Admin");
				admin.setUsername(adminUsername);
				admin.setEmail("admin@example.com");
				admin.setPassword(passwordEncoder.encode(rawPassword));
				admin.setCreatedAt(LocalDateTime.now());
				admin.setUpdatedAt(LocalDateTime.now());
				userRepository.save(admin);
			}
		};
	}

	private static Role ensureRole(RoleRepository roleRepository, String roleName) {
		return roleRepository.findByRoleName(roleName)
			.orElseGet(() -> {
				Role r = new Role();
				r.setRoleName(roleName);
				r.setCreatedAt(LocalDateTime.now());
				return roleRepository.save(r);
			});
	}
}

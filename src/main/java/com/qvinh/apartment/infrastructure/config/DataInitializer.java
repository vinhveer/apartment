package com.qvinh.apartment.infrastructure.config;

import com.qvinh.apartment.features.accounts.domain.Role;
import com.qvinh.apartment.features.accounts.domain.User;
import com.qvinh.apartment.features.accounts.persistence.RoleRepository;
import com.qvinh.apartment.features.accounts.persistence.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

	@Bean
	public CommandLineRunner initAdminUser(RoleRepository roleRepository,
	                                      UserRepository userRepository,
	                                      PasswordEncoder passwordEncoder) {
		return args -> {
			// Ensure base roles
			Role adminRole = roleRepository.findByRoleName("ADMIN")
				.orElseGet(() -> {
					Role r = new Role();
					r.setRoleName("ADMIN");
					r.setCreatedAt(LocalDateTime.now());
					return roleRepository.save(r);
				});

			roleRepository.findByRoleName("SALE").orElseGet(() -> {
				Role r = new Role();
				r.setRoleName("SALE");
				r.setCreatedAt(LocalDateTime.now());
				return roleRepository.save(r);
			});

			roleRepository.findByRoleName("USER").orElseGet(() -> {
				Role r = new Role();
				r.setRoleName("USER");
				r.setCreatedAt(LocalDateTime.now());
				return roleRepository.save(r);
			});

			// Ensure default admin account
			String adminUsername = "admin";
			if (userRepository.findByUsername(adminUsername).isEmpty()) {
				User admin = new User();
				admin.setRole(adminRole);
				admin.setDisplayName("System Admin");
				admin.setUsername(adminUsername);
				admin.setEmail("admin@example.com");
				admin.setPassword(passwordEncoder.encode("admin"));
				admin.setCreatedAt(LocalDateTime.now());
				admin.setUpdatedAt(LocalDateTime.now());
				userRepository.save(admin);
			}
		};
	}
}

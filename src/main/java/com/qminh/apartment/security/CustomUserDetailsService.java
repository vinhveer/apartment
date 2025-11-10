package com.qminh.apartment.security;

import com.qminh.apartment.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
		var user = userRepository.findByUsername(usernameOrEmail)
			.orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
				.orElseThrow(() -> new UsernameNotFoundException("User not found")));
		var roleName = user.getRole() != null ? user.getRole().getRoleName() : "USER";
		var auth = new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase(Locale.ROOT));
		return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
			.password(user.getPassword())
			.authorities(List.of(auth))
			.accountExpired(false)
			.accountLocked(false)
			.credentialsExpired(false)
			.disabled(false)
			.build();
	}
}



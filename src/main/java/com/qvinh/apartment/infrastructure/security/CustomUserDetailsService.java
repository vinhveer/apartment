package com.qvinh.apartment.infrastructure.security;

import com.qvinh.apartment.features.accounts.persistence.UserRepository;
import com.qvinh.apartment.features.accounts.constants.AccountsMessages;
import com.qvinh.apartment.shared.constants.RoleNames;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
		var user = userRepository.findByUsername(Objects.requireNonNull(usernameOrEmail))
			.orElseGet(() -> userRepository.findByEmail(Objects.requireNonNull(usernameOrEmail))
				.orElseThrow(() -> new UsernameNotFoundException(AccountsMessages.USER_NOT_FOUND)));
		var roleName = user.getRole() != null ? user.getRole().getRoleName() : RoleNames.USER;
		var auth = new SimpleGrantedAuthority(RoleNames.ROLE_PREFIX + roleName.toUpperCase(Locale.ROOT));
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

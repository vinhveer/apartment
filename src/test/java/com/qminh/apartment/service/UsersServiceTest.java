package com.qminh.apartment.service;

import com.qminh.apartment.dto.user.UserUpdateReq;
import com.qminh.apartment.entity.Role;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.repository.RoleRepository;
import com.qminh.apartment.repository.UserRepository;
import com.qminh.apartment.service.impl.UsersService;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UsersServiceTest extends PostgresTestContainer {

	@Autowired private UsersService service;
	@Autowired private UserRepository userRepository;
	@Autowired private RoleRepository roleRepository;

	private long userId;

	@BeforeEach
	void setup() {
		Role r = roleRepository.findByRoleName("ADMIN").orElseGet(() -> {
			Role nr = new Role(); nr.setRoleName("ADMIN"); return roleRepository.saveAndFlush(nr);
		});
		User u = new User();
		u.setUsername("userT");
		u.setEmail("userT@example.com");
		u.setPassword("x");
		u.setRole(r);
		userId = userRepository.saveAndFlush(u).getId();
	}

	@Test
	@Transactional
	@DisplayName("update and delete user")
	void update_delete() {
		UserUpdateReq up = new UserUpdateReq();
		up.setEmail("userT+new@example.com");
		up.setDisplayName("User T New");
		var res = service.update(userId, up);
		assertThat(res.getDisplayName()).isEqualTo("User T New");
		service.delete(userId);
		assertThatThrownBy(() -> service.get(userId)).isInstanceOf(RuntimeException.class);
	}
}



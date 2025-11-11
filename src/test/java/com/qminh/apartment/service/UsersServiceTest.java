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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
		assertThatThrownBy(() -> service.get(userId)).isInstanceOf(com.qminh.apartment.exception.ResourceNotFoundException.class);
	}

	@Test
	@Transactional
	@DisplayName("list(Pageable) supports pagination and sort; null pageable throws NPE")
	void list_pageable_and_null() {
		for (int i = 0; i < 5; i++) {
			User u = new User();
			u.setUsername("u_" + System.nanoTime() + "_" + i);
			u.setEmail("e_" + System.nanoTime() + "_" + i + "@example.com");
			u.setPassword("x");
			u.setRole(roleRepository.findByRoleName("ADMIN").orElseThrow());
			userRepository.saveAndFlush(u);
		}
		Pageable p = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "id"));
		var page = service.list(p);
		assertThat(page.getContent()).hasSizeLessThanOrEqualTo(3);
		assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(6);
		assertThatThrownBy(() -> service.list(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	@Transactional
	@DisplayName("not found cases: get/update/delete with non-existing id")
	void not_found_cases() {
		assertThatThrownBy(() -> service.get(999999)).isInstanceOf(com.qminh.apartment.exception.ResourceNotFoundException.class);
		UserUpdateReq up = new UserUpdateReq();
		up.setEmail("x@example.com");
		up.setDisplayName("X");
		assertThatThrownBy(() -> service.update(999999, up)).isInstanceOf(com.qminh.apartment.exception.ResourceNotFoundException.class);
		assertThatThrownBy(() -> service.delete(999999)).isInstanceOf(com.qminh.apartment.exception.ResourceNotFoundException.class);
	}
}



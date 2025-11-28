package com.qminh.apartment.service;

import com.qminh.apartment.dto.user.UserUpdateReq;
import com.qminh.apartment.entity.Role;
import com.qminh.apartment.repository.RoleRepository;
import com.qminh.apartment.repository.UserRepository;
import com.qminh.apartment.service.impl.CustomerAccountService;
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

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CustomerAccountServiceTest extends PostgresTestContainer {

	@Autowired private CustomerAccountService service;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;

	@BeforeEach
	void ensureRoles() {
		if (roleRepository.findByRoleName("USER").isEmpty()) {
			Role r = new Role();
			r.setRoleName("USER");
			roleRepository.saveAndFlush(r);
		}
	}

	@Test
	@Transactional
	@DisplayName("searchCustomerAccounts returns only USER role and supports keyword")
	void search_customer_accounts() {
		Role userRole = roleRepository.findByRoleName("USER").orElseThrow();
		for (int i = 0; i < 3; i++) {
			var u = new com.qminh.apartment.entity.User();
			u.setUsername("cust" + i);
			u.setEmail("cust" + i + "@example.com");
			u.setPassword("x");
			u.setRole(userRole);
			userRepository.saveAndFlush(u);
		}

		var pageAll = service.searchCustomerAccounts(null, org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageAll.getContent()).allMatch(u -> "USER".equals(u.getRoleName()));

		var pageFiltered = service.searchCustomerAccounts("cust1", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageFiltered.getContent()).extracting(com.qminh.apartment.dto.user.UserRes::getUsername).contains("cust1");
	}

	@Test
	@Transactional
	@DisplayName("editCustomerAccount updates email/displayName")
	void edit_customer_account() {
		Role userRole = roleRepository.findByRoleName("USER").orElseThrow();
		var u = new com.qminh.apartment.entity.User();
		u.setUsername("custEdit");
		u.setEmail("custEdit@example.com");
		u.setPassword("x");
		u.setRole(userRole);
		long id = userRepository.saveAndFlush(u).getId();

		UserUpdateReq up = new UserUpdateReq();
		up.setEmail("custEdit+new@example.com");
		up.setDisplayName("Cust Edit New");

		var res = service.editCustomerAccount(id, up);
		assertThat(res.getEmail()).isEqualTo("custEdit+new@example.com");
		assertThat(res.getDisplayName()).isEqualTo("Cust Edit New");
	}

	@Test
	@Transactional
	@DisplayName("deleteCustomerAccount removes user")
	void delete_customer_account() {
		Role userRole = roleRepository.findByRoleName("USER").orElseThrow();
		var u = new com.qminh.apartment.entity.User();
		u.setUsername("custDel");
		u.setEmail("custDel@example.com");
		u.setPassword("x");
		u.setRole(userRole);
		long id = userRepository.saveAndFlush(u).getId();

		service.deleteCustomerAccount(id);
		assertThat(userRepository.findById(id)).isNotPresent();
	}
}



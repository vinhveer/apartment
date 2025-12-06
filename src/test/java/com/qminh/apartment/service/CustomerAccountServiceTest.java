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
	@DisplayName("searchCustomerAccounts searches by username, email, and displayName")
	void search_customer_accounts_by_multiple_fields() {
		Role userRole = roleRepository.findByRoleName("USER").orElseThrow();

		// Create customers with different searchable fields
		var cust1 = new com.qminh.apartment.entity.User();
		cust1.setUsername("customer1");
		cust1.setEmail("customer1@example.com");
		cust1.setDisplayName("Alice Johnson");
		cust1.setPassword("x");
		cust1.setRole(userRole);
		userRepository.saveAndFlush(cust1);

		var cust2 = new com.qminh.apartment.entity.User();
		cust2.setUsername("customer2");
		cust2.setEmail("bob.wilson@example.com");
		cust2.setDisplayName("Bob Wilson");
		cust2.setPassword("x");
		cust2.setRole(userRole);
		userRepository.saveAndFlush(cust2);

		var cust3 = new com.qminh.apartment.entity.User();
		cust3.setUsername("customer3");
		cust3.setEmail("customer3@example.com");
		cust3.setDisplayName("Charlie Brown");
		cust3.setPassword("x");
		cust3.setRole(userRole);
		userRepository.saveAndFlush(cust3);

		// Search by username
		var pageByUsername = service.searchCustomerAccounts("customer1", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageByUsername.getContent()).extracting(com.qminh.apartment.dto.user.UserRes::getUsername).contains("customer1");
		assertThat(pageByUsername.getContent()).hasSize(1);

		// Search by email
		var pageByEmail = service.searchCustomerAccounts("bob.wilson@example.com", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageByEmail.getContent()).extracting(com.qminh.apartment.dto.user.UserRes::getEmail).contains("bob.wilson@example.com");
		assertThat(pageByEmail.getContent()).hasSize(1);

		// Search by displayName
		var pageByDisplayName = service.searchCustomerAccounts("Alice Johnson", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageByDisplayName.getContent()).extracting(com.qminh.apartment.dto.user.UserRes::getDisplayName).contains("Alice Johnson");
		assertThat(pageByDisplayName.getContent()).hasSize(1);

		// Search by partial displayName
		var pageByPartialDisplayName = service.searchCustomerAccounts("Brown", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageByPartialDisplayName.getContent()).extracting(com.qminh.apartment.dto.user.UserRes::getDisplayName).contains("Charlie Brown");
		assertThat(pageByPartialDisplayName.getContent()).hasSize(1);

		// Search by partial email
		var pageByPartialEmail = service.searchCustomerAccounts("example.com", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageByPartialEmail.getContent()).hasSizeGreaterThanOrEqualTo(3); // Should find all customers

		// Search with no results
		var pageNoResults = service.searchCustomerAccounts("nonexistent", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageNoResults.getContent()).isEmpty();
	}

	@Test
	@Transactional
	@DisplayName("searchCustomerAccounts handles null displayName correctly")
	void search_customer_accounts_with_null_displayName() {
		Role userRole = roleRepository.findByRoleName("USER").orElseThrow();

		// Create customer without displayName
		var cust = new com.qminh.apartment.entity.User();
		cust.setUsername("custNoDisplay");
		cust.setEmail("custNoDisplay@example.com");
		cust.setPassword("x");
		// displayName is null
		cust.setRole(userRole);
		userRepository.saveAndFlush(cust);

		// Search by username should still work
		var pageByUsername = service.searchCustomerAccounts("custNoDisplay", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageByUsername.getContent()).extracting(com.qminh.apartment.dto.user.UserRes::getUsername).contains("custNoDisplay");
		assertThat(pageByUsername.getContent()).hasSize(1);

		// Search by email should still work
		var pageByEmail = service.searchCustomerAccounts("custNoDisplay@example.com", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageByEmail.getContent()).extracting(com.qminh.apartment.dto.user.UserRes::getEmail).contains("custNoDisplay@example.com");
		assertThat(pageByEmail.getContent()).hasSize(1);
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



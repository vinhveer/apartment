package com.qminh.apartment.service;

import com.qminh.apartment.dto.account.AdminCreateReq;
import com.qminh.apartment.dto.account.SaleCreateReq;
import com.qminh.apartment.entity.Role;
import com.qminh.apartment.repository.PropertySaleInfoRepository;
import com.qminh.apartment.repository.RoleRepository;
import com.qminh.apartment.repository.UserRepository;
import com.qminh.apartment.service.impl.AccountService;
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
class AccountServiceTest extends PostgresTestContainer {

	@Autowired private AccountService service;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;
	@Autowired private PropertySaleInfoRepository saleInfoRepository;

	@BeforeEach
	void ensureRoles() {
		if (roleRepository.findByRoleName("ADMIN").isEmpty()) {
			Role r = new Role(); r.setRoleName("ADMIN"); roleRepository.saveAndFlush(r);
		}
		if (roleRepository.findByRoleName("SALE").isEmpty()) {
			Role r = new Role(); r.setRoleName("SALE"); roleRepository.saveAndFlush(r);
		}
	}

	@Test
	@Transactional
	@DisplayName("createSale creates user and sale info")
	void create_sale() {
		SaleCreateReq req = new SaleCreateReq();
		req.setUsername("saleT");
		req.setEmail("saleT@example.com");
		req.setPassword("123456");
		req.setDisplayName("Sale T");
		req.setFullName("Sale Tester");
		req.setPhone("0900000999");
		var res = service.createSale(req);
		assertThat(res.getUsername()).isEqualTo("saleT");
		var u = userRepository.findByUsername("saleT").orElseThrow();
		assertThat(saleInfoRepository.findByUserId(u.getId())).isPresent();
	}

	@Test
	@Transactional
	@DisplayName("createAdmin creates admin user")
	void create_admin() {
		AdminCreateReq req = new AdminCreateReq();
		req.setUsername("adminT");
		req.setEmail("adminT@example.com");
		req.setPassword("123456");
		req.setDisplayName("Admin T");
		var res = service.createAdmin(req);
		assertThat(res.getUsername()).isEqualTo("adminT");
		assertThat(userRepository.findByUsername("adminT")).isPresent();
	}
}



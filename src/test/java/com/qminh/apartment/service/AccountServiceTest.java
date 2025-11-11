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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.qminh.apartment.exception.ConflictException;

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

	@Test
	@Transactional
	@DisplayName("createSale duplicate username/email -> ConflictException (409)")
	void create_sale_duplicate_conflict() {
		SaleCreateReq req1 = new SaleCreateReq();
		req1.setUsername("dupSale");
		req1.setEmail("dupSale@example.com");
		req1.setPassword("123456");
		req1.setDisplayName("Dup Sale");
		req1.setFullName("Dup S");
		req1.setPhone("0900");
		service.createSale(req1);
		SaleCreateReq req2 = new SaleCreateReq();
		req2.setUsername("dupSale");
		req2.setEmail("dupSale@example.com");
		req2.setPassword("123456");
		req2.setDisplayName("Dup Sale 2");
		req2.setFullName("Dup S2");
		req2.setPhone("0901");
		assertThatThrownBy(() -> service.createSale(req2)).isInstanceOf(ConflictException.class);
	}

	@Test
	@Transactional
	@DisplayName("createAdmin duplicate username/email -> ConflictException (409)")
	void create_admin_duplicate_conflict() {
		AdminCreateReq a1 = new AdminCreateReq();
		a1.setUsername("dupAdmin");
		a1.setEmail("dupAdmin@example.com");
		a1.setPassword("123456");
		a1.setDisplayName("Dup Admin");
		service.createAdmin(a1);
		AdminCreateReq a2 = new AdminCreateReq();
		a2.setUsername("dupAdmin");
		a2.setEmail("dupAdmin@example.com");
		a2.setPassword("123456");
		a2.setDisplayName("Dup Admin 2");
		assertThatThrownBy(() -> service.createAdmin(a2)).isInstanceOf(ConflictException.class);
	}
}



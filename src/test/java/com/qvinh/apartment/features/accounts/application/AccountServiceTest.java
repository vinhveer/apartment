package com.qvinh.apartment.features.accounts.application;

import com.qvinh.apartment.features.accounts.application.impl.AccountService;
import com.qvinh.apartment.features.accounts.domain.Role;
import com.qvinh.apartment.features.accounts.dto.account.AccountCreateReq;
import com.qvinh.apartment.features.accounts.dto.user.UserRoleUpdateReq;
import com.qvinh.apartment.features.accounts.dto.user.UserUpdateReq;
import com.qvinh.apartment.features.accounts.persistence.PropertySaleInfoRepository;
import com.qvinh.apartment.features.accounts.persistence.RoleRepository;
import com.qvinh.apartment.features.accounts.persistence.UserRepository;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
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
import com.qvinh.apartment.shared.exception.ConflictException;

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
		if (roleRepository.findByRoleName("USER").isEmpty()) {
			Role r = new Role(); r.setRoleName("USER"); roleRepository.saveAndFlush(r);
		}
	}

	@Test
	@Transactional
	@DisplayName("createEmployeeAccount SALE creates user and sale info")
	void create_sale() {
		AccountCreateReq req = new AccountCreateReq();
		req.setUsername("saleT");
		req.setEmail("saleT@example.com");
		req.setPassword("123456");
		req.setDisplayName("Sale T");
		req.setFullName("Sale Tester");
		req.setPhone("0900000999");
		req.setRoleName("SALE");
		var res = service.createEmployeeAccount(req);
		assertThat(res.getUsername()).isEqualTo("saleT");
		var u = userRepository.findByUsername("saleT").orElseThrow();
		assertThat(saleInfoRepository.findByUserId(java.util.Objects.requireNonNull(u.getId()))).isPresent();
	}

	@Test
	@Transactional
	@DisplayName("createEmployeeAccount ADMIN creates admin user and sale info")
	void create_admin() {
		AccountCreateReq req = new AccountCreateReq();
		req.setUsername("adminT");
		req.setEmail("adminT@example.com");
		req.setPassword("123456");
		req.setDisplayName("Admin T");
		req.setFullName("Admin Tester");
		req.setPhone("0900000123");
		req.setRoleName("ADMIN");
		var res = service.createEmployeeAccount(req);
		assertThat(res.getUsername()).isEqualTo("adminT");
		var u = userRepository.findByUsername("adminT").orElseThrow();
		assertThat(saleInfoRepository.findByUserId(java.util.Objects.requireNonNull(u.getId()))).isPresent();
	}

	@Test
	@Transactional
	@DisplayName("createEmployeeAccount duplicate username/email -> ConflictException (409)")
	void create_sale_duplicate_conflict() {
		AccountCreateReq req1 = new AccountCreateReq();
		req1.setUsername("dupSale");
		req1.setEmail("dupSale@example.com");
		req1.setPassword("123456");
		req1.setDisplayName("Dup Sale");
		req1.setFullName("Dup S");
		req1.setPhone("0900");
		req1.setRoleName("SALE");
		service.createEmployeeAccount(req1);
		AccountCreateReq req2 = new AccountCreateReq();
		req2.setUsername("dupSale");
		req2.setEmail("dupSale@example.com");
		req2.setPassword("123456");
		req2.setDisplayName("Dup Sale 2");
		req2.setFullName("Dup S2");
		req2.setPhone("0901");
		req2.setRoleName("SALE");
		assertThatThrownBy(() -> service.createEmployeeAccount(req2)).isInstanceOf(ConflictException.class);
	}

	@Test
	@Transactional
	@DisplayName("createEmployeeAccount ADMIN duplicate username/email -> ConflictException (409)")
	void create_admin_duplicate_conflict() {
		AccountCreateReq a1 = new AccountCreateReq();
		a1.setUsername("dupAdmin");
		a1.setEmail("dupAdmin@example.com");
		a1.setPassword("123456");
		a1.setDisplayName("Dup Admin");
		a1.setFullName("Dup Admin Full");
		a1.setPhone("0900");
		a1.setRoleName("ADMIN");
		service.createEmployeeAccount(a1);
		AccountCreateReq a2 = new AccountCreateReq();
		a2.setUsername("dupAdmin");
		a2.setEmail("dupAdmin@example.com");
		a2.setPassword("123456");
		a2.setDisplayName("Dup Admin 2");
		a2.setFullName("Dup Admin 2 Full");
		a2.setPhone("0901");
		a2.setRoleName("ADMIN");
		assertThatThrownBy(() -> service.createEmployeeAccount(a2)).isInstanceOf(ConflictException.class);
	}

	@Test
	@Transactional
	@DisplayName("searchEmployeeAccounts returns only ADMIN/SALE and supports keyword")
	void search_employee_accounts() {
		// create employees via service
		AccountCreateReq sale = new AccountCreateReq();
		sale.setUsername("saleSearch");
		sale.setEmail("saleSearch@example.com");
		sale.setPassword("123456");
		sale.setDisplayName("Sale Search");
		sale.setFullName("Sale S");
		sale.setPhone("0900");
		sale.setRoleName("SALE");
		service.createEmployeeAccount(sale);

		AccountCreateReq admin = new AccountCreateReq();
		admin.setUsername("adminSearch2");
		admin.setEmail("adminSearch2@example.com");
		admin.setPassword("123456");
		admin.setDisplayName("Admin Search 2");
		admin.setFullName("Admin Search 2 Full");
		admin.setPhone("0901");
		admin.setRoleName("ADMIN");
		service.createEmployeeAccount(admin);

		var pageAll = service.searchEmployeeAccounts(null, org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageAll.getContent()).allMatch(u -> u.getRoleName().equals("ADMIN") || u.getRoleName().equals("SALE"));

		var pageFiltered = service.searchEmployeeAccounts("saleSearch", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageFiltered.getContent()).extracting(com.qvinh.apartment.features.accounts.dto.user.UserRes::getUsername).contains("saleSearch");
	}

	@Test
	@Transactional
	@DisplayName("searchEmployeeAccounts searches by username, email, and displayName")
	void search_employee_accounts_by_multiple_fields() {
		// Create employees with different searchable fields
		AccountCreateReq sale1 = new AccountCreateReq();
		sale1.setUsername("saleUser1");
		sale1.setEmail("sale1@example.com");
		sale1.setPassword("123456");
		sale1.setDisplayName("John Doe");
		sale1.setFullName("John Doe Full");
		sale1.setPhone("0900");
		sale1.setRoleName("SALE");
		service.createEmployeeAccount(sale1);

		AccountCreateReq sale2 = new AccountCreateReq();
		sale2.setUsername("saleUser2");
		sale2.setEmail("jane.smith@example.com");
		sale2.setPassword("123456");
		sale2.setDisplayName("Jane Smith");
		sale2.setFullName("Jane Smith Full");
		sale2.setPhone("0901");
		sale2.setRoleName("SALE");
		service.createEmployeeAccount(sale2);

		AccountCreateReq admin1 = new AccountCreateReq();
		admin1.setUsername("adminUser1");
		admin1.setEmail("admin1@example.com");
		admin1.setPassword("123456");
		admin1.setDisplayName("Admin Manager");
		admin1.setFullName("Admin Manager Full");
		admin1.setPhone("0902");
		admin1.setRoleName("ADMIN");
		service.createEmployeeAccount(admin1);

		// Search by username
		var pageByUsername = service.searchEmployeeAccounts("saleUser1", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageByUsername.getContent()).extracting(com.qvinh.apartment.features.accounts.dto.user.UserRes::getUsername).contains("saleUser1");
		assertThat(pageByUsername.getContent()).hasSize(1);

		// Search by email
		var pageByEmail = service.searchEmployeeAccounts("jane.smith@example.com", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageByEmail.getContent()).extracting(com.qvinh.apartment.features.accounts.dto.user.UserRes::getEmail).contains("jane.smith@example.com");
		assertThat(pageByEmail.getContent()).hasSize(1);

		// Search by displayName
		var pageByDisplayName = service.searchEmployeeAccounts("John Doe", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageByDisplayName.getContent()).extracting(com.qvinh.apartment.features.accounts.dto.user.UserRes::getDisplayName).contains("John Doe");
		assertThat(pageByDisplayName.getContent()).hasSize(1);

		// Search by partial displayName
		var pageByPartialDisplayName = service.searchEmployeeAccounts("Manager", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageByPartialDisplayName.getContent()).extracting(com.qvinh.apartment.features.accounts.dto.user.UserRes::getDisplayName).contains("Admin Manager");
		assertThat(pageByPartialDisplayName.getContent()).hasSize(1);

		// Search by partial email
		var pageByPartialEmail = service.searchEmployeeAccounts("example.com", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageByPartialEmail.getContent()).hasSizeGreaterThanOrEqualTo(3); // Should find all employees

		// Search with no results
		var pageNoResults = service.searchEmployeeAccounts("nonexistent", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageNoResults.getContent()).isEmpty();
	}

	@Test
	@Transactional
	@DisplayName("searchEmployeeAccounts handles null displayName correctly")
	void search_employee_accounts_with_null_displayName() {
		// Create employee without displayName
		AccountCreateReq sale = new AccountCreateReq();
		sale.setUsername("saleNoDisplay");
		sale.setEmail("saleNoDisplay@example.com");
		sale.setPassword("123456");
		// displayName is null
		sale.setFullName("Sale No Display");
		sale.setPhone("0900");
		sale.setRoleName("SALE");
		service.createEmployeeAccount(sale);

		// Search by username should still work
		var pageByUsername = service.searchEmployeeAccounts("saleNoDisplay", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageByUsername.getContent()).extracting(com.qvinh.apartment.features.accounts.dto.user.UserRes::getUsername).contains("saleNoDisplay");
		assertThat(pageByUsername.getContent()).hasSize(1);

		// Search by email should still work
		var pageByEmail = service.searchEmployeeAccounts("saleNoDisplay@example.com", org.springframework.data.domain.PageRequest.of(0, 10));
		assertThat(pageByEmail.getContent()).extracting(com.qvinh.apartment.features.accounts.dto.user.UserRes::getEmail).contains("saleNoDisplay@example.com");
		assertThat(pageByEmail.getContent()).hasSize(1);
	}

	@Test
	@Transactional
	@DisplayName("editEmployeeAccount updates email/displayName")
	void edit_employee_account() {
		AccountCreateReq sale = new AccountCreateReq();
		sale.setUsername("saleEdit");
		sale.setEmail("saleEdit@example.com");
		sale.setPassword("123456");
		sale.setDisplayName("Sale Edit");
		sale.setFullName("Sale E");
		sale.setPhone("0900");
		sale.setRoleName("SALE");
		var created = service.createEmployeeAccount(sale);

		UserUpdateReq up = new UserUpdateReq();
		up.setEmail("saleEdit+new@example.com");
		up.setDisplayName("Sale Edit New");

		var res = service.editEmployeeAccount(created.getId(), up);
		assertThat(res.getEmail()).isEqualTo("saleEdit+new@example.com");
		assertThat(res.getDisplayName()).isEqualTo("Sale Edit New");
	}

	@Test
	@Transactional
	@DisplayName("deleteEmployeeAccount removes user and sale info for SALE")
	void delete_employee_account_sale() {
		AccountCreateReq sale = new AccountCreateReq();
		sale.setUsername("saleDel");
		sale.setEmail("saleDel@example.com");
		sale.setPassword("123456");
		sale.setDisplayName("Sale Del");
		sale.setFullName("Sale D");
		sale.setPhone("0900");
		sale.setRoleName("SALE");
		var created = service.createEmployeeAccount(sale);

		Long id = java.util.Objects.requireNonNull(created.getId());
		service.deleteEmployeeAccount(id);
		assertThat(userRepository.findById(id)).isNotPresent();
		assertThat(saleInfoRepository.findByUserId(id)).isNotPresent();
	}

	@Test
	@Transactional
	@DisplayName("changeEmployeeRole ADMIN <-> SALE manages sale info correctly")
	void change_employee_role_flow() {
		// start as ADMIN
		AccountCreateReq admin = new AccountCreateReq();
		admin.setUsername("roleUser");
		admin.setEmail("roleUser@example.com");
		admin.setPassword("123456");
		admin.setDisplayName("Role User");
		admin.setFullName("Role User Full");
		admin.setPhone("0900");
		admin.setRoleName("ADMIN");
		var created = service.createEmployeeAccount(admin);

		UserRoleUpdateReq toSale = new UserRoleUpdateReq();
		toSale.setRoleName("SALE");
		toSale.setFullName("Sale Role");
		toSale.setPhone("0900");
		Long id = java.util.Objects.requireNonNull(created.getId());
		var saleRes = service.changeEmployeeRole(id, toSale);
		assertThat(saleRes.getRoleName()).isEqualTo("SALE");
		assertThat(saleInfoRepository.findByUserId(id)).isPresent();

		UserRoleUpdateReq toAdmin = new UserRoleUpdateReq();
		toAdmin.setRoleName("ADMIN");
		var adminRes = service.changeEmployeeRole(id, toAdmin);
		assertThat(adminRes.getRoleName()).isEqualTo("ADMIN");
		// sale info is kept for ADMIN as well
		assertThat(saleInfoRepository.findByUserId(id)).isPresent();
	}

	@Test
	@Transactional
	@DisplayName("changeEmployeeRole ADMIN -> SALE without fullName/phone auto-backfills from displayName/username")
	void change_admin_to_sale_without_fullname_phone() {
		// start as ADMIN
		AccountCreateReq admin = new AccountCreateReq();
		admin.setUsername("adminBackfill");
		admin.setEmail("adminBackfill@example.com");
		admin.setPassword("123456");
		admin.setDisplayName("Admin Backfill");
		admin.setFullName("Admin B Full");
		admin.setPhone("0900");
		admin.setRoleName("ADMIN");
		var created = service.createEmployeeAccount(admin);
		Long id = java.util.Objects.requireNonNull(created.getId());

		// Change to SALE without fullName/phone - should backfill from displayName
		UserRoleUpdateReq toSale = new UserRoleUpdateReq();
		toSale.setRoleName("SALE");
		// fullName and phone are null/not set
		var saleRes = service.changeEmployeeRole(id, toSale);
		assertThat(saleRes.getRoleName()).isEqualTo("SALE");
		var saleInfo = saleInfoRepository.findByUserId(id).orElseThrow();
		assertThat(saleInfo.getFullName()).isEqualTo("Admin Backfill"); // backfilled from displayName
		assertThat(saleInfo.getPhone()).isEqualTo("N/A"); // backfilled default
	}

	@Test
	@Transactional
	@DisplayName("changeEmployeeRole ADMIN -> SALE without displayName backfills from username")
	void change_admin_to_sale_backfill_from_username() {
		// start as ADMIN without displayName
		AccountCreateReq admin = new AccountCreateReq();
		admin.setUsername("adminNoDisplay");
		admin.setEmail("adminNoDisplay@example.com");
		admin.setPassword("123456");
		// displayName is null
		admin.setFullName("Admin No Display Full");
		admin.setPhone("0900");
		admin.setRoleName("ADMIN");
		var created = service.createEmployeeAccount(admin);
		Long id = java.util.Objects.requireNonNull(created.getId());

		// Change to SALE without fullName/phone - should backfill from username
		UserRoleUpdateReq toSale = new UserRoleUpdateReq();
		toSale.setRoleName("SALE");
		var saleRes = service.changeEmployeeRole(id, toSale);
		assertThat(saleRes.getRoleName()).isEqualTo("SALE");
		var saleInfo = saleInfoRepository.findByUserId(id).orElseThrow();
		assertThat(saleInfo.getFullName()).isEqualTo("adminNoDisplay"); // backfilled from username
		assertThat(saleInfo.getPhone()).isEqualTo("N/A"); // backfilled default
	}

	@Test
	@Transactional
	@DisplayName("changeEmployeeRole ADMIN -> SALE with fullName/phone uses provided values")
	void change_admin_to_sale_with_provided_values() {
		// start as ADMIN
		AccountCreateReq admin = new AccountCreateReq();
		admin.setUsername("adminWithValues");
		admin.setEmail("adminWithValues@example.com");
		admin.setPassword("123456");
		admin.setDisplayName("Admin With Values");
		admin.setFullName("Admin Original");
		admin.setPhone("0900");
		admin.setRoleName("ADMIN");
		var created = service.createEmployeeAccount(admin);
		Long id = java.util.Objects.requireNonNull(created.getId());

		// Change to SALE with provided fullName/phone
		UserRoleUpdateReq toSale = new UserRoleUpdateReq();
		toSale.setRoleName("SALE");
		toSale.setFullName("New Sale Name");
		toSale.setPhone("0901234567");
		var saleRes = service.changeEmployeeRole(id, toSale);
		assertThat(saleRes.getRoleName()).isEqualTo("SALE");
		var saleInfo = saleInfoRepository.findByUserId(id).orElseThrow();
		assertThat(saleInfo.getFullName()).isEqualTo("New Sale Name"); // uses provided value
		assertThat(saleInfo.getPhone()).isEqualTo("0901234567"); // uses provided value
	}
}

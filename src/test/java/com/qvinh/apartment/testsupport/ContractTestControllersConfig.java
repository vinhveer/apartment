package com.qvinh.apartment.testsupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("contract")
@TestConfiguration
@Import(ContractTestControllersConfig.BoomController.class)
public class ContractTestControllersConfig {

	@RestController
	static class BoomController {
		@GetMapping("/__contract/boom")
		public String boom() {
			throw new RuntimeException("boom");
		}
	}
}


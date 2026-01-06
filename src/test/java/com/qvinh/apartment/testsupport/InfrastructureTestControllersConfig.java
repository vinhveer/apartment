package com.qvinh.apartment.testsupport;

import org.slf4j.MDC;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Min;

@TestConfiguration
@Import({
	InfrastructureTestControllersConfig.InfraMdcController.class,
	InfrastructureTestControllersConfig.InfraValidationController.class,
})
public class InfrastructureTestControllersConfig {

	@RestController
	@RequestMapping("/__infra")
	static class InfraMdcController {
		@GetMapping("/mdc")
		public String mdc() {
			return String.valueOf(MDC.get("requestId"));
		}
	}

	@Validated
	@RestController
	@RequestMapping("/__infra/validate")
	static class InfraValidationController {
		@GetMapping("/missing")
		public String missingParam(@RequestParam String q) {
			return q;
		}

		@GetMapping("/constraint")
		public String constraint(@RequestParam @Min(1) int n) {
			return String.valueOf(n);
		}
	}
}


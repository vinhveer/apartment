package com.qvinh.apartment;

import com.qvinh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApartmentApplicationTests extends PostgresTestContainer {

	@Test
	void contextLoads() {
		// Verify Spring context boots with containerized DB
	}
}

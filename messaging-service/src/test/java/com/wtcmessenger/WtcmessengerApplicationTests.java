package com.wtcmessenger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"JWT_SECRET=dummy_secret_for_tests",
		"MONGODB_URI=mongodb://localhost:27017/test_db",
		"KAFKA_BOOTSTRAP_SERVERS=localhost:9092"
})
class WtcmessengerApplicationTests {

	@Test
	void contextLoads() {
	}

}

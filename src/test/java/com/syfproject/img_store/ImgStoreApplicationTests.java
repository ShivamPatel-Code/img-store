package com.syfproject.img_store;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Temporarily disabled until Kafka is configured for tests")
@SpringBootTest(properties = {"kafka.enabled=false"})
public class ImgStoreApplicationTests {

	@Test
	void contextLoads() {
	}
}

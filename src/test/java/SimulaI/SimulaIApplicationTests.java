package SimulaI;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import SimulaI.config.TestVectorStoreConfig;

@SpringBootTest
@Import(TestVectorStoreConfig.class)
class SimulaIApplicationTests {

	@Test
	void contextLoads() {
	}

}

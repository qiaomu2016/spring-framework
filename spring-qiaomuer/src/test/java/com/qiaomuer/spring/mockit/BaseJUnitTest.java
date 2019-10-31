package com.qiaomuer.spring.mockit;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;

/**
 * <p>
 * mockit+junit+spring
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-test.xml"}, inheritLocations = true)
@TestExecutionListeners({ServletTestExecutionListener.class, DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class, InitSpringProfilesExecutionListener.class})
@ActiveProfiles(value = "test")
public class BaseJUnitTest {

	/**
	 *  初始化mock
	 */
	@Before
	public void setup() {
		// 如果同时需要注入和mock注入，SpringJUnit4ClassRunner的前提下，注入mock
		MockitoAnnotations.initMocks(this);
	}
}
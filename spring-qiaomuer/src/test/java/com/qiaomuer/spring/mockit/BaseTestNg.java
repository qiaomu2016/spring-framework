package com.qiaomuer.spring.mockit;

import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.testng.annotations.BeforeMethod;

/**
 * <p>
 * mockit+testng+spring
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
@ContextConfiguration(locations = {"classpath:applicationContext-test.xml"}, inheritLocations = true)
@TestExecutionListeners({ServletTestExecutionListener.class, DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class, InitSpringProfilesExecutionListener.class})
@ActiveProfiles(value = "test")
public class BaseTestNg extends AbstractTestNGSpringContextTests {

	/**
	 * 初始化mock,由于这里使用的是testNg进行测试，只能显示初始化，没有提供testNg相关的runner
	 */
	@BeforeMethod
	public void init() {
		// 如果同时需要注入和mock注入，SpringJUnit4ClassRunner的前提下，注入mock
		MockitoAnnotations.initMocks(this);
	}


}
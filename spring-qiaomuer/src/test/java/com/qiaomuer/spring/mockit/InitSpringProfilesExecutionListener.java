package com.qiaomuer.spring.mockit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * 继承并实现org.springframework.test.context.support.AbstractTestExecutionListener，
 * 在执行测试类之前和结束时，按 用户设定执行相应的数据操作。
 * <p>
 * 用法如下：@TestExecutionListeners({DbUnitTestExecutionListener.class })
 * </p>
 */
public class InitSpringProfilesExecutionListener extends AbstractTestExecutionListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(InitSpringProfilesExecutionListener.class);

	private static final String SPRING_PROFILES_ACTIVE = "spring.profiles.active";

	/**
	 * 执行测试类之前，先执行定义的初始化数据操作
	 *
	 * @param testContext TestContext，spring框架测试上下文
	 * @throws Exception 任何异常直接抛出，数据源连不上、测试文件找不到、格式非法之类异常，将直接导致测试失败
	 */
	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		try {
			Class<?> testClass = testContext.getTestClass();
			ActiveProfiles activeProfiles = testClass.getAnnotation(ActiveProfiles.class);
			if (activeProfiles != null) {
				String[] value = activeProfiles.value();
				System.setProperty(SPRING_PROFILES_ACTIVE, value[0]);
			}
			LOGGER.info("beforeTestClass called with [" + testContext + "].");
		} catch (RuntimeException e) {
			// 释放资源
		}
	}

}
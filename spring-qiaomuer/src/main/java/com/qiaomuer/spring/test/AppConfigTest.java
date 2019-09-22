package com.qiaomuer.spring.test;

import com.qiaomuer.spring.AppConfig;
import com.qiaomuer.spring.dao.impl.UserDao;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * <p>
 * 类详细描述
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
public class AppConfigTest {

	public static void main(String[] args) {

		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
		UserDao userDao = applicationContext.getBean(UserDao.class);
		userDao.validate();

	}
}

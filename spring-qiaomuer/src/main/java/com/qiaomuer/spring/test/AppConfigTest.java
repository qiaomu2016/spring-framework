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
//		// 注：如果需要根据环境来加载不同的类，那么需要使用下面这种方式：先设置所属环境，再加载类
//		// 因为上面 new AnnotationConfigApplicationContext(RootConfig.class) 这种方式，是已经扫描加载了所有的类了，环境变量将不再起作用
//		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
//		// 设置环境
//		applicationContext.getEnvironment().setActiveProfiles("dev");
//
//		// 可以注入一个配置类，配置类上会加入注释扫描：@ComponentScan
//		applicationContext.register(AppConfig.class);
//		// 也可以注入单个类
//		applicationContext.register(UserDao.class);
//		applicationContext.scan("com.qiaomuer.spring");
//		applicationContext.refresh();

		UserDao userDao = applicationContext.getBean(UserDao.class);
		userDao.validate();

	}
}

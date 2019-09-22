package com.qiaomuer.spring;

import com.qiaomuer.spring.dao.impl.AccountDao;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * <p>
 * 类详细描述
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
@Configuration
@ComponentScan("com.qiaomuer.spring")
@Import(AccountDao.class)
public class AppConfig {
}

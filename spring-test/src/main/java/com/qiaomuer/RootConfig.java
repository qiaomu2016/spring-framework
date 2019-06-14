package com.qiaomuer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 类详细描述
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
@Configuration
public class RootConfig {

	@Bean
	public RootBean parent() {
		return new RootBean();
	}
}

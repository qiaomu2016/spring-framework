package com.qiaomuer.spring.proxy;

import org.springframework.cglib.proxy.FixedValue;

/**
 * <p>
 * 类详细描述
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
public class ConcreteClassFixedValue implements FixedValue {
	@Override
	public Object loadObject() throws Exception {
		System.out.println("ConcreteClassFixedValue loadObject ...");
		Object object = 999;
		return object;
	}
}

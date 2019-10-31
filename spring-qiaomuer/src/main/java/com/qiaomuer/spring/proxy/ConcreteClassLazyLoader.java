package com.qiaomuer.spring.proxy;

import org.springframework.cglib.proxy.LazyLoader;

/**
 * <p>
 * 类详细描述
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
public class ConcreteClassLazyLoader implements LazyLoader {
	@Override
	public Object loadObject() throws Exception {
		System.out.println("LazyLoader loadObject() ...");
		PropertyBean bean = new PropertyBean();
		bean.setPropertyName("lazy-load object propertyName!");
		bean.setPropertyValue(11);
		return bean;
	}
}

package com.qiaomuer.spring.proxy;

import org.springframework.cglib.proxy.Dispatcher;

/**
 * <p>
 * 类详细描述
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
public class ConcreteClassDispatcher implements Dispatcher {

	@Override
	public Object loadObject() throws Exception {
		System.out.println("Dispatcher loadObject ...");
		PropertyBean object = new PropertyBean();
		object.setPropertyName("PropertyBeanName!");
		object.setPropertyValue(1);
		return object;
	}
}

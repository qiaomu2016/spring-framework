package com.qiaomuer.spring.proxy;

import org.springframework.cglib.proxy.Enhancer;

/**
 * <p>
 * 类详细描述
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
public class DispatcherBean {
	private String name;
	private String value;
	private PropertyBean propertyBean;

	public DispatcherBean() {
		this.name = "DispatcherBean";
		this.value = "abc";
		this.propertyBean = createDispatcherBean();
	}

	protected PropertyBean createDispatcherBean() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(PropertyBean.class);
		return (PropertyBean) Enhancer.create(PropertyBean.class, new ConcreteClassDispatcher());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public PropertyBean getPropertyBean() {
		return propertyBean;
	}

	public void setPropertyBean(PropertyBean propertyBean) {
		this.propertyBean = propertyBean;
	}
}

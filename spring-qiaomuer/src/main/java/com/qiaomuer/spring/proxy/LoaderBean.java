package com.qiaomuer.spring.proxy;

import org.springframework.cglib.proxy.Enhancer;

/**
 * <p>
 * 类详细描述
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
public class LoaderBean {
	private String loaderName;
	private int loaderValue;
	private PropertyBean propertyBean;

	public LoaderBean() {
		this.loaderName = "loaderNameA";
		this.loaderValue = 123;
		this.propertyBean = createPropertyBean();
	}

	protected PropertyBean createPropertyBean() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(PropertyBean.class);
		return (PropertyBean) Enhancer.create(PropertyBean.class, new ConcreteClassLazyLoader());
	}

	public String getLoaderName() {
		return loaderName;
	}

	public void setLoaderName(String loaderName) {
		this.loaderName = loaderName;
	}

	public int getLoaderValue() {
		return loaderValue;
	}

	public void setLoaderValue(int loaderValue) {
		this.loaderValue = loaderValue;
	}

	public PropertyBean getPropertyBean() {
		return propertyBean;
	}

	public void setPropertyBean(PropertyBean propertyBean) {
		this.propertyBean = propertyBean;
	}
}

package com.qiaomuer.spring.proxy;


import org.springframework.cglib.proxy.*;

import java.lang.reflect.Method;

/**
 * <p>
 * 类详细描述
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
public class ConcreteClassInterceptor implements MethodInterceptor {

	/**
	 *
	 * @param o 由CGLib动态生成的代理类实例
	 * @param method 实体类所调用的被代理的方法引用
	 * @param objects 参数值列表
	 * @param methodProxy 生成的代理类对方法的代理引用
	 * @return 从代理实例的方法调用返回的值
	 * @throws Throwable
	 */
	@Override
	public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
		System.out.println("Before:" + method);
		// 调用代理类实例上的proxy方法的父类方法（即实体类ConcreteClassNoInterface中对应的方法）
		Object object = methodProxy.invokeSuper(o, objects);
		System.out.println("After:" + method);
		return object;
	}
}

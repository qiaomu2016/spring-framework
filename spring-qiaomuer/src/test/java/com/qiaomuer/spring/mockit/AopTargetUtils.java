package com.qiaomuer.spring.mockit;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;

/**
 * <p>
 * 类详细描述
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
public class AopTargetUtils {

	/**
	 * 获取 目标对象
	 *
	 * @param proxy 代理对象
	 * @return
	 * @throws Exception
	 */
	public static Object getTarget(Object proxy) throws Exception {
		System.out.println("proxy.getClass():" + proxy.getClass());
		if (!AopUtils.isAopProxy(proxy)) {
			System.out.println("不是代理对象");
			return proxy;// 不是代理对象
		}

		if (AopUtils.isJdkDynamicProxy(proxy)) {
			return getJdkDynamicProxyTargetObject(proxy);
		} else { // cglib
			return getCglibProxyTargetObject(proxy);
		}
	}

	private static Object getCglibProxyTargetObject(Object proxy) throws Exception {
		System.out.println("当前代理模式:cglib代理");
		Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
		h.setAccessible(true);
		Object dynamicAdvisedInterceptor = h.get(proxy);

		Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
		advised.setAccessible(true);

		return ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
	}

	private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
		System.out.println("当前代理模式:jdk动态代理");
		Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
		h.setAccessible(true);
		AopProxy aopProxy = (AopProxy) h.get(proxy);

		Field advised = aopProxy.getClass().getDeclaredField("advised");
		advised.setAccessible(true);

		return ((AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();
	}
}

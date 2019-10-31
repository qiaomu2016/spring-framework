package com.qiaomuer.spring.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * <p>
 * 类详细描述
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
public class PlanProxy implements InvocationHandler {

	/**
	 * 业务实现类对象，用来调用具体的业务方法
	 */
	private Object target;

	/**
	 * 绑定业务对象并返回一个代理类
	 */
	public Object bind(Object target) {
		// 接收业务实现类对象参数
		this.target = target;
		// 通过反射机制，创建一个代理类对象实例并返回。用户进行方法调用时使用
		// 创建代理对象时，需要传递该业务类的类加载器（用来获取业务实现类的元数据，在包装方法是调用真正的业务方法）、接口、handler实现类
		return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = null;
		System.out.println("方法执行前:权限校验======");
		//调用真正的业务方法
		result = method.invoke(target, args);
		System.out.println("方法执行后:操作日志======");
		return result;
	}
}

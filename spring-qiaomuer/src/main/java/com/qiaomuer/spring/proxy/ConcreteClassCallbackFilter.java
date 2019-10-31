package com.qiaomuer.spring.proxy;

import org.springframework.cglib.proxy.CallbackFilter;

import java.lang.reflect.Method;

/**
 * <p>
 * 类详细描述
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
public class ConcreteClassCallbackFilter implements CallbackFilter {

	// 在CGLib回调时可以设置对不同方法执行不同的回调逻辑，或者根本不执行回调。
	// 其中return值为被代理类的各个方法在回调数组Callback[]中的位置索引

	@Override
	public int accept(Method method) {
		if("getConcreteMethodB".equals(method.getName())){
			//Callback callbacks[0]
			return 0;
		}else if("getConcreteMethodA".equals(method.getName())){
			//Callback callbacks[1]
			return 1;
		}else if("getConcreteMethodFixedValue".equals(method.getName())){
			//Callback callbacks[2]
			return 2;
		}
		return 1;
	}
}

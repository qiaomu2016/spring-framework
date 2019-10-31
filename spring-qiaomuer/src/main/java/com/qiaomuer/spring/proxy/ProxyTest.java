package com.qiaomuer.spring.proxy;

import org.junit.Test;
import org.springframework.cglib.proxy.*;

import java.lang.reflect.Method;

/**
 * <p>
 * 类详细描述
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
public class ProxyTest {

	/**
	 * 测试cglib代理
	 */
	@Test
	public void testProxy() {

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(ConcreteClassNoInterface.class);
		enhancer.setCallback(new ConcreteClassInterceptor());
		ConcreteClassNoInterface proxyObject = (ConcreteClassNoInterface) enhancer.create();
		proxyObject.getConcreteMethodA("methodA");
		proxyObject.getConcreteMethodB(0);

	}


	/**
	 * 过滤器CallbackFilter：在CGLib回调时可以设置对不同方法执行不同的回调逻辑，或者根本不执行回调
	 */
	@Test
	public void testProxyCallbackFilter() {

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(ConcreteClassNoInterface.class);

		//生成代理类前，设置了CallbackFilter，上文中ConcreteClassCallbackFilter实现类的返回值对应Callback[]数组中的位置索引。此处包含了CGLib中的3种回调方式：
		//(1) MethodInterceptor：方法拦截
		//(2) NoOp.INSTANCE：这个NoOp表示no operator，即什么操作也不做，代理类直接调用被代理的方法不进行拦截。
		//(3) FixedValue：表示锁定方法返回值，无论被代理类的方法返回什么值，回调方法都返回固定值。
		CallbackFilter filter = new ConcreteClassCallbackFilter();
		enhancer.setCallbackFilter(filter);

		//(1) Callback
		Callback interceptor = new ConcreteClassInterceptor();
		//(2) Callback
		Callback noOp = NoOp.INSTANCE;
		//(3) Callback
		Callback fixedValue = new ConcreteClassFixedValue();
		Callback[] callbacks = new Callback[]{interceptor, noOp, fixedValue};
		enhancer.setCallbacks(callbacks);

		ConcreteClassNoInterface proxyObject = (ConcreteClassNoInterface) enhancer.create();

		System.out.println("*** NoOp Callback ***");
		proxyObject.getConcreteMethodA("abcde");

		System.out.println("*** MethodInterceptor Callback ***");
		proxyObject.getConcreteMethodB(1);

		System.out.println("*** FixedValue Callback ***");
		int fixed1 = proxyObject.getConcreteMethodFixedValue(128);
		System.out.println("fixedValue1:" + fixed1);
		int fixed2 = proxyObject.getConcreteMethodFixedValue(256);
		System.out.println("fixedValue2:" + fixed2);
	}


	/**
	 * 测试延迟加载
	 */
	@Test
	public void testLazyLoader() {

		// 延迟加载原理：
		// 对需要延迟加载的对象添加代理，在获取该对象属性时先通过代理类回调方法进行对象初始化。
		// 在不需要加载该对象时，只要不去获取该对象内属性，该对象就不会被初始化了
		// （在CGLib的实现中只要去访问该对象内属性的getter方法，就会自动触发代理类回调）

		// LazyLoader接口继承了Callback，因此也算是CGLib中的一种Callback类型。

		LoaderBean loader = new LoaderBean();
		System.out.println(loader.getLoaderName());
		System.out.println(loader.getLoaderValue());
		// 访问延迟加载对象
		PropertyBean propertyBean = loader.getPropertyBean();
		System.out.println(propertyBean.getPropertyName());
		System.out.println(propertyBean.getPropertyValue());
		System.out.println("after...");
		// 当再次访问延迟加载对象时,就不会再执行回调了
		System.out.println(propertyBean.getPropertyName());
	}

	/**
	 * 测试Dispatcher，Dispatcher接口同样继承于Callback，也是一种回调类型
	 * Dispatcher和LazyLoader的区别在于：
	 * LazyLoader只在第一次访问延迟加载属性时触发代理类回调方法
	 * Dispatcher在每次访问延迟加载属性时都会触发代理类回调方法
	 */
	@Test
	public void testDispatcher() {

		// Dispatcher接口同样继承于Callback，也是一种回调类型。
		// 但是Dispatcher和LazyLoader的区别在于：
		// LazyLoader只在第一次访问延迟加载属性时触发代理类回调方法，
		// 而Dispatcher在每次访问延迟加载属性时都会触发代理类回调方法。

		DispatcherBean dispatcherBean = new DispatcherBean();
		System.out.println(dispatcherBean.getName());
		System.out.println(dispatcherBean.getValue());

		PropertyBean pb = dispatcherBean.getPropertyBean();
		System.out.println(pb.getPropertyName());
		//在每次访问时都要进行回调
		System.out.println(pb.getPropertyValue());
	}

	/**
	 * InterfaceMaker会动态生成一个接口，该接口包含指定类定义的所有方法。
	 */
	@Test
	public void testInterfaceMaker() throws Exception {

		InterfaceMaker im = new InterfaceMaker();
		im.add(ConcreteClassNoInterface.class);
		Class interfaceOjb = im.create();
		System.out.println(interfaceOjb.isInterface());
		System.out.println(interfaceOjb.getName());

		Method[] methods = interfaceOjb.getMethods();
		for (Method method : methods) {
			System.out.println(method.getName());
		}

		// 此处让Object生成的代理类实现了由InterfaceMaker生成的接口，但是由于Object类并没有覆写其中的方法，
		// 因此，每当对生成接口内方法进行MethodInterceptor方法拦截时，都返回一个字符串，并在最后打印出来。
		Object obj = Enhancer.create(Object.class, new Class[]{interfaceOjb}, new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				return "intercept!";
			}
		});

		Method method = obj.getClass().getMethod("getConcreteMethodA", new Class[]{String.class});
		System.out.println(method.invoke(obj, new Object[]{"12345"}));
	}


}

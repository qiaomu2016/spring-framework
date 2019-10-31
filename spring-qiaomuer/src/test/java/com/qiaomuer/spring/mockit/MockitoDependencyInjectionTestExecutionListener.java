package com.qiaomuer.spring.mockit;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 类详细描述
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
public class MockitoDependencyInjectionTestExecutionListener extends DependencyInjectionTestExecutionListener {


	/**
	 * 代码的思路是，首先由BaseTest处理好TestCase的依赖注入问题，即示例中@Autowired注解的属性，然后分别针对@Mock、@Spy和@Autowired进行处理，
	 *
	 * 1)@Mock的处理
	 * TestCase中加上@Mock的属性可以是接口也可以是具体实现类，获得属性的类型Class，执行Mock；
	 *
	 * 2)@Spy的处理
	 * TestCase中加上@Spy的属性只能是具体实现类，这里通过属性的名字首先先从容器中获取，返回的Spring Bean有可能是一个AopProxy对象，而我们Spy的目标是AopProxy对象的目标对象，使用Mockito.spy目标对象然后替换；如果不是AopProxy对象， 执行Spy后后面做法与Mock相同；
	 *
	 * 30@Autowired的处理
	 * Mock和Spy之后可能要将结果设置到@Autowired的属性的内部属性中去，同样需要区分@Autowired属性是否是AopProxy对象，将fake后的对象按照属性名字设置到AopProxy目标对象的属性中（有点绕）；
	 */


	private Set<Field> injectFields = new HashSet<>();
	private Map<String, Object> mockObjectMap = new HashMap<>();

	@Override
	protected void injectDependencies(TestContext testContext) throws Exception {
		super.injectDependencies(testContext);
		init(testContext);
	}

	/**
	 * when A dependence on B
	 * mock B or Spy on targetObject of bean get from Spring IoC Container whose type is B.class or beanName is BImpl
	 * @param testContext
	 */
	private void init(TestContext testContext) throws Exception {

		AutowireCapableBeanFactory factory = testContext.getApplicationContext().getAutowireCapableBeanFactory();
		Object bean = testContext.getTestInstance();
		Field[] fields = bean.getClass().getDeclaredFields();

		for (Field field : fields) {
			Annotation[] annotations = field.getAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation instanceof Mock) {
					Class<?> clazz = field.getType();
					Object object = Mockito.mock(clazz);
					field.setAccessible(true);
					field.set(bean, object);
					mockObjectMap.put(field.getName(), object);
				} else if (annotation instanceof Spy) {
					Object fb = factory
							.getBean(field.getName()); //may be a proxy that can not be spy because $Proxy is final
					Object targetSource = AopTargetUtils.getTarget(fb);
					Object spyObject = Mockito.spy(targetSource);
					if (!fb.equals(targetSource)) { //proxy
						if (AopUtils.isJdkDynamicProxy(fb)) {
							setJdkDynamicProxyTargetObject(fb, spyObject);
						} else { //cglib
							setCglibProxyTargetObject(fb, spyObject);
						}
					} else {
						mockObjectMap.put(field.getName(), spyObject);
					}
					field.setAccessible(true);
					field.set(bean, spyObject);
				} else if (annotation instanceof Autowired) {
					injectFields.add(field);
				}
			}
		}
		for (Field field : injectFields) {
			field.setAccessible(true);
			Object fo = field.get(bean);
			if (AopUtils.isAopProxy(fo)) {
				Class targetClass = AopUtils.getTargetClass(fo);
				if (targetClass == null)
					return;
				Object targetSource = AopTargetUtils.getTarget(fo);
				Field[] targetFields = targetClass.getDeclaredFields();
				for (Field targetField : targetFields) {
					targetField.setAccessible(true);
					if (mockObjectMap.get(targetField.getName()) == null) {
						continue;
					}
					ReflectionTestUtils
							.setField(targetSource, targetField.getName(), mockObjectMap.get(targetField.getName()));
				}

			} else {
				Object realObject = factory.getBean(field.getType());
				if (null != realObject) {
					Field[] targetFields = realObject.getClass().getDeclaredFields();
					for (Field targetField : targetFields) {
						targetField.setAccessible(true);
						if (mockObjectMap.get(targetField.getName()) == null) {
							continue;
						}
						ReflectionTestUtils
								.setField(fo, targetField.getName(), mockObjectMap.get(targetField.getName()));
					}
				}
			}
		}
	}

	private void setCglibProxyTargetObject(Object proxy, Object spyObject)
			throws NoSuchFieldException, IllegalAccessException {
		Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
		h.setAccessible(true);
		Object dynamicAdvisedInterceptor = h.get(proxy);
		Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
		advised.setAccessible(true);
		((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).setTarget(spyObject);

	}

	private void setJdkDynamicProxyTargetObject(Object proxy, Object spyObject)
			throws NoSuchFieldException, IllegalAccessException {
		Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
		h.setAccessible(true);
		AopProxy aopProxy = (AopProxy) h.get(proxy);
		Field advised = aopProxy.getClass().getDeclaredField("advised");
		advised.setAccessible(true);
		((AdvisedSupport) advised.get(aopProxy)).setTarget(spyObject);
	}
}

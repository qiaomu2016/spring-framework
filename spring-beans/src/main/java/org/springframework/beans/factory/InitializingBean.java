/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory;

/**
 * Interface to be implemented by beans that need to react once all their properties
 * have been set by a {@link BeanFactory}: e.g. to perform custom initialization,
 * or merely to check that all mandatory properties have been set.
 *
 * <p>An alternative to implementing {@code InitializingBean} is specifying a custom
 * init method, for example in an XML bean definition. For a list of all bean
 * lifecycle methods, see the {@link BeanFactory BeanFactory javadocs}.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see DisposableBean
 * @see org.springframework.beans.factory.config.BeanDefinition#getPropertyValues()
 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getInitMethodName()
 */
public interface InitializingBean {

	/**
	 * Spring 在完成实例化后，设置完所有属性，进行 “Aware 接口” 和 “BeanPostProcessor 前置处理”之后，
	 * 会接着检测当前 bean 对象是否实现了 InitializingBean 接口。
	 * 如果是，则会调用其 #afterPropertiesSet() 方法，进一步调整 bean 实例对象的状态
	 *
	 * 虽然该接口为 Spring 容器的扩展性立下了汗马功劳，但是如果真的让我们的业务对象来实现这个接口就显得不是那么的友好了，
	 * Spring 的一个核心理念就是无侵入性，但是如果我们业务类实现这个接口就显得 Spring 容器具有侵入性了。
	 * 所以 Spring 还提供了另外一种实现的方式：init-method 方法
	 * eg:
	 * <bean id="initializingBeanTest" class="org.springframework.core.test.InitializingBeanTest"
	 *         init-method="setOtherName"/>
	 * 完全可以达到和 InitializingBean 一样的效果，而且在代码中我们没有看到丝毫 Spring 侵入的现象。
	 * 所以通过 init-method 我们可以使用业务对象中定义的任何方法来实现 bean 实例对象的初始化定制化，
	 * 而不再受制于 InitializingBean的 #afterPropertiesSet() 方法。
	 * 同时我们可以使用 <beans> 标签的 default-init-method 属性来统一指定初始化方法，
	 * 这样就省了需要在每个 <bean> 标签中都设置 init-method 这样的繁琐工作了。
	 * 比如在 default-init-method 规定所有初始化操作全部以 initBean() 命名
	 * eg:
	 * <beans default-init-method="initBean"></beans>
	 */

	/**
	 * 小结：
	 * 从 #invokeInitMethods(...) 方法中，我们知道 init-method 指定的方法会在 #afterPropertiesSet() 方法之后执行，
	 * 如果 #afterPropertiesSet() 方法的执行的过程中出现了异常，则 init-method 是不会执行的，
	 * 而且由于 init-method 采用的是反射执行的方式，所以 #afterPropertiesSet() 方法的执行效率一般会高些，
	 * 但是并不能排除我们要优先使用 init-method，主要是因为它消除了 bean 对 Spring 的依赖，Spring 没有侵入到我们业务代码，
	 * 这样会更加符合 Spring 的理念。
	 * 诚然，init-method 是基于 xml 配置文件的，就目前而言，我们的工程几乎都摒弃了配置，
	 * 而采用注释的方式，那么 @PreDestory 可能适合你
	 */

	/**
	 * 该方法在 BeanFactory 设置完了所有属性之后被调用
	 * 该方法允许 bean 实例设置了所有 bean 属性时执行初始化工作，如果该过程出现了错误则需要抛出异常
	 * Invoked by the containing {@code BeanFactory} after it has set all bean properties
	 * and satisfied {@link BeanFactoryAware}, {@code ApplicationContextAware} etc.
	 * <p>This method allows the bean instance to perform validation of its overall
	 * configuration and final initialization when all bean properties have been set.
	 * @throws Exception in the event of misconfiguration (such as failure to set an
	 * essential property) or if initialization fails for any other reason
	 */
	void afterPropertiesSet() throws Exception;

}

/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * Factory hook that allows for custom modification of new bean instances,
 * e.g. checking for marker interfaces or wrapping them with proxies.
 *
 * <p>ApplicationContexts can autodetect BeanPostProcessor beans in their
 * bean definitions and apply them to any beans subsequently created.
 * Plain bean factories allow for programmatic registration of post-processors,
 * applying to all beans created through this factory.
 *
 * <p>Typically, post-processors that populate beans via marker interfaces
 * or the like will implement {@link #postProcessBeforeInitialization},
 * while post-processors that wrap beans with proxies will normally
 * implement {@link #postProcessAfterInitialization}.
 *
 * @author Juergen Hoeller
 * @since 10.10.2003
 * @see InstantiationAwareBeanPostProcessor
 * @see DestructionAwareBeanPostProcessor
 * @see ConfigurableBeanFactory#addBeanPostProcessor
 * @see BeanFactoryPostProcessor
 */
public interface BeanPostProcessor {

	/**
	 * BeanPostProcessor 的作用：
	 * 在 Bean 完成实例化后，如果我们需要对其进行一些配置、增加一些自己的处理逻辑，那么请使用 BeanPostProcessor。
	 *
	 * BeanPostProcessor 可以理解为是 Spring 的一个工厂钩子（其实 Spring 提供一系列的钩子，如 Aware 、InitializingBean、DisposableBean），
	 * 它是 Spring 提供的对象实例化阶段强有力的扩展点，允许 Spring 在实例化 bean 阶段对其进行定制化修改，
	 * 比较常见的使用场景是处理标记接口实现类或者为当前对象提供代理实现（例如 AOP）。
	 *
	 * 一般普通的 BeanFactory 是不支持自动注册 BeanPostProcessor 的，
	 * 因为在 BeanFactory#getBean(...) 方法的过程中根本就没有将我们自定义的 BeanPostProcessor 注入进来，
	 * 所以要想 BeanFactory 容器 的 BeanPostProcessor 生效我们必须手动调用 #addBeanPostProcessor(BeanPostProcessor beanPostProcessor) 方法，
	 * 将定义的 BeanPostProcessor 注册到相应的 BeanFactory 中，
	 * 注册后的 BeanPostProcessor 适用于所有该 BeanFactory 创建的 bean，
	 * 如：factory.addBeanPostProcessor(beanPostProcessorTest);
	 *
	 * 但是 ApplicationContext 可以在其 bean 定义中自动检测所有的 BeanPostProcessor 并自动完成注册，
	 * 同时将他们应用到随后创建的任何 Bean 中。
	 *
	 * ApplicationContext 实现自动注册的原因，在于我们构造一个 ApplicationContext 实例对象的时候会调用
	 * #registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) 方法，
	 * 将检测到的 BeanPostProcessor 注入到 ApplicationContext 容器中，同时应用到该容器创建的 bean 中
	 *
	 *
	 * 总结：
	 * 1.BeanPostProcessor 的作用域是容器级别的，它只和所在的容器相关 ，
	 * 	当 BeanPostProcessor 完成注册后，它会应用于所有跟它在同一个容器内的 bean 。
	 * 2.BeanFactory 和 ApplicationContext 对 BeanPostProcessor 的处理不同，
	 * 	ApplicationContext 会自动检测所有实现了 BeanPostProcessor 接口的 bean，并完成注册，
	 * 	但是使用 BeanFactory 容器时则需要手动调用 AbstractBeanFactory#addBeanPostProcessor(BeanPostProcessor beanPostProcessor) 方法来完成注册
	 * 3.ApplicationContext 的 BeanPostProcessor 支持 Ordered，
	 * 	而 BeanFactory 的 BeanPostProcessor 是不支持的，
	 * 	原因在于ApplicationContext 会对 BeanPostProcessor 进行 Ordered 检测并完成排序，
	 * 	而 BeanFactory 中的 BeanPostProcessor 只跟注册的顺序有关。
	 *
	 */

	/**
	 * Apply this BeanPostProcessor to the given new bean instance <i>before</i> any bean
	 * initialization callbacks (like InitializingBean's {@code afterPropertiesSet}
	 * or a custom init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * <p>The default implementation returns the given {@code bean} as-is.
	 * @param bean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one;
	 * if {@code null}, no subsequent BeanPostProcessors will be invoked
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 */
	@Nullable
	default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * Apply this BeanPostProcessor to the given new bean instance <i>after</i> any bean
	 * initialization callbacks (like InitializingBean's {@code afterPropertiesSet}
	 * or a custom init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * <p>In case of a FactoryBean, this callback will be invoked for both the FactoryBean
	 * instance and the objects created by the FactoryBean (as of Spring 2.0). The
	 * post-processor can decide whether to apply to either the FactoryBean or created
	 * objects or both through corresponding {@code bean instanceof FactoryBean} checks.
	 * <p>This callback will also be invoked after a short-circuiting triggered by a
	 * {@link InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation} method,
	 * in contrast to all other BeanPostProcessor callbacks.
	 * <p>The default implementation returns the given {@code bean} as-is.
	 * @param bean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one;
	 * if {@code null}, no subsequent BeanPostProcessors will be invoked
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 * @see org.springframework.beans.factory.FactoryBean
	 */
	@Nullable
	default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}

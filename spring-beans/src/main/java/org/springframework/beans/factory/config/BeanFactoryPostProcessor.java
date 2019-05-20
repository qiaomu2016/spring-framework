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

/**
 * Allows for custom modification of an application context's bean definitions,
 * adapting the bean property values of the context's underlying bean factory.
 *
 * <p>Application contexts can auto-detect BeanFactoryPostProcessor beans in
 * their bean definitions and apply them before any other beans get created.
 *
 * <p>Useful for custom config files targeted at system administrators that
 * override bean properties configured in the application context.
 *
 * <p>See PropertyResourceConfigurer and its concrete implementations
 * for out-of-the-box solutions that address such configuration needs.
 *
 * <p>A BeanFactoryPostProcessor may interact with and modify bean
 * definitions, but never bean instances. Doing so may cause premature bean
 * instantiation, violating the container and causing unintended side-effects.
 * If bean instance interaction is required, consider implementing
 * {@link BeanPostProcessor} instead.
 *
 * @author Juergen Hoeller
 * @since 06.07.2003
 * @see BeanPostProcessor
 * @see PropertyResourceConfigurer
 */
@FunctionalInterface
public interface BeanFactoryPostProcessor {

	/**
	 *  Spring 容器启动阶段，Spring 也提供了一种容器扩展机制：BeanFactoryPostProcessor，
	 *  该机制作用于容器启动阶段，允许我们在容器实例化 Bean 之前对注册到该容器的 BeanDefinition 做出修改。
	 *
	 *
	 *  #postProcessBeanFactory(...) 方法，工作于 BeanDefinition 加载完成之后，Bean 实例化之前，
	 *  其主要作用是对加载 BeanDefinition 进行修改。
	 *  有一点需要需要注意的是在 #postProcessBeanFactory(...) 方法中，千万不能进行 Bean 的实例化工作，
	 *  因为这样会导致 Bean 过早实例化，会产生严重后果，
	 *  我们始终需要注意的是 BeanFactoryPostProcessor 是与 BeanDefinition 打交道的，
	 *  如果想要与 Bean 打交道，请使用 BeanPostProcessor
	 *
	 *
	 *  与 BeanPostProcessor 一样，BeanFactoryPostProcessor 同样支持排序，
	 *  一个容器可以同时拥有多个 BeanFactoryPostProcessor ，
	 *  这个时候如果我们比较在乎他们的顺序的话，可以实现 Ordered 接口
	 *
	 * 一般情况下我们是不会主动去自定义 BeanFactoryPostProcessor ，
	 * 其实 Spring 为我们提供了几个常用的 BeanFactoryPostProcessor，
	 * 他们是PropertyPlaceholderConfigurer 和 PropertyOverrideConfigurer ，其中
	 * PropertyPlaceholderConfigurer 允许我们在 XML 配置文件中使用占位符并将这些占位符所代表的资源单独配置到简单的 properties 文件中来加载，
	 * PropertyOverrideConfigurer 则允许我们使用占位符来明确表明bean 定义中的 property 与 properties 文件中的各配置项之间的对应关系，
	 * 这两个类在我们大型项目中有非常重要的作用
	 */

	/**
	 * Modify the application context's internal bean factory after its standard
	 * initialization. All bean definitions will have been loaded, but no beans
	 * will have been instantiated yet. This allows for overriding or adding
	 * properties even to eager-initializing beans.
	 * @param beanFactory the bean factory used by the application context
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}

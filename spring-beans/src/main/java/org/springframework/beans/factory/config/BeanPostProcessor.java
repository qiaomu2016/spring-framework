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
 * BeanPostProcessor是Spring框架提供的一个扩展类点（不止一种），通过实现BeanPostProcessor接口，程序员就可插手bean的实例化过程，
 * 从而减轻BeanFactory的负担，值得说明的是这个接口可以配置多个，会形成一个列表，然后依次执行。
 * 比如AOP就是在Bean实例化后期将切面逻辑织入Bean实例中，AOP也正是通过BeanPostProcessor（AbstractAutoProxyCreator）和IOC容器建立起联系
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
	 * 可以插手bean的实例化过程，实例化之后，在bean没有被Spring的bean容器管理之前添加处理逻辑，经典场景：处理bean的生命周期回调，AOP，如@PostConstruct -> CommonAnnotationBeanPostProcessor
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
	 * Spring内置了一些很有用的BeanPostProcessor接口实现类。
	 * 比如有AutowiredAnnotationBeanPostProcessor、RequiredAnnotationBeanPostProcessor、CommonAnnotationBeanPostProcessor、
	 * EventListenerMethodProcessor等。这些Processor会处理各自的场景。
	 * 正是有了这些processor，把bean的构造过程中的一部分功能分配给了这些processor处理，减轻了BeanFactory的负担。
	 * 而且添加一些新的功能也很方便，只需要实现BeanPostProcessor接口，并在实现类上添加@Component注解即可。
	 * 1）ApplicationContextAwareProcessor
	 * Spring容器的refresh方法内部调用prepareBeanFactory方法，prepareBeanFactory方法会添加ApplicationContextAwareProcessor到BeanFactory中。
	 * 这个Processor的作用在于为实现*Aware接口的bean调用该Aware接口定义的方法，并传入对应的参数。
	 * 比如实现EnvironmentAware接口的bean在该Processor内部会调用EnvironmentAware接口的setEnvironment方法，并把Spring容器内部的ConfigurableEnvironment传递进去。
	 *
	 * 2）CommonAnnotationBeanPostProcessor
	 * 在AnnotationConfigUtils类的registerAnnotationConfigProcessors方法中被封装成RootBeanDefinition并注册到Spring容器中。
	 * registerAnnotationConfigProcessors方法在一些比如扫描类的场景下注册。
	 * 比如 context:component-scan 标签或 context:annotation-config 标签的使用，或ClassPathBeanDefinitionScanner扫描器的使用、AnnotatedBeanDefinitionReader读取器的使用。
	 * 主要处理@Resource、@PostConstruct和@PreDestroy注解的实现。
	 *
	 * 3）AutowiredAnnotationBeanPostProcessor
	 * 跟CommonAnnotationBeanPostProcessor一样，在AnnotationConfigUtils类的registerAnnotationConfigProcessors方法被注册到Spring容器中。
	 * 主要处理@Autowired、@Value、@Lookup和@Inject注解的实现，处理逻辑跟CommonAnnotationBeanPostProcessor类似。
	 *
	 * 4）RequiredAnnotationBeanPostProcessor
	 * 跟CommonAnnotationBeanPostProcessor一样，在AnnotationConfigUtils类的registerAnnotationConfigProcessors方法被注册到Spring容器中。
	 * 主要处理@Required注解的实现(@Required注解只能修饰方法)
	 *
	 * 5）AbstractAutoProxyCreator
	 * 这是一个抽象类，实现了SmartInstantiationAwareBeanPostProcessor接口。主要用于aop在Spring中的应用
	 *
	 * 6）InstantiationAwareBeanPostProcessor
	 * InstantiationAwareBeanPostProcessor接口继承BeanPostProcessor接口，它内部提供了3个方法，再加上BeanPostProcessor接口内部的2个方法，所以实现这个接口需要实现5个方法。
	 * InstantiationAwareBeanPostProcessor接口的主要作用在于目标对象的实例化过程中需要处理的事情，包括实例化对象的前后过程以及实例的属性设置
	 * ①：postProcessBeforeInstantiation方法是最先执行的方法，它在目标对象实例化之前调用，该方法的返回值类型是Object，我们可以返回任何类型的值。
	 * 由于这个时候目标对象还未实例化，所以这个返回值可以用来代替原本该生成的目标对象的实例(比如代理对象)。
	 * 如果该方法的返回值代替原本该生成的目标对象，后续只有postProcessAfterInitialization方法会调用，其它方法不再调用；否则按照正常的流程走
	 * ②：postProcessAfterInstantiation方法在目标对象实例化之后调用，这个时候对象已经被实例化，但是该实例的属性还未被设置，都是null。
	 * 如果该方法返回false，会忽略属性值的设置；如果返回true，会按照正常流程设置属性值
	 * ③：postProcessPropertyValues方法对属性值进行修改(这个时候属性值还未被设置，但是我们可以修改原本该设置进去的属性值)。
	 * 如果postProcessAfterInstantiation方法返回false，该方法不会被调用。可以在该方法内对属性值进行修改
	 * ④：父接口BeanPostProcessor的2个方法postProcessBeforeInitialization和postProcessAfterInitialization都是在目标对象被实例化之后，并且属性也被设置之后调用的
	 * 注：Instantiation表示实例化，Initialization表示初始化。实例化的意思在对象还未生成，初始化的意思在对象已经生成
	 */

	/**
	 * postProcessBeforeInitialization和postProcessAfterInitialization方法被调用的时候。这个时候bean已经被实例化，并且所有该注入的属性都已经被注入，是一个完整的bean。
	 * 这2个方法的返回值可以是原先生成的实例bean，或者使用wrapper包装这个实例
	 */

	/**
	 * bean在初始化之前需要调用的方法
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
	 * bean在初始化之后需要调用的方法
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

/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.aop.config;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * {@code NamespaceHandler} for the {@code aop} namespace.
 *
 * <p>Provides a {@link org.springframework.beans.factory.xml.BeanDefinitionParser} for the
 * {@code <aop:config>} tag. A {@code config} tag can include nested
 * {@code pointcut}, {@code advisor} and {@code aspect} tags.
 *
 * <p>The {@code pointcut} tag allows for creation of named
 * {@link AspectJExpressionPointcut} beans using a simple syntax:
 * <pre class="code">
 * &lt;aop:pointcut id=&quot;getNameCalls&quot; expression=&quot;execution(* *..ITestBean.getName(..))&quot;/&gt;
 * </pre>
 *
 * <p>Using the {@code advisor} tag you can configure an {@link org.springframework.aop.Advisor}
 * and have it applied to all relevant beans in you {@link org.springframework.beans.factory.BeanFactory}
 * automatically. The {@code advisor} tag supports both in-line and referenced
 * {@link org.springframework.aop.Pointcut Pointcuts}:
 *
 * <pre class="code">
 * &lt;aop:advisor id=&quot;getAgeAdvisor&quot;
 *     pointcut=&quot;execution(* *..ITestBean.getAge(..))&quot;
 *     advice-ref=&quot;getAgeCounter&quot;/&gt;
 *
 * &lt;aop:advisor id=&quot;getNameAdvisor&quot;
 *     pointcut-ref=&quot;getNameCalls&quot;
 *     advice-ref=&quot;getNameCounter&quot;/&gt;</pre>
 *
 * @author Rob Harrop
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
public class AopNamespaceHandler extends NamespaceHandlerSupport {

	/**
	 * Register the {@link BeanDefinitionParser BeanDefinitionParsers} for the
	 * '{@code config}', '{@code spring-configured}', '{@code aspectj-autoproxy}'
	 * and '{@code scoped-proxy}' tags.
	 */
	@Override
	public void init() {

		// 初始化调用时间点：
		// 在调用 BeanDefinitionParserDelegate#parseCustomElement(Element, BeanDefinition) 方法时，会在内部构建
		// NamespaceHandler对象，在实例化NamespaceHandler对象后会调用init()方法进行初始化，
		// init()方法用来对自定义标签分配一个BeanDefinitionParser
		// 后面再调用该BeanDefinitionParser的parse()方法来解析生成 BeanDefinition

		// In 2.0 XSD as well as in 2.1 XSD.
		registerBeanDefinitionParser("config", new ConfigBeanDefinitionParser());
		registerBeanDefinitionParser("aspectj-autoproxy", new AspectJAutoProxyBeanDefinitionParser());
		registerBeanDefinitionDecorator("scoped-proxy", new ScopedProxyBeanDefinitionDecorator());

		// Only in 2.0 XSD: moved to context namespace as of 2.1
		registerBeanDefinitionParser("spring-configured", new SpringConfiguredBeanDefinitionParser());
	}


	/**
	 *
	 * AOP 的原理：无非是通过代理模式为目标对象生产代理对象，并将横切逻辑插入到目标方法执行的前后。
	 *
	 * AOP （ Aspect Oriented Programming，即面向切面的编程） 术语：
	 *
	 * 1）连接点 - Joinpoint
	 * 连接点是指程序执行过程中的一些点，比如方法调用，异常处理等。在 Spring AOP 中，仅支持方法级别的连接点。
	 * Joinpoint继承体系图如下所示：
	 * Joinpoint (org.aopalliance.intercept.Joinpoint)
	 * 	Invocation (org.aopalliance.intercept)
	 * 		ConstructorInvocation (org.aopalliance.intercept)
	 * 		MethodInvocation (org.aopalliance.intercept)
	 * 			ProxyMethodInvocation (org.springframework.aop)
	 * 				ReflectiveMethodInvocation (org.springframework.aop.framework)
	 * 					CglibMethodInvocation in CglibAopProxy (org.springframework.aop.framework)
	 *
	 *
	 * 2）切点 - Pointcut
	 * 切点是用于选择连接点的 (Pointcut定义了一个方法匹配器MethodMatcher),如 AspectJExpressionPointcut ->  execution(* com.sample.service.impl..*.*(..))
	 *
	 * 3）通知 - Advice
	 * 通知 Advice 即我们定义的横切逻辑，比如我们可以定义一个用于监控方法性能的通知，也可以定义一个安全检查的通知等。
	 * 如果说切点解决了通知在哪里调用的问题，那么现在还需要考虑了一个问题，即通知在何时被调用？
	 * 是在目标方法前被调用，还是在目标方法返回后被调用，还在两者兼备呢？
	 * Spring 帮我们解答了这个问题，Spring 中定义了以下几种通知类型：
	 * 前置通知（Before advice）- 在目标方便调用前执行通知
	 * 后置通知（After advice）- 在目标方法完成后执行通知
	 * 返回通知（After returning advice）- 在目标方法执行成功后，调用通知
	 * 异常通知（After throwing advice）- 在目标方法抛出异常后，执行通知
	 * 环绕通知（Around advice）- 在目标方法调用前后均可执行自定义逻辑
	 *
	 * 现在我们有了切点 Pointcut 和通知 Advice，由于这两个模块目前还是分离的，我们需要把它们整合在一起。
	 * 这样切点就可以为通知进行导航，然后由通知逻辑实施精确打击。那怎么整合两个模块呢？答案是，切面
	 *
	 *
	 * 4）切面 - Aspect
	 * 切面 Aspect 整合了切点和通知两个模块，切点解决了 where 问题，通知解决了 when 和 how 问题。
	 * 切面把两者整合起来，就可以解决 对什么方法（where）在何时（when - 前置还是后置，或者环绕）执行什么样的横切逻辑（how）的三连发问题。
	 * 在 AOP 中，切面只是一个概念，并没有一个具体的接口或类与此对应。
	 * 不过 Spring 中倒是有一个接口的用途和切面很像，我们不妨了解一下，这个接口就是切点通知器 PointcutAdvisor。
	 *
	 *
	 * 5）织入 - Weaving
	 * 现在我们有了连接点、切点、通知，以及切面等，可谓万事俱备，但是还差了一股东风。这股东风是什么呢？没错，就是织入。
	 * 所谓织入就是在切点的引导下，将通知逻辑插入到方法调用上，使得我们的通知逻辑在方法调用时得以执行。
	 * 说完织入的概念，现在来说说 Spring 是通过何种方式将通知织入到目标方法上的。
	 * 先来说说以何种方式进行织入，这个方式就是通过实现后置处理器 BeanPostProcessor 接口。
	 * 该接口是 Spring 提供的一个拓展接口，通过实现该接口，用户可在 bean 初始化前后做一些自定义操作。
	 * 那 Spring 是在何时进行织入操作的呢？答案是在 bean 初始化完成后，即 bean 执行完初始化方法（init-method）。
	 * Spring通过切点对 bean 类中的方法进行匹配。若匹配成功，则会为该 bean 生成代理对象，并将代理对象返回给容器。
	 * 容器向后置处理器输入 bean 对象，得到 bean 对象的代理，这样就完成了织入过程。
	 *
	 * 一个AOP Config 的事例如下所示：
	 * &lt;!-- 配置切面的Bean --&gt;
	 * <bean id="sysAspect" class="com.example.aop.SysAspect"/>
	 * &lt;!-- 配置AOP --&gt;
	 * <aop:config>
	 *     &lt;!-- 配置切点表达式  --&gt;
	 *     <aop:pointcut id="pointcut" expression="execution(public * com.example.controller.*Controller.*(..))"/>
	 *     &lt;!-- 配置切面及配置 --&gt;
	 *     <aop:aspect order="3" ref="sysAspect">
	 *         &lt;!-- 前置通知 --&gt;
	 *         <aop:before method="beforMethod"  pointcut-ref="pointcut" />
	 *         &lt;!-- 后置通知 --&gt;
	 *         <aop:after method="afterMethod"  pointcut-ref="pointcut"/>
	 *         &lt;!-- 返回通知 --&gt;
	 *         <aop:after-returning method="afterReturnMethod" pointcut-ref="pointcut" returning="result"/>
	 *         &lt;!-- 异常通知 --&gt;
	 *         <aop:after-throwing method="afterThrowingMethod" pointcut-ref="pointcut" throwing="ex"/>
	 *         <aop:around method="aroundMethod" pointcut-ref="pointcut"/>
	 *     </aop:aspect>
	 * </aop:config>
	 *
	 *
	 *
	 * 使用注解的方式：
	 * <context:component-scan base-package="com.springcode"/>
	 * <aop:aspectj-autoproxy />
	 *
	 * @Component
	 * @Aspect
	 * public class Operator {
	 *
	 *     @Pointcut("execution(* com.springcode.service..*.*(..))")
	 *     public void pointCut(){}
	 *
	 *     @Before("pointCut()")
	 *     public void doBefore(JoinPoint joinPoint){
	 *         System.out.println("AOP Before Advice...");
	 *     }
	 *
	 *     @After("pointCut()")
	 *     public void doAfter(JoinPoint joinPoint){
	 *         System.out.println("AOP After Advice...");
	 *     }
	 *
	 *     @AfterReturning(pointcut="pointCut()",returning="returnVal")
	 *     public void afterReturn(JoinPoint joinPoint,Object returnVal){
	 *         System.out.println("AOP AfterReturning Advice:" + returnVal);
	 *     }
	 *
	 *     @AfterThrowing(pointcut="pointCut()",throwing="error")
	 *     public void afterThrowing(JoinPoint joinPoint,Throwable error){
	 *         System.out.println("AOP AfterThrowing Advice..." + error);
	 *         System.out.println("AfterThrowing...");
	 *     }
	 *
	 *     @Around("pointCut()")
	 *     public void around(ProceedingJoinPoint pjp){
	 *         System.out.println("AOP Aronud before...");
	 *         try {
	 *             pjp.proceed();
	 *         } catch (Throwable e) {
	 *             e.printStackTrace();
	 *         }
	 *         System.out.println("AOP Aronud after...");
	 *     }
	 * }
	 *
	 */

}

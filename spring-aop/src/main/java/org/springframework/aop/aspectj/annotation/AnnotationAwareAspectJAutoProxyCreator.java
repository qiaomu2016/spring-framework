/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.aop.aspectj.annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link AspectJAwareAdvisorAutoProxyCreator} subclass that processes all AspectJ
 * annotation aspects in the current application context, as well as Spring Advisors.
 *
 * <p>Any AspectJ annotated classes will automatically be recognized, and their
 * advice applied if Spring AOP's proxy-based model is capable of applying it.
 * This covers method execution joinpoints.
 *
 * <p>If the &lt;aop:include&gt; element is used, only @AspectJ beans with names matched by
 * an include pattern will be considered as defining aspects to use for Spring auto-proxying.
 *
 * <p>Processing of Spring Advisors follows the rules established in
 * {@link org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator}.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory
 */
@SuppressWarnings("serial")
public class AnnotationAwareAspectJAutoProxyCreator extends AspectJAwareAdvisorAutoProxyCreator {

	/**
	 * 这个类的继承结构可以发现其实现了InstantiationAwareBeanPostProcessor和BeanPostProcessor两个接口
	 * 并且分别实现了下面两个方法：
	 * ①：在Spring实例化一个bean之前执行，如果这里能够返回目标bean对象，那么这里就直接使用该对象，
	 * Spring不会继续生成目标bean对象，这种方式可以实现自定义的bean对象
	 * 实现位于父类AbstractAutoProxyCreator中
	 * public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {
	 *     default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName)
	 *         throws BeansException {
	 *         return null;
	 *     }
	 * }
	 * ②：在Spring实例化一个bean之后执行，主要作用是对已经生成的bean进行一定的处理
	 * public interface BeanPostProcessor {
	 * 	default Object postProcessAfterInitialization(Object bean, String beanName)
	 *         throws BeansException {
	 * 		return bean;
	 * 	}
	 * }
	 *
	 * AnnotationAwareAspectJAutoProxyCreator对这两个方法都进行了重写，对于重写的第一个方法，
	 * 其主要目的在于如果用户使用了自定义的TargetSource对象，则直接使用该对象生成目标对象，而不会使用Spring的默认逻辑生成目标对象，
	 * 并且这里会判断各个切面逻辑是否可以应用到当前bean上，如果可以，则直接应用，
	 * 也就是说TargetSource为使用者在Aop中提供了一个自定义生成目标bean逻辑的方式，并且会应用相应的切面逻辑。
	 * 对于第二个方法，其主要作用在于Spring生成某个bean之后，将相关的切面逻辑应用到该bean上。
	 *
	 */

	@Nullable
	private List<Pattern> includePatterns;

	@Nullable
	private AspectJAdvisorFactory aspectJAdvisorFactory;

	@Nullable
	private BeanFactoryAspectJAdvisorsBuilder aspectJAdvisorsBuilder;


	/**
	 * Set a list of regex patterns, matching eligible @AspectJ bean names.
	 * <p>Default is to consider all @AspectJ beans as eligible.
	 */
	public void setIncludePatterns(List<String> patterns) {
		this.includePatterns = new ArrayList<>(patterns.size());
		for (String patternText : patterns) {
			this.includePatterns.add(Pattern.compile(patternText));
		}
	}

	public void setAspectJAdvisorFactory(AspectJAdvisorFactory aspectJAdvisorFactory) {
		Assert.notNull(aspectJAdvisorFactory, "AspectJAdvisorFactory must not be null");
		this.aspectJAdvisorFactory = aspectJAdvisorFactory;
	}

	@Override
	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		super.initBeanFactory(beanFactory);
		if (this.aspectJAdvisorFactory == null) {
			this.aspectJAdvisorFactory = new ReflectiveAspectJAdvisorFactory(beanFactory);
		}
		this.aspectJAdvisorsBuilder =
				new BeanFactoryAspectJAdvisorsBuilderAdapter(beanFactory, this.aspectJAdvisorFactory);
	}

	/**
	 * AnnotationAwareAspectJAutoProxyCreator 覆写了父类的方法 findCandidateAdvisors，
	 * 并增加了一步操作，即解析 @Aspect 注解，并构建成通知器
	 * @return
	 */
	@Override
	protected List<Advisor> findCandidateAdvisors() {
		// Add all the Spring advisors found according to superclass rules.
		// 调用父类方法从容器中查找所有的通知器
		List<Advisor> advisors = super.findCandidateAdvisors();
		// Build Advisors for all AspectJ aspects in the bean factory.
		// 解析 @Aspect 注解，并构建通知器
		if (this.aspectJAdvisorsBuilder != null) {
			advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
		}
		return advisors;
	}

	@Override
	protected boolean isInfrastructureClass(Class<?> beanClass) {
		// Previously we setProxyTargetClass(true) in the constructor, but that has too
		// broad an impact. Instead we now override isInfrastructureClass to avoid proxying
		// aspects. I'm not entirely happy with that as there is no good reason not
		// to advise aspects, except that it causes advice invocation to go through a
		// proxy, and if the aspect implements e.g the Ordered interface it will be
		// proxied by that interface and fail at runtime as the advice method is not
		// defined on the interface. We could potentially relax the restriction about
		// not advising aspects in the future.
		return (super.isInfrastructureClass(beanClass) ||
				(this.aspectJAdvisorFactory != null && this.aspectJAdvisorFactory.isAspect(beanClass)));
	}

	/**
	 * Check whether the given aspect bean is eligible for auto-proxying.
	 * <p>If no &lt;aop:include&gt; elements were used then "includePatterns" will be
	 * {@code null} and all beans are included. If "includePatterns" is non-null,
	 * then one of the patterns must match.
	 */
	protected boolean isEligibleAspectBean(String beanName) {
		if (this.includePatterns == null) {
			return true;
		}
		else {
			for (Pattern pattern : this.includePatterns) {
				if (pattern.matcher(beanName).matches()) {
					return true;
				}
			}
			return false;
		}
	}


	/**
	 * Subclass of BeanFactoryAspectJAdvisorsBuilderAdapter that delegates to
	 * surrounding AnnotationAwareAspectJAutoProxyCreator facilities.
	 */
	private class BeanFactoryAspectJAdvisorsBuilderAdapter extends BeanFactoryAspectJAdvisorsBuilder {

		public BeanFactoryAspectJAdvisorsBuilderAdapter(
				ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {

			super(beanFactory, advisorFactory);
		}

		@Override
		protected boolean isEligibleBean(String beanName) {
			return AnnotationAwareAspectJAutoProxyCreator.this.isEligibleAspectBean(beanName);
		}
	}

}

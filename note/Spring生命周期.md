# Spring Bean 的生命周期
1. Spring 容器根据实例化策略对 Bean 进行实例化（一般有反射和 CGLIB 动态字节码两种方式）。
2. 实例化完成后，如果该 bean 设置了一些属性的话，则利用 set 方法设置一些属性。
3. 激活 Aware
    + 如果该 Bean 实现了 BeanNameAware 接口，则调用 #setBeanName(String beanName) 方法。
    + 如果该 bean 实现了 BeanClassLoaderAware 接口，则调用 setBeanClassLoader(ClassLoader classLoader) 方法。
    + 如果该 bean 实现了 BeanFactoryAware接口，则调用 setBeanFactory(BeanFactory beanFactory) 方法。
4. 如果该容器注册了 BeanPostProcessor，则会调用#postProcessBeforeInitialization(Object bean, String beanName) 方法,完成 bean 前置处理
5. 初始化
    + 如果该 bean 实现了 InitializingBean 接口，则调用#afterPropertiesSet() 方法。
    + 如果该 bean 配置了 init-method 方法，则调用其指定的方法。
6. 初始化完成后，如果该容器注册了 BeanPostProcessor 则会调用 #postProcessAfterInitialization(Object bean, String beanName) 方法,完成 bean 的后置处理。
7. 对象完成初始化，开始方法调用。
8. 关闭容器
    + 在容器进行关闭之前，如果该 bean 实现了 DisposableBean 接口，则调用 #destroy() 方法。
    + 在容器进行关闭之前，如果该 bean 配置了 destroy-method ，则调用其指定的方法。
到这里一个 bean 也就完成了它的一生。


![Spring生命周期](https://gitee.com/chenssy/blog-home/raw/master/image/201811/15359386381747.jpg)
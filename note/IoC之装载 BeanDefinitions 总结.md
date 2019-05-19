# IoC 容器的初始化过程
IoC 容器的初始化过程分为三步骤：Resource 定位、BeanDefinition 的载入和解析，BeanDefinition 注册。
![流程图](https://gitee.com/chenssy/blog-home/raw/master/image/201811/spring-201805281001.png)
1. **Resource 定位**。我们一般用外部资源来描述 Bean 对象，所以在初始化 IoC 容器的第一步就是需要定位这个外部资源。
2. **BeanDefinition 的装载和解析**。装载就是 BeanDefinition 的载入。BeanDefinitionReader 读取、解析 Resource 资源，也就是将用户定义的 Bean 表示成 IoC 容器的内部数据结构：BeanDefinition 。
   * 在 IoC 容器内部维护着一个 BeanDefinition Map 的数据结构
   * 在配置文件中每一个 <bean> 都对应着一个 BeanDefinition 对象。
3. **BeanDefinition 注册**。向 IoC 容器注册在第二步解析好的 BeanDefinition，这个过程是通过 BeanDefinitionRegistry 接口来实现的。在 IoC 容器内部其实是将第二个过程解析得到的 BeanDefinition 注入到一个 HashMap 容器中，IoC 容器就是通过这个 HashMap 来维护这些 BeanDefinition 的。
   * 在这里需要注意的一点是这个过程并没有完成依赖注入（Bean 创建），Bean 创建是发生在应用第一次调用 #getBean(...) 方法，向容器索要 Bean 时。
   * 当然我们可以通过设置预处理，即对某个 Bean 设置 lazyinit = false 属性，那么这个 Bean 的依赖注入就会在容器初始化的时候完成。

这里我们同样也以这段代码作为我们研究 IoC 初始化过程的开端，如下：
```
ClassPathResource resource = new ClassPathResource("bean.xml");
DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
reader.loadBeanDefinitions(resource);
```

刚刚开始的时候可能对上面这几行代码不知道什么意思，现在应该就一目了然了：
+ ClassPathResource resource = new ClassPathResource("bean.xml"); ： 根据 Xml 配置文件创建 Resource 资源对象。ClassPathResource 是 Resource 接口的子类，bean.xml 文件中的内容是我们定义的 Bean 信息。
+ DefaultListableBeanFactory factory = new DefaultListableBeanFactory(); ：创建一个 BeanFactory 。DefaultListableBeanFactory 是 BeanFactory 的一个子类，BeanFactory 作为一个接口，其实它本身是不具有独立使用的功能的，而 DefaultListableBeanFactory 则是真正可以独立使用的 IoC 容器，它是整个 Spring IoC 的始祖。
+ XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory); ：创建 XmlBeanDefinitionReader 读取器，用于载入 BeanDefinition 。
+ reader.loadBeanDefinitions(resource);：开始 BeanDefinition 的载入和注册进程，完成后的 BeanDefinition 放置在 IoC 容器中。


# Resource 定位
* Spring 为了解决资源定位的问题，提供了两个接口：Resource、ResourceLoader，其中：
* Resource 接口是 Spring 统一资源的抽象接口
* ResourceLoader 则是 Spring 资源加载的统一抽象。
* Resource 资源的定位需要 Resource 和 ResourceLoader 两个接口互相配合

# BeanDefinition 的载入和解析
1. 转换为 Document 对象
2. 注册 BeanDefinition 流程
   + 对 Document 对象的解析
      * 默认标签解析
      * 自定义标签解析
   + 注册 BeanDefinition


# 小结
![流程图](https://gitee.com/chenssy/blog-home/raw/master/image/201811/spring-201807201001.png)
# spring

## Spring IoC 和 AOP

* IoC（Inverse of Control:控制反转）是一种**设计思想**，就是 **将原本在程序中手动创建对象的控制权，交由Spring框架来管理**。 **IoC 容器是 Spring 用来实现 IoC 的载体， IoC 容器实际上就是个Map（key，value）,Map 中存放的是各种对象**

> 关于 Map。在 spring 容器中，有 2 种 map 作为缓存以存储单例对象
> 1. singletonObjects：存放的是单例对象
> 2. earlySingletonObjects：存放的是提前暴露的对象（即虽然对象还在创建的过程中，但是可被注入依赖）
> 获取单例实例时，先从 1 的 map 中获取，如果没有的话，就从 2 的 map 中获取，如果仍然没有，就从 objectFactory 中 获取。

* bean 注册到 IOC 容器中的简化过程
> Xml 配置文件 ---> 读取为 Resource ---> 解析到 BeanDefinition 中 ---> 注册到 BeanFactory 中
* 在 spring 中仅使用构造函数和 setter 注入
> 构造函数注入和 setter 方式注入，在配置文件中定义 bean 的注入方式
> 0. 在实例化对象的时候，会遇到对象A依赖对象B，而对象B也依赖对象A，此时就会发生循环依赖
> 1. 构造函数方式注入产生的循环依赖，spring 无法解决，会抛出异常 BeanCurrentlylnCreationException
> 2. setter 方式注入的 bean 在遇到循环依赖时，spring 的解决方式是：
>    1. 非单例模式，无法解决
>    2. 单例模式：使用**三级缓存**。spring 会依次尝试从如下的 map 中获取单例对象
>    ```java
>    // 完成初始化的单例对象的 cache（一级缓存）
>    private final Map singletonObjects = new ConcurrentHashMap(256); 
>    
>    // 完成实例化但是尚未初始化的，提前暴光的单例对象的Cache （二级缓存）
>    private final Map earlySingletonObjects = new HashMap(16);
>    
>    // 进入实例化阶段的单例对象工厂的cache （三级缓存）
>    private final Map> singletonFactories = new HashMap>(16);
>    ```

* AOP

> 如日志管理和权限控制：减少重复代码和耦合度
> 基于动态代理：JDK 动态代理和 cglib 动态代理
> 1. JDK 动态代理：如果要代理的对象，实现了某个接口
> 2. cglib 动态代理：没有实现接口的对象，spring aop 会使用 cglib 代理
>
> **AOP 的几个概念：**
>
> 1. 切点 joinPoint：被特殊关注的一个方法。可通过在切面中通过注解 @Pointcut("execution(* add(..))") 标识
> 2. 切面 aspect：可通过 @Aspect 标记为一个切面
> 3. 通知 advice：在切点处切面采取的动作。before、after、after throw、around 等

## BeanFactory 和 FactoryBean

###BeanFactory
> 1. 是一个 bean 工厂，包含了各种Bean的定义，读取bean配置文档，管理bean的加载、实例化，控制bean的生命周期，维护bean之间的依赖关系
> 2. BeanFactroy采用的是延迟加载形式来注入Bean的，即只有在使用到某个Bean时(调用getBean())，才对该Bean进行加载实例化

### FactoryBean

> 1. 是一个特殊的 bean。FactoryBean跟普通Bean不同，其返回的对象不是指定类的一个实例，而是该FactoryBean的getObject方法所返回的对象。创建出来的对象是否属于单例由isSingleton中的返回决定。
> 2. 可以自定义实现 FactoryBean 接口，以自定义实现 bean 的其他较为复杂的逻辑

## ApplicationContext

> 在容器启动时，一次性创建了所有的Bean
> ApplicationContext接口作为BeanFactory的派生，除了提供BeanFactory所具有的功能外，还提供了更完整的框架功能：
> ①继承MessageSource，因此支持国际化。
> ②统一的资源文件访问方式。
> ③提供在监听器中注册bean的事件。
> ④同时加载多个配置文件。
> ⑤载入多个（有继承关系）上下文 ，使得每一个上下文都专注于一个特定的层次，比如应用的web层。

## spring bean 生命周期
> 对比 Servlet 的生命周期：实例化，初始 init，接收请求 service，销毁 destroy；
> Spring上下文中的Bean生命周期也类似，如下：
> 1. 实例化 Bean
>    对于 BeanFactory 容器，当客户向容器请求一个尚未初始化的bean时，或初始化 bean 的时候需要注入另一个尚未初始化的依赖时，容器就会调用createBean进行实例化。对于ApplicationContext容器，当容器启动结束后，通过获取 BeanDefinition 对象中的信息，实例化所有的bean。
> 2. 设置对象属性（依赖注入）
> 实例化后的对象被封装在BeanWrapper对象中，紧接着，Spring根据BeanDefinition中的信息 以及 通过BeanWrapper 提供的设置属性的接口完成依赖注入。
> 3. 处理 Aware 接口
> 接着，Spring会检测该对象是否实现了xxxAware接口，并将相关的xxxAware实例注入给Bean：
> ①如果这个Bean已经实现了BeanNameAware接口，会调用它实现的setBeanName(String beanId)方法，此处传递的就是Spring配置文件中Bean的id值；
> ②如果这个Bean已经实现了BeanFactoryAware接口，会调用它实现的setBeanFactory()方法，传递的是Spring工厂自身。
> ③如果这个Bean已经实现了ApplicationContextAware接口，会调用setApplicationContext(ApplicationContext) 方法，传入Spring上下文；
> 4. BeanPostProcessor：
> 如果想对Bean进行一些自定义的处理，那么可以让Bean实现了BeanPostProcessor接口，那将会调用postProcessBeforeInitialization(Object obj, String s)方法。
> 5. InitializingBean 与 init-method：
> 如果Bean在Spring配置文件中配置了 init-method 属性，则会自动调用其配置的初始化方法。
> 6. 如果这个Bean实现了BeanPostProcessor接口，将会调用postProcessAfterInitialization(Object obj, String s)方法；由于这个方法是在Bean初始化结束时调用的，所以可以被应用于内存或缓存技术；
> 以上几个步骤完成后，Bean就已经被正确创建了，之后就可以使用这个Bean了。
> 7. DisposableBean：
> 当Bean不再需要时，会经过清理阶段，如果Bean实现了DisposableBean这个接口，会调用其实现的destroy()方法；
> 8. destroy-method：
> 最后，如果这个Bean的Spring配置中配置了destroy-method属性，会自动调用其配置的销毁方法。

## spring 自动装配
> 对象无需自己查找或创建与其关联的其他对象，由容器负责把需要相互协作的对象引用赋予各个对象，使用autowire来配置自动装载模式。5 中装配方式：
>
> 1. No
> 2. byName
> 3. byType
> 4. constructor
> 5. autodetect
>
> @Autowired 和 Resource 区别
>
> 1. @Autowired 默认是按照**类型**装配注入的，默认要求依赖对象必须存在(可以设置它required属性为false)
> 2. @Resource 默认是按照**名称**来装配注入的，只有当找不到与名称匹配的bean才会按照类型来装配注入

## spring 事务
> 2 种实现方式：编程式 和 声明式。都需要设置或者是哟默认的事务传播方式等配置
>
> 事务传播行为：
>
> 1. PROPAGATION_**REQUIRED**：如果当前没有事务，就创建一个新事务，如果当前存在事务，就加入该事务，**该设置是最常用的设置。**
> 2. PROPAGATION_SUPPORTS：支持当前事务。如果当前存在事务，就加入该事务，如果当前不存在事务，就以非事务执行。
> 3. PROPAGATION_MANDATORY：支持当前事务。如果当前存在事务，就加入该事务，如果当前不存在事务，就抛出异常。
> 4. PROPAGATION_REQUIRES_NEW：创建新事务，无论当前存不存在事务，都创建新事务。
> 5. PROPAGATION_NESTED：如果当前存在事务，则在嵌套事务内执行。如果当前没有事务，则按REQUIRED属性执行。
>
> 事务隔离级别：与数据库定义一致
>
> 1. 读未提交
> 2. 读已提交
> 3. 可重复读
> 4. 串行化

# spring boot

## Spring Boot 中的 Starters
> 

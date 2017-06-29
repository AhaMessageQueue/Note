在使用Spring时，对于一些功能的配置可以通过Spring提供的XML命名空间进行配置，也可以通过提供的注解进行配置。这两种方式都是等价的，它们背后对应的工作原理是什么呢？

以事务管理为例：（以下代码来自Spring的API文档）

@EnableTransactionManagement注解启用了事务管理功能。
```
@Configuration  
@EnableTransactionManagement
public class AppConfig {  
 @Bean  
 public FooRepository fooRepository() {  
     // configure and return a class having @Transactional methods  
     return new JdbcFooRepository(dataSource());  
 }  

 @Bean  
 public DataSource dataSource() {  
     // configure and return the necessary JDBC DataSource  
 }  

 @Bean  
 public PlatformTransactionManager txManager() {  
     return new DataSourceTransactionManager(dataSource());  
 }  
}  
```
上面的代码与以下的XML配置是等效的：
```
<beans>  
     <tx:annotation-driven/>  
     <bean id="fooRepository" class="com.foo.JdbcFooRepository">  
         <constructor-arg ref="dataSource"/>  
     </bean>  
     <bean id="dataSource" class="com.vendor.VendorDataSource"/>  
     <bean id="transactionManager" class="org.sfwk...DataSourceTransactionManager">  
         <constructor-arg ref="dataSource"/>  
     </bean>  
 </beans>  
```
### 一、XML配置工作机制
那类似于`<tx:annotation-driven/>`的配置是如何生效的呢？以启动ClassPathXmlApplicationContext为例。

装载`Bean`部分过程如下：

1. `ClassPathXmlApplicationContex`构造函数中自动调用`refresh()`完成`Bean`信息的装载(除非显式指定手工刷新)：
2. `ApplicationContext`通过`XmlBeanDefinitionReader`来读取和解析XML文件。
3. `XmlBeanDefinitionReader`通过`DefaultBeanDefinitionDocumentReader`来读取XML中定义的Bean信息，并保存到`BeanDefinitionRegistry`中。
4. 对于默认命名空间中的XML标签通过`parseDefaultElement()`来进行解析。支持的XML标签有：import、alias、bean和beans，其他标签都会被忽略。
5. 对于其他命名空间中的标签，使用`DefaultNamespaceHandlerResolver`来获取对应的`NamespaceHandler`，完成标签的解析。`NamespaceHandler`相关的配置信息放在Spring的jar包中的`META-INF/spring.handlers`路径下。

由此可见，当XML中存在`<tx:annotation-driven/>`时，命名空间为tx，从配置文件中查找到的NamespaceHandler为`TxNamespaceHandler`。

由`TxNamespaceHandler`负责具体的解析工作，它的部分代码如下：
```
public void init() {  
  registerBeanDefinitionParser("advice", new TxAdviceBeanDefinitionParser());  
  registerBeanDefinitionParser("annotation-driven", new AnnotationDrivenBeanDefinitionParser());  
  registerBeanDefinitionParser("jta-transaction-manager", new JtaTransactionManagerBeanDefinitionParser());  
}
```
由代码可见，tx命名空间只支持`advice`、`annotation-driven`和`jta-transaction-manager`三个Bean定义。

### 二、注解配置的工作原理
@EnableTransactionManagement注解又是如何起作用的呢？

1. `ApplicationContex`调用`refresh()`中，首先刷新`BeanFactory`完成`PostProcessor监听器`的注册，其中就有`ConfigurationClassPostProcessor`，用来解析与`@Configuration`注解同时出现的注解信息。
2. 调用所有的`BeanFactoryPostProcessor`，其中`ConfigurationClassPostProcessor`对BeanFactory中所有的bean定义进行检查，对标注了`@Configuration`的Bean使用`ConfigurationClassParser`进行解析。
3. Parser解析包括：
    1）对Member成员的递归解析；
    2）检查Bean定义中的注解：@PropertySourc、@ComponentScan、@Import、@ImportResource、@Bean，以及对超类进行检查。记录相关的注解信息。
4. 在生成Bean实例前，调用相关的BeanPostProcessor，其中一步处理是为Bean找到相关的Advisor，完成切面的“编织”工作。

---

## EnableTransactionManagement API


org.springframework.transaction.annotation

**Annotation Type EnableTransactionManagement**

```
@Target(value=TYPE)
@Retention(value=RUNTIME)
@Documented
@Import(value=TransactionManagementConfigurationSelector.class)
public @interface EnableTransactionManagement
```
启用Spring注释驱动的事务管理功能，类似于在Spring `<tx：*>` XML命名空间中找到的支持。 要在`@Configuration`类中使用，如下所示：
```
@Configuration
 @EnableTransactionManagement
 public class AppConfig {

     @Bean
     public FooRepository fooRepository() {
         // configure and return a class having @Transactional methods
         return new JdbcFooRepository(dataSource());
     }

     @Bean
     public DataSource dataSource() {
         // configure and return the necessary JDBC DataSource
     }

     @Bean
     public PlatformTransactionManager txManager() {
         return new DataSourceTransactionManager(dataSource());
     }
 }
```
作为参考，上面的示例可以与以下Spring XML配置进行比较：
```
 <beans>

     <tx:annotation-driven/>

     <bean id="fooRepository" class="com.foo.JdbcFooRepository">
         <constructor-arg ref="dataSource"/>
     </bean>

     <bean id="dataSource" class="com.vendor.VendorDataSource"/>

     <bean id="transactionManager" class="org.sfwk...DataSourceTransactionManager">
         <constructor-arg ref="dataSource"/>
     </bean>

 </beans>
```
在上述两种情况下，`@EnableTransactionManagement`和`<tx：annotation-driven />`都负责注册必要的Spring组件来提供注释驱动的事务管理，
例如`TransactionInterceptor` 和 当JdbcFooRepository的@Transactional方法被调用，编织拦截器到调用堆栈的基于代理或AspectJ的`advice`。

这两个例子之间的一个小的区别在于PlatformTransactionManager bean的命名：在@Bean的情况下，名称是“txManager”（根据方法的名称）; 在XML的情况下，名称是“transactionManager”。 默认情况下，`<tx：annotation-driven />`硬连线以查找名为“transactionManager”的bean，但是@EnableTransactionManagement更灵活; 它将回退到按类型查找容器中任何的PlatformTransactionManager bean。 因此，名称可以是“txManager”，“transactionManager”或“tm”：它没关系。

对于希望在@EnableTransactionManagement和要使用的确切事务管理器bean之间建立更直接关系的人，可以实现TransactionManagementConfigurer回调接口 - 请注意以下的implements子句和@Override注释方法：

```
@Configuration
@EnableTransactionManagement
public class AppConfig implements TransactionManagementConfigurer {

 @Bean
 public FooRepository fooRepository() {
     // configure and return a class having @Transactional methods
     return new JdbcFooRepository(dataSource());
 }

 @Bean
 public DataSource dataSource() {
     // configure and return the necessary JDBC DataSource
 }

 @Bean
 public PlatformTransactionManager txManager() {
     return new DataSourceTransactionManager(dataSource());
 }

 @Override
 public PlatformTransactionManager annotationDrivenTransactionManager() {
     return txManager();
 }
}
```
这种方法可能是理想的，因为它是更明确的，或者可能有必要为了区分在同一容器中存在的两个PlatformTransactionManager bean。 顾名思义，annotationDrivenTransactionManager（）将是用于处理@Transactional方法的其中一个。 有关更多详细信息，请参阅TransactionManagementConfigurer Javadoc。

`mode()`属性控制如何应用`advice`; 如果模式是`AdviceMode.PROXY`（默认），那么其他属性控制代理的行为。如果`mode()`设置为`AdviceMode.ASPECTJ`，那么`proxyTargetClass()`属性已过时。 还要注意，在这种情况下，`spring-aspects module JAR`必须存在于类路径上。



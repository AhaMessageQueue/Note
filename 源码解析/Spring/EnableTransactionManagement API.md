启用Spring的**注释驱动事务管理**功能，类似于Spring的`<tx:*>`XML命名空间中的支持。
用于`@Configuration`类，如下所示：

```java
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

作为参考，上面的例子可以与下面的Spring XML配置进行比较：

```xml
 <beans>
  <!-- 默认根据 id 为 transactionManager 查找事务管理器 -->
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

在上述两种情况下，`@EnableTransactionManagement`和`<tx:annotation-driven/>`负责注册必要的Spring组件，这些组件支持注解驱动的事务管理，比如`TransactionInterceptor`和基于代理或AspectJ的通知，当`JdbcFooRepository`的`@Transactional`方法被调用时，将拦截器织入调用堆栈。

这两个示例之间的细微差别在于`PlatformTransactionManager` bean的命名：在`@Bean`情况下，名称是"txManager"（根据方法的名称）;在XML情况下，名称是"transactionManager"。默认情况下，`<tx:annotation-driven/>`查找名为"transactionManager"的bean，但`@EnableTransactionManagement`更加灵活;它按类型查找容器中任何`PlatformTransactionManager` bean。因此，名称可以是"txManager"，"transactionManager"或"tm"：它根本无关紧要。
对于希望在`@EnableTransactionManagement`和要使用的确切事务管理器bean之间建立更直接关系的用户，可以实现`TransactionManagementConfigurer`回调接口：

```java
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

这种方法可能是可取的，因为它更加明确，或者为了区分存在于同一个容器中的两个`PlatformTransactionManager` bean。顾名思义，`annotationDrivenTransactionManager()`将用于处理`@Transactional`方法。有关更多详细信息，请参阅`TransactionManagementConfigurer` Javadoc。mode()属性控制如何应用通知; 

- 如果模式是`AdviceMode.PROXY`（默认），则其他属性控制代理的行为。

- 如果模式设置为`AdviceMode.ASPECTJ`，则`proxyTargetClass()`属性不可用。 还要注意，在这种情况下，`spring-aspects`模块JAR必须存在于类路径中。
>Spring官網：http://spring.io/blog/2011/02/11/spring-framework-3-1-m1-released/

首个里程碑版本 - Spring 3.1 已经发布，我们有一系列文章来讲解 Spring 的一些新特性：

- Bean definition profiles
- ...
- ...

今天我们来讲解第一项 - 被称为 `Bean definition profiles` 的新特性。
我们收到最多的请求是提供一个基于核心容器的机制，以允许在不同环境中注册不同的`beans`。
`环境`一词针对不同的用户有不同的解释，不过最典型的场景是仅在性能测试环境下注册监控组件、或针对客户A和客户B的两个部署分别注册各自定制的beans。
可能最常用的案例是在开发阶段使用单独的 `datasource`，而在 QA环境或生产环境中从 `JNDI` 中查找一个相同的 `datasource`。
`Bean definition profiles` 是一种能够满足以上各种需求的通用解决方法，下面用一个示例来详细讲解。

## Understanding the application
首先来看一个银行系统中用于示范怎样在两个帐户之间转帐的 JUnit test case。
```
public class IntegrationTests {
    @Test
    public void transferTenDollars() throws InsufficientFundsException {

        ApplicationContext ctx = // instantiate the spring container

        TransferService transferService = ctx.getBean(TransferService.class);
        AccountRepository accountRepository = ctx.getBean(AccountRepository.class);

        assertThat(accountRepository.findById("A123").getBalance(), equalTo(100.00));
        assertThat(accountRepository.findById("C456").getBalance(), equalTo(0.00));

        transferService.transfer(10.00, "A123", "C456");

        assertThat(accountRepository.findById("A123").getBalance(), equalTo(90.00));
        assertThat(accountRepository.findById("C456").getBalance(), equalTo(10.00));
    }
}
```

我们的目标很简单，从帐户A123向帐户 C456转 10 美元。

## 典型的 XML 配置
`bean definition profiles` 也支持 `@Configuration` 方式的配置，在这里我们使用大家最熟悉的 XML 配置方式。
 
先不要管 `bean definition profiles`，考虑平时我们的 XML 配置会是怎样的。假设我们在开发阶段，一般我们会选择使用一个独立的 `datasource`，
为了方便在这里我们使用 HSQLDB( Spring 内存数据库( 嵌入式数据库 ) )

```
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jdbc="http://www.springframework.org/schema/jdbc"
    xsi:schemaLocation="...">
    <bean id="transferService" class="com.bank.service.internal.DefaultTransferService">
        <constructor-arg ref="accountRepository"/>
        <constructor-arg ref="feePolicy"/>
    </bean>
    
    <bean id="accountRepository" class="com.bank.repository.internal.JdbcAccountRepository">
        <constructor-arg ref="dataSource"/>
    </bean>

    <bean id="feePolicy" class="com.bank.service.internal.ZeroFeePolicy"/>

    <jdbc:embedded-database id="dataSource">
        <jdbc:script location="classpath:com/bank/config/sql/schema.sql"/>
        <jdbc:script location="classpath:com/bank/config/sql/test-data.sql"/>
    </jdbc:embedded-database>
```
基于上面的 XML 配置，之前 JUnit test case 缺少的部分如下：
```
public class IntegrationTests {
    @Test
    public void transferTenDollars() throws InsufficientFundsException {

        GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
        ctx.load("classpath:/com/bank/config/xml/transfer-service-config.xml");
        ctx.refresh();

        TransferService transferService = ctx.getBean(TransferService.class);
        AccountRepository accountRepository = ctx.getBean(AccountRepository.class);

        // perform transfer and issue assertions as above ...
    }
}
```
当运行测试程序时，测试条会显示绿色，我们这个简单的应用和容器连接在一起，我们从容器中获取 beans 并且使用它们，这里跟平时相比没有什么特别的。
当我们考虑怎样将该应用部署到 QA 环境或生产环境时问题变得有趣了。
例如，一个常用的场景是在开发阶段使用 Tomcat 作为服务器( 更易用 )，但在生产环境中会将应用部署到 WebSphere 中。而在生产环境中 `datasource` 通常被注册到服务器的 JNDI 目录中。
这意思着为了获取 `datasource` 我们必须要执行 JNDI 查找( JNDI lookup )。当然，对此 Spring 提供了非常好的支持，非常流行的方法是使用 Spring 的 `<jee:jndi-lookup/>` 元素。
产生环境中的配置如下：
```
<beans ...>
    <bean id="transferService" ... />

    <bean id="accountRepository" class="com.bank.repository.internal.JdbcAccountRepository">
        <constructor-arg ref="dataSource"/>
    </bean>

    <bean id="feePolicy" ... />

    <jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/datasource"/>
</beans>
```

上面的配置会正常工作。

但问题是如何基于当前环境来切换 `datasource` 的配置方式呢？

过去的一段时间里，Spring 的用户已经想出很多种方法来达到目的，通常都会依赖于一个系统环境变量( system environment variables )和 一个包含 ${placeholder}的<import/> 元素，通过环境变量的值来解析出正确的配置文件路径。
不过这种方法不能称为一流的解决方法。

## Enter bean definition profiles
概括一下上面所描述的基于环境的`bean`定义 - 在某些上下文中注册某些 `bean`。也可以说 在情况A时注册一批( a certain profile of )bean，而在情况B时注册另一批不同的 bean。
 
在 Spring 3.1 中，`<beans/>` XML 文档现在已经包含了这个新概念，针对上面的示例，我们可以把配置文件分为三个文件，注意 `*-datasource.xml` 文件中的 `profile="..."` 属性。

transfer-service-config.xml:
```
<beans ...>
    <bean id="transferService" ... />

    <bean id="accountRepository" class="com.bank.repository.internal.JdbcAccountRepository">
        <constructor-arg ref="dataSource"/>
    </bean>

    <bean id="feePolicy" ... />
</beans>
```
standalone-datasource-config.xml:
```
<beans profile="dev">
    <jdbc:embedded-database id="dataSource">
        <jdbc:script location="classpath:com/bank/config/sql/schema.sql"/>
        <jdbc:script location="classpath:com/bank/config/sql/test-data.sql"/>
    </jdbc:embedded-database>
</beans>
```
jndi-datasource-config.xml:
```
<beans profile="production">
    <jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/datasource"/>
</beans>
```

更新 test case，同时载入 3 个配置文件:
```
GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
ctx.load("classpath:/com/bank/config/xml/*-config.xml");
ctx.refresh();
```

这样还不行，当运行单元测试时我们会得到一个 `NoSuchBeanDefinitionException`，因为容器无法找到名为 `dataSource` 的 `bean`。
原因是虽然我们明确定义了两个 profiles - dev 和 production，但我们并没有激活其中的一个 profile。

## Enter the Environment
在 Spring 3.1 中出现了一个新概念 - Environment。

这个抽象概念贯穿于整个容器，以后的文章中会经常的看到这个概念。
在这里重要的是要知道，`Environment` 包括了哪个 profile 正处于激活状态的信息。
当 Spring ApplicationContext 加载上述三个配置文件时，会非常注意 `<beans>` 元素的 `profile` 属性，如果 `beans` 元素有 `profile` 属性，
且其属性值所代表的 profile 并不是当前激活的 profile，则整个配置文件会被跳过，没有任何 bean 会被解析或被注册。
 
激活一个 profile 有多种方式，最直接的办法是使用 ApplicationContext API 以编程式的方式来实现：
```
GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
ctx.getEnvironment().setActiveProfiles("dev");
ctx.load("classpath:/com/bank/config/xml/*-config.xml");
ctx.refresh();
```
这样当我们执行 test case 时，测试会通过。让我们看一下容器是怎样加载这三个配置文件的( *-config.xml )
- transfer-service-config.xml - beans 元素没有 profile 属性，因此总会被容器解析
- standalone-datasource-config.xml - 指定了 profile="div"，并且 div profile 是当前激活的 profile，因此会被解析
- jndi-datasource-config.xml - 指定了 profile="production"，但 production profile 并不是激活状态，因此被跳过

那在真正的生产环境中如何切换为 JNDI looup 呢？
当然必须要激活 production profile。
像上面那样为了执行单元测试而使用编程式激活profile 的方式是非常合适的，但当部署 WAR 文件时这种方法并不适用。
因此，`profiles` 也可以通过 `spring.profiles.active` 属性使用声明式激活方式，`spring.profiles.active` 属性值可以通过很多种方式指定：

- system environment variables
- JVM system properties
- servlet context parameters in web.xml

```
<servlet>
  <servlet-name>dispatcher</servlet-name>
  <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
  <init-param>
      <param-name>spring.profiles.active</param-name>
      <param-value>production</param-value>
  </init-param>
</servlet>
```
- servlet config parameter( 即上面`init-param`部分, 猜测 `init-param` 可能要添加到 Root Application Context 上才可以 )
- entry in JNDI

注意，profiles 并不是非此即彼的关系( 互斥 )，完全可以一次性激活多个 profile，使用编程式激活方法时，可以直接给 setActiveProfiles(String ...) 方法提供多个 profile name：
```
ctx.getEnvironment().setActiveProfiles("profile1", "profile2");
```

若使用声明式激活方法的话，spring.profiles.active 可以接收多个以逗号分隔的 profile name：

```
-Dspring.profiles.active="profile1,profile2"
```

beans 元素的 profile 属性也可以设置多个候选 profile name ：
```
<beans profile="profile1,profile2">
    ...
</beans>
```
这提供了分解应用的一种灵活的方法，以便交叉分析在哪种情况下哪些 bean 会被注册。

## Making it simpler : introducing nested `<beans/>` elements
目前为止，`bean definition profile` 给我们提供了一种方便的机制基于部署上下文/环境来决定哪些 beans 被注册，
但这样引来一个问题：本来是一个配置文件，现在不得不使用3个配置文件。

为了区分 `profile="dev"` 和 `profile="production"` 切割配置文件是必须的，因为 profile 属性是设置在 beans 元素上的。
 
在 Spring 3.1 中，在一个配置文件中存在嵌套的` <beans/>` 元素是允许的，这意味着，我们可以仅使用一个配置文件，来实现 profile 的定义：

```
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jdbc="http://www.springframework.org/schema/jdbc"
    xmlns:jee="http://www.springframework.org/schema/jee"
    xsi:schemaLocation="...">

    <bean id="transferService" class="com.bank.service.internal.DefaultTransferService">
        <constructor-arg ref="accountRepository"/>
        <constructor-arg ref="feePolicy"/>
    </bean>

    <bean id="accountRepository" class="com.bank.repository.internal.JdbcAccountRepository">
        <constructor-arg ref="dataSource"/>
    </bean>

    <bean id="feePolicy" class="com.bank.service.internal.ZeroFeePolicy"/>

    <beans profile="dev">
        <jdbc:embedded-database id="dataSource">
            <jdbc:script location="classpath:com/bank/config/sql/schema.sql"/>
            <jdbc:script location="classpath:com/bank/config/sql/test-data.sql"/>
        </jdbc:embedded-database>
    </beans>

    <beans profile="production">
        <jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/datasource"/>
    </beans>
</beans>
```
Spring-beans-3.1.xsd 已经更新，允许这种嵌套，但有一个限制条件，这些嵌套的 `<beans/>` 元素必须位于整个配置文件的最下面( 作为根结点 `<beans/>` 元素的最后面的子元素 )。
虽然这个特性是为了给 `bean definition profiles` 提供支持，但嵌套的 `<beans/>` 元素在其他情况下也非常有用，想象一下有一批 beans 需要设置为 lazy-init="true"，你可以为每个 bean 都设置 lazy-init="true"，但更方便的做法是直接给 `<beans/>` 元素设置 lazy-init="true"，这样其所有的子元素bean 都会继承这个设置。
而嵌套的 `<beans/>` 元素这一支持，使得你不需要专门为这一批 bean 创建一个单独的配置文件，直接嵌套 `<beans/>` 元素即可。

## Caveats
使用 bean definition profiles 时有一些注意事项

- 如果有更简洁的方法来实现目的，不要使用 profiles
如果在各个 profiles 之间唯一的变化是属性值，那么使用 Spring 已经提供的 PropertyPlaceholderConfigurer 或 `<context:property-placeholder />` 可能就够了。


## 2014.5.6 更新
经测试,应该通过全局的 `<context-param>` 来激活 profile, 通过上文中的 `<init-param>` 方式并不能激活相应的 profile
```
<context-param>
    <param-name>spring.profiles.active</param-name>
    <param-value>dev</param-value>
</context-param>
```

尤其当使用西部数码 Java 虚拟主机的 Tomcat 时, 只能对 server.xml 进行配置, 其他配置文件( 包括共享的 web.xml 文件 )是没权限碰的, 
下面是使用 server.xml 中 Context#Parameter 配置来取代 `<context-param>` 方法, 
实际上 server.xml#Context#Parameter 与 web.xml#context-param 的效果是完全一样的, 具体请参考下面的官方资料:
( server.xml )Context Parameter:
http://tomcat.apache.org/tomcat-7.0-doc/config/context.html#Context_Parameters

配置也很简单, ( server.xml )如下:
```
<Context docBase="..." path="" .../>
    <Parameter name="spring.profiles.active" value="product"/>
</Context>
```

针对不同环境的 Tomcat, 分别给 Context ( 容器 )设置相应的 Context Parameter, 这样, 同一个应用就可以在代码不变的情况下部署到各个环境中, 并且能够自动激活当前环境对应的 profile 了
 
另外, 测试时使用 Jetty 服务器我感觉比 Tomcat 要方便很多, 一般使用 Eclipse + RunJettyRun, 此时, server.xml#Context#Parameter 这种方式明显不适合 Jetty, 来看一下如何配置:
![](http://dl2.iteye.com/upload/attachment/0100/6148/4bbdce54-c201-36c7-bebc-7386a35ab187.png)

## 2014.5.22 更新
激活 Profile 的关键元素有哪些？看源码

org.springframework.core.env.StandardEnvironment
```
/** System environment property source name: {@value} */ 
 public static final String SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME = "systemEnvironment";
 
 /** JVM system properties property source name: {@value} */
 public static final String SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME = "systemProperties";
 
@Override
protected void customizePropertySources(MutablePropertySources propertySources) {
    propertySources.addLast(new MapPropertySource(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, getSystemProperties()));
    propertySources.addLast(new SystemEnvironmentPropertySource(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, 
        getSystemEnvironment())); 
 }
```
StandardEnvironment 的子类 StandardServletEnvironment
```
/** Servlet config init parameters property source name: {@value} */ 
 public static final String SERVLET_CONFIG_PROPERTY_SOURCE_NAME = "servletConfigInitParams";
 
/** Servlet context init parameters property source name: {@value} */
 public static final String SERVLET_CONTEXT_PROPERTY_SOURCE_NAME = "servletContextInitParams"; 
 
 /** JNDI property source name: {@value} */
 public static final String JNDI_PROPERTY_SOURCE_NAME = "jndiProperties";
 
@Override
 protected void customizePropertySources(MutablePropertySources propertySources) {
  propertySources.addLast(new StubPropertySource(SERVLET_CONFIG_PROPERTY_SOURCE_NAME));
  propertySources.addLast(new StubPropertySource(SERVLET_CONTEXT_PROPERTY_SOURCE_NAME));
  if (JndiLocatorDelegate.isDefaultJndiEnvironmentAvailable()) {
   propertySources.addLast(new JndiPropertySource(JNDI_PROPERTY_SOURCE_NAME));
  }
  super.customizePropertySources(propertySources);
 }
```
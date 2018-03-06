谈及Spring的Java配置，核心类就是`WebMvcConfigurationSupport`。
我们从`WebMvcConfigurationSupport`这个类开始逐步深入了解Spring的配置原理。

## WebMvcConfigurationSupport

这是提供SpringMVC Java config配置的主要类。通常通过将`@EnableWebMvc`添加到`@Configuration`注解的类来导入它。另一种更高级的方式是直接扩展这个类，并根据需要重写其方法，记住要将`@Configuration`添加到扩展的子类中，并添加`@Bean`到重写的@Bean方法。有关更多详细信息，请参阅`@EnableWebMvc`的Javadoc。

##### 这个类会注册下面的HandlerMappings：

- RequestMappingHandlerMapping排序索引为0，将请求映射到控制器方法。
- HandlerMapping排序索引为1，直接映射URL路径到视图名称。
- BeanNameUrlHandlerMapping排序索引为2，以将URL路径映射到控制器bean名称。
- HandlerMapping排序索引为Integer.MAX_VALUE-1，以提供静态资源请求。
- HandlerMapping排序索引为Integer.MAX_VALUE，将请求转发到默认的servlet。

##### 注册这些HandlerAdapter：

- RequestMappingHandlerAdapter用于使用控制器方法处理请求。
- HttpRequestHandlerAdapter用于使用HttpRequestHandlers处理请求。
- SimpleControllerHandlerAdapter用于使用_interface-based_控制器处理请求。

##### 用这个异常解析器链注册一个HandlerExceptionResolverComposite：

- ExceptionHandlerExceptionResolver用于通过@ExceptionHandler方法处理异常。
- ResponseStatusExceptionResolver用于使用@ResponseStatus注解的异常。
- DefaultHandlerExceptionResolver用于解析已知的Spring异常类型

##### 注册AntPathMatcher和UrlPathHelper以供以下用户使用：

- RequestMappingHandlerMapping
- ViewControllers的HandlerMapping
- 用于服务资源的HandlerMapping

请注意，这些bean可以使用PathMatchConfigurer进行配置。

默认情况下，RequestMappingHandlerAdapter和ExceptionHandlerExceptionResolver都使用以下默认实例进行配置：

- 一个ContentNegotiationManager
- 一个DefaultFormattingConversionService
- 一个org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean(如果JSR-303的实现存在于类路径中)
- 一系列HttpMessageConverters，这取决于类路径上可用的第三方库。

## @Configuration

首先看下声明：

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Configuration {
	String value() default "";
}
```

注意，`@Component`是`@Configuration`元注解，也即它具备`@Component`的特性。

指示一个类声明一个或多个@Bean方法，并且可以由Spring容器处理，以便在运行时为这些bean生成bean定义和处理请求，例如：

```java
@Configuration
 public class AppConfig {

     @Bean
     public MyBean myBean() {
         // instantiate, configure and return bean ...
     }
 }
```

### 引导@Configuration类

##### 通过AnnotationConfigApplicationContext

`@Configuration`类通常使用`AnnotationConfigApplicationContext`或web版本`AnnotationConfigWebApplicationContext`进行引导。 前者的一个简单例子如下：

```java
AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
 ctx.register(AppConfig.class);
 ctx.refresh();
 MyBean myBean = ctx.getBean(MyBean.class);
 // use myBean ...
```

有关更多详细信息，请参阅`AnnotationConfigApplicationContext` Javadoc，有关`web.xml`配置说明，请参阅`AnnotationConfigWebApplicationContext`。

##### 通过Spring `<beans>` XML

作为直接针对`AnnotationConfigApplicationContext`注册`@Configuration`类的替代方法，可以在Spring XML文件中将`@Configuration`类声明为`<bean>`定义：

```xml
 <beans>
    <context:annotation-config/>
    <bean class="com.acme.AppConfig"/>
 </beans>
```

在上面的示例中，为了启用`ConfigurationClassPostProcessor`和其他与注解有关的后置处理器来处理`@Configuration`类，需要`<context:annotation-config/>`。

##### 通过组件扫描

`@Component`是`@Configuration`元注解，因此`@Configuration`类是组件扫描的候选对象（通常使用Spring XML的`<context:component-scan/>`元素）。

`@Configuration`类不仅可以通过组件扫描进行引导，还可以自己使用`@ComponentScan`注解来配置组件扫描：

```java
@Configuration
@ComponentScan("com.acme.app.services")
public class AppConfig {
    // various @Bean definitions ...
}
```

有关详细信息，请参阅`@ComponentScan` javadoc。

>**_@ComponentScan_**
>
>配置用于`@Configuration`类的组件扫描指令。
>提供与Spring XML `<context:component-scan>`元素并行的支持。
>
>可以指定basePackageClasses()或basePackages()（或其别名value()）来定义要扫描的特定类包。
如果未定义特定的包，则将从声明此注解的类的包中进行扫描。
>
>请注意，`<context:component-scan>`元素具有`annotation-config`属性; 但是，这个注解没有。
这是因为在几乎所有使用`@ComponentScan`的情况下，默认的_annotation config processing_（例如处理@Autowired之类）_is assumed_。
此外，当使用`AnnotationConfigApplicationContext`和web版本`AnnotationConfigWebApplicationContext`时，_annotation config processors_总是被注册，这意味着任何试图在`@ComponentScan`级别禁用它们的尝试都将被忽略。
>
>有关使用示例，请参阅@Configuration的Javadoc。
>
> 💡 
>- `<context:annotation-config/>`启用`ConfigurationClassPostProcessor`和其他与注解有关的后置处理器来处理`@Configuration`类。
>- `<context:component-scan>`的`annotation-config`属性作用同`<context:annotation-config/>`。
>- `@ComponentScan`默认的_annotation config processing_（例如处理@Autowired之类）_is assumed_。此外，当使用`AnnotationConfigApplicationContext`和web版本`AnnotationConfigWebApplicationContext`时，_annotation config processors_总是被注册。

### 使用外部的值

##### 使用Environment API

通过使用`@Autowired`或`@Inject`注解将Spring Environment注入`@Configuration`类，来查找外部的值：

```java
@Configuration
 public class AppConfig {

     @Inject Environment env;

     @Bean
     public MyBean myBean() {
         MyBean myBean = new MyBean();
         myBean.setName(env.getProperty("bean.name"));
         return myBean;
     }
 }
```

通过Environment解析的属性属于一个或多个"属性源"对象，而`@Configuration`类可以使用`@PropertySources`注解向Environment对象提供属性源：

```java
@Configuration
 @PropertySource("classpath:/com/acme/app.properties")
 public class AppConfig {

     @Inject Environment env;

     @Bean
     public MyBean myBean() {
         return new MyBean(env.getProperty("bean.name"));
     }
 }
```

有关更多详细信息，请参阅Environment和@PropertySource Javadoc。

##### 使用@Value注解

外部的值可以通过`@Value`注解注入到`@Configuration`类中：

```java
@Configuration
 @PropertySource("classpath:/com/acme/app.properties")
 public class AppConfig {

     @Value("${bean.name}") String beanName;

     @Bean
     public MyBean myBean() {
         return new MyBean(beanName);
     }
 }
```

这种方法在使用Spring的`PropertySourcesPlaceholderConfigurer`时非常有用，通常通过XML `<context:property-placeholder/>`来启用。

有关使用`BeanFactoryPostProcessor`类型（PropertySourcesPlaceholderConfigurer）的详细信息，请参阅下面有关使用`@ImportResource`导入Spring XML来构造@Configuration类的部分，@Value Javadoc，@Bean Javadoc。

### 构造@Configuration类

##### 用@Import注解


`@Configuration`类可以使用`@Import`注解构造，与`<import>`在Spring XML中的工作方式相似。 由于`@Configuration`类对象是作为容器内的Spring bean进行管理的，因此可以使用`@Autowired`或`@Inject`注入导入的配置：

```java
@Configuration
 public class DatabaseConfig {

     @Bean
     public DataSource dataSource() {
         // instantiate, configure and return DataSource
     }
 }

 @Configuration
 @Import(DatabaseConfig.class)
 public class AppConfig {

     @Inject DatabaseConfig dataConfig;

     @Bean
     public MyBean myBean() {
         // reference the dataSource() bean method
         return new MyBean(dataConfig.dataSource());
     }
 }
```

现在，`AppConfig`和导入的`DatabaseConfig`都可以通过在Spring上下文中注册`AppConfig`来引导：new AnnotationConfigApplicationContext(AppConfig.class);

##### 用@Profile注解

`@Configuration`类可以使用`@Profile`注解标记，以表明只有给定的一个或多个`profile`处于`active`时才应该处理它们：

```java
@Profile("embedded")
 @Configuration
 public class EmbeddedDatabaseConfig {

     @Bean
     public DataSource dataSource() {
         // instantiate, configure and return embedded DataSource
     }
 }

 @Profile("production")
 @Configuration
 public class ProductionDatabaseConfig {

     @Bean
     public DataSource dataSource() {
         // instantiate, configure and return production DataSource
     }
 }
```

有关更多详细信息，请参阅@Profile和Environment javadocs。

##### 使用@ImportResource注解导入Spring XML


如上所述，`@Configuration`类可以在Spring XML文件中声明为常规的Spring `<bean>`定义。也可以使用`@ImportResource`注解将Spring XML配置文件导入到`@Configuration`类中。 从XML导入的Bean定义可以使用`@Autowired`或`@Inject`注入：

```java
@Configuration
 @ImportResource("classpath:/com/acme/database-config.xml")
 public class AppConfig {

     @Inject DataSource dataSource; // from XML

     @Bean
     public MyBean myBean() {
         // inject the XML-defined dataSource bean
         return new MyBean(this.dataSource);
     }
 }
```

##### 嵌套的@Configuration类

`@Configuration`类可以如下嵌套在一起：

```java
@Configuration
 public class AppConfig {

     @Inject DataSource dataSource;

     @Bean
     public MyBean myBean() {
         return new MyBean(dataSource);
     }

     @Configuration
     static class DatabaseConfig {
         @Bean
         DataSource dataSource() {
             return new EmbeddedDatabaseBuilder().build();
         }
     }
 }
```

当引导这样的配置时，只有`AppConfig`需要针对应用上下文进行注册。由于是一个嵌套的`@Configuration`类，`DatabaseConfig`将被自动注册。这样可以避免使用`@Import`注解。

### 配置延迟初始化

默认情况下，@Bean方法将在容器引导时被迫切地实例化。
为了避免这种情况，可以将`@Configuration`与`@Lazy`注解结合使用，以表明在类中声明的所有@Bean方法在默认情况下是懒惰地初始化的。请注意`@Lazy`也可以用于单独的@Bean方法。

### Testing对@Configuration类的支持

spring-test模块中提供的Spring TestContext框架提供`@ContextConfiguration`注解，从Spring 3.1开始，它可以接受一个`@Configuration` Class对象的数组：

```java
@RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes={AppConfig.class, DatabaseConfig.class})
 public class MyTests {

     @Autowired MyBean myBean;

     @Autowired DataSource dataSource;

     @Test
     public void test() {
         // assertions against myBean ...
     }
 }
```

有关详细信息，请参阅TestContext框架参考文档。

### 使用@Enable注解启用内置的Spring功能

诸如异步方法执行，计划任务执行，注解驱动事务管理，甚至Spring MVC等Spring特性可以使用各自的`@Enable*`注解在`@Configuration`类中启用和配置。有关详细信息，请参阅`@EnableAsync`，`@EnableScheduling`，`@EnableTransactionManagement`，`@EnableAspectJAutoProxy`和`@EnableWebMvc`。

## @EnableWebMvc

看下声明：

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DelegatingWebMvcConfiguration.class)
public @interface EnableWebMvc {
}
```

将此注解添加到`@Configuration`类中，从`WebMvcConfigurationSupport`导入Spring MVC配置，例如：

```java
@Configuration
 @EnableWebMvc
 @ComponentScan(basePackageClasses = { MyConfiguration.class })
 public class MyWebConfiguration {

 }
```

要自定义导入的配置，请实现`WebMvcConfigurer`接口，或者更好的方式是扩展包含一系列空方法的基类`WebMvcConfigurerAdapter`并覆盖单个方法，例如：

```java
@Configuration
 @EnableWebMvc
 @ComponentScan(basePackageClasses = { MyConfiguration.class })
 public class MyConfiguration extends WebMvcConfigurerAdapter {

           @Override
           public void addFormatters(FormatterRegistry formatterRegistry) {
         formatterRegistry.addConverter(new MyConverter());
           }

           @Override
           public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
         converters.add(new MyHttpMessageConverter());
           }

     // More overridden methods ...
 }
```


如果`WebMvcConfigurer`没有暴露某些需要配置的高级设置，请考虑删除`@EnableWebMvc`注解并直接扩展`WebMvcConfigurationSupport`或`DelegatingWebMvcConfiguration`，例如：

```java
@Configuration
 @ComponentScan(basePackageClasses = { MyConfiguration.class })
 public class MyConfiguration extends WebMvcConfigurationSupport {

           @Override
           public void addFormatters(FormatterRegistry formatterRegistry) {
         formatterRegistry.addConverter(new MyConverter());
           }

           @Bean
           public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
         // Create or delegate to "super" to create and
         // customize properties of RequestMappingHandlerAdapter
           }
 }
```

## DelegatingWebMvcConfiguration

声明如下：

```java
@Configuration
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {
```

`WebMvcConfigurationSupport`的一个子类，用于检测并委托所有类型为`WebMvcConfigurer`的Bean，使其可以自定义由`WebMvcConfigurationSupport`提供的配置。 这是由`@EnableWebMvc`实际导入的类。

## 总结

上面介绍了几个核心的API，下面说下他们彼此之间是如何关联，以及如何起作用的。

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DelegatingWebMvcConfiguration.class)
public @interface EnableWebMvc {
}
```

一、用户创建Java Config配置类，并使用`@Configuration`注解注释。

二、引导@Configuration配置类，上面提到三种方式：

1. 通过`AnnotationConfigApplicationContext`
  
    `@Configuration`类通常使用`AnnotationConfigApplicationContext`或web版本`AnnotationConfigWebApplicationContext`进行引导。 

    前者的一个简单例子如下：

    ```java
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.register(AppConfig.class);
    ctx.refresh();
    MyBean myBean = ctx.getBean(MyBean.class);
    // use myBean ...
    ```

2. 通过Spring `<beans>` XML

    在Spring XML文件中将`@Configuration`类声明为`<bean>`定义

     ```xml
    <beans>
        <context:annotation-config/>
        <bean class="com.acme.AppConfig"/>
     </beans>
    ```

    >- `<context:annotation-config/>`启用`ConfigurationClassPostProcessor`和其他与注解有关的后置处理器来处理`@Configuration`类。
    >- `<context:component-scan>`的`annotation-config`属性作用同`<context:annotation-config/>`。

3. 通过组件扫描

    `@Component`是`@Configuration`元注解，因此`@Configuration`类是组件扫描的候选对象。

    可以自己使用`@ComponentScan`注解来配置组件扫描：

    ```java
     @Configuration
     @ComponentScan("com.acme.app.services")
     public class AppConfig {
         // various @Bean definitions ...
     }
    ```

    > _TODO_ `@ComponentScan`默认的_annotation config processing_（例如处理@Autowired之类）_is assumed_。此外，当使用`AnnotationConfigApplicationContext`和web版本`AnnotationConfigWebApplicationContext`时，_annotation config processors_总是被注册。

三、将`@EnableWebMvc`注解添加到`@Configuration`类中，从`WebMvcConfigurationSupport`导入Spring MVC配置

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DelegatingWebMvcConfiguration.class)
public @interface EnableWebMvc {
}
```

`@EnableWebMvc`注解通过`@Import(DelegatingWebMvcConfiguration.class)`导入Spring MVC配置。

`DelegatingWebMvcConfiguration`类中又通过如下方法注入了`WebMvcConfigurer`，用于导入用于的自定义配置。

可以看到，

```java
@Autowired(required = false)
public void setConfigurers(List<WebMvcConfigurer> configurers) {
if (configurers == null || configurers.isEmpty()) {
return;
}
this.configurers.addWebMvcConfigurers(configurers);
}
```

通过`@Autowired(required = false)`注入了上下文中所有类型为`WebMvcConfigurer`的bean，其中required为false，说明自定义配置是可选的)。如果你创建的配置类实现`WebMvcConfigurer`接口，并交给Spring去管理，则会被注入到`WebMvcConfigurerComposite`中。

`WebMvcConfigurerComposite`的声明如下：

```java
class WebMvcConfigurerComposite implements WebMvcConfigurer {

    private final List<WebMvcConfigurer> delegates = new ArrayList<WebMvcConfigurer>();

    public void addWebMvcConfigurers(List<WebMvcConfigurer> configurers) {
        if (configurers != null) {
            this.delegates.addAll(configurers);
        }
    }
```

`WebMvcConfigurerComposite`维护了一个`WebMvcConfigurer`的List集合，`addWebMvcConfigurers`方法将所有的自定义配置加入该集合中。


_TODO_

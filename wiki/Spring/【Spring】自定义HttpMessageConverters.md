为`Spring Boot`应用程序暴露基于REST的端点，或者对于这一点，一个直接的SpringMVC应用程序是直截了当的，以下是一个控制器，根据发送给它的内容创建一个实体：

```java
@RestController
@RequestMapping("/rest/hotels")
public class RestHotelController {
        ....
@RequestMapping(method=RequestMethod.POST)
public Hotel create(@RequestBody @Valid Hotel hotel) {
    return this.hotelRepository.save(hotel);
}
```

Spring MVC内部使用一个称为`HttpMessageConverter`的组件将Http请求转换为对象表示并返回。

自动注册一组默认转换器，支持一系列不同的资源表示格式 - 例如json，xml。

现在，如果需要以某种方式自定义消息转换器，那么Spring Boot会使这简单化。 

例如，如果上述示例中的POST方法需要更灵活，并且应该忽略Hotel实体中不存在的属性 - 通常可以通过配置Jackson ObjectMapper来完成。
使用`Spring Boot`完成这些，所需要做的是创建一个新的HttpMessageConverter bean，最终将覆盖所有默认消息转换器，如下所示：
```java
@Bean
public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
    MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    jsonConverter.setObjectMapper(objectMapper);
    return jsonConverter;
}
```
这对于`Spring-Boot`应用程序很有效，但是对于不使用`Spring-Boot`的直接Spring MVC应用程序，配置自定义转换器要复杂一点 - 默认转换器默认情况下不会注册，最终用户必须明确注册默认值：

```java
@Configuration
public class WebConfig extends WebMvcConfigurationSupport {
    @Bean
    public MappingJackson2HttpMessageConverter customJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonConverter.setObjectMapper(objectMapper);
        return jsonConverter;
    }
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(customJackson2HttpMessageConverter());
        super.addDefaultHttpMessageConverters();
    }
}
```
这里`WebMvcConfigurationSupport`提供了一种更精细地调整基于Spring的应用程序的MVC层配置的方法。 
在`configureMessageConverters`方法中，注册自定义转换器，然后进行显式调用，以确保默认值也被注册。 
比基于`Spring-Boot`的应用程序多一点工作。

**扩展：**
```java
public class WebMvcConfigurationSupport implements ApplicationContextAware, ServletContextAware {}

...
//spring提供了WebMvcConfigurationSupport的实现类DelegatingWebMvcConfiguration
@Configuration
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {}
```
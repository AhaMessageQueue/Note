## Spring and jackson, how to disable FAIL_ON_EMPTY_BEANS through @ResponseBody

>https://stackoverflow.com/questions/28862483/spring-and-jackson-how-to-disable-fail-on-empty-beans-through-responsebody
### Q
Spring里有没有一个全局的配置，能够对所有标注注解`@ResponseBody`的控制器设置`SerializationFeature.FAIL_ON_EMPTY_BEANS`不可用。

### A
这里有一篇[文章](../SpringBoot/【SpringBoot】自定义HttpMessageConverters.md)介绍了自定义HttpMessageConverter;

创建一个自定义的MyJsonMapper类来扩展ObjectMapper，然后使用它
```java
public class MyJsonMapper extends ObjectMapper {    
        public MyJsonMapper() {
            this.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        }
}
```
xml:
```xml
<mvc:annotation-driven>
        <mvc:message-converters>
            <bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter">
                <property name="objectMapper" ref="jacksonObjectMapper" />
            </bean>
        </mvc:message-converters>
</mvc:annotation-driven>

<bean id="jacksonObjectMapper" class="com.mycompany.example.MyJsonMapper" >
```

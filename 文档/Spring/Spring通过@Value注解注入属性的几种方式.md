## 场景
假如有以下属性文件dev.properties, 需要注入下面的tag:
```
tag=123
```
### 通过PropertyPlaceholderConfigurer
```
<bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer"><!-- 属性占位符配置 -->
    <property name="location" value="dev.properties" />
</bean>
```
**代码:**
```
@Value("${tag}")
private String tag;
```
### 通过PreferencesPlaceholderConfigurer
```
<bean id="appConfig" class="org.springframework.beans.factory.config.PreferencesPlaceholderConfigurer">
    <property name="location" value="dev.properties" />
</bean>
```
**代码：**
```
@Value("${tag}")
private String tag;
```
## 通过PropertiesFactoryBean
```
<bean id="config" class="org.springframework.beans.factory.config.PropertiesFactoryBean">
    <property name="location" value="dev.properties" />
</bean>
```
**代码：**
```
@Value("#{config['tag']}")
private String tag;
```

## 通过util:properties
效果同PropertiesFactoryBean一样 代码：
```
@Value("#{config['tag']}")
private String tag;
```
## 其他方式
有时也可以不通过文件，直接写字面量
```
<bean id="appConfig" class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
    <!--
        <property name="location" value="classpath:${env}.properties" />
    -->
    <property name="properties">
        <props>
            <prop key="tag">123</prop>
        </props>
    </property>
</bean>
```
**代码：**
```
@Value("${tag}")
private String tag;
```

> 20170215补充如下：
## 代码方式注入
直接在Java类中通过注解实现配置文件加载
**在java类中引入配置文件**
```
@Configuration
@PropertySource("classpath:client/oauth2.properties")
public class Oauth2Config {

    @Value("${data_store_dir}")
    public String data_store_dir;

    @Value("${redirect_url}")
    public String redirect_url;
}
```
```
package com.github.spring.base4j.service.api;

import com.github.spring.base4j.config.Oauth2Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by 刘春龙 on 2017/2/15.
 * 测试Controller
 * 测试地址：http://127.0.0.1:8080/spring-base4j/spring/tom/say
 */
@Controller
@RequestMapping("spring")
public class SpringController {

    @Autowired
    Environment environment;
    @Autowired
    Oauth2Config oauth2Config;

    @RequestMapping(value = "{username}/say", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody//通过@ResponseBody返回json数据，否则会被当作视图进行转发
    public String sayHello(@PathVariable("username") String username) {
        return username + ", env.data_store_dir:" + environment.getProperty("data_store_dir") + ", oauth2Config.redirect_url:" + oauth2Config.redirect_url;
    }
}

```
>注：类成员变量不可为static
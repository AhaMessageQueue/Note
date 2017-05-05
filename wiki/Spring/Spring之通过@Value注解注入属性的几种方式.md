**Spring之通过@Value注解注入属性的几种方式:**

### 场景描述
假如有以下属性文件`dev.properties`, 代码中需要注入下面的tag:
```
tag=123
```
### 一、通过PropertyPlaceholderConfigurer配置
```
<bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
    <property name="location" value="dev.properties" />
</bean>
```
**代码:**
```
@Value("${tag}")
private String tag;
```
### 二、通过PreferencesPlaceholderConfigurer配置
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
## 三、通过PropertiesFactoryBean配置
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

## 四、通过util:properties配置
效果同`PropertiesFactoryBean`一样 代码：
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

## 代码方式注入
直接在Java类中通过注解实现配置文件加载
**在Java类中引入配置文件**
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
        return username + ", env.data_store_dir:" + environment.getProperty("data_store_dir") + ", config.redirect_url:" + oauth2Config.redirect_url;
    }
}

```
>注：
>类成员变量不可为static
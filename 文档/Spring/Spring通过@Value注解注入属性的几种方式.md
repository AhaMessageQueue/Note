### 场景
假如有以下属性文件dev.properties, 需要注入下面的tag
```
tag=123
```

### 通过PropertyPlaceholderConfigurer
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
### 通过PropertiesFactoryBean
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
### 通过util:properties
效果同PropertiesFactoryBean一样
**代码：**
```
@Value("#{config['tag']}")
private String tag;
```

### 其他方式
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
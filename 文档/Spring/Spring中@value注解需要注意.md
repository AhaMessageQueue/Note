首先，@value需要参数，这里参数可以是两种形式：
```
@Value("#{configProperties['t1.msgname']}")
@Value("${t1.msgname}")
```
其次，下面我们来看看如何使用这两形式，在配置上有什么区别：
1.@Value("#{configProperties['t1.msgname']}")这种形式的配置中有“configProperties”， 其实它指定的是配置文件的加载对象：配置如下：
```
<bean id="configProperties" class="org.springframework.beans.factory.config.PropertiesFactoryBean">
    <property name="locations">
        <list>
            <value>classpath:/config/t1.properties</value>
        </list>
    </property>
</bean>
```
这样配置就可完成对属性的具体注入了；

2、@Value("${t1.msgname}")这种形式不需要指定具体加载对象，这时候需要一个关键的对象来完成`PreferencesPlaceholderConfigurer`，
这个对象的配置可以利用上面配置1中的配置， 也可以自己直接自定配置文件路径。 如果使用配置1中的配置，可以写成如下情况：
```
<bean id="propertyConfigurer" class="org.springframework.beans.factory.config.PreferencesPlaceholderConfigurer">
    <property name="properties" ref="configProperties"/>
</bean>
```
如果直接指定配置文件的话，可以写成如下情况：
```
<bean id="propertyConfigurer" class="org.springframework.beans.factory.config.PreferencesPlaceholderConfigurer">
    <property name="location">
        <value>config/t1.properties</value>
    </property>
</bean>
```
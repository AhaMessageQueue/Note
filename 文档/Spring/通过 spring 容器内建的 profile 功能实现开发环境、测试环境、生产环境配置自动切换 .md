软件开发的一般流程为工程师开发 -> 测试 -> 上线，因此就涉及到三个不同的环境，开发环境、测试环境以及生产环境，
通常这三个环境会有很多配置参数不同，例如数据源、文件路径、url等，如果每次上线一个新版本时都手动修改配置会十分繁琐，容易出错。
spring 为我们提供了 profile 机制来解决这个问题。

spring允许我们通过定义 profile 来将若干不同的 bean 定义组织起来，从而实现不同环境自动激活不同的 profile 来切换配置参数的功能，
下面介绍以 xml 的方式定义 profile、如何激活 profile以及定义默认的 profile，整个过程我以配置不同环境的数据源为例，
为了简化配置，这里假设只有开发和生产两个环境。

数据源定义为：
```
<bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">  
    <property name="user" value="${jdbc.user}" />   
    <property name="password" value="${jdbc.password}" />   
    <property name="jdbcUrl" value="${jdbc.jdbcUrl}" />     
    <property name="driverClass" value="${jdbc.driverClass}" />  
    <property name="initialPoolSize" value="${c3p0.initialPoolSize}"/>  
    <property name="acquireIncrement" value="${c3p0.acquireIncrement}"/>  
    <property name="minPoolSize" value="${c3p0.minPoolSize}"/>  
    <property name="maxIdleTime" value="${c3p0.maxIdleTime}"/>  
    <property name="idleConnectionTestPeriod" value="${c3p0.idleConnectionTestPeriod}" />  
    <property name="preferredTestQuery" value="${c3p0.preferredTestQuery}"/>  
</bean> 
```
classpath下外部资源文件有两个 settings-development.properties 和 settings-production.properties，分别是开发环境和生产环境的数据源配置参数，内容如下

settings-development.properties:
```
jdbc.user=root  
jdbc.password=111111  
jdbc.driverClass=com.mysql.jdbc.Driver  
jdbc.jdbcUrl=jdbc:mysql://localhost:3306/xxx  
c3p0.minPoolSize=5   
c3p0.initialPoolSize=5  
c3p0.acquireIncrement=5  
c3p0.maxIdleTime=3600  
c3p0.idleConnectionTestPeriod=3600  
c3p0.preferredTestQuery=select 1  
```
settings-production.properties:
```
jdbc.user=xxx  
jdbc.password=xxxx  
jdbc.driverClass=com.mysql.jdbc.Driver  
jdbc.jdbcUrl=jdbc:mysql:///xxx  
c3p0.minPoolSize=20   
c3p0.initialPoolSize=20  
c3p0.acquireIncrement=10  
c3p0.maxIdleTime=3600  
c3p0.idleConnectionTestPeriod=3600  
c3p0.preferredTestQuery=select 1  
```
###  定义 profile
现在就可以通过定义 profile 来将开发和生产环境的数据源配置分开，这里我们定义两个 profile，一个名称是 development，另一个名称是 production
```
<!-- 开发环境配置文件 -->  
<beans profile="development">  
    <context:property-placeholder location="classpath:settings-development.properties"/>  
</beans>  
   
<!-- 生产环境配置文件 -->  
<beans profile="production">  
    <context:property-placeholder location="classpath:settings-production.properties"/>  
</beans>  
```

### 定义默认 profile
默认 profile 是指在没有任何 profile 被激活的情况下，默认 profile 内定义的内容将被使用，通常可以在 web.xml 中定义全局 servlet 上下文参数 spring.profiles.default 实现，代码如下

```
<!-- 配置spring的默认profile -->  
<context-param>  
    <param-name>spring.profiles.default</param-name>  
    <param-value>development</param-value>  
</context-param>  
```
### 激活 profile
spring 为我们提供了大量的激活 profile 的方法，可以通过代码来激活，也可以通过系统环境变量、JVM参数、servlet上下文参数来定义 spring.profiles.active 参数激活 profile，这里我们通过定义 JVM 参数实现。
在生产环境中，以 tomcat 为例，我们在 tomcat 的启动脚本中加入以下 JVM 参数
```
    -Dspring.profiles.active="production"  
```
而开发环境中不需要定义该参数，如果不定义，则会使用我们指定的默认 profile ，在这里也就是名称为 development 的 profile。这样当项目部署到不同的环境时，便可以通过 JVM 参数来实现不同环境 profile 自动激活。

### 延伸
该参数还可以延伸，以至于我们可以在 java 代码中继续通过该参数来区分不同的环境，从而执行不同的操作，代码如下

```
//定义环境变量
public class Config {
      public static String ENV = "development";
}
public class InitConfigListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent sce) {
        //侦测jvm环境，并缓存到全局变量中
        String env = System.getProperty("spring.profiles.active");
        if(env == null) {
            Config.ENV = "development";
        } else {
            Config.ENV = env;
        }
    }
}
```
在项目初始化时获取到该参数后，我们便可以在项目任何位置使用，来执行一些不同环境需要区别对待的操作，例如统计流量的代码只需要在生产环境激活，就可以在jsp中加入如下代码

```
<!-- 生产环境统计、推送代码 -->
<c:if test="${env == 'production' }">
<script>
//统计代码
..
</script>
</c:if> 
```
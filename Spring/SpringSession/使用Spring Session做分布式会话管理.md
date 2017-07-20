# 基于XML配置

在Web项目开发中，会话管理是一个很重要的部分，用于存储与用户相关的数据。通常是由符合session规范的容器来负责存储管理，也就是一旦容器关闭，重启会导致会话失效。因此打造一个高可用性的系统，必须将session管理从容器中独立出来。而这实现方案有很多种，下面简单介绍下：

　　第一种是使用容器扩展来实现，大家比较容易接受的是通过容器插件来实现，比如基于Tomcat的[tomcat-redis-session-manager](https://github.com/jcoleman/tomcat-redis-session-manager)，基于Jetty的[jetty-session-redis](https://github.com/Ovea/jetty-session-redis)等等。好处是对项目来说是透明的，无需改动代码。不过前者目前还不支持Tomcat 8，或者说不太完善。个人觉得由于过于依赖容器，一旦容器升级或者更换意味着又得从新来过。并且代码不在项目中，对开发者来说维护也是个问题。

　　第二种是自己写一套会话管理的工具类，包括Session管理和Cookie管理，在需要使用会话的时候都从自己的工具类中获取，而工具类后端存储可以放到Redis中。很显然这个方案灵活性最大，但开发需要一些额外的时间。并且系统中存在两套Session方案，很容易弄错而导致取不到数据。

　　第三种是使用框架的会话管理工具，也就是本文要说的[spring-session](http://docs.spring.io/spring-session/docs/current/reference/html5/)，可以理解是替换了Servlet那一套会话管理，既不依赖容器，又不需要改动代码，并且是用了spring-data-redis那一套连接池，可以说是最完美的解决方案。当然，前提是项目要使用Spring Framework才行。

这里简单记录下整合的过程：

如果项目之前没有整合过spring-data-redis的话，这一步需要先做，在maven中添加这两个依赖：

```xml
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-redis</artifactId>
    <version>1.5.2.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session</artifactId>
    <version>1.0.2.RELEASE</version>
</dependency>
```

再在applicationContext.xml中添加以下bean，用于定义redis的连接池和初始化redis模版操作类，自行替换其中的相关变量。
```xml
<!-- redis -->
<bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
    <property name="maxTotal" value="${redis.pool.maxTotal}"/>
    <property name="maxIdle" value="${redis.pool.maxIdle}"/>
    <property name="timeBetweenEvictionRunsMillis" value="${redis.pool.timeBetweenEvictionRunsMillis}"/>
    <property name="minEvictableIdleTimeMillis" value="${redis.pool.minEvictableIdleTimeMillis}"/>
    <property name="testOnBorrow" value="${redis.pool.testOnBorrow}"/>
</bean>

<bean id="jedisConnFactory"
      class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory">
    <property name="hostName" value="${redis.pool.host}"/>
    <property name="port" value="${redis.pool.port}"/>
    <property name="usePool" value="true"></property>
    <property name="poolConfig" ref="jedisPoolConfig"/>
</bean>

<!-- Redis Template -->
<bean id="redisTemplate" class="org.springframework.data.redis.core.StringRedisTemplate">
    <property name="connectionFactory" ref="jedisConnFactory" />
</bean>
 
<!-- 将session放入redis -->
<bean id="redisHttpSessionConfiguration"
class="org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration">
    <property name="maxInactiveIntervalInSeconds" value="1800" />
</bean>
```
这里前面几个bean都是操作redis时候使用的，最后一个bean才是spring-session需要用到的，其中的id可以不写或者保持不变，这也是一个约定优先配置的体现。
这个bean中又会自动产生多个bean，用于相关操作，极大的简化了我们的配置项。

其中有个比较重要的是springSessionRepositoryFilter，它将在下面的代理filter中被调用到。

maxInactiveIntervalInSeconds表示超时时间，默认是1800秒。

写上述配置的时候我个人习惯采用xml来定义，官方文档中有采用注解来声明一个配置类。

然后是在web.xml中添加一个session代理filter，通过这个filter来包装Servlet的getSession()。
需要注意的是这个filter需要放在所有filter链最前面。

```xml
<!-- delegatingFilterProxy -->
<filter>
    <filter-name>springSessionRepositoryFilter</filter-name>
    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
</filter>
<filter-mapping>
    <filter-name>springSessionRepositoryFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

这样便配置完毕了，需要注意的是，spring-session要求Redis Server版本不低于2.8。

验证：使用redis-cli就可以查看到session key了，且浏览器Cookie中的jsessionid已经替换为session。

```text
127.0.0.1:6379> KEYS *
1) "spring:session:expirations:1440922740000"
2) "spring:session:sessions:35b48cb4-62f8-440c-afac-9c7e3cfe98d3"
```
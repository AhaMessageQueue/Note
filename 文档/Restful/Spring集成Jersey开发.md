下文将会初步介绍如何在Spring中集成Jersey

所依赖的技术版本：

    Jersey 1.8
    Spring 3.0.5.RELEASE

### 项目依赖
pom.xml定义(注意去除jersey中引入的低版本的spring包)
```
<!-- Jersey -->
<dependency>
    <groupId>com.sun.jersey</groupId>
    <artifactId>jersey-server</artifactId>
    <version>1.8</version>
</dependency>

<!-- Spring 3 dependencies -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-core</artifactId>
    <version>3.0.5.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>3.0.5.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>3.0.5.RELEASE</version>
</dependency>

<!-- Jersey + Spring -->
<dependency>
    <groupId>com.sun.jersey.contribs</groupId>
    <artifactId>jersey-spring</artifactId>
    <version>1.8</version>
    <exclusions>
        <exclusion>
            <groupId>org.springframework</groupId>
            <artifactId>spring</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.springframework</groupId>
            <artifactId>spring-beans</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
        </exclusion>
    </exclusions>
</dependency>

```

### Spring Bean
定义一个名为“demoService”的bean，稍后我们会将其注入到jersey资源中

接口:
```
package org.qunyang.service;
public interface DemoService{
 
    String say();
 
}
```
实现类:
```
package org.qunyang.service.impl;
import org.qunyang.service.DemoService;
public class DemoServiceImpl implements DemoService {
    public String say() {
        return "Hello Jersey";
    }
}
```
spring配置:
```
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans-3.0.xsd 
    http://www.springframework.org/schema/context
    http://www.springframework.org/schema/context/spring-context-3.0.xsd">
 
    <context:component-scan base-package="org.qunyang.*" />
    
    <bean id="demoService" class="org.qunyang.service.impl.DemoServiceImpl" />
 
</beans>
```

### Jersey
我们将刚才声明的的bean通过自动注入的方式注入到jersey资源中
```
package org.qunyang.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.qunyang.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/demo")
public class RestfulService {

    @Autowired
    DemoService demoService;

    @GET
    @Path("/hello")
    public String sayHello() {
        return demoService.say();

    }

}
```
### 集成Spring和Jersey
集成最后的一步，也是最重要的一步，打开web.xml
```
<web-app id="WebApp_ID" version="2.4"
    xmlns="http://java.sun.com/xml/ns/j2ee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee 
    http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd">
    <display-name>Restful Web Application</display-name>

    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath:applicationContext.xml</param-value>
    </context-param>

    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>

    <servlet>
        <servlet-name>jersey-serlvet</servlet-name>
        <servlet-class>com.sun.jersey.spi.spring.container.servlet.SpringServlet</servlet-class>
        <init-param>
            <param-name>com.sun.jersey.config.property.packages</param-name>
            <param-value>com.mkyong.rest</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>jersey-serlvet</servlet-name>
        <url-pattern>/rest/*</url-pattern>
    </servlet-mapping>

</web-app>
```

这里有一点需要注意：

Jersey的servlet-class一项，一定是上面所示的：
```
<servlet-class>com.sun.jersey.spi.spring.container.servlet.SpringServlet</servlet-class>
```
请不要换成其它的类，以免发生bean不能正常注入的问题.

### 运行结果
![](http://images.cnitblog.com/blog/316152/201309/18153900-8a60209d97264fa6b112a04e47579ffc.png)
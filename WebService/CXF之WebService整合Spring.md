>spring4.2.0以上需要使用cxf3.x以上的版本

首先，CXF和spring整合需要准备如下Jar包：
```
<!-- cxf-webservice start -->
<!-- 加入cxf-webservice依赖包 -->
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-frontend-jaxws</artifactId>
    <version>3.1.6</version>
</dependency>
<!-- 该包会被自动依赖引入 -->
<!-- 
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-core</artifactId>
    <version>3.1.6</version>
</dependency>
-->
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-transports-http</artifactId>
    <version>3.1.6</version>
</dependency>
<!-- cxf内置的服务器jetty用户测试和调试 -->
<!-- 
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-transports-http-jetty</artifactId>
    <version>3.1.6</version>
</dependency>
-->
<!-- 加入cxf-restful依赖包 -->
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-frontend-jaxrs</artifactId>
    <version>3.1.6</version>
</dependency>
<!-- cxf-webservice end -->
```
这边我是用Spring的jar包是Spring官方提供的，并没有使用CXF中的Spring的jar文件。

添加这么多文件后，首先在web.xml中添加如下配置： 

```

<!-- spring context listener -->  
<listener>  
    <listener-class>org.springframework.web.util.IntrospectorCleanupListener</listener-class>  
</listener>  
<listener>  
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>  
</listener>  

<servlet>
    <servlet-name>CXFService</servlet-name>
    <servlet-class>org.apache.cxf.transport.servlet.CXFServlet</servlet-class>
</servlet>

<servlet-mapping>
    <servlet-name>CXFService</servlet-name>
    <url-pattern>/*</url-pattern>
</servlet-mapping>
```
特别的：
```
<servlet>
    <servlet-name>CXFService</servlet-name>
    <servlet-class>org.apache.cxf.transport.servlet.CXFServlet</servlet-class>
</servlet>

<servlet-mapping>
    <servlet-name>CXFService</servlet-name>
    <url-pattern>/webservice/*</url-pattern>
</servlet-mapping>
```
这里把`/webservice/*`作为对外暴露的路径，和`web service address`要结合起来，
比如说`http://ip:port/appname/webservice/HelloWorld`，就会指向`web service address`为`HelloWorld`的服务。 

关于`IntrospectorCleanupListener`的介绍，查看：
[IntrospectorCleanupListener作用](https://github.com/fnpac/Note/blob/master/note/Spring/IntrospectorCleanupListener作用.md)

然后在src目录中，新建一个applicationContext-server.xml文件，文件内容如下： 

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:jaxws="http://cxf.apache.org/jaxws"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
    http://www.springframework.org/schema/context
    http://www.springframework.org/schema/context/spring-context-3.0.xsd
    http://cxf.apache.org/jaxws 
    http://cxf.apache.org/schemas/jaxws.xsd">
```

注意上面的带下划线加粗部分，这个很重要的哦！不能写错或是遗漏了。

添加完这个文件后，还需要在这个文件中导入这么几个文件。文件内容如下： 

```
<!-- spring4.2.0以上需要使用cxf3.x以上的版本  -->
<!--
    cxf3.x版本以后，导入如下文件会报错：
    <import resource="classpath:META-INF/cxf/cxf-extension-soap.xml"/>
    cxf3.x版本，该文件不存在。
 -->
<import resource="classpath:META-INF/cxf/cxf.xml"/>
<import resource="classpath:META-INF/cxf/cxf-servlet.xml"/>
```
此外，cxf3.x版本以后，导入如下文件会报错：
```
<import resource="classpath:META-INF/cxf/cxf-extension-soap.xml"/>
```
错误如下：
```
nested exception is java.io.FileNotFoundException: class path resource [META-INF/cxf/cxf-extension-soap.xml] cannot be opened because it does not exist
```
`cxf.xml`与`cxf-servlet.xml`文件位置：

![](\images\cxf.xml.png)
![](\images\cxf-servlet.xml.png)

下面开始写服务器端代码，首先定制服务器端的接口，代码如下： 
```
package com.hoo.service;

import com.hoo.entity.User;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

/**
 * <b>function:</b>定制客户端请求WebService所需要的接口
 *
 * @author hoojo
 * @version 1.0
 * @createDate 2011-3-18 上午08:22:55
 * @file ComplexUserService.java
 * @package com.hoo.service
 * @project CXFWebService
 * @blog http://blog.csdn.net/IBM_hoojo
 * @email hoojo_@126.com
 */

@WebService
@SOAPBinding(style = Style.RPC)
public interface IComplexUserService {
    
    public User getUserByName(@WebParam(name = "name") String name);
    public void setUser(User user);

}
```
下面编写WebService的实现类，服务器端实现代码如下： 
```
package com.hoo.service;

import com.hoo.entity.User;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import java.util.Date;


/**
 * <b>function:</b> WebService传递复杂对象，如JavaBean、Array、List、Map等
 *
 * @author hoojo
 * @version 1.0
 * @createDate 2011-3-18 上午08:22:55
 * @file ComplexUserService.java
 * @package com.hoo.service
 * @project CXFWebService
 * @blog http://blog.csdn.net/IBM_hoojo
 * @email hoojo_@126.com
 */

@WebService
@SOAPBinding(style = Style.RPC)
@SuppressWarnings("deprecation")
public class ComplexUserService implements IComplexUserService {
    
    public User getUserByName(@WebParam(name = "name") String name) {

        User user = new User();
        user.setId(new Date().getSeconds());
        user.setName(name);
        user.setAddress("china");
        user.setEmail(name + "@hoo.com");
        return user;
    }


    public void setUser(User user) {

        System.out.println("############Server setUser###########");
        System.out.println("setUser:" + user);
    }

}
```
注意的是和Spring集成，这里一定要完成接口实现，如果没有接口的话会有错误的。

下面要在applicationContext-server.xml文件中添加如下配置： 
```
<bean id="userServiceBean" class="com.hoo.service.ComplexUserService"/>

 
<bean id="inMessageInterceptor" class="com.hoo.interceptor.MessageInterceptor">
    <constructor-arg  value="receive"/>
</bean>

<bean id="outLoggingInterceptor" class="org.apache.cxf.interceptor.LoggingOutInterceptor"/>
<!-- 注意下面的address，这里的address的名称就是访问的WebService的name -->
<jaxws:server id="userService" serviceClass="com.hoo.service.IComplexUserService" address="/Users">
    <jaxws:serviceBean>
        <!-- 要暴露的 bean 的引用 -->
        <ref bean="userServiceBean"/>
    </jaxws:serviceBean>
    <jaxws:inInterceptors>
        <ref bean="inMessageInterceptor"/>
    </jaxws:inInterceptors>
    <jaxws:outInterceptors>
        <ref bean="outLoggingInterceptor"/>
    </jaxws:outInterceptors>
</jaxws:server>
```
下面启动tomcat服务器后，在WebBrowser中请求：

http://localhost:8080/CXFWebService/Users?wsdl

如果你能看到wsdl的xml文件的内容，就说明你成功了，注意的是上面地址的Users就是上面xml配置中的address的名称，是一一对应的。

下面编写客户端请求的代码，代码如下： 

```
package com.hoo.client;

import com.hoo.entity.User;
import com.hoo.service.IComplexUserService;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

/**
 * <b>function:</b>请求Spring整合CXF的WebService客户端
 *
 * @author hoojo
 * @version 1.0
 * @createDate 2011-3-28 下午03:20:35
 * @file SpringUsersWsClient.java
 * @package com.hoo.client
 * @project CXFWebService
 * @blog http://blog.csdn.net/IBM_hoojo
 * @email hoojo_@126.com
 */

public class SpringUsersWsClient {

    public static void main(String[] args) {

        //调用WebService
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(IComplexUserService.class);
        factory.setAddress("http://localhost:8080/CXFWebService/Users");

        IComplexUserService service = (IComplexUserService) factory.create();

        System.out.println("#############Client getUserByName##############");
        User user = service.getUserByName("hoojo");
        System.out.println(user);

        user.setAddress("China-Guangzhou");
        service.setUser(user);
    }
}
```

运行后，可以在控制台中看到
```
log4j:WARN No appenders could be found for logger (org.apache.cxf.bus.spring.BusApplicationContext).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
2011-3-28 18:12:26 org.apache.cxf.service.factory.ReflectionServiceFactoryBean buildServiceFromClass
信息: Creating Service {http://service.hoo.com/}IComplexUserServiceService from class com.hoo.service.IComplexUserService
#############Client getUserByName##############
27#hoojo#hoojo@hoo.com#china
```

Tomcat控制台
![](\images\Console_out.png)

这个server端是通过Spring整合配置的，下面我们将Client端也通过Spring配置完成整合。

首先增加applicationContext-client.xml配置文件，文件内容如下： 
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:jaxws="http://cxf.apache.org/jaxws"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
    http://www.springframework.org/schema/context
    http://www.springframework.org/schema/context/spring-context-3.0.xsd
    http://cxf.apache.org/jaxws 
    http://cxf.apache.org/schemas/jaxws.xsd">
    
    <import resource="classpath:META-INF/cxf/cxf.xml"/>
    <import resource="classpath:META-INF/cxf/cxf-extension-soap.xml"/>
    <import resource="classpath:META-INF/cxf/cxf-servlet.xml"/>

    <jaxws:client id="userWsClient" serviceClass="com.hoo.service.IComplexUserService" 
        address="http://localhost:8080/CXFWebService/Users"/>
</beans>
```
>除了在Spring中配置jaxws:client外，我们还可以把JaxWsProxyFactoryBean用Spring类配置。
```
<bean id="logisticsWsApi" class="com.abc.warehouse.service.ILogisticsWsApi" factory-bean="clientFactory" 
    factory-method="create" />  
<bean id="clientFactory" class="org.apache.cxf.jaxws.JaxWsProxyFactoryBean">  
    <property name="serviceClass" value="com.abc.warehouse.service.ILogisticsWsApi" />
    <!--  http://localhost:8080/abc-api/services/logisticsWsApi  -->
    <property name="address" value="${logisticsWsApiAddress}" />  
</bean>
```

客户端请求代码如下： 
```
package com.hoo.client;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.hoo.entity.User;
import com.hoo.service.IComplexUserService;

/**
 * <b>function:</b>请求Spring整合CXF的WebService客户端
 *
 * @author hoojo
 * @version 1.0
 * @createDate 2011-3-28 下午03:20:35
 * @file SpringUsersWsClient.java
 * @package com.hoo.client
 * @project CXFWebService
 * @blog http://blog.csdn.net/IBM_hoojo
 * @email hoojo_@126.com
 */
public class SpringUsersWsClient {

    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-client.xml");
        IComplexUserService service = ctx.getBean("userWsClient", IComplexUserService.class);

        System.out.println("#############Client getUserByName##############");
        User user = service.getUserByName("hoojo");
        System.out.println(user);

        user.setAddress("China-Guangzhou");
        service.setUser(user);
    }

}
```
运行后结果如下：
```
#############Client getUserByName##############
45#hoojo#hoojo@hoo.com#china
############Server setUser###########
setUser:45#hoojo#hoojo@hoo.com#China-Guangzhou
```
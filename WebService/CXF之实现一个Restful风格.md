现在实现一个Restful风格的CXF。

## 1. 创建Webservice对外接口

```java
package com.github.ittalks.commons.example.ws.cxf.restful.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Created by 刘春龙 on 2017/10/31.
 */
@Path("/logisticsApi")
public interface ILogisticsApi {

    @GET
    @Path("/doGet/{first}/{last}")
    @Produces(MediaType.APPLICATION_XML)
    String doGet(@PathParam(value = "first") String firstName, @PathParam(value = "last") String lastName);

    @POST
    @Path("/itemConfirm")
    @Produces(MediaType.APPLICATION_XML)
    String itemConfirm(String xmlParam,
                       @Context HttpServletRequest servletRequest,
                       @Context HttpServletResponse servletResponse);
}
```
## 2. 实现Webservice接口

```java
package com.github.ittalks.commons.example.ws.cxf.restful.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by 刘春龙 on 2017/10/31.
 */
public class LogisticsApiImpl implements ILogisticsApi {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public String doGet(String firstName, String lastName) {
        // TODO Auto-generated method stub
        log.debug("doGet : " + firstName + ", lastName : " + lastName);
        // to to something ...
        return "doGet response";
    }

    @Override
    public String itemConfirm(String xmlParam, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        // TODO Auto-generated method stub
        // to do something ...
        return "itemConfirm response";
    }
}
```

## 3. 配置Spring xml，让Webservice提供服务

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:jaxws="http://cxf.apache.org/jaxws"
       xmlns:jaxrs="http://cxf.apache.org/jaxrs"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
    http://www.springframework.org/schema/context
    http://www.springframework.org/schema/context/spring-context-3.0.xsd
    http://cxf.apache.org/jaxws
    http://cxf.apache.org/schemas/jaxws.xsd
    http://cxf.apache.org/jaxrs
    http://cxf.apache.org/schemas/jaxrs.xsd">

    <!--
        cxf3.x版本以后，导入如下文件会报错：cxf3.x版本，该文件不存在。
        <import resource="classpath:META-INF/cxf/cxf-extension-soap.xml"/>
     -->
    <import resource="classpath:META-INF/cxf/cxf.xml"/>
    <!-- cxf-servlet.xml 为空配置文件，可以不导入 -->
    <import resource="classpath:META-INF/cxf/cxf-servlet.xml"/>

    <bean id="loggingInInterceptor" class="org.apache.cxf.interceptor.LoggingInInterceptor"/>
    <bean id="loggingOutInterceptor" class="org.apache.cxf.interceptor.LoggingOutInterceptor"/>

    <!-- ==========================  restful demo ======================== -->
    <bean id="encodingLoggingInInterceptor" class="com.github.ittalks.commons.example.ws.cxf.restful.server.EncodingLoggingInInterceptor"/>
    <bean id="logisticsApi" class="com.github.ittalks.commons.example.ws.cxf.restful.server.LogisticsApiImpl"/>

    <jaxrs:server id="restWsServer" address="/rest">
        <jaxrs:serviceBeans>
            <ref bean="logisticsApi" />
        </jaxrs:serviceBeans>

        <jaxrs:inInterceptors>
            <ref bean="encodingLoggingInInterceptor"/>
        </jaxrs:inInterceptors>
        <jaxrs:outInterceptors>
            <ref bean="loggingOutInterceptor"/>
        </jaxrs:outInterceptors>

        <jaxrs:extensionMappings>
            <!--
                <entry key="json" value="application/json" />
            -->
            <entry key="xml" value="application/xml" />
        </jaxrs:extensionMappings>

        <jaxrs:languageMappings>
            <entry key="en" value="en-gb"/>
        </jaxrs:languageMappings>
    </jaxrs:server>
</beans>
```

其中`EncodingLoggingInInterceptor`类主要是为了解决传输内容在`LoggingInInterceptor`类内构建并输出时的乱码问题

```java
package com.github.ittalks.commons.example.ws.cxf.restful.server;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 刘春龙 on 2017/10/31.
 */
public class EncodingLoggingInInterceptor extends LoggingInInterceptor {

    private Logger log = LoggerFactory.getLogger(getClass());

    public EncodingLoggingInInterceptor() {
        // TODO Auto-generated constructor stub  
        super();
    }

    /**
     * @see org.apache.cxf.interceptor.LoggingInInterceptor#handleMessage(org.apache.cxf.message.Message)
     */
    @Override
    public void handleMessage(Message message) throws Fault {

        // TODO Auto-generated method stub
        String encoding = System.getProperty("file.encoding");
        encoding = StringUtils.isEmpty(encoding) ? "UTF-8" : encoding;
        log.debug("encoding : " + encoding);

        message.put(Message.ENCODING, encoding);
        super.handleMessage(message);
    }
}
```

至此，Webservice服务器端代码已经编写完成，那么访问该Webservice接口的地址为：http://localhost:9090/futureN4J/ws/

## 4. 接下来我们编写一个基于WebClient简单客户端

```java
package com.github.ittalks.commons.example.ws.cxf.restful.client;

import org.apache.cxf.jaxrs.client.WebClient;

import javax.ws.rs.core.MediaType;

/**
 * Created by 刘春龙 on 2017/10/31.
 */
public class _Main {

    private static String baseAddress = "http://localhost:9090/futureN4J/ws/rest/logisticsApi";

    public static void main(String[] args) {
        WebClient client = WebClient.create(baseAddress)
                .header("charset", "UTF-8")
                .encoding("UTF-8")
                .acceptEncoding("UTF-8");

        Object xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<itemName>诺基亚</itemName>";
        String responseMessage = client.path("itemConfirm")
                .accept(MediaType.APPLICATION_XML)
                .post(xml, String.class);
        System.out.println("responseMessage : " + responseMessage);

        client = WebClient.create(baseAddress)
                .header("charset", "UTF-8")
                .encoding("UTF-8")
                .acceptEncoding("UTF-8");
        responseMessage = client.path("doGet/{first}/{last}", "fnpac", "凡派,")
                .accept(MediaType.APPLICATION_XML)
                .get(String.class);
        System.out.println("responseMessage : " + responseMessage);
    }
}

```

到这里我们就完成了基于Apache cxf JaxRs的服务端和客户端的Demo编写。
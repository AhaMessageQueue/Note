我们现在实现一个Restful风格的CXF。

## 一、我们首先依旧是基于Maven project配置pom.xml的依赖
```
<!-- cxf -->
<cxf.version>3.1.6</cxf.version>

...

<!-- cxf-webservice start -->
<!-- 加入cxf-webservice依赖包 -->
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-frontend-jaxws</artifactId>
    <version>${cxf.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-core</artifactId>
    <version>${cxf.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-transports-http</artifactId>
    <version>${cxf.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-transports-http-jetty</artifactId>
    <version>${cxf.version}</version>
</dependency>
<!-- 加入cxf-restful依赖包 -->
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-frontend-jaxrs</artifactId>
    <version>${cxf.version}</version>
</dependency>
<!-- cxf-webservice end -->
```

## 二、配置web.xml 
```
<!-- spring context listener -->
<listener>
    <listener-class>org.springframework.web.util.IntrospectorCleanupListener</listener-class>
</listener>
<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>

<!-- CXF -->
<servlet>
    <servlet-name>cxf</servlet-name>
    <servlet-class>org.apache.cxf.transport.servlet.CXFServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>cxf</servlet-name>
    <url-pattern>/services/*</url-pattern>
</servlet-mapping>
```

## 三、创建Webservice对外接口 
```
package com.abc.warehouse.service;  
  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  
import javax.ws.rs.GET;  
import javax.ws.rs.POST;  
import javax.ws.rs.Path;  
import javax.ws.rs.PathParam;  
import javax.ws.rs.Produces;  
import javax.ws.rs.core.Context;  
import javax.ws.rs.core.MediaType;  
  
@Path("/logisticsApi")  
public interface ILogisticsApi {  
  
    @GET  
    @Path("/doGet/{first}/{last}")  
    @Produces(MediaType.APPLICATION_XML)  
    public String doGet(@PathParam(value = "first") String firstName, @PathParam(value = "last") String lastName);  
      
      
    @POST  
    @Path("/itemConfirm")  
    @Produces(MediaType.APPLICATION_XML)  
    public String itemConfirm(String xmlParam,  
                                    @Context HttpServletRequest servletRequest,   
                                    @Context HttpServletResponse servletResponse);  
  
}  
```
## 四、实现Webservice接口 
```
package com.abc.api.service;  
  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  
  
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  
  
import com.abc.warehouse.service.ILogisticsApi;  
  
  
public class LogisticsApiImpl implements ILogisticsApi {  
  
    private Logger log = LoggerFactory.getLogger(getClass());  
      
    /**  
     * @see com.abc.warehouse.service.ILogisticsApi#itemConfirm(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse) 
     */  
    @Override  
    public String itemConfirm(String xmlParam,  
                                    HttpServletRequest servletRequest,   
                                    HttpServletResponse servletResponse) {  
        // TODO Auto-generated method stub  
        // to do something ...  
  
        return response;  
    }
    
    /**  
     * @see com.abc.warehouse.service.ILogisticsApi#doGet(java.lang.String, java.lang.String) 
     */  
    @Override  
    public String doGet(String firstName, String lastName) {  
        // TODO Auto-generated method stub  
        log.debug("doGet : " + firstName + ", lastName : " + lastName);  
        // to to something ...  
  
        return response;  
    }
}  
```
## 五、配置Spring xml，让Webservice提供服务
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:jaxws="http://cxf.apache.org/jaxws"
    xmlns:jaxrs="http://cxf.apache.org/jaxrs"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
                       http://www.springframework.org/schema/beans/spring-beans.xsd
                       http://cxf.apache.org/jaxws http://cxf.apache.org/schemas/jaxws.xsd
                    http://cxf.apache.org/jaxrs
                    http://cxf.apache.org/schemas/jaxrs.xsd">
    
    <import resource="classpath:META-INF/cxf/cxf.xml" />
    <import resource="classpath:META-INF/cxf/cxf-extension-soap.xml" />
    <import resource="classpath:META-INF/cxf/cxf-servlet.xml" />
    
    <bean id="encodingLoggingInInterceptor" class="com.abc.api.util.EncodingLoggingInInterceptor"/>
    <bean id="outLoggingInterceptor" class="org.apache.cxf.interceptor.LoggingOutInterceptor"/>
    <bean id="logisticsApi" class="com.abc.api.service.LogisticsApiImpl"/>

    <jaxrs:server id="logisticsApiServiceContainer">
        <jaxrs:serviceBeans>
            <ref bean="logisticsApi" />
        </jaxrs:serviceBeans>
        
        <jaxrs:inInterceptors>
            <ref bean="encodingLoggingInInterceptor"/>
        </jaxrs:inInterceptors>
        <jaxrs:outInterceptors>
            <ref bean="outLoggingInterceptor"/>
        </jaxrs:outInterceptors>
        
        <jaxrs:extensionMappings>
            <!-- <entry key="json" value="application/json" /> -->
            <entry key="xml" value="application/xml" />
        </jaxrs:extensionMappings>
    
        <jaxrs:languageMappings>
            <entry key="en" value="en-gb"/>  
        </jaxrs:languageMappings>
    </jaxrs:server>

</beans>
```

其中EncodingLoggingInInterceptor类主要是为了解决传输内容在LoggingInInterceptor类内构建并输出时的乱码问题 

```
package com.abc.api.util;  
  
import org.apache.cxf.interceptor.Fault;  
import org.apache.cxf.interceptor.LoggingInInterceptor;  
import org.apache.cxf.message.Message;  
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  
  
  
public class EncodingLoggingInInterceptor extends LoggingInInterceptor {  
  
    private Logger log = LoggerFactory.getLogger(getClass());  
      
    /** 
     *  
     */  
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
        encoding = encoding == null || encoding.equals("") ? "UTF-8" : encoding;  
          
        log.debug("encoding : " + encoding);  
        message.put(Message.ENCODING, encoding);  
          
        super.handleMessage(message);  
    }  
}  
```

至此，Webservice服务器端代码已经编写完成，假设Maven project名字为abc-api，那么访问该Webservice接口的地址为：http://ip:port/abc-api/services/

## 六、接下来我们编写一个基于WebClient简单客户端
```
package com.abc.api.service;  
  
import static org.junit.Assert.*;  
  
import javax.ws.rs.core.MediaType;  
  
import org.apache.cxf.jaxrs.client.WebClient;  
import org.junit.After;  
import org.junit.Before;  
import org.junit.Test;  
  
  
public class LogisticsApiTester {  
  
    private WebClient client;  
    private String baseAddress = "http://localhost:8080/abc-api/services/logisticsApi";  
    /** 
     *  
     * @throws java.lang.Exception 
     */  
    @Before  
    public void setUp() throws Exception {  
        client = WebClient.create(baseAddress)  
            .header("charset", "UTF-8")  
            .encoding("UTF-8")  
            .acceptEncoding("UTF-8");  
    }  
  
    /** 
     *  
     * @throws java.lang.Exception 
     */  
    @After  
    public void tearDown() throws Exception {  
        client = null;  
    }  
  
    /** 
     * Test method for {@link com.abc.api.service.LogisticsApiImpl#itemConfirm(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}. 
     */  
    @Test  
    public void testItemConfirm() {  
        //fail("Not yet implemented");  
          
        Object xmlParam = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"  
            + "<itemName>诺基亚</itemName>";  
        String responseMessage = client.path("itemConfirm")  
                                        .accept(MediaType.APPLICATION_XML)  
                                        .post(xmlParam, String.class);  
        System.out.println("responseMessage : " + responseMessage);  
        assertNotEquals(responseMessage, null);  
    }  
  
    /** 
     * Test method for {@link com.abc.api.service.LogisticsApiImpl#doGet(java.lang.String, java.lang.String)}. 
     */  
    @Test  
    public void testDoGet() {  
        //fail("Not yet implemented");  
          
        String responseString = client.path("doGet/{first}/{last}", 1, 2)  
                                    .accept(MediaType.APPLICATION_XML)  
                                    .get(String.class);  
        assertNotEquals(responseString, null);  
    }  
  
  
}  
```

到这里我们就完成了基于Apache cxf JaxRs的服务端和客户端的Demo编写。

>注意：这里xml配置文件里的配置使用的是`jaxrs`命名空间，同时客户端使用的是`jaxrs`包下的类创建的Client。
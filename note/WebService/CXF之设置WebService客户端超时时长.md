在使用WebService时，我们通常都会在客户端中设置请求超时的限制，以避免长时间的去连接不可用的服务器。在CXF的环境下，客户端可通过两个属性配置超时限制：

- ConnectionTimeout - WebService以TCP连接为基础,这个属性可以理解为TCP握手时的时间设置,超过设置的时间就认为是连接超时.以毫秒为单位,默认是30000毫秒,即30秒。
- ReceiveTimeout - 这个属性是发送WebService的请求后等待响应的时间,超过设置的时长就认为是响应超时.以毫秒为单位,默认是60000毫秒,即60秒.

## 客户端
这里可通过两种方式对**_客户端_**进行配置：
###  一、在spring的配置文件中进行设置。

```
<?xml version = "1.0" encoding = "UTF-8" ?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:jee="http://www.springframework.org/schema/jee"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:jaxws="http://cxf.apache.org/jaxws"
       xmlns:http-conf="http://cxf.apache.org/transports/http/configuration"
       xsi:schemaLocation="http://www.springframework.org/schema/beans    
                 http://www.springframework.org/schema/beans/spring-beans-2.0.xsd     
                 http://www.springframework.org/schema/jee    
                 http://www.springframework.org/schema/jee/spring-jee-2.0.xsd     
                 http://cxf.apache.org/jaxws http://cxf.apache.org/schemas/jaxws.xsd     
                 http://cxf.apache.org/transports/http/configuration    
                 http://cxf.apache.org/schemas/configuration/http-conf.xsd ">
    <http-conf:conduit name="{WSDL Namespace}portName.http-conduit">
        <http-conf:client ConnectionTimeout="10000" ReceiveTimeout="20000"/>
    </http-conf:conduit>
</beans>   
```
这里需要注意的有几个地方:  
1. 需要指定`http-conf`名称空间：`xmlns:http-conf=http://cxf.apache.org/transports/http/configuration`
2. 指定模式位置: `http://cxf.apache.org/transports/http/configuration http://cxf.apache.org/schemas/configuration/http-conf.xsd`
3. `http-conf:conduit`中的`name`属性,指定设置生效的服务。`name`属性由`service namespace`、WSDL中的`port name`和".http-conduit"组成，
    如`{http://apache.org/hello_world}HelloWorld.http- conduit`。如果将name属性设置为`*.http-conduit`，则会对所有服务生效。
### 二、通过Java代码进行设置。
```
Client client = ClientProxy.getClient(port);   
HTTPConduit http = (HTTPConduit) client.getConduit();   
HTTPClientPolicy httpClientPolicy =  new  HTTPClientPolicy();   
httpClientPolicy.setConnectionTimeout( 36000 );   
httpClientPolicy.setAllowChunking( false );   
httpClientPolicy.setReceiveTimeout( 32000 );   
http.setClient(httpClientPolicy);  
```

## 服务器
另：也可以对**_服务器_**端进行设置spring代码如下：
```
<!-- 在服务器端设置响应超时限制，现在使用的是默认值30秒 -->
<http-conf:destination name="*.http-conduit">
    <http-conf:server ReceiveTimeout="30000" />
</http-conf:destination> 
```

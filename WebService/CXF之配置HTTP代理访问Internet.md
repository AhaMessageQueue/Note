一个典型的`CXF Webservice`项目部署到正式环境后，服务器不能访问外网，只能通过代理访问外网，
找了很多资料，刚开始想法是不用Spring的配置文件，直接用java编程访问webservice。在java的Http请求中使用代理的方法如下：
```
String authentication="username:password";//用户+”:”+密码
String encodedLogin= new BASE64Encoder().encode(authentication.getBytes());
Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("IP", PORT));

HttpsURLConnection conn = (HttpsURLConnection) console.openConnection(proxy );
conn.setRequestProperty("Proxy-Authorization", " Basic " + encodedLogin);
```
使用JaxWsProxyFactoryBean创建client
```
System.setProperty("http.proxySet", "true");
System.setProperty("http.proxyHost", "IP");
System.setProperty("http.proxyPort", "PORT");

JaxWsProxyFactoryBean f = new JaxWsProxyFactoryBean();
f.setAddress("THE URL OF WEBSERVICE");
f.setServiceClass(IService.class);
IService client = (IService) f.create();
```
可以使用代理，但是用户名密码是不能像第一种方法中那样设置到HttpConnection里面去的。
       
又在网上找了很多资料，终于找到了一个配置的方法。

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" 
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
        xmlns:cxf="http://cxf.apache.org/core" 
        xmlns:sec="http://cxf.apache.org/configuration/security" 
        xmlns:http-conf="http://cxf.apache.org/transports/http/configuration" 
        xsi:schemaLocation=" 
                http://www.springframework.org/schema/beans
                http://www.springframework.org/schema/beans/spring-beans-2.0.xsd
                http://cxf.apache.org/core http://cxf.apache.org/schemas/core.xsd
                http://cxf.apache.org/configuration/security
                http://cxf.apache.org/schemas/configuration/security.xsd
                http://cxf.apache.org/transports/http/configuration
                http://cxf.apache.org/schemas/configuration/http-conf.xsd">
                
        <http-conf:conduit name="*.http-conduit"> 
            <http-conf:proxyAuthorization>
                    <!-- 用户名 -->
                    <sec:UserName>***</sec:UserName>
                    <!-- 密码 -->
                    <sec:Password>***</sec:Password>
             </http-conf:proxyAuthorization>
             <!--
                 ProxyServer IP
                 ProxyServerPort PORT
                 ProxyServerType: HTTP or SOCKS
              -->
             <http-conf:client
                 ProxyServer="192.168.1.4" 
                 ProxyServerPort="808"
                 ProxyServerType="HTTP"
                 Connection="Keep-Alive" 
                 AllowChunking="false"
                 ConnectionTimeout="50000" 
                 ReceiveTimeout="120000"
                 /> 
        </http-conf:conduit> 
</beans>
```
`<http-conf:conduit name="*.http-conduit">` 这里的name为`*.http-conduit`时，
将会对所有的client类启用这个代理，如果要配置某个client类使用代理，可以这么写
```
<http-conf:conduit name="{http://widgets/widgetvendor.net}widgetSOAPPort.http-conduit>
...
</http-conf:conduit>
```
`{}`里面的内容是webservice的wsdl的
```
<wsdl:definitions name="serviceName" targetNamespace="http://hafeyang.blogjava.net"
```
的targetNamespace属性

`{}`之后.之前的内容是
```
…
<wsdl:port name="BasicHttpBinding_IService" binding="i0:BasicHttpBinding_IService">
    <soap:address location="the address" />
</wsdl:port>
…
```
的name属性。

上述wsdl对应的配置是
```
<http-conf:conduit name="{http://hafeyang.blogjava.net}BasicHttpBinding_IService.http-conduit>
 ... 
</http-conf:conduit>
```

### MAVEN导入
这里要导入一个cxf的Jar包和cxf中内置jetty服务器的包
```
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-frontend-jaxws</artifactId>
    <version>3.1.4</version>
</dependency>
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-transports-http-jetty</artifactId>
    <version>3.1.4</version>
</dependency>
```
### 编写WS接口，及其实现类
```
package demo.ws.ri;

import javax.jws.WebService;

/**
 * Created by lizhaoz on 2016/4/23.
 */
@WebService
public interface HelloService {
    String say(String name);
}
```
```
package demo.ws.ri;

import javax.jws.WebService;
import java.util.Set;

/**
 * Created by lizhaoz on 2016/4/23.
 */
@WebService(
        serviceName = "HelloService",
        portName = "HelloServicePort",
        endpointInterface ="demo.ws.ri.HelloService"
)
public class HelloServiceImpl {
    public String say(String name){

        return "hello " + name;
    }
}
```

### 编写一个JaxWsServer类来发布WS
```
package demo.ws.ri;

import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

/**
 * 编写一个服务器来发布
 * Created by lizhaoz on 2016/4/24.
 */

public class JaxWsServer {
    public static void main(String[] args) {
        JaxWsServerFactoryBean factory=new JaxWsServerFactoryBean();
        factory.setAddress("http://localhost:8080/cxf/soap/hello");
        factory.setServiceBean(new HelloServiceImpl());
        factory.create();
    }
}
```
访问http://localhost:8080/cxf/soap/hello?wsdl就可以得到wsdl，
这种方式使用的是CXF内置的服务器jetty非常容易测试和调试，大大提高了开发效率，
但是不适合生产环境，所以我们需要会和spring,tomcat结合。

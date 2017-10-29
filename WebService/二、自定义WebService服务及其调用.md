其实在实际中，我们自定义ws的可能性比较小，一般都是去调用已有的ws较多，除非项目需要发布一个ws。但是为了更好的理解ws，这一篇博文主要来总结一下如何自定义ws，然后如何去调用自定义的ws，内容比较基础，完全针对小白。

## 1. 自定义webservice
### 1.1 原生态jdk定义

使用jdk自定义一个ws只需要做两件事：一是使用注解@WebService，二是调用Endpoint类的静态方法publish创建一个服务端点即可。如下：

```
/**
 * @Description 自定义ws，jdk1.7版本及以上才支持soap1.2
 * @author Ni Shengwu
 *
 */
@WebService //默认静态方法是不能发不成ws服务的
public class MyWebService {

    //提供一个方法，供下面测试用的
    public String sayHello(String name) {
        return name + " 你好！";
    }

    public static void main(String[] args) {
        // 一个端口可以发布多个ws服务，所以后面还有 /+服务名
        String address = "http://192.168.1.105:6666/ws";
        String address2 = "http://192.168.1.105:6666/ws2";
        // 创建一个服务端点
        Endpoint.publish(address, new MyWebService());
        Endpoint.publish(address2, new MyWebService());
        System.out.println("访问WSDL的地址为：" + address + "?WSDL");
        System.out.println("访问WSDL的地址为：" + address2 + "?WSDL");
    }
}
```

这个192.168.1.105是我的ip地址，6666是自己设定的端口号，后面为啥还要跟个名称ws呢？因为一个端口可以发布多个ws服务，所以可以自己起个名儿，我这里创建了两个ws服务地址，分别命名为ws和ws2。然后通过调用Endpoint类的静态方法publish创建服务端点，传进去刚刚定义好的ws服务地址和自定义ws的类即可。后面两个输出地址是用来根据WSDL生成Java代码方便用的。

这样的话，一个ws就定义好了，运行一下，控制台会输出两个地址，分别在浏览器中输入这两个地址就会显示两个相同的xml文档，我们待会儿根据这个WSDL来生成java代码。

### 1.2 使用CXF自定义ws

CXF是ws的一个框架，使用很方便，想要更多的了解CXF可以去百度或者谷歌一下。为了更加规范点，我将需要发布为ws的类单独写出来，并抽取接口，如下：

```
//这是接口
@WebService
public interface HelloWorld {

    public String sayHello(String str);
}

//这是实现类
@WebService
public class HelloWorldImpl implements HelloWorld{

    public String sayHello(String str) {
        return "Hello" + str;
    }

}
```

这里建立的是maven工程，pom.xml需要导入相关的jar包，我导入的是目前最新版的3.1.6。如下：
```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>webservice</groupId>
  <artifactId>WS_Server</artifactId>
  <version>0.0.1-SNAPSHOT</version>

  <dependencies>

    <dependency>
        <groupId>org.apache.cxf</groupId>
        <artifactId>cxf-rt-frontend-jaxws</artifactId>
        <version>3.1.6</version>
    </dependency>

    <dependency>
        <groupId>org.apache.cxf</groupId>
        <artifactId>cxf-core</artifactId>
        <version>3.1.6</version>
    </dependency>

    <dependency>
        <groupId>org.apache.cxf</groupId>
        <artifactId>cxf-rt-transports-http-jetty</artifactId>
        <version>3.1.6</version>
    </dependency>

  </dependencies>
</project>
```

接下来就是创建ws了，如下：

```
public class Server {

    public static void main(String[] args) {
        System.out.println("web service start");
        HelloWorld implementor = new HelloWorldImpl();
        String address = "http://192.168.1.105/ws";

        JaxWsServerFactoryBean factoryBean = new JaxWsServerFactoryBean();
        factoryBean.setAddress(address); // 设置暴露地址
        factoryBean.setServiceClass(HelloWorld.class); // 接口类
        factoryBean.setServiceBean(implementor); // 设置实现类
        factoryBean.create(); // 创建webservice接口
        System.out.println("web service started");
        System.out.println("请求地址为为：" + address + "?WSDL");
    }
}
```

这样就使用CXF创建好了ws了，下面就是调用这个ws服务了，下面的调用我用的是上面原生态jdk生成的ws服务，其实都一样的。

## 2. 调用自定义的webservice
### 2.1 使用普通java程序调用

刚刚已经生成了对应的WSDL了，在调用自定义的ws前，我们需要先通过解析这个WSDL，然后生成一些Java代码，可以看成是我们自定义ws的API。打开命令行，输入 
```
wsimport -s . -p ws.client.d http://192.168.1.105:6666/ws?WSDL
```

就能生成一个目录ws/client/d/，以及目录中的一些class文件和java文件，删掉class文件，然后拷贝ws目录，直接贴到工程另一个工程中，如下： 

![](\images\wsimport_project_struct_2.png)

里面框框圈的都是自定义ws相关的API，然后自己写一个_Main类来调用自定义ws，如下：

```
/**
 * @Description 调用自己发布的ws服务
 * @author Ni Shengwu
 *
 */
public class _Main {

    public static void main(String[] args) {
        // 获取ws服务名称（获取一个ws服务）
        MyWebServiceService service = new MyWebServiceService();
        //获取服务的类型，有get post soap1.1 soap1.2 jdk1.7及以上才支持soap1.2
        MyWebService port = service.getMyWebServicePort();
        //调用服务提供的方法
        System.out.println(port.sayHello("hello"));
    }   
}
```

根据上面的代码，即传入一个hello去调用自定义ws中的sayHello方法，所以控制台会打印出 “hello 你好！”。这样自定义ws以及调用就完成了。

### 2.2 使用Ajax调用　　
使用Ajax调用的话，流程也很简单，从前台发送Ajax请求，然后调用ws服务，再将结果返回给前台。但是Ajax不支持跨域调用，也就是说我们发布的ws服务，如http://192.168.1.105:6666/ws是不行的，Ajax要想正常调用必须通过wsimport命令转化的java bean，换句话说，我们需要在中间加上一个servlet（或者struts，springmvc），在servlet中调用ws服务，把数据返回到前台。写个demo如下： 

**servlet：**
```
public class _MyServlet extends HttpServlet {


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setCharacterEncoding("UTF-8");
        request.setCharacterEncoding("UTF-8");
        String name = request.getParameter("name");
        // 创建ws服务
        MyWebServiceService ws = new MyWebServiceService();
        MyWebService port = ws.getMyWebServicePort();
        String result = port.sayHello(name);

        // 返回结果
        response.getWriter().write(result);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        doGet(request, response);
    }

}
```

这个servlet映射的url为/MyServlet。 

**jsp：**
```
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>  
  <script type="text/javascript" src="jquery-1.3.js"></script> 
  </head> 
  <script type="text/javascript">
    $(function() {
        $("#btn").click(function() {
            $.post("MyServlet", {name:$("#txt").val()}, function(msg) {
                alert(msg);
            }, "text");
        });
    })
  </script>


  <body>
    <input type="text" id="txt" />
    <input type="button" id="btn" value="ajax调用ws服务演示">
  </body>
</html>
```

jsp中通过发送Ajax请求，将输入的值带给servlet，然后在servlet中获取该值，调用ws获取结果返回给前台显示，完成Ajax调用。
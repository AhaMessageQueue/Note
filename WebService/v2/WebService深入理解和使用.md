# 带你走进webservice的世界

## 一、webservice是啥？
准确的来说，webservice不是一种技术，而是一种规范。是一种跨平台，跨语言的规范，用于不同平台，不同语言开发的应用之间的交互。

举个例子，比如在Windows Server服务器上有个C#.Net开发的应用A，在Linux上有个Java语言开发的应用B，现在B应用要调用A应用，或者是互相调用，用于查看对方的业务数据，就需要webservice的规范。

再举个例子，天气预报接口。无数的应用需要获取天气预报信息，这些应用可能是各种平台，各种技术实现，而气象局的项目，估计也就一两种，要对外提供天气预报信息，这个时候，如何解决呢？webservice就是出于以上类似需求而定义出来的规范。

我们一般就是在具体平台开发webservice接口，以及调用webservice接口，每种开发语言都有自己的webservice实现框架。比如Java 就有 Apache Axis1、Apache Axis2、Codehaus XFire、Apache CXF、Apache Wink、Jboss RESTEasyd等等。其中Apache CXF用的比较多，它也可以和Spring整合。

## 二、重温socket
在分析如何调用webservice前，先来回忆一下传统的socket是如何通信的，这样更容易理解ws。

### 2.1 基于socket创建web服务
为什么要使用socket呢？看一下下面的原理图：

![](..\images\architecture_diagram.png)

从图中可以看出，程序A和程序B之间是无法实现直接调用的，那么现在A需要访问B的话，A即创建一个socket并制定B机器的端口号，在此之前B已经在本机创建好了socket等待用户来连接，A和B连接成功后，即可向B发送请求获取数据了，这很好理解，为了回忆一下socket的创建和使用，下面先写一个简单的socket通信的demo，服务端可以将小写字母转大写。

### 2.2 经典的socket服务

```java
package com.github.ittalks.commons.webservice;

import java.util.Scanner; 
import java.net.Socket;

public class SocketClient {
    public static void main(String[] args){
      Scanner input = new Scanner(System.in);
      
      // 1. 创建一个基于TCP协议的socket客户端，在建立对象时，要指定连接服务器和端口号
      Socket sc = new Socket("127.0.0.1", 9999);
      // 2. 通过建立的Socket对象调用getOutStream方法获取Socket中的输出流
      OutputStream out = sc.getOutputStream();
      
      System.out.println("请输入要转化的字母：");
      String initData = input.next();// 获取控制台的输入
      
      // 3. 写入到Socket输出流中
      out.write(initData.getBytes());
      System.out.println("等待服务器端返回数据");
      
      // 4. 通过建立的Socket对象获取Socket中的输入流，输入流会接受来自服务器端数据
      InputStream in = sc.getInputStream();
      byte[] b = new byte[4096];
      
      // 5. 获取输入字节流的数据，注意此方法是堵塞的，如果没有获取数据会一直等待
      int len = in.read(b);
      System.out.println("返回的结果为：" + new String(b, 0, len));

      // 关闭Socket
      out.close();
      in.close();
      sc.close();
      input.close();
    }
}
```

服务端：

```java
package com.github.ittalks.commons.webservice;

public class SocketServer {
    public static void main(String[] args){
      // 1. 建立服务器端的tcp socket服务，必须监听一个端口
      ServerSocket ss = new ServerSocket(9999);
      
       while(true) {
           System.out.println("等待客户端请求……");
           
           // 2. 通过服务器端的socket对象的accept方法获取连接上的客户端对象，没有则堵塞，等待
           Socket socket = ss.accept();
           System.out.println("握手成功……");
           
           // 3. 通过输入流获取数据
           InputStream input = socket.getInputStream();
           byte[] b = new byte[4096];
           int len = input.read(b);
           String data = new String(b, 0, len);
           System.out.println("客户端数据为：" + data);
           
           // 4. 通过服务器端Socket输出流，写数据，回传送到客户端Socket输入流中
           OutputStream out = socket.getOutputStream();
           out.write(data.toUpperCase().getBytes());
           
           // 5. 关闭socket
           out.close();
           input.close();
           socket.close();
       }
    }
}
```

这个demo很简单，先开启服务端的程序，在那等待，然后开启客户端程序，如果在控制台输入hello过去，就会从服务端返回一个HELLO回来，这说明socket通信是成功的。

### 2.3 web程序访问socket service
上面经典的demo是在本地写的两个java程序，我们现在的很多项目都是web项目，也就是通过浏览器来交互的，我们来看下通过浏览器的方式如何来访问socketService服务。
服务端还是使用上面的那个Java程序，客户端我改成浏览器请求，新写一个jsp如下：

```jsp
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>   
  </head> 
  <body>
    <form action="http://127.0.0.1:9999" method="post">
        <input type="text" name="sname">
        <input type="submit" value="提交">
    </form>
  </body>
</html>
```

注意看action的请求地址，包括端口，要和服务端的一样，这样当我们提交的时候就可以访问上面的服务端程序了。

看一下服务端的运行结果：

![](..\images\server_post_request.png)

可以看到，web程序确实和服务端握手成功了，而且数据也是可以传过去的，`sname=hello`，包括一些Http信息都可以传过去，

但是我们再来看看服务端返回给浏览器的数据是啥：

![](..\images\client_post_request.png)

我用的是chrome浏览器，其他浏览器可能没有数据，这都有可能。
但是从数据中来看，它只是单纯的把所有信息全部转成了大写……
而且它也没有Http的返回格式，也就是说，我所需要的就是个大写的HELLO即可，所以这是有问题的。

所以可以总结一下：不同的协议其实也是支持Socket通信的。 
web程序可以调用socket请求，但是由于协议不同，因此在处理的时候要过滤http的协议格式，返回的时候还需要添加http返回的格式，否则就会出现问题，可想而知，如果还要处理协议格式，是很麻烦的。

所以到这里，基本上就理解了为什么传统的socket无法满足需求了，其实除了上面的弊端外，还有其他的弊端，比如如果参数一多，就不好维护等等，这里就不多举例了。

## 三、调用已发布的WebService

关于webservice本身，我就不做过多的描述了，在最上面也有简单介绍，既然传统的socket通信无法满足，那么下面开始来调用已发布的ws，真正走进ws的世界。

有一个站点：<http://www.webxml.com.cn>，是上海的一家公司做的，上面提供了很多ws服务，其中有一个查询号码归属地的功能，我们用它来做测试。先来在它们的站点中测试一下，然后再在本地写程序来调用这个ws服务获取查询结果。

看一下站点上的查询：

![](..\images\webxml.png)

进入手机号码归属地查询web服务后， 

![](..\images\webxml_mobile.png)

选择getMobileCodeInfo，即可进入查询页面了， 

![](..\images\webxml_mobile_detail.png)

调用后就会出现`<string xmlns="http://WebXml.com.cn/">18312345678：广东 深圳 广东移动全球通卡</string>`的结果。这就是调用ws的结果，接下来我们在程序中来调用这个ws。

### 3.1 get请求方式

在Java程序中如果要发送http请求，需要使用HttpClient工具。

HttpClient 是 Apache Jakarta Common 下的子项目，可以用来提供高效的、最新的、功能丰富的支持 HTTP 协议的客户端编程工具包，并且它支持 HTTP 协议最新的版本和建议。

为什么要使用HttpClient工具呢？因为原生态的Socket基于传输层，现在我们要访问的WebService是基于HTTP的属于应用层，所以我们的Socket通信要借助HttpClient发HTTP请求，这样格式才能匹配。

```java
public void get(String number) throws Exception {
    // HttpClient：在java代码中模拟Http请求
    // 创建HttpClient对象
    HttpClient client = new HttpClient();
    // 填写数据，发送get请求
    GetMethod get = new GetMethod("http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx"
            + "/getMobileCodeInfo?mobileCode=" + number + "&userID=");
    // 指定传输的格式
    get.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
    // 发送请求
    int code = client.executeMethod(get);
    System.out.println("Http:状态码为：" + code);

    String result = get.getResponseBodyAsString();
    System.out.println("返回的结果为：" + result);
}
```

从程序中可以看出，请求的主机是ws.webxml.com.cn，这些url在ws提供方的网站上都有，我们只需要写对即可请求ws了，在main方法中调用一下该方法即可在控制台获取结果。

### 3.2 post请求方式
请求的过程都一样，只是url和传输格式不同而已，修改一下相应的地方即可，

```java
public void post(String number) throws Exception {
    // HttpClient：在java代码中模拟Http请求
    // 创建HttpClient对象
    HttpClient client = new HttpClient();
    // 填写数据，发送post请求
    PostMethod post = new PostMethod("http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx/getMobileCodeInfo");

    // 指定传输的格式
    post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");     
    // 传输参数
    post.setParameter("mobileCode", number);
    post.setParameter("userID", "");

    // 发送请求
    int code = client.executeMethod(post);
    System.out.println("Http:状态码为：" + code);

    String result = post.getResponseBodyAsString();
    System.out.println("返回的结果为：" + result);
}
```

### 3.3 SOAP方式请求

这也是在用的多的方式，它有两个版本soap1.1和soap1.2，jdk1.7及以上才可以使用soap1.2。

```
public void soap(String number) throws Exception {
    // HttpClient：在java代码中模拟Http请求
    // 创建HttpClient对象
    HttpClient client = new HttpClient();
    // 填写数据，发送post请求
    PostMethod post = new PostMethod("http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx");

    // 指定传输的格式为xml格式
    post.setRequestHeader("Content-Type", "application/soap+xml;charset=utf-8");
    // 传输xml，加载soap.txt
    post.setRequestBody(new FileInputStream("soap.txt"));  
    // 发送请求
    int code = client.executeMethod(post);
    System.out.println("Http:状态码为：" + code);

    String result = post.getResponseBodyAsString();
    // 如果采用的是soap，则返回的数据也是基于xml的soap格式
    System.out.println("返回的结果为：" + result);
}
```

由于soap方式需要向服务端发送xml，所以我们可以实现写好一个txt文档，里面是xml的数据，这个模板ws提供方会提供，我们需要写好即可：

```
<?xml version="1.0" encoding="utf-8"?>
<soap12:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap12="http://www.w3.org/2003/05/soap-envelope">
  <soap12:Body>
    <getMobileCodeInfo xmlns="http://WebXml.com.cn/">
      <mobileCode>18312345678</mobileCode>
      <userID></userID>
    </getMobileCodeInfo>
  </soap12:Body>
</soap12:Envelope>
```

上面这些方式推荐使用soap的方式，不过本质上还是http方式调用，只是调用的时候可以传输xml数据而已。而且HttpClient是Java的调用http协议的解决方案，但是不能保证其它语言也拥有类似的工具。所以ws推荐的方案是使用wsimport命令。这也是下面分析的重点。

### 3.4 使用wsimport

每个ws都会有一个WSDL，WSDL即WebService Description Language – Web服务描述语言。它是通过XML形式说明服务在什么地方 － 地址。通过XML形式说明服务提供什么样的方法 – 如何调用。

我们可以通过这个WSDL来获取和这个ws有关的信息，包括class和java代码。关于这个WSDL后面我再具体分析，这一节先来看一下如何使用。

wsimport是一个命令，jdk1.6及以上才可以使用，ws针对不同的语言都会有个wsimport命令，我们可以在自己安装的jdk的bin目录下找到这个wsimport.exe，正因为有了这个，所以我们可以在命令行中使用wsimport命令。怎么使用呢？

每个ws都会有一个WSDL，就拿上面的归属地查询服务来说，上面第二张图上面有个服务说明，点开就可以看到WSDL，当然也可以直接访问浏览器上的url来访问这个WSDL，即xml文档。如下： 

![](..\images\wsimport.png)

目前只需要复制一下那个url即可，然后打开命令提示符窗口，随便进入一个目录下（该目录要保存等会生成的和ws相关的文件，自己事先建一个即可），运行 

```
wsimport http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx?WSDL
```

就会生成相应的javabean，当然了，是.class文件，但是我们不想要class文件，我们想要java文件，所以可以使用如下命令： 

```
wsimport -s . http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx?WSDL
```

这样不仅生成了class文件，还生成了java文件，如果我们想要在固定的包下生成这些文件，等会方便直接拷贝到项目里，可以使用下面的命令： 

```
wsimport -s . -p ws.client.c http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx?WSDL
```

这样就会在目录ws/client/c/下生成所需要的class和java代码，然后我们删掉class文件，直接拷贝ws目录到工程中即可，如下(_Main是我自己写的，用来调用使用的)： 

![](..\images\wsimport_project_struct.png)

这样就有了号码归属地查询这个ws服务相关的API了，这是通过官方的WSDL来生成的，然后我们如何在自己的项目中使用呢？我新写一个_Main.java文件，直接使用这些API即可，如下：

```java
public class _Main {
    public static void main(String[] args) {

        // 获取一个ws服务
        MobileCodeWS ws = new MobileCodeWS();
        // 获取具体的服务类型：get post soap1.1 soap1.2
        MobileCodeWSSoap wsSoap = ws.getMobileCodeWSSoap();
        String address = wsSoap.getMobileCodeInfo("18312345678", null);
        System.out.println("手机归属地信息为：" + address);
    }
}
```

这样就很方便了，现在已经完全没有了上面那种连接啊，设置地址啊等等，直接封装好了，我直接调用这些API即可调用远程的webservice。这也是官方推荐的一种方法，当然我们也可以将生成的class文件打包成jar放到工程中。运行一下这个main方法后，也直接返回归属地，没有那些标签的东西了，这才是开发中所需要的东西。

到这里基本已经会调用webservice了，最后再简单总结一下，ws中这个WSDL很重要，这里面用xml描述了该ws的信息，所以我们可以通过解析WSDL来获取该ws相关的API，然后在自己的项目中调用这些API即可调用该ws。 

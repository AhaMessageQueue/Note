# 带你走进webservice的世界

## 一、webservice是啥？
准确的来说，webservice不是一种技术，而是一种规范。是一种跨平台，跨语言的规范，用于不同平台，不同语言开发的应用之间的交互。

举个例子，比如在Windows Server服务器上有个C#.Net开发的应用A，在Linux上有个Java语言开发的应用B，现在B应用要调用A应用，或者是互相调用，用于查看对方的业务数据，就需要webservice的规范。

再举个例子，天气预报接口。无数的应用需要获取天气预报信息，这些应用可能是各种平台，各种技术实现，而气象局的项目，估计也就一两种，要对外提供天气预报信息，这个时候，如何解决呢？webservice就是出于以上类似需求而定义出来的规范。

我们一般就是在具体平台开发webservice接口，以及调用webservice接口，每种开发语言都有自己的webservice实现框架。比如Java 就有 Apache Axis1、Apache Axis2、Codehaus XFire、Apache CXF、Apache Wink、Jboss RESTEasyd等等。其中Apache CXF用的比较多，它也可以和Spring整合。

## 二、重温socket
在分析如何调用webservice前，先来回忆一下传统的socket是如何通信的，这样更容易理解ws。

### 基于socket创建web服务
为什么要使用socket呢？看一下下面的原理图：

![](..\images\architecture_diagram.png)

从图中可以看出，程序A和程序B之间是无法实现直接调用的，那么现在A需要访问B的话，A即创建一个socket并制定B机器的端口号，在此之前B已经在本机创建好了socket等待用户来连接，A和B连接成功后，即可向B发送请求获取数据了，这很好理解，为了回忆一下socket的创建和使用，下面先写一个简单的socket通信的demo，服务端可以将小写字母转大写。

### 经典的socket服务
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
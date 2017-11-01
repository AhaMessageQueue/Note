## 需求描述

在正常的项目开发需求中，连接远程服务器的场景一般有二：
1. 自家实现的http服务器，api接口都已经约定好；
2. 开发平台服务，通常如新浪、百度云等平台提供的restful接口；

以上的两种场景通过原生的URLConnection或是apache提供的httpclient工具包都可以方便的实现调用。
 
然而，第三种场景是需要连接国外的开放服务，如google、twitter、tumblr等开放API接口。
在伟大的gfw关怀下，我们被告知不要随便和陌生人说话...

好吧，接下来让我们开始实现基于proxy的穿越吧！

## 准备工作

1. http代理服务器
    建议花点银子买个稳定的VPN，带http代理的那种。
 
2. 外网访问测试
    可以用chrome switchyOmega插件测试一把，不行直接设置IE系统代理

准备完毕，可以开始开发了。

## 设计分析

代理连接实现的关键步骤：

### 一、设置代理服务器地址端口

**方式一：Java支持以`System.setProperty`的方式设置http代理及端口，如下：**

```java    
System.setProperty("http.proxySet", "true");
System.setProperty("http.proxyHost", proxyHost);
System.setProperty("http.proxyPort", "" + proxyPort);

// 针对https也开启代理
System.setProperty("https.proxyHost", proxyHost);
System.setProperty("https.proxyPort", "" + proxyPort);
```

关于Java属性的详细设置可参考：<http://docs.oracle.com/javase/6/docs/technotes/guides/net/properties.html>

**方式二：使用`Proxy`对象，在建立连接时注入到`URLConnection`即可：**

```java
// 初始化proxy对象
Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
 
// 创建连接
URL u = new URL(url);
URLConnection conn = u.openConnection(proxy);
```

**关于两种方式的比较**
        
第一种方式更值得推荐，当你采用基于URLConnection封装实现的类库时，采用setProperty的方式则不需要动里面的代码，绿色轻便。

## 二、实现用户密码校验

**方式一：将校验信息写入http头，将用户名密码进行base64编码之后设置`Proxy-Authorization`头：**

```java
String encoded = new String(Base64.encodeBase64((proxyUser + ":" + proxyPass).getBytes()));
String headerValue = "Basic " + encoded;
conn.setRequestProperty("Proxy-Authorization", headerValue);
```

不少资料会推荐这样的方式，但经过测试，该方式在https的需求场景下无法正常工作！

**方式二：实现Authenticator接口，并注入为全局验证器：**

```java
public static class BasicAuthenticator extends Authenticator {
    String userName;
    String password;
 
    public BasicAuthenticator (String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
     
    /**
    * 当需要使用密码校验时自动触发
    */
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(userName, password.toCharArray());
    }
}
```

在执行连接之前注入校验实例：

```java
BasicAuthenticator auth = new BasicAuthenticator(proxyUser, proxyPass);  
Authenticator.setDefault(auth);
```

## 实例代码

```java
package com.github.ittalks.commons.example.proxy;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.helpers.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by 刘春龙 on 2017/11/1.
 * <p>
 * 使用Proxy代理
 */
public class _ProxyMain {

    private static String proxyHost = "";
    private static int proxyPort = 80;
    private static String proxyUser = "";
    private static String proxyPass = "";

    public static void main(String[] args) {
        String url = "https://www.google.com/";
        String content = doProxy(url);
        System.out.println("Result :===================\n "
                + content);
    }

    private static String doProxy(String url) {
        // 设置系统变量
        System.setProperty("http.proxySet", "true");
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", "" + proxyPort);
        // 针对https也开启代理
        System.setProperty("https.proxyHost", proxyHost);
        System.setProperty("https.proxyPort", "" + proxyPort);
        // 设置默认校验器
        BasicAuthenticator auth = new BasicAuthenticator(proxyUser, proxyPass);
        Authenticator.setDefault(auth);

        //开始请求
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            HttpsURLConnection.setFollowRedirects(true);

            String encoding = conn.getContentEncoding();
            if (StringUtils.isEmpty(encoding)) {
                encoding = "UTF-8";
            }

            InputStream is = conn.getInputStream();
            return IOUtils.toString(is, encoding);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public static class BasicAuthenticator extends Authenticator {

        private String userName;
        private String password;

        public BasicAuthenticator(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        /**
         * Called when password authorization is needed.  Subclasses should
         * override the default implementation, which returns null.
         *
         * @return The PasswordAuthentication collected from the
         * user, or null if none is provided.
         */
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(userName, password.toCharArray());
        }
    }
}
```
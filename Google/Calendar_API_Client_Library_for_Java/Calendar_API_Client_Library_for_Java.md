日历API：操作事件和其他日历数据。

此页面包含有关开始使用Google日历API的信息。 此外，您可能对以下文档感兴趣： 

- [浏览Calendar API的JavaDoc参考](https://developers.google.com/resources/api-libraries/documentation/calendar/v3/java/latest/)。 
- [参阅` Google API Client Library for Java`的开发人员指南](https://developers.google.com/api-client-library/java/google-api-java-client/dev-guide)。 
- [在浏览器中使用Google日历API的API Explorer与此API进行交互](https://developers.google.com/apis-explorer/#p/calendar/v3/)。

## Samples
以下示例可能有助于您开始使用客户端库：


- [calendar-android-sample](https://github.com/google/google-api-java-client-samples/tree/master/calendar-android-sample)
- [calendar-appengine-sample](https://github.com/google/google-api-java-client-samples/tree/master/calendar-appengine-sample)
- [calendar-cmdline-sample](https://github.com/google/google-api-java-client-samples/tree/master/calendar-cmdline-sample)

## Add Library to Your Project
从以下选项卡中选择您的构建环境（Maven或Gradle），或下载包含所有需要的jar的zip文件：

### Download the Calendar API v3 Client Library for Java.
https://developers.google.com/resources/api-libraries/download/calendar/v3/java

有关详细信息，请参阅calendar / readme.html文件：

 - zip文件包含什么。
 - 每个应用程序类型（Web，已安装或Android应用程序）需要哪些依赖jar包。

libs文件夹包含您在所有应用程序类型中可能需要的所有全局适用的依赖项。

### Maven
Add the following to your pom.xml file:

```
<project>
  <dependencies>
    <dependency>
      <groupId>com.google.apis</groupId>
      <artifactId>google-api-services-calendar</artifactId>
      <version>v3-rev234-1.22.0</version>
    </dependency>
  </dependencies>
</project>
```

See [all versions available on the Maven Central Repository](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.google.apis%22%20AND%20a%3A%22google-api-services-calendar%22).

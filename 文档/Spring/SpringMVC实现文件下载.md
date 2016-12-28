
>我们要学会用优雅的方式解决看似"复杂"的问题

>作为程序员，可能会在不经意间就写出来了一段让自己感到骄傲、欣喜、自豪的垃圾代码。
对！就是垃圾代码，此处不需要引号！这种情况是可悲的，更可悲的是你自己一直无法发现自己
的垃圾之处！我们如果想成长，想在编程的路上走下去，第一个资本就是要：学会、习惯、坚持
写优雅的高效的健壮的代码。这个过程不是一触而就的，只能在日常的小事中，自己编写的一段段
小的代码中慢慢改进。

OK，学习不要太严肃，以上只是自己的一点感想， 也算是对自己今后的码路的一点意见、提醒。
各位童鞋不是来听我谈理想的，俗话说得好“总是和老子谈理想的人都不是好人，要想和老子谈就谈钱”！

![](http://img.imooc.com/566a9f0100011ebf03630199.gif)

今天说的问题是关于文件上传的，我这些天在弄一个自己的小项目，使用SSM框架和MySQL数据库，
项目中有文件上传和下载的功能。OK！先看一段代码：

```
public static void downLoadFile(HttpServletResponse response, File file) {
        if (file == null || !file.exists()) {
            return;
        }
        OutputStream out = null;
        try {
            response.reset();
            //文件扩展名:.*（ 二进制流，不知道下载文件类型） 	Content-Type(Mime-Type):application/octet-stream
            response.setContentType("application/octet-stream; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            out = response.getOutputStream();
            out.write(FileUtils.readFileToByteArray(file));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
```
这段代码，各位看官感觉如何？其实在之前的servlet中，这种实现方式也是无可厚非的，也算是比较普遍的一种实现方式。
但是！！！今天在慕课群里聊天时，@慕男神的一句话提醒到了我。我上面提到了，我用到的是SSM框架，既然用了Spring MVC为什么还要暴露这种HttpServletResponse j2ee的接口用作实现下载功能呢？Spring 应该提供了更好的实现方式。OK，开始查资料...

结果...

真TM让我找到了

![](http://img.imooc.com/566aa1520001bc3601970164.gif)

上代码：
```
public ResponseEntity<byte[]> download(String fileName, File file) throws IOException {
        String dfileName = new String(fileName.getBytes("gb2312"), "iso8859-1");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", dfileName);
        return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
    }
```

代码很简洁，逻辑很严谨（我这是屁话），实现很优雅......

简单看了下源码里的实现方式:
```
   public class HttpHeaders implements MultiValueMap<String, String>, Serializable {
        private static final long serialVersionUID = -8578554704772377436L;
        private static final String ACCEPT = "Accept";
        private static final String ACCEPT_CHARSET = "Accept-Charset";
        private static final String ALLOW = "Allow";
        private static final String CACHE_CONTROL = "Cache-Control";
        private static final String CONNECTION = "Connection";
        private static final String CONTENT_DISPOSITION = "Content-Disposition";
        private static final String CONTENT_LENGTH = "Content-Length";
        private static final String CONTENT_TYPE = "Content-Type";
        private static final String DATE = "Date";
        private static final String ETAG = "ETag";
        ...
```
```
    static {
        ALL = valueOf(ALL_VALUE);
        APPLICATION_ATOM_XML = valueOf(APPLICATION_ATOM_XML_VALUE);
        APPLICATION_FORM_URLENCODED = valueOf(APPLICATION_FORM_URLENCODED_VALUE);
        APPLICATION_JSON = valueOf(APPLICATION_JSON_VALUE);
        APPLICATION_OCTET_STREAM = valueOf(APPLICATION_OCTET_STREAM_VALUE);
        APPLICATION_XHTML_XML = valueOf(APPLICATION_XHTML_XML_VALUE);
        APPLICATION_XML = valueOf(APPLICATION_XML_VALUE);
        IMAGE_GIF = valueOf(IMAGE_GIF_VALUE);
        IMAGE_JPEG = valueOf(IMAGE_JPEG_VALUE);
        IMAGE_PNG = valueOf(IMAGE_PNG_VALUE);
        MULTIPART_FORM_DATA = valueOf(MULTIPART_FORM_DATA_VALUE);
        TEXT_HTML = valueOf(TEXT_HTML_VALUE);
        TEXT_PLAIN = valueOf(TEXT_PLAIN_VALUE);
        TEXT_XML = valueOf(TEXT_XML_VALUE);
    }
```
```
   /**
     * A String equivalent of {@link MediaType#APPLICATION_OCTET_STREAM}.
     */
    public final static String APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream";
```

```
    /**
     * Set the given, single header value under the given name. * @param headerName the header name * @param headerValue the header value * @throws UnsupportedOperationException if adding headers is not supported * @see #put(String, List) * @see #add(String, String)
     */
    @Override
    public void set(String headerName, String headerValue) {
        List<String> headerValues = new LinkedList<String>();
        headerValues.add(headerValue);
        headers.put(headerName, headerValues);
    }
```
此处只简单看了一下源码实现方式，大体上实现方式差不多，只是让Spring封装了一遍，显得更严谨一点。
具体的源码，各位有兴趣去Spring官网查看吧...

最后，附上一点点关于这方面的问题：
使用这种方式需要修改下application配置文件：

```
<!-- 设置json和response的字符编码 -->
<bean class="org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter">
    <property name="messageConverters">
        <list>
            <bean class="org.springframework.http.converter.ByteArrayHttpMessageConverter"/>
            <ref bean="stringHttpMessageConverter"/>
        </list>
    </property>
</bean> <bean id="stringHttpMessageConverter" class="org.springframework.http.converter.StringHttpMessageConverter">
<property name="supportedMediaTypes">
    <list>
        <value>text/plain;charset=UTF-8</value>
    </list>
</property>
</bean>
```

这个是配置response的字符编码的，如果不配置，可能会出现乱码等一系列问题。

OK...

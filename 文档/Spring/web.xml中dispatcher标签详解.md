## 一、概念（RequestDispatcher）
RequestDispatcher提供两个方法forward和include区别:

```
servlet A  
RequestDispatcher disp = request.getRequestDispatcher("B");  
disp.forward(request, response);  
System.out.println("servlet A completed");  
PrintWriter pw = response.getWriter();  
pw.println("servlet A");  
  
servlet B  
PrintWriter pw = response.getWriter();  
pw.println("servlet B");  
  
输出结果：  
控制台：servlet A completed  
页面：servlet B  
如果将forward换成include的话，则结果为：  
控制台：servlet A completed  
页面：servlet B servlet A  
PS：如果在servlet B里吧pw给close掉了的话，那servlet A 这里就无法输出了，则结果就和第一个一样。  
```

## 二、<dispatcher>元素
2.4版本的servlet规范在部属描述符中新增加了一个`<dispatcher>`元素，
这个元素有四个可能的值：即REQUEST,FORWARD,INCLUDE和ERROR，
可以在一个`<filter-mapping>`元素中加入任意数目的`<dispatcher>`，
使得filter将会作用于直接从客户端过来的request，
通过forward过来的request，
通过include过来的request
和通过<error-page>过来的request。
如果没有指定任何`< dispatcher >`元素，默认值是REQUEST。

可以通过下面几个例子来辅助理解。 
```
    <filter-mapping>     
    <filter-name>Logging Filter</filter-name>     
    <url-pattern>/products/*</url-pattern>     
    </filter-mapping>  
```
这种情况下，过滤器将会作用于直接从客户端发过来的以`/products/…`开始的请求。因为这里没有制定任何的`< dispatcher >`元素，默认值是REQUEST。

```
    <filter-mapping>     
    <filter-name>Logging Filter</filter-name>     
    <servlet-name>ProductServlet</servlet-name>     
    <dispatcher>INCLUDE</dispatcher>     
    </filter-mapping>  
```
这种情况下，如果请求是通过request dispatcher的include方法传递过来的对ProductServlet的请求，则要经过这个过滤器的过滤。
其它的诸如从客户端直接过来的对ProductServlet的请求等都不需要经过这个过滤器。

指定filter的匹配方式有两种方法：直接指定`url-pattern`和指定`servlet`，后者相当于把指定的servlet对应的url-pattern作为filter的匹配模式
filter的路径匹配和servlet是一样的，都遵循servlet规范中《SRV.11.2 Specification of Mappings》一节的说明 

```
    <filter-mapping>     
    <filter-name>Logging Filter</filter-name>     
    <url-pattern>/products/*</url-pattern>     
    <dispatcher>FORWARD</dispatcher>     
    <dispatcher>REQUEST</dispatcher>     
    </filter-mapping>  
```
在这种情况下，如果请求是以`/products/…`开头的，并且是通过request dispatcher的forward方法传递过来或者直接从客户端传递过来的，则必须经过这个过滤器。

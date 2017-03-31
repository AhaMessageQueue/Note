## Jersey介绍

1. Jersey是JAX-RS的参考实现,它包含三个主要部分,它可以帮助您实现RESTful Web服务.
2. 核心服务器(Core Server):通过提供JSR 311中标准化的注释和API标准化,可以用直观的方式开发RESTful Web服务.
3. 核心客户端(Core Client):Jersey客户端API可以让我们与REST服务轻松通信.
4. 集成(Integration):Jersey还提供可以轻松集成Spring,Guice,Apache Abdera的库.

## Jersey spring整合
Web集成:在web.xml中加入servlet,见配置
```
<!-- Jersey Servlet 配置 -->
    <servlet>
        <servlet-name>JerseyServlet</servlet-name>
        <servlet-class>com.sun.jersey.spi.spring.container.servlet.SpringServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>JerseyServlet</servlet-name>
        <url-pattern>/rest/*</url-pattern>
    </servlet-mapping>
```
spring集成:将所需要的jar增加至classpath中,见maven pom.xml配置
```
<!-- rest begin -->
        <dependency>
            <groupId>com.sun.jersey</groupId>
            <artifactId>jersey-server</artifactId>
            <version>${version.jersey}</version>
        </dependency>

        <dependency>
            <groupId>com.sun.jersey</groupId>
            <artifactId>jersey-client</artifactId>
            <version>${version.jersey}</version>
        </dependency>

        <dependency>
            <groupId>com.sun.jersey.contribs</groupId>
            <artifactId>jersey-spring</artifactId>
            <version>${version.jersey}</version>
            <!-- 去除自带的spring版本 -->
            <exclusions>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-aop</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-context</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-beans</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-core</artifactId>
                </exclusion>
                <exclusion>
                    <artifactId>spring-web</artifactId>
                    <groupId>org.springframework</groupId>
                </exclusion>
            </exclusions>
        </dependency>
        <!-- rest end -->
```

## 示例详解
先看一个最简单的例子
```
@Component
@Path("/user")
public class UserResource {
 
    @Path("/users/{username}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getUser(@PathParam("username") String username) {
        log.debug("username:{}", username);
        return username;
    }
}
```

- @Component注解定义此类为spring组件,即bean类.
> - @Path注解的值是一个相对的URI路径,值得注意的是你仍然可以在URI中嵌入变量,方法如下@Path(“/users/{username}”).
  如果需要对userName进行规则匹配,可以自定义的正则表达式,它将覆盖默认的正则表达式”[^/]+”.
  例如@Path(“users/{username: [a-zA-Z][a-zA-Z_0-9]}*”),这里username将只匹配一个大写或小写字母和零个或多个字母数字字符和下划线开头,如果给定参数不匹配,将出现404错误.
  @Path中定义的URL前后是否有”/”,都不会有影响,但如果你启用了重定向机制,则上面的规则无效.
- @GET注解是请求方法指示符,这个指示符注解的Java方法会处理HTTP GET请求.
- @Produces注解用于指定MIME类型,通常与MediaType联用,可定义在方法区或者类定义上.
- 若方法区上与类定义上都指定了@Produces,那么方法区上的@Produces会覆盖掉类定义上的@Produces.
> - @QueryParam注解用于从请求的URL中提取查询参数,且只能在get请求中使用,@QueryParameter可以转换任何有以String为参数的构造函数类.
- @DefaultValue注解表示如果url中参数不存在,则会按DefaultValue给定值赋值,如果url中给定值不是给定类型,则会报404错误.

```
    @Path("save")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void saveUser(@FormParam("id") Stringid,
                        @FormParam("username") Stringusername,@Context HttpServletRequestrequest,
                        @Context HttpServletResponseresponse){
        log.debug("id:{},
 username:{}",id,username);

        log.debug("request:{},
 response:{}",request,response);
    }
```
- @Consumes注解指定了客户端发送的MIME媒体类型,与@Produces一样,通常与MediaType联用,并且可定义在方法区或者类定义上.
- @POST注解是请求方法指示符,这个指示符注解的Java方法会处理HTTP POST请求.
- @Context注解是注入上下文对象,如Request,Response,UriInfo,Servlet,Context等.
- @FormParam注解用于POST请求,@QueryParam用于GET请求,作用与@QueryParam一样.

## 返回结果

- 直接返回对象,用@Produces控制输出的格式为Html/Xml/JSON等.
- 返回Response对象,根据不同的输入参数,决定返回Xml/JSON.

## 异常处理
最简单的抛一个干净的500错误.
```
throw new WebApplicationException();
```
如果需要自定义错误返回码和错误信息,可以使用如下方法:
```
new WebApplicationException(Response.status(status).entity(message).type(MediaType.TEXT_PLAIN).build());
```


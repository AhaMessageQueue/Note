>摘要: Spring MVC 4.1 支持jsonp
 
### 使用ResponseBodyAdvice支持jsonp
ResponseBodyAdvice是一个接口，接口描述，
```java
package org.springframework.web.servlet.mvc.method.annotation;

/**
 * Allows customizing the response after the execution of an {@code @ResponseBody}
 * or an {@code ResponseEntity} controller method but before the body is written
 * with an {@code HttpMessageConverter}.
 *
 * <p>Implementations may be may be registered directly with
 * {@code RequestMappingHandlerAdapter} and {@code ExceptionHandlerExceptionResolver}
 * or more likely annotated with {@code @ControllerAdvice} in which case they
 * will be auto-detected by both.
 *
 * @author Rossen Stoyanchev
 * @since 4.1
 */
public interface ResponseBodyAdvice<T> {

   /**
    * Whether this component supports the given controller method return type
    * and the selected {@code HttpMessageConverter} type.
    * @param returnType the return type
    * @param converterType the selected converter type
    * @return {@code true} if {@link #beforeBodyWrite} should be invoked, {@code false} otherwise
    */
   boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType);

   /**
    * Invoked after an {@code HttpMessageConverter} is selected and just before
    * its write method is invoked.
    * @param body the body to be written
    * @param returnType the return type of the controller method
    * @param selectedContentType the content type selected through content negotiation
    * @param selectedConverterType the converter type selected to write to the response
    * @param request the current request
    * @param response the current response
    * @return the body that was passed in or a modified, possibly new instance
    */
   T beforeBodyWrite(T body, MethodParameter returnType, MediaType selectedContentType,
         Class<? extends HttpMessageConverter<?>> selectedConverterType,
         ServerHttpRequest request, ServerHttpResponse response);

}
```
**作用：**
允许在执行{@code @ResponseBody}或{@code ResponseEntity}控制器方法之后，
在使用{@code HttpMessageConverter}编写正文之前自定义响应。

其中一个方法就是 `beforeBodyWrite` 在使用相应的`HttpMessageConvert`进行write之前会被调用，就是一个切面方法。

和jsonp有关的实现类是`AbstractJsonpResponseBodyAdvice`，如下是 `beforeBodyWrite` 方法的实现，
```java
@Override
public final Object beforeBodyWrite(Object body, MethodParameter returnType,
      MediaType contentType, Class<? extends HttpMessageConverter<?>> converterType,
      ServerHttpRequest request, ServerHttpResponse response) {

   MappingJacksonValue container = getOrCreateContainer(body);
   beforeBodyWriteInternal(container, contentType, returnType, request, response);
   return container;
}
```

上面方法`beforeBodyWrite()`位于`AbstractJsonpResponseBodyAdvice`的父类中，
而`beforeBodyWriteInternal()`是在`AbstractJsonpResponseBodyAdvice`中实现的 ，如下，

```java
@Override
protected void beforeBodyWriteInternal(MappingJacksonValue bodyContainer, MediaType contentType,
      MethodParameter returnType, ServerHttpRequest request, ServerHttpResponse response) {

   HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

   for (String name : this.jsonpQueryParamNames) {
      String value = servletRequest.getParameter(name);
      if (value != null) {
         MediaType contentTypeToUse = getContentType(contentType, request, response);
         response.getHeaders().setContentType(contentTypeToUse);
         bodyContainer.setJsonpFunction(value);
         return;
      }
   }
}
```
就是根据`callback`请求参数或配置的其他参数来确定返回jsonp协议的数据。

**如何实现jsonp？**

首先继承`AbstractJsonpResponseBodyAdvice`，如下，
```java
package com.usoft.web.controller.jsonp;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.AbstractJsonpResponseBodyAdvice;

/**
 * 
 */
@ControllerAdvice(basePackages = "com.usoft.web.controller.jsonp")
public class JsonpAdvice extends AbstractJsonpResponseBodyAdvice {
    public JsonpAdvice() {
        super("callback", "jsonp");
    }
}
```

`super("callback", "jsonp");`的意思就是当请求参数中包含`callback`或`jsonp`参数时，就会返回jsonp协议的数据。
由AbstractJsonpResponseBodyAdvice.beforeBodyWriteInternal()方法中的语句：`String value = servletRequest.getParameter(name);`知：其value就作为回调函数的名称。

`super("callback", "jsonp");`即调用`AbstractJsonpResponseBodyAdvice`类的构造方法：
```java
protected AbstractJsonpResponseBodyAdvice(String... queryParamNames) {
    Assert.isTrue(!ObjectUtils.isEmpty(queryParamNames), "At least one query param name is required");
    this.jsonpQueryParamNames = queryParamNames;
}
```

这里必须使用`@ControllerAdvice`注解标注该类，并且配置对哪些Controller起作用。

关于注解@ControllerAdvice 的作用这里不做描述。

Controller实现jsonp，
```java
package com.usoft.web.controller.jsonp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.usoft.web.controller.JsonMapper;
import com.usoft.web.controller.Person;

/**
 * jsonp
 */
@Controller
public class JsonpController {

    /**
     * callback({"id":1,"age":12,"name":"lyx"})
     * 
     * @param args
     */
    public static void main(String args[]) {
        Person person = new Person(1, "lyx", 12);
        System.out.println(JsonMapper.nonNullMapper().toJsonP("callback",
            person));
    }

    @RequestMapping("/jsonp1")
    public Person jsonp1() {
        return new Person(1, "lyx", 12);
    }

    @RequestMapping("/jsonp2")
    @ResponseBody
    public Person jsonp2() {
        return new Person(1, "lyx", 12);
    }

    @RequestMapping("/jsonp3")
    @ResponseBody
    public String jsonp3() {
        return JsonMapper.nonNullMapper().toJsonP("callback",
            new Person(1, "lyx", 12));
    }
}
```
`jsonp2`方法就是一个**jsonp协议**的调用。`http://localhost:8081/jsonp2?callback=test`可以直接调用这个方法，并且返回jsonp协议的数据。

通过debug代码，我们来看一下他是怎么返回jsonp协议的数据的。

正因为我们前面在该Controller上配置了 `JsonpAdvice` 的 `ControllerAdvice`，
在调用 `MappingJackson2HttpMessageConverter`的`write()`方法往回写数据的时候，首先会调用`beforeBodyWrite`，

具体的代码如下，
```java
@Override
protected void beforeBodyWriteInternal(MappingJacksonValue bodyContainer, MediaType contentType,
      MethodParameter returnType, ServerHttpRequest request, ServerHttpResponse response) {

   HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

   for (String name : this.jsonpQueryParamNames) {
      String value = servletRequest.getParameter(name);
      if (value != null) {
         MediaType contentTypeToUse = getContentType(contentType, request, response);
         response.getHeaders().setContentType(contentTypeToUse);
         bodyContainer.setJsonpFunction(value);
         return;
      }
   }
}
```
当请求参数中含有配置的相应的回调参数时，就会`bodyContainer.setJsonpFunction(value);`这就标志着返回的数据时jsonp格式的数据。

其中：
`getContentType(contentType, request, response)`方法如下：
```java
protected MediaType getContentType(MediaType contentType, ServerHttpRequest request, ServerHttpResponse response) {
    return new MediaType("application", "javascript");
}
```
由此可知，`response.getHeaders().setContentType(contentTypeToUse);`response响应体的类型为：`Content-Type: application/javascript`

然后接下来就到了 `MappingJackson2HttpMessageConverter` 的`write()`方法真正写数据的时候了。
看他是怎么写数据的，相关的代码如下，
```java
@Override
protected void writeInternal(Object object, HttpOutputMessage outputMessage)
      throws IOException, HttpMessageNotWritableException {

   JsonEncoding encoding = getJsonEncoding(outputMessage.getHeaders().getContentType());
   JsonGenerator generator = this.objectMapper.getFactory().createGenerator(outputMessage.getBody(), encoding);
   try {
      writePrefix(generator, object);
      Class<?> serializationView = null;
      Object value = object;
      if (value instanceof MappingJacksonValue) {
         MappingJacksonValue container = (MappingJacksonValue) object;
         value = container.getValue();
         serializationView = container.getSerializationView();
      }
      if (serializationView != null) {
         this.objectMapper.writerWithView(serializationView).writeValue(generator, value);
      }
      else {
         this.objectMapper.writeValue(generator, value);
      }
      writeSuffix(generator, object);
      generator.flush();

   }
   catch (JsonProcessingException ex) {
      throw new HttpMessageNotWritableException("Could not write content: " + ex.getMessage(), ex);
   }
}
```
```java
@Override
protected void writePrefix(JsonGenerator generator, Object object) throws IOException {
   if (this.jsonPrefix != null) {
      generator.writeRaw(this.jsonPrefix);
   }
   String jsonpFunction =
         (object instanceof MappingJacksonValue ? ((MappingJacksonValue) object).getJsonpFunction() : null);
   if (jsonpFunction != null) {
      generator.writeRaw(jsonpFunction + "(");
   }
}
```
```java
@Override
protected void writeSuffix(JsonGenerator generator, Object object) throws IOException {
   String jsonpFunction =
         (object instanceof MappingJacksonValue ? ((MappingJacksonValue) object).getJsonpFunction() : null);
   if (jsonpFunction != null) {
      generator.writeRaw(");");
   }
}
```
代码非常清晰。看我们jsonp调用的结果。
```
http://localhost:8081/jsonp2?callback=test
```
响应消息如下，
```
HTTP/1.1 200 OK

Server: Apache-Coyote/1.1

Content-Type: application/javascript

Transfer-Encoding: chunked

Date: Sun, 19 Jul 2015 13:01:02 GMT


test({"id":1,"age":12,"name":"lyx"});
```
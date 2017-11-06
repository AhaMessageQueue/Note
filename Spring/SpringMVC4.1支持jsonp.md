>摘要: Spring MVC 4.1 支持jsonp

## 继承关系

```text
interface ResponseBodyAdvice<T>
    -   abstract class AbstractMappingJacksonResponseBodyAdvice
        -   abstract class AbstractJsonpResponseBodyAdvice
```

## 源码解读

### `ResponseBodyAdvice`接口

`ResponseBodyAdvice`定义了接口：

```java
package org.springframework.web.servlet.mvc.method.annotation;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

/**
 * 翻译：允许在执行{@code @ResponseBody}或{@code ResponseEntity}控制器方法之后，但在使用{@code HttpMessageConverter}写body之前自定义响应。
 * 
 * <p>该接口实现可以直接用{@code RequestMappingHandlerAdapter}和{@code ExceptionHandlerExceptionResolver}注册，
 * 或者更倾向于用{@code @ControllerAdvice}注解，在这种情况下，它们将被两者自动检测。
 * <p>
 * Allows customizing the response after the execution of an {@code @ResponseBody}
 * or a {@code ResponseEntity} controller method but before the body is written
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

其中一个方法就是 `beforeBodyWrite` ，在使用相应的`HttpMessageConvert`进行写之前会被调用。

### AbstractMappingJacksonResponseBodyAdvice抽象类

`AbstractMappingJacksonResponseBodyAdvice`抽象类实现了`beforeBodyWrite` 方法：

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

而`beforeBodyWriteInternal()`是在`AbstractJsonpResponseBodyAdvice`中实现的。

### AbstractJsonpResponseBodyAdvice抽象类

```java
package org.springframework.web.servlet.mvc.method.annotation;

import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * ResponseBodyAdvice的一个方便的基础类，用于指示org.springframework.http.converter.json.MappingJackson2HttpMessageConverter使用JSONP格式进行序列化。
 * <p>子类必须指定查询参数名称以检查JSONP回调函数的名称。
 * <p>子类更倾向于使用@ControllerAdvice注释注释，并自动检测，否则必须直接使用RequestMappingHandlerAdapter和ExceptionHandlerExceptionResolver进行注册。
 * 
 * <p>A convenient base class for a {@code ResponseBodyAdvice} to instruct the
 * {@link org.springframework.http.converter.json.MappingJackson2HttpMessageConverter}
 * to serialize with JSONP formatting.
 *
 * <p>Sub-classes must specify the query parameter name(s) to check for the name
 * of the JSONP callback function.
 *
 * <p>Sub-classes are likely to be annotated with the {@code @ControllerAdvice}
 * annotation and auto-detected or otherwise must be registered directly with the
 * {@code RequestMappingHandlerAdapter} and {@code ExceptionHandlerExceptionResolver}.
 *
 * @author Rossen Stoyanchev
 * @since 4.1
 */
public abstract class AbstractJsonpResponseBodyAdvice extends AbstractMappingJacksonResponseBodyAdvice {

	/**
	 * Pattern for validating jsonp callback parameter values.
	 */
	private static final Pattern CALLBACK_PARAM_PATTERN = Pattern.compile("[0-9A-Za-z_\\.]*");


	private final Log logger = LogFactory.getLog(getClass());

	private final String[] jsonpQueryParamNames;


	protected AbstractJsonpResponseBodyAdvice(String... queryParamNames) {
		Assert.isTrue(!ObjectUtils.isEmpty(queryParamNames), "At least one query param name is required");
		this.jsonpQueryParamNames = queryParamNames;
	}


	@Override
	protected void beforeBodyWriteInternal(MappingJacksonValue bodyContainer, MediaType contentType,
			MethodParameter returnType, ServerHttpRequest request, ServerHttpResponse response) {

		HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

		for (String name : this.jsonpQueryParamNames) {
			String value = servletRequest.getParameter(name);
			if (value != null) {
				if (!isValidJsonpQueryParam(value)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring invalid jsonp parameter value: " + value);
					}
					continue;
				}
				MediaType contentTypeToUse = getContentType(contentType, request, response);
				response.getHeaders().setContentType(contentTypeToUse);
				bodyContainer.setJsonpFunction(value);
				break;
			}
		}
	}

	/**
	 * Validate the jsonp query parameter value. The default implementation
	 * returns true if it consists of digits, letters, or "_" and ".".
	 * Invalid parameter values are ignored.
	 * @param value the query param value, never {@code null}
	 * @since 4.1.8
	 */
	protected boolean isValidJsonpQueryParam(String value) {
		return CALLBACK_PARAM_PATTERN.matcher(value).matches();
	}

	/**
	 * Return the content type to set the response to.
	 * This implementation always returns "application/javascript".
	 * @param contentType the content type selected through content negotiation
	 * @param request the current request
	 * @param response the current response
	 * @return the content type to set the response to
	 */
	protected MediaType getContentType(MediaType contentType, ServerHttpRequest request, ServerHttpResponse response) {
		return new MediaType("application", "javascript");
	}

}
```

就是根据`callback`请求参数或配置的其他参数来确定返回jsonp协议的数据。

## 如何实现jsonp

首先继承`AbstractJsonpResponseBodyAdvice`，如下，

```java
package com.github.ittalks.fn.common.advice;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.AbstractJsonpResponseBodyAdvice;

/**
 * Created by 刘春龙 on 2017/5/25.
 */
@ControllerAdvice
public class JsonpAdvice extends AbstractJsonpResponseBodyAdvice {

    public JsonpAdvice() {
        super("callback", "jsonp");
    }
}
```

`super("callback", "jsonp");`的意思就是当请求参数中包含`callback`或`jsonp`参数时，就会返回jsonp协议的数据。

由`AbstractJsonpResponseBodyAdvice.beforeBodyWriteInternal()`方法中的语句：`String value = servletRequest.getParameter(name);`知：其value就作为回调函数的名称。

`super("callback", "jsonp");`即调用`AbstractJsonpResponseBodyAdvice`类的构造方法：

```java
protected AbstractJsonpResponseBodyAdvice(String... queryParamNames) {
    Assert.isTrue(!ObjectUtils.isEmpty(queryParamNames), "At least one query param name is required");
    this.jsonpQueryParamNames = queryParamNames;
}
```

这里必须使用`@ControllerAdvice`注解标注该类，并且配置对哪些Controller起作用。

Controller实现jsonp：












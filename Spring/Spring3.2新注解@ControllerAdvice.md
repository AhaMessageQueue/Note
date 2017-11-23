@ControllerAdvice，是spring3.2提供的新注解，从名字上可以看出大体意思是控制器增强。让我们先看看@ControllerAdvice的实现：

```java
@Target(ElementType.TYPE)  
@Retention(RetentionPolicy.RUNTIME)  
@Documented  
@Component  
public @interface ControllerAdvice {  
  
}
```

没什么特别之处，该注解使用@Component注解，这样的话当我们使用`<context:component-scan>`扫描时也能扫描到

其javadoc定义是：

```text
/**
* Indicates the annotated class assists a "Controller".
*
* <p>Serves as a specialization of {@link Component @Component}, allowing for
* implementation classes to be autodetected through classpath scanning.
*
* <p>It is typically used to define {@link ExceptionHandler @ExceptionHandler},
* {@link InitBinder @InitBinder}, and {@link ModelAttribute @ModelAttribute}
* methods that apply to all {@link RequestMapping @RequestMapping} methods.
*
* @author Rossen Stoyanchev
* @since 3.2
*/
```

即把`@ControllerAdvice`注解内部使用`@ExceptionHandler`、`@InitBinder`、`@ModelAttribute`注解的方法应用到所有的`@RequestMapping`注解的方法。
非常简单，不过只有当使用`@ExceptionHandler`最有用，另外两个用处不大。

接下来看段代码：

```java
@ControllerAdvice  
public class ControllerAdviceTest {  
  
    @ModelAttribute  
    public User newUser() {  
        System.out.println("============应用到所有@RequestMapping注解方法，在其执行之前把返回值放入Model");  
        return new User();  
    }  
  
    @InitBinder  
    public void initBinder(WebDataBinder binder) {  
        System.out.println("============应用到所有@RequestMapping注解方法，在其执行之前初始化数据绑定器");  
    }  
  
    @ExceptionHandler(UnauthenticatedException.class)  
    @ResponseStatus(HttpStatus.UNAUTHORIZED)  
    public String processUnauthenticatedException(NativeWebRequest request, UnauthenticatedException e) {  
        System.out.println("===========应用到所有@RequestMapping注解方法，在其抛出UnauthenticatedException异常时执行");  
        return "viewName"; //返回一个逻辑视图名  
    }  
}
```

如果你的spring-mvc配置文件使用如下方式扫描bean

```xml
<context:component-scan base-package="com.sishuok.es" use-default-filters="false">  
   <context:include-filter type="annotation" expression="org.springframework.stereotype.Controller"/>  
</context:component-scan>
```

需要把`@ControllerAdvice`包含进来，否则不起作用：

```xml
<context:component-scan base-package="com.sishuok.es" use-default-filters="false">  
   <context:include-filter type="annotation" expression="org.springframework.stereotype.Controller"/>  
   <context:include-filter type="annotation" expression="org.springframework.web.bind.annotation.ControllerAdvice"/>  
</context:component-scan>
```

1. `@ModelAttribute`注解的方法作用请参考SpringMVC强大的数据绑定（2）——第六章 注解式控制器详解——跟着开涛学SpringMVC中的【二、暴露表单引用对象为模型数据】，作用是一样的，只不过此处是对所有的`@RequestMapping`注解的方法都起作用。当需要设置全局数据时比较有用。
2. `@InitBinder`注解的方法作用请参考SpringMVC数据类型转换——第七章 注解式控制器的数据验证、类型转换及格式化——跟着开涛学SpringMVC，同1类似。当需要全局注册时比较有用。
3. `@ExceptionHandler`，异常处理器，此注解的作用是当出现其定义的异常时进行处理的方法，其可以使用springmvc提供的数据绑定，比如注入HttpServletRequest等，还可以接受一个当前抛出的Throwable对象。可以参考javadoc或snowolf的Spring 注解学习手札（八）补遗——@ExceptionHandler。
 
该注解非常简单，大多数时候其实只`@ExceptionHandler`比较有用，其他两个用到的场景非常少，这样可以把异常处理器应用到所有控制器，而不是`@Controller`注解的单个控制器。
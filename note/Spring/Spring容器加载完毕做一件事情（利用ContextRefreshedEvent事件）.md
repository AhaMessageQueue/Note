关键字：spring容器加载完毕做一件事情（利用ContextRefreshedEvent事件） 

应用场景：很多时候我们想要在某个类加载完毕时干某件事情，但是使用了spring管理对象，
我们这个类引用了其他类（可能是更复杂的关联），所以当我们去使用这个类做事情时发现报空指针错误，
这是因为我们这个类有可能已经初始化完成，但是引用的其他类不一定初始化完成，所以发生了空指针错误，解决方案如下： 

### 监听ContextRefreshedEvent事件
1. 写一个类继承spring的ApplicationListener监听，并监听ContextRefreshedEvent事件（容器初始化完成事件） 
2. 定义简单的bean：
`<bean id="beanDefineConfigue" class="com.creatar.portal.webservice.BeanDefineConfigue"></bean> `
或者直接使用@Component("BeanDefineConfigue")注解方式 

完整的类如下： 
```
package com.creatar.portal.webservice;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component("BeanDefineConfigue")
public class BeanDefineConfigue implements
ApplicationListener<ContextRefreshedEvent> {//ContextRefreshedEvent为初始化完毕事件，spring还有很多事件可以利用

// @Autowired
// private IRoleDao roleDao;


/**
* 当一个ApplicationContext被初始化或刷新触发
*/
@Override
public void onApplicationEvent(ContextRefreshedEvent event) {
// roleDao.getUserList();//spring容器初始化完毕，加载用户列表到内存
System.out.println("=========================spring容器初始化完毕=======================");
}

} 
```
或者使用xml配置方式（非注解），简单配置个bean即可 
```
<bean id="beanDefineConfigue" class="com.creatar.portal.webservice.BeanDefineConfigue"></bean> 
```

### 其他定义方式：

完整的类如下： 
```
package com.creatar.portal.webservice;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component("BeanDefineConfigue2")
public class BeanDefineConfigue2 implements ApplicationListener<ApplicationEvent> {

List<String> list = new ArrayList<String>();

/**
* 当一个ApplicationContext被初始化或刷新触发
*/
@Override
public void onApplicationEvent(ApplicationEvent event) {
if (event instanceof ContextRefreshedEvent) {
System.out.println("==========================spring容器初始化完毕======================");
}

}
} 
```

### spring其他事件：

spring中已经内置的几种事件：
```
ContextClosedEvent
ContextRefreshedEvent
ContextStartedEvent
ContextStoppedEvent
RequestHandleEvent
```

### 后续研究
applicationContext和使用MVC之后的webApplicationContext会两次调用上面的方法，如何区分这个两种容器呢？

但是这个时候，会存在一个问题，在web 项目中（spring mvc），系统会存在两个容器，
一个是root application context ,另一个就是我们自己的 projectName-servlet context（作为root application context的子容器）。

这种情况下，就会造成onApplicationEvent方法被执行两次。为了避免上面提到的问题，我们可以只在root application context初始化完成后调用逻辑代码，其他的容器的初始化完成，则不做任何处理，修改后代码 
```
@Override 
public void onApplicationEvent(ContextRefreshedEvent event) { 
if(event.getApplicationContext().getParent() == null){//root application context 没有parent，他就是老大. 
     //需要执行的逻辑代码，当spring容器初始化完成后就会执行该方法。 
} 
}  
```

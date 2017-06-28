Spring Web Flow (SWF)是Spring Framework的一个脱离模块。这个模块是Spring Web应用开发模块栈的一部分，Spring Web包含Spring MVC。

Spring Web Flow的目标是成为管理Web应用页面流程的最佳方案。当你的应用需要复杂的导航控制，例如向导，在一个比较大的事务过程中去指导用户经过一连串的步骤的时候，SWF将会是一个功能强大的控制器。

下面我们还是从一个简单的demo开始了解它：

这个例子是结合Springmvc来实现，项目结构：

![](..\images\CartApp3项目目录树.png)

**Web.xml配置：**
```
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://java.sun.com/xml/ns/javaee" xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd" xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd" id="WebApp_ID" version="2.5">
  <display-name>CartApp</display-name>

<!-- 配置springmvc需要的servlet -->

<servlet>
  <servlet-name>CartServlet</servlet-name>
  <servlet-class>
  org.springframework.web.servlet.DispatcherServlet
  </servlet-class>
  <init-param>
    <param-name>contextConfigLocation</param-name> 
    <param-value> 
	/WEB-INF/config/web-application-config.xml 
	</param-value> 
  </init-param> 
  <load-on-startup>1</load-on-startup>
</servlet>

	<servlet-mapping>
		<servlet-name>CartServlet</servlet-name>
		<url-pattern>/spring/*</url-pattern>
	</servlet-mapping>
 
  <welcome-file-list>  
    <welcome-file>index.jsp</welcome-file>
  </welcome-file-list>
</web-app>
```
**对应的SpringMVC的配置文件：web-application-config.xml**
```
<?xml version="1.0" encoding="utf-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:context="http://www.springframework.org/schema/context"
	xsi:schemaLocation="http://www.springframework.org/schema/beans 
    http://www.springframework.org/schema/beans/spring-beans-2.5.xsd 
	http://www.springframework.org/schema/context 
	http://www.springframework.org/schema/context/spring-context-2.5.xsd">
	
	<!-- SpringMVC的配置文件-2015年6月14日15:45:54 -->
	<!-- 搜索 samples.webflow 包里的 @Component 注解，并将其部署到容器中 -->
	<context:component-scan base-package="samples.webflow" />
	<!-- 启用基于注解的配置 -->
	<context:annotation-config />
	<import resource="webmvc-config.xml" />
	<import resource="webflow-config.xml" />
</beans>

```
**其中引入的两个配置文件：webmvc-config.xml**
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans 
    http://www.springframework.org/schema/beans/spring-beans.xsd">
    
    <!-- SpringMVC的配置文件-2015年6月14日15:45:54 -->
    
    <!-- 对转向页面的路径解析。prefix：前缀， suffix：后缀 -->   
	<bean id="viewResolver"
		class="org.springframework.web.servlet.view.InternalResourceViewResolver">
		<property name="viewClass"
			value="org.springframework.web.servlet.view.JstlView">
		</property>
		<property name="prefix" value="/WEB-INF/jsp/">
		</property>
		<property name="suffix" value=".jsp">
		</property>
	</bean>
	<!--  如何根据http请求选择合适的controller是MVC中一项十分关键的功能，在Spring MVC中，HandlerMapping接口是这一活动的抽象 -->
	<!-- SimpleUrlHandlerMapping  通过配置文件，把一个URL映射到Controller -->
	<bean id="viewMappings"
		class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
		<!-- /shopping.do 请求由 flowController 来处理 -->
		<!-- 不管设成 /shopping.do 还是设成 /shopping ，或者 /shopping.htm ，效果都是一样的， flowController 都会去找 id 为 shopping的flow来执行 -->
		<property name="mappings">
			<value> /shopping.do=flowController </value>
		</property>
		 <property name="defaultHandler">
			<!-- UrlFilenameViewController 会将 "/index" 这样的请求映射成名为 "index" 的视图 -->
			<bean class="org.springframework.web.servlet.mvc.UrlFilenameViewController" />
		</property> 
	</bean>
	
	<!-- 另外一种配置方式：-->
	<!-- 
        <bean class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">  
            <property name="mappings">  
                <props>  
                    <!-- 这个逻辑视图名的 前缀 必须与流程注册表中的  
                        webflow:flow-location 的 id一致，  
                        而 后缀 必须是当前DispatcherServlet匹配的地址,也就是  
                        必须以.flow结束,否则不被前端控制器处理(视图名必须匹配*.flow)  
                     -->  
                    <!-- 这里代表将请求路径为hello.flow的url交给flowController处理 -->  
                    <prop key="hello.flow">flowController</prop>  
                </props>  
            </property>  
        </bean>  
    -->  
	
	<!-- 我们只要明白 FlowController 可根据客户端请求的结尾部分，找出相应的 flow 来执行。配置 FlowController只需指定FlowExecutor即可 -->
	<bean id="flowController" class="org.springframework.webflow.mvc.servlet.FlowController">
		<property name="flowExecutor" ref="flowExecutor" />
	</bean>
</beans>
```
对于UrlFilenameViewController类，此处理解为由于我们并不是通过访问controller来返回页面的形式，那么我们如何可以访问Web-INF下的保护类资源呢，就是通过这个类的作用来实现。此处的理解有什么偏差还请各位提出宝贵意见！

**webflow-config.xml：**
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:webflow="http://www.springframework.org/schema/webflow-config"
	xsi:schemaLocation=" http://www.springframework.org/schema/beans 
    http://www.springframework.org/schema/beans/spring-beans-2.5.xsd 
    http://www.springframework.org/schema/webflow-config 
    http://www.springframework.org/schema/webflow-config/spring-webflow-config-2.0.xsd">
    
    <!-- 流程的配置文件-2015年6月14日15:45:46 -->
    
    <!-- FlowExecutor 是 Spring Web Flow 的一个核心接口，启动某个 flow ，都要通过这个接口来进行。
    	从配置角度来说，只要保证有个 FlowExecutor 就可以了， Spring Web Flow 的默认行为已经足够 -->
    <!-- FlowExecutor 就是通过 id 来找出要执行的 flow 。至于这个 id ，则是要由用户来指定的。
    	在默认配置情况下，如果客户端发送了如下URL请求：http://localhost:8080/CartApp/spring/shopping。
    	则从 Spring Web Flow 的角度来看，这个 URL 就表示客户想要执行一个 id 为“ shopping ”的 flow ，
    	于是就会在 FlowRegistry 中查找名为“ shopping ”的 flow，由FlowExecutor负责执行。 -->
	<webflow:flow-executor id="flowExecutor" />
	
	<!-- 所有 flow的定义文件它的位置在这里进行配置， flow-builder-services 用于配置 flow 的特性 -->
	<!-- FlowRegistry 是存放 flow 的仓库，每个定义 flow 的 XML 文档被解析后，都会被分配一个唯一的 id ，并以 FlowDefinition 对象的形式存放在 FlowResigtry 中 -->
	<!-- 每个 flow 都必须要有 id 来标识，如果在配置中省略，那么该 flow 默认的 id 将是该定义文件（xml文件）的文件名去掉后缀所得的字符串
	（例如本例中如果去掉id="shopping"，那么flow的id就是shopping.xml去掉后缀名.xml后的shopping作为id） -->
	
	<!--  flow-builder-services 属性的配置指明了在这个 flow-registry “仓库”里的 flow 的一些基本特性，
		例如，是用 Unified EL 还是 OGNL 、 model （模型）对象中的数据在显示之前是否需要先作转换，等等 -->
		
	<webflow:flow-registry id="flowRegistry" flow-builder-services="flowBuilderServices">
		<webflow:flow-location path="/WEB-INF/flows/shopping.xml" id="shopping" />
		<webflow:flow-location path="/WEB-INF/flows/addToCart.xml" id="addToCart" />
	</webflow:flow-registry>
	
	<!--Web Flow 中的视图通过 MVC 框架的视图技术来呈现 -->
	
	<webflow:flow-builder-services id="flowBuilderServices" view-factory-creator="mvcViewFactoryCreator" />
	
	<!-- 指明 MVC 框架的 view resolver ，用于通过 view 名查找资源 -->
	<bean id="mvcViewFactoryCreator" class="org.springframework.webflow.mvc.builder.MvcViewFactoryCreator">
		<property name="viewResolvers" ref="viewResolver" />
	</bean>
</beans>
```
**使用的流程文件：shopping.xml**
```
<?xml version="1.0" encoding="UTF-8"?>
<flow xmlns="http://www.springframework.org/schema/webflow"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/webflow
 http://www.springframework.org/schema/webflow/spring-webflow-2.0.xsd">
 <!-- view-state中的view对应jsp文件夹中的jsp页面，on是触发事件，to对应state id -->
	<!-- 流程文件-2015年6月14日15:51:56 -->
	<!-- 根据排在第一位的顺序来执行 -->
	<view-state id="viewCart" view="viewCart">
		<transition on="submit" to="viewOrder">
		</transition>
	</view-state>
	<view-state id="viewOrder" view="viewOrder">
		<transition on="confirm" to="orderConfirmed">
		</transition>
	</view-state>
	<view-state id="orderConfirmed" view="orderConfirmed">
		<transition on="returnToIndex" to="returnToIndex">
		</transition>
	</view-state>
	<!-- externalRedirect 用在 view 名字中，表示所指向的资源是在 flow 的外部， servletRelative 则表明所指向资源的路径起始部分与 flow 所在 servlet 相同 -->
	<end-state id="returnToIndex" view="externalRedirect:servletRelative:/index.jsp">
	</end-state>
</flow>

```
**对应的页面：**

**index.jsp**
```
<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Cart Application</title>
</head>
<body>
	<h1>Hello!</h1>
	<br />
	<a href="shopping.do">View Cart</a>
</body>
</html><span style="font-family: Arial, Helvetica, sans-serif; background-color: rgb(255, 255, 255);"> </span>
```

**viewCart.jsp**
```
<body>
	<h1>View Cart</h1>
	<a href="${flowExecutionUrl}&_eventId=submit">Submit</a>
</body>
```

**viewOrder.jsp**
```
<body>
	<h1>Order</h1>
	<a href="${flowExecutionUrl}&_eventId=confirm">Confirm</a>

</body>
```

**orderConfirmed.jsp**
```
<body>
	<h1>Order Confirmed</h1>
	<a href="${flowExecutionUrl}&_eventId=returnToIndex">Return to
		index</a>
</body>

```
这几个页面都使用了变量 flowExecutionUrl ，表示 flow 执行到当前状态时的 URL 。 flowExecutionUrl 的值已经由 Spring Web Flow 2.0 框架的代码进行赋值，并放入相应的 model 中供 view 访问。 flowExecutionUrl 的值包含 flow 在执行过程中会为每一状态生成的唯一的 key ，因此不可用其他手段来获取。请求参数中 _eventId 的值与shoppting.xml中 transition 元素的 on 属性的值是对应的，在接收到_eventId参数后，相应transition会被执行。

**测试使用方式：**

访问地址：http://localhost:8080/CartApp3/spring/index.jsp

**总的来说，为什么要配置这么多内容呢？原因如下：**

**SpringWeb Flow 如何与 Spring Web MVC 整合在一起？**

客户端发送的请求，先会由 servlet 容器（Tomcat）接收， servlet容器会找到相应的应用程序（CartApp3），再根据 web.xml 的配置找到出符合映射条件的 servlet 来处理。Spring Web MVC 中处理请求的 servlet 是 DispatcherServlet ，如果请求的路径满足 DispatcherServlet的映射条件，则 DispatcherServlet 会找出 Spring IoC 容器中所有的 HandlerMapping ，根据这些HandlerMapping 中匹配最好的 handler （一般情况下都是 controller ，即控制器）来处理请求。当 Controller处理完毕，一般都会返回一个 view （视图）的名字，DispatcherServlet再根据这个view的名字找到相应的视图资源返回给客户端。

弄清楚Spring Web MVC 处理请求的流程后，基本上就可以明白要整合 Spring Web MVC 与 Spring Web Flow所需要的配置了。为了让客户端的请求变成执行某个 flow 的请求，要解决以下几个问题：

1. 需要在某个 HandlerMapping 中配置负责处理 flow 请求的 handler （或 controller ）--配置controller（flowController）
2. 该handler （或 controller ）要负责启动指定的 flow--该controller负责启动flow（flowExecutor）
3. flow 执行过程中以及执行完成后所涉及的视图应呈现给客户端--配置解析返回视图方式（viewResolvers）

所有这些配置的目的无非是两个：一是要让客户端的请求转变成flow 的执行，二是要让 flow 执行过程中、或执行结束后得到的视图能返还给客户端。

[源码](http://download.csdn.net/detail/hejingyuan6/8809997)
使用过springSecurity的朋友都知道，首先需要在web.xml进行以下配置
```
<filter>
  <filter-name>springSecurityFilterChain</filter-name>
  <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class> 
  <init-param>
        <param-name>targetFilterLifecycle</param-name>
        <param-value>true</param-value>  <!-- 默认是false -->
    </init-param>
 </filter>

<filter-mapping>
  <filter-name>springSecurityFilterChain</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```
非springSecurity用法如下：
```
    <filter>
        <filter-name>DelegatingFilterProxy</filter-name>
        <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
        <init-param>
            <param-name>targetBeanName</param-name>
            <!-- 自定义filter -->
            <param-value>exportExamineFilter</param-value>
        </init-param>
        <init-param>
        	<!-- 判断targetFilterLifecycle属性是false还是true,决定是否调用自定义类的init()、destry()方法 -->
            <param-name>targetFilterLifecycle</param-name>
            <param-value>false</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>DelegatingFilterProxy</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

```
从这个配置中，可能会给我们造成一个错觉，以为DelegatingFilterProxy类就是springSecurity的入口，但其实这个类位于spring-web-3.0.5.RELEASE.jar这个jar下面，说明这个类本身是和springSecurity无关。DelegatingFilterProxy类继承于抽象类GenericFilterBean,间接地implement 了javax.servlet.Filter接口，Servlet容器在启动时，首先会调用Filter的init方法,GenericFilterBean的作用主要是可以把Filter的初始化参数自动地set到继承于GenericFilterBean类的Filter中去。在其init方法的如下代码就是做了这个事：

```
PropertyValues pvs = new FilterConfigPropertyValues(filterConfig, this.requiredProperties);
BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
ResourceLoader resourceLoader = new ServletContextResourceLoader(filterConfig.getServletContext());
bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader));
initBeanWrapper(bw);
bw.setPropertyValues(pvs, true);
```
另外在init方法中调用了initFilterBean()方法，该方法是GenericFilterBean类是特地留给子类扩展用的
```
protected void initFilterBean() throws ServletException {
        // If no target bean name specified, use filter name.
        if (this.targetBeanName == null) {
            this.targetBeanName = getFilterName();
        }
 
        // Fetch Spring root application context and initialize the delegate early,
        // if possible. If the root application context will be started after this
        // filter proxy, we'll have to resort to lazy initialization.
        synchronized (this.delegateMonitor) {
            WebApplicationContext wac = findWebApplicationContext();
            if (wac != null) {
                this.delegate = initDelegate(wac);
            }
        }
    }
```
可以看出上述代码首先看Filter是否提供了targetBeanName初始化参数，如果没有提供则直接使用filter的name做为beanName,产生了beanName后，由于我们在web.xml的filter的name是springSecurityFilterChain,从spring的IOC容器中取出bean的代码是initDelegate方法，下面是该方法代码：
```
protected Filter initDelegate(WebApplicationContext wac) throws ServletException {
        Filter delegate = wac.getBean(getTargetBeanName(), Filter.class);
        if (isTargetFilterLifecycle()) {
            delegate.init(getFilterConfig());
        }
        return delegate;
}
```

通过跟踪代码，发现取出的bean是org.springframework.security.FilterChainProxy，该类也是继承于GenericFilterBean,取出bean后，判断targetFilterLifecycle属性是false还是true,决定是否调用该类的init方法。这个FilterChainProxy bean实例最终被保存在DelegatingFilterProxy类的delegate属性里,

下面看一下DelegatingFilterProxy类的doFilter方法
```
public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
 
        // Lazily initialize the delegate if necessary.
        Filter delegateToUse = null;
        synchronized (this.delegateMonitor) {
            if (this.delegate == null) {
                WebApplicationContext wac = findWebApplicationContext();
                if (wac == null) {
                    throw new IllegalStateException("No WebApplicationContext found: no ContextLoaderListener registered?");
                }
                this.delegate = initDelegate(wac);
            }
            delegateToUse = this.delegate;
        }
 
        // Let the delegate perform the actual doFilter operation.
        invokeDelegate(delegateToUse, request, response, filterChain);
    }
```

真正要关注invokeDelegate(delegateToUse, request, response, filterChain);这句代码,在下面可以看出DelegatingFilterProxy类实际是用其delegate属性即org.springframework.security.FilterChainProxy实例的doFilter方法来响应请求。
```
protected void invokeDelegate(
            Filter delegate, ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
 
        delegate.doFilter(request, response, filterChain);
    }
```
以上就是DelegatingFilterProxy类的一些内部运行机制，其实主要作用就是一个代理模式的应用,可以把servlet 容器中的filter同spring容器中的bean关联起来。

此外还要注意一个DelegatingFilterProxy的一个初始化参数：targetFilterLifecycle ,其默认值为false 。 但如果被其代理的filter的init()方法和destry()方法需要被调用时，需要设置targetFilterLifecycle为true。具体可见DelegatingFilterProxy中的如下代码：

```
protected void initFilterBean() throws ServletException {
        synchronized (this.delegateMonitor) {
            if (this.delegate == null) {
                // If no target bean name specified, use filter name.
                if (this.targetBeanName == null) {
                    this.targetBeanName = getFilterName();
                }
                // Fetch Spring root application context and initialize the delegate early,
                // if possible. If the root application context will be started after this
                // filter proxy, we'll have to resort to lazy initialization.
                WebApplicationContext wac = findWebApplicationContext();
                if (wac != null) {
                    this.delegate = initDelegate(wac);
                }
            }
        }
    }
 
 
protected Filter initDelegate(WebApplicationContext wac) throws ServletException {
        Filter delegate = wac.getBean(getTargetBeanName(), Filter.class);
        if (isTargetFilterLifecycle()) {    //注意这行
            delegate.init(getFilterConfig());
        }
        return delegate;
    }
```

---

转载地址：http://www.cnblogs.com/hzhuxin/archive/2011/12/19/2293730.html

转载有用的评论如下：
```
#4楼 2015-03-19 14:33 让自己行动起来  
我觉得Spring如此费功能的设计两个Filter,一个代理的Filter，还有一个真实的Security Filter主要是为了让Spring MVC可插拔性，也就是说你的security框架可以不用Spring的，可以用其它任何一家的security框架。
这也是为什么org.springframework.web.filter.DelegatingFilterProxy这个类是位于spring-web这个jar包下面。
我研究了一下，其过程是这样的。
web.xml下面咱们会配一个listen-class为org.springframework.web.context.ContextLoaderListener，然后其对应的contextConfigLocation Spring会把spring context及spring security的XML配置文件装载入Spring Bean容器中（由XmlWebApplicationContent来装载），然后在兄台上面说的
Filter delegate = wac.getBean(getTargetBeanName(), Filter.class);
这个方法的时候，Delegate就会向Spring容器要实现了Filter的叫targetBeanName的值（你上面配的是springSecurityFilterChain）的类，如果我们contextConfigLocation配置了Spring security的配置文件，自然就有这个springSecurityFilterChain的Bean了。如果我们不想用Spring Security，那么也可以在此处替换成我们要的security实现类。

不过我倒是没有搞明白为什么org.springframework.security.web.FilterChainProxy在Spring容器中的BeanName为什么是叫springSecurityFilterChain？
```
```
#5楼[楼主] 2015-07-10 19:10 杭州胡欣  
@ 让自己行动起来
这是springSecurity在加载自身的配置文件时指定的，只不过这个加载过程隐藏在命名空间之下了，也就是说BeanName的命名人家已经隐式地指定了，所以你看不到，人家就在文档上告诉你了。DelegatingFilterProxy类的真实作用就是为了让你在filter的配置上能引用spring的bean，这样达到可插拔的效果。
```
```
#9楼 2016-09-23 15:57 akka_li  
“DelegatingFilterProxy类的一些内部运行机制，其实主要作用就是一个代理模式的应用,可以把servlet 容器中的filter同spring容器中的bean关联起来”我觉得楼主说得这句是最重要的！

spring security为什么要用filter实现，而非aop、interceptor等等，？这些组件都是在dispatcherServlet之后执行的，此时再做一些安全校验是不是太晚（自己瞎猜的，主要是助于自己理解，在此做个笔记，有错希望指出！！！）

而确定了filter实现spring security，那么为什么又整出来一个DelegatingFilterProxy代理类啊？ 如果没有这个代理类，那么你就需要把spring security框架中的filter都配置到web.xml中，这样的用户体验过太差了，而且耦合得太紧密了；同时你通过web.xml配置这些filter，而没有通过spring ioc容器进行管理，有点不符合整体思想！而filter是属于java web的东西，必须配置在web.xml中，所以就有了目前的机制，通过配置一个DelegatingFilterProxy类到web.xml中，其他的spring security中的filter配置到ioc容器中管理，通过DelegatingFilterProxy代理类把javaweb中的filter和spring ioc容器中的filter关联起来了！（是自己目前的一种理解，如果有错，希望大家指出来，谢谢）
```
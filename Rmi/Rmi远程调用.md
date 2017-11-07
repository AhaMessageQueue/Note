通过使用Spring对RMI的支持，您可以通过RMI框架透明地导出您的服务。
完成这个设置后，除了没有对`security context propagation`或`remote transaction propagation`的标准支持外，基本上有一个类似于远程EJB的配置。
当使用`RMI invoker`时，Spring确实提供了额外的调用上下文的`hooks`，所以你可以在这里插入安全框架或者自定义安全凭证。

## Spring集成Rmi

### 使用RmiServiceExporter导出服务

使用`RmiServiceExporter`，我们可以将我们的`AccountService`对象的接口公开为RMI对象。
可以使用`RmiProxyFactoryBean`来访问接口。
`RmiServiceExporter`明确的支持通过`RMI invokers`导出任何非RMI服务。

当然，我们首先必须在Spring容器中设置我们的服务：

```xml
<bean id="accountService" class="example.AccountServiceImpl">
    <!-- any additional properties, maybe a DAO? -->
</bean>
```

接下来，我们将使用RmiServiceExporter导出我们的服务：

```xml
<bean class="org.springframework.remoting.rmi.RmiServiceExporter">
    <!-- does not necessarily have to be the same name as the bean to be exported -->
    <property name="serviceName" value="AccountService"/>
    <property name="service" ref="accountService"/>
    <property name="serviceInterface" value="example.AccountService"/>
    <!-- defaults to 1099 -->
    <property name="registryPort" value="1199"/>
</bean>
```


如您所见，我们覆盖了RMI注册表的端口。通常，您的应用程序服务器也维护一个RMI注册表，明智的做法是不要干预这个注册表。
此外，服务名称用于绑定下面的服务。所以现在，服务将被绑定在'rmi://HOST:1199/AccountService'。
稍后我们在客户端将使用该URL连接服务。

>注意servicePort属性已被省略（默认为0）。这意味着将使用匿名端口与服务进行通信。

### 连接服务

```java
public class SimpleObject {

    private AccountService accountService;

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }
}
```

为了在客户端连接服务，我们将创建一个单独的Spring容器，其中包含SimpleObject和服务连接配置：

```xml
<bean class="example.SimpleObject">
    <property name="accountService" ref="accountService"/>
</bean>

<bean id="accountService" class="org.springframework.remoting.rmi.RmiProxyFactoryBean">
    <property name="serviceUrl" value="rmi://HOST:1199/AccountService"/>
    <property name="serviceInterface" value="example.AccountService"/>
</bean>
```

这就是我们需要做的，以支持客户端上的远程帐户服务。

Spring将透明地创建一个`invoker`，并通过`RmiServiceExporter`启用远程账户服务。
在客户端，我们使用`RmiProxyFactoryBean`来连接它。

## 注意问题 

### RMI问题1

用 `./shutdown.sh` 关闭 rmi 服务器的 tomcat ，然后 `./startup.sh` 启动，客户端连接总是会导致如下错误：

```text
org.springframework.remoting.RemoteLookupFailureException: 
    Lookup of RMI stub failed; nested exception is java.rmi.UnmarshalException: error unmarshalling return; nested exception is:  
        java.io.EOFException
```

分析：

问题的原因是Spring使用服务器的类加载器创建了一个RMIRegistry。
然后，在重新启动服务器时，RMIRegistry没有关闭。
重新启动后，注册表的引用依然指向不存在的旧Stubs中。

有两个解决方案： 
1. 不在服务器应用程序的类路径下，在单独的进程中启动rmiregistry。 
2. （更好的方法）让Spring通过`RmiRegistryFactoryBean`启动`RMIRegistry`，它将正确关闭它。

解决：

将服务器中的 spring 配置代码，如下：

```xml
<bean class="org.springframework.remoting.rmi.RmiServiceExporter">
    <!-- does not necessarily have to be the same name as the bean to be exported -->
    <property name="serviceName" value="AccountService"/>
    <property name="service" ref="accountService"/>
    <property name="serviceInterface" value="example.AccountService"/>
    <!-- defaults to 1099 -->
    <property name="registryPort" value="1199"/>
</bean>
```

替换为：

```xml
<bean id="registry" class="org.springframework.remoting.rmi.RmiRegistryFactoryBean">
    <property name="port" value="1199"/>
</bean>

<bean class="org.springframework.remoting.rmi.RmiServiceExporter">
    <!-- does not necessarily have to be the same name as the bean to be exported -->
    <property name="serviceName" value="AccountService"/>
    <property name="service" ref="accountService"/>
    <property name="serviceInterface" value="example.AccountService"/>
    <!-- defaults to 1099 -->
    <property name="registry" ref="registry"/>
</bean>
```

### RMI问题2

RMI 服务器重启，总是会出现客户端连接拒绝的问题。

分析：

服务器重启会影响到客户端,说明客户端有保存着重启之前的服务器连接相关记录。经研究发现，客户端有缓存，所以只要刷新缓存即可解决问题。

解决：

在客户端连接代码中增加代码：

```java
RmiProxyFactoryBean factory= new RmiProxyFactoryBean();
factory.setServiceInterface(AccountService.class);
factory.setServiceUrl(url);

factory.setLookupStubOnStartup(false);
factory.setRefreshStubOnConnectFailure(true);

factory.afterPropertiesSet();
AccountService service = (AccountService)factory.getObject();
```

- lookupStubOnStartup：预查找远程对象 默认为true
- refreshStubOnConnectFailure：是否刷新远程调用缓存的stub

注意：如果上述两项不配置，当服务器未开启，客户端无法打包，会有拒绝连接异常。

### RMI问题3

Spring RMI会占用两个端口？

分析：

Spring RMI 有两个端口，一个是注册端口（默认为1099），还有一个是数据传输端口，如果不指定，数据传输端口是随机分配的。

解决：

在xml配置时，配置servicePort

```xml
<bean id="registry" class="org.springframework.remoting.rmi.RmiRegistryFactoryBean">
    <property name="port" value="1199"/>
</bean>

<bean class="org.springframework.remoting.rmi.RmiServiceExporter">
    <!-- does not necessarily have to be the same name as the bean to be exported -->
    <property name="serviceName" value="AccountService"/>
    <property name="service" ref="accountService"/>
    <property name="serviceInterface" value="example.AccountService"/>
    <!-- defaults to 1099 -->
    <property name="registry" ref="registry"/>
    <property name="servicePort" value="1299" />
</bean>
```
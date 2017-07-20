在基于注解方式配置Spring的配置文件中，你可能会见到`<context:annotation-config/>`这样一条配置，他的作用是显式地向 Spring 容器注册

`AutowiredAnnotationBeanPostProcessor`、`CommonAnnotationBeanPostProcessor`、

`PersistenceAnnotationBeanPostProcessor` 以及 `RequiredAnnotationBeanPostProcessor` 这 4 个BeanPostProcessor。

注册这4个 BeanPostProcessor的作用，就是为了你的系统能够识别相应的注解。

例如：

如果你想使用`@Autowired`注解，那么就必须事先在 Spring 容器中声明 `AutowiredAnnotationBeanPostProcessor` Bean。传统声明方式如下：

```xml
<bean class="org.springframework.beans.factory.annotation. AutowiredAnnotationBeanPostProcessor "/> 
```

如果想使用`@Resource` 、`@PostConstruct`、`@PreDestroy`等注解就必须声明`CommonAnnotationBeanPostProcessor`

如果想使用`@PersistenceContext`注解，就必须声明`PersistenceAnnotationBeanPostProcessor`的Bean。

如果想使用 `@Required` 的注解，就必须声明`RequiredAnnotationBeanPostProcessor`的Bean。

同样，传统的声明方式如下：

```xml
<bean class="org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor"/> 
```

一般来说，这些注解我们还是比较常用，尤其是`@Antowired`的注解，在自动注入的时候更是经常使用。
所以如果总是需要按照传统的方式一条一条配置显得有些繁琐和没有必要，于是spring给我们提供`<context:annotation-config/>`的简化配置方式，自动帮你完成声明。

不过，呵呵，我们使用注解一般都会配置扫描包路径选项

```xml
<context:component-scan base-package=”XX.XX”/> 
```

该配置项其实也包含了自动注入上述processor的功能，因此当使用 `<context:component-scan/>` 后，就可以将 `<context:annotation-config/>` 移除了。









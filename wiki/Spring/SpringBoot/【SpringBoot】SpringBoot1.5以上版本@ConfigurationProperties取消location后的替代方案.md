## 问题
在`spring boot（版本1.5.1.RELEASE）`项目中，当准备**映射自定义的配置文件属性到类**中的时候，
发现原本的`@ConfigurationProperties`注解已将`location`属性移除，因此导致无法正常给配置类的属性赋值
(spring boot这么做其实也有他的道理，具体可参考<https://github.com/spring-projects/spring-boot/issues/6726>)

![](./images/@ConfigurationProperties_location.png)

## 解决方案
之前一直采用的方式是添加`@ConfigurationProperties`注解，配置其`prefix`和`location`属性，然后在`spring boot`启动类中用`@EnableConfigurationProperties`激活配置类

既然不行了，那我们只能换一种方式：

1. 在`@EnableConfigurationProperties`取消激活自定义的配置类（重要）
2. 在配置类中采用`@Component`的方式注册为组件，然后使用`@PropertySource`来指定自定义的资源目录

![](images/@ConfigurationProperties_location2.png)

## 总结
这就是`@ConfigurationProperties`的`location`属性被取消后的一种替代方案,

当然，如果想改代码，也有其他的解决方案，可以参考
<http://stackoverflow.com/questions/42083276/spring-boot-remove-locations-attributes-from-configurationproperties>

当然，spring boot认为将一个配置类绑定到一个配置文件是一件不好的事，因此，我们也应当尽可能的去理解他的思想，然后找到一个最有效的方式来解决这个问题。
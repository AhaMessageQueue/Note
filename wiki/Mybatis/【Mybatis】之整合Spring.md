## Mybatis整合Spring
根据官方的说法，在ibatis3，也就是Mybatis3问世之前，Spring3的开发工作就已经完成了，所以Spring3中还是没有对Mybatis3的支持。因此由Mybatis社区自己开发了一个Mybatis-Spring用来满足Mybatis用户整合Spring的需求。下面就将通过Mybatis-Spring来整合Mybatis跟Spring的用法做一个简单的介绍。

### MapperFactoryBean
首先，我们需要从Mybatis官网上下载Mybatis-Spring的Jar包添加到我们项目的类路径下，当然也需要添加Mybatis的相关Jar包和Spring的相关Jar包。

我们知道在Mybatis的所有操作都是基于一个`SqlSession`的，而SqlSession是由SqlSessionFactory来产生的，SqlSessionFactory又是由SqlSessionFactoryBuilder来生成的。
但是Mybatis-Spring是基于`SqlSessionFactoryBean`的。在使用Mybatis-Spring的时候，我们也需要SqlSession，而且这个SqlSession是内嵌在程序中的，一般不需要我们直接访问。

`SqlSession`是由`SqlSessionFactory`来产生的，但是**Mybatis-Spring给我们封装了一个`SqlSessionFactoryBean`，在这个bean里面还是通过SqlSessionFactoryBuilder来建立对应的SqlSessionFactory，进而获取到对应的SqlSession**。

通过SqlSessionFactoryBean我们可以通过对其指定一些属性来提供Mybatis的一些配置信息。所以接下来我们需要在Spring的applicationContext配置文件中定义一个SqlSessionFactoryBean。

```xml
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">  
       <property name="dataSource" ref="dataSource" />  
       <property name="mapperLocations"  
              value="classpath:com/tiantian/ckeditor/mybatis/mappers/*Mapper.xml" />  
       <property name="typeAliasesPackage" value="com.tiantian.ckeditor.model" />  
</bean>
```

在定义SqlSessionFactoryBean的时候，dataSource属性是必须指定的，它表示用于连接数据库的数据源。当然，我们也可以指定一些其他的属性，下面简单列举几个：
- mapperLocations：它表示我们的Mapper文件存放的位置，当我们的Mapper文件跟对应的Mapper接口处于同一位置的时候可以不用指定该属性的值。
- configLocation：用于指定Mybatis的配置文件位置。如果指定了该属性，那么会以该配置文件的内容作为配置信息构建对应的SqlSessionFactoryBuilder，但是后续属性指定的内容会覆盖该配置文件里面指定的对应内容。
- typeAliasesPackage：它一般对应我们的实体类所在的包，这个时候会自动取对应包中不包括包名的简单类名作为包括包名的别名。多个package之间可以用逗号或者分号等来进行分隔。
- typeAliases：数组类型，用来指定别名的。指定了这个属性后，Mybatis会把这个类型的短名称作为这个类型的别名，前提是该类上没有标注@Alias注解，否则将使用该注解对应的值作为此种类型的别名。

```
<property name="typeAliases">  
    <array>  
        <value>com.tiantian.mybatis.model.Blog</value>  
        <value>com.tiantian.mybatis.model.Comment</value>  
    </array>  
</property>  
```

- plugins：数组类型，用来指定Mybatis的Interceptor。
- typeHandlersPackage：用来指定TypeHandler所在的包，如果指定了该属性，SqlSessionFactoryBean会自动把该包下面的类注册为对应的TypeHandler。多个package之间可以用逗号或者分号等来进行分隔。
- typeHandlers：数组类型，表示TypeHandler。

接下来就是在Spring的applicationContext文件中定义我们想要的Mapper对象对应的`MapperFactoryBean`了。通过MapperFactoryBean可以获取到我们想要的Mapper对象。

**MapperFactoryBean实现了Spring的FactoryBean接口**，所以**MapperFactoryBean是通过FactoryBean接口中定义的getObject方法来获取对应的Mapper对象的**。

在定义一个MapperFactoryBean的时候有两个属性需要我们注入，一个是Mybatis-Spring用来生成实现了SqlSession接口的SqlSessionTemplate对象的sqlSessionFactory；另一个就是我们所要返回的对应的Mapper接口了。

定义好相应Mapper接口对应的MapperFactoryBean之后，我们就可以把我们对应的Mapper接口注入到由Spring管理的bean对象中了，比如Service bean对象。

这样当我们需要使用到相应的Mapper接口时，**MapperFactoryBean会从它的getObject方法中获取对应的Mapper接口**，**而getObject内部还是通过我们注入的属性调用SqlSession接口的getMapper(Mapper接口)方法来返回对应的Mapper接口的**。
这样就通过把SqlSessionFactory和相应的Mapper接口交给Spring管理实现了Mybatis跟Spring的整合。

**Spring的applicationContext.xml配置文件：**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:context="http://www.springframework.org/schema/context"
    xmlns:mvc="http://www.springframework.org/schema/mvc"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
     http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
     http://www.springframework.org/schema/context
   http://www.springframework.org/schema/context/spring-context-3.0.xsd
     http://www.springframework.org/schema/mvc
     http://www.springframework.org/schema/mvc/spring-mvc-3.0.xsd">
     
     <context:component-scan base-package="com.tiantian.mybatis"/>
     
     <context:property-placeholder location="classpath:config/jdbc.properties"/>
     <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource"
        destroy-method="close">
        <property name="driverClassName" value="${jdbc.driver}" />
        <property name="url" value="${jdbc.url}" />
        <property name="username" value="${jdbc.username}" />
        <property name="password" value="${jdbc.password}" />
     </bean>
         
     <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource" />
        <property name="mapperLocations" value="classpath:com/tiantian/mybatis/mapper/*.xml"/>
        <property name="typeAliasesPackage" value="com.tiantian.mybatis.model" />
     </bean>
     
     <bean id="blogMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">
        <property name="mapperInterface"
            value="com.tiantian.mybatis.mapper.BlogMapper" />
        <property name="sqlSessionFactory" ref="sqlSessionFactory" />
     </bean>
     
</beans>
```

### MapperScannerConfigurer
利用上面的方法进行整合的时候，我们有一个Mapper就需要定义一个对应的MapperFactoryBean，当我们的Mapper比较少的时候，这样做也还可以，但是当我们的Mapper相当多时我们再这样定义一个个Mapper对应的MapperFactoryBean就显得速度比较慢了。为此Mybatis-Spring为我们提供了一个叫做MapperScannerConfigurer的类，通过这个类Mybatis-Spring会自动为我们注册Mapper对应的MapperFactoryBean对象。

如果我们需要使用MapperScannerConfigurer来帮我们自动扫描和注册Mapper接口的话我们需要在Spring的applicationContext配置文件中定义一个MapperScannerConfigurer对应的bean。对于MapperScannerConfigurer而言有一个属性是我们必须指定的，那就是basePackage。basePackage是用来指定Mapper接口文件所在的基包的，在这个基包或其所有子包下面的Mapper接口都将被搜索到。多个基包之间可以使用逗号或者分号进行分隔。最简单的MapperScannerConfigurer定义就是只指定一个basePackage属性，如：
       
```xml
<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">  
   <property name="basePackage" value="com.tiantian.mybatis.mapper" />  
</bean>  
```
这样MapperScannerConfigurer就会扫描指定基包下面的所有接口，并把它们注册为一个个MapperFactoryBean对象。当使用MapperScannerConfigurer加basePackage属性的时候，我们上面例子的applicationContext配置文件将变为这样：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:context="http://www.springframework.org/schema/context"
    xmlns:mvc="http://www.springframework.org/schema/mvc"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
     http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
     http://www.springframework.org/schema/context
     http://www.springframework.org/schema/context/spring-context-3.0.xsd
     http://www.springframework.org/schema/mvc
     http://www.springframework.org/schema/mvc/spring-mvc-3.0.xsd">
 
    <context:component-scan base-package="com.tiantian.mybatis" />
    <context:property-placeholder location="classpath:config/jdbc.properties" />
    <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource"
       destroy-method="close">
       <property name="driverClassName" value="${jdbc.driver}" />
       <property name="url" value="${jdbc.url}" />
       <property name="username" value="${jdbc.username}" />
       <property name="password" value="${jdbc.password}" />
    </bean>
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
       <property name="dataSource" ref="dataSource" />
       <property name="mapperLocations" value="classpath:com/tiantian/mybatis/mapper/*.xml" />
       <property name="typeAliasesPackage" value="com.tiantian.mybatis.model" />
    </bean>
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
       <property name="basePackage" value="com.tiantian.mybatis.mapper" />
    </bean>
</beans>
```

有时候我们指定的基包下面的并不全是我们定义的Mapper接口，为此MapperScannerConfigurer还为我们提供了另外两个可以缩小搜索和注册范围的属性。一个是annotationClass，另一个是markerInterface。

- annotationClass：当指定了annotationClass的时候，MapperScannerConfigurer将只注册使用了annotationClass注解标记的接口。
- markerInterface：markerInterface是用于指定一个接口的，当指定了markerInterface之后，MapperScannerConfigurer将只注册继承自markerInterface的接口。

**如果上述两个属性都指定了的话，那么MapperScannerConfigurer将取它们的并集，而不是交集。即使用了annotationClass进行标记或者继承自markerInterface的接口都将被注册为一个MapperFactoryBean**。

现在假设我们的Mapper接口都继承了一个SuperMapper接口，那么我们就可以这样来定义我们的MapperScannerConfigurer。
```xml
<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">  
   <property name="basePackage" value="com.tiantian.mybatis.mapper" />  
   <property name="markerInterface" value="com.tiantian.mybatis.mapper.SuperMapper"/>  
</bean> 
```

如果是都使用了注解MybatisMapper标记的话，那么我们就可以这样来定义我们的MapperScannerConfigurer。
```xml
<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">  
   <property name="basePackage" value="com.tiantian.mybatis.mapper" />  
   <property name="annotationClass" value="com.tiantian.mybatis.annotation.MybatisMapper"/>  
</bean>
```
除了用于缩小注册Mapper接口范围的属性之外，我们还可以指定一些其他属性，如：

- `sqlSessionFactory`：**这个属性已经废弃**。
    
    当我们**使用了多个数据源**的时候我们就需要通过`sqlSessionFactory`来指定在注册MapperFactoryBean的时候需要使用的SqlSessionFactory，
    因为**在没有指定sqlSessionFactory的时候，会以Autowired的方式自动注入一个**。
    换言之，当我们只使用一个数据源的时候，即只定义了一个SqlSessionFactory的时候我们就可以不给MapperScannerConfigurer指定SqlSessionFactory。

- sqlSessionFactoryBeanName：它的功能跟sqlSessionFactory是一样的，只是它指定的是定义好的SqlSessionFactory对应的bean名称。
- sqlSessionTemplate：**这个属性已经废弃**。

    它的功能也是相当于sqlSessionFactory的，因为就像前面说的那样，**MapperFactoryBean最终还是使用的SqlSession的getMapper方法取的对应的Mapper对象**。
    **当定义有多个SqlSessionTemplate的时候才需要指定它**。
    对于一个MapperFactoryBean来说SqlSessionFactory和SqlSessionTemplate只需要其中一个就可以了，当两者都指定了的时候，SqlSessionFactory会被忽略。

- sqlSessionTemplateBeanName：指定需要使用的sqlSessionTemplate对应的bean名称。

**注意：由于使用sqlSessionFactory和sqlSessionTemplate属性时会使一些内容在PropertyPlaceholderConfigurer之前加载，导致在配置文件中使用到的外部属性信息无法被及时替换而出错，因此官方现在新的Mybatis-Spring中已经把sqlSessionFactory和sqlSessionTemplate属性废弃了，推荐大家使用sqlSessionFactoryBeanName属性和sqlSessionTemplateBeanName属性。**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:context="http://www.springframework.org/schema/context"
    xmlns:mvc="http://www.springframework.org/schema/mvc"
    xmlns:mybatis="http://www.mybatis.org/schema/mybatis"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
     http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
     http://www.springframework.org/schema/context
     http://www.springframework.org/schema/context/spring-context-3.0.xsd
     http://www.mybatis.org/schema/mybatis
     http://www.mybatis.org/schema/mybatis/mybatis-spring.xsd
     http://www.springframework.org/schema/mvc
     http://www.springframework.org/schema/mvc/spring-mvc-3.0.xsd">
 
    <context:component-scan base-package="com.tiantian.mybatis" />
    <context:property-placeholder location="classpath:config/jdbc.properties" />
    <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource"
       destroy-method="close">
       <property name="driverClassName" value="${jdbc.driver}" />
       <property name="url" value="${jdbc.url}" />
       <property name="username" value="${jdbc.username}" />
       <property name="password" value="${jdbc.password}" />
    </bean>
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
       <property name="dataSource" ref="dataSource" />
       <property name="mapperLocations" value="classpath:com/tiantian/mybatis/mapper/*.xml" />
       <property name="typeAliasesPackage" value="com.tiantian.mybatis.model" />
    </bean>
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
       <property name="basePackage" value="com.tiantian.mybatis.mapper" />
       <property name="markerInterface" value="com.tiantian.mybatis.mapper.SuperMapper"/>
       <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
    </bean>
</beans>
```

### SqlSessionTemplate
除了上述整合之后直接使用Mapper接口之外，Mybatis-Spring还为我们提供了一种直接使用SqlSession的方式。Mybatis-Spring为我们提供了一个实现了SqlSession接口的SqlSessionTemplate类，它是线程安全的，可以被多个Dao同时使用。同时它还跟Spring的事务进行了关联，确保当前被使用的SqlSession是一个已经和Spring的事务进行绑定了的。而且它还可以自己管理Session的提交和关闭。当使用了Spring的事务管理机制后，SqlSession还可以跟着Spring的事务一起提交和回滚。
使用SqlSessionTemplate时我们可以在Spring的applicationContext配置文件中如下定义：
```xml
<bean id="sqlSession" class="org.mybatis.spring.SqlSessionTemplate">
    <constructor-arg index="0" ref="sqlSessionFactory" />
</bean>
```
这样我们就可以通过Spring的依赖注入在Dao中直接使用SqlSessionTemplate来编程了，这个时候我们的Dao可能是这个样子：

```java
package com.tiantian.mybatis.dao;
 
import java.util.List;
import javax.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;
import com.tiantian.mybatis.model.Blog;
 
@Repository
publicclass BlogDaoImpl implements BlogDao {
 
    private SqlSessionTemplate sqlSessionTemplate;
 
    publicvoid deleteBlog(int id) {
       sqlSessionTemplate.delete("com.tiantian.mybatis.mapper.BlogMapper.deleteBlog", id);
    }
 
    public Blog find(int id) {
      returnsqlSessionTemplate.selectOne("com.tiantian.mybatis.mapper.BlogMapper.selectBlog", id);
    }
 
    public List<Blog> find() {
       returnthis.sqlSessionTemplate.selectList("com.tiantian.mybatis.mapper.BlogMapper.selectAll");
    }
 
    publicvoid insertBlog(Blog blog) {
       this.sqlSessionTemplate.insert("com.tiantian.mybatis.mapper.BlogMapper.insertBlog", blog);
    }
 
    publicvoid updateBlog(Blog blog) {
       this.sqlSessionTemplate.update("com.tiantian.mybatis.mapper.BlogMapper.updateBlog", blog);
    }
   
    public SqlSessionTemplate getSqlSessionTemplate() {
       returnsqlSessionTemplate;
    }
   
    @Resource
    publicvoid setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
       this.sqlSessionTemplate = sqlSessionTemplate;
    }
 
}
```

注：**本文是基于Mybatis3.2.1、Mybatis-Spring1.1.0和Spring3.1写的**。
JDO(Java Data Object )是Java对象持久化的新的规范，也是一个用于存取某种数据仓库中的对象的标准化API。注意JDO是一种规范，而不是一个产品。而DataNucleus正是实现JDO规范的产品之一，JDO实现产品还有FOStore、JPOX等。当然DataNucleus不仅实现了JDO规范，还实现了JPA(java另一持久化规范)，也就是说有了DataNucleus，你既可以选择JDO API，也可以选择JPA API进行持久化操作。

现在DataNucleus官网上主要推的是3.3与4.0版本，3.3版本需要JDK1.6以上，而4.0版本需要JDK1.7以后.

这里选择使用4.X版本

## 基本配置
要使用DataNucleus，必须要添加其jar包，因为其jar包比较多还涉及jar包依赖关系，所以最好是使用maven进行构建。下面是项目依赖：
```
<dependencies>
    <!-- jdbc driver -->
    <dependency>
        <groupId>${jdbc.driver.groupId}</groupId>
        <artifactId>${jdbc.driver.artifactId}</artifactId>
    </dependency>
    <!-- JDO API -->
    <dependency>
        <groupId>javax.jdo</groupId>
        <artifactId>jdo-api</artifactId>
        <version>3.0.1</version>
    </dependency>
    <!-- 下面为DataNucleus所需jar包 -->
    <dependency>
        <groupId>org.datanucleus</groupId>
        <artifactId>datanucleus-core</artifactId>
        <version>4.0.4</version>
    </dependency>
    <dependency>
        <groupId>org.datanucleus</groupId>
        <artifactId>datanucleus-api-jdo</artifactId>
        <version>4.0.5</version>
    </dependency>
    <dependency>
        <groupId>org.datanucleus</groupId>
        <artifactId>datanucleus-rdbms</artifactId>
        <version>4.0.7</version>
    </dependency>
</dependencies>
```
有了jar包后，就应该写配置文件了。在类路径下新建一个 META-INF/persistence.xml文件，文件名称必须为persistence.xml且必须放在META-INF目录中，这是JDO规范中规定死的。下面是该配置文件内容：
```
<?xml version="1.0" encoding="UTF-8" ?>
<persistence xmlns="http://java.sun.com/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/persistence
        http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd" version="2.0">
    <!-- 一个配置文件中可以有多个持久化单元(persistence-unit)  RESOURCE_LOCAL表示本地事务 -->
    <persistence-unit name="core.datanucleus" transaction-type="RESOURCE_LOCAL">
        <!-- 包含一个Bean实体 -->
        <class>com.github.spring.base4j.core.jdo.pojo.PersonInfo</class>
        <exclude-unlisted-classes/>
        <properties>
            <property name="datanucleus.ConnectionDriverName" value="com.mysql.jdbc.Driver"/>
            <property name="datanucleus.ConnectionURL" value="jdbc:mysql://127.0.0.1:3306/calendar?characterEncoding=utf-8"/>
            <property name="datanucleus.ConnectionUserName" value="root"/>
            <property name="datanucleus.ConnectionPassword" value="root"/>
            <!-- just for 4.X+ -->
            <property name="datanucleus.schema.autoCreateAll" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```
properties中主要设置的是数据库连接信息，该连接信息还有另一种写法：
```
<property name="javax.jdo.option.ConnectionDriverName" value=""/>  
<property name="javax.jdo.option.ConnectionURL" value=""/>  
<property name="javax.jdo.option.ConnectionUserName" value=""/>  
<property name="javax.jdo.option.ConnectionPassword" value=""/>  
```
javax.jdo.option.ConnectionDriverName是datanucleus.ConnectionDriverName的别名，其它依此类推，
`datanucleus.autoCreateSchema=true(3.X)`/`datanucleus.schema.autoCreateAll=true(4.X)`表示在操作JDO API的时候对应的数据库表还没有创建的话会根据实体的元数据自动创建表。
当然可以设置的属性还有很多，具体请参看：http://www.datanucleus.org/products/accessplatform_4_0/persistence_properties.html

下面是PersonInfo类代码：
```
package com.github.spring.base4j.core.jdo.pojo;

import javax.jdo.annotations.*;

/**
 * Created by 刘春龙 on 2017/2/22.
 */
@PersistenceCapable(table="JDO_PERSON")
public class PersonInfo {

    //    @Column(name="P_ID")
    @PrimaryKey
    @Persistent(valueStrategy= IdGeneratorStrategy.IDENTITY)//ID自增长
    private Integer id;

//    @Column(name="P_NAME")
    @Persistent
    private String name;

//    @Column(name="P_AGE")
    @Persistent
    private int age;

//    @Column(name="P_EMAIL")
    @Persistent
    private String email;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    @Override
    public String toString() {
        return "PersonInfo [id=" + id + ", name=" + name + ", age=" + age
                + ", email=" + email + "]";
    }
}
```
配置元数据即可以使用XML配置文件，也可以使用注解，但个人习惯使用注解。要使一个类能够被JDO操作，必须在类上加上@PersistenceCapable注解，其table属性用于设置该实体对应的数据库表名称。@PrimaryKey用于表示这是主键，@Column(name="P_AGE")用于指定表字段名称，@Column不是必须的，这些注解即可放置在字段上，也可以放置在相应的getter方法上.JDO提供的注解有很多，有些注解还有很多属性，具体请参看：http://db.apache.org/jdo/annotations.html。

## Junit测试
测试代码如下：
```
package com.github.spring.base4j.core.jdo;

import com.github.spring.base4j.core.jdo.pojo.PersonInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

/**
 * Created by 刘春龙 on 2017/2/22.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/spring-basic.xml", "file:src/main/webapp/WEB-INF/spring-servlet.xml"})
public class JDOTest {

    private static PersistenceManagerFactory factory;

    @Before
    public void before() {

        factory = JDOHelper.getPersistenceManagerFactory("core.datanucleus");
    }

    @Test
    public void testSave() {
        PersistenceManager pm = factory.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            PersonInfo person = new PersonInfo();
            person.setId(1);
            person.setName("王五");
            person.setAge(32);
            person.setEmail("wangwu@qq.com");
            PersonInfo p = pm.makePersistent(person);
            tx.commit();
        } catch (Exception e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            e.printStackTrace();
        } finally {
            pm.close();
        }
    }
}

```

## Enhancer增强
任何persistence-capable类的实例在被JDO持久化引擎管理之前必须被加强。

可以通过插件来对PersonInfo类进行增强。该插件会在编译阶段自动执行。

```
<build>
        <plugins>
            <plugin>
                <groupId>org.datanucleus</groupId>
                <artifactId>datanucleus-maven-plugin</artifactId>
                <version>4.0.0-release</version>
                <configuration>
                    <api>JDO</api>
                    <!--<props>${basedir}/datanucleus.properties</props>-->
                    <!--<log4jConfiguration>${basedir}/log4j.properties</log4jConfiguration>-->
                    <verbose>true</verbose>
                </configuration>
                <executions>
                    <execution>
                        <phase>process-classes</phase>
                        <goals>
                            <goal>enhance</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

## 补充

1. 资源回收 pm.close()
我们采用传统SQL写代码时，最危险的就是资源释放问题，这在基于WEB的应用中尤其重要。因为与JDBC相关的资源不是在Java虚拟机中分配的，而是在系统底层分配的，Java的垃圾回收机制鞭长莫及，导致系统内存慢慢耗光而死机。

在JDBC中需要主动释放的资源有：Connection、Statement、PreparedStatement、ResultSet，在每个对这些类型的变量赋值的时候，都必须将先前的资源释放掉。无疑是一件繁琐而又容易被忽略的事情。 在JDO 中，事情变得简单多了，所有的资源在pm.close()的时候会自动释放（除非JDO产品增加了一些对PreparedStatement和 ResultSet的Cache），这是JDO规范的要求。因此，只要我们记住在对实体类处理完毕时调用pm.close()就行了。比如下面的代码：

```
PersistenceManager pm = null
try {
    pm = getPersistenceManagerFactory().getPersistenceManager();
    //做一些数据类的处理工作

} finally{
    pm.close();
}
```
有些人可能就是不喜欢调用它，觉得烦，因为每次要用时都要打开一个PM，而用完时都要关闭，如果JDO产品没有PM连接池的话，性能可能受到影响。这样，我们可以利用下面的继承java.lang.ThreadLocal的工具类完成这一点：

```
package com.github.spring.base4j.core.jdo.support;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import java.util.logging.Logger;

/**
 * Created by 刘春龙 on 2017/2/22.
 */
public class PersistenceManagerRetriever extends ThreadLocal {

    public static final Logger logger = Logger.getLogger(PersistenceManagerRetriever.class.getName());

    private static final PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("core.datanucleus");

    /**
     * 获取相关的PersistenceManagerFactory
     *
     * @return PersistenceManagerFactory实例
     */
    public PersistenceManagerFactory pmf() {
        return pmf;
    }

    /**
     * 获取一个与当前线程相关的PersistenceManager
     *
     * @return PersistenceManager实例
     */
    public PersistenceManager pm() {
        return (PersistenceManager) get();
    }

    public Object get() {

        PersistenceManager pm = (PersistenceManager) super.get();

        if (pm == null || pm.isClosed()) {
            pm = pmf.getPersistenceManager();
            set(pm);
            logger.info("retrieved new PM: " + pm);
        }
        return pm;
    }

    /**
     * 释放所有与本线程相关的JDO资源
     */
    public void cleanup() {
        PersistenceManager pm = pm();

        if (pm == null) return;

        try {
            if (!pm.isClosed()) {
                Transaction ts = pm.currentTransaction();
                if (ts != null && ts.isActive()) {
                    logger.info("发现一个未完成的Transaction [" + pmf.getConnectionURL() + "]！" + ts);
                    ts.rollback();
                }
                pm.close();
            }
        } catch (Exception e) {
            logger.info("释放JDO资源时出错：" + e);
        } finally {
            set(null);
        }
    }
}

```

这样，只要在一个线程中（比如一次页面请求），在所有的需要PM的地方，都只需直接调用
```
persistenceManagerRetriever.pm();
```
即可，并且，只在最后用完后才调用一次persistenceManagerRetriever.cleanup()以关闭它。

这个persistenceManagerRetriever可以在某个系统类的初始化代码中加入：
```
PersistenceManagerRetriever persistenceManagerRetriever = new PersistenceManagerRetriever(properties);
```
而关闭当前线程相关的PM的语句（persistenceManagerRetriever.cleanup()）可以配置一个JspFilter来完成它，比如：
```
public static class JspFilter implements javax.servlet.Filter {
        public void doFilter(
            javax.servlet.ServletRequest request,
            javax.servlet.ServletResponse response, 
            javax.servlet.FilterChain chain)
        throws javax.servlet.ServletException,java.io.IOException {
            try {
                chain.doFilter(request,response);
            } finally {
                if(pmRetriever != null) pmRetriever.cleanup();
            }
        }
        public void init(javax.servlet.FilterConfig filterConfig) throws javax.servlet.ServletException {}
        public javax.servlet.FilterConfig getFilterConfig() { return null; }
        public void setFilterConfig(javax.servlet.FilterConfig fc) {}
        public void destroy() {}
    }
```
然后我们将其配置在WebApp的描述符中：
```
<filter>
        <filter-name>jdo_JspFilter</filter-name>
        <filter-class>xxx.jdo_util.JspFilter</filter-class>
</filter>

<filter-mapping>
        <filter-name>jdo_JspFilter</filter-name>
        <url-pattern>*.jsp</url-pattern>
</filter-mapping>
```
这样，我们在JSP中的代码更简单：
```
persistenceManagerRetriever.pm().currentTransaction().begin();
```
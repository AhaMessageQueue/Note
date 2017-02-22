JDO(Java Data Object )是Java对象持久化的新的规范，也是一个用于存取某种数据仓库中的对象的标准化API。注意JDO是一种规范，而不是一个产品。而DataNucleus正是实现JDO规范的产品之一，JDO实现产品还有FOStore、JPOX等。当然DataNucleus不仅实现了JDO规范，还实现了JPA(java另一持久化规范)，也就是说有了DataNucleus，你既可以选择JDO API，也可以选择JPA API进行持久化操作。

现在DataNucleus官网上主要推的是3.3与4.0版本，3.3版本需要JDK1.6以上，而4.0版本需要JDK1.7以后，由于本人平常习惯于JDK1.6，所以在这里选择的是DataNucleus3.0版本。

要使用DataNucleus，必须要添加其jar包，因为其jar包比较多还涉及jar包依赖关系，所以最好是使用maven进行构建。下面是项目依赖：

```
<!-- JDO API -->  
<dependency>  
    <groupId>javax.jdo</groupId>  
    <artifactId>jdo-api</artifactId>  
    <version>3.1-rc1</version>  
</dependency>  
<!-- mysql数据库驱动 -->  
<dependency>  
    <groupId>mysql</groupId>  
    <artifactId>mysql-connector-java</artifactId>  
    <version>5.1.18</version>  
</dependency>  
<!-- 下面为DataNucleus所需jar包 -->  
<dependency>  
    <groupId>org.datanucleus</groupId>  
    <artifactId>datanucleus-core</artifactId>  
    <version>3.0.0-m4</version>  
</dependency>  
<dependency>  
    <groupId>org.datanucleus</groupId>  
    <artifactId>datanucleus-enhancer</artifactId>  
    <version>3.0.0-m4</version>  
</dependency>  
<dependency>  
    <groupId>org.datanucleus</groupId>  
    <artifactId>datanucleus-api-jdo</artifactId>  
    <version>3.0.0-m4</version>  
</dependency>  
<dependency>  
    <groupId>org.datanucleus</groupId>  
    <artifactId>datanucleus-jdo-query</artifactId>  
    <version>3.0.0-m2</version>  
</dependency>  
<dependency>  
    <groupId>org.datanucleus</groupId>  
    <artifactId>datanucleus-cache</artifactId>  
    <version>3.0.0-m2</version>  
</dependency>  
<dependency>  
    <groupId>org.datanucleus</groupId>  
    <artifactId>datanucleus-rdbms</artifactId>  
    <version>3.0.0-m4</version>  
</dependency>  
<dependency>  
    <groupId>org.datanucleus</groupId>  
    <artifactId>datanucleus-management</artifactId>  
    <version>1.0.2</version>  
</dependency>
```
有了jar包后，就应该写配置文件了。在类路径下新建一个 META-INF/persistence.xml文件，文件名称必须为persistence.xml且必须放在META-INF目录中，这是JDO规范中规定死的。下面是该配置文件内容：
```
<?xml version="1.0" encoding="UTF-8" ?>  
<persistence xmlns="http://java.sun.com/xml/ns/persistence"  
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
    xsi:schemaLocation="http://java.sun.com/xml/ns/persistence   
        http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd" version="2.0">  
    <!-- 一个配置文件中可以有多个持久化单元(persistence-unit)  RESOURCE_LOCAL表示本地事务 -->  
    <persistence-unit name="xtayfjpk" transaction-type="RESOURCE_LOCAL">  
        <!-- 包含一个Person实体 -->  
        <class>com.xtayfjpk.jdo.entity.Person</class>  
        <exclude-unlisted-classes/>  
        <properties>  
            <property name="datanucleus.ConnectionDriverName" value="com.mysql.jdbc.Driver"/>  
            <property name="datanucleus.ConnectionURL" value="jdbc:mysql://127.0.0.1:3306/test"/>  
            <property name="datanucleus.ConnectionUserName" value="root"/>  
            <property name="datanucleus.ConnectionPassword" value="******"/>  
            <property name="datanucleus.autoCreateSchema" value="true"/>  
        </properties>  
    </persistence-unit>  
</persistence>
```
properties中主要设置的是数据库连接信息，该连接信息还有另一种写法：
```
<property name="javax.jdo.option.ConnectionDriverName" value="com.mysql.jdbc.Driver"/>  
<property name="javax.jdo.option.ConnectionURL" value="jdbc:mysql://127.0.0.1:3306/test"/>  
<property name="javax.jdo.option.ConnectionUserName" value="root"/>  
<property name="javax.jdo.option.ConnectionPassword" value="******"/>  
```
javax.jdo.option.ConnectionDriverName是datanucleus.ConnectionDriverName的别名，其它依此类推，datanucleus.autoCreateSchema=true表示在操作JDO API的时候对应的数据库表还没有创建的话会根据实体的元数据自动创建表。当然可以设置的属性还有很多，具体请参看：http://www.datanucleus.org/products/accessplatform_3_3/persistence_properties.html。

下面是Person类代码：
```
    package com.xtayfjpk.jdo.entity;  
      
    import javax.jdo.annotations.Column;  
    import javax.jdo.annotations.PersistenceCapable;  
    import javax.jdo.annotations.PrimaryKey;  
      
    @PersistenceCapable(table="JDO_PERSON")  
    public class Person {  
        @PrimaryKey  
        @Column(name="P_ID")  
        //如果ID要自增长请加上@Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)  
        private Integer id;  
          
        @Column(name="P_NAME")  
        private String name;  
          
        @Column(name="P_AGE")  
        private int age;  
          
        @Column(name="P_EMAIL")  
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
            return "Person [id=" + id + ", name=" + name + ", age=" + age  
                    + ", email=" + email + "]";  
        }  
    }  
```
配置元数据即可以既使用XML配置文件，也可以使用注解，但个人习惯使用注解。要使一个类能够被JDO操作，必须在类上加上@PersistenceCapable注解，其table属性用于设置该实体对应的数据库表名称。@PrimaryKey用于表示这是主键，@Column(name="P_AGE")用于指定表字段名称，@Column不是必须的，这些注解即可放置在字段上，也可以放置在相应的getter方法上.JDO提供的注解有很多，有些注解还有很多属性，具体请参看：http://db.apache.org/jdo/annotations.html。

```
    package com.xtayfjpk.jdo;  
      
      
    import javax.jdo.JDOHelper;  
    import javax.jdo.PersistenceManager;  
    import javax.jdo.PersistenceManagerFactory;  
    import javax.jdo.Transaction;  
      
    import org.junit.Before;  
    import org.junit.Test;  
      
    import com.xtayfjpk.jdo.entity.Person;  
      
    /** 
     * Unit test for JDO. 
     */  
      
    public class JDOTest {  
        private static PersistenceManagerFactory pmf = null;  
          
        @Before  
        public void before() {  
            /*Properties properties = new Properties(); 
            properties.setProperty("javax.jdo.option.ConnectionDriverName","com.mysql.jdbc.Driver"); 
            properties.setProperty("javax.jdo.option.ConnectionURL","jdbc:mysql://127.0.0.1:3306/test"); 
            properties.setProperty("javax.jdo.option.ConnectionUserName","root"); 
            properties.setProperty("javax.jdo.option.ConnectionPassword","xtayfjpk"); 
            properties.setProperty("javax.jdo.option.Optimistic","true"); 
            properties.setProperty("datanucleus.autoCreateSchema","true");*/  
            //getPersistenceManagerFactory有很多重载的方法，具体请参看API  
            pmf = JDOHelper.getPersistenceManagerFactory("xtayfjpk");  
        }  
          
        @Test  
        public void testPersistenceManageFactory() {  
            System.out.println(pmf);  
        }  
          
        @Test  
        public void testSave() {  
            PersistenceManager pm = pmf.getPersistenceManager();  
            Transaction tx = pm.currentTransaction();  
            try {  
                tx.begin();  
                Person person = new Person();  
                person.setId(1);  
                person.setName("王五");  
                person.setAge(40);  
                person.setEmail("wangwu@qq.com");  
                Person p = pm.makePersistent(person);  
                tx.commit();  
            } catch (Exception e) {  
                e.printStackTrace();  
                if (tx.isActive()) {  
                    tx.rollback();  
                }  
            } finally {  
                pm.close();  
                pmf.close();  
            }  
        }  
          
    }  
```
有一点特别需要注解的是：虽然上面在写Person类的时候就是一个纯POJO，但DataNucleus其实并不能真正直接使用该类，所以DataNucleus采用了一种增强机制，这正是引入datanucleus-enhancer包的原因。如果不进行增强直接运行会报：
Found Meta-Data for class com.xtayfjpk.jdo.entity.Person but this class is not enhanced!! Please enhance the class before running DataNucleus.异常

增强POJO有两种方式，一种是手动，比较麻烦。另一种是自动增强，但要安装DataNucleus Eclipse插件，插件安装地址为：
http://www.datanucleus.org/downloads/eclipse-update/，推荐使用3版本，4版本好像有JDK兼容的问题。安装完插件后，右击工程弹出的菜单会多出一个DataNucleus菜单项。

![](http://img.blog.csdn.net/20140508165857593?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveHRheWZqcGs=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

然后添加DataNucleus支持(add DataNucleus support)，再选择enabel Auto-Ehancement启用自动增强。
> From stackoverflow : http://stackoverflow.com/questions/28353189/datanucleus-auto-create-table

### pom.xml
```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>somem</artifactId>
        <groupId>somem</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>persistence</artifactId>
    <packaging>jar</packaging>

    <name>persistence</name>
    <url>http://maven.apache.org</url>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- jdo -->
        <dependency>
            <groupId>javax.jdo</groupId>
            <artifactId>jdo-api</artifactId>
            <version>3.0.1</version>
        </dependency>
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
        
        <!-- spring -->
        <dependency>
            <artifactId>spring-orm</artifactId>
            <groupId>org.springframework</groupId>
            <version>${spring.version}</version>
        </dependency>
        
        <!-- 数据库驱动 -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>9.3-1102-jdbc41</version>
        </dependency>
    </dependencies>
</project>
```

### User entity 
```
@PersistenceCapable
public class User {

    /** 
     * 创建`带生成的键`（使用 valueStrategy = IdGeneratorStrategy.IDENTITY 的键字段）的新对象时，其键值初始为 null。
     * 键字段在对象写入数据存储区时填充。
     * 如果使用事务，则对象在事务提交时写入。
     * 否则，如果创建对象，则对象在调用 makePersistent() 方法时写入；如果更新对象，则对象在调用 PersistenceManager 实例的 close() 方法时写入。
     */
    @PrimaryKey
    @Persistent( valueStrategy = IdGeneratorStrategy.IDENTITY )
    private Long id;

    @Persistent
    private String name;

    public User() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

### Config
```
@Configuration
@EnableTransactionManagement
public class JDOConfiguration{

    @Bean
    public PersistenceManagerFactory persistenceManagerFactory() {

        PersistenceManagerFactory persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory("jdo.properties");

        return persistenceManagerFactory;
    }



    @Bean
    public JdoTransactionManager JdoTransactionManager() {
        JdoTransactionManager JdoTransactionManager = new JdoTransactionManager();
        JdoTransactionManager.setPersistenceManagerFactory(persistenceManagerFactory());
        return JdoTransactionManager;
    }

}
```

###  jdo.properties
```
datanucleus.schema.autoCreateAll=true
javax.jdo.PersistenceManagerFactoryClass=org.datanucleus.api.jdo.JDOPersistenceManagerFactory
javax.jdo.option.ConnectionURL= jdbc:postgresql://localhost/somem
javax.jdo.option.ConnectionUserName = dom
javax.jdo.option.ConnectionPassword = dom
javax.jdo.option.ConnectionDriverName = org.postgresql.Driver
```

>关于v3.X与4.0的区别：
>`datanucleus.autoCreateSchema` is not a valid property in DataNucleus 4.0 (see [the properties doc](http://www.datanucleus.org/products/accessplatform_4_0/persistence_properties.html)), 
>as defined by the [migration guide from v3.x](http://www.datanucleus.org/products/accessplatform_4_0/migration.html). Using `datanucleus.schema.autoCreateAll` would make more sense.
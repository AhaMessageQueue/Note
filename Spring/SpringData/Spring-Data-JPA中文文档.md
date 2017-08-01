# 目录

```text
# 简介
    ## 依赖管理
        ### Spring Boot
# 文档
    ## 1. 使用 Spring Data Repositories
        ### 1.1 核心概念
        ### 1.2 查询方法
            #### 1.2.1 声明Repository接口
            #### 1.2.2 Spring Data 多模块
            #### 1.2.3 定义查询方法
                ##### 构建查询
                ##### 属性表达式
                ##### 特殊参数处理
            #### 1.2.4 Streaming 查询结果集
            #### 1.2.5 异步查询结果
            #### 1.2.6 创建Repository实体
                ##### XML配置
                ##### JavaConfig
                ##### 独立使用
        ### 1.3 自定义Repository实现
            #### 1.3.1 在repository中添加自定义方法
                ##### 配置
                ##### 人工装载
            #### 1.3.2 为所有的repository添加自定义方法
        ### 1.4 事件发布
```

# 简介

>本文档对应的是Spring Data JPA 1.4.3 RELEASE
>文档部分内容已更新至version 1.11.6.RELEASE, 2017-07-27

## 依赖管理

为了让Spring Data的版本保持一致,可以使用maven提供的`dependencyManagement`

```xml

<properties>
    <spring.data.releasetrain.version>Ingalls-RELEASE</spring.data.releasetrain.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.data</groupId>
            <artifactId>spring-data-releasetrain</artifactId>
            <version>${release-train}</version>
            <scope>import</scope>
            <type>pom</type>
        </dependency>
    </dependencies>
</dependencyManagement>
```

目前的发行版本是Ingalls-SR6。目前可用的版本名称列在[这里](https://github.com/spring-projects/spring-data-commons/wiki/Release-planning)。 
版本名称遵循以下模式：${name}-${release}其中`release`可以是以下之一：

    BUILD-SNAPSHOT - current snapshots

    M1, M2 etc. - milestones

    RC1, RC2 etc. - release candidates

    RELEASE - GA release

    SR1, SR2 etc. - service releases

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-jpa</artifactId>
  </dependency>
<dependencies>
```

### Spring Boot

Spring Boot 会选择一个较新的版本,但是假使你想升级到一个更新的版本,你可以只配置`spring-data-releasetrain.version`属性为下列属性值中的一个.

    BUILD-SNAPSHOT - current snapshots

    M1, M2 etc. - milestones

    RC1, RC2 etc. - release candidates

    RELEASE - GA release

    SR1, SR2 etc. - service releases
    
# 文档

## 1. 使用 Spring Data Repositories

Spring Data Repository的存在，是为了把你从大量重复、繁杂的数据库层操作中解放出来。

### 1.1 核心概念
Spring Data Repository的核心接口是Repository(好像也没什么好惊讶的)。这个接口需要领域类(Domain Class)跟领域类的ID类型作为参数。
这个接口主要是让你能知道继承这个类的接口的类型。CrudRepository提供了对被管理的实体类的一些常用CRUD方法。

```java
// 例1.1 CrudRepository接口
public interface CrudRepository<T, ID extends Serializable>
extends Repository<T, ID> {
    <S extends T> S save(S entity);//① 保存给定的实体
    T findOne(ID primaryKey);//② 返回指定ID的实体
    Iterable<T> findAll();//③ 返回全部实体
    Long count();//④ 返回实体的总数
    void delete(T entity);//⑤ 删除指定的实体
    boolean exists(ID primaryKey);//⑥ 判断给定的ID是否存在
    // … 省略其他方法
}
```

通常我们要扩展功能，那么我们就需要在接口上做子接口。那么我们要添加功能的时候，就在CrudRepository的基础上去增加。

`PagingAndSortingRepository` 是一个继承`CrudRepository`的接口，他扩展了分页与排序的功能。

```java
// 例1.2 PagingAndSortingRepository
public interface PagingAndSortingRepository<T, ID extends Serializable>
extends CrudRepository<T, ID> {
    Iterable<T> findAll(Sort sort);
    Page<T> findAll(Pageable pageable);
}
```

```java
// 用户分页查询
PagingAndSortingRepository<User, Long> repository = // … get access to a bean
Page<User> users = repository.findAll(new PageRequest(1, 20));
```

### 1.2 查询方法

一般的增删改查功能都会有一些查询语句去查询数据库，在Spring Data，你只需要简单的做四个步骤即可实现！

1. 声明一个继承于Repository或者它的子接口的接口，并且输入类型参数，如下：
    
    ```java
    public interface PersonRepository extends Repository<User, Long> { … }
    ```
    
2. 声明查询的方法在接口上
    
    ```java
    List<Person> findByLastname(String lastname);
    ```
    
    你没有看错，你只要声明，不需要实现！Spring Data会创建代理对象帮你完成那些繁琐的事情。

3. 在Spring上配置

    - Java Config
    
    ```java
    import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
    
    @EnableJpaRepositories
    class Config {}
    ```
    
    - XML configuration
    
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <beans:beans xmlns:beans="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.springframework.org/schema/data/jpa"
        xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/data/jpa
        http://www.springframework.org/schema/data/jpa/spring-jpa.xsd">
        
        <repositories base-package="com.acme.repositories" />
        
    </beans>
    ```
    
    注意，上面的命名空间使用了JPA的命名空间

4. 在业务中使用
    
    ```java
    public class SomeClient {
        @Autowired
        private PersonRepository repository;
        
        public void doSomething() {
            List<Person> persons = repository.findByLastname("Matthews");
        }
    }
    ```
    
    这部分的代码将在下部分中解释。

#### 1.2.1 声明Repository接口

在上面的第一步操作中定义了接口，这些接口必须都继承于Repository或者其子类，并且标注领域类(Domain Class)以及ID类型。
如果你想暴露CRUD方法，那么你可以直接继承CrudRepository接口。

通常，我们的Repository会继承Repository, CrudRepository 或者PagingAndSortingRepository中的一个。
或者，如果您不想扩展Spring Data接口，还可以使用`@RepositoryDefinition`对repository接口进行注释。

继承CrudRepository接口可以让你暴露出很多方法去操作你的实体类。 
如果你仅仅想暴露几个接口给其他人使用， 那么你只需要从CrudRepository中拷贝几个需要的方法到自己的Repository中。

>这允许您在提供的Spring Data Repositories功能之上定义自己的抽象。

```java
// 例1.3 选择性的暴露接口
@NoRepositoryBean
interface MyBaseRepository<T, ID extends Serializable> extends Repository<T, ID> {
    T findOne(ID id);
    T save(T entity);
} 
interface UserRepository extends MyBaseRepository<User, Long> {
    User findByEmailAddress(EmailAddress emailAddress);
}
```

在这里我们只暴露出findOne(...)跟save(...)两个方法出来。
对于UserRepository，它除了有根据ID查询的方法、保存实体的方法之外，还有根据Email地址查询用户的方法。

>请注意，中间的repository接口使用`@NoRepositoryBean`注释。
确保将该注释添加到Spring Data不应在运行时创建实例的所有repository接口。

#### 1.2.2 Spring Data 多模块

在应用程序中使用唯一的Spring Data模块使事情变得简单，因此定义范围内的所有repository接口都绑定到Spring Data模块。

有时应用程序需要使用多个Spring Data模块。在这种情况下，需要repository definition区分持久性技术。 

Spring Data进入严格的repository配置模式，因为它在类路径上检测到多个repository factories。

严格的配置需要有关repository或域类的详细信息来确定repository definition的Spring数据模块绑定：

- 如果repository definition扩展了模块特定的repository，那么它是特定Spring Data模块的有效候选项。
- 如果域类使用模块特定类型注释进行注释，那么它是特定Spring Data模块的有效候选项。 
    Spring Data模块接受第三方注释（例如JPA的@Entity）或提供自己的注释，例如Spring Data MongoDB / Spring Data Elasticsearch的@Document。

**1、使用模块特定接口的repository definitions**

```java
interface MyRepository extends JpaRepository<User, Long> { }

@NoRepositoryBean
interface MyBaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
  …
}

interface UserRepository extends MyBaseRepository<User, Long> {
  …
}
```

`MyRepository`和`UserRepository`在其类型层次结构中扩展`JpaRepository`。 它们是**Spring Data JPA**模块的有效候选。

**2、使用通用接口的repository definitions**

```java
interface AmbiguousRepository extends Repository<User, Long> {
 …
}

@NoRepositoryBean
interface MyBaseRepository<T, ID extends Serializable> extends CrudRepository<T, ID> {
  …
}

interface AmbiguousUserRepository extends MyBaseRepository<User, Long> {
  …
}
```

`AmbiguousRepository`和`AmbiguousUserRepository`仅扩展其类型层次结构中的`Repository`和`CrudRepository`。
虽然使用独特的Spring Data模块是非常好的，但是多个模块无法区分哪一个特定的Spring Data是repositories要绑定的。

**3、使用带有注释的域类的repository definitions**

```java
interface PersonRepository extends Repository<Person, Long> {
 …
}

@Entity
public class Person {
  …
}

interface UserRepository extends Repository<User, Long> {
 …
}

@Document
public class User {
  …
}
```

`PersonRepository`引用用JPA注释@Entity注释的Person，因此这个repository显然属于Spring Data JPA。
`UserRepository`使用Spring Data MongoDB的@Document注释的User。

**4、使用具有混合注释的域类的repository definitions**

```java
interface JpaPersonRepository extends Repository<Person, Long> {
 …
}

interface MongoDBPersonRepository extends Repository<Person, Long> {
 …
}

@Entity
@Document
public class Person {
  …
}
```

此示例展示使用JPA和Spring Data MongoDB注释的域类。

它定义了两个repositories，JpaPersonRepository和MongoDBPersonRepository。
一个用于JPA，另一个用于MongoDB。
Spring Data不再能够区分出导致未定义的行为的repositories。

`Repository type details` 和 `identifying domain class annotations`用于严格的repository配置，以识别特定Spring Data模块的repository候选。
在同一个域类型上使用多个持久性特定技术的注释可能会跨多个持久性技术重用域类型，但是Spring Data不再能够确定绑定repository的唯一模块。

区分资源库的最后一个方法是定义repository基础包。基础包定义了扫描repository接口定义的起点，这意味着repository definitions位于相应的包中。

默认情况下，注释驱动的配置使用配置类所在的包。
基于XML的配置中的基本包是强制性的。

```java
@EnableJpaRepositories(basePackages = "com.acme.repositories.jpa")
@EnableMongoRepositories(basePackages = "com.acme.repositories.mongo")
interface Configuration { }
```

#### 1.2.3 定义查询方法

Spring Data通过方法名有两种方式去解析出用户的查询意图：一种是直接通过方法的命名规则去解析，第二种是通过Query去解析，那么当同时存在几种方式时，Spring Data怎么去选择这两
种方式呢？好了，Spring Data有一个策略去决定到底使用哪种方式：

**查询策略**：

接下来我们将介绍策略的信息,你可以通过配置`<repository>`的`query-lookup-strategy`属性来决定。
或者通过Java config的`Enable${store}Repositories`注解的`queryLookupStrategy`属性来指定:

- CREATE
    通过解析方法名字来创建查询。这个策略是删除方法中固定的前缀，然后再来解析其余的部分。
    
- USE_DECLARED_QUERY
    它会根据已经定义好的语句去查询，如果找不到，则会抛出异常信息。这个语句可以在某个注解或者方法上定义。根据给定的规范来查找可用选项，如果在方法被调用时没有找到定义的查
    询，那么会抛出异常。

- CREATE_IF_NOT_FOUND(默认)
    这个策略结合了以上两个策略。它会优先查询是否有定义好的查询语句，如果没有，就根据方法的名字去构建查询。这是一个默认策略，如果不特别指定其他策略，那么这个策略会在项目
    中沿用。
    
##### 构建查询

查询构造器是内置在SpringData中的，他是非常强大的，这个构造器会从方法名中剔除掉类似find...By, read...By, 或者get...By的前缀，然后开始解析其余的名字。

你可以在方法名中加入更多的表达式，例如你需要Distinct的约束，那么你可以在方法名中加入Distinct即可。在方法中，第一个By表示着查询语句的开始，你也可以用And或者Or来关联多个条件。

```java
// 例1.4 通过方法名字构建查询
public interface PersonRepository extends Repository<User, Long> {
    List<Person> findByEmailAddressAndLastname(EmailAddress emailAddress, String lastname);
    // 需要在语句中使用Distinct关键字， 你需要做的是如下
    List<Person> findDistinctPeopleByLastnameOrFirstname(String lastname, String firstname);
    List<Person> findPeopleDistinctByLastnameOrFirstname(String lastname, String firstname);
    // 如果你需要忽略大小写， 那么你要用IgnoreCase关键字， 你需要做的是如下
    List<Person> findByLastnameIgnoreCase(String lastname);
    // 所有属性都忽略大小写呢？ AllIgnoreCase可以帮到您
    List<Person> findByLastnameAndFirstnameAllIgnoreCase(String lastname, String firstname);
    // 同样的， 如果需要排序的话， 那你需要： OrderBy
    List<Person> findByLastnameOrderByFirstnameAsc(String lastname);
    List<Person> findByLastnameOrderByFirstnameDesc(String lastname);
}
```

根据方法名解析的查询结果跟数据库是相关，但是，还有几个问题需要注意：
- 多个属性的查询可以通过连接操作来完成，例如And，Or。当然还有其他的，例如Between，LessThan，GreaterThan，Like。这些操作时跟数据库相关的，当然你还需要看看相关的
- 数据库文档是否支持这些操作。
- 你可以使用IngoreCase来忽略被标记的属性的大小写，也可以使用AllIgnoreCase来忽略全部的属性，当然这个也是需要数据库支持才允许的。
- 你可以使用OrderBy来进行排序查询，排序的方向是Asc跟Desc，如果需要动态排序，请看后面的章节。

##### 属性表达式

好了，讲了那么多了，具体的方法名解析查询需要怎样的规则呢？这种方法名查询只能用在被管理的实体类上，就好像之前的案例。假设一个类Person中有个Address，并且Address还有
ZipCode，那么根据ZipCode来查询这个Person需要怎么做呢？

```java
List<Person> findByAddressZipCode(ZipCode zipCode);
```

在上面的例子中，我们用x.address.zipCode去检索属性，这种解析算法会在方法名中先找出实体属性的完整部分(AddressZipCode)，检查这部分是不是实体类的属性，如果解析不成功，则按
照驼峰式从右到左去解析属性，如：AddressZipCode将分为AddressZip跟Code，在这个时候，我们的属性解析不出Code属性，则会在此用同样的方式切割，分为Address跟ZipCode（如果
第一次分割不能匹配，解析器会向左移动分割点），并继续解析。
为了避免这种解析的问题，你可以用“_”去区分，如下所示：

```java
List<Person> findByAddress_ZipCode(ZipCode zipCode);
```

##### 特殊参数处理

要处理查询中的参数，您只需定义方法参数，如上述示例中所示。 此外，框架将识别某些特定类型，如Pageable和Sort，以动态地将分页和排序应用于查询。

```java
// 在查询方法中使用分页和排序
Page<User> findByLastname(String lastname, Pageable pageable);

Slice<User> findByLastname(String lastname, Pageable pageable);

List<User> findByLastname(String lastname, Sort sort);

List<User> findByLastname(String lastname, Pageable pageable);
```

第一种方法允许您将`org.springframework.data.domain.Pageable`实例传递给查询方法，以动态地将分页添加到静态定义的查询中。
`Page`会统计可用元素和分页的总数。它通过框架触发**计数查询**来计算总数。
这可能开销很大（取决于所使用的存储），因此`Slice`可以用作返回值。一个`Slice`只知道是否有一个下一个`Slice`可用，当遍历大结果集的时候，这已经足够了。

排序选项也可以通过`Pageable`实例来处理。
如果只需要排序，只需在您的方法中添加一个`org.springframework.data.domain.Sort`参数即可。
正如你看到的，只需返回`List`即可。
在这种情况下，不会创建构建实际`Page`实例所需的额外元数据（这意味着额外的计数查询是不必要的）。

### 1.2.3 限制查询结果

查询方法的结果可以通过关键字`first`或`top`进行限制，可以互换使用。
可选的数值可以追加到`top/first`以指定要返回的最大结果大小。如果数字被省略，则假设结果大小为1。

```java
// 例1.5 使用Top和First限制查询的结果大小
User findFirstByOrderByLastnameAsc();

User findTopByOrderByAgeDesc();

Page<User> queryFirst10ByLastname(String lastname, Pageable pageable);

Slice<User> findTop3ByLastname(String lastname, Pageable pageable);

List<User> findFirst10ByLastname(String lastname, Sort sort);

List<User> findTop10ByLastname(String lastname, Pageable pageable);
```

限制表达式还支持Distinct关键字。

>注意，通过`Sort`参数将限制结果集与动态排序结合，可以表示最小'K'以及最大'K'元素的查询方法。

#### 1.2.4 Streaming 查询结果集

可以通过使用Java 8 `Stream<T>`作为返回类型来逐步处理查询方法的结果。
不是简单地将查询结果包裹在`Stream`数据存储中，而是使用特定的方法来执行流式传输。

```java
// 使用Java 8 Stream<T>
@Query("select u from User u")
Stream<User> findAllByCustomQueryAndStream();

Stream<User> readAllByFirstnameNotNull();

@Query("select u from User u")
Stream<User> streamAllPaged(Pageable pageable);
```

Stream可能包装底层数据存储特定资源，因此必须在使用后关闭。 您可以使用`close()`方法或使用`Java 7 try-with-resources`块手动关闭`Stream`。

```java
// 使用try-with-resources代码块
try (Stream<User> stream = repository.findAllByCustomQueryAndStream()) {
  stream.forEach(…);
}
```

>并非所有的Spring Data模块都支持`Stream<T>`作为返回类型。

#### 1.2.5 异步查询结果

可以使用Spring的异步方法执行功能异步执行Repository查询。
这意味着方法将在调用后立即返回，并且实际的查询将发生在已提交给Spring TaskExecutor的任务中。

```java
@Async
Future<User> findByFirstname(String firstname);// Use java.util.concurrent.Future as return type.       

@Async
CompletableFuture<User> findOneByFirstname(String firstname);// Use a Java 8 java.util.concurrent.CompletableFuture as return type.

@Async
ListenableFuture<User> findOneByLastname(String lastname);// Use a org.springframework.util.concurrent.ListenableFuture as return type.
```

#### 1.2.6 创建Repository实体

创建已定义的Repository接口，最简单的方式就是使用Spring配置文件，当然，需要JPA的命名空间。

##### XML配置

你可以使用JPA命名空间里面的repositories去自动检索路径下的repositories元素：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans:beans xmlns:beans="http://www.springframework.org/schema/beans"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xmlns="http://www.springframework.org/schema/data/jpa"
             xsi:schemaLocation="http://www.springframework.org/schema/beans
http://www.springframework.org/schema/beans/spring-beans.xsd
http://www.springframework.org/schema/data/jpa
http://www.springframework.org/schema/data/jpa/spring-jpa.xsd">
    <repositories base-package="com.acme.repositories"/>
</beans:beans>
```

在本例中，Spring能够通过`base-package`检测出指定路径下所有继承Repository或者其子接口的接口(有点绕口)。每找到一个接口的时候，FactoryBean就会创建一个合适的代理去处理以及
调用里面的查询方法。每个注册的Bean的名称都是源于接口名称，例如：UserRepository将会被注册为userRepository。`base-package`允许使用通配符作为扫描格式。

**使用过滤器**:

在默认的设置中，将使用全路径扫描的方式去检索接口，当然，你在业务上可能需要更细致的操作，这时候，你可以在`<repositories>`中使用`<include-filter>`或者`<exclude-filter>`。这样的话，

你可以指定扫描的路径包含或者不包含指定的路径。

例如我们现在想过滤掉一些指定的接口，那么你可以这么做：

```xml
<!-- 例1.6 使用排除过滤 -->
<repositories base-package="com.acme.repositories">
    <context:exclude-filter type="regex" expression=".*SomeRepository" />
</repositories>
```

这个例子中，我们排除了所有以SomeRepository结尾的接口。

##### JavaConfig

你可以在JavaConfig中使用`@Enable${store}Repositories`注解来实现。那么代码就是如下：

```java
// 例1.7 使用JavaConfig
@Configuration
@EnableJpaRepositories("com.acme.repositories")
class ApplicationConfiguration {
    @Bean
    public EntityManagerFactory entityManagerFactory() {
    // …
    }
}
```

##### 独立使用

你可以不在Spring容器里面使用repository。但是你还需要Spring的依赖包在你的classpath中，你需要使用RepositoryFactory来实现，代码如下：

```java
// 例1.8 独立模式下使用
RepositoryFactorySupport factory = … // 初始化
UserRepository repository = factory.getRepository(UserRepository.class);
```

### 1.3 自定义Repository实现

我们可以自己实现repository的方法。

#### 1.3.1 在repository中添加自定义方法

为了丰富我们的接口我们通常会自定义自己的接口以及对应的实现类。

```java
// 例1.9 自定义接口
interface UserRepositoryCustom {
    public void someCustomMethod(User user);
}
```

```java
// 自定义接口的实现类
class UserRepositoryImpl implements UserRepositoryCustom {
    public void someCustomMethod(User user) {
        // 实现
    }
}
```

```java
// 扩展CRUDRepository
public interface UserRepository extends CrudRepository<User, Long>, UserRepositoryCustom {
    // 声明查询方法
}
```

这样的话，就能够在常用的Repository中实现自己的方法。

##### 配置

在XML的配置里面，框架会自动搜索`base-package`里面的实现类，这些实现类的后缀必须满足`repository-impl-postfix`中指定的命名规则，默认的规则是：`Impl`

```xml
<!-- 例1.12 配置实例 -->
<repositories base-package="com.acme.repository" />
<repositories base-package="com.acme.repository" repository-impl-postfix="FooBar" />
```

第一个配置我们将找到`com.acme.repository.UserRepositoryImpl`，而第二个配置我们将找到`com.acme.repository.UserRepositoryFooBar`。

##### 人工装载

前面的代码中，我们使用了注释以及配置去自动装载。如果你自己定义的实现类需要特殊的装载，那么你可以跟普通bean一样声明出来就可以了，框架会手工的装载起来，而不是自己创建一个。

```java
// 例1.13 人工装载实现类
<repositories base-package="com.acme.repository" />
<beans:bean id="userRepositoryImpl" class="…">
    <!-- 其他配置 -->
</beans:bean>
```

#### 1.3.2 为所有的repository添加自定义方法

当您想将一个方法添加到所有的Repository接口时，上述方法是不可行的。

要将自定义行为添加到所有Repositories，您首先添加一个中间接口来声明共享行为。

1. 你需要先声明一个中间接口，然后让你的接口来继承这个中间接口而不是Repository接口，代码如下：

    ```java
    // 例1.14 中间接口
    @NoRepositoryBean
    public interface MyRepository<T, ID extends Serializable>
      extends PagingAndSortingRepository<T, ID> {
    
      void sharedCustomMethod(ID id);
    }
    ```
    
    现在，您的各个Repository接口将扩展此中间接口而不是Repository接口，以包含声明的功能。

2. 接下来，创建中间接口的实现（扩展了持久性特定技术的Repository基类）。该类将用作Repository代理的自定义基类。
    
    ```java
    // 自定义基类
    public class MyRepositoryImpl<T, ID extends Serializable>
      extends SimpleJpaRepository<T, ID> implements MyRepository<T, ID> {
    
      private final EntityManager entityManager;
    
      public MyRepositoryImpl(JpaEntityInformation entityInformation,
                              EntityManager entityManager) {
        super(entityInformation, entityManager);
    
        // Keep the EntityManager around to used from the newly introduced methods.
        this.entityManager = entityManager;
      }
    
      public void sharedCustomMethod(ID id) {
        // implementation goes here
      }
    }
    ```
    
    >该类需要具有超类的构造函数。
    如果Repository基类具有多个构造函数，则覆盖使用`EntityInformation`和特定的框架对象（例如，`EntityManager`或模板类）的构造函数。
    
    Spring `<repositoryories />`命名空间的默认行为是为`base-package`下的所有接口提供一个实现。
    这意味着如果保持当前状态，Spring将创建一个`MyRepository`的实现实例。
    这当然是不希望的，因为它只是作为Repository和实际Repository接口之间的中介。
    
    要排除扩展Repository的接口不被实例化为Repository实例，您可以使用`@NoRepositoryBean`进行注释，也可以将其移动到配置的`base-package`之外。
    

3. 最后一步是使Spring Data框架意识到自定义的Repository基类。

    在JavaConfig中，这是通过使用`@Enable…Repositories`注释的`repositoryBaseClass`属性实现的：
    
    ```java
    // 使用JavaConfig配置自定义Repository基类
    @Configuration
    @EnableJpaRepositories(repositoryBaseClass = MyRepositoryImpl.class)
    class ApplicationConfiguration { … }
    ```
    
    相应的xml配置
    
    ```java
    // 使用XML配置自定义Repository基类
    <repositories base-package="com.acme.repository"
       base-class="….MyRepositoryImpl" />
    ```
    
### 1.4 事件发布


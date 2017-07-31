# 目录

# 简介
    ## Spring Boot依赖管理
# 第一部分：文档
    ## 1. 使用 Spring Data Repositories
        ### 1.1 核心概念
        ### 1.2 查询方法
            ##### 1.2.1 声明Repository接口
            ##### 1.2.2 多 Spring Data 模块
            ##### 1.2.3 定义查询方法





# 简介

>本文档对应的是Spring Data JPA 1.4.3 RELEASE

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

## Spring Boot依赖管理

Spring Boot 会选择一个较新的版本,但是假使你想升级到一个更新的版本,你可以只配置`spring-data-releasetrain.version`属性为下列属性值中的一个.

    BUILD-SNAPSHOT - current snapshots

    M1, M2 etc. - milestones

    RC1, RC2 etc. - release candidates

    RELEASE - GA release

    SR1, SR2 etc. - service releases
    
# 第一部分：文档

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

##### 1.2.1 声明Repository接口

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

##### 1.2.2 多 Spring Data 模块

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

它定义了两个repositories，JpaPersonRepository和MongoDBPersonRepository。一个用于JPA，另一个用于MongoDB。
Spring Data不再能够区分出导致未定义的行为的repositories。

`Repository type details` 和 `identifying domain class annotations`用于严格的repository配置，以识别特定Spring Data模块的repository候选。
在同一个域类型上使用多个持久性技术特定的注释可能会跨多个持久性技术重用域类型，但是Spring Data不再能够确定绑定repository的唯一模块。

区分资源库的最后一个方法是定义repository基础包。基础包定义了扫描repository接口定义的起点，这意味着repository definitions位于相应的包中。

默认情况下，注释驱动的配置使用配置类所在的包。
基于XML的配置中的基本包是强制性的。

```java
@EnableJpaRepositories(basePackages = "com.acme.repositories.jpa")
@EnableMongoRepositories(basePackages = "com.acme.repositories.mongo")
interface Configuration { }
```

##### 1.2.3 定义查询方法

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
    
**构建查询**

查询构造器是内置在SpringData中的，他是非常强大的，这个构造器会从方法名中剔除掉类似find...By, read...By, 或者get...By的前缀，然后开始解析其余的名字。

你可以在方法名中加入更多的表达式，例如你需要Distinct的约束，那么你可以在方法名中加入Distinct即可。在方法中，第一个By表示着查询语句的开始，你也可以用And或者Or来关联多个条件。

```java
//例1.4 通过方法名字构建查询
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

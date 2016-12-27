本着“不写单元测试的程序员不是好程序员”原则，我在坚持写着单元测试，不敢说所有的Java web应用都基于Spring，但至少一半以上都是基于Spring的。
发现通过Spring进行bean管理后，做测试会有各种不足，

例如，很多人做单元测试的时候，还要在Before方法中，初始化Spring容器，导致容器被初始化多次。

```
    @Before
     public void init() {
          ApplicationContext ctx = new FileSystemXmlApplicationContext( "classpath:spring/spring-basic.xml");
          baseDao = (IBaseDao) ctx.getBean("baseDao");
          assertNotNull(baseDao);
     }
```

在开发基于Spring的应用时，如果你还直接使用Junit进行单元测试，那你就错过了Spring满汉全席中最重要的一道硬菜。

再说这道菜之前，我们先来讨论下，在基于Spring的javaweb项目中使用Junit直接进行单元测试有什么不足

### 导致多次Spring容器初始化问题
根据JUnit测试方法的调用流程，每执行一个测试方法都会创建一个测试用例的实例并调用setUp()方法。由于一般情况下，我们在setUp()方法中初始化Spring容器，这意味着如果测试用例有多少个测试方法，Spring容器就会被重复初始化多次。虽然初始化Spring容器的速度并不会太慢，但由于可能会在Spring容器初始化时执行加载Hibernate映射文件等耗时的操作，如果每执行一个测试方法都必须重复初始化Spring容器，则对测试性能的影响是不容忽视的；

**使用Spring测试套件，Spring容器只会初始化一次！**

### 需要使用硬编码方式手工获取Bean
在测试用例类中我们需要通过ctx.getBean()方法从Spirng容器中获取需要测试的目标Bean，并且还要进行强制类型转换的造型操作。这种乏味的操作迷漫在测试用例的代码中，让人觉得烦琐不堪；

**使用Spring测试套件，测试用例类中的属性会被自动填充Spring容器的对应Bean ，无须在手工设置Bean**

### 数据库现场容易遭受破坏
测试方法对数据库的更改操作会持久化到数据库中。虽然是针对开发数据库进行操作，但如果数据操作的影响是持久的，可能会影响到后面的测试行为。举个例子，用户在测试方法中插入一条ID为1的User记录，第一次运行不会有问题，第二次运行时，就会因为主键冲突而导致测试用例失败。所以应该既能够完成功能逻辑检查，又能够在测试完成后恢复现场，不会留下“后遗症”；

**使用Spring测试套件，Spring会在你验证后，自动回滚对数据库的操作，保证数据库的现场不被破坏，因此重复测试不会发生问题**

### 不方便对数据操作正确性进行检查
假如我们向登录日志表插入了一条成功登录日志，可是我们却没有对t_login_log表中是否确实添加了一条记录进行检查。一般情况下，我们可能是打开数据库，肉眼观察是否插入了相应的记录，但这严重违背了自动测试的原则。试想在测试包括成千上万个数据操作行为的程序时，如何用肉眼进行检查？

**只要你继承Spring的测试套件的用例类，你就可以通过jdbcTemplate在同一事务中访问数据库，查询数据的变化，验证操作的正确性**

看完上面的内容，相信，你已经知道我说的硬菜是什么了。

**下面，让我们看看，使用Spring测试套件后，代码是如何变优雅的。**
### 加入依赖包
```
使用spring的测试框架需要加入以下依赖包：<br>
1. JUnit 4 <br>
2. Spring Test （Spring框架中的test包）<br>
3. Spring 相关其他依赖包（不再赘述了，就是context等包）<br>
```
如果使用maven，在基于spring的项目中添加如下依赖：
```
    <dependency>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
                <version>4.9</version>
                <scope>test</scope>
            </dependency>
    <dependency>
                <groupId>org.springframework</groupId>
                <artifactId>spring-test</artifactId>
                <version> 3.2.4.RELEASE  </version>
                <scope>provided</scope>
            </dependency>
```
### 创建测试源目录和包
在此，推荐创建一个和src平级的源文件目录，因为src内的类都是为日后产品准备的，而此处的类仅仅用于测试。而包的名称可以和src中的目录同名，这样由于在test源目录中，所以不会有冲突，而且名称又一模一样，更方便检索。这也是Maven的约定。

### 创建测试类
##### 基类,其实就是用来加载配置文件的
```
@RunWith(SpringJUnit4ClassRunner.class)  //使用junit4进行测试
@ContextConfiguration
({"/spring/app*.xml","/spring/service/app*.xml"}) //加载配置文件

//如果加入以下代码，所有继承该类的测试类都会遵循该配置，也可以不加，在测试类的方法上
//控制事务，参见下一个实例
//这个非常关键，如果不加入这个注解配置，事务控制就会完全失效！
//@Transactional
//这里的事务关联到配置文件中的事务控制器（transactionManager = "transactionManager"），同时
//指定自动回滚（defaultRollback = true）。这样做操作的数据才不会污染数据库！
//@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class BaseJunit4Test {
}
```

##### 接着是我们自己的测试类
```
public class UserAssignServiceTest extends BaseJunit4Test{

    @Resource  //自动注入,默认按名称
    private IBaseDao baseDao;

    @Test   //标明是测试方法
    @Transactional   //标明此方法需使用事务
    @Rollback(false)  //标明使用完此方法后事务不回滚,true时为回滚
    public void insert( ) {
        String sql="insert into user(name,password) values(?,?)";
        Object[] objs=new Object[]{"00","000"};
        baseDao.insert( sql , objs );

        String sql1="select * from user where name=? and password=? ";
        List<Map<String,Object>> list=baseDao.queryForList( sql1 , objs );
        System.out.println(list);
        assertTrue(list.size( )>0);
     }

}
```

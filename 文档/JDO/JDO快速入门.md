## JDO快速入门

Java数据对象(Java Data Objects，JDO)是一个应用程序接口(API)，它使Java程序员能够间接地访问数据库，也就是说，不需使用直接的结构化查询语言(SQL)语句。JDO是作为Java数据库连接(JDBC)的一个补充来介绍的，而JDBC是一个支持使用SOL语句对流行的数据库程序进行访问的接口。有了 JDO，程序员就可以使用类来定义数据对象，然后支撑程序就会根据类的定义来管理对给定数据库的实际的数据访问了。　

JDO是以Sun公司为首所制定的Java Community Process(Java标准制定组织，JCP)的一部分。JDBC仍然保留使用是因为它比起JDO允许程序员在数据库访问上有更大的控制权。除JDO和 JDBC外的另一个选择是Enterprise JavaBeans (EJB)。

### 一、为什么要用JDO
Java开发人员已经有好几种存取数据库的方法：序列化，JDBC，面向对象映射工具，面向对象数据库，以及实体EJB。那为什么还要介绍其他的存储架构呢？答案是，上面每一种实现存储的方案都存在一定的限制。JDO正在尝试解决这些限制。

**序列化**：是Java建立的一种传输机制，它能够把对象的信息转换成一系列的字节码，这些字节码可以被传输到网络或者存储到一个文件中。序列化的使用非常简单，但他还是有限制的。它必须立即存取对象的特征，而且它不适合存取大批量的数据。在更改一个对象的属性时如果有错误发生它无法实现“回滚”，因此不适于应用程序对数据完整性的要求，而且不能实现多个线程或程序异步读写数据。所有这些不足都使得序列化无法满足大多数数据存储要求。

**JDBC**：许多程序员使用 JDBC API来操作关系数据库。JDBC克服了许多序列化中存在的缺点：它可以操作大批量的数据，有确保数据一致性的机制，支持信息的并发存取，可以使用已经非常成熟的SQL语言。不幸的是，JDBC使用起来并不像序列化那么简单。JDBC使用的关系范例无法用于存储对象，因此你不得不放弃在代码中使用面向对象原则存储数据。

**面向对象映射工具**：由软件厂商创建的架构可以为你实现对象和关系数据库之间的映射。这种对象-关系映射支持使你专注于对象模型的设计而不必关心面向对象和关系数据库之间的匹配。不幸的是每一种对象-关系映射产品都有一套他自己厂商实现的标准。你不得不使自己的代码迁就于某一个单独厂商的实现。假如这个厂商提高产品价格或者停止对bug更改的支持，使你准备放弃它而用其他的厂商实现架构时，你就必须重写你的代码。

**面向对象的数据库**：比对象关系数据库映射更好的选择使使用一些软件厂商开发了一种新的把对象存储到数据库的方法。这种面向对象的数据库使用起来常常比对象关系映射软件简单。ODMG组织成立的目的之一就是创建一种访问对象数据库的标准API。多数厂商都遵崇ODMG组织的要求，因此由于厂商实现不同带来的麻烦也解决了。但是，一些企业对于从关系数据库转向对象数据库显得犹豫不决，因为有大量的数据存储在传统的关系数据库中。虽然一些数据库分析工具可以用于面向对象数据库与关系数据库之间的移植，然而大量的数据存储使用的仍然是关系数据库。

**实体EJB**:Java平台的**企业级应用**中引入了**实体EJB**。
实体EJB是一个组件，他描述了数据库中的持久性数据信息。
EJB使用类似于对象-关系映射的办法，它提供了一个持久性数据的面向对象的表示。
不同于对象关系软件，**EJB对于关系数据库没有限制**；它描述的持久性信息可以来自一个企业信息系统（EIS）或者其他的存储设备。
而且，**EJB要求遵循一个严格标准，实现它的厂商必须遵循这个标准**。
不幸的是，**EJB标准在面向对象方面稍微有些欠缺**，**比如一些高级的特性：继承、多态和复合关系等**。
另外，EJB的代码编写很复杂，而且它是一个重量级组建需要消耗应用服务器很多的资源来运行。
但是，EJB中的会话 Bean和消息驱动Bean有很多优势，所以**JDO规范详细定义了JDO如何与他们进行集成**。

**JDO**：JDO集成了很多上述持久性机制的特性，这使得在JDO中创建一个持久化(persistence)类就像创建一个序列化类一样简单。JDO支持批量数据的存储，数据一致性，并发处理和JDBC的查询功能。就像对象-关系映射软件和对象数据库一样，它允许使用面向对象的高级特性比如“继承”。它避免了像EJB中实体Bean一样必须依赖于来自厂商定义的严格规范。同EJB一样，JDO也不规定任何特定的后端数据库。

但是，这里还是要说一下，世界上没有“万灵丹”。所以，使用JDO并不是对于每一个应用程序都是有好处的。很多应用程序完全可以使用其他更理想的存储机制。

### 二、JDO架构
下面我开始对JDO的架构作一个简单的介绍。

下图显示了JDO架构主要的几部分：

![](http://dev.yesky.com/imagelist/05/10/cjm5t6op3ur7.gif)

- **JDOHelper** :javax.jdo.JDOHelper类拥有一些静态的助手（helper）方法。这个方法可以获得一个持久对象的生命周期还可以用来创建一个与具体实现厂商无关的PersistenceManagerFactory的实例，这里使用了工厂(factory)模式。
- **PersistenceManagerFactory**:javax.jdo.PersistenceManagerFactory类可以通过JDOHelper类的助手方法获得，这是一个标准的工厂类，他可以创建PersistenceManager类。
- **PersistenceManager**：javax.jdo.PersistenceManager接口是应用程序经常要使用的一个主要的JDO接口。每一个PersistenceManager负责控制一组持久化对象而且他还可以创建新的持久化对象或删除现有的持久化对象。Transaction和 PersistenceManager之间存在这一对一的关系，同时PersistenceManager又是Extent和Query的工厂类，也就是说这两个对象可以通过PersistenceManager创建。
- **PersistenceCapable**:用户定义的持久化类都必须扩展实现PersistenceCapable接口。大多数JDO实现的供应商都提供一种“增强器”（enhancer）的功能，它可以向你要实现的持久化类中增加PersistenceCapable接口的实现。也就是说，其实你根本不会自己去实现这个接口。
- **Transaction**:每一个PersistemceManager和javax.jdo.Transaction都是一一对应的。Transactions用来处理事务，它使得持久化数据可以成批的一次性添加到数据表中，如果出现异常就将数据回滚。
- **Extent**:java.jdo.Extent是映射数据库中具体表的类的一个逻辑视图。Extent可以拥有自己的子类，它通过PersistenceManager获得。
- **Query**:java.jdo.Query接口用具体的厂商JDO来实现，它负责处理JDO查询语言(JDOQL),这些JDOQL最终被解释为实际的数据库SQL语言。同样这个接口也是通过PersistenceManager获得的

下面的例子显示的JDO接口如何操作并执行一个查询并更新持久化对象。

**例子：JDO接口的交互**

```
//通过助手类获得PersistenceManagerFactory
PersistenceManagerFactory factory=
JDOHelper.getPersistenceManagerFactory(System.getProperties());

//通过PersistenceManagerFactory获得PersistenceManager对象
PersistenceManager pm=factory.getPersistenceManager();

//创建并开始一个事务
Transaction tx=pm.currentTransaction();
tx.begin();

//查询employee表中每周工作时间大于40小时的研究人员
Extent ex=pm.getExtent(Employee.class,false);

//获得一个Query
Query query=pm.newQuery();

//设置这个query作用的范围，即查询的是那个表或记录集
query.setCandidates(ex);
query.setFilter("division.name == ＼"Research＼" "+ "&& avgHours > 40");//可以直接使用对象属性查询
Collection result=(Collection)query.execute();
Employee emp;
for(Iterator itr=result.iterator();itr.hasNext();){
emp=(Employee)itr.next();
emp.setSalary(emp.getSalary()*2);
}

//提交记录释放资源
tx.commit();
pm.close();
factory.close(); 
```
上面的代码片断包括了JDO几个主要的接口，在此你可以对JDO各个接口的使用方法有一个粗略的印象，以后实际的应用中JDO接口也都是这样使用的。

### 三、JDO的异常
JDO不会抛出通常的运行时异常，比如NullPointerExceptions、 IllegalArgumentException等它只抛出JDOException异常。JDOExcetion的结构如下图所示，这是一个继承的层次结构，从他们的字面含义就可以看出它们的用途，在这里就不详细说了，要想了解JDO异常的层次结构可以参考它们的JavaDoc。

![](http://www.2cto.com/uploadfile/Collfiles/20160108/20160108091713100.jpg)

### 四、使用JDO的好处

- 简便性（Portability）：使用JDO API编写的程序可以在不同开发商的多种可用的实现上运行，不用修改一行代码，甚至不用重新编译。
- 透明地访问数据库（Transparent database access）：应用程序开发者编写代码透明地访问底层数据存储，而不需要使用任何数据库特定代码。
- 易用性（Ease of use）：JDO API允许开发者只需要关注他们自己范围内的数据模型（Domain Object Model，DOM），而持久化的细节就留给JDO实现。
- 高性能（High Performance）：Java应用程序开发者不需要担心数据访问的性能优化，因为这个任务已经委派给了JDO实现，它通过改善数据访问的模式以获得最佳性能。
- 和EJB集成（Integration with EJB）：应用程序可以利用EJB的特征，例如远程信息处理、自动分布式事务协调和贯穿整个企业级应用使用同样的DOMs实现安全性。

### 五、使用JDO，vs. EJB和JDBC
JDO并不意味着要取代JDBC。它们是两种以各自独一无二的能力互相补充的技术，具有不同技术背景和开发目的开发者可以使用二者中的一个。例如。JDBC通过直接的数据库访问控制和缓存管理，提供给开发者更大的弹性。JDBC是一种在工业界被广泛认可的成熟技术。另一方面，JDO，通过隐藏SQL提供给开发者更大的简便性。它将Java平台开发者从必须熟悉或学习SQL中解脱出来，而将精力集中在 DOM上，同时JDO管理在持久存储中对象存储的字段到字段的细节。

JDO被设计成EJB的补充。CMP为容器提供简便的持久化，而JDO可以以两种方式集成到EJB 中：

（1）通过会话Bean，它含有JDO Persistence-capable类（会话Bean的持久化助手类）用来实现依赖对象；

（2）通过实体Bean，它含有被用作BMP和CMP代理的 JDO Persistence-capable类。

你可以学习更多关于JDO和JDBC之间的关系，还有EJB2.0 CMP和JDO之间的关系。

### 六、POJO之路
JDO和 EJB之间在持久化模型上显著的差别曾经在开发者中间引起了混乱。作为回应，Sun微系统正领导一个社区项目为Java技术社区创建POJO持久化模型。这个项目在JSR-220的赞助下执行，由Linda DeMichiel领导。JDO2.0（JSR-243）的专家组成员被邀请加入到EJB3.0（JSR-220）专家组中。

创建POJO持久化模型的目的是为所有使用Java SE和Java EE平台的Java应用程序开发者提供一个对象—关系（object-relational）映射工具。值得注意的是Oracle正以co-specification lead的身份加入到Sun EJB3.0。EJB3.0的公众评论草案已经可以得到。

JSR-243（JDO2.0）遵循了那些来自于JSRs220和243规范的领导写给Java技术社区的信件所描述的轮廓。

JDO2.0并不打算作为EJB3.0持久化特定API的集中，而是作为JDO1.0.2的发展。但是JDO的POJO持久化模型和EJB3.0之间的类似处，使得JDO的客户当使用JDO2.0满足立即的需求时，可以很容易的接受EJB3.0持久化模型。另外，JSR-243打算将JDOQL用作一种关于EJB3.0持久化数据的可选查询语言。这种语言了已经被更新从而可以更好地对EJB3.0使用。

要了解更多关于持久化模型的知识，请查看EJB/JDO持久化FAQ。

### 七、JDO Class类型
在JDO中一共有三种类型的类：

- Persistence-capable：这种类型代表那些实例可以被持久化到一个数据存储中的类。请注意，这些类在JDO环境中被使用之前，需要通过JDO元数据规范进行加强。
- Persistence-aware：这些类操纵persistence-capable类。JDOHelper类包含了一些方法，它们允许询问一个persistence-capable类的实例的持久化状态。请注意，这些类使用最小化的JDO元数据加强。
- Normal：这些不可被持久化，并且对持久化一无所知。另外它们不需要JDO元数据。

### 八、JDO实例的生命周期
JDO管理一个对象从创建到删除的生命周期。在它的生命周期，JDO实例不断地转换它的状态，直到最后被Java虚拟机（JVM）作为垃圾回收。状态的转换使用PersistenceManager类的方法完成，包括 TransactionManager——例如makePersistent()、makeTransient()、deletePersistent ()——和提交或者回滚更改。

表1显示JDO规范定义的10种状态。前面的七种是必须的，后面的三种是可选的。如果一个实现不支持某些操作，那么就不会获得三种可选的状态。

表1 JDO生命周期

```
状态 	                    描述
Transient                   任何使用开发者定义的构造函数创建的对象，都不包括持久化环境。一个瞬时实例没有JDO身份。
Persistent-new 	            被应用程序组件请求的任何对象都变为持久的，通过使用PersistenceManager类的makePersistent()。这样的一个对象将会拥有一个分配的JDO身份。
Persistent-dirty            在当前事务中被改变的持久对象。
Hollow 	                    代表在数据存储中特定数据的持久对象，但是在它的实例中没有包含值。
Persistent-clean            代表在数据存储中的特定事务数据的持久对象，并且它们的数据在当前事务处理中还没有被改变。
Persistent-deleted          代表在数据存储中的特定数据的持久对象，并且在当前事务处理中已经被删除。
Persistent-new-deleted 	    在同一个事务处理中最近被持久化和删除的持久对象。
Persistent-nontransactional 代表数据存储中的数据的持久对象，当前它们的值已经被装载，但是还没有事务处理一致。
Transient-clean             代表一个瞬时事务处理实例的持久对象，它们的数据在当前事务中还没有被改变。
Transient-dirty             代表一个瞬时事务处理实例的持久对象，它们的数据在当前事务中已经被改变。
```

图2显示了JDO实例各状态之间的转换。

![](http://dev.yesky.com/imagelist/05/10/5vj7as39kldy.gif)

本文稍后的代码片断，将示范如何执行我们刚刚讨论的操作。

### 九、JDO参考实现
JDO参考实现，来自于Sun微系统，已经可用，一同发行的还有一种被称为fstore的基于文件的存储机制。Sun已经把JDO捐献给开源社区。JDO1.0和JDO2.0将会作为Apache JDO项目的一部分进行开发。但是由于时间的限制，JDO2.0的参考实现并不是作为Apache项目建立的，而是作为一个JPOX 发行。一些商业实现也是可用的。

### 十、JDO编程模型
JDO定义了两种类型的接口：JDO API（在javax.jdo包中）和JDO服务提供者接口（SPI）（在javax.jdo.spi包中）。JDO API面向应用程序开发者，而JDO SPI面向容器提供者，和JDO卖主。

一个应用程序包含两个主要的接口：
·**PersistenceManagerFactory**代表了应用程序开发者用来获得 PersistenceManager实例的访问点。
这个接口的实例可以被配置和序列化以备后来使用。然而，需要注意的是，一旦第一个 PersistenceManager实例从PersistenceManagerFactory中被获得，这个工厂就不再是可配置。
你可以使用下面的代码来获得PersistenceManagerFactory。
```
// 为JDO实现和数据存储设置一些属性
Properties props = new Properties();
props.put(...);
// 得到一个PersistenceManagerFactory
PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory (props);
```
·**PersistenceManager**是JDO-aware应用部分的主要接口。它提供了方法来持久化一个对象，也可以重新得到持久对象和将它们从持久存储中移除。可以使用下面的方法获得PersistenceManager。
```
PersistenceManager pm = pmf.getPersistenceManager ();
```
一旦获得了PersistenceManager对象后，应用程序就可以一些任务，例如：持久化一个对象、从持久数据中获得一个对象、从持久数据中删除一个对象、更新一个对象等等。

接下来的代码片断示范了如何持久化一个对象，它更新一个对象的状态从Transient到Hollow。

![](http://db.apache.org/jdo/images/state_transition_persist.gif)

```
Employee emp = new Employee("Sarah Jones", 23, 37000.00);
Transaction tx;
try {
    tx = pm.currentTransaction();
    tx.begin();
    pm.makePersistent(emp);
    tx.commit();
} catch (Exception e) {
    if(tx.isActive()) {
    tx.rollback();
    }
}
```
从持久数据中获得一个对象同样简单，你可以使用Extent（一个信息的持有者）或者Query（提供了更精确的过滤）。下面是一个使用Extent的例子：
```
try {
    tx = pm.currentTransaction();
    tx.begin();
    Extend ex = pm.getExtent(Employee.class, true);
    Iterator i = ex.iterator();
    while(i.hasNext()) {
        Employee obj = (Employee) i.next();
    }
    tx.commit();
} catch (Exception e) {
    if(tx.isActive()) {
    tx.rollback();
    }
}
```
最后，从持久数据中删除一个对象也可以简单完成，首先获得一个从持久数据中获得一个对象，然后调用deletePersistent(obj)方法。

### 十一、查询对象
JDO规范要求开发商必须提供使用JDOQL的查询能力，JDOQL是一种面向围绕被持久化对象的查询语言。PersistenceManager类定义了构造Query实现类的实例的方法。一个查询过滤器可以被指定为一个布尔表达式，就像SQL的布尔操作符。

生命周期开发：在你的应用程序中使用JDO

可以通过以下六个步骤建立一个JDO应用：
1. 设计你的范围内的将会正常使用的类。对一个要求持久化的类的唯一要求就是它要有一个默认构造函数，访问权限可能是private。
2. 使用元数据定义持久化定义：在这个步骤中，你编写元数据，指定那些类和字段应该被持久化等等。这个文件可以包含对于一个类或一个或者多个包含持久类的包的持久化信息。一个类的元数据文件的名称是这个类的名字加上“.jdo”后缀，注意，这个文件必须放在和.class文件相同的目录中。对于整个包的元数据文件的必须包含在一个称作package.jdo的文件中。元数据文件可以使用XDoclet或手动开发。下面是一个简单的对于两个类的元数据文件：
    ```
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE jdo SYSTEM "jdo.dtd">
    <jdo>
        <package name="com.github.okeyj">
            <class name="Employee" identity-type="application" objectidclass="EmployeeKey">
                <field name="name" primary-key="true">
                    <extension vendor-name="sunw" key="index" value="btree"/>
                </field>
                <field name="salary" default-fetch-group="true"/>
                <field name="dept">
                    <extension vendor-name="sunw" key="inverse" value="emps"/>
                </field>
                <field name="boss"/>
            </class>
    
            <class name="Department" identity-type="application" objectidclass="DepartmentKey">
                <field name="name" primary-key="true"/>
                <field name="emps">
                    <collection element-type="Employee">
                        <extension vendor-name="sunw" key="element-inverse" value="dept"/>
                    </collection>
                </field>
            </class>
        </package>
    </jdo>
    ```
3. 编译这些类，并且使用JDO加强器来加强它们。任何persistence-capable类的实例在被JDO持久化引擎管理之前必须被加强。JDO字节码加强器通过对类定义特定的改变来装换这个类，使得任何持久实例可以和数据存储中的数据描述保持同步。和参考实现一起发行的JDO加强器，能够从Sun微系统得到，可以使用如下的方式运行：

    ```
    > java -classpath
    %JDO-HOME%/lib/jdo.jar;%JDO-HOME%/lib/jdori.jar;
    %JDO-HOME%/jdori-enhancer.jar com.sun.jdori.enhancer.Main -d
    /enhanced -s . -f path/tp/package.jdo path/to/theclasses.class
    ```
    注意：对JDO加强器最重要的参数是一个.jdo文件的名字和.class文件的名字。另外，
    
    - -d选项指定输出文件的目标文件夹；
    
    - -s选项指定jdo和class文件的源文件夹；
    
    - -f选项强制重写输出文件。
    
    如果忽略这个步骤，那么当你运行应用程序和持久化一个对象时将会抛出ClassNotPersistenceCapableException异常。

4. 为被持久化的类建立数据库表。如果你已经有了一个数据库方案，那么这一步是可选的。基本上，你必须建立表、索引和在JDO元数据文件中为类定义的外键。有些JDO实现包含一个方案工具，可以根据JDO元数据文件产生所有的这些东西。

5. 编写代码来持久化你的对象。在这个步骤中，你要指定那些类在什么时间被实际持久化。正如前面提到的，最初的步骤是获得一个PersistenceManager的使用权。

6. 运行你的应用程序。使用java命令，并且包含必要的.jar文件在你的classpath中。

### 十二、JDO对开发的帮助有哪些 - 实例解析
1. 权责划分：业务开发组和数据库管理组

    对一个项目来讲，开发团队在逻辑上划分为两块：业务开发组和数据库管理组。两者各有特点，各有责任，但相互之间界限很清晰，不会有什么纠缠。

2. UML实体类图

    UML实体类图是项目中涉及到数据的部分，这些数据不会随着程序中止而丢失，称作可持续的（Persistent），所有数据库中的数据都是可持续的。
    
    而我们在设计的时候，最开始应该分析出系统有哪些实体类（即可持续的数据类），从而画出实体类图。在这个最初级的类图上面，可以不包含任何属性，但必须包含实体类之间的关系，这样才能一眼看出系统的大概轮廓。
    
    简单地说，项目可以说是一些具有相互关系的实体类加上处理业务逻辑的控制类，以及输入/输出数据的边界类组成，另外可能附加一些接口或特殊服务，如短信/邮件发送或面向第三方的数据访问接口等等。
    
    有了这个图，DBA就比较清楚数据库中会有什么样的数据表，表之间如何关联了。但数据库中的表与实体类并不是一一对应的。比如对实体类图中的某个多多对应关系，数据库中必须有一个额外的表来对应，有些实体的某部分属性可能会放在另一个额外表中以加强性能。
    
    下一步就是在这个图的基础上为实体类添加属性，然后给每个属性加上访问器（accessors，即getXXX()/isXXX()和setXXX() 等），以及一些必须的方法（比如getAge()，通过当前日期和生日得出年龄）。这样，才成为一个完整的实体类图。
    
    接下来，加入对普通属性的访问器方法，可能再给加一个Member.getAge()方法，这个实体类图就算是完成了。这些过程都比较简单，并且有很多工具可以自动完成，这里不再多说。
    
    有一点要着重说明的是，对实体类，只要给出这个图，然后用工具生成对应的Java类代码，这些类的代码就算是完成了，以后不用再在其中写代码了。

3. 透明的存储

    对开发人员来说，主要工作集中在业务逻辑的实现上，这就需要写一些控制类，来实现这些逻辑。这些控制类一般可以 XxxSession的方式来命名，表示面向某一类使用者的控制类，比如MemberSession，完成会员登录后的一些功能； AdminSession用于完成管理员登录后的一些功能。
    
    在这些控制类中的一个方法中，只需要通过JDO规范的接口类（javax.jdo.*）来获取对前面的实体的访问，从而完成业务功能。一个典型的方法如下：
    
    MemberSession的发表主题贴的方法：
    
    ```
    public Topic postTopic(String title,String content, String forumId) {
           //业务逻辑过程开始
           javax.jdo.PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
           pm.currentTransaction().begin();
    
           //先生成一个主题，设置基本属性
           Topic topic = new Topic();
           topic.setTitle(title);
           topic.setContent(content);
           topic.setPostTime(new Date());
    
           //获取相关的论坛和当前登录的会员
           //下面用到的this.logonMemberId是本MemberSession对象生成时必须提供的会员标识。
           //本MemberSession对象一般是在登录的时候生成的。
           Forum forum = (Forum)pm.getObjectById(pm.newObjectIdInstance(Forum.class,forumId));
           Member author = (Member)pm.getObjectById(pm.newObjectIdInstance(Member.class, this.logonMemberId));
    
           //设置该主题的论坛和作者
            topic.setForum(forum);
            topic.setAuthor(author);
    
            //标记为需要存储
            pm.makePersistent(topic);
    
            //顺便更改论坛和作者的一些相关属性
            forum.setTopicCount(forum.getTopicCount()+1);
            author.setPostCount(author.getPostCount()+1);
    
            //业务逻辑过程完成
            pm.currentTransaction().commit();
            pm.close();
    }
    ```
    这样，这个方法就算写完了。我们可以看到，只要将与实体类相关的代码放在pm.currentTransaction()的开始和提交之间就可以了。
    
    唯 一中间需要与JDO打交道的就是对新生成的对象（topic）需要调用一下pm.makePersistent()，但实际上在很多情况下，只要从pm中 取出的对象指向这个对象（比如：author.getPostTopics().add(topic)），就根本不需要这条语句（当然写上也没错），因为 pm会根据可达性（Reachability）的原则将当前已经在数据库中的对象能直接或间接指到的新生成的那些对象都存储起来。
    
    以上的代码说明了我们不必对每个发生变化的对象调用更新函数，因为JDO的pm会自动跟踪这些变化，并将确实发生改变的对象同步到数据库。这就是“透明的存储”。

4. 灵活的查询：JDOQL vs SQL
    
    JDOQL是JDO中使用的查询语言，是对象式的查询语言，很象OQL，也很象EJBQL，但没有EJBQL那种只能静态存在的缺点。
    
    对象式查询语言的优点有很多文章都有介绍，这里不再说明。只说明一点：JDOQL完全基于UML实体类图，不必理会具体数据库中的任何内容。
    下面举一些例子，说明这种灵活性。
    
    **4.1 例：查找某作者发表过贴子的所有论坛**
    
    我们给出的参数只有作者的姓名，希望得到的是所有的他发表过主题或回复过主题的论坛。我们需要这样的JDOQL条件：首先查询的目标是Forum类，然后是JDOQL的过滤串
    ```
    this == _topic.forum && (_topic.author.name == “<作者姓名>” || _topic.contains(_reply) && _reply.author.name == “<作者姓名>”)
    ```
    然后，声明用到的变量：Topic _topic; Reply _reply;
    再执行SQL即可。
    
    一般的JDO产品会将这个查询尽可能优化地翻译为：
    ```
    select a.<可预定义的最常用字段组> from FORUM a, TOPIC b, REPLY c, MEMBER d
    where a.FORUM_ID = b. FORUM_ID and (b.MEMBER_ID = d. MEMBER_ID and d.NAME=’<作者姓名>’ or b.TOPIC_ID = c. TOPIC_ID and c.MEMBER_ID = d.MEMBER_ID and d.NAME = ‘<作者姓名>’)
    ```
    从上面，我们可以看到，JDOQL无论在可读性还是可维护性上都远远好于SQL。我们还可以将作者姓名作为一个绑定参数，这样会更简单。
    
    如果直接操作SQL的话会变得很麻烦，一方面要注意实体类中的属性名，一方面又要注意在数据库中的对应字段，因为多数情况下，两者的拼写由于各种因素（如数据库关键字冲突等）会是不一样的。
    从这个例子扩展开去，我们可以进一步：
    
    **4.2 例：查找某作者发表过贴子的所有论坛中，总贴数大于100并且被作者收入自己的收藏夹的那些论坛**
    
    很简单，将过滤串这样写：
    ```
    //直接使用对象属性查询：postCount > 100
    this == _topic.forum && (_topic.author == _author || _topic.contains(_reply) && _reply.author == _author) && _author.name == ‘<作者姓名>’ && postCount > 100 && _author.favoriteForums.contains(this)
    ```
    这一次多了一个用到的变量：Member _author。其底层的SQL大家可以自己去模拟。
    
5. 长字符串

    我们经常会遇到用户输入的某个信息文字串超出了规定的数据字段的大小，导致很麻烦的处理，尤其是一些没有必要限制长度的字符串，比如一篇主题文章的内容，有可能几万字，这迫使我们将其分作很多子记录，每条子记录中放一部分。所有这些，都使我们的代码量加大，维护量加大。
    
    现在有了JDO，我们的代码就简单多了，我们可能尽量利用JDO提供的透明存储功能，通过一些简单的工具类实现：原理是将其分割为字符串子串。
    
    ```
    package jdo_util;
    import java.util.*;
    
    public class StringHelper {
        public static List setLongString(String value) {
            if(value == null) return null;
            int len = value.length();
            int count = (len+partSize-1)/partSize;
            List list = new ArrayList(count);
            for(int i = 0; i < count; i++) {
                int from = i*partSize;
                list.add(value.substring(from,Math.min(from+partSize,len)));
            }
            return list;
        }
    
        public static String getLongString(List list) {
            if(list == null) return null;
            StringBuffer sb = new StringBuffer();
            for(Iterator itr = list.iterator(); itr.hasNext(); ) sb.append(itr.next());
            s = sb.toString();
            return s;
        }
    
        private static int partSize = 127; //字符串片断的大小。针对不同的数据库可以不同，如Oracle用2000
    }
    ```
    有了这个类以后，我们只需要将Topic.content的类型换成List，而其访问器的接口不变，仍是String，只是内容变一下：（并在JDO描述符中指明该List的元素类型是String）
    ```
    public class Topic {
        …
        List content; //原先是String类型
        …
        public String getContent() {
            return StringHelper.getLongString(content);
        }
    
        public void setContent(String value) {
            content = StringHelper.setLongString(value);
        }
    }
    ```
    最后，唯一的缺陷是对内容进行关键字查询的时候需要将
    ```
    content.startsWith(‘%<关键字>’)
    ```
    变为
    ```
    content.contains(s) && s.startsWith(‘%<关键字>’)
    ```
    并且，可能查询结果不太准（比如正好跨越两个子串部分）。庆幸的是，一般这种对很长的字符串字段的查询需求不是太多。
    
    需要说明的是，采用传统的SQL同样也会需要对拆分的字符串进行额外的查询，并具有同样的缺点。
    另外，这个功能需要JDO产品支持规范中的一个可选选项：javax.jdo.option.List，主要的几个JDO产品都支持。比如KodoJDO和JDOGenie。
    
6. 资源回收：pm.close()
    
    我们采用传统SQL写代码时，最危险的就是资源释放问题，这在基于WEB的应用中尤其重要。因为与JDBC相关的资源不是在Java虚拟机中分配的，而是在系统底层分配的，Java的垃圾回收机制鞭长莫及，导致系统内存慢慢耗光而死机。
    
    在JDBC中需要主动释放的资源有：Connection、Statement、PreparedStatement、ResultSet，在每个对这些类型的变量赋值的时候，都必须将先前的资源释放掉。无疑是一件繁琐而又容易被忽略的事情。
    在JDO 中，事情变得简单多了，所有的资源在pm.close()的时候会自动释放（除非JDO产品增加了一些对PreparedStatement和 ResultSet的Cache），这是JDO规范的要求。因此，只要我们记住在对实体类处理完毕时调用pm.close()就行了。比如下面的代码：
    
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
    public class PersistenceManagerRetriever extends ThreadLocal {
        /**
         * 根据配置信息初始化一个PersistenceManager获取器
         * @param p
         */
        public PersistenceManagerRetriever(java.util.Properties p) {
            pmf = JDOHelper.getPersistenceManagerFactory(p);
        }
    
        /**
         * 获取相关的PersistenceManagerFactory
         * @return 一个PersistenceManagerFactory对象
         */
        public PersistenceManagerFactory pmf() {
            return pmf;
        }
    
        /**
         * 获取一个与当前线程相关的PersistenceManager
         * @return 一个PersistenceManager对象
         */
        public PersistenceManager pm() {
            return (PersistenceManager)get();
        }
    
        /**
         * 释放所有与本线程相关的JDO资源
         */
        public void cleanup() {
            PersistenceManager pm = pm();
            if(pm == null) return;
    
            try {
                if(!pm.isClosed()) {
                    Transaction ts = pm.currentTransaction();
                    if(ts.isActive()) {
                        log.warn("发现一个未完成的Transaction ["+pmf.getConnectionURL()+"]！"+ts);
                        ts.rollback();
                    }
                    pm.close();
                }
    
            } catch(Exception ex) {
                log.error("释放JDO资源时出错："+ex,ex);
    
            } finally {
                set(null);
            }
        }
    
        public Object get() {
            PersistenceManager pm = (PersistenceManager)super.get();
            if(pm == null || pm.isClosed()) {
                pm = pmf.getPersistenceManager();
                set(pm);
                if(log.isDebugEnabled()) log.debug("retrieved new PM: "+pm);
            }
            return pm;
        }
    
        public static final Logger log = Logger.getLogger(PersistenceManagerRetriever.class);
        private PersistenceManagerFactory pmf;
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
7. ID与对象模型
    
    对象标识字段，实际上只是一个数据库范畴的字段，在对象模型中实际上是不需要这些属性的。也就是说，在Java应用中，一个对象的标识就是在内存中的地址， 并不是这个对象本身的属性，因为根据这个内存地址就可以唯一地确定这个对象。比如一个编辑矢量地图的Java程序，从文件中读入各个地图元素（对象）后， 这些对象就有了一个唯一的内存地址，所以不需要给每个对象加一个类似“ID”之类的属性并写入文件。
    
    JDO也采用了这样的概念，ID独立于对象之外，并不属于对象的一部分。前面的论坛实体类图中我们可以看到，每个类中都没有类似“id”之类的属性。那么，JDO怎样控制与数据库中的主键的对应呢？这就是两个常用的工具类方法：
    ```
    Object PersistenceManager.getObjectId(Object obj)
    Object PersistenceManager.getObjectById(Object obj, boolean validate)
    ```
    这样，可以随时获得某个实体对象的ID，也可以在任何时候通过一个ID找出该对象。第一个方法还可以用javax.jdo.JDOHelper.getObjectId()代替。
    
    JDO 规范建议的模式中，这些ID都是由JDO产品自动生成的，项目应用中只在需要传递对象的引用的时候才使用，比如在两个页面间传送。
    并且，这些ID类都是可以与String互转的，这就方便了JSP间的传递。这种由JDO产品来控制的ID叫做datastore identity，在数据表中的字段名一般是“JDO_ID”。
    如果实在是想自己控制对象在数据库中的ID，JDO也提供用户自定义的ID，这时，该ID作为对象的一个属性存在，可以是任何类型，int, Date, String, 或其它自定义的复合类型（如两个属性合起来作ID）。这种类型的ID叫做application identity。
    
    就个人而言，我建议在新的项目中采用datastore identity，这样可省下很多时间。而在实体类中，也可以写一些替代的方法来保持与application identity保持兼容，如：
    
    ```
    public class SomePersistentClass {
           …
           public String getId() {
                return JDOHelper.getObjectById(this).toString();
           }
    
           public static SomePersistentClass getById(String id) {
               PersistenceManager pm = persistenceManagerRetriever.pm();
               return pm.getObjectById(pm.newObjectIdInstance(SomePersistentClass.class, id));
           }
    }
    ```
    这种方式对两种类型的ID都有效。注意，这个类本身有这两个方法，但并没有一个ID属性。
    
8. 缓冲与Optimistic Transaction

    缓冲是JDO中的一个亮点。虽然JDO规范并没有严格要求一个JDO产品必须实现什么样的缓冲，但几乎每一个JDO产品，尤其是商业化产品，都有比较完善的缓冲体系，这个体系是不同的JDO产品相互竞争的重点之一。
    
    主要的JDO产品包含下列缓冲：
    
    1. PM连接池。对PersistenceManager进行缓冲，类似JDBC连接池，在调用pm.close()的时候并不关闭它，而是等待下一次调用或超时。
    2. PreparedStatement缓冲。如果JDO底层发现一个JDOQL语句与前面用过的某句相同，则不会重新分析并生成一个新的 PreparedStatement，而是采用缓冲池中的已有的语句。对PreparedStatement的缓冲也是JDBC3.0规范中的一项功能。 而JDO底层发现如果配置的是符合JDBC3.0规范的驱动时，会采用驱动的缓冲，否则采用自己的缓冲。
    3. ResultSet缓冲。这种缓冲的实现的JDO产品不多，目前好象只有KodoJDO 2.5.0 beta实现了。其机制是如果第二次请求执行同样JDOQL语句、同样参数的查询时，JDO底层从上一次执行结果中取出该集合，直接返回，大大增强性能。 不过比较耗资源，因为是采用JDBC2.0中的ScrollableResultSet实现。
    
    一般我们在对数据库进行更新操作时，都会对数据库进行锁定操作，设定不同的隔离级别，可以完成不同程度的锁定，比如锁记录、锁字段、锁表、锁库等等。
    而JDO中可以在具体JDO产品的厂商扩展 （Vendor Extension）标记中设定。
    另外，JDO规范还提供了一种对数据库完全没有锁定的方式： javax.jdo.option.OptimisticTransaction，它是一项可选选项，也就是说，并不强制JDO厂商实现它，不过主要的几个厂商的JDO产品都实现了这个功能。
    
    OptimisticTransaction的机制原理是：在每个对象的数据库记录中增加一个交易控制字段，然后所有的对象更改在Java虚拟机的内存中完成，
    当提交的时候，会检查每个被改过的对象在从数据库中取出后是否被其它外部程序改过，这就是通过这个控制字段完成的。一般这个字段的实现方式有以下几种：
    
    1. 存放最近一次更改的时间，字段名多取作“JDO_LAST_UPDATE_TIME”
    2. 存放历史上被更改过的次数，字段名多取作“JDO_VERSION”
    
    在OptimisticTransaction 的一次Transaction中，JDO底层不会对数据库进行锁定，这就保证了时间跨度较长的transaction不会影响其它线程（请求）的执行，只是如果更新操作比较多，访问量又比较大的话，Transaction提交失败的的几率也会相应变大。
    
9. JDBC2.0和JDBC3.0
    
    JDO只是一种对象级的包装，是建立在JDBC的基础上的，两者不能相互替代。实际上，JDBC的规范从1.0到2.0，再到3.0，一直在做功能和性能方面的改进。
    
    JDO 产品当然不会放过这些，一般的JDO产品，会检测底层配置的JDBC驱动是符合哪个规范，并会尽量采用驱动本身的功能来实现具体的操作。
    对代码开发人员来说，我们大多数情况下只能掌握JDBC1.0的操作，和少量的2.0的操作，只有一些很精通JDBC的高手才会用到JDBC3.0中的高级功能。
    因此，采用JDO也可以帮助我们在不了解JDBC3.0规范的情况下提高性能和效率。

    换句话说，JDBC技术本身就是一件很复杂的东西，要想优化性能的话，很多JDBC技术和数据库技术是需要使用的，比如inner join, left/right outer join, Batch update，等等。
    这些对开发人员的技术要求很高，一方面要精确理解每种技术的应用范围和实际使用的注意事项，另一方面代码也会比较复杂。
    因此，既然有众多的有经验的JDO厂商在做这些事情，我们又何必再花功夫呢？
    
    以上我介绍了JDO对我们的数据库项目开发的比较明显的几个好处，以后的文章中，我会继续写关于JDO使用中的概念性的问题和具体JDO产品的配置与使用，以及一些技巧。
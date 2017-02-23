Spring框架提供了一种设计系统的机制，帮助您组件模块化，从而使组件更易于测试。 
框架的关键是您将业务逻辑对象和数据访问对象设计为Java bean，然后在它们之间提供依赖关系映射。 
从而形成具有可插拔特性的结构化系统。

**最重要的是你必须使用Spring 2.0或更高版本。**

Spring框架适用于各种架构，并且可以在系统的离散部分以及整个系统中使用。 
让我们举一个例子，我们想要使用Spring用于系统的业务逻辑层和数据访问层。
在我们的系统中，我们有一个典型的业务逻辑对象SampleService，它使用一个数据访问对象SampleDAO。 
我们将它们定义为Java Beans（默认构造函数，以及带有getters / setters的属性）。 
一旦我们定义了我们的bean，然后定义“glue”将它们链接在一起。 这通过XML配置文件执行。 
在这方面有很多方法可以利用Spring。 这里是我们的定义，使用通常是最简单的模式。 
这个文件用于创建一个ApplicationContext，它在启动时自动加载所有这些bean。

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN"
    "http://www.springframework.org/dtd/spring-beans.dtd">
<beans>

    <!-- PMF Bean -->
    <bean id="pmf"
        class="org.springframework.orm.jdo.LocalPersistenceManagerFactoryBean">
        <property name="jdoProperties">
            <props>
                <prop key="javax.jdo.PersistenceManagerFactoryClass">
                    org.datanucleus.api.jdo.JDOPersistenceManagerFactory</prop>
                <prop key="javax.jdo.option.ConnectionURL">jdbc:mysql://localhost/dbname</prop>
                <prop key="javax.jdo.option.ConnectionUserName">username</prop>
                <prop key="javax.jdo.option.ConnectionPassword">password</prop>
                <prop key="javax.jdo.option.ConnectionDriverName">com.mysql.jdbc.Driver</prop>
            </props>
        </property>
    </bean>
        
    <!-- Transaction Manager for PMF -->
    <bean id="jdoTransactionManager" class="org.springframework.orm.jdo.JdoTransactionManager">
        <property name="persistenceManagerFactory">
            <ref local="pmf"/>
        </property>
    </bean>

    <!-- Typical DAO -->
    <bean id="sampleDAO" class="org.jpox.spring.SampleDAO">
        <property name="persistenceManagerFactory">
            <ref local="pmf"/>
        </property>
    </bean>

    <!-- Typical Business Service -->
    <bean id="sampleService" class="org.jpox.spring.SampleService">
        <property name="sampleDAO">
            <ref local="sampleDAO"/>
        </property>
    </bean>

    <!-- Transaction Interceptor for Business Services -->
    <bean id="transactionInterceptor" 
        class="org.springframework.transaction.interceptor.TransactionProxyFactoryBean">
        <property name="transactionManager">
            <ref local="jdoTransactionManager">
        </property>
        <property name="target">
            <ref local="sampleService">
        </property>
        <property name="transactionAttributes">
            <props>
                <prop key="store*">PROPAGATION_REQUIRED</prop>
                <prop key="delete*">PROPAGATION_REQUIRED</prop>
                <prop key="*">PROPAGATION_REQUIRED,readOnly</prop>
            </props>
        </property>
    </bean>
</beans>
```
这里我们定义了SampleService，并依赖SampleDAO。 
反过来，SampleDAO依赖于PersistenceManagerFactory。 
我们选择使用Spring的事务处理能力，因此我们为PersistenceManagerFactory定义了一个JdoTransactionManager，
此外，我们在SampleService周围定义了一个事务拦截器，它将我们的事务与Spring的事务耦合。

## Design of a JDO DAO
您的数据持久层的设计应该是您实际与您的JDO实现（例如DataNucleus）交互的唯一的地方。 
在DAO之外，你的Java对象仅仅是Java对象。 这允许您将数据持久性选择的影响划分开，并为您的系统的其余部分留下一个干净的接口。 
它为您提供了在将来更换新的数据持久策略的灵活性。

JDO对数据持久性的选择意味着该层中的某些操作，以便提供干净的接口。 
JDO 2.0规范提供了两个重要的更改，使这更简单，即附加/分离功能和使用获取组。 举一个使用JDO的DAO示例:

```
public class SampleDAO extends JdoDaoSupport {

    /** Accessor for a collection of objects */
    public Collection getWorkers() throws DataAccessException {
        
        Collection workers = getJdoTemplate().find(Worker.class, null, 
                    "lastName ascending");
                workers = getPersistenceManager().detachCopyAll();
                return workers;
    }
    
    /** Accessor for a specified object */
    public Worker loadWorker(long id) throws DataAccessException {
        
        Worker worker =
                    (Worker)getJdoTemplate().getObjectById(Worker.class, new Long(id));
                if (worker == null)
                {
                    throw new RuntimeException("Worker " + id + " not found");
                }
                return (Worker) getPersistenceManager().detachCopy(worker);
    }
    
    /** Save/Update an object */
    public void storeWorker(Worker worker) throws DataAccessException {
        getJdoTemplate().makePersistent(worker);
    }
    
    /** Delete an object. */
    public void deleteWorker(Worker worker) throws DataAccessException {
        if (worker == null || worker.getId() == null) {
            throw new RuntimeException("Worker is not persistent");
        } else {
            getPersistenceManager().deletePersistent(worker);
        }
    }
}
```
## 1、消息队列需提供哪些功能？
在功能设计上，我崇尚奥卡姆剃刀法则。
对于消息队列，只需要两个方法： `生产` 和 `消费`。
具体的业务场景是任务队列，代码设计如下：
```
public abstract class TaskQueue{
    private final String name ;
    public String getName(){return this.name;}

    public abstract void addTask(Serializable taskId);
    public abstract Serializable popTask();
}
```
同时支持多个队列，每个队列都应该有个名字。final确保TaskQueue是线程安全的。TaskQueue的实现类也应该确保线程安全。

addTask向队列中添加一个任务。队列中仅保存任务的id，不存储任务的业务数据。

popTask从队列中取出一个任务来执行。
这种设计不是特别友好，因为她需要调用者自行保证任务执行成功，如果执行失败，自行确保重新把任务放回队列。 无论如何，这种机制是可以工作的。想想奥卡姆剃刀法则，我们先按照这个设计实现出来看看。
如果调用者把业务数据存在数据库中，业务数据中包含“状态“列，标识任务是否被执行，调用者需要自行管理这个状态，并控制事务。

**popTask采用阻塞方式，还是非阻塞方式呢？**
如果采用阻塞方式，队列中没任务的时候，客户端不会断开连接，只是等。
一般情况下，客户端会有多个worker抢着干活儿，几条狼一起等一个肉包子，画面太美。连接是重要资源，如果一直没活儿干，先放回池里，也不错。
先采用非阻塞的方式吧，如果队列是空的，popTask返回null，立即返回。

## 2、后续可能提供的功能

### 2.1、引入Task生命周期概念
应用场景不同，需求也不同。
在严格的应用场景中，需要确保每个Task执行“成功“了。
对于上面提到的popTask后不管的“模式“，这是另外一种“运行模式“，两种模式可以并行存在。

在这种新模式下，Task状态有3种：新创建（new，刚调用addTask加到队列中）、正在执行（in－process，调用popTask后，调用finishTask前）、完成（done，执行OK了，调用finishTask后）。
调整后的代码如下：
```
public abstract class TaskQueue{

    private final String name ;
    public String getName(){return this.name;}

    public abstract int getMode();

    public abstract void addTask(Serializable taskId);
    public abstract Serializable popTask();
    public abstract void finishTask(Serializable taskId);
}
```

### 2.2、增加批量取出任务的功能
popTask()一次取出一个任务，太磨叽了。
好比我们要买5瓶水，开车去超市买，每去一次买1瓶，有点儿傻。
我们需要一个一次取多个任务的方法。
```
public abstract class TaskQueue{
    ... ...
    public abstract Serializable[] popTasks(long cnt);
}
```

### 2.3、增加阻塞等待机制
想象一种场景：
小明同学，取出一个任务，发现干不了，放回队列，再去取，取出来发现还是干不了，又放回去。反反复复。
小明童鞋肿么了？可能是他干活需要网络，网络断了。可能是他做任务需要写磁盘，磁盘满了。

如果小明像邻居家的孩子一样优秀，当他发现哪里不对的时候，他应该冷静下来，歇会儿。

但他万一不是呢？只有我们能帮他了。

假如队列中有10000个待办任务。
这时候小明来了。他失败100次后，我们应该拦他吗？不应该，除非他主动要求（在系统参数中配置）。5000次后呢？也不应该，除非他主动要求。我们的原则是：我们做的所有事情，对于调用者，都是可以预期的。

我们可以在系统参数中要求调用者设置一个阀值N，如果不设置，默认为100。连续失败N次后，让调用者睡一会儿，睡多长时间，让调用者配置。

假如我们的底层实现中包含待办子队列、重做子队列和完成子队列（这种设计好复杂！pop的时候先pop重做，还是先pop待办，复杂死了！但愿不需要这样）。
待办子队列中有10000个任务。

在小明失败10000次后，所有的任务都在重做子队列了。这时候我们应该拦他吗？
重做子队列要不要设置大小，超过之后，让下一个访问者等。
等的话就会涉及超时，超时后，任务也不能丢弃。
太复杂了！设置一个连续失败次数的限制就够了！

### 2.4、考虑增加Task类
**不保存任务的相关数据**是**基本原则**，绝对不动摇。
增加Task类可以管理下生命周期，更有用的是，可以把Task本身设计成Listener，代码大概时这样的：
```
public abstract class Task{

    public Serializable getId();
    public int getState();

    pubic void doTask();

    public void whenAdded(final TaskQueue tq);
    public void whenPoped(final TaskQueue tq);
    // public void whenFaild(final TaskQueue tq);
    public void whenFinished(final TaskQueue tq);
}
```
通过Task接口，我们可以对调用过程进行更强势的管理（如进行事务控制），对调用者施加更强的控制，
用户也可以获得更多的交互机会，同TaskQueue有更好的交互（如在whenFinished中做持久化工作）。

但这些真的有必要吗？是不是太侵入了？注解的方式会好些吗？
再考虑吧。

### 2.5、增加系统参数
貌似需要个Config类了，不爽！
本来想做一个很小很精致的小东西的，如果必须再加吧。
如果做的话，需要支持properties、注解设置、api方式设置、Spring注入式设置，烦。

次回预告：Redis本身机制和TaskQueue的契合。
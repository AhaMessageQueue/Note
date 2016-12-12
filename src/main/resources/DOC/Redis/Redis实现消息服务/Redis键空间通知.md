# 需求分析
- 设置了生存时间的Key，在过期时能不能有所提示？
- 如果能对过期Key有个监听，如何对过期Key进行一个回调处理？
- 如何使用 Redis 来实现定时任务？

# 序言
本文所说的定时任务或者说计划任务并不是很多人想象中的那样，比如说每天凌晨三点自动运行起来跑一个脚本。这种都已经烂大街了，随便一个 Crontab 就能搞定了。
这里所说的定时任务可以说是计时器任务，比如说用户触发了某个动作，那么从这个点开始过二十四小时我们要对这个动作做点什么。那么如果有 1000 个用户触发了这个动作，就会有 1000 个定时任务。于是这就不是 Cron 范畴里面的内容了。
举个最简单的例子，一个用户推荐了另一个用户，我们定一个二十四小时之后的任务，看看被推荐的用户有没有来注册，如果没注册就给他搞一条短信过去。

# Redis介绍
在 Redis 的 2.8.0 版本之后，其推出了一个新的特性——键空间消息（Redis Keyspace Notifications），它配合 2.0.0 版本之后的 SUBSCRIBE 就能完成这个定时任务

的操作了，不过定时的单位是秒。

### Publish / Subscribe
Redis 在 2.0.0 之后推出了 Pub / Sub 的指令，大致就是说一边给 Redis 的特定频道发送消息，另一边从 Redis 的特定频道取值——形成了一个简易的消息队列。

### Redis Keyspace Notifications
在 Redis 里面有一些事件，比如键到期、键被删除等。然后我们可以通过配置一些东西来让 Redis 一旦触发这些事件的时候就往特定的 Channel 推一条消息。
大致的流程就是我们给 Redis 的某一个 db 设置过期事件，使其键一旦过期就会往特定频道推消息，我在自己的客户端这边就一直消费这个频道就好了。
以后一来一条定时任务，我们就把这个任务状态压缩成一个键，并且过期时间为距这个任务执行的时间差。那么当键一旦到期，就到了任务该执行的时间，Redis 自然会把过期消息推去，我们的客户端就能接收到了。这样一来就起到了定时任务的作用。

# Key过期事件的Redis配置
这里需要配置 notify-keyspace-events 的参数为 “Ex”。x 代表了过期事件。notify-keyspace-events "Ex" 保存配置后，重启Redis服务，使配置生效。
重启Reids服务器：
```
root@iZ23s8agtagZ:/etc/redis# service redis-server restart redis.conf
Stopping redis-server: redis-server.
Starting redis-server: redis-server.
```

添加过期事件订阅 开启一个终端，redis-cli 进入 redis 。开始订阅所有操作，等待接收消息。
```
tinywan@iZ23a7607jaZ:~$ redis-cli -h 151.41.38.209 -p 63789
221.141.218.123:63789> psubscribe __keyevent@0__:expired
Reading messages... (press Ctrl-C to quit)
1) "psubscribe"
2) "__keyevent@0__:expired"
3) (integer) 1
```

再开启一个终端，redis-cli 进入 redis，新增一个 20秒过期的键：
```
121.41.188.109:63789> SETEX coolName 123 20
OK
121.41.188.109:63789> get coolName
"20"
121.41.188.109:63789> ttl coolName
(integer) 104
```

另外一边执行了阻塞订阅操作后的终端，20秒过期后有如下信息输出：
```
121.141.188.209:63789> psubscribe __keyevent@0__:expired
Reading messages... (press Ctrl-C to quit)
1) "psubscribe"
2) "__keyevent@0__:expired"
3) (integer) 1
1) "pmessage"
2) "__keyevent@0__:expired"
3) "__keyevent@0__:expired"
4) "coolName"
```
说明：说明对过期Key信息的订阅是成功的。

====================================================================================
# Redis的Keyspace notifications功能初探
本文出处：http://blog.csdn.NET/chaijunkun/article/details/27361453 ，转载请注明。
由于本人不定期会整理相关博文，会对相应内容作出完善。因此强烈建议在原始出处查看此文。

最近在做一套系统，其中要求若干个Worker服务器将心跳信息都上报给中央服务器。当一定时间中央服务器没有得到心跳信息时则认为该Worker失效了，发出告警。

满足这种需求的解决方法多种多样，我开始想到了memcache，上报一次心跳信息就刷新一次缓存，当缓存内心跳信息对象超时被删除，即认为对应的Worker失效。然而由于memcache的工作原理，删除都是被动的，我们无法及时判断数据是否过期，即便知道了数据过期，也没有一种机制来回调方法来执行自定义的处理动作。难道缓存架构就真的不行了吗？

答案是否定的。在Redis 2.8.0版本起，加入了“Keyspace notifications”（即“键空间通知”）的功能。如何理解该功能呢？我们来看下官方是怎么说的：
键空间通知，允许Redis客户端从“发布/订阅”通道中建立订阅关系，以便客户端能够在Redis中的数据因某种方式受到影响时收到相应事件。
可能接收到的事件举例如下：
影响一个给出的键的所有命令（会告诉你哪个键被执行了一个命令，这个命令是什么）；
接收到了一个LPUSH操作的所有键（LPUSH命令：key v1 [v2 v3..]将指定的所有值从左到右进行压栈操作，形成一个栈，并将该栈命名为指定的key）；
在数据库0中失效的所有键（不一定非得是数据库0，这里这样表述其实想表达可以知道影响的哪个数据库）。

看到这里我联想到，如果一条缓存数据失效了，通过订阅关系，客户端会收到消息，通过分析消息可以得知何种消息，分析消息内容可以知道是哪个key失效了。这样就可以间接实现开头所描述的功能。

接下来我们来看下实验的步骤：
### 准备redis服务器
作为开源软件，redis下载后得到的是源代码，使用tar -xzvf redis-2.8.9.tar.gz解压后对其进行编译，过程也很简单，make就可以了。编译完成之后可以使用自带的runtest进行测试，看是否编译成功。然后就是安装了，执行make PREFIX=/usr/local/redis-2.8.9 install，PREFIX参数指定的就是安装路径。现在安装的只有可执行文件，还没有配置文件。其实在源码目录中有一个模板redis.conf，我们对它进行改动就可以了。
其他配置我们不关心，但是官方文档中说Keyspace notifications功能默认是关闭的（默认地，Keyspace 时间通知功能是禁用的，因为它或多或少会使用一些CPU的资源），我们需要打开它。打开的方法也很简单，配置属性：notify-keyspace-events

默认配置是这样的：notify-keyspace-events ""
根据文档中的说明：
```
K     Keyspace events, published with __keyspace@<db>__ prefix.
E     Keyevent events, published with __keyevent@<db>__ prefix.
g     Generic commands (non-type specific) like DEL, EXPIRE, RENAME, ...
$     String commands
l     List commands
s     Set commands
h     Hash commands
z     Sorted set commands
x     Expired events (events generated every time a key expires)
e     Evicted events (events generated when a key is evicted for maxmemory)
A     Alias for g$lshzxe, so that the "AKE" string means all the events.
```
我们配置为：notify-keyspace-events Ex，含义为：发布key事件，使用过期事件（当每一个key失效时，都会生成该事件）。

### 准备客户端和连接配置
本文中使用的客户端是Jedis，版本为2.4.2，为了代码的通用性，我使用spring来管理连接：
```
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext*.xml")
public class RedisSubscribeDemo {

	private static final Log Log= LogFactory.getLog(RedisSubscribeDemo.class);

	@Resource
	private JedisPool pool;

	@Test
	public void doTest() throws InterruptedException {
		for (int i = 0; i < 1; i++) {
			TestThread thread= new TestThread(pool);
			thread.start();
		}
		Thread.sleep(50000L);
		Log.info("Test finish...");
    }
}
```

然后使用Spring Test和Junit来测试代码
```
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext*.xml")
public class RedisSubscribeDemo {

	private static final Log Log= LogFactory.getLog(RedisSubscribeDemo.class);

	@Resource
	private JedisPool pool;

	@Test
	public void doTest() throws InterruptedException {
		for (int i = 0; i < 1; i++) {
			TestThread thread= new TestThread(pool);
			thread.start();
		}
		Thread.sleep(50000L);
		Log.info("Test finish...");
    }
}
```
由于要使用一定的延迟，我们把主要测试代码放到了TestThread中。当测试线程启动后，主线程停滞50秒，让我们有足够的时间来操作。
```
public class TestThread extends Thread {

	private Log log= LogFactory.getLog(TestThread.class);

	private JedisPool pool;

	public TestThread(JedisPool pool){
		log.info("loading test thread");
		this.pool= pool;
	}

	@Override
	public void run() {
		Jedis jedis= pool.getResource();
		jedis.psubscribe(new MySubscribe(), "*");
		try {
			Thread.sleep(10000L);
		} catch (InterruptedException e) {
			log.info("延时失败", e);
		}
		jedis.close();
		log.info("Test run finished");
	}
}
```
在测试线程中，我们将自定义的MySubscribe加入到了Jedis的模板订阅（即psubscribe，因为模板订阅的channel是支持星号'*'通配的，这样可以收集到多个通配通道的消息，而与之相反的还有一个subscribe，此订阅只能指定严格匹配的通道）中，同样为了测试过程能够将结果显示出来，在绑定了订阅后，对该线程进行了延时10秒。

```
public class MySubscribe extends JedisPubSub {

	private static final Log log= LogFactory.getLog(MySubscribe.class);

	// 初始化按表达式的方式订阅时候的处理
	public void onPSubscribe(String pattern, int subscribedChannels) {
    		log.info(pattern + "=" + subscribedChannels);
	}

	// 取得按表达式的方式订阅的消息后的处理
	public void onPMessage(String pattern, String channel, String message) {
    		log.info(pattern + "=" + channel + "=" + message);
	}

	...其他未用到的重写方法忽略
}
```

作为Jedis自定义订阅，必须继承redis.clients.jedis.JedisPubSub类，在psubscribe模式下，重点重写onPMessage方法，该方法为接收到模板订阅后处理事件的重要代码。pattern为在绑定订阅时使用的通配模板，channel为通配后符合条件的实际通道名称，message就不用多说了，就是事件消息内容。

### 实战
通过Redis自带的redis-cli命令，我们可以在服务端通过命令行的方式直接操作。我们运行上面的示例代码，然后迅速切换到redis-cli命令中，建立一个生命周期很短暂的数据：

```
127.0.0.1:6379> set chaijunkun 123 PX 100
```

PX参数指定生命周期单位为毫秒，100即声明周期，即100毫秒。key为chaijunkun的数据，其值为123。
当执行语句后，回显：
```
OK
```
这时我们看实例程序的输出：
```
*=__keyevent@0__:expired=chaijunkun
```
从输出可以看出，之前指定的通配符为*，通配任何通道；之后是实际的通道名称：__keyevent@0__:expired，这里我们可以看到订阅收到了一个keyevent位于数据库0，事件类型为：expired，是一个过期事件；最后是chaijunkun，这个是过期数据的key。
在官方文档中，keyevent通道的格式永远是这样的：
`__keyevent@<db>__:prefix`
对于数据过期事件，我们在绑定订阅时通配模板也可以精确地写成：
`__keyevent@*__:expired`
通过示例代码，我们可以看到确实印证了之前的构想，实现了数据过期的事件触发（event）或者说回调（callback）

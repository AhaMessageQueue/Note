>注意:以下方式实际上是采用的PUB/SUB方式(发布订阅)

### Redis实现消息服务原理
发布者和订阅者模式(PUB/SUB)：发布者发送消息到TOPIC，每个订阅者都能收到一样的消息。
生产者和消费者模式(P2P)：生产者将消息放入QUEUE，多个消费者共同监听，谁先抢到资源，谁就从队列中取走消息去处理。注意，每个消息只能最多被一个消费者接收。

#### 引入Maven依赖
引入Redis相应的maven依赖，这里需要spring-data-redis和jedis
pom.xml:
```
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-redis</artifactId>
    <version>1.6.0.RELEASE</version>
</dependency>

<!-- jedis -->
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>2.5.1</version>
</dependency>
```
#### 配置redis生产者消费者模式
applicationContext-redis.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:mvc="http://www.springframework.org/schema/mvc"
    xmlns:tx="http://www.springframework.org/schema/tx" xmlns:context="http://www.springframework.org/schema/context"
    xmlns:aop="http://www.springframework.org/schema/aop" xmlns:cache="http://www.springframework.org/schema/cache"
    xmlns:redis="http://www.springframework.org/schema/redis"
    xsi:schemaLocation="http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc-4.0.xsd
                            http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-34.0.xsd
                            http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
                            http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.0.xsd
                            http://www.springframework.org/schema/aop  http://www.springframework.org/schema/aop/spring-aop.xsd
                            http://www.springframework.org/schema/cache http://www.springframework.org/schema/cache/spring-cache-4.0.xsd
                            http://www.springframework.org/schema/redis http://www.springframework.org/schema/redis/spring-redis-1.0.xsd">

<description>spring-data-redis配置</description>

<!-- jedis -->
<bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
    <property name="maxIdle" value="300" /> <!-- 最大能够保持idel状态的对象数  -->
    <property name="maxTotal" value="60000" /> <!-- 最大分配的对象数 -->
    <property name="testOnBorrow" value="true" /> <!-- 当调用borrow Object方法时，是否进行有效性检查 -->
</bean>

<bean id="jedisPool" class="redis.clients.jedis.JedisPool">
    <constructor-arg index="0" ref="jedisPoolConfig" />
    <constructor-arg index="1" value="${redis.host}" />
    <constructor-arg index="2" value="${redis.port}" type="int" />
</bean>


<!-- ConnectionFactory -->
<bean id="redisConnectionFactory"
    class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory">
    <property name="hostName" value="${redis.host}"></property>
    <property name="port" value="${redis.port}"></property>
    <property name="usePool" value="true"></property>
    <property name="poolConfig" ref="jedisPoolConfig"></property>
</bean>
<!-- RedisTemplate -->
<bean id="redisTemplate" class="org.springframework.data.redis.core.RedisTemplate">
    <property name="connectionFactory" ref="redisConnectionFactory"></property>
</bean>




<!-- 序列化 -->
<bean id="jdkSerializer"
    class="org.springframework.data.redis.serializer.JdkSerializationRedisSerializer" />
<!-- MessageListenerAdapter -->
<bean id="smsMessageListener"
    class="org.springframework.data.redis.listener.adapter.MessageListenerAdapter">
    <property name="delegate" ref="smsMessageDelegateListener" />
    <property name="serializer" ref="jdkSerializer" />
</bean>
<!-- 消息监听 -->
<!--
    topic就是订阅的channel频道,是有发布到java这个channel的消息才会被接收.
    本文采用的方式是发布订阅,而非队列
 -->
<redis:listener-container>
    <redis:listener ref="smsMessageListener" method="handleMessage"
        serializer="jdkSerializer" topic="sms_queue_web_online" />
</redis:listener-container>



<!-- 自定义消息生产者,发送消息 -->
<bean id="sendMessage" class="com.djt.common.cache.redis.queue.SendMessage">
    <property name="redisTemplate" ref="redisTemplate"/>
</bean>
```

**主要的配置说明：**
序列化：一般我们向Redis发送一个消息定义的Java对象，这个对象需要序列化。这里使用JdkSerializationRedisSerializer：
```
<bean id="jdkSerializer" class="org.springframework.data.redis.serializer.JdkSerializationRedisSerializer" />
```

**发送者:**
```
<bean id="sendMessage" class="com.djt.common.cache.redis.queue.SendMessage">
    <property name="redisTemplate" ref="redisTemplate"/>
</bean>
```

**监听者:**
```
<bean id="smsMessageListener"
    class="org.springframework.data.redis.listener.adapter.MessageListenerAdapter">
    <property name="delegate" ref="smsMessageDelegateListener" />
    <property name="serializer" ref="jdkSerializer" />
</bean>
<redis:listener-container>
    <redis:listener ref="smsMessageListener" method="handleMessage"
        serializer="jdkSerializer" topic="sms_queue_web_online" />
</redis:listener-container>
```
smsMessageListener：消息监听器
redis:listener-Container：定义消息监听，method：监听消息执行的方法，serializer：序列化，topic：监听TOPIC

#### 代码实现
###### 定义短信消息对象SmsMessageVo
```
public class SmsMessageVo implements Serializable {
    //id
    private Integer smsId;

    //手机号
    private String mobile;

    //类型，1：验证码 2：订单通知
    private Byte type;

    //短信创建时间
    private Date createDate;

    //短信消息处理时间
    private Date processTime;

    //短信状态，1：未发送 2：发送成功 3：发送失败
    private Byte status;

    //短信内容
    private String content;

    //省略setter和getter方法
    ...
```

##### 定义发送对象SendMessage
```
//SendMessage.java
public class SendMessage {

    private RedisTemplate<String, Object> redisTemplate;


    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }



    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }



    public void sendMessage(String channel, Serializable message) {
        redisTemplate.convertAndSend(channel, message);
    }
}
```

##### 发送消息
```
 String smsContent = templateToContent(template.getContent(),
                    regMsgCode);
    SmsMessageVo smsMessageVo = new SmsMessageVo();
    smsMessageVo.setMobile(mobile);
    smsMessageVo.setType((byte) SmsType.VERIFICATION.getType());
    smsMessageVo.setChannelId(1);
    smsMessageVo.setContent(smsContent);
    smsMessageVo.setCreateDate(new Date());
    smsMessageVo.setStatus((byte) SmsSendStatus.TO_SEND.getType());
    smsMessageVo.setTemplateId(1);

    //先把待发送的短信存入数据库
    SmsQueue smsQueue = new SmsQueue();
    BeanUtils.copyProperties(smsQueue, smsMessageVo);
    smsQueueService.addSmsQueue(smsQueue);

    //异步发送短信到redis topic
    sendMessage.sendMessage(Constants.REDIS_QUEUE_SMS_WEB, smsMessageVo);
    //Constants.REDIS_QUEUE_SMS_WEB = "sms_queue_web_online",和applicationContext-redis中topic配置一样
```

##### 监听消息
```
//SmsMessageDelegateListener.java
@Component("smsMessageDelegateListener")
public class SmsMessageDelegateListener {

    @Autowired
    private SmsQueueService smsQueueService;

    //监听Redis消息
    public void handleMessage(Serializable message){
        if(message instanceof SmsMessageVo){
            SmsMessageVo messageVo = (SmsMessageVo) message;

            //发送短信
            SmsSender smsSender = SmsSenderFactory.buildEMaySender();
            smsSender.setMobile(messageVo.getMobile());
            smsSender.setContent(messageVo.getContent());
            boolean sendSucc = false;
            //判断短信类型
            //验证码短信
            if(messageVo.getType() == (byte)SmsType.VERIFICATION.getType()){
                sendSucc = smsSender.send();
            }


            if(!sendSucc){
                return;
            }

            // 异步更新短信表状态为发送成功
            final Integer smsId = messageVo.getSmsId();
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {
                public void run() {
                    SmsQueue smsQueue = new SmsQueue();
                    smsQueue.setSmsId(smsId);
                    smsQueue.setStatus((byte)SmsSendStatus.SEND.getType());
                    smsQueue.setProcessTime(new Date());
                    smsQueueService.updateSmsQueue(smsQueue);
                }
            });

        }
    }
}
```

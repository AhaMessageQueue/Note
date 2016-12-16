Redis中pub/sub特性，可以用来实现类似与JMS的“topic”功能，只不过这些消息无法被持久化而已。spring-data-redis组件中对pub/sub提供了类似JMS的编程模式，我们通过实例来展示如何使用。

需要注意的是，在redis中消息的订阅端(subscribe)需要独占链接，那么消息接收将是阻塞的。

代码实例中，使用了“连接池”/“消息异步接受”“消息并发处理”，请根据需要调整相关参数。

1) Redis中"pub/sub"的消息,为"即发即失",server不会保存消息,如果publish的消息,没有任何client处于"subscribe"状态,消息将会被丢弃.如果client在subcribe时,链接断开后重连,那么此期间的消息也将丢失.Redis server将会"尽力"将消息发送给处于subscribe状态的client,但是仍不会保证每条消息都能被正确接收.

2) 如果期望pub/sub的消息时持久的,那么需要借助额外的功能.参见"[pub/sub持久化订阅](http://shift-alt-ctrl.iteye.com/blog/1867454)"

### 配置文件
```
<beans xmlns="http://www.springframework.org/schema/beans"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://www.springframework.org/schema/beans
http://www.springframework.org/schema/beans/spring-beans.xsd"
default-autowire="byName">

<!--
default-autowire="byName"
byName : 根据`属性名`自动装配。
此选项将检查容器并`根据名字查找`属性完全一致的bean，并将其与属性自动装配。
 -->

<bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
    <property name="maxActive" value="32"></property>
    <property name="maxIdle" value="6"></property>
    <property name="maxWait" value="15000"></property>
    <property name="minEvictableIdleTimeMillis" value="300000"></property>
    <property name="numTestsPerEvictionRun" value="3"></property>
    <property name="timeBetweenEvictionRunsMillis" value="60000"></property>
    <property name="whenExhaustedAction" value="1"></property>
</bean>

<bean id="jedisConnectionFactory" class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory" destroy-method="destroy">
    <property name="poolConfig" ref="jedisPoolConfig"></property>
    <property name="hostName" value="127.0.0.1"></property>
    <property name="port" value="6379"></property>
    <property name="password" value="0123456"></property>
    <property name="timeout" value="15000"></property>
    <property name="usePool" value="true"></property>
</bean>

<!--
<bean id="redisTemplate" class="org.springframework.data.redis.core.StringRedisTemplate">
    <property name="connectionFactory" ref="jedisConnFactory" />
</bean>
-->
<bean id="jedisTemplate" class="org.springframework.data.redis.core.RedisTemplate">
    <property name="connectionFactory" ref="jedisConnectionFactory"></property>
    <property name="defaultSerializer">
        <bean class="org.springframework.data.redis.serializer.StringRedisSerializer"/>
    </property>
</bean>

<bean id="topicMessageListener" class="com.sample.redis.sdr.TopicMessageListener">
    <property name="redisTemplate" ref="jedisTemplate"></property>
</bean>

<bean id="topicContainer" class="org.springframework.data.redis.listener.RedisMessageListenerContainer" destroy-method="destroy">
    <property name="connectionFactory" ref="jedisConnectionFactory"/>
    <property name="taskExecutor">
        <bean class="org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler">
            <property name="poolSize" value="3"></property>
        </bean>
    </property>
    <property name="messageListeners">
        <map>
            <entry key-ref="topicMessageListener">
                <bean class="org.springframework.data.redis.listener.ChannelTopic">
                    <constructor-arg value="user:topic"/>
                </bean>
            </entry>
        </map>
    </property>
</bean>

</beans>
```

### 消息发布(pub):
```
    String channel = "user:topic";
    //其中channel必须为string，而且“序列化”策略也是StringSerializer
    //消息内容，将会根据配置文件中指定的valueSerializer进行序列化
    //本例中，默认全部采用StringSerializer
    //那么在消息的subscribe端也要对“反序列化”保持一致。
    redisTemplate.convertAndSend(channel, "from app 1");
```

### 消息接收(subscribe):
TopicMessageListener类：
```
    public class TopicMessageListener implements MessageListener {

        private RedisTemplate redisTemplate;

        public void setRedisTemplate(RedisTemplate redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        @Override
        public void onMessage(Message message, byte[] pattern) {
            byte[] body = message.getBody();//请使用valueSerializer
            byte[] channel = message.getChannel();
            //请参考配置文件，本例中key，value的序列化方式均为string。
            //其中key必须为stringSerializer。和redisTemplate.convertAndSend对应
            String itemValue = (String)redisTemplate.getValueSerializer().deserialize(body);
            String topic = (String)redisTemplate.getStringSerializer().deserialize(channel);
            //...
        }
    }
```
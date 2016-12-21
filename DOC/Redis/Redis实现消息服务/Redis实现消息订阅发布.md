>注意:以下方式实际上是采用的PUB/SUB方式(发布订阅)

### Redis实现消息服务原理
发布者和订阅者模式(PUB/SUB)：发布者发送消息到TOPIC，每个订阅者都能收到一样的消息。
生产者和消费者模式(P2P)：生产者将消息放入QUEUE，多个消费者共同监听，谁先抢到资源，谁就从队列中取走消息去处理。注意，每个消息只能最多被一个消费者接收。

### 引入Maven依赖
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

### JedisConnectionFactory配置
ApplicationContext-context.xml
```
<!-- redis -->
    <bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
        <property name="maxTotal" value="${redis.pool.maxTotal}"/>
        <property name="maxIdle" value="${redis.pool.maxIdle}"/>
        <property name="timeBetweenEvictionRunsMillis" value="${redis.pool.timeBetweenEvictionRunsMillis}"/>
        <property name="minEvictableIdleTimeMillis" value="${redis.pool.minEvictableIdleTimeMillis}"/>
        <property name="testOnBorrow" value="${redis.pool.testOnBorrow}"/>
    </bean>

    <bean id="jedisConnFactory"
          class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory">
        <property name="hostName" value="${redis.pool.host}"/>
        <property name="port" value="${redis.pool.port}"/>
        <property name="usePool" value="true"></property>
        <property name="poolConfig" ref="jedisPoolConfig"/>
    </bean>

    <!-- Redis Template -->
    <bean id="redisTemplate" class="org.springframework.data.redis.core.StringRedisTemplate">
        <property name="connectionFactory" ref="jedisConnFactory" />
    </bean>
```

### 消息订阅配置
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:redis="http://www.springframework.org/schema/redis"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans-4.1.xsd
       http://www.springframework.org/schema/redis
       http://www.springframework.org/schema/redis/spring-redis-1.0.xsd">

    <bean id="stringRedisSerializer"
          class="org.springframework.data.redis.serializer.StringRedisSerializer" />

    <bean id="remindsMessageListener"
          class="org.springframework.data.redis.listener.adapter.MessageListenerAdapter">
        <property name="delegate" ref="remindsDelegateListener" />
        <property name="serializer" ref="stringRedisSerializer" />
    </bean>
    <!-- 消息监听 -->
    <redis:listener-container connection-factory="jedisConnFactory">
        <redis:listener ref="remindsMessageListener" method="onMessage"
                        serializer="stringRedisSerializer" topic="__keyevent@*__:expired" />
    </redis:listener-container>

</beans>
```
```
package com.kingsoft.wps.calendar.message.subscribe;

import com.kingsoft.wps.calendar.api.service.RemindsService;
import com.kingsoft.wps.calendar.api.service.SubscribeService;
import com.kingsoft.wps.calendar.core.model.pojo.Reminds;
import com.kingsoft.wps.calendar.core.model.pojo.Subscribe;
import com.kingsoft.wps.calendar.message.enums.RemindsStatusEnum;
import com.kingsoft.wps.calendar.message.enums.SubChannelEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liuchunlong on 16-12-13.
 */
@Component("remindsDelegateListener")
public class RemindsDelegateListener implements MessageListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RemindsService remindsService;

    @Autowired
    private SubscribeService subscribeService;

    private final String remBarrierKeyPattern = "cal::remind::<instanceID>::<calendarID>::done";

    private static final String keyReg = "^cal::remind::(\\S+)::(\\S+)::ready$";
    private static final String channelReg = "^__keyevent@\\d+__:expired$";

    private final Logger logger = LoggerFactory.getLogger(RemindsDelegateListener.class);

    public void onMessage(Message message, byte[] pattern) {
        String key = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
        String channel = (String) redisTemplate.getStringSerializer().deserialize(message.getChannel());

        logger.info("[info-Reminds]redis推送通知,通知key:" + key);//[info-Reminds]redis推送通知,通知内容:cal::remind::<instanceID>::<calendarID>::ready
        logger.info("[info-Reminds]redis推送通知,通知channel:" + channel);//[info-Reminds]redis推送通知,通知频道:__keyevent@0__:expired

        //校验key值
        Pattern keyPattern = Pattern.compile(keyReg);
        Matcher keyMatcher = keyPattern.matcher(key);

        //校验频道
        Pattern channelPattern = Pattern.compile(channelReg);
        Matcher channelMatcher = channelPattern.matcher(channel);

        if(keyMatcher.matches() && channelMatcher.matches()) {
            //校验通过

            //获取key对应的value,即Reminds ID
            String remindsIdStr = (String) redisTemplate.opsForValue().get(key);
            long remindsId = Long.parseLong(remindsIdStr);

            //解析instanceID,calendarID
            String instanceIDStr = keyMatcher.group(1);
            String calendarIDStr = keyMatcher.group(2);
            //设置屏障,防止重复重复订阅处理
            String remBarrierKey = remBarrierKeyPattern.replace("<instanceID>", instanceIDStr).replace("<calendarID>", calendarIDStr);
            logger.info("[info-Reminds]redis推送通知, 通知屏障key:" + remBarrierKey);

            String originRemBarrier = (String) redisTemplate.opsForValue().getAndSet(remBarrierKey, remindsIdStr);
            if (!StringUtils.isEmpty(originRemBarrier)) {//如果存在该值,说明该提醒已被其他结点处理,则退出
                return;
            }
            redisTemplate.expire(remBarrierKey, 10, TimeUnit.SECONDS);

            //检查当前Reminds的提醒状态,只有是`待通知`状态才处理
            Reminds reminds = remindsService.getById(remindsId);
            if(reminds != null && reminds.getRemindState() != null &&
                    reminds.getRemindState().equals(RemindsStatusEnum.NOTREMINDS.getValue())) {

                //1.更新通知状态
                remindsService.updateRemindState(remindsId, RemindsStatusEnum.REMINDS.getValue());

                //2.获取所有订阅
                List<Subscribe> subscribeList = subscribeService.getByJustChannel(SubChannelEnum.REMINDS.getValue());

                //3.构造通知参数
                Map<String, String> params = new HashMap<String, String>();
                params.put("channel", SubChannelEnum.REMINDS.getValue());
                params.put("instanceID", instanceIDStr);
                params.put("calendarID", calendarIDStr);

                //3.通知订阅者
                MessageSender.send(subscribeList, params);
            }

        }

    }

}

```
### 消息发布配置:
```
    String channel = "user:topic";
    //其中channel必须为string，而且“序列化”策略也是StringSerializer
    //消息内容，将会根据配置文件中指定的valueSerializer进行序列化
    //本例中，默认全部采用StringSerializer
    //那么在消息的subscribe端也要对“反序列化”保持一致。
    redisTemplate.convertAndSend(channel, "from app 1");
```
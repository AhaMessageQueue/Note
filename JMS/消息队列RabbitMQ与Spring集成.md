## 1.RabbitMQ简介

RabbitMQ是流行的开源消息队列系统，用erlang语言开发。RabbitMQ是AMQP（高级消息队列协议）的标准实现。 

官网：<http://www.rabbitmq.com/>

## 2.Spring集成RabbitMQ

### 2.1 maven配置

```xml
<dependency>
  <groupId>com.rabbitmq</groupId>
  <artifactId>amqp-client</artifactId>
  <version>5.0.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.amqp</groupId>
    <artifactId>spring-rabbit</artifactId>
    <version>2.0.0.RELEASE</version>
</dependency>
```

## 2.2 rabbitmq配置文件

// rabbitmq-config.properties
```properties
rabbitmq.host=127.0.0.1
rabbitmq.username=guest
rabbitmq.password=guest
rabbitmq.port=5672
rabbitmq.vhost=fnpac-rabbitmq
```

## 2.3 Spring配置

// application-mq.xml
```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:rabbit="http://www.springframework.org/schema/rabbit" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
    http://www.springframework.org/schema/beans/spring-beans.xsd 
    http://www.springframework.org/schema/rabbit
    http://www.springframework.org/schema/rabbit/spring-rabbit.xsd">
    
    <description>rabbitmq 服务配置</description>
    
    <!-- 连接配置 -->
    <rabbit:connection-factory id="rabbitConnectionFactory" 
        host="${rabbitmq.host}" 
        username="${rabbitmq.username}" 
        password="${rabbitmq.password}" 
        port="${rabbitmq.port}"  
        virtual-host="${rabbitmq.vhost}"/>
    
    <!-- admin 负责创建rabbitmq管理组建 -->
    <rabbit:admin connection-factory="rabbitConnectionFactory"/>
    
    <!-- spring template声明-->
    <rabbit:template id="rabbitTemplate"
        exchange="rabbit.direct.exchange" 
        connection-factory="rabbitConnectionFactory" 
        message-converter="jsonMessageConverter" />
        
    <!-- 消息对象json转换类 -->
    <bean id="jsonMessageConverter" 
        class="org.springframework.amqp.support.converter.Jackson2JsonMessageConverter" />  
</beans>
```

## 3. 在Spring中使用RabbitMQ

### 3.1 申明一个消息队列Queue

//application-mq.xml
```xml
<rabbit:queue id="rabbitQueue" name="rabbit.queue" 
    durable="true" auto-delete="false" 
    exclusive="false" />
```

说明： 

1. durable:是否持久化
2. exclusive: 仅创建者可以使用的私有队列，断开后自动删除
3. auto-delete: 当所有消费客户端连接断开后，是否自动删除队列

### 3.2 Exchange定义

//application-mq.xml
```xml
<rabbit:direct-exchange id="rabbitDirectExchange" name="rabbit.direct.exchange" 
    durable="true" auto-delete="false">
    <rabbit:bindings>
        <rabbit:binding queue="rabbit.queue" key="rabbit.queue.key"/>
    </rabbit:bindings>
</rabbit:direct-exchange>
```

说明： 

1. rabbit:direct-exchange：定义exchange模式为direct，意思就是消息与一个特定的路由键完全匹配，才会转发。 
2. rabbit:binding：设置消息queue匹配的key

### 3.3 发送消息Producer

```java
public interface MQProducer {
    /**
     * 发送消息到指定队列
     * @param queueKey 队列的key
     * @param object 发送的消息
     */
    void sendDataToQueue(String queueKey, Object object);
}
```

```java
@Service
public class MQProducerImpl implements MQProducer {
    @Autowired
    private AmqpTemplate amqpTemplate;
    
    private final static Logger LOGGER = Logger.getLogger(MQProducerImpl.class);
    
    /* (non-Javadoc)
     * @see com.stnts.tita.rm.api.mq.MQProducer#sendDataToQueue(java.lang.String, java.lang.Object)
     */
    @Override
    public void sendDataToQueue(String queueKey, Object object) {
        try {
            amqpTemplate.convertAndSend(queueKey, object);
        } catch (Exception e) {
            LOGGER.error(e);
        }

    }
}
```

说明： 

convertAndSend：
将Java对象转换为消息发送到默认Exchange，由于配置了JSON转换，这里是将Java对象转换成JSON字符串的形式。
原文：Convert a Java object to an Amqp Message and send it to a default exchange with a specific routing key.

### 3.4 异步接收消息Consumer

```java
@Component
public class QueueListenter implements MessageListener {

    @Override
    public void onMessage(Message msg) {
        try{
            System.out.print(msg.toString());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
```

监听配置

//application-mq.xml

```xml
<rabbit:listener-container connection-factory="rabbitConnectionFactory" acknowledge="auto">
    <rabbit:listener queues="rabbitQueue" ref="queueListenter"/>
</rabbit:listener-container>
```

说明：

1. queues：监听的队列id，多个的话用逗号（,）分隔；
    queue-names指定队列的名称，多个的话用逗号（,）分隔
2. ref：监听器

## 1、访问Redis的工具类
```
package com.github.commons.redis.queue;

import com.github.commons.redis.queue.config.ConfigManager;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.Pool;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by 刘春龙 on 2017/3/3.
 * <p>
 * Redis连接池工具类
 */
public class RedisManager {

    public final static Logger logger = Logger.getLogger(RedisManager.class.getName());

    private static Pool<Jedis> pool;

    static {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化连接池
     */
    private static void init() throws Exception {

        Properties props = ConfigManager.getProperties("redis");
        logger.info("初始化Redis连接池。");

        if (props == null) {
            throw new RuntimeException("没有找到Redis配置文件");
        }

        //创建jedis池配置实例
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        //加载jedis池配置项
        Integer poolMaxTotal = Integer.valueOf(props.getProperty("redis.pool.maxTotal").trim());
        jedisPoolConfig.setMaxTotal(poolMaxTotal);

        Integer poolMaxIdle = Integer.valueOf(props.getProperty("redis.pool.maxIdle").trim());
        jedisPoolConfig.setMaxIdle(poolMaxIdle);

        Long poolMaxWaitMillis = Long.valueOf(props.getProperty("redis.pool.maxWaitMillis").trim());
        jedisPoolConfig.setMaxWaitMillis(poolMaxWaitMillis);

        logger.info(String.format("poolMaxTotal: %s, poolMaxIdle: %s, poolMaxWaitMillis: %s", poolMaxTotal, poolMaxIdle, poolMaxWaitMillis));


        String connectMode = props.getProperty("redis.connectMode");
        String hostPort = props.getProperty("redis.hostPort");

        logger.info(String.format("connectMode : %s, hostPort: %s.", connectMode, hostPort));

        if (StringUtils.isEmpty(hostPort)) {
            throw new RuntimeException("redis配置文件未配置主机-端口集。");
        }
        //根据配置实例化jedis池
        String[] hostPortSet = hostPort.split(",");
        if ("single".equals(connectMode)) {
            //单机连接
            String[] hostPortInfo = hostPortSet[0].split(":");
            pool = new JedisPool(jedisPoolConfig, hostPortInfo[0], Integer.valueOf(hostPortInfo[1].trim()));
        } else if ("sentinel".equals(connectMode)) {
            Set<String> sentinels = new HashSet<String>();

            for (String hostPortInfo : hostPortSet) {
                sentinels.add(hostPortInfo);
            }
            pool = new JedisSentinelPool("gmaster", sentinels, jedisPoolConfig);
        }
    }

    /**
     * 获取Jedis对象
     * 使用完成后，必须调用returnResource归还到连接池中
     *
     * @return Jedis对象
     */
    public static Jedis getResource() {
        Jedis jedis = pool.getResource();
        logger.info("获得Redis连接：" + jedis);
        return jedis;
    }

    /**
     * 获取Jedis对象
     * 使用完成后，必须调用returnResource归还到连接池中
     *
     * @param db Redis数据库序号
     * @return Jedis对象
     */
    public static Jedis getResource(int db) {
        Jedis jedis = pool.getResource();
        jedis.select(db);
        logger.info("获得Redis连接：" + jedis);
        return jedis;
    }

    /**
     * 归还Redis连接到连接池
     *
     * @param jedis Jedis对象
     */
    public static void returnResource(Jedis jedis) {
        if (jedis != null) {
            pool.returnResource(jedis);
            logger.info("归还Redis连接到连接池：" + jedis);
        }
    }

    public static void destroy() throws Exception {
        pool.destroy();
    }
}

```
## 2、队列接口
```
package com.github.commons.redis.queue;

import java.io.Serializable;

/**
 * Created by 刘春龙 on 2017/3/3.
 */
public interface TaskQueue {

    /**
     * 获取队列名
     * @return
     */
    String getName();

    /**
     * 往队列中添加任务
     * @param task
     */
    void pushTask(Serializable task);

    /**
     * 从队列中取出一个任务
     * @return
     */
    String popTask();
}

```
## 3、队列的Redis实现类
```
package com.github.commons.redis.queue;

import com.alibaba.fastjson.JSON;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Created by 刘春龙 on 2017/3/3.
 * <p>
 * 任务队列Redis实现<br/>
 * 采用每次获取Jedis并放回Pool的方式<br/>
 */
public class RedisTaskQueue implements TaskQueue {

    private static final int REDIS_DB_IDX = 0;

    public static final Logger logger = Logger.getLogger(RedisTaskQueue.class.getName());

    private final String name;

    /**
     * 构造函数
     * @param name 任务队列名称
     */
    public RedisTaskQueue(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void pushTask(Serializable task) {
        Jedis jedis = null;
        try {
            jedis = RedisManager.getResource(REDIS_DB_IDX);
            String taskValue = JSON.toJSONString((Task)task);
            jedis.lpush(this.name, taskValue);
        } catch (Throwable e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                RedisManager.returnResource(jedis);
            }
        }
    }

    @Override
    public Serializable popTask() {
        Jedis jedis = null;
        Serializable task = null;
        try{
            jedis = RedisManager.getResource(REDIS_DB_IDX);
            String taskValue = jedis.rpop(this.name);
            task = (Serializable)JSON.parseObject(taskValue, Task.class);
        } catch (Throwable e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                RedisManager.returnResource(jedis);
            }
        }
        return task;
    }
}

```
## 4、获取队列实例的工具类
```
package com.github.commons.redis.queue;

import com.github.commons.redis.queue.config.ConfigManager;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by 刘春龙 on 2017/3/3.
 * <p>
 * //获得队列
 * TaskQueue queue = TaskQueueManager.get(QUEUE_NAME);
 * <p>
 * // 添加任务到队列
 * queue.pushTask(TASK);
 * <p>
 * // 从队列取出任务
 * Task task = queue.popTask();
 */
public class TaskQueueManager {

    public static final Logger logger = Logger.getLogger(TaskQueueManager.class.getName());

    private static Map<String, RedisTaskQueue> queueMap =
            new ConcurrentHashMap<String, RedisTaskQueue>();

    private static void initQueueMap() {
        logger.info("初始化任务队列...");
        Properties properties = ConfigManager.getProperties("redisQueues");
        String[] queues = properties.getProperty("redis.queues").trim().split(",");
        for (String queue : queues) {
            queueMap.put(queue, new RedisTaskQueue(queue));
            logger.info("建立队列：" + queue);
        }
    }

    static {
        initQueueMap();
    }

    public static TaskQueue get(String name) {
        return queueMap.get(name);
    }
}

```

## 5、向队列中添加任务的代码
```
TaskQueue queue = TaskQueueManager.get(QUEUE_NAME);
queue.pushTask(TASK);
```

## 6、从队列中取出任务执行的代码
```
package com.github.commons.redis.queue;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Created by 刘春龙 on 2017/3/3.
 */
public class TaskSenderManager {

    public static final Logger logger = Logger.getLogger(TaskSenderManager.class.getName());

    public void execute() {
        TaskQueue taskQueue = null;
        Task task;

        try {
            taskQueue = TaskQueueManager.get("SMS_QUEUE");

            task = (Task) taskQueue.popTask();

            while (task != null) {

                //执行任务
                executeSingleTask(taskQueue, task);
                //继续从队列取出任务，进入下一次循环
                task = (Task) taskQueue.popTask();
            }
        } catch (Throwable e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        }
    }

    private void executeSingleTask(TaskQueue taskQueue, Task task) {
        try {

        } catch (Throwable e) {
            if (task != null) {
                taskQueue.pushTask((Serializable) task);
                logger.info(String.format("任务[%s]执行失败：%s，重新放回队列", task, e.getMessage()));
            } else {
                e.printStackTrace();
            }
        }
    }
}

```
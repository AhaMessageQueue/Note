>官方文档：spring-data-redis API:http://docs.spring.io/spring-data/redis/docs/1.5.1.RELEASE/api/

首先跟大家**道歉**，为什么呢？在不久之前，写了一篇<http://blog.mkfree.com/posts/12>,简单地使用是没有问题的，但如果在并发量高的时候，问题就会慢慢出现了，是什么问题呢？

当在高并发的情况下，向redis发出请求，每次操作都新建了一个连接，并且调用了一次连接就不释放也不重新使用，没有做连接池，虽然Redis的服务端，有一个机制是当客户端过一些时间没有操作时就关闭客户端连接。

这样有两个缺点：
1. 在一定时间内，客户端连接数太多，在关闭连接时，也是非常消耗性能
2. 达到一定的连接数时，服务端直接报错，连接数过多

经测试总结到以上两个问题.那应该怎么办呢？
其实，客户端连接可以放到一个连接池里，用完了，放回到连接池中，这样可以重复调用。。这样就可以限制一定数理的连接数，并且性能方面也优化了。。。

后来我在官方上看到了`spring data redis RedisTemplate`,它封装了redis连接池管理的逻辑，业务代码无须关心获取，释放连接逻辑；spring redis同时支持了Jedis，Jredis,rjc 客户端操作；

下面就来看看具体的程序代码：

我首先定义了一些操作接口,我为了方便使用，重新封装下RedisTemplate.

### RedisService 想要更多的操作自己在接口里添加啦

```
    package com.mkfree.framework.common.redis;

    import java.util.Set;

    /**
     * redis 的操作开放接口
     *
     * @author hk
     *
     *         2013-3-31 下午7:25:42
     */
    public interface RedisService {

        /**
         * 通过key删除
         *
         * @param key
         */
        public abstract long del(String... keys);

        /**
         * 添加key value 并且设置存活时间(byte)
         *
         * @param key
         * @param value
         * @param liveTime
         */
        public abstract void set(byte[] key, byte[] value, long liveTime);

        /**
         * 添加key value 并且设置存活时间
         *
         * @param key
         * @param value
         * @param liveTime
         *            单位秒
         */
        public abstract void set(String key, String value, long liveTime);

        /**
         * 添加key value
         *
         * @param key
         * @param value
         */
        public abstract void set(String key, String value);

        /**
         * 添加key value (字节)(序列化)
         *
         * @param key
         * @param value
         */
        public abstract void set(byte[] key, byte[] value);

        /**
         * 获取redis value (String)
         *
         * @param key
         * @return
         */
        public abstract String get(String key);

        /**
         * 通过正则匹配keys
         *
         * @param pattern
         * @return
         */
        public abstract Setkeys(String pattern);

        /**
         * 检查key是否已经存在
         *
         * @param key
         * @return
         */
        public abstract boolean exists(String key);

        /**
         * 清空redis 所有数据
         *
         * @return
         */
        public abstract String flushDB();

        /**
         * 查看redis里有多少数据
         */
        public abstract long dbSize();

        /**
         * 检查是否连接成功
         *
         * @return
         */
        public abstract String ping();

    }
```

### RedisServiceImpl 接口实现类
```
package com.mkfree.framework.common.redis;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 封装redis 缓存服务器服务接口
 *
 * @author hk
 *
 *         2012-12-16 上午3:09:18
 */
@Service(value = "redisService")
public class RedisServiceImpl implements RedisService {

    private static String redisCode = "utf-8";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @param key
     */
    public long del(final String... keys) {
        return redisTemplate.execute(new RedisCallback() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                long result = 0;
                for (int i = 0; i < keys.length; i++) {
                    result = connection.del(keys[i].getBytes());
                }
                return result;
            }
        });
    }

    /**
     * @param key
     * @param value
     * @param liveTime
     */
    public void set(final byte[] key, final byte[] value, final long liveTime) {
        redisTemplate.execute(new RedisCallback() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                connection.set(key, value);
                if (liveTime > 0) {
                    connection.expire(key, liveTime);
                }
                return 1L;
            }
        });
    }

    /**
     * @param key
     * @param value
     * @param liveTime
     */
    public void set(String key, String value, long liveTime) {
        this.set(key.getBytes(), value.getBytes(), liveTime);
    }

    /**
     * @param key
     * @param value
     */
    public void set(String key, String value) {
        this.set(key, value, 0L);
    }

    /**
     * @param key
     * @param value
     */
    public void set(byte[] key, byte[] value) {
        this.set(key, value, 0L);
    }

    /**
     * @param key
     * @return
     */
    public String get(final String key) {
        return redisTemplate.execute(new RedisCallback() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    return new String(connection.get(key.getBytes()), redisCode);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return "";
            }
        });
    }

    /**
     * @param pattern
     * @return
     */
    public Setkeys(String pattern) {
        return redisTemplate.keys(pattern);

    }

    /**
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return redisTemplate.execute(new RedisCallback() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.exists(key.getBytes());
            }
        });
    }

    /**
     * @return
     */
    public String flushDB() {
        return redisTemplate.execute(new RedisCallback() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                connection.flushDb();
                return "ok";
            }
        });
    }

    /**
     * @return
     */
    public long dbSize() {
        return redisTemplate.execute(new RedisCallback() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.dbSize();
            }
        });
    }

    /**
     * @return
     */
    public String ping() {
        return redisTemplate.execute(new RedisCallback() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {

                return connection.ping();
            }
        });
    }

    private RedisServiceImpl() {

    }
}
```

### 其它

```
/**
 * 设置 redisTemplate
 * @param redisTemplate the redisTemplate to set
 */
public void setRedisTemplate(RedisTemplate<K, V> redisTemplate) {
    this.redisTemplate = redisTemplate;
}

/**
 * 获取 RedisSerializer
 */
protected RedisSerializer<String> getRedisSerializer() {
    return redisTemplate.getStringSerializer();
}
```

```
/**
 * 批量新增 使用pipeline方式
 *@param list
 *@return
 */
public boolean add(final List<User> list) {
    Assert.notEmpty(list);
    boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
        public Boolean doInRedis(RedisConnection connection)
                throws DataAccessException {
            RedisSerializer<String> serializer = getRedisSerializer();
            for (User user : list) {
                byte[] key  = serializer.serialize(user.getId());
                byte[] name = serializer.serialize(user.getName());
                connection.setNX(key, name);
            }
            return true;
        }
    }, false, true);
    return result;
}


/**
* 通过key获取
* @param keyId
* @return
*/
public User get(final String keyId) {
User result = redisTemplate.execute(new RedisCallback<User>() {
        public User doInRedis(RedisConnection connection)
                throws DataAccessException {
            RedisSerializer<String> serializer = getRedisSerializer();
            byte[] key = serializer.serialize(keyId);
            byte[] value = connection.get(key);
            if (value == null) {
                return null;
            }
            String name = serializer.deserialize(value);
            return new User(keyId, name, null);
        }
    });
    return result;
}
```

Redis在2.6推出了脚本功能，允许开发者使用Lua语言编写脚本传到Redis中执行。

使用脚本的好处如下:
1. 减少网络开销：本来多次网络请求的操作，可以用一个请求完成，原先多次请求的逻辑放在redis服务器上完成。使用脚本，减少了网络往返时延。
2. 原子操作：Redis会将整个脚本作为一个整体执行，中间不会被其他命令插入。
3. 复用：客户端发送的脚本会永久存储在Redis中，意味着其他客户端可以复用这一脚本而不需要使用代码完成同样的逻辑。
4. 可以实现我们传统数据库里面的"存储过程"

### 实现一个访问频率控制
某个ip在短时间内频繁访问页面，需要记录并检测出来，就可以通过Lua脚本高效的实现

在redis客户端机器上，新建一个文件ratelimiting.lua，内容如下
```text
local times = redis.call('incr',KEYS[1])

if times == 1 then
    redis.call('expire',KEYS[1], ARGV[1])
end

if times > tonumber(ARGV[2]) then
    return 0
end
return 1
```
在redis客户端机器上，如何测试这个脚本呢？如下：
```text
redis-cli --eval ratelimiting.lua rate.limitingl:127.0.0.1 , 10 3
```
`--eval`参数是告诉`redis-cli`读取并运行后面的Lua脚本，`ratelimiting.lua`是脚本的位置，后面跟着是传给Lua脚本的参数。
其中","前的`rate.limiting:127.0.0.1`是要操作的键，可以再脚本中用`KEYS[1]`获取，","后面的10和3是参数，在脚本中能够使用`ARGV[1]`和`ARGV[2]`获得。

注：","两边的空格不能省略，否则会出错

结合脚本的内容可知，这行命令的作用是将访问频率限制为每10秒最多3次，所以在终端中不断的运行此命令会发现当访问频率在10秒内小于或等于3次时返回1，否则返回0。

```text
[root@rhel6 redis-learning]# redis-cli --eval ratelimiting.lua rate.limitingl:127.0.0.1 , 10 3
(integer) 1
[root@rhel6 redis-learning]# redis-cli --eval ratelimiting.lua rate.limitingl:127.0.0.1 , 10 3
(integer) 1
[root@rhel6 redis-learning]# redis-cli --eval ratelimiting.lua rate.limitingl:127.0.0.1 , 10 3
(integer) 1
[root@rhel6 redis-learning]# redis-cli --eval ratelimiting.lua rate.limitingl:127.0.0.1 , 10 3
(integer) 0
[root@rhel6 redis-learning]# redis-cli --eval ratelimiting.lua rate.limitingl:127.0.0.1 , 10 3
(integer) 0
```
补充：
现在Lua脚本用在很多游戏上，主要是Lua脚本做到可以嵌入到其他程序中运行，游戏升级的时候，可以直接升级脚本，而不用重新安装游戏。比如游戏的很多关卡，只需要增加lua脚本，在游戏中嵌入Lua解释器，游戏团队线上更新Lua脚本，然后游戏自动下载最新的游戏关卡。例如之前很多的游戏《愤怒的小鸟》就是用Lua语言实现的关卡。

摘自《Redis入门指南》

### Java调用
```java
public class RedisLuaTest {
    private static final Log log = LogFactory.getLog(RedisLuaTest.class);

    public static void main(String[] args) {

        Jedis jedis = new Jedis("127.0.0.1", 6379);
        List<String> keys = new ArrayList<>();
        List<String> vals = new ArrayList<>();

        // 测试一 基本测试
        Object eval = jedis.eval("return 1", keys, vals);
        log.info(eval);

        // 测试二 eval  里面也可以是一个文件
        keys.add("key");
        Object eval2 = jedis.eval("local tab={}  for i=1,#KEYS do  tab[i] = redis.call('get',KEYS[i]) end return tab",
                keys, vals);
        log.info(eval2);

        // 测试三 scriptLoad evalsha
        //好处：这样可以缓存到服务器，不用每次把lua脚本的内容传过去
        String lua = "local tab={}  for i=1,#KEYS do  tab[i] = redis.call('get',KEYS[i]) end return tab";
        String scriptLoad = jedis.scriptLoad(lua);
        log.info(scriptLoad);
        Object evalsha = jedis.evalsha(scriptLoad, keys, vals);
        log.info(evalsha);

    }
}
```
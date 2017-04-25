Redis是一种高级key-value数据库。它跟memcached类似，不过数据可以持久化，而且支持的数据类型很丰富。有字符串，链表，集 合和有序集合。支持在服务器端计算集合的并，交和补集(difference)等，还支持多种排序功能。所以Redis也可以被看成是一个数据结构服务 器。

Redis的所有数据都是保存在内存中，然后不定期的通过异步方式保存到磁盘上(这称为“半持久化模式”)；也可以把每一次数据变化都写入到一个append only file(aof)里面(这称为“全持久化模式”)。

### 下载地址：
```
$ wget http://redis.googlecode.com/files/redis-2.6.13.tar.gz
```
### 解压缩
```
$ tar -zxvf redis-2.6.13.tar.gz
```
### 编译
```
$ cd redis-2.6.13
$ make
$ make install
$ cp redis.conf /etc/
```

参数介绍：
make install命令执行完成后，会在/usr/local/bin目录下生成本个可执行文件，分别是redis-server、redis-cli、redis-benchmark、redis-check-aof 、redis-check-dump，它们的作用如下：

redis-server：Redis服务器的daemon启动程序

redis-cli：Redis命令行操作工具。也可以用telnet根据其纯文本协议来操作

redis-benchmark：Redis性能测试工具，测试Redis在当前系统下的读写性能

redis-check-aof：数据修复

redis-check-dump：检查导出工具


###  修改系统配置文件，执行命令
a) echo vm.overcommit_memory=1 >> /etc/sysctl.conf

b) sysctl vm.overcommit_memory=1 或执行echo vm.overcommit_memory=1 >>/proc/sys/vm/overcommit_memory

使用数字含义：
0，表示内核将检查是否有足够的可用内存供应用进程使用；如果有足够的可用内存，内存申请允许；否则，内存申请失败，并把错误返回给应用进程。
1，表示内核允许分配所有的物理内存，而不管当前的内存状态如何。
2，表示内核允许分配超过所有物理内存和交换空间总和的内存

###  修改redis配置文件
```
a) $ cd /etc

b) vi redis.conf

c) 修改daemonize yes---目的使进程在后台运行
```

参数介绍：

daemonize：是否以后台daemon方式运行

pidfile：pid文件位置

port：监听的端口号

timeout：请求超时时间

loglevel：log信息级别

logfile：log文件位置

databases：开启数据库的数量

save * *：保存快照的频率，第一个*表示多长时间，第二个*表示执行多少次写操作。在一定时间内执行一定数量的写操作时，自动保存快照。可设置多个条件。

rdbcompression：是否使用压缩

dbfilename：数据快照文件名（只是文件名，不包括目录）

dir：数据快照的保存目录（这个是目录）

appendonly：是否开启appendonlylog，开启的话每次写操作会记一条log，这会提高数据抗风险能力，但影响效率。

appendfsync：appendonlylog如何同步到磁盘（三个选项，分别是每次写都强制调用fsync、每秒启用一次fsync、不调用fsync等待系统自己同步）


### 启动redis
```
a) $ cd /usr/local/bin

b) ./redis-server /etc/redis.conf
```

### 检查是否启动成功
```
a) $ ps -ef | grep redis
```

### 总结Redis操作常用命令
```
redis-server /usr/.../redis.conf 启动redis服务，并指定配置文件
redis-cli 启动redis 客户端

#关闭redis服务
pkill redis-server
#使用客户端关闭redis服务
redis-cli shutdown 

netstat -tunlp|grep 6379 查看redis 默认端口号6379占用情况
```

pkill 和killall 应用方法差不多，也是直接杀死运行中的程序；如果您想杀掉单个进程，请用kill 来杀掉。
应用方法：
```
#pkill 正在运行的程序名
```
举例：
```
[root@localhost beinan]# pgrep -l gaim
2979 gaim
[root@localhost beinan]# pkill gaim
```
也就是说：
kill 对应的是 PID
pkill 对应的是COMMAND

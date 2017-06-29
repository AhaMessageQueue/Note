>Rsync（remote synchronize）是一个远程数据同步工具，可通过LAN/WAN快速同步多台主机间的文件，也可以使用 Rsync 同步本地硬盘中的不同目录。rsync共有3种使用方法，在配置rsync也是有6个步骤的。下面我们就从rsync的介绍，rsync的使用和rsync的配置带你走进rsync的世界

## 一.rsync简介
Rsync（remote synchronize）是一个远程数据同步工具，可通过LAN/WAN快速同步多台主机间的文件，也可以使用 Rsync 同步本地硬盘中的不同目录。 在使用 rsync 进行远程同步时，可以使用两种方式：远程 Shell 方式（建议使用 ssh，用户验证由 ssh 负责）和 C/S 方式（即客户连接远程 rsync 服务器，用户验证由 rsync 服务器负责）。rsync 被称为是一个文件同步的快速方法，主要是因为其在同步文件时会检查文件之间是否有差异，它只同步存在差异或者不存在的文件，但是首次同步时速度依然很慢。

rsync有许多选项：
```
-n: 在不确定命令是否能按意愿执行时，务必要事先测试；-n可以完成此功能；
-v: --verbose，详细输出模式
-q: --quiet，静默模式
-c: --checksum，开启校验功能，强制对文件传输进行校验
-r: --recursive，递归复制；
-a: --archives，归档，保留文件的原有属性
-p: --perms 保留文件的权限
-t: --times 保留文件的时间戳
-l: --links 保留文件的符号链接 
-g: --group 保留文件的属组
-o: --owner 保留文件的属主
-D： --devices 保留设备文件
-H, --hard-links 保留硬链结;
-S, --sparse 对稀疏文件进行特殊处理以节省DST的 空间;
-e ssh: 表示使用ssh协议作承载
-z: 对文件压缩后传输
--progress：显示进度条
--stats: 显示如何执行压缩和传输
--delete：删除那些DST中有而SRC没有的文件，即删除DST中多余的文件；
```
## 二，rsync使用方法
rsync可以在此处下载   http://rsync.samba.org/  ，CentOS系统上rsync默认是安装的。

rsync有六种不同的工作模式：
### 1. 拷贝本地文件

当SRC和DES路径信息都不包含有单个冒号”:”分隔符时就启动这种工作模式。
```
[root@localhost ~]# rsync -avSH /home/feiyu/ /bak/
```
### 2.将本地机器的内容拷贝到远程机器

使用一个远程shell程序（如rsh、ssh）来实现将本地机器的内容拷贝到远程机器。当DST路径地址包含单个冒号”:”分隔符时启动该模式。
```
[root@localhost ~]# rsync -av /home/feiyu/ 192.168.0.24:/home/feiyu/
```
### 3.将远程机器的内容拷贝到本地机器

使用一个远程shell程序（如rsh、ssh）来实现将远程机器的内容拷贝到本地机器。当SRC地址路径包含单个冒号”:”分隔符时启动该模式。
```
[root@localhost ~]# rsync -av 192.168.0.24:/home/feiyu/  /home/feiyu/ 
```
注意：rsync命令使用中，如果源参数的末尾有斜线，就会复制指定目录的内容，而不复制目录本身；没有斜线，则会复制目录本身；目标参数末尾的斜线没有作用；因此下面的命令
```
[root@localhost ~]# rsync -r /mydata/data /backups/ : 会把目录data直接同步至/backups目录中
 
[root@localhost ~]# rsync -r /mydata/data/ /backups/: 会把目录data/中的内容的同步至/backups目录中
```
后面三种模式都是在rsync作为服务器时才能使用的，下面就开始配置rsync作为一个服务。
## 三，配置rsync服务
配置一个简单的rsync服务并不复杂，但是我们安装好rsync后，并没有发现配置文件，所以你需要手动建立一些配置文件。rsync可以经由xinetd启动daemon，或者作为一个独立进程启动daemon。如果把它作为一个独立进程来启动，只需要运行命令：rsync –daemon即可；但是我们一般将其作为超级守护进程使用。下面是安装步骤：
### 1.安装并启动xinetd
```
[root@localhost ~]# yum -y install xinetd 
 
[root@localhost ~]# ls  /etc/xinetd.d/    #rsync的xinetd配置文件已经存在
chargen-dgram   daytime-dgram   discard-dgram   echo-dgram   rsync          time-dgram
chargen-stream  daytime-stream  discard-stream  echo-stream  tcpmux-server  time-stream
```
### 2. 为rsync服务提供配置文件
配置文件为/etc/rsyncd.conf，获取帮助的方式：man rsyncd.conf。配置文件需要定义一个全局配置和多个rsync共享配置。
```
[root@localhost ~]# cat  /etc/rsyncd.conf 
# Global Settings
# port = 873   端口号默认为873，可以不指定
uid = nobody  //指定当模块传输文件的守护进程UID
gid = nobody  //指定当模块传输文件的守护进程GID
use chroot = no  //使用chroot到文件系统中的目录中
max connections = 5   //最大并发连接数
strict modes = yes      #严格检查文件权限
pid file = /var/run/rsyncd.pid  //指定PID文件
lock file = /usr/local/rsyncd/rsyncd.lock  //指定支持max connection的锁文件，默认为/var/run/rsyncd.lock
log file = /var/log/rsyncd.log  //rsync 服务器的日志
 
# Directory to be synced
[mydata]          //自定义模块
path = /mydata/data    //用来指定要备份的目录
ignore errors = yes    //可以忽略一些IO错误
read only = no  //设置no，客户端可以上传文件，yes是只读
write only = no  //no为客户端可以下载，yes 不能下载
hosts allow = 192.168.0.0/16  //可以连接的IP
hosts deny = *   //禁止连接的IP
list = false       //客户请求时，使用模块列表
uid = root
gid = root	
auth users = myuser   //连接用户名，和linux系统用户名无关系
secrets file = /etc/rsyncd.passwd	//验证密码文件
```
说明（deny | allow 规则）：
1. 二者都不出现时，默认为允许访问；
2. 只出现hosts allow: 定义白名单；但没有被匹配到的主机由默认规则处理，即为允许；
3. 只出现hosts deny： 定义黑名单；出现在名单中的都被拒绝；
4. 二者同时出现：先检查hosts allow，如果匹配就allow，否则，检查hosts deny，如果匹配则拒绝；如二者均无匹配，则由默认规则处理，即为允许；

### 3.创建密码文件
文件格式(明文)： username : password
```
[root@localhost ~]# echo "myuser:mypass" >  /etc/rsyncd.passwd 
 
[root@localhost ~]# chmod 600 /etc/rsyncd.passwd     #权限必须为600
```
### 4.启动服务
```
[root@localhost ~]# service xinetd  start
```
### 5.使用方法 ( 后三种模式 )
a. 从远程rsync服务器中拷贝文件到本地机。当SRC路径信息包含”::”分隔符时启动该模式。
```
[root@localhost ~]# rsync -av myuser@192.168.0.23::mydata /tmp/     #myuser为rsync服务器的一个用户
```
b. 从本地机器拷贝文件到远程rsync服务器中。当DST路径信息包含”::”分隔符时启动该模式。
```
[root@localhost ~]# rsync -av install.log.syslog  myuser@192.168.0.23::mydata 
```
c. 列远程机的文件列表。这类似于rsync传输，不过只要在命令中省略掉本地机信息即可。
```
[root@localhost ~]# rsync -av myuser@192.168.0.23::mydata
```
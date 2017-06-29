>原文：http://www.cnblogs.com/itech/archive/2009/08/10/1542945.html

>rsync实现网站的备份，文件的同步，不同系统的文件的同步，如果是windows的话，需要windows版本cwrsync

## 一、什么是rsync 
rsync，remote synchronize顾名思意就知道它是一款实现远程同步功能的软件，它在同步文件的同时，可以保持原来文件的权限、时间、软硬链接等附加信息。 rsync是用 “rsync 算法”提供了一个客户机和远程文件服务器的文件同步的快速方法，而且可以通过ssh方式来传输文件，这样其保密性也非常好，另外它还是免费的软件。

rsync 包括如下的一些特性：
- 能更新整个目录和树和文件系统；
- 有选择性的保持符号链链、硬链接、文件属于、权限、设备以及时间等；
- 对于安装来说，无任何特殊权限要求；
- 对于多个文件来说，内部流水线减少文件等待的延时；
- 能用rsh、ssh 或直接端口做为传输入端口；
- 支持匿名rsync 同步文件，是理想的镜像工具；

## 二、架设rsync服务器
架设rsync 服务器比较简单，写一个配置文件rsyncd.conf 。文件的书写也是有规则的，我们可以参照rsync.samba.org 上的文档来做。当然我们首先要安装好rsync这个软件才行；

### rsync的安装；
**获取rsync**

rysnc的官方网站：http://rsync.samba.org/可以从上面得到最新的版本。目前最新版是3.05。当然，因为rsync是一款如此有用的软件，所以很多Linux的发行版本都将它收录在内了。

**软件包安装**
```
# sudo apt-get  install  rsync  注：在debian、ubuntu 等在线安装方法；
# yum install rsync    注：Fedora、Redhat 等在线安装方法；
# rpm -ivh rsync       注：Fedora、Redhat 等rpm包安装方法；
```
**源码包安装**
```
tar xvf  rsync-xxx.tar.gz
cd rsync-xxx
./configure --prefix=/usr  ;make ;make install   注：在用源码包编译安装之前，您得安装gcc等编译开具才行；
```
### 配置文件
rsync的主要有以下三个配置文件rsyncd.conf(主配置文件)、rsyncd.secrets(密码文件)、rsyncd.motd(rysnc服务器信息)

服务器配置文件(/etc/rsyncd.conf)，该文件默认不存在，请创建它。

具体步骤如下：
```
#touch /etc/rsyncd.conf  #创建rsyncd.conf，这是rsync服务器的配置文件。
#touch /etc/rsyncd.secrets  #创建rsyncd.secrets ，这是用户密码文件。
#chmod 600 /etc/rsyncd/rsyncd.secrets  #将rsyncd.secrets这个密码文件的文件属性设为root拥有, 且权限要设为600, 否则无法备份成功!
#touch /etc/rsyncd.motd
```
下一就是我们修改rsyncd.conf和rsyncd.secrets和rsyncd.motd文件的时候了。

#### A、设定/etc/rsyncd.conf
rsyncd.conf是rsync服务器主要配置文件。我们先来个简单的示例，后面在详细说明各项作用。

比如我们要备份服务器上的/home和/opt，在/home中我想把easylife和samba目录排除在外；

```
    # Distributed under the terms of the GNU General Public License v2
　　# Minimal configuration file for rsync daemon
　　# See rsync(1) and rsyncd.conf(5) man pages for help

　　# This line is required by the /etc/init.d/rsyncd script
　　pid file = /var/run/rsyncd.pid  
　　port = 873
　　address = 192.168.1.171 
　　#uid = nobody
　　#gid = nobody   
　　uid = root  
　　gid = root  

　　use chroot = yes 
　　read only = yes 

　　#limit access to private LANs
　　hosts allow=192.168.1.0/255.255.255.0 10.0.1.0/255.255.255.0 
　　hosts deny=*

　　max connections = 5
　　motd file = /etc/rsyncd.motd

　　#This will give you a separate log file
　　#log file = /var/log/rsync.log

　　#This will log every file transferred - up to 85,000+ per user, per sync
　　#transfer logging = yes

　　log format = %t %a %m %f %b
　　syslog facility = local3
　　timeout = 300

　　[rhel4home]  
　　path = /home   
　　list=yes
　　ignore errors
　　auth users = root
　　secrets file = /etc/rsyncd.secrets 
　　comment = This is RHEL 4 data 
　　exclude = easylife/  samba/     

　　[rhel4opt]
　　path = /opt
　　list=no
　　ignore errors
　　comment = This is RHEL 4 opt
　　auth users = easylife
　　secrets file = /etc/rsyncd/rsyncd.secrets
```
>注：关于auth users是必须在服务器上存在的真实的系统用户，如果你想用多个用户以,号隔开，
比如auth users = easylife,root 

#### B、设定密码文件
密码文件格式很简单，rsyncd.secrets的内容格式为：用户名:密码

我们在例子中rsyncd.secrets的内容如下类似的；在文档中说，有些系统不支持长密码，自己尝试着设置一下吧。
```
easylife:keer
root:mike
```
修改权限：
```
chown root.root rsyncd.secrets 　#修改属主
chmod 600 rsyncd.secrets     #修改权限
```
>注：1. 将rsyncd.secrets这个密码文件的文件属性设为root拥有, 且权限要设为600, 否则无法备份成功!
        出于安全目的，文件的属性必需是只有属主可读。
 　　2. 这里的密码值得注意，为了安全你不能把系统用户的密码写在这里。比如你的系统用户easylife密码是000000，
        为了安全你可以让rsync中的easylife为keer。这和samba的用户认证的密码原理是差不多的。
        
#### C、设定rsyncd.motd 文件
它是定义rysnc服务器信息的，也就是用户登录信息。比如让用户知道这个服务器是谁提供的等；类似ftp服务器登录时，我们所看到的 linuxsir.org ftp ……。 当然这在全局定义变量时，并不是必须的，你可以用#号注掉，或删除；我在这里写了一个 rsyncd.motd的内容为：
```
++++++++++++++++++++++++++++++++++++++++++++++
Welcome to use the mike.org.cn rsync services!
       2002     ----        2009
++++++++++++++++++++++++++++++++++++++++++++++
```
### 三、rsyncd.conf服务器的配置详解
#### A、全局定义
在rsync 服务器中，全局定义有几个比较关健的，根据我们前面所给的配置文件 rsyncd.conf 文件；
```
pid file = /var/run/rsyncd.pid   注：告诉进程写到 /var/run/rsyncd.pid 文件中；
port = 873  注：指定运行端口，默认是873，您可以自己指定；
address = 192.168.1.171  注：指定服务器IP地址
uid = nobody  
gid = nobdoy  
```
注：服务器端传输文件时，要发哪个用户和用户组来执行，默认是nobody。 如果用nobody 用户和用户组，可能遇到权限问题，有些文件从服务器上拉不下来。所以我就偷懒，为了方便，用了root 。不过您可以在定义要同步的目录时定义的模块中指定用户来解决权限的问题。

```
use chroot = yes 
```
注：用chroot，在传输文件之前，服务器守护程序在将chroot 到文件系统中的目录中，这样做的好处是可能保护系统被安装漏洞侵袭的可能。缺点是需要超级用户权限。另外对符号链接文件，将会排除在外。也就是说，你在 rsync服务器上，如果有符号链接，你在备份服务器上运行客户端的同步数据时，只会把符号链接名同步下来，并不会同步符号链接的内容；这个需要自己来尝 试

```
read only = yes 
```
注：read only 是只读选择，也就是说，不让客户端上传文件到服务器上。还有一个 write only选项，自己尝试是做什么用的吧；

```
#limit access to private LANs
hosts allow=192.168.1.0/255.255.255.0 10.0.1.0/255.255.255.0 
```
注：在您可以指定单个IP，也可以指定整个网段，能提高安全性。格式是ip 与ip 之间、ip和网段之间、网段和网段之间要用空格隔开；

```
max connections = 5   
```
注：客户端最多连接数

```
motd file = /etc/rsyncd/rsyncd.motd
```
注：motd file 是定义服务器信息的，要自己写 rsyncd.motd 文件内容。当用户登录时会看到这个信息。比如我写的是：

```
++++++++++++++++++++++++++++++++++++++++++++++
Welcome to use the mike.org.cn rsync services!
       2002     ----        2009
++++++++++++++++++++++++++++++++++++++++++++++
```
```
log file = /var/log/rsync.log
```
注：rsync 服务器的日志；
```
transfer logging = yes
```
注：这是传输文件的日志

```
log format = %t %a %m %f %b
syslog facility = local3
timeout = 300 
```

#### B、模块定义
模块定义什么呢？主要是定义服务器哪个目录要被同步。每个模块都要以`[name]`形式。
这个名字就是在rsync 客户端看到的名字，其实有点象Samba服务器提供的共享名。
而服务器真正同步的数据是通过path 指定的。我们可以根据自己的需要，来指定多个模块。
每个模块要指定认证用户，密码文件、但排除并不是必须的。

下面是前面配置文件模块的例子：
```
    [rhel4home]  #模块，它为我们提供了一个链接的名字，链接到哪呢，在本模块中，链接到了/home目录；要用[name] 形式；

　　path = /home    #指定文件目录所在位置，这是必须指定的
　　auth users = root   #认证用户是root  ，是必须在服务器上存在的用户
　　list=yes   #list 意思是把rsync服务器上提供同步数据的目录在服务器上模块是否显示列出来。默认是yes 。如果你不想列出来，就no ；如果是no是比较安全的，至少别人不知道你的服务器上提供了哪些目录。你自己知道就行了；
　　ignore errors  #忽略IO错误
　　secrets file = /etc/rsyncd.secrets   #密码存在哪个文件
　　comment = linuxsir home  data  #注释可以自己定义
　　exclude = beinan/ samba/  #exclude 是排除的意思，也就是说，要把/home目录下的beinan和samba 排除在外； beinan/和samba/目录之间有空格分开
```
```
    [rhel4opt] 
　　path = /opt
　　list=no
　　comment = optdir  
　　auth users = beinan 
　　secrets file = /etc/rsyncd/rsyncd.secrets
　　ignore errors
```

### 四、启动rsync服务器及防火墙的设置
启动rsync服务器相当简单，有以下几种方法
#### A、--daemon参数方式，是让rsync以服务器模式运行
```
#/usr/bin/rsync --daemon  --config=/etc/rsyncd/rsyncd.conf 　#--config用于指定rsyncd.conf的位置,如果在/etc下可以不写
```
#### B、xinetd方式
修改services加入如下内容
```
# nano -w /etc/services

rsync　　873/tcp　　# rsync
rsync　　873/udp　　# rsync
```
这一步一般可以不做，通常都有这两行(我的RHEL4和GENTOO默认都有)。
修改的目的是让系统知道873端口对应的服务名为rsync。如没有的话就自行加入。

设定 /etc/xinetd.d/rsync, 简单例子如下:
```
    # default: off
　　# description: The rsync server is a good addition to am ftp server, as it \
　　#       allows crc checksumming etc.
　　service rsync
　　{
        disable = no
        socket_type     = stream
        wait            = no
        user            = root
        server          = /usr/bin/rsync
        server_args     = --daemon
        log_on_failure  += USERID
　　}
```
上述, 主要是要打开rsync這個daemon, 一旦有rsync client要连接時, xinetd会把它转介給 rsyncd(port 873)。然后service xinetd restart, 使上述设定生效.

#### C、rsync服务器和防火墙
Linux 防火墙是用iptables，所以我们至少在服务器端要让你所定义的rsync 服务器端口通过，客户端上也应该让通过。
```
#iptables -A INPUT -p tcp -m state --state NEW  -m tcp --dport 873 -j ACCEPT
#iptables -L  查看一下防火墙是不是打开了 873端口
```
如果你不太懂防火墙的配置，可以先service iptables stop 将防火墙关掉。当然在生产环境这是很危险的，做实验才可以这么做哟！

### 五、通过rsync客户端来同步数据
在配置完rsync服务器后，就可以从客户端发出rsync命令来实现各种同步的操作。rsync有很多功能选项，下面就对介绍一下常用的选项：

#### A、语法详解
Rsync的命令格式可以为以下六种：
```
rsync [OPTION]... SRC DEST
rsync [OPTION]... SRC [USER@]HOST:DEST
rsync [OPTION]... [USER@]HOST:SRC DEST
rsync [OPTION]... [USER@]HOST::SRC DEST
rsync [OPTION]... SRC [USER@]HOST::DEST
rsync [OPTION]... rsync://[USER@]HOST[:PORT]/SRC [DEST]
```
对应于以上六种命令格式，rsync有六种不同的工作模式：
1. 拷贝本地文件。当SRC和DES路径信息都不包含有单个冒号”:”分隔符时就启动这种工作模式。如：rsync -a /data /backup
2. 使用一个远程shell程序(如rsh、ssh)来实现将本地机器的内容拷贝到远程机器。当DST路径地址包含单个冒号”:”分隔符时启动该模式。如：rsync -avz *.c foo:src
3. 使用一个远程shell程序(如rsh、ssh)来实现将远程机器的内容拷贝到本地机器。当SRC地址路径包含单个冒号”:”分隔符时启动该模式。如：rsync -avz foo:src/bar /data
4. 从远程rsync服务器中拷贝文件到本地机。当SRC路径信息包含”::”分隔符时启动该模式。如：rsync -av root@172.16.78.192::www /databack
5. 从本地机器拷贝文件到远程rsync服务器中。当DST路径信息包含”::”分隔符时启动该模式。如：rsync -av /databack root@172.16.78.192::www
6. 列远程机的文件列表。这类似于rsync传输，不过只要在命令中省略掉本地机信息即可。如：rsync -v rsync://172.16.78.192/www

rsync参数的具体解释如下：
```
-v, --verbose 详细模式输出
-q, --quiet 精简输出模式
-c, --checksum 打开校验开关，强制对文件传输进行校验
-a, --archive 归档模式，表示以递归方式传输文件，并保持所有文件属性，等于-rlptgoD
-r, --recursive 对子目录以递归模式处理
-R, --relative 使用相对路径信息
-b, --backup 创建备份，也就是对于
目的已经存在有同样的文件名时，将老的文件重新命名为~filename。可以使用--suffix选项来指定不同的备份文件前缀。
--backup-dir 将备份文件(如~filename)存放在目录下。
-suffix=SUFFIX 定义备份文件前缀
-u, --update 仅仅进行更新，也就是跳过所有已经存在于DST，并且文件时间晚于要备份的文件。(不覆盖更新的文件)
-l, --links 保留软链结
-L, --copy-links 像对待常规文件一样处理软链结
--copy-unsafe-links 仅仅拷贝指向SRC路径目录树以外的链结
--safe-links 忽略指向SRC路径目录树以外的链结
-H, --hard-links 保留硬链结
-p, --perms 保持文件权限
-o, --owner 保持文件属主信息
-g, --group 保持文件属组信息
-D, --devices 保持设备文件信息
-t, --times 保持文件时间信息
-S, --sparse 对稀疏文件进行特殊处理以节省DST的空间
-n, --dry-run显示哪些文件将被传输
-W, --whole-file 拷贝文件，不进行增量检测
-x, --one-file-system 不要跨越文件系统边界
-B, --block-size=SIZE 检验算法使用的块尺寸，默认是700字节
-e, --rsh=COMMAND 指定使用rsh、ssh方式进行数据同步
--rsync-path=PATH 指定远程服务器上的rsync命令所在路径信息
-C, --cvs-exclude 使用和CVS一样的方法自动忽略文件，用来排除那些不希望传输的文件
--existing 仅仅更新那些已经存在于DST的文件，而不备份那些新创建的文件
--delete 删除那些DST中SRC没有的文件
--delete-excluded 同样删除接收端那些被该选项指定排除的文件
--delete-after 传输结束以后再删除
--ignore-errors 即使出现IO错误也进行删除
--max-delete=NUM 最多删除NUM个文件
--partial 保留那些因故没有完全传输的文件，以加快随后的再次传输。
阻止rsync在传输中断时删除已拷贝的部分(如果在拷贝文件的过程中，传输被中断，rsync的默认操作是撤消操作，即从目标机上删除已拷贝的部分文件。)
--force 强制删除目录，即使不为空
--numeric-ids 不将数字的用户和组ID匹配为用户名和组名
--timeout=TIME IP超时时间，单位为秒
-I, --ignore-times 不跳过那些有同样的时间和长度的文件
--size-only 当决定是否要备份文件时，仅仅查看文件大小而不考虑文件时间
--modify-window=NUM 决定文件是否时间相同时使用的时间戳窗口，默认为0
-T --temp-dir=DIR 在DIR中创建临时文件
--compare-dest=DIR 同样比较DIR中的文件来决定是否需要备份
-P 等同于 --partial
--progress 显示备份过程
-z, --compress 对备份的文件在传输时进行压缩处理
--exclude=PATTERN 指定排除不需要传输的文件模式
--include=PATTERN 指定不排除而需要传输的文件模式
--exclude-from=FILE 排除FILE中指定模式的文件
--include-from=FILE 不排除FILE指定模式匹配的文件
--version 打印版本信息
--address 绑定到特定的地址
--config=FILE 指定其他的配置文件，不使用默认的rsyncd.conf文件
--port=PORT 指定其他的rsync服务端口
--blocking-io 对远程shell使用阻塞IO
-stats 给出某些文件的传输状态
--progress 在传输时显示传输过程
--log-format=formAT 指定日志文件格式
--password-file=FILE 从FILE中得到密码
--bwlimit=KBPS 限制I/O带宽，KBytes per second
-h, --help 显示帮助信息
```
#### B、一些实例
###### 列出rsync 服务器上的所提供的同步内容；

**B1、列出rsync 服务器上的所提供的同步内容；**

首先：我们看看rsync服务器上提供了哪些可用的数据源
```
    # rsync  --list-only  root@192.168.145.5::
　　++++++++++++++++++++++++++++++++++++++++++++++
    Welcome to use the mike.org.cn rsync services!
         2002     ----        2009
    ++++++++++++++++++++++++++++++++++++++++++++++

　　rhel4home       This is RHEL 4 data
```
注：前面是rsync所提供的数据源，也就是我们在rsyncd.conf中所写的`[rhel4home]`模块。而“This is RHEL 4 data”是由`[rhel4home]`模块中的 comment = This is RHEL 4 data 提供的；为什么没有把rhel4opt数据源列出来呢？因为我们在`[rhel4opt]`中已经把list=no了。

```
    $ rsync  --list-only  root@192.168.145.5::rhel4home 

　　++++++++++++++++++++++++++++++++++++++++++++++
      Welcome to use the mike.org.cn rsync services!
           2002     ----        2009
    ++++++++++++++++++++++++++++++++++++++++++++++

　　Password:
　　drwxr-xr-x        4096 2009/03/15 21:33:13 .
　　-rw-r--r--        1018 2009/03/02 02:33:41 ks.cfg
　　-rwxr-xr-x       21288 2009/03/15 21:33:13 wgetpaste
　　drwxrwxr-x        4096 2008/10/28 21:04:05 cvsroot
　　drwx------        4096 2008/11/30 16:30:58 easylife
　　drwsr-sr-x        4096 2008/09/20 22:18:05 giddir
　　drwx------        4096 2008/09/29 14:18:46 quser1
　　drwx------        4096 2008/09/27 14:38:12 quser2
　　drwx------        4096 2008/11/14 06:10:19 test
　　drwx------        4096 2008/09/22 16:50:37 vbird1
　　drwx------        4096 2008/09/19 15:28:45 vbird2
```
后面的root@ip中，root是指定密码文件中的用户名，之后的::rhel4home这是rhel4home模块名

**B2、rsync客户端同步数据；**

```
#rsync -avzP root@192.168.145.5::rhel4home rhel4home
```
Password: 这里要输入root的密码，是服务器端rsyncd.secrets提供的。在前面的例子中我们用的是mike，输入的密码并不回显，输好就回车。

>注： 这个命令的意思就是说，用root用户登录到服务器上，把rhel4home数据，同步到本地当前目录rhel4home上。
当然本地的目录是可以你自己定义的。如果当你在客户端上当前操作的目录下没有rhel4home这个目录时，系统会自动为你创建一个；当存在rhel4home这个目录中，你要注意它的写权限。

```
#rsync -avzP  --delete linuxsir@linuxsir.org::rhel4home   rhel4home
```
这回我们引入一个--delete 选项，表示客户端上的数据要与服务器端完全一致，如果 linuxsir home目录中有服务器上不存在的文件，则删除。
最终目的是让linuxsirhome目录上的数据完全与服务器上保持一致；用的时候要 小心点，最好不要把已经有重要数所据的目录，当做本地更新目录，否则会把你的数据全部删除；

**設定 rsync client**

设定密码文件
```
#rsync -avzP  --delete  --password-file=rsyncd.secrets   root@192.168.145.5::rhel4home rhel4home
```
这次我们加了一个选项 --password-file=rsyncd.secrets，这是当我们以root用户登录rsync服务器同步数据时，密码将读取rsyncd.secrets这个文件。这个文件内容只是root用户的密码。我们要如下做；

```
# touch rsyncd.secrets
# chmod 600 rsyncd.secrets
# echo "mike"> rsyncd.secrets
```
```
# rsync -avzP  --delete  --password-file=rsyncd.secrets   root@192.168.145.5::rhel4home rhel4home
```
>注：这里需要注意的是这份密码文件权限属性要设得只有属主可读。

这样就不需要密码了；其实这是比较重要的，因为服务器通过crond 计划任务还是有必要的；

**B3、让rsync客户端自动与服务器同步数据**
服务器是重量级应用，所以数据的网络备份还是极为重要的。我们可以在生产型服务器上配置好rsync 服务器。我们可以把一台装有rysnc机器当做是备份服务器。让这台备份服务器，每天在早上4点开始同步服务器上的数据；并且每个备份都是完整备份。有时 硬盘坏掉，或者服务器数据被删除，完整备份还是相当重要的。这种备份相当于每天为服务器的数据做一个镜像，当生产型服务器发生事故时，我们可以轻松恢复数 据，能把数据损失降到最低；是不是这么回事？？

- step1：创建同步脚本和密码文件
    
    ```
    #mkdir   /etc/cron.daily.rsync
    #cd  /etc/cron.daily.rsync
    #touch rhel4home.sh  rhel4opt.sh
    #chmod 755 /etc/cron.daily.rsync/*.sh 
    #mkdir /etc/rsyncd/
    #touch /etc/rsyncd/rsyncrhel4root.secrets
    #touch /etc/rsyncd/rsyncrhel4easylife.secrets
    #chmod 600  /etc/rsyncd/rsync.*
    ```
    注： 我们在 /etc/cron.daily.rsync中创建了两个文件rhel4home.sh和rhel4opt.sh ，并且是权限是755的。创建了两个密码文件root用户用的是rsyncrhel4root.secrets ，easylife用户用的是 rsyncrhel4easylife.secrets，权限是600；
    
    **我们编辑rhel4home.sh，内容是如下的：**`#! /bin/sh` 是指此脚本使用/bin/sh来解释执行
    ```
    #!/bin/sh
    #backup 192.168.145.5:/home 
    /usr/bin/rsync   -avzP  --password-file=/etc/rsyncd/rsyncrhel4root.secrets  root@192.168.145.5::rhel4home   /home/rhel4homebak/$(date +'%m-%d-%y')
    ```
    我们编辑 rhel4opt.sh ，内容是：
    ```
    #!/bin/sh
    #backup 192.168.145.5:/opt
    /usr/bin/rsync   -avzP  --password-file=/etc/rsyncd/rsyncrhel4easylife.secrets    easylife@192.168.145.5::rhel4opt   /home/rhel4optbak/$(date +'%m-%d-%y')
    ```
    注：你可以把rhel4home.sh和rhel4opt.sh的内容合并到一个文件中，比如都写到rhel4bak.sh中；
    接着我们修改 /etc/rsyncd/rsyncrhel4root.secrets和rsyncrhel4easylife.secrets的内容；
    ```
    # echo "mike" > /etc/rsyncd/rsyncrhel4root.secrets
    # echo "keer"> /etc/rsyncd/rsyncrhel4easylife.secrets
    ```
    然后我们在/home目录下创建rhel4homebak 和rhel4optbak两个目录，意思是服务器端的rhel4home数据同步到备份服务器上的/home/rhel4homebak 下，rhel4opt数据同步到 /home/rhel4optbak/目录下。并按年月日归档创建目录；每天备份都存档；
    
    ```
    #mkdir /home/rhel4homebak
    #mkdir /home/rhel4optbak
    ```
- step2：修改crond服务器的配置文件 加入到计划任务
    
    ````
    #crontab  -e
    ````
    加入下面的内容：
    ```
    # Run daily cron jobs at 4:10 every day  backup rhel4 data: 
    10 4 * * * /usr/bin/run-parts   /etc/cron.daily.rsync   1> /dev/null
    ```
    注：第一行是注释，是说明内容，这样能自己记住。
    　　第二行表示在每天早上4点10分的时候，运行 /etc/cron.daily.rsync 下的可执行脚本任务；
    
    配置好后，要重启crond 服务器；
    
    ```
    # killall crond    注：杀死crond 服务器的进程；
    # ps aux |grep crond  注：查看一下是否被杀死；
    # /usr/sbin/crond    注：启动 crond 服务器；
    # ps aux  |grep crond  注：查看一下是否启动了？
    root      3815  0.0  0.0   1860   664 ?        S    14:44   0:00 /usr/sbin/crond
    root      3819  0.0  0.0   2188   808 pts/1    S+   14:45   0:00 grep crond 
    ```
    
## 六、FAQ
略
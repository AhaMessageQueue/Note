nginx命令行参数

不像许多其他软件系统，Nginx 仅有几个命令行参数，完全通过配置文件来配置 
- -c 为 Nginx 指定一个配置文件，来代替缺省的。 
- -t 不运行，而仅仅测试配置文件。nginx 将检查配置文件的语法的正确性，并尝试打开配置文件中所引用到的文件。
- -v 显示 nginx 的版本。
- -V 显示 nginx 的版本，编译器版本和配置参数。

## nginx控制信号
可以使用信号系统来控制主进程。默认，nginx 将其主进程的 pid 写入到 /usr/local/nginx/nginx.pid 文件中。通过传递参数给 ./configure 或使用 pid 指令，来改变该文件的位置。

主进程可以处理以下的信号：
 
![](\images\控制信号.png)

尽管你不必自己操作工作进程，但是，它们也支持一些信号：

![](\images\控制信号2.png)

## nginx 启动、停止、重启命令
### nginx启动
```
sudo /usr/local/nginx/nginx (nginx二进制文件绝对路径，可以根据自己安装路径实际决定)
```
### nginx从容停止命令，等所有请求结束后关闭服务
```
ps -ef |grep nginx

kill -QUIT nginx主进程号
```
### nginx 快速停止命令，立刻关闭nginx进程
```
ps -ef |grep nginx

kill -TERM nginx主进程号
```
### 如果以上命令不管用，可以强制停止
```
kill -9 nginx主进程号
```

如果嫌麻烦可以不用查看进程号，直接使用命令进行操作 
其中/usr/local/nginx/nginx.pid 为nginx.conf中pid命令设置的参数，用来存放nginx主进程号的文件 

```
kill -信号类型(HUP|TERM|QUIT) cat /usr/local/nginx/nginx.pid 
//例如 
kill -QUIT cat /usr/local/nginx/nginx.pid
```

### nginx重启命令
nginx重启可以分成几种类型
- 简单型，先关闭进程，修改你的配置后，重启进程。 
    ```
    kill -QUIT cat /usr/local/nginx/nginx.pid 
    sudo /usr/local/nginx/nginx
    ```
- 重新加载配置文件，不重启进程，不会停止处理请求
- 平滑更新nginx二进制，不会停止处理请求

### 使用信号加载新的配置
Nginx 支持几个信号，能在它运行时控制其操作。其中最普通的是 15 ，用来中止运行的进程：
```
# ps aux | egrep (PID|nginx)
USER       PID %CPU %MEM    VSZ   RSS TTY      STAT START   TIME COMMAND
root      2213  0.0  0.0   6784  2036 ?        Ss   03:01   0:00 nginx: master process /usr/sbin/nginx -c /etc/nginx/nginx.conf
# kill -15 2213
```

而最有趣的是能平滑改变 nginx 配置的选项（请注意，在重载前，要先测试一下配置文件）：
```
# nginx -t -c /etc/nginx/nginx.conf
2006/09/16 13:07:10 [info] 15686#0: the configuration file /etc/nginx/nginx.conf syntax is ok
2006/09/16 13:07:10 [info] 15686#0: the configuration file /etc/nginx/nginx.conf was tested successfully
# ps aux | egrep (PID|nginx)
USER       PID %CPU %MEM    VSZ   RSS TTY      STAT START   TIME COMMAND
root      2213  0.0  0.0   6784  2036 ?        Ss   03:01   0:00 nginx: master process /usr/sbin/nginx -c /etc/nginx/nginx.conf
# kill -HUP 2213
```
当 nginx 接收到 HUP 信号，它会尝试先解析配置文件（如果指定配置文件，就使用指定的，否则使用默认的），成功的话，就应用新的配置文件（例如：重新打开日志文件或监听的套接字）。
之后，nginx 运行新的工作进程并从容关闭旧的工作进程。通知工作进程关闭监听套接字但是继续为当前连接的客户提供服务。所有客户端的服务完成后，旧的工作进程被关闭。 
如果新的配置文件应用失败，nginx 将继续使用旧的配置进行工作。

### 平滑升级到新的二进制代码
你可以在不中断服务的情况下 - 新的请求也不会丢失，使用新的 nginx 可执行程序替换旧的（当升级新版本或添加/删除服务器模块时）。

首先，使用新的可执行程序替换旧的（最好做好备份），然后，发送 USR2 (kill -USR2 pid)信号给主进程。
主进程将重命名它的 .pid 文件为 .oldbin (比如：/usr/local/nginx/logs/nginx.pid.oldbin)，
然后执行新的可执行程序，依次启动新的主进程和新的工作进程：
```
  PID  PPID USER    %CPU   VSZ WCHAN  COMMAND
33126     1 root     0.0  1164 pause  nginx: master process /usr/local/nginx/sbin/nginx
33134 33126 nobody   0.0  1368 kqread nginx: worker process (nginx)
33135 33126 nobody   0.0  1380 kqread nginx: worker process (nginx)
33136 33126 nobody   0.0  1368 kqread nginx: worker process (nginx)
36264 33126 root     0.0  1148 pause  nginx: master process /usr/local/nginx/sbin/nginx
36265 36264 nobody   0.0  1364 kqread nginx: worker process (nginx)
36266 36264 nobody   0.0  1364 kqread nginx: worker process (nginx)
36267 36264 nobody   0.0  1364 kqread nginx: worker process (nginx)
```
在这时，两个 nginx 实例会同时运行，一起处理输入的请求。要逐步停止旧的实例，你必须发送 WINCH 信号给旧的主进程，然后，它的工作进程就将开始从容关闭：
```
  PID  PPID USER    %CPU   VSZ WCHAN  COMMAND
33126     1 root     0.0  1164 pause  nginx: master process /usr/local/nginx/sbin/nginx
33135 33126 nobody   0.0  1380 kqread nginx: worker process is shutting down (nginx)
36264 33126 root     0.0  1148 pause  nginx: master process /usr/local/nginx/sbin/nginx
36265 36264 nobody   0.0  1364 kqread nginx: worker process (nginx)
36266 36264 nobody   0.0  1364 kqread nginx: worker process (nginx)
36267 36264 nobody   0.0  1364 kqread nginx: worker process (nginx)
```
一段时间后，旧的工作进程处理了所有已连接的请求后退出，就仅由新的工作进程来处理输入的请求了：
```
  PID  PPID USER    %CPU   VSZ WCHAN  COMMAND
33126     1 root     0.0  1164 pause  nginx: master process /usr/local/nginx/sbin/nginx
36264 33126 root     0.0  1148 pause  nginx: master process /usr/local/nginx/sbin/nginx
36265 36264 nobody   0.0  1364 kqread nginx: worker process (nginx)
36266 36264 nobody   0.0  1364 kqread nginx: worker process (nginx)
36267 36264 nobody   0.0  1364 kqread nginx: worker process (nginx)
```
这时，因为旧的服务器还尚未关闭它监听的套接字，所以，通过下面的几步，你仍可以恢复旧的服务器：

- 发送 HUP 信号给旧的主进程 - 它将在不重载配置文件的情况下启动它的工作进程
- 发送 QUIT 信号给新的主进程，要求其从容关闭其工作进程
- 发送 TERM 信号给新的主进程，迫使其退出
- 如果因为某些原因新的工作进程不能退出，向其发送 KILL 信号.
新的主进程退出后，旧的主进程会由移除 .oldbin 前缀，恢复为它的 .pid 文件，这样，一切就都恢复到升级之前了。

如果尝试升级成功，而你也希望保留新的服务器时，发送 QUIT 信号给旧的主进程使其退出而只留下新的服务器运行：
```
     PID  PPID USER    %CPU   VSZ WCHAN  COMMAND
    36264     1 root     0.0  1148 pause  nginx: master process /usr/local/nginx/sbin/nginx
    36265 36264 nobody   0.0  1364 kqread nginx: worker process (nginx)
    36266 36264 nobody   0.0  1364 kqread nginx: worker process (nginx)
    36267 36264 nobody   0.0  1364 kqread nginx: worker process (nginx)
```

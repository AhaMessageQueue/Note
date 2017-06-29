我们平时都会用`service xxx start`来启动某个进程，那么它背后究竟执行了什么？

其实`service`的绝对路径为`/sbin/service`，打开这个文件`cat /sbin/service`，我们会发现其实它就是一个很普通的shell脚本：

```
[root@zejin240 ~]# cat /sbin/service
#!/bin/sh
 
. /etc/init.d/functions
 
VERSION="$(basename $0) ver. 0.91"
USAGE="Usage: $(basename $0) < option > | --status-all | \
[ service_name [ command | --full-restart ] ]"
SERVICE=
SERVICEDIR="/etc/init.d"
OPTIONS=
 
if [ $# -eq 0 ]; then
echo "${USAGE}" >&2
exit 1
fi
……
一些参数条件判断
if [ -f "${SERVICEDIR}/${SERVICE}" ]; then
env -i PATH="$PATH" TERM="$TERM" "${SERVICEDIR}/${SERVICE}" ${OPTIONS}
else
echo $"${SERVICE}: unrecognized service" >&2
exit 1
fi
```

其实这个脚本`service`主要作了如下两点：

1. 初始化执行环境变量PATH和TERM
    
      PATH=/sbin:/usr/sbin:/bin:/usr/bin
      TERM，为显示外设的值，一般为xterm
      
2. 调用/etc/init.d/文件夹下的相应脚本，脚本的参数为service命令第二个及之后的参数

    以service mysqld restart为例
    那么mysqld为/etc/init.d/下面的一个可执行文件，我们可以看到
    ```
    [root@zejin240 chenzejin]# ll /etc/init.d/mysql
    -rwxr-xr-x. 1 root root 10815 Jan 14 2014 /etc/init.d/mysql
    ```
    restart为参数，将传递给mysqld脚本
    
    这个命令在service执行到后面最终调用的是：
    ```
    env -i PATH="$PATH" TERM="$TERM" "${SERVICEDIR}/${SERVICE}" ${OPTIONS}
    ```
    
    相当于执行了如下命令
    ```
    /etc/init.d/mysqld restart
    ```
    
    类似的，如果你的执行命令为`service mysqld stop start`，那么就相当于执行了
    ```
    /etc/init.d/mysqld stop start
     ```
    至于命令执行成不成功，就看你脚本支不支持多个参数的调用。
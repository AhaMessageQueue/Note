top之前一直都是一知半解，今天周末加班，我的工作已经完成，在等同事吃饭，就把这个写下来。

![](\images\top.png)

**第一行：**

top - 20:42:47 up 57 days,  1:25,  4 users,  load average: 0.00, 0.00, 0.00

系统当前时间20:42:47。系统启动了57 days, 1:25分钟。4个用户终端在线。1分钟前、5分钟前、15分钟前进程的平均数

**第二行：**

Tasks: 199 total,   1 running, 198 sleeping,   0 stopped,   0 zombie

不用解释。一个前端进程被ctrl+z，变成stopped，同时可以用kill -STOP 1234产生效果。可以用fg恢复到前台，也可以用bg恢复到后台，也可以用kill -CONT 1234恢复。在STOPPED状态下，可以kill进程。

**第三行：**

Cpu(s):  3.4%us,  0.8%sy,  0.5%ni, 94.8%id,  0.0%wa,  0.0%hi,  0.6%si,  0.0%st 这都是占用时长的百分比

3.4的用户进程空间内被nice过的进程的执行时间，0.8的内核空间执行时间，0.5的用户进程空间内被nice过的进程的执行时间，
94.8 idle，0.0的等待IO时间，0.6的hardware interruption时间，0.0的software interruption时间。

**第四/五行：**

KiB Mem:   8176784 total,  7860540 used,   316244 free,   285000 buffers
KiB Swap:        0 total,        0 used,        0 free.  2615632 cached Mem

cached和Swap没关系，就是Mem里边cache住的。used值包括buffers和cached，真正的used是这个used减去(buffers+cached)，
而这个free是减去(buffers+cached)后的free，真正的free是加上(buffers+cached)的，因为(buffers+cached)是用来缓存程序可能用到的内容。

```
[root@localhost ~]# free -m (拿这个来说，第二行的-/+后的结果才是真正的used和free)
             total       used       free     shared    buffers     cached
Mem:         24030       8939      15091          0        192       3641
-/+ buffers/cache:       5105      18925
```

具体buffers和cache，buffers不仅存着数据，还会存数据的来源、权限，跟踪去处。cache只存数据。见参考    

**第六行：**

PID,USER进程号，用户名。

NI，nice，动态修正CPU调度。范围（-20~19）。越大，cpu调度越一般，越小，cpu调度越偏向它。一般用于后台进程，调整也是往大了调，用来给前台进程让出CPU资源。

PR：优先级，会有两种格式，一种是数字（默认20），一种是RT字符串。

PR默认是20，越小，优先级越高。修改nice可以同时修改PR，测试过程：先开一个窗口，运行wc，另开一个窗口运行top，按N按照PID倒序排，按r输入要renice的PID，然后输入-19~20之间的值，可以看到NI变成输入的值，PR=PR+NI。修改NI得到PR的范围是0~39。

VIRT：一个进程瞬时可以访问的所有内存总和大小，包括RES自己在使用的，共享的类库，和其他进程共享的内存，内存中的文件数据。共享的类库，一个大文件，只有一个程序片段被用到，这个文件会被 map到VIRT和SHR中，程序片段会在RES中。

S：状态S -- Process Status. The status of the task which can be one of:
```
D Uninterruptible sleep (usually IO)
R Running or runnable (on run queue)
S Interruptible sleep (waiting for an event to complete)
T Stopped, either by a job control signal or because it is being traced.
W paging (not valid since the 2.6.xx kernel)
X dead (should never be seen)
Z Defunct ("zombie") process, terminated but not reaped by its parent.
```

%CPU，总体CPU百分比，按H可以显示所有线程。8个核，从0~800%。

%mem，RES占总MEM的百分比

TIME+，自启动到现在占用的CPU时间。
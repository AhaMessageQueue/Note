# Java线程池的监控

如果想实现线程池的监控，必须要自定义线程池继承ThreadPoolExecutor类，
并且实现beforeExecute，afterExecute和terminated方法，我们可以在任务执行前，执行后和线程池关闭前干一些事情。
如监控任务的平均执行时间，最大执行时间和最小执行时间等。
这几个方法在线程池里是空方法。如：

```
//每执行一个工作任务线程之前都会执行此实现的方法
protected void beforeExecute(Thread t, Runnable r) {

//t – 放在线程池里面要执行的线程。
//r – 将要执行这个线程的线程池里面的工作线程。

}

//每执行一个工作任务线程之后都会执行的方法

protected void afterExecute(Runnable r, Throwable t) {

//r – 已经运行结束的工作线程。
//t – 运行异常。

}

//线程池关闭之前可以干一些事情；

protected void terminated() { };


```

### 线程池里有一些属性在监控线程池的时候可以使用:

1. taskCount：线程池需要执行的任务数量。
2. completedTaskCount：线程池在运行过程中已完成的任务数量。小于或等于taskCount。
3. largestPoolSize：线程池曾经创建过的最大线程数量。通过这个数据可以知道线程池是否满过。如等于线程池的最大大小，则表示线程池曾经满了。
4. getPoolSize:线程池的线程数量。
5. getActiveCount：获取活动的线程数。

### 大家想一想如果你来写的话如何去写，提供实例demo如下，慢慢体会一下：
```

public class MonitorThreadPoolExecutorDemo {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        Thread. sleep(500L);// 方便测试

        ExecutorService executor = new MonitorThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS , new LinkedBlockingQueue() );

        for (int i = 0; i < 3; i++) {
            Runnable runnable = new Runnable() {

                public void run() {
                    try {
                        Thread. sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            executor.execute(runnable);
        }
        executor.shutdown();
        System. out.println(“Thread Main End!” );
    }
}

class MonitorThreadPoolExecutor extends ThreadPoolExecutor {

    public MonitorThreadPoolExecutor(int arg0, int arg1, long arg2, TimeUnit arg3, BlockingQueue<Runnable> arg4) {
        super(arg0, arg1, arg2, arg3, arg4);
    }

    protected void beforeExecute(Thread paramThread, Runnable paramRunnable) {
        System. out.println(“work_task before:” + paramThread.getName());
    }

    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        System. out.println(“work_task after worker thread is :” + r);
    }

    protected void terminated() {
        System. out.println(“terminated getCorePoolSize:” + this.getCorePoolSize() + “；getPoolSize:” + this.getPoolSize() + “；getTaskCount:” + this .getTaskCount() + “；getCompletedTaskCount:”
        + this.getCompletedTaskCount() + “；getLargestPoolSize:” + this.getLargestPoolSize() + “；getActiveCount:” + this.getActiveCount());
        System. out.println(“ThreadPoolExecutor terminated:” );
    }
}

```


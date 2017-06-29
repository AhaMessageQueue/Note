## 线程池监控

如果想实现**线程池的监控**，必须要**自定义线程池继承ThreadPoolExecutor类**，
并且实现`beforeExecute`，`afterExecute`和`terminated`方法，我们可以在任务执行前，执行后和线程池关闭前干一些事情。

如监控任务的平均执行时间，最大执行时间和最小执行时间等。

这几个方法在线程池里是空方法。如：
```java
//每执行一个工作任务线程之前都会执行的方法
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

### 线程池属性

线程池里有一些属性，在监控线程池的时候可以使用:

1. `taskCount`：线程池需要执行的**任务数量**。
2. `completedTaskCount`：线程池在运行过程中**已完成的任务数量**。小于或等于`taskCount`。
3. `largestPoolSize`：线程池**曾经创建过的最大线程数量**。
    通过这个数据可以知道线程池是否满过。如等于线程池的最大大小，则表示线程池曾经满了。
4. `getPoolSize`:线程池的线程数量。
5. `getActiveCount`：获取活动的线程数。

### 示例 

大家想一想如果你来写的话如何去写，提供实例demo如下，慢慢体会一下：
```java
public static class MonitorThreadPoolExecutor extends ThreadPoolExecutor {

    private boolean isDebug = false;//是否打印debug日志

    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial
     * parameters and default rejected execution handler.
     *
     * @param corePoolSize    the number of threads to keep in the pool, even
     *                        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *                        pool
     * @param keepAliveTime   when the number of threads is greater than
     *                        the core, this is the maximum time that excess idle threads
     *                        will wait for new tasks before terminating.
     * @param unit            the time unit for the {@code keepAliveTime} argument
     * @param workQueue       the queue to use for holding tasks before they are
     *                        executed.  This queue will hold only the {@code Runnable}
     *                        tasks submitted by the {@code execute} method.
     * @param threadFactory   the factory to use when the executor
     *                        creates a new thread
     * @throws IllegalArgumentException if one of the following holds:<br>
     *                                  {@code corePoolSize < 0}<br>
     *                                  {@code keepAliveTime < 0}<br>
     *                                  {@code maximumPoolSize <= 0}<br>
     *                                  {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException     if {@code workQueue}
     *                                  or {@code threadFactory} is null
     */
    public MonitorThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public MonitorThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, boolean isDebug) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.isDebug = isDebug;
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        if (isDebug) {
            logger.info("Thread[ " + r + "] Execute Start, Thread Pool Status -  [CorePoolSize: " + this.getCorePoolSize() + "； PoolSize: " + this.getPoolSize() + "； TaskCount: " + this.getTaskCount() + "； CompletedTaskCount: "
                    + this.getCompletedTaskCount() + "； LargestPoolSize: " + this.getLargestPoolSize() + "； ActiveCount: " + this.getActiveCount() + "]");
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (isDebug) {
            if (t != null) {
                logger.error("Thread[ " + r + "] Execute End，Throw Exception:" + t + ", Thread Pool Status -  [CorePoolSize: " + this.getCorePoolSize() + "； PoolSize: " + this.getPoolSize() + "； TaskCount: " + this.getTaskCount() + "； CompletedTaskCount: "
                        + this.getCompletedTaskCount() + "； LargestPoolSize: " + this.getLargestPoolSize() + "； ActiveCount: " + this.getActiveCount() + "]");

            }
        }
    }

    @Override
    protected void terminated() {
        super.terminated();
    }
}
```


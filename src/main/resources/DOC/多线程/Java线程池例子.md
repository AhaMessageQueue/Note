在做很多高并发应用的时候，单线程的瓶颈已经满足不了我们的需求，此时使用多线程来提高处理速度已经是比较常规的方案了。在使用多线程的时候，我们可以使用线程池来管理我们的线程，至于使用线程池的优点就不多说了。

对于多线程的线程安全处理，这个也非常重要，有些同学还是要多补补课。

Java线程池说起来也简单，简单说下继承关系：
ThreadPoolExecutor extends AbstractExecutorService implements ExecutorService extends Executor

还有一个支持延时执行线程和可以重复执行线程的实现类：
ScheduledThreadPoolExecutor extends ThreadPoolExecutor implements ScheduledExecutorService

大家把这些类中的相关方法弄清楚，使用线程池就不在话下了。其实弄清楚里面各个方法的功能也就够了。
最重要的还是在实践中总结经验，企业需要的是能实际解决问题的人。

下面是我写的一个例子，包括3个Java文件，分别是：
ExecutorServiceFactory.java
ExecutorProcessPool.java
ExecutorTest.java

下面贴出代码：
### ExecutorServiceFactory.java
```
package com.kingsoft.wps.calendar.api.common.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liuchunlong on 16-12-16.
 * 线程池构造工厂
 */
public class ExecutorServiceFactory {

    private static ExecutorServiceFactory executorFactory = new ExecutorServiceFactory();

    /**
     * 线程池
     */
    private ExecutorService executors;

    private ExecutorServiceFactory() {

    }

    /**
     * 获取ExecutorServiceFactory
     * @return
     */
    public static ExecutorServiceFactory getInstance() {
        return executorFactory;
    }

    /**
     * 创建一个线程池，它可安排在给定延迟后运行命令或者定期地执行。
     */
    public ExecutorService createScheduledThreadPool() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();// CPU个数
        executors = Executors.newScheduledThreadPool(availableProcessors * 10, getThreadFactory());
        return executors;
    }

    /**
     * 创建一个使用单个 worker 线程的Executor，以无界队列方式来运行该线程。
     * （注意，如果因为在关闭前的执行期间出现失败而终止了此单个线程，
     * 那么如果需要，一个新线程将代替它执行后续的任务）。可保证顺序地执行各个任务，并且在任意给定的时间不会有多个线程是活动的。与其他等效的
     * newFixedThreadPool(1) 不同，可保证无需重新配置此方法所返回的执行程序即可使用其他的线程。
     */
    public ExecutorService createSingleThreadExecutor() {
        executors = Executors.newSingleThreadExecutor(getThreadFactory());
        return executors;
    }


    /**
     * 创建一个可根据需要创建新线程的线程池，但是在以前构造的线程可用时将重用它们。对于执行很多短期异步任务的程序而言，这些线程池通常可提高程序性能。调用
     * execute 将重用以前构造的线程（如果线程可用）。如果现有线程没有可用的，则创建一个新线程并添加到池中。终止并从缓存中移除那些已有 60
     * 秒钟未被使用的线程。因此，长时间保持空闲的线程池不会使用任何资源。注意，可以使用 ThreadPoolExecutor
     * 构造方法创建具有类似属性但细节不同（例如超时参数）的线程池。
     */
    public ExecutorService createCachedThreadPool() {
        // 创建
        executors = Executors.newCachedThreadPool(getThreadFactory());
        return executors;
    }

    /**
     * 创建一个可重用固定线程数的线程池，以共享的无界队列方式来运行这些线程。在任意点，在大多数 nThreads
     * 线程会处于处理任务的活动状态。如果在所有线程处于活动状态时提交附加任务
     * ，则在有可用线程之前，附加任务将在队列中等待。如果在关闭前的执行期间由于失败而导致任何线程终止
     * ，那么一个新线程将代替它执行后续的任务（如果需要）。在某个线程被显式地关闭之前，池中的线程将一直存在。
     */
    public ExecutorService createFixedThreadPool(int count) {
        executors = Executors.newFixedThreadPool(count, getThreadFactory());
        return executors;
    }

    /**
     * 获取线程池工厂
     */
    private ThreadFactory getThreadFactory() {
        return new ThreadFactory() {
            AtomicInteger index = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {

                SecurityManager securityManager = System.getSecurityManager();
                ThreadGroup threadGroup = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
                Thread thread = new Thread(threadGroup, r);
                thread.setName("任务线程 - " + index.incrementAndGet());
                return thread;
            }
        };
    }
}

```

### ExecutorProcessPool.java
```
package com.kingsoft.wps.calendar.api.common.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by liuchunlong on 16-12-16.
 * 线程处理类
 */
public class ExecutorProcessPool {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorProcessPool.class);

    private static ExecutorProcessPool pool = new ExecutorProcessPool();

    private ExecutorService executor;
    private final int threadMax = 10;//最大线程数

    private ExecutorProcessPool() {
        logger.info("[Thread] - 最大线程数:" + threadMax);
        executor = ExecutorServiceFactory.getInstance().createFixedThreadPool(threadMax);
    }

    public static ExecutorProcessPool getInstance() {
        return pool;
    }

    /**
     * 关闭线程池，这里要说明的是：调用关闭线程池方法后，线程池会执行完队列中的所有任务才退出
     */
    public void shutdown(){
        executor.shutdown();
    }

    /**
     * 提交任务到线程池，可以接收线程返回值
     * @param task
     * @return
     */
    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    /**
     * 提交任务到线程池，可以接收线程返回值
     * @param task
     * @return
     */
    public Future<?> submit(Callable<?> task) {
        return executor.submit(task);
    }

    /**
     * 直接提交任务到线程池，无返回值
     * @param task
     */
    public void execute(Runnable task){
        executor.execute(task);
    }
}

```
### ExecutorDemo.java
```
package com.kingsoft.wps.calendar.api.common.threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by liuchunlong on 16-12-16.
 * 线程池使用Demo
 */
public class ExecutorDemo {

    public static void main(String[] args) {
        ExecutorProcessPool pool = ExecutorProcessPool.getInstance();

        for (int i = 0; i < 200; i++) {
            Future<?> future = pool.submit(new CallableTask(i + ""));
            try {
                //如果接收线程返回值，future.get() 会阻塞，如果这样写就是一个线程一个线程执行。所以非特殊情况不建议使用接收返回值的
                System.out.println(future.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < 200; i++) {
            pool.execute(new RunnableTask(i+""));
        }

        //关闭线程池，如果是需要长期运行的线程池，不用调用该方法。
        //监听程序退出的时候最好执行一下。
        pool.shutdown();
    }

    /**
     * 实现Callable方式
     */
    static class CallableTask implements Callable {

        private String taskName;

        public CallableTask(String taskName) {
            this.taskName = taskName;
        }

        @Override
        public Object call() throws Exception {

            //Java 6/7最佳的休眠方法为TimeUnit.MILLISECONDS.sleep(100);
            //最好不要用 Thread.sleep(100);
            TimeUnit.MILLISECONDS.sleep((long)Math.random() * 1000);

            return taskName;
        }
    }

    /**
     * 实现Runnable方式
     */
    static class RunnableTask implements Runnable {

        private String taskName;

        public RunnableTask(String taskName) {
            this.taskName = taskName;
        }

        @Override
        public void run() {

            //Java 6/7最佳的休眠方法为TimeUnit.MILLISECONDS.sleep(100);
            //最好不要用 Thread.sleep(100);
            try {
                TimeUnit.MILLISECONDS.sleep((long)Math.random() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}

```
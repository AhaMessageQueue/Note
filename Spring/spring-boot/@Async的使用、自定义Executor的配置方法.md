## 默认线程池

一、在主类中添加@EnableAsync注解

```java
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SpringBootFnApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootFnApplication.class, args);
	}
}
```

二、创建一个AsyncTask类，在里面添加两个用@Async注解的task：

```java
package com.github.fnpac.fn.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.concurrent.Future;

/**
 * Created by 刘春龙 on 2017/8/3.
 */
public class AsyncTask {

    protected static final Logger logger = LoggerFactory.getLogger(AsyncTask.class);

    @Async
    public Future<String> doTask1() throws InterruptedException {

        logger.info("Task1 started.");

        long start = System.currentTimeMillis();
        Thread.sleep(5000);
        long end = System.currentTimeMillis();

        logger.info("Task1 finished, time elapsed: {} ms.", end - start);

        return new AsyncResult<>("Task1 accomplished!");
    }

    @Async
    public Future<String> doTask2() throws InterruptedException {

        logger.info("Task2 started.");

        long start = System.currentTimeMillis();
        Thread.sleep(3000);
        long end = System.currentTimeMillis();

        logger.info("Task2 finished, time elapsed: {} ms.", end - start);

        return new AsyncResult<>("Task2 accomplished!");
    }
}
```

三、万事俱备，开始测试：

```java
package com.github.fnpac.fn;

import com.github.fnpac.fn.task.AsyncTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootFnApplicationTests {

	protected static final Logger logger = LoggerFactory.getLogger(SpringBootFnApplicationTests.class);
	
	@Autowired
	private AsyncTask asyncTask;

	@Test
	public void contextLoads() {
	}

	@Test
	public void asyncTaskTest() throws InterruptedException, ExecutionException {
		Future<String> task1 = asyncTask.doTask1();
		Future<String> task2 = asyncTask.doTask2();

		while(true) {
			if(task1.isDone() && task2.isDone()) {
				logger.info("Task1 result: {}", task1.get());
				logger.info("Task2 result: {}", task2.get());
				break;
			}
			Thread.sleep(1000);
		}

		logger.info("All tasks finished.");
	}
}
```

测试结果：

```text
12:58:16.501 [main] INFO  o.s.s.a.AnnotationAsyncExecutionInterceptor - No task executor bean found for async processing: no bean of type TaskExecutor and no bean named 'taskExecutor' either
12:58:16.554 [SimpleAsyncTaskExecutor-1] INFO  com.github.fnpac.fn.task.AsyncTask - Task1 started.
12:58:16.556 [SimpleAsyncTaskExecutor-2] INFO  com.github.fnpac.fn.task.AsyncTask - Task2 started.
12:58:19.563 [SimpleAsyncTaskExecutor-2] INFO  com.github.fnpac.fn.task.AsyncTask - Task2 finished, time elapsed: 3001 ms.
12:58:21.563 [SimpleAsyncTaskExecutor-1] INFO  com.github.fnpac.fn.task.AsyncTask - Task1 finished, time elapsed: 5001 ms.
12:58:22.514 [main] INFO  c.g.i.f.SpringBootFnApplicationTests - Task1 result: Task1 accomplished!
12:58:22.515 [main] INFO  c.g.i.f.SpringBootFnApplicationTests - Task2 result: Task2 accomplished!
12:58:22.515 [main] INFO  c.g.i.f.SpringBootFnApplicationTests - All tasks finished.
```

可以看到，没有自定义的Executor，所以使用缺省的TaskExecutor。

前面是最简单的使用方法。如果想使用自定义的Executor，可以按照如下几步来：

## 自定义线程池

一、新建一个Executor配置类，顺便把@EnableAsync注解搬到这里来：

```java
package com.github.fnpac.fn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by 刘春龙 on 2017/8/3.
 */
@Configuration
@EnableAsync
public class FnExecutorConfigurer {

    /**
     * Set the ThreadPoolExecutor's core pool size.
     */
    private int corePoolSize = 10;
    /**
     * Set the ThreadPoolExecutor's maximum pool size.
     */
    private int maxPoolSize = 200;
    /**
     * Set the capacity for the ThreadPoolExecutor's BlockingQueue.
     */
    private int queueCapacity = 10;

    @Bean
    public Executor fnAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("FnAsyncExecutor-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor fnAsyncPolicyExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("FnAsyncPolicyExecutor-");

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

这里定义了两个不同的Executor，第二个重新设置了pool已经达到max size时候的处理方法；同时指定了线程名字的前缀。

二、自定义Executor的使用：

```java
package com.github.fnpac.fn.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

/**
 * Created by 刘春龙 on 2017/8/3.
 */
@Component
public class AsyncTask {

    protected static final Logger logger = LoggerFactory.getLogger(AsyncTask.class);

    @Async("fnAsyncExecutor")
    public Future<String> doTask1() throws InterruptedException {

        logger.info("Task1 started.");

        long start = System.currentTimeMillis();
        Thread.sleep(5000);
        long end = System.currentTimeMillis();

        logger.info("Task1 finished, time elapsed: {} ms.", end - start);

        return new AsyncResult<>("Task1 accomplished!");
    }

    @Async("fnAsyncPolicyExecutor")
    public Future<String> doTask2() throws InterruptedException {

        logger.info("Task2 started.");

        long start = System.currentTimeMillis();
        Thread.sleep(3000);
        long end = System.currentTimeMillis();

        logger.info("Task2 finished, time elapsed: {} ms.", end - start);

        return new AsyncResult<>("Task2 accomplished!");
    }
}
```

就是把上面自定义Executor的类名，放进@Async注解中。 

三、测试（测试用例不变）结果：

```text
12:59:57.773 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Returning cached instance of singleton bean 'asyncExecutor'
12:59:57.777 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Returning cached instance of singleton bean 'policyExecutor'
12:59:57.796 [PolicyExecutor-1] INFO  com.github.fnpac.fn.task.AsyncTask - Task2 started.
12:59:57.797 [AsyncExecutor-1] INFO  com.github.fnpac.fn.task.AsyncTask - Task1 started.
13:00:00.797 [PolicyExecutor-1] INFO  com.github.fnpac.fn.task.AsyncTask - Task2 finished, time elapsed: 3001 ms.
13:00:02.801 [AsyncExecutor-1] INFO  com.github.fnpac.fn.task.AsyncTask - Task1 finished, time elapsed: 5001 ms.
13:00:03.780 [main] INFO  c.g.i.f.SpringBootFnApplicationTests - Task1 result: Task1 accomplished!
13:00:03.780 [main] INFO  c.g.i.f.SpringBootFnApplicationTests - Task2 result: Task2 accomplished!
13:00:03.780 [main] INFO  c.g.i.f.SpringBootFnApplicationTests - All tasks finished.
```

可见，线程名字的前缀变了，两个task使用了不同的线程池了。 
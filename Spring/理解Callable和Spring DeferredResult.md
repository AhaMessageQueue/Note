## 介绍

**Servlet 3.0**中的异步支持为在另一个线程中处理HTTP请求提供了可能性。当有一个长时间运行的任务时，这是特别有趣的，因为当另一个线程处理这个请求时，容器线程被释放，并且可以继续为其他请求服务。
这个主题已经解释了很多次，Spring框架提供的关于这个功能的类似乎有一点混乱——在一个Controller中返回`Callable`和`DeferredResult`。

在这篇文章中，我将实施这两个例子，以显示其差异。
这里所显示的所有示例都包括执行一个控制器，该控制器将执行一个长期运行的任务，然后将结果返回给客户机。

长时间运行的任务由taskservice处理：

```java
package com.github.fnpac.async;

import org.springframework.stereotype.Service;

import java.util.logging.Logger;

/**
 * Created by 刘春龙 on 2018/2/27.
 */
@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = Logger.getLogger(TaskServiceImpl.class.getName());

    public String execute() {
        try {
            Thread.sleep(5000);
            logger.info("Slow task executed");
            return "Task finished";
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
    }
}
```

## 阻塞的Controller

在这个例子中，一个请求到达控制器。servlet线程不会被释放，直到长时间运行的方法被执行，才会退出`@requestmapping`注释的方法。

```java
package com.github.fnpac.async;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

/**
 * Created by 刘春龙 on 2018/2/27.
 */
@RestController
public class BlockingController {

    private static final Logger logger = Logger.getLogger(BlockingController.class.getName());

    private final TaskService taskService;

    @Autowired
    public BlockingController(TaskService taskService) {
        this.taskService = taskService;
    }

    @RequestMapping(value = "/block", method = RequestMethod.GET, produces = "text/html")
    public String executeSlowTask() {
        logger.info("Request received");
        String result = taskService.execute();
        logger.info("Servlet thread released");
        return result;
    }
}
```

如果我们运行这个例子`http://localhost:8080/websocketApp/block`，在日志里我们会发现Servlet request不会被释放，直到长时间的任务执行完（5秒后）。

```text
11:18:18.796 [http-bio-8080-exec-1] INFO  c.g.fnpac.async.BlockingController - Request received
11:18:23.800 [http-bio-8080-exec-1] INFO  c.github.fnpac.async.TaskServiceImpl - Slow task executed
11:18:23.801 [http-bio-8080-exec-1] INFO  c.g.fnpac.async.BlockingController - Servlet thread released
```

## 返回Callable

在这个例子中，不是直接返回的结果，我们将返回一个`Callable`：

```java
package com.github.fnpac.async;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Created by 刘春龙 on 2018/2/27.
 */
@RestController
public class AsyncCallableController {

    private static final Logger logger = Logger.getLogger(AsyncCallableController.class.getName());

    private final TaskService taskService;

    @Autowired
    public AsyncCallableController(TaskService taskService) {
        this.taskService = taskService;
    }

    @RequestMapping(value = "/callable", method = RequestMethod.GET, produces = "text/html")
    public Callable<String> executeSlowTask() {
        logger.info("Request received");
        Callable<String> runnable = taskService::execute;
        logger.info("Servlet thread released");
        return runnable;
    }
}
```

返回`Callable`意味着Spring MVC将在不同的线程中执行定义的任务。
Spring将使用`TaskExecutor`来管理线程。在等待完成的长期任务之前，servlet线程将被释放。

```text
11:19:23.088 [http-bio-8080-exec-2] INFO  c.g.f.async.AsyncCallableController - Request received
11:19:23.091 [http-bio-8080-exec-2] INFO  c.g.f.async.AsyncCallableController - Servlet thread released
11:19:28.111 [MvcAsync1] INFO  c.github.fnpac.async.TaskServiceImpl - Slow task executed
```

你可以看到我们在长时间运行的任务执行完毕之前就已经从servlet返回了。这并不意味着客户端收到了一个响应。
与客户端的通信仍然是开放的等待结果，但接收到的请求的线程已被释放，并可以服务于另一个客户的请求。

## 返回DeferredResult

首先，我们需要创建一个`DeferredResult`对象。此对象将由控制器返回。
我们将完成和`Callable`相同的事，当我们在另一个线程处理长时间运行的任务的时候释放servlet线程。

```java
package com.github.fnpac.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.CompletableFuture;

/**
 * Created by 刘春龙 on 2018/2/27.
 */
@RestController
public class AsyncDeferredController {

    private static final Logger logger = LoggerFactory.getLogger(AsyncDeferredController.class);

    private final TaskService taskService;

    @Autowired
    public AsyncDeferredController(TaskService taskService) {
        this.taskService = taskService;
    }

    @RequestMapping(value = "/deferred/v1", method = RequestMethod.GET, produces = "text/html")
    public DeferredResult<String> executeSlowTask() {
        logger.info("Request received");
        DeferredResult<String> deferredResult = new DeferredResult<>();
        CompletableFuture.supplyAsync(taskService::execute)
                .whenCompleteAsync((result, throwable) -> deferredResult.setResult(result));
        return deferredResult;
    }
}
```

所以，返回`DeferredResult`和返回`Callable`有什么区别？

不同的是这一次线程是由我们管理。创建一个线程并将结果set到`DeferredResult`是由我们自己来做的。

用`CompletableFuture`创建一个异步任务。这将创建一个新的线程，在那里我们的长时间运行的任务将被执行。
也就是在这个线程中，我们将set结果到DeferredResult并返回。

是在哪个线程池中我们取回这个新的线程？

默认情况下，在`CompletableFuture`的`supplyAsync`方法将在forkjoin池运行任务。
如果你想使用一个不同的线程池，你可以通过传一个executor到`supplyAsync`方法：

```java
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
```

如果我们运行这个例子，我们将得到如下结果：

```text
12:26:37.594 [http-bio-8080-exec-1] INFO  c.g.f.async.AsyncDeferredController - Request received
12:26:37.613 [http-bio-8080-exec-1] INFO  c.g.f.async.AsyncDeferredController - Servlet thread released
12:26:42.611 [ForkJoinPool.commonPool-worker-1] INFO  c.g.f.async.service.TaskServiceImpl - Slow task executed
```

## 结论

站在一定高度来看这问题，`Callable`和`Deferredresult`做的是同样的事情——释放容器线程，在另一个线程上异步运行长时间的任务。
不同的是谁管理执行任务的线程。
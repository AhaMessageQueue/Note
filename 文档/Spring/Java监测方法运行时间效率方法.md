### 前言：
这周在写一个小项目，虽然小但是是纯调外部接口的，调完了接口还不停的循环接口返回的数据
（已转换JSONArray），然后再判断值，再做不同处理，关键是数据量还比较大，这刚做完还没
开始上线，测试也还没开始测呢，就想着自己先看看每个方法运行效率，省的数据大了项目挂掉
（循环判断好多，有时还有2个for嵌套循环），就是纯粹在时间上进行监测，没有内存和cpu的监控。

主要利用了Spring AOP 技术，对想要统计的方法进行横切处理，方法执行前开始计时，方法
执行后停止计时，得到计时方法就是该方法本次消耗时间。

### 步骤：

- 首先编写自己的Interceptor类来实现MethodInterceptor类，来用于切入方法，运行计时代码
- Spring AOP 的XML配置，配置需要监测的方法和切入方法（自定义的Interceptor）

### 代码
```
package com.cplatform.tencent.task;

import java.util.HashMap;
import java.util.Map;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.time.StopWatch;


public class MethodTimeActive implements MethodInterceptor {
    /**
     * 自定义map集合，key：方法名，value：[0：运行次数，1：总时间]
     */
    public static Map<String,Long[]> methodTest = new HashMap<String, Long[]>();
    /**
     * 拦截要执行的方法
     */
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // 创建一个计时器
        StopWatch watch = new StopWatch();
        // 计时器开始
        watch.start();
        // 执行方法
        Object object = invocation.proceed();
        // 计时器停止
        watch.stop();
        // 方法名称
        String methodName = invocation.getMethod().getName();
        // 获取计时器计时时间
        Long time = watch.getTime();
        if(methodTest.containsKey(methodName)) {
            Long[] x = methodTest.get(methodName);
            x[0]++;
            x[1] += time;
        }else{
            methodTest.put(methodName, new Long[] {1L,time});
        }
        return object;
    }

}
```
### XML配置
```
<bean id="methodTimeAdvice" class="com.cplatform.tencent.task.MethodTimeActive"/>
<!-- 日志记录某个类中方法花费时间aop -->
<aop:config>
    <!-- Spring 2.0 可以用 AspectJ 的语法定义 Pointcut，这里自定义要拦截方法的包所在 -->
    <aop:advisor id="methodTimeLog" advice-ref="methodTimeAdvice" pointcut="execution(* com.cplatform.tencent.sync..*.*(..))"/>
    <aop:advisor id="methodTimeLog2" advice-ref="methodTimeAdvice" pointcut="execution(* com.cplatform.tencent.utils..*.*(..))"/>
</aop:config>
```
使用AOP面向切面技术时，XML配置里面千万别忽略了以下配置：
![](http://images.cnitblog.com/blog/679050/201411/161559477121554.jpg)

### 使用
```
package test;

import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.cplatform.tencent.sync.PersistenceTicketService;
import com.cplatform.tencent.task.MethodTimeActive;
import com.cplatform.tencent.utils.AppConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-configuration/*.xml"})
@ActiveProfiles("production")
public class CopyOfDBTest {

    @Autowired
        private AppConfig appConfig;    // 这是我项目里用到的  可忽略
        @Autowired
        private PersistenceTicketService persistenceTicketService;

        // 测试某个方法，这个方法内部调用了很多业务处理方法
        @Test
        public void testInsertOrUpdate() {
            persistenceTicketService.insertOrUpdate(appConfig.getTicketCityIds(), appConfig.getAgentId());
        }

        // 测试方法运行完毕后，取出定义的Map集合，取出数据
        @After
        public void testMethodActive() {
            Map<String, Long[]> map = MethodTimeActive.methodTest;
            Set<String> set = map.keySet();
            Long[] x = null;
            for(String s : set) {
                x = map.get(s);
                System.out.println(s+":"+x[0]+"次，"+x[1]+"毫秒");
            }
        }

}
```
Spring 提供了几个帮助类用于在应用中做调度，包括JDK Timer类和OpenSymphony Quartz Scheduler两种.

# Quartz基础
Quartz包括五种主要结构用于实现调度：

Job接口 JobDetail类 Trigger 抽象类 Scheduler接口 SchedulerFactory 接口.
Job接口表示一个作业(job)。一个作业专注做一件事。它的API非常简洁。
只有一个execute方法，该方法在作业被执行时有Quartz调度。
该方法有一个JobExecuteContext参数，可以通过该参数给execute()方法传递有用信息。

```
public interface Job{
    void execute(JobExecuteContext ctx);
}
```

一些数据可以通过JobDataMap传递给作业。
如果一个JobDataMap被注册到JobDetail中，就能够在作业中通过 JobExecuteContext来访问。
JobDetail用来描述一个特定Job的信息。Job通过触发器（Trigger）触发。
Quartz提供了集中Trigger的实现，如SimpleTrigger和CronTrigger。
SimpleTrigger类似一个简单时钟，你可以定义开始时间，结束时间，重复次数，重复周期。
CronTrigger类似Linux系统中的cron。CronTrigger的设置可以非常详细，如在每个月最后一个周五的上午10:15执行作业。
需要注意的是Trigger和Job是具名的，可以被赋值给一个组，在同一组内不能出现同名。你可以对一个组创建一个 触发器，在该组内的所有Job都将会执行。
SchedulerFactory 用于获得Scheduler实例，可以用于注册作业和触发器。

实现一个简单的实例：每十秒钟打印一次欢迎。
首先实现一个作业：
```
public class SimpleJob implements Job {
    @Override
    public void execute(JobExecutionContext arg0)
    throws JobExecutionException {
        System.out.println("[JOB] Welcome to Quartz!");
    }
}
```

定义一个Scheduler，注册触发器和作业:
```
public class SimpleSchedule {
    public static void main(String[] args) {
        SchedulerFactory factory=new StdSchedulerFactory();
        try {
            Scheduler scheduler = factory.getScheduler();
            scheduler.start();

            JobDetail jobDetail = new JobDetail("SimpleJob",null, SimpleJob.class);
            Trigger simplerTrigger = TriggerUtils.makeSecondlyTrigger(10);
            simplerTrigger.setName("SimpleTrigger");

            scheduler.scheduleJob(jobDetail, simplerTrigger);
        }catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
```
执行过后，每十秒输出[JOB] Welcome to Quartz!

# Spring中的Quartz
Spring中的Quartz API位于org.springframework.scheduling.quartz包中。主要类结构包括：
```
QuartzJobBean 抽象类
JobDetailBean
SimpleTriggerBean
CronTriggerBean
SchedulerFactoryBean
MethodInvokingDetailFactoryBean
```
很明显对应实现Quartz中相应的接口。QuartzJob实现Job，JobDetailBean继承JobDetail。 SimpleTriggerBean和CronTriggerBean继承自相应的Trigger。
MethodInvokingJobDetailFactoryBean用于在类中调用任何对象的方法。

# 注意
一个触发器只能触发一个Job，不过一个Job可以有多个Trigger触发，这会带来并发问题。
在Quartz中，如果你不想并发执行一个同一个 Job，你可以实现StatefulJob，而不是Job。
在Spring中如果使用 MethodInvokingJobDetailFactoryBean，可以通过设置concurrent="false"属性来实现。

# 尾注
在Spring中使用Quartz而不是单独的一个应用的好处包括：

将所有的任务调度设置放在同一个地方，是任务易于维护。
只对Job编码，Trigger和Scheduler可以通过配置设置
可以使用Pojo Java Bean执行job，而无需实现Job接口

# Cron表达式的详细用法
```
字段 允许值 允许的特殊字符
秒 0-59 , - * /
分 0-59 , - * /
小时 0-23 , - * /
日期 1-31 , - * ? / L W C
月份 1-12 或者 JAN-DEC , - * /
星期 1-7 或者 SUN-SAT , - * ? / L C #
年（可选） 留空, 1970-2099 , - * /

例子：
0/5 * * * * ? ： 每5秒执行一次
```
“”字符被用来指定所有的值。如：""在分钟的字段域里表示“每分钟”。
“?”字符只在日期域和星期域中使用。它被用来指定“非明确的值”。当你需要通过在这两个域中的一个来指定一些东西的时候，它是有用的。看下面的例子你就会明白。
月份中的日期和星期中的日期这两个元素时互斥的一起应该通过设置一个问号来表明不想设置那个字段。

“-”字符被用来指定一个范围。如：“10-12”在小时域意味着“10点、11点、12点”。

“,”字符被用来指定另外的值。如：“MON,WED,FRI”在星期域里表示”星期一、星期三、星期五”。

“/”字符用于指定增量。如：“0/15”在秒域意思是每分钟的0，15，30和45秒。“5/15”在分钟域表示每小时的5，20，35和50。符号“”在“/”前面（如：/10）等价于0在“/”前面（如：0/10）。记住一条本质：表达式的每个数值域都是一个有最大值和最小值的集合，如：秒域和分钟域的集合是0-59，日期域是 1-31，月份域是1-12。字符“/”可以帮助你在每个字符域中取相应的数值。如：“7/6”在月份域的时候只有当7月的时候才会触发，并不是表示每个 6月。

L是‘last’的省略写法可以表示day-of-month和day-of-week域，但在两个字段中的意思不同，例如day-of- month域中表示一个月的最后一天。如果在day-of-week域表示‘7’或者‘SAT’，如果在day-of-week域中前面加上数字，它表示一个月的最后几天，例如‘6L’就表示一个月的最后一个星期五。

字符“W”只允许日期域出现。这个字符用于指定日期的最近工作日。例如：如果你在日期域中写 “15W”，表示：这个月15号最近的工作日。所以，如果15号是周六，则任务会在14号触发。如果15好是周日，则任务会在周一也就是16号触发。如果是在日期域填写“1W”即使1号是周六，那么任务也只会在下周一，也就是3号触发，“W”字符指定的最近工作日是不能够跨月份的。字符“W”只能配合一个单独的数值使用，不能够是一个数字段，如：1-15W是错误的。

“L”和“W”可以在日期域中联合使用，LW表示这个月最后一周的工作日。

字符“#”只允许在星期域中出现。这个字符用于指定本月的某某天。例如：“6#3”表示本月第三周的星期五（6表示星期五，3表示第三周）。“2#1”表示本月第一周的星期一。“4#5”表示第五周的星期三。

字符“C”允许在日期域和星期域出现。这个字符依靠一个指定的“日历”。也就是说这个表达式的值依赖于相关的“日历”的计算结果，如果没有“日历” 关联，则等价于所有包含的“日历”。如：日期域是“5C”表示关联“日历”中第一天，或者这个月开始的第一天的后5天。星期域是“1C”表示关联“日历” 中第一天，或者星期的第一天的后1天，也就是周日的后一天（周一）。


# 表达式举例
```
"0 0 12 * * ?" 每天中午12点触发
"0 15 10 ? * *" 每天上午10:15触发
"0 15 10 * * ?" 每天上午10:15触发
"0 15 10 * * ? *" 每天上午10:15触发
"0 15 10 * * ? 2005" 2005年的每天上午10:15触发
"0 * 14 * * ?" 在每天下午2点到下午2:59期间的每1分钟触发
"0 0/5 14 * * ?" 在每天下午2点到下午2:55期间的每5分钟触发
"0 0/5 14,18 * * ?" 在每天下午2点到2:55期间和下午6点到6:55期间的每5分钟触发
"0 0-5 14 * * ?" 在每天下午2点到下午2:05期间的每1分钟触发
"0 10,44 14 ? 3 WED" 每年三月的星期三的下午2:10和2:44触发
"0 15 10 ? * MON-FRI" 周一至周五的上午10:15触发
"0 15 10 15 * ?" 每月15日上午10:15触发
"0 15 10 L * ?" 每月最后一日的上午10:15触发
"0 15 10 ? * 6L" 每月的最后一个星期五上午10:15触发
"0 15 10 ? * 6L 2002-2005" 2002年至2005年的每月的最后一个星期五上午10:15触发
"0 15 10 ? * 6#3" 每月的第三个星期五上午10:15触发
```

# 注解配置方式(20161215)
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:task="http://www.springframework.org/schema/task"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans-4.1.xsd
       http://www.springframework.org/schema/task
       http://www.springframework.org/schema/task/spring-task-4.1.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context-4.1.xsd">

    <!-- 开启这个配置，spring才能识别@Scheduled注解-->
    <task:annotation-driven/>

    <!-- 扫描注解@Component -->
    <context:component-scan base-package="com.kingsoft.wps.calendar.message"/>
</beans>
```
```
package com.kingsoft.wps.calendar.message.job;

import com.kingsoft.wps.calendar.message.job.handler.reminds.RemindsPushHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created by liuchunlong on 16-12-13.
 */
@Component("remindsPushJob")
public class RemindsPushJob {

    private static final Logger logger = LoggerFactory.getLogger("RemindsPushJob.class");
    private static int timeScope;//推送Reminds的提醒时间范围

    static {
        Properties properties = new Properties();
        try {
            properties.load(RemindsPushJob.class.getClassLoader().getResourceAsStream("config/reminds.properties"));
            timeScope = Integer.parseInt(properties.get("reminds.push.timescope").toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private RemindsPushHandler remindsPushHandler;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void run() {

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        logger.info("Reminds push job start---" + format.format(new Date()));

        remindsPushHandler.push(timeScope);

        logger.info("Reminds push job end---" + format.format(new Date()));

    }
}

```

# 非注解配置方式
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:task="http://www.springframework.org/schema/task"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans-4.1.xsd
       http://www.springframework.org/schema/task
       http://www.springframework.org/schema/task/spring-task-4.1.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context-4.1.xsd">

    <!-- 开启这个配置，spring才能识别@Scheduled注解-->
    <!--<task:annotation-driven/>-->

    <context:component-scan base-package="com.kingsoft.wps.calendar.message"/>

    <bean name="remindsMethodInvokingJob" class="org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean">
        <property name="targetObject" ref="remindsPushJob"/>
        <property name="targetMethod" value="run"/>
    </bean>

    <bean name="remindsMethodTrigger" class="org.springframework.scheduling.quartz.SimpleTriggerFactoryBean">
        <property name="jobDetail" ref="remindsMethodInvokingJob" />
        <property name="startDelay" value="10000"/>
        <property name="repeatInterval" value="60000"/>
    </bean>

    <bean name="schedulerFactory" class="org.springframework.scheduling.quartz.SchedulerFactoryBean">
        <property name="triggers">
            <list>
                <ref bean="remindsMethodTrigger"/>
            </list>
        </property>
    </bean>
</beans>
```

```
package com.kingsoft.wps.calendar.message.job;

import com.kingsoft.wps.calendar.message.job.handler.reminds.RemindsPushHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created by liuchunlong on 16-12-13.
 */
@Component("remindsPushJob")
public class RemindsPushJob {

    private static final Logger logger = LoggerFactory.getLogger("RemindsPushJob.class");
    private static int timeScope;//推送Reminds的提醒时间范围

    static {
        Properties properties = new Properties();
        try {
            properties.load(RemindsPushJob.class.getClassLoader().getResourceAsStream("config/reminds.properties"));
            timeScope = Integer.parseInt(properties.get("reminds.push.timescope").toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private RemindsPushHandler remindsPushHandler;

    //@Scheduled(cron = "0 0/1 * * * ?")
    public void run() {

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        logger.info("Reminds push job start---" + format.format(new Date()));

        remindsPushHandler.push(timeScope);

        logger.info("Reminds push job end---" + format.format(new Date()));

    }
}

```
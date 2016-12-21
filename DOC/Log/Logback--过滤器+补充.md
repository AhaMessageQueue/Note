ok，这里先简单罗列下logback日志框架的好处，关于仔细的整理后面会专门写一篇博客的。

1，配置简单，易于上手
2，一个日志文件中只能某一个级别的日志
3，一个类中可以指定多个不同的日志，并且生成的每个日志文件中只包含其本身的内容
4，可以关闭或者打开某几个包的日志，并且可以设置不同的包使用不同的日志级别。

关于上面的第1点，第3点，第4点我前面博客里面都有整理到了。这里我们重点看下第2种情况：
日志级别及文件

考虑如下实际编码中经常遇见的场景：


日志记录采用分级记录，级别与日志文件名相对应，不同级别的日志信息记录到不同的日志文件中。例如：error级别记录到log_error_xxx.log或log_error.log（该文件为当前记录的日志文件），而log_error_xxx.log为归档日志，
日志文件按日期记录，同一天内，若日志文件大小等于或大于2M，则按0、1、2...顺序分别命名。例如log-level-2013-12-21.0.log。


OK，这里就要用到这篇博客讲的<filter>标签了。下面是具体代码和配置。

下面是一份配置文件：
```
<?xml version="1.0" encoding="UTF-8"?>

<!-- debug：打印logback内部日志信息，实时查看logback的运行状态，默认为false -->
<!-- scan：配置文件如果发生改变，是否被重新加载，默认为true。 -->
<!-- scanPeriod：设置检测配置文件是否有修改的时间间隔，如果没有给出时间单位，默认单位是毫秒，默认的时间间隔为1分钟，默认为true。 -->
<configuration debug="true" scan="true" scanPeriod="30 seconds">

    <!-- 读取属性文件 -->
    <property resource="config/logback.properties"/>

    <contextName>Application</contextName>
    <!-- 时间戳定义，timeReference：使用日志产生日期为时间基准 -->
    <timestamp key="byDay" datePattern="yyyy-MM-dd" timeReference="contextBirth" />

    <!--定义日志文件的存储地址 勿在 LogBack 的配置中使用相对路径，可以使用系统变量 -->
    <!-- <property name="LOG_HOME" value="${app.home}/log" /> -->
    <property name="LOG_HOME" value="log" />

    <!-- 控制台输出，生产环境将请stdout去掉 -->
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <!--格式化输出：%d表示日期，%thread表示线程名，%-5level：级别从左显示5个字符宽度，%msg：日志消息，%n是换行符 -->
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
            </pattern>
        </encoder>
    </appender>

    <!-- 按照每天生成日志文件 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 日志输出文件 -->
        <file>${LOG_HOME}/LoggingBack-${byDay}.log</file>
        <!-- 追加日志到原文件结尾 -->
        <append>true</append>
        <!-- timebasedrollingpolicy：演示时间和大小为基础的日志文件归档 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- 归档的日志文件的路径，例如今天是2013-12-21日志，当前写的日志文件路径为file节点指定。 -->
            <!--可以将此文件与file指定文件路径设置为不同路径，从而将当前日志文件或归档日志文件置不同的目录。 -->
            <!--而2013-12-21的日志文件在由fileNamePattern指定。%d{yyyy-MM-dd}指定日期格式，%i指定索引 -->
            <!-- 文件滚动日期格式：每天：.YYYY-MM-dd（默认）；每星期：.YYYY-ww；每月：.YYYY-MM -->
            <!-- 每隔半天：.YYYY-MM-dd-a；每小时：.YYYY-MM-dd-HH；每分钟：.YYYY-MM-dd-HH-mm -->
            <fileNamePattern>${LOG_HOME}/log-%d{yyyy-MM-dd}.%i.log</fileNamePattern>

            <!-- 控制归档文件的最大数量的保存，删除旧的文件，默认单位天数 -->
            <maxHistory>7</maxHistory>

            <!-- 设置当前日志的文件的大小，决定日志翻滚 -->
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <!-- 除按日志记录之外，还配置了日志文件不能超过10M(默认)，若超过10M，日志文件会以索引0开始， -->
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n
            </pattern>
        </encoder>
    </appender>

    <appender name="FILE-INFO" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 这里添加一个过滤器 -->
        <file>${LOG_HOME}/LoggingBack-info.log</file>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>INFO</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_HOME}/LOG-INFO-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxHistory>7</maxHistory>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n
            </pattern>
        </encoder>
    </appender>

    <appender name="FILE-ERROR" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 这里添加一个过滤器 -->
        <file>${LOG_HOME}/LoggingBack-error.log</file>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_HOME}/LOG-ERROR-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxHistory>7</maxHistory>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n
            </pattern>
        </encoder>
    </appender>

    <!-- 可以写多个日志文件appender，然后区分多个模块的日志 -->
    <appender name="LOGGINGBACK2" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_HOME}/LoggingBack2.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_HOME}/LOG-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxHistory>7</maxHistory>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n
            </pattern>
        </encoder>
    </appender>

    <!-- 指定一个包，name必填，additivity选填：控制是否继承父类appender，默认true -->
    <!-- level选填，如果木有指定从最近的父类继承，顶级为root的级别 -->
    <logger name="org.linkinpark.commons.logbackLogging" additivity="true">
        <appender-ref ref="FILE" />
        <appender-ref ref="FILE-INFO" />
        <appender-ref ref="FILE-ERROR" />
    </logger>

    <root level="DEBUG">
        <appender-ref ref="STDOUT" />
    </root>


</configuration>
```
关于上面的配置文件说明：
我在上面的配置文件中一共定义了4个appender，
然后一个是全局的日志输出文件，一个INFO级别的日志过滤输出，一个是ERROR级别的日志过滤输出，一个是特定名称的日志输出文件。
我们在使用的时候直接写<logger>标签里面引用这些appender就OK了。

下面是我们测试的Java代码：
```
    package org.linkinpark.commons.logbackLogging;

    import org.junit.Test;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    public class LoggingBack
    {

        private static Logger logger = LoggerFactory.getLogger(LoggingBack.class);

        @Test
        public void test()
        {
            logger.debug("LoggingBack.debug()。。。");
            logger.info("LoggingBack.info()。。。");
            logger.error("LoggingBack.error()。。。");
        }

    }
```
测试上面的代码，junit绿条，然后项目的log文件夹下生成4个日志文件：
logback-2016-03-01.log，该文件里面包含了所有的日志输出，包含所有的日志等级

logback-info.log，该文件里面只包含INFO级别的日志输出

logback-error.log，该文件里面只包含ERROR级别的日志输出

logback2.log，该文件为空的，配置文件中我们并没有使用这个appender。


OK，现在我们来看下`<filter>`标签。

过滤器，执行一个过滤器会有返回枚举值，即DENY，NEUTRAL，ACCEPT其中之一。
返回DENY，日志将立即被抛弃不再经过其他过滤器；

返回NEUTRAL，有序列表里的下个过滤器过接着处理日志；

返回ACCEPT，日志会被立即处理，不再经过剩余过滤器。

过滤器被添加到`<Appender>` 中，为`<Appender>` 添加一个或多个过滤器后，可以用任意条件对日志进行过滤。`<Appender> `有多个过滤器时，按照配置顺序执行。

### 下面是几个常用的过滤器：
##### LevelFilter： 级别过滤器，根据日志级别进行过滤。
如果日志级别等于配置级别，过滤器会根据onMath 和 onMismatch接收或拒绝日志。

```
<level>:设置过滤级别
<onMatch>:用于配置符合过滤条件的操作
<onMismatch>:用于配置不符合过滤条件的操作
```
例如：将过滤器的日志级别配置为INFO，所有INFO级别的日志交给appender处理，非INFO级别的日志，被过滤掉。前面我已经举过例子了，这里就不做赘述了。

##### ThresholdFilter： 临界值过滤器，过滤掉低于指定临界值的日志。
当日志级别等于或高于临界值时，过滤器返回NEUTRAL；当日志级别低于临界值时，日志会被拒绝。
例如：过滤掉所有低于INFO级别的日志。下面是配置文件：
```
    <configuration>
      <appender name="CONSOLE"
        class="ch.qos.logback.core.ConsoleAppender">
        <!-- 过滤掉 TRACE 和 DEBUG 级别的日志-->
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
          <level>INFO</level>
        </filter>
        <encoder>
          <pattern>
            %-4relative [%thread] %-5level %logger{30} - %msg%n
          </pattern>
        </encoder>
      </appender>
      <root level="DEBUG">
        <appender-ref ref="CONSOLE" />
      </root>
    </configuration>
```

##### EvaluatorFilter： 求值过滤器，评估、鉴别日志是否符合指定条件。
有以下子节点：
```
<evaluator>:鉴别器，常用的鉴别器是JaninoEventEvaluator，也是默认的鉴别器，
它以任意的java布尔值表达式作为求值条件，求值条件在配置文件解释过成功被动态编译，布尔值表达式返回true就表示符合过滤条件。

evaluator有个子标签<expression>，用于配置求值条件

<onMatch>，用于配置符合过滤条件的操作

<onMismatch>，用于配置不符合过滤条件的操作
```

例如：过滤掉所有日志消息中不包含“billing”字符串的日志。配置文件如下：
```
    <configuration>

      <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <filter class="ch.qos.logback.core.filter.EvaluatorFilter">
          <evaluator> <!-- 默认为 ch.qos.logback.classic.boolex.JaninoEventEvaluator -->
            <expression>return message.contains("billing");</expression>
          </evaluator>
          <OnMatch>ACCEPT </OnMatch>
          <OnMismatch>DENY</OnMismatch>
        </filter>
        <encoder>
          <pattern>
            %-4relative [%thread] %-5level %logger - %msg%n
          </pattern>
        </encoder>
      </appender>

      <root level="INFO">
        <appender-ref ref="STDOUT" />
      </root>
    </configuration>
```

##### 当然我们也可以自定义过滤器。
实现过程很简单，我们只需要写一个类实现filter接口就OK，里面实现decide()方法。

代码如下：
```
    package org.linkinpark.commons.logbackLogging;

    import ch.qos.logback.classic.spi.ILoggingEvent;
    import ch.qos.logback.core.filter.Filter;
    import ch.qos.logback.core.spi.FilterReply;

    public class LinkinFilter extends Filter<ILoggingEvent>
    {

        @Override
        public FilterReply decide(ILoggingEvent event)
        {
            if (event.getMessage().contains("LinkinPark"))
            {
                return FilterReply.ACCEPT;
            }
            else
            {
                return FilterReply.DENY;
            }
        }

    }
```

然后在配置文件中使用到该过滤器的地方配置`<filter>`就OK了。
```
    <!-- 控制台输出，生产环境将请stdout去掉 -->
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <filter class="org.linkinpark.commons.logbackLogging.LinkinFilter" />
            <encoder>
                <!--格式化输出：%d表示日期，%thread表示线程名，%-5level：级别从左显示5个字符宽度，%msg：日志消息，%n是换行符 -->
                <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
                </pattern>
            </encoder>
        </appender>
```

### 补充
最后我们在这里再补充2点关于logback的知识，就可以结束logback相关的整理了。
##### 日志文件颜色光亮
logback的官网上日志输出颜色可以加高亮颜色的，但是很遗憾我尝试了下控制台输出是乱码，所以这里暂时贴出官网上说的，以后再说吧。

##### Coloring
Grouping by parentheses as explained above allows coloring of sub-patterns. As of version 1.0.5, PatternLayoutrecognizes "%black", "%red", "%green","%yellow","%blue", "%magenta","%cyan", "%white", "%gray", "%boldRed","%boldGreen", "%boldYellow", "%boldBlue", "%boldMagenta""%boldCyan", "%boldWhite" and "%highlight" as conversion words. These conversion words are intended to contain a sub-pattern. Any sub-pattern enclosed by a coloring word will be output in the specified color.
Below is a configuration file illustrating coloring. Note the %cyan conversion specifier enclosing "%logger{15}". This will output the logger name abbreviated to 15 characters in cyan. The %highlight conversion specifier prints its sub-pattern in bold-red for events of level ERROR, in red for WARN, in BLUE for INFO, and in the default color for other levels.
Example: Highlighting levels (logback-examples/src/main/java/chapters/layouts/highlighted.xml)

```
<configuration debug="true">
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <!-- On Windows machines setting withJansi to true enables ANSI
         color code interpretation by the Jansi library. This requires
         org.fusesource.jansi:jansi:1.8 on the class path.  Note that
         Unix-based operating systems such as Linux and Mac OS X
         support ANSI color codes by default. -->
    <withJansi>true</withJansi>
    <encoder>
      <pattern>[%thread] %highlight(%-5level) %cyan(%logger{15}) - %msg %n</pattern>
    </encoder>
  </appender>
  <root level="DEBUG">
    <appender-ref ref="STDOUT" />
  </root>
</configuration>
```
Here is the corresponding output:
```
[main] WARN  c.l.TrivialMain - a warning message 0
[main] DEBUG c.l.TrivialMain - hello world number1
[main] DEBUG c.l.TrivialMain - hello world number2
[main] INFO  c.l.TrivialMain - hello world number3
[main] DEBUG c.l.TrivialMain - hello world number4
[main] WARN  c.l.TrivialMain - a warning message 5
[main] ERROR c.l.TrivialMain - Finish off with fireworks
```


It takes very few lines of code to create a coloring conversion word. The section entitled creating a custom conversion specifier discusses the steps necessary for registering a conversion word in your configuration file.


### log4j.propertites文件转logback.xml文件。
打开logback官网，在左下角有一个在线转log4j的配置文件成logback配置文件的工具，挺好的，我自己尝试过，没问题。
所以以后如果我们原来的项目用的是log4j的日志系统的话想切成logback的话，直接这里转下配置文件，然后代码一行都不用动，挺好的。
在线转文件的地址如下：log4j.prorpertites转成logback.xml文件。
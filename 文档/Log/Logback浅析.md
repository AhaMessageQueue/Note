### Logback为取代log4j而生
Logback是由log4j创始人Ceki Gülcü设计的又一个开源日志组件。logback当前分成三个模块：logback-core,logback- classic和logback-access。

### Logback的核心对象：Logger、Appender、Layout
Logback主要建立于Logger、Appender 和 Layout 这三个类之上。
Logger:日志的记录器，把它关联到应用的对应的context上后，主要用于存放日志对象，也可以定义日志类型、级别。Logger对象一般多定义为静态常量，如：

```
package com.logs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyApp {
    final static Logger logger = LoggerFactory.getLogger("MyApp.class");
    public static void main(String[] args) {

        logger.trace("trace");
        logger.debug("debug str");
        logger.info("info str");
        logger.warn("warn");
        logger.error("error");
    }
}
```

Appender:用于指定日志输出的目的地，目的地可以是控制台、文件、远程套接字服务器、 MySQL、 PostreSQL、Oracle和其他数据库、 JMS和远程UNIX Syslog守护进程等。
Layout:负责把事件转换成字符串，格式化的日志信息的输出。具体的Layout通配符，可以直接查看帮助文档。

### Level 有效级别
Logger可以被分配级别。级别包括：TRACE、DEBUG、INFO、WARN和ERROR，定义于ch.qos.logback.classic.Level类。程序会打印高于或等于所设置级别的日志，设置的日志等级越高，打印出来的日志就越少。如果设置级别为INFO，则优先级高于等于INFO级别（如：INFO、 WARN、ERROR）的日志信息将可以被输出,小于该级别的如DEBUG将不会被输出。为确保所有logger都能够最终继承一个级别，根logger总是有级别，默认情况下，这个级别是DEBUG。

### 三值逻辑
Logback的过滤器基于三值逻辑（ternary logic），允许把它们组装或成链，从而组成任意的复合过滤策略。过滤器很大程度上受到Linux的iptables启发。这里的所谓三值逻辑是说，过滤器的返回值只能是ACCEPT、DENY和NEUTRAL的其中一个。

如果返回DENY，那么记录事件立即被抛弃，不再经过剩余过滤器；

如果返回NEUTRAL，那么有序列表里的下一个过滤器会接着处理记录事件；

如果返回ACCEPT，那么记录事件被立即处理，不再经过剩余过滤器。

### Filter 过滤器
Logback-classic提供两种类型的过滤器：常规过滤器和TuroboFilter过滤器。Logback整体流程：Logger 产生日志信息；Layout修饰这条msg的显示格式；Filter过滤显示的内容；Appender具体的显示，即保存这日志信息的地方。

### 具体使用案例
Java项目中一般都会应用比如struts、spring、hibernate等开源框架，而这些框架很多是应用log4j记录日志的，所以我们考虑用log4j + slf4j + logback 。这样我们需要导入log4j-over-slf4j-1.6.4.jar 、logback-classic-1.0.1.jar 、logback-core-1.0.1.jar 、slf4j-api-1.6.4.jar ，如果你要用到EvaluatorFilter过滤器来过滤日志Msg中的特殊字符需要导入其依赖包 janino-2.3.2.jar。其logback.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- 控制台输出 -->
    <appender name="stdout" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%date [%thread] %-5level %logger{80} - %msg%n</pattern>
        </encoder>
    </appender>
    <!-- 时间滚动输出 level为 DEBUG 日志 -->
    <appender name="file—debug"
            class="ch.qos.logback.core.rolling.RollingFileAppender">
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>DEBUG</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY </onMismatch>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <FileNamePattern>D:/logs/debug.%d{yyyy-MM-dd}.log</FileNamePattern>
            <MaxHistory>30</MaxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%date [%thread] %-5level %logger{80} - %msg%n</pattern>
        </encoder>
    </appender>
    <!-- 时间滚动输出 level为 ERROR 日志 -->
    <appender name="file—error"
        class="ch.qos.logback.core.rolling.RollingFileAppender">
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY </onMismatch>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <FileNamePattern>D:/logs/error.%d{yyyy-MM-dd}.log</FileNamePattern>
            <MaxHistory>30</MaxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%date [%thread] %-5level %logger{80} - %msg%n</pattern>
        </encoder>
    </appender>
    <!-- 特定过滤含有某字符串的日志 -->
    <appender name="file-str"
        class="ch.qos.logback.core.rolling.RollingFileAppender">
        <filter class="ch.qos.logback.core.filter.EvaluatorFilter">
            <evaluator>
                <expression>message.contains("str")</expression>
            </evaluator>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <FileNamePattern>D:/logs/contains.%d{yyyy-MM-dd}.log
            </FileNamePattern>
            <MaxHistory>30</MaxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%date [%thread] %-5level %logger{80} - %msg%n</pattern>
        </encoder>
    </appender>
    <!-- 数据库输出 -->
    <appender name="db" class="ch.qos.logback.classic.db.DBAppender">
        <connectionSource
            class="ch.qos.logback.core.db.DriverManagerConnectionSource">
            <driverClass>com.mysql.jdbc.Driver</driverClass>
            <url>jdbc:mysql://host_name:3306/datebase_name</url>
            <user>username</user>
            <password>password</password>
        </connectionSource>
    </appender>
    <logger name="java.sql.Connection">
        <level value="DEBUG" />
    </logger>
    <logger name="java.sql.Statement">
        <level value="DEBUG" />
    </logger>
    <logger name="com.ibatis">
        <level value="DEBUG" />
    </logger>
    <logger name="com.ibatis.common.jdbc.SimpleDataSource">
        <level value="DEBUG" />
    </logger>
    <logger name="com.ibatis.common.jdbc.ScriptRunner">
        <level value="DEBUG" />
    </logger>
    <logger name="com.ibatis.sqlmap.engine.impl.SqlMapClientDelegate">
        <level value="DEBUG" />
    </logger>
    <logger name="com.danga.MemCached">
        <level value="INFO" />
    </logger>
    <logger name="org.springframework.test">
        <level value="DEBUG" />
    </logger>
    <logger name="org.apache.struts2">
        <level value="DEBUG" />
    </logger>
    <root level="DEBUG">
        <appender-ref ref="stdout" />
        <appender-ref ref="file-debug" />
        <appender-ref ref="file-error" />
        <appender-ref ref="file-str" />
        <appender-ref ref="db" />
    </root>
</configuration>
```

# 补充
## 属性
通过设置TimeBasedRollingPolicy的maxHistory属性，可以控制已产生日志的数量。
如果maxHistory设置为30，那么超过30天的log文件会被自动删除，
这是通过RollingFileAppender的rollover()方法来实现的。
## 过滤器
### ThresholdFilter
ThresholdFilter： 临界值过滤器，过滤掉低于指定临界值的日志。当日志级别等于或高于临界值时，过滤器返回NEUTRAL；当日志级别低于临界值时，日志会被拒绝。
例如：过滤掉所有低于INFO级别的日志。
```
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
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


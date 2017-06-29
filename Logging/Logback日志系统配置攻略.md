logback是log4j作者推出的新日志系统，原生支持slf4j通用日志api，允许平滑切换日志系统，并且对简化应用部署中日志处理的工作做了有益的封装。

官方地址为：http://logback.qos.ch/

Logback日志需要依赖一下jar包：
```
slf4j-api-1.6.0.jar
logback-core-0.9.21.jar
logback-classic-0.9.21.jar
logback-access-0.9.21.jar
```
主配置文件为logback.xml，放在类加载目录下下，logback会自动加载

logback.xml的基本结构如下：
```
<?xml version="1.0" encoding="UTF-8"?>  
<configuration>  
<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">  
    <encoder>  
      <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>  
    </encoder>  
  </appender>  
  <root level="DEBUG"><appender-ref ref="STDOUT" /></root>  
</configuration>  
```
logback.xml的基本配置信息都包含在configuration标签中，需要含有至少一个appender标签用于指定日志输出方式和输出格式，root标签为系统默认日志进程，通过level指定日志级别，通过appender-ref关联前面指定的日志输出方式。

例子中的appender使用的是ch.qos.logback.core.ConsoleAppender类，用于对控制台进行日志输出

其中encoder标签指定日志输出格式为“时间 线程 级别 类路径 信息”

logback的文件日志输出方式还提供多种日志分包策略

### 1.文件日志
```
<appender name="ROLLING" class="ch.qos.logback.core.rolling.RollingFileAppender">  
    <file>E:/logs/mylog.txt</file>  
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">  
      <!-- rollover daily -->  
      <fileNamePattern>E:/logs/mylog-%d{yyyy-MM-dd_HH-mm}.%i.log</fileNamePattern>  
      <maxHistory>5</maxHistory>   
      <timeBasedFileNamingAndTriggeringPolicy  
            class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">  
        <!-- or whenever the file size reaches 100MB -->  
        <maxFileSize>100MB</maxFileSize>  
      </timeBasedFileNamingAndTriggeringPolicy>  
    </rollingPolicy>  
    <encoder>  
      <pattern>%date %level [%thread] %logger{36} [%file : %line] %msg%n</pattern>  
    </encoder>  
  </appender>
```
文件日志输出采用的ch.qos.logback.core.rolling.RollingFileAppender类，它的基本属性包括`<file>`指定输入文件路径,encoder指定日志格式。
其中，rollingPolicy标签指定的是日志分包策略。
ch.qos.logback.core.rolling.TimeBasedRollingPolicy类实现的是基于时间的分包策略，分包间隔是根据fileNamePattern中指定的时间最小单位，
比如例子中的`%d{yyyy-MM-dd_HH-mm}`的最小时间单位为分，它的触发方式就是1分钟，策略在每次向日志中添加新内容时触发，如果满足条件，就将mylog.txt复制到E:/logs/目录并更名为mylog-2010-06-22_13-13.1.log，并删除原mylog.txt。

maxHistory的值为指定E:/logs目录下存在的类似mylog-2010-06-22_13-13.1.log文件的最大个数，当超过时会删除最早的文件。

此外，策略还可以互相嵌套，比如本例中在时间策略中又嵌套了文件大小策略，ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP实现对单文件大小的判断，
当超过maxFileSize中指定大小时，文件名中的变量%i会加一，即在不满足时间触发且满足大小触发时，会生成mylog-2010-06-22_13-13.1.log和mylog-2010-06-22_13-13.2.log两个文件。

### 2.数据库日志
```
<appender name="DB" class="ch.qos.logback.classic.db.DBAppender">  
    <connectionSource class="ch.qos.logback.core.db.DriverManagerConnectionSource">  
       <dataSource  
        class="com.mchange.v2.c3p0.ComboPooledDataSource">  
      <driverClass>com.mysql.jdbc.Driver</driverClass>  
      <url>jdbc:mysql://127.0.0.1:3306/databaseName</url>  
      <user>root</user>  
      <password>root</password>  
      </dataSource>  
    </connectionSource>  
  </appender>  
```
数据库输出使用ch.qos.logback.classic.db.DBAppender类，数据源支持c3p0数据连接池，例子中使用的MySql，其他配置方式请参考官方文档。

使用数据库输出需要在数据库中建立3个表，建表脚本如下
```
    # Logback: the reliable, generic, fast and flexible logging framework.  
    # Copyright (C) 1999-2010, QOS.ch. All rights reserved.  
    #  
    # See http://logback.qos.ch/license.html for the applicable licensing   
    # conditions.  
    # This SQL script creates the required tables by ch.qos.logback.classic.db.DBAppender.  
    #  
    # It is intended for MySQL databases. It has been tested on MySQL 5.1.37   
    # on Linux  
      
    BEGIN;  
    DROP TABLE IF EXISTS logging_event_property;  
    DROP TABLE IF EXISTS logging_event_exception;  
    DROP TABLE IF EXISTS logging_event;  
    COMMIT;  
      
    BEGIN;  
    CREATE TABLE logging_event   
      (  
        timestmp         BIGINT NOT NULL,  
        formatted_message  TEXT NOT NULL,  
        logger_name       VARCHAR(254) NOT NULL,  
        level_string      VARCHAR(254) NOT NULL,  
        thread_name       VARCHAR(254),  
        reference_flag    SMALLINT,  
        arg0              VARCHAR(254),  
        arg1              VARCHAR(254),  
        arg2              VARCHAR(254),  
        arg3              VARCHAR(254),  
        caller_filename   VARCHAR(254) NOT NULL,  
        caller_class      VARCHAR(254) NOT NULL,  
        caller_method     VARCHAR(254) NOT NULL,  
        caller_line       CHAR(4) NOT NULL,  
        event_id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY  
      );  
    COMMIT;  
    BEGIN;  
    CREATE TABLE logging_event_property  
      (  
        event_id          BIGINT NOT NULL,  
        mapped_key        VARCHAR(254) NOT NULL,  
        mapped_value      TEXT,  
        PRIMARY KEY(event_id, mapped_key),  
        FOREIGN KEY (event_id) REFERENCES logging_event(event_id)  
      );  
    COMMIT;  
    BEGIN;  
    CREATE TABLE logging_event_exception  
      (  
        event_id         BIGINT NOT NULL,  
        i                SMALLINT NOT NULL,  
        trace_line       VARCHAR(254) NOT NULL,  
        PRIMARY KEY(event_id, i),  
        FOREIGN KEY (event_id) REFERENCES logging_event(event_id)  
      );  
    COMMIT;  
```

### 3.其他
此外logback还提供基于mail,基于jmx等多种日志输出方式，你也可以通过继承ch.qos.logback.core.AppenderBase自己写appender实现

除了使用默认的日志主线程`<root>`外，还可以通过`<logger>`标签定制其他日志线程如：
```
<logger name="com.test" level="DEBUG">  
    <appender-ref ref="STDOUT" />  
</logger>  
```
其中name指定线程针对的包路径，level是日志级别，<appender-ref>定义使用那种appender。

例如要实现打印jdbc提交的sql，可以加入如下logger:
```
<logger name="Java.sql.Connection" level="DEBUG"><appender-ref ref="STDOUT" /></logger>
```
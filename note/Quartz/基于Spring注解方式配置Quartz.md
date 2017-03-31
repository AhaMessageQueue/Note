之前,我们都是通过基于XML的方式实现spring  Quartz.
虽然配置起来特别的方便,但是Spring还支持基本注解的方式来配置，
这样做不仅更加简单，而且代码量也更加少了很多。

### 配置需要调度的类，并添加注解
```
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HelloJob {

    public HelloJob() {
        System.out.println("HelloJob创建成功");
    }
    @Scheduled(cron = "0/1 * *  * * ? ")
    // 每隔1秒隔行一次
    public void run() {
        System.out.println("Hello MyJob  " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ").format(new Date()));
    }
}
```

### 首先要配置我们的beans.xml，在xmlns 多加下面的内容
```
xmlns:task="http://www.springframework.org/schema/task"
```

### 然后xsi:schemaLocation多加下面的内容
```
http://www.springframework.org/schema/task
http://www.springframework.org/schema/task/spring-task-3.0.xsd
```
### 自动配置扫描spring配置文件里面配置内容
```
    <!--开启这个配置，spring才能识别@Scheduled注解-->
    <task:annotation-driven/>
    <!-- 自动扫描注解的bean -->
    <context:component-scan base-package="com.binnor"/>
```

平时产生随机数时我们经常拿时间做种子，比如用System.currentTimeMillis的结果，但是在执行一些循环中使用了System.currentTimeMillis，那么每次的结果将会差别很小，甚至一样，因为现代的计算机运行速度很快。后来看到Java中产生随机数函数以及线程池中的一些函数使用的都是System.nanoTime，下面说一下这2个方法的具体区别。

System.nanoTime提供相对精确的计时，但是不能用他来计算当前日期，在jdk中的说明如下：

```
public static long nanoTime()
    
    返回最准确的可用系统计时器的当前值，以毫微秒为单位。
    
    此方法只能用于测量已过的时间，与系统或钟表时间的其他任何时间概念无关。
    返回值表示从某一固定但任意的时间算起的毫微秒数（或许从以后算起，所以该值可能为负）。
    此方法提供毫微秒的精度，但不是必要的毫微秒的准确度。它对于值的更改频率没有作出保证。
    在取值范围大于约 292 年（263 毫微秒）的连续调用的不同点在于：由于数字溢出，将无法准确计算已过的时间。
    
    例如，测试某些代码执行的时间长度：
    
    long startTime = System.nanoTime();
    // ... the code being measured ...
    long estimatedTime = System.nanoTime() - startTime;


返回：
    系统计时器的当前值，以毫微秒为单位。
从以下版本开始：
    1.5
```

System.currentTimeMillis返回的是从1970.1.1 UTC 零点开始到现在的时间，精确到毫秒，平时我们可以根据System.currentTimeMillis来计算当前日期，星期几等，可以方便的与Date进行转换，下面时jdk中的介绍：

```
public static long currentTimeMillis()

    返回以毫秒为单位的当前时间。注意，当返回值的时间单位是毫秒时，值的粒度取决于底层操作系统，并且粒度可能更大。
    例如，许多操作系统以几十毫秒为单位测量时间。
    
    请参阅 Date 类的描述，了解可能发生在“计算机时间”和协调世界时（UTC）之间的细微差异的讨论。

返回：
    当前时间与协调世界时 1970 年 1 月 1 日午夜之间的时间差（以毫秒为单位测量）。
```
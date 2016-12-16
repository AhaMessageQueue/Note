Joda-Time提供了一组Java类包用于处理包括ISO8601标准在内的date和time。可以利用它把JDK Date和Calendar类完全替换掉，而且仍然能够提供很好的集成。

http://joda-time.sourceforge.net/

版本：joda-time-2.1.jar

# 时间类
```
//方法一：取系统时间
DateTime dt1 = new DateTime();

//方法二：通过java.util.Date对象生成
DateTime dt2 = new DateTime(new Date());

//方法三：指定年月日时分秒毫秒生成(参数依次是：年,月,日,时,分,秒,毫秒)
DateTime dt3 = new DateTime(2012, 5, 20, 13, 14, 0, 0);

//方法四：ISO8601形式生成
DateTime dt4 = new DateTime("2012-05-20");
DateTime dt5 = new DateTime("2012-05-20T13:14:00");

//只需要年月日的时候
LocalDate localDate = new LocalDate(2009, 9, 6);// September 6, 2009

//只需要时分秒毫秒的时候
LocalTime localTime = new LocalTime(13, 30, 26, 0);// 1:30:26PM
```

# 获取年月日时分秒
```
DateTime dt = new DateTime();
//年
int year = dt.getYear();
//月
int month = dt.getMonthOfYear();
//日
int day = dt.getDayOfMonth();
//星期
int week = dt.getDayOfWeek();
//时
int hour = dt.getHourOfDay();
//分
int min = dt.getMinuteOfHour();
//秒
int sec = dt.getSecondOfMinute();
//毫秒
int msec = dt.getMillisOfSecond();
```

# 星期的特殊处理
```
DateTime dt = new DateTime();

//星期
switch(dt.getDayOfWeek()) {
case DateTimeConstants.SUNDAY:
    System.out.println("星期日");
    break;
case DateTimeConstants.MONDAY:
    System.out.println("星期一");
    break;
case DateTimeConstants.TUESDAY:
    System.out.println("星期二");
    break;
case DateTimeConstants.WEDNESDAY:
    System.out.println("星期三");
    break;
case DateTimeConstants.THURSDAY:
    System.out.println("星期四");
    break;
case DateTimeConstants.FRIDAY:
    System.out.println("星期五");
    break;
case DateTimeConstants.SATURDAY:
    System.out.println("星期六");
    break;
}
```

# 与JDK日期对象的转换
```
DateTime dt = new DateTime();

//转换成java.util.Date对象
Date d1 = new Date(dt.getMillis());
Date d2 = dt.toDate();

//转换成java.util.Calendar对象
Calendar c1 = Calendar.getInstance();
c1.setTimeInMillis(dt.getMillis());
Calendar c2 = dt.toCalendar(Locale.getDefault());//public static Locale getDefault()
```

# 日期前后推算
```
DateTime dt = new DateTime();

//昨天
DateTime yesterday = dt.minusDays(1);
//明天
DateTime tomorrow = dt.plusDays(1);
//1个月前
DateTime before1month = dt.minusMonths(1);
//3个月后
DateTime after3month = dt.plusMonths(3);
//2年前
DateTime before2year = dt.minusYears(2);
//5年后
DateTime after5year = dt.plusYears(5);
```

# 取特殊日期
```
DateTime dt = new DateTime();

//月末日期
DateTime lastday = dt.dayOfMonth().withMaximumValue();

//90天后那周的周一
DateTime firstday = dt.plusDays(90).dayOfWeek().withMinimumValue();
```

# 时区
```
//默认设置为日本时间
DateTimeZone.setDefault(DateTimeZone.forID("Asia/Tokyo"));
DateTime dt1 = new DateTime();

//伦敦时间
DateTime dt2 = new DateTime(DateTimeZone.forID("Europe/London"));
```

# 计算区间
```
DateTime begin = new DateTime("2012-02-01");
DateTime end = new DateTime("2012-05-01");

//计算区间毫秒数
Duration d = new Duration(begin, end);
long time = d.getMillis();

//计算区间天数
Period p = new Period(begin, end, PeriodType.days());
int days = p.getDays();

//计算特定日期是否在该区间内
Interval i = new Interval(begin, end);
boolean contained = i.contains(new DateTime("2012-03-01"));
```

# 日期比较
```
DateTime d1 = new DateTime("2012-02-01");
DateTime d2 = new DateTime("2012-05-01");

//和系统时间比
boolean b1 = d1.isAfterNow();
boolean b2 = d1.isBeforeNow();
boolean b3 = d1.isEqualNow();

//和其他日期比
boolean f1 = d1.isAfter(d2);
boolean f2 = d1.isBefore(d2);
boolean f3 = d1.isEqual(d2);
```

# 格式化输出
```
DateTime dateTime = new DateTime();

String s1 = dateTime.toString("yyyy/MM/dd hh:mm:ss.SSSa");
String s2 = dateTime.toString("yyyy-MM-dd HH:mm:ss");
String s3 = dateTime.toString("EEEE dd MMMM, yyyy HH:mm:ssa");
String s4 = dateTime.toString("yyyy/MM/dd HH:mm ZZZZ");
String s5 = dateTime.toString("yyyy/MM/dd HH:mm Z");
```


Jodd的JDateTime也提供了很不错的时间API。

参考资料：http://www.ibm.com/developerworks/cn/java/j-jodatime.html
# Overview

JLine 是一个用来处理控制台输入的Java类库，目前最新的版本是0.9.94。其官方网址是https://jline.github.io。在介绍JLine之前，首先还是介绍一下Java中的`Console`类，以便进行对比。

# Java Console

通过调用`System.console()`方法可以得到与当前虚拟机对应的`Console`对象。但是**该方法并不保证其返回值一定非null**，这取决于底层平台和虚拟机启动的方式：如果是通过**交互式的命令行启动**，并且标准输入和输出流没有被重定向，那么该方法的返回值通常是非null；如果是被自动启动（例如cron）或者通过Eclipse启动，那么返回值通常为null。

Console类支持的功能有限，其中一个比较有用的功能是以非回显（echo）的方式从控制台读取密码。

# JLine

JLine不依赖任何core Java以外的类库，但是其不是纯Java的实现。
- 在Windows平台下，JLine通过自带的`.dll`文件初始化终端。`jline.jar`中包含了`jline32.dll`和`jline64.dll`，在Windows平台上使用的时候， JLine会自动将其解压缩到临时目录并进行加载。
- 在Unix或者Max OS X平台下，JLine通过stty命令初始化终端。例如通过调用`stty -icanon min 1`将控制台设置为`character-buffered`模式。以及通过调用`stty -echo`禁止控制台回显。在修改终端的属性之前，JLine会对终端的属性进行备份，然后注册一个`ShutdownHook`，以便在程序退出时进行恢复。由于JVM在非正常退出时（例如收到`SIGKILL`信号）不保证`ShutdownHook`一定会被调用，因此终端的属性可能无法恢复。
      JLine使用起来非常简单，jline.jar中一共只有20几个类，源码也不难懂。以下是个简单的例子，其中`readLine`函数的参数指定了命令行提示符：

```
ConsoleReader reader = new ConsoleReader();  
String line = reader.readLine(">");  
```

## Features

### Command History

通过按下键盘的上下箭头键，可以浏览输入的历史数据。此外JLine也支持终端快捷键，例如`Ctrl+A`, `Ctrl+W`,`Ctrl+K`, `Ctrl+L`等等，使用的时候非常便捷。
      可以通过`ConsoleReader`的`setUseHistory(boolean useHistory)`方法启用/禁用`Command History`功能。`ConsoleReader`的`history`成员变量负责保存历史数据，默认情况下历史数据只保存在内存中。如果希望将历史数据保存到文件中，那么只需要以`File`对象作为参数构造`History`对象，并将该`History`对象设置到`ConsoleReader`即可。

### Character Masking

`ConsoleReader`提供了一个`readLine(final Character mask)`方法，用来指定`character mask`。如果参数为null，那么输入的字符正常回显；如果为0，那么不回显；否则回显mask指定的字符。

### Tab Completion

JLine中跟自动补全相关的接口是Completor，它有以下几个实现：
- `SimpleCompletor`: 对一系列指定的字符串进行自动补全。
- `FileNameCompletor`: 类似于bash中的文件名自动补全。
- `ClassNameCompletor`: 对classpath中出现的全路径类名进自动补全。
- `NullCompletor`: 不进行自动补全。
- `ArgumentCompletor`: 为每个属性使用指定的Completor。
      
以下是个简单的例子：

```
ConsoleReader reader = new ConsoleReader();  
List<Completor> completors = new ArrayList<Completor>();  
completors.add(new SimpleCompletor(new String[]{"abc", "def"}));  
completors.add(new FileNameCompletor());  
completors.add(new ClassNameCompletor());  
completors.add(new NullCompletor());  
reader.addCompletor(new ArgumentCompletor(completors));  
reader.readLine(">");  
```

以上例子中首先在命令行上键入a，然后按下TAB后会自动补全第一个属性abc；然后键入空格，再按下TAB会进行文件名的自动补全；再键入空格和按下TAB后会进行类名的自动补全； 再键入空格和按下TAB后不再有自动补全。需要注意的是，ArgumentCompletor会对命令行上所有索引超过completors长度的属性使用completors中最后一个元素指定的Completor。如果要禁用这个行为，那么将completors的最后一个元素设置为NullCompletor对象。

### Custom Keybindings

通过创建 `HOME/.jlinebindings.properties`文件（或者制定 `jline.keybindings` 系统变量），可以定制keybindings。


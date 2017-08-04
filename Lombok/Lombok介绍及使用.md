## lombok简介

lombok是暑假来到公司实习的时候发现的一个非常好用的小工具，刚见到的时候就感觉非常惊艳，有一种相见恨晚的感觉，
用了一段时间之后感觉的确挺不错，所以特此来推荐一下。

lombok的官方地址：<https://projectlombok.org/>

lombok的Github地址：<https://github.com/rzwitserloot/lombok>

那么lombok到底是个什么呢，lombok是一个可以通过简单的注解的形式来帮助我们简化消除一些必须有但显得很臃肿的 Java 代码的工具，
简单来说，比如我们新建了一个类，然后在其中写了几个字段，然后通常情况下我们需要手动去建立getter和setter方法啊，构造函数啊之类的，
lombok的作用就是为了省去我们手动创建这些代码的麻烦，它能够在我们编译源码的时候自动帮我们生成这些方法。

lombok能够达到的效果就是在源码中不需要写一些通用的方法，但是在编译生成的字节码文件中会帮我们生成这些方法，这就是lombok的神奇作用。

虽然有人可能会说IDE里面都自带自动生成这些方法的功能，但是使用lombok会使你的代码看起来更加简洁，写起来也更加方便。

## lombok安装

lombok的安装跟一般引用jar包没有什么区别，可以到官网上下载最新的jar包，然后导入到项目里面就好啦。

Maven添加依赖
```xml
<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.16.18</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

Intellij idea开发的话需要安装Lombok plugin，同时设置 Setting -> Compiler -> Annotation Processors -> Enable annotation processing勾选。

## lombok使用

lombok使用过程中主要是靠注解起作用的，官网上的文档里面有所有的注解，这里不一一罗列。

## FAQ
官方文档<https://projectlombok.org/features/index.html>
官方文档<http://jnb.ociweb.com/jnb/jnbJan2010.html>
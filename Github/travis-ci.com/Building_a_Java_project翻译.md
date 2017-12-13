### 本指南涵盖的内容
本指南介绍了Java项目特定的构建环境和配置主题。 请务必先阅读我们的[入门指南](https://docs.travis-ci.com/user/getting-started/)和[一般构建配置](https://docs.travis-ci.com/user/customizing-the-build/)指南。

### 概览
Travis CI环境提供了Oracle JDK 7（默认），Oracle JDK 8，OpenJDK 6，OpenJDK 7，Gradle 2.0，Maven 3.2和Ant 1.8，并为使用Gradle，Maven或Ant的项目提供了合理的默认设置。

要使用Java环境，请将以下内容添加到`.travis.yml`中：
```
language: java
```

### 使用Maven的项目

#### 默认脚本命令
如果您的项目在代码库根目录中有`pom.xml`文件，但没有`build.gradle`，则Travis CI使用Maven 3构建项目：
```
mvn test -B
```
如果您的项目还在代码库根目录中包含`mvnw`封装脚本，则Travis CI会使用它：
```
./mvnw test -B
```
>默认命令不生成JavaDoc（-Dmaven.javadoc.skip = true）。

要使用不同的构建命令，请自定义[构建步骤](https://docs.travis-ci.com/user/customizing-the-build/#Customizing-the-Build-Step)。

>使用-B参数：该参数表示让Maven使用批处理模式构建项目，能够避免一些需要人工参与交互而造成的挂起状态。

#### 依赖管理
```
mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B -V
```

或者如果您的项目使用`mvnw`封装脚本：

```
./mvnw install -DskipTests=true -Dmaven.javadoc.skip=true -B -V
```

>-B,--batch-mode                        Run in non-interactive (batch) mode(非交互式批处理模式)
>-v,--version                           Display version information

### 针对多个JDK的测试
要测试多个JDK，请使用`.travis.yml`中的`jdk：` 键。 例如，要测试Oracle JDK 7和8以及OpenJDK 6：
```
jdk:
  - oraclejdk8
  - oraclejdk7
  - openjdk6
```
>请注意，OSX不支持针对多个Java版本的测试。 有关更多详细信息，请参阅[OS X Build Environment](https://docs.travis-ci.com/user/osx-ci-environment/#JDK-and-OS-X)。

Travis CI提供OpenJDK 6，OpenJDK 7，Oracle JDK 7和Oracle JDK 8.
Sun JDK 6 is not provided, because it is EOL as of November 2012.

OpenJDK 8 is available on our Trusty images, spcify `dist: trusty` to make use of it.

JDK 7向后兼容，我们认为，如果资源允许，所有项目都将首先开始针对JDK 7进行测试，然后是JDK 6。

值得注意的是：OracleJDK 8 and JavaFX projects may need to update to the latest available version from a repository. 
这可以通过将[此问题评论中](https://github.com/travis-ci/travis-ci/issues/3259#issuecomment-130860338)的以下行添加到`.travis.yml`来实现：
```
sudo: false
addons:
  apt:
    packages:
      - oracle-java8-installer
```

### 构建矩阵
对于Java项目，可以使用`env`和`jdk`作为数组来构造构建矩阵。

### 在一个作业中切换JDK
如果您的构建需要在Job中切换JDK，则可以使用jdk_switcher
```
script:
  - jdk_switcher use oraclejdk8
  - # do stuff with Java 8
  - jdk_switcher use oraclejdk7
  - # do stuff with Java 7
```
Use of `jdk_switcher` also updates `$JAVA_HOME` appropriately。

### 举例

- [JRuby](https://github.com/jruby/jruby/blob/master/.travis.yml)
- [Riak Java client](https://github.com/basho/riak-java-client/blob/master/.travis.yml)
- [Cucumber JVM](https://github.com/cucumber/cucumber-jvm/blob/master/.travis.yml)
- [Symfony 2 Eclipse plugin](https://github.com/pulse00/Symfony-2-Eclipse-Plugin/blob/master/.travis.yml)
- [RESThub](https://github.com/resthub/resthub-spring-stack/blob/master/.travis.yml)
- [Joni](https://github.com/jruby/joni/blob/master/.travis.yml), JRuby’s regular expression implementation

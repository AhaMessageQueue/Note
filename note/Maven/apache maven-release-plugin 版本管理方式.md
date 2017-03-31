项目开发需要发布release版本，人工管理的方式，需要手动修改version配置，修改频繁，
且容易出错。现引入`maven-release-plugin`插件，可以提高效率，自动修改版本。

# 具体使用步骤:
### 1.正确配置maven配置文件setting.xml

### 2.在项目pom.xml中增加如下配置：

![](http://img.blog.csdn.net/20150906152517798?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

![](http://img.blog.csdn.net/20150906152538597?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

![](http://img.blog.csdn.net/20150906152600820?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

### 3.父项目的pom.xml以及子模块的pom.xml中，version字段都需要配置成“x.x.x-SNAPSHOT”版本

### 4.如果要发布snapshot版本，不需要特殊操作，只需要`mvn clean deploy`即可

### 5.如果要发布release版本，通常只需要如下几步：

#### mvn release:prepare:
Maven会进入交互模式，询问需要发布release的版本（默认是将当前版本的“-SNAPSHOT去掉”）；
然后询问发布后snapshot版本的版本号（默认当前版本增加一位小版本号）；直接回车即可确认。
然后插件开始工作，主要进行的操作有：

- 替换父工程和子模块的pom.xml中的version字段为1.0.5；然后在本地Git仓库当前分支Commit一个版本
- 在本地git仓库，创建一个tag，默认命名为XXX-1.0.5
- 再将父工程和子模块的pom.xml中的version字段替换成1.0.6-SNAPSHOT；然后本地git仓库当前分支再Commit一个版本
- 将以上本地版本push到git remote仓库

#### mvn release:perform:
主要进行的操作是将第一步生成的tag clone到本地，然后对其进行build和deploy操作，
完成之后能看到maven release仓库中已经有了对应的版本

#### mvn release:clean
这一步将上述过程中生成的临时文件删除






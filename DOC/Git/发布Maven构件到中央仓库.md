>摘要: maven的神威，我想用过的人都会说好的，用多了难免就会有将自己写的代码也发布到
中央仓库的想法，本文就结合我自己的经历简单介绍一下怎样将构件发布到maven的中央仓库.

使用maven有一段时间了，不管你同意与否，我都坚决认定maven是很伟大的软件项目，使用它，
可以节约很多时间，特别是和git搭配使用的时候，简直就是绝了，就像川菜的花椒和海椒的
搭配一样，无与伦比，用过之后肯定会爱上它的。在我的以前的博客里面有关于怎样使用git
（eclipse里面的egit插件）管理项目的说明，下面我就怎样将构件发布到maven的中央仓库
进行说明。如果你还不知道什么事maven或者git（及时你的工作不会使用到它们，但是都应该
有所了解吧），那真的应该去了解一下了。

这里需要说明的是，我的发布过程是参考了开源中国里面的两篇博客的
（@黄勇 的http://my.oschina.net/huangyong/blog/226738 和
@路小磊 的http://my.oschina.net/looly/blog/270767 ），在此向他们表示感谢。

闲话不多说，下面正式开始。

### 1、注册Sonatype的账户
maven中央仓库是有一个叫做Sonatype的公司在维护的，在发布构件之前需要到
https://issues.sonatype.org/secure/Signup!default.jspa 去注册一个账号，记住
自己的用户名和密码，以后要用。

同时，还要记住一个地址，将来在查询自己所发布构件状态和进行一些操作的时候要使用，
https://oss.sonatype.org/ 。

### 2、提交发布申请
提交申请，在这里是创建一个issue的形式，创建地址：
https://issues.sonatype.org/secure/CreateIssue.jspa?issuetype=21&pid=10134

在填写issue信息的时候，有一些需要注意的地方：

- `group id`就是别人在使用你的构件的时候在pom.xml里面进行定位的坐标的一部分，
而且尽量应该是你的站点的url倒序，参考java包命名的规范，有时候工作人员会想你确认你是
否是这个站点url的拥有者，这里由于我已经购买了songxinqiang.cn域名的所有权，所以我
填写的就是“cn.songxinqiang”

- `project url`这个是项目的站点，一般是用作说明的

- `SCM url`这个构件的源代码的url，便于别人查看源代码，当然这个得是公网能够访问的，
不要是什么内部的svn地址什么的哈，我是用的是开源中国的GIT@OSC,也推荐大家使用

其他的就没有什么了，提交之后就等工作人员离开确认吧，有时候工作人员会问你些你没有明确
的内容，只需要回答就好。需要注意的是，这个系统的工作人员是在美国上班的，经过我的等待
和观察，他们会在北京时间的22:00开始上班、处理issue，所以在这个时间之前就不要去查询
状态了，人家还没有上班呢（我会告诉你，我在上午9:00左右提交了，每个半小时查看一次直到
工作人员回复我吗）。

等到工作人员在你创建的issue下面回复你说“配置已经修改……”（还有几个链接）的时候就说明
审批已经通过了，你就可以进行构件的上传了。

![](http://static.oschina.net/uploads/space/2014/0913/225041_9ZAO_932586.jpg)

### 3、上传之前的准备
在上传构件之前，需要准备GPG以便对发布的文件进行签名。

windows用户到http://www.gpg4win.org/download.html 去下载Gpg4win-Vanilla版来
使用，linux的直接安装gpg软件包就行。

```
gpg --gen-key
```

需要输入姓名、邮箱等字段，其它字段可使用默认值，此外，还需要输入一个 Passphase，
相当于一个密钥库的密码，一定不要忘了，也不要告诉别人，最好记下来，因为后面会用到。

更多的请参考@黄勇 的博客（http://my.oschina.net/huangyong/blog/226738）。

### 4、配置maven
找你所使用的maven的配置文件<mvn_home>/conf/settings.xml，在配置文件中找到`<servers>`节点，
这个节点默认是注释了的，我们就在这个猪似的外边增加一个<servers>的配置

```
<servers>
        <server>
            <id>oss</id>
            <username>用户名</username>
            <password>密码</password>
        </server>
    </servers>
```
这里的id是将来要在pom.xml里面使用的，所以务必记好，用户名和密码就是在Sonatype上面
注册的用户名和密码。

### 5、配置pom.xml
接下来就是重头戏了，pom.xml是一个maven项目的重点配置，一个项目的所有配置都可以由
这个文件来描述，文件中的所有配置都有默认值，也就是说所有的配置都是可选配置，但是为了
把构件发布到中央仓库，我们必须配置一些关键信息，否则再发布时是不会通过了。

这些必须明确致命的信息包括：name、description、url、licenses、developers、scm等
基本信息，此外，使用了 Maven 的 profile 功能，只有在 release 的时候，创建源码包、
创建文档包、使用 GPG 进行数字签名。此外，snapshotRepository 与 repository 中
的 id 一定要与 settings.xml 中 server 的 id 保持一致。

大家可以直接继承自我的一个配置的构件，然后再自己定义一个坐标就行了，我的构件坐标是：
```
<groupId>cn.songxinqiang</groupId>
<artifactId>oss-parent</artifactId>
<version>1.0</version>
```

### 6、上传构件
待构件编写完成，就可以进行上传、发布了。在命令行进入项目pom.xml所在路径，执行：

```
mvn clean deploy -P release
```

![](http://static.oschina.net/uploads/space/2014/0913/225210_DkgX_932586.png)

在稍后些时候会要你输入gpg密钥库的密码，输入即可完成上传，具体时间取决于网络情况。
当然有时候不会弹出输入密码的输入框，只是提示需要输入密码，根据gpg插件的官网解释
（http://maven.apache.org/plugins/maven-gpg-plugin/usage.html ），需要加上
密码作为参数执行命令，即：
```
mvn clean deploy -P release -Dgpg.passphrase=密码
```

### 7、在OSS中发布构件
构件上传之后需要在OSS系统中对操作进行确认，将构件发布，进入https://oss.sonatype.org/
使用你的用户名和密码登陆之后，在左边菜单找到“Staging Repositories”，点击，在右边
上面一点有一个输入搜索框输入你的groupid进行快速定位，可以发现这时你的构件状态是“open”，
勾选你的构件，查看校验的结果信息，如果没有错误就可以点击刚才勾选的checkbox上面右边一点
的“close”按钮，在弹出框中“confirm”，这里又需要校验一次，稍后结果会通过邮箱通知。

等成功后（系统自动进行，很快的），再次登录系统找到你的构件，这是状态已经是“closed”的了，
再次勾选，然后点击“close”旁边的“release”，在弹出框中进行“confirm”，稍后结果会通过邮件进行通知。
![](http://static.oschina.net/uploads/space/2014/0913/225121_qM3d_932586.png)

![](http://static.oschina.net/uploads/space/2014/0913/225137_Rj0T_932586.png)

### 8、通知sonatype的工作人员关闭issue
回到issue系统，找到你的那个申请发布构件的issue，在下面回复工作人员，说明构件已经发布，
待工作人员确认后，会关闭这个issue。

### 9、使用构件
一切完成后并不可以马上就使用你所发布的issue，得等系统将你的构件同步到中央仓库之后
才可以使用，这个时间至少要2个小时，然后就可以在中央仓库的搜索页面
（http://search.maven.org/ ）搜到你的构件啦，

赶快截图，向他人炫耀一下吧。
### 10、特别说明
上面的系统是不是很繁琐？别担心，这只是第一次使用你的groupid发布构件，经实践证明，
第一次成功之后，以后就可以使用你的groupid发布任何的构件了，只需要你的groupid没有
变就行，（当然不能发布重复构件哈），不用这么麻烦。

以后的发布流程：

1. 构件准备好之后，在命令行上传构建；
2. 在https://oss.sonatype.org/ “close”并“release”构件；
3. 等待同步好（大约2小时多）之后，就可以使用了
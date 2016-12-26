很多朋友都给我提过同样的建议：
>建议把 Smart 的 jar 包放到 Maven 中央仓库吧！放在 OSC Maven 里我下载不到。

需要澄清的是，并不是从 OSC Maven 下载不到 Smart 构件，而是这些构件属于第三方构件，
存放在 OSC Maven 的第三方仓库里，所以我们需要在 pom.xml 或 setting.xml 里配置：
```
<!-- lang: xml -->
<repository>
    <id>osc_thirdparty</id>
    <url>http://maven.oschina.net/content/repositories/thirdparty/</url>
</repository>

```

然而，很多朋友并不知道需要这样配置，所以就产生了那个建议。
此外，需要注意的是，OSC Maven 的第三方仓库只能存放 RELEASE 构件，而不能存放 SNAPSHOT 构件。
为了让大家能够更方便的使用 Smart，我做了一个慎重的决定：将 Smart 构件发布到 Maven 中央仓库中！
将构件放入中央仓库是一件非常麻烦的事情，需要做很多准备工作，我会尽可能有条理地展现每个步骤，
就是为了让大家少走弯路，节省更多的时间，去做更重要的事情。

现在就开始吧！
说到中央仓库，不得不说 Sonatype 这家公司，因为中央仓库就是这家公司砸钱搞的，并且免费
向全球所有的 Java 开发者提供构件托管服务，这对于我们而言，简直就是“福利”啊！
>Sonatype 官网：http://www.sonatype.org/

对于向我这样的新手而言，第一次将构件发布到中央仓库，真的不是一件非常轻松的事情，所以
现在非常有必要把些步骤记下来，这样可以节省大家的时间，做更多重要的事情。

### 第一步：注册一个 Sonatype 用户
>注册地址：https://issues.sonatype.org/secure/Signup!default.jspa

这里的用户名与密码是非常重要的，后面会用到，一定要保存好。

此外，Sonatype 还提供了一个名为 OSS 的系统：

>Sonatype OSS：https://oss.sonatype.org

在 OSS 中可以查询到全世界已发布的构件，当然它还有另外一个作用，后面会提到。

### 第二步：创建一个 Issue
>Issue 地址：https://issues.sonatype.org/secure/CreateIssue.jspa?issuetype=21&pid=10134

此时，相当于提交一个申请。其中，最重要的信息就是 groupId 了，对于这个 groupId，我必要多说几句：
因为我的 Smart 项目源码托管在 OSC Git 上，其域名为 oschina.net，所以 Smart 的 groupId
应该是 net.oschina.huangyong。很明显，这种 groupId 不是我想要的，所以我需要购买一个
属于 Smart 的域名。因为 Smart 是一个开源项目，属于非盈利组织，所以域名后缀应该是 org 的，
于是第一反应是想买 smart.org 域名。在 万网 上查询了一下，发现这个域名已经被人买了，
没办法，只能换一个域名了。经大家一番讨论后，决定 Smart 的域名为 smart4j.org，
在万网上的价格是 139 元/年。经 大漠 的推荐，最后在 GoDaddy 上以 87 元/年的价格
购买了该域名，在 GoDaddy 上是可以使用支付宝交易的。这样一来，Smart 在中央仓库里就
可以申请到名为 org.smart4j 的 groupId 了。

### 第三步：等待 Issue 审批通过
一般需要 1 ~ 2 天时间，需要耐心等候，审批通过后会发邮件通知，此外，在自己提交的
Issue 下面会看到 Sonatype 工作人员的回复。

### 第四步：使用 GPG 生成密钥对
如果是 Windows 操作系统，需要下载 Gpg4win 软件来生成密钥对。建议大家下载 Gpg4win-Vanilla 版本，
因为它仅包括 GnuPG，这个工具才是我们所需要的。

安装 GPG 软件后，打开命令行窗口，依次做以下操作：
##### 1. 查看是否安装成功
```
gpg --version
```
能够显示 GPG 的版本信息，说明安装成功了。

##### 2. 生成密钥对
```
gpg --gen-key
```

此时需要输入姓名、邮箱等字段，其它字段可使用默认值，此外，还需要输入一个 Passphase，
相当于一个密钥库的密码，一定不要忘了，也不要告诉别人，最好记下来，因为后面会用到。

##### 3. 查看公钥
```
gpg --list-keys
```
输出如下信息：
```
C:/Users/huangyong/AppData/Roaming/gnupg/pubring.gpg
----------------------------------------------------
pub   2048R/82DC852E 2014-04-24
uid                  hy_think <hy_think@163.com>
sub   2048R/3ACA39AF 2014-04-24
```

可见这里的公钥的 ID 是：82DC852E，很明显是一个 16 进制的数字，马上就会用到。

##### 4. 将公钥发布到 GPG 密钥服务器
```
gpg --keyserver hkp://pool.sks-keyservers.net --send-keys 82DC852E
```
此后，可使用本地的私钥来对上传构件进行数字签名，而下载该构件的用户可通过上传的公钥来
验证签名，也就是说，大家可以验证这个构件是否由本人上传的，因为有可能该构件被坏人给篡改了。

##### 5. 查询公钥是否发布成功
```
gpg --keyserver hkp://pool.sks-keyservers.net --recv-keys 82DC852E
```
实际上就是从 key server 上通过公钥 ID 来接收公钥，此外，也可以到 sks-keyservers.net
上通过公钥 ID 去查询。

### 第五步：修改 Maven 配置文件
需要修改的 Maven 配置文件包括：setting.xml（全局级别）与 pom.xml（项目级别）。
###### 1. setting.xml
```
<!-- lang: xml -->
<settings>

    ...

    <servers>
        <server>
            <id>oss</id>
            <username>用户名</username>
            <password>密码</password>
        </server>
    </servers>

    ...

</settings>

```
使用自己注册的 Sonatype 账号的用户名与密码来配置以上 server 信息。

##### 2. pom.xml
```
<!-- lang: xml -->
<project>

    ...

    <name>smart</name>
    <description>Smart is a lightweight Java Web Framework and reusable components.</description>
    <url>http://www.smart4j.org/</url>

    <licenses>
        <license>
            <name>The Apache Software License, Version 2.0</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        </license>
    </licenses>

    <developers>
        <developer>
            <name>huangyong</name>
            <email>huangyong.java@gmail.com</email>
        </developer>
    </developers>

    <scm>
        <connection>scm:git:git@git.oschina.net:huangyong/smart.git</connection>
        <developerConnection>scm:git:git@git.oschina.net:huangyong/smart.git</developerConnection>
        <url>git@git.oschina.net:huangyong/smart.git</url>
    </scm>

    ...

    <profiles>
        <profile>
            <id>release</id>
            <build>
                <plugins>
                    <!-- Source -->
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-source-plugin</artifactId>
                        <version>2.2.1</version>
                        <executions>
                            <execution>
                                <phase>package</phase>
                                <goals>
                                    <goal>jar-no-fork</goal>
                                </goals>
                            </execution>
                        </executions>
                    </plugin>
                    <!-- Javadoc -->
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-javadoc-plugin</artifactId>
                        <version>2.9.1</version>
                        <executions>
                            <execution>
                                <phase>package</phase>
                                <goals>
                                    <goal>jar</goal>
                                </goals>
                            </execution>
                        </executions>
                    </plugin>
                    <!-- GPG -->
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-gpg-plugin</artifactId>
                        <version>1.5</version>
                        <executions>
                            <execution>
                                <phase>verify</phase>
                                <goals>
                                    <goal>sign</goal>
                                </goals>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
            <distributionManagement>
                <snapshotRepository>
                    <id>oss</id>
                    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
                </snapshotRepository>
                <repository>
                    <id>oss</id>
                    <url>https://oss.sonatype.org/service/local/staging/deploy/maven2/</url>
                </repository>
            </distributionManagement>
        </profile>
    </profiles>

    ...

</project>
```

注意：以上 pom.xml 必须包括：name、description、url、licenses、developers、scm 等
基本信息，此外，使用了 Maven 的 profile 功能，只有在 release 的时候，创建源码包、
创建文档包、使用 GPG 进行数字签名。此外，snapshotRepository 与 repository 中
的 id 一定要与 setting.xml 中 server 的 id 保持一致。

### 第六步：上传构件到 OSS 中
```
mvn clean deploy -P release
```
当执行以上 Maven 命令时，会自动弹出一个对话框，需要输入上面提到的 Passphase，它就是
通过 GPG 密钥对的密码，只有自己才知道。随后会看到大量的 upload 信息，而且速度比较慢，
经常会 timeout，需要反复尝试。

注意：此时上传的构件并未正式发布到中央仓库中，只是部署到 OSS 中了，下面才是真正的发布。

### 第七步：在 OSS 中发布构件
在 OSS 中，使用自己的 Sonatype 账号登录后，可在 Staging Repositories 中查看刚才
已上传的构件，这些构件目前是放在 Staging 仓库中，可进行模糊查询，快速定位到自己的
构件。此时，该构件的状态为 Open，需要勾选它，然后点击 Close 按钮。接下来系统会自动
验证该构件是否满足指定要求，当验证完毕后，状态会变为 Closed，最后，点击 Release
按钮来发布该构件。

### 第八步：通知 Sonatype“构件已成功发布”
需要在曾经创建的 Issue 下面回复一条“构件已成功发布”的评论，这是为了通知 Sonatype
的工作人员为需要发布的构件做审批，发布后会关闭该 Issue。

### 第九步：等待构件审批通过
没错，还是要等，也许又是 1 ~ 2 天。同样，当审批通过后，将会收到邮件通知。

### 第十步：从中央仓库中搜索构件
最后，就可以到中央仓库中搜索到自己发布的构件了！

    中央仓库搜索网站：http://search.maven.org/

最后，想说一句：第一次都是很痛的，以后就舒服了。没错，只有第一次发布才如此痛苦，
以后 deploy 的构件会自动部发布到中央仓库，无需再这样折腾了。

至此，Smart 构件已成功发布到中央仓库，现在可在你的代码中直接配置 Smart 依赖了。


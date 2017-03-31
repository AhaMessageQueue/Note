插件官方地址:   http://maven.apache.org/plugins/maven-war-plugin/

在进行项目发布的时候,可能会碰到这样的情况, 希望在保持项目源代码不变的前提下,希望能够针对不同的运行环境获得相应的运行包.(比如war包)

那么使用配置文件是可以解决这个问题的.可以将项目中和运行环境相关的一些参数放到配置文件中, 每个环境一份,这样,在打包前只要指定这次打包我需要哪个配置文件即可.于是就可以使运行包与环境相对应了.

现在有另一个做法, 使用maven-war-plugin这个插件可以在执行打包命令的时候指定我要打哪个环境的包, 而不需要去关注我现在要用什么配置文件了.当然只适用于Maven项目.

例如: `maven package –P  youEnvName`   这样你就可以打出一个`youEnvName`环境的的运行包了.

### 第1步
还是要为不同环境准备不同的运行参数: 这里在src/main/resources 目录下新建两个属性文件,分别用来配置两个环境的一些不同参数.注意,属性文件需要是一个合法的properties文件:如:
```
src/main/resources/IProject-test.properties   
src/main/resources/IProject-real.properties 
```
分别对应测试和正式两个环境.

### 第2步 
在src/main下面新建一个目录,例如:src/main/packageFilter。
然后将你要用到这几个参数的一个或多个配置文件文件找出来(通常这些配置文件是在WEB-INF目录中的),将他们移动到src/main/packageFilter目录中去.(这个时候你可能会问:配置文件换地方了,项目懂吗? 先不着急.)

这些配置文件移到src/main/packageFilter目录中后,要对其中的使用上面属性文件中的那几个参数作一下修改(仅仅是样例) 
```
<bean id="myService" class="com.nileader.MyService" init-method="init">  
    <property name="version">  
        <value>${ system.envid }</value>  
    </property>  
</bean>
```
这里看到了吧, 参数已经用一个`${key}` 的占位符. 

### 第3步
在pom文件添加打包插件的依赖: 
```
    <filters>  
        <filter> src/main/resources/IProject-test.properties </filter>  
    </filters>  
    <plugin>  
        <groupId>org.apache.maven.plugins</groupId>  
        <artifactId>maven-war-plugin</artifactId>  
        <configuration>  
            <webResources>  
                <resource>  
                    <directory>src/main/packageFilter</directory>  
                    <filtering>true</filtering>  
                    <targetPath>WEB-INF</targetPath>  
                </resource>  
            </webResources>  
        </configuration>  
    </plugin> 
```
这里就可以解决刚才提到的那个问题了,配置文件看上去已经移动了,但是并不需要告诉你的项目,
其实在项目加载配置文件的时候,我们已经将一个完整的,不包含任何占位符的配置放在了项目本应该放的位置 ,这里由下面这段配置实现的:
```
<resource>  
    <directory>src/main/packageFilter</directory>  
    <filtering>true</filtering>  
    <targetPath>WEB-INF</targetPath>  
</resource> 
```
这段配置的意思是: 在打包的时候,要将`<directory>src/main/packageFilter</directory>`中的文件全部搬到
```
  <targetPath>WEB-INF</targetPath>
```
目录中去,并且这个过程是覆盖的.

同时`<filtering>true</filtering>`指定了在这个”搬运”过程中,需要进行过滤.

另外,这里还有个标签: `<filters>….</filters>` 先不讲
好了,新的问题出来了,什么是过滤.现在进入最后一个步骤.

### 第4步
还是在pom.xml文件中, 配置如下信息:
```
    <profiles>  
        profile for bulid   
        <profile>  
            <id>test</id>  
            <build>  
                <filters>  
                    <filter> src/main/resources/IProject-test.properties</filter>  
                </filters>  
            </build>  
        </profile>  
        <profile>  
            <id>real</id>  
            <build>  
                <filters>  
                    <filter> src/main/resources/IProject-real.properties</filter>  
                </filters>  
            </build>  
        </profile>  
    </profiles>  
```
好了,现在知道了吧, 根据单词`filter`可以猜到这个配置是干嘛的了.他的工作过程就是在打包的时候,
使用命令
```
mvn package –P real.
```
那么他就会找到`<id>real</id>`对应的那个参数信息`src/main/resources/IProject- real.properties`, 
然后用其中的key-value对来过滤`<directory>src/main/packageFilter< /directory>`中的文件,
并将填充完整后的配置文件全部搬运到`<targetPath>WEB-INF</targetPath>`中去.

另外,由于配置<plugin>的时候留下一个标签
```
<filters>  
    <filter> src/main/resources/IProject-test.properties </filter>  
</filters>  
<plugin>  
……..  
```
这个就是所谓的默认参数,也就是当你执行 mvn package,他默认使用这个文件中的参数来过滤,相当于执行:
```
mvn package –P test.
```
### 如何调试
上面已经提到了,`maven-war-plugin` 在工作过程中是把 `<directory>src/main/packageFilter</directory>` 中的文件全部搬到
 `<targetPath>WEB-INF</targetPath>`下,并且是覆盖.

所以可以这样处理,在开发调试的时候,可以在配置文件的目录中放上完整的配置文件,里面的填充需要的参数,这位就可以顺利进行调试了, 并且不影响打包.

### 注意事项

现在可以调试了,但是要注意, 在开发调试过程中,如果对这个配置文件有改动,一定要反映到模板文件中去,所以建议在这个需要被填充的配置中添加上一些注释,可以提醒自己:
```
<!-- 注意,对这个文件的任何修改必须同步到文件:src/main/packageFilter/****.xml,否则白改了. -->  
```
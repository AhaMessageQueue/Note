尊重他人的劳动成果，转载请标明出处：http://blog.csdn.net/gengqiquan/article/details/52329259， 本文出自:【gengqiquan的博客】

不知不觉在在这家公司快三个月了，刚接手项目的时候是真的被里面的代码恶心到了，网络请求用的原生的httpURLconnection。
这本没什么，关键是根本就没有进行任何封装。activity里面充斥着大量的handler，要找个oncreated()函数先得把类拉到一半，
那种感觉岂止酸爽。由于项目周期紧张。没时间去大规模的重构，新框架只能在新功能里写。
采用了retrofit，初期做了简单的封装，断断续续一段时间的优化整理。现在分享出来给大家。

为了方便大家，会形成依赖库。引入和调用方式请看文章底部的github文档
本次封装需要做到的
```
1支持异步请求统一回调
2参数可配置
3链式调用
4支持基本get和post封装
5支持rxjava返回
6支持一个接口Service 对应一种请求类型，而不是每个API都需要去定义一个接口方法
7支持取消单个请求
8支持请求打标签，退出页面时取消当前页面所有请求
9支持动态配置缓存，缓存与具体的请求接口绑定，
10支持追加统一参数，登录信息token、设备号等
11支持接口版本号配置
12流式下载文件，取消下载请求
```
下面我们来看下封装过程，博客底部会贴出示例项目地址 

retrofit独树一帜的把请求采用了接口，方法和注解参数（parameter annotations）来声明式定义一个请求应该如何被创建的方式。
像这样

```
public interface GitHub {
      @GET("/repos/{owner}/{repo}/contributors")
        List<Contributor> contributors(@Path("owner")
        String owner, @Path("repo")
        String repo);
```

然后去实例化并且调用请求

```
GitHub github = restAdapter.create(GitHub.class);
// Fetch and print a list of the contributors to this library.
List<Contributor> contributors = github.contributors("square",
        "retrofit");
```

先不谈retrofit到底做了多少优化、性能上有多少提升。光是这样的调用方式我就受不了。我特么得写多少个 像上面的GitHub 一样的Service，就算是把url注解方法都写在一个里面，那也得多少行？一个项目六七十行应该是没什么问题的了。嗯。反正我会看疯了的。而且这样的一种调用方式是直接面向框架层的，以后万一我们换框架了怎么办？代码挨个找出来全换一边?你疯不疯？
那我们有没有什么办法改变他？很简单，我们在框架外面再套一层通用框架，作为框架的设计者，我们应该让调用者知道怎么调用就可以了，而不应该让调用者去考虑底层实现的细节。

好在retrofit提供了Url 参数替换注解@Url String url，通过这个注解我们可以动态的设置请求的url。

下面列出一些简单的参数注解

当前文档适用于 LayIM PC端 最新版，如果你正在了解的并非该版本，你可以前往其它版本阅览： 

[移动版文档](http://www.layui.com/doc/modules/layim_mobile.html)

[2.x文档](http://www.layui.com/doc/modules/layim2.html)

>在WebIM似乎已被打入冷宫的今天，LayIM正试图重新为网页带来一些社交想象。
>作为一款Web即时通讯前端解决方案（服务端需自写），LayIM提供了全方位的前端接口支撑，不仅能让您更高效地接入到自己的通讯服务中，更能让你轻松地与 环信、融云、野狗 等第三方通讯服务平台对接。
>LayIM始终坚持极简的体验，致力于拉近你的用户在web间的距离。
>
>LayIM兼容除IE6/7以外的所有浏览器，如果你的网站仍需兼容ie6/7，那么强烈建议你说服你的老板或者客户。

>模块加载名称：layim，官网地址：layim.layui.com

## 开始使用
LayIM基于layui模块体系，因此你获得的其实是一个包含LayIM的layui框架，不同的是，开源版的layui并不包含LayIM。
捐赠后，将您获得的压缩包解压，将layui整个目录文件放入你的项目后，不用再对其代码做任何修改（方便下次升级）。然后您只需引入下述两个文件即可。

```java
./build/css/layui.css
./build/layui.js
```

假如你将layui放入你的/static/目录中，并且你的html页面在根目录，那么一个最直接的例子是：






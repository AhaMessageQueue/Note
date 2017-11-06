## 1.下载安装

在官网<http://www.rabbitmq.com/download.html>下载RabbitMQ Server,
这个页面右侧有导航，可以找到[Install: Mac OS X](http://www.rabbitmq.com/install-standalone-mac.html)链接，
或者页面中的Installation Guides项目下可以找到[Mac OS X: Standalone](http://www.rabbitmq.com/install-standalone-mac.html)下载地址,
现在最新版本为rabbitmq-server-mac-standalone-3.6.12.tar.xz，下载解压这个文件。

## 2.启动服务器

终端进入解压之后的目录

```text
cd rabbitmq_server-3.6.0
cd sbin
sudo sh rabbitmq-server
```

终端输出：

```text
RabbitMQ 3.6.0. Copyright (C) 2007-2015 Pivotal Software, Inc.
## ## Licensed under the MPL. See http://www.rabbitmq.com/
## ##
########## Logs: /Users/zhangmeng27/Downloads/proj/RabbitMQ/rabbitmq_server-3.6.0/var/log/rabbitmq/rabbit@zhangmeng27deMacBook-Pro.log
###### ## /Users/zhangmeng27/Downloads/proj/RabbitMQ/rabbitmq_server-3.6.0/var/log/rabbitmq/rabbit@zhangmeng27deMacBook-Pro-sasl.log
##########
Starting broker... completed with 0 plugins.
```

表明RabbitMQ 服务器已经启动

## 3.查询服务器状态，关闭服务器

另启一个终端，进入rabbitmq_server-3.6.0/sbin目录下，可以通过 `sh rabbitmqctl status` 命令查看已经启动的服务器的状态，
rabbitmqcrl相关的命令解释可以在[这里](http://www.rabbitmq.com/man/rabbitmqctl.1.man.html)找到

关闭RabbitMQ服务器可以在启动服务器的那个终端中通过control+c关闭服务，或者新启一个终端进入sbin目录，通过 `sh rabbitmqctl stop` 命令关闭服务器。

## 4.通过web管理和监控RabbitMQ

rabbitmqctl组合对应命令可以通过终端查看rabbitmq服务器的状态，但是这样很不直观，rabbitmq还提供了web页面查看并管理rabbitmq服务器，
这个功能通过插件实现，默认插件没有开启，所以上面启动服务器过程中最后提示completed with 0 plugins。

启动rabbitmq服务器的状态下，另起一个终端在sbin下执行 `sh rabbitmq-plugins enable rabbitmq_management` 会提示

```text
The following plugins have been enabled:
mochiweb
webmachine
rabbitmq_web_dispatch
amqp_client
rabbitmq_management_agent
rabbitmq_management

Applying plugin configuration to rabbit@zhangmeng27deMacBook-Pro... started 6 plugins.
```

现在可以通<http://localhost:15672>打开web页面

使用默认的账号密码`guest/guest`登录,登录之后可以看到rabbitmq服务器的内部状态，队列中的消息收发数目，连接数量，状态等信息

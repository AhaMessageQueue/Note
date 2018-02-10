## 出站、入站

**什么情况下是出站和入站？**

什么情况下出站，例如：
从一个客户端应用程序的角度看，如果事件的运动方向是客户端到服务端，那么我们称这些事件为出站的，反之则成为入站，
同理，从一个服务端的角度看，如果事件的运动方向是服务端到客户端则称为出站，反之为入站。

## 实例

参考代码：

https://github.com/fnpac/netty_study/blob/90d21054e22c20a7de80a071a9d22e933eb440cc/ch03/server_03/src/main/java/EchoServer.java#L52-L56

使用`ctx.write`,handler的执行顺序如下：

1 -> 2 -> 4 -> 3

```text
Jan 08, 2018 3:36:02 PM handler.In1Handler channelRead
信息: in 1 - Netty rocks
Jan 08, 2018 3:36:02 PM handler.In2Handler channelRead
信息: in 2 - Netty rocks
Jan 08, 2018 3:36:02 PM handler.Out4Handler write
信息: out 4 - NETTY ROCKS
Jan 08, 2018 3:36:02 PM handler.Out3Handler write
信息: out 3 - NETTY ROCKS
```

`Out5Handler` 被忽略了。

如果使用`ctx.channel().write`,会导致消息从pipleline尾部开始执行,
执行结果如下：

1 -> 2 -> 5 -> 4 -> 3

```text
Jan 08, 2018 3:46:41 PM handler.In1Handler channelRead
信息: in 1 - Netty rocks
Jan 08, 2018 3:46:41 PM handler.In2Handler channelRead
信息: in 2 - Netty rocks
Jan 08, 2018 3:46:41 PM handler.Out5Handler write
信息: out 5 - NETTY ROCKS
Jan 08, 2018 3:46:41 PM handler.Out4Handler write
信息: out 4 - NETTY ROCKS
Jan 08, 2018 3:46:41 PM handler.Out3Handler write
信息: out 3 - NETTY ROCKS
```

>**在使用Handler的过程中，需要注意**：
>1. `ChannelInboundHandler`之间的传递，通过调用 `ctx.fireChannelRead(msg)` 实现；
调用`ctx.write(msg)` 将传递到`ChannelOutboundHandler`。
2. `ctx.write()`方法执行后，需要调用`flush()`方法才能令它立即执行。
3. `ChannelOutboundHandler` 在注册的时候需要放在最后一个`ChannelInboundHandler`之前，
否则将无法传递到ChannelOutboundHandler(比如 `Out5Handler`),当然也可以使用`ctx.channel().write`。
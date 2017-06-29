## 如何接入ServerSDK
1. 开发者需要登录开发者网站http://developer.xiaomi.com，申请AppID, AppKey, AppSecret。
2. 从开发者网站<http://dev.xiaomi.com/mipush/downpage>下载SDK，将MiPush_SDK_Sever目录下的jar文件加入到自己的服务端项目中。
    Jar文件包括MiPush_SDK_Sever_1_2.jar文件和json-simple-1.1.1.jar。
3. 选择环境。 
    
    在正式环境下使用push服务，启动时需要调用如下代码： 
    ```java
    Constants.useOfficial();
    ```
    
    在测试环境下使用push服务，启动时需要调用如下代码： 
    ```java
    Constants.useSandbox();
    ```
    
    在测试环境中使用push服务不会影响线上用户。
    
    >注：测试环境只提供对IOS支持，不支持Android。
    
Eg,
```java
private void sendMessage() throws Exception {
    Constants.useOfficial();
    Sender sender = new Sender(APP_SECRET_KEY);
    
    String messagePayload = “This is a message”;
    String title = “notification title”;
    String description = “notification description”;
    
    Message message = new Message.Builder()
            .title(title)
            .description(description).payload(messagePayload)
            .restrictedPackageName(MY_PACKAGE_NAME)
            .notifyType(1)     // 使用默认提示音提示
            .build();
    Result result = sender.send(message, regId, 0); //Result对于sendToAlias()，broadcast()和send()调用方式完全一样
    Log.v("Server response: ", "MessageId: " + result.getMessageId()
                            + " ErrorCode: " + result.getErrorCode().toString()
                            + " Reason: " + result.getReason());
}
```
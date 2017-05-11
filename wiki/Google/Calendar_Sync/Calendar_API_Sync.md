>代码地址：https://github.com/googlesamples/calendar-sync

一组示例演示如何在日历API中使用同步令牌和etag来更有效地同步资源。

## Introduction
为了让应用程序更容易与使用者的Google日历资料保持同步，API已加入支持同步令牌的功能。 这些令牌存储有关您已检索的数据的信息，以便下次请求您时，您只会获得自上次同步后添加，更新或删除的资源。 这些示例演示了如何有效地同步，利用同步令牌以及资源版本控制（etags）。

## Prerequisites
阅读以下指南：

- [Syncing Guide](https://developers.google.com/google-apps/calendar/v3/sync)
- [Resource Versioning Guide](https://developers.google.com/google-apps/calendar/v3/version-resources)

设置Java环境：

- Install JDK 1.6 or higher
- Install Apache Maven

## Getting Started

- 编辑`client_secrets.json`并设置客户端ID和密钥。 您可以使用[Google Developers Console](https://console.developers.google.com/)创建ID/secret对。
- 运行`mvn compile`来构建项目。

运行三个示例之一： 

同步令牌示例：
```
mvn exec:java -Dexec.mainClass="com.google.api.services.samples.calendar.sync.SyncTokenSample"
```
条件修改示例：
```
mvn exec:java -Dexec.mainClass="com.google.api.services.samples.calendar.sync.ConditionalModificationSample"
```
条件检索示例：
```
mvn exec:java -Dexec.mainClass="com.google.api.services.samples.calendar.sync.ConditionalRetrievalSample"
```

## Support


- Stack Overflow Tag: [google-calendar](http://stackoverflow.com/questions/tagged/google-calendar)
- Issue Tracker: [apps-api-issues](https://code.google.com/a/google.com/p/apps-api-issues/issues/list)

如果您在此示例中发现错误，请提出问题：https：//github.com/googlesamples/calendar-sync/issues

鼓励补丁，并且可以通过fork这个项目并通过GitHub提交pull request来提交补丁。


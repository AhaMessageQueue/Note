>更新时间：2017-02-10
>详情查看[官方文档](https://developers.google.com/api-client-library/java/)

`Calendar API Client Library for Java`提供了所有Google API的常见功能，
例如HTTP传输，错误处理，身份验证，JSON解析，媒体下载/上传和批处理。 
该类库包括一个具有一致接口的功能强大的[OAuth 2.0库](https://developers.google.com/api-client-library/java/google-api-java-client/oauth2); 轻量级，支持任何数据模式的高效的XML和JSON数据模型; 并支持[协议缓冲区](https://developers.google.com/protocol-buffers/)。

要使用`Calendar API Client Library for Java`调用Google API，您需要为您正在访问的Google API生成Java类库。 
这些[生成的类库](https://developers.google.com/api-client-library/java/apis/)包括核心google-api-java-client库以及特定于API的信息，例如根URL。 它们还包括API上下文中表示实体的类，这些类对于在JSON对象和Java对象之间进行转换非常有用。

### 测试版功能
在`类`或`方法`级别标有@Beta的功能可能会更改。 它们可能在任何主要版本中被修改或删除。 如果您的代码也是作为类库（即，如果您的代码被您的控制之外的用户使用），请不要使用测试版功能。

### 已弃用的功能
已弃用的非beta版功能将在其首次废弃的`release`版本发布后的十八个月内删除。 你必须在此时间之前修正你的用法。 如果不这样做，任何类型的破损都可能导致 ，并且不能保证发生编译错误。

### Calendar API Client Library for Java的亮点

#### 调用Google API非常简单
您可以通过`Calendar API Client Library for Java`，使用Google服务特定的生成类库，调用Google API。 （要查找生成的Google API客户端类库，请访问[支持的Google API列表](https://developers.google.com/api-client-library/java/apis/)
。）以下示例使用`Calendar API Client Library for Java`调用Google Calendar API：

```
// Show events on user's calendar.
 View.header("Show Calendars");
 CalendarList feed = client.calendarList().list().execute();
 View.display(feed);
```
#### 类库使授权更容易
类库包括一个强大的[认证库](https://developers.google.com/api-client-library/java/google-api-java-client/oauth2)，可以减少处理OAuth 2.0所需的代码量。 有时你只需要几行代码。 例如：
```
/** Authorizes the installed application to access user's protected data. */
 private static Credential authorize() throws Exception {
   // load client secrets
   GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
       new InputStreamReader(CalendarSample.class.getResourceAsStream("/client_secrets.json")));
   // set up authorization code flow
   GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
       httpTransport, JSON_FACTORY, clientSecrets,
       Collections.singleton(CalendarScopes.CALENDAR)).setDataStoreFactory(dataStoreFactory)
      .build();
   // authorize
   return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
}
```

#### 类库使批处理和媒体上传/下载更容易
类库提供了用于[批处理](https://developers.google.com/api-client-library/java/google-api-java-client/batch)，[媒体上传](https://developers.google.com/api-client-library/java/google-api-java-client/media-upload)和[媒体下载](https://developers.google.com/api-client-library/java/google-api-java-client/media-download)的辅助类。

#### 类库在Google App Engine上运行
App Engine特定的帮助程序可以快速完成对API的身份验证调用，您无需担心为令牌交换代码。 

```
 @Override
 protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
   AppIdentityCredential credential =
       new AppIdentityCredential(Arrays.asList(UrlshortenerScopes.URLSHORTENER));
   Urlshortener shortener =
       new Urlshortener.Builder(new UrlFetchTransport(), new JacksonFactory(), credential)
       .build();
   UrlHistory history = shortener.URL().list().execute();
   ...
 }
```
#### 类库在Android 1.5或更高版本（@Beta）上运行。
`Calendar API Client Library for Java`的Android特定的帮助类与[Android AccountManager](http://developer.android.com/reference/android/accounts/AccountManager.html)完美集成。 例如：
```
@Override
 public void onCreate(Bundle savedInstanceState) {
   super.onCreate(savedInstanceState);
   // Google Accounts
   credential =
       GoogleAccountCredential.usingOAuth2(this, Collections.singleton(TasksScopes.TASKS));
   SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
   credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
   // Tasks client
   service =
       new com.google.api.services.tasks.Tasks.Builder(httpTransport, jsonFactory, credential)
           .setApplicationName("Google-TasksAndroidSample/1.0").build();
 }
```
#### 安装简单
如果您没有使用[生成的类库](https://developers.google.com/api-client-library/java/apis/)，
可以直接从[下载页面](https://developers.google.com/api-client-library/java/google-api-java-client/download)下载适用于`Calendar API Client Library for Java`的二进制文件，也可以使用Maven或Gradle。 
要使用Maven，请将以下行添加到pom.xml文件中：
```
<project>
  <dependencies>
   <dependency>
     <groupId>com.google.api-client</groupId>
     <artifactId>google-api-client</artifactId>
     <version>1.22.0</version>
   </dependency>
  </dependencies>
 </project>
```
要使用Gradle，请将以下行添加到build.gradle文件中：
```
 repositories {
      mavenCentral()
  }
  dependencies {
      compile 'com.google.api-client:google-api-client:1.22.0'
  }
```
有关安装和设置`Calendar API Client Library for Java`的详细信息，请参阅[下载和设置说明](https://developers.google.com/api-client-library/java/google-api-java-client/setup)。

#### 支持环境
适用于Java的Google API客户端库支持以下Java环境：
- Java 5或更高版本，标准（SE）和企业（EE）。 
- [Google App Engine](https://cloud.google.com/appengine/docs)。 
- Android 1.5或更高版本 - 但如果您需要的Google服务可以使用[Google Play服务库](https://developer.android.com/google/play-services/index.html)，请使用该库，而不是这个库。 Google Play库将为您提供最佳的效果和体验。

不支持：Google Web Toolkit（GWT），Java移动（ME）和Java 1.4（或更早版本）。

#### 依赖
`Google API Client Library for Java`（google-api-java-client）构建在两个通用库的基础上，这些库也是由Google构建的，并且也设计为与网络上的任何HTTP服务配合使用：
- [Google HTTP Client Library for Java](https://developers.google.com/api-client-library/java/google-http-java-client/)
- [Google OAuth Client Library for Java](https://developers.google.com/api-client-library/java/google-oauth-java-client/)
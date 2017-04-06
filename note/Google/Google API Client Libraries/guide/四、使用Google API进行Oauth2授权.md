### 概述
目的：本文档介绍如何使用[GoogleCredential](https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/auth/oauth2/GoogleCredential.html)工具类对Google服务执行OAuth 2.0授权。 
有关我们提供的通用OAuth 2.0功能的信息，请参阅[OAuth 2.0和Google OAuth客户端库](https://developers.google.com/api-client-library/java/google-oauth-java-client/oauth2)。

摘要：要访问存储在Google服务中的受保护数据，请使用[OAuth 2.0](https://developers.google.com/accounts/docs/OAuth2)进行授权。 
Google API支持针对不同类型的客户端应用程序的OAuth 2.0流程。 
在所有这些流程中，客户端应用程序请求仅与您的客户端应用程序和正在访问的受保护数据的所有者相关联的访问令牌。 
访问令牌还与`a limited scope`相关联，该范围定义客户端应用程序可访问的数据类型（例如“管理您的任务”）。 
OAuth 2.0的一个重要目标是提供对受保护数据的安全和方便的访问，同时最小化访问令牌被盗时的潜在影响。

Google API客户端库中的OAuth 2.0软件包构建在通用的Google OAuth 2.0客户端库（Java版）上。
有关详细信息，请参阅以下软件包的Javadoc文档： 

- [com.google.api.client.googleapis.auth.oauth2](https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/auth/oauth2/package-summary.html)（from [google-api-client](https://developers.google.com/api-client-library/java/google-api-java-client/setup#google-api-client)） 
- [com.google.api.client.googleapis.extensions.appengine.auth.oauth2](https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/extensions/appengine/auth/oauth2/package-summary.html)（from [google-api-client-appengine](https://developers.google.com/api-client-library/java/google-api-java-client/setup#google-api-client-appengine)）

### Google Developers Console
在您可以访问Google API之前，无论您的客户端是已安装的应用，移动应用，网络服务器还是在浏览器中运行的客户端，您都需要在[Google Developers Console](https://console.developers.google.com/)上设置一个用于授权和结算目的的项目。

有关正确设置凭据的说明，请参阅[Developers Console帮助](https://developer.google.com/console/help/console/)。

### 凭据
#### GoogleCredential
[GoogleCredential](https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/auth/oauth2/GoogleCredential.html)是OAuth 2.0的线程安全工具类，可使用访问令牌访问受保护的资源。 例如，如果您已拥有访问令牌，则可以通过以下方式发出请求：
```
GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
Plus plus = new Plus.builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
    .setApplicationName("Google-PlusSample/1.0")
    .build();
```

#### Google App Engine identity
这种替代凭证基于[Google App Engine App Identity Java API](https://cloud.google.com/appengine/docs/java/appidentity/?csw=1#Asserting_Identity_to_Google_APIs)。 
与客户端应用程序请求访问最终用户数据的凭证不同，`App Identity API `提供对客户端应用程序自己数据的访问。
使用[AppIdentityCredential](https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/extensions/appengine/auth/oauth2/AppIdentityCredential.html)（from[google-api-client-appengine](https://developers.google.com/api-client-library/java/google-api-java-client/setup#google-api-client-appengine)）。
这个凭证要简单得多，因为Google App Engine会处理所有细节。 
您只需指定所需的OAuth 2.0`scope`。
取自[urlshortener-robots-appengine-sample](https://github.com/google/google-api-java-client-samples/tree/master/urlshortener-robots-appengine-sample)的示例代码：
```
static Urlshortener newUrlshortener() {
  AppIdentityCredential credential =
      new AppIdentityCredential(Arrays.asList(UrlshortenerScopes.URLSHORTENER));
  return new Urlshortener.Builder(new UrlFetchTransport(), JacksonFactory.getDefaultInstance(), credential)
      .build();
}
```

### Data store
访问令牌的有效期通常为1小时，之后如果您尝试使用访问令牌，则会收到错误。 [GoogleCredential](https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/auth/oauth2/GoogleCredential.html)会自动“刷新”令牌，这意味着获取新的访问令牌。 
这是通过有着很长有效期的刷新令牌（refresh token）完成的，如果您在授权代码流程中使用access_type = offline参数，则通常会与访问令牌一起接收到。（请参阅[GoogleAuthorizationCodeFlow.Builder.setAccessType（String）](https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/auth/oauth2/GoogleAuthorizationCodeFlow.Builder.html#setAccessType(java.lang.String))）。

大多数应用程序将需要持久化凭证的访问令牌和/或刷新令牌。 要持久化凭据的访问和/或刷新令牌，您可以使用[StoredCredential](https://developers.google.com/api-client-library/java/google-oauth-java-client/reference/1.20.0/com/google/api/client/auth/oauth2/StoredCredential.html)提供自己的[DataStoreFactory](https://developers.google.com/api-client-library/java/google-http-java-client/reference/1.20.0/com/google/api/client/util/store/DataStoreFactory.html)和[DataStore](https://developers.google.com/api-client-library/java/google-http-java-client/reference/1.20.0/com/google/api/client/util/store/DataStore.html)实现; 或者可以使用库提供的以下实现之一：

- [JdoDataStoreFactory](https://developers.google.com/api-client-library/java/google-http-java-client/reference/1.20.0/com/google/api/client/extensions/jdo/JdoDataStoreFactory.html)：使用JDO持久化凭证。 
- [AppEngineDataStoreFactory](https://developers.google.com/api-client-library/java/google-http-java-client/reference/1.20.0/com/google/api/client/extensions/appengine/datastore/AppEngineDataStoreFactory.html)：使用Google App Engine Data Store API持久化凭证。 
- [MemoryDataStoreFactory](https://developers.google.com/api-client-library/java/google-http-java-client/reference/1.20.0/com/google/api/client/util/store/MemoryDataStoreFactory.html)：将凭证“持久”在内存中，这只是作为进程生命周期的短期存储有用。 
- [FileDataStoreFactory](https://developers.google.com/api-client-library/java/google-http-java-client/reference/1.20.0/com/google/api/client/util/store/FileDataStoreFactory.html)：将凭证保留在文件中。

AppEngine用户：[AppEngineCredentialStore](https://developers.google.com/api-client-library/java/google-oauth-java-client/reference/1.20.0/com/google/api/client/extensions/appengine/auth/oauth2/AppEngineCredentialStore.html)已弃用，很快就会删除。 
我们建议您使用[AppEngineDataStoreFactory](https://developers.google.com/api-client-library/java/google-http-java-client/reference/1.20.0/com/google/api/client/util/store/AbstractDataStoreFactory.html)与[StoredCredential](https://developers.google.com/api-client-library/java/google-oauth-java-client/reference/1.20.0/com/google/api/client/auth/oauth2/StoredCredential.html)。
如果您以旧方式存储凭据，则可以使用添加的辅助方法[migrateTo（AppEngineDataStoreFactory）](https://developers.google.com/api-client-library/java/google-oauth-java-client/reference/1.20.0/com/google/api/client/extensions/appengine/auth/oauth2/AppEngineCredentialStore.html#migrateTo(com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory))或[migrateTo（DataStore）](https://developers.google.com/api-client-library/java/google-oauth-java-client/reference/1.20.0/com/google/api/client/extensions/appengine/auth/oauth2/AppEngineCredentialStore.html#migrateTo(com.google.api.client.util.store.DataStore))进行迁移。

您可以使用[DataStoreCredentialRefreshListener](https://developers.google.com/api-client-library/java/google-oauth-java-client/reference/1.20.0/com/google/api/client/auth/oauth2/DataStoreCredentialRefreshListener.html)并使用[GoogleCredential.Builder.addRefreshListener（CredentialRefreshListener）](https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/auth/oauth2/GoogleCredential.Builder.html#addRefreshListener(com.google.api.client.auth.oauth2.CredentialRefreshListener))为凭据设置它。

### Authorization code flow
使用授权代码流程允许最终用户授予您的应用访问用户在Google API上受保护数据的权限。此流程的协议在授权码授权中指定。

此流程使用GoogleAuthorizationCodeFlow实现。步骤是：

- 最终用户登录到您的应用程序。您需要将该用户与对您的应用程序唯一的用户ID相关联。
- 基于用户ID调用[AuthorizationCodeFlow.loadCredential（String）](https://developers.google.com/api-client-library/java/google-oauth-java-client/reference/1.20.0/com/google/api/client/auth/oauth2/AuthorizationCodeFlow.html#loadCredential(java.lang.String))以检查最终用户的凭据是否已知。如果是这样，我们就完成了。
- 如果没有，请调用[AuthorizationCodeFlow.newAuthorizationUrl（）](https://developers.google.com/api-client-library/java/google-oauth-java-client/reference/1.20.0/com/google/api/client/auth/oauth2/AuthorizationCodeFlow.html#newAuthorizationUrl())并将最终用户的浏览器定向到授权页面，以授予应用程序访问受保护数据的权限。
- 然后，Google授权服务器会将浏览器重定向到应用指定的重定向网址并指定code参数。使用code参数使用[AuthorizationCodeFlow.newTokenRequest（String）](https://developers.google.com/api-client-library/java/google-oauth-java-client/reference/1.20.0/com/google/api/client/auth/oauth2/AuthorizationCodeFlow.html#newTokenRequest(java.lang.String))请求访问令牌。
- 使用[AuthorizationCodeFlow.createAndStoreCredential（TokenResponse，String）](https://developers.google.com/api-client-library/java/google-oauth-java-client/reference/1.20.0/com/google/api/client/auth/oauth2/AuthorizationCodeFlow.html#createAndStoreCredential(com.google.api.client.auth.oauth2.TokenResponse,%20java.lang.String))存储和获取用于访问受保护资源的凭据。

或者，如果您不使用[GoogleAuthorizationCodeFlow](https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/auth/oauth2/GoogleAuthorizationCodeFlow.html)，则可以使用较低级别的类：

使用[DataStore.get（String）](https://developers.google.com/api-client-library/java/google-http-java-client/reference/1.20.0/com/google/api/client/util/store/DataStore.html#get(java.lang.String))根据用户ID从存储加载凭据。
使用[GoogleAuthorizationCodeRequestUrl](https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/auth/oauth2/GoogleAuthorizationCodeRequestUrl.html)将浏览器定向到授权页面。
使用[AuthorizationCodeResponseUrl](https://developers.google.com/api-client-library/java/google-oauth-java-client/reference/1.20.0/com/google/api/client/auth/oauth2/AuthorizationCodeResponseUrl.html)来处理授权响应并解析授权码。
使用[GoogleAuthorizationCodeTokenRequest](https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/auth/oauth2/GoogleAuthorizationCodeTokenRequest.html)请求访问令牌和可能的刷新令牌。
创建一个新的[GoogleCredential](https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/auth/oauth2/GoogleCredential.html)并使用[DataStore.set（String，V）](https://developers.google.com/api-client-library/java/google-http-java-client/reference/1.20.0/com/google/api/client/util/store/DataStore.html#set(java.lang.String,%20V))存储它。
使用`GoogleCredential`访问受保护的资源。已过期的访问令牌将使用刷新令牌（如果适用）自动刷新。确保使用[DataStoreCredentialRefreshListener](https://developers.google.com/api-client-library/java/google-oauth-java-client/reference/1.20.0/com/google/api/client/auth/oauth2/DataStoreCredentialRefreshListener.html)并使用[GoogleCredential.Builder.addRefreshListener（CredentialRefreshListener）](https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/auth/oauth2/GoogleCredential.Builder.html#addRefreshListener(com.google.api.client.auth.oauth2.CredentialRefreshListener))为凭据设置它。

在Google Developers Console中设置项目时，您可以根据所使用的流程在不同的凭证中进行选择。有关详情，请参阅设置OAuth 2.0和OAuth 2.0场景。每个流的代码片段如下。

#### Web server applications
此流程的协议是使用[OAuth 2的Web服务器应用程序](https://developers.google.com/accounts/docs/OAuth2WebServer)的解释。
此库提供servlet帮助类，以大大简化基本用例的授权代码流程。 您只需提供[AbstractAuthorizationCodeServlet](https://developers.google.com/api-client-library/java/google-oauth-java-client/reference/1.20.0/com/google/api/client/extensions/servlet/auth/oauth2/AbstractAuthorizationCodeServlet.html)和[AbstractAuthorizationCodeCallbackServlet](https://developers.google.com/api-client-library/java/google-oauth-java-client/reference/1.20.0/com/google/api/client/extensions/servlet/auth/oauth2/AbstractAuthorizationCodeCallbackServlet.html)（从[google-oauth-client-servlet](https://developers.google.com/api-client-library/java/google-oauth-java-client/setup#google-oauth-client-servlet)）的具体子类，并将它们添加到您的web.xml文件。 
请注意，您仍然需要处理您的Web应用程序的用户登录并提取用户ID。 [JdoDataStoreFactory](https://developers.google.com/api-client-library/java/google-http-java-client/reference/1.20.0/com/google/api/client/extensions/jdo/JdoDataStoreFactory.html)（来自[google-oauth-client-servlet](https://developers.google.com/api-client-library/java/google-oauth-java-client/setup#google-oauth-client-servlet)）是使用JDO持久化凭据的一个很好的选择。

```
public class CalendarServletSample extends AbstractAuthorizationCodeServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // do stuff
  }

  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    url.setRawPath("/oauth2callback");
    return url.build();
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return new GoogleAuthorizationCodeFlow.Builder(
        new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
        "[[ENTER YOUR CLIENT ID]]", "[[ENTER YOUR CLIENT SECRET]]",
        Collections.singleton(CalendarScopes.CALENDAR)).setDataStoreFactory(
        DATA_STORE_FACTORY).setAccessType("offline").build();
  }

  @Override
  protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
    // return user ID
  }
}

public class CalendarServletCallbackSample extends AbstractAuthorizationCodeCallbackServlet {

  @Override
  protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
      throws ServletException, IOException {
    resp.sendRedirect("/");
  }

  @Override
  protected void onError(
      HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
      throws ServletException, IOException {
    // handle error
  }

  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    url.setRawPath("/oauth2callback");
    return url.build();
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return new GoogleAuthorizationCodeFlow.Builder(
        new NetHttpTransport(), JacksonFactory.getDefaultInstance()
        "[[ENTER YOUR CLIENT ID]]", "[[ENTER YOUR CLIENT SECRET]]",
        Collections.singleton(CalendarScopes.CALENDAR)).setDataStoreFactory(
        DATA_STORE_FACTORY).setAccessType("offline").build();
  }

  @Override
  protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
    // return user ID
  }
}
```

#### Google App Engine applications
App Engine上的授权代码流程与servlet授权代码流程几乎完全相同，只是我们可以利用Google App Engine的Users Java API。 
用户需要登录才能启用[Users Java API](https://cloud.google.com/appengine/docs/java/users/); 有关将用户重定向到登录页面的信息（如果他们尚未登录），请参阅[安全和身份验证](https://cloud.google.com/appengine/docs/java/config/webxml?csw=1#Security_and_Authentication)（在web.xml中）。
与servlet案例的主要区别在于，您提供[AbstractAppEngineAuthorizationCodeServlet](https://developers.google.com/api-client-library/java/google-oauth-java-client/reference/1.20.0/com/google/api/client/extensions/appengine/auth/oauth2/AbstractAppEngineAuthorizationCodeServlet.html)和[AbstractAppEngineAuthorizationCodeCallbackServlet](https://developers.google.com/api-client-library/java/google-oauth-java-client/reference/1.20.0/com/google/api/client/extensions/appengine/auth/oauth2/AbstractAppEngineAuthorizationCodeCallbackServlet.html)（来自[google-oauth-client-appengine](https://developers.google.com/api-client-library/java/google-oauth-java-client/setup#google-oauth-client-appengine)）的具体子类，它们使用Users Java API扩展抽象servlet类并实现getUserId方法。
[AppEngineDataStoreFactory](https://developers.google.com/api-client-library/java/google-http-java-client/reference/1.20.0/com/google/api/client/extensions/appengine/datastore/AppEngineDataStoreFactory.html) (from [google-http-client-appengine](https://developers.google.com/api-client-library/java/google-http-java-client/setup#google-http-client-appengine))是使用Google App Engine Data Store API持久化凭证的不错选项。

使用[calendar-appengine-sample](https://github.com/google/google-api-java-client-samples/tree/master/calendar-appengine-sample)所做的示例（稍作修改）：
```
public class CalendarAppEngineSample extends AbstractAppEngineAuthorizationCodeServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // do stuff
  }

  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    return Utils.getRedirectUri(req);
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return Utils.newFlow();
  }
}

class Utils {
  static String getRedirectUri(HttpServletRequest req) {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    url.setRawPath("/oauth2callback");
    return url.build();
  }

  static GoogleAuthorizationCodeFlow newFlow() throws IOException {
    return new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
        getClientCredential(), Collections.singleton(CalendarScopes.CALENDAR)).setDataStoreFactory(
        DATA_STORE_FACTORY).setAccessType("offline").build();
  }
}

public class OAuth2Callback extends AbstractAppEngineAuthorizationCodeCallbackServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
      throws ServletException, IOException {
    resp.sendRedirect("/");
  }

  @Override
  protected void onError(
      HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
      throws ServletException, IOException {
    String nickname = UserServiceFactory.getUserService().getCurrentUser().getNickname();
    resp.getWriter().print("<h3>" + nickname + ", why don't you want to play with me?</h1>");
    resp.setStatus(200);
    resp.addHeader("Content-Type", "text/html");
  }

  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    return Utils.getRedirectUri(req);
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return Utils.newFlow();
  }
}
```
有关其他示例，请参见storage-serviceaccount-appengine-sample。

#### Service accounts
[GoogleCredential](https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/auth/oauth2/GoogleCredential.html)还支持[服务帐户](https://developers.google.com/accounts/docs/OAuth2ServiceAccount)。 与客户端应用程序请求访问最终用户数据的凭证不同，服务帐户提供对客户端应用程序自己的数据的访问。 您的客户端应用程序使用从[Google Developers Console](https://console.developers.google.com/)下载的私钥对访问令牌的请求进行签名。

取自[plus-serviceaccount-cmdline-sample](https://github.com/google/google-api-java-client-samples/tree/master/plus-serviceaccount-cmdline-sample)的示例代码：
```
HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
...
// Build service account credential.

GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("MyProject-1234.json"))
    .createScoped(Collections.singleton(PlusScopes.PLUS_ME));
// Set up global Plus instance.
plus = new Plus.Builder(httpTransport, jsonFactory, credential)
    .setApplicationName(APPLICATION_NAME).build();
...
```
有关其他示例，请参阅[storage-serviceaccount-cmdline-sample](https://github.com/GoogleCloudPlatform/cloud-storage-docs-xml-api-examples)。

注意：虽然您可以在Google Apps域执行的应用程序中使用服务帐户，但服务帐户不是Google Apps帐户的成员，不受Google Apps管理员设定的网域政策管辖。 例如，Google Apps管理控制台中设置的限制Apps最终用户在域外共享文档的功能的策略不适用于服务帐户。

##### Impersonation
您还可以使用服务帐户流程模拟您拥有的域中的用户。 这与上述服务帐户流程非常相似，但您还需要调用[GoogleCredential.Builder.setServiceAccountUser（String）](https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/auth/oauth2/GoogleCredential.Builder.html#setServiceAccountUser(java.lang.String))。

#### Installed applications
[Using OAuth 2.0 for Installed Applications](https://developers.google.com/accounts/docs/OAuth2InstalledApp)描述的`command-line authorization code flow`。
[plus-cmdline-sample](https://github.com/google/google-api-java-client-samples/tree/master/plus-cmdline-sample)中的示例代码段：
```
public static void main(String[] args) {
  try {
    httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
    // authorization
    Credential credential = authorize();
    // set up global Plus instance
    plus = new Plus.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
        APPLICATION_NAME).build();
   // ...
}

private static Credential authorize() throws Exception {
  // load client secrets
  GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
      new InputStreamReader(PlusSample.class.getResourceAsStream("/client_secrets.json")));
  // set up authorization code flow
  GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
      httpTransport, JSON_FACTORY, clientSecrets,
      Collections.singleton(PlusScopes.PLUS_ME)).setDataStoreFactory(
      dataStoreFactory).build();
  // authorize
  return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
}
```

#### Client-side applications
要使用为[客户端应用程序使用OAuth 2.0](https://developers.google.com/accounts/docs/OAuth2UserAgent)中所述的基于浏览器的客户端流程，您通常需要执行以下步骤： 

- 使用[GoogleBrowserClientRequestUrl](https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/auth/oauth2/GoogleBrowserClientRequestUrl.html)将浏览器中的最终用户重定向到授权页面，以允许浏览器应用程序访问最终用户的受保护数据。 
- 使用[适用于JavaScript的Google API客户端库](https://developers.google.com/api-client-library/javascript/)来处理在Google Developers Console中注册的重定向URI的网址片段中的访问令牌。

Web应用程序的示例用法：
```
public void doGet(HttpServletRequest request, HttpServletResponse response)throws IOException {
  String url = new GoogleBrowserClientRequestUrl("812741506391.apps.googleusercontent.com",
      "https://oauth2-login-demo.appspot.com/oauthcallback", Arrays.asList(
          "https://www.googleapis.com/auth/userinfo.email",
          "https://www.googleapis.com/auth/userinfo.profile")).setState("/profile").build();
  response.sendRedirect(url);
}
```

#### Android
##### 哪些库能被android使用
如果您要开发Android，您想要使用的Google API被包含在[Google Play服务库](https://developer.android.com/google/play-services/index.html)中，请使用该库以获得最佳效果和体验。 如果您要使用的适用于Android的Google API不是Google Play服务库的一部分，则您可以使用适用于Java的Google API客户端库（支持Android 1.5（或更高版本）），如下所述。适用于Java的Google API客户端库中@Beta支持 Android。

##### Background
从Eclair（SDK 2.1）开始，用户帐户在Android设备上使用帐户管理器进行管理。 所有Android应用程序授权由SDK使用[AccountManager](http://developer.android.com/reference/android/accounts/AccountManager.html)集中管理。 您可以指定应用程序需要的OAuth 2.0 `scope`，并返回要使用的访问令牌。
OAuth 2.0 `scope`通过`authTokenType`参数指定为`oauth2：`加上`scope`。 例如：

```
oauth2:https://www.googleapis.com/auth/tasks
```
这指定了对Google Tasks API的读/写访问权限。 如果您需要多个OAuth 2.0范围，请使用空格分隔的列表。
一些API还有特殊的authTokenType参数。 例如，"Manage your tasks"是上面显示的authtokenType示例的别名。
您还必须从指定Google Developers Console中的的API密钥。 否则，AccountManager给您的令牌只为您提供匿名配额，通常非常低。 
相比之下，通过指定API密钥，您可以获得更高的免费配额，并可以选择性地设置超过此限制的使用费用。
[tasks-android-sample](https://github.com/google/google-api-java-client-samples/tree/master/tasks-android-sample)中的示例代码段：
```
com.google.api.services.tasks.Tasks service;

@Override
public void onCreate(Bundle savedInstanceState) {
  credential =
      GoogleAccountCredential.usingOAuth2(this, Collections.singleton(TasksScopes.TASKS));
  SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
  credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
  service =
      new com.google.api.services.tasks.Tasks.Builder(httpTransport, jsonFactory, credential)
          .setApplicationName("Google-TasksAndroidSample/1.0").build();
}

private void chooseAccount() {
  startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  super.onActivityResult(requestCode, resultCode, data);
  switch (requestCode) {
    case REQUEST_GOOGLE_PLAY_SERVICES:
      if (resultCode == Activity.RESULT_OK) {
        haveGooglePlayServices();
      } else {
        checkGooglePlayServicesAvailable();
      }
      break;
    case REQUEST_AUTHORIZATION:
      if (resultCode == Activity.RESULT_OK) {
        AsyncLoadTasks.run(this);
      } else {
        chooseAccount();
      }
      break;
    case REQUEST_ACCOUNT_PICKER:
      if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
        String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
        if (accountName != null) {
          credential.setSelectedAccountName(accountName);
          SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
          SharedPreferences.Editor editor = settings.edit();
          editor.putString(PREF_ACCOUNT_NAME, accountName);
          editor.commit();
          AsyncLoadTasks.run(this);
        }
      }
      break;
  }
}
```


谷歌的 OAuth 2.0 系统支持 server-to-server 的交互，类似于 Web 应用与谷歌服务那样的交互。
为了使用这类方案，你需要一个服务账号，这个账号是属于你的应用程序的，用以代替个人终端用户。
你的应用程序代表服务账号调用谷歌的应用接口，所以用户不用直接参与。这个方案有时被称为 two-legged OAuth 或者 2LO。（相关词 three-legged OAuth 指的是一个应用程序代表终端用户调用谷歌应用接口的方案，有时是需要用户同意的）

特别地，当应用程序调用谷歌应用接口时，应用程序使用服务账号，更多是处理应用程序本身的数据而不是用户的数据。例如应用程序使用谷歌的云存储服务来储存持久化数据时，将会通过服务账号来鉴定应用的调用。

如果你拥有一个谷歌应用域名，又或者你使用谷歌应用来工作，例如，谷歌应用的管理员（`G Suite domain administrators `）可以在的应用域名里授权应用代表用户访问用户数据。
再例如，调用谷歌日历接口的应用，将在域名内代替用户添加事项到所有用户的日历中去。在域名内，服务账号被授权代替用户访问数据，有时称为域范围内的授权认证。

>注意：当你使用谷歌应用市场来安装应用程序到你的设备上时，请求的权限将会自动授予给应用。你不需要手动为应用使用的服务账号授权。

以下文档描述了一个应用程序如何使用谷歌应用接口的客户端程序库或者 http，来完成 server-to-server 的 OAuth 2.0 协议。

## 内容
### 概述
为了支持 server-to-server 的交互，首先在开发者面板中为你的项目创建一个服务账号。
如果要访问G Suite域中的用户的用户数据，请将域范围内的访问权限委派给服务帐户。

接着，准备你的应用程序，通过服务账号的证书来调用授权接口，向 OAuth 2.0 auth 服务发出请求，获取访问令牌。

最后，你的应用程序能够使用访问令牌来调用谷歌应用接口。

>建议：你的应用程序可以通过你熟悉的编程语言的谷歌接口库或者通过使用 OAuth 2.0 系统和 HTTP 来完成这些任务。然而，server-to-server 的授权交互的体系结构会要求应用创建用于存储密钥的 JSON Web Tokens，这种做法非常容易出现错误，严重影响到你应用的安全。
因为这个原因，我们强烈建议你使用封装好的库，例如 `Google APIs client libraries`，它从你的代码中抽象了加密过程。

### 创建服务账号

服务账号的证书包含了唯一的电子邮件地址，一个客户 ID，至少一对公共/私人密钥。

如果你的程序运行在谷歌应用引擎上，当你创建项目时就会自动建立起一个服务账号。

如果你的应用运行在谷歌计算引擎上，当你创建项目时也会自动建立起一个服务账号，但是在你创建谷歌计算引擎实例时，你必须指定应用可以访问的范围。浏览更多信息，请查看 [Preparing an instance to use service accounts](https://cloud.google.com/compute/docs/authentication#using)。

如果你的应用并没有运行在谷歌应用引擎或者谷歌计算引擎上时，你必须在谷歌开发者面板中获取证书。为了生成一个服务账号的证书，或者浏览一个你早已生成公共证书，按如下步骤执行：

- 打开[服务帐户页面](https://console.developers.google.com/permissions/serviceaccounts)。 如果出现提示，请选择项目。 
- 单击创建服务帐户。 
- 在创建服务帐户窗口中，键入服务帐户的名称，然后选择配备新的专用密钥。 如果您想要[将G Suite的域范围权限授予服务帐户](https://developers.google.com/identity/protocols/OAuth2ServiceAccount#delegatingauthority)，请选择`Enable G Suite Domain-wide Delegation`。 然后单击创建。

你创建了新的密钥，然后你就可以下载到你的机器上了。它作为该密钥的唯一副本。 您负责安全地存储它。控制台只会在你初始化服务账号时显示你的私有密钥的密码，然后密码就不会再次显示了。你现在获取了一个 **Generate New JSON Key** 和 **Generate New P12 Key** 以及删除密钥的权利。

在任何时刻你都可以返回[开发者面板](https://console.developers.google.com/)去查看 `client ID`，电子邮件地址，公共密钥指纹或者生成额外的公共密钥/私有钥对。了解更多服务账号证书的细节请打开开发者面板，参阅 [Service accounts](https://developers.google.com/console/help/service-accounts) 的帮助文件。

留意服务账号的电子邮箱地址，存储服务账号的 P12 私有密钥到本地，让你的应用程序可以访问到这些东西。你的应用程序在调用授权接口时需要用到它们。

注意：在开发环境或者产品环境中，你都必须存储以及严密地管理自己的私有密钥。谷歌不会保存你的私有密钥，只会保存公共密钥。

### 服务账号域内授权
如果你的应用想访问用户数据，你创建的服务账号应确保有访问那些你想要访问的谷歌应用域内用户数据的权限。

以下步骤必须由管理员执行:

- 前往你的谷歌应用的管理员面板。
- 在列表中选中 **Security**。如果你不能看见 **Security** 列表，在页面底部的灰色栏中选中 **More controls**，然后在列表中选中 **Security**，确保你是以管理员身份执行。
- 选中 **Show more**，然后在选项中选中 **Advanced settings**。
- 在 **Authentication** 中选中 **Manage API client access**。
- 在 **Client Name** 字段中输入服务帐户的 **Client ID**。
- 在 **One or More API Scopes** 字段输入你应用可以访问的范围。例如，你的应用需要域内访问谷歌的导航应用接口以及谷歌日历应用接口，便可以输入： **https://www.googleapis.com/auth/drive**，**https://www.googleapis.com/auth/calendar**。
点击 **Authorize**。

您的应用程序现在有应用接口调用权限，就像你域内的一个用户(模拟用户)。准备进行已授权的API调用时，请指定要模拟的用户。

### 准备做一个授权 API 调用
#### Java
在你从开发者面板获取客户端电子邮箱和私有密钥之后，使用 Java 版的 [Google APIs Client Library](https://code.google.com/p/google-api-java-client/wiki/OAuth2#Service_Accounts)，根据服务账号的证书以及你的应用需要访问的范围来创建 `GoogleCredential` 对象。例如：

**平台： Google App Engine**

```
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.services.sqladmin.SQLAdminScopes;

// ...

AppIdentityCredential credential =
    new AppIdentityCredential(SQLAdminScopes.SQLSERVICE_ADMIN);
```

>注意：如果你的应用运行在 `Google App Engine`，你只能使用 `AppIdentityCredential` 证书对象。如果你的应用需要在其他运行环境上运行，在本地测试你的应用时，必须检测环境以及使用不同的证书机制（查看[其他平台](https://developers.google.com/identity/protocols/OAuth2ServiceAccount#jwtsample_java)）。

**平台：Google Compute Engine**

```
import com.google.api.client.googleapis.compute.ComputeCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

// ...

JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
ComputeCredential credential = new ComputeCredential.Builder()
    .setTransport(httpTransport)
    .setJsonFactory(JSON_FACTORY)
    .build();
```

>注意：如果你的应用程序运行在 `Google Compute Engine`，你只能使用 `ComputeCredential` 证书对象。如果你的应用需要在其他运行环境上运行，在本地测试你的应用时，必须检测环境以及使用不同的证书机制（查看[其他平台](https://developers.google.com/identity/protocols/OAuth2ServiceAccount#jwtsample_java)）。你可以使用[默认证书](https://developers.google.com/identity/protocols/application-default-credentials)来简化流程。

**平台：其他平台**

```
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sqladmin.SQLAdminScopes;

// ...

String emailAddress = "123456789000-abc123def456@developer.gserviceaccount.com";
JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
GoogleCredential credential = new GoogleCredential.Builder()
    .setTransport(httpTransport)
    .setJsonFactory(JSON_FACTORY)
    .setServiceAccountId(emailAddress)
    .setServiceAccountPrivateKeyFromP12File(new File("MyProject.p12"))
    .setServiceAccountScopes(Collections.singleton(SQLAdminScopes.SQLSERVICE_ADMIN))
    .build();
```

如果你委托域内访问给服务账号，然后你想模仿一个用户账号，要指定用户的电子邮箱给 `GoogleCredential` 对象的 `setServiceAccountUser` 方法。例如:

```
GoogleCredential credential = new GoogleCredential.Builder()
    .setTransport(httpTransport)
    .setJsonFactory(JSON_FACTORY)
    .setServiceAccountId(emailAddress)
    .setServiceAccountPrivateKeyFromP12File(new File("MyProject.p12"))
    .setServiceAccountScopes(Collections.singleton(SQLAdminScopes.SQLSERVICE_ADMIN))
    .setServiceAccountUser("user@example.com")
    .build();

```

在你的应用中使用 `GoogleCredential` 对象来调用谷歌应用接口。

#### HTTP/REST
>建议：你的应用程序可以通过使用 OAuth 2.0 系统和 HTTP 来完成这些任务。然而 server-to-server 的授权交互的体系结构会要求应用创建用于存储密钥的 JSON Web Tokens，这种做法非常容易出现错误，严重影响到你应用程序的安全。
因为这个原因，我们强烈建议你使用封装好的库，例如 Google APIs client libraries，他从你的代码中抽象了加密过程。
****
在你从开发者面板获取到 client ID 和私人密钥后，你需要完成如下步骤：


- 一个 [JSON Web Tokens](https://developers.google.com/identity/protocols/OAuth2ServiceAccount#creatingjwt)，包含了一个头部，一个设置，一个签名。
- 从 Google OAuth 2.0 授权服务[请求一个访问码](https://developers.google.com/identity/protocols/OAuth2ServiceAccount#makingrequest)。
- [处理授权服务返回的 JSON 响应](https://developers.google.com/identity/protocols/OAuth2ServiceAccount#handlingresponse)。

如果响应包含了一个访问码，你可以使用这个访问码来调用谷歌应用接口。（如果响应没有包含访问令牌，你的JSON Web Token 和令牌请求或许不符合格式，或者服务账号没有权限去访问被请求的范围。）

当访问码的过了[有效期](https://developers.google.com/identity/protocols/OAuth2ServiceAccount#expiration)，你的应用会生成另一个 JWT，记下他，然后使用它来请求另一个访问码。

![](http://udn.yyuap.com/doc/wiki/project/google-oauth-2/images/serviceaccounts.png)

本节的其余部分描述了创建JWT，签名JWT，形成访问令牌请求和处理响应的细节。

##### 创建一个 JWT
一个 JWT 是由三部分组成：一个头部，一个要求设置，一个签名。头部和要求设置是 JSON 对象，这些 JSON 对象被序列化成 UTF-8 字节，然后通过 Base64url 进行编码。由于重复编码操作对编码的变化提供了弹性。头部，要求设置，签名通过 （.） 字符来串接在一起。

一个 JWT 的组成如下：
```
{Base64url encoded header}.{Base64url encoded claim set}.{Base64url encoded signature}
```

签名的内容组成如下：
```
{Base64url encoded header}.{Base64url encoded claim set}
```

##### 格式化 JWT 的头部
头部由两个字段组成，一个是签名的算法，一个是格式的类型。全部的字段都是强制性的，每个字段只有一个值。说明了其他的算法和格式，头部也会相应的变化。

服务账号依赖于 RSA SHA-256 算法和 JWT token 格式。一个头部的 JSON 示例如下：
```
{"alg":"RS256","typ":"JWT"}
```

Base64url 编码后的展示如下：
```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9
```

##### 格式化 JWT 的要求设置
JWT 的要求设置包含了 JWT 的相关信息，包含了请求的权限（scopes），目标的令牌，发行者，令牌被生产时的时间，令牌的有效期。大部分字段是必要的，就像 JWT 的头部，JWT 的要求设置是一个 JSON 对象，用于签名的计算。

**请求要求：**

在下面展示了请求要求的字段。他们可以以任意的顺序出现。
```
字段名 	描述
iss 	服务账号的电子邮箱
scope 	一个用空格分隔应用程序请求的权限列表。
aud 	目标的描述符的声明。做一个访问令牌请求时这个值总是 https://www.googleapis.com/oauth2/v3/token.
exp 	声明的过期时间，指定为秒就是从00:00:00 UTC，1970年1月1日。这个值最大为发布后1小时。
iat 	声明时，指定为秒就是从00:00:00 UTC，1970年1月1日。
```
一个 JWT 的要求设置的 JSON 如下：
```
{
  "iss":"761326798069-r5mljlln1rd4lrbhg75efgigp36m78j5@developer.gserviceaccount.com",
  "scope":"https://www.googleapis.com/auth/devstorage.readonly",
  "aud":"https://www.googleapis.com/oauth2/v3/token",
  "exp":1328554385,
  "iat":1328550785
}
```

**额外的要求:**

在一些企业的案例中，在组织内应用程序能代表一个特定的用户请求权限。
在允许执行这种模拟类型的操作前，通常是由域管理员授权应用程序模拟一个用户。查看更多有关于域管理员的信息，请前往 [Managing API client access](http://support.google.com/a/bin/answer.py?hl=en&answer=162106)。

为了获取访问码，使应用可以访问到包括用户的电子邮箱在内的资源，JWT 的要求设置的`sub`字段描述如下：
```
字段名 	描述
sub 	应用程序请求授权访问用户的电子邮件地址。
```
如果应用程序没有权限去模拟用户，那么响应的访问码包含的sub字段将是一个错误。

包含`sub`字段的 JWT 要求设置如下：
```
{
  "iss":"761326798069-r5mljlln1rd4lrbhg75efgigp36m78j5@developer.gserviceaccount.com",
  "sub":"some.user@example.com",
  "scope":"https://www.googleapis.com/auth/prediction",
  "aud":"https://www.googleapis.com/oauth2/v3/token",
  "exp":1328554385,
  "iat":1328550785
}

```

##### 对 JWT 的要求设置进行编码
就像 JWT 的头部，要求设置应该被序列化成 UTF-8 字节，然后通过 Base64url 进行编码。下面的就是一个 JWT 的要求设置的 JSON 例子和 Base64url-safe 例子：
```
{
  "iss":"761326798069-r5mljlln1rd4lrbhg75efgigp36m78j5@developer.gserviceaccount.com",
  "scope":"https://www.googleapis.com/auth/prediction",
  "aud":"https://www.googleapis.com/oauth2/v3/token",
  "exp":1328554385,
  "iat":1328550785
}
```

```
eyJpc3MiOiI3NjEzMjY3OTgwNjktcjVtbGpsbG4xcmQ0bHJiaGc3NWVmZ2lncDM2bTc4ajVAZGV2ZWxvcGVyLmdzZXJ2aWNlYWNjb3VudC5jb20iLCJzY29wZSI6Imh0dHBzOi8vd3d3Lmdvb2dsZWFwaXMuY29tL2F1dGgvcHJlZGljdGlvbiIsImF1ZCI6Imh0dHBzOi8vYWNjb3VudHMuZ29vZ2xlLmNvbS9vL29hdXRoMi90b2tlbiIsImV4cCI6MTMyODU1NDM4NSwiaWF0IjoxMzI4NTUwNzg1fQ
```

##### 计算签名
[JSON Web Signature](http://self-issued.info/docs/draft-ietf-jose-json-web-signature.html)是用来为 JWT 生成签名的。输入的签名就如下列的字符数组：

```
{Base64url encoded header}.{Base64url encoded claim set}

```

在计算签名的时候，必须使用 JWT 头部中指定的签名算法。Google OAuth 2.0 授权服务只支持使用 SHA-256 哈希算法的 RSA。这个算法在 JWT 头部的 alg 字段表示成 RS256。

使用 SHA256withRSA（就是使用 SHA-256 哈希函数的 RSASSA-PKCS1-V1_5-SIGN）和从开发者面板获取的私人密钥计算输入的 UTF-8 序列。将输出字符数组。

签名必须被 Base64url 编码。头部，要求设置，签名，通过使用实心圆点 （.） 进行串接，其结果就是 JWT。他应该像如下展示那样：

```
{Base64url encoded header}.
{Base64url encoded claim set}.
{Base64url encoded signature}
```

下面是 JWT 被 Base64url 编码前的例子：
```
{"alg":"RS256","typ":"JWT"}.
{
"iss":"761326798069-r5mljlln1rd4lrbhg75efgigp36m78j5@developer.gserviceaccount.com",
"scope":"https://www.googleapis.com/auth/prediction",
"aud":"https://www.googleapis.com/oauth2/v3/token",
"exp":1328554385,
"iat":1328550785
}.
[signature bytes]

```

下面是 JWT 被 Base64url 编码后，并准备传输的例子：

```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.
eyJpc3MiOiI3NjEzMjY3OTgwNjktcjVtbGpsbG4xcmQ0bHJiaGc3NWVmZ2lncDM2bTc4ajVAZGV2ZWxvcGVyLmdzZXJ2aWNlYWNjb3VudC5jb20iLCJzY29wZSI6Imh0dHBzOi8vd3d3Lmdvb2dsZWFwaXMuY29tL2F1dGgvcHJlZGljdGlvbiIsImF1ZCI6Imh0dHBzOi8vYWNjb3VudHMuZ29vZ2xlLmNvbS9vL29hdXRoMi90b2tlbiIsImV4cCI6MTMyODU1NDM4NSwiaWF0IjoxMzI4NTUwNzg1fQ.
ixOUGehweEVX_UKXv5BbbwVEdcz6AYS-6uQV6fGorGKrHf3LIJnyREw9evE-gs2bmMaQI5_UbabvI4k-mQE4kBqtmSpTzxYBL1TCd7Kv5nTZoUC1CmwmWCFqT9RE6D7XSgPUh_jF1qskLa2w0rxMSjwruNKbysgRNctZPln7cqQ
```

##### 创建访问令牌请求
在生成一个 JWT 之后，应用可以使用这个 JWT 来请求访问码。访问码请求是一个 HTTPS 的POST请求， POST 的数据包会被 URL 编码。 URL 是：

```
https://www.googleapis.com/oauth2/v3/token
```
在 HTTPSPOST请求中的参数如下：
```
字段名 	    描述
grant_type 	使用如下的字符串，必须进行 URL 编码：urn:ietf:params:oauth:grant-type:jwt-bearer。
assertion 	JWT，包括签名。
```
下面是用来请求访问码的HTTPS`POST`的原生数据：
```
POST /oauth2/v3/token HTTP/1.1
Host: www.googleapis.com
Content-Type: application/x-www-form-urlencoded

grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiI3NjEzMjY3OTgwNjktcjVtbGpsbG4xcmQ0bHJiaGc3NWVmZ2lncDM2bTc4ajVAZGV2ZWxvcGVyLmdzZXJ2aWNlYWNjb3VudC5jb20iLCJzY29wZSI6Imh0dHBzOi8vd3d3Lmdvb2dsZWFwaXMuY29tL2F1dGgvcHJlZGljdGlvbiIsImF1ZCI6Imh0dHBzOi8vYWNjb3VudHMuZ29vZ2xlLmNvbS9vL29hdXRoMi90b2tlbiIsImV4cCI6MTMyODU3MzM4MSwiaWF0IjoxMzI4NTY5NzgxfQ.ixOUGehweEVX_UKXv5BbbwVEdcz6AYS-6uQV6fGorGKrHf3LIJnyREw9evE-gs2bmMaQI5_UbabvI4k-mQE4kBqtmSpTzxYBL1TCd7Kv5nTZoUC1CmwmWCFqT9RE6D7XSgPUh_jF1qskLa2w0rxMSjwruNKbysgRNctZPln7cqQ
```

下面使用使用 `curl` 做相同的请求：
```
curl -d 'grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiI3NjEzMjY3OTgwNjktcjVtbGpsbG4xcmQ0bHJiaGc3NWVmZ2lncDM2bTc4ajVAZGV2ZWxvcGVyLmdzZXJ2aWNlYWNjb3VudC5jb20iLCJzY29wZSI6Imh0dHBzOi8vd3d3Lmdvb2dsZWFwaXMuY29tL2F1dGgvcHJlZGljdGlvbiIsImF1ZCI6Imh0dHBzOi8vYWNjb3VudHMuZ29vZ2xlLmNvbS9vL29hdXRoMi90b2tlbiIsImV4cCI6MTMyODU3MzM4MSwiaWF0IjoxMzI4NTY5NzgxfQ.RZVpzWygMLuL-n3GwjW1_yhQhrqDacyvaXkuf8HcJl8EtXYjGjMaW5oiM5cgAaIorrqgYlp4DPF_GuncFqg9uDZrx7pMmCZ_yHfxhSCXru3gbXrZvAIicNQZMFxrEEn4REVuq7DjkTMyCMGCY1dpMa8aWfTQFt3Eh7smLchaZsU
' https://www.googleapis.com/oauth2/v3/token
```

##### 处理响应
如果 JWT 和访问码请求被适当的编码，以及服务账号拥有执行的权限，从授权服务返回 JSON 响应包含一个访问码，一个响应例子如下：
```
{
  "access_token" : "1/8xbJqaOZXSUZbHLl5EOtu1pxz3fmmetKx9W8CV4t79M",
  "token_type" : "Bearer",
  "expires_in" : 3600
}
```
访问码的有效期为一个小时，在有限期内可以被有效使用的。

### 调用谷歌应用接口
#### JAVA
使用 `GoogleCredential` 对象完成调用谷歌应用接口的步骤如下：


1. 通过 `GoogleCredential` 对象创建一个服务对象调用你想调用的接口。例如：
```
      SQLAdmin sqladmin =
          new SQLAdmin.Builder(httpTransport, JSON_FACTORY, credential).build();
```
2. 使用 [interface provided by the service object](https://code.google.com/p/google-api-java-client/) 为应用接口服务创建一个请求。例如，列出 `exciting-example-123` 工程的实例化的云数据库：
```
      SQLAdmin.Instances.List instances =
          sqladmin.instances().list("exciting-example-123").execute();
```

#### HTTP/REST
在你的应用获取到访问令牌之后，你能使用这个令牌代替用户或者服务账号来调用谷歌应用接口。为了完成以上功能，调用应用接口的请求通过 `access_token`字段查询参数或者`Authorization: Bearer` HTTP 头部包含访问码。
如果允许，使用 HTTP 头部包含访问令牌会更好，因为查询字符串容易被泄露。你可以通过封装的库来建立更多的谷歌应用调用的事例。（例如调用 [People API](https://developers.google.com/+/api/latest/people/get#examples)）。

你可以尝试所有的谷歌应用接口，在 [OAuth 2.0 Playground](https://developers.google.com/oauthplayground/) 查看它们的应用范围。

**例子:**
如下，通过使用`access_token`查询字符串参数调用 [people.get](https://developers.google.com/+/api/latest/people/get) 的终端，你需要指定属于你自己的访问码：
```
GET https://www.googleapis.com/plus/v1/people/userId?access_token=1/fFBGRNJru1FQd44AzqT3Zg
```
下面是授权用户使用`Authorization: Bearer`头部完成相同的调用：
```
GET /plus/v1/people/me HTTP/1.1
Authorization: Bearer 1/fFBGRNJru1FQd44AzqT3Zg
Host: googleapis.com
```

你可以尝试在控制台应用中使用 curl 。下面是使用 HTTP 头部完成相同的调用功能：
```
curl -H "Authorization: Bearer 1/fFBGRNJru1FQd44AzqT3Zg" https://www.googleapis.com/plus/v1/people/me
```

或者，作为一种完成相同的调用功能选择，使用查询字符串参数：
```
curl https://www.googleapis.com/plus/v1/people/me?access_token=1/fFBGRNJru1FQd44AzqT3Zg
```

#### 访问码过期

通过 Google OAuth 2.0 Authorization Server 发布的访问码的有效期是一个小时。当访问码失效后，应用应该生成另一个 JWT 来请求另一个新的访问码。
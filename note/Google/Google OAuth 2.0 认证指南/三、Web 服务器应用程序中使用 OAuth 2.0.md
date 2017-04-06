谷歌 OAuth 2.0 端点支持使用下列语言和框架编写的 Web 服务器应用程序：PHP，Java，Python，Ruby，和 ASP.NET。 这些应用程序可能会在用户使用的时候或用户没在使用的时候访问谷歌 API。这种工作流需要应用程序有能力保存一个 secret。

这个文档描述了如何从 Web 服务器应用程序使用 OAuth 2.0 访问谷歌 API。 

## 概览
授权程序会在你的应用程序中将浏览器重定向到一个谷歌 URL；一个包含查询参数的 URL 显示出正在请求的访问类型。和其他情形一样，谷歌会处理用户认证，会话选择，用户准许（User consent）。这一程序的结果是授权码，谷歌会将授权码以查询串的形式返回给您的应用程序。

接收到授权码之后，你的应用程序可以通过密码交换（同时也会进行客户端 ID 和客户端 secret 交换）来获得访问令牌,在某种情形下还会得到一个刷新令牌。

应用程序可以使用访问令牌来访问谷歌 API。

如果一个刷新令牌在授权码交换时存在，那么它可以在任何时候用来获取新的访问令牌。这叫做离线访问，因为应用程序用这种方法取得新的访问令牌时不需要用户值守浏览器。

![](http://udn.yyuap.com/doc/wiki/project/google-oauth-2/images/webflow.png)

### 生成 URL
用于认证用户的 URL 是 `https://accounts.google.com/o/oauth2/auth`。这个端点只能通过 SSL 访问，HTTP 连接会被拒绝。

---

**对于 web 服务器应用程序，谷歌认证服务器支持的查询串参数集为：**

---

**参数**： response_type （响应类型）

**值**： code 

**描述**： 决定 Google OAuth 2.0 端点是否要返回授权码。 对于 Web 服务器应用程序，参数应该使用 code。

---

**参数**： client_id （客户端 ID）

**值**： 你从[开发者控制台](https://console.developers.google.com/)处获得的客户端 ID。

**描述**： 确定是哪个客户端正在发出请求。传过去的这个参数值必须要与开发者控制台里显示的完全一致。 

---

**参数**： redirect_uri （重定向 URI）

**值**： 在开发者控制台里列出的这个工程的 redirect_uri values 其中一个值。

**描述**： 决定回应(Response)会发向哪里。这个参数的值必须和谷歌开发者控制台中这个工程所显示`redirect_uri values`的其中一个完全一致（包括完整的 HTTP 或 HTTPS 格式、大小写、和末尾的'/'符号）。

---

**参数**： scope （域）

**值**： 用空格分隔该应用程序所请求的权限集。

**描述**： 确认您的应用程序请求的谷歌 API 访问权。传过去的参数值会以用户准许页面的方式向用户显示。
请求的权限数量和获得用户准许的可能性有逆相关关系。若想了解可用的[登陆域](https://developers.google.com/+/api/oauth#login-scopes)，请参见登陆域。
若想了解所有谷歌 API 的可用域，请访问 [API 浏览器](https://developers.google.com/apis-explorer/#p/)。基本上渐进地请求域是一项最佳实践，比起提前一次性请求所有权限，按需请求更好。
举例来说，一个想要支持购买功能的应用程序应该在用户点击“购买”按钮的时候才要求谷歌钱包访问权; 详情参阅[渐进式授权](https://developers.google.com/apis-explorer/#p/)。

---

**参数**： state （状态）

**值**： 任意字符串

**描述**： 当收到回应时提供任何可能对您的应用程序有用的状态。谷歌认证服务器会回传这个参数，所以您的应用程序会收到和它发出去一样的内容。若想防止跨站请求伪造攻击(CSRF), 我们强烈推荐您在状态中包含防伪造令牌，并且在回应中进行确认。详情请参见[OpenID](https://developers.google.com/identity/protocols/OpenIDConnect#createxsrftoken)连接 来获得实现这个功能的示例。

---

**参数**： access_type （访问类型）

**值**： online 或 offline（在线或离线）

**描述**： 表示当用户不在浏览器前时，您的应用程序是否需要访问谷歌 API。这个参数默认值是 online。如果您的应用程序需要在用户不在浏览器前的时候刷新访问令牌，那么请使用 offline，这样做会让您的应用程序在第一次为用户获取授权码的时候获得一个刷新令牌。

---

**参数**： login_hint （登陆提示）

**值**： 电子邮箱地址或子标识符

**描述**： 当您的应用程序知道哪个用户正在尝试进行认证时，可以提供这个参数作为对认证服务器的提示。将这个参数传过去会在登陆页面自动填写用户邮箱地址，或者选择适合的多用户登录会话，从而简化登陆工作流。

---

**参数**： include_granted_scopes （包含已许可的域）

**值**： true 或 false

**描述**： 如果这个参数值被设为 true，同时认证请求被许可，那么该次认证会许可所有曾经许可给这组用户和应用程序的其他域；请参见渐进式授权。

---

**下面是一串示例 URL。（包含换行和空格来加强可读性）**

```
 https://accounts.google.com/o/oauth2/auth?
 scope=email%20profile&
 state=security_token%3D138r5719ru3e1%26url%3Dhttps://oa2cb.example.com/myHome&
 redirect_uri=https%3A%2F%2Foauth2-login-demo.appspot.com%2Fcode&,
 response_type=code&
 client_id=812741506391.apps.googleusercontent.com&
 approval_prompt=force
```

### 处理回应
回应会被发送到请求 URL 中 `redirect_uri` 所指定的目的地。如果用户准许访问请求，那么回应会包含授权码和状态参数（如果请求中有包含状态参数的话）。如果用户没有准许该次请求，那么回应会包含一个错误信息。所有回应都会发送到查询串中的 web 服务器，如下例所示：

一个包含错误的回应:
```
https://oauth2-login-demo.appspot.com/code?error=access_denied&state=security_token%3D138r5719ru3e1%26url%3Dhttps://oa2cb.example.com/myHome
```
一个授权码回应:
```
https://oauth2-login-demo.appspot.com/code?state=security_token%3D138r5719ru3e1%26url%3Dhttps://oa2cb.example.com/myHome&code=4/P7q7W91a-oMsCeLvIaQm6bTrgtp7
```
>重要信息: 如果您接收回应的端点会渲染 HTML 页面，那么页面的所有资源都可以看见 URL 中的授权码。其中脚本能直接读取 URL，页面上任何资源都可能收到带有授权码的 URL 的 Referer HTTP头。请认真考虑您是否真的想发送认证凭证给那个页面上的所有资源（特别是第三方脚本，例如社交网络插件和统计分析插件）。若想避免这个问题，我们推荐让服务器先单独处理请求，然后再重定向到另外一个不包含回应参数的 URL。

服务器收到授权码后，它就能用授权码来交换一个访问令牌和一个刷新令牌。这种请求实际上是将一个 HTTPS POST 发送到 URL：`https://www.googleapis.com/oauth2/v3/token` ，其中包含以下参数:

---

**字段**： code 

**描述**： 从初次请求获得的授权码

---

**字段**： client_id （客户端 ID）

**描述**： 从开发者控制台处获得的客户端 ID。

---

**字段**： client_secret （客户端 secret）

**描述**： 从开发者控制台处获得的客户端 secret。

---

**字段**： redirect_uri （重定向 URI）

**描述**： 这个工程在开发者控制台处列出的重定向 URI 项之一。

---

**字段**： grant_type （许可类型）

**描述**： 正如 OAuth 2.0 规格中定义的，该字段值为authorization_code。

---

实际请求可能会像下面这样：
```
POST /oauth2/v3/token HTTP/1.1
Host: www.googleapis.com
Content-Type: application/x-www-form-urlencoded

code=4/P7q7W91a-oMsCeLvIaQm6bTrgtp7&
client_id=8819981768.apps.googleusercontent.com&
client_secret={client_secret}&
redirect_uri=https://oauth2-login-demo.appspot.com/code&
grant_type=authorization_code
```

一个针对这个请求的成功的回应应该包含以下字段：

---

**字段**： access_token （访问令牌）

**描述**： 可以发送给谷歌 API 的令牌。

---

**字段**： refresh_token （刷新令牌）

**描述**： 用于获得新的访问令牌的令牌。刷新令牌会一直有效直到用户对其废除访问权。该字段只有在授权码请求中的 access_type = offline 的时候才可能出现。

---

**字段**： expires_in （有效期）

**描述**： 访问令牌剩余的生命期。

---

**字段**： token_type （令牌类型）

**描述**： 确认返回的令牌类型。在这次示例中，这个字段总是会拥有值 Bearer。

---

一个成功的回应会以 JSON 数组的形式发送回来，和下面的例子类似：
```
{
  "access_token":"1/fFAGRNJru1FTz70BzhT3Zg",
  "expires_in":3920,
  "token_type":"Bearer"
}
```

>注意: 有时回应可能会包含其他字段，您的程序不应该将这种情况视为错误。上面示例中的集合是最小集。

### 调用谷歌 API
您的程序获得访问令牌之后，您可以使用令牌以用户或者服务账户的名义来对谷歌 API 进行调用。
要做到这一点，请将访问令牌包含到发给 API 的请求中，可以通过包含 `access_token` 查询参数或者一个 `Authorization: Bearer` HTTP 头来实现。如果可能的话，我们更欢迎 HTTP 头的方法，因为查询串更容易在服务器记录中可见。在大多数情况下你可以使用客户端库来设置您的谷歌 API 调用（例如，当对[谷歌人脉 API](https://developers.google.com/+/api/latest/people/get#examples) 进行调用时）。

你可以在[OAuth 2.0 游乐场](https://developers.google.com/oauthplayground/)中自行尝试所有的谷歌 API 并查看他们所对应的域。

#### 示例
一个通过使用 `access_token` 查询串参数对 `people.get` 端点 （谷歌人脉 API）的调用会和下例相类似，当然在实际情况中您需要提供您自己的访问令牌：
```
GET https://www.googleapis.com/plus/v1/people/userId?access_token=1/fFBGRNJru1FQd44AzqT3Zg
```
而对于已经认证的用户（me）而言，通过 `Authorization: Bearer` HTTP 头调用同样的 API 就会像下面这样：

```
GET /plus/v1/people/me HTTP/1.1
Authorization: Bearer 1/fFBGRNJru1FQd44AzqT3Zg
Host: googleapis.com
```

您可以尝试 curl 命令行应用程序。下面是使用 HTTP 头的方法（推荐）的示例：
```
curl -H "Authorization: Bearer 1/fFBGRNJru1FQd44AzqT3Zg" https://www.googleapis.com/plus/v1/people/me
```
或者，您也可以使用查询串参数的方法实现：
```
curl https://www.googleapis.com/plus/v1/people/me?access_token=1/fFBGRNJru1FQd44AzqT3Zg
```

### 渐进式授权
在 OAuth 协议中，您的应用程序为了访问资源所请求的认证是以域来区分的，如果用户已经被认证而且同意权限请求，您的应用程序会接收到一个生命期比较短的访问令牌，这些令牌能让它访问目标资源，而刷新令牌（可选）可以让应用程序拥有长期的访问权限。

按需请求资源访问权限通常被认为是一项最佳实践。举例来说，一个让人们可以对音乐进行采样并且创建混音的应用程序也许在登陆时候只需要非常少量的资源，说不定除了登陆者的名字之外就没别的了。但是，要保存一个完整的混音可能会需要访问他们的谷歌网盘。大多数人都会觉得在应用程序需要存储文件时申请谷歌网盘的访问权限是非常自然的。

这种情况下，在登录时应用程序可以请求这个域：`https://www.googleapis.com/auth/plus.loginto` 来实现一个基本的社交登陆功能，然后在需要保存混音的时候才申请这个域：`https://www.googleapis.com/auth/drive.file`。

同时使用在使用 [OpenID 连接](https://developers.google.com/identity/protocols/OpenIDConnect)和使用 [OAuth 2.0](https://developers.google.com/accounts/docs/OAuth2) 来访问谷歌 API 里描述的步骤通常会让你的应用程序不得不管理两个不同的访问令牌。
如果您希望避开这种复杂性，则需要在所有 OAuth 2.0 工作流的第一步时，将发送给 `https://accounts.google.com/o/oauth2/auth` 的认证 URI 里添加一个额外的参数。
这个参数是 `include_granted_scopes`，其中值可以被赋为 `true` 或 `false`（默认值是 `false`），当这个值为 `true` 时，如果您的域认证要求被批准，谷歌认证服务器会将这次认证和所有之前已经成功的认证为这组用户-应用程序合并。这类请求的 URI 可能看上去会像下面这样（下例有插入换行和空格来增强可读性）：

```
https://accounts.google.com/o/oauth2/auth?
  scope=https://www.googleapis.com/auth/drive.file&
  state=security_token%3D138r5719ru3e1%26url%3Dhttps://oa2cb.example.com/myHome&
  redirect_uri=https%3A%2F%2Fmyapp.example.com%2Fcallback&
  response_type=code&
  client_id=8127352506391.apps.googleusercontent.com&
  approval_prompt=force&
  include_granted_scopes=true
```

我们现在把上面这种认证叫做“组合认证”；关于组合认证：

- 您可以使用刚才得到的访问令牌来访问任何在组合认证中已经合并的域。
- 当您使用刷新令牌来进行组合认证时，返回的新访问令牌也代表了组合认证，所以也可以用于访问其任意域。
- 组合认证包括了所有过去已经被许可的权限，即使这些权限请求曾经是从不同的客户端发出的。举例来说，如果您在桌面应用程序请求了 https://www.googleapis.com/auth/plus.loginscope ，然后又向同一个用户的移动端发送了同样的请求，那么后面这个请求会被自动批准，因为组合认证会包含两边的域。
- 当您废除了一个代表组合认证的的令牌，其所有的认证都会被同时废除；这意味着如果你还保留着任何以往的权限的令牌，他们也会跟着失效。

### 离线访问
在某些情况下，你的应用程序可能需要在用户不在的时候访问谷歌 API。例如备份服务或者一些可以让博客的帖子可以在周一早上准时8点钟发布的应用程序。 这种类型的访问被称作离线的，而 Web 服务器应用程序可能会向用户要求离线访问权。反之，正常和默认的情况下访问被称作在线的。

如果您的应用程序需要离线访问谷歌 API，那么在授权码的请求中就应该包含 `access_type` 参数，并将其值设定为 `offline`。离线访问请求的例子在下方，含有换行和空格来增强可读性：

```
https://accounts.google.com/o/oauth2/auth?
 scope=email%20profile&
 state=security_token%3D138r5719ru3e1%26url%3Dhttps://oa2cb.example.com/myHome&
 redirect_uri=https%3A%2F%2Foauth2-login-demo.appspot.com%2Fcode&
 response_type=code&
 client_id=812741506391.apps.googleusercontent.com&
 access_type=offline
```
当目标用户的浏览器第一次被指向这串 URL 时，他们会看见用户准许页面。如果他们批准访问，那么回应中就会包含一个授权码，授权码可以用来交换获得一个访问令牌和一个刷新令牌。

下面是授权码交换的示例：

```
POST /oauth2/v3/token HTTP/1.1
Host: www.googleapis.com
Content-Type: application/x-www-form-urlencoded

code=4/P7q7W91a-oMsCeLvIaQm6bTrgtp7&
client_id=8819981768.apps.googleusercontent.com&
client_secret={client_secret}&
redirect_uri=https://oauth2-login-demo.appspot.com/code&
grant_type=authorization_code
```
如果这是应用程序第一次为用户进行授权码交换，那么回应中就会包含一个访问令牌和一个刷新令牌，如下例所示：
```
{
  "access_token":"1/fFAGRNJru1FTz70BzhT3Zg",
  "expires_in":3920,
  "token_type":"Bearer",
  "refresh_token":"1/xEoDL4iW3cxlI7yDbSRFYNG01kVKM2C-259HOF2aQbI"
}
```

>重要：当您的应用程序获得一个刷新令牌时，将刷新令牌保存起来供未来使用时很重要的。
因为一旦您的应用程序丢失了刷新令牌，它就只能重新向用户进行用户准许才能获得另一个刷新令牌了。
如果你需要重新向用户进行用户准许，请在授权码请求里面包含 `approval_prompt` 参数，并将其值设定为 `force`。

您的应用程序接收刷新令牌之后，就可以在任意时间获得新的访问令牌了。请参见[刷新令牌的相关章节](https://developers.google.com/identity/protocols/OAuth2WebServer#refresh) 来获得更多信息。

下一次您的应用程序为同一个用户请求授权码时，用户不会再一次被要求同意准许了（假设他们以前已经同意了这次请求所包含的的域）。
和预料的一样，回应会包含一个用于交换的授权码。不过，**和第一次为用户交换授权码不一样，返回的内容中不会包括刷新令牌。**下面是这类请求的一个示例；
```
{
  "access_token":"1/fFAGRNJru1FQd77BzhT3Zg",
  "expires_in":3920,
  "token_type":"Bearer",
}
```

### 使用刷新令牌
和之前章节提到的一样，一个刷新令牌是在使用参数 `access_type` 为 `offline` 的**第一次授权码交换**中获得的。在这类情况下，您的应用程序可以通过发送刷新令牌到谷歌 OAuth 2.0 认证服务器来获得一个新的访问令牌。

若要通过这种方式获取访问令牌，您的应用程序应该发送一个 HTTPS POST 请求到 https://www.googleapis.com/oauth2/v3/token。 并且请求必须包含以下参数：

---

**字段**： refresh_token （刷新令牌）

**描述**： 从授权码交换返回的刷新令牌

---

**字段**： client_id （客户端 ID）

**描述**： 从开发者控制台处获得的客户端 ID。

---

**字段**： client_secret （客户端 secret）

**描述**： 从开发者控制台处获得的客户端 secret。

---

**字段**： grant_type （许可类型）

**描述**： 正如 OAuth 2.0 规格中定义的，该字段值为refresh_token。

---

这种请求如下例所示：
```
POST /oauth2/v3/token HTTP/1.1
Host: www.googleapis.com
Content-Type: application/x-www-form-urlencoded

client_id=8819981768.apps.googleusercontent.com&
client_secret={client_secret}&
refresh_token=1/6BMfW9j53gdGImsiyUH5kU5RsR4zwI9lUVX-tqf8JXQ&
grant_type=refresh_token
```

只要用户没有废除您的应用程序的访问权限，回应中就会包括一个新的访问令牌。对于上述请求，回应应该和下例相似：
```
{
  "access_token":"1/fFBGRNJru1FQd44AzqT3Zg",
  "expires_in":3920,
  "token_type":"Bearer",
}
```
请注意刷新令牌的发放数量是有限制的；限制的单位是每一组客户端-用户，你应该在长期存储器中保存刷新令牌并一直使用它直到过期。如果您的应用程序请求太多刷新令牌，就可能会触发限制，这种情况下最旧的令牌会失效。

### 废除令牌
在某些情况下，一个用户也许会想要废除已经授予给应用程序的权限。用户可以用过访问以下 URL 来显式地废除权限 https://accounts.google.com/b/0/IssuedAuthSubTokens。 同时，通过编程让应用程序自行废除已经获得的权限也是可能的。程序化废除工作在用户取消订阅或者卸载程序时是很重要的。换句话来说，卸载的过程可以包含一个 API 请求，用于确保废除被卸载的应用程序已经获得的权限。

若想程序化废除令牌，您的应用程序需要向 `https://accounts.google.com/o/oauth2/revoke` 发送一个请求，并且将令牌作为参数发送出去：

```
curl https://accounts.google.com/o/oauth2/revoke?token={令牌}
```
其中的“令牌”可以是访问令牌，也可以是刷新令牌。如果令牌是一个访问令牌，并且有成对的刷新令牌，那么所对应的刷新令牌也会被废除。

如果废除工作被成功执行，那么回应的状态码会是 `200`。如果发生错误，回应的状态码会是 `400`，并且回应会包含一个错误码。

注意： 在接收到废除工作成功的回应之后，可能需要隔一段时间废除才会完全生效。
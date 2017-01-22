微信分为公众平台和开放平台，其中涉及到多种ACCESS_TOKEN.
这里拿`获取用户基本信息`和`使用授权码换取公众号的接口调用凭据和授权信息`为例讲述几个不同场景：

>**关于四种ACCESS_TOKEN**
>1. 微信网页授权是通过OAuth2.0机制实现的，在用户授权给公众号后，公众号可以获取到一个网页授权特有的接口调用凭证（网页授权access_token），通过网页授权access_token可以进行授权后接口调用，如获取用户基本信息；
>2. 其他微信接口，需要通过基础支持中的“获取access_token”接口来获取到的普通access_token调用。
>3. component_access_token:微信开放平台接口调用凭据，微信三方平台使用该凭据调用自己的接口。
>4. authorizer_access_token:公众号的接口调用凭据，微信三方平台使用该凭据调用其服务的公众号的接口为公众号服务。

### 微信公众平台 - 获取用户基本信息 - access_token（普通access_token）
>基础支持中的access_token，该access_token用于调用微信公众平台接口。

在关注者与公众号产生消息交互后(即用户关注了公众号，如果用户未关注该公众号，拉取不到用户的信息)，公众号可获得关注者的OpenID（加密后的微信号，每个用户对每个公众号的OpenID是唯一的。对于不同公众号，同一用户的openid不同）。
公众号可通过本接口来根据OpenID获取用户基本信息，包括昵称、头像、性别、所在城市、语言和关注时间。 

**接口调用请求说明**
```
http请求方式: GET
https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN
```
**参数说明**
```
参数 	        是否必须 	说明
access_token 	是 	        调用接口凭证
openid 	        是 	        普通用户的标识，对当前公众号唯一
lang 	        否 	        返回国家地区语言版本，zh_CN 简体，zh_TW 繁体，en 英语 
```

**返回说明**
```
{
    "subscribe": 1, 
    "openid": "o6_bmjrPTlm6_2sgVt7hMZOPfL2M", 
    "nickname": "Band", 
    "sex": 1, 
    "language": "zh_CN", 
    "city": "广州", 
    "province": "广东", 
    "country": "中国", 
    "headimgurl":    "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0", 
   "subscribe_time": 1382694957,
   "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
   "remark": "",
   "groupid": 0
}
```
**参数说明**
```
参数 	        说明
subscribe       用户是否订阅该公众号标识，值为0时，代表此用户没有关注该公众号，拉取不到其余信息。
openid 	        用户的标识，对当前公众号唯一
nickname        用户的昵称
sex 	        用户的性别，值为1时是男性，值为2时是女性，值为0时是未知
city 	        用户所在城市
country         用户所在国家
province        用户所在省份
language        用户的语言，简体中文为zh_CN
headimgurl      用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像），用户没有头像时该项为空。若用户更换头像，原有头像URL将失效。
subscribe_time 	用户关注时间，为时间戳。如果用户曾多次关注，则取最后关注时间
unionid         只有在用户将公众号绑定到微信开放平台帐号后，才会出现该字段。详见：获取用户个人信息（UnionID机制）
remark 	        公众号运营者对粉丝的备注，公众号运营者可在微信公众平台用户管理界面对粉丝添加备注
groupid         用户所在的分组ID 
```

### 微信公众平台 - 获取用户基本信息 - access_token(网页授权access_token)
>特殊的网页授权access_token,与基础支持中的access_token（该access_token用于调用微信公众平台其他接口）不同

>关于网页授权的两种scope的区别说明
>1. 以snsapi_base为scope发起的网页授权，是用来获取进入页面的用户的openid的，并且是静默授权并自动跳转到回调页的。用户感知的就是直接进入了回调页（往往是业务页面）
>2. 以snsapi_userinfo为scope发起的网页授权，是用来获取用户的基本信息的。但这种授权需要用户手动同意，并且由于用户同意过，所以无须关注，就可在授权后获取该用户的基本信息。
>3. 用户管理类接口中的“获取用户基本信息接口”，是在用户和公众号产生消息交互或关注后事件推送后，才能根据用户OpenID来获取用户基本信息。这个接口，包括其他微信接口，都是需要该用户（即openid）关注了公众号后，才能调用成功的。

#### 第一步：用户同意授权，获取code
在确保微信公众账号拥有授权作用域（scope参数）的权限的前提下（服务号获得高级接口后，默认拥有scope参数中的snsapi_base和snsapi_userinfo），引导关注者打开如下页面： 
```
https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect
若提示“该链接无法访问”，请检查参数是否填写错误，是否拥有scope参数对应的授权作用域权限。
```
尤其注意：由于授权操作安全等级较高，所以在发起授权请求时，微信会对授权链接做正则强匹配校验，如果链接的参数顺序不对，授权页面将无法正常访问

```
参考链接(请在微信客户端中打开此链接体验)
Scope为snsapi_base
https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx520c15f417810387&redirect_uri=https%3A%2F%2Fchong.qq.com%2Fphp%2Findex.php%3Fd%3D%26c%3DwxAdapter%26m%3DmobileDeal%26showwxpaytitle%3D1%26vb2ctag%3D4_2030_5_1194_60&response_type=code&scope=snsapi_base&state=123#wechat_redirect
Scope为snsapi_userinfo
https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxf0e81c3bee622d60&redirect_uri=http%3A%2F%2Fnba.bluewebgame.com%2Foauth_response.php&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect
```
**参数说明:**
```
参数 	            是否必须 	说明
appid 	            是 	        公众号的唯一标识
redirect_uri 	    是 	        授权后重定向的回调链接地址，请使用urlencode对链接进行处理
response_type 	    是 	        返回类型，请填写code
scope 	            是 	        应用授权作用域，snsapi_base （不弹出授权页面，直接跳转，只能获取用户openid），snsapi_userinfo （弹出授权页面，可通过openid拿到昵称、性别、所在地。并且，即使在未关注的情况下，只要用户授权，也能获取其信息）
state 	            否 	        重定向后会带上state参数，开发者可以填写a-zA-Z0-9的参数值，最多128字节
#wechat_redirect    是 	        无论直接打开还是做页面302重定向时候，必须带此参数 
```
下图为scope等于snsapi_userinfo时的授权页面： 

![](https://mp.weixin.qq.com/wiki/static/assets/421812a0f2587f921c51413a84ac527b.png)

**用户同意授权后**
如果用户同意授权，页面将跳转至 redirect_uri/?code=CODE&state=STATE。若用户禁止授权，则重定向后不会带上code参数，仅会带上state参数redirect_uri?state=STATE

```
code说明 ：
code作为换取access_token的票据，每次用户授权带上的code将不一样，code只能使用一次，5分钟未被使用自动过期。
```

#### 第二步：通过code换取网页授权access_token
首先请注意，这里通过code换取的是一个特殊的网页授权access_token,与基础支持中的access_token（该access_token用于调用其他接口）不同。
公众号可通过下述接口来获取网页授权access_token。如果网页授权的作用域为snsapi_base，则本步骤中获取到网页授权access_token的同时，也获取到了openid，snsapi_base式的网页授权流程即到此为止。

尤其注意：由于公众号的secret和获取到的access_token安全级别都非常高，必须只保存在服务器，不允许传给客户端。后续刷新access_token、通过access_token获取用户信息等步骤，也必须从服务器发起。

**请求方法**
```
获取code后，请求以下链接获取access_token： 
https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
```
**参数说明**
```
参数 	        是否必须 	说明
appid 	        是 	公众号的唯一标识
secret 	        是 	公众号的appsecret
code 	        是 	填写第一步获取的code参数
grant_type      是 	填写为authorization_code 
```
**返回说明**
正确时返回的JSON数据包如下：
```
{
   "access_token":"ACCESS_TOKEN",
   "expires_in":7200,
   "refresh_token":"REFRESH_TOKEN",
   "openid":"OPENID",
   "scope":"SCOPE",
   "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
}
```
```
参数 	        描述
access_token 	网页授权接口调用凭证,注意：此access_token与基础支持的access_token不同
expires_in      access_token接口调用凭证超时时间，单位（秒）
refresh_token 	用户刷新access_token
openid 	        用户唯一标识，请注意，在未关注公众号时，用户访问公众号的网页，也会产生一个用户和公众号唯一的OpenID
scope 	        用户授权的作用域，使用逗号（,）分隔
unionid         只有在用户将公众号绑定到微信开放平台帐号后，才会出现该字段。详见：获取用户个人信息（UnionID机制）
```

#### 第三步：刷新access_token（如果需要）

#### 第四步：拉取用户信息(需scope为 snsapi_userinfo)
如果网页授权作用域为snsapi_userinfo，则此时开发者可以通过access_token和openid拉取用户信息了。 

**请求方法**
```
http：GET（请使用https协议）
https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN
```
**参数说明**
```
参数 	        描述
access_token 	网页授权接口调用凭证,注意：此access_token与基础支持的access_token不同
openid 	        用户的唯一标识
lang 	        返回国家地区语言版本，zh_CN 简体，zh_TW 繁体，en 英语 
```

**返回说明**
```
{
   "openid":" OPENID",
   " nickname": NICKNAME,
   "sex":"1",
   "province":"PROVINCE"
   "city":"CITY",
   "country":"COUNTRY",
    "headimgurl":    "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/46", 
	"privilege":[
	"PRIVILEGE1"
	"PRIVILEGE2"
    ],
    "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
}
```
#### 附：检验授权凭证（access_token）是否有效
**请求方法**
```
http：GET（请使用https协议）
https://api.weixin.qq.com/sns/auth?access_token=ACCESS_TOKEN&openid=OPENID
```
**参数说明**
```
参数 	        描述
access_token 	网页授权接口调用凭证,注意：此access_token与基础支持的access_token不同
openid 	        用户的唯一标识 
```
**返回说明**
正确的Json返回结果：
```
{ "errcode":0,"errmsg":"ok"}
```
错误时的Json返回示例：
```
{ "errcode":40003,"errmsg":"invalid openid"}
```

### 微信开放平台 - 使用授权码换取公众号的接口调用凭据和授权信息 - authorizer_access_token&component_access_token

>component_access_token:微信开放平台接口调用凭据，微信三方平台使用该凭据调用自己的接口。
>authorizer_access_token:公众号的接口调用凭据，微信三方平台使用该凭据调用其服务的公众号的接口为公众号服务。

该API用于使用授权码换取授权公众号的授权信息，并换取authorizer_access_token和authorizer_refresh_token。 
授权码的获取，需要在用户在第三方平台授权页中完成授权流程后，在回调URI中通过URL参数提供给第三方平台方。
请注意，由于现在公众号可以自定义选择部分权限授权给第三方平台，因此第三方平台开发者需要通过该接口来获取公众号具体授权了哪些权限，而不是简单地认为自己声明的权限就是公众号授权的权限。

**接口调用请求说明**
```
http请求方式: POST（请使用https协议）
https://api.weixin.qq.com/cgi-bin/component/api_query_auth?component_access_token=xxxx
```
**POST数据示例:**
```
{
"component_appid":"appid_value" ,
"authorization_code": "auth_code_value"
}
```
**请求参数说明**
```
参数                 说明
component_appid     第三方平台appid
authorization_code  授权code,会在授权成功时返回给第三方平台，详见第三方平台授权流程说明
```
**返回结果示例**
```
{ 
"authorization_info": {
"authorizer_appid": "wxf8b4f85f3a794e77", 
"authorizer_access_token": "QXjUqNqfYVH0yBE1iI_7vuN_9gQbpjfK7hYwJ3P7xOa88a89-Aga5x1NMYJyB8G2yKt1KCl0nPC3W9GJzw0Zzq_dBxc8pxIGUNi_bFes0qM", 
"expires_in": 7200, 
"authorizer_refresh_token": "dTo-YCXPL4llX-u1W1pPpnp8Hgm4wpJtlR6iV0doKdY", 
"func_info": [
{
"funcscope_category": {
"id": 1
}
}, 
{
"funcscope_category": {
"id": 2
}
}, 
{
"funcscope_category": {
"id": 3
}
}
]
}
```
**结果参数说明**
```
参数                         说明
authorization_info          授权信息
authorizer_appid            授权方appid
authorizer_access_token     授权方接口调用凭据（在授权的公众号具备API权限时，才有此返回值），也简称为令牌
expires_in                  有效期（在授权的公众号具备API权限时，才有此返回值）
authorizer_refresh_token    接口调用凭据刷新令牌（在授权的公众号具备API权限时，才有此返回值），刷新令牌主要用于公众号第三方平台获取和刷新已授权用户的access_token，只会在授权时刻提供，请妥善保存。 一旦丢失，只能让用户重新授权，才能再次拿到新的刷新令牌
func_info                   公众号授权给开发者的权限集列表，ID为1到15时分别代表：
                                消息管理权限
                                用户管理权限
                                帐号服务权限
                                网页服务权限
                                微信小店权限
                                微信多客服权限
                                群发与通知权限
                                微信卡券权限
                                微信扫一扫权限
                                微信连WIFI权限
                                素材管理权限
                                微信摇周边权限
                                微信门店权限
                                微信支付权限
                                自定义菜单权限
                            请注意：
                            1）该字段的返回不会考虑公众号是否具备该权限集的权限（因为可能部分具备），
                            请根据公众号的帐号类型和认证情况，来判断公众号的接口权限。
```
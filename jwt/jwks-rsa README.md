# jwks-rsa

## Install

Maven

```xml
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>jwks-rsa</artifactId>
    <version>0.3.0</version>
</dependency>
```

Gradle

```text
compile 'com.auth0:jwks-rsa:0.3.0'
```

## Usage

您从授权服务器获得的JSON Web Tokens包含一个Header参数 密钥ID ("kid")，用于唯一标识用于签署令牌的密钥。

i.e.: Given the following JWT:

```text
eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlJrSTVNakk1T1VZNU9EYzFOMFE0UXpNME9VWXpOa1ZHTVRKRE9VRXpRa0ZDT1RVM05qRTJSZyJ9.eyJpc3MiOiJodHRwczovL3NhbmRyaW5vLmF1dGgwLmNvbS8iLCJzdWIiOiJhdXRoMHw1NjMyNTAxZjQ2OGYwZjE3NTZmNGNhYjAiLCJhdWQiOiJQN2JhQnRTc3JmQlhPY3A5bHlsMUZEZVh0ZmFKUzRyViIsImV4cCI6MTQ2ODk2NDkyNiwiaWF0IjoxNDY4OTI4OTI2fQ.NaNeRSDCNu522u4hcVhV65plQOiGPStgSzVW4vR0liZYQBlZ_3OKqCmHXsu28NwVHW7_KfVgOz4m3BK6eMDZk50dAKf9LQzHhiG8acZLzm5bNMU3iobSAJdRhweRht544ZJkzJ-scS1fyI4gaPS5aD3SaLRYWR0Xsb6N1HU86trnbn-XSYSspNqzIUeJjduEpPwC53V8E2r1WZXbqEHwM9_BGEeNTQ8X9NqCUvbQtnylgYR3mfJRL14JsCWNFmmamgNNHAI0uAJo84mu_03I25eVuCK0VYStLPd0XFEyMVFpk48Bg9KNWLMZ7OUGTB_uv_1u19wKYtqeTbt9m1YcPMQ
```

使用任何JWT库或工具（如[jwt.io](https://jwt.io/?value=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlJrSTVNakk1T1VZNU9EYzFOMFE0UXpNME9VWXpOa1ZHTVRKRE9VRXpRa0ZDT1RVM05qRTJSZyJ9.eyJpc3MiOiJodHRwczovL3NhbmRyaW5vLmF1dGgwLmNvbS8iLCJzdWIiOiJhdXRoMHw1NjMyNTAxZjQ2OGYwZjE3NTZmNGNhYjAiLCJhdWQiOiJQN2JhQnRTc3JmQlhPY3A5bHlsMUZEZVh0ZmFKUzRyViIsImV4cCI6MTQ2ODk2NDkyNiwiaWF0IjoxNDY4OTI4OTI2fQ.NaNeRSDCNu522u4hcVhV65plQOiGPStgSzVW4vR0liZYQBlZ_3OKqCmHXsu28NwVHW7_KfVgOz4m3BK6eMDZk50dAKf9LQzHhiG8acZLzm5bNMU3iobSAJdRhweRht544ZJkzJ-scS1fyI4gaPS5aD3SaLRYWR0Xsb6N1HU86trnbn-XSYSspNqzIUeJjduEpPwC53V8E2r1WZXbqEHwM9_BGEeNTQ8X9NqCUvbQtnylgYR3mfJRL14JsCWNFmmamgNNHAI0uAJo84mu_03I25eVuCK0VYStLPd0XFEyMVFpk48Bg9KNWLMZ7OUGTB_uv_1u19wKYtqeTbt9m1YcPMQ)）
对其进行解码，然后从Header声明中提取`kid`参数。

```json
{
  "typ": "JWT",
  "alg": "RS256",
  "kid": "RkI5MjI5OUY5ODc1N0Q4QzM0OUYzNkVGMTJDOUEzQkFCOTU3NjE2Rg"
}
```

在下面列举的任何`JwkProviders`上使用这个kid来获得您配置的JWKS端点提供的签名密钥。

## UrlJwkProvider

`UrlJwkProvider`从提供的域颁发者的`/.well-known/jwks.json`中获取`jwk`，如果`kid`匹配其中一个注册的键，则返回一个`Jwk`。

```java
JwkProvider provider = new UrlJwkProvider("https://samples.auth0.com/");
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

它也可以从任何给定的URL（甚至是文件系统中的本地文件）加载`jwks.json`文件。

```java
JwkProvider provider = new UrlJwkProvider(new URL("https://samples.auth0.com/"));
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

## GuavaCachedJwkProvider

`GuavaCachedJwkProvider`将`jwk`缓存在内存缓存中的LRU中，如果在缓存中没有找到`jwk`，它将向另一个提供者请求，并将其结果存储在缓存中。

>默认情况下，它存储5个键10小时，但这些值可以更改

```java
JwkProvider http = new UrlJwkProvider("https://samples.auth0.com/");
JwkProvider provider = new GuavaCachedJwkProvider(http);
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

## RateLimitJwkProvider

`RateLimitJwkProvider`将限制在给定时间范围内获取不同签名密钥的数量。

>默认情况下，限制为每分钟10个不同的键，但可以更改这些值

```java
JwkProvider url = new UrlJwkProvider("https://samples.auth0.com/");
Bucket bucket = new Bucket(10, 1, TimeUnit.MINUTES);
JwkProvider provider = new RateLimitJwkProvider(url, bucket);
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

## JwkProviderBuilder

为域`https://samples.auth0.com`创建具有缓存和频率限制的提供者：

```java
JwkProvider provider = new JwkProviderBuilder("https://samples.auth0.com/")
    .build();
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

并指定缓存和速率限制属性

```java
JwkProvider provider = new JwkProviderBuilder("https://samples.auth0.com/")
    .cached(10, 24, TimeUnit.HOURS)
    .rateLimited(10, 1, TimeUnit.MINUTES)
    .build();
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

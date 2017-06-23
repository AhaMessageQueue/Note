## jwks-rsa

Github:<https://github.com/auth0/jwks-rsa-java>

### Install
#### Maven
```xml
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>jwks-rsa</artifactId>
    <version>0.1.0</version>
</dependency>
```
#### Gradle
```text
compile 'com.auth0:jwks-rsa:0.1.0'
```

### Usage
#### UrlJwkProvider

UrlJwkProvider从提供的域签发者的/.well-known/jwks.json获取jwk，如果kid与其中一个注册的密钥相匹配，则返回一个Jwk。

```java
JwkProvider provider = new UrlJwkProvider("https://samples.auth0.com/");
Jwk jwk = provider.get("{kid of the signing key}"); //当没有找到或者不能获得时抛出异常
```
还可以从任何给定的Url（甚至文件系统中的本地文件）加载jwks.json文件。

```java
JwkProvider provider = new UrlJwkProvider(new URL("https://samples.auth0.com/"));
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

#### GuavaCachedJwkProvider
GuavaCachedJwkProvider将Jwk缓存在内存缓存中的LRU中，如果在缓存中没有找到Jwk，它会请求另一个Provider，并将其存储在缓存中。

默认情况下，它存储5个键10个小时，但这些值可以更改

```java
JwkProvider http = new UrlJwkProvider("https://samples.auth0.com/");
JwkProvider provider = new GuavaCachedJwkProvider(http);
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

#### RateLimitJwkProvider
RateLimitJwkProvider将限制在给定时间范围内获得的不同签名密钥的数量。

默认情况下，速率限制为每分钟10个不同的密钥，但这些值可以更改

```java
JwkProvider url = new UrlJwkProvider("https://samples.auth0.com/");
Bucket bucket = new Bucket(10, 1, TimeUnit.MINUTES);
JwkProvider provider = new RateLimitJwkProvider(url, bucket);
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

#### JwkProviderBuilder
要创建具有缓存和速率限制的域https://samples.auth0.com的Provider：
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


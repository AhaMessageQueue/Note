# Java JWT

JSON Web Tokens ([draft-ietf-oauth-json-web-token-08](http://self-issued.info/docs/draft-ietf-oauth-json-web-token.html)) 的Java实现。

如果您正在寻找**Android**版本的JWT Decoder，请查看我们的[JWTDecode.Android](https://github.com/auth0/JWTDecode.Android)库。

## Installation

### Maven

```xml
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>java-jwt</artifactId>
    <version>3.3.0</version>
</dependency>
```

### Gradle

```text
compile 'com.auth0:java-jwt:3.3.0'
```

## Available Algorithms

该库使用以下算法实现JWT验证和签名：

![](./images/algorithms.png)

## Usage

### Pick the Algorithm

算法定义了令牌如何被签名和验证。

- 在HMAC算法的情况下，可以用`secret`实例化;
- 在RSA和ECDSA算法情况下，使用密钥对(`key pairs`)或`KeyProvider`实例化。 

一旦创建，该实例就可以重用于令牌签名和验证操作。

When using RSA or ECDSA algorithms and you just need to sign JWTs you can avoid specifying a Public Key by passing a null value. 
The same can be done with the Private Key when you just need to verify JWTs.

Using static secrets or keys:

```java
//HMAC
Algorithm algorithmHS = Algorithm.HMAC256("secret");

//RSA
RSAPublicKey publicKey = //Get the key instance
RSAPrivateKey privateKey = //Get the key instance
Algorithm algorithmRS = Algorithm.RSA256(publicKey, privateKey);
```

Using a KeyProvider:

通过使用`KeyProvider`，您可以在运行时更改用于验证令牌签名或为RSA或ECDSA算法签署新令牌的密钥。
这是通过实现`RSAKeyProvider`或`ECDSAKeyProvider`方法来实现的：

- getPublicKeyById(String kid)：它在令牌签名验证期间被调用，它应该返回用于验证令牌的密钥。
    如果正在使用`key rotation`，例如:[JWK](https://tools.ietf.org/html/rfc7517)可以使用id获取正确的`key rotation`。（或者只是一直返回相同的密钥）。
- getPrivateKey()：它在令牌签名期间被调用，它应该返回将被用来签署JWT的密钥。
- getPrivateKeyId()：它在令牌签名期间被调用，它应该返回标识由`getPrivateKey()`返回的密钥的ID。
    此值优于`JWTCreator.Builder#withKeyId(String)`方法中设置的值。

如果您不需要设置`kid`值，请避免使用`KeyProvider`实例化算法。

以下片段使用示例类来显示这将如何工作:

```java
final JwkStore jwkStore = new JwkStore("{JWKS_FILE_HOST}");
final RSAPrivateKey privateKey = //Get the key instance
final String privateKeyId = //Create an Id for the above key

RSAKeyProvider keyProvider = new RSAKeyProvider() {
    @Override
    public RSAPublicKey getPublicKeyById(String kid) {
        // 如果在Token的Header中没有定义'kid',接收到的值则可能为null
        RSAPublicKey publicKey = jwkStore.get(kid);
        return (RSAPublicKey) publicKey;
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    @Override
    public String getPrivateKeyId() {
        return privateKeyId;
    }
};

Algorithm algorithm = Algorithm.RSA256(keyProvider);
//Use the Algorithm to create and verify JWTs.
```

>对于使用JWK的简单`key rotation`，请尝试[jwks-rsa-java](https://github.com/auth0/jwks-rsa-java)库。

>源码参考：

```java
package com.auth0.jwt.impl;


public interface PublicClaims {

    //Header
    String ALGORITHM = "alg";
    String CONTENT_TYPE = "cty";
    String TYPE = "typ";
    String KEY_ID = "kid";

    //Payload
    String ISSUER = "iss";
    String SUBJECT = "sub";
    String EXPIRES_AT = "exp";
    String NOT_BEFORE = "nbf";
    String ISSUED_AT = "iat";
    String JWT_ID = "jti";
    String AUDIENCE = "aud";

}
```

```java
public final class JWTCreator {
    ...
    
    public static class Builder {
        ...
        
        /**
         * 添加一个特定的密钥ID("kid")声明(Claims)到`Header`。
         * 
         * 如果用于签署该令牌的{@link Algorithm}是通过`KeyProvider`实例化的，那么将从该`KeyProvider`处获取'kid'值，而这个值将被忽略。
         * 
         * @param keyId the Key Id value.
         * @return this same Builder instance.
         */
        public Builder withKeyId(String keyId) {
            this.headerClaims.put(PublicClaims.KEY_ID, keyId);
            return this;
        }
    }
}
```

## Create and Sign a Token

您首先需要通过调用`JWT.create()`来创建一个`JWTCreator`实例。
使用builder来定义您的令牌需要具有的自定义声明(Claims)。最后调用`sign() `并传递`Algorithm`实例得到字符串类型的Token。

Example using HS256

```java
try {
    Algorithm algorithm = Algorithm.HMAC256("secret");
        String token = JWT.create()
            .withIssuer("auth0")
            .sign(algorithm);
} catch (UnsupportedEncodingException exception){
    //UTF-8 encoding not supported
} catch (JWTCreationException exception){
    //Invalid Signing configuration / Couldn't convert Claims.
}
```

Example using RS256

```java
RSAPublicKey publicKey = //Get the key instance
RSAPrivateKey privateKey = //Get the key instance
try {
    Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);
    String token = JWT.create()
        .withIssuer("auth0")
        .sign(algorithm);
} catch (JWTCreationException exception){
    //Invalid Signing configuration / Couldn't convert Claims.
}
```

如果声明(Claim)无法转换为JSON，或者签名过程中使用的密钥无效，则会抛出JWTCreationException。

## Verify a Token

您首先需要通过调用`JWT.require()`并传递Algorithm实例来创建一个`JWTVerifier`实例。
如果您需要令牌具有特定的声明值(Claim)，请使用构建器(builder)来定义它们。
build()方法返回的实例是可重用的，所以你可以定义一次并用它来验证不同的标记。
最后调用verifier.verify()传递令牌。

Example using HS256

```java
String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXUyJ9.eyJpc3MiOiJhdXRoMCJ9.AbIJTDMFc7yUa5MhvcP03nJPyCPzZtQcGEp-zWfOkEE";
try {
    Algorithm algorithm = Algorithm.HMAC256("secret");
    JWTVerifier verifier = JWT.require(algorithm)
        .withIssuer("auth0")
        .build(); //Reusable verifier instance
    DecodedJWT jwt = verifier.verify(token);
} catch (UnsupportedEncodingException exception){
    //UTF-8 encoding not supported
} catch (JWTVerificationException exception){
    //Invalid signature/claims
}
```

Example using RS256

```java
String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXUyJ9.eyJpc3MiOiJhdXRoMCJ9.AbIJTDMFc7yUa5MhvcP03nJPyCPzZtQcGEp-zWfOkEE";
RSAPublicKey publicKey = //Get the key instance
RSAPrivateKey privateKey = //Get the key instance
try {
    Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);
    JWTVerifier verifier = JWT.require(algorithm)
        .withIssuer("auth0")
        .build(); //Reusable verifier instance
    DecodedJWT jwt = verifier.verify(token);
} catch (JWTVerificationException exception){
    //Invalid signature/claims
}
```

如果令牌具有无效签名或者不符合声明(Claim)要求，则会抛出JWTVerificationException。

### Time Validation

JWT令牌可能包含可用于验证的DateNumber字段：

- 令牌是在过去的日期发行的 `"iat" < TODAY`;
    iat(issued at): 在什么时候签发的token
- 令牌尚未过期 `"exp" > TODAY`;
    exp(expires): token什么时候过期
- 令牌已经可以使用了 `"nbf" > TODAY`; // TODO
    nbf(not before)：token在此时间之前不能被接收处理

验证令牌时会自动进行时间验证，在值无效时抛出`JWTVerificationException`。如果以前的任何字段丢失，则不会在此验证中考虑。

要指定令牌仍应被视为有效的回旋窗口(`leeway window`)，请使用`JWTVerifier builder`中的`acceptLeeway()`方法并传递一个正的秒值。
这适用于上面列出的每个项目。

```java
JWTVerifier verifier = JWT.require(algorithm)
    .acceptLeeway(1) // 1 sec for nbf, iat and exp
    .build();
```

您还可以为给定的日期声明指定自定义值，并仅覆盖该声明的默认值。

```java
JWTVerifier verifier = JWT.require(algorithm)
    .acceptLeeway(1)   //1 sec for nbf and iat
    .acceptExpiresAt(5)   //5 secs for exp
    .build();
```

如果您需要在`lib/app`中测试此行为，请将`Verification`实例转换为`BaseVerification`以获取接受自定义`Clock`参数的`verification.build()`方法的可见性。 

例如：

```java
BaseVerification verification = (BaseVerification) JWT.require(algorithm)
    .acceptLeeway(1)
    .acceptExpiresAt(5);
Clock clock = new ClockImpl(); //ClockImpl Must implement Clock interface
JWTVerifier verifier = verification.build(clock);
```

>源码参考：

```java
package com.auth0.jwt;

public final class JWTVerifier {
    
    private final Algorithm algorithm;
    final Map<String, Object> claims;
    private final Clock clock;

    JWTVerifier(Algorithm algorithm, Map<String, Object> claims, Clock clock) {
        this.algorithm = algorithm;
        this.claims = Collections.unmodifiableMap(claims);
        this.clock = clock;
    }
    
    ...
    
    public static class BaseVerification implements Verification {
        ...
        
        /**
         * 使用已经提供的配置创建{@link JWTVerifier}的新的可重用实例
         *
         * @return 新的{@link JWTVerifier}实例
         */
        @Override
        public JWTVerifier build() {
            return this.build(new ClockImpl());
        }
        
        /**
         * 
         * 使用已经提供的配置创建JWTVerifier的新的可重用实例。仅用于测试目的。
         *
         * @param clock the instance that will handle the current time.
         * @return 使用自定义Clock的新的{@link JWTVerifier}实例
         */
        public JWTVerifier build(Clock clock) {
            addLeewayToDateClaims();
            return new JWTVerifier(algorithm, claims, clock);
        }
        
        /**
         * 定义Not Before, Issued At and Expires At 声明依然有效的默认回旋窗口(`leeway window`)，以秒为单位
         * 
         * 在给定的Claim中设置一个特定的`leeway`值将覆盖该Claim的这个值。
         *
         * @param leeway Not Before, Issued At and Expires At 声明依然有效的默认回旋窗口(`leeway window`)，以秒为单位
         * @return 同一个Verification实例.
         * @throws IllegalArgumentException 如果leeway是负数.
         */
        @Override
        public Verification acceptLeeway(long leeway) throws IllegalArgumentException {
            assertPositive(leeway);
            this.defaultLeeway = leeway;
            return this;
        }
        
        private void addLeewayToDateClaims() {
            if (!claims.containsKey(PublicClaims.EXPIRES_AT)) {
                claims.put(PublicClaims.EXPIRES_AT, defaultLeeway);
            }
            if (!claims.containsKey(PublicClaims.NOT_BEFORE)) {
                claims.put(PublicClaims.NOT_BEFORE, defaultLeeway);
            }
            if (!claims.containsKey(PublicClaims.ISSUED_AT)) {
                claims.put(PublicClaims.ISSUED_AT, defaultLeeway);
            }
        }
        
        @Override
        public Verification acceptExpiresAt(long leeway) throws IllegalArgumentException {
            assertPositive(leeway);
            requireClaim(PublicClaims.EXPIRES_AT, leeway);
            return this;
        }
        
        private void requireClaim(String name, Object value) {
            if (value == null) {
                claims.remove(name);
                return;
            }
            claims.put(name, value);
        }
    }
    
}
```

## Decode a Token

```java
String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXUyJ9.eyJpc3MiOiJhdXRoMCJ9.AbIJTDMFc7yUa5MhvcP03nJPyCPzZtQcGEp-zWfOkEE";
try {
    DecodedJWT jwt = JWT.decode(token);
} catch (JWTDecodeException exception){
    //Invalid token
}
```

如果令牌具有无效的语法，或者Header或有效负载不是JSON，则会抛出JWTDecodeException。

## Header Claims

- Algorithm ("alg")

返回算法值，如果未在Header中定义，则返回null。

```java
String algorithm = jwt.getAlgorithm();
```

- Type ("typ")

返回Type值，如果没有在Header中定义，则返回null。

```java
String type = jwt.getType();
```

- Content Type ("cty")

返回Content Type值;如果未在Header中定义，则返回null。

```java
String contentType = jwt.getContentType();
```

- Key Id ("kid")

返回Key Id值;如果未在Header中定义，则返回null。

```java
String keyId = jwt.getKeyId();
```

- Private Claims

令牌Header中定义的其他声明可以通过调用`getHeaderClaim()`并传递声明名称来获得。
声明将始终返回，即使找不到。
您可以通过调用`claim.isNull()`来检查Claim的值是否为null。

```java
Claim claim = jwt.getHeaderClaim("owner");
```

当使用`JWT.create()`创建令牌时，您可以通过调用`withHeader()`来指定Header声明，并传递声明的Map。

```java
Map<String, Object> headerClaims = new HashMap();
headerClaims.put("owner", "auth0");
String token = JWT.create()
        .withHeader(headerClaims)
        .sign(algorithm);
```

>在签名过程之后，`alg`和`typ`值将始终包含在Header中。

## Payload Claims

- Issuer ("iss")

返回Issuer值，如果在有效载荷中未定义则返回null。

```java
String issuer = jwt.getIssuer();
```

- Subject ("sub")

返回Subject值，如果在有效载荷中未定义则返回null。

```java
String subject = jwt.getSubject();
```

- Audience ("aud")

返回Audience值，如果在有效载荷中未定义则返回null。

```java
List<String> audience = jwt.getAudience();
```

- Expiration Time ("exp")

返回Expiration Time值，如果在有效载荷中未定义则返回null。

```java
Date expiresAt = jwt.getExpiresAt();
```

- Not Before ("nbf")

返回Not Before值，如果在有效载荷中未定义则返回null。

```java
Date notBefore = jwt.getNotBefore();
```

- Issued At ("iat")

返回Issued At值，如果在有效载荷中未定义则返回null。

```java
Date issuedAt = jwt.getIssuedAt();
```

- JWT ID ("jti")

返回JWT ID值，如果在有效载荷中未定义则返回null。

```java
String id = jwt.getId();
```

- Private Claims

通过调用`getClaims()`或`getClaim()`并传递声明名称，可以获得在令牌的有效负载中定义的其他声明。
声明将始终返回，即使找不到。您可以通过调用`claim.isNull()`来检查Claim的值是否为null。

```java
Map<String, Claim> claims = jwt.getClaims();    //Key is the Claim name
Claim claim = claims.get("isAdmin");
```

or

```java
Claim claim = jwt.getClaim("isAdmin");
```

使用`JWT.create()`创建令牌时，可以通过调用`withClaim()`并传递名称和值来指定自定义声明。

```java
String token = JWT.create()
        .withClaim("name", 123)
        .withArrayClaim("array", new Integer[]{1, 2, 3})
        .sign(algorithm);
```

您还可以**_通过调用`withClaim()`并传递名称和所需值来验证`JWT.require()`上的自定义声明_**。

```java
JWTVerifier verifier = JWT.require(algorithm)
    .withClaim("name", 123)
    .withArrayClaim("array", 1, 2, 3)
    .build();
DecodedJWT jwt = verifier.verify("my.jwt.token");
```

>当前支持的自定义JWT声明创建和验证的类是：Boolean, Integer, Double, String, Date and Arrays of type String and Integer。

## Claim Class

声明类是声明值的包装。它允许您将声明作为不同的类类型获取。

### Primitives

可用的帮助方法有：

- asBoolean（）：返回布尔值，如果无法转换，则返回null。 
- asInt（）：返回整数值，如果无法转换则返回null。 
- asDouble（）：返回Double值，如果无法转换，则返回null。 
- asLong（）：返回Long值，如果无法转换，则返回null。 
- asString（）：返回String值，如果无法转换，则返回null。 
- asDate（）：返回Date值，如果无法转换，则返回null。 这必须是NumericDate（Unix Epoch / Timestamp）。 请注意，JWT标准规定所有NumericDate值必须以秒为单位。

### Custom Classes and Collections

要获取声明作为集合，您需要提供要转换的内容的类类型。 

- as（class）：返回解析为Class Type的值。 对于集合，您应该使用asArray和asList方法。 
- asMap（）：返回解析为Map <String，Object>的值。 
- asArray（class）：返回解析为类型为Class Type的元素的数组的值，如果值不是JSON数组，则返回null。 
- asList（class）：返回解析为类型为Class Type的元素的集合的值，如果该值不是JSON数组，则返回null。

如果值不能转换为给定的类类型，则会抛出JWTDecodeException异常。

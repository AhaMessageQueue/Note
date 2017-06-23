## Java JWT
Github地址：<https://github.com/auth0/java-jwt>

协议：<https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-31>

JSON Web Tokens的Java实现

如果您正在寻找一个Android版本的JWT Decoder，请查看我们的[JWTDecode.Android库](https://github.com/auth0/JWTDecode.Android)。

## Installation
### Maven
```xml
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>java-jwt</artifactId>
    <version>3.2.0</version>
</dependency>
```
### Gradle
```text
compile 'com.auth0:java-jwt:3.2.0'
```

### Available Algorithms
该库使用以下算法实现JWT签名和验证：

|JWS    |Algorithm  |Description    |
|-------|-----------|---------------|
|HS256 	|HMAC256 	|HMAC with SHA-256                      |
|HS384 	|HMAC384 	|HMAC with SHA-384                      |
|HS512 	|HMAC512 	|HMAC with SHA-512                      |
|RS256 	|RSA256 	|RSASSA-PKCS1-v1_5 with SHA-256         |
|RS384 	|RSA384 	|RSASSA-PKCS1-v1_5 with SHA-384         |
|RS512 	|RSA512 	|RSASSA-PKCS1-v1_5 with SHA-512         |
|ES256 	|ECDSA256 	|ECDSA with curve P-256 and SHA-256     |
|ES384 	|ECDSA384 	|ECDSA with curve P-384 and SHA-384     |
|ES512 	|ECDSA512 	|ECDSA with curve P-521 and SHA-512     |

## Usage
### Pick the Algorithm
算法定义了令牌如何签名和验证。
- 在HMAC算法的情况下，可以使用the raw value of the secret实例化;
- 在RSA和ECDSA算法的情况下，可以使用the key pairs或KeyProvider来实例化。

创建后，该实例可重用于令牌签名和验证操作。

#### Using static secrets or keys
```text
//HMAC
Algorithm algorithmHS = Algorithm.HMAC256("secret");

//RSA
RSAPublicKey publicKey = //Get the key instance
RSAPrivateKey privateKey = //Get the key instance
Algorithm algorithmRS = Algorithm.RSA256(publicKey, privateKey);
```
#### Using a KeyProvider
通过使用`KeyProvider`，您可以在**运行时**更改用于验证令牌签名或为RSA或ECDSA算法签署新令牌的密钥。

这是通过实现RSAKeyProvider或ECDSAKeyProvider方法来实现的：

- `getPublicKeyById(String kid)`：它在验证令牌签名期间调用，它应该返回用于验证令牌的密钥(key)。
    如果使用`key rotation`，例如[JWK](https://tools.ietf.org/html/rfc7517)，可以使用`id`获取正确的`rotation key`。
    (或者只是一直返回相同的键)。
    
- `getPrivateKey()`：它在令牌签名期间调用，它应该返回将用于签署JWT的密钥。
- `getPrivateKeyId()`：它在令牌签名期间调用，并且应该返回标识由getPrivateKey()返回的密钥的ID。 
    该值优于JWTCreator.Builder#withKeyId(String)方法中的值。
    如果您不需要设置一个kid值，则避免使用KeyProvider实例化一个算法。

JWTCreator.Builder#withKeyId(String)方法源码：
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
package com.auth0.jwt;

public final class JWTCreator {
    
    ...
    public static class Builder {
        ...
        /**
        * 向Header添加特定的Key Id ("kid")声明。
        * 
        * 如果用于签署该令牌的{@link Algorithm}用KeyProvider进行了实例化，那么kid值将从KeyProvider获取，而这个值将被忽略。
        */
        public Builder withKeyId(String keyId) {
            this.headerClaims.put(PublicClaims.KEY_ID, keyId);
            return this;
        }
    }
}

```
以下代码片段使用示例类来显示如何工作：
```java
final JwkStore jwkStore = new JwkStore("{JWKS_FILE_HOST}");
final RSAPrivateKey privateKey = //获取密钥实例
final String privateKeyId = //创建上述密钥的ID

RSAKeyProvider keyProvider = new RSAKeyProvider(){
    
    //公钥
    @Override
    public RSAPublicKey getPublicKeyById(String kid) {
        //如果Token的header中没有定义，则收到kid值可能为null
        RSAPublicKey publicKey = jwkStore.get(kid);
        return (RSAPublicKey) publicKey;
    }
    
    //私钥
    @Override
    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }
    
    @Override
    public String getPrivateKeyId() {
        return privateKeyId;
    }
}
```
>对于使用JWK的简单key rotation，请尝试使用[jwks-rsa-java](https://github.com/auth0/jwks-rsa-java)库。

### Create and Sign a Token
您首先需要通过调用`JWT.create()`创建一个`JWTCreator`实例。

使用`builder`定义您的令牌需要具有的自定义声明。

最后传递Algorithm实例并调用sign()得到令牌。

- Example using HS256
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

- Example using RS256
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
如果Claim无法转换为JSON或签名过程中使用的密钥无效，将会抛出JWTCreationException异常。

### Verify a Token
您首先需要通过调用JWT.require()并传递Algorithm实例来创建一个JWTVerifier实例。 

**如果要求令牌具有特定的声明值，请使用Verification.xxx()（Builder模式）来定义它们**。 

方法build()返回的实例是可重用的，因此您可以定义一次，并使用它来验证不同的令牌。 

最后调用verifier.verify()传递令牌。

- Example using HS256
```java
String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXUyJ9.eyJpc3MiOiJhdXRoMCJ9.AbIJTDMFc7yUa5MhvcP03nJPyCPzZtQcGEp-zWfOkEE";
try {
    Algorithm algorithm = Algorithm.HMAC256("secret");
    JWTVerifier verifier = JWT.require(algorithm)
            .withIssuer("auth0")// 要求令牌颁发者为auth0
            .build(); //Reusable verifier instance
        DecodedJWT jwt = verifier.verify(token);
} catch (UnsupportedEncodingException exception){
    //UTF-8 encoding not supported
} catch (JWTVerificationException exception){
    //Invalid signature/claims
}
```
- Example using RS256
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
如果令牌具有无效的签名或者不满足Claim要求，则会抛出JWTVerificationException异常。

### Time Validation
JWT令牌可能包含可以用于验证的DateNumber字段：

- 令牌是在以前颁发的 "iat" < TODAY
- 令牌尚未过期 "exp" > TODAY
- 令牌可以被使用 "nbf" > TODAY

验证令牌时，会自动执行时间验证，当值无效时，抛出JWTVerificationException异常。

如果之前的任何字段丢失，则此验证不会被考虑。

指定令牌仍应视为有效的leeway window，请使用JWTVerifier builder中的acceptLeeway()方法并传递正秒值。

这适用于上述每个项目。

```java
JWTVerifier verifier = JWT.require(algorithm)
    .acceptLeeway(1) // 1 sec for nbf, iat and exp
    .build();
```
您还可以为给定的日期声明指定自定义值，并仅覆盖该声明的默认值。

```java
JWTVerifier verifier = JWT.require(algorithm)
    .acceptLeeway(1)   // 1 sec for nbf, iat and exp
    .acceptExpiresAt(5)   //5 secs for exp
    .build();
```
>源码参考：
```java
package com.auth0.jwt;

public final class JWTVerifier {
    
    ...
    public static class BaseVerification implements Verification {
        ...
        
        @Override
        public Verification withIssuer(String issuer) {
            requireClaim(PublicClaims.ISSUER, issuer);
            return this;
        }
        
        ...
        
        /**
        * 定义Not Before, Issued At and Expires At Claims仍将有效的默认window(以秒为单位)。
        * 设置一个特定的leeway值在给定的Claim，将会覆盖该Claim的这个值。
        *  
        * @param leeway Not Before, Issued At and Expires At Claims仍将有效的默认window(以秒为单位)
        * @return 同一个验证实例。
        * @throws IllegalArgumentException 如果leeway是负数。
        */
        @Override
        public Verification acceptLeeway(long leeway) throws IllegalArgumentException {
            assertPositive(leeway);
            this.defaultLeeway = leeway;
            return this;
        }
        
        /**
        * 以秒为单位设置一个Expires At ("exp") Claim仍然有效的特定的leeway window。
        * 
        * Expiration Date当值存在时总是被验证。此方法将覆盖使用acceptLeeway设置的值
        * 
        * @param leeway Expires At Claims仍然有效的秒数。
        * @return 同一个验证实例。
        * @throws IllegalArgumentException 如果leeway是负数。
        */
        @Override
        public Verification acceptExpiresAt(long leeway) throws IllegalArgumentException {
            assertPositive(leeway);
            requireClaim(PublicClaims.EXPIRES_AT, leeway);
            return this;
        }
        ...
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
        
        private void requireClaim(String name, Object value) {
            if (value == null) {
                claims.remove(name);
                return;
            }
            claims.put(name, value);
        }
        ...
        
        public JWTVerifier build() {
            return this.build(new ClockImpl());
        }
        
        public JWTVerifier build(Clock clock) {
            addLeewayToDateClaims();
            return new JWTVerifier(algorithm, claims, clock);
        }
        
        private void assertPositive(long leeway) {
            if (leeway < 0) {
                throw new IllegalArgumentException("Leeway value can't be negative.");
            }
        }
    }
}
```
您可以将`Verification`实例转换为`BaseVerification`， 以使用接受自定义`Clock`的`verification.build()`方法。

```java
BaseVerification verification = (BaseVerification) JWT.require(algorithm)
    .acceptLeeway(1)
    .acceptExpiresAt(5);
Clock clock = new CustomClock(); //Must implement Clock interface
JWTVerifier verifier = verification.build(clock);
```

### Decode a Token
```java
String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXUyJ9.eyJpc3MiOiJhdXRoMCJ9.AbIJTDMFc7yUa5MhvcP03nJPyCPzZtQcGEp-zWfOkEE";
try {
    DecodedJWT jwt = JWT.decode(token);
} catch (JWTDecodeException exception){
    //Invalid token
}
```
如果令牌具有无效的语法，或者令牌头或有效载荷不是JSON，那么将会抛出JWTDecodeException异常。

### Header Claims

#### Algorithm ("alg")
返回Algorithm值，如果Header中没有定义，则返回null。
```java
String algorithm = jwt.getAlgorithm();
```
#### Type ("typ")
返回Type值，如果在Header中没有定义，则返回null。
```java
String type = jwt.getType();
```

#### Content Type ("cty")
返回Content Type值，如果Header中没有定义，则返回null。
```java
String contentType = jwt.getContentType();
```

#### Key Id ("kid")
返回Key Id值，如果Header中没有定义，则返回null。
```java
String keyId = jwt.getKeyId();
```

#### Private Claims
通过调用`getHeaderClaim()`并传递声明名称可以获得令牌头中定义的附加声明。
该方法总会返回声明（Claim），即使该声明无法找到。您可以通过调用claim.isNull()来检查Claim的值是否为null。

```java
Claim claim = jwt.getHeaderClaim("owner");
```

使用`JWT.create()`创建令牌时，可以通过调用withHeader()并传递声明的Map来指定令牌头部声明。
```java
Map<String, Object> headerClaims = new HashMap();
headerClaims.put("owner", "auth0");
String token = JWT.create()
        .withHeader(headerClaims)
        .sign(algorithm);
```
>签名过程中，alg和typ将始终包含在令牌头部中。

### Payload Claims
#### Issuer ("iss")
返回Issuer值，如果在有效载荷中未定义则返回null。、
```java
String issuer = jwt.getIssuer();
```
#### Subject ("sub")
返回Subject值，如果在有效载荷中未定义则返回null。
```java
String subject = jwt.getSubject();
返回Audience值，如果在有效载荷中未定义则返回null。
```
#### Audience ("aud")
```java
List<String> audience = jwt.getAudience();
```
#### Expiration Time ("exp")
返回Expiration Time值，如果在有效载荷中未定义则返回null。
```java
Date expiresAt = jwt.getExpiresAt();
```
#### Not Before ("nbf")
返回Not Before值，如果在有效载荷中未定义则返回null。
```java
Date notBefore = jwt.getNotBefore();
```
#### Issued At ("iat")
返回Issued At值，如果在有效载荷中未定义则返回null。
```java
Date issuedAt = jwt.getIssuedAt();
```
#### JWT ID ("jti")
返回JWT ID值，如果在有效载荷中未定义则返回null。
```java
String id = jwt.getId();
```
#### Private Claims
在令牌的有效载荷中定义的附加声明可以通过调用getClaims()或getClaim()并传递声明名称来获得。
该方法总会返回Claim，即使该声明无法找到。 您可以通过调用claim.isNull()来检查Claim的值是否为null。

```java
Map<String, Claim> claims = jwt.getClaims();    //Key is the Claim name
Claim claim = claims.get("isAdmin");
```

```java
Claim claim = jwt.getClaim("isAdmin");
```
使用JWT.create()创建令牌时，可以通过调用withClaim()并传递名称和值两者来指定自定义声明。

```java
String token = JWT.create()
        .withClaim("name", 123)
        .withArrayClaim("array", new Integer[]{1, 2, 3})
        .sign(algorithm);
```
您还可以**通过调用withClaim()并传递名称和所需的值，来验证JWT.require()中的自定义声明**。
```java
JWTVerifier verifier = JWT.require(algorithm)
    .withClaim("name", 123)
    .withArrayClaim("array", 1, 2, 3)
    .build();
DecodedJWT jwt = verifier.verify("my.jwt.token");
```
用于自定义JWT声明创建和验证的当前支持的类是：Boolean，Integer，Double，String，Date，String和Integer类型的Arrays数组。

### Claim Class
声明类是声明值的包装。它允许您将声明作为不同的类类型获取。
#### Primitives
可用的帮助方法有：
- asBoolean（）：返回布尔值，如果无法转换，则返回null。 
- asInt（）：返回整数值，如果无法转换则返回null。 
- asDouble（）：返回Double值，如果无法转换，则返回null。 
- asLong（）：返回Long值，如果无法转换，则返回null。 
- asString（）：返回String值，如果无法转换，则返回null。 
- asDate（）：返回Date值，如果无法转换，则返回null。 这必须是NumericDate（Unix Epoch / Timestamp）。 请注意，JWT标准规定所有NumericDate值必须以秒为单位。

##### Custom Classes and Collections
要获取声明作为集合，您需要提供要转换的内容的类类型。 

- as（class）：返回解析为Class Type的值。 对于集合，您应该使用asArray和asList方法。 
- asMap（）：返回解析为Map <String，Object>的值。 
- asArray（class）：返回解析为类型为Class Type的元素的数组的值，如果值不是JSON数组，则返回null。 
- asList（class）：返回解析为类型为Class Type的元素的集合的值，如果该值不是JSON数组，则返回null。

如果值不能转换为给定的类类型，则会抛出JWTDecodeException异常。


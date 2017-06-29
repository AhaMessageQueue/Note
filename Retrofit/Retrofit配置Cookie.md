### 设置cookie
服务端可能需要保持请求是同一个cookie，主要看各自需求

1. app/build.gradle

    ```
    compile 'com.squareup.okhttp3:okhttp-urlconnection:3.2.0'
    ```
2. 设置cookie

    ```
    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    builder.cookieJar(new JavaNetCookieJar(cookieManager));
    ```
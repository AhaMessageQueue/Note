### 添加依赖
app/build.gradle
```
compile 'com.squareup.retrofit2:retrofit:2.0.2'
```
首先Builder()，得到OkHttpClient.Builder对象builder
```
OkHttpClient.Builder builder = new OkHttpClient.Builder();
```
### Log信息拦截器
`Debug`可以看到，网络请求，打印Log信息，发布的时候就不需要这些log

1. 添加依赖
    
    app/build.gradle
    ```
    compile 'com.squareup.okhttp3:logging-interceptor:3.1.2'
    ```
    
2. Log信息拦截器
    
    ```
    if (BuildConfig.DEBUG) {
        // Log信息拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        //设置 Debug Log 模式
        builder.addInterceptor(loggingInterceptor);
    }
    ```
### 缓存机制
无网络时，也能显示数据

### 公共参数
```
//公共参数
Interceptor addQueryParameterInterceptor = new Interceptor() {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request request;
        String method = originalRequest.method();
        Headers headers = originalRequest.headers();
        HttpUrl modifiedUrl = originalRequest.url().newBuilder()
                // Provide your custom parameter here
                .addQueryParameter("platform", "android")
                .addQueryParameter("version", "1.0.0")              
                .build();
        request = originalRequest.newBuilder().url(modifiedUrl).build();
        return chain.proceed(request);
    }
};
//公共参数
builder.addInterceptor(addQueryParameterInterceptor);
```
### 设置头
有的接口可能对请求头要设置
```
Interceptor headerInterceptor = new Interceptor() {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder()
                .header("AppType", "TPOS")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method(originalRequest.method(), originalRequest.body());
        Request request = requestBuilder.build();
        return chain.proceed(request);
    }
};
//设置头
builder.addInterceptor(headerInterceptor );
```
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

### 设置超时和重连
希望超时时能重连
```
`//设置超时
 builder.connectTimeout(15, TimeUnit.SECONDS);
 builder.readTimeout(20, TimeUnit.SECONDS);
 builder.writeTimeout(20, TimeUnit.SECONDS);
 //错误重连
 builder.retryOnConnectionFailure(true);
```
最后将这些配置设置给retrofit：
```
OkHttpClient okHttpClient = builder.build();
Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(ApiStores.API_SERVER_URL)
        //设置 Json 转换器
        .addConverterFactory(GsonConverterFactory.create())
        //RxJava 适配器
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .client(okHttpClient)
        .build();
```
### 完整配置
```
public class AppClient {
    public static Retrofit retrofit = null;
    public static Retrofit retrofit() {
        if (retrofit == null) {
	         OkHttpClient.Builder builder = new OkHttpClient.Builder();
            /**
             *设置缓存，代码略
             */
                      
            /**
             *  公共参数，代码略
             */
           
            /**
             * 设置头，代码略
             */           
           
			 /**
             * Log信息拦截器，代码略
             */
            
			 /**
             * 设置cookie，代码略
             */
            
             /**
             * 设置超时和重连，代码略
             */
            //以上设置结束，才能build(),不然设置白搭
            OkHttpClient okHttpClient = builder.build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiStores.API_SERVER_URL)                  
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }
}
```
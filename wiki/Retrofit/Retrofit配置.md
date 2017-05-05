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

1. 先开启OkHttp缓存
    ```
    File httpCacheDirectory = new File(MyApp.mContext.getCacheDir(), "responses");
    int cacheSize = 10 * 1024 * 1024; // 10 MiB
    Cache cache = new Cache(httpCacheDirectory, cacheSize);
    
    OkHttpClient client = new OkHttpClient.Builder()
           .addInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
           .cache(cache).build();
    Retrofit retrofit = new Retrofit.Builder()
           .baseUrl(BASE_URL)
           .client(client)
           .addConverterFactory(GsonConverterFactory.create())
           .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
           .build();
    ```
    这一步是设置缓存路径，以及缓存大小，其中addInterceptor是我们第二步的内容。
    
2. 设置 OkHttp 拦截器
    
    - 设置拦截器
    ```
    Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = chain -> {
    
        CacheControl.Builder cacheBuilder = new CacheControl.Builder();
        cacheBuilder.maxAge(0, TimeUnit.SECONDS);
        cacheBuilder.maxStale(365,TimeUnit.DAYS);
        CacheControl cacheControl = cacheBuilder.build();
        
        Request request = chain.request();
        if(!StateUtils.isNetworkAvailable(MyApp.mContext)){
         request = request.newBuilder()
                     .cacheControl(CacheControl.FORCE_CACHE)
                     .build();
        }
        Response originalResponse = chain.proceed(request);
        if (StateUtils.isNetworkAvailable(MyApp.mContext)) {
         int maxAge = 0; // 有网络时 设置缓存超时时间0个小时
         return originalResponse.newBuilder()
                 .removeHeader("Pragma")
                 .header("Cache-Control", "public ,max-age=" + maxAge)
                 .build();
        } else {
         int maxStale = 60 * 60 * 24 * 28; // 无网络时，设置超时为4周
         return originalResponse.newBuilder()
                 .removeHeader("Pragma")
                 .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                 .build();
        }
    };
    ```
    
**`max-age`和`max-stale`的区别：**

![](\images\max-age与max-stale含义.png)

想了解更多[点击这里](http://www.cnblogs.com/_franky/archive/2011/11/23/2260109.html).

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
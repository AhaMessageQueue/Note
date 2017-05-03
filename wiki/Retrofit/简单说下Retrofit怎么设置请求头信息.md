简单说下Retrofit怎么设置请求头信息

不多说，直接上代码:

有三种方式：

### 直接在参数里写 每次访问的时候都要传入一下
```
@GET("weatherservice/citylist")
Observable<WeatherRestBean> queryWeather(@Header("apikey") String apikey,@Query("cityname") String cityname);
```

### 写到注解里这样就少了个参数，但是每定义个接口都要写一次也是比较麻烦
```
@Headers("apikey:ac7c302dc489a69082cbee6a89e3646c")
@GET("weatherservice/cityid")
Observable<WeatherEntity> query(@Query("cityid")String cityid);
```

### 在创建Retrofit的时候添加，最方便的方式
```
OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Request.Builder builder1 = request.newBuilder();
                Request build = builder1.addHeader("apikey", "ac7c302dc489a69082cbee6********").build();
                return chain.proceed(build);
            }
        }).retryOnConnectionFailure(true)
          .build();
mRetrofit = new Retrofit.Builder()
           .client(client)
           .baseUrl(ConstantApi.url)
           .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
           .addConverterFactory(GsonConverterFactory.create())
```
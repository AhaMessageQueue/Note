本文更新于2017-1-18，基于okthhp 3.5.0测试,最好查看原文，因为可能会更新

转载务必注明出处：http://blog.csdn.net/u014614038/article/details/51210685

## 首先进行设置：
```
public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/json; charset=utf-8");//设置MediaType  
    private static final OkHttpClient client;  
    private static final long cacheSize = 1024*1024*20;//缓存文件最大限制大小20M  
    private static String cachedirectory = FilePathroot + "/caches";  //设置缓存文件路径  
    private static Cache cache = new Cache(new File(cachedirectory), cacheSize);  //  
  
    static {  
          
          
        OkHttpClient.Builder builder = new OkHttpClient.Builder();  
        builder.connectTimeout(8, TimeUnit.SECONDS);  //设置连接超时时间  
        builder.writeTimeout(8, TimeUnit.SECONDS);//设置写入超时时间  
        builder.readTimeout(8, TimeUnit.SECONDS);//设置读取数据超时时间  
        builder.retryOnConnectionFailure(false);//设置不进行连接失败重试  
        builder.cache(cache);//设置缓存  
        client = builder.build();  
  
    }  
```
>注意的是：okhttp只会对get请求进行缓存，post请求是不会进行缓存，这也是有道理的，因为get请求的数据一般是比较持久的，而post一般是交互操作，没太大意义进行缓存。

## 进行Get请求：
一般来说的是，我们get请求有时有不一样的需求，有时需要进行缓存，有时需要直接从网络获取，有时只获取缓存数据，
这些处理，okhttp都有帮我们做了，我们做的只需要设置就是了。下面是整理的各种需求的设置与使用方法。

### 进行get请求,并将数据缓存。
```
/**
 * get请求，并缓存请求数据，返回的是缓存数据，注意，如果超出了maxAge，缓存会被清除，回调onFailure
 *
 * @param url
 * @param cache_maxAge_inseconds 缓存最大生存时间，单位为秒
 * @return 当前call
 */
public static Call DoGetAndCache(String url,int cache_maxAge_inseconds) {

    Request request = new Request.Builder()
            .cacheControl(new CacheControl.Builder().maxAge(cache_maxAge_inseconds, TimeUnit.SECONDS).build())
            .url(url).build();

    Call call = client.newCall(request);
    doRequest(call);
    return call;
}
```

### get请求，但只获取返回网络数据。
```
/**
 * get请求 ,只获取返回网络请求数据，不进行缓存
 * 
 * @param url
 * @param responseListener
 */
public static Call DoGetOnlyNet(String url) {
    Request request = new Request.Builder()
            .cacheControl(new CacheControl.Builder().maxAge(0, TimeUnit.SECONDS).build())
            .url(url).build();

    Call call = client.newCall(request);
    doRequest(call);
    return call;
}
```

### 进行get请求,并限制过时时间
```
/**
 * get请求, 没有超过过时时间StaleTime的话，返回缓存数据，否则重新去获取网络数据，StaleTime限制了默认数据fresh时间
 *
 * @param url
 * @param staletime 过时时间，秒为单位
 */
public static Call DoGetInStaleTime(String url, int staletime) {
    Request request = new Request.Builder()
            .cacheControl(new CacheControl.Builder().maxStale(staletime, TimeUnit.SECONDS).build()).url(url).build();

    Call call = client.newCall(request);
    doRequest( call);
    return call;
}
```

### 只获取返回缓存数据
```
/**
 * get请求, 只使用缓存，注意，如果是超出了staletime或者超出了maxAge的话会返回504，否则就返回缓存数据
 *
 * @param url
 */
public static Call DoGetOnlyCache(String url) {
    Request request = new Request.Builder()
            .cacheControl(new CacheControl.Builder().onlyIfCached().build()).url(url).build();

    Call call = client.newCall(request);
    doRequest(call);
    return call;
```

### doRequest(Call call0):
```
private static void doRequest(final Call call0) {  
          
        try {  
  
            call0.enqueue(new Callback() {  
  
                @Override  
                public void onFailure(Call arg0, IOException arg1) {  
                    //请求失败  
  
                }  
  
                @Override  
                public void onResponse(Call arg0, Response response) throws IOException {  
  
                    //请求返回数据  
                }  
  
            });  
  
        } catch (Exception e) {  
  
        }  
    }  
```

下面测试一下，设置缓存maxAge为10秒，我们将请求返回的数据打印出来：
![](\images\缓存测试.png)

上面图片的1是点击获取的服务器的数据，获取后断开网络然后继续点击，可以看到2还能获取到数据，说明这是缓存的数据，
当到3时，差不多就是十秒的时间，可以看到，获取数据失败了，这时已经去服务器获取数据了，缓存被清空，由于断开网络所以请求失败。


以上都是本人查看官方文档并进行测试验证后整理的，有不对的地方，万请指正。

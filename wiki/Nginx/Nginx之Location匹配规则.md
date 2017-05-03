### location匹配命令
```
~     #表示执行一个正则匹配，区分大小写
~*    #表示执行一个正则匹配，不区分大小写
^~    #表示普通字符匹配，如果该选项匹配，只匹配该选项，不匹配别的选项，一般用来匹配目录
=     #进行普通字符精确匹配
@     #定义一个命名的 location，使用在内部定向时，例如 error_page, try_files
```

### location 匹配的优先级(与location在配置文件中的顺序无关)
1. `=` `精确匹配`会第一个被处理。如果发现精确匹配，nginx停止搜索其他匹配。
2. 普通字符匹配，`正则表达式规则`和`长的块规则`将被优先和查询匹配，
    也就是说如果该项匹配还需去看有没有正则表达式匹配和更长的匹配。
    `^~` 则只匹配该规则，nginx停止搜索其他匹配，否则nginx会继续处理其他location指令。
3. 最后匹配理带有`~`和`~*`的指令，如果找到相应的匹配，则nginx停止搜索其他匹配；
    当没有正则表达式或者没有正则表达式被匹配的情况下，那么匹配程度最高的逐字匹配指令会被使用(即匹配2的结果)。
    
### location 优先级官方文档
>1.  Directives with the = prefix that match the query exactly. If found, searching stops.
2.  All remaining directives with conventional strings, longest match first. If this match used the ^~ prefix, searching stops.
3.  Regular expressions, in order of definition in the configuration file.
4.  If #3 yielded a match, that result is used. Else the match from #2 is used.

1. 带有`=`前缀的指令完全匹配查询。 如果找到，搜索停止。
2. 所有剩余的指令与常规字符串，最长的优先匹配。 如果此匹配使用`^〜`前缀，则搜索停止。
3. 正则表达式，按照配置文件中的定义顺序。
4. 如果＃3产生匹配，则使用该结果。 否则使用＃2的匹配。

```
location  = / {
  # 只匹配"/".
  [ configuration A ] 
}
location  / {
  # 匹配任何请求，因为所有请求都是以"/"开始
  # 但是更长字符匹配或者正则表达式匹配会优先匹配
  [ configuration B ] 
}
location ^~ /images/ {
  # 匹配任何以 /images/ 开始的请求，并停止匹配 其它location
  [ configuration C ] 
}
location ~* .(gif|jpg|jpeg)$ {
  # 匹配以 gif, jpg, or jpeg结尾的请求. 
  # 但是所有 /images/ 目录的请求将由 [Configuration C]处理.   
  [ configuration D ] 
}
```
请求URI例子:
```
/documents/1.jpg ->符合 configuration D
/documents/document.html -> 符合configuration B
/images/1.gif -> 符合configuration C
/ -> 符合configuration A
```

@location 例子
```
error_page 404 = @fetch;
location @fetch(
    proxy_pass http://fetch;
)
```
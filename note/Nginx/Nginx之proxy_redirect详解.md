今天在做nginx反向代理apache的时候出了一点点问题，原来后端apache用的端口是8080通过反向代理后，使用wireshark抓包发现location头域数值为http://192.168.1.154:8080/wuman/  如果把这个返回给客户端肯定是不可以的，看起来别扭而且还暴露了apache的具体信息

所以在这里用到了nginx的proxy_redirect指定修改被代理服务器返回的响应头中的location头域跟refresh头域数值

以下是截取nginx的一小段配置文档
```
server {
       listen       80;
       server_name  www.boke.com;
       location / {
            proxy_pass http://192.168.1.154:8080;
            proxy_redirect off;
       }
 }
```
此时我们通过curl查看结果得出
```
[root@localhost nginx]# curl -I http://www.boke.com/wuman
HTTP/1.1 301 Moved Permanently
Server: nginx
Date: Thu, 24 Dec 2015 12:02:00 GMT
Content-Type: text/html; charset=iso-8859-1
Connection: keep-alive
Location: http://192.168.1.154:8080/wuman/
```
这里location为带有后端服务器实际地址跟端口的响应头信息这样在实际线上是不允许的所以这里我们打算通过proxy_redirect将被代理服务器的响应头中的location字段进行修改后返回给客户端
```
server {
       listen       80;
       server_name  www.boke.com;
       location / {
            proxy_pass http://192.168.1.154:8080;
            proxy_redirect http://192.168.1.154:8080/wuman/  http://www.boke.com/wuman/;
       }
```
则curl查看返回结果
```
[root@localhost nginx]# curl -I http://www.boke.com/wuman
HTTP/1.1 301 Moved Permanently
Server: nginx
Date: Thu, 24 Dec 2015 12:08:34 GMT
Content-Type: text/html; charset=iso-8859-1
Connection: keep-alive
Location: http://www.boke.com/wuman/
```
此时查看location已经变成了我们想要的结果了。 此时通过`replacement 301`重定向到了我们新的页面

---
### proxy_redirect
```
语法：proxy_redirect [ default|off|redirect replacement ] 
默认值：proxy_redirect default 
使用字段：http, server, location 
```
如果需要修改从被代理服务器传来的应答头中的”Location”和”Refresh”字段，可以用这个指令设置。

1. 假设被代理服务器返回Location字段为： http://localhost:8000/two/some/uri/
这个指令：
```
proxy_redirect http://localhost:8000/two/ http://frontend/one/;
```
将Location字段重写为http://frontend/one/some/uri/。

2. 在代替的字段中可以不写服务器名：
```
proxy_redirect http://localhost:8000/two/ /;
```
这样就使用服务器的基本名称和端口，即使它来自非80端口。

3. 如果使用“default”参数，将根据location和proxy_pass参数的设置来决定。
例如下列两个配置等效：
```
location /one/ {
  proxy_pass       http://upstream:port/two/;
  proxy_redirect   default;
}
 
location /one/ {
  proxy_pass       http://upstream:port/two/;
  proxy_redirect   http://upstream:port/two/   /one/;
}
```
4. 在指令中可以使用一些变量：
```
proxy_redirect   http://localhost:8000/    http://$host:$server_port/;
```
这个指令有时可以重复：
```
proxy_redirect   default;
proxy_redirect   http://localhost:8000/    /;
proxy_redirect   http://www.example.com/   /;
```
参数off将在这个字段中禁止所有的proxy_redirect指令：
```
proxy_redirect   off;
proxy_redirect   default;
proxy_redirect   http://localhost:8000/    /;
proxy_redirect   http://www.example.com/   /;
```
利用这个指令可以为被代理服务器发出的相对重定向增加主机名：
```
proxy_redirect   /   /;
```
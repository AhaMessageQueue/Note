前段时间，我花了不少时间来寻求一种方法，把新开发的代码推送到到生产系统中部署，生产系统要能够零宕机、对使用用户零影响。

我的设想是使用集群来搞定，通过通知负载均衡Nginx，取下集群中的Tomcat节点，然后对Tomcat上的应用进行升级，再通知负载均衡Nginx，把Tomcat节点重新加载上去。依次这么做，把集群中的所有Tomcat都替换一次即可。

那么问题来了，在取下Tomcat节点和加载新Tomcat节点时如何做到对用户无影响呢？方法很简单，共享Session。
下面，我们用实例来说明此方案。我们的例子使用了一台Nginx做负载均衡，后端挂接了两台Tomcat，且每台Tomcat的Session会话都保存到Redis数据库中。 其中，Nginx配置为non-sticky运行模式，也即每一个请求都可以被分配到集群中的任何节点。当要上线新代码时，只需简单地取下Tomcat实 例，此时所有的访问用户会被路由到活动的Tomcat实例中去，而且由于会话数据都是保存在Redis数据库中，所以活跃用户并不会受影响。当 Tomcat更新完毕，又可以把此节点加入到Nginx中。

# 安装Nginx
nginx可以使用各平台的默认包来安装，本文是介绍使用源码编译安装，包括具体的编译参数信息。

正式开始前，编译环境gcc g++ 开发库之类的需要提前装好，这里默认你已经装好。

ububtu平台编译环境可以使用以下指令:
```
apt-get install build-essential
apt-get install libtool
```
centos平台编译环境使用如下指令
安装make：
```
yum -y install gcc automake autoconf libtool make
```
安装g++:
```
yum install gcc gcc-c++
```
下面正式开始:

---
一般我们都需要先装pcre, zlib，前者为了重写rewrite，后者为了gzip压缩。
**1.选定源码目录**
可以是任何目录，本文选定的是/usr/local/src
```
cd /usr/local/src
```
**2.安装PCRE库**
ftp://ftp.csx.cam.ac.uk/pub/software/programming/pcre/ 下载最新的 PCRE 源码包，使用下面命令下载编译和安装 PCRE 包：

```
cd /usr/local/src
wget ftp://ftp.csx.cam.ac.uk/pub/software/programming/pcre/pcre-8.39.tar.gz
tar -zxvf pcre-8.39.tar.gz
cd pcre-8.39
./configure
make
make install
```
**3.安装zlib库**
 http://zlib.net/zlib-1.2.8.tar.gz 下载最新的 zlib 源码包，使用下面命令下载编译和安装 zlib包：
```
cd /usr/local/src

wget http://zlib.net/zlib-1.2.8.tar.gz
tar -zxvf zlib-1.2.8.tar.gz
cd zlib-1.2.8
./configure
make
make install
```
**4.安装ssl（某些vps默认没装ssl)**
```
cd /usr/local/src
wget https://www.openssl.org/source/openssl-1.0.1t.tar.gz
tar -zxvf openssl-1.0.1t.tar.gz
cd openssl-1.0.1t
./config
make
make install
```
**5.安装nginx**
Nginx 一般有两个版本，分别是稳定版和开发版，您可以根据您的目的来选择这两个版本的其中一个，下面是把 Nginx 安装到 /usr/local/nginx 目录下的详细步骤：
```
cd /usr/local/src
wget http://nginx.org/download/nginx-1.9.15.tar.gz
tar -zxvf nginx-1.9.15.tar.gz
cd nginx-1.9.15

./configure --sbin-path=/usr/local/nginx/nginx \
--conf-path=/usr/local/nginx/nginx.conf \
--pid-path=/usr/local/nginx/nginx.pid \
--with-http_ssl_module \
--with-pcre=/usr/local/src/pcre-8.39 \
--with-zlib=/usr/local/src/zlib-1.2.8 \
--with-openssl=/usr/local/src/openssl-1.0.1t

make
make install
```
若安装时找不到上述依赖模块，使用`--with-openssl=<openssl_dir>、--with-pcre=<pcre_dir>、--with-zlib=<zlib_dir>`指定依赖的模块目录。如已安装过，此处的路径为安装目录；若未安装，则此路径为编译安装包路径，nginx将执行模块的默认编译安装。

安装成功后 /usr/local/nginx 目录下如下
```
fastcgi.conf            koi-win             nginx.conf.default
fastcgi.conf.default    logs                scgi_params
fastcgi_params          mime.types          scgi_params.default
fastcgi_params.default  mime.types.default  uwsgi_params
html                    nginx               uwsgi_params.default
koi-utf                 nginx.conf          win-utf
```
**6.启动**
 确保系统的 80 端口没被其他程序占用，运行/usr/local/nginx/nginx 命令来启动 Nginx，
```
netstat -ano|grep 80
```
如果查不到结果后执行，有结果则忽略此步骤（ubuntu下必须用sudo启动，不然只能在前台运行）
```
sudo /usr/local/nginx/nginx
```
# Nginx配置详解
## 2.1.nginx.conf
nginx.conf是主配置文件，默认配置去掉注释之后的内容如下:
```
worker_processes  1;    #表示工作进程的数量，一般设置为cpu的核数


events {
    worker_connections  1024;   #表示每个工作进程的最大连接数`
}


http {
    include       mime.types;
    default_type  application/octet-stream;


    sendfile        on;

    keepalive_timeout  65;

    server {#定义了虚拟主机
        listen       80;    #listener监听端口
        server_name  localhost; #server_name监听域名

        location / {    #用来为匹配的 URI 进行配置,URI 即语法中的“/uri/”.location  / { }匹配任何查询，因为所有请求都以 / 开头
            root   html;    #指定对应uri的资源查找路径，这里html为相对路径，完整路径为/usr/local/nginx/html
            index  index.html index.htm;    #index指定首页index文件的名称，可以配置多个，以空格分开。如有多个，按配置顺序查找。
        }


        # redirect server error pages to the static page /50x.html
        #
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }

    }
}
```
从配置可以看出，nginx监听了80端口、域名为localhost、根路径为html文件夹（我的安装路径为/usr/local/nginx，所以/usr/local/nginx/html）、默认index文件为index.html， index.htm、服务器错误重定向到50x.html页面。

可以看到/usr/local/nginx/html有以下文件：
```
[root@172 html]# ll
总用量 8
-rw-r--r--. 1 root root 537 8月  23 16:37 50x.html
-rw-r--r--. 1 root root 612 8月  23 16:37 index.html
```
这也是上面在浏览器中输入[http://localhost](http://localhost)，能够显示欢迎页面的原因。实际上访问的是/usr/local/nginx/html/index.html文件。


## 2.2.mime.types
**文件扩展名与文件类型映射表，nginx****根据映射关系，设置http****请求响应头的Content-Type****值**。当在映射表找不到时，使用nginx.conf中default-type指定的默认值。例如，默认配置中的指定的default-type为application/octet-stream。

    include       mime.types;

    default_type  application/octet-stream;

默认

下面截一段mime.types定义的文件扩展名与文件类型映射关系，完整的请自行查看：

![](\images\mime.types.png)

## 2.3.fastcgi_params
nginx配置Fastcgi解析时会**调用fastcgi_params配置文件来传递服务器变量**，这样CGI中可以获取到这些变量的值。默认传递以下变量：

![](\images\fastcgi_params.png)

这些变量的作用从其命名可以看出。

## 2.4.fastcgi.conf
对比下fastcgi.conf与fastcgi_params文件，可以看出只有以下差异：
```
[root@172 nginx]# diff fastcgi.conf fastcgi_params
2d1
< fastcgi_param  SCRIPT_FILENAME    $document_root$fastcgi_script_name;
```
即fastcgi.conf只比fastcgi_params多了一行"fastcgi_param  SCRIPT_FILENAME    $document_root$fastcgi_script_name;"

原本只有fastcgi_params文件，fastcgi.conf是nginx 0.8.30 (released: 15th of December 2009)才引入的。主要为是解决以下问题（参考：http://www.dwz.cn/x3GIJ）：

原本Nginx只有fastcgi_params，后来发现很多人在定义SCRIPT_FILENAME时使用了硬编码的方式。例如，fastcgi_param SCRIPT_FILENAME /var/www/foo$fastcgi_script_name。于是为了规范用法便引入了fastcgi.conf。

不过这样的话就产生一个疑问：为什么一定要引入一个新的配置文件，而不是修改旧的配置文件？这是因为fastcgi_param指令是数组型的，和普通指令相同的是：内层替换外层；和普通指令不同的是：当在同级多次使用的时候，是新增而不是替换。换句话说，如果在同级定义两次SCRIPT_FILENAME，那么它们都会被发送到后端，这可能会导致一些潜在的问题，为了避免此类情况，便引入了一个新的配置文件。

因此不再建议大家使用以下方式（搜了一下，网上大量的文章，并且nginx.conf的默认配置也是使用这种方式）：
```
fastcgi_param  SCRIPT_FILENAME    $document_root$fastcgi_script_name;
include fastcgi_params;
```
而使用最新的方式：
```
include fastcgi.conf;
```

## 2.5.uwsgi_params
与`fastcgi_params`一样，**传递哪些服务器变量**，只有前缀不一样，以`uwsgi_param`开始而非`fastcgi_param`。

## 2.6.scgi_params
与`fastcgi_params`一样，**传递哪些服务器变量**，只有前缀不一样，以`scgi_params`开始而非`fastcgi_param`。

## 2.7.koi-utf、koi-win、win-utf
这三个文件都是与编码转换映射文件，用于在输出内容到客户端时，将一种编码转换到另一种编码。
```
koi-win： charset_map koi8-r < – > windows-1251

koi-utf： charset_map koi8-r < – > utf-8

win-utf： charset_map windows-1251 < – > utf-8
```
koi8-r是斯拉夫文字8位元编码，供俄语及保加利亚语使用。在Unicode未流行之前，KOI8-R 是最为广泛使用的俄语编码，使用率甚至起ISO/IEC 8859-5还高。这3个文件存在是因为作者是俄国人的原因。

## 附：可能遇到的错误和一些帮助信息

### 1.1编译pcre错误
```
libtool: compile: unrecognized option `-DHAVE_CONFIG_H'
libtool: compile: Try `libtool --help' for more information.
make[1]: *** [pcrecpp.lo] Error 1
make[1]: Leaving directory `/usr/local/src/pcre-8.34'
make: *** [all] Error 2
```
![](\images\编译pcre错误.png)

解决办法：安装g++,别忘了重新configure
```
apt-get install g++
apt-get install build-essential
make clean
./configure
make
```

### 1.2 make出错
```
make: *** No rule to make target `build', needed by `default'.  Stop.
./configure: error: SSL modules require the OpenSSL library.
You can either do not enable the modules, or install the OpenSSL library
into the system, or build the OpenSSL library statically from the source
with nginx by using --with-openssl=<path> option.
```
按照第4步的安装方法或 
ubuntu下
```
apt-get install openssl
apt-get install libssl-dev
```
centos下
```
yum -y install openssl openssl-devel
```

## nginx编译选项
make是用来编译的，它从Makefile中读取指令，然后编译。

make install是用来安装的，它也从Makefile中读取指令，安装到指定的位置。

configure命令是用来检测你的安装平台的目标特征的。它定义了系统的各个方面，包括nginx的被允许使用的连接处理的方法，比如它会检测你是不是有CC或GCC，并不是需要CC或GCC，它是个shell脚本，执行结束时，它会创建一个Makefile文件。nginx的configure命令支持以下参数：


*   `--prefix=`_path_ 定义一个目录，存放服务器上的文件 ，也就是nginx的安装目录。默认使用 `/usr/local/nginx。`
*   `--sbin-path=`_path_ 设置nginx的可执行文件的路径，默认为_prefix_`/sbin/nginx`.
*   `--conf-path=`_path_ 设置在nginx.conf配置文件的路径。nginx允许使用不同的配置文件启动，通过命令行中的-c选项。默认为_prefix_`/conf/nginx.conf`.
*   `--pid-path=`_path_  设置nginx.pid文件，将存储的主进程的进程号。安装完成后，可以随时改变的文件名 ， 在nginx.conf配置文件中使用 PID指令。默认情况下，文件名为 _prefix_`/logs/nginx.pid`.
*   `--error-log-path=`_path_ 设置主错误，警告，和诊断文件的名称。安装完成后，可以随时改变的文件名 ，在nginx.conf配置文件中 使用 的error_log指令。默认情况下，文件名为 _prefix_`/logs/error.log`.
*   `--http-log-path=`_path_ 设置主请求的HTTP服务器的日志文件的名称。安装完成后，可以随时改变的文件名 ，在nginx.conf配置文件中 使用 的access_log指令。默认情况下，文件名为 _prefix_`/logs/access.log`.
*   `--user=`_name_ 设置nginx工作进程的用户。安装完成后，可以随时更改的名称在nginx.conf配置文件中 使用的 user指令。默认的用户名是nobody。
*   `--group=`_name_ 设置nginx工作进程的用户组。安装完成后，可以随时更改的名称在nginx.conf配置文件中 使用的 user指令。默认的为非特权用户。
*   `--with-select_module` `--without-select_module` 启用或禁用构建一个模块来允许服务器使用select()方法。该模块将自动建立，如果平台不支持的kqueue，epoll，rtsig或/dev/poll。
*   `--with-poll_module` `--without-poll_module` 启用或禁用构建一个模块来允许服务器使用poll()方法。该模块将自动建立，如果平台不支持的kqueue，epoll，rtsig或/dev/poll。
*   `--without-http_gzip_module` — 不编译压缩的HTTP服务器的响应模块。编译并运行此模块需要zlib库。
*   `--without-http_rewrite_module`  不编译重写模块。编译并运行此模块需要PCRE库支持。
*   `--without-http_proxy_module` — 不编译http_proxy模块。
*   `--with-http_ssl_module` — 使用https协议模块。默认情况下，该模块没有被构建。建立并运行此模块的OpenSSL库是必需的。
*   `--with-pcre=`_path_ — 设置PCRE库的源码路径。PCRE库的源码（版本4.4 - 8.30）需要从PCRE网站下载并解压。其余的工作是Nginx的./ configure和make来完成。正则表达式使用在location指令和 ngx_http_rewrite_module 模块中。
*   `--with-pcre-jit` —编译PCRE包含“just-in-time compilation”（1.1.12中， pcre_jit指令）。
*   `--with-zlib=`_path_ —设置的zlib库的源码路径。要下载从 zlib（版本1.1.3 - 1.2.5）的并解压。其余的工作是Nginx的./ configure和make完成。ngx_http_gzip_module模块需要使用zlib 。
*   `--with-cc-opt=`_parameters_ — 设置额外的参数将被添加到CFLAGS变量。例如,当你在FreeBSD上使用PCRE库时需要使用:`--with-cc-opt="-I /usr/local/include。`.如需要需要增加 `select()支持的文件数量`:`--with-cc-opt="-D FD_SETSIZE=2048".`
*   `--with-ld-opt=`_parameters_ —设置附加的参数，将用于在链接期间。例如，当在FreeBSD下使用该系统的PCRE库,应指定:`--with-ld-opt="-L /usr/local/lib".`

## Nginx配置
修改配置文件/etc/nginx/nginx.conf，并添加下面的内容：
```
http {
    upstream demo_uri  { 
        server 127.0.0.1:8080; 
        server 127.0.0.1:8081; 
    }
```

```
server {
        listen       80;
        server_name  localhost;
        # charset koi8-r;
        # access_log  logs/host.access.log  main;
        location / {
            root   html;
            index  index.html index.htm;
            proxy_pass  http://demo_uri;
        }
```

此起启动nginx，需要使用：
```
# pwd
/usr/local/nginx
# ./nginx -c nginx.conf
```

>注意：这里后台服务器获取用户的请求URL格式是： http://demo_uri/project1_view/index.html 这类格式 
所以想要获取真实请求URL，需要设置反向代理：
```
location ~ /project1-* {
            root   html;
            index  index.html index.htm;
            proxy_pass  http://demo_uri;
            #以下是一些反向代理的配置可删除.
            proxy_redirect off;
            #后端的Web服务器可以通过X-Forwarded-For获取用户真实IP
            proxy_set_header Host $host:$server_port;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        }
```
接下来，安装两个Tomcat实例。由于我们是在同一台服务器上做的演示，要让两台Tomcat不发生冲突，需要修改第二个Tomcat实例的端口号。由 于Nginx配置为non-sticky运行模式，对每个请求采用的是Round-robin负载均衡方式，这意味着它会为每个请求都抽奖一个新会话。

接着，下载并安装Redis。步骤省略，很简单。

最后，我们需要配置Tomcat，让Tomcat把会话Session保存到Redis数据库。

我们要使用tomcat-redis-session-manager这样的第三方库，主页见：

https://github.com/jcoleman/tomcat-redis-session-manager
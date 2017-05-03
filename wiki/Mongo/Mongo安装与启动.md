## 下载
MongoDB提供了linux平台上32位和64位的安装包，你可以在官网下载安装包。

下载地址：http://www.mongodb.org/downloads

![](\images\Mongo下载.png)

## 安装

下载完成后，在你安装的目录下解压zip包。

## 创建数据库目录
MongoDB的数据存储在data目录的db目录下，但是这个目录在安装过程不会自动创建，所以你需要手动创建data目录，并在data目录中创建db目录。

注意：请将data目录创建于根目录下(/)。

![](\images\数据库路径.png)

## 命令行中运行 MongoDB 服务
你可以在命令行中执行mongo安装目录中的bin目录下的mongod命令来启动mongdb服务。

![](\images\服务启动.png)

作为后台进程启动：
```
/usr/local/mongodb-3.4.3/bin$ sudo ./mongod --logpath /data/logs/mongo/ --fork
```
注意，`--fork`指定后台启动，并且一定要指定`--logpath`，否则启动失败。

## MongoDB后台管理 Shell
如果你需要进入MongoDB后台管理，你需要先打开mongodb装目录的下的bin目录，然后执行mongo命令文件。

MongoDB Shell是MongoDB自带的交互式Javascript shell,用来对MongoDB进行操作和管理的交互式环境。

当你进入mongoDB后台后，它默认会链接到 test 文档（数据库）：

![](\images\客户端启动.png)

## MongoDb web 用户界面
在比MongoDB服务的端口多1000的端口上，你可以访问到MondoDB的web用户界面。

如：如果你的MongoDB运行端口使用默认的27017，你可以在端口号为28017访问web用户界面。

![](\images\Mongo Web界面.png)


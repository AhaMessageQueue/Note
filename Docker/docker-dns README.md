docker-dns
----------

Automatic container DNS for [Docker][docker] in a single Python file.

Note that [docker-dns-rest][dns-rest] expands on this, adding a REST API to add and remove domain names dynamically, allowing multiple domain names to be associated with containers, either by name or container ID.  It also includes support for wildcards.

[docker]: http://github.com/docker/docker "Docker"
[dns-rest]: http://github.com/phensley/docker-dns-rest "docker-dns-rest"

Usage
-----

Run some containers:

```docker
% docker run -d --name foo ubuntu bash -c "sleep 600"
```

`docker run` Usage:	

```text
docker run [OPTIONS] IMAGE [COMMAND] [ARG...]

Run a command in a new container
```

- ubuntu 为镜像名称
- `bash -c "sleep 600"`为command
    
    - 用法: bash -c "cmd string"
    - 通常使用shell去运行脚本，两种方法:
        - `bash xxx.sh`
        - `bash -c "cmd string"`

Start up dockerdns:

```docker
% docker run -d --name dns -v /var/run/docker.sock:/docker.sock phensley/docker-dns \
--domain example.com
```

- docker run 命令使用 -v 标记来创建一个数据卷并挂载到容器里

Start more containers:

```docker
% docker run -d --name bar ubuntu bash -c "sleep 600"
```

Check dockerdns logs:

```text
% docker logs dns

2014-10-08T20:45:37.349161 [dockerdns] table.add dns.example.com -> 172.17.0.3
2014-10-08T20:45:37.351574 [dockerdns] table.add foo.example.com -> 172.17.0.2
2014-10-08T20:45:37.351574 [dockerdns] table.add bar.example.com -> 172.17.0.4
```

Query for the containers by hostname:

```text
% host foo.example.com 172.17.0.3

    Using domain server:
    Name: 172.17.0.3
    Address: 172.17.0.3#53
    Aliases:

    foo.example.com has address 172.17.0.2
```

`host` Usage: 

```text
host [-aCdlriTwv] [-c class] [-N ndots] [-t type] [-W time]
            [-R number] [-m flag] hostname [server]
```

Use dns container as a resolver inside a container:

```docker
% docker run -it --dns $(docker inspect -f '{{.NetworkSettings.IPAddress}}' dns) \
--dns-search example.com ubuntu bash
```

注意，这里不加`-d`参数，因为我们需要登录到该容器的`bash`命令行

```text
% docker inspect -f '{{.NetworkSettings.IPAddress}}' dns
172.17.0.2
```

```text
root@95840788bf08:/# ping dns.example.com
PING dns.example.com (172.17.0.2) 56(84) bytes of data.
64 bytes from 172.17.0.2: icmp_seq=1 ttl=64 time=0.050 ms
64 bytes from 172.17.0.2: icmp_seq=2 ttl=64 time=0.093 ms
64 bytes from 172.17.0.2: icmp_seq=3 ttl=64 time=0.091 ms
64 bytes from 172.17.0.2: icmp_seq=4 ttl=64 time=0.063 ms
64 bytes from 172.17.0.2: icmp_seq=5 ttl=64 time=0.088 ms
64 bytes from 172.17.0.2: icmp_seq=6 ttl=64 time=0.086 ms
```

当然还可以做如下修改：

```text
root@95840788bf08:/# cat /etc/resolv.conf
nameserver 172.17.0.3
search example.com
```

```text
root@95840788bf08:/# ping foo
PING foo.example.com (172.17.0.2) 56(84) bytes of data.
64 bytes from 172.17.0.2: icmp_seq=1 ttl=64 time=0.112 ms
64 bytes from 172.17.0.2: icmp_seq=2 ttl=64 time=0.112 ms
```


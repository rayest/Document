# Linux

> 基于个人服务器 centos-7.6

## 进程

```bash
$ ps aux
$ ps -ef | grep sshd | less
$ lsof | wc -l
$ lsof -i | grep tymetrobill
```

## openssl 和 openssh

* Linux 或者 macOS 生成随机密码

```bash
$ openssl rand -base64 12
```

* Linux 搭建 Sftp 服务器

  > 使用 openssh(ssh)
  >
  > 参考：https://my.oschina.net/u/593517/blog/968177

## 磁盘

* 查看磁盘各分区大小、已用空间等信息

```bash
$ df -h
```

## 安装

* 更改 yum 源为阿里云源，并使用 yum 安装 Redis

> 参考：https://www.jianshu.com/p/ebda253a8daa

* 使用 yum 安装 JDK1.8

> 参考：https://my.oschina.net/andyfeng/blog/601291

* 使用 yum 安装 Docker

```bash
$ yum list docker
$ sudo yum -y install docker.x86_64
$ systemctl start docker
```

* yum 安装 mysql

> 参考：https://qizhanming.com/blog/2017/05/10/centos-7-yum-install-mysql-57
>
> 添加远程登录用户时，admin 为数据库新用户，secret 为密码
>
> systemctl enable firewalld
>
> systemctl start firewalld

## 阿里云

* 暴露端口

> 在实例中找到·安全组规则·，设置端口范围 80/80、91/91、3306/3306等，授权对象 0.0.0.0/0
>
> 访问：http://ip:port

# Linux 网络

## network namespace

> 网络命名空间。是实现网络虚拟化的重要功能。它能创建多个隔离的网络空间，它们有独自的网络栈信息。不管是虚拟机还是容器，运行的时候仿佛自己就在独立的网络中

* 通过安装包 iproute2 安装 ip 命令工具

  > 参考：https://segmentfault.com/a/1190000009491002
  >
  > 参考：https://cizixs.com/2017/02/10/network-virtualization-network-namespace

```shell
# ip netns: 查看、添加、删除命名空间
$ ip netns help
Usage: ip netns list # 列出所有 network namespace，默认为空
       ip netns add NAME # 新增
       ip netns set NAME NETNSID
       ip [-all] netns delete [NAME] # 删除 network namespace
       ip netns identify [PID]
       ip netns pids NAME
       ip [-all] netns exec [NAME] cmd ...
       ip netns monitor
       ip netns list-id
 
# ip link：网卡等设备的操作 
$ ip link list

# 增加 veth pair：veth0 和 veth1
$ ip link add veth0 type veth peer name veth1

# 添加 bridge：br0
$ ip link add br0 type bridge

# 将 veth1 设置到 net1 命名空间内
$ ip link set veth1 netns net1

# 启动设备 veth0
$ ip link set dev veth0 up

# 删除设备 veth0
$ ip link delete veth0

# 为设备 br0 设置IP
$ ip addr add 172.16.222.101/24 dev br0

# 删除设备配置好的 IP
$ ip addr del 172.16.222.101/24 dev veth0

```


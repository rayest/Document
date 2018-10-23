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
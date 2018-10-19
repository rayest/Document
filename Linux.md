# Linux

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


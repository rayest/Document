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


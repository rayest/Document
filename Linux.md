# Linux

* 关于进程

```bash
$ ps aux
$ ps -ef | grep sshd | less
$ lsof | wc -l
$ lsof -i | grep tymetrobill
```

* Linux 或者 macOS 生成随机密码

```bash
$ openssl rand -base64 12
```


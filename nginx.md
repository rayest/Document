## nginx

> 在 centos 上安装 nginx 或者在 MacOS 上通过 brew 安装 nginx。
>
> 1. 确保 /usr/local/var/www 目录下有文件 index.html
> 2. 确保 /usr/local/etc/nginx/nginx.conf 配置 user root owner
> 3. 日志 /usr/local/var/log/nginx 为默认配置目录
> 4. 重启：brew services restart nginx
> 5. Linux 中查看防火墙开放的端口：firewall-cmd --list-ports 确保 80 端口开放可访问
> 6. Linux 开启端口：sudo firewall-cmd --add-port=80/tcp --permanent


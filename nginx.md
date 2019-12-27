## nginx

> 在 centos 上安装 nginx 或者在 MacOS 上通过 brew 安装 nginx。
>
> 1. 确保 /usr/local/var/www 目录下有文件 index.html
> 2. 确保 /usr/local/etc/nginx/nginx.conf 配置 user root owner
> 3. 日志 /usr/local/var/log/nginx 为默认配置目录
> 4. 重启：brew services restart nginx
> 5. Linux 中查看防火墙开放的端口：firewall-cmd --list-ports 确保 80 端口开放可访问
> 6. Linux 开启端口：sudo firewall-cmd --add-port=80/tcp --permanent

### nginx.conf

> nginx.conf 配置文件有三个组成部分：全局块、events 块、http 块

### 反向代理

> 1. 启动 tomcat 或者项目服务可访问接口如：127.0.0.1:8099
>
> 2. 在 /etc/hosts 中配置域名和IP映射如 ：192.168.124.20 www.rayest.com
>
> 3. 则可访问 www.rayest.com:8099
>
> 4. 修改 nginx. conf 配置文件
>
> 5. ```nginx
>    server {
>            listen       80;
>            server_name  192.168.124.20; # 客户端的请求 IP
>    
>            location / {
>                root   html;
>    	    			proxy_pass http://127.0.0.1:8099; # 代理的真正服务器及端口
>                index  index.html index.htm;
>            }
>      ```
>5. 访问 www.rayest.com

* 反向代理二

> ```nginx
> # another virtual host using mix of IP-, name-, and port-based configuration
> server {
>     listen       9000;
>     server_name  192.168.124.20;
> # 根据不同请求标识，过滤出处理服务器
>     location ~ /test1/ {
>         proxy_pass http://127.0.0.1:8080;
> 		}
> 
> 		location ~ /test2/ {
>     		proxy_pass http://127.0.0.1:8081;
> 		}
> }
> ```

### 负载均衡

> ```nginx
> upstream myserver {
>   server 192.168.124.20:8080 weight=5;
>   server 192.168.124.20:8081 weight=2;
> }
> 
> server {
>         listen       80;
>         server_name  192.168.124.20;
>         location / {
> 	    			proxy_pass http://myserver;
>         }
> }
> ```


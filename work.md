### 服务代码发布到测试服

- 确保代码为最新

> git pull
>
> git commit
>
> git push

- 待 gitlab 打包和镜像制作成功，自动生成并更新镜像 tag，如 master-1234

- 于 Registry 中获取最新的 tag，并替换 DevOps —> env-sj 项目下对应服务的 image 值，并执行

  > kubectl-test apply -f XXX-service.yaml

### 接口暴露

- 在 backend —> openresty 下找到相关服务，如 bank_service.conf，添加并配置 location
- git push 以生成镜像
- 删除 Kubernetes dashboard 部署中的 gate 网关服务
- 执行 kubectl-test apply -f svc-gateway.yaml

### kubectl 的使用ku

> Kubectl-test get pods -n test-sj
>
> kubectl-test apply -f svc-sj-bus.yaml
>
> kubectl-test logs -f -n test-sj sj-bus-service-f84df68cd-m4gjz
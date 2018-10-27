# 高性能MySQL

## MySQL 基准测试

### sysbench

* Centos 和 Mac OS安装参考：`https://github.com/akopytov/sysbench`
* CPU 基准测试：
  * `sysbench --test=cpu --cpu-max-prime=20000 run`
    * 测试计算素数直到某个最大值所需要的时间
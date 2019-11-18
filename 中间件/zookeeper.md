# Zookeeper

### 创建节点

```java
// 添加依赖
compile group: 'org.apache.zookeeper', name: 'zookeeper', version: '3.5.5'
compile group: 'org.apache.curator', name: 'curator-framework', version: '4.0.1'
compile group: 'org.apache.curator', name: 'curator-recipes', version: '4.0.1'
```

```java
package mobi.rayson.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;

@Slf4j
@Service
public class CuratorTestService implements CommandLineRunner {
    @Override
    public void run(String... args) {
        CuratorFramework client = CuratorFrameworkFactory
            .builder()
            .connectString("127.0.0.1:2181")
            .connectionTimeoutMs(6000)
            .sessionTimeoutMs(6000)
            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
            .build();

        try {

            log.info("开启客户端...");
            client.start();

            log.info("开始创建节点 z-node");
            client.create().forPath("/China");
            client.create().forPath("/America", "zhangsan".getBytes());

            // 创建一个初始内容为空的临时节点
            client.create().withMode(CreateMode.EPHEMERAL).forPath("/France");

            // 递归创建, /Russia是持久节点
            client.create()
           .creatingParentsIfNeeded()
           .withMode(CreateMode.EPHEMERAL)
           .forPath("/Russia/car", "haha".getBytes());


            /*
              异步创建节点
              注意:
              如果指定了线程池,那么相应的操作就会在线程池中执行,
              如果没有指定,那么就会使用Zookeeper的EventThread线程对事件进行串行处理
             */
            client
              .create()
              .withMode(CreateMode.EPHEMERAL)
              .inBackground((client1, event) -> System.out.println("当前线程:" + Thread.currentThread().getName() + ",code:" + event.getResultCode() + ",type:" + event.getType()), Executors.newFixedThreadPool(10)).forPath("/async-curator-my");

            client
              .create()
              .withMode(CreateMode.EPHEMERAL)
              .inBackground((client12, event) -> System.out.println("当前线程:" + Thread.currentThread().getName() + ",code:" + event.getResultCode() + ",type:" + event.getType())).forPath("/async-curator-zookeeper");

            // 获取节点内容
            byte[] data = client.getData().forPath("/America");
            System.out.println(new String(data));

            // 传入一个旧的 stat 变量, 来存储服务端返回的最新的节点状态信息
            byte[] data2 = client.getData().storingStatIn(new Stat()).forPath("/America");
            System.out.println(new String(data2));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
}

```



### 分布式锁

> 通过 zookeeper 实现分布式锁。源码解释参考：
>
> [http://www.dengshenyu.com/java/%E5%88%86%E5%B8%83%E5%BC%8F%E7%B3%BB%E7%BB%9F/2017/10/23/zookeeper-distributed-lock.html](http://www.dengshenyu.com/java/分布式系统/2017/10/23/zookeeper-distributed-lock.html)

```java
package mobi.rayson.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CuratorLockTestService implements CommandLineRunner {
    @Override
    public void run(String... args) {
        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectString("127.0.0.1:2181")
                .connectionTimeoutMs(6000)
                .sessionTimeoutMs(6000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();

        client.start();

        // 创建分布式锁
        final InterProcessMutex lock = new InterProcessMutex(client, "/locktest");

        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                private int id;

                public Runnable setArgs(int x) {
                    id = x;
                    return this;
                }

                @Override
                public void run() {
                    try {
                        // 加锁
                        lock.acquire();
                        // lock.acquire(5, TimeUnit.SECONDS) == true;
                        //-------------业务处理开始
                        System.out.println(System.currentTimeMillis() + ":当前抢票用户:" + id);
                        Thread.sleep(1000);
                        //-------------业务处理结束
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            // 释放
                            lock.release();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.setArgs(i), "t" + i).start();
        }
        try {
            Thread.sleep(100 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
}
```


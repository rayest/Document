## Arthas 

> Java 线上问题排查神器

```shell
# 下载 arthas-boot.jar 并启动
$ java -jar arthas-boot.jar

[INFO] arthas-boot version: 3.1.4
[INFO] Found existing java process, please choose one and hit RETURN.
* [1]: 42756 mobi.rayson.Application # 用于测试的应用
  [2]: 39908 org.jetbrains.idea.maven.server.RemoteMavenServer
  [3]: 2873
  [4]: 42557 org.gradle.launcher.daemon.bootstrap.GradleDaemon
1 # 选择应用测试的编号
[INFO] arthas home: /Users/lirui/.arthas/lib/3.1.7/arthas
[INFO] Try to attach process 42756 # 连接到测试应用
[INFO] Attach process 42756 success.
[INFO] arthas-client connect 127.0.0.1 3658

# 显示当前进程的信息:Memory堆信息/线程信息/运行时信息
[arthas@42756]$ dashboard 

# 显示所有线程
[arthas@44749]$ thread

# 显示 CPU 占用前5的线程
[arthas@44749]$ thread -n 5

# 反编译指定的类
[arthas@42756]$ jad mobi.rayson.film.FilmService

# 通过 watch 命令来查看函数的返回值。发送请求时，返回结果如下
[arthas@44749]$ watch mobi.rayson.film.FilmService getByName returnObj
Affect(class-cnt:1 , method-cnt:1) cost in 29 ms.
ts=2020-01-12 15:54:53; [cost=8.279203ms] result=@Film[
    id=@Integer[1],
    name=@String[Lee],
    director=@String[Anthony],
    releaseTime=null,
    createDate=null,
    updateDate=null,
]

# 查看当前JVM信息
[arthas@44749]$ jvm
-----------------------------------------------------------------------------------------------
 OPERATING-SYSTEM
-----------------------------------------------------------------------------------------------
 OS                                           Mac OS X
 ARCH                                         x86_64
 PROCESSORS-COUNT                             4
 LOAD-AVERAGE                                 2.5556640625
 VERSION                                      10.14.1

-----------------------------------------------------------------------------------------------
 THREAD
-----------------------------------------------------------------------------------------------
 COUNT                                        35
 DAEMON-COUNT                                 28
 PEAK-COUNT                                   36
 STARTED-COUNT                                61
 DEADLOCK-COUNT                               0

-----------------------------------------------------------------------------------------------
 FILE-DESCRIPTOR
-----------------------------------------------------------------------------------------------
 MAX-FILE-DESCRIPTOR-COUNT                    10240
 OPEN-FILE-DESCRIPTOR-COUNT                   206
 
 # 显示系统属性
 [arthas@44749]$ sysprop
 
 # 显示系统环境变量
 [arthas@44749]$ sysenv
 
 # 查看，更新VM诊断相关的参数
 [arthas@44749]$ vmoption
 
 # 查看logger信息，更新logger level
 [arthas@44749]$ logger
 
 # 查看类静态变量
 [arthas@44749]$ getstatic mobi.rayson.film.FilmService name
 
 # 打印类的详细信息
 [arthas@44749]$ sc mobi.rayson.film.FilmService -d -f
 class-info        mobi.rayson.film.FilmService
 code-source       /Users/lirui/Documents/code/project-for-java/spring-boot/spring-boot-docker/build/classes/java/main/
 name              mobi.rayson.film.FilmService
 isInterface       false
 isAnnotation      false
 isEnum            false
 isAnonymousClass  false
 isArray           false
 isLocalClass      false
 isMemberClass     false
 isPrimitive       false
 isSynthetic       false
 simple-name       FilmService
 modifier          public
 annotation        org.springframework.stereotype.Service
 interfaces
 super-class       +-java.lang.Object
 class-loader      +-sun.misc.Launcher$AppClassLoader@18b4aac2
                     +-sun.misc.Launcher$ExtClassLoader@6cd8737
 classLoaderHash   18b4aac2
 fields            name       filmMapper
                   type       mobi.rayson.film.FilmMapper
                   modifier   private
                   annotation javax.annotation.Resource
                   
# 打印方法详细信息
[arthas@44749]$ sm mobi.rayson.film.FilmService -d
 declaring-class   mobi.rayson.film.FilmService
 constructor-name  <init>
 modifier          public
 annotation
 parameters
 exceptions
 classLoaderHash   18b4aac2

 declaring-class  mobi.rayson.film.FilmService
 method-name      getByName
 modifier         public
 annotation
 parameters       java.lang.String
 return           mobi.rayson.film.Film
 exceptions
 classLoaderHash  18b4aac2

 declaring-class  mobi.rayson.film.FilmService
 method-name      exist
 modifier         public
 annotation
 parameters       java.lang.String
 return           boolean
 exceptions
 classLoaderHash  18b4aac2
 
 # 按类加载类型查看统计信息
 [arthas@44749]$ classloader
 name                                                numberOfInstances  loadedCountTotal
 sun.misc.Launcher$AppClassLoader                    1                  6149
 BootstrapClassLoader                                1                  3370
 com.taobao.arthas.agent.ArthasClassloader           1                  1145
 sun.reflect.DelegatingClassLoader                   116                116
 sun.misc.Launcher$ExtClassLoader                    1                  30
 javax.management.remote.rmi.NoCallStackClassLoader  2                  2
 sun.reflect.misc.MethodUtil                         1                  1
 
 # 方法执行监控：响应时间、失败率等信息

 [arthas@44749]$ monitor mobi.rayson.film.FilmService getByName -c 5
Press Q or Ctrl+C to abort.
Affect(class-cnt:1 , method-cnt:1) cost in 38 ms.
 timestamp            class              method     total  success  fail  avg-rt(ms)  fail-rate
-----------------------------------------------------------------------------------------------
 2020-01-12 16:26:18  mobi.rayson.film.  getByName  1      1        0     6.38        0.00%

 timestamp            class              method     total  success  fail  avg-rt(ms)  fail-rate
-----------------------------------------------------------------------------------------------
 2020-01-12 16:26:23  mobi.rayson.film.  getByName  1      1        0     5.23        0.00%

 timestamp            class              method     total  success  fail  avg-rt(ms)  fail-rate
-----------------------------------------------------------------------------------------------
 2020-01-12 16:26:28  mobi.rayson.film.  getByName  0      0        0     0.00        0.00%
 
 
 # 方法内部调用路径，并输出方法路径上的每个节点上耗时.用以排查接口调用响应慢时
 [arthas@44749]$ trace mobi.rayson.film.FilmService getByName
Press Q or Ctrl+C to abort.
Affect(class-cnt:1 , method-cnt:1) cost in 35 ms.
`---ts=2020-01-12 16:32:25;thread_name=http-nio-8080-exec-7;id=27;is_daemon=true;priority=5;TCCL=org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader@1e9c6689
    `---[3.922206ms] mobi.rayson.film.FilmService:getByName()
        `---[3.647221ms] mobi.rayson.film.FilmMapper:getByName() #25
```


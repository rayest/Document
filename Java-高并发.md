# 并行的世界

## 基本概念

* 同步
  * 同步方法调用一旦开始，调用者必须等到方法调用返回结束后，才能继续后续的行为
* 异步
  * 异步方法在另一个线程中执行，不会阻碍调用者的工作
  * 若异步调用有返回结果，则当异步调用真实完成时。则会通知调用者
* 并发
  * 多个任务交替执行，整个过程是交替执行的，系统会不停地在多个任务之间切换
* 并行
  * 同时执行，仅可能出现在拥有多个CPU的系统中
  * 一个CPU一次仅能执行一条指令
* 临界区
  * 表示公共资源或者共享数据，可以被多个线程使用，每一次只能有一个线程使用，其他线程需等待该资源的释放
* 阻塞
  * 线程等待资源释放而暂停工作
* 非阻塞
  * 线程正常执行，没有受到其他线程的影响而无法工作
* 死锁
  * 线程之间需要彼此占用的资源，导致彼此不能释放资源
* 饥饿
  * 线程无法获得需要的资源，导致一直无法继续执行
* 活锁
  * 线程之间过度谦让，主动将资源释放给别的线程，导致资源在线程之间来回跳动，而无一个线程能拿到所有的资源以正常执行

## 并发级别

* 临界区的存在，使得必须要控制多线程之间的并发，根据并发的策略，把并发的级别分为了阻塞、无饥饿、无障碍、无锁、无等待

# 并行程序基础

## 线程的基本操作

### 线程周期状态

> new、runnable、running、blocked、terminated

### 新建线程

> ```java
> public class ThreadDemo1 implements Runnable{
>     
>     private static Logger logger = LoggerFactory.getLogger(ThreadDemo1.class);
> 
>     @Override
>     public void run() {
>         System.out.println("实现 Runnable 接口，处理业务逻辑");
>     }
> 
>     public static void main(String[] args) {
>         logger.info("新建线程并启动");
>         Thread thread = new Thread(new ThreadDemo1());
>         thread.start();
>     }
> }
> ```

* `new Thread()` 并实现 `Runnable `接口

### 终止线程

* `new Thread().stop()` 用于暴力终止线程，已被摒弃
* 可能会在多线程数据写入时，导致数据写入不一致
* `stop`会直接终止线程，并且会立即释放该线程所持有的锁，该锁用于维持对象的一致性。当数据写入一部分时，被直接终止，写对象遭到破坏，会被另外的线程读到该不一致的数据
* 可以自行定义线程终止的条件，当满足该条件时，就退出线程

### 线程中断

* 不会使线程立即退出，而是会给该线程发送通知，该线程收到该通知后，会根据具体情况自行处理

* 线程中断涉及到的三个方法

* ```java
  public void Thread.interrupt() // 中断线程，设置中断标志位
  public boolean Thread.isInterrupted() // 判断线程是否被中断，检查中断标志位
  public static boolean Thread.interrupted() // 是否被中断，并清除当前中断状态，清除中断标志位
  ```

* 三个方法都涉及到`中断标志位`

  * 如果要通过该方法退出线程，需要先中断线程，并在判断其中断状态后，执行退出操作

### Sleep 函数

* 使当前线程休眠若干时间，当休眠中的线程被中断时，会抛出中断异常`InterruptedException`

### Wait 函数和Notify 函数

* `wait()`和`notify()`是 Object 类的内部方法，可供任何对象调用。实例对象调用该方法后，当前线程就会停止继续执行，而在该对象上等待，直到其他线程调用 `notify()`方法为止，线程才继续执行
* 线程调用`wait()`方法后会进入对象的等待队列，该队列中可能有多个线程同时等地一个对象
* 线程调用`notify()`方法后，对象会在等待队列中随机选择一个线程，并将其唤醒
* `notifyAll()`会唤醒等待队列中的所有线程
* `wait()`的调用必须在`synchronized`语句中，`wait()`和`notify()`都需要首先获得目标对象的一个监视器
* `wait()`执行后会释放这个监视器
* `wait()`方法也可以让线程等待若干时间，且该方法会释放目标对象的锁，而`sleep`方法不会释放任何资源

### Suspend 和 resume

* 挂起和继续执行。被 Thread 废弃的方法
* 一对相反的操作，被挂起的线程只有在等到resume后才会继续执行
* `suspend`会将线程挂起，但是并不释放任何锁资源，导致该锁无法被任何线程使用

### Join 和 yield

* `join() 和 join(long millis)`：一个线程的输入依赖于另一个线程的输出，需要等到依赖线程的执行完毕，才继续执行，如：

```java
public static void main(string[] args) {
    System.out.println("开始执行 main 主线程");
    Thread t1 = new Thread();
    t1.start(); //  在主线程中启动子线程
    t1.join(); // 主线程需要等待子线程执行完再继续执行
    System.out.println("继续执行 main 主线程");
}
```

* 一个线程加入另一个线程，如主线程中加入了一个新线程
* `join` 的本质是让调用线程`wait`在当前线程对象实例上
* `yield()`方法被执行后，会使当前线程让出 CPU，之后还会进行 CPU 的争夺
* 如果一个线程不太重要或者优先级比较低且占用较多的 CPU，可以尝试 `yield`

## Volatile 与 Java 内存模型（JMM）

* volatile 等关键字用于告诉虚拟机，该处比较特殊，需要注意
* `易变的、不稳的`，用于标记某些字段，以使 JVM 采取特殊手段，保证该字段的可见性和有序性
* 保证了操作的原子性，但不能代替锁

### 内存模型

* 计算机执行时，每条指令都是在 CPU 中执行的，就会涉及到数据读取和写入，为缓解 CPU 指令执行速度和从内存中操作数据的差异性，在 CPU 中引入了高速缓存

* 程序运行过程中，数据的操作都是在高速缓存中进行的，在运算结束后。再将高速缓存中的数据刷新到主存中

* 单线程运行没有问题，但是多线程运行时就会出现问题

* 在多核 CPU 中，每条线程可能运行于不同的 CPU，因此每条线程拥有自己的高速缓存，这将导致数据存在缓存不一致的问题。即一个变量在多个 CPU 中都存在缓存，而这些缓存的结果可能不一样

* 2 种解决方法，都是硬件层的方式
  * 在总线加 LOCK# 锁
    * 阻塞其他 CPU 对其他部件的访问，使只能有一个 CPU 能使用这个变量的内存。即在等待含有共享变量的代码全部执行完毕，其他 CPU 才能从变量所在的内存读取变量
    * 在锁住总线期间，其他 CPU 无法访问内存，效率低
  * 通过缓存一致性协议
    * 保证每个缓存中使用的共享变量的副本是一致的
    * 当写数据时，若操作的变量是共享变量，在其他 CPU 中也存在该变量的副本，会发出信号通知其他的 CPU 将该变量的缓存设置为无效状态，当该 CPU 需要读取这个变量时，发现自己缓存中的缓存变量是无效的，就会从内存中重新获取
  
* 并发编程常见的三个问题是：原子性问题、可见性问题、有序性问题（指令重排）

* 指令重排
  * 如在某些方法中有多条语句，代码按一定顺序编写好之后，JVM实际执行时的顺序是不一定的，即发生了指令重排
  * 指令重排是处理器为了提高程序运行效率，自身对输入代码进行优化的，它不保证程序中各个语句的执行先后顺序和代码中的一致，但会保证最终执行的结果是一致的
  * 不会影响到单线程的执行结果，但是会影响到多线程的执行结果
  
* Java 内存模型
  * 屏蔽了各个硬件平台和操作系统的内存访问差异，实现在各个平台下一致的内存访问结果
  * 也有缓存一致性问题和指令重排问题
  * 规定所有的变量都存在主存（物理内存）中，每个线程有自己的工作内存即类似于高速缓存
  * 线程对变量的所有操作都必须在工作内存中进行，而非在主存中进行，线程之间不能互相访问工作内存
  * `y=x`语句其实不是原子性操作，包含两个操作，一个是读取 `x`操作，一个是将`x`写入内存操作
  * JMM 只保证了基本读取和赋值是原子性操作，但是更大范围的原子性操作需要 synchronized 或者 Lock 来实现。两种方式能够保证任意时刻只有一个线程可以操作该端代码，不存在原子性问题
  * volatile 关键字保证可见性
    * 保证被该关键词修饰的共享变量在被修改后立即更新到主存，当有其他线程需要时。从内存重新读取
    * synchronized 和 Lock 也可以保证可见性，在释放锁之前会将对变量的修改刷新到主存中
  * volatile 和 synchronized、lock 可以保证变量的有序性
  
* volatile
  * 保证了不同线程对该变量进行操作时的可见性，即一个线程修改了某个变量的值，这新值对其他线程来说是立即可见的
  * 禁止进行指令重排序，在一定程度上保证有序性
  * 没办法保证对变量的操作的原子性
  * 自增操作是不具备原子性的，它包括读取变量的原始值、进行加1操作、写入工作内存。那么就是说自增操作的三个子操作可能会分割开执行
  
  ### synchronized
  
  * 锁住的是对象而不是代码块
  * 没有对象被 new 出来时，锁住的是类， this 代表的是当前对象。同一个对象中其他的非同步方法，在被调用时，不管该对象是否被锁住，都可以访问。

## 线程组

* 当线程数量很多时，如几十个上百个，可以使用线程组，将相同功能的线程放在一个线程组里

## 守护线程 daemon

* 是系统的守护者，完成一些系统性服务，如垃圾回收线程和JIT线程。与之对应的是用户线程
* 用户线程完成一些业务工作，当用户线程全部结束，守护线程守护的对象已不存在，整个应用程序应该退出
* 只有守护线程时，JVM会自动退出

## 线程优先级

* 根据任务优先级对不同线程设置不同的优先级，使其更可能优先处理相关业务
* 有效范围在 1 和 10 之间，数字越大则优先级越高

## 线程安全和 synchronized

* 并行程序的重点是线程安全
* 三种用法
  * 指定加锁对象，对给定的对象加锁
  * 直接作用于实例方法，对当前实例加锁
  * 直接作用于静态方法，在当前类上加锁

#  JDK 并发包

## 同步控制

### 重入锁

* 重入锁可以完全替代 synchronized 关键字
  * 中断响应
  * 锁申请等待限时：过时不请求
  * 公平锁：先到先到，终会得到。维护成本、性能低，默认是非公平的

### 重入锁的搭档：Condition 条件

* `Object.wait()`和`Object.notify()`是配合 `synchronized` 关键字使用的

* condition 是配合重入锁使用的，等待和通知类似于 Object。Condition 接口的方法：

* ```java
  void await() throws InterruptedException;
  void awaitUninterruptibly();
  long awaitNanos(long nanosTimeout) throws InterruptedException;
  boolean await(long time, TimeUnit unit) throws InterruptedException;
  boolean awaitUtil(Date deadline) throws InterruptedException;
  void signal();
  void signalAll();
  ```

### 允许多个线程同时访问：信号量（Semaphore）

* 对锁的扩展，与内部锁 synchronized 和可重入锁 ReentrantLock 不同，信号量可指定多个线程同时访问同一个资源

```java
public Semaphore(int permits); // 准入数，许可
public Semaphore(int permits, boolean fair); // 是否公平
```

### ReadWriteLock 读写锁

* 读写锁允许多个线程同时读，使读与读之间真真并行
* 考虑到数据完整性，读写之间和写写之间需要相互等待
* 读读不互斥，其余情况都需要阻塞
* 读次数远大于写操作时，读写锁的性能最优

### 倒计时器：CountDownLatch

* 门栓。锁住线程，用于控制线程等待，让某一个线程等待直到倒计时器结束，再开始继续执行
* **强调一个线程等n个线程完成某件事情。而 CyclicBarrier 是多个线程互等，等大家都完成**
* 如：裁判线程等待运动员线程都跑到终点，统计排名

### 循环栅栏：CyclicBarrier

* 与 countDownLatch 类似，用于实现线程间的计数等待
* 栅栏即障碍物，阻止线程继续执行；循环即为该计时器可以反复使用
* **强调的是n个线程，大家相互等待，只要有一个没完成，所有人都得等着**
* 如：n个羊线程，都到齐了，再一起出去吃草

### 线程阻塞工具类：LockSupport

* 可以在线程内任意位置让线程阻塞
* `park()` 方法可以阻塞当前线程，实现了一个限时等待

## 线程复用：线程池

* 多线程的频繁使用可能会对系统产生不利的影响，因此需要控制线程的数量
* 创建和销毁线程需要时间，线程会占用内存空间，线程回收也会给GC带来压力
* 数据库连接池
  * 使用数据库连接池维护一些数据库连接，让他们长期维持在一个激活状态
  * 当需要使用数据库时，并不是需要创建一个新的连接，而是从连接池中获得一个可用的连接即可
  * 当需要关闭连接时，并不真的将连接关闭，而是将该连接归还给连接池
  * 节约了创建和销毁的时间
* 线程池亦如是
* `newFixedThreadPool()`
  * 返回固定线程数量的线程池，线程池中的数量始终不变
  * 当有一个新的任务提交时，若线程池中有空闲的线程，则立即执行
  * 若没有，新任务会被暂存在一个任务列表中，待有空闲线程时，再处理任务列表中的任务
* `newSingleThreadExecutor()`
  * 返回只有一个线程的线程池
  * 若有多余的任务被提交到线程池，则该任务会被保存到一个任务列表中，待线程空闲时，按先进先出的顺序执行列表中的任务
* `newCachedThreadPool()`
  * 返回可根据实际情况调整线程数量的线程池，线程数量不固定
  * 若有空闲线程可以使用，则优先使用可复用的线程
  * 若所有的线程都在工作，则在有新任务提交时，会创建新的线程处理
  * 当所有的线程都执行完毕后，将线程返还给线程池
* `newSingleThreadScheduledExecutor()`
  * 返回`ScheduledExecutorService()`对象，线程池大小为1。在给定时间执行某任务的功能
* `newScheduledThreadPool()`
  * 返回`ScheduledExecutorService()`对象，线程池可以指定线程数量

### 拒绝策略

* 当任务数量超过了系统实际承载能力时，需要用到拒绝策略
* 线程池中线程用完，等待队列中也已经排满
* 4 种JDK内置的拒绝策略
  * AbortPolicy：直接抛出异常，阻止系统正常工作
  * CallerRunsPolicy：只要线程池未关闭，该策略直接在调用者线程中，运行当前被抛弃的任务
  * DiscardOldestPolicy：丢弃最老一个请求，即即将被执行的一个任务，并尝试再次提交当前任务
  * DiscardPolicy：默默丢弃无法处理的任务，不予任何处理

### 自定义线程创建：ThreadFactory

* 线程池创建线程时，会调用接口 ThreadFactory 的 newThread() 方法

### 分而治之：Fork/Join 框架

* `fork()`开启线程，使得系统多一个执行分支。`join()`表示等待
* JDK 的 ForkJoinPool 线程池
* 两个线程会相互帮助。如果一个线程完成任务，另一个线程未完成，则会从未完成任务的线程的任务队列中拿出一个任务进行处理
* 线程帮助另一个线程时，线程试图从另一个线程的任务队列的底部开始拿数据，与另一个线程相反。避免数据竞争

## JDK 内部的轮子

### 并发集合

* copyOnWriteArrayList
* ConcurrentHashMap
* ConcurrentLinkedQueue
* BlockingQueue
* ConcurrentSkipListMap

### 高效读取

* 当读操作远大于写操作时，希望读操作快，而写操作慢一些影响不大
* CopyOnWriteArrayList 对读取不用加锁，仅在写与写之间需进行同步等待，读操作性能就会较大提升，因为不会阻塞
* 即写时复制。仅在需要写的时候，复制一份数据作为副本，写完之后将修改完的数据替换原来的数据，而不影响读

# 锁的优化

* 避免死锁、减小锁粒度、锁分离

## 悲观锁和乐观锁

* 悲观锁和乐观锁是一种设计思想，而不是一种实在锁。悲观锁认为资源总会被占用，乐观锁则认为资源基本不会被抢占。MySQL 实现悲观锁是在查询语句末尾添加 FOR UPDATE；乐观锁则是通过为记录添加版本 version 等字段进行控制，读取 -> 比较 -> 写（CAS）。JAVA 实现悲观锁就是 sync 同步，乐观锁就是原子类（内部通过CAS实现）
* 乐观锁：认为读多写少

## 提高锁性能

### 减小锁持有的时间

* 只有在必要的时候才进行同步加锁

### 减小锁粒度

* 如 HashMap 是不安全的，对其进行整个加锁的话，可以但是锁的了粒度太大
* ConcurrentHashMap 内部默认细分了 16 个段，每一个段是较小的 HashMap。当需要对其进行添加操作时，并不是将整个 HashMap 都加锁，而是在得知该新加的项需要添加到哪一个段中时，对该段进行加锁，以完成 put 操作
* 在多线程中，如果多个线程的 put 操作是针对不同的段，则可实现真正的并行
* 问题是：在需要取得全局锁如计算 HashMap 大小 size 时，需要先获得所有锁的段，然后求和，此时的 ConcurrentHashMap 的 size() 性能就会差于同步的 HashMap

### 读写分离锁来替代独占锁

- 如读写锁 ReadWriteLock 可以提高系统性能，替代了独占锁是减小粒度的一种特殊情况
- 是对系统功能点的分割

### 锁分离

* 如 `LinkedBlockingQueue` 是基于链表的，其 `take()` 方法和` put() `方法分别实现了从队列中取得数据和向队列中增肌数据的功能，都对当前队列进行了修改，但是两个操作分别作用于队列的前端和尾端，两者其实不冲突
* 为使二者实现真正并发，使用了两把不同的锁分离了 `take()` 和 `put()` 操作

### 锁粗化

* 当对同一个锁过于频繁的请求、同步和释放，也会消耗系统资源，降低性能。过犹不及
* JVM 在遇到一连串连续地对同一锁不断进行请求和释放的操作时，便会把所有的锁操作整合成对锁的一次请求，减少对锁的请求同步次数，即为锁的粗化
* 如有些循环中，循环内请求锁可以在外层实现

## JVM 对锁优化的努力

### 锁偏向

* 是对加锁操作的优化
* 若一个线程获得了锁，锁就进入了偏向模式。当该线程再次请求锁时，不需再做任何同步操作，节省了锁申请的时间
* 适合于几乎没有锁竞争的场合，而锁竞争激烈的场合效果不佳
* 可是通过 JVM 参数开启偏向锁：`-XX:+UseBiasedLocking`

### 轻量级锁

* 若偏向锁失效，虚拟机并不会立即挂起线程。会使用轻量级锁的优化手段
* 简单地将对象头部作为指针，指向持有锁的线程堆栈的内部，来判断一个线程是否持有对象锁
* 若轻量级锁获得成功，则线程可以顺利进入临界区
* 若轻量级锁加锁失败，表示其他线程抢到了锁，则当前线程的锁请求会膨胀为重量级锁

### 自旋锁

* 在锁膨胀后，虚拟机为避免线程真实地在操作系统层面挂起，还会进行自旋锁操作
* 当前线程无法获得锁，系统会进行一次赌注：假设在不久的将来线程可以获得该锁
* 虚拟机会让当前线程做几个空循环，记过若干次循环后，如果可以获得锁，则顺利进入了临界区
* 若仍然获取不到锁，才会真实地将线程在操作系统层面挂起

### 锁消除

* 一种更彻底的锁优化。JVM 在 JIT 编译时，通过对运行上下文的扫描，去除不可能存在共享资源的竞争的锁，以节省毫无意义的请求锁时间
* JDK 内部一些 API 内部实现原理可能用到了锁，而实际开发时仅仅使用了其 API，如 StringBuffer 等
* 涉及到的一项关键技术是逃逸分析
* 逃逸分析
  * 观察一个变量是否会逃离某一个作用域
  * 若函数或者方法内部的一个变量没有直接返回出去，则没有逃逸该函数，可以将内部的加锁操作去除
  * 若函数或者方法内部的一个变量本身被返回出去，即其他线程有可能访问该变量，则已经逃逸了，不能将锁去除
  * 必须在` -server`模式下进行，可以使用`-XX:+DoEscapeAnalysis `参数打开逃逸分析
  * 使用`-XX:+EliminateLocks`参数打开锁消除

## ThreadLocal
* 是一个线程的局部变量，亦即只有当前的线程可以访问

* 关注 TheadLocal 的`set()`和`get()`方法

* ```java
  public void set(T value){
      Thread t = Thread.currentThread();
      ThreadLocalMap map = getMap(t);
      if(map != null){
          map.set(this, value);
      }else{
          createMap(t, value);
      }
  }
  ```

* 在 `set()` 时，首先获得当前线程对象，然后通过 `getMap()`拿到线程的 `ThreadLocalMap`，并将 `value`设置到其中，该 map 设置的 key 为当前对象。而`ThreadLocalMap`类似于 HashMap。  

* `get()`时，即将数据从 map 中拿出来

* ```java
  public T get() {
      Thread t = Thread.currentThread();
      ThreadLocalMap map = getMap(t);
      if (map != null) {
          ThreadLocalMap.Entry e = map.getEntry(this);
           if (e != null) {
           @SuppressWarnings("unchecked")
           T result = (T)e.value;
           return result;
           }
      }
  return setInitialValue();
  }
  ```

* 可以看到Z值`value`是保存在 ThreadLocalMap 中的键值对`Entry`中的

## 无锁

* 就并发控制而言，锁是一种悲观的策略。无锁是一种乐观的策略，假设没有冲突不需要等待
* 比较交换的技术：CAS，以鉴别线程冲突，一旦检测到有冲突，就重试当前操作直到没有冲突为止
* 无为而治

### 与众不同的并发策略：CAS

* 比锁更加复杂，但是由于其自身的非阻塞性，线程间的相互影响较小，没有锁及其调度开销
* CAS：compare and swap。包含了三个参数：V、E、N
* V：表示要更新的变量
* E：表示预期值
* N：表示新值
* 仅当V=E，才将V值设为N，否则说明了已经有其他线程作了更新，当前的线程什么都不做
* 最后，CAS返回当前V的真实值
* 当多个线程同时使用CAS操作一个变量时，只有一个会胜出并更新成功，其余均会失败
* 失败的线程不会被挂起，而是仅被告知失败，仍然可以再次尝试

### 无锁的线程安全整数：AtomicInteger

* JDK 并发包中的 atomic 包，实现了一些直接使用CAS操作的线程安全类型
* 与 Integer 不同的是，是可变且是线程安全的，对其进行修改等任何操作，都是 CAS 指令进行的

### 无锁的对象引用：AtomicReference

* 对普通的对象引用。保证在修改对象引用时的线程安全

# 并行模式与算法

## 不变模式

* 依靠对象的不变性，确保在多线程环境中依然始终保持内部状态的一致性和正确性
* 一旦被创建，内部状态和数据将不会再改变
* 去除所有setter方法以及所有修改自身属性的方法
* 将所有属性设置为私有，并用final标记
* 确保没有子类可以重载修改它的行为
* 有一个可以创建完整对象的构造函数
* 如 `java.lang.String`等类

## Future 模式

* 核心是异步调用
* 不会立即返回实际需要的数据，但是会返回一个凭据契约，可以凭借该契约重新获得需要的信息数据

# 补充

* 程序出现异常时，锁会被释放 --> 要注意异常的处理。catch 住可以不释放锁
* volatile 只能保证可见性，synchronized 既保证了可见性和原子性

# 各个击破之 JUC

> 代码参考 concurrency 仓库

## lock

### LockSupport

1. LockSupport 类可以阻塞当前线程以及唤醒指定被阻塞的线程
2. 主要是通过 park() 和 unpark(thread) 方法来实现
3. 每个线程都有一个许可(permit)，permit 只有两个值 1 和 0，默认是 0
   1. 当调用 unpark(thread) 方法，就会将线程的许可 permit 设置成1(注意多次调用 unpark 方法，不会累加，permit 值还是1)。
   2. 当调用 park() 方法，如果当前线程的 permit 是 1，那么将 permit 设置为 0，并立即返回。如果当前线程的 permit 是0，那么当前线程就会阻塞，直到别的线程将当前线程的 permit 设置为 1。park 方法会将 permit 再次设置为 0，并返回。

### ReentrantLock

### 线程池

```java
package mobi.rayson;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {

    private ExecutorService threadPool = new ThreadPoolExecutor(4, 8, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));

    public static void main(String[] args) {
        Main main = new Main();
        String result = main.getResultFromAllServers();
        System.out.println("result: " + result);
    }

    private String getResultFromAllServers() {
        Future<String> futureFromA = threadPool.submit(this::getFromServerA);
        Future<String> futureFromB = threadPool.submit(this::getFromServerB);
        Future<String> futureFromC = threadPool.submit(this::getFromServerC);
        try {
            String a = futureFromA.get();
            String b = futureFromB.get();
            String c = futureFromC.get();
            return a + b + c;
        } catch (InterruptedException | ExecutionException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    private String getFromServerC() {
        System.out.println("从 C 服务获取数据");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return "c";
    }

    private String getFromServerB() {
        System.out.println("从 B 服务获取数据");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return "b";
    }

    private String getFromServerA() {
        System.out.println("从 A 服务获取数据");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return "a";
    }
}
```


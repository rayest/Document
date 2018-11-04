* 源代码参见：`JVM-code`

# 原码、反码、补码

* 原码：整数绝对值大小转换为二进制数
  * int 类型`(4字节32位)`的整数`3`在计算机中存储形式是`00000000 0000000 00000000 00000011`
  * -3 在计算机中存储形式是：`11111111 11111111 11111111 11111101`
  * 正数转换成二进制后最高位为 0，负数的二进制最高位为 1
* 反码：由原码按位取反操作取得，原码1变0，0变1
  * 3 的反码：`11111111 11111111 11111111 11111100`
* 补码：反码加1
  * 3 的补码：`11111111 11111111 11111111 11111101`
* 正数在计算机中按原码存储，负数按相应正数的补码存储

# JVM 基本结构

## Java 虚拟机的架构

* 类加载系统负责从文件系统或者网络中加载`class`信息，加载的类信息被放在一个称为`方法区`的内存块
* 方法区：除了要存放类的加载信息，还存放运行时常量信息(如：字符串字面量、数字常量)
* `Java`堆：`Java`堆在`JVM`启动时创建，是最主要的内存工作区域。几乎所有的`Java`对象实例都存放在堆中。堆空间是所有线程共享的，也是与`Java`应用密切相关的内存区间
* 直接内存：是在`Java`堆外的、直接向系统申请的内存空间。读写性能高于`Java`堆
* 垃圾回收：对方法区、堆和直接内存进行回收。由垃圾回收系统在后台默默完成
* `Java`栈：每一个`JVM`线程都一个私有的`Java`栈。随线程创建而被创建，保存局部变量、方法参数
* 本地方法栈：类似于`Java`栈，用于本地方法调用，而`Java`栈用于`Java`方法的调用
* `PC`寄存器：即`Program Counter` 寄存器。也是每个线程私有的空间
* 执行引擎：负责执行虚拟机的字节码

## 设置虚拟机参数

* 代码：`/JVM-code/Demo1.java` 注意仅仅是一个纯文本，无依赖亦无包

```bash
$ javac Demo1.java # 编译。
$ java -Xmx32M Demo1 "Hello"  # 修改虚拟机参数并传入参数"Hello"
```

## Java 堆

*  Java 堆是完全自动化管理的，通过垃圾回收机制，垃圾对象会被自动回收，而不需要显示手动释放
* 不同的垃圾回收机制使得Java堆拥有不同的结构
* 常见的一种构成是将整个java堆分为新生代和老年代。新生代存放新生对象或年龄不大的对象，老年代存放老年对象

```java
public class SimpleHeap {
    
    private int id;
    
    public SimpleHeap(int id) {
        this.id = id;
    }

    public void show(){
        System.out.println("Id is: " + id);
    }

    public static void main(String[] args) {
        SimpleHeap s1 = new SimpleHeap(1);
        SimpleHeap s2 = new SimpleHeap(2);
        s1.show();
        s2.show();
    }
}
```



![image](https://github.com/rayest/Document/raw/master/images/堆-栈-方法区关系.png)

## Java 栈

* Java 堆和程序数据密切相关，而 Java 栈则和线程执行密切相关。线程执行的基本行为是函数调用，每次调用的数据都是通过 Java 栈传递的

* 类似于数据结构中的栈，Java 栈亦是一块先进后出的数据结构，只支持进栈和出栈操作，其中保存的主要内容是栈帧

* 每一次函数调用都会有一个对应的栈帧被压入Java栈，函数执行结束，该函数对应的栈帧会被弹出Java栈。如一个执行操作遇到多个函数依次调用，则有相应的栈帧依次压入Java栈，当前正在执行的函数对应的栈帧处于栈顶，并且保存着当前函数的局部变量、中间运算结果等数据

* 注意：正是由于函数每次调用都会声称对应的栈帧，从而占用一定的空间。因此，若栈空间不足则会导致函数不能继续调用，程序无法继续运行。当请求的栈深度大于最大可用栈深度时，会抛出错误`StackOverflowError`

  ```bash
  $ java -Xss256K  # JVM参数"-Xss"用于设置指定线程的最大栈空间。栈值越大函数可以支持的调用次数越多
  ```

* 当函数返回时（正常返回 return；或者抛出异常 throw），栈帧都会从Java栈中弹出

* 一个栈帧中，至少包括局部变量表、操作数栈、帧数据三个部分

  ![image](https://github.com/rayest/Document/raw/master/images/栈帧与函数.png)

### 局部变量表

* 用于保存函数的参数以及局部变量。此中的变量只在当前函数调用中有效，函数调用结束、函数栈帧就会被销毁，因此其中的局部变量表的数据也会被销毁
* 若函数的参数与局部变量过多，会导致局部变量表过大，每一次函数调用就会占用更多的栈空间，最终导致函数嵌套调用次数减少

### 操作数栈

* `保存操作数的栈`。保存计算过程中的中间结果、作为计算过程中变量的临时存储空间
* 先进后出、只支持入栈和出栈。当输入 iadd 指令时，操作数栈会弹出两个整数进行加法计算，计算结果重新入栈

![image](https://github.com/rayest/Document/raw/master/images/操作数栈.png)

### 帧数据区

* 用以支持常量池解析、正常方法返回和异常处理
* 保存了访问常量池的指针
* 异常处理表：用于在发生异常的时候找到处理异常的代码

```javascript
Exception table
from	to	  target	type
  4		16	    19		any
 19		21	    19		any	
```

* 解释：
  * 以上表示了在字节码偏移量 4~16 字节可能抛出异常。如遇到异常，则跳到字节码偏移量 19 处执行
  * 当方法抛出异常时，虚拟机会查找类似的异常进行处理，若无法在异常处理表中找到合适的处理方法，则结束当前函数调用，返回调用函数，并在调用函数中抛出相同的异常，并查找调用函数的异常处理表进行处理

### 栈上分配

* 是一种优化技术。对于线程私有的对象将其打散分配在栈上，而非堆上

* 栈上的对象不需要垃圾回收器的介入，在函数调用结束后自行销毁

* 技术基础是`逃逸分析`

* 逃逸分析

  * 判断对象的作用于是否有可能逃逸出函数体

  ```java
  private static User user; // user 为逃逸对象
  public static void alloc(){
  	user = new User();
      user.id = 1;
      user.name = "lee";
  }
  
  public static void anotherAlloc(){
      User u = new User(); // u 为非逃逸对象(没有返回出去，也没有任何形式的公开)
      u.id = 2;            // 虚拟机有可能将其分配在栈上而非堆上
      u.name = "rayest";
  }
  ```

  * 逃逸分析示例

  ```java
  public class OnStackTest {
      public static class User {
          public int id = 0;
          public String name = "";
      }
  
      public static void alloc() {
          User user = new User();
          user.id = 1;
          user.name = "lee";
      }
  
      public static void main(String[] args) {
          long a = System.currentTimeMillis();
          for (int i = 0; i < 100000000; i++) {
              alloc();
          }
          long b = System.currentTimeMillis();
          System.out.println(b - a);
      }
  }
  ```

  ```bash
  $ java -server -Xmx10M  -XX:+DoEscapeAnalysis -XX:+PrintGC OnStackDemo
  ```

  * 以上示例 OnStackTest 在运行时，累计分配空间将到达1.5G左右，若堆空间小于该值，必然会发生GC
  * `-Xmx10M` 指定堆空间大小为 10 M，`-XX:+DoEscapeAnalysis`开启逃逸分析。执行时间不超过 **6** 毫秒
  * 若`-XX:-DoEscapeAnalysis`则关闭逃逸分析，执行时间有 **1000** 多毫秒，并打印很多 GC 日志
  * 但是栈空间比较小，对于大对象也不适合在栈上分配

## 方法区

* 是一块所有线程共享的内存区域。用于保存系统的类信息：类的字段、方法、常量池
* 方法区的大小决定了系统可以保存多少个类，定义太多类导致方法区溢出，虚拟机也会抛出内存溢出错误
* 在 JDK1.6 和 JDK1.7 中，方法区又可理解为**永久区**。较大的永久区可以保存较多的类信息

```bash
$ java -XX:permSize=5M
$ java -XX:MaxPermSize=64M
```

* **JDK1.8** 中移除了永久区，取而代之的是**元数据区**，是一块堆外的直接内存
* 若不指定元数据区大小，虚拟机会耗尽所有的可用系统内存

# 常用Java虚拟机参数

## 跟踪调试参数

* 通过 JVM 提供的用于跟踪系统状态的参数，分析和排查问题

### 跟踪垃圾回收

* Java 自动进行垃圾回收，若垃圾回收过于频繁或者垃圾回收占用太长的CPU时间，需要排查

```bash
$ java -XX:+PrintGC  # 凡是遇到 GC，就会输出日志 
$ java -XX:+PrintGCDetails # 详细的 GC 信息
$ java -XX:PrintHeapAtGC
$ java -XX:+PrintGCApplicationStoppedTime  # 打印程序由于GC而产生的停顿时间
......
```

```javascript
若输出：[GC 2048K->424K(9728K), 0.0007689 secs]
表示：一次GC，GC前堆空间使用量为2MB，GC后堆空间使用量为424KB，当前可用堆空间总和9728KB，时间是GC花费的时间
```

### 类加载/卸载跟踪

* 系统加载的类位于文件系统中，以Jar的形式打包或者class文件的形式存在。而动态代理或者AOP生成的类无法通过文件系统找到，可以通过JVM虚拟机提供的类加载和卸载跟踪参数进行跟踪

```bash
$java -XX:+TraceClassUnloading
$java -XX:+TraceClassLoading
```

### 系统参数查看

* 查看当前应用或者系统使用了哪些JVM参数

```bash
$ java -XX:+PrintVMOptions  # 打印了虚拟机接收到的命令行显示参数
$ java -XX:+PrintCommandLineFlags  # 打印了虚拟机接收到的命令行显示参数、以及隐式参数(默认的参数)
$ java -XX:+PrintFlagsFinal  # 打印所有的系统参数及其当前取值
```

## 堆的配置参数

### 最大堆和初始堆

```bash
$ java -Xms5m  # JVM启动时，会分配一块初始堆空间，其大小通过该命令指定
$java -Xmx20m  # JVM耗尽初始堆空间时，会对堆空间进行扩展，扩展上限为最大堆空间，其大小通过该命令指定
```

### 新生代

```bash
$ java -Xmn1m  # 新生代的大小一般为整个堆空间的 (1/4 ~ 1/3)
$ java -XX:SurvivorRatio=eden/from(eden/to)  # 设置新生代中 eden 空间和 from/to 空间的比例关系
$ java -XX:NewRatio=老年代/新生代
```

```java
public class NewSizeDemo {
    public static void main(String[] args) {
        byte[] b = null;
        for (int i = 0; i < 10; i++) {
            b = new byte[1 * 1024 * 1024];
        }
    }
}
```

```bash
$ java -Xmx20m -Xms20m -Xmn1m -XX:SurvivorRatio=2 -XX:+PrintGCDetails NewSizeDemo  # ①
$ java -Xmx20m -Xms20m -Xmn7m -XX:SurvivorRatio=2 -XX:+PrintGCDetails NewSizeDemo  # ②
$ java -Xmx20m -Xms20m -Xmn15m -XX:SurvivorRatio=8 -XX:+PrintGCDetails NewSizeDemo  # ③
```

* 上述示例：连续向系统请求 10MB 空间（每次 1MB）。配置三种不同的JVM参数，GC日志不同（JVM行为不同）
* 三种方式，分别设置新生代大小为1、7、15MB，eden/from(to) 大小分别为2、2、8。（详细解释查阅《实战Java虚拟机》）
* 说明：在大多数情况下对象首先被分配在eden区，在一次新生代回收后，若对象还活着，则会进入from或者to区。之后每经过一次新生代回收，对象如果还活着，其年龄就会加 1。当年龄达到一定条件后，进入老年代
* 总之：基本策略是，尽可能将对象留在新生代，减少老年代GC次数

### 堆溢出处理

* 堆空间不足时，可能抛出内存溢出错误（out of memory）

```bash
$ java -XX:+HeapDumpOnOutOfMemoryError  # 在内存溢出时，导出整个堆信息
$ java -XX:HeapDumpPath  # 导出堆的存放路径
```

## 非堆内存的参数配置 

### 方法区

* 或曰：永久区（JDK1.8 代之以元数据区）

```bash
$ java -XX:PermSize  # 初始永久区 (JDK1.8 无)
$ java -XX:MaxPermSize  # 最大永久区 (JDK1.8 无)
$ java -XX:MaxMetaspaceSize  # 元数据区最大可用值 JDK1.8
```

* 栈配置

```bash
$ java -Xss256K  # 配置指定线程的栈大小
```

### 直接内存配置

* 在 NIO 被广泛使用后，直接内存的使用也更加普遍。直接内存跳过了 JAVA 堆，使 Java 程序可以直接访问原生堆，合理的使用可以在一定程度上加快了内存空间的访问速度 
* 默认为最大堆空间（-Xmx）。当直接内存使用量达到配置值时，就会触发垃圾回收，若垃圾回收不能释放足够空间时，仍旧会抛出 out-of-memory 错误
* **适合于申请次数少，访问频繁的场合**

```bash
$ java -XX:MaxDirectMemorySize  # 最大可用直接内存
```

## 虚拟机的工作模式

* Client 和 Server 模式
* Java 虚拟机支持两种运行模式，默认情况当前计算机系统会自动选择合适的运行模式
* server 模式的启动比较慢，因为其会收集更多的系统性能信息，使用更复杂的优化算法对程序进行优化，稳定之后的 server 模式执行速度会快于 client 模式
* server 模式适合于后台长期运行的系统。64 位系统中的虚拟机更倾向于使用 **server** 模式

# 垃圾回收概念与算法

## 垃圾回收

* 对内存中不会再使用的对象进行销毁，以减少内存空间的占用

## 垃圾回收算法

### 引用计数法

* 对于一个对象A，若有任何一个对象引用了A，则A的引用计数器就加1，当引用失效时，引用计数器就减1。当对象A的引用计数器为0，则对象A就不能再被使用

* 实现时，为每一个对象配备一个整型的计数器

* 无法处理循环引用的情况（**因此，Java的垃圾回收器中，没有使用该算法**）

  * 如垃圾对象之间相互引用，垃圾回收器无法识别

  ![image](https://github.com/rayest/Document/raw/master/images/循环引用.png)

* 引用计算器要求在每次因引用产生和消除的时候，伴有加法和减法操作，影响系统性能

* 可达对象：通过根对象进行引用搜索，最终可以达到的对象

* 不可达对象：通过根对象进行引用搜索，最终没有被引用到的对象

### 标记清除法

* 将垃圾回收分为两个阶段：标记阶段和清除阶段
* 标记阶段：通过根节点，标记所有从根节点开始的可达对象，未被标记的就是未被引用的垃圾对象
* 清除阶段：清除所有未被标记的对象
* 回收后的空间可能是不连续的，会产生大量的空间碎片

### 复制算法

* 将原有的内存空间分为两块A、B，每次只使用其中一块
* 垃圾回收时，将正在使用的内存A中的存活对象复制到未使用的内存块B中，此时对象在B内存中连续存储
* 之后，清除正在使用的内存块A中的剩余所有对象，交换两个内存的角色，完成垃圾回收
* Java 中**新生代**串行垃圾回收器中，即使用了该复制思想
  * 新生代分为 eden 空间、from 空间和 to 空间三部分
  * 其中 from 和 to 空间可视为用于复制的两块大小相同、地位相等、且可进行角色互换的空间块，用于存放未被回收的对象
* 新生代：存放年轻对象的空间，刚刚创建或者经历垃圾回收次数不多的对象
* 老年代：存放老年对象的堆空间，经历过多次垃圾回收依然存活的对象
* 当存活对象少、垃圾对象多时比较高效，常用于新生代

### 标记压缩法

* 老年代的回收算法
* 先从根节点开始标记所有可达对象，然后将所有存活的对象压缩到内存一端，再清理边界外所有的空间
* 避免了空间碎片的产生，以及不需要额外的相同大小的内存空间，性价比较高

### 分代算法

* 上述的每一种算法都各有自己的特点，适合于不同条件下的垃圾回收
* 分代算法根据每块内存区间的特点，使用不同的回收算法
* 新生代：用于存放新创建的对象，新生代中的对象朝生夕死，大部分会很快被回收，适合于复制算法
* 老年代：可采用标记压缩或者标记清除法
* 新生代回收频次高，耗时短；老年代回收频次少，耗时长
* 卡表：一种数据结构，用以支持高效率的新生代的回收

### 分区算法

* 将整个堆空间划分为连续的不同小空间，每一个小空间独立使用、独立回收
* 堆空间越大，GC时所需要的时间就越长，即产生的停顿越长
* 大空间被分为多个小空间后，每次合理地回收若干个小空间，以减少停顿时间

## 判断可触及性

* 垃圾回收的基本思想是：判断每一个对象的可达性，即是否能够从根节点访问到该对象；如果可以访问到，怎说明该对象正在被使用，否则该对象已经不被使用了，理论上需要回收不被使用的对象
* 但是，某些情况下会出现无法触及到的对象复活，此时该对象就不能被回收
* 可触及性的三种状态：可触及的、可复活的、不可触及的

### 对象的复活

* 对象有可能在 `finalize()`函数中复活，`finalize`不是一个好的资源释放方式，不推荐使用

### 引用和可触及性的强度

* 4个级别的引用：强引用、软引用、弱引用、虚引用
* 强引用就是程序中最常用到的引用类型，强引用的对象是可触及的，不会被回收；其余三个引用的对象在一定条件下是可以被回收的

```java
StringBuffer str = new StringBuffer("Hello World");  // 假设在函数体内运行
```

* 局部变量 str 会被分配在栈上，而对象 StringBuffer 实例会被分配在堆上。str 指向 StringBuffer 实例所在的堆空间
* str 可以操作该实例，是 StringBuffer 实例的强引用
* 强引用可以直接访问目标对象、所指向的对象不会被回收、可能导致内存泄漏而抛出错误

### 软引用

* 通过`java.lang.ref.SoftReference`类实现
* 在堆空间不足时会被回收

### 弱引用

* 在GC时，若发现弱引用，不管系统堆空间是否不足，都会进行对象回收
* 但是由于垃圾回收线程通常优先级较低，并一定能立刻发现持有弱引用的对象。因此，弱引用可能存在较长时间

### 虚引用

* 和没有引用几乎是一样的。随时都有可能被回收

## 垃圾回收时的停顿现象

* `Stop-The-World`
* 垃圾回收器进行内存清理，为了使垃圾回收高效地执行，大部分情况会要求系统进入短暂的停顿状态，即终止所有应用线程的执行，以避免在垃圾回收过程中新垃圾的产生。即`Stop-The-World(STW)`

# 垃圾收集器和内存分配

* 垃圾回收器类型、特点及使用；对象在内存中的分配和回收

## 串行回收器

* 是指使用单线程进行垃圾回收的回收器。即每次回收时，串行回收器只有一个工作线程
* 垃圾回收时，Java 应用程序中的线程都要暂停，以等待垃圾回收的完成
* 可以在新生代和老年代使用

### 新生代串行回收器

* 使用复制算法，大多数情况下性能优异

```bash
$ java -XX:+UseSerialGC  # 指定使用新生代串行回收器和老年代串行回收器
```

### 老年代串行回收器

* 使用标记压缩法。在堆空间较大的应用中，GC导致的停顿时间较长

```bash
$ java -XX:+UseSerialGC  # 新生代、老年代都使用串行回收器
$ java -XX:+UseParNewGC  # 新生代使用 ParNew 回收器，老年代使用串行回收器
$ java -XX:+UseParallelGC:  # 新生代使用 ParallelGC 回收器，老年代使用串行回收器 
```

## 并行回收器

* 多个线程同时进行垃圾回收，适合于并行能力较强的计算机系统

### 新生代 ParNew 回收器

* 工作在新生代的垃圾回收器。只是将串行回收器改为多线程化，其余诸如回收策略、算法和参数与串行回收器一样

```bash
$ java -XX:+UseParNewGC
$ java -XX:+UseConcMarkSweepGC  # 新生代使用 ParNew 回收器，老年代使用 CMS
$ java -XX:ParallelGCThreads  # 指定线程数量，一般设为等于系统 CPU 值
```

### 新生代 ParallelGC 回收器

* 使用复制算法。与 ParNew 回收器一样。多线程、独占式收集器。但更关注系统吞吐量

```bash
$ java -XX:+UseParallelGC  # 新生代使用 ParallelGC 回收器，老年代使用串行回收器 
$ java -XX:+UseParallelOldGC  # 新生代使用 ParallelGC 回收器，老年代使用 ParallelOldGC 回收器

$ java -XX:MaxGCPauseMillis  # 设置最大垃圾收集停顿时间
$ java -XX:GCTimeRatio  # 设置吞吐量，0 ~ 100

$ java -XX:+UseAdaptiveSizePolicy  # 打开自适应 GC 策略。各参数被系统自动调整为较为合适的状态
```

### 老年代 ParallelOldGC 回收器

* 标记压缩法、多线程并发、关注吞吐量

## CMS 回收器

* 一心多用。主要关注于系统停顿时间。Concurrent Mark Sweep，使用的是标记清除算法，多线程并行完成垃圾回收

### 工作步骤

* 标记清除算法
* 初始标记和重新标记是独占系统的，其余步骤是可以和用户线程并发进行的
* ![image](https://github.com/rayest/Document/raw/master/images/CMS工作流程图.png)
* 标记：标出需要回收的对象
* 并发清理：在标记完成之后，正式回收垃圾对象
* 并发重置：垃圾回收后，重新初始化CMS数据结构和数据

```bash
$ java -XX:+UseConcMarkSweepGC  # 启用 CMS 垃圾回收器
$ java -XX:ConcGCThreads  # 设置并发线程数量
$ java -XX:ParallelCMSThreads  # 设置并发线程数量
```

* 在 CMS 回收过程中，应用程序还在继续执行，仍会产生新的垃圾对象，其在当前的 CMS 中无法被回收清除。因此，应确保在 CMS 回收过程中，应用程序还有足够的可用内存。所以，可以设置堆内存在使用到一定阈值时，便进行回收，从而确保垃圾回收和应用程序同时运行

```bash
$ java -XX:CMSInitiatingOccupancyFraction  # 设置CMS回收阈值，默认68。表示老年代空间使用到68%时回收
```

* 例如：若内存增长缓慢，则可以设置一个稍大的阈值，以降低CMS的触发率；反之，若内存增加很快，则可以降低阈值，以避免频繁触发老年代串行收集器
* CMS 采取标记清除法，在垃圾回收后，可能产生较多的空间碎片。使得虽然仍有较大的内存空间，但是不连续问题会造成额外不必要的被迫垃圾回收，因此需要在 CMS 之后进行内存压缩

```bash
$ java -XX:+UseCMSCompactAtFullCollection  # 开启内存压缩整理，在CMS垃圾回收完成之后
$ java -XX:CMSFullGCsBeforeCompaction  # 设置进行一定次数的CMS回收后，进行一次内存压缩
```

## G1 回收器

* 属于分代垃圾回收器，区分年轻代和老年代，有 eden 代和 survivor 代
* 并行性、并发性、分代GC、空间整理( 减少空间碎片)、可预见性（可选取部分区域进行回收）

### G1 内存划分和收集过程

* 内存划分：G1 收集器将堆进行分区，划分为很多个小区域，每次收集时，只收集其中的几个区域

![image](https://github.com/rayest/Document/raw/master/images/G1回收器区域.png)

* 收集过程：新生代 GC、并发标记周期、混合收集、或者可能继续 Full GC

### G1 的新生代 GC

* 主要回收 eden 区和 survivor 区
* 一旦 eden 区被占满，新生代 GC 即会启动。只回收 eden 和 survivor 区，其中 eden 区被全部回收

### G1 的并发标记周期

* 初始标记：标记从根节点直接可达的对象，应用程序需要停止
* 跟区域扫描：扫描由 survivor 区直接可达的老年代区域并标记
* 并发标记：扫描整个堆的存活对象并标记，并回收部分对象
* 重新标记：应用程序停顿，对并发标记的结果进行修正和补充
* 独占清理：停顿，计算各个区域的存活对象和GC回收比例，识别可供混合回收的区域
* 并发清理：识别并清理完全空闲的区域

### 混合回收

* 并发标记阶段清理了一小部分对象。并发标记之后，可以明确哪些区域有较多的垃圾对象。混合回收阶段会优先回收垃圾比例较高的区域
* 既会执行正常的年轻代 GC，也会选取一些被标记的老年代区域进行回收

### G1 相关参数

```bash
$ java -XX:+UseG1GC  # 启用 G1 回收器
$ java -XX:MaxGCPauseMillis  # 最大目标停顿时间
$ java -XX:ParallelGCThreads  # 并行 GC 的工作线程数
$ java -XX:InitiatingHeapOccupancyPercent  # 达到设置的整个堆使用率时，触发并发标记周期的执行
```

## 对象内存分配和回收

### 对象何时进入老年区

* 初创在 eden 区：一般情况，创建的对象会被分配新生代的 eden 区（eden：伊甸园，人类开始居住的地方）。如果没有GC 的介入，该区中的对象不会离开
* 老年对象进入老年区：对象年龄由对象GC的次数决定，新生代到了一定GC次数即为老年代
* 大对象进入老年代：对象体积大使得新生代无法容纳该对象，将会直接晋升到老年代

### 在 TLAB 上分配对象

* 线程本地分配缓存，一个线程专用的内存分配区域，是线程的一块私有内存
* TLAB本身占用eden区空间。默认是开启的。TLAB空间的内存非常小，缺省情况下仅占有整个Eden空间的1%

# 性能检测工具

## Linux/MacOS

### 显示系统整体资源使用

```bash
$ top # 观察系统各个进程对 CPU 的占用情况以及内存使用


$ vmstat 1 3  # 每秒采样一次，采样3次。查看内存、交互分区、IO操作、上下文切换、始终中断、CPU使用
[root@iZbp11jt0i73lffge9yew8Z ~]# vmstat 1 3
procs -----------memory----------         ---swap--  ----io---- -system--  ------cpu-----
 r  b     swpd   free    buff    cache     si   so    bi    bo   in   cs   us  sy id   wa  st
 1  0      0     185272  192136  2523036    0    0     4    28   14    5   0   0  99   0   0
 0  0      0     185296  192136  2523036    0    0     0     8   363  448  0   0  100  0   0
 0  0      0     185296  192136  2523036    0    0     0     0   351  441  0   0  99   0   0
 

$ iostat 1 3  # CPU 概述和磁盘详细的I/O信息。以判断系统是否有大量的I/O操作
[root@iZbp11jt0i73lffge9yew8Z ~]# iostat 1 2
Linux 3.10.0-693.2.2.el7.x86_64 (iZbp11jt0i73lffge9yew8Z) 	11/04/2018 	_x86_64_	(2 CPU)

avg-cpu:  %user   %nice %system %iowait  %steal   %idle
           0.37    0.00    0.20    0.07    0.00   99.36

Device:            tps    kB_read/s    kB_wrtn/s    kB_read    kB_wrtn
vda               3.36         7.22        55.74   10965957   84640584

avg-cpu:  %user   %nice %system %iowait  %steal   %idle
           0.00    0.00    0.00    0.50    0.00   99.50

Device:            tps    kB_read/s    kB_wrtn/s    kB_read    kB_wrtn
vda               2.00         0.00        12.00          0         12


$ pidstat -p 20306 1 3 -u -t  # -p 进程号；-u 对CPU监控；-t 线程级别
[root@iZbp11jt0i73lffge9yew8Z ~]# pidstat -p 20306 -u -t
Linux 3.10.0-693.2.2.el7.x86_64 (iZbp11jt0i73lffge9yew8Z) 	11/04/2018 	_x86_64_	(2 CPU)

12:12:24 PM   UID      TGID       TID    %usr %system  %guest    %CPU   CPU  Command
12:12:24 PM   994     20306         -    0.02    0.01    0.00    0.03     1  java
12:12:24 PM   994         -     20306    0.00    0.00    0.00    0.00     1  |__java
12:12:24 PM   994         -     20308    0.00    0.00    0.00    0.00     0  |__java
12:12:24 PM   994         -     20310    0.00    0.00    0.00    0.00     1  |__java
12:12:24 PM   994         -     20311    0.00    0.00    0.00    0.00     1  |__java
12:12:24 PM   994         -     20317    0.00    0.00    0.00    0.00     1  |__java

$ pidstat -p 20306 1 3 -d -t  # -d 对磁盘监控
[root@iZbp11jt0i73lffge9yew8Z ~]# pidstat -p 20306 -d -t
Linux 3.10.0-693.2.2.el7.x86_64 (iZbp11jt0i73lffge9yew8Z) 	11/04/2018 	_x86_64_	(2 CPU)

12:13:13 PM   UID      TGID       TID   kB_rd/s   kB_wr/s kB_ccwr/s  Command
12:13:13 PM   994     20306         -      0.04      0.09      0.00  java
12:13:13 PM   994         -     20306      0.00      0.00      0.00  |__java
12:13:13 PM   994         -     20308      0.04      0.00      0.00  |__java
12:13:13 PM   994         -     20310      0.00      0.00      0.00  |__java
12:13:13 PM   994         -     20311      0.00      0.00      0.00  |__java
12:13:13 PM   994         -     20317      0.00      0.00      0.00  |__java
12:13:13 PM   994         -     20318      0.00      0.00      0.00  |__java

$ pidstat -r -p 20306 1 5  # 监控指定进程的内存
[root@iZbp11jt0i73lffge9yew8Z ~]# pidstat -r -p 20306 1 5
Linux 3.10.0-693.2.2.el7.x86_64 (iZbp11jt0i73lffge9yew8Z) 	11/04/2018 	_x86_64_	(2 CPU)

12:14:47 PM   UID       PID  minflt/s  majflt/s     VSZ    RSS   %MEM  Command
12:14:48 PM   994     20306      0.00      0.00 3612964 547032  14.09  java
12:14:49 PM   994     20306      0.00      0.00 3612964 547032  14.09  java
12:14:50 PM   994     20306      0.00      0.00 3612964 547032  14.09  java
12:14:51 PM   994     20306      0.00      0.00 3612964 547032  14.09  java
12:14:52 PM   994     20306      0.00      0.00 3612964 547032  14.09  java
Average:      994     20306      0.00      0.00 3612964 547032  14.09  java


$ jstat -gc 1360  # 与GC相关的堆信息
$ jstat -class -t 1360 1000 2  # 类加载器信息。-t时间戳；1360进程号；1000毫秒；2两次
$ jstat -gccapacity 1360  # 各个代的当前大小以及各个代的最大值和最小值
$ jstat -gccause 1360  # 最近一次GC的原因以及当前GC的原因
$ jstat -gcnew 1360  # 新生代信息
$ jstat -gcnewcapacity 1360  # 新生代各个区大小信息
$ jinfo 
$ jmap
$ jstack
$ jcmd -l 
$ jps
# jConsole 图形化界面
$ jvisualvm  # 自动启动图形化界面
```

# 分析 Java 堆

## 内存溢出

* 出现于内存空间耗尽的时候：堆溢出、直接内存溢出、永久区溢出(JDK1.8无永久区的概念)

### 堆溢出

* 是 Java 程序中最重要的内存空间，大量的对象都直接分配在堆上。因此，最有可能是堆溢出的区间，原因是大量的对象占据了堆空间，且这些对象都是强引用，导致无法回收

### 直接内存溢出

* Java 的 NIO 操作支持通过代码直接向操作系统申请一块内存空间。申请速度慢，但是访问速度快
* 但是直接内存没有托管给虚拟机处理，若使用不当则会容易触发直接内存溢出

```java
 ByteBuffer.allocateDirect(1024 * 1024);  // java.nio.ByteBuffer。直接向系统申请内存空间
```

### 过多的线程导致 OOM

* 每一个线程的开启都要占用系统内存，线程的栈空间也是在堆外分配的
* 建议分配较小的堆空间，以预留更多的内存用于线程的创建
* 或者减小每一个线程占用的内存空间，`-Xss` 指定线程的栈空间（但是有可能造成栈溢出）

### 永久区溢出

* 用于存放类的元数据区域（或称为元数据区）
* 若系统定义了过多的类，有可能导致永久区的溢出（如可以动态代理产生很多类）
* 增加 MaxPermSize 值、减少系统类的数量、使用 ClassLoader 合理装载各个类并定期进行回收

### GC 效率低下引起的 OOM

* GC 时间是否超过了 98%、老年代释放的内存是否小于 2%、eden 区释放的内存是否小于 2%，是否连续5次GC都出现上述情况。只有满足以上所有条件，虚拟机才会出现 OOM 错误

## String 在虚拟机中的实现

### String 对象的特点

* 不变性、针对常量池的优化、类的 final 定义

* 不变性

  * String 对象一旦生成则不会再改变
  * 可以提高多线程的访问性能。对象不可变，因此对于所有线程都是只读的（因为无法写入更改）。多线程访问时，不加同步也不会造成数据不一致，减少了系统开销
  * 若要对其进行修改，只能产生新的字符串以替代

* 针对常量池的优化

  * 当两个 String 对象拥有相同的值时，他们只引用常量池中的同一个拷贝
  * 若同一个字符串反复出现时，可以大幅度节省内存空间

  ```java
  String s1 = new String("hello");
  String s2 = new String("hello");
  System.out.println(s1 == s2);  // false
  System.out.println(s1 == s2.intern());  // false。intern() 返回字符串在常量池中的引用
  System.out.println("hello" == s2.intern());  // true
  ```

* 类的 final 定义

  * 使得 String 对象不会有任何子类

### String 常量池的位置

* 在虚拟机中，字符串常量存放于常量池中，该区间在堆中进行管理

# 锁与并发


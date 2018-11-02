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
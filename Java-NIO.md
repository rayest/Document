

# 简介

## I/O 概念

### 缓存区操作

* 是 I/O 的基础。I/O 即将数据移入或者移出缓冲区
* 进程执行 I/O 操作，即向操作系统发出请求。让其把缓冲区里的数据排干(写)，或者用数据把缓冲区填满(读)

![image](https://github.com/rayest/Document/raw/master/images/I:O缓存区操作.png)

* 读操作是进程通过向系统发出读请求，使用 `read()`系统调用。内核随即向磁盘控制硬件发出命令，要求其从磁盘读取数据。磁盘控制器将数据从磁盘写入内核缓冲区，内核再把数据从**内核空间**的临时缓冲区拷贝到**用户空间**的缓冲区

* 用户空间：常规进程所在的区域。JVM 即为常规进程，常驻于用户空间，不能直接访问硬件设备

* 内核空间：操作系统所在区。能与设备控制器通讯，控制着用户区域进程的状态

* 而所有 I/O 都直接或者间接通过内核空间

* 注意：

  * 当进程请求 I/O 操作的时候，执行一个系统调用将控制权交给内核。内核采取相应的方式找到该进程需要的数据，并把数据输送到用户空间指定的缓冲区
  * 内核会对磁盘读取的数据进行高速缓存或者预读取，如果缓冲区已经存在进程需要的数据，则不需再从磁盘读取
  * 用户空间和内核空间：硬件不能直接访问用户空间；用户空间可能请求任意大小的数据块，而磁盘是基于块存储的，操作的是固定大小的数据块，内核负责数据的分解与再组合

* 发散/汇聚

  * 进程只需一个系统调用，就能把一连串缓冲区地址传递给操作系统。然后，内核就可以顺序填充或者排干多个缓冲区，读的时候就把数据**发散**到多个用户缓冲区，写的时候再从多个缓冲区**汇聚**起来

  ![image](https://github.com/rayest/Document/raw/master/images/三个缓冲区的发散读操作.png)

### 虚拟内存

* 所有的现代操作系统都是用虚拟内存。即用虚拟地址代替物理内存地址`硬件RAM`
  * 一个以上的虚拟地址可以指向同一个物理内存地址
  * 虚拟内存空间可大于实际可用的硬件的内存
* 使用虚拟内存，可以把内核空间地址与用户空间的虚拟地址映射到同一个物理地址。这样，DMA 硬件(只能访问物理内存地址)就可以填充对**内核**与**用户空间**进程**同时可见**的**缓冲区**

![image](https://github.com/rayest/Document/raw/master/images/内存空间多重映射.png)

* 节省了内核空间与用户空间的往来拷贝。前提是，内核与用户缓冲区必须使用相同的**页对齐**，缓冲区的大小还必须是**磁盘控制器块**大小(通常为 512 字节磁盘扇区)的倍数。操作系统把**内存地址空间**划分为**页**，即固定大小的字节组。**内存页**的大小总是磁盘块大小的倍数，通常为 2 次幂(这样可简化寻址操作)。典型的内存页为 1,024、2,048 和 4,096 字节。虚拟和物理内存**页的大小**总是相同的。下图显示了来自多个虚拟地址的虚拟内存页是如何映射到物理内存的

![image](https://github.com/rayest/Document/raw/master/images/内存页.png)

### 补充  

* 虚拟内存和物理内存的区别
  * 应用程序的每个进程都有自己独立的内存空间，各个进程的内存空间具有类似的结构
  * 新进程建立的时候，会建立自己的内存空间。该进程的数据、代码会被拷贝到自己的进程空间中。这些数据都由进程控制表中的 **task_struct** 记录，**task_struct** 中记录了一条链表，记录了内存空间的分配情况，那些地址有数据、哪些地址无数据、哪些可读、哪些可写，都可以通过该链表记录
  * 每个进程被分配的内存空间，都与对应的磁盘空间映射
  * 每个进程拥有的内存空间，实际是指虚拟内存空间。每次访问内存空间就是访问虚拟内存空间，此时需要将虚拟内存地址翻译为实际物理内存地址
  * 所有进程都共享物理内存，每个进程只把自己**当前需要**的**虚拟内存空间**映射到物理内存上
  * 进程要知道哪些内存地址上的数据在物理内存上，哪些不在，以及在物理内存上的哪里，需要**页表**来记录
  * 页表：每一个表项分为两个部分，第一部分记录此页是否在物理内存上；第二部分记录物理内存页的地址
  * 当进程访问某个虚拟地址，先查看页表，以确定对应的数据是否在物理内存中，若果不存在，则发生缺页异常
  * **缺页异常**的处理：把进程需要的数据从磁盘拷贝到物理内存中。若物理内存已满，则找一个页覆盖；若该页有被更新过，需要将此页写入到磁盘
  * 当不同的进程使用同样的代码，如库文件中的代码，物理内存可以只存储一份这样的代码，不同的进程只需要把自己的虚拟内存映射过去就行，节省内存
  * 当程序需要分配连续的内存空间时，只需要在虚拟内存空间分配连续的空间，不需要实际物理内存进行空间连续分配，可以充分利用物理内存
  * 其实，在进程建立时，内核只是为进程创建虚拟内存，即初始化进程控制表中内存相关的链表，而不是立即就把虚拟内存对应位置的程序数据和代码拷贝到物理内存中，只是建立好虚拟内存和磁盘文件之间的映射。待运行到对应的程序时，通过缺页异常进行数据拷贝。程序内存的动态分配，也只是分配了虚拟内存
  * 

### 内存页面调度

* 为实现虚拟内存寻址空间大于物理内存，就必须进行虚拟内存分页(或称为**交换**，真正的交换是在进程层面完成，而非页层面)
* 虚拟内存空间的页面能够继续存在于外部磁盘存储，就为物理内存中的其他虚拟页面腾出了空间
* 物理内存充当了分页区的高速缓存；而所谓分页区，即从物理内存置换出来，存储于磁盘上的内存页面

![image](https://github.com/rayest/Document/raw/master/images/用于分区页高速缓存的物理内存.png)

* 以上。显示了分属于四个进程的虚拟页面，其中每个进程都有属于自己的虚拟内存空间。进程 A 有五个页面，其中两个装入内存，其余存储于磁盘
* 把内存页大小设定为磁盘块大小的倍数，这样内核就可直接向磁盘控制硬件发布命令，把内存页写入磁盘，在需要时再重新装入。结果是，所有磁盘 I/O 都在页层面完成
* 计算机CPU 包含一个称为内存管理单元(MMU)的子系统，逻辑上位于 CPU 与物理内存之间。该设备包含虚拟地址向物理内存地址转换时所需映射信息

### 文件I/O

* 属于系统范畴，文件系统和磁盘迥然不同。磁盘将数据存在扇区上，每个扇区 512 字节。磁盘是硬件设备，对文件系统一无所知，只是提供了一系列数据的存取接口。扇区和内存页相似的是大小统一
* 文件系统更为抽象，是安排和解释磁盘数据的一种方式，通常代码都要和文件系统打交道，而不是直接和磁盘打交道
* 文件系统定义了文件名、路径、文件、文件属性等
* 页面调度：是较为底层的操作，仅发生于磁盘扇区和内存页之间的直接传输
* 文件I/O：任意大小、任意定位
* 文件系统把一连串大小一致的数据块组织起来。
  * 有些块存储元信息：空闲快、目录、索引等的映射；有些含文件数据
  * 单个文件的元信息：描述了哪些块包含文件数据、数据在哪里结束、最后一次更新是什么时候等
* 当用户进程请求读取文件数据时，文件系统需要确定数据具体在磁盘什么位置，然后将相关磁盘扇区读进内存
  * 现代操作系统采用分页技术，利用请求页面调度取得所需数据
* 页：其大小或者与基本内存页一致，或者是其倍数。如 2048 字节 到 8192 字节不等，是基本内存页的倍数
* 分页技术的操作系统执行I/O
  * 确定请求的数据分布于文件系统的哪些页，即位于哪些磁盘扇区组
  * 在内核空间分配足够多数量的内存页，以容纳得到确定的文件系统页
  * 在内存页和磁盘上的文件系统页之间建立映射
  * 为每一个内存页产生页错误
  * 虚拟内存系统俘获页错误，安排页面调入，从磁盘读取页内容。使页有效
  * 页面调入完成后，文件系统对原始数据进行解析，取得文件内容和属性信息
* 文件系统数据也会痛其他内存页一样得到高速缓存

#### 内存映射文件

* 传统的文件 I/O 是通过用户进程发布的 Read 和 write 系统调用来实现数据传输的
* 数据是在内核空间和用户空间之间进行流动的。通常需要多次拷贝，因为文件系统页和用户缓冲区往往没有一一对应
* 内存映射I/O
  * 允许用户进程最大限度地利用面向页的系统I/O特性，并完全摒弃缓冲区拷贝
  * ![image](https://github.com/rayest/Document/raw/master/images/用户内存到文件系统页的映射.png)
  * 使用文件系统建立从**用户空间**直接到**可用文件系统页**的**虚拟内存映射**
    * 用户进程将文件数据当做内存，无需发布 Read 和 write 系统调用，系统调用在内核空间
    * 当用户进程碰触到**映射内存空间**，页错误会自动产生，从而将文件数据从磁盘读进内存。若用户修改了映射内存空间，相关页会自动标记为脏，随后舒心到磁盘，文件得到更新
    * 操作系统的虚拟内存子系统会对页进行智能高速缓存，自动根据系统负载进行内存管理
    * 数据总是按页对齐的，无需要执行缓冲区拷贝
    * 大型文件使用映射，无序耗费大量内存，即可进行数据拷贝

#### 文件锁定

* 允许一个进程阻止其他进程存取文件，或限制其存取方式。控制共享信息的更新方式，或用于事务隔离
* 如数据库等复杂应用严重依赖于文件锁定
* 文件锁定不是单纯指锁定整个文件，甚至可以仅仅只锁定文件中的单个字节或者某一片区域
* 如数据库的行锁等
* 两种方式
  * 共享的：多个共享锁可以同时对同一片区域发生作用
  * 独占的：要求同一区域不能同时有其他锁定在起作用
* 经典应用是控制读取的共享文件的更新
  * 某个进程需要**读取**文件，会先取得该文件或该文件部分区域的**共享锁**，当第二个进程希望**读取**相同文件区域时，也会请求共享锁。二者可以并行读取，互不影响
  * 当第三个进程需要**更新**该文件，会请求**独占锁**，该进程会停止等待，直到既有的共享锁或者独占锁全部释放
  * 待进程获得独占锁后，其他读取进程会处于停滞状态，直至独占锁解除
  * 因此，更新进程可以更新文件，而其他读取进程不会因为文件的更改前后而得到不一致的结果

### 流I/O

* 前述的I/O是面向块的，即数据的移动是通过整块的复制或者缓冲实现的
* 流 I/O 与之不同。是模仿了通道。I/O 字节流必须顺序存取
* 通常，流的传输慢于块设备，经常用于间歇性输入

# 缓冲区

## 缓冲区基础

* 缓冲区：是包在一个对象内的基本数据元素**数组**，Buffer 类是将关于数据的数据内容和信息包含在一个单一对象中
* Java 的 Buffer 类内部其实就是一个基本数据类型的数组
* 常见的缓冲区如ByteBuffer、IntBuffer、DoubleBuffer ... 内部对应的数组依次是 byte、int、double...

### 属性

* 所有的缓冲区都有**四个属性**，以提供关于其所包含的数据元素的信息

* 容量

  * Capacity：缓冲区能容纳的元素的最大数量，在缓冲区创建时被设定，并且不能改变

  * ```java
    ByteBuffer bf = ByteBuffer.allocate(10);
    ```

* 上界

  * Limit：缓冲区中第一个不能读或者写的元素的数组下标索引，也可以认为是缓冲区中实际元素的数量；

* 位置

  * Position：下一个要被读或者写的元素的索引。位置会自动由相应的 get 和 put 函数更新

* 标记

  * Mark：一个备忘位置
  * 关系：0 <= mark <= position <= limit <= capacity

### 缓冲区 API

* 看一下可以如何使用一个缓冲区。以下是 Buffer 类的方法签名:

```java
package java.nio;

public abstract class Buffer {
    public final int capacity();
    public final int position();
    public final Buffer position(int newPosition); 
    public final int limit();
    public final Buffer limit(int newLimit); 
    public final Buffer mark();
    public final Buffer reset();
    public final Buffer clear();
    public final Buffer flip();
    public final Buffer rewind();
    public final int remaining();
    public final boolean hasRemaining(); 
    public abstract boolean isReadOnly(); // 缓冲区都是可读的，并非都可写。标记缓冲区内容是否可被修改
}
```

### 存取：put、get

* 缓冲区数据的存放和获取，需要通过 `get()` 和 `put()` 方法实现。但是需要 `Buffer` 抽象类的子类实现

```java
public abstract class ByteBuffer extends Buffer implements Comparable {
   	public abstract byte get();
    public abstract byte get (int index);
    public abstract ByteBuffer put (byte b);
    public abstract ByteBuffer put (int index, byte b);
}
```

### 填充：put

* 示例：将表示 “Hello” 的字符串的 ASCII 码值载入一个名为 byteBuffer 的 ByteBuffer 对象中

```java
ByteBuffer byteBuffer = ByteBuffer.allocate(10);
byteBuffer.put((byte) 'H').put((byte) 'e').put((byte) 'l').put((byte) 'l').put((byte) 'o');
```

![image](https://github.com/rayest/Document/raw/master/images/五次调用put之后的缓冲区.png)

### 翻转：flip

* 将一个能够**继续添加元素**的**填充状态**的缓冲区翻转为一个**准备读取元素**的**释放状态**
* 可以看作是“改变属性的指针指向”，即 `limit = position, position = 0`

```java
byteBuffer.flip();
```

![image](https://github.com/rayest/Document/raw/master/images/翻转后得的缓冲区.png)

* 填充状态：position 指向第一个可填充的位置，limit 指向第一个不可以添加的位置
* 释放状态：position 指向第一个可读的元素位置，limit 指向第一个不可读的位置
* 所以两次翻转后，缓冲区大小将变为 0

### 释放：clear

```java
for (int i = 0; buffer.hasRemaining(), i++) {
	myByteArray [i] = buffer.get(); 
}
// 或者
int count = buffer.remaining();
for (int i = 0; i < count, i++) {
	myByteArray [i] = buffer.get( ); 
}

public class BufferFillDrain {
  public static void main(String[] argv) throws Exception {
    CharBuffer buffer = CharBuffer.allocate(100);
    while (fillBuffer(buffer)) {
      buffer.flip();
      drainBuffer(buffer);
      buffer.clear();
    }
  }

  private static void drainBuffer(CharBuffer buffer) {
    while (buffer.hasRemaining()) {
      System.out.print(buffer.get());
    }
    System.out.println("");
  }

  private static boolean fillBuffer(CharBuffer buffer) {
    if (index >= strings.length) {
      return (false);
    }
    String string = strings[index++];
    for (int i = 0; i < string.length(); i++) {
      buffer.put(string.charAt(i));
    }
    return (true);
  }

  private static int index = 0;
  private static String[] strings = {
      "A random string value",
      "The product of an infinite number of monkeys",
      "Hey hey we're the Monkees",
      "Opening act for the Monkees: Jimi Hendrix",
      "'Scuse me while I kiss this fly",
      "Help Me! Help Me!",
  };
}
```



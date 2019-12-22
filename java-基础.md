## Java 基础

> 记录 Java 基础知识点

### 反射

```java
public class Person {
    private String name;

    public String getName() {
        return name;
    }

    private void say(String message) {
        System.out.println("Hello: " + message);
    }
}

public class Main {
    public static void main(String[] args) throws Exception {
        Person person = new Person();

        // 设置私有方法
        Class<? extends Person> personClass = person.getClass();
        Method method = personClass.getDeclaredMethod("say", String.class);
        method.setAccessible(true);
        method.invoke(person, "lee"); // Hello: lee

        // 设置私有属性
        Field field = personClass.getDeclaredField("name");
        field.setAccessible(true);
        field.set(person, "rayest");
        System.out.println("更新后的 name：" + person.getName()); // rayest

    }
}
```

### OOM 定位排查

1. 常见原因
> 1. 内存分配过小，业务使用了大量内存
> 2. 某一个对象被频繁申请，却没有释放，内存不断泄漏，导致内存耗尽
> 3. 某一个资源被频繁申请，系统资源耗尽，例如：不断创建线程，不断发起网络连接

2. 定位方式

> 1. jmap -heap PID：查看新生代、老生代堆内存的分配大小以及使用情况，看是否本身分配过小。
> 2. jmap -histo:live PID | more：显示存活对象的信息，并按照所占内存大小排序。以找到**最耗内存的对象**

### 浏览器输入 URL 后发生了什么

> 1. DNS 解析
> 2. TCP 连接
> 3. HTTP 请求
> 4. HTTP 响应
> 5. 浏览器解析响应
> 6. 页面渲染

### 三次握手

> 是指建立一个 TCP 连接时，需要客户端和服务器总共发送3个包。
>
> 1. 第一次握手(SYN=1, seq=x):
>
>    客户端发送一个 SYN 标志为 1 的包，指明客户端打算连接的服务器的端口，以及初始序号 X
>
>    发送完毕后，客户端进入 `SYN_SEND` 状态。
>
> 2. 第二次握手(SYN=1, ACK=1, seq=y, ACKnum=x+1):
>
>    服务器发回确认包(ACK)应答。即 SYN 标志位和 ACK 标志位均为1。将确认序号(Acknowledgement Number)设置为客户的 ISN 加1，即X+1。 发送完毕后，服务器端进入 `SYN_RCVD` 状态。
>
> 3. 第三次握手(ACK=1，ACKnum=y+1)
>
>    客户端再次发送确认包(ACK)，SYN 标志位为0，ACK 标志位为1，并且把服务器发来 ACK 的序号字段+1
>
>    发送完毕后，客户端进入 `ESTABLISHED` 状态，当服务器端接收到这个包时，也进入 `ESTABLISHED` 状态，TCP 握手结束。

### 四次挥手

> TCP 的连接的拆除需要发送四个包，因此称为四次挥手。客户端或服务器均可主动发起挥手动作，在 socket 编程中，任何一方执行 `close()` 操作即可产生挥手操作。
>
> - 第一次挥手(FIN=1，seq=x)
>
>   假设客户端想要关闭连接，客户端发送一个 FIN 标志位置为1的包，表示自己已经没有数据可以发送了，但是仍然可以接受数据。发送完毕后，客户端进入 `FIN_WAIT_1` 状态。
>
> - 第二次挥手(ACK=1，ACKnum=x+1)
>
>   服务器端确认客户端的 FIN 包，发送一个确认包，表明自己接受到了客户端关闭连接的请求，但还没有准备好关闭连接。
>
>   发送完毕后，服务器端进入 `CLOSE_WAIT` 状态，客户端接收到这个确认包之后，进入 `FIN_WAIT_2` 状态，等待服务器端关闭连接。
>
> - 第三次挥手(FIN=1，seq=y)
>
>   服务器端准备好关闭连接时，向客户端发送结束连接请求，FIN 置为1。
>
>   发送完毕后，服务器端进入 `LAST_ACK` 状态，等待来自客户端的最后一个ACK。
>
> - 第四次挥手(ACK=1，ACKnum=y+1)
>
>   客户端接收到来自服务器端的关闭请求，发送一个确认包，并进入 `TIME_WAIT`状态，等待可能出现的要求重传的 ACK 包。
>
>   服务器端接收到这个确认包之后，关闭连接，进入 `CLOSED` 状态。
>
>   客户端等待了某个固定时间（两个最大段生命周期，2MSL，2 Maximum Segment Lifetime）之后，没有收到服务器端的 ACK ，认为服务器端已经正常关闭连接，于是自己也关闭连接，进入 `CLOSED` 状态。
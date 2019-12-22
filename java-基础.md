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
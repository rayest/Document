## Java 基础

> 记录 Java 基础知识点

* 反射基本使用

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


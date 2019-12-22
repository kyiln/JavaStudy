# 读源码--Unsafe（确实不懂，将彤哥的文章里的demo跑了一遍）

## 获取unsafe的实例

### 源码获取实例

因为安全问题，getUnsafe方法只允许虚拟机内部调用，否则会抛出Unsafe异常

```java
    @CallerSensitive
    public static Unsafe getUnsafe() {
        Class var0 = Reflection.getCallerClass();
        if (!VM.isSystemDomainLoader(var0.getClassLoader())) {
            throw new SecurityException("Unsafe");
        } else {
            return theUnsafe;
        }
    }
```

### 我们来获得实例

反射大法好

```java
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
    	// 源码中theUnsafe字段的类型就是Unsafe
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Unsafe unsafe = (Unsafe) field.get(null);
    }
```

## 通过Unsafe实例化类

又增加了一种实例化类的方法，尽管应该不常用

```java
@Data
public class User {
    private int age;
    public User() {
        this.age = 10;
    }

    public int getAge() {
        return this.age;
    }
}

    User user = new User();
    System.out.println(user.age);
    
	//通过Unsafe的实例来实例化User类
    User user1 = (User) unsafe.allocateInstance(User.class); 
    System.out.println(user1.age);

```

## 修改私有字段的值

通常情况下私有字段的值是不允许外部更改的，但Unsafe可以（反射大法也行）

```java
    User user2 = new User();
    Field age = user2.getClass().getDeclaredField("age");
    // objectFieldOffset获取字段的偏移值
    unsafe.putInt(user2,unsafe.objectFieldOffset(age),20);
    System.out.println(user2.getAge());
```

put系列方法

```java
    /**
     * 给字段赋值
     * @param var1 对象
     * @param var2 字段的偏移值
     * @param var4 新值
     */
    public native void putInt1(Object var1, long var2, int var4);
```



## 抛出checked异常

使用unsafe抛出异常时可以不用在方法签名上定义（有什么用？）

```java
    public static void readFileUnsafe() {
        unsafe.throwException(new IOException());
    }
```

## 使用堆外内存

如果程序在运行过程中，内存不足，会频繁进行GC。此时可以考虑堆外内存，此内存不受jvm控制，使用完毕须释放

```java
public class OffHeapArray {
    private static final int INT = 4;
    private long size;
    private long address;

    private static Unsafe unsafe;
	// 静态块获取Unsafe实例
    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    // 构造方法，分配内存
    public OffHeapArray(long size) {
        this.size = size;
        address = unsafe.allocateMemory(size*INT);
    }

    public int get(long i) {
        return unsafe.getInt(address+i*INT);
    }

    public void set(long i, int value) {
        unsafe.putInt(address + i * INT, value);
    }

    public long size() {
        return size;
    }

    public void freeMemory() {
        unsafe.freeMemory(address);
    }

    public static void main(String[] args) {
        OffHeapArray offHeapArray = new OffHeapArray(4);
        offHeapArray.set(0, 1);
        offHeapArray.set(1, 2);
        offHeapArray.set(2, 3);
        offHeapArray.set(3, 4);
        offHeapArray.set(2,5);

        int sum = 0;
        for (int i = 0; i < offHeapArray.size(); i++) {
            sum += offHeapArray.get(i);
        }
        System.out.println(sum); //12:1+2+4+5
        offHeapArray.freeMemory(); // 释放内存
    }

}
```

## CompareAndSwap操作

暂时没接触也没用过，但感觉很厉害的样子。留个印象，运用与无锁算法，与java的悲观锁性比，它可利用CAS处理器提供极大的加速。

```java
public class Counter {
    private volatile int count = 0;
    private static long offset;
    private static Unsafe unsafe;

    {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
            // 获取count字段的偏移地址，以便修改它
            offset = unsafe.objectFieldOffset(Counter.class.getDeclaredField("count"));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对比并替换字段的值
     * @param obj 实例
     * @param offset 字段的偏移地址
     * @param expect 期望值
     * @param update 更新值
     * @return 若期望值与字段当前值相等，则更新；否则不更新
     */
    public native boolean compareAndSwapInt(Object obj, long offset,int expect, int update);
    
    public void increment() {
        int before = count;
        // compareAndSwapInt中before的
        // before被赋于count的值后，若其他线程未对count做出修改，则此时before值与count当前值相同，则更新；否则befere重新赋值，直至更新成功
        while (!unsafe.compareAndSwapInt(this, offset, before, before + 1)) {
            before = count;
        }
    }

    public int getCount() {
        return count;
    }

    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();
        ExecutorService threadPool = Executors.newFixedThreadPool(100); // 线程池
		
        // 100个线程各自增10000次
        IntStream.range(0,100)
                .forEach(i->threadPool.submit(()->IntStream.range(0,10000)
                        .forEach(j->counter.increment())));

        threadPool.shutdown();
        Thread.sleep(2000);

        System.out.println(counter.getCount()); //1000,000

    }
}
```

## park/unpark

当一个线程在等待某个操作时，JVM调用Unsafe的park方法来阻塞此线程。

当阻塞中的线程需要再次执行时，JVM调用Unsafe的unpark方法来唤醒此线程。

## 面试积累一波

实例化6大法

```java
public class InstantialTest {
    private static Unsafe unsafe;
    {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    static class User implements Cloneable, Serializable {
        private int age;

        public User() {
            this.age = 10;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    private static User unSerializable(User user) throws IOException, ClassNotFoundException {
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("E://Project//JavaStudy//week_02//08//object.txt"));
        outputStream.writeObject(user);
        outputStream.close();

        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("E://Project//JavaStudy//week_02//08//object.txt"));
        User user1 = (User) inputStream.readObject();
        inputStream.close();

        return user1;
    }

    public static void main(String[] args) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, CloneNotSupportedException, IOException, ClassNotFoundException {
        // new
        User user = new User();

        // Class
        User user1 = User.class.newInstance();

        // 反射
        User user2 = User.class.getConstructor().newInstance();

        // clone
        User user3 = (User) user.clone();

        // 反序列化
        User user4 = unSerializable(user);

        // unsafe
        User user5 = (User) unsafe.allocateInstance(User.class);

        System.out.println(user.age);
        System.out.println(user1.age);
        System.out.println(user2.age);
        System.out.println(user3.age);
        System.out.println(user4.age);
        System.out.println(user5.age);
    }
```






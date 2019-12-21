# 读源码--AtomicInteger

## 属性

unsafe属性：为了保证线程安全，AtomicIngeger的增减方法的底层都用到了Unsafe类的cas方法

```java
private static final Unsafe unsafe = Unsafe.getUnsafe();
```

valueOffset属性：字段的偏移地址，在cas中用来得到当前字段的值

```java
private static final long valueOffset;
```

value属性：此处用volatile修饰，volatile，被称为“轻量级的synchronized”，但只能修饰变量。

```java
private volatile int value;
```

## 方法

AtomicInteger的实现方法模式基本一致，用一个方法说明，getAndSet方法

```java
public final int getAndSet(int newValue) {
    return unsafe.getAndSetInt(this, valueOffset, newValue);
}
// get在前，返回值则为修改前的值；若get在后，则返回值为修改后的值
public final int getAndSetInt(Object var1, long var2, int var4) {
    int var5;
    // 此处用到cas算法，保证线程安全
    do {
        var5 = this.getIntVolatile(var1, var2);
    } while(!this.compareAndSwapInt(var1, var2, var5, var4));

    return var5;
}
```

## CAS

### CAS概念

比较并交换，unsafe中有该方法，广泛应用与文字操作。CAS是乐观锁技术，当多个线程尝试更新同一个变量时，只有一个能更新成功，而其他线程都失败。失败的线程不会被挂起，而是被告知失败，重新尝试。

### CAS 缺点

1.循环时间长开销大

2.只能保证一个共享变量的原子操作

3.ABA问题。解决办法，版本控制。Java中提供了AtomicStampedReference通过控制变量值的版本来保证CAS的正确性。

## volatile

### 特性

1.保证了不同线程对变量操作的内存可见性（可见性）；

2.禁止指令重排序（有序性）

3.无法保证复合操作的原子性。要想保证原子性，只能借助于synchronized，Lock及原子操作了，如Atomic系列类

https://juejin.im/post/5a2b53b7f265da432a7b821c volatile面试强文

### 并发编程三大特性

1.原子性：Java中对基本数据类型的读取和赋值操作

2.可见性：Java利用volatile来提供可见性。变量被volatile修饰是，对它的修改会立即刷新到贮存；当其他线程需要读取该变量是，会从主存中共读取新值。

3.有序性：JMM允许编译器和处理器对指令重排序。对单线程没有影响，对多线程有影响/

```java
int a = 0;
bool flag = false;

public void write() {
    a = 2;              //1
    flag = true;        //2
}

public void multiply() {
    if (flag) {         //3
        int ret = a * a;//4
    }
    
}
// 若线程1先执行write方法中flag=true；线程2执行multiply，线程1再执行a=2；
```

### 应用场景

1.状态量标记：标记为volatile可以保证修改对线程立即可见。比synchronized，Lock有一定的效率提升。

2.单例模式的实现

```java
class Singleton{
    // 可避免初始化操作的指令重排序
    private volatile static Singleton instance = null;
 
    private Singleton() {
 
    }
 
    public static Singleton getInstance() {
        if(instance==null) {
            synchronized (Singleton.class) {
                if(instance==null)
                    instance = new Singleton();
            }
        }
        return instance;
    }
}
```


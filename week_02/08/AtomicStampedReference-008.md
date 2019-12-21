# 读源码--AtomicStampedReference

## ABA问题

在CAS过程中，会出现ABA问题

```java
public class ABATest {
    public static void main(String[] args) {
        AtomicInteger atomicInteger = new AtomicInteger(1);
        new Thread(() -> {
            int value = atomicInteger.get();
            System.out.println("Thread 1 read value:"+value);

            LockSupport.parkNanos(100000000L);
            if (atomicInteger.compareAndSet(value, 3)) {
                System.out.println("Thread 1 update from " + value + " to 3");
            } else {
                System.out.println("Thread 1 update fail !");
            }
        }).start();

        new Thread(() -> {
            int value = atomicInteger.get();
            System.out.println("Thread 2 read value:" + value);

            if (atomicInteger.compareAndSet(value, 2)) {
                System.out.println("Thread 2 update from "+value+" to 2");
            }

            value = atomicInteger.get();
            System.out.println("Thread 2 read value:"+value);
            if (atomicInteger.compareAndSet(value, 1)) {
                System.out.println("Thread 2 update from "+value+" to 3");
            }
        }).start();
    }
}
```

结果

```java
Thread 1 read value:1
Thread 2 read value:1
Thread 2 update from 1 to 2
Thread 2 read value:2
Thread 2 update from 2 to 3
Thread 1 update from 1 to 3
```



## ABA危害

例子，一个无锁的栈结构

```java
public class ABATest1 {
    static class Stack{
        private AtomicReference<Node> top = new AtomicReference();

        static class Node {
            int value;
            Node next;
            public Node(int value) {
                this.value = value;
            }
        }

        // 出栈
        public Node pop() {
            for (; ; ) {
                // 获取栈顶节点
                Node t = top.get();
                if (t == null) {
                    return null;
                }

                Node next = t.next;
                // top指向栈顶的next
                if (top.compareAndSet(t, next)) {
                    t.next = null; // next清空防止外面直接操作栈
                    return t;
                }
            }
        }

        public void push(Node node) {
            for (; ; ) {
                // 获取栈顶节点
                Node t = top.get();

                node.next = t;
                // 更新top指向node节点
                if (top.compareAndSet(t, node)) {
                    return;
                }

            }
        }
    }

    public static void testStack() {
        // 初始化栈top->1->2->3
        Stack stack = new Stack();
        stack.push(new Stack.Node(3));
        stack.push(new Stack.Node(2));
        stack.push(new Stack.Node(1));

        new Thread(() ->{
            // 线程1出栈一个元素
            stack.pop();
        }).start();

        new Thread(() ->{
            // 线程2出栈两个元素
            Stack.Node A = stack.pop();
            Stack.Node B = stack.pop();
            // 线程2入栈A元素
            stack.push(A);
        }).start();

    }

    public static void main(String[] args) {
        testStack();
    }
}
```

初始化栈top->1->2->3

1）.线程1出栈，在top.compareAndSet(t, next)暂停，未弹出节点1

2）.线程2，出栈，弹出节点1，栈结构变为top->2->3

3）.线程2，出栈，弹出节点2，栈结构变为top->3

4）.线程2，入栈，添加节点1，栈结构变为top->1->3

5）.线程1，弹出节点1，结果top->2

竟然不是top->3?

因为线程1在第一步保持的next时节点2，所以它执行成功后top节点就指向节点2了

## ABA解决方法

1.版本号

2.不重复使用节点的引用。如上述节点1

3.直接操作元素而不是节点。

## AtomicStampedReference源码

### 内部类

```java
    private static class Pair<T> {
        final T reference; //值
        final int stamp; //版本号
        private Pair(T reference, int stamp) {
            this.reference = reference;
            this.stamp = stamp;
        }
        static <T> Pair<T> of(T reference, int stamp) {
            return new Pair<T>(reference, stamp);
        }
    }
```

### 属性

```java
private volatile Pair<V> pair;
```

### 构造器

```java
    /**
     * Creates a new {@code AtomicStampedReference} with the given
     * initial values.
     *
     * @param initialRef the initial reference 初始引用
     * @param initialStamp the initial stamp 初始版本号
     */
    public AtomicStampedReference(V initialRef, int initialStamp) {
        pair = Pair.of(initialRef, initialStamp);
    }
```

### 改造ABA问题例子

```java
public class ABATest2 {
    public static void main(String[] args) {
        AtomicStampedReference<Integer> atomicStampedReference = new AtomicStampedReference<>(1,1);
        new Thread(() -> {
            int[] stampHolder = new int[1];
            int value = atomicStampedReference.get(stampHolder);
            int stamp = stampHolder[0];
            System.out.println("Thread 1 read value:"+value);
            System.out.println("Thread 1 read stamp:"+stamp);

            LockSupport.parkNanos(100000000L);

            value = atomicStampedReference.get(stampHolder);
            stamp = stampHolder[0];
            System.out.println("After parking,Thread 1 read value:"+value); 
            System.out.println("Thread 1 read stamp:"+stamp); // 版本号变为3了
            if (atomicStampedReference.compareAndSet(value,3,stamp,stamp+1)) {
                System.out.println("Thread 1 update from " + value + " to 3");
            } else {
                System.out.println("Thread 1 update fail !");
            }
        }).start();

        new Thread(() -> {
            int[] stampHolder = new int[1];
            int value = atomicStampedReference.get(stampHolder);
            int stamp = stampHolder[0];
            System.out.println("Thread 2 read value:" + value);
            System.out.println("Thread 2 read stamp:"+stamp);

            if (atomicStampedReference.compareAndSet(value,2,stamp,stamp+1)) {
                System.out.println("Thread 2 update from "+value+" to 2");
            }

            value = atomicStampedReference.get(stampHolder);
            stamp = stampHolder[0];
            System.out.println("Thread 2 read value:"+value);
            System.out.println("Thread 2 read stamp:"+stamp);
            if (atomicStampedReference.compareAndSet(value,3,stamp,stamp+1)) {
                System.out.println("Thread 2 update from "+value+" to 3");
            }
        }).start();
    }
}
```

结果：

```java
Thread 1 read value:1
Thread 1 read stamp:1
Thread 2 read value:1
Thread 2 read stamp:1
Thread 2 update from 1 to 2
Thread 2 read value:2
Thread 2 read stamp:2
Thread 2 update from 2 to 3
After parking,Thread 1 read value:3
Thread 1 read stamp:3
Thread 1 update from 3 to 3
```






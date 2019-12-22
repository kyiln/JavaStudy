# LongAdder

## 原理

在最初无竞争时，只更新base的值；有竞争时，通过分段的思想，让不同的线程更新不同的段，最后sum得到各个线程更新的结果。原理图如下：

![LongAdder原理图](E:\BBBMyProject\JavaStudy\week_02\08\LongAdder原理图.jpg)

## 内部类Cell

LongAdder继承于Striped64，Striped的内部类Cell

```java
 @sun.misc.Contended static final class Cell {
        volatile long value;
        Cell(long x) { value = x; }
        final boolean cas(long cmp, long val) {
            // 用到Unsafe类的CAS方法
            return UNSAFE.compareAndSwapLong(this, valueOffset, cmp, val);
        }

        // Unsafe mechanics
        private static final sun.misc.Unsafe UNSAFE;
        private static final long valueOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> ak = Cell.class;
                valueOffset = UNSAFE.objectFieldOffset
                    (ak.getDeclaredField("value"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }
```

## 主要属性

```java
	
    // 存储多个线程的Cell
	transient volatile Cell[] cells;
	// 基础值
    transient volatile long base;
	// 锁，当扩容或创建新Cell时用到，0：无锁；1：有锁
    transient volatile int cellsBusy;
```

## 主要方法

add方法

```java
    public void add(long x) {
        // as保存cells
        // b保存base值，v保存当前线程hash到Cell的值
        // m保存cells的长度-1
        // a当前线程hash到Cell
        Cell[] as; long b, v; int m; Cell a;
 		// 但Cells为空时，首先执行casBase方法，若成功，则结束
        // 条件1：当cells不为空，说明cells已经创建，出现过竞争
        // 条件2：casBase执行失败，说明其他线程抢先修改过base，正在出现竞争
        if ((as = cells) != null || !casBase(b = base, b + x)) {
            // true竞争不激烈
            // false竞争激烈，可能需要扩容
            boolean uncontended = true;
            // 条件1：cells为空，说明正出现竞争，从上面条件2过来
            // 条件2：不可能出现
            // 条件3：当前线程对应的Cell为空，说明当前线程没有更新过Cell，需初始化一个Cell
            // 条件4：cas失败，说明竞争激烈，多个线程hash到同一个Cell，应扩容
            if (as == null || (m = as.length - 1) < 0 ||
                (a = as[getProbe() & m]) == null ||
                !(uncontended = a.cas(v = a.value, v + x)))
                longAccumulate(x, null, uncontended);
        }
    }
```

longAccumulate方法

```java
    final void longAccumulate(long x, LongBinaryOperator fn,
                              boolean wasUncontended) {
        int h;
        if ((h = getProbe()) == 0) {
            ThreadLocalRandom.current(); // force initialization
            h = getProbe();
            wasUncontended = true;
        }
        boolean collide = false;                // True if last slot nonempty
        for (;;) {
            Cell[] as; Cell a; int n; long v;
            if ((as = cells) != null && (n = as.length) > 0) {
                // 当前线程hash的Cell为空，创建一个新的Cell
                if ((a = as[(n - 1) & h]) == null) {
                    if (cellsBusy == 0) {       // Try to attach new Cell
                        Cell r = new Cell(x);   // Optimistically create
                        if (cellsBusy == 0 && casCellsBusy()) { //casCellsBusy()加锁
                            boolean created = false;
                            try {               // Recheck under lock
                                Cell[] rs; int m, j;
                                // 这里一定要重新获取cells，因为as并不在锁定范围内
                                if ((rs = cells) != null &&
                                    (m = rs.length) > 0 &&
                                    rs[j = (m - 1) & h] == null) {
                                    rs[j] = r;
                                    created = true;
                                }
                            } finally {
                                cellsBusy = 0; //释放锁
                            }
                            if (created)
                                break;
                            continue;           // Slot is now non-empty
                        }
                    }
                    collide = false;
                }
                else if (!wasUncontended)       // CAS already known to fail
                    wasUncontended = true;      // Continue after rehash
                else if (a.cas(v = a.value, ((fn == null) ? v + x :
                                             fn.applyAsLong(v, x))))
                    break;
                else if (n >= NCPU || cells != as)
                    collide = false;            // At max size or stale
                else if (!collide)
                    collide = true;
                else if (cellsBusy == 0 && casCellsBusy()) {
                    try {
                        if (cells == as) {      // Expand table unless stale
                            Cell[] rs = new Cell[n << 1];
                            for (int i = 0; i < n; ++i)
                                rs[i] = as[i];
                            cells = rs;
                        }
                    } finally {
                        cellsBusy = 0;
                    }
                    collide = false;
                    continue;                   // Retry with expanded table
                }
                h = advanceProbe(h);
            }
            else if (cellsBusy == 0 && cells == as && casCellsBusy()) {
                boolean init = false;
                try {                           // Initialize table
                    if (cells == as) {
                        Cell[] rs = new Cell[2];
                        rs[h & 1] = new Cell(x);
                        cells = rs;
                        init = true;
                    }
                } finally {
                    cellsBusy = 0;
                }
                if (init)
                    break;
            }
            else if (casBase(v = base, ((fn == null) ? v + x :
                                        fn.applyAsLong(v, x))))
                break;                          // Fall back on using base
        }
    }
```

sum方法：将cells中的值累加起来

```java
    public long sum() {
        Cell[] as = cells; Cell a;
        long sum = base;
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    sum += a.value;
            }
        }
        return sum;
    }
```

## LongAdder与AtomicLong比较

```java
public class LongAdderTest {
    public static void main(String[] args) throws InterruptedException {
        testAtomicLongAndLongAdder(1,10000000);
        testAtomicLongAndLongAdder(10,10000000);
        testAtomicLongAndLongAdder(20,10000000);
        testAtomicLongAndLongAdder(40,10000000);
        testAtomicLongAndLongAdder(80,10000000);
    }

    public static void testAtomicLongAndLongAdder(final int threadCount,final int times) throws InterruptedException {
        System.out.println("threadCount:"+threadCount+",times:"+times);
        Long start = System.currentTimeMillis();
        testAtomicLong(threadCount,times);
        System.out.println("AtomicLong elapse:"+(System.currentTimeMillis()-start));

        Long start2 = System.currentTimeMillis();
        testLongAdder(threadCount,times);
        System.out.println("LongAdder elapse:"+(System.currentTimeMillis()-start2));
        System.out.println("------------");
    }

    public static void testLongAdder(final int threadCount,final int times) throws InterruptedException {
        LongAdder longAdder = new LongAdder();
        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            list.add(new Thread(()->{
                for (int j = 0; j < times; j++) {
                    longAdder.add(1);
                }
            }));
        }

        for (Thread thread : list) {
            thread.start();
        }
        for (Thread thread : list) {
            thread.join();
        }
    }

    public static void testAtomicLong(final int threadCount,final int times) throws InterruptedException {
        AtomicLong atomicLong = new AtomicLong();
        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            list.add(new Thread(()->{
                for (int j = 0; j < times; j++) {
                    atomicLong.incrementAndGet();
                }
            }));
        }

        for (Thread thread : list) {
            thread.start();
        }
        for (Thread thread : list) {
            thread.join();
        }
    }
}
```

结果：

```java
threadCount:1,times:10000000
AtomicLong elapse:93
LongAdder elapse:85
------------
threadCount:10,times:10000000
AtomicLong elapse:1854
LongAdder elapse:195
------------
threadCount:20,times:10000000
AtomicLong elapse:3776
LongAdder elapse:390
------------
threadCount:40,times:10000000
AtomicLong elapse:7602
LongAdder elapse:615
------------
threadCount:80,times:10000000
AtomicLong elapse:13096
LongAdder elapse:1227
```

结论：线程数少的时候，二者差异小；随着线程数的增加，二者的差异越来越大。


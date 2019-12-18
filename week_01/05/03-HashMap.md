# HashMap 源码分析

## TOP 带着问题看源码

1. HashMap 的数据结构是什么
2. Hash冲突解决办法是什么，什么时候会转为红黑树
3. 容量为什么为2的N次幂
4. HashMap 是怎么扩容的
5. HashMap 为什么使用红黑树

## 1. 继承和实现关系

<img src="http://qiniu.itliusir.com/hashmap01.png" style="zoom:50%;" />

- *AbstractMap 实现类*

  提供一些围绕着iterator的基础方法

- *Cloneable 接口*

  标记该类对象能够被Object.clone()

- *Serializable 接口*

  标记该类是可序列化的。

## 2. 成员变量分析

```java
// 默认初始容量
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
// 最大容量
static final int MAXIMUM_CAPACITY = 1 << 30;
// 负载因子
static final float DEFAULT_LOAD_FACTOR = 0.75f;
// 大于8转为树
static final int TREEIFY_THRESHOLD = 8;
// 小于6转为链表
static final int UNTREEIFY_THRESHOLD = 6;
// 当内部数组size小于64并且单位置冲突超过8，优先扩容，而不是树化
static final int MIN_TREEIFY_CAPACITY = 64;
```

## 3. 构造方法分析

### 3.1 无参构造方法

使用默认负载因子做全局负载因子

```java
public HashMap() {
    this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
}
```

### 3.2 带初始化容量的构造方法

指定容量和默认负载因子，走下面带负载因子的构造方法

```java
public HashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
}
```

### 3.3 带初始化容量和负载因子的构造方法

check参数，容量转为参数的最小2次幂。

为什么要转为2的N次幂呢，主要是为了后面做取模运算可以使用性能更好地位运算来代替%

回到 **TOP 3** 问题，可以明白了为什么这样设计。

```java
public HashMap(int initialCapacity, float loadFactor) {
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal initial capacity: " +
                                           initialCapacity);
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new IllegalArgumentException("Illegal load factor: " +
                                           loadFactor);
    this.loadFactor = loadFactor;
    this.threshold = tableSizeFor(initialCapacity);
}
```

## 4. 核心方法分析

### 4.1 获取元素

先计算key的hash值，然后调用getNode方法获取到节点的值

```java
public V get(Object key) {
    Node<K,V> e;
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}
```

我们先来看hash方法，可以看到是通过高半区与低半区进行异或，为什么要这样做呢？

主要是把高位的特征也给加入到扰动计算中，降低低位的冲突。那降低低位冲突目的是啥呢？

其实可以从取下标位置(n-1) & hash来分析，n为2的N次幂，在n - 1在二进制中低位肯定全是1，那和hash做与运算相当于结果是hash低位的截取操作。也就是hash的冲突情况完全取决于hash自身低位的冲突情况。

```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

这段代码的主要逻辑就是先计算下标，然后对比hash值和value值来获取元素(①)。注意的是如果节点是tree，会使用递归来遍历查找，时间复杂度则会转为O(nlogn)(②)。如果是链表则会遍历来获取，这段长度比较短并不会太影响性能(③)。

```java
final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (first = tab[(n - 1) & hash]) != null) {
        // ①
        if (first.hash == hash && // always check first node
            ((k = first.key) == key || (key != null && key.equals(k))))
            return first;
        if ((e = first.next) != null) {
          	// ②
            if (first instanceof TreeNode)
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
            do {
              	// ③
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    return e;
            } while ((e = e.next) != null);
        }
    }
    return null;
}
```

### 4.2 新增&更新元素

#### 4.2.1 put(K key, V value)

计算 key 的 hash，onlyIfAbsent 设置为 false (默认覆盖旧的 key )，evict 设置为 true (代表会逐出元素，在LinkedHashMap 实现 LRU 时候的重写方法 removeEldestEntry 里会用到。在序列化也会涉及到，序列化时候会设置为 false)

```java
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}
```

#### 4.2.2 putIfAbsent(K key, V value)

对比默认的 put 方法，只是把 onlyIfAbsent 设置为true，表示有则不覆盖

```java
public V putIfAbsent(K key, V value) {
    return putVal(hash(key), key, value, true, true);
}
```

####4.2.3 resize()

在分析 putVal 方法之前，我们先分析扩容方法 resize

核心逻辑主要分为以下五个部分

① 没超过最大值，且数组元素超过了64的阈值则扩容为原来的2倍

② 无冲突情况数组桶重新hash

③ 节点是红黑树，走红黑树拆分逻辑，和下面链表差不多，会增加阈值判断，若扩容后节点数小于6则会转为链表

④ 节点是链表，④-① 和 ④-② 是判断hash值新增bit位是0还是1的情况，来分散链表

⑤ 对④做最后的铺垫，根据不同情况放置不同位置

```java
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    int oldThr = threshold;
    int newCap, newThr = 0;
    if (oldCap > 0) {
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)
            // ①
            newThr = oldThr << 1; // double threshold
    }
    else if (oldThr > 0) // initial capacity was placed in threshold
        newCap = oldThr;
    else {               // zero initial threshold signifies using defaults
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    if (newThr == 0) {
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab;
    if (oldTab != null) {
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {
                oldTab[j] = null;
                if (e.next == null)
                    // ②
                    newTab[e.hash & (newCap - 1)] = e;
                else if (e instanceof TreeNode)
                  	// ③
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { // preserve order
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    // ④
                    do {
                        next = e.next;
                        // ④-①
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        }
                      	// ④-②
                        else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                  	// ⑤
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```

回到 **TOP 4** 问题，可以明白了hashMap扩容的机制和场景

####4.2.3 putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict)

核心逻辑主要分为如下6个步骤

① 先检查存储数组是否为空(例如使用默认构造方法没有设置初始值)，为空了则调用上面的扩容方法resize

② 然后计算hash值对应的位置是否为空，如果为空则构建一个next节点是null的空节点放到该位置

③ 如果位置不为空，hash值相同，且key相同则更新元素

④ 如果节点是 treeNode，则调用 Tree 版本的putTreeVal，逻辑都差不多，就是遍历左右子树，查到了就返回查不到就构建一个

⑤ 如果节点是链表，首先遍历到链表最后一位加入构建的节点，然后 check 阈值是否要转为红黑树，最后若存在相同的key就覆盖

⑥ 超过最大容量则扩容处理

```java
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
  	// ①
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    // ②
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        Node<K,V> e; K k;
      	// ③
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
      	// ④
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
          	// ⑤
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
  	// ⑥
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```

回到 **TOP 2** 问题，可以明白了解决冲突的方式是采用了拉链法，当链表长度大于8则会转为红黑树

### 4.3 删除元素

计算hash值，然后调用 removeNode 方法

```java
public V remove(Object key) {
    Node<K,V> e;
    return (e = removeNode(hash(key), key, null, false, true)) == null ?
        null : e.value;
}
```

核心逻辑主要分为三个步骤

① 定位元素的位置

② 找到键相同的元素

③ 删除相关节点

```java
final Node<K,V> removeNode(int hash, Object key, Object value,
                           boolean matchValue, boolean movable) {
    Node<K,V>[] tab; Node<K,V> p; int n, index;
    if ((tab = table) != null && (n = tab.length) > 0 &&
        // ①
        (p = tab[index = (n - 1) & hash]) != null) {
        Node<K,V> node = null, e; K k; V v;
      	// ②
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            node = p;
        else if ((e = p.next) != null) {
            if (p instanceof TreeNode)
                node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
            else {
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key ||
                         (key != null && key.equals(k)))) {
                        node = e;
                        break;
                    }
                    p = e;
                } while ((e = e.next) != null);
            }
        }
      	// ③
        if (node != null && (!matchValue || (v = node.value) == value ||
                             (value != null && value.equals(v)))) {
            if (node instanceof TreeNode)
                ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
            else if (node == p)
                tab[index] = node.next;
            else
                p.next = node.next;
            ++modCount;
            --size;
            afterNodeRemoval(node);
            return node;
        }
    }
    return null;
}
```

## 5. 总结

### 5.1 数据结构的设计

总体是一个散列表的设计，底层使用数组，这里为了方便位运算，会将size重置为最接近你所设置的2^n，这样取模就可以用位运算代替了。

### 5.2 冲突的处理

hash 冲突采用的是拉链法，王争老师的《数据结构与算法之美》专栏里有讲解，对于数据较少的话使用开放寻址法处理冲突较为合适，例如ThreadLocal，显然不适合HashMap。

回到 **TOP 1** 问题，可以明白了 HashMap 底层使用的数组+链表(红黑树) 来实现的。

### 5.3 为什么使用红黑树

当链足够长，HashMap设置的阈值是8 超过8就会转成红黑树，原因是链表的时间复杂度在数据多的情况下会表现很差。至于为什么使用的是红黑树而不是相同时间复杂度实现更为简单的跳表呢？ 实际上使用跳表也不是不可以，但是HashMap主要的场景还是散列表，每个冲突都用跳表结构属实有些浪费空间。

解释了 **TOP 5问题**

### 5.4 扩容

在扩容期间，为了避免单链过长，扩容时候会对链进行分开处理，所以就又有了冲突的长度小于6会把树节点重新转化为链表。
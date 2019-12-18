# <Center>HashMap源码分析</Center>

- 以下分析基于JDK1.8

## HashMap 简介

HashMap 主要用来存放键值对，它基于哈希表的Map接口实现，是常用的Java集合之一。 

```
hashMap继承自抽象类AbstractMap实现了Map接口等
```

HashMap 最多允许一条记录的键为null，允许多条记录的值为null。HashMap 是非线程安全的，即任一时刻有多个线程同时写 HashMap ，可能会导致数据不一致，。如果要满足线程安全，可以使用 Collections 的 SynchronizedMap 方法 或者使用 ConcurrentHashMap。

## HashMap分析

1、属性分析

- ```java
  // 序列号ID,hashmap实现了Serializable接口因此是可在网络中传输
  private static final long serialVersionUID = 362498820763181265L;
  ```

- ```java
  // 默认的初始容量是16
  static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
  ```

- ```java
  // 最大容量
  static final int MAXIMUM_CAPACITY = 1 << 30;
  ```

- ```
  // 默认的负载率 75%
  static final float DEFAULT_LOAD_FACTOR = 0.75f;
  ```

- ```java
  // 当node上的结点数大于这个值时会由链表结构转成红黑树
  static final int TREEIFY_THRESHOLD = 8;
  ```

- ```java
  // 当node上的结点数小于这个值时树转链表
  static final int UNTREEIFY_THRESHOLD = 6;
  ```

- ```java
  // 桶中结构转化为红黑树对应的table的最小大小
  static final int MIN_TREEIFY_CAPACITY = 64;
  ```

2、构造方法分析

```java
// 无参构造函数
	public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }
// 指定“容量大小”的构造函数
   public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }
// 含另一个“Map”的构造函数
   public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }
// 指定“容量大小”和“加载因子”的构造函数
	public HashMap(int initialCapacity, float loadFactor) {
        // 初始容量小于0，则抛出异常
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        // 始容量大于容量最大值，则使用最大值作为初始容量
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        // 果负载率小于等于0或负载率不是浮点数，则抛出异常
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);
        this.loadFactor = loadFactor;
        // 设置阀值为初始容量
        this.threshold = tableSizeFor(initialCapacity);
        
    }
// 回大于输入参数且最近的2的整数次幂的数。比如10，则返回16 相比于1.7提升效率
  static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }


```

3、常见方法分析

- get方法

```java
public V get(Object key) {
    Node<K,V> e;
    
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}
//计算key的hash值
static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {// 首节点不为空
             // 数组元素相等
            if (first.hash == hash && // always check first node
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
           // 桶中不止一个节点
            if ((e = first.next) != null) {
                // 首节点是红黑树按红黑树算法找节点
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                do {
                     // 首节点是链表按链表算法找节点
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }
```

- put方法

  ```java
  public V put(K key, V value) {
      return putVal(hash(key), key, value, false, true);
  }
  
  /**
   * Implements Map.put and related methods
   *
   * @param hash hash for key
   * @param key the key
   * @param value the value to put
   * @param onlyIfAbsent if true, don't change existing value
   * @param evict if false, the table is in creation mode.
   * @return previous value, or null if none
   */
  final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                 boolean evict) {
      Node<K,V>[] tab; Node<K,V> p; int n, i;
      // table未初始化或者长度为0，进行扩容创建table
      if ((tab = table) == null || (n = tab.length) == 0)
          n = (tab = resize()).length;// 桶中已经存在元素
      // (n - 1) & hash 确定元素存放在哪个桶中，桶为空，新生成结点放入桶里
      if ((p = tab[i = (n - 1) & hash]) == null)
          //此位置没有对象创建新的node
          tab[i] = newNode(hash, key, value, null);
      else {// 桶中已经存在元素
          Node<K,V> e; K k;
            // 比较桶中第一个元素(数组中的结点)的hash值相等，key相等
          if (p.hash == hash &&
              ((k = p.key) == key || (key != null && key.equals(k))))
              e = p;
             // hash值不相等，即key不相等；为红黑树结点
          else if (p instanceof TreeNode)
              e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
          else {//链表
             // 在链表最末插入结点
              for (int binCount = 0; ; ++binCount) {
                  // 是否到达表表尾部
                  if ((e = p.next) == null) {
                        // 在尾部插入新结点
                      p.next = newNode(hash, key, value, null);
                       // 判断是否需要转换为红黑树
                      if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                          treeifyBin(tab, hash);
                      break;
                  }
                    // 判断链表中结点的key值与插入的元素的key值是否相等
                  if (e.hash == hash &&
                      ((k = e.key) == key || (key != null && key.equals(k))))
                      break;
                  p = e;
              }
          }
           // 表示在桶中找到key值hash值与插入元素相等的结点
          if (e != null) { // existing mapping for key
              V oldValue = e.value;
              if (!onlyIfAbsent || oldValue == null)
                  // 替换
                  e.value = value;
              afterNodeAccess(e);
              return oldValue;
          }
      }
      ++modCount;
      
      if (++size > threshold)// 实际大小大于阈值则扩容
          resize();
      afterNodeInsertion(evict);
      return null;
  }
  
  
  // 扩容 
  final Node<K,V>[] resize() {
          Node<K,V>[] oldTab = table;
          int oldCap = (oldTab == null) ? 0 : oldTab.length;
          int oldThr = threshold;
          int newCap, newThr = 0;
          if (oldCap > 0) {
              // 超过最大值就不再扩了，
              if (oldCap >= MAXIMUM_CAPACITY) {
                  //阈值设置为最大，返回久数组
                  threshold = Integer.MAX_VALUE;
                  return oldTab;
              }
                // 没超过最大值，就扩充为原来的2倍
              else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                       oldCap >= DEFAULT_INITIAL_CAPACITY)
                  newThr = oldThr << 1; // double threshold
          }
          else if (oldThr > 0) // initial capacity was placed in threshold
         //旧数组无长度，长度设置为旧的容量的大小
                   newCap = oldThr;
          else {               // zero initial threshold signifies using defaults
              newCap = DEFAULT_INITIAL_CAPACITY;
              newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
          }
      // 计算新的resize上限
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
               // 把每个bucket都移动到新的buckets中
              for (int j = 0; j < oldCap; ++j) {
                  Node<K,V> e;
                  if ((e = oldTab[j]) != null) {
                      oldTab[j] = null;
                      if (e.next == null)
                          newTab[e.hash & (newCap - 1)] = e;
                      else if (e instanceof TreeNode)
                          ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                      else { // preserve order
                          Node<K,V> loHead = null, loTail = null;
                          Node<K,V> hiHead = null, hiTail = null;
                          Node<K,V> next;
                          do {
                              next = e.next;
                              // 原索引
                              if ((e.hash & oldCap) == 0) {
                                  if (loTail == null)
                                      loHead = e;
                                  else
                                      loTail.next = e;
                                  loTail = e;
                              }
                              // 原索引+oldCap
                              else {
                                  if (hiTail == null)
                                      hiHead = e;
                                  else
                                      hiTail.next = e;
                                  hiTail = e;
                              }
                          } while ((e = next) != null);
                           // 原索引放到bucket里
                          if (loTail != null) {
                              loTail.next = null;
                              newTab[j] = loHead;
                          }
                           // 原索引+oldCap放到bucket里
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

## 总结

进行扩容，会伴随着一次重新hash分配，并且会遍历hash表中所有的元素，是非常耗时的。在编写程序中，合理给定初始容量，就算适当浪费空间也要尽量避免resize。








HashMap
定义
public class HashMap<K,V> extends AbstractMap<K,V> implements Map<K,V>, Cloneable, Serializable
HashMap没有什么要说的，直接切入正题，初始化一个HashMap。

初始化
HashMap map = new HashMap();
通过这个方法会调用HashMap的无参构造方法。

//两个常量 向下追踪
public HashMap() {
  this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
}

//无参构造创建对象之后 会有两个常量
//DEFAULT_INITIAL_CAPACITY 默认初始化容量 16  这里值得借鉴的是位运算
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
//DEFAULT_LOAD_FACTOR 负载因子默认为0.75f 负载因子和扩容有关 后文详谈
static final float DEFAULT_LOAD_FACTOR = 0.75f;

//最大容量为2的30次方
static final int MAXIMUM_CAPACITY = 1 << 30;

//以Node<K,V>为元素的数组，长度必须为2的n次幂
transient Node<K,V>[] table;

//已经储存的Node<key,value>的数量，包括数组中的和链表中的，逻辑长度
transient int size;

threshold 决定能放入的数据量，一般情况下等于 Capacity * LoadFactor
通过上述代码我们不难发现，HashMap的底层还是数组（注意，数组会在第一次put的时候通过 resize() 函数进行分配），数组的长度为2的N次幂。

在HashMap中，哈希桶数组table的长度length大小必须为2的n次方(一定是合数)，这是一种非常规的设计，常规的设计是把桶的大小设计为素数。相对来说素数导致冲突的概率要小于合数，Hashtable初始化桶大小为11，就是桶大小设计为素数的应用（Hashtable扩容后不能保证还是素数）。HashMap采用这种非常规设计，主要是为了在取模和扩容时做优化，同时为了减少冲突，HashMap定位哈希桶索引位置时，也加入了高位参与运算的过程。

那么Node<K,V>是什么呢？

//一个静态内部类 其实就是Map中元素的具体存储对象  
static class Node<K,V> implements Map.Entry<K,V> {
  		//每个储存元素key的哈希值
        final int hash;
  		//这就是key-value
        final K key;
        V value;
  		//next 追加的时候使用，标记链表的下一个node地址
        Node<K,V> next;
		
        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
此时我们就拥有了一个空的HashMap，下面我们看一下put

put
JDK8 HashMap put的基本思路：

对key的hashCode()进行hash后计算数组下标index;
如果当前数组table为null，进行resize()初始化；
如果没碰撞直接放到对应下标的位置上；
如果碰撞了，且节点已经存在，就替换掉 value；
如果碰撞后发现为树结构，挂载到树上。
如果碰撞后为链表，添加到链表尾，并判断链表如果过长(大于等于TREEIFY_THRESHOLD，默认8)，就把链表转换成树结构；
数据 put 后，如果数据量超过threshold，就要resize。
public V put(K key, V value) {
  //调用putVal方法 在此之前会对key做hash处理
  return putVal(hash(key), key, value, false, true);
}
//hash
static final int hash(Object key) {
  int h;
 // h = key.hashCode() 为第一步 取hashCode值
 // h ^ (h >>> 16)  为第二步 高位参与运算
  //具体的算法就不解释了 作用就是性能更加优良
  return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}

//进行添加操作
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
  Node<K,V>[] tab; Node<K,V> p; int n, i;
  //如果当前数组table为null，进行resize()初始化
  if ((tab = table) == null || (n = tab.length) == 0)
    n = (tab = resize()).length;
  //(n - 1) & hash 计算出下标 如果该位置为null 说明没有碰撞就赋值到此位置
  if ((p = tab[i = (n - 1) & hash]) == null)
    tab[i] = newNode(hash, key, value, null);
  else {
    //反之 说明碰撞了  
    Node<K,V> e; K k;
    //判断 key是否存在 如果存在就覆盖原来的value  
    if (p.hash == hash &&
        ((k = p.key) == key || (key != null && key.equals(k))))
      e = p;
    //没有存在 判断是不是红黑树
    else if (p instanceof TreeNode)
      //红黑树是为了防止哈希表碰撞攻击，当链表链长度为8时，及时转成红黑树，提高map的效率
      e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
    //都不是 就是链表 
    else {
      for (int binCount = 0; ; ++binCount) {
        if ((e = p.next) == null) {
          //将next指向新的节点
          p.next = newNode(hash, key, value, null);
          //这个判断是用来判断是否要转化为红黑树结构
          if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
            treeifyBin(tab, hash);
          break;
        }
        // key已经存在直接覆盖value
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
  if (++size > threshold)
    resize();
  afterNodeInsertion(evict);
  return null;
}
在刚才的代码中我们提到了红黑树是为了防止**哈希表碰撞攻击，当链表链长度为8时，及时转成红黑树，提高map的效率。**那么接下来说一说什么是哈希表碰撞攻击。

现在做web开发RESTful风格的接口相当的普及，因此很多的数据都是通过json来进行传递的，而json数据收到之后会被转为json对象，通常都是哈希表结构的，就是Map。

我们知道理想情况下哈希表插入和查找操作的时间复杂度均为O(1)，任何一个数据项可以在一个与哈希表长度无关的时间内计算出一个哈希值（key），从而得到下标。但是难免出现不同的数据被定位到了同一个位置，这就导致了插入和查找操作的时间复杂度不为O(1)，这就是哈希碰撞。

java的中解决哈希碰撞的思路是单向链表和黑红树，上文提到红黑树是JDK8之后添加，为了防止哈希表碰撞攻击，为什么？。

不知道你有没有设想过这样一种场景，添加的所有数据都碰撞在一起，那么这些数据就会被组织到一个链表中，随着链表越来越长，哈希表会退化为单链表。哈希表碰撞攻击就是通过精心构造数据，使得所有数据全部碰撞，人为将哈希表变成一个退化的单链表，此时哈希表各种操作的时间均提升了一个数量级，因此会消耗大量CPU资源，导致系统无法快速响应请求，从而达到拒绝服务攻击（DoS）的目的。

我们需要注意的是红黑树实际上并不能解决哈希表攻击问题，只是减轻影响，防护该种攻击还需要其他的手段，譬如控制POST数据的数量。

扩容resize()
不管是list还是map，都会遇到容量不足需要扩容的时候，但是不同于list，HashMap的扩容设计的非常巧妙，首先在上文提到过数组的长度为2的N次方，也就是说初始为16，扩容一次为32... 好处呢？就是上文提到的扩容是性能优化和减少碰撞，就是体现在此处。

数组下标计算： index = (table.length - 1) & hash ，由于 table.length 也就是capacity 肯定是2的N次方，使用 & 位运算意味着只是多了最高位，这样就不用重新计算 index，元素要么在原位置，要么在原位置+ oldCapacity.

如果增加的高位为0，resize 后 index 不变；高位为1在原位置+ oldCapacity。resize 的过程中原来碰撞的节点有一部分会被分开。

扩容简单说有两步：

1.扩容

创建一个新的Entry空数组，长度是原数组的2倍。

2.ReHash

遍历原Entry数组，把所有的Entry重新Hash到新数组。

//HashMap的源码真的长  0.0  这段改天补上
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
          newTab[e.hash & (newCap - 1)] = e;
        else if (e instanceof TreeNode)
          ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
        else { // preserve order
          Node<K,V> loHead = null, loTail = null;
          Node<K,V> hiHead = null, hiTail = null;
          Node<K,V> next;
          do {
            next = e.next;
            if ((e.hash & oldCap) == 0) {
              if (loTail == null)
                loHead = e;
              else
                loTail.next = e;
              loTail = e;
            }
            else {
              if (hiTail == null)
                hiHead = e;
              else
                hiTail.next = e;
              hiTail = e;
            }
          } while ((e = next) != null);
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
为什么HashMap是线程不安全的
由于源码过长，HashMap的其他方法就不写了。下面说一下关于HashMap的一些问题

1.如果多个线程同时使用put方法添加元素会丢失元素

假设正好存在两个put的key发生了碰撞，那么根据HashMap的实现，这两个key会添加到数组的同一个位置，这样最终就会发生其中一个线程的put的数据被覆盖。

2.多线程同时扩容会造成死循环

多线程同时检查到扩容，并且执行扩容操作，在进行rehash的时候会造成闭环链表，从而在get该位置元素的时候，程序将会进入死循环。【证明HashMap高并发下问题会在以后的文章中出现】

如何让HashMap实现线程安全？

直接使用Hashtable
Collections.synchronizeMap方法
使用ConcurrentHashMap 下篇文章就是分析ConcurrentHashMap是如何实现线程安全的
总结
HashMap 在第一次 put 时初始化，类似 ArrayList 在第一次 add 时分配空间。
HashMap 的 bucket 数组大小一定是2的n次方
HashMap 在 put 的元素数量大于 Capacity * LoadFactor（默认16 * 0.75） 之后会进行扩容
负载因子是可以修改的，也可以大于1，但是建议不要轻易修改，除非情况非常特殊
JDK8处于提升性能的考虑，在哈希碰撞的链表长度达到TREEIFY_THRESHOLD（默认8)后，会把该链表转变成树结构
JDK8在 resize 的时候，通过巧妙的设计，减少了 rehash 的性能消耗
扩容是一个特别耗性能的操作，所以当在使用HashMap的时候，估算map的大小，初始化的时候给一个大致的数值，避免map进行频繁的扩容
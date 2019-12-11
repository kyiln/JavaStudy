# HashMap
HashMap采用key-value的存储结构，每个唯一key对应一个唯一的value，通常情况下HashMap的查询和修改时间复杂度为O(1)，因为是散列存储，HashMap不能保证元素存储的顺序，且线程不安全。

```java
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {
 ```
 （1）继承了AbstractMap，实现了Map接口，具备Map的所有功能
 （2）实现了Cloneable，可以被克隆
 （3）实现了Serializable，可以被序列化

### 属性
```java
/**
     * 默认初始容量为16
     * 容量必须指定为2的n次方, 目的是为了使hash函数能够更加有效的获取散列值
     * index = hashCode & (capacity - 1)
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * 最大容量 = 2^30
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 默认负载因子
     * 意味着当HashMap的容量被使用75%的时候会进行扩容
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 当一个桶中的链表长度大于等于8时转化为红黑树
     */
    static final int TREEIFY_THRESHOLD = 8;

    /**
     * 当一个桶中的链表长度小于等于6时转化为链表
     */
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * 当桶的个数到达64个才能够进行树化
     */
    static final int MIN_TREEIFY_CAPACITY = 64;

	/* ---------------- Fields -------------- */

    /**
     * 位桶数组
     */
    transient Node<K,V>[] table;

    /**
     * 作为entrySet()的缓存
     */
    transient Set<Map.Entry<K,V>> entrySet;

    /**
     * Map中的元素个数
     */
    transient int size;

    /**
     * 修改次数
     */
    transient int modCount;

    /**
     * 当位桶数组的数量到达多少时可以进行扩容 , shreshold = (capacity * load factor).
     */
    int threshold;

    /**
     * 负载因子
     */
    final float loadFactor;
```

### Node内部类
```java
	/**
     * 典型的单链表节点
     */
    static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;//用于存储通过hash函数处理后的key.hashCode()
        final K key;
        V value;
        Node<K,V> next;
		....
    }
```

### TreeNode内部类
```java
	static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
    TreeNode<K,V> parent;  // red-black tree links
    TreeNode<K,V> left;
    TreeNode<K,V> right;
    TreeNode<K,V> prev;    // needed to unlink next upon deletion
    boolean red;
```

### 构造方法
```java
	/**
     * 1. 指定初始容量和负载因子的构造方法
     */
    public HashMap(int initialCapacity, float loadFactor) {
        //判断初始Capacity是否合法
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        //检查负载因子是否合法
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);
        this.loadFactor = loadFactor;
        //计算容量门槛
        this.threshold = tableSizeFor(initialCapacity);
    }

	/**
     * 将Capacity转换为往上取最近的2的n次方
     * 该算法的思想就是将Capacity的有效二进制位转换为全1, 然后加1取到二进制位
     * 例如(14)2 = 1100, 1100低位全部转换为1, 1100 -> 1111, 1111 + 1 = 100000
     * 如果日常需要类似的算法场景, 就可以直接从这里照搬了(●'◡'●)
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;//-1是为了避免一个二进制数被转换为更大的二进制数
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * 2. 只指定初始容量的构造方法, 底层调用了第一个构造方法, 对其设置了默认负载因子
     */
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 3. 空构造方法, 使用默认负载因子
     */
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }
```

### put(K key, V value) 添加键值对
（1）计算节点key的hash值  
（2）如果是刚初始化的map，调用resize()初始化位桶数组  
（3）hash&(n - 1)计算出newNode存放的下标值  
（4）如果下标位置桶为空，那么直接放入newNode即可，跳转到步骤（7）
（5）如果下标位置桶不为空：  
　　（5.1）观察桶上第一个节点的key与newNode.key是否相同，若相同保存该节点，跳转到步骤（6）
　　（5.2）若不相同，且第一个节点为TreeNode，则按照红黑树的方式进行添加
　　（5.3）若不相同，且第一个节点不为红黑树，则遍历链表寻找具有相同key的节点，若找到了保存该节点，跳转到步骤（6）；若未找到则在末尾添加newNode
　　（5.4）观察添加newNode后是否需要树化
（6）将保存的具有相同key的节点value进行更新，并返回oldValue
（7）观察是否需要扩容，若需要调用resize()
（8）因为寻找到相同key的结果会在步骤（6）中return，这里只可能存在未找到相同key的情况，return null

```java
	public V put(K key, V value) {
		//调用hash方法计算key的hash值, 然后进行putVal
		//如果替换了相同key节点的value, 那么return oldValue, else return null
		
        return putVal(hash(key), key, value, false, true);
    }
	
	static final int hash(Object key) {
        int h;
    	//if key = null, return 0, else return (key的32位hashCode异或key的高16位)
    	//目的时为了让hash更加分散
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
	
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab;
        Node<K,V> p; 
        int n, i;
        //如果桶数组为空, 则初始化位桶数组(Lazy-load)
        if ((tab = table) == null || (n = tab.length) == 0)
            //调用resize()初始化, 将初始化后的位桶数组长度赋给n
            n = (tab = resize()).length;
        //-------------------------important---------------------------//
        //这里就是HashMap非常经典的计算下标算法了
        //(n - 1) & hash这个算法充分利用了位桶数组的长度n和hash值计算出更加散列的下标
        //观察该下标是否存在元素, 如果为空就直接把newNode放入
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        //如果该位置上已经存在了节点
        else {
            Node<K,V> e; 
            K k;
            //如果桶中第一个节点的key与待插入节点的key相同, 保存该结点为e, 用于后续修改value
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            //如果桶中第一个节点的key与待插入节点的key不相同, 且第一个节点是树化的节点
            else if (p instanceof TreeNode)
            	//此时调用putTreeVal方法将node插入
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            //如果桶中第一个节点的key与待插入节点的key不相同, 且第一个节点是链表节点
            else {
            	//那么就需要遍历这个链表, 寻找相同key的节点    
                for (int binCount = 0; ; ++binCount) {
                    //如果链表遍历完了都没有找到相同key的节点, 则在末尾追加新节点
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        //如果插入新节点之后, 链表节点的长度大于等于8, 则需要进行链表树化
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    //如果找到了相同的key的节点, 则退出循环
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            //e != null, 说明找到了相同key的节点, 那么需要进行value替换
            if (e != null) { // existing mapping for key
                //记录旧值
                V oldValue = e.value;
                //判断是否需要替换旧值
                if (!onlyIfAbsent || oldValue == null)
                    //替换旧值为新值
                    e.value = value;
                //在节点被访问后需要做点什么事, LinkedListHashMap中用到    
                afterNodeAccess(e);
                //返回旧值
                return oldValue;
            }
        }
        //下面这些代码会处理没有寻找到相同key节点的情况
        ++modCount;
        //观察放入Node之后的size是否需要扩容
        if (++size > threshold)
            resize();
        //在节点被访问后做点什么事, 在LinkedHashMap中用到
        afterNodeInsertion(evict);
        //未替换相同key节点的value, return null
        return null;
    }
```

### resize()方法
（1）如果使用默认构造方法，则第一次插入元素时初始化容量为16，扩容门槛为12  
（2）如果使用非默认构造方法，则第一次插入元素时初始化容量等于扩容门槛（初始容量往上取2的n次方）  
（3）如果旧容量大于0，设置新容量和新扩容门槛  
（4）创建新容量的桶  
（5）搬迁元素  
```java
	/**
     * 对位桶数组的扩容方法
     * 这里是创建了一个新的位桶数组, 并将老的位桶数组搬家到新数组中
     */
    final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        //如果oldCapacity > 0
        if (oldCap > 0) {
            //且oldCapacity已经到达最大容量, 那么不再进行扩容, 直接返回oldTable
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            //oldCapacity*2 < 最大容量, 并且oldCapacity >= 默认初始容量(16)
            //那么新容量 = oldCapacity*2, 新扩容门槛 = oldThreshold*2
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        //使用非默认构造方法指定initialCapacity创建的map, 第一次put时会进入这里, capa
        //如果旧容量为0, 且旧扩容门槛大于0, 则把oldThreShold = 往上取2^n 赋值给新容量
        else if (oldThr > 0)
            newCap = oldThr;
        //调用默认构造方法创建的map, 第一次put会进入这里
        else {               // zero initial threshold signifies using defaults
        	//newCapacity = 16
            newCap = DEFAULT_INITIAL_CAPACITY;
            //newThreshold = 0.75 * 16
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        //如果新扩容门槛为0, 在保证不超过最大容量的情况下, 设置新扩容门槛为newCapacity*负载因子
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        //将新扩容门槛赋值给HashMap.threshold
        threshold = newThr;
        //根据新容量新建一个位桶数组
        @SuppressWarnings({"rawtypes","unchecked"})
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        //将新数组赋值给HashMap.table
        table = newTab;
        //如果旧数组不为空, 那么需要把原来的元素搬到新的位桶数组中
        if (oldTab != null) {
        	//遍历旧数组
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                //如果旧数组的当前位置不为空, 
                if ((e = oldTab[j]) != null) {
                	//清空旧数组便于GC回收
                    oldTab[j] = null;
                    //若当前位置的桶只存在一个元素
                    if (e.next == null)
                    	//只需要计算该元素在新桶中的位置然后搬到新桶中即可
                        newTab[e.hash & (newCap - 1)] = e;
                    //若当前位置的桶下, 第一个节点为TreeNode
                    else if (e instanceof TreeNode)
                    	//那么把这棵树打散成两棵树搬到新桶中
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { 
                    	//将链表分化为两个链表存放到新位桶数组中
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {//遍历链表所有节点
                            next = e.next;
                            //当前节点的hash对oldCapacity取余 == 0, 将其归为lowLinkedList
                            if ((e.hash & oldCap) == 0) {
                                //若lowLinkedList为空, 头节点为该节点
                                if (loTail == null)
                                    loHead = e;
                                else
                                	//否则尾部追加
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {//若不满足取余==0的条件, 则以同样的操作将该节点赋给highLinkedList
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        //遍历完成后就将原链表分化为2个链表了
                        //低位链表在新桶中的位置还是与旧桶一样
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        //高位链表在新桶中的位置刚好实在原位置之上加上旧容量
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



### get(Object key) 获取map中key对应的value
（1）计算key的hash值
（2）通过计算找到key所在的桶数组下标
（3）如果第一个节点就是要查找的key节点，return
（4）如果第一个节点不是，且第一个节点是TreeNode，那么通过红黑树的方式查找
（5）如果第一个节点不是，且第一个节点是链表，那么遍历链表查找
```java
	public V get(Object key) {
        Node<K,V> e;
        //根据传入的key计算其hash, 并下到位桶数组中寻找对应桶的位置
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; 
        Node<K,V> first, e; 
        int n; 
        K k;
        //若位桶数组不为空且长度>0, 且根据hash值计算出的下标对应的桶下存在节点
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
            //检查第一个节点是否是要查的元素, if true, return value
            if (first.hash == hash && // always check first node
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
    		//若第一个节点不是, 且下一个节点不为空
            if ((e = first.next) != null) {
            	//下一个节点若是TreeNode, 则按红黑树的方式查找
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                //否则遍历整个链表查找相同key的节点
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }
```

### remove(Object key) 根据传入key删除节点
（1）计算key的hash值
（2）计算下标，看下标对应的桶上第一个节点是否是我们要删除的
（3）若是，保存该结点
（4）若不是，且头结点是TreeNode，按照红黑树的方式遍历获取到该节点
（5）若不是，且头结点是链表，则遍历链表获取到该结点
（6）观察保存的结点是否是TreeNode，如果是则按照红黑树的方式删除
（7）若不是TreeNode，则按照链表的方式删除

```java
	public V remove(Object key) {
        Node<K,V> e;
        //计算出key的hash值, 然后下到removeNode方法进行删除
        return (e = removeNode(hash(key), key, null, false, true)) == null ?
            null : e.value;
    }

    final Node<K,V> removeNode(int hash, Object key, Object value,
                               boolean matchValue, boolean movable) {
        Node<K,V>[] tab; Node<K,V> p; int n, index;
        //如果桶数组不为空且长度>0, 并且计算出下标对应的桶上存在节点
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (p = tab[index = (n - 1) & hash]) != null) {
            Node<K,V> node = null, e; K k; V v;
            //如果桶上第一个节点恰好是要删除的, 赋值给node后续删除使用
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                node = p;
            //如果桶上第一个节点不是我们寻找的, 且该节点的next节点不为空
            else if ((e = p.next) != null) {
            	//如果桶上第一个节点是TreeNode
                if (p instanceof TreeNode)
                	//按照红黑树的方式遍历获取到该节点
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                //否则遍历链表查找要删除的节点
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
            //如果找到了要删除的节点, 则看参数是否需要匹配value值, 如果不需要匹配value值则直接删除, 否则判断value是否相同
            if (node != null && (!matchValue || (v = node.value) == value ||
                                 (value != null && value.equals(v)))) {
                if (node instanceof TreeNode)
                	//如果该节点是TreeNode, 按照红黑树的方式删除
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
                //如果删除的元素是第一个节点, 把next节点移动到头节点位置
                else if (node == p)
                    tab[index] = node.next;
                else//否则删除node节点
                    p.next = node.next;
                ++modCount;
                --size;
                //删除节点的后续处理
                afterNodeRemoval(node);
                //成功删除, 返回删除节点
                return node;
            }
        }
        //若删除失败, 返回null
        return null;
    }
```

### 总结
（1）HashMap是一种散列表，采用数组 + 链表 + 红黑树存储结构  
（2）若未预先指定，HashMap的初始容量是16，负载因子是0.75  
（3）若预先指定，HashMap的初始容量必须是2的n次方  
（4）HashMap除了通过默认构造创建时扩容门槛是16*0.75，其余情况下每次扩容容量为原来的两倍，扩容门槛也为原来的两倍  
（5）当桶数组的数量<64时不会进行树化，只会扩容  
（6）当桶数组的数量>64，且桶中元素个数大于8，进行树化  
（7）当桶中元素小于6，进行反树化  
（8）非线程安全  
（9）通常情况下，查找和添加元素的时间复杂度都是O(1)  

一些需要注意的点写在末尾：
### 1. 扩容导致的性能影响
因为每一次调用resize()方法，都会创建一次新的位桶数组，并且将旧数组中的元素移动到新数组中，整个过程非常耗时，因此推荐使用HashMap(int initialCapacity)这个构造器，并在最初就尽量指定好容量大小。

### 2. 为什么HashMap要树化
HashMap在大多数情况下，查询的时间复杂度为O(1)，且HashMap的扰动函数和散列处理也足够高效了，可以理解为即便存在链表，这个链表也不会太长。那么为什么要大费周折添加一个红黑树的结构呢？
其本质是一个安全问题，在现实环境下，构建冲突的数据并不是非常复杂的事，恶意代码就可以利用这些数据大量与服务端进行交互，导致服务端CPU大量被占用，这就构成了hash碰撞拒接服务攻击。树化可以一定程度上减少碰撞攻击带来的性能损失。

### 3. 线程不安全
HashMap是线程不安全的，并发场景下很可能出现两个线程同时对HashMap进行操作导致死锁问题，在并发场景下建议使用ConcurrentHashMap。
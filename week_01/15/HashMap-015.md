### [Java 8] HashMap

`HashMap` 是哈希表的基本实现，不是线程安全的。`HashMap` 底层主要存储是一个数组 `table`，数组中每个元素称为一个桶。将 `key` 通过哈希函数得 `key.hashCode()` 到哈希值 `hash`，再将 `hash` 按照桶的个数（即数组长度）取模得到该 `key` 所映射的桶（即数组的索引）。  

因此从 `key` 到桶的映射过程可能会碰撞，即不同的 `key` 可能会映射到同一个桶，因此桶内需要能存多个键值对。桶默认使用链表存储多个键值对。

如果碰撞过多会严重影响 `HashMap` 的性能，本来算个 `hash` 在取个模再比较个 `key` 三部曲就完事的工作，在碰撞时第三步要遍历链表挨个比较键值找到要查找的 `key`，这样 *O(1)* 的时间复杂度退化为 *O(K)*，其中K为桶内键值对数。

因此为了减小碰撞带来的性能退化，有两种策略分别针对不同的场景：  
1. 假设哈希函数分布还是不错的，但因为桶数量太少了，广泛分布的 `hash` 被压缩到少量的桶中不碰撞才怪。既然这样，就扩容桶的数量，原先映射到一个桶就可能分散到不同的桶。比如桶的数量为4，`hash` 为3和7的 `key` 都会映射到索引为3的桶，但把桶扩容到8后，两者就分别映射到索引为3和7的桶；当然等键值对数增长到桶的数量再扩容有点晚了，肯定已经发生一些碰撞了，试想多牛逼的哈希函数配上多么契合的场景才能保证一个桶只落一个键值对呢。因此需要一个阈值，键值对数超过阈值就扩容。

2. 假设哈希函数实现得比较烂，一堆不同的 `key` 通过哈希函数计算后都是差不多的 `hash`，桶再多又有毛用，全TMD映射到少量的桶中，桶里链表又特别长。既然这样，那就提高键值对多的桶内查找 `key` 的效率，用红黑树替换链表，将 *O(K)* 补救到 *O(logK)*


#### 常量及实例变量
``` java
// 默认桶的数量，必须是2的整数次幂
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

// 桶的最大数量
static final int MAXIMUM_CAPACITY = 1 << 30;

// 默认负载因子
static final float DEFAULT_LOAD_FACTOR = 0.75f;

// 桶内链表转化为红黑树的键值对数量阈值
static final int TREEIFY_THRESHOLD = 8;

// 桶内红黑树转化为链表的键值对数量阈值
static final int UNTREEIFY_THRESHOLD = 6;

// 当桶的数量没有达到这个阈值时，桶内链表不会转化为红黑树
static final int MIN_TREEIFY_CAPACITY = 64;

// 桶数组
transient Node<K,V>[] table;

// 键值对总数
transient int size;

// HashMap修改次数（确切地说是结构变更次数，不包括修改已存在key的value），其实相当于当前集合快照版本，用于迭代器遍历时检查集合是否被修改
transient int modCount;

// 桶数组扩容阈值
int threshold;

// 负载因子
final float loadFactor;
```
负载因子 `loadFacotr` 是桶数组相对扩容阈值，是一个相对于桶数量的比例（可以大于1），因此绝对阈值 `threshold` 就是桶的数量 `table.length` 乘以负载因子 `loadFactor` 。当键值对总数 `size` 达到 `threshold` 时，触发 `resize` 方法进行桶数组 `table` 扩容。 因此 `loadFactor` 可以理解为 `HashMap` 时间和空间的权衡  

`loadFactor` 是 `HashMap` 初始化时可以指定的，如果未指定则默认为 `DEFAULT_LOAD_FACTOR`    

桶的数量即 `table` 的大小也是 `HashMap` 初始化时可以指定的，如果未指定则默认为 `DEFAULT_INITIAL_CAPACITY`  

`table` 也不是无限扩容的，最多支持 `MAXIMUM_CAPACITY` 个桶  

`table` 的长度必须是2的整数次幂，这是为了取模运算更高效，即hash对2的整数次幂n取模可以用骚操作位运算 `hash & (n - 1)` 。这是也是为什么默认值 `DEFAULT_INITIAL_CAPACITY` 及最大值 `MAXIMUM_CAPACITY` 也要求是2的整数次幂

`table` 的元素是 `HashMap.Node<K, V>` 类型，`HashMap.Node<K, V>` 是默认的链表节点，`HashMap.TreeNode<K,V>` 是红黑树节点，继承了 `HashMap.Node<K, V>` 。既然链表有头树有根，`table` 中就只引用一个头/根节点即可。
 
当某个桶内键值对数量超过 `TREEIFY_THRESHOLD` 时将触发 `treeifyBin` 方法将这个桶的链表转化为红黑树，当然这有个大前提，就是当前桶数量不少于 `MIN_TREEIFY_CAPACITY` ，因为桶很少的时候冲突的可能性就是非常高，这时就因为某个链太长就转为红黑树太鲁莽了，怎么也得先多弄几个桶看看是桶太少还是哈希函数太烂

由于 `HashMap.TreeNode<K,V>` 空间几乎是 `HashMap.Node<K, V>` 的2倍，因此在性能提升不大的情况下链表没必要转化为红黑树，另外对于良好实现分布均匀的哈希函数，冲突的概率很小，对应于这种情况就应该只有极低的概率链表转化红黑树。对于服从常数为0.5的泊松分布的哈希函数，8个 `key` 落到同一个桶中的概率只有0.00000006，因此将 `TREEIFY_THRESHOLD` 设为8可以满足上面的论断

如果某个桶已经转化为红黑树，`resize` 后原先桶里的键值对可能落到不同的桶中，即触发 `split`方法，`split` 后树节点可能很少了，浪费多一倍的空间没什么必要了，可以转化回链表，即触发 `untreeify` 。如果没有这个操作，多次 `resize` 和 `split` 后链表可能有很多的桶只有很少的节点，但却使用红黑树结构。`split` 后一个桶内红黑树节点降到多少转化回链表受阈值 `UNTREEIFY_THRESHOLD` 控制  


#### 哈希
``` java
static final int hash(Object key) {
    int h;
    // HashMap允许key为null，对应的hash为0
    // 由于hash之后要 &(table.length-1)确定桶索引，只有比table.length唯一的1的位低的位保留下来，高位信息都被过滤掉了
    // 对于哈希函数分布不均的情况这里挣扎了下，将hash低16位和高16位做异或，这样高位信息也会反应在低位中，降低某些哈希函数实现只在高位变化从而碰撞的概率，当然这里也只是用开销很小的位操作，因为分布平均的哈希函数不需要挣扎，而实现比较烂的哈希函数有转化红黑树兜底
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```  


#### 构造函数及相关辅助方法
``` java
public HashMap(int initialCapacity, float loadFactor) {
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal initial capacity: " +
                                            initialCapacity);
    // 桶数量不超过MAXIMUM_CAPACITY
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new IllegalArgumentException("Illegal load factor: " +
                                            loadFactor);
    this.loadFactor = loadFactor;
    // 调用tableSizeFor得到是桶数量，这里却赋值给了threshold，这是一个构造时的临时处理，因为table是延迟初始化的，并且没有专门的字段存储桶容量，因此先扔给threshold存着，具体等第一次resize初始化table时再将真正的扩容阈值赋给threshold
    this.threshold = tableSizeFor(initialCapacity);
}

public HashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
}

public HashMap() {
    this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
}

public HashMap(Map<? extends K, ? extends V> m) {
    this.loadFactor = DEFAULT_LOAD_FACTOR;
    putMapEntries(m, false);
}

// 计算大于等于cap的最小的2的整数幂
static final int tableSizeFor(int cap) {
    // -1是专门针对cap正好就是2的整数幂这种情况
    int n = cap - 1;
    // 将n最高1位右边的位都设置为1
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    // 再+1正好就是2的整数幂
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}

final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
    // 获取传进来的键值对数量
    int s = m.size();
    if (s > 0) {
        // 判断table是否初始化
        if (table == null) { // pre-size
            // 用键值对数量除以负载因子倒推桶容量，加上1预防浮点数计算误差，这里还不是真正的桶容量，因为还未向上取最小2的整数幂
            float ft = ((float)s / loadFactor) + 1.0F;
            // 桶容量不超过MAXIMUM_CAPACITY
            int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                        (int)ft : MAXIMUM_CAPACITY);
            // table没初始化时threshold暂存桶容量，或者threshold为0表示使用默认桶数量，无论哪种情况，这里将通过键值对反推的桶容量取向上最小的2的整数幂赋给threshold，在第一次resize时用来初始化table
            if (t > threshold)
                threshold = tableSizeFor(t);
        }
        else if (s > threshold)
            // table已经初始化了，但发现键值对数量超过扩容阈值了，那就赶紧先resize，不要等到putVal再resize
            resize();
        // 遍历将每个键值对增加到该HashMap中
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            K key = e.getKey();
            V value = e.getValue();
            putVal(hash(key), key, value, false, evict);
        }
    }
}
```  


#### 桶容量
``` java
// 这里恰好和构造函数呼应，体现了table延迟初始化前后桶容量是如何保存的
final int capacity() {
    // 1. table若已初始化，当然table.length就是桶容量
    // 2. 若table未初始化，且threshold大于0，对应HashMap前两个构造函数，参数指定了桶容量，暂存在threshold中
    // 3. 若table未初始化，且threshold等于0，对应HashMap第三个构造函数（无参），没有指定桶容量则使用默认桶容量
    return (table != null) ? table.length :
        (threshold > 0) ? threshold :
        DEFAULT_INITIAL_CAPACITY;
}
```


#### 扩容
``` java
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    int oldThr = threshold;
    int newCap, newThr = 0;
    if (oldCap > 0) {
        // oldCap大于0，说明table已经初始化
        if (oldCap >= MAXIMUM_CAPACITY) {
            // 桶容量已经达到MAXIMUM_CAPACITY了，再也扩不动了
            // 将threshold设置为Integer.MAX_VALUE，再也不触发resize
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        // 桶容量扩容一倍
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                    oldCap >= DEFAULT_INITIAL_CAPACITY)
            // 只有扩容后容量未达到MAXIMUM_CAPACITY并且扩容前容量不低于DEFAULT_INITIAL_CAPACITY时才将threshold增大一倍
            // 第一个条件类似上面的分支，扩容后如果桶容量达到MAXIMUM_CAPACITY，那么threshold就应该设置为Integer.MAX_VALUE而不是傻傻地增大一倍，这个操作在下面newThr==0的分支中处理
            // 第二个条件是因为当桶容量很小的时候，threshold移位操作带来的小数点上的误差影响非常大，应该由扩容后的桶容量乘以负载因子重新计算，这同样交给下面newThr==0的分支中计算
            newThr = oldThr << 1; // double threshold
    }
    else if (oldThr > 0) // initial capacity was placed in threshold
        // table未初始化但threshold大于0，说明指定容量构造后第一次resize，threshold暂存的就是table初始化的容量，这里正式移交给桶容量变量，threshold本身则由下面newThr==0的分支中计算
        newCap = oldThr;
    else {               // zero initial threshold signifies using defaults
        // table未初始化且threshold等于0，说明无参构造后第一次resize，桶容量使用默认容量，threshold直接由默认容量乘以负载因子计算
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    if (newThr == 0) {
        // 计算扩容阈值
        float ft = (float)newCap * loadFactor;
        // 如果桶容量达到MAXIMUM_CAPACITY或扩容阈值达到MAXIMUM_CAPACITY，直接将threshold设置为Integer.MAX_VALUE，否则将计算好的阈值赋给threshold
        // 之所以还要判断计算的阈值是否达到MAXIMUM_CAPACITY是因为loadFactor是可能大于1的
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                    (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
    // 用扩容容量构造新table
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab;
    // 如果table不是第一次初始化，则需要将旧table的键值对迁移到新table中，键值对可能在新table中落到另外一个桶中
    if (oldTab != null) {
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {
                // 移除旧table中引用在这次循环后尽早回收
                oldTab[j] = null;
                // e.next为null说明该桶内只有一个节点
                if (e.next == null)
                    // 直接将这个节点移到新table中hash对应的索引即可
                    newTab[e.hash & (newCap - 1)] = e;
                // e.next不为null说明桶内有多个节点，可能是链表也可能是红黑树
                else if (e instanceof TreeNode)
                    // 该桶是红黑树
                    // 很有可能拆成两棵分别迁移到不同的桶
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { // preserve order
                    // 该桶是链表
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    do {
                        next = e.next;
                        // 由于oldCap是2的整数幂，只有唯一的1，e.hash&oldCap得到e.hash相应这一位的信息。如果结果为0，则说明 e.hash % newCap < oldCap，则扩容前后该节点落到相同索引的桶（低索引半区）；但如果结果为1，则说明 oldCap <= e.hash % newCap < newCap，扩容后该节点将落在新扩展的高索引半区并且与扩容前桶索引（低索引半区）相差oldCap
                        if ((e.hash & oldCap) == 0) {
                            // 通过低索引链表尾节点判断低索引桶是否有节点
                            if (loTail == null)
                                // 低索引桶第一个节点，设置低索引链表头节点
                                loHead = e;
                            else
                                // 追加到低索引链表尾节点
                                loTail.next = e;
                            loTail = e;
                        }
                        else {
                            // 通过高索引链表尾节点判断高索引桶是否有节点
                            if (hiTail == null)
                                // 高索引桶第一个节点，设置高索引链表头节点
                                hiHead = e;
                            else
                                // 追加到高索引链表尾节点
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    if (loTail != null) {
                        loTail.next = null;
                        // 设置低索引桶
                        newTab[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        // 设置高索引桶
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```  

#### 键值对总数
``` java
public int size() {
    return size;
}

public boolean isEmpty() {
    return size == 0;
}
``` 


#### 增/改键值对
``` java
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}

final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    // 判断table是否初始化
    if ((tab = table) == null || (n = tab.length) == 0)
        // 触发resize初始化table
        n = (tab = resize()).length;
    // 判断所属桶是否有节点
    if ((p = tab[i = (n - 1) & hash]) == null)
        // 没有节点则直接创建链表头节点
        tab[i] = newNode(hash, key, value, null);
    else {
        // 该桶已经有节点
        Node<K,V> e; K k;
        // 判断头/根节点是否是要找的key
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        // 判断是否是红黑树根节点
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            // 链表有多个节点且头节点不是要找的key，则向下遍历链表
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    // 遍历了一圈没有找到key，则说明需要新增一个键值对
                    p.next = newNode(hash, key, value, null);
                    // 判断该桶内链表节点数量是否达到转化红黑树阈值TREEIFY_THRESHOLD，这里用TREEIFY_THRESHOLD - 1是因为头节点已经在循环前遍历过了
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        // 达到转化红黑树的阈值，转化红黑树
                        treeifyBin(tab, hash);
                    break;
                }
                // 判断该节点是否是要找的key
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        // 如果找到了key，则说明是一个值更新操作
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
    // 递增键值对总数size，判断结果是否超过扩容阈值threshold
    if (++size > threshold)
        // 超过threshold，调用resize扩容
        resize();
    afterNodeInsertion(evict);
    return null;
}

public void putAll(Map<? extends K, ? extends V> m) {
    putMapEntries(m, true);
}
```  


#### 转化红黑树
``` java
final void treeifyBin(Node<K,V>[] tab, int hash) {
    int n, index; Node<K,V> e;
    // 如果桶容量小于MIN_TREEIFY_CAPACITY，说明桶数量还太少，则扩容而不是转化红黑树
    if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
        resize();
    else if ((e = tab[index = (n - 1) & hash]) != null) {
        TreeNode<K,V> hd = null, tl = null;
        do {
            TreeNode<K,V> p = replacementTreeNode(e, null);
            if (tl == null)
                hd = p;
            else {
                p.prev = tl;
                tl.next = p;
            }
            tl = p;
        } while ((e = e.next) != null);
        if ((tab[index] = hd) != null)
            hd.treeify(tab);
    }
}
```  


#### 查找
``` java
public V get(Object key) {
    Node<K,V> e;
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}

final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
    // 判断table是否初始化以及hash对应的桶是否有节点
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (first = tab[(n - 1) & hash]) != null) {
        // 判断头/根节点是否是要找的key
        if (first.hash == hash && // always check first node
            ((k = first.key) == key || (key != null && key.equals(k))))
            return first;
        // 如果头/根节点不是要找的key，且桶内还有其他节点
        if ((e = first.next) != null) {
            // 判断是否是红黑树，如是则在红黑树内查找key
            if (first instanceof TreeNode)
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
            // 否则遍历链表
            do {
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    return e;
            } while ((e = e.next) != null);
        }
    }
    // 没找到key
    return null;
}
```  


#### 删除
``` java
public V remove(Object key) {
    Node<K,V> e;
    return (e = removeNode(hash(key), key, null, false, true)) == null ?
        null : e.value;
}

final Node<K,V> removeNode(int hash, Object key, Object value,
                            boolean matchValue, boolean movable) {
    Node<K,V>[] tab; Node<K,V> p; int n, index;
    // 与getNode类似，查找key的节点
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (p = tab[index = (n - 1) & hash]) != null) {
        Node<K,V> node = null, e; K k; V v;
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
        // 判断节点是否找到且满足值匹配条件(如果开启值匹配选项)
        if (node != null && (!matchValue || (v = node.value) == value || (value != null && value.equals(v)))) {
            // 判断是否是红黑树节点
            if (node instanceof TreeNode)
                // 移除红黑树节点
                ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
            // 判断是否是链表头节点
            else if (node == p)
                // 无论是下一个节点还是null，都直接赋给桶
                tab[index] = node.next;
            else
                // 父节点next直接指向子节点，及删除链表当前节点
                p.next = node.next;
            ++modCount;
            // 递减键值对总数
            --size;
            afterNodeRemoval(node);
            return node;
        }
    }
    // 没找到key
    return null;
}

public void clear() {
    Node<K,V>[] tab;
    modCount++;
    if ((tab = table) != null && size > 0) {
        size = 0;
        // 清除所有桶对头/根节点的引用
        for (int i = 0; i < tab.length; ++i)
            tab[i] = null;
    }
}
```

# HashMap 源码分析

## init
```java
// initialCapacity 初始化容量大小
// loadFactor 负载因子
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
        // threshold 是HashMap是否要进行扩容的标记量
        this.threshold = tableSizeFor(initialCapacity);
    }
```

## putVal
```java
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;

        // table 为 null 说明是首次调用 put 方法 , 进行 resize 操作真正为 table 分配存储空间

        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;

        // i = (n - 1) & hash 计算出的值判断 table[i] 是否为 null , 
        // 如果为 null 就为 key ， value 创建一个新的 Node 节点 ,
        // 不需要进行碰撞检测直接存储在 table 中 i 的位置上 

        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {
            Node<K,V> e; K k;

            // 检测要存储的 key 是否和 bucket 中存储的头节点相同
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;

            // 检测 bucket 中当前存放的节点类型是不是红黑树结构 ,
            // 是红黑树结构 , 存储为一个红黑树节点

            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {

                // 这个 bucket 中存放的节点是链表结构 , 
                // 循环直到链表的末尾或者是找到相同的 key 

                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);

                        // 存储新节点的时候 ， 检测链表长度是否超过 TREEIFY_THRESHOLD - 1 ,
                        // 超过的话将链表转换为红黑树结构 ,提高性能

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

        // 并发修改计数器 ,有并发修改就抛异常 ConcurrentModificationException
        ++modCount;

        // 存储了一个新节点 ， 检测 size 是否超过 threshold 
        // 如果超过了要进行 resize 操作
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }
```

## getNode
```java
final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;

        // 检测 table 中是否已经存储了节点 ，
        // 检测key所在的 bucket 是否存储了节点 ，
        // 以上两点都不满足说明 key 不存在

        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {

            // 对 bucket 中存储节点的头结点进行碰撞检测 ,
            // 如果运气好的话只需要进行这一次碰撞检测

            if (first.hash == hash && // always check first node
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;

            // 检测 bucket 存储的节点是否是单个节点

            if ((e = first.next) != null) {

                // 检测节点数据结构是否是红黑树

                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                do {

                    // 节点是链表数据结构，循环直到链表末尾或者是发现 key 一致的节点
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }
```
## removeNode
```java
final Node<K,V> removeNode(int hash, Object key, Object value,
                               boolean matchValue, boolean movable) {
        Node<K,V>[] tab; Node<K,V> p; int n, index;
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
                    
                        // p 是 node 的前一个节点
                        p = e;
                    } while ((e = e.next) != null);
                }
            }

            // 以上代码是 getNode(hash , key) , 个人觉得这个函数中的代码冗余了

            // 获取到 key 对应的节点 ， 判断是否要进行值匹配

            if (node != null && (!matchValue || (v = node.value) == value ||
                                 (value != null && value.equals(v)))) {

                // 进行删除操作 , 红黑树的删除是比较复杂的 , 链表的删除十分简单
                                     
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

## clear
```java
 public void clear() {
        Node<K,V>[] tab;
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;

            // 很简单把数组中每个位置设置为 null
            for (int i = 0; i < tab.length; ++i)
                tab[i] = null;
        }
    }
```
## HashMap 总结

initialCapacity ， loadFactor  这两个参数都影响着HashMap的性能。initialCapacity 决定了下一次 resize 后的容量（capacity << 1） ,  loadFactor  决定了 resize 发生的条件 （size > (capacity * loadFactor )） （一般情况下 ， 极端情况下是 size > Integer.MAX_VALUE） 。如果初始化时不指定这两个参数，会使用默认值 ， capacity  = 16 ， loadFactor  = 0.75 。对于 16 的容量空间，如果不能充分利用的话会造成空间资源的浪费。

散列的过程就是将存入的元素均匀的分布到HashMap内部Node数据的过程。均匀分布指的是 ， 数组中的每个位置尽量都存储了一个Node节点，并且该位置上的链表只有一个元素。散列分布的越均匀进行碰撞检测的次数就越少，查询性能就越高。

散列较为均匀的，查询时最好情况下可以直接定位，最坏情况下需要进行一次碰撞检测。

散列的不均匀的，查询时会进行多次碰撞检测造成效率较低。

 ((capacity - 1) & hash) 会计算出 key 存储在 Node 数组中的那个位置上 （得到的值始终会落在 Node数组的长度范围内 ， 等同于 hash % capacity  ， 不过位运算的效率更高）， 如果发现该位置上已经存在Node 了，会将新存入的数据作为链表的尾节点。这样存储和查询时都会进行碰撞检测。碰撞检测其实就是比较传入的 key 的 hash 与同一 bucket 上所有的 key 的 hash 是否一致的过程。 jdk8 在这方面做了优化，加入了树型结构来弥补链表线性结构性能较低的不足。

提高碰撞检测的性能，从代码中也能看出来， 该运算的最理想情况（hash 相等情况下）是执行两步 ， 比较 hash ， 比较 key 。 最坏情况是执行 4 步 ， 只要取最好情况就达到了提高性能的目的 。以此类推 key 就可以用一些 String ， Enum 这之类的数据类型。

rezise 是一个较为消耗性能的过程 ， 在首次向HashMap中存入元素的时候会进行首次resize ,  在之后每当产生新节点（这里的节点指的是Node） ， 同时 size > threshold 的时候会进行 resize ，resize 的过程也是 rehash 的过程。

HashMap 是不支持并发的 ， 在并发修改时它采用 fail-fast 的策略 ， 抛出 ConcurrentModificationException 。 多线程环境下操作HashMap有可能会造成死循环 ， 在 resize 的过程当中。不要在多线程场景下使用HashMap 。
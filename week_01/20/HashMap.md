#### 简介
+ HashMap采用key/value存储结构，每个key对应唯一的value，查询和修改的速度都很快，能达到O(1)的平均时间复杂度。它是非线程安全的，且不保证元素存储的顺序。
+ HashMap实现了Cloneable，可以被克隆。
+ HashMap实现了Serializable，可以被序列化。
+ HashMap继承自AbstractMap，实现了Map接口，具有Map的所有功能。
#### 存储结构
+ ![](https://mmbiz.qpic.cn/mmbiz_png/C91PV9BDK3ybgqMRZDOdr5w7uDsHFrhqTibA17Zqqibm0Nwe5d0nxB2q3nDLSertDuNJfvT7F6kicSs7k9O5CpvKA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)
+ 数组 + 链表 + 红黑树（O(1)、O(k)、O(logk)）
  
#### 源码解析
+ 成员变量
    ```java
    //默认的初始容量，必须是2的幂。
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
    //最大容量（必须是2的幂且小于2的30次方，传入容量过大将被这个值替换）
    static final int MAXIMUM_CAPACITY = 1 << 30;
    //默认装载因子，默认值为0.75，如果实际元素所占容量占分配容量的75%时就要扩容了。如果填充比很大，说明利用的空间很多，但是查找的效率很低，因为链表的长度很大（当然最新版本使用了红黑树后会改进很多），HashMap本来是以空间换时间，所以填充比没必要太大。但是填充比太小又会导致空间浪费。如果关注内存，填充比可以稍大，如果主要关注查找性能，填充比可以稍小。
    static final float _LOAD_FACTOR = 0.75f;

    //一个桶的树化阈值
    //当桶中元素个数超过这个值时，需要使用红黑树节点替换链表节点
    //这个值必须为 8，要不然频繁转换效率也不高
    static final int TREEIFY_THRESHOLD = 8;

    //一个树的链表还原阈值
    //当扩容时，桶中元素个数小于这个值，就会把树形的桶元素 还原（切分）为链表结构
    //这个值应该比上面那个小，至少为 6，避免频繁转换
    static final int UNTREEIFY_THRESHOLD = 6;

    //哈希表的最小树形化容量
    //当哈希表中的容量大于这个值时，表中的桶才能进行树形化
    //否则桶内元素太多时会扩容，而不是树形化
    //为了避免进行扩容、树形化选择的冲突，这个值不能小于 4 * TREEIFY_THRESHOLD
    static final int MIN_TREEIFY_CAPACITY = 64;

    //存储数据的Entry数组，长度是2的幂。
    transient Entry[] table;
    //
    transient Set<Map.Entry<K,V>> entrySet;
    //map中保存的键值对的数量
    transient int size;
    //需要调整大小的极限值（容量*装载因子）
    int threshold;
    //装载因子
    final float loadFactor;
    //map结构被改变的次数
    transient volatile int modCount;
    ```
+ 计算阀值
  ```java
    static final int tableSizeFor(int cap) {
        //经过下面的 或 和位移 运算， n最终各位都是1。
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        //判断n是否越界，返回 2的n次方作为 table（哈希桶）的阈值
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    } 
  ``` 
+ 扩容
  ```java
    final Node<K,V>[] resize() {
        //oldTab 为当前表的哈希桶
        Node<K,V>[] oldTab = table;
        //当前哈希桶的容量 length
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        //当前的阈值
        int oldThr = threshold;
        //初始化新的容量和阈值为0
        int newCap, newThr = 0;
        //如果当前容量大于0
        if (oldCap > 0) {
            //如果当前容量已经到达上限
            if (oldCap >= MAXIMUM_CAPACITY) {
                //则设置阈值是2的31次方-1
                threshold = Integer.MAX_VALUE;
                //同时返回当前的哈希桶，不再扩容
                return oldTab;
            }//否则新的容量为旧的容量的两倍。 
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                oldCap >= DEFAULT_INITIAL_CAPACITY)
                //如果旧的容量大于等于默认初始容量16
                //那么新的阈值也等于旧的阈值的两倍
                newThr = oldThr << 1; // double threshold
        }
        //如果当前表是空的，但是有阈值。代表是初始化时指定了容量、阈值的情况
        else if (oldThr > 0) 
            newCap = oldThr;//那么新表的容量就等于旧的阈值
        else {    
        //如果当前表是空的，而且也没有阈值。代表是初始化时没有任何容量/阈值参数的情况               
            newCap = DEFAULT_INITIAL_CAPACITY;//此时新表的容量为默认的容量 16
        //新的阈值为默认容量16 * 默认加载因子0.75f = 12
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            //如果新的阈值是0，对应的是  当前表是空的，但是有阈值的情况
            float ft = (float)newCap * loadFactor;//根据新表容量 和 加载因子 求出新的阈值
            //进行越界修复
            newThr = (newCap < MAXIMUM_CAPACITY && ft <(float)MAXIMUM_CAPACITY ? (int)ft : Integer.MAX_VALUE);
        }
        //更新阈值 
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
        //根据新的容量 构建新的哈希桶
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        //更新哈希桶引用
        table = newTab;
        //如果以前的哈希桶中有元素
        //下面开始将当前哈希桶中的所有节点转移到新的哈希桶中
        if (oldTab != null) {
            //遍历老的哈希桶
            for (int j = 0; j < oldCap; ++j) {
            //取出当前的节点 e
            Node<K,V> e;
            //如果当前桶中有元素,则将链表赋值给e
            if ((e = oldTab[j]) != null) {
                //将原哈希桶置空以便GC
                oldTab[j] = null;
                //如果当前链表中就一个元素，（没有发生哈希碰撞）
                if (e.next == null)
                //直接将这个元素放置在新的哈希桶里。
                //注意这里取下标 是用 哈希值 与 桶的长度-1 。 由于桶的长度是2的n次方，这么做其实是等于 一个模运算。但是效率更高
                newTab[e.hash & (newCap - 1)] = e;
                //如果发生过哈希碰撞 ,而且是节点数超过8个，转化成了红黑树
                else if (e instanceof TreeNode)
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                //如果发生过哈希碰撞，节点数小于8个。则要根据链表上每个节点的哈希值，依次放入新哈希桶对应下标位置。
                else {
                    //因为扩容是容量翻倍，所以原链表上的每个节点，现在可能存放在原来的下标，即low位，或者扩容后的下标，即high位。high位=low位+原哈希桶容量
                    //低位链表的头结点、尾节点
                    Node<K,V> loHead = null, loTail = null;
                    //高位链表的头节点、尾节点
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;//临时节点 存放e的下一个节点
                    do {
                        next = e.next;
                    　　//利用位运算代替常规运算：利用哈希值与旧的容量，可以得到哈希值去模后，是大于等于oldCap还是小于oldCap，等于0代表小于oldCap，应该存放在低位，否则存放在高位
                        if ((e.hash & oldCap) == 0) {
                            //给头尾节点指针赋值
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        }//高位也是相同的逻辑
                        else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                            }//循环直到链表结束
                        } while ((e = next) != null);
                        //将低位链表存放在原index处
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        //将高位链表存放在新index处
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
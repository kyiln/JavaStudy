基于jdk8的HashMap源码分析：
周末才有空写，写之前看了大家写的内容，有些源码写的详细的我就略过了，主要写一下自己的一些见解；
1、关于容量和负载因子，如果负载因子太小，会造成resize()频繁调用，造成新能消耗
2、如果负载因子过大，会对容器空间的利用更加充分，但是会增加查找时间
3、生成2的幂次方的容量，可以采用这种方法
static final int tableSizeFor(int cap) {
        int n = cap - 1; 
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
4、添加元素
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        if ((tab = table) == null || (n = tab.length) == 0)   //判断是否是没出初始化的容器
            n = (tab = resize()).length;                       //对容器初始化
        if ((p = tab[i = (n - 1) & hash]) == null)              //判断队列所在的地址是否有值，没有则生成一个 node
            tab[i] = newNode(hash, key, value, null);
        else {
            Node<K,V> e; K k;
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))   //判断key值是否相同。如果hash值不一样，则key值肯定不一样
                e = p;
            else if (p instanceof TreeNode)                                 //判断是否是红黑树结构
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
                for (int binCount = 0; ; ++binCount) {                  //遍历node 下的链表，直到next没有值，在链表的最后一个添加 node
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) //对于node 中可能会出现第一个key值不相同，而后面的值相同的情况
                        break;
                    p = e;
                }
            }
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;   // 返回被替换的值
            }
        }
        ++modCount;
        if (++size > threshold)   //当前size大于下次预设值，就会触发resize()
            resize();
        afterNodeInsertion(evict);
        return null;
    }
    
5、resize()方法
final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;             
        int newCap, newThr = 0;
        // 生成 新的容量大小，以及预设值
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
            for (int j = 0; j < oldCap; ++j) {          //遍历原来的容器
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {          //对数组节点下有值得才进行处理
                    oldTab[j] = null;
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;      //使用新的容器（数组的下标与hash进行与运算，确定新容器（table）的数组下标）
                    else if (e instanceof TreeNode)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);      //为红黑树处理
                    else { // preserve order                     //对于所在数组节点有链表的情况
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
6、对于HashMap不是安全的，主要是发生在多线程put元素的时候，可能引起 死循环，可以参考
https://blog.csdn.net/bjwfm2011/article/details/81076736  描述的很清楚
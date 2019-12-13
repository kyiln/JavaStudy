    继承AbstractMap
    >实现Map, Cloneable, Serializable
    >JDK版本	    实现方式	                    节点数>=8	    节点数<=6
    >1.8以前	    数组+单向链表	            数组+单向链表	数组+单向链表
    >1.8以后	数组+单向链表+红黑树(提高查找效率)	数组+红黑树	    数组+单向链表

    主要参数：
    * DEFAULT_INITIAL_CAPACITY（1 << 4 -> 初始容量大小）
    * MAXIMUM_CAPACITY（1 << 30 -> 容量极限大小）
    * DEFAULT_LOAD_FACTOR（负载因子默认大小）
    * TREEIFY_THRESHOLD（节点数大于8时会转为红黑树存储）
    * UNTREEIFY_THRESHOLD（节点数小于6时会转为单向链表存储）
    * MIN_TREEIFY_CAPACITY（红黑树最小长度为64）
    * Node<K, V>（实现Map.Entry<K,V> , 单向链表）
        Node是Map.Entry接口的实现类，存储数据的Node数组容量是2次幂，每个Node本质都是一个单向链表
    * TreeNode<K,V>（extends LinkedHashMap.Entry<K,V> , 红黑树）    
    * size（HashMap大小，代表HashMap保存的键值对的大小）
    * modCount（被改变的次数）
    * threshold（下一次HashMap扩容的大小->put时根据oldCap和newCap比较确定下一次扩容大小）
    * loadFactor（存储负载因子的常量）
    
    1、hash的计算:
    //将传入的参数key本身的hashCode与h无符号右移16位进行二进制异或运算得出一个新的hash值
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
    那为什么要这么做呢？直接通过key.hashCode()获取hash不得了吗? 为什么在右移16位后进行异或运算？
        与HashMap的table数组下计算标有关系
        //put函数代码块中
        tab[i = (n - 1) & hash]) 
        //get函数代码块中
        tab[(n - 1) & hash])
        根据索引得到tab中节点数据,key.hashCode()>>>16经过^异或运算和经过&与运算后得到的二进制是一致的
        当发生较大碰撞时也用树形存储降低了冲突。减少了系统的开销
    1.1、put方法
        1、首先获取Node数组table对象和长度，若table为null或长度为0，则调用resize()扩容方法获取table最新对象，并通过此对象获取长度大小
            if ((tab = table) == null || (n = tab.length) == 0)
                n = (tab = resize()).length;
        2、判定数组中指定索引下的节点是否为Null，若为Null则new出一个单向链表赋给table中索引下的这个节点
            if ((p = tab[i = (n - 1) & hash]) == null)
                tab[i] = newNode(hash, key, value, null)
        3、若判定不为Null,我们的判断再做分支
            3.1、首先对hash和key进行匹配,若判定成功直接赋予e
                if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
                    e = p;
            3.2、若匹配判定失败,则进行类型匹配是否为TreeNode 若判定成功则在红黑树中查找符合条件的节点并将其回传赋给e
                else if (p instanceof TreeNode)
                    e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            3.3、若以上判定全部失败则进行最后操作,向单向链表中添加数据若单向链表的长度大于等于8,
                则将其转为红黑树保存，记录下一个节点,对e进行判定若成功则返回旧值
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
            综合以上判断得出：        
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }            
        4、最后判定数组大小需不需要扩容
            if (++size > threshold)
                resize()
    1.2、get方法
        1、判定三个条件 table不为Null & table的长度大于0 & table指定的索引值不为Null
            if ((tab = table) != null && (n = tab.length) > 0 &&(first = tab[(n - 1) & hash]) != null) 
        2、判定 匹配hash值 & 匹配key值 成功则返回该值
            if (first.hash == hash && ((k = first.key) == key || (key != null && key.equals(k))))
                return first
        3、若first节点的下一个节点不为Null 
                if ((e = first.next) != null)
            3.1、若first的类型为TreeNode红黑树，通过红黑树查找匹配值并返回查询值
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key)
            3.2、若上面判定不成功则认为下一个节点为单向链表,通过循环匹配值
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
    
    
    2、三个构造方法
    //指定容量大小
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }
     //指定容量大小和负载因子大小
    public HashMap(int initialCapacity, float loadFactor) {
        //指定的容量大小不可以小于0,否则将抛出IllegalArgumentException异常
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
         //判定指定的容量大小是否大于HashMap的容量极限
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
         //指定的负载因子不可以小于0或为Null，若判定成立则抛出IllegalArgumentException异常
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        this.loadFactor = loadFactor;
        //设置HashMap阈值，当HashMap中存储数据的数量达到threshold时，就需要将HashMap的容量加倍。
        this.threshold = tableSizeFor(initialCapacity);
    }
    //传入Map集合,将Map集合中元素Map.Entry全部添加进HashMap实例中
    public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        //此构造方法主要实现了Map.putAll()
        putMapEntries(m, false);
    }
一.HashMap 概述
    HashMap是一个键对应一个值的Map集合，它在查找和插入时的效率极高，理论上可以达到O(1).
    HashMap是利用hash函数对所需要存入的值进行计算存储位置。
    Hash函数会计算不通值之间的时候，会发生冲突，就叫hash冲突。
    哈希冲突的解决方案有多种:开放定址法（发生冲突，继续寻找下一块未被占用的存储地址）（顺序寻找下一个未被占用地址，效率低），
    再散列函数法（利用值的平方，再散列寻找未被占用的地址），链地址法，而HashMap即是采用了链地址法，也就是数组+链表的方式。
二.HashMap 继承结构
    1.HashMap继承了AbstractMap<K,V>
    2.HashMap实现了Map<K,V>，（可以使用一些Map的操作）
    3.HashMAp实现了 Cloneable, （可被克隆）
    4.HashMap实现了Serializable，（可序列化）
三.构造方法
    // 1.无参构造方法、
    // 构造一个空的HashMap，初始容量为16，负载因子为0.75
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }
// 2.构造一个初始容量为initialCapacity，负载因子为0.75的空的HashMap，
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }        
    /// 等待更新
四.核心方法
   
五.总结
    
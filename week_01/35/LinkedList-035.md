    LinkedList继承AbstractSequentialList的双向循环链表。它也可以被当作堆栈、队列或双端队列进行操作
    实现List, Deque(双端队列), Cloneable, Serializable接口,非同步的
    
    主要参数：
        size：集合的长度
        first：双向链表头部节点
        last：双向链表尾部节点
    
    与ArrayList比较：
    * 内部使用链表实现，相比于ArrayList更加耗费空间。
    * 插入，删除节点不用大量copy原来元素，效率更高。
    * 查找元素使用遍历，效率一般。
    * 双向队列的实现
    
    
    构造函数：
    * 构造一个空的LinkedList
    public LinkedList() {
        //将header节点的前一节点和后一节点都设置为自身
        header.next = header. previous = header ;
    }
    
    * 构造一个包含指定 collection 中的元素的列表，这些元素按其 collection 的迭代器返回的顺序排列
    public LinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }
    
    新增元素
    public boolean add(E e) {
        linkLast(e);
        return true;
    }
    
    在指定位置插入新元素
    public void add(int index, E element) {
        checkPositionIndex(index);//index位置检查（不能小于0，大于size）-->index >= 0 && index <= size，否则抛出异常IndexOutOfBoundsException
        if (index == size) //如果index==size，直接在链表最后插入，相当于调用add(E e)方法
            linkLast(element); //size++,没有扩容机制
        else
            linkBefore(element, node(index));
    }
    
    //判断index在链表的哪边。遍历查找index或者size，找出对应节点
    //相比于数组的直接索引获取，遍历获取节点效率不高
    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }
    
    //检查index位置
    // 调用node方法获取节点，接着调用unlink(E e)
    //相比于ArrayList的copy数组覆盖原来节点，效率同样更高
    public E remove(int index) {
       checkElementIndex(index);
       return unlink(node(index));
    }
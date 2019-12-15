一.LinkedList概述
    1.LinkedList基于双向链表适用于增删频繁且查询不频繁的场景.
    2.线程不安全的且适用于单线程（这点和ArrayList很像）。
    3.可以用LinkedList来实现栈和队列.
二.LinkedList继承结构
    1.LinkedList继承了AbstractSequentialList
    2.LinkedList实现了List，（集合功能）
    3.LinkedList实现了Deque （双端队列功能，模拟栈和队列）
    4.LinkedList实现了Cloneable（克隆数组）
    5.LinkedList实现了Serializable（序列化）
三.构造方法
    transient int size = 0;
    transient Node<E> first;
    transient Node<E> last;
    private static class Node<E> { //内部类，定义一个节点
        E item;//当前元素的value
        Node<E> next;//下一个元素。null代表尾元素
        Node<E> prev;//上一个元素。null代表首原素
        //定义一个节点
        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
    //无参构造函数        
    public LinkedList() {
    }
    //添加一个集合
    public LinkedList(Collection<? extends E> c) {
       this();
       addAll(c);
    }
    
四.核心方法
    //在头部增加一个节点
    private void linkFirst(E e) {
            final Node<E> f = first;
            final Node<E> newNode = new Node<>(null, e, f);
            first = newNode;
            if (f == null)
                last = newNode;
            else
                f.prev = newNode;
            size++;
            modCount++;
    }
    //在尾部增加一个节点
    void linkLast(E e) {
            final Node<E> l = last;
            final Node<E> newNode = new Node<>(l, e, null);
            last = newNode;
            if (l == null)
                first = newNode;
            else
                l.next = newNode;
            size++;
            modCount++;
    }
    //在节点succ之前加入一个节点
    void linkBefore(E e, Node<E> succ) {
            // assert succ != null;
            final Node<E> pred = succ.prev;
            //在pred和succ之间插入newNode
            final Node<E> newNode = new Node<>(pred, e, succ);
            succ.prev = newNode;
            if (pred == null) //是否是头元素
                first = newNode;
            else
                pred.next = newNode;
            size++; //计数加1
            modCount++;
        }
    //删除头元素，并返回头元素的值
    private E unlinkFirst(Node<E> f) {
            // assert f == first && f != null;
            final E element = f.item;
            final Node<E> next = f.next;
            f.item = null;
            f.next = null; // help GC
            first = next;
            if (next == null)
                last = null;
            else
                next.prev = null;
            size--;
            modCount++;
            return element;
    }
    //索引第index个节点
    Node<E> node(int index) {
            //assert isElementIndex(index);
            if (index < (size >> 1)) {
                Node<E> x = first;
                for (int i = 0; i < index; i++)
                    x = x.next;
                return x;
            } else {
                Node<E> x = last;
                for (int i = size - 1; i > index; i--)
                    x = x.prev;
                return x;
            }
        }
    2.其他方法类似
五.总结
    1.LinkedList方法内部实现是链表，且内部有fist与last指针操作数据
    2.LinkedList线程不安全的，因为其内部添加、删除、等操作，没有进行同步操作。
    3.LinkedList增删元素速度较快。
    4.遍历效率(快-慢)：
        Iterator迭代 > for循环
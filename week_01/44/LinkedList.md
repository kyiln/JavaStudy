# 一：简介

概括的说，LinkedList 是线程不安全的，允许元素为null的双向链表。
其底层数据结构是链表，它实现List<E>, Deque<E>, Cloneable, java.io.Serializable接口，它实现了Deque<E>,所以它也可以作为一个双端队列。和ArrayList比，没有实现RandomAccess所以其以下标，随机访问元素速度较慢。

因其底层数据结构是链表，所以可想而知，它的增删只需要移动指针即可，故时间效率较高。不需要批量扩容，也不需要预留空间，所以空间效率比ArrayList高。

缺点就是需要随机访问元素时，时间效率很低，虽然底层在根据下标查询Node的时候，会根据index判断目标Node在前半段还是后半段，然后决定是顺序还是逆序查询，以提升时间效率。不过随着n的增大，总体时间效率依然很低。

当每次增、删时，都会修改modCount。
————————————————
参考：https://blog.csdn.net/zxt0601/article/details/77341098

​	彤哥读源码

# 二：源码分析

## 1. 主要属性和node节点

```Java
// 元素个数
transient int size = 0;
// 链表首节点
transient Node<E> first;
// 链表尾节点
transient Node<E> last;
```

```Java
// 从Node定义可以看出是个双向链表
private static class Node<E> {
    E item;
    Node<E> next;
    Node<E> prev;

    Node(Node<E> prev, E element, Node<E> next) {
        this.item = element;
        this.next = next;
        this.prev = prev;
    }
}
```

## 2. 构造方法

```Java
    public LinkedList() {
    }

    public LinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }
```

简单的构造方法，一个空参，一个调用addAll()函数添加集合C的元素

## 3. 添加元素

因为继承了Deque，是个双端队列，所以添加元素主要有两种方式，一种是在队列尾部添加元素，一种是在队列首部添加元素。而作为List，也能去中间插入数据。

```Java
// 从队首添加元素
private void linkFirst(E e) {
    // 存储首节点
    final Node<E> f = first;
    // 创建一个新节点，它的next是首节点
    final Node<E> newNode = new Node<>(null, e, f);
    // 首节点更新为新节点
    first = newNode;
    // 判断是否是第一个元素
    // 是把last也置为新节点
    // 否则把原首节点的prev指针更新为新节点
    if (f == null)
        last = newNode;
    else
        f.prev = newNode;
    // 元素个数加1
    size++;
    modCount++;
}

// 从队尾添加元素
void linkLast(E e) {
    // 尾节点
    final Node<E> l = last;
    // 创建新节点，其prev节点是尾节点
    final Node<E> newNode = new Node<>(l, e, null);
    // 判断是不是第一个添加的元素    
    // 如果是就把first也置为新节点
    // 否则把原尾节点的next指针置为新节点
    last = newNode;
    if (l == null)
        first = newNode;
    else
        l.next = newNode;
    // 元素个数加1
    size++;
    modCount++;
}

public void addFirst(E e) {
        linkFirst(e);
}

public void addLast(E e) {
        linkLast(e);
}

public boolean offerFirst(E e) {
    addFirst(e);
    return true;
}

public boolean offerLast(E e) {
    addLast(e);
    return true;
}

// 在节点succ之前添加元素
void linkBefore(E e, Node<E> succ) {
    // assert succ != null;
    final Node<E> pred = succ.prev;
    final Node<E> newNode = new Node<>(pred, e, succ);
    succ.prev = newNode;
    if (pred == null)
        first = newNode;
    else
        pred.next = newNode;
    size++;
    modCount++;
}


```

## 4. 删除元素

```Java
    private E unlinkLast(Node<E> l) {
        // assert l == last && l != null;
        final E element = l.item;
        final Node<E> prev = l.prev;
        l.item = null;
        l.prev = null; // help GC
        last = prev;
        if (prev == null)
            first = null;
        else
            prev.next = null;
        size--;
        modCount++;
        return element;
    }

    E unlink(Node<E> x) {
        // assert x != null;
        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        x.item = null;
        size--;
        modCount++;
        return element;
    }
```

## 5. 栈

```Java
public void push(E e) {
        addFirst(e);
}

public void push(E e) {
        addFirst(e);
}
```

# 三：总结

（1）LinkedList是一个以双链表实现的List；

（2）LinkedList还是一个双端队列，具有队列、双端队列、栈的特性；

（3）LinkedList在队列首尾添加、删除元素非常高效，时间复杂度为O(1)；

（4）LinkedList在中间添加、删除元素比较低效，时间复杂度为O(n)；

（5）LinkedList不支持随机访问，所以访问非队列首尾的元素比较低效；

（6）LinkedList在功能上等于ArrayList + ArrayDeque。

参考：彤哥读源码
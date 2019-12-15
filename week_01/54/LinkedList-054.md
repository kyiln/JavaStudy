###### LinkedList基于jdk1.8源码分析

**成员**

transient int size = 0;//list节点个数

/**
 * Pointer to first node.指向头节点
 * Invariant: (first == null && last == null) ||
 *            (first.prev == null && first.item != null)
 */
transient Node<E> first;

/**
 * Pointer to last node.//指向尾节点
 * Invariant: (first == null && last == null) ||
 *            (last.next == null && last.item != null)
 */
transient Node<E> last;

**构造函数**

/**
 * Constructs an empty list.//创建一个空list
 */
public LinkedList() {
}

/**
 * Constructs a list containing the elements of the specified
 * collection, in the order they are returned by the collection's
 * iterator.
 *
 * @param  c the collection whose elements are to be placed into this list
 * @throws NullPointerException if the specified collection is null
 */
public LinkedList(Collection<? extends E> c) {
//把一个Collection变为LinkedList
    this();
    addAll(c);
}

**核心方法**

添加

/**
 * Appends the specified element to the end of this list.
 *
 * <p>This method is equivalent to {@link #addLast}.
 *
 * @param e element to be appended to this list
 * @return {@code true} (as specified by {@link Collection#add})
 */
public boolean add(E e) {
//添加一个元素，通过linkLast方法
    linkLast(e);
    return true;
}

/**
 * Links e as last element.向尾部添加一个元素
 */
void linkLast(E e) {
    final Node<E> l = last;
    final Node<E> newNode = new Node<>(l, e, null);以尾部元素为前面节点next创建一个新节点
    last = newNode;
    if (l == null)
    //判断是否为空链表
        first = newNode;//头节点为newNode
    else
        l.next = newNode;//将原来的尾部next插入需要插入的节点
    //更新链表的次数以及大小
    size++;
    modCount++;
}

/**
 * Inserts the specified element at the specified position in this list.
 * Shifts the element currently at that position (if any) and any
 * subsequent elements to the right (adds one to their indices).
 *
 * @param index index at which the specified element is to be inserted
 * @param element element to be inserted
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public void add(int index, E element) {
//指定位置插入元素
    checkPositionIndex(index);//检查位置是否合理

    if (index == size)
        linkLast(element);//如果位置是在最后一位采用尾插法
    else
        linkBefore(element, node(index));
}

/**
 * Inserts element e before non-null Node succ.
 */
void linkBefore(E e, Node<E> succ) {
    // assert succ != null;
    final Node<E> pred = succ.prev;//插入位置的前节点
    final Node<E> newNode = new Node<>(pred, e, succ);//新建节点为succ的前节点
    succ.prev = newNode;succ的前节点为newNode
    if (pred == null)
    //前节点为null 表示为头节点  ，直接first为newNode
        first = newNode;
    else
    //插入位置的前节点的next为newNode
        pred.next = newNode;
    size++;
    modCount++;
}

/**
 * Appends all of the elements in the specified collection to the end of
 * this list, in the order that they are returned by the specified
 * collection's iterator.  The behavior of this operation is undefined if
 * the specified collection is modified while the operation is in
 * progress.  (Note that this will occur if the specified collection is
 * this list, and it's nonempty.)
 *
 * @param c collection containing elements to be added to this list
 * @return {@code true} if this list changed as a result of the call
 * @throws NullPointerException if the specified collection is null
 */
public boolean addAll(Collection<? extends E> c) {
//在size后面添加一个Collection集合
    return addAll(size, c);
}

/**
 * Inserts all of the elements in the specified collection into this
 * list, starting at the specified position.  Shifts the element
 * currently at that position (if any) and any subsequent elements to
 * the right (increases their indices).  The new elements will appear
 * in the list in the order that they are returned by the
 * specified collection's iterator.
 *
 * @param index index at which to insert the first element
 *              from the specified collection
 * @param c collection containing elements to be added to this list
 * @return {@code true} if this list changed as a result of the call
 * @throws IndexOutOfBoundsException {@inheritDoc}
 * @throws NullPointerException if the specified collection is null
 */
public boolean addAll(int index, Collection<? extends E> c) {
    checkPositionIndex(index);//检查位置是否合理

    Object[] a = c.toArray();//转换为数组
    int numNew = a.length;
    if (numNew == 0)//数组大小为0
        return false;

    Node<E> pred, succ;
    if (index == size) {//如果size和index相等，初始化succ为null，pred为尾节点
        succ = null;
        pred = last;
    } else {
    //否则 采用折半查找index元素，并设置succ为当前index的数据，pred为当前元素的prev
        succ = node(index);
        pred = succ.prev;
    }

    for (Object o : a) {
    //循环object数组，链表循环添加
        @SuppressWarnings("unchecked") E e = (E) o;
        Node<E> newNode = new Node<>(pred, e, null);
        if (pred == null)
            first = newNode;
        else
            pred.next = newNode;
        pred = newNode;
    }
    //循环结束后，判断如果succ是null， 说明此时是在队尾添加的，设置一下队列尾节点last，
    //如果不是在队尾，则更新之前插入位置节点的前节点和当前要插入节点的尾节点
    if (succ == null) {
        last = pred;
    } else {
        pred.next = succ;
        succ.prev = pred;
    }

    size += numNew;
    modCount++;
    return true;
}

删除
/**
 * Removes the element at the specified position in this list.  Shifts any
 * subsequent elements to the left (subtracts one from their indices).
 * Returns the element that was removed from the list.
 *
 * @param index the index of the element to be removed
 * @return the element previously at the specified position
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public E remove(int index) {
//删除指定位置的元素
    checkElementIndex(index);
    return unlink(node(index));
}

/**
 * Unlinks non-null node x.
 */
E unlink(Node<E> x) {
    // assert x != null;
    final E element = x.item;
    final Node<E> next = x.next;//获取当前next赋值给临时node next
    final Node<E> prev = x.prev;//获取当前prev赋值给临时node prev

    if (prev == null) {
    //头节点
        first = next;//直接赋值
    } else {
        prev.next = next;//把next给prev的next
        x.prev = null;//设置为null
    }

    if (next == null) {
    //尾节点
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

查询
/**
 * Returns the element at the specified position in this list.
 *
 * @param index index of the element to return
 * @return the element at the specified position in this list
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public E get(int index) {
    checkElementIndex(index);//判断位置是否合理
    return node(index).item;//折半查找
}

修改
/**
 * Replaces the element at the specified position in this list with the
 * specified element.
 *
 * @param index index of the element to replace
 * @param element element to be stored at the specified position
 * @return the element previously at the specified position
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public E set(int index, E element) {
    checkElementIndex(index);//判断位置是否合理
    Node<E> x = node(index);//折半查找
    E oldVal = x.item;//获取旧元素
    x.item = element;//替换成新的元素
    return oldVal;//返回旧元素
}
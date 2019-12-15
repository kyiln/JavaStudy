# LinkedList 源码阅读笔记

## 一. 简介

LinkedList是链表实现的List，内部维护了一个双向链表，插入、修改或删除元素效率较高，访问元素效率低于ArrayList。LinkedList也是一个非线程安全的集合。

## 二. 实现接口

LinkedList实现了List接口，是一个有序的线性集合，具有添加、删除、插入、遍历等操作。
LinkedList实现了Cloneable接口，实现为浅拷贝。
LinkedList实现了序列化接口Serializable，可以被序列化。
LinkedList实现了双端队列接口Deque，说明LinkedList是一种具有队列和栈的性质的数据结构。

## 三. 核心源码

### 1. 类属性

#### 内部类

双链表节点Node

```java
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

#### 属性

```java
/**
    * 指向双向链表第一个节点的指针
    */
transient Node<E> first;

/**
    * 指向双向链表最后一个节点的指针
    */
transient Node<E> last;

/**
 * LinkedList中元素的个数
 *
 */
transient int size;
```

### 2. 核心方法

#### 构造方法

无参构造方法，构造一个空List。

```java
public LinkedList() {
}
```

传入一个集合初始化，将集合中的元素添加到链表中。

```java
public LinkedList(Collection<? extends E> c) {
    this();
    addAll(c);
}
```

#### 添加元素

LinkedList既可作为List，又可作为双端队列，因此它除了可以在任意位置添加元素外，还应该具有双端队列的性质，即在队列头或队列尾添加元素。

add(E e)方法、addLast(E e)方法、offer(E e)和offerLast(E e)都将在LinkedList尾部添加元素，平均时间复杂度O(1)。

```java
public boolean add(E e) {
    linkLast(e);
    return true;
}

public void addLast(E e) {
    linkLast(e);
}

public boolean offer(E e) {
    return add(e);
}

public boolean offerLast(E e) {
    addLast(e);
    return true;
}

void linkLast(E e) {
    // 尾节点引用
    final Node<E> l = last;
    // 创建新节点，prev指向尾节点，next指向null
    final Node<E> newNode = new Node<>(l, e, null);
    // 新节点成为尾节点
    last = newNode;
    if (l == null)
        // last节点为null说明之前LinkedList为空，则把首节点也指向新节点
        first = newNode;
    else
        l.next = newNode;
    size++;
    modCount++;
}
```

addFirst(E e)方法、offerFirst(E e)方法和push(E e)方法都将在LinkedList首部添加元素，平均时间复杂度O(1)。

```java
public void addFirst(E e) {
    linkFirst(e);
}

public boolean offerFirst(E e) {
    addFirst(e);
    return true;
}

public void push(E e) {
    addFirst(e);
}

private void linkFirst(E e) {
    // 首节点引用
    final Node<E> f = first;
    // 创建新节点，prev指向null，next指向首节点
    final Node<E> newNode = new Node<>(null, e, f);
    // 新节点成为首节点
    first = newNode;
    if (f == null)
        // first节点为null说明之前LinkedList为空，则把尾节点也指向新节点
        last = newNode;
    else
        f.prev = newNode;
    size++;
    modCount++;
}
```

add(int index, E element)方法可以将element添加到指定的index上，平均时间复杂度O(n)。

```java
public void add(int index, E element) {
    // 检查index是否越界
    checkPositionIndex(index);

    if (index == size)
        // 如果index == size，则添加元素到尾部
        linkLast(element);
    else
        // 将元素插入到index处
        linkBefore(element, node(index));
}

Node<E> node(int index) {
    // assert isElementIndex(index);

    // 根据index获取节点，思路为如果index < size / 2则从首节点开始往后遍历
    // 否则从尾节点往前遍历
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

/**
 * e是待插入节点，succ是待添加节点的后继节点
 */
void linkBefore(E e, Node<E> succ) {
    // assert succ != null;

    // succ的前驱节点
    final Node<E> pred = succ.prev;
    // 创建新节点，prev指向succ的前驱节点，next指向succ
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

addAll(Collection<? extends E> c)方法可以将一个集合中的所有元素全部有序添加到LinkedList尾部。
addAll(int index, Collection<? extends E> c)方法可以将一个集合插入到LinkedList指定index上。

```java
public boolean addAll(Collection<? extends E> c) {
    return addAll(size, c);
}

public boolean addAll(int index, Collection<? extends E> c) {
    // 检查index是否越界
    checkPositionIndex(index);

    Object[] a = c.toArray();
    int numNew = a.length;
    if (numNew == 0)
        return false;

    Node<E> pred, succ;
    if (index == size) {
        succ = null;
        pred = last;
    } else {
        succ = node(index);
        pred = succ.prev;
    }

    // 迭代器遍历复制
    for (Object o : a) {
        @SuppressWarnings("unchecked") E e = (E) o;
        Node<E> newNode = new Node<>(pred, e, null);
        if (pred == null)
            first = newNode;
        else
            pred.next = newNode;
        pred = newNode;
    }

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
```

#### 删除元素

remove(int index)方法将删除指定index上的元素，并返回该元素。删除时需要进行遍历找到删除节点的位置，平均时间复杂度O(n)。

```java
public E remove(int index) {
    // index越界检查
    checkElementIndex(index);
    // 找到index位置上的元素，从链表中删除
    return unlink(node(index));
}

E unlink(Node<E> x) {
    // assert x != null;
    final E element = x.item;
    final Node<E> next = x.next;
    final Node<E> prev = x.prev;

    if (prev == null) {
        // prev == null说明待删除节点为首节点，则把待删除节点后继节点设为首节点
        first = next;
    } else {
        prev.next = next;
        x.prev = null;
    }

    if (next == null) {
        // next == null说明待删除节点为尾节点，则把待删除节点前驱节点设为尾节点
        last = prev;
    } else {
        next.prev = prev;
        x.next = null;
    }

    // 有利于GC
    x.item = null;
    size--;
    modCount++;
    return element;
}
```

remove(Object o)方法删除指定元素值的方法(通过equals()方法判断)，平均时间复杂度O(n)。

```java
public boolean remove(Object o) {
    if (o == null) {
        for (Node<E> x = first; x != null; x = x.next) {
            if (x.item == null) {
                unlink(x);
                return true;
            }
        }
    } else {
        for (Node<E> x = first; x != null; x = x.next) {
            if (o.equals(x.item)) {
                unlink(x);
                return true;
            }
        }
    }
    return false;
}
```

removeFirst()方法和pop()方法删除首节点元素并返回该元素，如果首节点为null则抛出异常;
poll()和pollFirst()也是删除首节点元素并返回该元素，不同的是如果首节点为null则返回null，它们的时间复杂度都为O(1)。

```java
public E removeFirst() {
    final Node<E> f = first;
    if (f == null)
        throw new NoSuchElementException();
    return unlinkFirst(f);
}

public E poll() {
    final Node<E> f = first;
    return (f == null) ? null : unlinkFirst(f);
}

public E pollFirst() {
    final Node<E> f = first;
    return (f == null) ? null : unlinkFirst(f);
}

public E pop() {
    return removeFirst();
}

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
```

removeLast()方法删除首节点元素并返回该元素，如果首节点为null则抛出异常;
pollLast()也是删除首节点元素并返回该元素，不同的是如果首节点为null则返回null，它们的时间复杂度都为O(1)。

```java
public E removeLast() {
    final Node<E> f = last;
    if (f == null)
        throw new NoSuchElementException();
    return unlinkLast(f);
}

public E pollLast() {
    final Node<E> l = last;
    return (l == null) ? null : unlinkLast(l);
}

private E unlinkLast(Node<E> f) {
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
```

#### 获取元素

get(int index)方法很简单，遍历获取指定索引位置的元素，时间复杂度为O(n)。

```java
public E get(int index) {
    checkElementIndex(index);
    return node(index).item;
}
```

getFirst()、element()和peek()方法都返回链表的首节点，getFirst()方法和element()方法在首节点为null时将抛出异常，而peek()方法则会返回null，时间复杂度为O(1)。

```java
public E getFirst() {
    final Node<E> f = first;
    if (f == null)
        throw new NoSuchElementException();
    return f.item;
}

public E element() {
    return getFirst();
}

public E peek() {
    final Node<E> f = first;
    return (f == null) ? null : f.item;
}
```

getLast()方法返回链表的尾节点，在尾节点为null时将抛出异常，时间复杂度为O(1)。

```java
public E getLast() {
    final Node<E> l = last;
    if (l == null)
        throw new NoSuchElementException();
    return l.item;
}
```

#### 设置元素

set(int index, E element)方法可以将指定index的元素设置为指定element，并返回旧元素，时间复杂度O(1)。

```java
public E set(int index, E element) {
    checkElementIndex(index);
    Node<E> x = node(index);
    E oldVal = x.item;
    x.item = element;
    return oldVal;
}
```

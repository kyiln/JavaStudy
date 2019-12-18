# LinkedList 源码分析

## TOP 带着问题看源码

1. LinkedList 采用的数据结构是什么

## 1. 继承和实现关系

<img src="http://qiniu.itliusir.com/linkedlist01.png" style="zoom:50%;" />

- *AbstractSequentialList 实现类*

  提供一些围绕着iterator的基础方法

- *List 接口*

  提供 list 功能

- *Deque 接口*

  提供双端操作功能，以此可以猜出 LinkedList 数据结构是一个双向链表

- *Cloneable 接口*

  标记该类对象能够被Object.clone()

- *Serializable 接口*

  标记该类是可序列化的

## 2. 成员变量分析

```java
// 容量
transient int size = 0;
// 首节点
transient Node<E> first;
// 尾节点
transient Node<E> last;
```

接下来看节点 Node的成员变量

```java
// 节点的值
E item;
// next 指针
Node<E> next;
// prev 指针
Node<E> prev;
```

回到 **TOP 1** 问题，根据实现可以明白其数据结构是一个双向链表

## 3. 构造方法分析

一个是默认无参，一个是带集合内容的。

把传来的集合新增入当前list

```java
public LinkedList(Collection<? extends E> c) {
    this();
    addAll(c);
}
```

接下来看节点 Node的构造方法，根据入参默认维护两个指针。

```java
Node(Node<E> prev, E element, Node<E> next) {
    this.item = element;
    this.next = next;
    this.prev = prev;
}
```

## 4. 核心方法分析

### 4.1 获取元素

先check，然后通过 node(index)方法取

```java
public E get(int index) {
    checkElementIndex(index);
    return node(index).item;
}
```

node 方法通过判断索引 index 的范围(若是大于一半集合容量，则从尾结点向前遍历，若小于则从头结点向后遍历)来尽量高效的取到对应的节点

### 4.2 新增元素

#### 4.2.1 add(E e)

尾插

```java
public boolean add(E e) {
    linkLast(e);
    return true;
}
```

构建一个next指针是null，prev指针是尾结点的新节点newNode，如果尾结点不为空则将尾结点的next结点指向newNode，否则将头结点指向newNode

```java
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
```

#### 4.2.2 add(int index, E element)

先check，如果插入的还是尾部，则调用 linkLast 方法，否则先获取到索引 index 对应的节点然后调用 linkBefore 方法

```java
public void add(int index, E element) {
    checkPositionIndex(index);

    if (index == size)
        linkLast(element);
    else
        linkBefore(element, node(index));
}
```

构建一个 prev 指针是索引 index - 1 对应节点，next节点是索引 index 对应节点的新节点newNode **(图中绿色部分)**。然后把index节点的prev指向newNode **(图中蓝色部分)**，如果要插入的是第一个位置，则把 first 指针指向newNode，否则维护剩余的指针关系(index - 1 节点的next指向newNode)**(图中红色部分)**

<img src="http://qiniu.itliusir.com/linkedlist02.png" style="zoom:50%;" />



### 4.3 更新元素

根据下标位置获取节点，然后把节点的值进行覆盖

```java
public E set(int index, E element) {
    checkElementIndex(index);
    Node<E> x = node(index);
    E oldVal = x.item;
    x.item = element;
    return oldVal;
}
```

### 4.4 删除元素

#### 4.4.1 remove(int index)

先check，然后先获取index对应节点最后调用unlink方法

```java
public E remove(int index) {
    checkElementIndex(index);
    return unlink(node(index));
}
```

同按照下标新增那块逻辑差不多，去除一个节点(prev next item置null)，重新维护指针关系

```java
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

#### 4.4.2 remove(Object o)

遍历要找的元素的index，然后调用unlink方法

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

#### 4.4.3 clear()

遍历赋值null，size重置为0
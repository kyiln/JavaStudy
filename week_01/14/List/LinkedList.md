# 名字

## 问题

## 一、简介

优点：

缺点：

遍历效率(快-慢)：

​	Iterator迭代 > for循环

## 二、继承关系图

 ![在这里插入图片描述](https://img-blog.csdnimg.cn/20191211111520915.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM5OTM4NzU4,size_16,color_FFFFFF,t_70)

通过继承体系，我们可以看到LinkedList不仅实现了List接口，还实现了Queue和Deque接口，所以它既能作为List使用，也能作为双端队列使用，当然也可以作为栈使用

## 三、存储结构

链路型数据结构

## 四、源码分析

#### 内部类

```java
private static class Node<E> {
    E item;//当前元素的value
    Node<E> next;//下一个元素。null代表尾元素
    Node<E> prev;//上一个元素。null代表受原素

    Node(Node<E> prev, E element, Node<E> next) {
        this.item = element;
        this.next = next;
        this.prev = prev;
    }
}
```

#### 属性

```Java
transient int size = 0;//元素数量

/**
 * Pointer to first node.
 * Invariant: (first == null && last == null) ||
 *            (first.prev == null && first.item != null)
 */
transient Node<E> first;//头元素，if((first==null&&last==null)
												//||first.prew.null&&firest.item)

/**
 * Pointer to last node.
 * Invariant: (first == null && last == null) ||
 *            (last.next == null && last.item != null)
 */
transient Node<E> last;//尾元素，if((last==null&&last==null)
												//||last.next==null&&firest.item!=ull).
```

#### 构造

```java
public LinkedList() {
}

public LinkedList(Collection<? extends E> c) {
  this();
  addAll(c);
}
```

#### 主要方法

**void addFirst(E e)**

```java
/**从头添加元素**/
public void addFirst(E e) {
  linkFirst(e);
}

private void linkFirst(E e) {
	//保存头元素
  final Node<E> f = first;
  //根据e 创建新元素对象
  final Node<E> newNode = new Node<>(null, e, f);
  //设置newNode为头节点
  first = newNode;
  //如果为null 那么头尾节点都是一个。此时只有一个元素
  if (f == null)
    last = newNode;
  else//否则之前的头元素prev指向新的first元素
    f.prev = newNode;
  size++;//长度+1
  modCount++;//修改+1
}
```

**void addLast(E e)**	

**boolean add(E e)** 等同上面，只是多一个return ture。

```java
/**从队尾添加元素**/
public void addLast(E e) {
  linkLast(e);
}
void linkLast(E e) {
  //保存尾元素
  final Node<E> l = last;
  //根据e 创建新元素对象
  final Node<E> newNode = new Node<>(l, e, null);
  //设置newNode为尾节点
  last = newNode;
  //如果尾为nul，那么代表之前是nullLinkedList。直接设置头节点等于lastNode
  if (l == null)
    first = newNode;
  else//否则之前的尾节点的下一个节点为新建newNode节点
    l.next = newNode;
  size++;//长度+1
  modCount++;//修改+1
}
```

E poll()	：获得头节点，并且移除头节点

E peek()	：获得头元素

peekFirst
peekLast
pollFirst
pollLast

**remove方法**

```java
public E remove(int index)；//根据索引寻找元素，并删除元素
public E remove();//返回头元素，并删除头元素

//根据元素值删除
public boolean remove(Object o) {
        if (o == null) {
      //如果是null元素。判断first是否为null，如果不为null则一直循环到null元素删除返回true。否则false
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
       //如果不为null，循环判断元素是否相等，如果相等则删除，直到返回。
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





## 五、总结

```
  可重复的顺序存储结构，和ArrayList相反，添加删除修改时间复杂度 1   查询时间复杂度是 n
```
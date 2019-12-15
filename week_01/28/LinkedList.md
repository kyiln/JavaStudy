# LinkedList 源码分析

## 在LinkedList中，每一个元素都是Node存储，Node拥有一个存储值的item与一个前驱prev和一个后继next

```java
// 数据结构
private static class Node<E> {
    E item;// 存储元素
    Node<E> next;// 指向上一个元素
    Node<E> prev;// 指向下一个元素
    Node(Node<E> prev, E element, Node<E> next) {
        this.item = element;
        this.next = next;
        this.prev = prev;
    }
}
```

## 添加数据方法

```java
public boolean addAll(int index, Collection<? extends E> c) {
        checkPositionIndex(index);  //校验index是否下标越界。

        Object[] a = c.toArray();
        int numNew = a.length;
        if (numNew == 0)  //没有添加新的元素，返回false
            return false;

        Node<E> pred, succ;  //建立前节点，后节点
        if (index == size) {  //index==size，说明是在当前集合的末尾插入新数据，因此没有后节点，succ=null，前节点为当前集合的最后一个节点pred=last
            succ = null;
            pred = last;
        } else {
            succ = node(index);  //找到当前下标指代的节点，要在该节点前插入数据，因此令succ等于该节点。
            pred = succ.prev;  //pred则等于succ前面一位的节点。需要注意当index=0时，该点可以为null。
        }

        for (Object o : a) {
            @SuppressWarnings("unchecked") E e = (E) o;  //强制类型转换Object转为E
            Node<E> newNode = new Node<>(pred, e, null);  //创建一个新的节点，节点元素为e，前节点为pred，后节点为null。
            if (pred == null)  //说明index=0，因此新插入的集合的第一个元素，作为first
                first = newNode;
            else
                pred.next = newNode;  //说明index<>0，因此新的节点，作为前节点的后节点(pred.next)
            pred = newNode;  //令当前节点作为前节点，获取下一个元素，循环。
        }

        if (succ == null) {  //说明是从当前集合的末尾开始插入数据，因此数据的最后一个元素，作为当前集合的last
            last = pred;
        } else {  //pred为新插入的最后一个数据，令其的后节点等于之前拆开位置的后节点，succ为之前拆开位置的前节点，令其前节点prev等于新插入的元素的最后一个数据。
            pred.next = succ;
            succ.prev = pred;
        }

        size += numNew;  //新插入numNew条数据，size增加相应的大小。
        modCount++;
        return true;
    }
```

## 在addAll(int,Collection)方法中，首先执行checkPositionIndex(int)检验index的合法性(是否在当前对象的size范围内)

```java
private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }
```
### LinkedList 总结

LinkedList重点在于对内部类Node<E>的理解。
每一个元素在LinkedList中都是在Node<E>中存储，每个Node<E>中同时还存储了当前元素的前节点和后节点。
新增元素或集合，只要找到添加的位置，获取该位置的前节点pred，和后节点succ，令pred.next=新插入集合的第一个元素节点，令succ.pred=新插入的集合的最后一个元素节点即可。
删除元素或集合，找到删除的位置，获取该位置的前节点pred，和后节点succ，令pred.next=succ.pred即可。
注意不论是新增还是删除，均要考虑到起始节点没有pred和结束节点没有next的问题。
每一个LinkedList对象中均只存了first和last两个节点，因此当根据索引寻找元素时，代码会判断索引是在前半部分还是后半部分，从而决定从first出发，还是从last出发。




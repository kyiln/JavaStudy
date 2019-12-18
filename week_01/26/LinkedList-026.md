# LindedList
## 简单说明
#### LinkedList底层的数据结构是基于双向循环链表的，存在一种数据结构——链表节点。每个节点所对应的类是Entry的实例。Entry中包含成员变量： previous、next、element。previous是该节点的上一个节点，next是该节点的下一个节点，element是该节点所包含的值
#### LinkedList集合和ArrayList集合一样，也不是线程安全的，在多线程开发时也要额外添加同步代码，保证集合的线程安全。

## 基础属性
```Java
transient int size = 0; // 节点数量
transient Node<E> first;    // 第一个节点（头结点）
transient Node<E> last; // 最后一个节点（尾节点）
private static class Node<E> {  // Node的数据结构
    E item; // 存放的对象
    Node<E> next;   // 下一个节点
    Node<E> prev;   // 上一个节点
 
    Node(Node<E> prev, E element, Node<E> next) {
        this.item = element;
        this.next = next;
        this.prev = prev;
    }
}
```
## 常用方法
#### add方法
```Java
public boolean add(E e) {
    linkLast(e);    // 调用linkLast方法, 将节点添加到尾部
    return true;
}
```
```Java
public void add(int index, E element) { // 在index位置插入节点，节点值为element
    checkPositionIndex(index);
 
    if (index == size)  // 如果索引为size，即将element插入链表尾部
        linkLast(element);
    else    // 否则，将element插入原index位置节点的前面，即：将element插入index位置，将原index位置节点移到index+1的位置
        linkBefore(element, node(index));   // 将element插入index位置
}
```
```Java
void linkLast(E e) {    // 将e放到链表的最后一个节点
    final Node<E> l = last; // 拿到当前的尾节点l节点
    final Node<E> newNode = new Node<>(l, e, null); // 使用e创建一个新的节点newNode, prev属性为l节点, next属性为null
    last = newNode; // 将当前尾节点设置为上面新创建的节点newNode
    if (l == null)  // 如果l节点为空则代表当前链表为空, 将newNode设置为头结点
        first = newNode;
    else    // 否则将l节点的next属性设置为newNode
        l.next = newNode;
    size++;
    modCount++;
}
```
```Java
void linkBefore(E e, Node<E> succ) {    // 将e插入succ节点前面
    // assert succ != null;
    final Node<E> pred = succ.prev; //　拿到succ节点的prev节点
    final Node<E> newNode = new Node<>(pred, e, succ);  // 使用e创建一个新的节点newNode，其中prev属性为pred节点，next属性为succ节点
    succ.prev = newNode;    // 将succ节点的prev属性设置为newNode
    if (pred == null)   // 如果pred节点为null，则代表succ节点为头结点，要把e插入succ前面，因此将first设置为newNode
        first = newNode;
    else    // 否则将pred节点的next属性设为newNode
        pred.next = newNode;
    size++;
    modCount++;
}
```
#### get方法
```Java
public E get(int index) {   
    checkElementIndex(index);   // 校验index是否越界
    return node(index).item;    // 根据index， 调用node方法寻找目标节点，返回目标节点的item
}
```
#### set方法
```Java
public E set(int index, E element) {    // 替换index位置节点的值为element
    checkElementIndex(index);   // 检查index是否越界
    Node<E> x = node(index);    // 根据index， 调用node方法寻找到目标节点
    E oldVal = x.item;  // 节点的原值
    x.item = element;   // 将节点的item属性设为element
    return oldVal;  //返回节点原值
}
```
#### remove方法
```Java
public boolean remove(Object o) {
    if (o == null) {    // 如果o为空, 则遍历链表寻找item属性为空的节点, 并调用unlink方法将该节点移除
        for (Node<E> x = first; x != null; x = x.next) {
            if (x.item == null) {
                unlink(x);
                return true;
            }
        }
    } else {    // 如果o不为空, 则遍历链表寻找item属性跟o相同的节点, 并调用unlink方法将该节点移除
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
```Java
public E remove(int index) {    // 移除index位置的节点
    checkElementIndex(index);   // 检查index是否越界
    return unlink(node(index)); // 移除index位置的节点
}
```
```Java
E unlink(Node<E> x) {   // 移除链表上的x节点
    // assert x != null;
    final E element = x.item;   // x节点的值
    final Node<E> next = x.next;    // x节点的下一个节点
    final Node<E> prev = x.prev;    // x节点的上一个节点
 
    if (prev == null) { // 如果prev为空，则代表x节点为头结点，则将first指向next即可
        first = next;
    } else {    // 否则，x节点不为头结点，
        prev.next = next;   // 将prev节点的next属性指向x节点的next属性
        x.prev = null;  // 将x的prev属性清空
    }
 
    if (next == null) { // 如果next为空，则代表x节点为尾节点，则将last指向prev即可
        last = prev;
    } else {    // 否则，x节点不为尾节点
        next.prev = prev;   // 将next节点的prev属性指向x节点的prev属性
        x.next = null;  // 将x的next属性清空
    }
 
    x.item = null;  // 将x的值清空，以便垃圾收集器回收x对象
    size--;
    modCount++;
    return element;
}
```
#### clear方法
```Java
public void clear() {   // 清除链表的所有节点
    for (Node<E> x = first; x != null; ) {  // 从头结点开始遍历将所有节点的属性清空
        Node<E> next = x.next;
        x.item = null;
        x.next = null;
        x.prev = null;
        x = next;
    }
    first = last = null;    // 将头结点和尾节点设为null
    size = 0;
    modCount++;
}
```
## ArrayList和LinkedList比较
#### 1、ArrayList底层基于动态数组实现，LinkedList底层基于链表实现
#### 2、对于随机访问（get/set方法），ArrayList通过index直接定位到数组对应位置的节点，而LinkedList需要从头结点或尾节点开始遍历，直到寻找到目标节点，因此在效率上ArrayList优于LinkedList
#### 3、对于插入和删除（add/remove方法），ArrayList需要移动目标节点后面的节点（使用System.arraycopy方法移动节点），而LinkedList只需修改目标节点前后节点的next或prev属性即可，因此在效率上LinkedList优于ArrayList。

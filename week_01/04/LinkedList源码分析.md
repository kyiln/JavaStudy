# LinkedList

> ```
> * Doubly-linked list implementation of the {@code List} and {@code Deque}
> * interfaces.  Implements all optional list operations, and permits all
> * elements (including {@code null}).
> ```
>
> 双向链表实现了List接口和Deque，实现了所有的list操作，允许包含空的所有元素。
>
> ** not synchronized ** -> Collections#synchronizedList 方法调用
>
> 实现了 List Deque





## 主要属性

```java
// 元素的个数
transient int size = 0;

/**
 * 指向第一个节点
 * Pointer to first node.
 * 什么意思？
 * Invariant: (first == null && last == null) ||
 *            (first.prev == null && first.item != null)
 */
transient Node<E> first;

/**
 * 指向最后一个节点
 * Pointer to last node.
 * Invariant: (first == null && last == null) ||
 *            (last.next == null && last.item != null)
 */
transient Node<E> last;
```



## 构造方法

1. LinkedList()

   ```java
   /**
    * 构造空的集合
    * Constructs an empty list.
    */
   public LinkedList() {
   }
   ```

2. LinkedList(Collection<? extends E> c)

   ```java
   /**
    * 构造一个list集合包含传入的集合的所有元素，他们的顺序按照 集合c 所iterator的返回顺序
    * Constructs a list containing the elements of the specified
    * collection, in the order they are returned by the collection's
    * iterator.
    *
    * @param  c the collection whose elements are to be placed into this list
    * @throws NullPointerException if the specified collection is null
    */
   public LinkedList(Collection<? extends E> c) {
       this();
     	// 将集合加到此list中 后续再看
       addAll(c);
   }
   ```

   





## 方法

### 简单看

1. 查

   ```java
   /**
    * 返回第一个元素 O1
    * Returns the first element in this list.
    *
    * @return the first element in this list
    * @throws NoSuchElementException if this list is empty
    */
   public E getFirst() {
       final Node<E> f = first;
       if (f == null)
           throw new NoSuchElementException();
       return f.item;
   }
   
   /**
    * 返回最后一个元素 O1
    * Returns the last element in this list.
    *
    * @return the last element in this list
    * @throws NoSuchElementException if this list is empty
    */
   public E getLast() {
       final Node<E> l = last;
       if (l == null)
           throw new NoSuchElementException();
       return l.item;
   }
   
   ```

2. 删

   ```java
   /**
    * 移出并返回第一元素
    * Removes and returns the first element from this list.
    *
    * @return the first element from this list
    * @throws NoSuchElementException if this list is empty
    */
   public E removeFirst() {
       final Node<E> f = first;
       if (f == null)
           throw new NoSuchElementException();
       return unlinkFirst(f);
   }
   
   /**
    * 移出并返回最后一个元素
    * Removes and returns the last element from this list.
    *
    * @return the last element from this list
    * @throws NoSuchElementException if this list is empty
    */
   public E removeLast() {
       final Node<E> l = last;
       if (l == null)
           throw new NoSuchElementException();
       return unlinkLast(l);
   }
   ```

   

3. 



### 细看

1. addAll

   ```java
   /**
    * 将传入的集合c全部加入到此集合list中，他们的顺序是按照collection c iterator的顺序。
    * 当传入的collection c 在操作在进行中时，被修改了。此操作的表现 undefined 
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
       return addAll(size, c);
   }
   
   /**
    * 将传入的集合的所有元素加入到此list中（从指定位置开始），将当前位置交换，右边的往后移。
    * 新增的元素会以集合的 iterator的函数顺序
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
     	// 检查索引是否越界
       checkPositionIndex(index);
   
       Object[] a = c.toArray();
       int numNew = a.length;
       if (numNew == 0)
           return false;
   		// pred 要加入的前一个，succ 要插入的元素的位置
       Node<E> pred, succ;
       if (index == size) {
           succ = null;
           pred = last;
       } else {
           succ = node(index);
           pred = succ.prev;
       }
   		// 遍历要加入集合的元素数组
       for (Object o : a) {
         	// 将元素强转成对应的类型
           @SuppressWarnings("unchecked") E e = (E) o;
           Node<E> newNode = new Node<>(pred, e, null);
           if (pred == null)
             	// 前者为空 将此作为链首
               first = newNode;
           else
             	// 否则就将前者的后节点指向他
               pred.next = newNode;
         	// pred 使用newNode
           pred = newNode;
       }
   
       if (succ == null) {
          	//  插入的位置为空的情况 证明最后一个就是加进去的最后一个即pred
           last = pred;
       } else {
           // 不为空仅需将俩相连
           pred.next = succ;
           succ.prev = pred;
       }
   
       size += numNew;
       modCount++;
       return true;
   }
   
   /**
    * 返回在指定索引位置的节点
    * Returns the (non-null) Node at the specified element index.
    */
   Node<E> node(int index) {
       // assert isElementIndex(index);
   		// 在前一半从前往后找
       if (index < (size >> 1)) {
           Node<E> x = first;
           for (int i = 0; i < index; i++)
               x = x.next;
           return x;
       } else {
         	// 在后一半从后往前找
           Node<E> x = last;
           for (int i = size - 1; i > index; i--)
               x = x.prev;
           return x;
       }
   }
   ```

2. add

   ```java
   /**
    * 插入具体元素在指定的位置 移动此位置的元素和右边的元素
    * Inserts the specified element at the specified position in this list.
    * Shifts the element currently at that position (if any) and any
    * subsequent elements to the right (adds one to their indices).
    *
    * @param index index at which the specified element is to be inserted
    * @param element element to be inserted
    * @throws IndexOutOfBoundsException {@inheritDoc}
    */
   public void add(int index, E element) {
       checkPositionIndex(index);
   
       if (index == size)
           linkLast(element);
       else
           linkBefore(element, node(index));
   }
   ```

3. unlinkFirst, unlinkLast

   ```java
   /**
    * 移出并返回非空的第一节点f，删除头
    * Unlinks non-null first node f.
    */
   private E unlinkFirst(Node<E> f) {
       // assert f == first && f != null;
       final E element = f.item;
       final Node<E> next = f.next;
       f.item = null;
       f.next = null; // help GC
       first = next;
       if (next == null)
         	// 如果下一个为空 证明这条链就是无元素的
           last = null;
       else
         	// 将头结点 prev至成空
           next.prev = null;
       size--;
       modCount++;
       return element;
   }
   
   /**
    * 移出并返回非空的最后一个节点，删除尾
    * Unlinks non-null last node l.
    */
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
   ```

4. linkFirst, linkLast

   ```java
   /**
    * 将元素作为头结点连接
    * Links e as first element.
    */
   private void linkFirst(E e) {
     	// 头结点取出作为 新增节点的next节点
       final Node<E> f = first;
       final Node<E> newNode = new Node<>(null, e, f);
     	// 将头重置成新节点
       first = newNode;
       if (f == null)
         	// 如果头结点为空 那么尾节点也是这个新节点
           last = newNode;
       else
           // f的prev指向现有节点
           f.prev = newNode;
       size++;
       modCount++;
   }
   
   /**
    * Links e as last element.
    */
   void linkLast(E e) {
     	// 找到之前最后一个节点
       final Node<E> l = last;
     	// 组装出现有节点
       final Node<E> newNode = new Node<>(l, e, null);
       last = newNode;
       if (l == null)
         	// 之前的最后一个节点为空 证明这条链无数据
           first = newNode;
       else
         	// 最后一个节点的
           l.next = newNode;
       size++;
       modCount++;
   }
   ```

5. linkBefore

   ```java
   /**
    * 将元素e 插入到succ之前
    * Inserts element e before non-null Node succ.
    */
   void linkBefore(E e, Node<E> succ) {
       // assert succ != null;
     	// succ之前的位置
       final Node<E> pred = succ.prev;
     	// pred -> newNode(e) -> succ
       final Node<E> newNode = new Node<>(pred, e, succ);
       succ.prev = newNode;
       if (pred == null)
   	      // 之前的最后一个节点为空 证明这条链无数据
           first = newNode;
       else
           pred.next = newNode;
       size++;
       modCount++;
   }
   ```


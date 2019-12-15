# 读源码--LinkedList

1. ## 继承体系

   1. ### 示意图

      ### <img src="E:\Project\JavaStudy\week_01\08\LinkedList继承体系.jpg" style="zoom:67%;" />

   2. ### 继承分析

      1. #### 继承AbstractSequentialList

      2. #### 实现

         - RandomAcces--可随机访问
         - Cloneable--可拷贝
         - java.io.Serializable--可序列化

2. ## 属性和方法

   1. ### 属性

      ```
      transient int size = 0; // 大小
      transient Node<E> first; // 首节点
      transient Node<E> last; // 末节点
      ```

   2. ### 构造器

      ```
          public LinkedList() {
          }
          
          public LinkedList(Collection<? extends E> c) {
          this();
          addAll(c);
          }
      ```

   3. ### 节点方法（非环链表首节点的prev为null，尾节点的next为null）

      1. 设置元素为首节点

         ```
             private void linkFirst(E e) {
                 final Node<E> f = first;
                 final Node<E> newNode = new Node<>(null, e, f);
                 first = newNode;
                 // 判断原链表是否为空
                 // 为空则把last也置为新节点
                 // 否则把原首节点的prev置为新节点
                 if (f == null)
                     last = newNode;
                 else
                     f.prev = newNode;
                 size++;
                 modCount++;
             }
         ```

      2. 设置元素为末节点

         ```
             void linkLast(E e) {
                 final Node<E> l = last;
                 final Node<E> newNode = new Node<>(l, e, null);
                 last = newNode;
                 // 判断原链表是否为空
                 // 为空则把first也置为新节点
                 // 否则把原首节点的prev置为新节点
                 if (l == null)
                     first = newNode;
                 else
                     l.next = newNode;
                 size++;
                 modCount++;
             }
         ```

      3. 插入

         ```
             void linkBefore(E e, Node<E> succ) {
                 // assert succ != null;
                 // 获取首节点的上个节点
                 final Node<E> pred = succ.prev; 
                 final Node<E> newNode = new Node<>(pred, e, succ);
                 succ.prev = newNode;
                 //若上个节点为空，则新节点为首节点
                 if (pred == null)
                     first = newNode;
                 else
                     pred.next = newNode;
                 size++;
                 modCount++;
             }
         ```

      4. 删除首节点

                 private E unlinkFirst(Node<E> f) {
                     // assert f == first && f != null;
                     final E element = f.item;
                     final Node<E> next = f.next;
                     f.item = null;
                     f.next = null; // help GC
                     first = next;
                     //只有一个节点的情况下
                     if (next == null) // 下个节点为空
                         last = null;
                     else
                         next.prev = null;
                     size--;
                     modCount++;
                     return element;
                 }

      5. 删除末节点

         ```
             private E unlinkLast(Node<E> l) {
                 // assert l == last && l != null;
                 final E element = l.item;
                 final Node<E> prev = l.prev;
                 l.item = null;
                 l.prev = null; // help GC
                 last = prev;
                 //只有一个节点的情况下
                 if (prev == null)
                     first = null;
                 else
                     prev.next = null;
                 size--;
                 modCount++;
                 return element;
             }
         ```

      6. 删除任意节点

         ```
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

      7. 查询

         - getFirst()

         - getLast()

         - Node<E> node(int index) 

           ```
               Node<E> node(int index) {
                   // assert isElementIndex(index);
           
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
           ```

           

   4. ### 常用方法

      1. contains(Object o)
      2. size()
      3. clear()

3. ## 增删改查

   1. ### 增

      - void addFirst(E e)：首端添加
      - void addLast(E e)：末端添加
      - boolean add()：同addLast()，区别是该方法有返回值
      - boolean (Collection<? extends E> c)：添加集合
      - boolean (int index, Collection<? extends E> c)：指定位置添加集合
      - void add(int index, E element) ：指定位置添加元素

   2. ### 删

      - boolean remove(Object o) ：删除linkedList中值为o的节点
      - E remove(int index) 

   3. ### 改

      - E set(int index, E element) 

   4. ### 查

      - E get(int index) ：查询下标为index的节点的值
      - boolean isElementIndex(int index)：
      - boolean isPositionIndex(int index)：
      - int indexOf(Object o) 
      - int lastIndexOf(Object o)

   5. ### 迭代器

      1. ListIterator<E> listIterator(int index)



### 


# ArrayList 源码分析



## 继承体系

- AbstractList
- List
- RandomAccess
- Cloneable
- java.io.Serializable

> List 接口，是一种有顺序的集合，sequence （序列，顺序）
>
> index
>
> typically Duplicate elements (Unlike sets)



> ArrayList Resiable-array, permits alll elements including null.
>
> roughly equivalent to Vector (synchronized)
>
> size,isEmpty,get,set,iterator,listIterator  -> constant time
>
> add -> amortized(均摊) constant time 
>
> other operations ->  linear time (roughly)
>
> capacity >= list.size() ; capacity group automatically 



## 构造方法

1. 默认空构造 

   ​	

   ```java
   /**
   * Constructs an empty list with an initial capacity of ten.
   * private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
   * 为什么说是10呢，可能是因为添加第一个元素时会扩展成10
   * Any empty ArrayList with elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
   * will be expanded to DEFAULT_CAPACITY when the first element is added
   */
   public ArrayList() {
       this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
   }
   ```

2. 传入 initialCapacity的 构造

   ```java
   /**
    * Constructs an empty list with the specified initial capacity.
    * 当传入 初始容量为0的构造时，为什么和上面的空构造不用同一个 DEFAULTCAPACITY_EMPTY_ELEMENTDATA
    * 这俩是为了什么设计而存在的呢？
    * 在 DEFAULTCAPACITY_EMPTY_ELEMENTDATA 上的注释写了
    * -----------------------------
    * Shared empty array instance used for default sized empty instances. We
    * distinguish this from EMPTY_ELEMENTDATA to know how much to inflate when
    * first element is added.
    * -----------------------------
    * 为什么存在呢？ 
    * 大致意思就是如果你构造函数里传入了0，证明你这个故意为之，就是为了减少空间的使用，
    * 默认构造呢 就是会使用默认的10容量
    * @param  initialCapacity  the initial capacity of the list
    * @throws IllegalArgumentException if the specified initial capacity
    *         is negative
    */
   public ArrayList(int initialCapacity) {
       if (initialCapacity > 0) {
           this.elementData = new Object[initialCapacity];
       } else if (initialCapacity == 0) {
           this.elementData = EMPTY_ELEMENTDATA;
       } else {
           throw new IllegalArgumentException("Illegal Capacity: "+
                                              initialCapacity);
       }
   }
   ```

3. 传入集合的构造

   ```java
   /**
    * Constructs a list containing the elements of the specified
    * collection, in the order they are returned by the collection's
    * iterator.
    * 
    * @param c the collection whose elements are to be placed into this list
    * @throws NullPointerException if the specified collection is null
    */
   public ArrayList(Collection<? extends E> c) {
       elementData = c.toArray();
       if ((size = elementData.length) != 0) {
           // defend against c.toArray (incorrectly) not returning Object[]
           // (see e.g. https://bugs.openjdk.java.net/browse/JDK-6260652)
           if (elementData.getClass() != Object[].class)
               elementData = Arrays.copyOf(elementData, size, Object[].class);
       } else {
           // replace with empty array.
           this.elementData = EMPTY_ELEMENTDATA;
       }
   }
   ```

   ```java
   @HotSpotIntrinsicCandidate
   public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
       @SuppressWarnings("unchecked")
     	// 相同类型的元素
       T[] copy = ((Object)newType == (Object)Object[].class)
           ? (T[]) new Object[newLength]
           : (T[]) Array.newInstance(newType.getComponentType(), newLength);
       System.arraycopy(original, 0, copy, 0,
                        Math.min(original.length, newLength));
       return copy;
   }
   ```

   ## 

## Constant Time Methods

1. size

   ```java
      /**
        * Returns the number of elements in this list.
        * 返回list中的元素数量
        * @return the number of elements in this list
        */
       public int size() {
           return size;
       }
   ```

2. isEmpty

   ```java
   /**
    * Returns {@code true} if this list contains no elements.
    * 如果list中不包含元素返回 true
    * @return {@code true} if this list contains no elements
    */
   public boolean isEmpty() {
       return size == 0;
   }
   ```

3. get

   ```java
   /**
    * Returns the element at the specified position in this list.
    * 返回list在指定位置的元素
    * @param  index index of the element to return
    * @return the element at the specified position in this list
    * @throws IndexOutOfBoundsException {@inheritDoc}
    */
   public E get(int index) {
       Objects.checkIndex(index, size);
       return elementData(index);
   }
   ```

4. set

   ```java
   /**
    * Replaces the element at the specified position in this list with
    * the specified element.
    * 替换指定位置元素
    * @param index index of the element to replace
    * @param element element to be stored at the specified position
    * @return the element previously at the specified position
    * @throws IndexOutOfBoundsException {@inheritDoc}
    */
   public E set(int index, E element) {
       Objects.checkIndex(index, size);
       E oldValue = elementData(index);
       elementData[index] = element;
       return oldValue;
   }
   ```

5. iterator

   ```java
   /**
    * Returns an iterator over the elements in this list in proper sequence.
    * 返回此list的遍历器（以正确的顺序）
    * <p>The returned iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
    *
    * @return an iterator over the elements in this list in proper sequence
    */
   public Iterator<E> iterator() {
     	// 具体实现后续再看
       return new Itr();
   }
   ```

6. listIterator

   ```java
   /**
    * Returns a list iterator over the elements in this list (in proper
    * sequence), starting at the specified position in the list.
    * The specified index indicates the first element that would be
    * returned by an initial call to {@link ListIterator#next next}.
    * An initial call to {@link ListIterator#previous previous} would
    * return the element with the specified index minus one.
    *
    * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
    *
    * @throws IndexOutOfBoundsException {@inheritDoc}
    */
   public ListIterator<E> listIterator(int index) {
       rangeCheckForAdd(index);
       return new ListItr(index);
   }
   ```



## 主要看

1. add

   ```java
       /**
        * Appends the specified element to the end of this list.
        * 在列表中结尾增加指定元素
        * @param e element to be appended to this list
        * @return {@code true} (as specified by {@link Collection#add})
        */
       public boolean add(E e) {
         	// 修改次数 + 1
           modCount++;
           add(e, elementData, size);
           return true;
       }
   
       /**
        * 此方法使方法字节码小于35，使得在C1编译循环中区分开来
        * This helper method split out from add(E) to keep method
        * bytecode size under 35 (the -XX:MaxInlineSize default value),
        * which helps when add(E) is called in a C1-compiled loop.
        */
       private void add(E e, Object[] elementData, int s) {
           if (s == elementData.length)
             	// 由于元素已满，所以调用grow函数
               elementData = grow();
           elementData[s] = e;
           size = s + 1;
       }
   
   
       private Object[] grow() {
           return grow(size + 1);
       }
       /**
        * 增加容量确保集合可以存储传入的 minCapacity 数量
        * Increases the capacity to ensure that it can hold at least the
        * number of elements specified by the minimum capacity argument.
        *
        * @param minCapacity the desired minimum capacity
        * @throws OutOfMemoryError if minCapacity is less than zero
        */
       private Object[] grow(int minCapacity) {
           return elementData = Arrays.copyOf(elementData,
                                              newCapacity(minCapacity));
       }
   
   
    		/**
    		 * 返回 至少大于给定的 minCapacity大小，如果 1.5倍能够满足就返回。
    		 * 除非给定MAX_ARRAY_SIZE不然不会返回这个值
        * Returns a capacity at least as large as the given minimum capacity.
        * Returns the current capacity increased by 50% if that suffices.
        * Will not return a capacity greater than MAX_ARRAY_SIZE unless
        * the given minimum capacity is greater than MAX_ARRAY_SIZE.
        *
        * @param minCapacity the desired minimum capacity
        * @throws OutOfMemoryError if minCapacity is less than zero
        */
       private int newCapacity(int minCapacity) {
           // overflow-conscious code
           int oldCapacity = elementData.length;
         	// 1.5倍
           int newCapacity = oldCapacity + (oldCapacity >> 1);
           if (newCapacity - minCapacity <= 0) {
               if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
                   return Math.max(DEFAULT_CAPACITY, minCapacity);
               if (minCapacity < 0) // overflow
                 	// 溢出考虑
                   throw new OutOfMemoryError();
               return minCapacity;
           }
           return (newCapacity - MAX_ARRAY_SIZE <= 0)
               ? newCapacity
               : hugeCapacity(minCapacity);
       }
   
       private static int hugeCapacity(int minCapacity) {
           if (minCapacity < 0) // overflow
               throw new OutOfMemoryError();
           return (minCapacity > MAX_ARRAY_SIZE)
               ? Integer.MAX_VALUE
               : MAX_ARRAY_SIZE;
       }
   
       /**
        * 将元素插入list中指定位置。交换此时指定位置的现有元素，后移后置元素
        * Inserts the specified element at the specified position in this
        * list. Shifts the element currently at that position (if any) and
        * any subsequent elements to the right (adds one to their indices).
        *
        * @param index index at which the specified element is to be inserted
        * @param element element to be inserted
        * @throws IndexOutOfBoundsException {@inheritDoc}
        */
       public void add(int index, E element) {
         	// 检查是否越界
           rangeCheckForAdd(index);
         	// 修改次数+1
           modCount++;
           final int s;
           Object[] elementData;
           if ((s = size) == (elementData = this.elementData).length)
               elementData = grow();
         	// 调用系统数组拷贝
           System.arraycopy(elementData, index,
                            elementData, index + 1,
                            s - index);
           elementData[index] = element;
           size = s + 1;
       }
   
       /**
        * A version of rangeCheck used by add and addAll.
        */
       private void rangeCheckForAdd(int index) {
           if (index > size || index < 0)
               throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
       }
   ```

2. addAll

   ```java
   /**
    * 将所有集合中元素加入此list的最后
    * Appends all of the elements in the specified collection to the end of
    * this list, in the order that they are returned by the
    * specified collection's Iterator.  The behavior of this operation is
    * undefined if the specified collection is modified while the operation
    * is in progress.  (This implies that the behavior of this call is
    * undefined if the specified collection is this list, and this
    * list is nonempty.)
    *
    * @param c collection containing elements to be added to this list
    * @return {@code true} if this list changed as a result of the call
    * @throws NullPointerException if the specified collection is null
    */
   public boolean addAll(Collection<? extends E> c) {
     	// 转成数组
       Object[] a = c.toArray();
     	// 修改数量 + 1
       modCount++;
     	// 增加的数量 还有空指针的风险
       int numNew = a.length;
       if (numNew == 0)
           return false;
       Object[] elementData;
     	// 增加的位置
       final int s;
       if (numNew > (elementData = this.elementData).length - (s = size))
           elementData = grow(s + numNew);
     	// 将集合c中的元素拷贝到最后
       System.arraycopy(a, 0, elementData, s, numNew);
       size = s + numNew;
       return true;
   }
   ```

3. remove

   ```java
   /**
    * 移除list中第一个出现的元素（如果存在的话），如果list不包含元素，那这个就不会改变。更正式来说就是移除低索  
    * 引的元素 满足Objects.equals(o, get(i))的元素存在。
    * Removes the first occurrence of the specified element from this list,
    * if it is present.  If the list does not contain the element, it is
    * unchanged.  More formally, removes the element with the lowest index
    * {@code i} such that
    * {@code Objects.equals(o, get(i))}
    * (if such an element exists).  Returns {@code true} if this list
    * contained the specified element (or equivalently, if this list
    * changed as a result of the call).
    *
    * @param o element to be removed from this list, if present
    * @return {@code true} if this list contained the specified element
    */
   public boolean remove(Object o) {
       final Object[] es = elementData;
       final int size = this.size;
       int i = 0;
     	// 代码块
       found: {
         	// 区分为空
           if (o == null) {
               for (; i < size; i++)
                   if (es[i] == null)
                       break found;
           // 不为空的
           } else {
               for (; i < size; i++)
                   if (o.equals(es[i]))
                       break found;
           }
           return false;
       }
       fastRemove(es, i);
       return true;
   }
   
       /**
        * 基础的移除方法 跳过界限检查不返回移除的元素
        * Private remove method that skips bounds checking and does not
        * return the value removed.
        */
       private void fastRemove(Object[] es, int i) {
           modCount++;
           final int newSize;
           if ((newSize = size - 1) > i)
               System.arraycopy(es, i + 1, es, i, newSize - i);
           es[size = newSize] = null;
       }
   
   ```

   
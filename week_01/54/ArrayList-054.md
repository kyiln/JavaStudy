ArrayList底层是基于数组实现的

**成员**
/**
* Default initial capacity. 默认的初始容量大小
*/
private static final int DEFAULT_CAPACITY = 10;

/**
* Shared empty array instance used for empty instances.（用于空实例的共享空数组实例）实际上就是空数组对象
*/
private static final Object[] EMPTY_ELEMENTDATA = {};

/**
 * Shared empty array instance used for default sized empty instances. We
 * distinguish this from EMPTY_ELEMENTDATA to know how much to inflate when
 * first element is added. 
 * 用于默认大小的空实例的共享空数组实例。我们将其与空的元素数据区分开来，以了解何时                       
 *  添加第一个元素
 */
private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

transient Object[] elementData; //用于增删改查的数组

/**
 * The size of the ArrayList (the number of elements it contains).
 *
 * @serial
 */
private int size;//元素的大小  默认为0


/**
 * The maximum size of array to allocate.
 * Some VMs reserve some header words in an array.
 * Attempts to allocate larger arrays may result in
 * OutOfMemoryError: Requested array size exceeds VM limit
 */
private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;//最大数组容量，如果数组过长，会抛出OOM错误

**构造方法**

 /**
 * Constructs an empty list with the specified initial capacity.
 *
 * @param  initialCapacity  the initial capacity of the list
 * @throws IllegalArgumentException if the specified initial capacity
 *         is negative
 */
public ArrayList(int initialCapacity) {
//带初始容量的构造方法
    if (initialCapacity > 0) {
    //大于0 新建一个Object数组
        this.elementData = new Object[initialCapacity];
    } else if (initialCapacity == 0) {
    //等于0 使用上面的空数组对象
        this.elementData = EMPTY_ELEMENTDATA;
    } else {
    //其他的  抛出IllegalArgumentException
        throw new IllegalArgumentException("Illegal Capacity: "+
                                           initialCapacity);
    }
}

 /**
 * Constructs an empty list with an initial capacity of ten.
 */
public ArrayList() {
//无参构造方法，默认上面的数组对象
    this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
}

/**
 * Constructs a list containing the elements of the specified
 * collection, in the order they are returned by the collection's
 * iterator.
 *
 * @param c the collection whose elements are to be placed into this list
 * @throws NullPointerException if the specified collection is null
 */
public ArrayList(Collection<? extends E> c) {
//集合的构造方法
    elementData = c.toArray();//把集合转换为数组
    if ((size = elementData.length) != 0) {
    //先把集合大小给size,如果集合大小不为0，就会去判断是否为Object[].class
        // c.toArray might (incorrectly) not return Object[] (see 6260652)
        if (elementData.getClass() != Object[].class)
            elementData = Arrays.copyOf(elementData, size, Object[].class);//复制所有元素
    } else {
    //集合大小为0 使用上面的空数组对象
        // replace with empty array.
        this.elementData = EMPTY_ELEMENTDATA;
    }
}
**主要方法**

add方法
/**
 * Appends the specified element to the end of this list.(添加一个特定的元素到list的末尾)
 *
 * @param e element to be appended to this list
 * @return <tt>true</tt> (as specified by {@link Collection#add})
 */
public boolean add(E e) {
//向数组中添加元素
    ensureCapacityInternal(size + 1);  // Increments modCount!! 确定内部容量是否足够
    //在数据中size++位置放入元素
    elementData[size++] = e;
    return true;
}

private void ensureCapacityInternal(int minCapacity) {
    if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
    //判断传入进来的数组是否为空，因为如果是空的话，minCapacity=size+1；
    //其实就是等于1，空的数组没有长度就存放不了，所以就将minCapacity变成10，也就是默认大小，
    //但是在这里，还没有真正的初始化这个elementData的大小
        minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
    }

    ensureExplicitCapacity(minCapacity);
}

private void ensureExplicitCapacity(int minCapacity) {
    modCount++;

    // overflow-conscious code
    if (minCapacity - elementData.length > 0)
        grow(minCapacity);//ArrayList自动扩展大小
}

/**
 * Increases the capacity to ensure that it can hold at least the
 * number of elements specified by the minimum capacity argument.
 *
 * @param minCapacity the desired minimum capacity
 */
private void grow(int minCapacity) {
    // overflow-conscious code
    int oldCapacity = elementData.length;//先定义一个oldCapacity 存放扩充前的elementData大小
    int newCapacity = oldCapacity + (oldCapacity >> 1);//新的容量为1.5倍的oldCapacity
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;//当所有为0时，默认为10
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);//超出最大容量限制，调用hugeCapacity，就是将能给的最大值给newCapacity
    // minCapacity is usually close to size, so this is a win:
    elementData = Arrays.copyOf(elementData, newCapacity);//改变elementData大小
}

private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();//传入的参数必须大于0，否则报OOM错误
        //如果minCapacity 大于MAX_ARRAY_SIZE（Integer.MAX_VALUE - 8）（2147483639）就返回Integer.MAX_VALUE（ 2的31次方减一）（2147483647） 否则就直接给MAX_ARRAY_SIZE
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

/**
 * Inserts the specified element at the specified position in this
 * list. Shifts the element currently at that position (if any) and
 * any subsequent elements to the right (adds one to their indices).
 *
 * @param index index at which the specified element is to be inserted
 * @param element element to be inserted
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public void add(int index, E element) {
//在指定位置插入元素

    //检查index位置是否合理
    rangeCheckForAdd(index);

    ensureCapacityInternal(size + 1);  // Increments modCount!!
    
    //插入元素之前需要把index之后的元素往后移动一位
    System.arraycopy(elementData, index, elementData, index + 1,
                     size - index);
     //index位置元素复制
    elementData[index] = element;
    //size增加1
    size++;
}

/**
 * A version of rangeCheck used by add and addAll.
 */
private void rangeCheckForAdd(int index) {
    if (index > size || index < 0)
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));//判断index位置是否超过size和小于0 抛出数组越界异常
}

/**
 * Appends all of the elements in the specified collection to the end of
 * this list, in the order that they are returned by the
 * specified collection's Iterator.  The behavior of this operation is
 * undefined if the specified collection is modified while the operation
 * is in progress.  (This implies that the behavior of this call is
 * undefined if the specified collection is this list, and this
 * list is nonempty.)
 *
 * @param c collection containing elements to be added to this list
 * @return <tt>true</tt> if this list changed as a result of the call
 * @throws NullPointerException if the specified collection is null
 */
public boolean addAll(Collection<? extends E> c) {
    //向当前集合添加一个集合
    Object[] a = c.toArray();
    int numNew = a.length;
    ensureCapacityInternal(size + numNew);  // Increments modCount //确定容量
    System.arraycopy(a, 0, elementData, size, numNew);//扩容数组长度
    size += numNew;
    return numNew != 0;//返回新长度和size是否相等的boolean
}

/**
 * Inserts all of the elements in the specified collection into this
 * list, starting at the specified position.  Shifts the element
 * currently at that position (if any) and any subsequent elements to
 * the right (increases their indices).  The new elements will appear
 * in the list in the order that they are returned by the
 * specified collection's iterator.
 *
 * @param index index at which to insert the first element from the
 *              specified collection
 * @param c collection containing elements to be added to this list
 * @return <tt>true</tt> if this list changed as a result of the call
 * @throws IndexOutOfBoundsException {@inheritDoc}
 * @throws NullPointerException if the specified collection is null
 */
public boolean addAll(int index, Collection<? extends E> c) {
//将指定集合中的所有元素插入集合，从指定位置开始。移动元素目前处于该位置（如有）以及
//右边（增加他们的指数）。新的元素将出现在集合中按指定集合的迭代器。
    rangeCheckForAdd(index);

    Object[] a = c.toArray();
    int numNew = a.length;
    ensureCapacityInternal(size + numNew);  // Increments modCount

    int numMoved = size - index;
    if (numMoved > 0)
        System.arraycopy(elementData, index, elementData, index + numNew,
                         numMoved);

    System.arraycopy(a, 0, elementData, index, numNew);
    size += numNew;
    return numNew != 0;
}

remove方法

/**
 * Removes the element at the specified position in this list.
 * Shifts any subsequent elements to the left (subtracts one from their
 * indices).
 *
 * @param index the index of the element to be removed
 * @return the element that was removed from the list
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public E remove(int index) {
//移除指定位置元素
    
    //判断index是否合理
    rangeCheck(index);

    modCount++;
    E oldValue = elementData(index);//直接通过索引找到该元素

    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index,
                         numMoved);//移动元素
    赋值为null 让GC更快回收
    elementData[--size] = null; // clear to let GC do its work

    return oldValue;
}

@SuppressWarnings("unchecked")
E elementData(int index) {
    //通过index查找所在对应位置元素
    return (E) elementData[index];
}

 /**
 * Removes the first occurrence of the specified element from this list,
 * if it is present.  If the list does not contain the element, it is
 * unchanged.  More formally, removes the element with the lowest index
 * <tt>i</tt> such that
 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
 * (if such an element exists).  Returns <tt>true</tt> if this list
 * contained the specified element (or equivalently, if this list
 * changed as a result of the call).
 *
 * @param o element to be removed from this list, if present
 * @return <tt>true</tt> if this list contained the specified element
 */
public boolean remove(Object o) {
//循环所有找到对应的元素
    if (o == null) {
        for (int index = 0; index < size; index++)
            if (elementData[index] == null) {
                fastRemove(index);
                return true;
            }
    } else {
        for (int index = 0; index < size; index++)
            if (o.equals(elementData[index])) {
                fastRemove(index);
                return true;
            }
    }
    return false;
}

/*
 * Private remove method that skips bounds checking and does not
 * return the value removed.
 */
private void fastRemove(int index) {
    //其实就跟remove(int index)方法一样
    modCount++;
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index,
                         numMoved);
    elementData[--size] = null; // clear to let GC do its work
}

/**
 * Removes all of the elements from this list.  The list will
 * be empty after this call returns.
 */
public void clear() {
//把所有元素置换成null 让GC快速回收
    modCount++;

    // clear to let GC do its work
    for (int i = 0; i < size; i++)
        elementData[i] = null;

    size = 0;
}

/**
 * Removes from this list all of the elements whose index is between
 * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
 * Shifts any succeeding elements to the left (reduces their index).
 * This call shortens the list by {@code (toIndex - fromIndex)} elements.
 * (If {@code toIndex==fromIndex}, this operation has no effect.)
 *
 * @throws IndexOutOfBoundsException if {@code fromIndex} or
 *         {@code toIndex} is out of range
 *         ({@code fromIndex < 0 ||
 *          fromIndex >= size() ||
 *          toIndex > size() ||
 *          toIndex < fromIndex})
 */
protected void removeRange(int fromIndex, int toIndex) {
//移除一个区间内的元素
    modCount++;
    int numMoved = size - toIndex;
    System.arraycopy(elementData, toIndex, elementData, fromIndex,
                     numMoved);
    // clear to let GC do its work
    int newSize = size - (toIndex-fromIndex);
    for (int i = newSize; i < size; i++) {
        elementData[i] = null;
    }
    size = newSize;
}

/**
 * Removes from this list all of its elements that are contained in the
 * specified collection.
 *
 * @param c collection containing elements to be removed from this list
 * @return {@code true} if this list changed as a result of the call
 * @throws ClassCastException if the class of an element of this list
 *         is incompatible with the specified collection
 * (<a href="Collection.html#optional-restrictions">optional</a>)
 * @throws NullPointerException if this list contains a null element and the
 *         specified collection does not permit null elements
 * (<a href="Collection.html#optional-restrictions">optional</a>),
 *         or if the specified collection is null
 * @see Collection#contains(Object)
 */
public boolean removeAll(Collection<?> c) {
    Objects.requireNonNull(c);//判断是否为Null
    return batchRemove(c, false);
}

//Objects里面的方法
public static <T> T requireNonNull(T obj) {
    if (obj == null)
        throw new NullPointerException();
    return obj;
}

private boolean batchRemove(Collection<?> c, boolean complement) {
    final Object[] elementData = this.elementData;//记录原集合为elementData
    int r = 0, w = 0;
    boolean modified = false;
    try {
        for (; r < size; r++)
            if (c.contains(elementData[r]) == complement)
                elementData[w++] = elementData[r];//判断集合是否有，有就给elementData
    } finally {
        // Preserve behavioral compatibility with AbstractCollection,
        // even if c.contains() throws.
        if (r != size) {
        //如果数量不相等 则把剩下的元素给elementData
            System.arraycopy(elementData, r,
                             elementData, w,
                             size - r);
            w += size - r;
        }
        if (w != size) {
            // clear to let GC do its work
            for (int i = w; i < size; i++)
                elementData[i] = null;
            modCount += size - w;
            size = w;
            modified = true;
        }
    }
    return modified;
}

修改

/**
 * Replaces the element at the specified position in this list with
 * the specified element.
 *
 * @param index index of the element to replace
 * @param element element to be stored at the specified position
 * @return the element previously at the specified position
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public E set(int index, E element) {
//修改指定index位置下的元素
    rangeCheck(index);

    E oldValue = elementData(index);
    elementData[index] = element;
    return oldValue;//返回旧的元素
}

查询

/**
 * Returns the element at the specified position in this list.
 *
 * @param  index index of the element to return
 * @return the element at the specified position in this list
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public E get(int index) {
//查询指定位置下的元素
    rangeCheck(index);

    return elementData(index);
}

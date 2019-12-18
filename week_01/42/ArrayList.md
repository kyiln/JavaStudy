/**
*
* 实现了 RandomAccess 可以随机访问；Cloneable 可以进行clone ；实现了序列号接口Serializable
* 遗漏问题：modCount 作用是什么？？？？？？
*/
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    private static final long serialVersionUID = 8683452581122892189L;

    /**
     * 默认大小（实际上如果用无参构造方法创建一个集合的时候初始时候大小是0，当第一次调用add方法的时候会直接设置大小为10）
     */
    private static final int DEFAULT_CAPACITY = 10;

	/**
	* 两个不同的属性，不同的方法初始化集合的时候会用到，
	*/
    private static final Object[] EMPTY_ELEMENTDATA = {};
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer. Any
     * empty ArrayList with elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
     * will be expanded to DEFAULT_CAPACITY when the first element is added.
	 * 集合底层就是一个数组的实现，通过数组来进行数据的存储。这里英文说得非常的明确，
	 * 初始化为空的集合的时候该属性指向DEFAULTCAPACITY_EMPTY_ELEMENTDATA
	 * 当添加第一个元素的时候，扩展长度为DEFAULT_CAPACITY（10）
     */
    transient Object[] elementData; 


    private int size;

    /**
	 * 指定大小创建集合对象
     */
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {// 长度大于0则初始化的时候集合大小为指定大小
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) { // 指定大小= 0则，默认EMPTY_ELEMENTDATA 
            this.elementData = EMPTY_ELEMENTDATA;
        } else { // 小于0，会报该异常（属于RuntimeException）
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        }
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
	 * 把原英文注解留在这里主要是说明并不是初始化的时候长度就是10，而是add第一个元素的时候，会直接设置长度为10
     */
    public ArrayList() {
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
		// 通过拷贝的方式，把入参的数据拷贝到该集合对象的数组下面
        elementData = c.toArray();
        if ((size = elementData.length) != 0) {
			// 当前集合A如果是ArrayList<String> 直接添加Integer元素是不可行的，但是如果添加一个ArrayList<Integer> 集合B则可以
			// 如果代码就是做了一个容错处理
            // c.toArray might (incorrectly) not return Object[] (see 6260652)
            if (elementData.getClass() != Object[].class)
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            // 如果入参集合为空，则当前集合指向 EMPTY_ELEMENTDATA （保证空集合都指向 EMPTY_ELEMENTDATA 减少内存开销）
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }


	// 实际应用场景不太知道有何作用
    public void trimToSize() {
        modCount++;（问题：不知道有何作用）
        if (size < elementData.length) {
            elementData = (size == 0)
              ? EMPTY_ELEMENTDATA
              : Arrays.copyOf(elementData, size);
        }
    }


    // 保证指定大小长度，如果长度不够则扩容，如果指定长度小于集合长度，不会处理。
    public void ensureCapacity(int minCapacity) {
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
            // any size if not default element table
            ? 0
            // larger than default for default empty table. It's already
            // supposed to be at default size.
            : DEFAULT_CAPACITY;

        if (minCapacity > minExpand) {
            ensureExplicitCapacity(minCapacity);
        }
    }

	// 添加元素的时候，保证集合长度
    private void ensureCapacityInternal(int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        ensureExplicitCapacity(minCapacity);
    }

    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
	 * 也就是说集合最大长度只能是Integer.MAX_VALUE - 8，有的JVM需要额外的长度来存储一些关键字
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);//  右移运算，右移一位，相当于除以2
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity); // 保证集合长度不不超过最大长度
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }


    public int size() {
        return size;
    }


    public boolean isEmpty() {
        return size == 0;
    }


	// 通过遍历的方式，查找对应的元素
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    // 这里很明显可以看出来，ArrayList是可以存储null的
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }


    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size-1; i >= 0; i--)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = size-1; i >= 0; i--)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }


    public Object clone() {
        try {
            ArrayList<?> v = (ArrayList<?>) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

   
	// 设置指定位置元素，返回老元素
    public E set(int index, E element) {
        rangeCheck(index);

        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }
	
	//添加元素
    public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }

	// 在指定位置添加元素，指定位置元素，全部向后移
    public void add(int index, E element) {
        rangeCheckForAdd(index);

        ensureCapacityInternal(size + 1);  // Increments modCount!!
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);
        elementData[index] = element;
        size++;
    }

	// 删除元素的时候相当于指定元素之后的全部元素向前移动一位，然后最后一位元素设置为null
    public E remove(int index) {
        rangeCheck(index);

        modCount++;
        E oldValue = elementData(index);

        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // clear to let GC do its work

        return oldValue;
    }

	// 删除指定元素，集合可以有重复元素，有该元素则都删除。
	// 遍历集合，找到该元素的位置，然后通过拷贝后面的元素，再把集合最后一位元素设置为null
    public boolean remove(Object o) {
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

	// 拷贝
    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // clear to let GC do its work
    }

	// 清除集合，设置所有元素为null
    public void clear() {
        modCount++;

        // clear to let GC do its work
        for (int i = 0; i < size; i++)
            elementData[i] = null;

        size = 0;
    }



	// 删除指定范围内元素，
    protected void removeRange(int fromIndex, int toIndex) {
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
    
}

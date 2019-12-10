在分析动态数组ArrayList的源码之前先对其底层数据结构——数组进行分析
# 数组
数组是一种最基本的数据结构，采用了一组连续的内存空间按顺序存储对象或基本数据类型。其访问的时间复杂度为O(1)，插入以及删除由于涉及到元素的移动时间复杂度为O(n)，在Java中声明一个数组需要事先指定数组的大小，那么就可能会造成空间浪费以及扩容问题，为了解决这些问题，动态数组ArrayList就诞生了。

# ArrayList
```java
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
```
（1）实现了RandomAccess接口，标识其具备随机访问功能  
（2）实现了List，提供了List基础的增，删，改，查等操作  
（3）实现了Cloneable，可以被克隆  
（4）实现了Serializable，可以被序列化  

### 属性
```java
    /**
     * 默认的容量大小为10
     * Lazy-load: 只有在ArrayList真正添加元素的时候才会通过DEFAULT_CAPACITY创建数组, 
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * 当指定ArrayList容量为0的时候, 底层使用的空数组
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * 调用默认构造函数ArrayList()时，使用这个空数组作为elementData
     * 与EMPTY_ELEMENTDATA的区别在于: DEFAULTCAPACITY_EMPTY_ELEMENTDATA在第一次添加元素时会变为长度为DEFAULT_CAPACITY的数组
     */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    /**
     * 真正存储元素的数组
     * 标记了transient，此字段不会被序列化
     */
    transient Object[] elementData; // non-private to simplify nested class access

    /**
     * 容器中元素的个数
     */
    private int size;
```

### 构造函数
```java
	/**
	* 1. 指定容器大小初始化ArrayList
	*/
	public ArrayList(int initialCapacity) {
	  //若传入初始Capacity > 0, 那么就根据Capacity创建对应长度的Object数组
	  if (initialCapacity > 0) {
	      this.elementData = new Object[initialCapacity];
	  //若传入初始Capacity = 0, 那么就将EMPTY_ELEMENTDATA这个空数组赋给elementData
	  } else if (initialCapacity == 0) {
	      this.elementData = EMPTY_ELEMENTDATA;
	  //若传入初始Capacity < 0, throw Exception
	  } else {
	      throw new IllegalArgumentException("Illegal Capacity: "+
	                                         initialCapacity);
	  }
	}
	
	/**
	* 2. 默认构造函数
	*/
	public ArrayList() {
	  //将DEFAULTCAPACITY_EMPTY_ELEMENTDATA这个空数组赋给elementData
	  this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
	}
	
	/**
	* 3. 通过传入一个集合初始化ArrayList
	*/
	public ArrayList(Collection<? extends E> c) {
	  //把传入的集合元素转换为数组拷贝给elementData
	  elementData = c.toArray();
	  //检查拷贝之后, elementData的长度是否为0
	  if ((size = elementData.length) != 0) {
	      // c.toArray或许不能正确地转换为Object数组
	      if (elementData.getClass() != Object[].class)
	      	  //若没有正确转换为Object[], 重新拷贝成Object[].class类型再赋给elementData
	          elementData = Arrays.copyOf(elementData, size, Object[].class);
	  } else {
	      // 若拷贝之后elementData长度为0, 那么直接将EMPTY_ELEMENTDATA赋给elementData
	      this.elementData = EMPTY_ELEMENTDATA;
	  }
	}
```

### add(E e)	添加元素到数组末尾
（1）明确添加元素后数组所需要的最小长度minCapacity  
（2）如果minCapacity > elementData.length，需要进行数组扩容  
（3）在末尾处添加元素  
（4）添加成功return true  
  
```java
	/**
     * 添加元素到数组末尾, 时间复杂度为O(1)
     */
    public boolean add(E e) {
    	//确保ArrayList有足够的Capacity添加新的元素
        ensureCapacityInternal(size + 1);
        //把元素插入到elementData的末尾元素之后
        elementData[size++] = e;
        return true;
    }
	
    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }
	
	private static int calculateCapacity(Object[] elementData, int minCapacity) {
		//如果当前ArrayList处于刚初始化的状态, 就返回默认长度10
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        //如果ArrayList已经处于正在使用的状态(已经进行了添加操作), 直接返回传入的minCapacity
        return minCapacity;
    }
	
	
	private void ensureExplicitCapacity(int minCapacity) {
        modCount++;//modCount用于记录操作ArrayList的次数
        
        // 如果elementData的长度小于添加元素所需要的minCapacity, 需要对原数组进行扩容操作
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }
	
	private void grow(int minCapacity) {
        //原数组长度
        int oldCapacity = elementData.length;
        //新数组的长度 = 原数组长度的1.5倍
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        //如果新数组的长度依然小于minCapacity, 那么以minCapacity为准进行数组扩容
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        //若新数组长度已经超过ArrayList规定的最大长度, 则使用最大长度
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        //底层是根据newCapacity创建了一个新的数组, 然后在这个新数组之上添加原数组的元素
 		//将新数组赋值给elementData
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
```

### add(int index, E element) 在指定位置添加元素
（1）检查index是否越界  
（2）检查是否需要扩容  
（3）右移index以及后续所有元素  
（4）index位置插入新元素  
（5）size++  
```java
	/**
     * 在指定位置插入元素, 并将原本在该位置的元素以及右边的所有元素向右移动一位
     */
    public void add(int index, E element) {
    	//检查index是否越界
        rangeCheckForAdd(index);
		//确保ArrayList有足够的Capacity添加新的元素
        ensureCapacityInternal(size + 1);
        //移动原本在该位置的元素以及右边的所有元素
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);
        //在index位置插入元素
        elementData[index] = element;
        size++;
    }
	
	/**
     * 确保index合法
     */
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
```

### addAll(Collection c) 在末尾追加集合c中的所有元素
（1）将集合c转换为Object数组  
（2）检查是否需要扩容  
（3）将集合c中的元素全部拷贝到原数组末尾  
```java
	public boolean addAll(Collection<? extends E> c) {
		//将集合c转换为Object[]
        Object[] a = c.toArray();
        int numNew = a.length;
        //检查是否需要扩容
        ensureCapacityInternal(size + numNew);  // Increments modCount
        //将集合c中的元素全部拷贝到原数组末尾
        System.arraycopy(a, 0, elementData, size, numNew);
        size += numNew;
        //若c不为空return true, else return false
        return numNew != 0;
    }
```

### addAll(int index, Collection c) 指定位置添加集合c的所有元素
（1）检查index是否越界  
（2）将集合c转换为Object数组  
（3）检查是否需要扩容  
（4）移动index以及右边的所有元素，挪出空间存放集合c的所有元素  
（5）index位置添加集合c的所有元素  
```java
	public boolean addAll(int index, Collection<? extends E> c) {
		//检查index是否合法
        rangeCheckForAdd(index);
		//这里的几个步骤都和addAll(Collection c)相同
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);  // Increments modCount
		//需要移动的元素个数
        int numMoved = size - index;
        //若需要移动的元素个数大于0, 那么需要进行元素移动操作
        if (numMoved > 0)
        	//右移index以及右边的所有元素, 挪出空间用于存放集合c的元素
            System.arraycopy(elementData, index, elementData, index + numNew,
                             numMoved);
		//在index位置将集合c的所有元素拷贝至elementData
        System.arraycopy(a, 0, elementData, index, numNew);
        size += numNew;
        return numNew != 0;
    }
```

### get(int index) 获取指定位置上的元素
（1）检查index是否越界  
（2）返回index位置上的元素  
```java
	public E get(int index) {
        rangeCheck(index);
        return elementData(index);
    }
    
	/**
     * 这里只检查是否越上界, 如果越下界会抛出ArrayIndexOutOfBoundsException
     */
    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
	
	E elementData(int index) {
        return (E) elementData[index];
    }
```

### remove(int index) 删除指定位置上的元素并返回
（1）检查index是否越界  
（2）获取index位置上的元素  
（3）若index不指向末尾元素，index后面的元素前移一位  
（4）末尾元素置为null，便于GC回收  
（5）返回删除元素  
```java
    public E remove(int index) {
    	//检查index是否越界
        rangeCheck(index);

        modCount++;
        //获取index位置上的元素
        E oldValue = elementData(index);
		//如果index并不指向最后一个元素, 那么就前移index后面的元素
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
       	//elementData最后一位置为null, 消除引用便于GC回收
        elementData[--size] = null;
        return oldValue;
    }
```

### remove(Object o)  删除指定元素值
（1）遍历整个数组找到指定元素  
（2）fastRemove  
```java
	/**
     	删除数组中第一次出现的指定元素, 删除成功return true, else return false
     */
    public boolean remove(Object o) {
    	//遍历整个数组, 删除第一个出现的null元素 (这里使用"=="进行比较)
        if (o == null) {
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
        } else {//遍历整个数组, 删除第一个出现的指定元素 (这里使用"equals()"进行比较)
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }

	/*
     * 删除指定位置的元素
     * 与remove()相比, 该方法略去了越界检查以及返回删除元素, 其他步骤都是一样的
     * 之所以这样做是因为, 省去了不必要的检查操作, 提升了性能
     */
    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // clear to let GC do its work
    }
```

### retainAll(Collection c) 求与集合c的交集部分
（1）集合c检查是否为空  
（2）遍历element，保留与集合c相交的部分，删除不相交的部分（写指针之后的所有元素置为空）  
（3）若成功批量删除return true，else return false  
```java
    public boolean retainAll(Collection<?> c) {
        //要求集合c不为空
        Objects.requireNonNull(c);
        //调用批量删除方法, 此时boolean complement = true, 表示删除不包含在c中的元素
        return batchRemove(c, true);
    }
	
	/**
	 *	批量删除元素
	 *	complement = true: 删除c中不包含的元素
	 *	complement = false: 删除c中包含的元素	
	*/
    private boolean batchRemove(Collection<?> c, boolean complement) {
        final Object[] elementData = this.elementData;	
        //初始化读指针r, 写指针w
        int r = 0, w = 0;
        //是否成功修改的返回值, 默认false
        boolean modified = false;
        try {
        	//遍历整个数组, 如果集合c中包含读指针指向的元素, 且complement = ture, 就将读指针指向的元素放在写指针指向的位置, 这样就留下了集合c与ArrayList相交的部分, 删除了不相交的元素
        	//如果集合c中不包含读指针指向的元素, 且complement = false, 就将读指针指向的元素放在写指针指向的位置, 这样就留下了集合c与ArrayList不相交的部分, 删除了相交的元素
        	//整个操作都在原数组上完成, 不需要额外空间
        	//这个操作有点类似LeetCode上移动零那道题, 比较典型的双指针解法
            for (; r < size; r++)
                if (c.contains(elementData[r]) == complement)
                    elementData[w++] = elementData[r];
        } finally {
            //正常情况下, 读指针最后一定是指向size的, 除非c.contains()抛出了异常
            if (r != size) {
            	//若c.contains抛出异常, 则把未读元素都拷贝到写指针之后
                System.arraycopy(elementData, r,
                                 elementData, w,
                                 size - r);
                //写指针指向相应的位置
                w += size - r;
            }
            //若写指针不指向末尾元素, 那么写指针后序的元素都需要置为空
            if (w != size) {
                // clear to let GC do its work
                for (int i = w; i < size; i++)
                    elementData[i] = null;
                modCount += size - w;
                //此时删除后的size等于写指针所指的位置
                size = w;
                //标识完成了整个批量删除
                modified = true;
            }
        }
        return modified;
    }
```

### removeAll(Collection c) 求与集合c的差集
```java
	/**
     * 保留当前集合中不与c相交的元素
     */
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        //同样调用了批量删除方法, complement = false意味着删除包含在c中的元素
        return batchRemove(c, false);
    }
```

# 总结
（1）ArrayList底层采用Object数组进行数据存储，每次添加时都会检查是否需要扩容，若容量不足则通过创建一个新的长度为原长度1.5倍的数组，并将原数组拷贝至新数组并返回。  
（2）ArrayList随机访问时间复杂度为O(1)  
（3）ArrayList添加元素到末尾的时间复杂度为O(1)，添加元素到中间位置的时间复杂度为O(n)  
（4）ArrayList删除末尾元素的时间复杂度为O(1)，删除中间位置元素的时间复杂度为O(n)  
（5）ArrayList支持求并集，调用addAll(Collection c)  
（6）ArrayList支持求交集，调用retainAll(Collection c)  
（7）ArrayList支持求单向差集，调用removeAll(Collection c)  

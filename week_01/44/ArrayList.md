# 一：简介 #
ArrayList是一种以数组实现的List。
![](C:\Users\asiaw\Desktop\考研报名\ss\1.png)

- 继承了AbstractList类
- 实现了List<E>, RandomAccess, Cloneable, java.io.Serializable接口
- ArrayList实现了List，提供了基础的添加、删除、遍历等操作。
- ArrayList实现了RandomAccess，提供了随机访问的能力。
- ArrayList实现了Cloneable，可以被克隆。
- ArrayList实现了Serializable，可以被序列化。

# 二：源码分析 #
## 1. 字段 ##

```java
private static final int DEFAULT_CAPACITY = 10;

private static final Object[] EMPTY_ELEMENTDATA = {};

private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

transient Object[] elementData; // non-private to simplify nested class access

private int size;
```
（1）DEFAULT_CAPACITY = 10

默认容量为10，初始化时没有参数传递进来时的默认初始容量

（2）EMPTY_ELEMENTDATA = {} 

空数组。通过 new ArrayList(0)方法创建ArrayList时使用的空数组

（3）DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {}

空数组。通过 new ArrayList()方法创建ArrayList时使用的空数组。与EMPTY_ELEMENTDATA不同的地方是在添加第一个元素时使用这个空数组的会初始化为DEFAULT_CAPACITY（10）个元素

（4）elementData

真正存放数据的地方，使用 transient 关键字是为了不序列化这个字段。没有使用private修饰备注解释为简化nested class access

（5）size

当前元素的个数

## 2. 构造方法

```java
  	public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }
```

ArrayList()

没有参数，elementData初始化为DEFAULTCAPACITY_EMPTY_ELEMENTDATA空数组，在第一次添加元素时会扩容成默认的容量大小10。

```java
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

public ArrayList(int initialCapacity)

参数为初始容量。如果该数 > 0 即将elementData初始化为参数大小。若 = 0，elementData初始化为EMPTY_ELEMENTDATA空数组。若 < 0，抛出异常，显示“Illegal Capacity”。

```Java
public ArrayList(Collection<? extends E> c) {
        elementData = c.toArray();
        if ((size = elementData.length) != 0) {            
            if (elementData.getClass() != Object[].class)
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {            
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }
```

ArrayList(Collection<? extends E> c)

参数为集合。用这个集合的 toArray() 方法初始化elementData。如果元素个数为0，elementData初始化为EMPTY_ELEMENTDATA；不为0，并且elementData的类型与Object的类型不相同时，利用Arrays的copyof方法把类型转换为Object[].class类型。

## 3. add方法

```java
    public boolean add(E e) {
        // 检查是否需要扩容
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        // 添加元素到最后一位。size++。返回true
        elementData[size++] = e;
        return true;
    }    
		
    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }
	
	// 计算最小容量
    private static int calculateCapacity(Object[] elementData, int minCapacity) {
        // 如果是DEFAULTCAPACITY_EMPTY_ELEMENTDATA这个空数组，返回默认大小10
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }
	
    protected transient int modCount = 0;

    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // 溢出则扩容
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }
	
	// 定义的最大容量
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        // 新容量为旧容量的1.5倍
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        // 如果新容量仍小于最小容量，改为所需的最小容量
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        // 若新容量大于定义最大容量，则若所需最小容量小于定义最大容量，使用定义最大容量，否则使用Integer.MAX_VALUE
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        // 创建新容量的数组并拷贝数据
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
```

add(E e)

一个参数，要加入的元素。

（1）先检查是否需要扩容。

（2）如果elementData是用 new ArrayList()初始化的，将容量初始化为默认大小10。

（3）扩容时，新容量为旧容量的1.5倍。如果还不够，则扩容为所需大小。如果超出定义最大容量，则看情况改为定义最大容量或Integer的最大数。

（4）创建新数组拷贝数据

```java
    public void add(int index, E element) {
        // 检查是否越界
        rangeCheckForAdd(index);
        // 检查是否需要扩容
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        // 将index及以后的数据都往后移一位，将index处空出来        
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);
        // 添加元素到指定位置
        elementData[index] = element;
        // 元素个数加 1
        size++;
    }

    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
```

add(int index, E element)

参数指定位置和元素。将元素添加到指定位置，平均时间复杂度为O(n)。

（1）检查是否越界

（2）检查是否需要扩容

（3）把指定位置往后的元素都向后移动一位

（4）添加元素到指定位置

（5）元素个数加1

```java
    public boolean addAll(Collection<? extends E> c) {
        // 转化为数组
        Object[] a = c.toArray();
        int numNew = a.length;
        // 检查是否需要扩容
        ensureCapacityInternal(size + numNew);  // Increments modCount
        // 将C中的所有元素拷贝到elementData的最后
        System.arraycopy(a, 0, elementData, size, numNew);
        // 元素个数增加C的元素个数
        size += numNew;
        // c 不为空返回true，为空返回false
        return numNew != 0;
    }	
```

addAll(Collection<? extends E> c)

将集合C中的所有元素添加到当前的ArrayList中

（1）将集合C转化为数组

（2）检查是否需要扩容

（3）将C中元素拷贝到elementData的末尾，size添加C的元素个数

```java
    public boolean addAll(int index, Collection<? extends E> c) {
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
```

addAll(int index, Collection<? extends E> c)

将集合C拷贝到当前list的指定位置

## 4. get方法

```java
    public E get(int index) {
        // 检查是否越界
        rangeCheck(index);
        // 返回指定位置元素
        return elementData(index);
    }

    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    E elementData(int index) {
        return (E) elementData[index];
    }
```

get(int index)

返回指定位置的元素。时间复杂度O(1)

## 5. remove方法

```Java
    public E remove(int index) {
        // 检查是否越界
        rangeCheck(index);

        modCount++;
        // 获取index处元素
        E oldValue = elementData(index);
        // 计算需要移动的元素
        int numMoved = size - index - 1;
        // 将index后面的元素向前移动一位
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        // 将最后一个元素删除，帮助GC
        elementData[--size] = null; // clear to let GC do its work
        // 返回删除元素		
        return oldValue;
    }
```

remove(int index)：删除指定位置的元素，时间复杂度O(n)

（1）检查是否越界

（2）获取index处位置

（3）将index后的元素向前移动一位

（4）删除最后一个元素并返回删除的index处的元素

```java
    public boolean remove(Object o) {
        if (o == null) {
            // 遍历数组，找到第一个为空的位置，删除并返回true
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
        } else {
            // 遍历数组，找到元素第一次出现的位置，删除并返回true
            for (int index = 0; index < size; index++)
                // 元素不为空，使用 equals() 方法进行比较是否相等
                if (o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        // 没找到返回false
        return false;
    }
	
	// 没有判断是否越界，因为不是指定位置。
    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // clear to let GC do its work
    }
```

（1）找到第一个等于指定元素值的元素索引

（2）快速删除，不适用remove(index)方法是因为可以省略判断越界方法

```Java
    public boolean retainAll(Collection<?> c) {
        // 集合C不能为空
        Objects.requireNonNull(c);
        // 调用batchremove方法，传入true，表示删除不在C中的元素
        return batchRemove(c, true);
    }
	/**
	* 批量删除元素
	* complement为true表示删除C中不包含的元素
	* complement为false表示删除C中包含的元素
	*/
    private boolean batchRemove(Collection<?> c, boolean complement) {
        final Object[] elementData = this.elementData;
        // 使用读写两个指针同时遍历数组
        // 读指针每次加1，写指针写入元素时才加1
        // 这样不需要额外空间，在原数组上操作
        int r = 0, w = 0;
        boolean modified = false;
        try {
            // 遍历整个数组，如果C中包含该元素，则把该元素写入数组
            for (; r < size; r++)
                if (c.contains(elementData[r]) == complement)
                    elementData[w++] = elementData[r];
        } finally {
            // Preserve behavioral compatibility with AbstractCollection,
            // even if c.contains() throws.
            if (r != size) {
                System.arraycopy(elementData, r,
                                 elementData, w,
                                 size - r);
                w += size - r;
            }
            if (w != size) {
                // clear to let GC do its work
                // 将写指针之后的元素置空，帮助GC
                for (int i = w; i < size; i++)
                    elementData[i] = null;
                modCount += size - w;
                // 更新元素个数
                size = w;
                modified = true;
            }
        }
        // 有修改返回true，无返回false
        return modified;
    }
```

retainAll(Collection<?> c)：返回两个集合的交集

（1）遍历elementData数组；

（2）如果元素在c中，则把这个元素添加到elementData数组的w位置并将w位置往后移一位；

（3）遍历完之后，w之前的元素都是两者共有的，w之后（包含）的元素不是两者共有的；

（4）将w之后（包含）的元素置为null，方便GC回收；

```Java
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, false);
    }
```

removeAll(Collection<?> c)：保留不在集合C中的位置

```java
    public void clear() {
        modCount++;

        // clear to let GC do its work
        for (int i = 0; i < size; i++)
            elementData[i] = null;

        size = 0;
    }
```

clear()：清空当前ArrayList

## 6. size,set,contains,indexof,lastindexof,isempty,trimToSize方法

```java
public int size() {        return size;    }

public E set(int index, E element) {
    // 检查是否越界
    rangeCheck(index);

    E oldValue = elementData(index);
    // 更新元素
    elementData[index] = element;
    // 返回旧值
    return oldValue;
}

public boolean isEmpty() {        return size == 0;    }

public int indexOf(Object o) {
    // 遍历查找是否存在元素，为空使用 == 方法，不为空使用 equals() 方法
    if (o == null) {
        for (int i = 0; i < size; i++)
            if (elementData[i]==null)
                return i;
    } else {
        for (int i = 0; i < size; i++)
            if (o.equals(elementData[i]))
                return i;
    }
    // 没有找到返回 -1 
    return -1;
}

// 返回最后出现指定元素的位置。。从数组末尾开始遍历即可
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

// 调用indexof方法，不返回-1则表示存在此元素
public boolean contains(Object o) {        return indexOf(o) >= 0;    }

// 将数组大小调整为size
public void trimToSize() {
    modCount++;
    if (size < elementData.length) {
        elementData = (size == 0)
            ? EMPTY_ELEMENTDATA
            : Arrays.copyOf(elementData, size);
    }
}
```

# 三：总结

1. 序列化和反序列化没仔细看。接口实现部分也没有很仔细看

2. 一些ArrayList常见的方法都进行了分析。
























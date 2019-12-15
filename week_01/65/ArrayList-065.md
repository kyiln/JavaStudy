# ArrayList 源码阅读笔记

## 一. 简介

ArrayList是日常编码中使用频率最高的数据结构，也是实现最简单的数据结构。它和数组一样有较高的随机访问效率，又实现了动态扩容，相当于动态数组。ArrayList是非线程安全容器。

## 二. 实现接口

ArrayList实现了List接口，是一个有序的线性集合，具有添加、删除、插入、遍历等操作。
ArrayList实现了Cloneable接口，实现为浅拷贝。
ArrayList实现了序列化接口Serializable，可以被序列化。
ArrayList实现了随机访问接口RandomAccess，这是一个标记接口，实现了这个接口的集合for循环遍历效率高于iterator迭代器遍历。

## 三. 核心源码

### 1. 类属性

```java
/**
 * 默认初始化容量
 */
private static final int DEFAULT_CAPACITY = 10;

/**
 * 为所有空集合实例共用的空数组
 */
private static final Object[] EMPTY_ELEMENTDATA = {};

/**
 * 用于以无参构造方法进行初始化的空实例的共享空数组。区分它和
 * EMPTY_ELEMENTDATA 以了解添加第一个元素时需要扩容
 */
private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

/**
 * 存储ArrayList元素的数组缓冲区。
 * ArrayList的容量是这个数组的长度.每个空ArrayList的elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA时
 * 都会被扩容到DEFAULT_CAPACITY。
 */
transient Object[] elementData;

/**
 * ArrayList中元素的个数
 *
 */
private int size;

/**
 * 数组最大长度。Java中的数组是由JVM构造的类，根据OOP-Klass二分模型，对象有对象头，而数组不能自己计算自己的长度，需要8字节
 * 存储长度信息，所以是Integer.MAX_VALUE - 8
 */
private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

/**
 * 这是一个从父类AbstractList中继承而来的属性，记录了ArrayList被结构性修改的次数
 * java.util包下的集合类都是快速失败（fail—fast）的，不能在多线程下发生并发修改（迭代过程中被修改）
 */
protected transient int modCount = 0;
```

### 2. 核心方法

#### 构造方法

初始容量为参数，如果大于0就初始化elementData为对应大小，如果等于0就使用EMPTY_ELEMENTDATA空数组，如果小于0抛出异常。

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

无参构造方法，elementData初始化为DEFAULTCAPACITY_EMPTY_ELEMENTDATA。

```java
public ArrayList() {
    this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
}
```

传入一个集合初始化，如果传入集合元素个数为0，则初始化为EMPTY_ELEMENTDATA空数组，否则使用Arrays.copyOf()拷贝到elementData。

```java
public ArrayList(Collection<? extends E> c) {
    elementData = c.toArray();
    if ((size = elementData.length) != 0) {
        // c.toArray might (incorrectly) not return Object[] (see 6260652)
        if (elementData.getClass() != Object[].class)
            elementData = Arrays.copyOf(elementData, size, Object[].class);
    } else {
        // replace with empty array.
        this.elementData = EMPTY_ELEMENTDATA;
    }
}
```

#### 添加元素

add(E e)方法将单个元素添加到ArrayList的尾部，其中涉及容量检查和扩容处理，平均时间复杂度O(1)。

```java
public boolean add(E e) {
    ensureCapacityInternal(size + 1);  // 检查是否需要扩容
    elementData[size++] = e;
    return true;
}

private void ensureCapacityInternal(int minCapacity) {
    ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
}

private static int calculateCapacity(Object[] elementData, int minCapacity) {
    /*如果elementData是由无参构造方法初始化为DEFAULTCAPACITY_EMPTY_ELEMENTDATA的，则需要扩容到DEFAULT_CAPACITY
     */
    if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
        return Math.max(DEFAULT_CAPACITY, minCapacity);
    }
    return minCapacity;
}

private void ensureExplicitCapacity(int minCapacity) {
    // 增加modCount，添加元素是结构性修改
    modCount++;

    // 扩容
    if (minCapacity - elementData.length > 0)
        grow(minCapacity);
}

private void grow(int minCapacity) {
    int oldCapacity = elementData.length;
    // 先将新容量设为旧容量的1.5倍。 >> 1 有符号右移一位，相当于除以2
    int newCapacity = oldCapacity + (oldCapacity >> 1);
    if (newCapacity - minCapacity < 0)
        // 如果新容量达不到需求，那容量就以minCapacity为准
        newCapacity = minCapacity;
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        /**当传入容量参数太大，大到超过了数组的容量限定值Integer.MAX_VALUE-8却又小于整数限定值Integer.MAX_VALUE，那么新
         * 的数组容量以整数限定值Integer.MAX_VALUE为准，但是当传入的容量参数不大于数组的容量限定值时，以容量限定值
         * Integer.MAX_VALUE-8为准。
         */
        newCapacity = hugeCapacity(minCapacity);
    // 拷贝元素到新扩容的数组
    elementData = Arrays.copyOf(elementData, newCapacity);
}

private static int hugeCapacity(int minCapacity) {
    if (minCapacity < 0) // 溢出报错
        throw new OutOfMemoryError();
    return (minCapacity > MAX_ARRAY_SIZE) ?
        Integer.MAX_VALUE :
        MAX_ARRAY_SIZE;
}
```

add(int index, E element)方法可以将element添加到指定的index上。

```java
public void add(int index, E element) {
    rangeCheckForAdd(index);

    // 检查是否需要扩容
    ensureCapacityInternal(size + 1);
    // 复制并调整数组内元素位置
    System.arraycopy(elementData, index, elementData, index + 1,
                        size - index);
    elementData[index] = element;
    size++;
}

private void rangeCheckForAdd(int index) {
    // 检查插入index是否合法，不合法抛出异常
    if (index > size || index < 0)
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}
```

addAll(Collection<? extends E> c)方法可以将一个集合中的所有元素全部有序添加到ArrayList尾部。
addAll(int index, Collection<? extends E> c)方法可以将一个集合插入到ArrayList指定index上。

```java
public boolean addAll(Collection<? extends E> c) {
    // 集合转数组
    Object[] a = c.toArray();
    int numNew = a.length;
    // 扩容处理
    ensureCapacityInternal(size + numNew);
    // 元素复制
    System.arraycopy(a, 0, elementData, size, numNew);
    size += numNew;
    return numNew != 0;
}

public boolean addAll(int index, Collection<? extends E> c) {
    // 检查index合法性
    rangeCheckForAdd(index);

    Object[] a = c.toArray();
    int numNew = a.length;
    // 扩容
    ensureCapacityInternal(size + numNew);

    // 元素复制
    int numMoved = size - index;
    if (numMoved > 0)
        System.arraycopy(elementData, index, elementData, index + numNew,
                            numMoved);

    System.arraycopy(a, 0, elementData, index, numNew);
    size += numNew;
    return numNew != 0;
}
```

#### 删除元素

remove(int index)方法将删除指定index上的元素，并返回该元素。删除时需要进行遍历调整index，平均时间复杂度O(n)。

```java
public E remove(int index) {
    // 检查index是否合法
    rangeCheck(index);

    // 增加modCount，删除元素也是结构性修改
    modCount++;
    // 获取待删除index上的元素
    E oldValue = elementData(index);

    // 如果index不是最后一位，则将index之后的元素往前挪一位
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index,
                            numMoved);
    elementData[--size] = null; // 最后一个元素的位置置为null，有利于GC

    return oldValue;
}

private void rangeCheck(int index) {
    // 检查index是否越界
    if (index >= size)
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}

E elementData(int index) {
    return (E) elementData[index];
}
```

remove(Object o)方法删除指定元素值的方法(通过equals()方法判断)，平均时间复杂度O(n)。

```java
public boolean remove(Object o) {
    if (o == null) {
        // 遍历寻找index并删除null元素
        for (int index = 0; index < size; index++)
            if (elementData[index] == null) {
                fastRemove(index);
                return true;
            }
    } else {
        // 遍历寻找index并删除普通元素
        for (int index = 0; index < size; index++)
            if (o.equals(elementData[index])) {
                fastRemove(index);
                return true;
            }
    }
    return false;
}

private void fastRemove(int index) {
    // fastRemove(int index)相比remove(int index)方法，少了index越界检查。
    // 增加modCount
    modCount++;

    // 如果index不是最后一位，则将index之后的元素往前挪一位
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index,
                            numMoved);
    elementData[--size] = null; // 最后一个元素的位置置为null，有利于GC
}
```

removeAll(Collection<?> c)方法可以从ArrayList中删除一个指定集合.

```java
public boolean removeAll(Collection<?> c) {
    // 指定集合不能为null
    Objects.requireNonNull(c);
    // 批量删除
    return batchRemove(c, false);
}

/**
 * 批量删除元素
 * complement为true表示删除c中不包含的元素
 * complement为false表示删除c中包含的元素
 */
private boolean batchRemove(Collection<?> c, boolean complement) {
    final Object[] elementData = this.elementData;
    // 读写分别用两个index表示
    int r = 0, w = 0;
    boolean modified = false;
    try {
        // 遍历整个数组，根据complement把该元素放到写index的位置
        for (; r < size; r++)
            if (c.contains(elementData[r]) == complement)
                elementData[w++] = elementData[r];
    } finally {
        // 如果contains()抛出异常,则把未读的元素都拷贝到写index之后
        if (r != size) {
            System.arraycopy(elementData, r,
                                elementData, w,
                                size - r);
            w += size - r;
        }
        if (w != size) {
            // 将写index之后的元素置为null，有利于GC
            for (int i = w; i < size; i++)
                elementData[i] = null;
            // 修改modCount
            modCount += size - w;
            // 新大小等于写指针的位置（因为每写一次写指针就加1，所以新大小正好等于写指针的位置）
            size = w;
            modified = true;
        }
    }
    return modified;
}
```

#### 获取元素

get(int index)方法很简单，获取指定索引位置的元素，时间复杂度为O(1)。

```java
public E get(int index) {
    // index越界检查
    rangeCheck(index);

    return elementData(index);
}
```

#### 设置元素

set(int index, E element)方法可以将指定index的元素设置为指定element，并返回旧元素，时间复杂度O(1)。

```java
public E set(int index, E element) {
    // inde越界检查
    rangeCheck(index);

    // 获取旧元素
    E oldValue = elementData(index);
    elementData[index] = element;
    return oldValue;
}
```

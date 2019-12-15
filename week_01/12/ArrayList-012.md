#### 问题
elementData为什么加transient?
自动扩容是如何进行的?
modCount作用是什么?

#### 简介
线性表之一，基于数组，支持动态扩容
#### 继承体系
![image](https://raw.githubusercontent.com/sljie1988/image/master/jdkSourceStudy/ArrayListInheritSystem.png?token=AKPZOCKK54HH5325AXJNB2K56XVGI)
#### 源码解析

##### 属性
```
// 默认容量
private static final int DEFAULT_CAPACITY = 10;
// 空数组，如果传入的容量为0时使用
private static final Object[] EMPTY_ELEMENTDATA = {};
// 空数组，传传入容量时使用，添加第一个元素的时候会重新初始为默认容量大小
private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
// 存储元素的数组
transient Object[] elementData;
// 集合中元素的个数
private int size;
```
##### 构造方法
```
public ArrayList(int initialCapacity);
// 初始化为DEFAULT空数组,添加第一个元素时扩容为默认大小,10
public ArrayList();
// 使用拷贝把传入集合的元素拷贝到elementData数组中
public ArrayList(Collection<? extends E> c);
```
##### 主要方法
###### boolean add(E e)
```
public boolean add(E e) {
    ensureCapacityInternal(size + 1);  // Increments modCount!!
    elementData[size++] = e;
    return true;
}

// 增加第一个元素时设定默认容量10
private void ensureCapacityInternal(int minCapacity) {
    if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
        minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
    }
    ensureExplicitCapacity(minCapacity);
}

// 容量不够时扩容
private void ensureExplicitCapacity(int minCapacity) {
    modCount++;
    if (minCapacity - elementData.length > 0)
        grow(minCapacity);
}

private void grow(int minCapacity) {
    int oldCapacity = elementData.length;
    // 1.5倍扩容
    int newCapacity = oldCapacity + (oldCapacity >> 1);
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;
    // 最大容量,2的31次方-1
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
    elementData = Arrays.copyOf(elementData, newCapacity);
}
```
###### void add(int index, E element)
```
public void add(int index, E element) {
    // 角标越界检查
    rangeCheckForAdd(index);
    ensureCapacityInternal(size + 1);
    // index+1处复制起始位置为index,长度为size-index的数据
    System.arraycopy(elementData, index, elementData, index + 1,
            size - index);
    elementData[index] = element;
    size++;
}
```
###### E remove(int index)
```
public E remove(int index) {
    rangeCheck(index);

    modCount++;
    E oldValue = elementData(index);

    int numMoved = size - index - 1;
    if (numMoved > 0)
        // index处复制起始位置为index+1,长度为size - index - 1的数据
        System.arraycopy(elementData, index+1, elementData, index,
                numMoved);
    // 尾元素置空,size-1
    elementData[--size] = null; // clear to let GC do its work

    return oldValue;
}
```
###### boolean remove(Object o)
```
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

private void fastRemove(int index) {
    // 无需检查越界
    modCount++;
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index,
                numMoved);
    elementData[--size] = null; // clear to let GC do its work
}
```
###### voic clear()
```
public void clear() {
    modCount++;

    // clear to let GC do its work
    for (int i = 0; i < size; i++)
        elementData[i] = null;

    size = 0;
}
```
###### E set(int index, E element)
```
public E set(int index, E element) {
    rangeCheck(index);

    E oldValue = elementData(index);
    elementData[index] = element;
    return oldValue;
}
```
###### boolean addAll(Collection<? extends E> c)
尾部添加集合
###### boolean addAll(int index, Collection<? extends E> c)
###### void removeRange(int fromIndex, int toIndex)
###### boolean removeAll(Collection<?> c)
```
// 移除集合中包含参数集合中的数据
public boolean removeAll(Collection<?> c) {
    Objects.requireNonNull(c);
    return batchRemove(c, false);
}
```
###### boolean retainAll(Collection<?> c)
```
// 保留集合中包含参数集合中的数据
public boolean retainAll(Collection<?> c) {
    Objects.requireNonNull(c);
    return batchRemove(c, true);
}
```
```
private boolean batchRemove(Collection<?> c, boolean complement) {
    final Object[] elementData = this.elementData;
    int r = 0, w = 0;
    boolean modified = false;
    try {
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
            for (int i = w; i < size; i++)
                elementData[i] = null;
            modCount += size - w;
            size = w;
            modified = true;
        }
    }
    return modified;
}
```
###### void writeObject(java.io.ObjectOutputStream s)
###### void readObject(java.io.ObjectInputStream s)
###### ListIterator<E> listIterator(int index)
###### List<E> subList(int fromIndex, int toIndex)
###### void sort(Comparator<? super E> c)

#### 总结
默认容量为10，以1.5倍容量扩容
Collection.toArray()转换后不一定全是Object[]

#### 延伸
bug网址：
https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6260652
# ArrayList 源码分析

## TOP 带着问题看源码

1. List list = new ArrayList(20) 扩容了几次
2. ArrayList 怎么实现数组动态扩容，扩容时机，扩容倍数
3. ArrayList 怎么实现remove的
4. 为什么remove具体元素性能差
5. ArrayList 是怎么序列化的

## 1. 继承和实现关系

<img src="http://qiniu.itliusir.com/arraylist01.png" style="zoom:50%;" />



- *RandomAccess 接口*

  标记该类具有快速随机访问能力。当一个集合拥有该能力时候，采用for循环遍历会很快；若没有则采用Iterator迭代器最快。参考ArrayList的indexOf(Object o)方法和AbstractList的indexOf(Object o)方法区别。

- *Serializable 接口*

  标记该类是可序列化的。

- *Cloneable 接口*

  标记该类对象能够被Object.clone()

  根据重写的clone方法实现主要分为如下两种克隆方式

  1. 浅克隆

     只copy对象本身和对象中的基本变量，不copy包含引用的对象

  2. 深克隆

     不仅copy对象本身，还copy对象包含的引用对象

- *AbstractList 抽象类*

  ​	提供一些基础方法: IndexOf、clear、addAll、iterator等

## 2. 成员变量分析

```java
// 默认容量
private static final int DEFAULT_CAPACITY = 10;
// 空数组实例(为0时候)
private static final Object[] EMPTY_ELEMENTDATA = {};
// 默认大小时候的空数组实例
private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
// 存储数组
transient Object[] elementData;
// 数组大小
private int size;
// 数组最大容量，减8是因为可能一些VM会在数组保留一些header，防止OOM
private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
```

## 3. 构造方法分析

### 3.1 无参构造方法

默认赋值一个空数组实例

```java
public ArrayList() {
    this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
}
```

### 3.2 带初始化容量的构造方法

可以看到是由参数的大小来创建对应大小的 elementData 数组，回到 **TOP 1** 问题，可以看出来不会发生扩容，也就是0次

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

### 3.3 带集合内容的构造方法

把传过来的集合转化为数组赋值给 elementData 数组

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

## 4. 核心方法分析

### 4.1 获取元素

先 check ，再按照 index 取。check也是为了保证工程中不会出现奇奇怪怪的结果

```java
public E get(int index) {
    rangeCheck(index);

    return elementData(index);
}
```

使用 final 修饰的数组来接收存储数组，对其遍历。 modCount 变量和 final 修饰的 expectedModCount 进行对比来判断是否存在并发读写情况

```java
public void forEach(Consumer<? super E> action) {
    Objects.requireNonNull(action);
    final int expectedModCount = modCount;
    @SuppressWarnings("unchecked")
    final E[] elementData = (E[]) this.elementData;
    final int size = this.size;
    for (int i=0; modCount == expectedModCount && i < size; i++) {
        action.accept(elementData[i]);
    }
    if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
    }
}
```

### 4.2 新增元素

#### 4.2.1 add(E e)

把一个元素新增到elementData，主要涉及如下几点

1. modCount++ 声明我新增元素了，在并发情况下起到容量是否发生变化作用
2. 如果容量不足，则扩容数组大小(参考下面grow方法)

```java
public boolean add(E e) {
    ensureCapacityInternal(size + 1);  // Increments modCount!!
    elementData[size++] = e;
    return true;
}
```

####4.2.2 add(int index, E element)

按照index位置来插入元素，和上面方法同理。

```java
public void add(int index, E element) {
    rangeCheckForAdd(index);

    ensureCapacityInternal(size + 1);  // Increments modCount!!
    System.arraycopy(elementData, index, elementData, index + 1,
                     size - index);
    elementData[index] = element;
    size++;
}
```

####4.2.3 grow(int minCapacity)

第4行可以看到，使用位运算扩容了 1.5 倍大小空间，至于为啥是1.5倍，我猜是经验值。

回到 **TOP 2** 问题，可以明白了扩容机制是通过数组 copy方式，时机就是容量不够的时候，倍数是1.5倍

```java
private void grow(int minCapacity) {
    // overflow-conscious code
    int oldCapacity = elementData.length;
    int newCapacity = oldCapacity + (oldCapacity >> 1);
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
    // minCapacity is usually close to size, so this is a win:
    elementData = Arrays.copyOf(elementData, newCapacity);
}
```

### 4.3 更新元素

直接数组下标覆盖，返回旧值，至于为什么返回的是旧值，可能一方面是根据下标查询不是很影响性能索性给查询出来，另一方面下标和新值请求者都清楚也没必要返回。

```java
public E set(int index, E element) {
    rangeCheck(index);

    E oldValue = elementData(index);
    elementData[index] = element;
    return oldValue;
}
```

### 4.4 删除元素

#### 4.4.1 remove(int index)

计算要删除的下标后一位到数组末尾的长度，然后通过copy这段长度覆盖到原数组的位置，最后把最后一位置null,实现删除。

回到 **TOP 3** 问题，可以明白删除机制也是通过数组copy覆盖的思想来实现的

```java
public E remove(int index) {
    rangeCheck(index);

    modCount++;
    E oldValue = elementData(index);
		// 计算长度
    int numMoved = size - index - 1;
    if (numMoved > 0)
      	// param1: 源数组
      	// param2: 源数组要复制的起始位置
      	// param3: 目标数组
      	// param4: 目标数组放置的起始位置
      	// param5: 复制的长度
        System.arraycopy(elementData, index+1, elementData, index,
                         numMoved);
    elementData[--size] = null; // clear to let GC do its work

    return oldValue;
}
```

####4.4.2 remove(Object o)

首先分为两个场景，第一个是要删除的元素是null，第二个是要删除的是非null的。

主要是遍历要找的元素，找到该元素对应的index，然后使用 fastRemove(index) 去快速删除

回到 **TOP 4** 问题，可以明白计算某个元素下标的时间复杂度是 O(n) 的，所以性能没有直接根据下标删除好

```java
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
```

#### 4.4.3 fastRemove(int index)

因为调用该方法都是内部计算index后调用的，所以不需要再校验index是否越界，也不需要返回oldValue。

```java
private void fastRemove(int index) {
    modCount++;
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index,
                         numMoved);
    elementData[--size] = null; // clear to let GC do its work
}
```

#### 4.4.4 clear()

遍历赋值null，size重置为0

## 5. 序列化

首先我们在最开始就有介绍 ArrayList 类实现的有 Serializable 接口，但是我们在成员变量那一节看到的存储数组 elementData 是有 `transient` 修饰的，也就是elementData不会参与默认序列化，那实现这个 Serializable 接口还有意义么？

其实仔细观察类里的方法你会发现有两个与序列化的流有关系的方法：`writeObject` 、`readObject` 

在序列化过程中如果有这两个方法，会默认调用这两个方法进行用户自定义的序列化和反序列化，如果没有才走默认序列化。

那么我们知道作者的序列化是自定义了，那为什么这样做呢，为什么不直接使用默认序列化呢？

我们可以想下，每次扩容1.5倍，那这个数组实际会有一些空间扩容后还未被填充，如果使用默认序列化则会将null也给序列化进去。

接下来我们来看一下自定义序列化方法具体的实现：

###5.1  writeObject

写入数组大小，遍历写入数组元素，检查并发冲突

```java
private void writeObject(java.io.ObjectOutputStream s)
    throws java.io.IOException{
    // Write out element count, and any hidden stuff
    int expectedModCount = modCount;
    s.defaultWriteObject();

    // Write out size as capacity for behavioural compatibility with clone()
    s.writeInt(size);

    // Write out all elements in the proper order.
    for (int i=0; i<size; i++) {
        s.writeObject(elementData[i]);
    }

    if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
    }
}
```

### 5.2  readObject

初始化存储数组elementData，读取写入的数组大小，构造数组并写入元素

```java
private void readObject(java.io.ObjectInputStream s)
    throws java.io.IOException, ClassNotFoundException {
    elementData = EMPTY_ELEMENTDATA;

    // Read in size, and any hidden stuff
    s.defaultReadObject();

    // Read in capacity
    s.readInt(); // ignored

    if (size > 0) {
        // be like clone(), allocate array based upon size not capacity
        int capacity = calculateCapacity(elementData, size);
        SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, capacity);
        ensureCapacityInternal(size);

        Object[] a = elementData;
        // Read in all elements in the proper order.
        for (int i=0; i<size; i++) {
            a[i] = s.readObject();
        }
    }
}
```

回到 **TOP 5** 问题，可以明白 ArrayList 序列化采用的是自定义序列化方式

### 5.3 自定义序列化的原理

通过跟踪ObjectOutputStream的writeObject()方法，调用链路如下所示：

`writeObject -> writeObject0 -> writeOrdinaryObject -> writeSerialData` 

代码如下所示，可以看到会先判断是否有 writeObject 方法，如果有的话，会通过反射的方式调用序列化对象的writeObject方法，如果没有则使用默认序列化方式

```java
private void writeSerialData(Object obj, ObjectStreamClass desc)
    throws IOException
{
    ObjectStreamClass.ClassDataSlot[] slots = desc.getClassDataLayout();
    for (int i = 0; i < slots.length; i++) {
        ObjectStreamClass slotDesc = slots[i].desc;
        if (slotDesc.hasWriteObjectMethod()) {
            PutFieldImpl oldPut = curPut;
            curPut = null;
            SerialCallbackContext oldContext = curContext;

            if (extendedDebugInfo) {
                debugInfoStack.push(
                    "custom writeObject data (class \"" +
                    slotDesc.getName() + "\")");
            }
            try {
                curContext = new SerialCallbackContext(obj, slotDesc);
                bout.setBlockDataMode(true);
                slotDesc.invokeWriteObject(obj, this);
                bout.setBlockDataMode(false);
                bout.writeByte(TC_ENDBLOCKDATA);
            } finally {
                curContext.setUsed();
                curContext = oldContext;
                if (extendedDebugInfo) {
                    debugInfoStack.pop();
                }
            }

            curPut = oldPut;
        } else {
            defaultWriteFields(obj, slotDesc);
        }
    }
}
```
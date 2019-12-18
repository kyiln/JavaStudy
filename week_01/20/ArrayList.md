+ 定义
```java
public class ArrayList<E> extends AbstractList<E>             
    implements List<E>, RandomAccess, Cloneable, java.io.Serializable 
```
+ 实际上是一个动态数组，容量可以动态的增长，其继承了AbstractList
```java
//如果是无参构造方法创建对象的话，ArrayList的初始化长度为10，这是一个静态常量
private static final int DEFAULT_CAPACITY = 10;
​
//MPTY_ELEMENTDATA实际上是一个空的对象数组
    private static final Object[] EMPTY_ELEMENTDATA = {};
​
//保存ArrayList数据的对象数组缓冲区 elementData的初始容量为10，大小会根据ArrayList容量的增长而动态的增长。
    private transient Object[] elementData;
//集合的长度
    private int size;
``` 
+ add方法
```java
/**
    * Appends the specified element to the end of this list.
    */
//增加元素到集合的最后
public boolean add(E e) {
ensureCapacityInternal(size + 1);  // Increments modCount!!
//因为++运算符的特点 先使用后运算  这里实际上是
//elementData[size] = e
//size+1
elementData[size++] = e;
    return true;
}
``` 
+ 扩容
+ （1）检查是否需要扩容；
+ （2）如果elementData等于DEFAULTCAPACITY_EMPTY_ELEMENTDATA则初始化容量大小为DEFAULT_CAPACITY；
+ （3）新容量是老容量的1.5倍（oldCapacity + (oldCapacity >> 1)），如果加了这么多容量发现比需要的容量还小，则以需要的容量为准；
+ （4）创建新容量的数组并把老数组拷贝到新数组；
```java
private void grow(int minCapacity) {
    // overflow-conscious code
    int oldCapacity = elementData.length;
    //新的容量是在原有的容量基础上+50% 右移一位就是二分之一
    int newCapacity = oldCapacity + (oldCapacity >> 1);
    //如果新容量小于最小容量，按照最小容量进行扩容
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
    // minCapacity is usually close to size, so this is a win:
    //这里是重点 调用工具类Arrays的copyOf扩容
    elementData = Arrays.copyOf(elementData, newCapacity);
}
```
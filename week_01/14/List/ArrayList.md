# ArrayList

## 一、简介

##### 说明：

​	1、是一种线性数据结构，它的底层是用数组实现的，相当于动态数组。与Java中的数组相比，它的容量能动态增长。

##### 优点：

​	1、根据下标遍历元素效率较高。

​	2、根据下标访问元素效率较高。

​	3、在数组的基础上封装了对元素操作的方法。

​	4、可以自动扩容。

##### 缺点：

​	1、插入和删除的效率比较低。

​	2、根据内容查找元素的效率较低。

##### 扩容规则：

​	每次扩容现有容量的50%。

##### 遍历效率(快-慢)：

​	for循环 > Iterator迭代器 

##### 是否线程安全：

​	否

## 二、继承关系图

 ![在这里插入图片描述](https://img-blog.csdnimg.cn/20191211104815479.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM5OTM4NzU4,size_16,color_FFFFFF,t_70)

ArrayList实现了Cloneable，可以被克隆，是浅拷贝。

ArrayList实现了Serializable，可以被序列化。

ArrayList实现了RandomAccess，提供了随机访问功能，

​	1、实现这个接口的List，for循环比迭代速度快

​	2、为了能够更好地判断集合是否ArrayList或者LinkedList，从而能够更好选择更优的遍历方式，提高性能！

​	3、用instanceof来判断List集合子类是否实现RandomAccess接口，实现了RandomAccess接口for循环遍历速度比迭代快，LinkedList没有实现RandomAccess接口

ArrayList继承自AbstractList，实现了List接口，具有List的所有功能。	

## 三、存储结构

1、11

```Java
    ArrayList al=new ArrayList();
    al.add(new ArrayList(11,"nihao"));
    al.add(new ArrayList(12,"tianchao"));
```
2、 
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191211104836846.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM5OTM4NzU4,size_16,color_FFFFFF,t_70)
## 四、源码分析

#### 内部类

#### 属性

```Java
private static final long serialVersionUID = 8683452581122892189L;
//默认初始化容量
private static final int DEFAULT_CAPACITY = 10;
//空数组，使用ArrayList(0)创建时使用这个空数组.
private static final Object[] EMPTY_ELEMENTDATA = {};
//空数组，使用ArrayList()创建时使用这个空数组，添加第一个元素的时候会重新初始化为默认容量大小10
private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
//transient关键字标记的成员变量不参与序列化过程
//存储元素的数组
transient Object[] elementData;
//集合中元素的个数,而不是数组的长度
private int size;
```

#### 构造

```java
//构造具有指定初始容量的空列表
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

//构造一个初始容量为10的空列表。
public ArrayList() {
  this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
}

//构造一个包含指定*集合元素的列表，按集合的*迭代器返回元素的顺序
public ArrayList(Collection<? extends E> c) {
  elementData = c.toArray();
  if ((size = elementData.length) != 0) {
    //c.toArray 也许不能正确返回Object[]，所以再确定
    // c.toArray might (incorrectly) not return Object[] (see 6260652)
    if (elementData.getClass() != Object[].class)
      elementData = Arrays.copyOf(elementData, size, Object[].class);
  } else {
    // 用空数组代替 c 进行赋值
    this.elementData = EMPTY_ELEMENTDATA;
  }
}
```



#### 主要方法

##### 1、add方法

```java
//将指定的元素追加到此列表的末尾。
public boolean add(E e) {
  //修改操作次数，并且是否需要扩容，如果要就进行扩容
  ensureCapacityInternal(size + 1);  // Increments modCount!!
  //赋值
  elementData[size++] = e;
  return true;
}

private void ensureCapacityInternal(int minCapacity) {
  ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
}

//修改次数+1，并且验证是否超出当前的数组长度，如果是grww数组
private void ensureExplicitCapacity(int minCapacity) {
  modCount++;

  // overflow-conscious code
  if (minCapacity - elementData.length > 0)
    grow(minCapacity);//扩容
}

/* 
* 返回新的数组最小需要容量数 
*/
private static int calculateCapacity(Object[] elementData, int minCapacity) {
  if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
    return Math.max(DEFAULT_CAPACITY, minCapacity);
  }
  return minCapacity;
}

/**
*  增加容量，以确保它至少可以容纳由最小容量参数指定的*数量的元素
*/ 
private void grow(int minCapacity) {
  // overflow-conscious code
  int oldCapacity = elementData.length;
  // 新容量 = 当前数组长度+(int)当前数组长度/2
  int newCapacity = oldCapacity + (oldCapacity >> 1);
  // 新容量 < 最小需要容量数，则 新容量 = 最小需要容量数
  if (newCapacity - minCapacity < 0)
    newCapacity = minCapacity;
  // 新容量 - 最大数组容量(int最大值-8) > 0 ， 则
  if (newCapacity - MAX_ARRAY_SIZE > 0)
    newCapacity = hugeCapacity(minCapacity);
  // minCapacity is usually close to size, so this is a win:
  elementData = Arrays.copyOf(elementData, newCapacity);
}
//返回最大容量（最小容量 > 数组最大容量 ，则返回Integer.MAX_VALUE((2<<31)-1)）,否则返回数组最大容量
//就算创建一个List<Object> 空的满的数组，也需约24G，
/* java中空对象占八个字节，对象的引用占四个字节。所以上面那条语句所占的空间是4byte+8byte=12byte.java中的内存是以8的倍数来分配的，所以分配的内存是16byte.
Class O{
	int i;
	byte j;
	String s;
}
其所占内存的大小是空对象（8）+int(4)+byte(1)+String引用(4)=17byte,因要是8的整数倍，所以其占大小为24byte
当然，如果类里有其他对象的话，也要把其他对象的空间算进去。

*/

/**	
 *  返回MAX_ARRAY_SIZE或Integer.MAX_VALUE
 */
private static int hugeCapacity(int minCapacity) {
  if (minCapacity < 0) // overflow
    throw new OutOfMemoryError();
  return (minCapacity > MAX_ARRAY_SIZE) ?
    Integer.MAX_VALUE :
  MAX_ARRAY_SIZE;
}
```

##### 2、remove(int index)方法

```java
public E remove(int index) {
  rangeCheck(index);//检查下标是否超出数组下标
  modCount++;
  E oldValue = elementData(index);//获得元素

  int numMoved = size - index - 1;//要拷贝的数据长度
  if (numMoved > 0)
    //拷贝数据
    System.arraycopy(elementData, index+1, elementData, index,
                     numMoved);
  //清空最后一个无用数据，让Gc处理
  elementData[--size] = null; // clear to let GC do its work

  return oldValue;
}
```

## 五、总结

```
工业中常用，插入有序、修改和读时间复杂度 1   添加和删除时间复杂度n
```
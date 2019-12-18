##关于ArrayList小笔记


###1、认识ArrayList
	  
1）ArrayList就是动态数组

 ```java
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
 ```
 
2）实现了List，提供了基础的添加、删除等操作；

3）实现Cloneable；

4）实现java.io.Serializable，可被序列化；
 
5）实现RandomAccess，可随机访问。

###2、属性概览

```java
/**
 * 默认初始化大小.
 */
 private static final int DEFAULT_CAPACITY = 10;     
 /**
  * 空数组 当创建实例数量为空时使用.
  */
 private static final Object[] EMPTY_ELEMENTDATA = {};
 /**
  * 存储元素的数组.
  */
  transient Object[] elementData;// non-private to simplify nested class access
 /**
  * 数组大小.
  */
  private int size;
        
``` 

###3、构造器
```java
/**
 * 创建指定大小数组
 * @param  数组大小
 */
public ArrayList(int initialCapacity) {
    super();
    // 如果传入大小小于0，抛出异常
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal Capacity: "+
                                           initialCapacity);
    // 创建指定容量大小新数组
    this.elementData = new Object[initialCapacity];
}
    
/**
 * 默认构造器为空数组，使用时按默认大小 
 */
 public ArrayList() {
    super();
    this.elementData = EMPTY_ELEMENTDATA;
}
    
/**
 * 将传入的集合转成数组
 */
 public ArrayList(Collection<? extends E> c) {
    elementData = c.toArray();
    size = elementData.length;
    // c.toArray might (incorrectly) not return Object[] (see 6260652)
    //see 6260652为bug编号 https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6260652
    if (elementData.getClass() != Object[].class)
        elementData = Arrays.copyOf(elementData, size, Object[].class);
}
	
```
ps：针对最后一类创建数组方式为什么返回不一定是一个对象，测试如下：

```java
//第一种方式
List<String> test1 = new ArrayList<String>( );
test1.add("123");
System.out.println(test1.toArray());
//返回结果：[Ljava.lang.Object;@a74868d
	
//第二种方式
List<String> test2 = Arrays.asList("123");
System.out.println(test2.toArray());
//返回结果：[Ljava.lang.String;@12c8a2c0
```
	
### 4、add
1）add(E e)

```java
/**
 * 添加数组，从末尾开始添加
 */
public boolean add(E e) {
   //检查是否需要扩容
    ensureCapacityInternal(size + 1);  // Increments modCount!!
    //将元素添加到数组最后一位
    elementData[size++] = e;
    return true;
}

private void ensureCapacityInternal(int minCapacity) {
	//如果是空数组，则初始化默认大小10
    if (elementData == EMPTY_ELEMENTDATA) {
        minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
    }
    ensureExplicitCapacity(minCapacity);
}

private void ensureExplicitCapacity(int minCapacity) {
	//用于统计list被修改的次数
    modCount++;	
    // overflow-conscious code
    if (minCapacity - elementData.length > 0)
    //扩容
    grow(minCapacity);
}

private void grow(int minCapacity) {
    // overflow-conscious code
    int oldCapacity = elementData.length;
    //扩容1.5倍
    int newCapacity = oldCapacity + (oldCapacity >> 1);
    //如果新容量小于需要最小容量，则以需要容量为准
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;
    //如果新容量大于最大容量，则以最大容量为准
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
    // minCapacity is usually close to size, so this is a win:
    //以新容量拷贝一个新数组出来
    elementData = Arrays.copyOf(elementData, newCapacity);
}
```
2）add(int index, E element)

``` java
/**
 * 添加元素到指定位置
 */
public void add(int index, E element) {
	//检查是否越界
    rangeCheckForAdd(index);
    //检查是否需要扩容
    ensureCapacityInternal(size + 1);  // Increments modCount!!
    //将数组index之后的元素往后移动一位
    System.arraycopy(elementData, index, elementData, index + 1,
                     size - index);
    //再将index设置为需要添加的元素
    elementData[index] = element;
    //数组长度+1
    size++;
}
```

###5、get

``` java
/**
 * 获得指定位置该元素
 */
public E get(int index) {
	///检查是否越界
    rangeCheck(index);
    //返回元素
    return elementData(index);
}


private void rangeCheck(int index) {
  if (index < 0 || index >= this.size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}

private void checkForComodification() {
	//保证操作为同一list（考虑并发情况下）
	if (ArrayList.this.modCount != this.modCount)
     throw new ConcurrentModificationException();
}
        
``` 

###6、remove

``` java	
/**
 * 删除指定位置元素
 */
public E remove(int index) {
	//检查是否越界
    rangeCheck(index);
	
    modCount++;
    //获取index位置元素
    E oldValue = elementData(index);
	 //如果index不是最后一位，则把index之后的元素往前挪一位
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index,
                         numMoved);
    //// 将最后一个元素删除，帮助GC
    elementData[--size] = null; // clear to let GC do its work
    //返回删除旧值
    return oldValue;
}

``` 

[TOC]

# 1.ArrayList底层原理

## 1.1 ArrayList的UML

![01-ArrayList关系类图](01-ArrayList关系类图.png)

```java
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable{...｝
```

- 继承了AbstractList；
- 实现了List接口；
- 实现了RandomAccess接口，支持快速随机访问，通过下标序号进行快速访问；
- 实现了Cloneable接口，支持克隆；
- 实现了Serializable接口，支持序列化；

## 1.2 ArrayList的数据结构

```java
/**
 * Resizable-array implementation of the <tt>List</tt> interface.  Implements
 * all optional list operations, and permits all elements, including
 * <tt>null</tt>. 
```

​		如上，通过ArrayList的源码文档可知，ArrayList是可动态调整容量的数组(`Resizable-array`)，ArrayList实现了List的所有操作并允许包括null在内的所有元素（ArrayList中存放的是Object[]数组，意味着ArrayList可以存放任何继承自Object的对象）。

## 1.3 ArrayList中定义的常量、变量以及构造函数

```java
	//序列版本号
	private static final long serialVersionUID = 8683452581122892189L;

    /**
     * 默认初始化容量
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * 用于空实例的共享空数组实例。
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * JDK 1.8中添加的，也是一个空数组，与EMPTY_ELEMENTDATA区分开来
     * （当调用无参构造方法时默认复制这个空数组）
     */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    /**
     * 保存添加到数组中的元素；
     * elementData是一个动态数组；
     * 通过public ArrayList(int initialCapacity){}构造函数时elementData=initialCapacity;
     * 通过public ArrayList(){}构造函数时elementData=DEFAULT_CAPACITY=10;
     */
    transient Object[] elementData; // 非私有以简化嵌套类访问

    /**
     * 数组中元素的大小
     *
     * @serial
     */
    private int size;

 	/**
     * 指定初始化容量大小的构造函数
     *
     * @param  initialCapacity  数组的初始化容量大小
     * @throws IllegalArgumentException 输入参数为负数时抛出异常
     */
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

    /**
     * 无参构造函数构造，初始化容量为10
     */
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    /**
     * 构造一个包含指定集合的元素的列表，其顺序由集合的迭代器返回
     *
     * @param c 将其元素放入此列表的集合
     * @throws NullPointerException 传入集合参数为null时抛出异常
     */
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

### 1.3.1 ArrayList的默认容量是多少？

```java
 	private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

	public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }
```

​		通过ArrayList的无参构造函数可以看出，初始化时ArrayList的默认容量是一个空数组，相当于默认容量为0；

注：JDK1.8之前默认容量是10.

### 1.3.2 elementData为什么要用transient修饰？

​		首先说一下transient关键字：Java的serialization提供了一种持久化对象的机制，当持久化对象时，可能有一个特殊的对象数据成员，不想用serialization机制来保存，为了在一个特定对象的一个域上关闭serialization，可以在这个域前加上关键字transient。当一个对象被序列化时，transient型变量的值不包括在序列化的表示中，非transient型的变量是被包括进去的。（HashMap、ArrayList中都使用了transient关键字）

- transient修饰的变量不能被序列化；

- transient只作用于实现 Serializable 接口；

- transient只能用来修饰普通成员变量字段；

- 不管有没有 transient 修饰，静态变量都不能被序列化；

​	ArrayList是可序列化的类，elementdata是ArrayList用来存储元素的成员，用transient关键字修饰elementdata，真的就意味着elementdata不能序列化了吗？而且这样反序列化后的ArrayList也会把原来存储的元素弄丢？

   如果继续看ArrayList的源码，会发现ArrayList会调用自己实现的writeObject()和readObject()方法进行序列化和反序列化，之所以这样，是因为elementdata是一个缓存数组，通常会预留一些容量，等到容量不足时再扩容，这些预留的容量空间里没有实际存储元素。所以采用transient关键字保证elementdata不会被serialization提供的持久化机制保存（序列化），再加上ArrayList自己实现的序列化和反序列方法，这样就可以保证ArrayList序列化时只会序列化实际存储的那些元素，而不包含预留容量中空的存储空间，从而节省序列化反序列化的时间和空间。

参考：[ArrayList中elementData为什么被transient修饰？](https://blog.csdn.net/zero__007/article/details/52166306)

### 1.3.3 DEFAULTCAPACITY_EMPTY_ELEMENTDATA和EMPTY_ELEMENTDATA的区别？

//TODO

### 1.3.4 ArrayList三种构造函数

1.无参构造函数：无参构造函数时，创建一个容量为0的数组；

```java
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }
```

```java
        ArrayList arrayList = new ArrayList();
```

2.指定容量构造函数

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

```
        ArrayList arrayList = new ArrayList(20);
```

- `initialCapacity > 0`时，ArrayList容量为给定的`initialCapacity`；
- `initialCapacity == 0` 时，ArrayList容量为`EMPTY_ELEMENTDATA = 0`；
- `initialCapacity < 0` 时，抛出异常：`"Illegal Capacity: "+ initialCapacity`；

3.构造一个包含指定集合的元素的列表，按集合的迭代器返回元素的顺序排列。

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

```
        ArrayList arrayList = new ArrayList(Arrays.asList(new String[]{"1","2","3"}));
```

1. 将传入集合`c`拷贝给`elementData`。

```java
        elementData = c.toArray();
        			|
        			↓
        根据toArray()进入Arrays查看该方法源码
        @Override
        public Object[] toArray() {
            return a.clone();
        }
```

​	  2. 对得到 `c` 拷贝后的`elementData`进行判断：

​			2.1传入集合为空时：指定ArrayList容量为`EMPTY_ELEMENTDATA = 0`；

​			2.2传入集合不为空时且类型与Object[]不相同时，进行一次`Arrays.copyOf()`，将源数组中的元素类型向上转型后将复制的新数组返回给elementData；


























































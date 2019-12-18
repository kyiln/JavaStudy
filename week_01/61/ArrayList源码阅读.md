# <Center>ArrayList源码分析</Center>

- 以下分析基于JDK1.8

## ArrayList 简介

 ArrayList是可以动态扩容和动态删除冗余容量的索引序列，基于数组实现的集合，是常用的Java集合之一。 

```
 ArrayLis继承自抽象类AbstractList实现了List接口等
```

和Vector不同，ArrayList中的操作不是线程安全的！所以，建议在单线程中才使用ArrayList，而在多线程中可以选择Vector或者CopyOnWriteArrayList，或者使用Collections工具类的synchronizedList方法将其包装 

## ArrayList分析

1、属性分析

- ```java
  // 序列号ID,ArrayList实现了Serializable接口因此是可在网络中传输
  private static final long serialVersionUID = 8683452581122892189L;
  ```

- ```java
  // //默认初始容量
  private static final int DEFAULT_CAPACITY = 10;
  ```

- ```java
  // 一个空对，当用户指定ArrayList容量为0时，返回该数组
  private static final Object[] EMPTY_ELEMENTDATA = {};
  ```

- ```java
  // 一个空对象，如果使用默认构造函数创建，则默认对象内容默认是该值
   private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
  ```

- ```java
  // 动态数组的实际大小 ，默认为0
   private int size;
  ```

- ```java
  // 最大数组容量Integer -8 
  private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
  ```
  


2、构造方法分析

```java
// 传入初始容量 initialCapacity
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

   //无参构造  DEFAULTCAPACITY_EMPTY_ELEMENTDATA=={} 注意此时初始容量是0，而不是的 10
//当元素第一次被加入时，扩容至默认容量 10
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

   // 创建一个包含collection或其子类的ArrayList 要放入 ArrayList 中的集合，其内元素将会全部添加到新建的 ArrayList 实例中
    public ArrayList(Collection<? extends E> c) {
      //  集合传化成Object[]数组
        elementData = c.toArray();
        // 转化后的数组长度赋给当前ArrayList的size,并判断是否为0
        if ((size = elementData.length) != 0) {
            // c.toArray might (incorrectly) not return Object[] (see 6260652)
            if (elementData.getClass() != Object[].class)
                // 若 c.toArray() 返回的数组类型不是 Object[]，则利用 Arrays.copyOf(); 来构造一个大小为 size 的 Object[] 数组
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            // replace with empty array.
             // 替换空数组
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }


```

3、常见方法分析

- add方法

```java
public boolean add(E e) {
    // 赋值初始长度或者扩容,新增元素，当前实际size+1的长度
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }
// 确保elemenData数组有合适的大小- 如果元素为空，则复制长度默认为10 或者更大
  private void ensureCapacityInternal(int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        ensureExplicitCapacity(minCapacity);
    }
// 保证elemenData数组有合适的大小
private void ensureExplicitCapacity(int minCapacity) {
    //记录修改次数，迭代中不一致会触发fail-fast机制,因此在遍历中删除元素的正确做法应该是使用Iterator.remove()
        modCount++;

        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }
// 扩容
  private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
       //拷贝扩容
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
 // 如果小于0 就报错，如果大于最大值 则取最大值
    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }


    // 给指定下标，添加元素
    public void add(int index, E element) {
        rangeCheckForAdd(index);//检查是否越界
   //赋值初始长度  或者扩容
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);
        elementData[index] = element;
        size++;
    }

```

- remove方法

  ```java
  //根据指定下标 删除元素
  public E remove(int index) {
          rangeCheck(index);//检查越界
  
          modCount++;
          E oldValue = elementData(index);
  //将数组elementData中index位置之后的所有元素向前移一位
          int numMoved = size - index - 1;
          if (numMoved > 0)
              System.arraycopy(elementData, index+1, elementData, index,
                               numMoved);
          elementData[--size] = null; // clear to let GC do its work 将原数组最后一个位置置为null，由GC回收
  
          return oldValue;
      }
  
     // 根据指定元素 删除元素   
      public boolean remove(Object o) {
          // ArrayList中允许存放null，因此下面通过两种情况来分别处理。
          if (o == null) {
              for (int index = 0; index < size; index++)
                  if (elementData[index] == null) {
                       // 私有的移除方法，跳过index参数的边界检查以及不返回任何值
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
   
   // 根据下标快速删除元素
  private void fastRemove(int index) {
      modCount++;
      //将数组elementData中index位置之后的所有元素向前移一位
      int numMoved = size - index - 1;
      if (numMoved > 0)
          System.arraycopy(elementData, index+1, elementData, index,
                           numMoved);
      elementData[--size] = null; // clear to let GC do its work
  }
  ```

- set方法

  ```java
  public E set(int index, E element) {
          rangeCheck(index);//越界检查
  
          E oldValue = elementData(index);
          elementData[index] = element; // 指定位置替换
          return oldValue;
      }
  ```

- get方法

  ```java
  public E get(int index) {
      rangeCheck(index);
  
      return elementData(index);
  }
   E elementData(int index) {
          return (E) elementData[index];//取数组指定位置并返回
      }
  ```

## 总结

和hashmap一样 扩容都会增加时耗初始化的时候指定合适的长度。

arrayList由于本质是数组，所以它在数据的查询方面会很快，而在插入删除这些方面，性能下降很多，有移动很多数据才能达到应有的效果，而LinkedList则相反










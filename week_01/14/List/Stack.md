# Stack

## 问题

## 一、简介

优点：Stack是**线程安全**的,底层是数组

缺点：



## 二、继承关系图

 ![在这里插入图片描述](https://img-blog.csdnimg.cn/2019121111330029.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM5OTM4NzU4,size_16,color_FFFFFF,t_70)

是浅拷贝

Stack实现了继承Vector，具有线程安全，除了push方法，其他都加了synchornized

实现了RandomAccess接口，for 循环速度比迭代速度快

## 三、存储结构

## 四、源码分析

#### 内部类

无

#### 属性

无

#### 构造

默认构造

#### 主要方法

```java
//Stack类中唯一二个没有加synchornized，另外一个是isEmpty()。。。。
public E push(E item) {
  addElement(item);//掉的vector的添加方法
  return item;
}
//取元素，调用的peek，取出尾元素，并且删除
public synchronized E pop() {
  E       obj;
  int     len = size();
  obj = peek();
  removeElementAt(len - 1);

  return obj;
}
//取出尾元素，并且删除
public synchronized E peek() {
  int     len = size();

  if (len == 0)
    throw new EmptyStackException();
  return elementAt(len - 1);
}

public boolean empty() {
  return size() == 0;
}
//查询栈中是否有元素0，并且返回索引下标，如果没有返回-1
public synchronized int search(Object o) {
  int i = lastIndexOf(o);

  if (i >= 0) {
    return size() - i;
  }
  return -1;
}
```



## 五、总结
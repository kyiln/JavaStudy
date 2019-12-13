# Vector

## 问题

## 一、简介

优点：线程安全，除了构造方法大部分方法都加锁了的（不加锁的里面也加了锁或者调用的是有锁的防范），加的互斥锁synchornized

缺点：



## 二、继承关系图

![image-20190530165403023](/Users/leili/Desktop/源码阅读/集合/List/image-20190530165403023.png)

是浅拷贝

HashMap实现了Cloneable，可以被克隆，是浅拷贝。

HashMap实现了Serializable，可以被序列化。

HashMap继承自AbstractMap，实现了Map接口，具有Map的所有功能。

## 三、存储结构

## 四、源码分析

#### 内部类

#### 属性

#### 构造

#### 主要方法

## 五、总结
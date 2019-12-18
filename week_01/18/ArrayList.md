# ArrayList

## ArrayList的基础特点
	1. Resizable-array
	2. permits all elements, including null
	3. unsynchronized vs  Vector
	4. adding n elements requires O(n) time
	5. it is always at least as large as the list size.  As elements are added to an ArrayList, its capacity grows automatically.*

- ArrayList 的类关系?
## Capacity
1. ArrayList 的几种构造函数对 ArrayList容量的影响? 传入集合的构造函数的实现?
	1. 默认构造函数
![](ArrayList/BF00ECE6-732E-444A-ACB1-A6A8FBF0E713.png)
	2. 构造函数,带初始容器initialCapacity参数
![](ArrayList/1B8F3C92-DE88-42D8-98DC-C0D48ABB192B.png)
- 通过上面的比较,`DEFAULTCAPACITY_EMPTY_ELEMENTDATA ` 和`EMPTY_ELEMENTDATA` ,**前者知道扩容的容量是10**
![](ArrayList/4A579666-1860-4B86-8921-2749B3F8F839.png)

2. 几种控制ArrayList 容量的方法?
	- 如何缩容?这个方法可以直观的说明 ::size<= capacity:: ,将容量缩减至当前的size 大小
![](ArrayList/18F2F986-FECA-4F67-A7C1-554722D8C9D9.png)
	- 如何扩容?(第一种:目标容量从外部传入,即调用方指定目标容量)
![](ArrayList/A5418EEA-107A-40D1-AD2F-C180951E180F.png)
	- 如何扩容?(第二种:目标容量从外部传入,即调用方指定目标容量)
![](ArrayList/76CEBE20-CAB1-4A53-A9F2-6C20D90A08B0.png)

## modCount
1. modCount的表达的意义(什么是 structurally modified)?用途和如何使用?
	- 意义
![](ArrayList/7B84EAAB-ADA6-47FD-A836-6BC69AA7B3E3.png)
	- 用途(什么是 fail-fast behavior)和如何使用(关键看子类方法是否要提供 fail-fast 功能)
![](ArrayList/3236957C-AEE5-41BF-A7C3-E79542C095F2.png)


2. ArrayList  中哪些方法会导致集合structurally modified(需要modCount++)?
> sort,replaceAll等其他方法同样同样分析  
	-  在增加元素的时候modCount++,因为都要调扩容方法,modCount++写在扩容方法里
![](ArrayList/3F7854B9-884A-49EF-A2D3-6EA5E9F506D4.png)
	- 除了增删改,sort方法为什么也会导致(structurally modified.需要modCount++) ?
		- 为什么校验 modCount 值?
			- sort排序的过程也要迭代元素,比如最后**modCount**发生了改变,你的排序是不准确的,要抛出并发修改异常
		- 这里 sort 用的是**归并排序**
![](ArrayList/E2F335A9-19FF-4CF4-84C7-559019C8BF6D.png)
		- 排序虽然不会增删元素,但是可能会将(2,3,1)—>(1,2,3),集合发生了结构性改变;而且,这时候如果有其它方法校验集合的第一个元素就发现了2变成了1,如果不让 modCount++.其它方法就没办法发现这种改变,造成不可预期的错误
		
	- 注意:对增删来讲没增删一个元素 modCount++,所以批量删除方法这样写的
![](ArrayList/260DD573-E696-4AA4-8F9E-C61A5C4FCB20.png)
	

## batchRemove方法
1. removeAll(Collection<?> c),retainAll(Collection<?> c)
怎么实现的?
![](ArrayList/FE975389-C228-445D-A53F-BD5AF5290B1E.png)
- contains(Object o) 什么时候抛出异常
![](ArrayList/C2139442-9F3B-44BE-A66E-17FCDAF013EE.png)

## ArrayList 的迭代器ListItr,Itr和Iterator
1. ListItr,ListIterator.Itr和Iterator之间的关系?
![](ArrayList/D061D159-5CD3-40A5-AA1E-97E39EADD00D.png)
![](ArrayList/6C380A25-F767-4D36-87DF-FB60DE1B89F5.png)
2. ListItr迭代器拥有的3个属性的含义?
	1. int cursor**;       *// index of next element to return*
	2. int lastRet**= -1; *// index of last element returned; -1 if no such*
	3. int expectedModCount= modCount;  (校验容器是否被并发修改)
3. 迭代器的构造函数的作用?
![](ArrayList/92C4B78A-AFD7-4FA8-8FED-A2016BB83935.png)

4. ListItr迭代器在父类 Itr 迭代器的基础上新增的 previous()方法实现?新增 add,set 方法的特点,以及和 ArrayList中的 add,set 的区别?
> 关键点是怎么维护cursor,lastRet两个字段  
	- add是加在 cursor 指向的索引位置,因为不返回任何元素,**lastRet= -1**;但是还可以接着加元素,所以**cursor+1**
	- set是更新lastRet位置的元素,并返回旧值
![](ArrayList/BCA8000C-ABFC-4361-8B4E-927C709A5989.png)


## SubList
1. 为什么要在 ArrayList 里搞一个内部类数据结构 SubList?
	- 这里面主要是视图思想,同一个数据源可以提供多个不同的视图对象(提供需要的数据,隐藏不需要的数据,安全又方便)
	- 感觉在 JDK8后这些就被流取代了,Stream流更适合做这些, SubList毕竟还是数据结构
![](ArrayList/9AFEEE04-BE12-4CB2-9A8F-7EE1570E1660.png)


## Java8引入函数式接口带来的改变
	1. [[Spliterator]] <-----待补充

1. `transient Object[] elementData;`
的作用?为什么用 transient 修饰?
2. fail-fast behavior of an iterator? fail-fast机制带来的问题?



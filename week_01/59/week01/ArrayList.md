一.ArrayList概述
    1.ArrayList是可以动态增长和缩减的索引序列，它是基于数组实现的List类
    2.ArrayList封装了一个动态分配的Object[]数组（elementData）初始化长度是10，和一个size属性代表当前ArrayList内元素的数量。
    3.ArrayList是线程不安全的，当多条线程访问同一个ArrayList集合时，程序需要手动保证该集合的同步性。
二.ArrayList继承结构
    1.ArrayList先继承AbstractList类，让AbstractList去实现下面几个类，从而减少ArrayList的代码量。
    1.AbstractList实现List,提供一些基本的属性和方法，方便对ArrayList进行元素的操作和判断。
    2.AbstractList实现RandomAccess，可以随机访问List元素。
    3.AbstractList实现Cloneable,可以克隆。
    4.AbstractList实现Serializable，可以被序列化,能够从类变成字节流传输，然后还能从字节流变成原来的类。
三.构造方法
    1.初始化一个长度的构造方法
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
    2.无参构造方法会初始化一个长度为10的list
    public ArrayList() {
            this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }
    3.传入一个集合的构造方法
    public ArrayList(Collection<? extends E> c) {
            elementData = c.toArray();//转化成数组赋值
            if ((size = elementData.length) != 0) {
                // c.toArray might (incorrectly) not return Object[] (see 6260652)
                if (elementData.getClass() != Object[].class)
                //每个集合的toarray()的实现方法不一样，所以需要判断一下，
                //如果不是Object[].class类型，那么久需要使用ArrayList中的方法去改造一下。
                    elementData = Arrays.copyOf(elementData, size, Object[].class);
            } else {
                // replace with empty array.
                this.elementData = EMPTY_ELEMENTDATA;
            }
    }
四.核心方法
    1.4个add方法
    public boolean add(E e) {    //数组内加一个元素，size熟悉加1
            ensureCapacityInternal(size + 1);  // Increments modCount!!
            elementData[size++] = e;
            return true;
    }

    public void add(int index, E element) {//在一个特定的位置加一个元素

            // 检查是否越界
            rangeCheckForAdd(index);
            //检查是否需要扩容
            ensureCapacityInternal(size + 1);  // Increments modCount!!
            //把当前elementData的index位置到结尾长度的值，移到index+1往后的位置。空出index位置
            System.arraycopy(elementData, index, elementData, index + 1,
                             size - index);
            elementData[index] = element;
            size++;
    }
    public boolean addAll(Collection<? extends E> c) {//在当前list末尾加上一个集合
            Object[] a = c.toArray();
            int numNew = a.length;
            ensureCapacityInternal(size + numNew);  // Increments modCount
            System.arraycopy(a, 0, elementData, size, numNew);
            size += numNew;
            return numNew != 0;
    }
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);//检查是否超出范围

        Object[] a = c.toArray();
        int numNew = a.length;
        //检查是否需要扩容
        ensureCapacityInternal(size + numNew);  // Increments modCount

        int numMoved = size - index;
        if (numMoved > 0)
            System.arraycopy(elementData, index, elementData, index + numNew,
                             numMoved);//同理拷贝数组
        //拷贝中间数组
        System.arraycopy(a, 0, elementData, index, numNew);
        size += numNew;
        return numNew != 0;
    }
    2.扩容数组
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        //右移相当于 除以2，扩大1.5倍
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)//当list为空时，满足条件
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0) //超过最大限制时 要给能给的最大值
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);//copy数组，并改变容量
    }
    3.其他核心方法类似
五.总结
    1.arrayList区别于数组的地方在于能够自动扩展大小，其中关键的方法就是gorw()方法，每次扩容可以扩大1.5倍。
    2.arrayList由于本质是数组，所以它在数据的查询方面会很快，而在插入删除这些方面，性能下降很多，有移动很多数据才能达到应有的效果。
    3.arrayList实现了RandomAccess，所以在遍历它的时候推荐使用for循环。
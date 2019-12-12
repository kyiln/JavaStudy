ArrayList源码解析
前言：源码都是基于JDK1.8。ArrayList是我们开发中比较常用的一个集合类，底层是基于数组实现的，现在就来看一看里面是怎么实现的
1.首先看一下定义的成员变量
    int DEFAULT_CAPACITY = 10; //默认初始化数组的大小
    Object[] EMPTY_ELEMENTDATA = {}; //空数组对象
    Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {}; //默认大小空数组对象，跟上面变量的区别在于，新建一个集合对象时，没有指定大小，就用这个对象
    transient Object[] elementData; //集合CRUD时操作的数组，不可序列化
    int size; //集合的大小
    int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8; //最大的数组大小，为啥要-8，我也没搞懂
2.成员变量之后来看一下几个构造方法
    //带容量参数的构造方法
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {          //容量大于0，直接新建一个Object数组
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {      //容量等于0，直接用之前声明的空数组对象
            this.elementData = EMPTY_ELEMENTDATA;
        } else {                            //如果小于0，参数是不合法的，抛出异常
            throw new IllegalArgumentException("Illegal Capacity: "+ initialCapacity);
        }
    }
    //不带参数的构造方法
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA; //默认大小空数组对象
    }
    //带集合参数的构造方法
    public ArrayList(Collection<? extends E> c) {
        elementData = c.toArray();          //先把集合转换为数组，下面会分析toArray()这个方法
        if ((size = elementData.length) != 0) { //把集合的大小赋值给size，如果传入集合的长度不为0，再进去判断数组的Class对象是不是Object[].class
            // c.toArray might (incorrectly) not return Object[] (see 6260652)
            if (elementData.getClass() != Object[].class)   //如果不是Object[].class，那就得复制整个数组里面的元素
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {    //如果集合大小等于0(不可能小于0，因为小于0都会抛异常)，赋值为空数组对象
            // replace with empty array.
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }
3.构造方法说完之后，再来看几个重要的方法，其它方法就不一一分析了
    //首先看一下add方法，有两个add方法，一个是向数组添加元素，一个是向数组指定位置添加元素
    public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }
    //这个方法是确保数组的容量，让新加的元素能加到数组中去
    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }
    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;
        // overflow-conscious code
        if (minCapacity - elementData.length > 0)  //如果最小容量大于数组的大小，扩容数组
            grow(minCapacity);
    }
    //扩容方法
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1); //先把数组扩容1.5倍
        if (newCapacity - minCapacity < 0)      //如果扩容后的大小还小于minCapacity，那就直接把大小改成minCapacity
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)   //如果扩容后大小大于最大数组大小，看minCapacity大小是否大于最大数组大小，如果大于返回Integer.MAX_VALUE，否则返回MAX_ARRAY_SIZE
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);      //把数组复制一份返回
    }
    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?     //这个地方返回Integer.MAX_VALUE，和上面最大数组大小，会不会有问题？
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
    //计算容量大小
    private static int calculateCapacity(Object[] elementData, int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) { //如果数组为空，取默认大小和最小容量大小中的最大值
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }   //否则返回最小容量大小
        return minCapacity;
    }

    //在指定位置插入数据
    public void add(int index, E element) {
        rangeCheckForAdd(index);    //判断添加元素的位置是否越界
        ensureCapacityInternal(size + 1);  // Increments modCount!!     //跟上面的方法一样，判断是否要扩容，如果要扩容，扩容后返回新数组
        System.arraycopy(elementData, index, elementData, index + 1, size - index);  //在指定位置插入元素后，把指定位置后的元素全部向后移一位
        elementData[index] = element;
        size++;
    }

    //批量增加的方法
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);    //判断添加元素的位置是否越界
        Object[] a = c.toArray();   //把要添加的集合转换为Object[]
        int numNew = a.length;
        //判断是否需要扩容，如果需要的话，大小为size + numNew
        ensureCapacityInternal(size + numNew);  // Increments modCount
        int numMoved = size - index;
        if (numMoved > 0)   //判断原数组里面的元素是否需要移动，如果需要，移动到index + numNew，为什么是这个长度呢？因为index + numNew的长度要放批量新加的集合
            System.arraycopy(elementData, index, elementData, index + numNew, numMoved);
        //把新加的集合添加到数组中
        System.arraycopy(a, 0, elementData, index, numNew);
        size += numNew; //把size变成添加集合后的大小
        return numNew != 0;
    }

    //get方法
    public E get(int index) {
        rangeCheck(index);      //判断元素下标是否大于size
        return elementData(index); //从数组中取元素返回
    }

    //删除方法，这里有两个，一个是根据下标删除，并返回删除的元素，时间复杂度为O(1)。还有一个是根据元素删除，返回是否删除成功，需要循环数组中的元素，最好时间复杂度为O(1),最坏时间复杂度为O(n)，平均时间复杂度为O(n)
    public E remove(int index) {
        rangeCheck(index);  //判断元素下标是否大于size
        modCount++; 
        E oldValue = elementData(index);    //取出要删除的元素，最后返回
        int numMoved = size - index - 1;    //得到要移动元素的长度
        if (numMoved > 0)   //如果元素大于0，移动数组
            System.arraycopy(elementData, index+1, elementData, index, numMoved);
        elementData[--size] = null; // clear to let GC do its work  //把最后一个元素置为空
        return oldValue;
    }

    //截取集合
    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size);        //检查边界，是否合法
        return new SubList(this, 0, fromIndex, toIndex);    //每次截取集合，会返回一个新的SubList，这个新的SubList又实现了集合中的大部分方法。这里就不贴代码了，太长了
    }

    //1.8新增的循环集合方法
    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);         //判断传的函数式对象是否为空
        final int expectedModCount = modCount;  //操作次数
        @SuppressWarnings("unchecked")
        final E[] elementData = (E[]) this.elementData; //底层数组
        final int size = this.size; //数组大小
        for (int i=0; modCount == expectedModCount && i < size; i++) {  //这里多一个判断是因为在集合循环的时候，不能去增、删、改里面的元素
            action.accept(elementData[i]);
        }
        if (modCount != expectedModCount) {     //如果有增、删、改操作的话，就抛异常
            throw new ConcurrentModificationException();
        }
    }

4.总结：ArrayList就分析到这了，如有错误请指正，或者有建议也欢迎提出来一起讨论



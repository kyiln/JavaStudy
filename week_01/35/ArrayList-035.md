ArrayList
基于数组实现
继承抽象类AbstractList
实现了List（接口->Collection、Iterable）
RandomAccess（随机访问）
Cloneable（可克隆）
Serializable（可序列化）

主要参数：
DEFAULT_CAPACITY（默认初始化大小）
EMPTY_ELEMENTDATA（指定list为空时使用的数组）
DEFAULTCAPACITY_EMPTY_ELEMENTDATA（当使用默认无参构造器创建的空list数组，在扩容时会考虑使用默认的扩容方案DEFAULT_CAPACITY）
elementData（存放元素的数组）
size（数组大小）

madCount(全局变量)
madCount交由迭代器（Iterator）和列表迭代器（ListIterator）使用，
当进行next()、remove()、previous()、set()、add()等操作时，
如果madCount的值意外改变，那么迭代器或者列表迭代器就会抛出ConcurrentModificationException异常。

ArrayList中就继承了AbstractList并在每个结构性改变的方法中让madCount变量自增1，并且实现了自己的迭代器;
在next()方法中，判断了当前的modCount跟初始化Itr时的expectedModCount是否相同，
不同则说明列表数据进行了结构性改变，此时就会抛出ConcurrentModificationException。

1、三个构造方法
    1）无参
        初始大小10
    2）int参数
        大于0初始化传入值，新建数组对象；等于0等于空实例数组；否则抛出IllegalArgumentException
    3）Collection参数
        定义为空集合时，初始化大小10；否则获取toArry的length进行初始化
        if (elementData.getClass() != Object[].class)->调用toArray方法返回的不一定是Object[]类型
        不一致时，调用Arrays.copyof()定义了一个新的数组，将原数组的数据拷贝到了新的数组中去.
        源码：
        public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
            @SuppressWarnings("unchecked")
            T[] copy = ((Object)newType == (Object)Object[].class)
                ? (T[]) new Object[newLength]
                : (T[]) Array.newInstance(newType.getComponentType(), newLength);
            System.arraycopy(original, 0, copy, 0,
                             Math.min(original.length, newLength));
            return copy;
        }
        
        
# ArrayList
## 简单说明
#### ArrayList 是一个数组队列，相当于动态数组。容量能动态增，但是操作不是线程安全的。当多个线程并发访问同一个ArrayList时，会抛出ConcurrentModificationException,这就是fail-fast机制。
#### 在ArrayList中有两个情况可以导致OutOfMemoryError：1、当minCapacity<0时，系统无法创建长度小于0 的数组；2、当数组容量超过VM中堆的剩余空间大小时，VM无法为其分配足够的内存。
## 常用方法
#### 1、get方法
```Java
public E get(int index) {   // 根据索引获取元素
    rangeCheck(index);  // 校验索引是否越界
 
    return elementData(index);  // 直接根据index返回对应位置的元素（底层elementData是个数组）
}
```
#### 2、set方法
```Java 
public E set(int index, E element) {    // 用指定的元素（element）替换指定位置（index）的元素
    rangeCheck(index);  // 校验索引是否越界
 
    E oldValue = elementData(index);    // 根据index获取指定位置的元素
    elementData[index] = element;   // 用传入的element替换index位置的元素
    return oldValue;    // 返回index位置原来的元素
}
```
#### 3、add方法
```Java
public boolean add(E e) {   // 增加一个元素
    ensureCapacityInternal(size + 1);  // 将modCount+1，并校验添加元素后是否需要扩容
    elementData[size++] = e;    // 在数组尾部添加元素，并将size+1
    return true;
}
```
```Java
public void add(int index, E element) { // 将指定的元素（element）插入此列表中的指定位置（index）。将index位置及后面的所有元素（如果有的话）向右移动一个位置
    rangeCheckForAdd(index);    // 校验索引是否越界
 
    ensureCapacityInternal(size + 1);  // 将modCount+1，并校验添加元素后是否需要扩容
    System.arraycopy(elementData, index, elementData, index + 1,    // 将index位置及之后的所有元素向右移动一个位置（为要添加的元素腾出1个位置）
                     size - index);
    elementData[index] = element;   // index位置设置为element元素
    size++; // 元素数量+1
}   
```
#### 4、remove方法
```Java
public E remove(int index) {    // 删除列表中index位置的元素，将index位置后面的所有元素向左移一个位置
    rangeCheck(index);  // 校验索引是否越界
 
    modCount++; // 修改次数+1
    E oldValue = elementData(index);    // index位置的元素，也就是将要被移除的元素
 
    int numMoved = size - index - 1;    // 计算需要移动的元素个数，例如：size为10，index为9，此时numMoved为0，则无需移动元素，因为此时index为9的元素刚好是最后一个元素，直接执行下面的代码，将索引为9的元素赋值为空即可
    if (numMoved > 0)   // 如果需要移动元素
        System.arraycopy(elementData, index+1, elementData, index,
                         numMoved); // 将index+1位置及之后的所有元素，向左移动一个位置
    elementData[--size] = null; // 将size-1，并将size-1位置的元素赋值为空（因为上面将元素左移了，所以size-1位置的元素为重复的，将其移除）
 
    return oldValue;    // 返回index位置原来的元素
}
```
```Java
public boolean remove(Object o) {   // 如果存在与入参相同的元素，则从该列表中删除指定元素的第一个匹配项。如果列表不包含元素，则不变
    if (o == null) {    // 如果入参元素为空，则遍历数组查找是否存在元素为空，如果存在则调用fastRemove将该元素移除，并返回true表示移除成功
        for (int index = 0; index < size; index++)
            if (elementData[index] == null) {
                fastRemove(index);
                return true;
            }
    } else {    // 如果入参元素不为空，则遍历数组查找是否存在元素与入参元素使用equals比较返回true，如果存在则调用fastRemove将该元素移除，并返回true表示移除成功
        for (int index = 0; index < size; index++)
            if (o.equals(elementData[index])) {
                fastRemove(index);
                return true;
            }
    }
    return false;   // 不存在目标元素，返回false
}
```
```Java
private void fastRemove(int index) {    // 私有方法，供上面的remove方法调用，直接删除掉index位置的元素
    modCount++; // 修改次数+1
    int numMoved = size - index - 1; // 计算需要移动的元素个数，例如：size为10，index为9，此时numMoved为0，则无需移动元素，因为此时index为9的元素刚好是最后一个元素，直接执行下面的代码，将索引为9的元素赋值为空即可
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index,
                         numMoved); // 将index+1位置及之后的所有元素，向左移动一个位置
    elementData[--size] = null; // 将size-1，并将size-1位置的元素赋值为空（因为上面将元素左移了，所以size-1位置的元素为重复的，将其移除）
}
```
#### 5、clear方法
``` Java
public void clear() {   // 删除此列表中的所有元素。
    modCount++; // 修改次数+1
    for (int i = 0; i < size; i++)  // 遍历数组将所有元素清空
        elementData[i] = null;
 
    size = 0;   // 元素数量赋0
}
```
## 扩容
#### 当数组容量不够时，数组有一个扩容的过程，在扩容的过程中，会将原来数组的元素拷贝到新的数组中，这是一个很耗时的操作。动态数组(ArrayList)在使用方便的同时，也会承担降低性能的风险。
```Java
// 初始容量10
private static final int DEFAULT_CAPACITY = 10; 
 // 空实例数组
private static final Object[] EMPTY_ELEMENTDATA = {};  
// 默认大小的空实例数组，在第一次调用ensureCapacityInternal时会初始化长度为10
private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {}; 
// 存放元素的数组  
transient Object[] elementData; 
 // 数组当前的元素数量
private int size;  
// 数组允许的最大长度
private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8; 
```
```Java
private void ensureCapacityInternal(int minCapacity) {
    // 校验当前数组是否为DEFAULTCAPACITY_EMPTY_ELEMENTDATA，
    // 如果是则将minCapacity设为DEFAULT_CAPACITY，
    // 主要是给DEFAULTCAPACITY_EMPTY_ELEMENTDATA设置初始容量
    if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) { 
        minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
    }
 
    ensureExplicitCapacity(minCapacity);
}
```
```Java
private void ensureExplicitCapacity(int minCapacity) {
    modCount++; // 修改次数+1
 
    // 如果添加该元素后的大小超过数组大小，则进行扩容
    if (minCapacity - elementData.length > 0)
        grow(minCapacity);  // 进行扩容
}
```
```Java
private void grow(int minCapacity) {    // 数组扩容
    // overflow-conscious code
    int oldCapacity = elementData.length;   // 原来的容量
    int newCapacity = oldCapacity + (oldCapacity >> 1); // 新容量 = 老容量 + 老容量 / 2
    if (newCapacity - minCapacity < 0)  // 如果新容量比minCapacity小，
        newCapacity = minCapacity;  // 则将新容量设为minCapacity，兼容初始化情况
    if (newCapacity - MAX_ARRAY_SIZE > 0)   // 如果新容量比最大允许容量大，
        newCapacity = hugeCapacity(minCapacity);    // 则调用hugeCapacity方法设置一个合适的容量
    // 将原数组元素拷贝到一个容量为newCapacity的新数组（使用System.arraycopy），
    // 并且将elementData设置为新数组
    elementData = Arrays.copyOf(elementData, newCapacity);  
}
```
```Java
private static int hugeCapacity(int minCapacity) {
    if (minCapacity < 0) // overflow
        throw new OutOfMemoryError();   // 越界
    // 如果minCapacity大于MAX_ARRAY_SIZE，则返回Integer.MAX_VALUE，否则返回MAX_ARRAY_SIZE
    return (minCapacity > MAX_ARRAY_SIZE) ? 
        Integer.MAX_VALUE :
        MAX_ARRAY_SIZE;
```
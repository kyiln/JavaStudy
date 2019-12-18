ArrayList基于1.8源码学习
默认容量大小是 DEFAULT_CAPACITY = 10;
1、扩容
private void grow(int minCapacity) {   
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
如果容器默认大小是10 ，当两个线程在容器有9个值得时候add数据，可能会造成数组下标越界。
2、在HashMap中也存在modCount，起作用就是在遍历容器时，如果有add（put），remove操作会快速报错
final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
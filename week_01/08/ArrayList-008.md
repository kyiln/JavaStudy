# 										读源码--ArrayList

1. ## 继承结构

   1. ### 继承类

      - #### AbstractList

   2. ### 实现接口

      - List
      - RandomAcces--可随机访问
      - Cloneable--可拷贝
      - java.io.Serializable--可序列化

2. ## 属性和方法

   1. ### 属性

      - #### 默认空间（static）：DEFAULT_CAPACITY--10

      - #### 初始化数组（static）

        - ##### EMPTY_ELEMENTDATA：

        - ##### DEFAULTCAPACITY_EMPTY_ELEMENTDATA

      - #### 瞬态对象：elementData

   2. ### 常用方法

      1. #### 构造器

         - ArrayList（int size）
         - ArrayList（Collections c）：任意集合转ArrayList。底层实现为数组

      2. #### trimToSize()：去除空值生成新的集合

      3. #### int size()

      4. #### isEmpty()

      5. #### contains(Object o)

      6. #### indexOf(Object o)

         - 若为null，则返回第一个null所在的索引
         - 无则返回-1

      7. #### lastIndexOf(Object o)

      8. #### toArray()：转数组

      9. #### clear()：将数组所有元素置空，便于GC回收

3. ## 扩容（调试）

   1. #### 添加元素add

   2. #### 最小容量minCapacity（添加元素后的数组长度）与数组容量element.length

      - ##### 初始化时为均为0

      - ##### 首次添加单个元素后为element.length变为10

        ```
            private static int calculateCapacity(Object[] elementData, int minCapacity) {
            	// 首次增加元素容量扩展为默认容量10
                if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
                    return Math.max(DEFAULT_CAPACITY, minCapacity);
                }
                return minCapacity;
            }
        ```

        ```
            private void ensureCapacityInternal(int minCapacity) {
                ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
            }
        ```

        ```
            // 精确扩容
            private void ensureExplicitCapacity(int minCapacity) {
                modCount++;
        
                // overflow-conscious code
                if (minCapacity - elementData.length > 0)
                    grow(minCapacity);
            }
        ```

      - ##### 后面超过element.length后依次1.5增长

        ```
            // 真正执行扩容的方法grow
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
        ```

        

      - ###### 注：非1.5倍扩容的情况

        - ##### add()首次添加单个元素扩容至10

        - ##### addAll()批量增加后的数组长度大于扩容1.5倍容量时，直接扩容至数组长度

          ```
              public static void main(String[] args) {
                  ArrayList list = new ArrayList();
                  ArrayList list1 = new ArrayList();
                  int count = 10;
                  int count1 = 8;
                  for (int i = 0; i < count1; i++) {
                      list1.add(i);
                  }
                  for (int i = 0; i < count; i++) {
                      list.add(i);
                      System.out.println(i+":"+list);
                  }
                  // 当增加的数组长度大于1.5倍容量的扩容情况
                  list.addAll(list1);
              }
          ```

4. ## 增删改查

   1. ### 通用方法

      - #### System.arraycopy()方法

        ```
           public static native void arraycopy(Object src,  int  srcPos,
                                                Object dest, int destPos,
                                                int length);
        ```

   2. ### 增

      - ##### 末尾增加：boolean add(E e) 

      - ##### 指定位置增加

        ```
            public void add(int index, E element) {
                rangeCheckForAdd(index);
        
                ensureCapacityInternal(size + 1);  // Increments modCount!!
                // 后续元素依次往后移，添加缓慢，删除同理
                System.arraycopy(elementData, index, elementData, index + 1,
                                 size - index);
                elementData[index] = element;
                size++;
            }
        ```

      - ##### 批量添加

        ```
            public boolean addAll(Collection<? extends E> c) {
                Object[] a = c.toArray();
                int numNew = a.length;
                // 此时的容量（size + numNew）可能超过扩容后1.5倍，则扩容后的容量为（size + numNew）
                ensureCapacityInternal(size + numNew);  // Increments modCount
                System.arraycopy(a, 0, elementData, size, numNew);
                size += numNew;
                return numNew != 0;
            }
        ```

        ```
            private void grow(int minCapacity) {
                // overflow-conscious code
                int oldCapacity = elementData.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                
                // 需求容量minCapacity大于扩容后的容量newCapacity
                if (newCapacity - minCapacity < 0)
                    newCapacity = minCapacity;
                if (newCapacity - MAX_ARRAY_SIZE > 0)
                    newCapacity = hugeCapacity(minCapacity);
                // minCapacity is usually close to size, so this is a win:
                elementData = Arrays.copyOf(elementData, newCapacity);
            }
        ```

      - ##### 指定位置批量添加

   3. ### 删除

      - ##### 指定下标

        ```
            public E remove(int index) {
                rangeCheck(index);
        
                modCount++;
                E oldValue = elementData(index);
        		// 下标为index的元素，实际是数组的第index+1个元素
                int numMoved = size - index - 1;
                if (numMoved > 0)
                    System.arraycopy(elementData, index+1, elementData, index,
                                     numMoved);
                elementData[--size] = null; // clear to let GC do its work
        
                return oldValue;
            }
        ```

      - ##### 指定对象

        ```
           public boolean remove(Object o) {
                if (o == null) {
                    for (int index = 0; index < size; index++)
                        if (elementData[index] == null) {
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
        ```

      - ##### 指定下标范围批量移除
      
   4. ### 改

      1. #### set(int index, E element)：替换指定位置的元素

   5. ### 查

      1. #### get(int index)

      2. #### 迭代方法

         - ##### ListIterator <E> listIterator（int index）：类似String的subString

         - #####  ListIterator <E> listIterator（）

      3. #### List<E> subList(int fromIndex, int toIndex)：

         - ##### 返回的是ArrayList的内部类--SubList

         - ##### 该SubList无法转换为ArrayList，只是ArrayList的一个视图

           - ###### 对父子类做的非结构性修改，都会影响到彼此

           - ###### 对子List做结构性修改，操作会反映到父List上

           - ###### 对父List做结构性修改，会抛出异常ConcurrentModificationException

           - 若需要对subList进行修改，有不想动原list,那么可以创建subList的一个拷贝

             ```
             subList = Lists.newArrayList(subList);
             list.stream().skip(strart).limit(end).collect(Collectors.toList());
             ```

             

      4. 

         - 

   

   
# 读源码--HashMap

1. ## 继承体系

   1. ### 继承抽象类AbstractHashMap

   2. ### 实现接口List，Cloneable，Serializable

2. ## 常规属性与方法

   1. ### 重要静态属性

      ```
      // 默认初始化容量为16
      static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
      // 最大容量2的30次方
      static final int MAXIMUM_CAPACITY = 1 << 30;
      // 默认负载因子0.75
      static final float DEFAULT_LOAD_FACTOR = 0.75f;
      // 转红黑树阈值8
      static final int TREEIFY_THRESHOLD = 8;
      // 转链表阈值6
      static final int UNTREEIFY_THRESHOLD = 6;
      // 转树的最小容量64，哈希表的容量小于64，会先进行扩容；不能小于4*TREEIFY_THRESHOLD
      static final int MIN_TREEIFY_CAPACITY = 64;
      ```

   2. ### 内部类Node<K,V>（final？）

   3. ### 方法

      1. #### hash方法：将key进行hash重算，让key分别更均匀
      
         ```
       static final int hash(Object key) {
                 int h;
                 return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
             }
         ```
      
         
      
      2. #### comparableClassFor（Object  o）：判断该对象是否实现Comparable接口
      
      3. #### compareComparables(Class<?> kc, Object k, Object x)：k实现Comparable接口，若x为kc类，比较k与x
      
      4. #### tableSizeFor(int cap)：得到大于或等于给定cap的最小二次幂，如cap为15，16，返回的都是16.（位运算技巧牛逼）
      
   4. ### 成员属性
   
      ```
      // 
      transient Node<K,V>[] table;
      transient Set<Map.Entry<K,V>> entrySet;
      transient int size;
      transient int modCount;
      int threshold; //下次扩容的阈值
      final float loadFactor;//负载因子
      ```
   
      
   
   5. ### 构造器
   
      1. #### HashMap(int initialCapacity, float loadFactor)
   
      2. #### HashMap(int initialCapacity)
   
      3. #### HashMap()
   
      4. #### public HashMap(Map<? extends K, ? extends V> m) 
   
   6. ### 常规方法
   
      1. #### size（）
   
      2. #### isEmpty()
   
      3. #### containKey(Object key)
   
      4. #### containKey(Object value)
   
      5. #### clear()：清空map
   
3. ## 底层方法

   1. ### putMapEntries方法：

      ```
         final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
              int s = m.size();
              if (s > 0) {
              	// 若桶为空
                  if (table == null) { // pre-size
                      float ft = ((float)s / loadFactor) + 1.0F;
                      int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                               (int)ft : MAXIMUM_CAPACITY);
                      // 若计算出的容量大于当前扩容阈值，则重新计算阈值
                      if (t > threshold)
                          threshold = tableSizeFor(t);
                  }
                  // 当map大小大于当前阈值时，扩容
                  else if (s > threshold)
                      resize();
                  // 赋值
                  for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                      K key = e.getKey();
                      V value = e.getValue();
                      putVal(hash(key), key, value, false, evict);
                  }
              }
          }
      ```

      

   2. ### putVal方法：赋值

      ```
      final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                         boolean evict) {
              Node<K,V>[] tab; Node<K,V> p; int n, i;
              // 判断tab是否为null
              if ((tab = table) == null || (n = tab.length) == 0)
                  n = (tab = resize()).length;
              // 若对应索引下tab值为空，则直接插入
              if ((p = tab[i = (n - 1) & hash]) == null)
                  tab[i] = newNode(hash, key, value, null);
              // 若存在，说明存在相同hash值。1：key值相同，说明存在该key了 2：key值不同，hash冲突
              else {
                  Node<K,V> e; K k;
                  // key值相同
                  if (p.hash == hash &&
                      ((k = p.key) == key || (key != null && key.equals(k))))
                      e = p;
                  // key值不同
                  // p为树节点
                  else if (p instanceof TreeNode)
                      e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
                  // p为链表
                  else {
                      for (int binCount = 0; ; ++binCount) {
                      	// 插入到链表尾部
                          if ((e = p.next) == null) {
                              p.next = newNode(hash, key, value, null);
                              // 超过转树的阈值，转为树节点，-1是因为binCount从0开始
                              if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                                  treeifyBin(tab, hash);
                              break;
                          }
                          if (e.hash == hash &&
                              ((k = e.key) == key || (key != null && key.equals(k))))
                              break;
                          p = e;
                      }
                  }
                  if (e != null) { // existing mapping for key
                      V oldValue = e.value;
                      if (!onlyIfAbsent || oldValue == null)
                          e.value = value;
                      afterNodeAccess(e);
                      return oldValue;
                  }
              }
              ++modCount;
              if (++size > threshold)
                  resize();
              afterNodeInsertion(evict);
              return null;
          }
      ```

      ![](E:\Project\JavaStudy\week_01\08\HashMap-put执行流程示意图.jpg)

   3. ### reSize()：扩容

      ```
       final Node<K,V>[] resize() {
              Node<K,V>[] oldTab = table;
              int oldCap = (oldTab == null) ? 0 : oldTab.length;
              int oldThr = threshold;
              int newCap, newThr = 0;
              if (oldCap > 0) {
                  if (oldCap >= MAXIMUM_CAPACITY) {
                      threshold = Integer.MAX_VALUE;
                      return oldTab;
                  }
                  // 
                  else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                           oldCap >= DEFAULT_INITIAL_CAPACITY)
                      newThr = oldThr << 1; // double threshold
              }
              else if (oldThr > 0) // initial capacity was placed in threshold
                  newCap = oldThr;
              else {               // zero initial threshold signifies using defaults
                  newCap = DEFAULT_INITIAL_CAPACITY;
                  newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
              }
              if (newThr == 0) {
                  float ft = (float)newCap * loadFactor;
                  newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                            (int)ft : Integer.MAX_VALUE);
              }
              threshold = newThr;
              @SuppressWarnings({"rawtypes","unchecked"})
                  Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
              table = newTab;
              if (oldTab != null) {
                  for (int j = 0; j < oldCap; ++j) {
                      Node<K,V> e;
                      if ((e = oldTab[j]) != null) {
                          oldTab[j] = null;
                          if (e.next == null)
                              newTab[e.hash & (newCap - 1)] = e;
                          else if (e instanceof TreeNode)
                              ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                          else { // preserve order
                              Node<K,V> loHead = null, loTail = null;
                              Node<K,V> hiHead = null, hiTail = null;
                              Node<K,V> next;
                              do {
                                  next = e.next;
                                  if ((e.hash & oldCap) == 0) {
                                      if (loTail == null)
                                          loHead = e;
                                      else
                                          loTail.next = e;
                                      loTail = e;
                                  }
                                  else {
                                      if (hiTail == null)
                                          hiHead = e;
                                      else
                                          hiTail.next = e;
                                      hiTail = e;
                                  }
                              } while ((e = next) != null);
                              if (loTail != null) {
                                  loTail.next = null;
                                  newTab[j] = loHead;
                              }
                              if (hiTail != null) {
                                  hiTail.next = null;
                                  newTab[j + oldCap] = hiHead;
                              }
                          }
                      }
                  }
              }
              return newTab;
          }
      ```

   4. ### treeifyBin()：转树（树结构不懂，待学习）

      ```
          final void treeifyBin(Node<K,V>[] tab, int hash) {
              int n, index; Node<K,V> e;
              // 桶大小小于最小转树容量，先扩容
              if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
                  resize();
              else if ((e = tab[index = (n - 1) & hash]) != null) {
                  TreeNode<K,V> hd = null, tl = null;
                  do {
                      TreeNode<K,V> p = replacementTreeNode(e, null);
                      if (tl == null)
                          hd = p;
                      else {
                          p.prev = tl;
                          tl.next = p;
                      }
                      tl = p;
                  } while ((e = e.next) != null);
                  if ((tab[index] = hd) != null)
                      hd.treeify(tab);
              }
          }
      ```

      

4. ## 增删改查

   1. ### 查：

      1. #### get(Object   key)

      2. #### getNode(int hash, Object key)

         ```
             final Node<K,V> getNode(int hash, Object key) {
                 Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
                 if ((tab = table) != null && (n = tab.length) > 0 &&
                     (first = tab[(n - 1) & hash]) != null) {
                     if (first.hash == hash && // always check first node
                         ((k = first.key) == key || (key != null && key.equals(k))))
                         return first;
                     // 当存在hash冲突时，进行链表查询
                     if ((e = first.next) != null) {
                     	// 若为树，则进行树节点查询（链表长度超过8会转为红黑树）
                         if (first instanceof TreeNode)
                             return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                         // 若不是树，则返回对应的链表查询值
                         do {
                             if (e.hash == hash &&
                                 ((k = e.key) == key || (key != null && key.equals(k))))
                                 return e;
                         } while ((e = e.next) != null);
                     }
                 }
                 return null;
             }
         ```

         

   2. ### 增

      1. #### put(K key,V value)：增或改

      2. #### putAll(Map<? extends K, ? extends V> m)：增加map

   3. ### 删

      1. #### remove(Object  key)

      2. #### remove(K key,V value)：

   4. ### 改

      1. #### put(K key,V value)：

      2. #### replace(K key, V oldValue, V newValue)

   5. #### 遍历

      1. #### Set<K> keySet() ：将map中所有键放入Set中，通过遍历Set达到遍历map键的目的

      2. #### Collections<V> values()：获取map中的所有值

      3. #### Set<Map.Entry<K,V>> entrySet()：遍历map键和值
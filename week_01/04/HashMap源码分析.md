# HashMap



## 继承体系

### 继承

​	AbstractMap

### 实现

1. Map
2. Cloneable
3. Serializable

## 主要属性

```java
/**
 * 默认初始容量16
 * The default initial capacity - MUST be a power of two.
 */
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

/**
 * 最大的容量
 * The maximum capacity, used if a higher value is implicitly specified
 * by either of the constructors with arguments.
 * MUST be a power of two <= 1<<30.
 */
static final int MAXIMUM_CAPACITY = 1 << 30;

/**
 * 默认加载因子
 * The load factor used when none specified in constructor.
 */
static final float DEFAULT_LOAD_FACTOR = 0.75f;

/**
 * 使用树的桶数量阈值
 * The bin count threshold for using a tree rather than list for a
 * bin.  Bins are converted to trees when adding an element to a
 * bin with at least this many nodes. The value must be greater
 * than 2 and should be at least 8 to mesh with assumptions in
 * tree removal about conversion back to plain bins upon
 * shrinkage.
 */
static final int TREEIFY_THRESHOLD = 8;

/**
 * 树退化成链表的阈值
 * The bin count threshold for untreeifying a (split) bin during a
 * resize operation. Should be less than TREEIFY_THRESHOLD, and at
 * most 6 to mesh with shrinkage detection under removal.
 */
static final int UNTREEIFY_THRESHOLD = 6;

/**
 * The smallest table capacity for which bins may be treeified.
 * (Otherwise the table is resized if too many nodes in a bin.)
 * Should be at least 4 * TREEIFY_THRESHOLD to avoid conflicts
 * between resizing and treeification thresholds.
 */
static final int MIN_TREEIFY_CAPACITY = 64;
```



```java
/**
 * 桶（表），第一次使用时才会初始化，必要时扩容
 * The table, initialized on first use, and resized as
 * necessary. When allocated, length is always a power of two.
 * (We also tolerate length zero in some operations to allow
 * bootstrapping mechanics that are currently not needed.)
 */
transient Node<K,V>[] table;

/**
 * 缓存键值对的entrySet
 * Holds cached entrySet(). Note that AbstractMap fields are used
 * for keySet() and values().
 */
transient Set<Map.Entry<K,V>> entrySet;

/**
 * 键值对的数量
 * The number of key-value mappings contained in this map.
 */
transient int size;

/**
 * 修改次数
 * The number of times this HashMap has been structurally modified
 * Structural modifications are those that change the number of mappings in
 * the HashMap or otherwise modify its internal structure (e.g.,
 * rehash).  This field is used to make iterators on Collection-views of
 * the HashMap fail-fast.  (See ConcurrentModificationException).
 */
transient int modCount;

/**
 * 下一个要重新resize的大小
 * The next size value at which to resize (capacity * load factor).
 *
 * @serial
 */
// (The javadoc description is true upon serialization.
// Additionally, if the table array has not been allocated, this
// field holds the initial array capacity, or zero signifying
// DEFAULT_INITIAL_CAPACITY.)
int threshold;

/**
 * 加载因子
 * The load factor for the hash table.
 *
 * @serial
 */
final float loadFactor;
```

## 构造函数

1. 空构造

   ```java
   /**
    * 默认值 16 load factor .75
    * Constructs an empty <tt>HashMap</tt> with the default initial capacity
    * (16) and the default load factor (0.75).
    */
   public HashMap() {
       this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
   }
   ```

2. 传入Map的构造

   ```java
   /**
    * 构造足够承载目标的容量
    * Constructs a new <tt>HashMap</tt> with the same mappings as the
    * specified <tt>Map</tt>.  The <tt>HashMap</tt> is created with
    * default load factor (0.75) and an initial capacity sufficient to
    * hold the mappings in the specified <tt>Map</tt>.
    *
    * @param   m the map whose mappings are to be placed in this map
    * @throws  NullPointerException if the specified map is null
    */
   public HashMap(Map<? extends K, ? extends V> m) {
       this.loadFactor = DEFAULT_LOAD_FACTOR;
       putMapEntries(m, false);
   }
   
   /**
    * Implements Map.putAll and Map constructor.
    *
    * @param m the map
    * @param evict false when initially constructing this map, else
    * true (relayed to method afterNodeInsertion).
    */
   final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
       int s = m.size();
       if (s > 0) {
           if (table == null) { // pre-size
             	// 除以加载因子 + 1 防止因加入全部元素而要扩容影响性能
               float ft = ((float)s / loadFactor) + 1.0F;
             	// 防止大于 MAXIMUM_CAPACITY
               int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                        (int)ft : MAXIMUM_CAPACITY);
             	// 修改threshold值
               if (t > threshold)
                   threshold = tableSizeFor(t);
           }
           else if (s > threshold)
             	// 查看如下
               resize();
           for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
               K key = e.getKey();
               V value = e.getValue();
               // 进行存值操作
               putVal(hash(key), key, value, false, evict);
           }
       }
   }
   ```

   ```java
   /**
    * 初始化或将table双倍。如果是初始化，通过初始化容量threshold来分配，
    * 然而由于我们使用了二次幂的扩容方式，元素可能存储在原索引位置，或者移动到二次幂的新表中
    * Initializes or doubles table size.  If null, allocates in
    * accord with initial capacity target held in field threshold.
    * Otherwise, because we are using power-of-two expansion, the
    * elements from each bin must either stay at same index, or move
    * with a power of two offset in the new table.
    *
    * @return the table
    */
   final Node<K,V>[] resize() {
     	// 旧的表
       Node<K,V>[] oldTab = table;
       // 过去的容量
       int oldCap = (oldTab == null) ? 0 : oldTab.length;
       // 过去的扩容指标
       int oldThr = threshold;
       int newCap, newThr = 0;
       if (oldCap > 0) {
           if (oldCap >= MAXIMUM_CAPACITY) {
               threshold = Integer.MAX_VALUE;
               return oldTab;
           }
           // 旧容量 >= DEFAULT_INITIAL_CAPACITY && 新容量 = 旧容量 * 2 < MAXIMUM_CAPACITY
           else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                    oldCap >= DEFAULT_INITIAL_CAPACITY)
             	// 这种就是翻倍的情况
               newThr = oldThr << 1; // double threshold
       }
       else if (oldThr > 0) // initial capacity was placed in threshold
           newCap = oldThr;
       else {               // zero initial threshold signifies using defaults
           // 初始默认值
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
                   // gc help
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

   ```java
   /**
    * 实现Map.put 和 相关方法
    * Implements Map.put and related methods.
    *
    * @param hash hash for key
    * @param key the key
    * @param value the value to put
    * @param onlyIfAbsent if true, don't change existing value 如果为true 不需要改变已存在的值
    * @param evict if false, the table is in creation mode. 如果为false ，表是在创建模式
    * @return previous value, or null if none
    */
   final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                  boolean evict) {
       Node<K,V>[] tab; Node<K,V> p; int n, i;
       // 容器没有初始化的情况下 需要调用resize
       if ((tab = table) == null || (n = tab.length) == 0)
           n = (tab = resize()).length;
       // 如果该位置为空的情况直接赋值 即 桶的位置上为空
       if ((p = tab[i = (n - 1) & hash]) == null)
           tab[i] = newNode(hash, key, value, null);
       else {
           // 此位置已存在桶
           Node<K,V> e; K k;
           if (p.hash == hash &&
               ((k = p.key) == key || (key != null && key.equals(k))))
               e = p;
           else if (p instanceof TreeNode)
               // 如果已经是树了，那么调用TreeNode#putTreeVal
               e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
           else {
               for (int binCount = 0; ; ++binCount) {
                   if ((e = p.next) == null) {
                       p.next = newNode(hash, key, value, null);
                       // 如果插入了新数据后链表长度大于8，那么就要进行树化
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
           // 找到相同key的元素
           if (e != null) { // existing mapping for key
               V oldValue = e.value;
               // 是否要替换旧值
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

   ```java
   /**
    * Replaces all linked nodes in bin at index for given hash unless
    * table is too small, in which case resizes instead.
    */
   final void treeifyBin(Node<K,V>[] tab, int hash) {
       int n, index; Node<K,V> e;
       if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
           resize();
       else if ((e = tab[index = (n - 1) & hash]) != null) {
           TreeNode<K,V> hd = null, tl = null;
         	// 把所有节点转化为树节点
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

3. 



## 内部类

### 简单看



### 仔细看

1. Node

   ```java
   /**
    * 基础hash桶节点
    * Basic hash bin node, used for most entries.  (See below for
    * TreeNode subclass, and in LinkedHashMap for its Entry subclass.)
    */
   static class Node<K,V> implements Map.Entry<K,V> {
       final int hash;
       final K key;
       V value;
     	// 单向链表
       Node<K,V> next;
   
       Node(int hash, K key, V value, Node<K,V> next) {
           this.hash = hash;
           this.key = key;
           this.value = value;
           this.next = next;
       }
   
       public final K getKey()        { return key; }
       public final V getValue()      { return value; }
       public final String toString() { return key + "=" + value; }
   
       public final int hashCode() {
           return Objects.hashCode(key) ^ Objects.hashCode(value);
       }
   
       public final V setValue(V newValue) {
           V oldValue = value;
           value = newValue;
           return oldValue;
       }
   
       public final boolean equals(Object o) {
           if (o == this)
               return true;
           if (o instanceof Map.Entry) {
               Map.Entry<?,?> e = (Map.Entry<?,?>)o;
               if (Objects.equals(key, e.getKey()) &&
                   Objects.equals(value, e.getValue()))
                   return true;
           }
           return false;
       }
   }
   ```

   

2. TreeNode

   ```java
   /**
    * 树桶，继承了 LinkedHashMap.Entry,所以即可以作为常规或链式节点
    * Entry for Tree bins. Extends LinkedHashMap.Entry (which in turn
    * extends Node) so can be used as extension of either regular or
    * linked node.
    */
   static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
       // 父亲节点
       TreeNode<K,V> parent;  // red-black tree links
       // 左节点
       TreeNode<K,V> left;
     	// 右节点
       TreeNode<K,V> right;
       TreeNode<K,V> prev;    // needed to unlink next upon deletion
       boolean red;
       // 构造方法
       TreeNode(int hash, K key, V val, Node<K,V> next) {
           super(hash, key, val, next);
       }
   
       /**
        * Returns root of tree containing this node.
        * 树的根
        */
       final TreeNode<K,V> root() {
           for (TreeNode<K,V> r = this, p;;) {
               if ((p = r.parent) == null)
                   return r;
               r = p;
           }
       }
   
       /**
        * 确保给的根是头结点，将Root移到最前面
        * Ensures that the given root is the first node of its bin.
        */
       static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root) {
           int n;
           if (root != null && tab != null && (n = tab.length) > 0) {
               int index = (n - 1) & root.hash;
               TreeNode<K,V> first = (TreeNode<K,V>)tab[index];
               if (root != first) {
                   Node<K,V> rn;
                   tab[index] = root;
                   TreeNode<K,V> rp = root.prev;
                   if ((rn = root.next) != null)
                       ((TreeNode<K,V>)rn).prev = rp;
                   if (rp != null)
                       rp.next = rn;
                   if (first != null)
                       first.prev = root;
                   root.next = first;
                   root.prev = null;
               }
               assert checkInvariants(root);
           }
       }
   
       /**
        * 找到给定hash值和key的Treenode
        * Finds the node starting at root p with the given hash and key.
        * The kc argument caches comparableClassFor(key) upon first use
        * comparing keys.
        */
       final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
           TreeNode<K,V> p = this;
           do {
               int ph, dir; K pk;
               TreeNode<K,V> pl = p.left, pr = p.right, q;
               if ((ph = p.hash) > h)
                   p = pl;
               else if (ph < h)
                   p = pr;
               else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                   return p;
               else if (pl == null)
                   p = pr;
               else if (pr == null)
                   p = pl;
               else if ((kc != null ||
                         (kc = comparableClassFor(k)) != null) &&
                        (dir = compareComparables(kc, k, pk)) != 0)
                   p = (dir < 0) ? pl : pr;
               else if ((q = pr.find(h, k, kc)) != null)
                   return q;
               else
                   p = pl;
           } while (p != null);
           return null;
       }
   
       /**
        * Calls find for root node.
        */
       final TreeNode<K,V> getTreeNode(int h, Object k) {
           return ((parent != null) ? root() : this).find(h, k, null);
       }
   
       /**
        * Tie-breaking utility for ordering insertions when equal
        * hashCodes and non-comparable. We don't require a total
        * order, just a consistent insertion rule to maintain
        * equivalence across rebalancings. Tie-breaking further than
        * necessary simplifies testing a bit.
        */
       static int tieBreakOrder(Object a, Object b) {
           int d;
           if (a == null || b == null ||
               (d = a.getClass().getName().
                compareTo(b.getClass().getName())) == 0)
               d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                    -1 : 1);
           return d;
       }
   
       /**
        * Forms tree of the nodes linked from this node.
        */
       final void treeify(Node<K,V>[] tab) {
           TreeNode<K,V> root = null;
           for (TreeNode<K,V> x = this, next; x != null; x = next) {
               next = (TreeNode<K,V>)x.next;
               x.left = x.right = null;
               if (root == null) {
                   x.parent = null;
                   x.red = false;
                   root = x;
               }
               else {
                   K k = x.key;
                   int h = x.hash;
                   Class<?> kc = null;
                   for (TreeNode<K,V> p = root;;) {
                       int dir, ph;
                       K pk = p.key;
                       if ((ph = p.hash) > h)
                           dir = -1;
                       else if (ph < h)
                           dir = 1;
                       else if ((kc == null &&
                                 (kc = comparableClassFor(k)) == null) ||
                                (dir = compareComparables(kc, k, pk)) == 0)
                           dir = tieBreakOrder(k, pk);
   
                       TreeNode<K,V> xp = p;
                       if ((p = (dir <= 0) ? p.left : p.right) == null) {
                           x.parent = xp;
                           if (dir <= 0)
                               xp.left = x;
                           else
                               xp.right = x;
                           root = balanceInsertion(root, x);
                           break;
                       }
                   }
               }
           }
           moveRootToFront(tab, root);
       }
   
       /**
        * Returns a list of non-TreeNodes replacing those linked from
        * this node.
        */
       final Node<K,V> untreeify(HashMap<K,V> map) {
           Node<K,V> hd = null, tl = null;
           for (Node<K,V> q = this; q != null; q = q.next) {
               Node<K,V> p = map.replacementNode(q, null);
               if (tl == null)
                   hd = p;
               else
                   tl.next = p;
               tl = p;
           }
           return hd;
       }
   
       /**
        * Tree version of putVal.
        */
       final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
                                      int h, K k, V v) {
           Class<?> kc = null;
           boolean searched = false;
           TreeNode<K,V> root = (parent != null) ? root() : this;
           for (TreeNode<K,V> p = root;;) {
               int dir, ph; K pk;
               if ((ph = p.hash) > h)
                   dir = -1;
               else if (ph < h)
                   dir = 1;
               else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                   return p;
               else if ((kc == null &&
                         (kc = comparableClassFor(k)) == null) ||
                        (dir = compareComparables(kc, k, pk)) == 0) {
                   if (!searched) {
                       TreeNode<K,V> q, ch;
                       searched = true;
                       if (((ch = p.left) != null &&
                            (q = ch.find(h, k, kc)) != null) ||
                           ((ch = p.right) != null &&
                            (q = ch.find(h, k, kc)) != null))
                           return q;
                   }
                   dir = tieBreakOrder(k, pk);
               }
   
               TreeNode<K,V> xp = p;
               if ((p = (dir <= 0) ? p.left : p.right) == null) {
                   Node<K,V> xpn = xp.next;
                   TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
                   if (dir <= 0)
                       xp.left = x;
                   else
                       xp.right = x;
                   xp.next = x;
                   x.parent = x.prev = xp;
                   if (xpn != null)
                       ((TreeNode<K,V>)xpn).prev = x;
                   moveRootToFront(tab, balanceInsertion(root, x));
                   return null;
               }
           }
       }
   
       /**
        * Removes the given node, that must be present before this call.
        * This is messier than typical red-black deletion code because we
        * cannot swap the contents of an interior node with a leaf
        * successor that is pinned by "next" pointers that are accessible
        * independently during traversal. So instead we swap the tree
        * linkages. If the current tree appears to have too few nodes,
        * the bin is converted back to a plain bin. (The test triggers
        * somewhere between 2 and 6 nodes, depending on tree structure).
        */
       final void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab,
                                 boolean movable) {
           int n;
           if (tab == null || (n = tab.length) == 0)
               return;
           int index = (n - 1) & hash;
           TreeNode<K,V> first = (TreeNode<K,V>)tab[index], root = first, rl;
           TreeNode<K,V> succ = (TreeNode<K,V>)next, pred = prev;
           if (pred == null)
               tab[index] = first = succ;
           else
               pred.next = succ;
           if (succ != null)
               succ.prev = pred;
           if (first == null)
               return;
           if (root.parent != null)
               root = root.root();
           if (root == null
               || (movable
                   && (root.right == null
                       || (rl = root.left) == null
                       || rl.left == null))) {
               tab[index] = first.untreeify(map);  // too small
               return;
           }
           TreeNode<K,V> p = this, pl = left, pr = right, replacement;
           if (pl != null && pr != null) {
               TreeNode<K,V> s = pr, sl;
               while ((sl = s.left) != null) // find successor
                   s = sl;
               boolean c = s.red; s.red = p.red; p.red = c; // swap colors
               TreeNode<K,V> sr = s.right;
               TreeNode<K,V> pp = p.parent;
               if (s == pr) { // p was s's direct parent
                   p.parent = s;
                   s.right = p;
               }
               else {
                   TreeNode<K,V> sp = s.parent;
                   if ((p.parent = sp) != null) {
                       if (s == sp.left)
                           sp.left = p;
                       else
                           sp.right = p;
                   }
                   if ((s.right = pr) != null)
                       pr.parent = s;
               }
               p.left = null;
               if ((p.right = sr) != null)
                   sr.parent = p;
               if ((s.left = pl) != null)
                   pl.parent = s;
               if ((s.parent = pp) == null)
                   root = s;
               else if (p == pp.left)
                   pp.left = s;
               else
                   pp.right = s;
               if (sr != null)
                   replacement = sr;
               else
                   replacement = p;
           }
           else if (pl != null)
               replacement = pl;
           else if (pr != null)
               replacement = pr;
           else
               replacement = p;
           if (replacement != p) {
               TreeNode<K,V> pp = replacement.parent = p.parent;
               if (pp == null)
                   root = replacement;
               else if (p == pp.left)
                   pp.left = replacement;
               else
                   pp.right = replacement;
               p.left = p.right = p.parent = null;
           }
   
           TreeNode<K,V> r = p.red ? root : balanceDeletion(root, replacement);
   
           if (replacement == p) {  // detach
               TreeNode<K,V> pp = p.parent;
               p.parent = null;
               if (pp != null) {
                   if (p == pp.left)
                       pp.left = null;
                   else if (p == pp.right)
                       pp.right = null;
               }
           }
           if (movable)
               moveRootToFront(tab, r);
       }
   
       /**
        * Splits nodes in a tree bin into lower and upper tree bins,
        * or untreeifies if now too small. Called only from resize;
        * see above discussion about split bits and indices.
        *
        * @param map the map
        * @param tab the table for recording bin heads
        * @param index the index of the table being split
        * @param bit the bit of hash to split on
        */
       final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
           TreeNode<K,V> b = this;
           // Relink into lo and hi lists, preserving order
           TreeNode<K,V> loHead = null, loTail = null;
           TreeNode<K,V> hiHead = null, hiTail = null;
           int lc = 0, hc = 0;
           for (TreeNode<K,V> e = b, next; e != null; e = next) {
               next = (TreeNode<K,V>)e.next;
               e.next = null;
               if ((e.hash & bit) == 0) {
                   if ((e.prev = loTail) == null)
                       loHead = e;
                   else
                       loTail.next = e;
                   loTail = e;
                   ++lc;
               }
               else {
                   if ((e.prev = hiTail) == null)
                       hiHead = e;
                   else
                       hiTail.next = e;
                   hiTail = e;
                   ++hc;
               }
           }
   
           if (loHead != null) {
               if (lc <= UNTREEIFY_THRESHOLD)
                   tab[index] = loHead.untreeify(map);
               else {
                   tab[index] = loHead;
                   if (hiHead != null) // (else is already treeified)
                       loHead.treeify(tab);
               }
           }
           if (hiHead != null) {
               if (hc <= UNTREEIFY_THRESHOLD)
                   tab[index + bit] = hiHead.untreeify(map);
               else {
                   tab[index + bit] = hiHead;
                   if (loHead != null)
                       hiHead.treeify(tab);
               }
           }
       }
   
       /* ------------------------------------------------------------ */
       // Red-black tree methods, all adapted from CLR
   		// 左旋
       static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root,
                                             TreeNode<K,V> p) {
           TreeNode<K,V> r, pp, rl;
           if (p != null && (r = p.right) != null) {
               if ((rl = p.right = r.left) != null)
                   rl.parent = p;
               if ((pp = r.parent = p.parent) == null)
                   (root = r).red = false;
               else if (pp.left == p)
                   pp.left = r;
               else
                   pp.right = r;
               r.left = p;
               p.parent = r;
           }
           return root;
       }
   		// 右旋
       static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root,
                                              TreeNode<K,V> p) {
           TreeNode<K,V> l, pp, lr;
           if (p != null && (l = p.left) != null) {
               if ((lr = p.left = l.right) != null)
                   lr.parent = p;
               if ((pp = l.parent = p.parent) == null)
                   (root = l).red = false;
               else if (pp.right == p)
                   pp.right = l;
               else
                   pp.left = l;
               l.right = p;
               p.parent = l;
           }
           return root;
       }
   		// 增加平衡
       static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root,
                                                   TreeNode<K,V> x) {
           x.red = true;
           for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {
               if ((xp = x.parent) == null) {
                   x.red = false;
                   return x;
               }
               else if (!xp.red || (xpp = xp.parent) == null)
                   return root;
               if (xp == (xppl = xpp.left)) {
                   if ((xppr = xpp.right) != null && xppr.red) {
                       xppr.red = false;
                       xp.red = false;
                       xpp.red = true;
                       x = xpp;
                   }
                   else {
                       if (x == xp.right) {
                           root = rotateLeft(root, x = xp);
                           xpp = (xp = x.parent) == null ? null : xp.parent;
                       }
                       if (xp != null) {
                           xp.red = false;
                           if (xpp != null) {
                               xpp.red = true;
                               root = rotateRight(root, xpp);
                           }
                       }
                   }
               }
               else {
                   if (xppl != null && xppl.red) {
                       xppl.red = false;
                       xp.red = false;
                       xpp.red = true;
                       x = xpp;
                   }
                   else {
                       if (x == xp.left) {
                           root = rotateRight(root, x = xp);
                           xpp = (xp = x.parent) == null ? null : xp.parent;
                       }
                       if (xp != null) {
                           xp.red = false;
                           if (xpp != null) {
                               xpp.red = true;
                               root = rotateLeft(root, xpp);
                           }
                       }
                   }
               }
           }
       }
   		// 删除平衡
       static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root,
                                                  TreeNode<K,V> x) {
           for (TreeNode<K,V> xp, xpl, xpr;;) {
               if (x == null || x == root)
                   return root;
               else if ((xp = x.parent) == null) {
                   x.red = false;
                   return x;
               }
               else if (x.red) {
                   x.red = false;
                   return root;
               }
               else if ((xpl = xp.left) == x) {
                   if ((xpr = xp.right) != null && xpr.red) {
                       xpr.red = false;
                       xp.red = true;
                       root = rotateLeft(root, xp);
                       xpr = (xp = x.parent) == null ? null : xp.right;
                   }
                   if (xpr == null)
                       x = xp;
                   else {
                       TreeNode<K,V> sl = xpr.left, sr = xpr.right;
                       if ((sr == null || !sr.red) &&
                           (sl == null || !sl.red)) {
                           xpr.red = true;
                           x = xp;
                       }
                       else {
                           if (sr == null || !sr.red) {
                               if (sl != null)
                                   sl.red = false;
                               xpr.red = true;
                               root = rotateRight(root, xpr);
                               xpr = (xp = x.parent) == null ?
                                   null : xp.right;
                           }
                           if (xpr != null) {
                               xpr.red = (xp == null) ? false : xp.red;
                               if ((sr = xpr.right) != null)
                                   sr.red = false;
                           }
                           if (xp != null) {
                               xp.red = false;
                               root = rotateLeft(root, xp);
                           }
                           x = root;
                       }
                   }
               }
               else { // symmetric
                   if (xpl != null && xpl.red) {
                       xpl.red = false;
                       xp.red = true;
                       root = rotateRight(root, xp);
                       xpl = (xp = x.parent) == null ? null : xp.left;
                   }
                   if (xpl == null)
                       x = xp;
                   else {
                       TreeNode<K,V> sl = xpl.left, sr = xpl.right;
                       if ((sl == null || !sl.red) &&
                           (sr == null || !sr.red)) {
                           xpl.red = true;
                           x = xp;
                       }
                       else {
                           if (sl == null || !sl.red) {
                               if (sr != null)
                                   sr.red = false;
                               xpl.red = true;
                               root = rotateLeft(root, xpl);
                               xpl = (xp = x.parent) == null ?
                                   null : xp.left;
                           }
                           if (xpl != null) {
                               xpl.red = (xp == null) ? false : xp.red;
                               if ((sl = xpl.left) != null)
                                   sl.red = false;
                           }
                           if (xp != null) {
                               xp.red = false;
                               root = rotateRight(root, xp);
                           }
                           x = root;
                       }
                   }
               }
           }
       }
   
       /**
        * Recursive invariant check
        */
       static <K,V> boolean checkInvariants(TreeNode<K,V> t) {
           TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,
               tb = t.prev, tn = (TreeNode<K,V>)t.next;
           if (tb != null && tb.next != t)
               return false;
           if (tn != null && tn.prev != t)
               return false;
           if (tp != null && t != tp.left && t != tp.right)
               return false;
           if (tl != null && (tl.parent != t || tl.hash > t.hash))
               return false;
           if (tr != null && (tr.parent != t || tr.hash < t.hash))
               return false;
           if (t.red && tl != null && tl.red && tr != null && tr.red)
               return false;
           if (tl != null && !checkInvariants(tl))
               return false;
           if (tr != null && !checkInvariants(tr))
               return false;
           return true;
       }
   }
   ```

   

## 主要方法

### 简单看

```java
/**
 * 返回指定key映射的值，如果不存在此Key 就返回null
 * Returns the value to which the specified key is mapped,
 * or {@code null} if this map contains no mapping for the key.
 *
 * <p>More formally, if this map contains a mapping from a key
 * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
 * key.equals(k))}, then this method returns {@code v}; otherwise
 * it returns {@code null}.  (There can be at most one such mapping.)
 *
 * <p>A return value of {@code null} does not <i>necessarily</i>
 * indicate that the map contains no mapping for the key; it's also
 * possible that the map explicitly maps the key to {@code null}.
 * The {@link #containsKey containsKey} operation may be used to
 * distinguish these two cases.
 *
 * @see #put(Object, Object)
 */
public V get(Object key) {
    Node<K,V> e;
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}

/**
 * Implements Map.get and related methods.
 *
 * @param hash hash for key
 * @param key the key
 * @return the node, or null if none
 */
final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (first = tab[(n - 1) & hash]) != null) {
      	// 为什么要check头节点 因为 map一开始并不做初始化？
        if (first.hash == hash && // always check first node
            ((k = first.key) == key || (key != null && key.equals(k))))
            return first;
        if ((e = first.next) != null) {
          	// 第一个元素是树那么就按照树的方式查找
            if (first instanceof TreeNode)
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
          	// 否则遍历整个链表查询
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



```java
/**
 * 在这个map中将键值关联，如果map之前存在此key，老的值会被替代
 * Associates the specified value with the specified key in this map.
 * If the map previously contained a mapping for the key, the old
 * value is replaced.
 *
 * @param key key with which the specified value is to be associated
 * @param value value to be associated with the specified key
 * @return the previous value associated with <tt>key</tt>, or
 *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
 *         (A <tt>null</tt> return can also indicate that the map
 *         previously associated <tt>null</tt> with <tt>key</tt>.)
 */
public V put(K key, V value) {
  	// 此方法上面有分析
    return putVal(hash(key), key, value, false, true);
}
```

### 仔细读


#### 问题
扩容、解决hash冲突、冲突优化是如何进行的

如何以数组存储键值对
数组满了怎么扩容
hash冲突时以链表大于阈值后，链表如何树化，左旋、右旋如何实现
链表过大时会树化，树过大时怎么处理，会扩容吗
如何使hash更分散不易冲突呢

如何遍历entry

函数式编程如何实现

#### 简介
键值对形式存储，通过负载因子控制Node[]数组扩容，hash冲突时node节点以链表存在，一个node节点大于阀值会树化

#### 继承体系
![image](https://raw.githubusercontent.com/sljie1988/image/master/jdkSourceStudy/HashMapInheritSystem.png?token=AKPZOCLMNKO5IMH7VYVRQSK57FSVQ)
#### 类结构说明
static class Node<K,V> implements Map.Entry<K,V>
static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V>
final class EntrySet extends AbstractSet<Map.Entry<K,V>>
final class EntryIterator extends HashIterator implements Iterator<Map.Entry<K,V>>
static final class EntrySpliterator<K,V> extends HashMapSpliterator<K,V> implements Spliterator<Map.Entry<K,V>>

#### 源码解析
##### 属性
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
static final int MAXIMUM_CAPACITY = 1 << 30;
static final float DEFAULT_LOAD_FACTOR = 0.75f;
static final int TREEIFY_THRESHOLD = 8;
static final int UNTREEIFY_THRESHOLD = 6;
static final int MIN_TREEIFY_CAPACITY = 64;

transient Node<K,V>[] table;
transient Set<Map.Entry<K,V>> entrySet;
transient int size;
transient int modCount;


##### 构造方法
public HashMap(int initialCapacity, float loadFactor)
public HashMap(int initialCapacity)
public HashMap()
public HashMap(Map<? extends K, ? extends V> m)

##### 主要方法
final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict)

public V put(K key, V value)
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,boolean evict)
final Node<K,V>[] resize()
final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab, int h, K k, V v)
final void treeifyBin(Node<K,V>[] tab, int hash)
final void treeify(Node<K,V>[] tab)
static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root, TreeNode<K,V> x)
static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root, TreeNode<K,V> p)
static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root, TreeNode<K,V> p)

final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit)

##### 其他方法
public V get(Object key)
final Node<K,V> getNode(int hash, Object key)

public V remove(Object key)
final Node<K,V> removeNode(int hash, Object key, Object value, boolean matchValue, boolean movable)
final void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab, boolean movable)
final Node<K,V> untreeify(HashMap<K,V> map)
static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root, TreeNode<K,V> x)

public boolean containsKey(Object key)
public boolean containsValue(Object value)
public void clear()

public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)
public void forEach(BiConsumer<? super K, ? super V> action)
public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function)

public Object clone()
private void writeObject(java.io.ObjectOutputStream s)
private void readObject(java.io.ObjectInputStream s)

-----------
static class Node<K,V> implements Map.Entry<K,V>
final int hash;
final K key;
V value;
Node<K,V> next;

-----------
static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V>
TreeNode<K,V> parent;  // red-black tree links
TreeNode<K,V> left;
TreeNode<K,V> right;
TreeNode<K,V> prev;    // needed to unlink next upon deletion
boolean red;

static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root)
final TreeNode<K,V> getTreeNode(int h, Object k)
final void treeify(Node<K,V>[] tab)
final Node<K,V> untreeify(HashMap<K,V> map)
final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab, int h, K k, V v)
final void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab, boolean movable)
final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit)

static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root, TreeNode<K,V> p)
static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root, TreeNode<K,V> p)
static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root, TreeNode<K,V> x)
static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root, TreeNode<K,V> x)

static <K,V> boolean checkInvariants(TreeNode<K,V> t)

-----------
final class EntrySet extends AbstractSet<Map.Entry<K,V>>
final class KeySet extends AbstractSet<K>
final class Values extends AbstractCollection<V>

public Set<Map.Entry<K,V>> entrySet()
public Set<K> keySet()
public Collection<V> values()

public final boolean contains(Object o)
public final boolean remove(Object o)
public final void forEach(Consumer<? super Map.Entry<K,V>> action)
public final Spliterator<Map.Entry<K,V>> spliterator()

-----------
abstract class HashIterator
final class EntryIterator extends HashIterator implements Iterator<Map.Entry<K,V>>
final class KeyIterator extends HashIterator implements Iterator<K>
final class ValueIterator extends HashIterator implements Iterator<V>

public final Iterator<Map.Entry<K,V>> iterator()
public final boolean hasNext()
public final Map.Entry<K,V> next()
final Node<K,V> nextNode()
public final void remove()

-----------
static class HashMapSpliterator<K,V>
static final class EntrySpliterator<K,V> extends HashMapSpliterator<K,V> implements Spliterator<Map.Entry<K,V>>
static final class KeySpliterator<K,V> extends HashMapSpliterator<K,V> implements Spliterator<Map.Entry<K,V>>
static final class ValueSpliterator<K,V> extends HashMapSpliterator<K,V> implements Spliterator<Map.Entry<K,V>>

final int getFence()
public final long estimateSize()

public EntrySpliterator<K,V> trySplit()
public void forEachRemaining(Consumer<? super Map.Entry<K,V>> action)
public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action)
public int characteristics()

#### 总结
hashMap存储过程，将数据存入节点为key value的节点桶组中，hash冲突时，以链表方式存储，当链表长度大于8且桶组长度大于64时，
进行树化，以查找二叉树存储，通过左右旋与红黑树实现树平衡。
hash值分散，对象地址的hash值与自身高位16位异或实现。
扩容，以原容量2倍方式扩容，老桶组给新桶组复制时，桶组直接复制，hash冲突的链表和树化后的数据会根据情况切割成两小份复制。


# LinkedHashMap
LinkedHashMap结合了HashMap查询时间复杂度为O(1)和LinkedList增删时间复杂度为O(1)的特性，使其相比起LinkedList的随机访问更加高效，并且相比起HashMap拥有了有序的特性，但由于每一次对元素操作之后需要同时维护HashMap和LinkedList中的存储，性能上相较于HashMap稍慢。  

LinkedHashMap也可以用来实现LRU缓存策略，且只需要将accessOrder设置为true即可，若需要设置缓存淘汰策略，重写removeEldestEntry()方法即可。  

```java
public class LinkedHashMap<K,V> extends HashMap<K,V> implements Map<K,V>
 ```
继承了HashMap，实现了Map接口，拥有HashMap的所有特性，并且额外增加了一定按顺序访问的特性

### 属性
```java
    /**
     * HashMap.Node subclass for normal LinkedHashMap entries.
     */
    static class Entry<K,V> extends HashMap.Node<K,V> {
        Entry<K,V> before, after;
        Entry(int hash, K key, V value, Node<K,V> next) {
            super(hash, key, value, next);
        }
    }
    /**
     * 双向链表的头节点, 旧数据存在头节点
     */
    transient LinkedHashMap.Entry<K,V> head;

    /**
     * 双向链表的尾节点, 新数据存在尾结点
     */
    transient LinkedHashMap.Entry<K,V> tail;

    /**
     * 标识是否按访问顺序排序
     * true: 按照访问顺序存储元素
     * false: 按照插入顺序存储元素
     */
    final boolean accessOrder;
```

### 链表节点
```java
    /**
     * 位于LinkedHashMap中的Node节点, 也就是LinkedList + HashMap中属于链表的节点
     */
    static class Entry<K,V> extends HashMap.Node<K,V> {
        Entry<K,V> before, after;
        Entry(int hash, K key, V value, Node<K,V> next) {
            super(hash, key, value, next);
        }
    }
```

### 构造方法
```java
    /**
     * 1. 指定初始容量与扩容因子的构造方法
     * 内部是通过HashMap.HashMap(int initialCapacity, float loadFactor)这个构造方法创建的map
     * 并且默认按照元素插入顺序进行排序
     */
    public LinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        accessOrder = false;
    }

    /**
     * 2. 指定初始容量的构造方法
     * 内部是通过HashMap.HashMap(int initialCapacity)这个构造方法创建的默认扩容因子为0.75的map
     * 并且默认按照元素插入顺序进行排序
     */
    public LinkedHashMap(int initialCapacity) {
        super(initialCapacity);
        accessOrder = false;
    }

    /**
     * 3. 默认构造方法
     * 内部是通过HashMap.HashMap()这个构造方法创建的默认初始容量为16且默认扩容因子为0.75的map
     * 并且默认按照元素插入顺序进行排序
     */
    public LinkedHashMap() {
        super();
        accessOrder = false;
    }

    /**
     * 4. 通过传入一个Map进行构建LinkedHashMap, 底层调用了HashMap(Map m)
     * 并且默认按照元素插入顺序进行排序
     */
    public LinkedHashMap(Map<? extends K, ? extends V> m) {
        super();
        accessOrder = false;
        putMapEntries(m, false);
    }

    /**
     * 5. 通过指定初始容量, 扩容因子, 插入顺序进行构建LinkedHashMap
     * 底层先通过HashMap(initialCapacity, loadFactor)这个构造方法创建map, 并指定排序顺序
     * 这个构造方法也是实现LRU缓存的关键
     */
    public LinkedHashMap(int initialCapacity,
                         float loadFactor,
                         boolean accessOrder) {
        super(initialCapacity, loadFactor);
        this.accessOrder = accessOrder;
    }
```

### afterNodeInsertion(boolean evict) 
指定LinkedHashMap在完成put操作之后还需做什么，这个方法在HashMap中putVal()方法被调用，但是实现为空
```java
   void afterNodeInsertion(boolean evict) { // possibly remove eldest
        LinkedHashMap.Entry<K,V> first;
        //如果evict = true, 并且双向链表的头节点不为空, 且确定移除最老的元素
        if (evict && (first = head) != null && removeEldestEntry(first)) {
            K key = first.key;
            //调用removeNode方法移除头节点
            //在removeNode方法内部移除节点之后会调用afterNodeRemoval()方法用于修改双向链表
            removeNode(hash(key), key, null, false, true);
        }
    }

	//是否移除最老的元素, 默认为false
	protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return false;
    }
```

### afterNodeAcces(Node e)
指定LinkedHashMap在完成访问操作之后还需做什么，这个方法在HashMap中调用put()方法时，更新相同key节点的value时有调用，但实现也为空。在LinkedHashMap中调用put()、get()方法时会用到，若指定为true，则调用这个方法把最近访问过的节点移动到双端链表末尾。  

（1）若指定accessOrder = true，且访问的节点不是末尾节点
（2）双向链表中移除该结点并再次添加到链表末尾
```java
    void afterNodeAccess(Node<K,V> e) { // move node to last
        LinkedHashMap.Entry<K,V> last;
        //若指定accessOrder = true, 也就是需要按访问顺序进行排序, 且访问的不是末尾节点
        if (accessOrder && (last = tail) != e) {
            LinkedHashMap.Entry<K,V> p =
                (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
            p.after = null;
            //将节点p从双端链表中删除
            if (b == null)
                head = a;
            else
                b.after = a;
            if (a != null)
                a.before = b;
            else
                last = b;
            //把节点p放在双端链表末尾
            if (last == null)
                head = p;
            else {
                p.before = last;
                last.after = p;
            }
            //尾结点等于p
            tail = p;
            ++modCount;
        }
    }
```

### afterNodeRemoval(Node e)  
在HashMap中将该节点删除之后，在双端链表也将该节点进行删除
```java
void afterNodeRemoval(Node<K,V> e) { // unlink
        LinkedHashMap.Entry<K,V> p =
            (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
        p.before = p.after = null;
        if (b == null)
            head = a;
        else
            b.after = a;
        if (a == null)
            tail = b;
        else
            a.before = b;
    }
```

### get(Object key) 通过传入key获取指定节点的value
（1）调用HashMap的getNode方法检索节点e  
（2）若节点e不为空，且指定按访问顺序排序，更新该节点到链表末尾  
```java
    public V get(Object key) {
        Node<K,V> e;
        //若未查找到对应节点 return null
        if ((e = getNode(hash(key), key)) == null)
            return null;
        //若找到了对应节点, 且accessOrder = true
        if (accessOrder)
        	//更新该节点为最近访问节点, 移动到双端链表末尾
            afterNodeAccess(e);
        return e.value;
    }
```

### 总结：
（1）LinkedHashMap继承自HashMap，具有HashMap的所有特性  
（2）LinkedHashMap内部维护了一个双端链表存储所有的元素  
（3）若accessOrder = false，则按插入元素的顺序进行排序  
（4）若accessOrder = true，则按访问元素的顺序进行排序  
（5）默认的LinkedHashMap并不会移除旧元素，如果需要移除达到某个条件的最久未使用的旧元素，则需要重写removeEldestEntry()方法设置淘汰策略 


### LRU基于LinkedHashMap的实现
```java
public class LRUCache extends LinkedHashMap<Integer, Integer> {
    int capacity;

    public LRUCache(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    public int get(int key) {
        return super.getOrDefault(key, -1);
    }

    public void put(int key, int value) {
        super.put(key, value);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
        return size() > capacity;
    }
}
```

### LRU基于HashMap + LinkedList的实现
```java
class LRUCache {
    HashMap<Integer, DoubleLinkedList.ListNode> map;
    DoubleLinkedList list;
    int capacity;

    public LRUCache(int capacity) {
        this.map = new HashMap<>();
        this.list = new DoubleLinkedList();
        this.capacity = capacity;
    }

    public int get(int key) {
        //if exist, get and update
        if (map.containsKey(key)) {
            int v = map.get(key).value;
            put(key, v);
            return v;
        }
        return -1;    
    }

    public void put(int key, int value) {
        DoubleLinkedList.ListNode x = new DoubleLinkedList.ListNode(key, value);
        //if key is already exist in cache
        if (map.containsKey(key)) {
            //update cache
            DoubleLinkedList.ListNode temp = map.get(key);
            list.remove(temp);
            list.addEnd(x);
            map.put(key, x);
        } else {
            if (list.size >= capacity) {
                //remove oldest, then add
                DoubleLinkedList.ListNode rmv = list.removeFirst();
                map.remove(rmv.key);
            }
            list.addEnd(x);
            map.put(key, x);
        }
    }
}

class DoubleLinkedList {
    private ListNode head;
    private ListNode tail;
    int size;

    public DoubleLinkedList() {
        this.head = new ListNode(0, 0);
        this.tail = new ListNode(0, 0);
        this.head.next = tail;
        this.tail.prev = head;
        this.size = 0;

    }

    public void remove(ListNode node) {
        if (node == head || node == tail) throw new RuntimeException("node is can't to be head or tail");
        ListNode prev = node.prev;
        ListNode next = node.next;
        prev.next = next;
        next.prev = prev;
        size--;
    }


    public ListNode removeFirst() {
        if (head.next != null) {
            ListNode deleteHead = head.next;
            remove(deleteHead);
            return deleteHead;
        }
        return null;
    }

    public void addEnd(ListNode node) {
        if (node == null) throw new RuntimeException("node is can't to be null");
        ListNode tailPrev = tail.prev;
        tailPrev.next = node;
        node.prev = tailPrev;
        node.next = tail;
        tail.prev = node;
        size++;
    }

    static class ListNode {
        int key;
        int value;
        ListNode prev;
        ListNode next;

        public ListNode(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }
}
```
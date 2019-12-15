
import java.io.IOError;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;


/**
 * 【源码链接】
 * Source for java.util.ArrayList:
 * http://developer.classpath.org/doc/java/util/LinkedList-source.html
 * 
 * */

 /**
 * 【简介】
 * LinkedList实现了List接口，除了List相关方法外，该类还提供了在O（1）时间内访问第一个和最后一个List元素的权限，
 * 以便轻松创建堆栈、队列或双端队列（deque）。LinkedList是双向链表的，从最靠近元素的一端开始遍历给定的索引。
 * 
 * LinkedList不是线程同步的（non-synchronized），如果需要多线程访问可以这样做：
 * List list = Collections.synchronizedList(new LinkedList(...))
 * 
 * LinkedList在队列首尾添加、删除元素的时间复杂度为O(1)，中间添加、删除为O(n)，不支持随机访问。
 * */

 public class LinkedList<T> extends AbstractSequentialList<T> implements List<T>,Deque<T>,Cloneable,Serializable{
    
    private static final long serialVersionUID = 876323262645176354L;

    //LinkedList第一个元素
    transient Entry<T> first;

    //LinkedList最后一个元素
    transient Entry<T> last;

    //LinkedList的长度
    transient int size = 0;

    //新建内部类来表示列表中的项,包含单个元素。
    private static final class Entry<T>{
        //列表元素
        T data;
        //后继指针
        Entry<T> next;
        //前继指针
        Entry<T> previous;

        Entry(T data){
            this.data = data;
        }
    }

    //获取LinkedList位置下标为n的元素，顺序or倒序
    Entry<T> getEntry(int n){
        Entry<T> e;
        if(n < size/2){
            e = first;
            while(n-- > 0){
                e = e.next;
            }
        }else{
            e = last;
            while(++n < size){
                e = e.previous;
            }
        }
        return e;
    }

    //从列表中删除条目。这将调整大小并适当处理“first”和“last”
    //modCount字段表示list结构上被修改的次数.
    void removeEntry(Entry<T> e){
        modCount++;
        size--;
        if(size == 0){
            first = last = null;
        }else {
            if(e == first){
                first = e.next;
                e.next.previous = null;
            }else if(e == last){
                last = e.previous;
                e.previous.next = null;
            }else{
                e.next.previous = e.previous;
                e.previous.next = e.next;
            }
        }
    }

    //检查索引是否在可能的元素范围内
    private void checkBoundsInclusive(int index){
        if(index < 0 || index > size){
            throw new IndexOutOfBoundsException("Index:"+index+",Size:"+size);
        }
    }

    //检查索引是否在现有元素的范围内。
    private void checkBoundsExclusive(int index){
        if(index < 0 || index >= size){
            throw new IndexOutOfBoundsException("Index:"+index+",Size:"+size);
        }
    }

    //创建一个空的LinkedList
    public LinkedList(){

    }

    //根据给定元素创建一个LinkedList
    public LinkedList(Collection<?extends T> c){
        addAll(c);
    }

    //返回LinkedList第一个元素
    public T getFirst(){
        if(size ==0){
            throw new NoSuchElementException();
        }
        return first.data;
    }

    //返回LinkedList最后一个元素
    public T getLast(){
        if(size == 0){
            throw new NoSuchElementException();
        }
        return last.data;
    }

    //移除并返回LinkedList第一个元素
    public T removeFirst(){
        if(size == 0){
            throw new NoSuchElementException();
        }
        modCount++;
        size--;
        T r = first.data;

        if(first.next != null){
            first.next.previous = null;
        }else{
            last = null;
        }

        first = first.next;
        return r;
    }
    
    //移除并返回LinkedList最后一个元素
    public T removeLast(){
        if(size == 0){
            throw new NoSuchElementException();
        }
        modCount++;
        size--;
        T r = last.data;

        if(last.previous != null){
            last.previous.next = null;
        }else{
            first = null;
        }

        last = last.previous;
        return r;
    }

    //在LinkedList首部插入元素
    public void addFirst(T o){
        Entry<T> e = new Entry<>(o);
        modCount++;
        if(size == 0){
            first = last = e;
        }else{
            e.next = first;
            first.previous = e;
            first = e;
        }
        size++;
    }

    //在LinkedList尾部插入元素
    public void addLast(T o){
        addLastEntry(new Entry<T>(o));
    }
    private void addLastEntry(Entry<T> e) {
        modCount++;
        if(size ==0){
            first = last = e;
        }else{
            e.previous = last;
            last.next = e;
            last = e;
        }
        size++;
    }

    //如果列表包含给定的对象，则返回true
    public boolean contains(Object o){
        Entry<T> e = first;
        while(e != null){
            if(o.equals(e.data)){
                return true;
            }
            e = e.next;
        }
        return false;
    }

    //返回LinkedList的大小
    public int size(){
        return size;
    }

    //在LinkedList尾部添加元素
    public boolean add(T o){
        addLastEntry(new Entry<T>(o));
        return true;
    }

    //删除列表中与给定对象匹配的最低索引处的项
    public boolean remove(Object o){
        Entry<T> e = first;
        while(e != null){
            if(o.equals(e.data)){
                removeEntry(e);
                return true;
            }
            e = e.next;
        }
        return false;
    }

    //按迭代顺序将集合的元素追加到此列表的末尾
    public boolean addAll(Collection<?extends T> c){
        return addAll(size,c);
    }

    //在此列表的给定索引处按迭代顺序插入集合的元素
    public boolean addAll(int index,Collection<?extends T> c){
        checkBoundsInclusive(index);
        int csize = c.size();
        if(csize == 0){
            return false;
        }
        Iterator<?extends T> itr = c.iterator();

        Entry<T> after = null;
        Entry<T> before = null;
        if(index != size){
            after = getEntry(index);
            before = after.previous;
        }else{
            before = last;
        }

        //创建第一个新条目。我们还没有设置从“before”到第一个条目的链接，
        Entry<T> e = new Entry<T>(itr.next());
        e.previous = before;
        Entry<T> prev = e;
        Entry<T> firstNew = e;

        //创建并链接所有剩余条目。
        for(int pos = 1;pos < csize; pos++){
            e = new Entry<T>(itr.next());
            e.previous = prev;
            prev.next = e;
            prev = e;
        }

        //将新的条目链链接到列表中。
        modCount++;
        size += csize;
        prev.next = after;
        if(after != null){
            after.previous = e;
        }else{
            last = e;
        }
        if(before != null){
            before.next = firstNew;
        }else{
            first = firstNew;
        }
        return true;
    }

    //清空LinkedList
    public void clear(){
        if(size > 0 ){
            modCount++;
            first = null;
            last = null;
            size =0;
        }
    }

    //获取元素的下标
    public T get(int index){
        checkBoundsExclusive(index);
        return getEntry(index).data;
    }

    //替换列表中给定位置的元素。
    public T set(int index,T o){
        checkBoundsExclusive(index);
        Entry<T> e = getEntry(index);
        T old = e.data;
        e.data = o;
        return old;
    }

    //在列表中d 给定位置插入元素。
    public void add(int index ,T o){
        checkBoundsInclusive(index);
        Entry<T> e = new Entry<T>(o);
        if(index <size){
            modCount++;
            Entry<T> after = getEntry(index);
            e.next = after;
            e.previous = after.previous;
            if(after.previous == null){
                first = e;
            }else{
                after.previous.next = e;
            }
            after.previous = e;
            size ++;
        }else{
            addLastEntry(e);
        }
    }

    //从列表中删除位于给定位置的元素。
    public T remove(int index){
        checkBoundsExclusive(index);
        Entry<T> e = getEntry(index);
        removeEntry(e);
        return e.data;
    }

    //返回元素位于列表中的第一个索引，或-1
    public int indexOf(Object o){
        int index = 0;
        Entry<T> e = first;
        while(e != null){
            if(o.equals(e.data)){
                return index;
            }
            index ++;
            e = e.next;
        }
        return -1;
    }

    //返回元素位于列表中的最后一个索引，或-1。
    public int lastIndexOf(Object o){
        int index = size -1;
        Entry<T> e = last;
        while(e != null){
            if(o.equals(e.data)){
                return index;
            }
            index --;
            e = e.previous;
        }
        return -1;
    }

    //从给定的索引开始，获取此列表上的ListIterator。此方法返回的ListIterator支持add、remove和set方法。
    public ListIterator<T> listIterator(int index){
        checkBoundsInclusive(index);
        return new LinkedListItr<T>(index);
    }
    //列表上的列表迭代器。这个类跟踪它在列表中的位置以及它所处的两个列表项。
    private final class LinkedListItr<I> implements ListIterator<I>{

        private int konwnMod = modCount;
        private Entry<I> next;
        private Entry<I> previous;
        private Entry<I> lastReturned;
        private int position;

        //初始化迭代器
        public LinkedListItr(int index) {
            if(index == size){
                next = null;
                previous = (Entry<I>) last;
            }else{
                next = (Entry<I>)getEntry(index);
                previous = next.previous;
            }
            position = index;
        }

        //检查迭代器的一致性。
        private void checkMod(){
            if(konwnMod != modCount)
                throw new ConcurrentModificationException();
        }

        //返回下一个元素的下标
        public int nextIndex(){
            return position;
        }

        //返回前一个元素的下标
        public int previousIndex(){
            return position-1;
        }

        //如果通过下一个元素存在更多元素，则返回true。
        public boolean hasNext(){
            return (next != null);
        }

        //如果先前存在更多元素，则返回true。
        public boolean hasPrevious(){
            return (previous != null);
        }

        //返回下一个元素
        public I next(){
            checkMod();
            if(next == null)
                throw new NoSuchElementException();
            position++;
            lastReturned = previous = next;
            next = lastReturned.next;
            return lastReturned.data;
        }

        //返回前一个元素
        public I previous(){
            checkMod();
            if(previous == null)
                throw new NoSuchElementException();
            position--;
            lastReturned = next = previous;
            previous = lastReturned.previous;
            return lastReturned.data;
        }

        //从列表中删除最近返回的元素
        public void remove(){
            checkMod();
            if(lastReturned == null){
                throw new IllegalStateException();
            }
            if(lastReturned == previous){
                position--;
            }

            next = lastReturned.next;
            previous  = lastReturned.previous;
            removeEntry((Entry<T>)lastReturned);
            
            konwnMod++;
            lastReturned = null;
        }
        
        //在上一个和下一个之间添加元素，然后前进到下一个。
        public void add(I o){
            checkMod();
            modCount++;
            konwnMod++;
            size++;
            position++;
            Entry<I> e = new Entry<I>(o);
            e.previous = previous;
            e.next = next;

            if(previous != null)
                previous.next = e;
            else
                first = (Entry<T>)e;

            if(next != null){
                next.previous = e;
            }else{
                last = (Entry<T>) e;
            }
            previous = e;
            lastReturned = null;
        }

        //更改最近返回的元素的内容。
        public void set(I o){
            checkMod();
            if(lastReturned == null){
                throw new IllegalStateException();
            }
            lastReturned.data = o;
        }
    }



    //LinkedList的浅拷贝
    public Object clone(){
        LinkedList<T> copy = null;
        try{
            copy = (LinkedList<T>) super.clone();
        }catch(CloneNotSupportedException ex){

        }
        copy.clear();
        copy.addAll(this);
        return copy;
    }

    //返回按顺序包含列表元素的数组。
    public Object[] toArray(){
        Object[] array = new Object[size];
        Entry<T> e = first;
        for(int i=0;i<size;i++){
            array[i] = e.data;
            e = e.next;
        }
        return array;
    }

    //返回其组件类型为传入数组的运行时组件类型的数组。返回的数组将填充此LinkedList中的所有元素。
    public <S> S[] toArray(S[] a){
        if(a.length < size){
            a = (S[])Array.newInstance(a.getClass().getComponentType(), size);
        }else if(a.length > size){
            a[size] = null;
        }
        Entry<T> e = first;
        for(int i=0;i<size;i++){
            a[i] = (S) e.data;
            e = e.next;
        }
        return a;
    }

    //将指定的元素添加到列表的末尾。
    public boolean offer(T value){
        return add(value);
    }

    //返回列表的第一个元素而不删除它。
    public T element(){
        return getFirst();
    }

    //返回列表的第一个元素而不删除它。空值返回null
    public T peek(){
        if(size == 0){
            return null;
        }
        return getFirst();
    }

    //删除并返回列表的第一个元素。空值返回null
    public T poll(){
        if(size == 0){
            return null;
        }
        return removeFirst();
    }

    //删除并返回列表的第一个元素。
    public T remove(){
        return removeFirst();
    }

    //将此对象序列化为给定流。
    private void writeObject(ObjectOutputStream s)throws IOException{
        s.defaultWriteObject();
        s.writeInt(size);
        Entry<T> e = first;
        while(e != null){
            s.writeObject(e.data);
            e = e.next;
        }
    }

    //从给定流反序列化此对象。
    private void readObject(ObjectInputStream s) throws IOException,ClassNotFoundException{
        s.defaultReadObject();
        int i = s.readInt();
        while(--i >= 0){
            addLastEntry(new Entry<T>((T) s.readObject()));
        }
    }

    //按相反的顺序获取此列表上的迭代器。
    public Iterator<T> descendIterator(){
        return new Iterator<T>() {
            private int konwnMod = modCount;
            private Entry<T> next =last;
            private Entry<T> lastReturned;
            private int position = size() - 1;
            //检查迭代过程中从其他地方对列表所做的修改。
            private void checkMod(){
                if(konwnMod != modCount)
                    throw new ConcurrentModificationException();
            }
            public boolean hasNext(){
                return next != null;
            }
            public T next(){
                checkMod();
                if(next == null)
                    throw new NoSuchElementException();
                --position;
                lastReturned = next;
                next = lastReturned.previous;
                return lastReturned.data;
            }
            public void remove(){
                checkMod();
                if(lastReturned == null)
                    throw new IllegalStateException();
                removeEntry(lastReturned);
                lastReturned = null;
                ++konwnMod;
            }
        };
    }

    //在列表的前面插入指定的元素。
    public boolean offerFirst(T value){
        addFirst(value);
        return true;
    }

    //在列表的后面插入指定的元素。
    public boolean offerLast(T value){
        return add(value);
    }

    //返回列表的第一个元素而不删除
    public T peekFirst(){
        return peek();
    }

    //返回列表的最后一个元素而不删除
    public T peekLast(){
        if(size == 0){
            return null;
        }
        return getLast();
    }

    //删除并返回列表的第一个元素
    public T pollFirst(){
        return poll();
    }

    //删除并返回列表的最后一个元素
    public T pollLast(){
        if(size == 0){
            return null;
        }
        return removeLast();
    }

    //通过移除并返回列表中的第一个元素，从堆栈中弹出一个元素。
    public T pop(){
        return removeFirst();
    }

    //通过将元素添加到列表的前面，将其推送到堆栈上。
    public void push(T value) {
        addFirst(value);
    }

    //从头到尾遍历列表时，从列表中删除指定元素的第一个匹配项。
    public boolean removeFirstOccurrence(Object o){
        return remove(o);
    }

    //从头到尾遍历列表时，从列表中删除指定元素的最后一个匹配项。
    public boolean removeLastOccurrence(Object o){
        Entry<T> e = last;
        while(e!=null){
            if(o.equals(e.data)){
                removeEntry(e);
                return true;
            }
            e = e.previous;
        }
        return false;
    }
 }
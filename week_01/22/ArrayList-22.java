import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

/**
 * 【源码链接】
 * Source for java.util.ArrayList:
 * http://developer.classpath.org/doc/java/util/ArrayList-source.html
 * 
 * */

/**
 * 【简介】
 * ArrayList底层使用的是数组来实现List接口，提供了所有可选的List操作并且允许null值。
 * 元素的随机访问是常数时间O(1)，在列表中间添加或者删除元素的时间复杂度是O(n)的。
 * 每个List都有一个容量，当达到最大容量时会自动增加自身的容量。
 * 我们可以通过ensureCapacity和trimToSize来确保容量大小，避免重新分配或浪费内存。
 * 
 * ArrayList不是synchronized的，如果需要多线程访问，可以这样做：
 * List list = Collections.synchronizedList(new ArrayList(...))
 * 
 * 以下就主要方法进行解析说明:
 * **/

 public class ArrayList<E> extends AbstractList<E> implements List<E>,RandomAccess,Cloneable,Serializable{
     
    private static final long serialVersionUID = 8683452581122892189L;
    
    //新建ArrayList的默认容量大小
    private static final int DEFAULT_CAPACITY = 10;
    
    //ArrayList的元素个数
    private int size;

    //存储数据的数组
    private transient E[] data;

    //根据容量大小来构建ArrayList
    public ArrayList(int capacity){
        if(capacity < 0){
            throw new IllegalArgumentException();
        }
        data = (E[]) new Object[capacity];
    }

    //默认容量大小来构建ArrayList
    public ArrayList(){
        this(DEFAULT_CAPACITY);
    }

    //根据给定元素来构建ArrayList
    public ArrayList(Collection<?extends E>c){
        this((int) (c.size() * 1.1f));
        addAll(c);
    }

    //修改size使得等于ArrayList实际大小
    public void trimToSize(){
        if(size != data.length){
            E[] newData = (E[]) new Object[size]; 
            System.arraycopy(data, 0, newData, 0, size);
            data = newData;
        }
    }

    //如果ArrayList容量不足以存储元素，则自动扩展到length*2
    public void ensureCapacity(int minCapacity){
        int current = data.length;
        if(minCapacity > current){
            E[] newData = (E[]) new Object[Math.max(current*2, minCapacity)];
            System.arraycopy(data, 0, newData, 0, size);
            data = newData;
        }
    }

    //返回List的元素个数
    public int size(){
        return size;
    }

    //判断List是否为空
    public boolean isEmpty(){
        return size == 0;
    }

    //判断element是否在ArrayList中
    public boolean contains(Object e){
        return indexOf(e) != -1;
    }

    //判断element在ArrayList中首次出现的最低位置索引，否则返回-1
    public int indexOf(Object e){
        for(int i = 0; i < size; i++){
            if(e.equals(data[i])){
                return i;
            }
        }
        return -1;
    }

    //判断element在ArrayList中首次出现的最高位置索引，否则返回-1
    public int lastIndexOf(Object e){
        for(int i = size-1; i > 0; i--){
            if(e.equals(data[i])){
                return i;
            }
        }
        return -1;
    }

    //ArrayList的浅拷贝
    public Object clone(){
        ArrayList<E> clone = null;
        try{
            clone = (ArrayList<E>) super.clone();
            clone.data = (E[]) data.clone();
        }catch(CloneNotSupportedException e){

        }
        return clone;
    }

    //返回一个独立的数组，存储ArrayList的所有元素
    public Object[] toArray(){
        E[] array = (E[]) new Object[size];
        System.arraycopy(data, 0, array, 0, size);
        return array;
    }

    //返回一个运行时传入数组类型的独立数组，存储ArrayList的所有元素
    //如果存储数组的size太小，则扩展为目标类型T的大小
    public <T> T[] toArray(T[] a){
        if(a.length < size){
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        }else if(a.length > size){
            a[size] = null;
        }
        System.arraycopy(data, 0, a, 0, size);
        return a;
    }

    //检查索引是否在可能的元素范围内
    private void checkBoundInclusive(int index) {
        if(index > size){
            throw new IndexOutOfBoundsException("Index:" + index + ",Size:" + size );
        }
    }

    //检查索引是否在现有元素的范围内。
    private void checkBoundExclusive(int index) {
        if(index >= size){
            throw new IndexOutOfBoundsException("Index:" + index + ",Size:" + size );
        }
    }

    //检索用户提供的索引处的元素
    public E get(int index){
        checkBoundExclusive(index);
        return data[index];
    }

    //给特定下标元素进行赋值,返回以前位于指定索引处的元素
    public E set(int index, E e){
        checkBoundExclusive(index);
        E result = data[index];
        data[index] = e;
        return result;
    }

    //在ArrayList的尾部添加元素:如果已满，则size+1；
    //modCount字段表示list结构上被修改的次数.
    public boolean add(E e){
        modCount++;
        if(size == data.length){
            ensureCapacity(size + 1);
        }
        data[size++] = e;
        return true;
    }

    //根据索引下标位置添加元素：如果已满，则size+1；
    //如果插入位置不是尾部，将index后面元素往后移动一位，再插入元素于index
    public void add(int index , Collection<? extends E> c) {
        checkBoundExclusive(index);
        modCount++;
        if (size == data.length) {
            ensureCapacity(size + 1);
        }
        if (index != size) {
            System.arraycopy(data, index, data, index + 1, size - index);
        }
        data[index] = c;
        size++;
    }

    //根据索引下标位置移除元素
    public E remove(int index){
        checkBoundExclusive(index);
        E r = data[index];
        modCount++;
        if(index != --size){
            System.arraycopy(data, index, data, index + 1, size - index);
        }
        data[size] = null;
        return r;
    }

    //清空ArrayList
    public void clear(){
        if(size > 0 ){
            modCount++;
            Arrays.fill(data, 0, size, null);
            size = 0 ;
        }
    }

    //将提供的集合中的每个元素添加到此列表
    public boolean addAll(Collection<? extends E>c){
        return addAll(size, c);
    }

    //将提供的集合中的每个元素添加到此列表index开始的位置:先将index后面元素移动csize个位置，然后插入
    public boolean addAll(int index,Collection<?extends E>c){
        checkBoundExclusive(index);
        Iterator<?extends E> itr = c.iterator();
        int csize = c.size();

        modCount++;
        if(csize+size > data.length){
            ensureCapacity(size + csize);
        }
        //移动原列表元素
        int end = index + csize;
        if(size > 0 && index != size){
            System.arraycopy(data, index, data, end, size - index);
        }
        size += csize;
        //添加新元素
        for(;index < end;index++){
            data[index] = itr.next();
        }
        return csize>0;
    }

    //移除在某个范围间隔的列表元素:将toIndex后面的元素往前移动（size - toIndex）位
    protected void removeRange(int fromIndex, int toIndex){
        int change = toIndex - fromIndex;
        if(change > 0){
            modCount++;
            System.arraycopy(data, toIndex, data, fromIndex, size - toIndex);
            size -= change;
        }
        else if(change < 0){
            throw new IndexOutOfBoundsException();
        }
    }

    //从此列表中删除给定集合中包含的所有元素
    //判断元素存在,如【a,b,c,d】中存在【b】,返回下标i=1，将【c,d】前移获得【a,c,d】
    boolean removeAllInternal(Collection<?>c){
        int i,j;
        for(i=0;i<size;i++){
            if(c.contains(data[i])){
                break;
            }
        }
        if(i == size){
            return false;
        }

        modCount++;
        for(j = i++; i < size; i++){
            if(!c.contains(data[i])){
                data[j++] = data[i];
            }
        }
        size -= i-j;
        return true;
    }

    //在此向量中仅保留给定集合中包含的元素。
    boolean retainAllInternal(Collection<?>c){
        int i,j;
        for(i=0;i<size;i++){
            if(!c.contains(data[i])){
                break;
            }
        }
        if(i == size){
            return false;
        }

        modCount++;
        for(j = i++; i < size; i++){
            if(c.contains(data[i])){
                data[j++] = data[i];
            }
        }
        size -= i-j;
        return true;
    }

    //将此对象序列化为给定流。
    private void writeObject(ObjectOutputStream s) throws IOException{
    s.defaultWriteObject();
    int len = data.length;
    s.writeInt(len);
    for (int i = 0; i < size; i++)
        s.writeObject(data[i]);
    }

    //从给定流反序列化此对象
    private void readObject(ObjectInputStream s)throws IOException, ClassNotFoundException{
     s.defaultReadObject();
     int capacity = s.readInt();
     data = (E[]) new Object[capacity];
     for (int i = 0; i < size; i++)
       data[i] = (E) s.readObject();
    }

 }
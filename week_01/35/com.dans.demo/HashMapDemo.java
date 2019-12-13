import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

/**
 * @ProjectName: JavaStudy
 * @Package: com.dans.demo
 * @ClassName: HashMapDemo
 * @Author: dans
 * @Description: demo
 * @Date: 2019/12/13 21:30
 */
public class HashMapDemo {

    public static void main(String[] arg0) {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, 1);
        map.put(1, 2);
        map.put(2, 3);
        map.put(3, 4);

        //HashMap中modCount和expectedModCount不相等，则会抛出异常
        //遍历时禁止执行有modCount++相关的操作

        //1.8之后新方法
        map.forEach(new BiConsumer<Integer, Integer>() {
            @Override
            public void accept(Integer key, Integer val) {
                System.out.println("foreach--------->key：" + key + "--------val：" + val);
                //执行remove，抛出错误（ConcurrentModificationException）
                /*if (key == 1) {
                    map.remove(key);
                }*/
            }
        });

        Iterator iterator = map.entrySet().iterator();
        Entry<Integer, Integer> entry = null;
        while (iterator.hasNext()) {
            entry = (Entry<Integer, Integer>) iterator.next();
            if (entry.getKey() == 1)
                iterator.remove();
            System.out.println("iterator------>key：" + entry.getKey() + "--------val：" + entry.getValue());
        }

        for (Entry<Integer, Integer> entry1 : map.entrySet()) {
            //执行remove()抛错（ConcurrentModificationException）
//            map.remove(entry.getKey());
            System.out.println("entry1------->key：" + entry1.getKey() + "--------val：" + entry1.getValue());
        }
    }
}

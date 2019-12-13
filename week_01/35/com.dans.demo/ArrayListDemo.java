import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName: JavaStudy
 * @Package: com.dans.demo
 * @ClassName: ArrayListDemo
 * @Author: dans
 * @Description: demo
 * @Date: 2019/12/12 21:53
 */
public class ArrayListDemo {

    public static void main(String[] arg0) {
        List<String> demo = new ArrayList<>();
        demo.add("0");
        demo.add("1");

        //指定位置插入数据(返回该下标的老数据)
        String oldVal = demo.set(0, "2");
        System.out.println("oldVal: " + oldVal);
        //根据内容移出（返回boolean类型；
        // 若传入为null,则循环找到空值，执行fastRemove方法，根据下标移出，内部调用了System.arraycopy()；
        // 不为空，则循环用equals做比较，再执行fastRemove方法,根据下标移出，内部调用System.arraycopy()）
//        demo.remove("2");
//        指定下标移出（返回移出数据）
        demo.remove(0);

        demo.stream().forEach(str -> {
            System.out.println(str);
        });
    }
}

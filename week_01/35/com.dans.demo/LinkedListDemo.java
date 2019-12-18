package com.dans.demo;
import java.util.LinkedList;
import java.util.List;

/**
 * @ProjectName: JavaStudy
 * @Package: com.dans.demo
 * @ClassName: LinkedListemo
 * @Author: dans
 * @Description: demo
 * @Date: 2019/12/13 23:30
 */
public class LinkedListDemo {

    public static void main(String[] arg0) {
        List<Integer> list = new LinkedList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        list.remove(1);

        list.stream().forEach(i -> {
            System.out.println(i);
        });
    }
}

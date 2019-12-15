ava.util.ArrayList 是非常重要的一个类，在代码中广泛使用，E表示泛型，ArrayList是一个泛型类。 ArrayList相当于C++ 的vector，用于存储对象。与数组不同，数组一旦创建，长度固定，但是ArrayList的长度是动态的，不受限制，可以存储任意多的对象，但是只能存储对象，不能存储原生数据类型例如int。

import java.util.ArrayList; public class TestArrayList { public static void main(String[] args) { // Create a list to store cities ArrayList cityList = new ArrayList();

    // Add some cities in the list
    cityList.add("London");
    // cityList now contains [London]

    cityList.add("Denver");
    // cityList now contains [London, Denver]

    cityList.add("Paris");
    // cityList now contains [London, Denver, Paris]

    cityList.add("Miami");
    // cityList now contains [London, Denver, Paris, Miami]

    cityList.add("Seoul");
    // Contains [London, Denver, Paris, Miami, Seoul]

    cityList.add("Tokyo");
    // Contains [London, Denver, Paris, Miami, Seoul, Tokyo]

    System.out.println("List size? " + cityList.size());  // 6
    System.out.println("Is Miami in the list? " + cityList.contains("Miami"));  // true
    System.out.println("The location of Denver in the list? " + cityList.indexOf("Denver")); // 1 返回索引，如果不在list中，返回-1
    System.out.println("Is the list empty? " + cityList.isEmpty()); // Print false

    // Insert a new city at index 2
    cityList.add(2, "Xian");
    // Contains [London, Denver, Xian, Paris, Miami, Seoul, Tokyo]

    // Remove a city from the list
    cityList.remove("Miami");
    // Contains [London, Denver, Xian, Paris, Seoul, Tokyo]

    // Remove a city at index 1
    cityList.remove(1);
    // Contains [London, Xian, Paris, Seoul, Tokyo]

    // Display the contents in the list
    System.out.println(cityList.toString());

    // Display the contents in the list in reverse order
    for (int i = cityList.size() - 1; i >= 0; i--)
        System.out.print(cityList.get(i) + " ");
    System.out.println();

    // Create a list to store two circles
    ArrayList<CircleFromSimpleGeometricObject> list = new ArrayList<CircleFromSimpleGeometricObject>();

    // Add two circles
    list.add(new CircleFromSimpleGeometricObject(2));
    list.add(new CircleFromSimpleGeometricObject(3));

    // Display the area of the first circle in the list
    System.out.println("The area of the circle? " + list.get(0).getArea());
}
}
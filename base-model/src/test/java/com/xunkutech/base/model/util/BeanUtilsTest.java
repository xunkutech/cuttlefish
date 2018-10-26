package com.xunkutech.base.model.util;

import com.google.gson.reflect.TypeToken;
import com.xunkutech.base.model.JsonSerializable;
import lombok.Setter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class BeanUtilsTest {

    Demo1<String, Integer> demo = new Demo1<>();

    @Before
    public void setUp() throws Exception {

        demo.setA("aaa");
        demo.setB(123);
        demo.setC(1.2);
        demo.setD(1111L);
        demo.setE(321);
        demo.setF(2.1);
        demo.setG(2222L);
        demo.setH(new Date());
        demo.setI(Instant.now());
        demo.setJ(new String[]{"a", "b", "c"});
        demo.setK(new int[]{7, 8, 9});
        demo.setL(Arrays.stream("1.2.3".split("\\.")).map(Integer::valueOf).collect(Collectors.toList()));
        demo.setM(Arrays.stream("4.5.6".split("\\.")).map(Integer::valueOf).collect(Collectors.toList()));

        Map<String, Integer> n = new HashMap<>();
        n.put("a", 1);
        n.put("b", 2);
        n.put("c", 3);
        demo.setN(n);
        demo.setO(n);

        demo.setP(E.X);

    }

    @After
    public void tearDown() throws Exception {


    }

    @Test
    public void deepCopy() throws Exception {
//        System.out.println(demo.printJson());
//
        Map r = JsonUtils.fromJson(demo.printJson(), Map.class);

        Demo1<String, Integer> d4 = JsonUtils.fromJson(tt, new TypeToken<Demo1<String, Integer>>() {
        }.getType());
        BeanUtils.deepCopy(r, d4, new TypeToken<Demo1<String, Integer>>() {
        }.getType(), null);


        System.out.println(JsonUtils.printJson(r));

//        Demo1<String, Integer> demo2  = new Demo1<>();
//        BeanUtils.deepCopy(demo, demo2, new TypeToken<Demo1<String, Integer>>(){}.getType());
//
        Demo1<String, Integer> demo1 = new Demo1<>();
        BeanUtils.deepCopy(demo, demo1, new TypeToken<Demo1<String, Integer>>() {
        }.getType());
        System.out.println("Demo1");
        System.out.println(demo1.printJson());

        Demo2<String, Integer> demo2 = new Demo2<>();
        BeanUtils.deepCopy(demo, demo2, new TypeToken<Demo2<String, Integer>>() {
        }.getType());

        System.out.println("Demo2");
        System.out.println(demo2.printJson());
    }


    String tt = "{\n" +
            "  \"a\": \"aaa\",\n" +
            "  \"b\": 123,\n" +
            "  \"c\": 1.2,\n" +
            "  \"d\": 1111,\n" +
            "  \"e\": 321,\n" +
            "  \"f\": 2.1,\n" +
            "  \"g\": 2222,\n" +
            "  \"h\": \"Oct 25, 2017 1:42:48 PM\",\n" +
            "  \"i\": \"2017-10-25T05:42:48.408Z\",\n" +
            "  \"j\": [\n" +
            "    \"a\",\n" +
            "    \"b\",\n" +
            "    \"c\"\n" +
            "  ],\n" +
            "  \"k\": [\n" +
            "    7,\n" +
            "    8,\n" +
            "    9\n" +
            "  ],\n" +
            "  \"l\": [\n" +
            "    1,\n" +
            "    2,\n" +
            "    3\n" +
            "  ],\n" +
            "  \"m\": [\n" +
            "    4,\n" +
            "    5,\n" +
            "    6\n" +
            "  ],\n" +
            "  \"n\": {\n" +
            "    \"a\": 1,\n" +
            "    \"b\": 2,\n" +
            "    \"c\": 3\n" +
            "  },\n" +
            "  \"o\": {\n" +
            "    \"a\": 1,\n" +
            "    \"b\": 2,\n" +
            "    \"c\": 3\n" +
            "  }\n" +
            "}";
}

@Setter
class Demo1<T, P> implements JsonSerializable {
    String a;
    Integer b;
    Double c;
    Long d;
    int e;
    double f;
    long g;
    Date h;
    Instant i;
    T[] j;
    int[] k;
    List<P> l;
    List<Integer> m;
    Map<T, P> n;
    Map<String, Integer> o;
    E p;
}

@Setter
class Demo2<T, P> implements JsonSerializable {
    String a;
    Integer b;
    Double c;
    Long d;
    int e;
    double f;
    long g;
    Date h;
    Instant i;
    String[] j;
    int[] k;
    List<P> l;
    List<Integer> m;
    Map<T, P> n;
    Map<String, Integer> o;
    E p;
}

enum E {
    X, Y, Z
}

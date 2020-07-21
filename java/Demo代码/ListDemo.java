package com.atLearn;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class ListDemo {
    public static void main(String[] args) {
//        List<String> list = new ArrayList<>();
//        List<String> list = new Vector<>();
//        List<String> list = Collections.synchronizedList(new ArrayList<>());
//        List<String> list = new CopyOnWriteArrayList();
//        for (int i = 0; i < 30; ++i) {
//            new Thread(() -> {
//                list.add(UUID.randomUUID().randomUUID().toString().substring(0, 4));
//                System.out.println(list);
//            }).start();
//        }

//        Set<String> set = new CopyOnWriteArraySet<>();
        Set<String> set = Collections.synchronizedSet(new HashSet<>());
//        Map<String,String> map = Collections.synchronizedMap(new HashMap<>());
         Map<String,String> map = new ConcurrentHashMap();
        for(int i = 0;i<30;i++){
            new Thread(()->{
                set.add(UUID.randomUUID().randomUUID().toString().substring(0, 4));
                System.out.println(Thread.currentThread().getName()+"\t"+set);
            },"thread_set "+i).start();
        }

        for(int i = 0;i<30;i++){
            new Thread(() ->{
                map.put(UUID.randomUUID().randomUUID().toString().substring(0, 4),UUID.randomUUID().randomUUID().toString().substring(0,1));
                System.out.println(Thread.currentThread().getName()+"\t"+map);
            },"thread_map "+i).start();
        }

    }
}

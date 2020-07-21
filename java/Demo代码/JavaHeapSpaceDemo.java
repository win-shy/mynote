package com.atLearn;

import java.util.Random;

/**
 * -Xms5m -Xmx5m -XX:+PrintGCDetails
 */
public class JavaHeapSpaceDemo {
    public static void main(String[] args){
        // java 虚拟机中的内存总量
        long totalMemory = Runtime.getRuntime().totalMemory();

        // Java 虚拟机试图使用的最大内存
        long maxMemory = Runtime.getRuntime().maxMemory();
        System.out.println("total memory: "+totalMemory/(1024* 1024));
        System.out.println("max memory: "+maxMemory/(1024* 1024));

        String  str ="seu";
        while (true){
            str += str + new Random().nextInt(11111111)+new Random().nextInt(22222222);
            str.intern();
        }
    }
}

package com.atLearn;

import java.util.ArrayList;
import java.util.List;

/**
 * -XX:+PrintGCDetails -XX:MaxDirectMemorySize=5m -Xms12m -Xmx12m
 */
public class GCOverheadDemo {
    public static void main(String[] args) {
        int i =0;
        List<String> list = new ArrayList<>();

        try {
            while (true) {
                list.add(String.valueOf(++i).intern());
            }
        }catch (Throwable e) {
            System.out.println("i: " + i);
            e.printStackTrace();
            throw e;
        }
    }
}

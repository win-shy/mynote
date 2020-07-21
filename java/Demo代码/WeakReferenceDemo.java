package com.atLearn;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 *  VM Options: -XX:+PrintGCDetails
 */
public class WeakReferenceDemo {
    public static void main(String[] args) {
        Object obj1 = new Object();
        WeakReference<Object> obj2 = new WeakReference<>(obj1);

        System.out.println(obj1 + "\t"+ obj2.get());
        System.out.println("======================启动垃圾回收=====================");
        obj1 = null;

        System.gc();
        System.out.println(obj1 + "\t"+ obj2.get());

//        Map<String,SoftReference<Bitmap>> imageCache = new HashMap<String,SoftReference<Bitmap>>();
    }
}

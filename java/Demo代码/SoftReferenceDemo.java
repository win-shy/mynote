package com.atLearn;

import java.lang.ref.SoftReference;

/**
 * VM Options: -Xms5m -Xmx5m -XX:+PrintGCDetails
 */
public class SoftReferenceDemo {

    public static void main(String[] args) {
        System.out.println("===========内存足够===========");
        softRef_Memory_Enough();
        System.out.println("============内存不足==============");
        softRef_Memory_NotEnough();
    }

    private static void softRef_Memory_NotEnough(){
        Object obj1 = new Object();
        SoftReference<Object> obj2 = new SoftReference<>(obj1);
        System.out.println(obj1 + "\t"+ obj2.get());

        obj1 = null;

        try {
            byte[] bytes = new byte[5 * 1024 * 1024];
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            System.out.println(obj1 + "\t"+ obj2.get());
        }
    }

    private static void softRef_Memory_Enough() {
        Object obj1 = new Object();
        SoftReference<Object> obj2 = new SoftReference<>(obj1);

        System.out.println(obj1 + "\t"+ obj2.get());

        obj1 = null;
        System.gc();

        System.out.println(obj1 + "\t"+ obj2.get());
    }
}

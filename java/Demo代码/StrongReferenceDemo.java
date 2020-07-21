package com.atLearn;

public class StrongReferenceDemo {
    public static void main(String[] args) {

        // 强引用对象
        Object obj1 = new Object();
        // 引用赋值
        Object obj2 = obj1;

        obj1 = null;

        System.gc();
        System.out.println(obj1 + "\t" + obj2);
    }
}

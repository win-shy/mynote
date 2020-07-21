package com.atLearn;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 *  VM Options:  -XX:+PrintGCDetails
 */
public class PhantomReferenceDemo {
    public static void main(String[] args) {
        Object obj1 = new Object();
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
        PhantomReference<Object> phantomReference = new PhantomReference<>(obj1,referenceQueue);

        System.out.println(obj1 + "\t" + phantomReference.get()+"\t" + referenceQueue.poll());
        System.out.println("============gc================");

        obj1 = null;
        System.gc();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(obj1 + "\t" + phantomReference.get()+"\t" + referenceQueue.poll());
    }
}

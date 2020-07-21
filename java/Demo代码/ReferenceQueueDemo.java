package com.atLearn;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 *  VM Options:  -XX:+PrintGCDetails
 */
public class ReferenceQueueDemo {

    public static void main(String[] args) {
        Object obj1 = new Object();
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
        WeakReference<Object> weakReference= new WeakReference<>(obj1,referenceQueue);

        System.out.println(obj1 +"\t"+weakReference.get()+"\t" + referenceQueue.poll());
        System.out.println("===============gc之后=================");
        obj1 = null;
        // 开启 GC，弱引用被回收。
        System.gc();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(obj1 +"\t"+weakReference.get()+"\t" + referenceQueue.poll());
    }
}

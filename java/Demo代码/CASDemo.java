package com.atLearn;

import java.util.concurrent.atomic.AtomicInteger;

public class CASDemo {
    public static void main(String[] args) {
        AtomicInteger atomicInteger = new AtomicInteger(4);
        System.out.println(atomicInteger.compareAndSet(4,6) +" , current: "+ atomicInteger.get());
        System.out.println(atomicInteger.compareAndSet(4,10) + " ,current: "+atomicInteger.get());
    }
}

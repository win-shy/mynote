package com.atLearn;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

public class ABADemo {
    static AtomicReference<Integer> atomicReference = new AtomicReference<>(10);
    static AtomicStampedReference<Integer> atomicStampedReference = new AtomicStampedReference<>(100,1);


    public static void main(String[] args) {
        new Thread(() ->{
           atomicReference.compareAndSet(10,11);
           atomicReference.compareAndSet(11,10);
        },"t1").start();

        new Thread(() ->{
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(Thread.currentThread().getName() +"\t"+ atomicReference.compareAndSet(10,2019)+"\t"+atomicReference.get());
        },"t2").start();

        System.out.println("=======================================");

        new Thread(() ->{
            System.out.println(Thread.currentThread().getName() +"\t"+"传入"+"\t"+ atomicStampedReference.getStamp());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            atomicStampedReference.compareAndSet(100,101,atomicStampedReference.getStamp(),atomicStampedReference.getStamp()+1);
            System.out.println(Thread.currentThread().getName() +"\t"+"第一次"+"\t"+ atomicStampedReference.getStamp() + "\t" + atomicStampedReference.getReference());
            atomicStampedReference.compareAndSet(101,100,atomicStampedReference.getStamp(),atomicStampedReference.getStamp()+1);
            System.out.println(Thread.currentThread().getName() +"\t"+"第二次"+"\t"+ atomicStampedReference.getStamp() + "\t" + atomicStampedReference.getReference());
        },"t3").start();

        new Thread(()->{
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName()+"\t第1次版本号："+atomicStampedReference.getStamp());

            boolean result = atomicStampedReference.compareAndSet(100,2019,/*atomicStampedReference.getStamp()*/1,atomicStampedReference.getStamp()+1);

            System.out.println(Thread.currentThread().getName()+"\t修改成功否： "+result+"\t当前最新实际版本号："+atomicStampedReference.getStamp() + "\t" + atomicStampedReference.getReference());

        },"t4").start();
    }
}


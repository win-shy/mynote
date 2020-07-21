package com.atLearn;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class VolatileDemo {

    public static void main(String[] args) {
        MyData data = new MyData();
        for(int i = 0;i<20;i++){
            new Thread(()->{
                for(int j = 0;j < 1000;j++) {
                    data.addPlus();
                }
            },"线程1 "+String.valueOf(i)).start();

        }


        while (Thread.activeCount() > 2){
            Thread.yield();
        }

        System.out.println(Thread.currentThread().getName()+" 的 number 变量变化了: " + data.number);
    }
}


class MyData{
    volatile int number = 0;
    volatile AtomicInteger atomicInteger = new AtomicInteger();
    public void addTen(){
        this.number = 10;
    }

    public synchronized void addPlus(){
        number++;
    }
}


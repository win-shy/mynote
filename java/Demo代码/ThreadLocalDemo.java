package com.atLearn;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadLocalDemo {
    public static void main(String[] args) {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        MyDatas myDatas = new MyDatas();
        for(int i = 0;i<10;i++){
            new Thread(()->{
                myDatas.addTen();
//                threadLocal.set(myDatas.number+1+"");

//                System.out.println(threadLocal.get());
                System.out.println(myDatas.number+1);
            },"Thread "+i).start();
        }
        System.out.println(myDatas.number);
        System.out.println("main Thread end");
    }
}

class MyDatas{
    volatile int number = 0;
    volatile AtomicInteger atomicInteger = new AtomicInteger();
    public void addTen(){
        this.number = 10;
    }

    public synchronized void addPlus(){
        number++;
    }
}
package com.atLearn;

import java.sql.Time;
import java.util.concurrent.CountDownLatch;

public class CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(6);

        for(int i = 0;i< 6 ;i++){
            new Thread(()->{
                System.out.println(Thread.currentThread().getName()+"\t AAAAAAAAAA");
                countDownLatch.countDown();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            },i+"").start();

        }

        countDownLatch.await();
        System.out.println(Thread.currentThread().getName()+"\t BBBBBBBBBBBBBB \t" + countDownLatch.getCount());
    }
}

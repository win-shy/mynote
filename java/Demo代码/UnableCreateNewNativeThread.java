package com.atLearn;

import java.util.concurrent.TimeUnit;

public class UnableCreateNewNativeThread {
    public static void main(String[] args) {
        for(int i =1;;i++) {
            System.out.println("i: "+i);

            new Thread(()->{
                try {
                    Thread.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            },""+i).start();
        }
    }
}

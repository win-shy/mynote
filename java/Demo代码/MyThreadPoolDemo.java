package com.atLearn;

import java.util.concurrent.*;

public class MyThreadPoolDemo {

    public static void main(String[] args) {
       ExecutorService threadPool = new ThreadPoolExecutor(
               2,
               5,
               1L,
               TimeUnit.SECONDS,
               new LinkedBlockingQueue<>(3),
               Executors.defaultThreadFactory(),
               new ThreadPoolExecutor.DiscardPolicy()
       );
        try {
            for (int i = 1; i <= 10; i++) {
                threadPool.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + "\t 办理业务");
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            threadPool.shutdown();
        }

    }

    private static void initPool() {
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
        ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();

        try{
            for(int i=1;i<100;i++){
                cachedThreadPool.execute(()->{
                    System.out.println(Thread.currentThread().getName());
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cachedThreadPool.shutdown();
        }
    }
}

package com.atLearn;

import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockDemo {
    public static void main(String[] args) {
        MyCache myCache = new MyCache();

        for(int i= 1; i<=5;i++){
            final int tmpInt = i;
            new Thread(()->{
                myCache.put(tmpInt+"",tmpInt+"");
            },i+"").start();
        }

        for(int i=1;i<=5;i++){
            final int tempInt = i;
            new Thread(()->{
                myCache.get(tempInt+"");
            },String.valueOf(i)).start();
        }
    }
}

class MyCache{
    private volatile Map<String,Object> map = new HashMap();
//    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private ReentrantLock lock = new ReentrantLock();
    public void put(String key,Object value){
//        lock.writeLock().lock();
        lock.lock();
        System.out.println(Thread.currentThread().getName()+"\t"+"正在写入");

//        try {
//            TimeUnit.SECONDS.sleep(1);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        try {
            map.put(key, value);
            System.out.println(Thread.currentThread().getName() + "\t" + "写入完成");
        }finally {
//            lock.writeLock().unlock();
            ;lock.unlock();
        }
    }

    public void get(String key){
//        lock.readLock().lock();
        lock.lock();
        System.out.println(Thread.currentThread().getName()+"\t 正在读取："+key);
//        try {
//            TimeUnit.MILLISECONDS.sleep(300);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        try {
            Object result = map.get(key);
            System.out.println(Thread.currentThread().getName() + "\t 读取完成" + "\t" + result);
        }finally {
            lock.unlock();
//            lock.readLock().unlock();
        }
    }
}
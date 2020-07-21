package com.atLearn;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/*
 * 题目：多线程之间按顺序调用，实现A->B->C三个线程启动，要求如下：
 * A打印5次，B打印10次，C打印15次
 * 紧接着
 * A打印5次，B打印10次，C打印15次
 * 。。。。。
 * 打印10轮
 *
 * */
public class SyncAndReentrantLockDemo {
    public static void main(String[] args) {
        ShareResource shareResource = new ShareResource();
        new Thread(()->{
            for(int i =1;i<=10;i++){
                shareResource.printA();
            }
        },"A").start();

        new Thread(()->{
            for(int i = 1;i<=10;i++){
                shareResource.printB();
            }
        },"B").start();

        new Thread(()->{
            for(int i =1;i<=10;i++){
                shareResource.printC();
            }
        },"C").start();
    }
}

class ShareResource{
    private int number = 1;
    private Lock lock = new ReentrantLock();
    private Condition cond1 =  lock.newCondition();
    private Condition cond2 = lock.newCondition();
    private Condition cond3 = lock.newCondition();

    public void printA(){
        lock.lock();
        try{
            while (number != 1){
                cond1.await();
            }

            for(int i =1;i<=5;i++){
                System.out.println(Thread.currentThread().getName()+"\t"+i);
            }

            number = 2;
            cond2.signal();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    public void printB(){
        lock.lock();

        try{
            while (number != 2){
                cond2.await();
            }

            for(int i = 1;i<=10;i++){
                System.out.println(Thread.currentThread().getName()+"\t"+i);
            }
            number = 3;
            cond3.signal();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    public void printC(){
        lock.lock();
        try{
            while (number != 3){
                cond3.await();
            }

            number = 1;
            cond1.signal();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
}
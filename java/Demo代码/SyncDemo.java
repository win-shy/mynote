package com.atLearn;

public class SyncDemo {
    public static void main(String[] args) {

    }
    int x = 1;
     synchronized void  set(SyncDemo s){
//        synchronized (s) {
            x =x +1;
//        }
    }
}

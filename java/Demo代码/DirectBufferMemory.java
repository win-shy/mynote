package com.atLearn;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * -XX:+PrintGCDetails -XX:MaxDirectMemorySize=2m
 */
public class DirectBufferMemory {
    public static void main(String[] args) {
        System.out.println("maxDirectMemory: "+sun.misc.VM.maxDirectMemory()/(1024 * 1024));

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(10 * 1024 * 1024);
    }
}

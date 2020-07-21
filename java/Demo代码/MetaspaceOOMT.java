package com.atLearn;

/**
 * 使用Java -XX:+PrintFlagsInitial命令查看本机的初始化参数，-XX:MetaspaceSize为21810376B(约20M)
 *
 * VM options:    -XX:+PrintGCDetails -XX:MetaspaceSize=1m -XX:MaxMetaspaceSize=1m
 */
public class MetaspaceOOMT {
    static class OOMTest{
    }

    public static void main(String[] args) {
        int i = 0;
        try{
            while (true){
                i++;
            }
        }catch (Throwable e){
            e.printStackTrace();
            System.out.println("i: "+i);
        }
    }
}

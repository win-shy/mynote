package com.atLearn;

public class StackOverflowErrorDemo {
    public static void main(String[] args) {
        stackOverFlowError();
    }

    private static void stackOverFlowError(){
        stackOverFlowError();
    }
}

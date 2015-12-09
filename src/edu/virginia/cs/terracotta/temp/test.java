/*
 * test.java
 * Created on 2004-9-27
 */
package edu.virginia.cs.terracotta.temp;

/**
 * @author Jinlin Yang
 */
public class test implements Runnable {
    public void run() {
        System.out.println(Thread.currentThread());
    }

    public static void main(String[] args) {
        byte a = -1;
        byte b = 0;
        b |= a;
        System.out.println(b);
        test t = new test();
        Thread t1 = new Thread(t, "t1");
        Thread t2 = new Thread(t, "t1");
        t1.start();
        t2.start();
    }
}
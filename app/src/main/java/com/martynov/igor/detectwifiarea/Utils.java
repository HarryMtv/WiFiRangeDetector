package com.martynov.igor.detectwifiarea;

/*
 * Created by igor.martynov on 4/29/18.
 */

public class Utils {

    private Utils() { }

    public static void waitTime(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

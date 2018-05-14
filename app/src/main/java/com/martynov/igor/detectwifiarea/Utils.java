package com.martynov.igor.detectwifiarea;

/*
 * Created by igor.martynov on 4/29/18.
 */

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Utils {

    private Utils() { }

    public static void waitTime(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static LatLng findMassCenter(List<WiFiPoint> points) {
        int massSum = points.stream().mapToInt(WiFiPoint::getSignalStrange).sum();

        double x = points.stream().mapToDouble(WiFiPoint::getXMass).sum() / massSum;
        double y = points.stream().mapToDouble(WiFiPoint::getYMass).sum() / massSum;

        return new LatLng(x, y);
    }
}

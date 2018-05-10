package com.martynov.igor.detectwifiarea;

/*
 * Created by igor.martynov on 5/10/18.
 */

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PointsStorage {

    private static PointsStorage pointsStorage;
    private Map<String, List<WiFiPoint>> wifiPointsStorage;

    public PointsStorage() {
        wifiPointsStorage = new HashMap<>();
    }

    public static PointsStorage getPointsStorage() {
        if(pointsStorage == null) {
            pointsStorage = new PointsStorage();
        }
        return pointsStorage;
    }

    public void addPoint(String wifiName, WiFiPoint wiFiPoint) {
        List<WiFiPoint> wiFiPointsList = wifiPointsStorage.get(wifiName);

        // if list does not exist create it
        if(wiFiPointsList == null) {
            wiFiPointsList = new ArrayList<>();
            wiFiPointsList.add(wiFiPoint);
            wifiPointsStorage.put(wifiName, wiFiPointsList);
        } else {
            // add if item is not already in list
            if(!wiFiPointsList.contains(wiFiPoint)) wiFiPointsList.add(wiFiPoint);
        }
    }

    public static WiFiPoint generateWifiPoint(final LatLng point, final int signalStrange) {
        return new WiFiPoint(point, signalStrange);
    }

    public Map<String, List<WiFiPoint>> getWifiPointsStorage() {
        return wifiPointsStorage;
    }
}

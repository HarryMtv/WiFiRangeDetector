package com.martynov.igor.detectwifiarea;

/*
 * Created by igor.martynov on 5/10/18.
 */

import com.google.android.gms.maps.model.LatLng;

public class WiFiPoint {

    private LatLng point;
    private int signalStrange;

    public WiFiPoint(final LatLng point, final int signalStrange) {
        this.point = point;
        this.signalStrange = signalStrange;
    }

    @Override
    public String toString() {
        return String.valueOf(signalStrange);
    }
}

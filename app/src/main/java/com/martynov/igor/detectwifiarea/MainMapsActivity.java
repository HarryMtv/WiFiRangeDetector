package com.martynov.igor.detectwifiarea;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.martynov.igor.detectwifiarea.PointsStorage.generateWifiPoint;
import static com.martynov.igor.detectwifiarea.PointsStorage.getPointsStorage;
import static com.martynov.igor.detectwifiarea.Utils.findMassCenter;

public class MainMapsActivity extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        GoogleMap.OnMarkerClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final String TAG = "MainMapsActivity";

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private String[] mNavigationDrawerItemTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    Toolbar toolbar;
    private CharSequence mTitle;
    ActionBarDrawerToggle mDrawerToggle;
    private GoogleMap googleMap;
    LocationManager locationManager;
    Context context = null;
    Circle circle = null;

    private LatLng myLocation;
    private Marker myLocationMarker;
    private LatLng currentWiFiPoint;
    private Marker currentWiFiMarker;
    private Polygon currentWiFiPolygon;
    private List<LatLng> points; //added
    Polyline line; //added

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mTitle = getTitle();
        mNavigationDrawerItemTitles = getResources().getStringArray(R.array.navigation_drawer_items_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        points = new ArrayList<>(); //added

        setupToolbar();

        DataModel[] drawerItem = new DataModel[3];

        drawerItem[0] = new DataModel(R.drawable.connect, "Connect");
        drawerItem[1] = new DataModel(R.drawable.fixtures, "Fixtures");
        drawerItem[2] = new DataModel(R.drawable.table, "Table");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);

        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, R.layout.list_view_item_row, drawerItem);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        setupDrawerToggle();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        initContext();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        locationProvider();
    }

    private void drawCircle() {
        WifiInfo wifiInfo = getCurrentConnectionInfo(context);
        List<WiFiPoint> currentWiFiPoints = getPointsStorage().getWifiPointsStorage()
                .get(wifiInfo.getSSID().replace("\"", ""));

        /*double sumX = currentWiFiPoints.stream().mapToDouble(WiFiPoint::getX).sum();
        double sumY = currentWiFiPoints.stream().mapToDouble(WiFiPoint::getY).sum();
        double sumXsumX = currentWiFiPoints.stream().mapToDouble(point -> point.getX() * point.getX()).sum();
        double sumYsumY = currentWiFiPoints.stream().mapToDouble(point -> point.getY() * point.getY()).sum();

        double delta = ((sumXsumX + sumYsumY) -
                2 * (currentWiFiPoint.latitude * sumX + currentWiFiPoint.longitude * sumY))
                / currentWiFiPoints.size();

        double radius = Math.sqrt(
                (currentWiFiPoint.latitude) * (currentWiFiPoint.latitude) +
                        (currentWiFiPoint.longitude) * (currentWiFiPoint.longitude) + delta
        );*/

        double sum = currentWiFiPoints.stream().mapToDouble(point ->
                Math.sqrt((point.getX() - currentWiFiPoint.latitude) * (point.getX() - currentWiFiPoint.latitude)
                        + (point.getY() - currentWiFiPoint.longitude) * (point.getY() - currentWiFiPoint.longitude))).sum();
        double tradius = sum / currentWiFiPoints.size();

        if(circle != null) circle.remove();
        circle = googleMap.addCircle(new CircleOptions()
                .center(currentWiFiPoint)
                .radius(tradius * 1000000)
                .strokeColor(Color.RED)
                .fillColor(0x220000FF)
                .strokeWidth(5));
    }

    private void redrawLine() {
        googleMap.clear();  //clears all Markers and Polylines

        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            options.add(point);
        }
        //addMarker(); //add Marker in current position
        line = googleMap.addPolyline(options); //add Polyline
    }

    private void addMarker() {
        MarkerOptions options = new MarkerOptions();

        // following four lines requires 'Google Maps Android API Utility Library'
        // https://developers.google.com/maps/documentation/android/utility/
        // I have used this to display the time as title for location markers
        // you can safely comment the following four lines but for this info
        //IconGenerator iconFactory = new IconGenerator(this);
        //iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
        // options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(mLastUpdateTime + requiredArea + city)));
        //options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(requiredArea + ", " + city)));
        //options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        //LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        //options.position(currentLatLng);
        Marker mapMarker = googleMap.addMarker(options);
        //long atTime = mCurrentLocation.getTime();
        //mLastUpdateTime = DateFormat.getTimeInstance().format(new Date(atTime));
        //String title = mLastUpdateTime.concat(", " + requiredArea).concat(", " + city).concat(", " + country);
        mapMarker.setTitle("Hello");

        //TextView mapTitle = (TextView) findViewById(R.id.textViewTitle);
        //mapTitle.setText("Hello");

        Log.d(TAG, "Marker added.............................");
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13));
        Log.d(TAG, "Zoom done.............................");
    }

    private void addMyPosition(final LatLng myLocation) {
        if(myLocationMarker!= null) myLocationMarker.remove();
        this.myLocation = myLocation;
        myLocationMarker = googleMap
                .addMarker(new MarkerOptions()
                        .position(myLocation)
                        .title("My Position")
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }

    private void drawPolygon() {
        WifiInfo wifiInfo = getCurrentConnectionInfo(context);
        List<WiFiPoint> currentWiFiPoints = getPointsStorage().getWifiPointsStorage()
                .get(wifiInfo.getSSID().replace("\"", ""));

        LatLng[] currentWiFiCoordinates = currentWiFiPoints.stream().map(WiFiPoint::getPoint)
                .toArray(LatLng[]::new);

        double sumX = currentWiFiPoints.stream().mapToDouble(point -> point.getXMass() * point.getXMass()).sum();
        double sumY = currentWiFiPoints.stream().mapToDouble(point -> point.getYMass() * point.getYMass()).sum();

        double delta = ((sumX + sumY) -
                2 * (currentWiFiPoint.latitude * sumX + currentWiFiPoint.longitude * sumY))
                / currentWiFiPoints.size();

        double radius = Math.sqrt(
                        (currentWiFiPoint.latitude)*(currentWiFiPoint.latitude) +
                        (currentWiFiPoint.longitude)*(currentWiFiPoint.longitude) + delta
        );

        if(currentWiFiPolygon != null) currentWiFiPolygon.remove();
        currentWiFiPolygon = googleMap.addPolygon(new PolygonOptions()
                //.clickable(true)
                .add(currentWiFiCoordinates)
                .fillColor(0xff388E3C));
        currentWiFiPolygon.setTag("curwifi");
    }

    LocationListener locationListenerGPS = new LocationListener() {

        @Override
        public void onLocationChanged(android.location.Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            /*String msg = "New Latitude: " + latitude + "\nNew Longitude: " + longitude;
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();*/

            LatLng latLng = new LatLng(latitude, longitude); //you already have this
            points.add(latLng); //added

            //redrawLine(); // draw radius
            addMyPosition(latLng); // add my location
            //drawCircle(location);
            scanWifiNetworks(context);
            addCurrentWifiMarker();
            //drawPolygon();
            drawCircle();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void addCurrentWifiMarker() {
        WifiInfo wifiInfo = getCurrentConnectionInfo(context);
        List<WiFiPoint> currentWiFiPoints = getPointsStorage().getWifiPointsStorage()
                .get(wifiInfo.getSSID().replace("\"", ""));

        if(currentWiFiPoints != null) {
            //String tmp = wifiInfo.getSSID() + " : " + wifiInfo.getRssi() + " : " + wifiInfo.getFrequency();
            if(currentWiFiMarker!= null) currentWiFiMarker.remove();
            currentWiFiPoint = findMassCenter(currentWiFiPoints);
            currentWiFiMarker = googleMap
                    .addMarker(new MarkerOptions()
                            .position(currentWiFiPoint)
                            .title(wifiInfo.getSSID()));
        }
    }

    public static WifiInfo getCurrentConnectionInfo(Context context) {
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager != null) {
            return wifiManager.getConnectionInfo();
        }
        return null;
    }

    public void scanWifiNetworks(Context context) {
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager != null && wifiManager.startScan()) {
            List<ScanResult> scanResultList = wifiManager.getScanResults();
            //List<String> SSIDList = scanResultList.stream().map(result -> result.SSID).collect(Collectors.toList());
            //Toast.makeText(context, SSIDList.toString(), Toast.LENGTH_LONG).show();

            scanResultList.forEach(result -> getPointsStorage()
                    .addPoint(result.SSID, generateWifiPoint(getMyLocation(), result.level)));
        }
    }

    private void isLocationEnabled() {
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which){
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDialog.create();
            alert.show();
        }
        else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Confirm Location");
            alertDialog.setMessage("Your Location is enabled, please enjoy");
            alertDialog.setNegativeButton("Back to interface", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDialog.create();
            alert.show();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        googleMap.setOnMyLocationButtonClickListener(this);
        googleMap.setOnMyLocationClickListener(this);
        googleMap.setOnMarkerClickListener(this);
        enableMyLocation();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (googleMap != null) {
            // Access to the location has been granted to the app.
            googleMap.setMyLocationEnabled(false);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
            locationProvider();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @SuppressLint("MissingPermission")
    private void locationProvider() {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100,
                1, locationListenerGPS);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100,
                1, locationListenerGPS);
        isLocationEnabled();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.equals(myLocationMarker)) {
            googleMap
                    .moveCamera(CameraUpdateFactory.newLatLngZoom(getMyLocation(),15));
            // Zoom in, animating the camera.
            googleMap
                    .animateCamera(CameraUpdateFactory.zoomIn());
            // Zoom out to zoom level 10, animating with a duration of 2 seconds.
            googleMap
                    .animateCamera(CameraUpdateFactory.zoomTo(18), 3000, null);
            marker
                    .showInfoWindow();
            return true;
        }

        if(marker.equals(currentWiFiMarker)) {
            marker
                    .showInfoWindow();
        }

        //Toast.makeText(this, marker.getTitle(), Toast.LENGTH_LONG).show();
        return false;
    }

    //Left panel

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {

        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new ConnectFragment();
                break;
            case 1:
                fragment = new FixturesFragment();
                break;
            case 2:
                fragment = new TableFragment();
                break;

            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(mNavigationDrawerItemTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);

        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    void setupToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    void setupDrawerToggle(){
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.app_name, R.string.app_name);
        //This is necessary to change the icon of the Drawer Toggle upon state change.
        mDrawerToggle.syncState();
    }

    private void initContext() {
        if(context == null) {
            context = this;
        }
    }

    private LatLng getMyLocation() {
        return myLocation != null ? myLocation : new LatLng(1, 1);
    }
}

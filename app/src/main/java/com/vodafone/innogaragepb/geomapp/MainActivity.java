package com.vodafone.innogaragepb.geomapp;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener, LocationListener{
    public JSONObject jsonObj;
    private GoogleMap mMap;
    public Boolean ready;
    public LatLng myLatLng;
    public LatLng myLatLng2;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Marker mCurrentLocation;
    Location mLastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

// Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final Button accidentButton = (Button) findViewById(R.id.accidentButton);
        final Button trafficjamButton = (Button) findViewById(R.id.trafficjamButton);
        final Button speedlimitButton = (Button) findViewById(R.id.speedlimitButton);


        final String jsonString = loadJSONFromAsset();
        try {
             jsonObj = new JSONObject(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }



        //Initializing the buttons
        accidentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Place a marker with location and time to live
                setSituation(5000, myLatLng, "accident");
            }
        });

        trafficjamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Sendtraffic icon
                setSituation(10000, myLatLng2, "trafficjam");
            }
        });

        speedlimitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Sendtraffic icon
                //setLocation(5000, myLatLng2, myMarker, "pink");
                try {
                    setMarkers(jsonObj);
                } catch (JSONException e) {
                    System.out.println("Could not set markers");
                }
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        myLatLng = new LatLng(51.23610018, 6.73155069);
        myLatLng2 = new LatLng(51.23708092, 6.72972679);
        // Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.getUiSettings().setZoomControlsEnabled(true);
        ready = true;
    }

//SET USE CASES: Situation in the traffic or just location of the other cars
    public void setSituation(long duration, LatLng myLatLng, String myString) {
        Marker marker;
        final String code = myString;

        if (ready) {
                marker = mMap.addMarker(new MarkerOptions()
                        .position(myLatLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(resizer(code, 70, 70))));
                fadeTime(duration, marker);
            }
        }

    public void setLocation (long duration, LatLng myLatLng, String cellColor){
        if (ready) {
            Marker marker;
            int id = getResources().getIdentifier(cellColor, "drawable", getPackageName());
            marker = mMap.addMarker(new MarkerOptions()
                    .position(myLatLng)
                    .icon(BitmapDescriptorFactory.fromResource(id)));
            fadeTime(duration, marker);
        }
    }

    public void setMarkers(JSONObject myJson)throws JSONException   {
        final JSONObject json = myJson;
        final JSONArray cans = myJson.getJSONArray("cans");

        System.out.println("Inside setMarkers");


                for (int i = 0; i < cans.length(); i++) {
                    JSONObject c;
                    final int number = i;
                    try {
                        c = cans.getJSONObject(i);
                        final String MessageTypeID = c.getString("MessageTypeID");
                        final String Erzeugerzeitpunkt = c.getString("Erzeugerzeitpunkt");
                        final Long Lebensdauer = c.getLong("Lebensdauer");
                        //Float Lat = Float.valueOf(c.getString("Lat"));
                        //Float Long = Float.valueOf(c.getString("Long"));
                        final Double latitude = c.getDouble("Lat");
                        final Double longitude = c.getDouble("Long");
                        JSONArray CellID = c.getJSONArray("CellID");
                        final String Message = c.getString("Message");
                        int id = getResources().getIdentifier("pink", "drawable", getPackageName());

                        runOnUiThread(new Runnable(){
                            @Override
                            public void run(){

                                setCoordinates(latitude,longitude, Lebensdauer, number, Erzeugerzeitpunkt, Message);

                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
    }

private void setCoordinates(Double lat, Double lon, Long dur, int number, String ezp, String msg){

    int id = getResources().getIdentifier("pink", "drawable", getPackageName());
    LatLng myPosition = new LatLng(lat, lon);

    mMap.addMarker(new MarkerOptions()
            .position(myPosition)
            .icon(BitmapDescriptorFactory.fromResource(id))
            .title("Point "+number)
            .snippet("Message: " + msg )

            );

    //fadeTime(dur,mar);


}

//Customize characteristics of the markers: Size and time to fade

    public Bitmap resizer(String iconName, int width, int height) {
        Bitmap imgBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imgBitmap, width, height, false);
        return resizedBitmap;
    }

    public void fadeTime(long duration, Marker marker) {

        final Marker myMarker = marker;
        ValueAnimator myAnim = ValueAnimator.ofFloat(1, 0);
        myAnim.setDuration(duration);
        myAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                myMarker.setAlpha((float) animation.getAnimatedValue());
            }
        });
        myAnim.start();








    }


    @Override
    public void onLocationChanged(Location location) {
       mLastLocation = location;
        if (mCurrentLocation != null){
            mCurrentLocation.remove();
        }
        //Place my location Marker
        LatLng latlong = new LatLng(location.getLatitude(), location.getLongitude());
        //mCurrentLocation = mMap.addMarker(new MarkerOptions()
          //  .position(latlong));
            //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlong));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
        }
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
        @Override
        public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
            switch (requestCode) {
                case MY_PERMISSIONS_REQUEST_LOCATION: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        // Permission Granted
                        if (ContextCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {

                            if (mGoogleApiClient == null) {
                                buildGoogleApiClient();
                            }
                            mMap.setMyLocationEnabled(true);
                        }
                    } else {

                        // Permission denied, Disable the functionality that depends on this permission.
                        Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                    }
                    return;
                }
            }
        }
    /*------------------Reading JSON Data------------------------*/
    public String loadJSONFromAsset() {
        String json = null;
        try {

            InputStream is = getAssets().open("data1.json");


            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }







}


//http://stackoverflow.com/questions/28109597/gradually-fade-out-a-custom-map-marker
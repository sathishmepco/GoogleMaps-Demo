package com.cdac.googlemapsdemo;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Marker currentLocationMarker;
    Location lastLocation;
    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        findViewById(R.id.buttonGetAddress).setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkPermission();
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if(currentLocationMarker == null){
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            currentLocationMarker= mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        }else{
            currentLocationMarker.setPosition(latLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest .setInterval(1000);
        locationRequest .setFastestInterval(1000);
        locationRequest .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void checkPermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},123);
    }

    private void initAfterMapReady() {
        mMap.setMyLocationEnabled(true);
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient= new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 123){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                initAfterMapReady();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //stop location updates
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonGetAddress:
                getAddress();
                break;
        }
    }

    private void getAddress(){
        if(lastLocation == null)
            return;

        new Thread(){
            @Override
            public void run() {
                super.run();
                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(lastLocation.getLatitude(),lastLocation.getLongitude(),1);
                    if(addresses.size() > 0){
                        String completeAddress = "";
                        String  city = addresses.get(0).getLocality();
                        String locality = addresses.get(0).getSubLocality();
                        String line1 = addresses.get(0).getAddressLine(0);
                        String line2 = addresses.get(0).getAddressLine(1);
                        String country = addresses.get(0).getCountryName();
                        String state = addresses.get(0).getAdminArea();
                        String postalCode = addresses.get(0).getPostalCode();

                        Log.d("TAG","Address Line 0 "+addresses.get(0).getAddressLine(0));
                        Log.d("TAG","Address Line 1 "+addresses.get(0).getAddressLine(1));
                        Log.d("TAG","Admin Area "+addresses.get(0).getAdminArea());
                        Log.d("TAG","Country Name"+addresses.get(0).getCountryName());
                        Log.d("TAG","Postal Code "+addresses.get(0).getPostalCode());
                        Log.d("TAG","Permises "+addresses.get(0).getPremises());
                        Log.d("TAG","Sub Locality "+addresses.get(0).getSubLocality());
                        Log.d("TAG","getThoroughfare "+addresses.get(0).getThoroughfare());
                        Log.d("TAG","getSubThoroughfare "+addresses.get(0).getSubThoroughfare());
                        Log.d("TAG","getSubAdminArea "+addresses.get(0).getSubAdminArea());

                        if(line1 != null)
                            completeAddress += line1 +"\n";
                        if(line2 != null)
                            completeAddress += line2 +"\n";
                        if(locality != null)
                            completeAddress += locality +"\n";
                        if(city != null)
                            completeAddress += city +"\n";
                        if(state != null)
                            completeAddress += state +"\n";
                        if(postalCode != null)
                            completeAddress += postalCode;

                        final String address = line1;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showAlert(address);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void showAlert(String completeAddress){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(completeAddress);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
}
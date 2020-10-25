package com.example.memorableplace;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
//lat lng:locationAddress
//address:locations
//arrayAdapter:places
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationListener locationListener;
    LocationManager locationManager;
    public void centreMapOnLocation(Location location,String title)
    {
        if(location!=null)
        {LatLng userLocation=new LatLng(location.getLatitude(),location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnown=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centreMapOnLocation(lastKnown,"YOUR LOCATION");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Intent i=getIntent();
        int pos=i.getIntExtra("place",0);
      // Toast.makeText(MapsActivity.this, Integer.toString(pos), Toast.LENGTH_SHORT).show();
        if(pos==0)
        {
            mMap.clear();
            locationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
            locationListener=new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                 centreMapOnLocation(location,"YOUR LOCATION");
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
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    List<Address> list;
                    Address address;
                    String locationAdd="";
                    try {
                        list = geocoder.getFromLocation(latLng.latitude,latLng.longitude, 1);
                        if((list != null) && (list.size() > 0)) {
                            address = list.get(0);
                            if (address.getThoroughfare() != null)
                                locationAdd+=  address.getThoroughfare() + " ";
                            if (address.getLocality() != null)
                                locationAdd+= address.getLocality() + " ";
                            if (address.getAdminArea() != null)
                                locationAdd+= address.getAdminArea() + " ";
                            if(address!=null)
                            {MarkerOptions options = new MarkerOptions().position(new LatLng(latLng.latitude,latLng.longitude)).title(address.getLocality());
                                mMap.addMarker(options).showInfoWindow();
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(locationAdd.length()==0)
                    {
                        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm yyyy-MM-dd");
                        locationAdd+=sdf.format(new Date());
                    }
                    MainActivity.locations.add(locationAdd);
                    MainActivity.locationAddress.add(latLng);
                    MainActivity.places.notifyDataSetChanged();
                    Toast.makeText(MapsActivity.this,"Location Saved:)", Toast.LENGTH_LONG).show();
                    Log.i("Location:",locationAdd);
                    SharedPreferences sharedPreferences=MapsActivity.this.getSharedPreferences("com.example.memorableplace",Context.MODE_PRIVATE);
                    try{
                        ArrayList<String> latitude=new ArrayList<String>();
                        ArrayList<String> longitude=new ArrayList<String>();
                        for(LatLng coord: MainActivity.locationAddress)
                        {
                            latitude.add(Double.toString(coord.latitude));
                            longitude.add(Double.toString(coord.longitude));
                        }
                      sharedPreferences.edit().putString("addresses",ObjectSerializer.serialize(MainActivity.locations)).apply();
                      Log.i("addresses",ObjectSerializer.serialize(MainActivity.locations));
                        sharedPreferences.edit().putString("latitudes",ObjectSerializer.serialize(latitude)).apply();
                        Log.i("latitudes",ObjectSerializer.serialize(latitude));
                        sharedPreferences.edit().putString("longitudes",ObjectSerializer.serialize(longitude)).apply();
                        Log.i("longitudes",ObjectSerializer.serialize(longitude));
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            Location lastKnown=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            centreMapOnLocation(lastKnown,"YOUR LOCATION");
        }
        }
        else
        {
            Location l=new Location(LocationManager.GPS_PROVIDER);
            l.setLatitude(MainActivity.locationAddress.get(pos).latitude);
            l.setLongitude(MainActivity.locationAddress.get(pos).longitude);
            if(l!=null)
            {LatLng userLocation=new LatLng(l.getLatitude(),l.getLongitude());
                mMap.addMarker(new MarkerOptions().position(userLocation).title(MainActivity.locations.get(pos))).showInfoWindow();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
            }
        }
        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

}
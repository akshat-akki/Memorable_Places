package com.example.memorableplace;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
ListView listView;
static ArrayList<String> locations=new ArrayList<String>();
static ArrayAdapter<String> places;
    static ArrayList<LatLng> locationAddress=new ArrayList<LatLng>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=findViewById(R.id.listView);
        SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.memorableplace",Context.MODE_PRIVATE);
        ArrayList<String> latitude=new ArrayList<String>();
        ArrayList<String> longitude=new ArrayList<String>();
        locations.clear();
        latitude.clear();
        longitude.clear();
        locationAddress.clear();
        try{

            locations=(ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("addresses",ObjectSerializer.serialize(new ArrayList<String>())));
            latitude=(ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("latitudes",ObjectSerializer.serialize(new ArrayList<String>())));
            longitude=(ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longitudes",ObjectSerializer.serialize(new ArrayList<String>())));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        if(locations.size()>0 && latitude.size()>0 && longitude.size()>0)
        {
            if(longitude.size()==locations.size() && longitude.size()==latitude.size())
            {
                for(int k=0;k<latitude.size();k++)
                {
                    locationAddress.add(new LatLng(Double.parseDouble(latitude.get(k)),Double.parseDouble(longitude.get(k))));
                }
            }
        }
        else
        {
            locations.add("Add new place");
            locationAddress.add(new LatLng(0,0));
        }
        places=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,locations);
        listView.setAdapter(places);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),MapsActivity.class);
                //Toast.makeText(MainActivity.this, Integer.toString(position), Toast.LENGTH_SHORT).show();
                intent.putExtra("place",position);
                startActivity(intent);
            }
        });
    }
}
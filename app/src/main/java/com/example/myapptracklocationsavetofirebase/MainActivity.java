package com.example.myapptracklocationsavetofirebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient client;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("address");
    Geocoder geocoder;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationAddress();
            }
        });

    }

    private void getLocationAddress() {

        client = LocationServices.getFusedLocationProviderClient(this);
        client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {

                    // ส่งค่าข้อมูล ไปที่ MapActivity
                    Intent i = new Intent(MainActivity.this, MapActivity.class);
                    i.putExtra("LON", location.getLatitude());
                    i.putExtra("LAT", location.getLongitude());
                    startActivity(i);
                }

                // getAddress + writeDataToFirebase
                addAddress(location);
            }

        });
    }

    private void addAddress(Location location) {
        // สร้างชุดข้อมูล List จาก Class Address
        List<Address> addresses;
        geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

        try {
            // เรียกข้อมูล Address เข้าไปเก็บที่ List ข้อมูล
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            //String state = addresses.get(0).getAdminArea();
            String tambon = addresses.get(0).getSubLocality();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            String amphoe = addresses.get(0).getSubAdminArea();

            // save data to Firebase
            String uploadID = myRef.push().getKey();
            // Init Value to Model Class
            ModelAddress modelAddress = new ModelAddress(uploadID,location.getLatitude(), location.getLongitude(),
                    address,city,tambon,postalCode,country,amphoe,knownName);
            // Write data to Firebase Database
            myRef.child(uploadID).setValue(modelAddress);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }
}
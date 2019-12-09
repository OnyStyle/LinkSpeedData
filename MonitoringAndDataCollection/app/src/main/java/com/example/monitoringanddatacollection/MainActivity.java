package com.example.monitoringanddatacollection;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    LocationManager lm;
    Location location;
    Sensor pressure;
    SensorManager mngr;
    SensorEventListener listener;
    Geocoder gcd;
    List<Address> addresses;
    List locationValue = new ArrayList();
    LocationListener locationListener;
    WifiManager wf;
    WifiInfo wifiInfo;
    double longitude;
    double latitude;
    double altitude;
    int linkSpeed;
    int dataPoint;
    Context context;
    File file;
    File path;
    String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView wifiStrength =(TextView)findViewById(R.id.wifiStr);
        final Button calcButton = (Button) findViewById(R.id.calculateButton);
        final Button addData = (Button) findViewById(R.id.AddData);
        wf = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wf.isWifiEnabled() == false){
            wf.setWifiEnabled(true);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        }
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        mngr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (mngr.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            pressure = mngr.getDefaultSensor(Sensor.TYPE_PRESSURE);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }

        if (listener == null) {
            mngr.registerListener(listener, pressure, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        else {
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        locationValue.add(latitude);
        locationValue.add(longitude);
        locationValue.add((altitude));
        gcd = new Geocoder(MainActivity.this, Locale.getDefault());


        if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locationListener = new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    altitude = location.getAltitude();
                    wifiInfo = wf.getConnectionInfo();

                    try {
                        gcd.getFromLocation(latitude, longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        addresses = gcd.getFromLocation(latitude, longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

        }

        if(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
        {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, locationListener);
        }
        else {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,10,0, locationListener);
        }
        try {
            gcd.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        context = getApplicationContext();
        path = context.getExternalFilesDir(null);
        file = new File(path, "collected.txt");
        dataPoint = 1;





        calcButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (data != null){
                    writeToFile(data, file);
                }
                //wifiStrength.setText("" + level + " boop " +addresses.get(0).getAddressLine(0));

            }
        });
        addData.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                wifiInfo = wf.getConnectionInfo();
                linkSpeed = wifiInfo.getLinkSpeed();
                if(data != null){
                    data = data + ""+ dataPoint +" Link Speed:" +linkSpeed +" Address is: " + addresses.get(0).getAddressLine(0) +"\n";
                    dataPoint++;
                }
                else{
                    data =  dataPoint +" Link Speed:" +linkSpeed +" Address is: " + addresses.get(0).getAddressLine(0) +"\n";
                    dataPoint++;
                }
                if (data != null){
                    wifiStrength.setText(data);
                }



            }
        });






    }
    private void writeToFile(String dataM ,File file) {
        try {

            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(dataM.getBytes());
            } finally {
                stream.close();
            }
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}

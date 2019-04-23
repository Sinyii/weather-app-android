package com.sinyi.weatherapptab;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.sinyi.weatherapptab.ui.main.SectionsPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.sinyi.weatherapptab.App.CHANNEL_1_ID;
import static com.sinyi.weatherapptab.App.CHANNEL_2_ID;

public class MainActivity extends AppCompatActivity implements
        CurrentTemp.OnFragmentInteractionListener,
        Daily.OnFragmentInteractionListener,
        Hourly.OnFragmentInteractionListener{

    public static String CITY = "Boston";
    public static String WEATHERMAP_API_KEY = "YOUR API KEY";
    public static final int APICALL_UPPERBOUND = 60;
    public static int CALLCOUNT = 0;
    public static int CURRENTDEGREE = 0;
    public static int ABOVE = -1;
    public static String NOTIFIDEGREE = "0";
    private NotificationManagerCompat notificationManager;
    private Handler myHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        notificationManager = NotificationManagerCompat.from(this);

        myHandler = new android.os.Handler();
        myHandler.postDelayed(updateTimerThread, 0);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.locateMe:
                locate();
                Toast.makeText(this, "Current city:" + CITY, Toast.LENGTH_SHORT).show();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                return true;
            case R.id.notification:
                Toast.makeText(this, "notification is selected", Toast.LENGTH_SHORT).show();
                Intent settingIntent = new Intent(this, SettingActivity.class);
                startActivity(settingIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void locate(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                !=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            try {
                CITY = getLocation(location.getLatitude(), location.getLongitude());
            } catch (Exception e){
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "City not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1000:{
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    try {
                        CITY = getLocation(location.getLatitude(), location.getLongitude());
                    } catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "City not found!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private String getLocation(double lat, double loc){
        String cityName = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try{
            addresses = geocoder.getFromLocation(lat, loc, 10);
            if(addresses.size() > 0){
                for(Address adr: addresses){
                    if( adr.getLocality() != null && adr.getLocality().length() > 0){
                        cityName = adr.getLocality();
                        break;
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void sendOnChannel1(View v) {
        String title = "Notification!";
        String message;
        if (ABOVE == 1) {
            message = "Current degree is above your setting value.";
        } else {
            message ="Current degree is below your setting value.";

        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_sentiment_very_satisfied_black_24dp)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1, notification);
    }

    //TODO: Check the senOnChannel1 call, its parameter might be wrong
    public void checkNotification(){
        if(ABOVE == 1){
            if(CURRENTDEGREE > Integer.parseInt(NOTIFIDEGREE)){
                sendOnChannel1(getWindow().getDecorView().getRootView());
                //Toast.makeText(this, "A msg should send: Above " + NOTIFIDEGREE, Toast.LENGTH_SHORT).show();

            }
        }
        else if(ABOVE == 2){
            if(CURRENTDEGREE < Integer.parseInt(NOTIFIDEGREE)){
                sendOnChannel1(getWindow().getDecorView().getRootView());
                //Toast.makeText(this, "A msg should send: Below" + NOTIFIDEGREE, Toast.LENGTH_SHORT).show();

            }
        }
    }

    private Runnable updateTimerThread = new Runnable()
    {
        public void run()
        {
            checkNotification();
            myHandler.postDelayed(this, 5000); // 5 secs
        }
    };
}
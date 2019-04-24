package com.sinyi.weatherapptab;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
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
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class CurrentTemp extends Fragment {
    private String city = MainActivity.CITY;
    private String WEATHERMAP_API_KEY = MainActivity.WEATHERMAP_API_KEY;
    private TextView cityNameField, detailField, currentTemperatureField,
            humidityField, pressureField, weatherIcon, updatedField;
    private ProgressBar progressBar;
    private Typeface weatherFont;

    // Useless now
    private static final String ARG_PARAM1 = "param1";

    private OnFragmentInteractionListener mListener;

    public CurrentTemp() {
        // Required empty public constructor
    }

    // Useless constructor, keep for future use
    public static CurrentTemp newInstance(String param1) {
        CurrentTemp fragment = new CurrentTemp();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            city = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_current_temp, container, false);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        cityNameField = (TextView) v.findViewById(R.id.cityNameField);
        updatedField = (TextView) v.findViewById(R.id.updatedField);
        detailField = (TextView) v.findViewById(R.id.detailField);
        currentTemperatureField = (TextView) v.findViewById(R.id.currentTemperatureField);
        humidityField = (TextView) v.findViewById(R.id.humidityField);
        pressureField = (TextView) v.findViewById(R.id.pressureField);
        weatherIcon = (TextView) v.findViewById(R.id.weatherIcon);
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weathericons-regular-webfont.ttf");
        weatherIcon.setTypeface(weatherFont);
        // Get city's weather data
        taskLoadUp(city);

        // Inflate the layout for this fragment
        return v;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    // Connect with openweathermap
    public void taskLoadUp(String query) {
        if (Function.isNetworkAvailable(getActivity().getApplicationContext())) {
            DownloadWeather task = new DownloadWeather();
            task.execute(query);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

    class DownloadWeather extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            // If the number of api call is bigger than its upper bound,
            if (MainActivity.CALLCOUNT >= MainActivity.APICALL_UPPERBOUND){
                MainActivity.CALLCOUNT = 0;
                try {
                    // pause the process for 1 min.
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            String xml = Function.excuteGet("https://api.openweathermap.org/data/2.5/weather?q=" + args[0] +
                    "&units=imperial&appid=" + WEATHERMAP_API_KEY);
            if (xml == null) {
                return "";
            }
            // Count api calls, prevent this app to exceed the limitation of free trial.
            MainActivity.CALLCOUNT++;
            return xml;
        }

        // Process data
        @Override
        protected void onPostExecute(String xml) {
            try {
                JSONObject json = new JSONObject(xml);
                if (json != null) {
                    JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = json.getJSONObject("main");
                    DateFormat df = DateFormat.getDateTimeInstance();

                    cityNameField.setText(json.getString("name").toUpperCase(Locale.US) + ", " + json.getJSONObject("sys").getString("country"));
                    detailField.setText(details.getString("description").toUpperCase(Locale.US));
                    currentTemperatureField.setText(String.format("%.2f", main.getDouble("temp")) + "°F");
                    humidityField.setText("Humidity: " + main.getString("humidity") + "%");
                    pressureField.setText("Pressure: " + main.getString("pressure") + " hPa");
                    updatedField.setText(df.format(new Date(json.getLong("dt") * 1000)));
                    weatherIcon.setText(Html.fromHtml(Function.setWeatherIcon(details.getInt("id"),
                            json.getJSONObject("sys").getLong("sunrise") * 1000,
                            json.getJSONObject("sys").getLong("sunset") * 1000)));

                    progressBar.setVisibility(View.GONE);
                    // This class variable of MainActiviry will influence the notification.
                    MainActivity.CURRENTDEGREE = (int)main.getDouble("temp");
                }
            } catch (JSONException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Error, Check City: " + city, Toast.LENGTH_SHORT).show();
            }
        }
    }
}

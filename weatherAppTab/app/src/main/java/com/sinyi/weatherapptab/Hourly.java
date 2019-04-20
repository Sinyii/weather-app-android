package com.sinyi.weatherapptab;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Hourly.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Hourly#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Hourly extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener {
    String city = MainActivity.CITY;
    String WEATHERMAP_API_KEY = MainActivity.WEATHERMAP_API_KEY;
    double[] temp, maxTemp, minTemp;
    String[] time, date;
    LineChart mChart;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final int FORECAST_HOURS = 24;

    // TODO: Rename and change types of parameters
    private String mParam1;

    private OnFragmentInteractionListener mListener;

    public Hourly() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static Hourly newInstance(String param1) {
        Hourly fragment = new Hourly();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //city = getArguments().getString(ARG_PARAM1);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_hourly, container, false);
        temp = new double[FORECAST_HOURS];
        maxTemp = new double[FORECAST_HOURS];
        minTemp = new double[FORECAST_HOURS];
        date = new String[FORECAST_HOURS];
        time = new String[FORECAST_HOURS];

        mChart = (LineChart) v.findViewById(R.id.linecChart);
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);

        taskLoadUp(city);

        // Inflate the layout for this fragment
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
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

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

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
            super.onPreExecute();
            /*
            progressBar.setVisibility(View.VISIBLE);
            */

        }

        protected String doInBackground(String... args) {
            String xml = Function.excuteGet("https://api.openweathermap.org/data/2.5/forecast/hourly?q=" + args[0] +
                    "&units=imperial&appid=" + WEATHERMAP_API_KEY);
            if (xml == null) {
                return "";
            }
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            try {
                JSONObject json = new JSONObject(xml);
                if (json != null) {
                    JSONArray hrList = json.getJSONArray("list");
                    for (int i = 0; i < hrList.length() && i < FORECAST_HOURS; i++) {
                        JSONObject hr = hrList.getJSONObject(i);
                        JSONObject main = hr.getJSONObject("main");
                        temp[i] = main.getDouble("temp");
                        maxTemp[i] = main.getDouble("temp_max");
                        minTemp[i] = main.getDouble("temp_min");
                        String[] tmp = hr.getString("dt_txt").split(" ");
                        date[i] = tmp[0];
                        time[i] = tmp[1];
                    }


                    mChart.setDragEnabled(true);
                    mChart.setScaleEnabled(false);
                    mChart.getDescription().setText("X:hr, Y:°F");


                    ArrayList<Entry> yValues = new ArrayList<>();
                    ArrayList<Entry> yValuesMax = new ArrayList<>();
                    ArrayList<Entry> yValuesMin = new ArrayList<>();

                    int count = 0;
                     for(int i=0; i<FORECAST_HOURS || i<time.length ; i++){
                        String[] tmp = time[i].split(":");
                        yValues.add(new Entry(count, (float)temp[i]));
                        yValuesMax.add(new Entry(count, (float)maxTemp[i]));
                        yValuesMin.add(new Entry(count, (float)minTemp[i]));
                        count++;
                    }


                    LineDataSet set1 = new LineDataSet(yValues, "Hourly Temperature");
                    //LineDataSet set2 = new LineDataSet(yValuesMax, "Max");
                    //LineDataSet set3 = new LineDataSet(yValuesMin, "Min");

                    set1.setFillAlpha(110);

                    //set2.setColor(Color.GREEN);
                    //set3.setColor(Color.MAGENTA);

                    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                    dataSets.add(set1);
                    //dataSets.add(set2);
                    //dataSets.add(set3);

                    LineData data = new LineData(dataSets);


                    taskLoadUp(city);

                    mChart.setData(data);

                }
            } catch (JSONException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Error, Check hourly City:" + city, Toast.LENGTH_SHORT).show();
            }
        }
    }
}

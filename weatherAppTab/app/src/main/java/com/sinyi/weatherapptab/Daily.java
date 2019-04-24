package com.sinyi.weatherapptab;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
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
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Daily.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Daily#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Daily extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener {
    private String city = MainActivity.CITY;
    private String WEATHERMAP_API_KEY = MainActivity.WEATHERMAP_API_KEY;
    private double[] dTemp, dMaxTemp, dMinTemp;
    private String[] dDate, dTime;
    private LineChart mChartDaily;
    // Upper bound of hours
    private static final int FORECAST_HOURS = 96;

    private static final String ARG_PARAM1 = "param1";

    private OnFragmentInteractionListener mListener;

    public Daily() {
        // Required empty public constructor
    }
    // Useless constructor, keep for future use
    public static Daily newInstance(String param1) {
        Daily fragment = new Daily();
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
        View v = inflater.inflate(R.layout.fragment_daily, container, false);
        dTemp = new double[FORECAST_HOURS];
        dMaxTemp = new double[FORECAST_HOURS];
        dMinTemp = new double[FORECAST_HOURS];
        dDate = new String[FORECAST_HOURS];
        dTime = new String[FORECAST_HOURS];

        mChartDaily = (LineChart)v.findViewById(R.id.linecChartDaily);
        mChartDaily.setOnChartGestureListener(this);
        mChartDaily.setOnChartValueSelectedListener(this);
        mChartDaily.setDragEnabled(true);
        mChartDaily.setScaleEnabled(false);
        mChartDaily.setNoDataText("Tap to load data.");
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
        void onFragmentInteraction(Uri uri);
    }

    // Connect with openweathermap
    public void taskLoadUp(String query) {
        if (Function.isNetworkAvailable(getActivity().getApplicationContext())) {
            Daily.DownloadWeather task = new Daily.DownloadWeather();
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
            //progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();

        }

        protected String doInBackground(String... args) {
            String xml = Function.excuteGet("https://api.openweathermap.org/data/2.5/forecast/hourly?q=" + args[0] +
                    "&units=imperial&appid=" + WEATHERMAP_API_KEY);
            if (xml == null) {
                return "";
            }
            // Count api calls, prevent this app to exceed the limitation of free trial.
            MainActivity.CALLCOUNT++;
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            try {
                JSONObject json = new JSONObject(xml);
                DateFormat df = DateFormat.getDateTimeInstance();

                if (json != null) {
                    JSONArray hrList = json.getJSONArray("list");
                    for (int i = 0; i < hrList.length() && i < FORECAST_HOURS; i++) {
                        JSONObject hr = hrList.getJSONObject(i);
                        JSONObject main = hr.getJSONObject("main");
                        dTemp[i] = main.getDouble("temp");
                        dMaxTemp[i] = main.getDouble("temp_max");
                        dMinTemp[i] = main.getDouble("temp_min");
                        String[] tmp = hr.getString("dt_txt").split(" ");
                        dDate[i] = tmp[0];
                        dTime[i] = tmp[1];
                    }

                    mChartDaily.getDescription().setText("X:date, Y:°F");
                    // temp
                    ArrayList<Entry> entries = new ArrayList<>();
                    // max temp
                    //ArrayList<Entry> entriesMax = new ArrayList<>();
                    // min temp
                    //ArrayList<Entry> entriesMin = new ArrayList<>();

                    final int[] dateLabel = new int[FORECAST_HOURS/24+1];
                    int preDay = 0;
                    int dateCount = 0;
                    for (int i = 0; i < FORECAST_HOURS || i < dDate.length; i++) {
                        String[] tmp = dDate[i].split("-");
                        int currentDay = Integer.valueOf(tmp[2]);
                        if(currentDay != preDay){
                            entries.add(new Entry(currentDay, (float) dTemp[i]));
                            //entriesMax.add(new Entry(currentDay, (float) dMaxTemp[i]));
                            //entriesMin.add(new Entry(currentDay, (float) dMinTemp[i]));
                            preDay = currentDay;
                            dateLabel[dateCount] = currentDay;
                            dateCount++;
                        }

                    }

                    XAxis  xAxis = mChartDaily.getXAxis();
                    //xAxis.setAxisMinimum(dateLabel[0]);
                    //xAxis.setAxisMaximum(dateLabel[dateLabel.length-1]); // because there are 250 data points
                    xAxis.setGranularity(1);
                    xAxis.setLabelCount(dateCount); // if you want to display 0, 5 and 10s which are 3 values then put 3 else whatever of your choice.

                    LineDataSet set1 = new LineDataSet(entries, "Dayly Temperature");
                    //LineDataSet set2 = new LineDataSet(entriesMax, "Max");
                    //LineDataSet set3 = new LineDataSet(entriesMin, "Min");



                    set1.setFillAlpha(110);
                    set1.setColor(Color.BLUE);
                    //set2.setColor(Color.GREEN);
                    //set3.setColor(Color.MAGENTA);

                    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                    dataSets.add(set1);
                    //dataSets.add(set2);
                    //dataSets.add(set3);

                    LineData data = new LineData(dataSets);

                    mChartDaily.setData(data);

                }
            } catch (JSONException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Error: No chart data.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

package com.example.myChart;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Jawmie on 27/04/2016.
 */
public class TempChart extends Activity {
    Button graphTemp;
    private ArrayList dates = new ArrayList();
    private LineChart lineChart;

    private final int[] tempColour = {
            Color.rgb(255, 0, 0)
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_temp);

        graphTemp = (Button) findViewById(R.id.graphTemp);

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.main_id);
        rl.setBackgroundColor(Color.WHITE);

        lineChart = (LineChart) findViewById(R.id.chart);
        lineChart.setDrawGridBackground(true);
        lineChart.setDescription("Temperature Graph");
        lineChart.setData(new LineData());

        graphTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                    new HttpAsyncTask().execute("http://10.12.25.168:8080/android");
                } else {
                    Toast.makeText(getApplicationContext(), "Not Connected", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String jsonString = "";
            try {
                jsonString = HttpUtils.urlContentPost(params[0]);
                Log.d("url",jsonString.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return jsonString;
        }

        protected void onPostExecute(String result){
            ArrayList <Double>temps = new ArrayList<Double>();
            ArrayList <String>room = new ArrayList<String>();
            String theName = "Collective Temperatures";

            JSONArray jsonArray;
            JSONObject jsonObject = new JSONObject();

            try {
                jsonArray = new JSONArray(result);
                for(int i = 0; i < jsonArray.length(); i ++){
                    jsonObject = jsonArray.getJSONObject(i);
                    dates.add(jsonObject.getString("datelogged"));
                    temps.add(jsonObject.getDouble("temperature"));
                    room.add(jsonObject.getString("room"));

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("values", temps.toString());

            addDataSet(temps, theName, dates, tempColour);

        }
    }

    private void addDataSet(ArrayList data, String name, ArrayList dates, int[] colour) {
        LineData values = lineChart.getData();

        Log.d("values",data.toString());
        if(values != null){
            // Entry Objects
            ArrayList<Entry> y = new ArrayList<Entry>();

            if(values.getXValCount() == 0){
                for(int i = 0; i< data.size(); i ++)
                    values.addXValue("" + dates.get(i).toString()); // add divisions to x axis
            }
            try {
                if(data.get(0).toString().contains(".")){
                    for (int i = 0; i < data.size(); i++) {
                        y.add(new Entry((float) Double.parseDouble(data.get(i).toString()), i));
                        Log.d("y", y.toString());
                    }
                }
            }catch(NumberFormatException NFE){NFE.printStackTrace();}
            LineDataSet set = new LineDataSet(y,name);


            set.setColors(colour);
            set.setLineWidth(2.0f);
            set.setCircleRadius(4.0f);
            set.setCircleColors(colour);
            set.setValueTextSize(10f);
            set.setDrawValues(false);
            values.addDataSet(set);

            lineChart.fitScreen();
            lineChart.notifyDataSetChanged();
            lineChart.animateX(1500);
            lineChart.invalidate();
        }
    }
/*
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(4, 0));
        entries.add(new Entry(8, 1));
        entries.add(new Entry(6, 2));
        entries.add(new Entry(2, 3));
        entries.add(new Entry(18, 4));
        entries.add(new Entry(9, 5));
        entries.add(new Entry(4, 6));

        LineDataSet dataset = new LineDataSet(entries, "# of Calls");

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("January");
        labels.add("February");
        labels.add("March");
        labels.add("April");
        labels.add("May");
        labels.add("June");
        labels.add("July");

        LineData data = new LineData(labels, dataset);
        dataset.setColors(ColorTemplate.JOYFUL_COLORS); //
        dataset.setDrawCubic(true);
        dataset.setDrawFilled(true);

        lineChart.setData(data);
        lineChart.animateY(5000);

    }*/
}
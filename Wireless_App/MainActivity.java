package com.example.myChart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Jawmie on 26/04/2016.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Button for viewing Temperature chart
    public void temp_act(View view){
        Intent tempAct = new Intent(this, TempChart.class);
        startActivity(tempAct);
    }

    // Button for viewing Light chart
    public void light_act(View view){
        Intent lightAct = new Intent(this, LightChart.class);
        startActivity(lightAct);
    }

}
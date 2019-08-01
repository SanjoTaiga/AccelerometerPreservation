package com.example.accelerometer;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.accelerometer.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.Locale;


public class MainActivity extends Activity
        implements SensorEventListener, View.OnClickListener {

    private SensorManager sensorManager;
    private Sensor accel;
    private TextView textView;

    private LineChart mChart;
    private String[] labels = new String[]{
            "linear_accelerationX",
            "linear_accelerationY",
            "linear_accelerationZ"};
    private int[] colors = new int[]{
            Color.BLUE,
            Color.GRAY,
            Color.MAGENTA};

    private boolean lineardata = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 縦画面
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get an instance of the TextView
        textView = findViewById(R.id.text_view);


        mChart = findViewById(R.id.chart);
        // インスタンス生成
        mChart.setData(new LineData());
        // no description text
        mChart.getDescription().setEnabled(false);
        // Grid背景色
        mChart.setDrawGridBackground(true);
        // 右側の目盛り
        mChart.getAxisRight().setEnabled(false);

        Button buttonStart = findViewById(R.id.button_start);
        buttonStart.setOnClickListener(this);

        Button buttonStop = findViewById(R.id.button_stop);
        buttonStop.setOnClickListener(this);

        Button buttonChange = findViewById(R.id.button_change);
        buttonChange.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_start:
                sensorManager.registerListener(this, accel,
                        SensorManager.SENSOR_DELAY_NORMAL);
                break;
            case R.id.button_stop:
                sensorManager.unregisterListener(this);
                break;
            case R.id.button_change:
                if(lineardata){
                    lineardata = false;
                }
                else{
                    lineardata = true;
                }
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Listenerの登録
        accel = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accel,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    // 解除するコードも入れる!
    @Override
    protected void onPause() {
        super.onPause();
        // Listenerを解除
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float gravity[] = new float[3];
        float linear_acceleration[] = new float[3];

        final float alpha = 0.6f;


        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {

            // alpha is calculated as t / (t + dT)
            // with t, the low-pass filter's time-constant
            // and dT, the event delivery rate

            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            String accelero;

            if(!lineardata){
                accelero = String.format(Locale.US,
                        "X: %.3f\nY: %.3f\nZ: %.3f",
                        event.values[0],event.values[1],event.values[2]);
            }
            else {
                accelero = String.format(Locale.US,
                        "X: %.3f\nY: %.3f\nZ: %.3f",
                        gravity[0],gravity[1],gravity[2]);
            }

            textView.setText(accelero);


            LineData data = mChart.getLineData();

            if(data != null){
                for(int i = 0; i < 3; i++){
                    ILineDataSet set3 = data.getDataSetByIndex(i);
                    if(set3 == null){
                        LineDataSet set = new LineDataSet(null, labels[i]);
                        set.setLineWidth(2.0f);
                        set.setColor(colors[i]);
                        // liner line
                        set.setDrawCircles(false);
                        // no values on the chart
                        set.setDrawValues(false);
                        set3 = set;
                        data.addDataSet(set3);
                    }

                    // data update
                    if(!lineardata){
                        data.addEntry(new Entry(set3.getEntryCount(), event.values[i]), i);
                    }
                    else{
                        data.addEntry(new Entry(set3.getEntryCount(), linear_acceleration[i]), i);
                    }

                    data.notifyDataChanged();
                }

                mChart.notifyDataSetChanged(); // 表示の更新のために変更を通知する
                mChart.setVisibleXRangeMaximum(50); // 表示の幅を決定する
                mChart.moveViewToX(data.getEntryCount()); // 最新のデータまで表示を移動させる
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
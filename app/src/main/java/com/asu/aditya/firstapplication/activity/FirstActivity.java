package com.asu.aditya.firstapplication.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.asu.aditya.firstapplication.R;
import com.asu.aditya.firstapplication.services.AccelerometerService;
import com.asu.aditya.firstapplication.views.GraphView;

/**
 * Created by group22 on 9/5/16.
 */

public class FirstActivity extends Activity implements View.OnClickListener {
    private static final String TAG = FirstActivity.class.getCanonicalName();
    /**
     * Variable Array for GraphView
     * verlabel : Background Height Values
     * horlabel : Background Width Values
     * values : Max Values of Foreground Active Graph
     */
    private float[] values = new float[60];
    private String[] verticalLabels = new String[]{"20", "18", "16", "14", "12", "10", "8", "6", "4", "2", "0",};
    private String[] horizontalLabels = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private GraphView graphView;
    private LinearLayout graph;
    private boolean runnable = false;
    private Button btnStartGraph, btnStopGraph;
    private Toolbar toolbar;
    private Intent serviceIntent;

    //Random number assigned for message which is our group number
    private static final int CLOCK_TICK = 22;

    private String patient_name, patient_age, patient_id, patient_sex;
    private EditText etPatientName, etPatientAge, etPatientId;
    private RadioGroup sexRadioGroup;
    private RadioButton btnRadioMale, btnRadioFemale;
    private AccelerometerService mAccelerometerService;
    private Boolean mBound;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        graph = (LinearLayout) findViewById(R.id.graph);
        btnStartGraph = (Button) findViewById(R.id.start_graph);
        btnStopGraph = (Button) findViewById(R.id.stop_graph);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        etPatientAge = (EditText) findViewById(R.id.patient_age);
        etPatientName = (EditText) findViewById(R.id.patient_name);
        etPatientId = (EditText) findViewById(R.id.patient_id);
        sexRadioGroup = (RadioGroup) findViewById(R.id.radioSex);
        btnRadioMale = (RadioButton) findViewById(R.id.radioMale);
        btnRadioFemale = (RadioButton) findViewById(R.id.radioFemale);

        toolbar.setTitle("Group 22 - Assignment 2");
        serviceIntent = new Intent(FirstActivity.this, AccelerometerService.class);
        graphView = new GraphView(FirstActivity.this, horizontalLabels, verticalLabels, GraphView.LINE);
        graphView.setValues(values);
        graphView.setTitle(null);
        btnStartGraph.setOnClickListener(this);
        btnStopGraph.setOnClickListener(this);
        btnStartGraph.setEnabled(true);
        btnStopGraph.setEnabled(true);
        graph.addView(graphView);

        //star Service
        startService(serviceIntent);

    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mServiceConnection);
    }

    ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(TAG, "OnServiceConnected");
            AccelerometerService.LocalBinder localBinder = (AccelerometerService.LocalBinder) service;
            mAccelerometerService = localBinder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };


    /*
    Stop the background thread when activity is
    destroyed or when user press back button
     */

    @Override
    public void onDestroy() {
        super.onDestroy();
        runnable = false;
    }


    /*
    setGraph method will receive the value from handler
    and append the value to values[] array.
    After that values array is set to GraphView's values
     */
    public void setGraph(int data) {
        for (int i = 0; i < values.length - 1; i++) {
            values[i] = values[i + 1];
        }

        values[values.length - 1] = (float) data;
        graphView.setValues(values);
        graphView.invalidate();
    }

    /*
    This handler of the main thread receives the CLOCK_TICK
    message from the background thread and generate a random
    value using Math.random function between range of 0 to 600
     */
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {

                case CLOCK_TICK:
                    int testValue = (int) (Math.random() * 600);
                    setGraph(testValue);
                    break;
            }
        }
    };

    /*
    RunGraph is a Class that extends Thread Class
    This thread generates a trigger to its Handler
    after every 500ms to generate a random value.
     */
    public class RunGraph extends Thread {
        @Override
        public void run() {
            while (runnable) {
                handler.sendEmptyMessage(CLOCK_TICK);
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /*
    This is overridden function from View.OnClickListener interface
    with following Functionalities :
    1.) Start or Stop the thread by setting the value of runnable
    2.) Set the UI of different components accordingly.

     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_graph:
                if (validateInputs()) {
                    runnable = true;
                    Toast.makeText(this, "Graph Running", Toast.LENGTH_SHORT).show();
                    new RunGraph().start();
                    graphView.setTitle(patient_name);
                    btnStartGraph.setEnabled(false);
                    etPatientAge.setEnabled(false);
                    etPatientName.setEnabled(false);
                    etPatientId.setEnabled(false);
                    sexRadioGroup.setEnabled(false);
                    btnRadioMale.setEnabled(false);
                    btnRadioFemale.setEnabled(false);
                    btnStopGraph.setEnabled(true);
                }
                break;
            case R.id.stop_graph:
                //test code
                if (true) {
                    stopService(serviceIntent);
                    return;
                }
                //test code ends
                runnable = false;
                Toast.makeText(this, "Graph Cleared", Toast.LENGTH_SHORT).show();
                values = new float[60];
                graphView.setValues(values);
                graphView.setTitle(null);
                graphView.invalidate();
                btnStartGraph.setEnabled(true);
                etPatientAge.setEnabled(true);
                etPatientName.setEnabled(true);
                etPatientId.setEnabled(true);
                sexRadioGroup.setEnabled(true);
                btnRadioMale.setEnabled(true);
                btnRadioFemale.setEnabled(true);
                btnStopGraph.setEnabled(false);
                break;
        }
    }

    /*
    * validateInputs() function is used to check whether
    * user has filled every field or not
    *
    * return false if any of the field is empty and true otherwise
    */
    private Boolean validateInputs() {
        patient_age = etPatientAge.getText().toString();
        patient_id = etPatientId.getText().toString();
        patient_name = etPatientName.getText().toString();
        switch (sexRadioGroup.getCheckedRadioButtonId()) {
            case R.id.radioMale:
                patient_sex = "Male";
                break;
            case R.id.radioFemale:
                patient_sex = "Female";
                break;
        }

        if (patient_id.equals("") || patient_name.equals("") || patient_sex.equals("") || patient_age.equals("")) {
            Toast.makeText(this, "Please fill inputs first!!", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }

    }
}
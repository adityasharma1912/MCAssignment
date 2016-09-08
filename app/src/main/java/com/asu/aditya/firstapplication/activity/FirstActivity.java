package com.asu.aditya.firstapplication.activity;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.asu.aditya.firstapplication.R;
import com.asu.aditya.firstapplication.views.GraphView;

public class FirstActivity extends Activity implements View.OnClickListener {
    /**
     * Variable Array for GraphView
     * verlabel : Background Height Values
     * horlabel : Background Width Values
     * values : Max Values of Foreground Active Graph
     */
    private float[] values = new float[60];
    private String[] verticalLabels = new String[]{"600", "500", "400", "300", "200", "100", "80", "60", "40", "20", "0",};
    private String[] horizontalLabels = new String[]{"0", "10", "20", "30", "40", "50", "60"};
    private GraphView graphView;
    private LinearLayout graph;
    private boolean runnable = false;
    private Button btnStartGraph, btnStopGraph;
    private Toolbar toolbar;

    private String patient_name, patient_age, patient_id, patient_sex;
    private EditText etPatientName, etPatientAge, etPatientId;
    private RadioGroup sexRadioGroup;


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

        toolbar.setTitle("Group 22 - Assignment 1");
        graphView = new GraphView(FirstActivity.this, horizontalLabels, verticalLabels, GraphView.LINE);
        graphView.setValues(values);
        graphView.setTitle(null);
        btnStartGraph.setOnClickListener(this);
        btnStopGraph.setOnClickListener(this);
        btnStartGraph.setEnabled(true);
        btnStopGraph.setEnabled(false);
        graph.addView(graphView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        runnable = false;
    }

    public void setGraph(int data) {
        for (int i = 0; i < values.length - 1; i++) {
            values[i] = values[i + 1];
        }

        values[values.length - 1] = (float) data;
        graphView.setValues(values);
        graphView.invalidate();
    }

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {

                case 0x01:
                    int testValue = (int) (Math.random() * 600) + 1;
                    setGraph(testValue);
                    break;
            }
        }
    };

    public class RunGraph extends Thread {
        @Override
        public void run() {
            while (runnable) {
                handler.sendEmptyMessage(0x01);
                try {
                    Thread.sleep(300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_graph:
                if (validateInputs()) {
                    runnable = true;
                    new RunGraph().start();
                    graphView.setTitle(patient_name);
                    btnStartGraph.setEnabled(false);
                    etPatientAge.setEnabled(false);
                    etPatientName.setEnabled(false);
                    etPatientId.setEnabled(false);
                    sexRadioGroup.setEnabled(false);
                    btnStopGraph.setEnabled(true);
                }
                break;
            case R.id.stop_graph:
                runnable = false;
                values = new float[60];
                graphView.setValues(values);
                graphView.setTitle(null);
                graphView.invalidate();
                btnStartGraph.setEnabled(true);
                etPatientAge.setEnabled(true);
                etPatientName.setEnabled(true);
                etPatientId.setEnabled(true);
                sexRadioGroup.setEnabled(true);
                btnStopGraph.setEnabled(false);
        }
    }

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
            Toast.makeText(this, "AGE : " + patient_age + " ID : " + patient_id + " NAME : " + patient_name + " SEX : " + patient_sex, Toast.LENGTH_LONG).show();
            return true;
        }

    }
}
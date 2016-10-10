package com.asu.aditya.firstapplication.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.asu.aditya.firstapplication.R;
import com.asu.aditya.firstapplication.database.PatientDbHelper;
import com.asu.aditya.firstapplication.services.AccelerometerService;
import com.asu.aditya.firstapplication.services.DownloadFileAsyncTask;
import com.asu.aditya.firstapplication.services.UploadFileAsyncTask;
import com.asu.aditya.firstapplication.views.GraphView;

/**
 * Created by group22 on 10/5/16.
 */

public class PatientGraphActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = PatientGraphActivity.class.getCanonicalName();
    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 8503;
    /**
     * Variable Array for GraphView
     * verticalLabels : Background Height Values
     * horizontalLabel : Background Width Values
     * values : Max Values of Foreground Active Graph
     */
    private float[] values = new float[10];
    private String[] verticalLabels = new String[]{"20", "18", "16", "14", "12", "10", "8", "6", "4", "2", "0",};
    private String[] horizontalLabels = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private GraphView graphView;
    private LinearLayout graph;
    private Button btnStartGraph, btnStopGraph, btnUploadDb, btnDownloadDb;
    private Toolbar toolbar;
    private Intent serviceIntent;

    private String patient_name, patient_age, patient_id, patient_sex;
    private EditText etPatientName, etPatientAge, etPatientId;
    private RadioGroup sexRadioGroup;
    private RadioButton btnRadioMale, btnRadioFemale;
    private AccelerometerService mAccelerometerService;
    private Boolean mBound;
    private Spinner valueSpinner;
    int whichAxis = 0; //x-axis values are default.

    ProgressDialog mProgressDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        graph = (LinearLayout) findViewById(R.id.graph);
        btnStartGraph = (Button) findViewById(R.id.start_graph);
        btnStopGraph = (Button) findViewById(R.id.stop_graph);
        btnUploadDb = (Button) findViewById(R.id.upload_db);
        btnDownloadDb = (Button) findViewById(R.id.download_db);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        etPatientAge = (EditText) findViewById(R.id.patient_age);
        etPatientName = (EditText) findViewById(R.id.patient_name);
        etPatientId = (EditText) findViewById(R.id.patient_id);
        sexRadioGroup = (RadioGroup) findViewById(R.id.radioSex);
        btnRadioMale = (RadioButton) findViewById(R.id.radioMale);
        btnRadioFemale = (RadioButton) findViewById(R.id.radioFemale);
        valueSpinner = (Spinner) findViewById(R.id.value_spinner);
        valueSpinner.setOnItemSelectedListener(this);

        toolbar.setTitle("Group 22 - Assignment 2");
        mProgressDialog = new ProgressDialog(this);
        serviceIntent = new Intent(PatientGraphActivity.this, AccelerometerService.class);
        graphView = new GraphView(PatientGraphActivity.this, horizontalLabels, verticalLabels, GraphView.LINE);
        graphView.setValues(values);
        graphView.setTitle(null);
        btnStartGraph.setOnClickListener(this);
        btnStopGraph.setOnClickListener(this);
        btnUploadDb.setOnClickListener(this);
        btnDownloadDb.setOnClickListener(this);
        btnStartGraph.setEnabled(true);
        btnStopGraph.setEnabled(false);
        btnUploadDb.setEnabled(true);
        btnDownloadDb.setEnabled(true);
        graph.addView(graphView);
        storagePermissionCheck();

        //star Service
        startService(serviceIntent);

    }

    private void storagePermissionCheck() {
        if (ContextCompat.checkSelfPermission(PatientGraphActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PatientGraphActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSIONS_REQUEST_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(PatientGraphActivity.this,"Thanks for granting storage permission",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PatientGraphActivity.this,"Sorry but storage permissions are necessary",Toast.LENGTH_SHORT).show();
                    PatientGraphActivity.this.finish();
                }
                return;
            }
        }

    }

    private void uploadDatabase() {
        final UploadFileAsyncTask uploadDb = new UploadFileAsyncTask(PatientGraphActivity.this, mProgressDialog);
        final String databasePath = PatientDbHelper.DATABASE_NAME;
//        uploadDb.execute("https://impact.asu.edu/"+appName,appName);
        uploadDb.execute(databasePath);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                uploadDb.cancel(true);
            }
        });
    }

    private void downloadDatabase() {
        final DownloadFileAsyncTask uploadDb = new DownloadFileAsyncTask(PatientGraphActivity.this, mProgressDialog);
        final String databasePath = PatientDbHelper.DATABASE_NAME;
        final String databaseNameToDownload = "group22.db";
        uploadDb.execute(databaseNameToDownload, databasePath);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                uploadDb.cancel(true);
            }
        });
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
            mAccelerometerService.setHandler(handler);
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
        stopService(serviceIntent);
    }


    /*
    setGraph method will receive the value from handler
    and append the value to values[] array.
    After that values array is set to GraphView's values
     */
    public void setGraph(float data) {
        for (int i = 0; i < values.length - 1; i++) {
            values[i] = values[i + 1];
        }

        values[values.length - 1] = data + 10; //10 added because accelerometer values vary between
        // -10 to10 hence keeping everything +ve and in range of 0 to 20
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

                case AccelerometerService.CLOCK_TICK:
                    float testValue = msg.getData().getFloat("AxisValue");
                    Log.v(TAG, "Update Value Received : " + testValue);
                    setGraph(testValue);
                    break;

                case AccelerometerService.CLEAR_GRAPH:
                    clearGraphAndResetUI();
            }
        }
    };


    /*
    Method to reset variables and User interface.
     */
    private void clearGraphAndResetUI() {
        Toast.makeText(this, "Graph Cleared", Toast.LENGTH_SHORT).show();
        values = new float[10];
        graphView.setValues(values);
        graphView.setTitle(null);
        graphView.invalidate();
        btnStartGraph.setEnabled(true);
        etPatientAge.setEnabled(true);
        etPatientName.setEnabled(true);
        etPatientId.setEnabled(true);
        valueSpinner.setEnabled(true);
        sexRadioGroup.setEnabled(true);
        btnRadioMale.setEnabled(true);
        btnRadioFemale.setEnabled(true);
        btnUploadDb.setEnabled(true);
        btnDownloadDb.setEnabled(true);
        btnStopGraph.setEnabled(false);
    }

    /*
    Method to fetch data of last 10 seconds from downloaded database.
     */
    private void fetchPreviousData(String tableName, int whichAxis) {
        values = mAccelerometerService.fetchInitialSetOfValues(tableName, whichAxis);
        //setting fetched data on GraphView...
        for (int i = 0; i < values.length; i++)
            values[i] += 10;
        graphView.setValues(values);
        graphView.invalidate();
        if (mAccelerometerService != null)
            mAccelerometerService.startFetchingData();
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
                    graphView.setTitle(patient_name);
                    btnStartGraph.setEnabled(false);
                    etPatientAge.setEnabled(false);
                    etPatientName.setEnabled(false);
                    etPatientId.setEnabled(false);
                    valueSpinner.setEnabled(false);
                    sexRadioGroup.setEnabled(false);
                    btnRadioMale.setEnabled(false);
                    btnRadioFemale.setEnabled(false);
                    btnUploadDb.setEnabled(false);
                    btnDownloadDb.setEnabled(false);
                    btnStopGraph.setEnabled(true);
                    String tableName = patient_name + "_"
                            + patient_id + "_" + patient_age + "_" + patient_sex;
                    fetchPreviousData(tableName, whichAxis);
                }
                break;
            case R.id.stop_graph:
                if (mAccelerometerService != null)
                    mAccelerometerService.stopFetchingData();
                break;
            case R.id.upload_db:
                uploadDatabase();
                break;
            case R.id.download_db:
                downloadDatabase();
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.v(TAG, "position : " + position);
        whichAxis = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.v(TAG, "Nothing Selected");
    }
}
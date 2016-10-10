package com.asu.aditya.firstapplication.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.asu.aditya.firstapplication.database.PatientDbHelper;

/**
 * Created by aditya on 10/1/16.
 */
public class AccelerometerService extends Service implements SensorEventListener {

    private static final String TAG = AccelerometerService.class.getCanonicalName();
    private SensorManager accelerometerManage;
    private Sensor senseAccelerometer;
    private IBinder mBinder = new LocalBinder();
    private SensorEvent mSensorEvent;
    private Boolean doFetchData;
    private Handler mHandler;
    private String tableName;
    private int whichAxis;
    PatientDbHelper patientDbHelper;
    //Random number assigned for message which is our group number
    public static final int CLOCK_TICK = 22;
    public static final int CLEAR_GRAPH = 23;

    public class LocalBinder extends Binder {
        public AccelerometerService getService() {
            return AccelerometerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //create database in external directory...
        patientDbHelper = new PatientDbHelper(getApplicationContext());
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        accelerometerManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAccelerometer = accelerometerManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void startFetchingData() {
        doFetchData = true;
        accelerometerManage.registerListener(this, senseAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        //start thread for saving accelerometer data...
        FetchAccelerometerDataThread threadObj = new FetchAccelerometerDataThread();
        threadObj.start();
    }

    public void stopFetchingData() {
        doFetchData = false;
        accelerometerManage.unregisterListener(this);
    }

    public float[] fetchInitialSetOfValues(String tableName, int whichValue) {
        this.tableName = tableName;
        this.whichAxis = whichValue;
        float value[] = patientDbHelper.getPatientData(tableName, whichValue);
        return value;
    }

    private class FetchAccelerometerDataThread extends Thread {
        @Override
        public void run() {
            while (doFetchData) {
                try {
                    /*
                    Since frequency is 1Hz there fore sleep of 1 second is introduced.
                     */
                    Thread.sleep(1000);
                    Message msg = mHandler.obtainMessage();
                    msg.what = CLOCK_TICK;
                    Bundle bundle = new Bundle();
                    SensorEvent sensorEvent = AccelerometerService.this.mSensorEvent;
                    float[] value = sensorEvent.values;
                    long timeStamp = sensorEvent.timestamp;
                    bundle.putFloat("AxisValue", value[whichAxis]);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                    //save value in database;
                    patientDbHelper.insertPatientData(tableName, value, timeStamp);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Message msg = mHandler.obtainMessage();
            msg.what = CLEAR_GRAPH;
            mHandler.sendMessage(msg);
        }
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mSensorEvent = event;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {

    }
}

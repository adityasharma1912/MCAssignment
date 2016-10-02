package com.asu.aditya.firstapplication.services;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by aditya on 10/1/16.
 */
public class AccelerometerService extends Service implements SensorEventListener {

    private static final String TAG = AccelerometerService.class.getCanonicalName();
    private SensorManager accelerometerManage;
    private Sensor senseAccelerometer;
    private IBinder mBinder = new LocalBinder();
    SQLiteDatabase mDataBase;
    private SensorEvent mSensorEvent;
    private Boolean doFetchData;
    private Handler mHandler;
    private String tableName;
    private int whichAxis;
    //Random number assigned for message which is our group number
    public static final int CLOCK_TICK = 22;

    public class LocalBinder extends Binder {
        public AccelerometerService getService() {
            return AccelerometerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            String externalStorageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
            mDataBase = SQLiteDatabase.openOrCreateDatabase(externalStorageDirectory + "/databaseFolder/group22Db", null);
        } catch (SQLiteException se) {
            se.printStackTrace();
        }
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
        float savedXValues[] = new float[10];
        float savedYValues[] = new float[10];
        float savedZValues[] = new float[10];
        float value[] = new float[10];
        Cursor cursor;
        boolean tableExists = false;
        String[] projection = {"xValue", "yValue", "zValue"};
        String sortBy = "timeStamp DESC";

        try {
            cursor = mDataBase.query(tableName, projection,
                    null, null, null, null, sortBy);
            cursor.moveToFirst();
            int loopCount = (cursor.getCount() < 10) ? cursor.getCount() : 10;
            for (int i = 9; i > (9 - loopCount); i--) {
                savedXValues[i] = cursor.getFloat(cursor.getColumnIndex("xValue"));
                savedYValues[i] = cursor.getFloat(cursor.getColumnIndex("yValue"));
                savedZValues[i] = cursor.getFloat(cursor.getColumnIndex("zValue"));
            }

            tableExists = true;
        } catch (Exception e) {
            //table doesn't exist... Create new table with tableName...
            createTableIfNotExist(tableName);
            Log.d(TAG, tableName + " doesn't exist");
        }
        switch (whichValue) {
            case 0:
                value = savedXValues;
                break;
            case 1:
                value = savedYValues;
                break;
            case 2:
                value = savedZValues;
                break;
        }
        return value;
    }

    private void createTableIfNotExist(String tableName) {
        try {
            mDataBase.beginTransaction();
            try {
                mDataBase.execSQL("create table if not exists " + tableName + " ("
                        + " recordID integer PRIMARY KEY autoincrement, "
                        + " xValue float, "
                        + " yValue float, "
                        + " zValue float, "
                        + " timeStamp double ); ");

                mDataBase.setTransactionSuccessful();
                //populate data to database...
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                mDataBase.endTransaction();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateDataToDb(float[] value, long timeStamp) {
        try {
            mDataBase.beginTransaction();
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put("xValue", value[0]);
                contentValues.put("yValue", value[1]);
                contentValues.put("zValue", value[2]);
                contentValues.put("timeStamp", timeStamp);
                mDataBase.insert(this.tableName, null, contentValues);
                //populate data to database...
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                mDataBase.endTransaction();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private class FetchAccelerometerDataThread extends Thread {
        @Override
        public void run() {
            while (doFetchData) {
                try {
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
                    populateDataToDb(value, timeStamp);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.v(TAG, "onSensorChanged");
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mSensorEvent = event;
//            Log.v(TAG, "timestamp = " + event.timestamp + "value array length" + event.values.length);
//            Log.v(TAG, "X = " + event.values[0]);
//            Log.v(TAG, "Y = " + event.values[1]);
//            Log.v(TAG, "Z = " + event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {

    }
}

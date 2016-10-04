package com.asu.aditya.firstapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

/**
 * Created by aditya on 10/2/16.
 */
public class PatientDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    private static final String TAG = PatientDbHelper.class.getCanonicalName();
    private static String externalStorageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String DATABASE_NAME = externalStorageDirectory + "/databaseFolder/group22.db";
    public static final String PATIENT_COLUMN_RECORD_ID = "recordID";
    public static final String PATIENT_COLUMN_X_VALUE = "xValue";
    public static final String PATIENT_COLUMN_Y_VALUE = "yValue";
    public static final String PATIENT_COLUMN_Z_VALUE = "zValue";
    public static final String PATIENT_COLUMN_TIMESTAMP = "timeStamp";

    public PatientDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(TAG, "onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(TAG, "onUpgrade");
    }

    public void createPatientTable(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("create table if not exists " + tableName + " ("
                + PATIENT_COLUMN_RECORD_ID + " integer PRIMARY KEY autoincrement, "
                + PATIENT_COLUMN_X_VALUE + " float, "
                + PATIENT_COLUMN_Y_VALUE + " float, "
                + PATIENT_COLUMN_Z_VALUE + " float, "
                + PATIENT_COLUMN_TIMESTAMP + " double ); ");
    }

    public boolean insertPatientData(String tableName, float[] values, long timeStamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PATIENT_COLUMN_X_VALUE, values[0]);
        contentValues.put(PATIENT_COLUMN_Y_VALUE, values[1]);
        contentValues.put(PATIENT_COLUMN_Z_VALUE, values[2]);
        contentValues.put(PATIENT_COLUMN_TIMESTAMP, timeStamp);
        db.insert(tableName, null, contentValues);
        return true;
    }

    public float[] getPatientData(String tableName, int whichValue) {
        SQLiteDatabase db = this.getReadableDatabase();
        float savedXValues[] = new float[10];
        float savedYValues[] = new float[10];
        float savedZValues[] = new float[10];
        float value[] = new float[10];
        Cursor cursor;
        String[] projection = {"xValue", "yValue", "zValue"};
        String sortBy = "timeStamp DESC";

        try {
            cursor = db.query(tableName, projection,
                    null, null, null, null, sortBy);
            cursor.moveToFirst();
            int loopCount = (cursor.getCount() < 10) ? cursor.getCount() : 10;
            for (int i = 9; i > (9 - loopCount); i--) {
                savedXValues[i] = cursor.getFloat(cursor.getColumnIndex("xValue"));
                savedYValues[i] = cursor.getFloat(cursor.getColumnIndex("yValue"));
                savedZValues[i] = cursor.getFloat(cursor.getColumnIndex("zValue"));
                cursor.moveToNext();
            }
        } catch (Exception e) {
            //table doesn't exist... Create new table with tableName...
            createPatientTable(tableName);
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

    public int numberOfRows(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, tableName);
        return numRows;
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
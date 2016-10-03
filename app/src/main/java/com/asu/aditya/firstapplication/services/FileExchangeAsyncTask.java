package com.asu.aditya.firstapplication.services;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by aditya on 10/2/16.
 */

//async task params, progress and result...
public class FileExchangeAsyncTask extends AsyncTask<String, Integer, String> {
    private static final String TAG = FileExchangeAsyncTask.class.getCanonicalName();
    private Context context;
    private ProgressDialog mProgressDialog;
    private PowerManager.WakeLock mWakeLock;
    public static final String uploadServerUrl = "http://192.168.0.19/UploadToServer.php";

    public FileExchangeAsyncTask(Context context, ProgressDialog progressDialog) {
        this.context = context;
        mProgressDialog = progressDialog;

    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
        mProgressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        mProgressDialog.dismiss();
        if (result != null) {
            Toast.makeText(context, "Upload error: " + result, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "File Uploaded Successfully", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected String doInBackground(String... params) {
//        InputStream input = null;
//        OutputStream output = null;
//        OutputStream outputStream = null;
        DataOutputStream dataOutputStream = null;
        HttpURLConnection connection = null;
        FileInputStream fileInputStream = null;
        int bytesRead, bytesAvailable, bufferSize;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        byte[] buffer;
        int maxBufferSize = 4 * 1024;
        String sourceFileUri = params[0];
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        }};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new SecureRandom());

//            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            File sourceFile = new File(sourceFileUri);
            if (!sourceFile.isFile()) {
                return null;
            }

            fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(uploadServerUrl);  // providing url to upload file
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true); // Allow Inputs
            connection.setDoOutput(true); // Allow Outputs
            connection.setUseCaches(false); // Don't use a Cached Copy
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("ENCTYPE", "multipart/form-data");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connection.setRequestProperty("uploaded_file",sourceFileUri);

            //creating new dataoutputstream
            dataOutputStream = new DataOutputStream(connection.getOutputStream());

            //writing bytes to data outputstream
            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + sourceFileUri + "\"" + lineEnd);

            dataOutputStream.writeBytes(lineEnd);


            bytesAvailable = fileInputStream.available();
            int totalBytesAvailable = bytesAvailable;
            int bytesSent = 0;

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dataOutputStream.write(buffer, 0, bufferSize);
                bytesSent += bufferSize;
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                publishProgress((int) (bytesSent * 100 / totalBytesAvailable));
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();

            Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                String str =  "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
                Log.v(TAG,str);
            }
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (dataOutputStream != null)
                    dataOutputStream.close();
                if (fileInputStream != null)
                    fileInputStream.close();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
            if (connection != null)
                connection.disconnect();
        }
        return null;


//        try {
//            URL url = new URL(uploadServerUrl);
//            connection = (HttpsURLConnection) url.openConnection();
//            connection.connect();
//
//            // expect HTTP 200 OK, so we don't mistakenly save error report
//            // instead of the file
//            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
//                return "Server returned HTTP " + connection.getResponseCode()
//                        + " " + connection.getResponseMessage();
//            }
//
//            //downloadButton.setText(Integer.toString(fileLength));
//            // download the file
//            input = new FileInputStream(sourceFileUri);
//            output = connection.getOutputStream();
//
//
//
//            bytesAvailable = fileInputStream.available();
//            int totalBytesAvailable = bytesAvailable;
//            int bytesSent = 0;
//
//            bufferSize = Math.min(bytesAvailable, maxBufferSize);
//            buffer = new byte[bufferSize];
//
//            // read file and write it into form...
//            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//            while (bytesRead > 0) {
//                if (isCancelled()) {
//                    input.close();
//                    return "uploading cancelled";
//                }
//                output.write(buffer, 0, bufferSize);
//                bytesSent += bufferSize;
//                bytesAvailable = fileInputStream.available();
//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                publishProgress((int) (bytesSent * 100 / totalBytesAvailable));
//                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//            }
//
//            //downloadButton.setText("Connecting .....");
////            byte data[] = new byte[4096];
////            long total = 0;
////            int count;
////            while ((count = input.read(data)) != -1) {
////                // allow canceling with back button
////                if (isCancelled()) {
////                    input.close();
////                    return "uploading cancelled";
////                }
////                total += count;
////                // publishing the progress....
////                if (fileLength > 0) // only if total length is known
////                    publishProgress((int) (total * 100 / fileLength));
////                output.write(data, 0, count);
////            }
//        } catch (Exception e) {
//            return e.toString();
//        } finally {
//            try {
//                if (output != null)
//                    output.close();
//                if (input != null)
//                    input.close();
//            } catch (IOException ignored) {
//                return ignored.toString();
//            }
//
//            if (connection != null)
//                connection.disconnect();
//        }
//        return null;
    }
}

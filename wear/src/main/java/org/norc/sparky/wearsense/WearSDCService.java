/*
 * Copyright 2014 NORC at the University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.norc.sparky.wear.sense;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.data.FreezableUtils;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
// import com.google.android.gms.wearable.DataEventBuffer;
// import com.google.android.gms.wearable.MessageApi;
// import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
// import com.google.android.gms.wearable.WearableListenerService;
// import com.google.android.gms.wearable.Wearable;
// import com.google.android.gms.wearable.WearableListenerService;

import com.google.android.gms.wearable.*;

// SDC = Sensor Data Collection

public class WearSDCService
    extends WearableListenerService
    implements SensorEventListener {
    // extends Service

    private String TAG = "WearSDCService";

    private boolean mInitialized;
    private float mLastX, mLastY, mLastZ;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private final float NOISE = (float) 2.0;


    private int SDC_START = 1;
    private int SDC_STOP = 2;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private GoogleApiClient mGoogleApiClient;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
	public ServiceHandler(Looper looper) {
	    super(looper);
	}
	@Override
	public void handleMessage(Message msg) {
	    if (msg.arg1 == SDC_START) {
		Log.i(TAG, "starting sensor data collection");
		startSDC();
	    } else if (msg.arg1 == SDC_STOP) {
		Log.i(TAG, "stopping SDC service");
		mSensorManager.unregisterListener(WearSDCService.this);
		// Stop the service using the startId, so that we don't stop
		// the service in the middle of handling another job
		// stopSelf(msg.arg1);
	    }
	}
    }

    @Override
    public void onCreate() {
	super.onCreate();

	Log.v(TAG, "onCreate");

	mGoogleApiClient = new GoogleApiClient.Builder(this)
	    .addConnectionCallbacks(new ConnectionCallbacks() {
		    @Override
		    public void onConnected(Bundle connectionHint) {
			Log.d(TAG, "onConnected: " + connectionHint);
			// Now you can use the data layer API
			// syncDesc();
		    }
		    @Override
		    public void onConnectionSuspended(int cause) {
			Log.d(TAG, "onConnectionSuspended: " + cause);
		    }
		})
	    .addOnConnectionFailedListener(new OnConnectionFailedListener() {
		    @Override
		    public void onConnectionFailed(ConnectionResult result) {
			Log.d(TAG, "onConnectionFailed: " + result);
		    // 	if (mResolvingError) {
		    // 	    // Already attempting to resolve an error.
		    // 	    return;
		    // 	} else if (result.hasResolution()) {
		    // 	    try {
		    // 		mResolvingError = true;
		    // 		result.startResolutionForResult(mActivity,
		    // 						REQUEST_RESOLVE_ERROR);
		    // 	    } catch (SendIntentException e) {
		    // 		// There was an error with the resolution intent. Try again.
		    // 		mGoogleApiClient.connect();
		    // 	    }
		    // 	} else {
		    // 	    // Show dialog using GooglePlayServicesUtil.getErrorDialog()
		    // 	    showErrorDialog(result.getErrorCode());
		    // 	    mResolvingError = true;
		    // 	}
		    }
		})
	    .addApi(Wearable.API)
	    .build();
        mGoogleApiClient.connect();

	mInitialized = false;
	mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

	// List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
	// listSensorType = new ArrayList<String>();
	// for(int i=0; i<deviceSensors.size(); i++){
	//     Log.i(TAG,
	// 	  "SENSOR: "
	// 	  // deviceSensors.get(i).getVendor()
	// 	  // + " " + deviceSensors.get(i).getName()
	// 	  // + " " + deviceSensors.get(i).getVersion()
	// 	  + ": " + deviceSensors.get(i).toString());
	//     listSensorType.add(deviceSensors.get(i).toString());
	// }

	// Start up the thread running the service.  Note that we create a
	// separate thread because the service normally runs in the process's
	// main thread, which we don't want to block.  We also make it
	// background priority so CPU-intensive work will not disrupt our UI.
	HandlerThread thread = new HandlerThread("ServiceStartArguments",
						 Process.THREAD_PRIORITY_BACKGROUND);
	thread.start();

	// Get the HandlerThread's Looper and use it for our Handler
	mServiceLooper = thread.getLooper();
	mServiceHandler = new ServiceHandler(mServiceLooper);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	Log.i(TAG, "onStartComment");
        return super.onStartCommand(intent, flags, startId);

	Toast.makeText(this,
		       "sensor data collection starting",
		       Toast.LENGTH_SHORT).show();

	// For each start request, send a message to start a job and deliver the
	// start ID so we know which request we're stopping when we finish the job
	Message msg = mServiceHandler.obtainMessage();
	msg.arg1 = SDC_START;
	mServiceHandler.sendMessage(msg);

	// If we get killed, after returning from here, restart
	return START_STICKY;
    }

    // @Override
    // public IBinder onBind(Intent intent) {
    // 	// We don't provide binding, so return null
    // 	return null;
    // }

    @Override
    public void onDestroy() {
	Toast.makeText(this,
		       "sensor data collection stopping",
		       Toast.LENGTH_SHORT).show();
	mSensorManager.unregisterListener(this);
	Message msg = mServiceHandler.obtainMessage();
	msg.arg1 = SDC_STOP;
	mServiceHandler.sendMessage(msg);
    }

    private void startSDC() {
	// sensor stuff

	mGoogleApiClient.connect();

	mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	mSensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_NORMAL);
    }

    // sensor callbacks
    // @Override
    // protected void onResume() {
    //     super.onResume();
    // 	Log.i(TAG, "onResume");
    //     mSensorManager.registerListener(this, mAccelerometer,
    // 					SensorManager.SENSOR_DELAY_NORMAL);
    // }

    // @Override
    // protected void onPause() {
    //     super.onPause();
    // 	Log.i(TAG, "onPause");
    //     mSensorManager.unregisterListener(this);
    // }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
	// can be safely ignored for this demo
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
	// TextView tvX= (TextView)findViewById(R.id.x_axis);
	// TextView tvY= (TextView)findViewById(R.id.y_axis);
	// TextView tvZ= (TextView)findViewById(R.id.z_axis);
	// ImageView iv = (ImageView)findViewById(R.id.image);
	float x = event.values[0];
	float y = event.values[1];
	float z = event.values[2];

	// mTextView.setText(Float.toString(x));

	if (!mInitialized) {
	    mLastX = x;
	    mLastY = y;
	    mLastZ = z;
	    // tvX.setText("0.0");
	    // tvY.setText("0.0");
	    // tvZ.setText("0.0");
	    mInitialized = true;
	} else {
	    float deltaX = Math.abs(mLastX - x);
	    float deltaY = Math.abs(mLastY - y);
	    float deltaZ = Math.abs(mLastZ - z);
	    if (deltaX < NOISE) deltaX = (float)0.0;
	    if (deltaY < NOISE) deltaY = (float)0.0;
	    if (deltaZ < NOISE) deltaZ = (float)0.0;
	    mLastX = x;
	    mLastY = y;
	    mLastZ = z;
	    // tvX.setText(Float.toString(deltaX));
	    // tvY.setText(Float.toString(deltaY));
	    // tvZ.setText(Float.toString(deltaZ));
	    // iv.setVisibility(View.VISIBLE);
	    // if (deltaX > deltaY) {
	    // 	iv.setImageResource(R.drawable.horizontal);
	    // } else if (deltaY > deltaX) {
	    // 	iv.setImageResource(R.drawable.vertical);
	    // } else {
	    // 	iv.setVisibility(View.INVISIBLE);
	    // }
	}

	if (mGoogleApiClient.isConnected()) {
	    // Log.i(TAG, "syncing sensor data");

	    PutDataMapRequest dataMap = PutDataMapRequest.create("/sensor/accelerometer");
	    dataMap.getDataMap().putFloat("x", x);
	    dataMap.getDataMap().putFloat("y", y);
	    dataMap.getDataMap().putFloat("z", z);
	    PutDataRequest request = dataMap.asPutDataRequest();
	    PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
		.putDataItem(mGoogleApiClient, request);

	Log.i(TAG, "put accelerometer: (" + x + "," + y + "," + z + ")");

	    // PutDataMapRequest dataMapRqst = PutDataMapRequest.create("/wearable/sensors");
	    // DataMap dataMap = dataMapRqst.getDataMap();

	    // for(int i=0 ; i<mAdapter.getCount() ; i++){
	    // 	String s = mAdapter.getItem(i);
	    // 	dataMap.putString(Integer.toString(i), "foo");
	    // }

	    // PutDataRequest request = dataMapRqst.asPutDataRequest();
	    // PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
	    // 	.putDataItem(mGoogleApiClient, request);
	    // Log.i(TAG, "putDataItem returned");
	} else {
            Log.e(TAG, "No connection to wearable available!");
        }
    }

    public void onMessageReceived(final MessageEvent messageEvent) {

	// MessageEvent methods:
	// abstract byte[]	getData();
	// abstract String	getPath();
	// abstract int		getRequestId();
	// abstract String	getSourceNodeId();
        Log.v(TAG, "Message received on wear: " + messageEvent.getPath());
    }
}

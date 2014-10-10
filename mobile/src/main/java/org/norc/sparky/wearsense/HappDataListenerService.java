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

package org.norc.sparky.wearsense;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
// import com.google.android.gms.wearable.DataItem;
// import com.google.android.gms.wearable.MessageApi;
// import com.google.android.gms.wearable.MessageEvent;
// import com.google.android.gms.wearable.Node;
// import com.google.android.gms.wearable.NodeApi;
// import com.google.android.gms.wearable.Wearable;
// import com.google.android.gms.wearable.WearableListenerService;
import com.google.android.gms.wearable.*;
// import com.google.android.gms.wearable.DataEventBuffer;
// import com.google.android.gms.wearable.MessageEvent;
// import com.google.android.gms.wearable.WearableListenerService;



public class HappDataListenerService extends WearableListenerService {

    private static final String TAG = "HappDataListenerService";
    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";

    private static TextView mTextView;

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

	Log.i(TAG, "onCreate");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        //  "onConnected: null" is normal.
                        //  There's nothing in our bundle.
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	Log.i(TAG, "onStartComment");
        return super.onStartCommand(intent, flags, startId);
	// Message msg = mServiceHandler.obtainMessage();
	// msg.arg1 = SDC_START;
	// mServiceHandler.sendMessage(msg);

	// // If we get killed, after returning from here, restart
	// return START_STICKY;

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        // if (Log.isLoggable(TAG, Log.DEBUG)) {
            // Log.i(TAG, "onDataChanged: " + dataEvents);
        // }
        final List events = FreezableUtils
                .freezeIterable(dataEvents);

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult =
                googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }

        // Loop through the events and send a message
        // to the node that created the data item.
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(TAG, "DataItem deleted: " + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
		Log.d(TAG, "onDataChanged URI:\n " + event.getDataItem().getUri());
		DataItem di = event.getDataItem();
		DataMapItem dmi = DataMapItem.fromDataItem(di);
		DataMap dm = dmi.getDataMap();
		Float x = dm.getFloat("x");
		Float y = dm.getFloat("y");
		Float z = dm.getFloat("z");

		Log.i(TAG,
		      "("
		      + x + ","
		      + y + ","
		      + z + ")");
	    }
	    // TODO: upload to server

	    // TODO: display on handset IF activity is running
	    // forward data to GUI thread
	    // DataMap.toBundle as intent extra?
	    // BETTER: addListener in main activity

	    // mTextView = (TextView) findViewById(R.id.txtAccel);
	    // mTextView.setText("Accel: (" + x + "," + y + "," + z + ")");


            // // Get the node id from the host value of the URI
            // String nodeId = uri.getHost();
            // // Set the data of the message to be the bytes of the URI.
            // byte[] payload = uri.toString().getBytes();

            // Send the RPC
            // Wearable.MessageApi.sendMessage(googleApiClient, nodeId,
	    // 				    DATA_ITEM_RECEIVED_PATH, payload);
        }
    }
}

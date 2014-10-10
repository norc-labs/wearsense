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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
// import com.google.android.gms.wearable.DataEventBuffer;
// import com.google.android.gms.wearable.MessageApi;
// import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
// import com.google.android.gms.wearable.WearableListenerService;
// import com.google.android.gms.wearable.Wearable;
// import com.google.android.gms.wearable.WearableListenerService;

import com.google.android.gms.wearable.*;

public class WearDataListenerService
    extends WearableListenerService
    implements MessageApi.MessageListener {

    private static final String TAG = "Wear/DataListener";

    public static final String PATH_SDC_START = "/sdc/start";
    public static final String PATH_SDC_STOP = "/sdc/stop";
    public static final String PATH_DEVDESC = "/device";

    // Intent Action
    // TODO:  use @string
    public static final String ACTION_DEVDESC = "org.norc.sparky.wearsense.WEARDEVDESC";

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
    }

// ################################################################
// ####  WearableListenerService Callbacks
// ################################################################

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        // if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.i(TAG, "onDataChanged: " + dataEvents);
        // }
        final List events = FreezableUtils
                .freezeIterable(dataEvents);

        // GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
        //         .addApi(Wearable.API)
        //         .build();

        // ConnectionResult connectionResult =
        //         googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        // if (!connectionResult.isSuccess()) {
        //     Log.e(TAG, "Failed to connect to GoogleApiClient.");
        //     return;
        // }

        // Loop through the events and send a message
        // to the node that created the data item.
        for (DataEvent event : dataEvents) {
            Uri uri = event.getDataItem().getUri();

            // Get the node id from the host value of the URI
            String nodeId = uri.getHost();
            // Set the data of the message to be the bytes of the URI.
            byte[] payload = uri.toString().getBytes();

            // Send the RPC
            // Wearable.MessageApi.sendMessage(googleApiClient, nodeId,
	    // 				    DATA_ITEM_RECEIVED_PATH, payload);
        }
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {

	// MessageEvent methods:
	// abstract byte[]	getData();
	// abstract String	getPath();
	// abstract int		getRequestId();
	// abstract String	getSourceNodeId();
        Log.v(TAG, "Message received on wear: " + messageEvent.getPath());

	if (messageEvent.getPath().startsWith(PATH_SDC_START))
	    {
		// TODO: launch SDC service in background
		Log.v(TAG, "starting Sensor Data Collection service");
		Intent i= new Intent(this, WearSDCService.class);
		// potentially add data to the intent
		i.putExtra("KEY1", "Value to be used by the service");
		this.startService(i);

	    }
	else if (messageEvent.getPath().startsWith(PATH_SDC_STOP))
	    {
		Log.v(TAG, "stopping Sensor Data Collection service");
		Intent i= new Intent(this, WearSDCService.class);
		// potentially add data to the intent
		this.stopService(i);
	    }
	else if (messageEvent.getPath().equals(PATH_DEVDESC))
	    {
		// launch WappActivity, which runs DevDesc task?
		// OR: since no user interaction needed,
		//     run an AsyncTask to get the dev desc
		Log.v(TAG, "launching WappActivity");

		// Intent startIntent = new Intent("android.intent.action.MAIN");
		// startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// if (startIntent.resolveActivity(getPackageManager()) != null) {
		//     startActivity(startIntent);
		// } else {
		//     Log.v(TAG, "resolveActivity failed" +
		// 	  startIntent
		// 	  .resolveActivity(getPackageManager())
		// 			   .toString());
		// }
	    }
    }

    @Override
    public void onPeerConnected(final Node peer) {
	Log.i(TAG, "onPeerDisconnected, node: " + peer.getId());
    }

    @Override
    public void onPeerDisconnected(final Node peer) {
	Log.i(TAG, "onPeerDisconnected, node: " + peer.getId());
    }
}

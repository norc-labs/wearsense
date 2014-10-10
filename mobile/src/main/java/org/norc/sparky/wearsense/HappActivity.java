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

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;

import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataApi.DataListener;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple launcher activity offering access to the individual samples in this project.
 */
public class HappActivity
    extends Activity
    implements AdapterView.OnItemClickListener {

    private static final String TAG = "HappActivity";

    public static final String PATH_SDC = "/sdc/";
    public static final String PATH_DEVDESC = "/device/";

    private GoogleApiClient mGoogleApiClient;

    private Sample[] mMobileSamples;
    private Sample[] mWearSamples;
    private ListView mMobileListView;
    private ListView mWearListView;

    private SDCListener mSDCListener = new SDCListener();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        //  "onConnected: null" is normal.
                        //  There's nothing in our bundle.
			// if (Log.isLoggable(TAG, Log.DEBUG)) {
                        Log.d(TAG, "onConnected: " + connectionHint);
			// }
			Wearable.DataApi.addListener(mGoogleApiClient,
						     mSDCListener);
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

	sendWearDevDescMsg();

	Switch switchWearSDC = (Switch) findViewById(R.id.switchWearSDC);
        switchWearSDC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
		String sdc = ((Switch) v).isChecked()? "start" : "stop";
                sendWearSDCMsg(sdc);

		// TODO:  save switch state on pause/stop, restore on resume

		// enable/disable DataApi.DataListener?

            }
        });

        // Prepare list of samples in this dashboard.
        mMobileSamples = new Sample[]{
            // new Sample(R.string.mobiledev_test_title,
	    // 	       R.string.mobiledev_test_description,
	    // 	       NavigationDrawerActivity.class),
        };
        mMobileListView = (ListView) findViewById(R.id.lvMobileDesc);
        mMobileListView.setAdapter(new MobileSampleAdapter());
        mMobileListView.setOnItemClickListener(this);

        // Prepare list of samples in this dashboard.
        // mWearSamples = new Sample[]{
        // };

	// mWearListView = (ListView) findViewById(R.id.lvWearDesc);
        // mWearListView.setAdapter(new WearSampleAdapter());
        // mWearListView.setOnItemClickListener(this);
    }

    @Override			// API:  Activity Lifecycle
    protected void onStart() {
        super.onStart();
	Log.d(TAG, "onStart");
        // if (!mResolvingError) {
            mGoogleApiClient.connect();
        // }
    }

    @Override			// API:  Activity Lifecycle
    protected void onRestart() {
        super.onRestart();
	Log.d(TAG, "onRestart");
    }

    @Override			// API:  Activity Lifecycle
    protected void onPause() {
	super.onPause();
	// TODO:  save settings (e.g. SDC switches)
	Log.d(TAG, "onPause");
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, mSDCListener);
	}
    }

    @Override			// API:  Activity Lifecycle
    protected void onResume() {
	super.onResume();
	// TODO:  restore settings (e.g. SDC switches)
	Log.d(TAG, "onResume");
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.addListener(mGoogleApiClient, mSDCListener);
	}
    }

    @Override			// API:  Activity Lifecycle
    protected void onStop() {
	super.onStop();		// always super first
	Log.d(TAG, "onStop");
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, mSDCListener);
            mGoogleApiClient.disconnect();
        }
    }

    @Override			// API:  Activity Lifecycle
    protected void onDestroy() {
	super.onDestroy();
    }

    ////////////////////////////////////////////////////////////////
    // API: AdapterView.OnItemClickListener
    @Override
    public void onItemClick(AdapterView<?> container,
			    View view,
			    int position,
			    long id) {
        startActivity(mMobileSamples[position].intent);
        // startActivity(mWearSamples[position].intent);
    }

    ////////////////////////////////////////////////////////////////
    // API:  HappActivity (this)
    private void sendWearSDCMsg(final String cmd) {

        new AsyncTask<Void, Void, List<Node>>(){

            @Override
            protected List<Node> doInBackground(Void... params) {
                return getNodes();
            }

            @Override
            protected void onPostExecute(List<Node> nodeList) {
                for(Node node : nodeList) {
                    Log.v("X" + TAG, "sending " + String.valueOf(cmd)
			  + " command to node " + node.getId());

                    PendingResult<MessageApi.SendMessageResult> result
			= Wearable.MessageApi.sendMessage(
							  mGoogleApiClient,
							  node.getId(),
							  PATH_SDC + String.valueOf(cmd),
							  null
							  );

                    result
			.setResultCallback
			(new ResultCallback<MessageApi.SendMessageResult>() {
				@Override
				public void onResult
				    (MessageApi.SendMessageResult sendMessageResult)
				{
				    Log.v(TAG, "send result: "
					  + sendMessageResult.getStatus().getStatusMessage());
				}
			    });
                }
            }
        }.execute();
    }

    private void sendWearDevDescMsg() {

        new AsyncTask<Void, Void, List<Node>>(){

            @Override
            protected List<Node> doInBackground(Void... params) {
                return getNodes();
            }

            @Override
            protected void onPostExecute(List<Node> nodeList) {
                for(Node node : nodeList) {
                    Log.v(TAG, "sending DEVDESC command to node " + node.getId());

                    PendingResult<MessageApi.SendMessageResult> result
			= Wearable.MessageApi.sendMessage(
							  mGoogleApiClient,
							  node.getId(),
							  PATH_DEVDESC,
							  null
							  );

                    result
			.setResultCallback
			(new ResultCallback<MessageApi.SendMessageResult>() {
				@Override
				public void onResult
				    (MessageApi.SendMessageResult sendMessageResult)
				{
				    Log.v(TAG, "send result: "
					  + sendMessageResult.getStatus().getStatusMessage());
				}
			    });
                }
            }
        }.execute();
    }

    private List<Node> getNodes() {
        List<Node> nodes = new ArrayList<Node>();
        NodeApi.GetConnectedNodesResult rawNodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : rawNodes.getNodes()) {
            nodes.add(node);
        }
        return nodes;
    }

    // ################
    private class SDCListener
	implements DataApi.DataListener {

	TextView mTextView;

	@Override
	public void onDataChanged(DataEventBuffer dataEvents) {
	    for (DataEvent event : dataEvents) {
		if (event.getType() == DataEvent.TYPE_DELETED) {
		    Log.d(TAG, "DataItem deleted: " + event.getDataItem().getUri());
		} else if (event.getType() == DataEvent.TYPE_CHANGED) {
		    // Log.d(TAG, "onDataChanged URI:\n " + event.getDataItem().getUri());
		    DataItem di = event.getDataItem();
		    DataMapItem dmi = DataMapItem.fromDataItem(di);
		    DataMap dm = dmi.getDataMap();
		    Float x = dm.getFloat("x");
		    Float y = dm.getFloat("y");
		    Float z = dm.getFloat("z");

		    final String xyz = "Accel: ( " + x + ",   " + y + ",   " + z + "  )";
		    Log.i(TAG, xyz);

		    // mWearListView = (ListView) findViewById(R.id.lvWearDesc);
		    // mWearListView.setAdapter(new WearSampleAdapter());
		    // mWearListView.setOnItemClickListener(this);

		    // some code #1
		    Thread t = new Thread("Thread1") {
			    @Override
			    public void run() {
				// some code #2
				HappActivity.this.runOnUiThread(new Runnable() {
					public void run() {
		    mTextView = (TextView)
			HappActivity.this.findViewById(R.id.txtAccel);
		    Log.d(TAG, "textview: " + mTextView.toString());
		    mTextView.setText(xyz);
					}
				    });

			    }
			};
		    t.start();
		}
	    }


	}
    }

    private class MobileSampleAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mMobileSamples.length;
        }

        @Override
        public Object getItem(int position) {
            return mMobileSamples[position];
        }

        @Override
        public long getItemId(int position) {
            return mMobileSamples[position].hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.sample_dashboard_item,
                        container, false);
            }

            ((TextView) convertView.findViewById(android.R.id.text1)).setText(
                    mMobileSamples[position].titleResId);
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(
                    mMobileSamples[position].descriptionResId);
            return convertView;
        }
    }

    private class WearSampleAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mWearSamples.length;
        }

        @Override
        public Object getItem(int position) {
            return mWearSamples[position];
        }

        @Override
        public long getItemId(int position) {
            return mWearSamples[position].hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.sample_dashboard_item,
                        container, false);
            }

            ((TextView) convertView.findViewById(android.R.id.text1)).setText(
                    mWearSamples[position].titleResId);
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(
                    mWearSamples[position].descriptionResId);
            return convertView;
        }
    }

    private class Sample {
        int titleResId;
        int descriptionResId;
        Intent intent;

        private Sample(int titleResId, int descriptionResId, Intent intent) {
            this.intent = intent;
            this.titleResId = titleResId;
            this.descriptionResId = descriptionResId;
        }

        private Sample(int titleResId, int descriptionResId,
                Class<? extends Activity> activityClass) {
            this(titleResId, descriptionResId,
                    new Intent(HappActivity.this, activityClass));
        }
    }
}

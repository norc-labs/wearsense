package org.norc.sparky.wearsense;

// for reading CPU info from linux in /proc
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
// import android.app.Fragment;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.Bundle;
import android.os.Build;

import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;

public class WappActivity
    extends Activity {
    // implements SensorEventListener {

    public static final String TAG = "WappActivity";

    private Activity mActivity;

    private float mLastX, mLastY, mLastZ;
    private boolean mInitialized;
    // private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private final float NOISE = (float) 2.0;

    private GoogleApiClient mGoogleApiClient;

    private TextView mTextView;

    private ListView mListView;

    private List<String> listSensorType;

    private ArrayAdapter<String> mAdapter;

    private Random ran = new Random();

   // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);

	mActivity = this;  // ????

        Log.i(TAG, "Ready");

	// mInitialized = false;
        // mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // mSensorManager.registerListener(this, mAccelerometer,
	// 				SensorManager.SENSOR_DELAY_NORMAL);

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

	// mAdapter = new ArrayAdapter<String>((Context) this,
	// 				    android.R.layout.simple_list_item_1,
	// 				    listSensorType);


	mGoogleApiClient = new GoogleApiClient.Builder(this)
	    .addConnectionCallbacks(new ConnectionCallbacks() {
		    @Override
		    public void onConnected(Bundle connectionHint) {
			Log.d(TAG, "onConnected: " + connectionHint);
			// Now you can use the data layer API
			syncDesc();
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
			if (mResolvingError) {
			    // Already attempting to resolve an error.
			    return;
			} else if (result.hasResolution()) {
			    try {
				mResolvingError = true;
				result.startResolutionForResult(mActivity,
								REQUEST_RESOLVE_ERROR);
			    } catch (SendIntentException e) {
				// There was an error with the resolution intent. Try again.
				mGoogleApiClient.connect();
			    }
			} else {
			    // Show dialog using GooglePlayServicesUtil.getErrorDialog()
			    showErrorDialog(result.getErrorCode());
			    mResolvingError = true;
			}
		    }
		})
	    .addApi(Wearable.API)
	    .build();

	// setListAdapter(adapter);
	// getListView().setTextFilterEnabled(true);

	// if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
	//   // Success! There's a magnetometer.
	//   }
	// else {
	//   // Failure! No magnetometer.
	//   }

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener()
	    {
		@Override
		public void onLayoutInflated(WatchViewStub stub) {

		    // mListView = (ListView) stub.findViewById(R.id.sensorList);
		    // mListView.setAdapter(mAdapter);

		    syncDesc();

		    // mTextView = (TextView) stub.findViewById(R.id.textMfg);
		    // mTextView.setText("Mfg: " + android.os.Build.MANUFACTURER);

		    // mTextView = (TextView) stub.findViewById(R.id.textBrand);
		    // mTextView.setText("Brand: " + android.os.Build.BRAND);

		    // mTextView = (TextView) stub.findViewById(R.id.textProduct);
		    // mTextView.setText("Product: " + android.os.Build.PRODUCT);

		    // mTextView = (TextView) stub.findViewById(R.id.textModel);
		    // mTextView.setText("Model: " + android.os.Build.MODEL);

		    // mTextView = (TextView) stub.findViewById(R.id.textSerial);
		    // mTextView.setText("Serial: " + android.os.Build.SERIAL);

		    // mTextView = (TextView) stub.findViewById(R.id.textHardware);
		    // mTextView.setText("Hardware: " + android.os.Build.HARDWARE);

		    // mTextView = (TextView) stub.findViewById(R.id.textCPU);
		    // mTextView.setText("CPU: " + getCPUInfo());

		    // mTextView = (TextView) stub.findViewById(R.id.textBoard);
		    // mTextView.setText("Board: " + android.os.Build.BOARD);

		    // mTextView = (TextView) stub.findViewById(R.id.textBootloader);
		    // mTextView.setText("Bootloader: " + android.os.Build.BOOTLOADER);

		    // mTextView = (TextView) stub.findViewById(R.id.textDevice);
		    // mTextView.setText("Device: " + android.os.Build.DEVICE);

		    // mTextView = (TextView) stub.findViewById(R.id.textDisplay);
		    // mTextView.setText("Display: " + android.os.Build.DISPLAY);

		    // mTextView = (TextView) stub.findViewById(R.id.textCPU_ABI);
		    // mTextView.setText("CPU ABI: " + android.os.Build.CPU_ABI);

		    // mTextView = (TextView) stub.findViewById(R.id.textCPU_ABI2);
		    // mTextView.setText("CPU ABI2: " + android.os.Build.CPU_ABI2);

		};
	    });
    }

    @Override
    protected void onStart() {
	Log.d(TAG, "onStart()");
        super.onStart();
        if (!mResolvingError) {  // more about this later
            mGoogleApiClient.connect();
        } else {
	    Log.d(TAG, "resolving error");
	}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (requestCode == REQUEST_RESOLVE_ERROR) {
	    mResolvingError = false;
	    if (resultCode == RESULT_OK) {
		// Make sure the app is not already connected or attempting to connect
		if (!mGoogleApiClient.isConnecting() &&
                    !mGoogleApiClient.isConnected()) {
		    mGoogleApiClient.connect();
		}
	    }
	}
    }

    @Override
    protected void onResume() {
        super.onResume();
	Log.i(TAG, "onResume");
        // mSensorManager.registerListener(this, mAccelerometer,
	// 				SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
	Log.i(TAG, "onPause");
        // mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
	Log.i(TAG, "onStop");
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    // // sensor callbacks
    // @Override
    // public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // 	// can be safely ignored for this demo
    // }

    // @Override
    // public void onSensorChanged(SensorEvent event) {
    // 	// TextView tvX= (TextView)findViewById(R.id.x_axis);
    // 	// TextView tvY= (TextView)findViewById(R.id.y_axis);
    // 	// TextView tvZ= (TextView)findViewById(R.id.z_axis);
    // 	// ImageView iv = (ImageView)findViewById(R.id.image);
    // 	float x = event.values[0];
    // 	float y = event.values[1];
    // 	float z = event.values[2];

    // 	// mTextView.setText(Float.toString(x));

    // 	if (!mInitialized) {
    // 	    mLastX = x;
    // 	    mLastY = y;
    // 	    mLastZ = z;
    // 	    // tvX.setText("0.0");
    // 	    // tvY.setText("0.0");
    // 	    // tvZ.setText("0.0");
    // 	    mInitialized = true;
    // 	} else {
    // 	    float deltaX = Math.abs(mLastX - x);
    // 	    float deltaY = Math.abs(mLastY - y);
    // 	    float deltaZ = Math.abs(mLastZ - z);
    // 	    if (deltaX < NOISE) deltaX = (float)0.0;
    // 	    if (deltaY < NOISE) deltaY = (float)0.0;
    // 	    if (deltaZ < NOISE) deltaZ = (float)0.0;
    // 	    mLastX = x;
    // 	    mLastY = y;
    // 	    mLastZ = z;
    // 	    // tvX.setText(Float.toString(deltaX));
    // 	    // tvY.setText(Float.toString(deltaY));
    // 	    // tvZ.setText(Float.toString(deltaZ));
    // 	    // iv.setVisibility(View.VISIBLE);
    // 	    // if (deltaX > deltaY) {
    // 	    // 	iv.setImageResource(R.drawable.horizontal);
    // 	    // } else if (deltaY > deltaX) {
    // 	    // 	iv.setImageResource(R.drawable.vertical);
    // 	    // } else {
    // 	    // 	iv.setVisibility(View.INVISIBLE);
    // 	    // }
    // 	}
    // }

    private void syncDesc() {
	if (mGoogleApiClient.isConnected()) {
	    Log.i(TAG, "syncing");

	    PutDataMapRequest dataMap = PutDataMapRequest.create("/count");
	    dataMap.getDataMap().putInt("COUNT", ran.nextInt());
	    PutDataRequest request = dataMap.asPutDataRequest();
	    PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
		.putDataItem(mGoogleApiClient, request);

	    // PutDataMapRequest dataMapRqst = PutDataMapRequest.create("/wearable/sensors");
	    // DataMap dataMap = dataMapRqst.getDataMap();

	    // for(int i=0 ; i<mAdapter.getCount() ; i++){
	    // 	String s = mAdapter.getItem(i);
	    // 	dataMap.putString(Integer.toString(i), "foo");
	    // }

	    // PutDataRequest request = dataMapRqst.asPutDataRequest();
	    // PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
	    // 	.putDataItem(mGoogleApiClient, request);
	    Log.i(TAG, "putDataItem returned");
	} else {
            Log.e(TAG, "No connection to wearable available!");
        }
    }

    // The rest of this code is all about building the error dialog

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        // dialogFragment.show(getSupportFragmentManager(), "errordialog");
        dialogFragment.show(getFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((WappActivity)getActivity()).onDialogDismissed();
        }
    }

    private String getCPUInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append("abi: ").append(Build.CPU_ABI).append("\n");
        if (new File("/proc/cpuinfo").exists()) {
            try {
                BufferedReader br = new BufferedReader(
						       new FileReader(new File("/proc/cpuinfo")));
                String aLine;
                while ((aLine = br.readLine()) != null) {
                    sb.append(aLine + "\n");
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}

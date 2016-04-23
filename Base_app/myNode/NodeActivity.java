/*****************************************************************************
 *
 *   Copyright(C) 2011, Embedded Artists AB
 *   All rights reserved.
 *
 ******************************************************************************
 * Software that is described herein is for illustrative purposes only
 * which provides customers with programming information regarding the
 * products. This software is supplied "AS IS" without any warranties.
 * Embedded Artists AB assumes no responsibility or liability for the
 * use of the software, conveys no license or title under any patent,
 * copyright, or mask work right to the product. Embedded Artists AB
 * reserves the right to make changes in the software without
 * notification. Embedded Artists AB also make no representation or
 * warranty that such application will be suitable for the specified
 * use without further testing or modification.
 *****************************************************************************/
package com.example.myNode;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

public class NodeActivity extends Activity implements Node.OnValueChangeListener, 
NodeList.OnListChangeListener {

	// Member object for Bluetooth Service
	private BluetoothService mBluetoothService = null;
	private static final String TAG = "BluetoothActivites";
	/**************************************************************************
	 * Private variables 
	 *************************************************************************/	
	
	private int nodeId = 0;
	private LinearLayout tempLayout;
	private LinearLayout roomLayout;
	private LinearLayout btnLayout;
	private LinearLayout lightLayout;
	public Button postTemp;
	private TextView nodeVals;
	private TextView tempValue;
	private TextView lightValue;
	private TextView btnValue;
	private EditText roomVal;

	private NodeList nodeList;
	
	private static final int VALUE_UPDATED = 0;

	/**
	 * String buffer for outgoing messages
	 */
	private StringBuffer mOutStringBuffer;

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	private Set<BluetoothDevice> pairedDevices;

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	/**************************************************************************
	 * Overridden methods 
	 *************************************************************************/	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.my_node);
        
        Log.i("EA AOA CAN", "NodeActivity.onCreate");
        
        tempLayout = (LinearLayout)findViewById(R.id.tempLayout);
		roomLayout = (LinearLayout)findViewById(R.id.room);
        btnLayout = (LinearLayout)findViewById(R.id.btnLayout);
        lightLayout = (LinearLayout)findViewById(R.id.lightLayout);
        
        tempValue = (TextView)findViewById(R.id.tempValue);
        lightValue = (TextView)findViewById(R.id.lightValue);
		roomVal = (EditText)findViewById(R.id.roomEnter);
        btnValue = (TextView)findViewById(R.id.btn1Value);

		nodeVals = (TextView)findViewById(R.id.nodeVals);
		postTemp = (Button)findViewById(R.id.postTemp);

        nodeList = NodeList.getInstance();

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			//   return;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		// If BT is not on, request that it be enabled.
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		} else if(mBluetoothService == null){
			setupBlue();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		Log.i("EA AOA CAN", "NodeActivity.onPause");
		
		nodeList.removeOnListChangeListener(this);
		
		Node n = nodeList.getNodeWithId(nodeId);
		if (n != null) {
			n.removeOnValueChangeListener(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

        Log.i("EA AOA CAN", "NodeActivity.onResume");

        // get the Node ID associated with this NodeActivity.
        Intent intent = getIntent();
        if (intent != null) {
        	nodeId = intent.getIntExtra("nodeId", -1);

			setTitle("Air Quality App");
        	        	
        	Node n = nodeList.getNodeWithId(nodeId);
        	int[] caps = n.getCapabilities();
        	enableLayouts(caps);
        	
        }		
		
    	nodeList.addOnListChangeListener(this);

        Node n = nodeList.getNodeWithId(nodeId);
    	n.addOnValueChangeListener(this);
    	
    	int[] caps = n.getCapabilities();

    	for (int i = 0; i < caps.length; i++) {
    		updateValue(n, caps[i]);
    	}

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
		if (mBluetoothService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't started already
			if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
				// Start the Bluetooth chat services
				mBluetoothService.start();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mBluetoothService != null) {
			mBluetoothService.stop();
		}
	}

	/**
	 * Makes this device discoverable.
	 */
	private void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() !=
				BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	private void setupBlue() {
		Log.d(TAG, "setupBlue()");



		// Initialize the BluetoothChatService to perform bluetooth connections
		mBluetoothService = new BluetoothService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	/**
	 * Sends a message.
	 *
	 * @param message A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
			Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mBluetoothService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			//mOutEditText.setText(mOutStringBuffer);
		}
	}

	/**
	 * A button has been clicked
	 */
	public void showInfo(View v) throws JSONException {

		//Intent mIntent = getIntent();
		String valT = tempValue.getText().toString();
		String valL = lightValue.getText().toString();
		String valR = roomVal.getText().toString();

		JSONObject inputTemp = new JSONObject();
		inputTemp.put("tempVal", valT);
		inputTemp.put("lightVal", valL);
		inputTemp.put("roomVal", valR);

		nodeVals.setText(valT + " " + valL);
		String baseUrl = "http://10.12.25.168:8080/AirServletMod";
		new HttpAsyncTask().execute(baseUrl, inputTemp.toString());
	}

	public void blueTx(View v) {//throws JSONException{
		//Intent mIntent = getIntent();
		/*String valT = tempValue.getText().toString();
		String valL = lightValue.getText().toString();
		String valR = roomVal.getText().toString();

		JSONObject inputTemp = new JSONObject();
		inputTemp.put("tempVal", valT);
		inputTemp.put("lightVal", valL);
		inputTemp.put("roomVal", valR);

		byte[] json_bytes = inputTemp.toString().getBytes();*/

		//ConnectedThread.write(json_bytes);
		String poopers = "this is poopers";

		sendMessage(poopers);
	}

	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls){
			String jsonString = null;

			try{
				jsonString = HttpUtils.urlContentPost(urls[0], "temperature", urls[1]);
			}catch(IOException e){
				e.printStackTrace();
			}
			return jsonString;
		}
	}
	/**
	 * A value has changed on the Node associated with this NodeActivity
	 */
	public void onValueChange(int capId) {
		Message m = Message.obtain(handler, VALUE_UPDATED);
        m.arg1 = capId;
		handler.sendMessage(m);						
	}
	
	/**
	 * The node list has changed.
	 */
	public void onListChange() {
		Node n = nodeList.getNodeWithId(nodeId);
        if (n == null) {
			// node associated with this activity has been removed
			finish();
		}
	}	

	/**************************************************************************
	 * Private methods  
	 *************************************************************************/	
	
	/**
	 * Enable layout/graphics for a specific capability/device
	 * 
	 * @param caps capabilities for the node
	 */	
	private void enableLayouts(int[] caps) {
		for (int i = 0; i < caps.length; i++) {
			switch (caps[i]) {
			case Node.DEV_TEMP:
				tempLayout.setVisibility(View.VISIBLE);
				break;
			case Node.DEV_LIGHT:
				lightLayout.setVisibility(View.VISIBLE);
				break;
			case Node.DEV_BTN:
				btnLayout.setVisibility(View.GONE);
				roomLayout.setVisibility(View.VISIBLE);
				break;
			}
		}
	}
	
	/**
	 * Update a value
	 */	
	private void updateValue(Node n, int capId) {
		switch (capId) {
		case Node.DEV_TEMP:
			tempValue.setText(n.getValue(capId));
			break;
		case Node.DEV_LIGHT:
			lightValue.setText(n.getValue(capId));
			break;
		case Node.DEV_BTN:
			btnValue.setText(n.getValue(capId));
			break;
		}		
	}

	/**************************************************************************
	 * Anonymous classes 
	 *************************************************************************/	

	/*
	 * The Android UI Toolkit is not thread-safe which means that only the 
	 * UI Thread may manipulate the User Interface. 
	 * 
	 * A Handler allows a thread to send a message to another thread's 
	 * message queue. When the Handler is created it will be associated
	 * with the thread that is creating the Handler. For the Handler below
	 * this means the UI thread.
	 * 
	 * Since messages from the Accessory are received by a separate thread
	 * those messages must be sent to the Handler below which is responsible
	 * for updating the UI.
	 */
	
	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			
			switch(msg.what) {
			case VALUE_UPDATED: 
								
				Node n = NodeList.getInstance().getNodeWithId(nodeId);
				updateValue(n, msg.arg1);
				//sendDat(n, msg.arg1);
				
				break;
			}
		}
	};

	/**
	 * The Handler that gets information back from the BluetoothChatService
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			//NodeActivity activity = getActivity();
			BluetoothDevice myBlue;
			switch (msg.what) {
				case Constants.MESSAGE_STATE_CHANGE:
					switch (msg.arg1) {
						case BluetoothService.STATE_CONNECTED:
							//setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
							//mConversationArrayAdapter.clear();
							for(BluetoothDevice bt : pairedDevices)

							Toast.makeText(getApplicationContext(),bt.getName(),Toast.LENGTH_SHORT).show();
							break;
						case BluetoothService.STATE_CONNECTING:
							//setStatus(R.string.title_connecting);
							Toast.makeText(getApplicationContext(),"Connecting State",Toast.LENGTH_SHORT).show();
							break;
						case BluetoothService.STATE_LISTEN:
						case BluetoothService.STATE_NONE:
							//setStatus(R.string.title_not_connected);
							Toast.makeText(getApplicationContext(),"Not Connected",Toast.LENGTH_SHORT).show();
							break;
					}
					break;
				case Constants.MESSAGE_WRITE:
					byte[] writeBuf = (byte[]) msg.obj;
					// construct a string from the buffer
					String writeMessage = new String(writeBuf);
					//mConversationArrayAdapter.add("Me:  " + writeMessage);
					Toast.makeText(getApplicationContext(),"Writing Message",Toast.LENGTH_SHORT).show();
					break;
				case Constants.MESSAGE_READ:
					byte[] readBuf = (byte[]) msg.obj;
					// construct a string from the valid bytes in the buffer
					String readMessage = new String(readBuf, 0, msg.arg1);
					//mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
					Toast.makeText(getApplicationContext(),"Reading Message",Toast.LENGTH_SHORT).show();
					break;
				case Constants.MESSAGE_DEVICE_NAME:
					// save the connected device's name
					/*mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
					if (null != activity) {
						Toast.makeText(getApplicationContext(), "Connected to "
								+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
					}*/
					Toast.makeText(getApplicationContext(),"Connected to device",Toast.LENGTH_SHORT).show();
					break;
				case Constants.MESSAGE_TOAST:
					if (null != getApplicationContext()) {
						Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
								Toast.LENGTH_SHORT).show();
					}
					break;
			}
		}
	};
}

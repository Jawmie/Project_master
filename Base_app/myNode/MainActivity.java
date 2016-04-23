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
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;
import com.example.myNode.AccessoryControl.OpenStatus;


public class MainActivity extends Activity implements NodeList.OnListChangeListener{

	//private LinearLayout tempLayout;
	//private TextView tempValue;
	Button b1, b2, b3;

	private ListView nodeListView;
	private TextView emptyView;
	private NodeListAdapter nodeListAdapter;
	private AccessoryControl accessoryControl;

	private static final int NODE_LIST_CHANGED = 0;

	/** TAG used for logging messages */
	private static final String TAG = "EA AOA CAN";

	// Variables for new handler
	protected boolean active = true;
	protected int splashTime = 2000;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);

		b1=(Button)findViewById(R.id.onButt);
		b2=(Button)findViewById(R.id.listButt);
		b3=(Button)findViewById(R.id.offButt);
		setTitle("Accessory Disconnected");

		// Handler for starting up bluetooth
		/*Handler myBlueHand = new Handler();
		myBlueHand.postDelayed(new Runnable() {
			public void run() {
				finish();
				Intent ii = new Intent(MainActivity.this, BluetoothActivity.class);
				startActivity(ii);
			}
		}, splashTime);*/

		/*********************************************************************/

		NodeList nodeList = NodeList.getInstance();
		nodeList.addOnListChangeListener(this);
		nodeListAdapter = new NodeListAdapter(this, nodeList);

		emptyView = (TextView)findViewById(R.id.emptyView);
		if (nodeList.size() == 0) {
			emptyView.setVisibility(View.VISIBLE);
		} else {
			emptyView.setVisibility(View.GONE);
		}
		//tempLayout = (LinearLayout)findViewById(R.id.tempLayout);
		//tempValue = (TextView)findViewById(R.id.tempValue);

		nodeListView = (ListView)findViewById(R.id.nodeListView);
		nodeListView.setAdapter(nodeListAdapter);
		//nodeListView.setBackgroundColor(Color.YELLOW);
		//nodeListView.setVisibility(View.GONE);

		nodeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position,
									long id) {
				NodeView nv = (NodeView) view;

				// when an item in the node list has been clicked start the NodeActivity
				Intent intent = new Intent(MainActivity.this, NodeActivity.class);
				intent.putExtra("nodeId", nv.getNode().getNodeId());
				startActivity(intent);
			}
		});

		accessoryControl = AccessoryControl.getInstance();
		accessoryControl.init(this);
        
        /*
         * Register a receiver for permission (granted/not granted)
         * messages and Accessory detached messages.
         */
		IntentFilter filter = new IntentFilter(AccessoryControl.ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.i(TAG, "onResume");
		
		/*
		 * The application has been resumed. Try to open
		 * and access the accessory.
		 */
		OpenStatus status = accessoryControl.open();
		if (status == OpenStatus.CONNECTED) {
			connected();
		}
		else if(status != OpenStatus.REQUESTING_PERMISSION
				&& status != OpenStatus.NO_ACCESSORY) {
			showError(status);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Log.i(TAG, "onDestroy");

		accessoryControl.appIsClosing();
		accessoryControl.close();
		
		/*
		 * The application is completely closed and removed from memory.
		 * Remove all resources which in this case means unregistering the
		 * broadcast receiver.
		 */
		unregisterReceiver(receiver);
	}

	/**
	 * The node list has changed
	 */
	public void onListChange() {
		Message m = Message.obtain(handler, NODE_LIST_CHANGED);
		handler.sendMessage(m);
	}
	/**************************************************************************
	 * Private methods  
	 *************************************************************************/
	/**
	 * Indicate in the UI that an accessory is connected.
	 */
	private void connected() {
		setTitle("Air Module Connected");
	}

	/**
	 * Indicate in the UI that an accessory isn't connected.
	 */
	private void disconnected() {
		setTitle("Air Module Disconnected");
		NodeList.getInstance().clear();
	}

	/**
	 * Show an error dialog as a response to opening an accessory.
	 */
	private void showError(OpenStatus status) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Error: " + status.toString());

		AlertDialog d = builder.create();
		d.show();
	}
	/**************************************************************************
	 * Anonymous classes 
	 *************************************************************************/

	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case NODE_LIST_CHANGED:
				/*
				 * Node list has changed make sure the adapter is notified
				 * so the list view can be updated
				 */
					if (NodeList.getInstance().size() > 0) {
						emptyView.setVisibility(View.GONE);
					}
					else {
						emptyView.setVisibility(View.VISIBLE);
					}

					nodeListAdapter.notifyDataSetChanged();
					break;
			}
		}
	};

	/*
	 * A fundamental part of Android applications is intents. Intents are 
	 * messages between components and applications. 
	 * 
	 * The Broadcast receiver handles broadcast intents and the receiver
	 * below has been register in onCreate above with a filter to receive
	 * Accessory Detached and permission actions.
	 */

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (AccessoryControl.ACTION_USB_PERMISSION.equals(action)) {
				UsbAccessory accessory = UsbManager.getAccessory(intent);

				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

					Log.i(TAG, "Permission Granted");
					OpenStatus status = accessoryControl.open(accessory);
					if (status == OpenStatus.CONNECTED) {
						connected();
					}
					else {
						showError(status);
					}
				}
				else {
					Log.i(TAG, "Permission NOT Granted");
					disconnected();
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				Log.i(TAG, "Detached");
				disconnected();
				accessoryControl.close();
			}
		}
	};
}
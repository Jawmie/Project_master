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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AccessoryControl { 

	public static final String ACTION_USB_PERMISSION = 
			"com.example.myNode.mainActivity.action.USB_PERMISSION";
	 
	/*
	 * Message indexes for messages sent from the Accessory
	 */
	public static final byte MESSAGE_IN_NODE_ADD    = 0;
	public static final byte MESSAGE_IN_NODE_REMOVE = 1;	
	public static final byte MESSAGE_IN_NODE_VALUE  = 2;
	
	/*
	 * Message indexes for messages sent to the Accessory
	 */
	public static final byte MESSAGE_OUT_SET_VALUE    = 10;
	
	/*
	 * Special messages to/from the accessory
	 */
	
	
	/** 
	 * Sent to the accessory to indicate that the application is 
	 * ready to receive data.
	 */
	private static final byte MESSAGE_CONNECT = 98;	
	/** 
	 * Sent to the accessory to indicate that the application is 
	 * closing. The accessory acks the message using the same
	 * ID/index.
	 */
	private static final byte MESSAGE_DISCONNECT = 99;
	
	/*
	 * 
	 */
	public static final int MESSAGE_RGB_VAL_RED = 0x01;
	public static final int MESSAGE_RGB_VAL_BLUE = 0x02;
	public static final int MESSAGE_RGB_VAL_GREEN = 0x04;
	
	
	/** Manufacturer string expected from the Accessory */
	private static final String ACC_MANUF = "Embedded Artists AB";
	/** Model string expected from the Accessory */
	private static final String ACC_MODEL = "AOA Board - Nodes";
	
	/** Tag used for logging */
	private static final String TAG = "EA AOA";
	
	public enum OpenStatus {
		CONNECTED, REQUESTING_PERMISSION, UNKNOWN_ACCESSORY, NO_ACCESSORY, NO_PARCEL
	}
	
	
	private boolean permissionRequested = false;	
	private boolean isOpen = false;
	private UsbManager usbManager;
	private Context context;
	
	private ParcelFileDescriptor parcelFileDescriptor; 
	private FileOutputStream accOutputStream;	
	private Receiver receiver;
	private NodeList nodeList;
	
	private static AccessoryControl instance;
	

	/**
	 * Private constructor
	 */
	private AccessoryControl() {		
		nodeList = NodeList.getInstance();
	}
	

	/**
	 * Get instance of AccecssoryControl
	 * @return instance
	 */
	public static AccessoryControl getInstance() {
		if (instance == null) {
			instance = new AccessoryControl();
		}
		
		return instance;
	}
	
	/**
	 * Initialize with context
	 * 
	 * @param context
	 */
	public void init(Context context) {
		this.context = context;
		usbManager = UsbManager.getInstance(context);
	}
	
	/**
	 * Open the accessory and establish a connection. This method will
	 * check if there is any accessory connected to the device, request
	 * permissions if necessary and then establish input and output 
	 * streams to/from the accessory.
	 * 
	 * @return status of the Open call. 
	 */
	public OpenStatus open() {
		
		if (isOpen) {
			return OpenStatus.CONNECTED;
		}
				
		UsbAccessory[] accList = usbManager.getAccessoryList();
		if (accList != null && accList.length > 0) {

			if (usbManager.hasPermission(accList[0])) {
				return open(accList[0]);
			}
			else if (!permissionRequested) {

				PendingIntent permissionIntent = PendingIntent.getBroadcast(
						context, 0,	new Intent(ACTION_USB_PERMISSION), 0);				
				
				Log.i(TAG, "Requesting USB permission");
				
				usbManager.requestPermission(accList[0], permissionIntent);
				permissionRequested = true;
				
				return OpenStatus.REQUESTING_PERMISSION;
			}
		}
				
		return OpenStatus.NO_ACCESSORY;
	}
	
	/**
	 * Open an accessory. This method should be called if an accessory
	 * object has already been obtained. The method will establish the 
	 * connection and start receiver thread.
	 * 
	 * @param accessory an instance of UsbAccessory
	 * @return status of the Open call
	 */
	public OpenStatus open(UsbAccessory accessory) {

		if (isOpen) {
			return OpenStatus.CONNECTED;
		}
			
		// check if it is a known and supported accessory 
		if (!ACC_MANUF.equals(accessory.getManufacturer()) 
				|| !ACC_MODEL.equals(accessory.getModel())) {
			
			Log.i(TAG, "Unknown accessory: " + accessory.getManufacturer() 
					+ ", " + accessory.getModel());
			
			return OpenStatus.UNKNOWN_ACCESSORY;
		}
		
		parcelFileDescriptor = usbManager.openAccessory(accessory); 
		if (parcelFileDescriptor != null) {
			byte[] data = new byte[1];
						
			accOutputStream = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());		
			receiver = new Receiver(new FileInputStream(parcelFileDescriptor.getFileDescriptor()));
			
			isOpen = true;
			new Thread(receiver).start();
			
			
			// notify the accessory that we are now ready to receive data
			data[0] = MESSAGE_CONNECT;
			writeCommand(data);
			
			return OpenStatus.CONNECTED;
		}

		Log.i(TAG, "Couldn't get any ParcelDescriptor");
		return OpenStatus.NO_PARCEL;
	}
	
	/**
	 * Close the connection with the accessory.
	 */
	public void close() {
				
		if (!isOpen) {
			return;
		}
						
		permissionRequested = false;
		isOpen = false;
		
		try {
			receiver.close();
			accOutputStream.close();
			parcelFileDescriptor.close();
		} catch (IOException ioe) {
			Log.w(TAG, "Got exception when closing", ioe);
		}
	}
	
	/**
	 * Call this method when the application is closing. This method
	 * will notify the accessory that the application is about to
	 * be closed.
	 */
	public void appIsClosing() {
		byte[] data = new byte[1];
		if (!isOpen) {
			return;
		}
		
		Log.i(TAG, "Sending Disconnect message to accessory");
		data[0] = AccessoryControl.MESSAGE_DISCONNECT;
		writeCommand(data);
		long t = System.currentTimeMillis() + 5000;
		try {
			while (!receiver.done && System.currentTimeMillis() < t) {
				Thread.sleep(200);
			}
		} catch(InterruptedException ie) {	
		}
		
	}
	
	/** 
	 * Write/send a command to the accessory. For simplicity all messages
	 * are 3 bytes long; command index and two data bytes.
	 * 
	 * @param //cmd - command index
	 * @param //hiVal - first data byte
	 * @param //loVal - second data byte
	 */
	public void writeCommand(byte[] buffer) {
		
		if (!isOpen) {
			return;
		}
				
		try {
			synchronized(accOutputStream) {
				accOutputStream.write(buffer);
			}
		} catch(IOException ioe) {		
		}

	}
	

	/*
	 * The receiver thread. This Thread is responsible for reading
	 * data from the Accessory and dispatching messages to the Handler
	 * (UI thread)
	 */
	
	private class Receiver implements Runnable  {

		private FileInputStream inputStream;
		private boolean done = false;
		
		Receiver(FileInputStream inputStream) {
			this.inputStream = inputStream;
		}
		
		public void run() {

			int msgLen = 0;
			int numRead = 0;
			int pos = 0;
			byte[] buffer = new byte[16384];
					
			Log.i(TAG, "Receiver.run");
			
			try {
				
				while(!done) {
					numRead = inputStream.read(buffer);
					pos = 0;
					
				
					while(pos < numRead) {
						int len = numRead - pos;
						
						switch(buffer[pos]) {
						
						case AccessoryControl.MESSAGE_IN_NODE_ADD:

							msgLen = 3;
							if (len >= 3) {

								
								int nodeId = (int) (buffer[pos+1] & 0xff);
								int capLen = (int) (buffer[pos+2] & 0xff);
								
								Log.i(TAG, "Recv Node Add: id="+nodeId+", capLen="+capLen);
								
								msgLen += capLen;
								int[] caps = new int[capLen];
								int maxLen = caps.length;
								if (maxLen > len-3) {
									maxLen = len-3;
								}
								for (int i = 0; i < maxLen; i++) {
									caps[i] = (int) (buffer[pos+3+i] & 0xff);
								}
																								
								Node n = new Node(nodeId, caps, AccessoryControl.this);
								nodeList.addNode(n);
							}
							pos += msgLen;
							break;
						case AccessoryControl.MESSAGE_IN_NODE_REMOVE:

							Log.i(TAG, "Recv Node Remove: id="+(buffer[pos+1] & 0xff));
							
							if (len >= 2) {
								nodeList.removeNode((int) (buffer[pos+1] & 0xff));
							}
							pos += 2;
							break;				

						case AccessoryControl.MESSAGE_IN_NODE_VALUE:

							if (len >= 5) {
								int nodeId = (int) (buffer[pos+1] & 0xff);
								int capId = (int) (buffer[pos+2] & 0xff);
															
								Node n = nodeList.getNodeWithId(nodeId);
								if (n != null) {
									n.setValue(capId, toInt(buffer[pos + 3], buffer[pos + 4]));
								}
							}
							pos += 5;
							break;				
							
						case AccessoryControl.MESSAGE_DISCONNECT:

							Log.i(TAG, "Received Disconnect (ACK) message from accessory");
							
							// We want to make sure the Receive thread ends at this point
							// and doesn't start reading data
							done = true;
							pos = numRead;
							break;				
														
						default:
							// invalid message (or out of sync)

							Log.w(TAG, "Unknown command: " + buffer[pos]);
							pos += len;
							break;
						}
						
					}
					
					
				}
				
			} catch (IOException ioe) {
			}
						
		}
		
		/**
		 * Close the receiver thread.
		 */
		public void close() {
			done = true;
			
			try {
				inputStream.close();
			} catch(IOException ioe) {				
			}
		}
		
		/** Convert two bytes to an integer */
		private int toInt(byte hi, byte lo) {
			return (( (int)(hi&0xff) << 8) | (int)(lo&0xff));
		}
				
	};
	

}

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Node {
	
	/**************************************************************************
	 * Public constants 
	 *************************************************************************/	
	
	public static final int DEV_TEMP  = (0x01 << 4);
	public static final int DEV_LIGHT = (0x02 << 4);
	public static final int DEV_BTN   = (0x03 << 4);
	public static final int DEV_RGB   = (0x04 << 4);
	public static final int DEV_LED   = (0x05 << 4);
	
	/**************************************************************************
	 * Private variables 
	 *************************************************************************/	
	
	private int nodeId; 

	private Map<Integer, Integer> values;
	private int[] capabilities;
	private AccessoryControl accessoryControl;

	private ArrayList<OnValueChangeListener> listeners;
	
	/**************************************************************************
	 * Public interface 
	 *************************************************************************/	
	
	public interface OnValueChangeListener {
		public void onValueChange(int capId);
	}
	
	/**************************************************************************
	 * Constructor 
	 *************************************************************************/	
	
	/**
	 * Create a new Node
	 * 
	 * @param nodeId the unique ID of the Node
	 * @param capabilites node capabilities is an array with capability IDs
	 *                    DEV_TEMP, DEV_LIGHT, ...
	 */
	public Node (int nodeId, int[] capabilites, AccessoryControl accessoryControl) {
		this.nodeId = nodeId;
		this.capabilities = capabilites;
		this.accessoryControl = accessoryControl;
		
		listeners = new ArrayList<OnValueChangeListener>();
		
		values = new HashMap<Integer, Integer>();
		for (int i = 0; i < capabilities.length; i++) {
			values.put(Integer.valueOf(capabilites[i]), 0);			
		}
	}
	
	/**************************************************************************
	 * Public methods 
	 *************************************************************************/	
	
	/**
	 * Get Node ID
	 * @return the node ID
	 */
	public int getNodeId() {
		return nodeId;
	}
	
	/**
	 * Get Node capabilities
	 * @return node capabilities
	 */
	public int[] getCapabilities() {
		return capabilities;
	}

	/**
	 * Add value change listener  
	 * @param l listener to add
	 */
	public void addOnValueChangeListener(OnValueChangeListener l) {
		listeners.add(l);
	}
	
	/**
	 * Remove value change listener
	 * @param l listener to remove
	 */
	public void removeOnValueChangeListener(OnValueChangeListener l) {
		listeners.remove(l);
	}	
	
	/**
	 * Update the value of a capability in this node object
	 *  
	 * @param capId capability ID
	 * @param value value to set
	 */
	public void setValue(int capId, int value) {
	
		values.put(Integer.valueOf(capId), Integer.valueOf(value));
		
		for (OnValueChangeListener l : listeners) {
			l.onValueChange(capId);
		}
	
	}
	
	/**
	 * Get the value of a specific capability
	 * @param capId capability ID
	 * @return the value of the capability
	 */
	public String getValue(int capId) {
		String val = "";
		int v = values.get(Integer.valueOf(capId));
		
		switch (capId) {
		case Node.DEV_TEMP:
			val = Double.toString( ((double)v)/100 );
			break;
		case Node.DEV_LIGHT:
			val = Integer.toString(v);
			break;
		case Node.DEV_BTN:
			val = Integer.toString(v);
			break;						
		
		}
		
		return val;
	}
	
	/**
	 * Set the RGB LED on the node. A message will be sent
	 * to the attached accessory
	 */
	private void setRgb(int led, boolean on) {
		byte[] data = new byte[5];
		data[0] = AccessoryControl.MESSAGE_OUT_SET_VALUE;
		data[1] = (byte)this.nodeId;
		data[2] = DEV_RGB;
		data[3] = (byte)led;
		data[4] = (byte)(on ? 1 : 0);
		accessoryControl.writeCommand(data);		
	}
	
	/**
	 * Set Red LED
	 * @param on true if LED should be turned on
	 */
	public void setRedLed(boolean on) {
		setRgb(AccessoryControl.MESSAGE_RGB_VAL_RED, on);
	}
	
	/**
	 * Set Blue LED
	 * @param on true if LED should be turned on
	 */
	public void setBlueLed(boolean on) {
		setRgb(AccessoryControl.MESSAGE_RGB_VAL_BLUE, on);
	}
	
	/**
	 * Set Green LED
	 * @param on true if LED should be turned on
	 */
	public void setGreenLed(boolean on) {
		setRgb(AccessoryControl.MESSAGE_RGB_VAL_GREEN, on);
	}	
	
	/**
	 * Set LED state. A message will be sent to the Accessory
	 * 
	 * @param on true if the LED should be turned on
	 */
	public void setLed(boolean on) {
		byte[] data = new byte[5];
		data[0] = AccessoryControl.MESSAGE_OUT_SET_VALUE;
		data[1] = (byte)this.nodeId;
		data[2] = DEV_LED;
		data[3] = 0;
		data[4] = (byte)(on ? 1 : 0);
		accessoryControl.writeCommand(data);
	}
}

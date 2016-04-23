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

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class NodeView extends LinearLayout implements Node.OnValueChangeListener {

	/**************************************************************************
	 * Private variables 
	 *************************************************************************/	
	
	private TextView contView;
	private Node node;
	private Map<Integer, TextView> values;
	
	private static final int VALUE_UPDATED = 0;

	/**************************************************************************
	 * Constructor 
	 *************************************************************************/	
	
	/**
	 * Create a NodeView
	 * @param context context
	 * @param node node associated with this view
	 */
	public NodeView(Context context, Node node) {
		super(context);
		this.node = node;

		this.setOrientation(VERTICAL);		
		
		values = new HashMap<Integer, TextView>();
		node.addOnValueChangeListener(this);

		RelativeLayout mine = new RelativeLayout(context);
		RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		contView = new TextView(context);
		contView.setTextSize(24);
		contView.setGravity(Gravity.CENTER);
		//contView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		contView.setText("Touch to Continue");

		params1.addRule(RelativeLayout.ALIGN_BOTTOM, contView.getId());
		mine.addView(contView, params1);

		addView(mine);

		//addView(contView, new RelativeLayout.LayoutParams(			);
		
		/*LinearLayout capLayout = new LinearLayout(context);
		capLayout.setOrientation(HORIZONTAL);
		addView(capLayout, new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));*/
		
		int[] caps = node.getCapabilities();
		
		// Sets up Starting layout, should try to remove and add button or remove altogether
		for (int i = 0; i < caps.length; i++) {
			LinearLayout vl = new LinearLayout(context); // Main LinearLayout
			vl.setOrientation(HORIZONTAL);			
			vl.setMinimumWidth(110);	// Expandable Width


			//ImageView iv = getImageView(context, caps[i]); // Gets images
			//if (iv == null) continue;
			//iv.setPadding(0, 0, 30, 5);
			//vl.addView(iv, new LinearLayout.LayoutParams(					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			
			TextView tv = new TextView(context);
			tv.setText("" + node.getValue(caps[i]));

		//	vl.addView(tv, new LinearLayout.LayoutParams(					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			//vl.setBackgroundColor(Color.YELLOW);
						
			
			// Mapping capability with the TextView that holds its value.
			// A limitation with using the capability type as key is
			// that a node can only have one capability of each type
			values.put(Integer.valueOf(caps[i]), tv);
			//capLayout.addView(vl, new LinearLayout.LayoutParams(				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ));
		}
	}
	
	/**************************************************************************
	 * Public methods 
	 *************************************************************************/	
	
	/**
	 * Get Node associated with this NodeView
	 * @return
	 */
	public Node getNode() {
		return this.node;
	}
	
	/**
	 * OnValueChangeListener
	 */
	public void onValueChange(int capId) {
						
		Message m = Message.obtain(handler, VALUE_UPDATED);
		m.arg1 = capId;
		handler.sendMessage(m);		
		
	}	
	
	/**************************************************************************
	 * Private methods 
	 *************************************************************************/	
	
	/**
	 * Get ImageView given a capability
	 * @param context
	 * @param capability capability ID
	 * @return ImageView
	 */
	private ImageView getImageView(Context context, int capability) {
		ImageView iv = null;
		switch (capability) {
		case Node.DEV_TEMP:
			iv = new ImageView(context);
			iv.setImageResource(R.drawable.temperature);
			break;
		case Node.DEV_LIGHT:
			iv = new ImageView(context);
			iv.setImageResource(R.drawable.lightbulb);			
			break;
		case Node.DEV_BTN:
			iv = new ImageView(context);
			iv.setImageResource(R.drawable.button);			
			break;
		}
		return iv;
	}
	
	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			
			switch(msg.what) {
			case VALUE_UPDATED: 
											
				TextView tv = values.get(msg.arg1);
				tv.setText(node.getValue(msg.arg1));				
				
				break;										
			}
		}
	};
}

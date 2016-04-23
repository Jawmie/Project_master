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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class NodeListAdapter extends BaseAdapter {

	/**************************************************************************
	 * Private variables 
	 *************************************************************************/	
	
	private Context context;
	private NodeList nodeList;

	/**************************************************************************
	 * Constructor 
	 *************************************************************************/	
	
	/**
	 * Create a NodeListAdapter
	 * @param context context
	 * @param nodeList list with nodes
	 */
	public NodeListAdapter(Context context, NodeList nodeList) {
		this.context = context;
		this.nodeList = nodeList;
	}
	
	/**************************************************************************
	 * Public methods 
	 *************************************************************************/	
	
	/**
	 * Number of items
	 */
	public int getCount() {
		return nodeList.size();
	}

	/**
	 * Get item at specific position
	 */
	public Object getItem(int position) {		
		return nodeList.getNodeAtPosition(position);
	}

	/**
	 * Get item ID
	 */
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Get view associated with an item at specific position
	 */
	public View getView(int position, View view, ViewGroup parent) {
			
		Node n = nodeList.getNodeAtPosition(position);		
		view = new NodeView(context, n);	
		
		return view;
	}
	


}

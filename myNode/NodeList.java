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

public class NodeList {

	/**************************************************************************
	 * Public interface 
	 *************************************************************************/	
	
	public interface OnListChangeListener {
		public void onListChange();
	}
	
	
	/**************************************************************************
	 * Private variables 
	 *************************************************************************/	
	
	private static NodeList instance = null;
	private ArrayList<Node> list; 
	private ArrayList<OnListChangeListener> listeners;
	
	
	/**************************************************************************
	 * Constructor 
	 *************************************************************************/	
	
	/**
	 * Private constructor. Use getInstace() to get an instance of this class.
	 */
	private NodeList() {
		list = new ArrayList<Node>();
		listeners = new ArrayList<OnListChangeListener>();
	}
	
	/**************************************************************************
	 * Public methods 
	 *************************************************************************/	
	
	/**
	 * Get instance of NodeList
	 * @return instance of NodeList
	 */
	public static NodeList getInstance() {
		if (instance == null) {
			instance = new NodeList();
		}
		
		return instance;
	}
	
	/**
	 * Get size of node list
	 * @return size of node list
	 */
	public int size() {
		return list.size();
	}
	
	/** 
	 * Get a node given the Node ID
	 * @param nodeId unique node ID
	 * @return node with given node ID
	 */
	public Node getNodeWithId(int nodeId) {
		Node result = null;
		for (Node n : list) {
			if (n.getNodeId() == nodeId) {
				result = n;
				break;
			}
		}
		
		return result;
	}

	/**
	 * Get node at specific position in list
	 * @param position position of node
	 * @return node at specific position
	 */
	public Node getNodeAtPosition(int position) {
		return list.get(position);
	}	
	
	/**
	 * Add node to the list
	 * @param n node to add
	 */
	public void addNode(Node n) {
		Node exists = getNodeWithId(n.getNodeId());
		if (exists != null) {
			return;
		}
		
		list.add(n);
		
		for (OnListChangeListener l : listeners) {
			l.onListChange();
		}				
	}
	
	/**
	 * Remove node from the list
	 * @param n node to add
	 */
	public void removeNode(int nodeId) {
		Node n = getNodeWithId(nodeId);
		list.remove(n);
		
		for (OnListChangeListener l : listeners) {
			l.onListChange();
		}						
	}

	/**
	 * Clear the list
	 */	
	public void clear() {
		list.clear();
		
		for (OnListChangeListener l : listeners) {
			l.onListChange();
		}							
	}
	
	/**
	 * Add node change listener  
	 * @param l listener to add
	 */
	public void addOnListChangeListener(OnListChangeListener l) {
		listeners.add(l);	
	}
	
	/**
	 * Remove node change listener
	 * @param l listener to remove
	 */
	public void removeOnListChangeListener(OnListChangeListener l) {
		listeners.remove(l);
	}	
	
}

/**
* Copyright (c) 2014 Digi International Inc.,
* All rights not expressly granted are reserved.
*
* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this file,
* You can obtain one at http://mozilla.org/MPL/2.0/.
*
* Digi International Inc. 11001 Bren Road East, Minnetonka, MN 55343
* =======================================================================
*/
package com.digi.xbee.api.listeners;

import com.digi.xbee.api.packet.XBeePacket;

/**
 * This interface defines the required methods that should be implemented to 
 * behave as a packet listener and be notified when new packets are received 
 * from an XBee device of the network.
 */
public interface IPacketReceiveListener {

	/**
	 * Called when a packet received through the connection interface.
	 * 
	 * @param receivedPacket The received packet.
	 * 
	 * @see XBeePacket
	 */
	public void packetReceived(XBeePacket receivedPacket);
}
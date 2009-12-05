/*
 * BigBlueButton - http://www.bigbluebutton.org
 * 
 * Copyright (c) 2008-2009 by respective authors (see below). All rights reserved.
 * 
 * BigBlueButton is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 3 of the License, or (at your option) any later 
 * version. 
 * 
 * BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along 
 * with BigBlueButton; if not, If not, see <http://www.gnu.org/licenses/>.
 *
 * Author: Richard Alam <ritzalam@gmail.com>
 * 
 * $Id: $
 */
package org.bigbluebutton.webconference.red5.voice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.so.ISharedObject;
import org.slf4j.Logger;

public class ClientManager implements ClientNotifier {
	private static Logger log = Red5LoggerFactory.getLogger(ClientManager.class, "bigbluebutton");

	private final ConcurrentMap<String, RoomInfo> voiceRooms;
	private final ConcurrentMap<String, RoomInfo> webRooms;
	
	public ClientManager() {
		voiceRooms = new ConcurrentHashMap<String, RoomInfo>();
		webRooms = new ConcurrentHashMap<String, RoomInfo>();
	}
	
	public void addSharedObject(String webRoom, String voiceRoom, ISharedObject so) {
		log.debug("Adding SO for [" + webRoom + "," + voiceRoom + "]");
		RoomInfo soi = new RoomInfo(webRoom, voiceRoom, so);
		voiceRooms.putIfAbsent(voiceRoom, soi);
		webRooms.putIfAbsent(webRoom, soi);
	}
	
	public void removeSharedObject(String webRoom) {
		RoomInfo soi = webRooms.remove(webRoom);
		if (soi != null) voiceRooms.remove(soi.getVoiceRoom());
	}
		
	public void joined(String room, Integer participant, String name, Boolean muted, Boolean talking){
		log.debug("Participant " + name + "joining room " + room);
		RoomInfo soi = voiceRooms.get(room);
		if (soi != null) {
			List<Object> list = new ArrayList<Object>();
			list.add(participant);
			list.add(name);
			list.add(name);
			list.add(muted);
			list.add(talking);
			log.debug("Sending join to client " + name);
			soi.getSharedObject().sendMessage("userJoin", list);
		}				
	}
	
	public void left(String room, Integer participant){
		log.debug("Participant [" + participant + "," + room + "] leaving");
		RoomInfo soi = voiceRooms.get(room);
		if (soi != null) {
			List<Object> list = new ArrayList<Object>();
			list.add(participant);
			soi.getSharedObject().sendMessage("userLeft", list);
		}
	}
	
	public void muted(String room, Integer participant, Boolean muted){
		log.debug("Participant " + participant + " " + muted);
		RoomInfo soi = voiceRooms.get(room);
		if (soi != null) {
			List<Object> list = new ArrayList<Object>();
			list.add(participant);
			list.add(muted);
			soi.getSharedObject().sendMessage("userMute", list);
		}		
	}
	
	public void talking(String room, Integer participant, Boolean talking){
		log.debug("Participant " + participant + " " + talking);
		RoomInfo soi = voiceRooms.get(room);
		if (soi != null) {
			List<Object> list = new ArrayList<Object>();
			list.add(participant);
			list.add(talking);
			soi.getSharedObject().sendMessage("userTalk", list);
		}
	}	
}

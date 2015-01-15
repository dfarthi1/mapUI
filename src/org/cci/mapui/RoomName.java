/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cci.mapui;

/**
 *
 * @author itsadmin
 */
public class RoomName {

    String roomName,roomNumber;
	
    /**
     *
     * @param name
     * @param number
     */
    public RoomName(String name, String number)
	{
		roomName = name;
		roomNumber = number;
	}
	
	public String getRoomName()
	{
		return roomName;
	}
	
	public String getRoom()
	{
		return roomNumber;
	}    
}

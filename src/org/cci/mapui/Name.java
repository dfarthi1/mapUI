package org.cci.mapui;

public class Name {
	
	String firstName,lastName,roomNumber;
	
	public Name(String f, String l, String n)
	{
		firstName = f;
		lastName = l;
		roomNumber = n;
	}
	
	public String getName()
	{
		return firstName + " " + lastName;
	}
	
	public String getRoom()
	{
		return roomNumber;
	}
}

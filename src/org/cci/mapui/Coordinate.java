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
public class Coordinate {
    
    private final int x;  //x value of the coordinate
    private final int y;  //y value of the coordinate
    
    public Coordinate(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    
    public int getX()
    {
        return x;
    }
    
    public int getY()
    {
        return y;
    }
    @Override
    public String toString()
    {
        return x + "," + y;
    }
}

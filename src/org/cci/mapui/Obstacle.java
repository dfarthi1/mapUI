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
public class Obstacle {
    
    private final String name;
    private final Coordinate upperLeft;
    private final Coordinate upperRight;
    private final Coordinate lowerLeft;
    private final Coordinate lowerRight;
    
    public Obstacle(String name, Coordinate upperLeft, Coordinate lowerRight)
    {
        this.name = name;
        this.upperLeft = upperLeft;
        this.lowerRight = lowerRight;
        upperRight = new Coordinate(lowerRight.getX(),upperLeft.getY());
        lowerLeft = new Coordinate(upperLeft.getX(),lowerRight.getY());
    }
    
    public Coordinate getUpperLeft()
    {
        return upperLeft;
    } 
    
    public Coordinate getLowerRight()
    {
        return lowerRight;
    }
    
    public Coordinate getUpperRight()
    {
        return upperRight;
    } 
    
    public Coordinate getLowerLeft()
    {
        return lowerLeft;
    }
    
    public String getName()
    {
        return name;
    }
    
    @Override
    public String toString()
    {
        return getName() + " " + getUpperLeft().getX() + ","  + getUpperLeft().getY() + "/" + getLowerRight().getX() + "," + getLowerRight().getY();
    }
    
}

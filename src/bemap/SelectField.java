/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bemap;

import java.awt.Color;
import java.awt.Point;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapRectangleImpl;

/**
 *
 * @author Micha
 */



public class SelectField {
    
private Coordinate upperLeft;
private Coordinate lowerRight;
private boolean active = false;
private MapRectangleImpl rectangle;

    public SelectField(){
        //constructor
    }
    
    boolean isActive(){return active;}
    Coordinate upperLeft(){return upperLeft;}
    Coordinate lowerRight(){return lowerRight;}
    
    void setFirstCorner(Coordinate corner){
        upperLeft = corner;
    }
    
    void setSecondCorner(Coordinate corner){
        lowerRight = corner;
        enable();
    }
    
    void enable(){
        BeMapEditor.mainWindow.getMap().removeMapRectangle(rectangle); //remove any existing
        rectangle = new MapRectangleImpl(upperLeft,lowerRight);
        rectangle.setBackColor(Color.blue);
        BeMapEditor.mainWindow.getMap().addMapRectangle(rectangle);
        rectangle.setVisible(true);
        active = true; 
    }
    
    void disable(){
        BeMapEditor.mainWindow.getMap().removeMapRectangle(rectangle);
        active = false;
    }
    
}

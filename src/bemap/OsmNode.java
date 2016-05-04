/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bemap;

import org.openstreetmap.gui.jmapviewer.Coordinate;

/**
 *
 * @author Micha
 */
public class OsmNode {
    private long id;
    private double lat;
    private double lon;
    private boolean init = false;
    
    
    private static final double PRECISION = 0.00000001; //precision for double comparison
    
    public OsmNode(){
        init = false;
    }
    
    public OsmNode(long id, double lat, double lon){
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        init = true;
    }
    
    public boolean isInit(){return init;}
    
    public double getLon(){return lon;}
    public double getLat(){return lat;}
    public Coordinate getCoord(){
        Coordinate c = new Coordinate(lat,lon);
        return c;
    }
    
    /**
     * Intern function to compare two double values with a certain precision
     * stored as a local variable
     * @param a double 1 to compare
     * @param b double 2 to compare
     * @return true when equal
     */
    private static boolean equals(double a, double b){
    return a == b ? true : Math.abs(a - b) < PRECISION;
    }
    
    public static boolean equals(OsmNode n1, OsmNode n2){
        if(equals(n1.getLat(),n2.getLat())&&equals(n1.getLon(),n2.getLon()))
            return true;
        else return false;
    }
            
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcan2;

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
            
    
    
}

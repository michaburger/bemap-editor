/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//overpass API

package bemap;

import java.awt.BasicStroke;
import java.awt.Color;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.List;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;

/**
 *
 * @author Micha
 */
public class RoadSegment {

    private double neLat;
    private double neLon;
    private double swLat;
    private double swLon;
    private double roadVector[] = new double[2];
    private double perpVector[] = new double[2]; //perpendicular normed vector [lat, lon]
    private List<Coordinate> corners = new ArrayList<>(); //the 4 corner points

    private static final int LAT = 0; //N
    private static final int LON = 1; //E

    public RoadSegment(double neLat, double neLon, double swLat, double swLon, double size) {
        this.neLat = neLat;
        this.neLon = neLon;
        this.swLat = swLat;
        this.swLon = swLon;

        roadVector[LAT] = neLat - swLat;
        roadVector[LON] = neLon - swLon;
        perpVector = createPerpNormed(roadVector);
             
        //add the 4 corners to the list
        corners.add(new Coordinate(neLat+size*perpVector[LAT],neLon+size*perpVector[LON]));
        corners.add(new Coordinate(swLat+size*perpVector[LAT],swLon+size*perpVector[LON]));
        corners.add(new Coordinate(swLat-size*perpVector[LAT],swLon-size*perpVector[LON]));
        corners.add(new Coordinate(neLat-size*perpVector[LAT],neLon-size*perpVector[LON]));
      
    }

    public void draw() {
        JMapViewer map = BeMapEditor.mainWindow.getMap();
        MapPolygon poly = new MapPolygonImpl(corners);
        MapMarkerDot m1 = new MapMarkerDot(neLat,neLon);
        MapMarkerDot m2 = new MapMarkerDot(swLat,swLon);
        map.addMapMarker(m1);
        map.addMapMarker(m2);
        map.setMapMarkerVisible(true);
        map.addMapPolygon(poly);
        
    }
    
    private double[] createPerpNormed(double [] vector){
        double [] perp = new double[2];
        double [] perpNormed = new double[2];
        //swap lat and lon --> perpendicular vector
        perp[LAT] = -1*vector[LON];
        perp[LON] = vector[LAT];
        
        perpNormed[LAT] = perp[LAT] / length(perp);
        perpNormed[LON] = perp[LON] / length(perp);
        
        return perpNormed;
    }
    
    private double length(double [] vector){
        return sqrt(vector[LAT]*vector[LAT] + vector[LON]*vector[LON]);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcan2;

import java.awt.BasicStroke;
import java.awt.Color;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.List;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.Style;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;

/**
 *
 * @author Micha
 */
public class Segment {
    private OsmNode node1;
    private OsmNode node2;
    private ArrayList<DataPoint> dpList = new ArrayList<>();
    
    private double roadVector[] = new double[2];
    private double perpVector[] = new double[2]; //perpendicular normed vector [lat, lon]
    private List<Coordinate> corners = new ArrayList<>(); //the 4 corner points
    private final static double size = 0.0001;

    private static final int LAT = 0; //N
    private static final int LON = 1; //E
    
    private final int OPACITY = 150;
    
    private boolean isComplete = false;
    
    public Segment(){
        //no initialisations: federal palace
    }
    
    public boolean isComplete(){return isComplete;}
    
    
    
    public Segment(OsmNode node1, DataPoint dp){
        this.node1 = node1;
        dpList.add(dp);
        
        
        //draw points for debug
        /*
        MapMarkerDot m1 = new MapMarkerDot(node1.getCoord());
        MapMarkerDot m2 = new MapMarkerDot(node2.getCoord());
        BeMapEditor.mainWindow.getMap().addMapMarker(m1);
        BeMapEditor.mainWindow.getMap().addMapMarker(m2);
        m1.setVisible(true);
        m2.setVisible(true);
        */
    }
    
    public void segmentEnd(OsmNode node2, DataPoint dp){
        this.node2 = node2;
        dpList.add(dp);
                
        roadVector[LAT] = node1.getLat() - node2.getLat();
        roadVector[LON] = node1.getLon() - node2.getLon();
        
        perpVector = createPerpNormed(roadVector);
        
        //add the 4 corners to the list
        corners.add(new Coordinate(node1.getLat()+size*perpVector[LAT],node1.getLon()+size*perpVector[LON]));
        corners.add(new Coordinate(node2.getLat()+size*perpVector[LAT],node2.getLon()+size*perpVector[LON]));
        corners.add(new Coordinate(node2.getLat()-size*perpVector[LAT],node2.getLon()-size*perpVector[LON]));
        corners.add(new Coordinate(node1.getLat()-size*perpVector[LAT],node1.getLon()-size*perpVector[LON]));
        
        isComplete = true;
        
        draw();
    }
    public void draw() {
        JMapViewer map = BeMapEditor.mainWindow.getMap();
        
        Color col = getColor();
        
        Layer global = new Layer("Global");
        Style style = new Style();
        style.setBackColor(col);
        style.setColor(col);
        style.setStroke(new BasicStroke(0));
        MapPolygon poly = new MapPolygonImpl(global,"",corners,style);
        map.addMapPolygon(poly);
        
    }
    
    private Color getColor(){
        int rSum=0,gSum=0,bSum=0,aSum=0;
        for(DataPoint dp : dpList){
            Color c = DataPoint.chooseColor(dp.getSensor(BeMapEditor.mainWindow.getSensorNumber()), BeMapEditor.mainWindow.getSensorNumber(), OPACITY);
            rSum += c.getRed();
            gSum += c.getGreen();
            bSum += c.getBlue();
            aSum += c.getAlpha();
        }
        int d=dpList.size();
        return new Color(rSum/d,gSum/d,bSum/d,aSum/d);
    }
    
    public void addDataPoint(DataPoint dp){
        dpList.add(dp);
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
    
    /**
     * Returns true if the point is already stored in this Data object
     * @param lat latitude
     * @param lon longitude
     * @return true when point exists, false when point doesn't exist
     */
    private boolean exists(OsmNode n1, OsmNode n2){
        boolean exists = false;
        
        //if n1 == node1 AND n2 == node 2
        //or if n1 == node2 AND n2 == node 1
        if((OsmNode.equals(n1,node1) && OsmNode.equals(n2,node2))||(OsmNode.equals(n1,node2)&&OsmNode.equals(n2,node1))){
            exists = true;
        }
        return exists;
    }
    
    
}

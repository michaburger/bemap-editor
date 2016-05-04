/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bemap;

import java.util.ArrayList;
import org.openstreetmap.gui.jmapviewer.Coordinate;

/**
 * Class responsible for the pollution modelling.
 * @author Micha
 */
public class Modeling {
    
    private boolean rendered = false;
    private OsmAPI api = new OsmAPI();
    private ArrayList<Segment> segments = new ArrayList<>();
    private static final boolean MODEL_DEBUG = true;
    private static final int MIN_POINTS_SEGMENT = 10;
    
    public Modeling(){
        
    }
    
    public boolean isRendered(){return rendered;}
    
    public void render(ArrayList<DataPoint> pointList) throws Exception{
        if(!rendered){
            //render
            
            //save bar values
            int barMax = BeMapEditor.mainWindow.getBarMax();
            int barVal = BeMapEditor.mainWindow.getBarVal();
            //status
            BeMapEditor.mainWindow.setBarMax(pointList.size());
            
            Segment lastSegment = new Segment();
            OsmNode lastNode = new OsmNode();
            
            boolean firstSegmentCreated = false;
            
            int position = 0;
            for (DataPoint dp : pointList) {
                
                ArrayList<OsmNode> nodes = api.getOSMSegments(dp.lat(),dp.lon());
                
                if(nodes.size()>=2){
                //find two nearest points, create segment if it doesn't exist yet
                //todo: check if segment exists
                OsmNode nearest = findNearest(new Coordinate(dp.lat(),dp.lon()),nodes);
                if(nearest.isInit()){
                    if(!firstSegmentCreated){
                        //create first segment
                        lastNode = nearest;
                        lastSegment = new Segment(nearest,dp);
                        firstSegmentCreated = true;
                        if(MODEL_DEBUG) BeMapEditor.mainWindow.append("\nFirst Segment created");
                    }
                    else{
                    //check if next point is different
                        if(OsmNode.equals(nearest,lastNode) || lastSegment.getPointNumber() < MIN_POINTS_SEGMENT){
                             //no --> add to existing segment
                            lastSegment.addDataPoint(dp);
                            if(MODEL_DEBUG) BeMapEditor.mainWindow.append("\nPoint added to existing Segment");
                        }
                        else{
                            //yes --> end segment, start new one
                            lastSegment.segmentEnd(nearest, dp);
                            segments.add(lastSegment);
                            lastSegment = new Segment(nearest,dp);
                            lastNode = nearest;
                            if(MODEL_DEBUG) BeMapEditor.mainWindow.append("\nNew Segment created");
                            
                        }
                    
                   
                    }
                }
                else {
                    if(MODEL_DEBUG) BeMapEditor.mainWindow.append("\nSegment ignored");
                }
                
                }
                else{
                   //else ignore points or put them in front of Federal Palace
                   lastSegment.addDataPoint(dp);
                   if(MODEL_DEBUG) BeMapEditor.mainWindow.append("\nPoint added to existing Segment");
                }
                //adapt status bar
                Thread.sleep(1);
                BeMapEditor.mainWindow.setBar(position);
                position++;
            }
            
            //close last segment
            if(lastSegment.getPointNumber()>MIN_POINTS_SEGMENT){
               lastSegment.segmentEnd(lastNode,pointList.get(pointList.size()-1));
               segments.add(lastSegment);
            }
            else lastSegment = null; //will be deleted by garbage collector
            
            BeMapEditor.mainWindow.getMap().removeAllMapMarkers();
            
            //restitue memory bar
            BeMapEditor.mainWindow.setBar(barVal);
            BeMapEditor.mainWindow.setBarMax(barMax);
            
        }
        
        
    }
    
    /**
     * Returns only the nearest two nodes, where the point with lat and lon lies inbetween.
     * @param lat
     * @param lon
     * @param allNodes
     * @return 
     */
    private OsmNode findNearest(Coordinate c, ArrayList<OsmNode> allNodes){
        OsmNode near = new OsmNode();
        
        for (OsmNode n : allNodes) {
            if(calculateDistance(n.getCoord(),c) < calculateDistance(near.getCoord(),c)){
                //replace the nearest node
                near=n;
            }
        }
        ArrayList<OsmNode> finalList = new ArrayList<>();
        
        return near;
        
    }
    
    /**
     * Calculates the distance between 2 coordinates in decimal degrees.
     * @param c1 Coordinate 1
     * @param c2 Coordinate 2
     * @return 
     */
    private double calculateDistance(Coordinate c1, Coordinate c2){
        double dLon = c1.getLat()-c2.getLat();
        double dLat = c1.getLon()-c2.getLon();
        return Math.sqrt(dLon*dLon+dLat*dLat);
    }
    
}

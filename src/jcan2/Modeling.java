/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcan2;

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
            
            int position = 0;
            for (DataPoint dp : pointList) {
                
                Segment lastSegment = new Segment();
                
                ArrayList<OsmNode> nodes = api.getOSMSegments(dp.lat(),dp.lon());
                
                if(nodes.size()>=2){
                //find two nearest points, create segment if it doesn't exist yet
                //todo: check if segment exists
                ArrayList<OsmNode> segmentNodes = getStartEndNodes(new Coordinate(dp.lat(),dp.lon()),nodes);
                if(segmentNodes.get(0).isInit() && segmentNodes.get(1).isInit()){
                    Segment seg = new Segment(segmentNodes.get(0),segmentNodes.get(1),dp);
                    if(MODEL_DEBUG) BeMapEditor.mainWindow.append("\nNew Segment created");
                    segments.add(seg);
                    lastSegment = seg;
                }
                else if(MODEL_DEBUG) BeMapEditor.mainWindow.append("\nSegment ignored");
                }
                else{
                   //else ignore points or put them in front of Federal Palace
                   lastSegment.addDataPoint(dp);
                   if(MODEL_DEBUG) BeMapEditor.mainWindow.append("\nPoint added to existing Segment");
                }
                //adapt status bar
                BeMapEditor.mainWindow.setBar(position);
                position++;
            }
            
            
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
    private ArrayList<OsmNode> getStartEndNodes(Coordinate c, ArrayList<OsmNode> allNodes){
        OsmNode near1 = new OsmNode();
        OsmNode near2 = new OsmNode();
        
        for (OsmNode n : allNodes) {
            if(calculateDistance(n.getCoord(),c) < calculateDistance(near1.getCoord(),c)){
                //replace the nearest nodes
                near2=near1;
                near1=n;
            }
        }
        ArrayList<OsmNode> finalList = new ArrayList<>();
        finalList.add(near1);
        finalList.add(near2);
        
        return finalList;
        
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

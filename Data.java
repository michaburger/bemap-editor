

package jcan2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.data.xy.XYSeries;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.MapMarkerCircle;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.Style;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;

/**
 *The class Data generates a new "data layer" for the map that contains
 * points, polynoms, and that can be deleted independantly from the other layers.
 * @author Micha
*/

public class Data {
    
    //stores all the data points
    private final ArrayList<DataPoint> pointList = new ArrayList<>();
    private final boolean DATA_DEBUG = false;
    private final byte ERROR_TYPE_TRANSMISSION = 1;
    private final String ERROR_MSG_TRANSMISSION = "Transmission error. Please try again.\n";
    private final byte ERROR_TYPE_NO_VALUES = 2;
    private final byte NO_ERROR = 0;
    private final double MAX_DEGRE = 0.005; //max distance for showing points
    private static final double PRECISION = 0.000000001; //precision for double comparison
    private final String ERROR_MSG_NO_VALUES = "No values stored in the device. Could not be imported\n";
    private int trackID;
    private String layerName;
    private static final int GLOBAL = 0;
    private Modeling model = new Modeling();
    
    private static final int NB_POINTS = 18; //for polygon, 360/n must be an int
    private static final double GLOBAL_R = 0.0003;
          
    
    //String "buffer" for data input:
    private String outputDataString = "";
    
    /**
     * Constructor of a data layer
     * @param id the identifier of the track to create
     * @param layerName the desired name of the track
     */
    public Data(int id, String layerName){
        trackID = id;
        this.layerName = layerName;
    }
    
    public void setID(int id){
        trackID = id;
    }
    
    public String getBuffer() {return outputDataString;}

    /**
     * Change the stored name of the layer
     * @param layerName new name
     */
    public void setLayerName(String layerName){
        this.layerName = layerName;
    }
    /**
     * Retreive the stored name for this layer
     * @return current layer name
     */
    public String getLayerName() {return layerName;}
    
    /**
     * Getter for the track identifier (equals to the position in the Data 
     * array list of the class MainWindow.
     * @return track ID
     * 
     */
    public int getID(){return trackID;}

    /**
     * Stores a point in the DataPoint array list of the Data object.
     * @param lat latitude of the point
     * @param lon longitude of the point
     * @param date date in the format ddMMyy
     * @param time time in the format hhmmsscc
     * @param trackID identifiant du track
     * @param s1 values of sensor 1 (1-255 for gaz value, 0 for error)
     * @param s2 values of sensor 2 (1-255 for gaz value, 0 for error)
     * @param s3 values of sensor 3 (0-100 for humidity, else error = grey)
     * @param s4 values of sensor 4 (
     * @param s5
     * @param sent
     */
    public void storePoint(int trackID, double lat, double lon, long date, long time, int s1, int s2, int s3, int s4, int s5, boolean sent){
           DataPoint point = new DataPoint();
           point.setDataPoint(trackID, lat, lon, pointList.size(),date,time,s1,s2,s3,s4,s5,sent);
           pointList.add(point);
       }
    
    //only for manually creating test data
    /**
     * This method can be used for manually storing a point in order to create 
     * test data; all the sensor values will have the same value stored.
     * @param lat latitude
     * @param lon longitude
     * @param poll pollution placeholder between 0 and 100, determinates the color of the point
     */
    public void storePoint(double lat, double lon, int poll){
        DataPoint point = new DataPoint();
        DateFormat date = new SimpleDateFormat("ddMMyy");
        DateFormat time = new SimpleDateFormat("HHmmss");
        Calendar cal = Calendar.getInstance();
        
        long d = Long.parseLong(date.format(cal.getTime()));
        long t = Long.parseLong(time.format(cal.getTime())) * 100; //add hundreths
        point.setDataPoint(-1,lat,lon,pointList.size(),d,t,poll,poll,poll,poll,-1,true); //true: never send manual points to server
        pointList.add(point);
    }
    
    public void removeSendToServerFlag(){
        if(pointList.size()>0){
          //iterate through point list
          for (DataPoint dp : pointList) {
              //for every point dp, do
              dp.removeServerFlag();
          }
        }
    }
    
   
    /**
     * Cleans the point list.
     */
    public void deleteAllPoints(){
        pointList.removeAll(pointList);
    }
    
    public int deleteSelected(Coordinate upperLeft, Coordinate lowerRight){
        
        int counter = 0;
        if(pointList.size()>0){
          //iterate through point list
          for (int i=0; i<pointList.size(); i++) {
              //for every point dp, delete if in rectangle
              DataPoint dp = pointList.get(i);
              if(dp.lat()<upperLeft.getLat() &&
                 dp.lon()>upperLeft.getLon() &&
                 dp.lat()>lowerRight.getLat() &&
                 dp.lon()<lowerRight.getLon()){
                  pointList.remove(i);
                  i--; //indexes change due to remove
                  counter++;
              }
          }
        }
        return counter;
    }
    

    /**
     * Decodes the transmitted int into a char and adds it to the input buffer that
     * can directly be accessed by a Data object as a local variable. 
     * @param plus transmitted ASCII character as an int
     * @throws UnsupportedEncodingException 
     */
    public void appendOutputString(int plus) throws UnsupportedEncodingException{
        byte[] b = new byte[1];
        b[0] = (byte)plus;
        String s = new String (b, "UTF-8");
        outputDataString = outputDataString + s;
        
    }
    
    public void clearOutputString(){
        outputDataString = "";
    }
    
    /**
     * Returns true if the point is already stored in this Data object
     * @param lat latitude
     * @param lon longitude
     * @return true when point exists, false when point doesn't exist
     */
    private boolean pointExists(double lat, double lon){
        boolean exists = false;
        
        if(pointList.size()>0){
          //iterate through point list
          for (DataPoint dp : pointList) {
              //for every point dp, do
              if(equals(dp.lat(),lat) && equals(dp.lon(),lon)) exists = true;
          }
        }
        return exists;
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
    
    
    /**
     * Reads the data in the input buffer (stored as a local variable in the 
     * Data object) and adds the point correctly to the DataPoint ArrayList
     * @return error type: -1 (not defined), 0 (no error), 1 (transmission error), 2 (no values stored on device). 
     */
    public int treatData(String data){
        String[] lines = data.split("\n");
        
        int errorType = -1; //-1: not defined, 0: no error, 1,2,3: error types
        int pointCounter = 0;
        
        for (int i=0; i<lines.length; i++){
            String[] seperated = lines[i].split(",");
            
            //the device could give the following responses:
            //$BMVAL + values
            if("$BMVAL".equals(seperated[0])){
                //seperated[0]: identifier ($BMVAL)
                //seperated[1]: id_value
                //seperated[2]: track_id
                //seperated[3]: latitude
                //seperated[4]: longitude
                //seperated[5]: ddmmyy
                //seperated[6]: hhmmsscc
                //seperated[7]: s1
                //seperated[8]: s2
                //seperated[9]: s3
                //seperated[10]: s4
                
                //lat format: 46519025
            int trackID = Integer.parseInt(seperated[2]);
            long rawLat = Long.parseLong(seperated[3]);
            long rawLon = Long.parseLong(seperated[4]);

            long date = Long.parseLong(seperated[5]);
            long time = Long.parseLong(seperated[6]);
            int s1 = Integer.parseInt(seperated[7]);
            int s2 = Integer.parseInt(seperated[8]);
            int s3 = Integer.parseInt(seperated[9]);
            int s4 = Integer.parseInt(seperated[10]);
            int s5 = 0;
            double latitude = rawLat / 1000000.00;
            double longitude = rawLon / 1000000.00;
            if(DATA_DEBUG) BeMapEditor.mainWindow.append("Point " +seperated[0]+": "+rawLat+","+rawLon+" "+date+" "+time+" "+s1+" "+s2+" "+s3+" "+s4+" "+s5+"\n");

                storePoint(trackID,latitude,longitude,date,time,s1,s2,s3,s4,s5,false);
            
                pointCounter++;
                errorType = 0; //there's at least one point stored
            
                if(DATA_DEBUG) BeMapEditor.mainWindow.append("Point successfully stored.\n"); 

            }
            else if(DATA_DEBUG) BeMapEditor.mainWindow.append("Point ignored\n");
            
            else if(("$BMERR".equals(seperated[0]))){
                //collect error type in order to print it only once.
                //error priority: 0,2,1
                
                if(DATA_DEBUG) BeMapEditor.mainWindow.append("ERR: "+ seperated[1]);
                switch (Integer.parseInt(seperated[1])) {
                    case ERROR_TYPE_TRANSMISSION: if(errorType != 2 && errorType != 0)
                                                    errorType = 1;
                                                  break;
                    case ERROR_TYPE_NO_VALUES: if(errorType != 0)
                                                errorType = 2;
                                               break;
                }        
            }
            
        }
        
        //print errors only once
                switch(errorType){
                    case ERROR_TYPE_TRANSMISSION: BeMapEditor.mainWindow.append(ERROR_MSG_TRANSMISSION);
                                                  break;
                    case ERROR_TYPE_NO_VALUES: BeMapEditor.mainWindow.append(ERROR_MSG_NO_VALUES);
                                               break;
                    case NO_ERROR: BeMapEditor.mainWindow.append("Transmission ok. "+ pointCounter + " points stored.\n");
                                   break;
                }
        
        outputDataString="";
        return pointCounter;
                    
    }
    
    /**
     * Draws all the points of the data object on the map, and applies the
     * time filter if activated in the mainWindow class.
     */
    public void drawAllPoints(){
        long timespread = BeMapEditor.mainWindow.getTimeSliderValue();
        if(DATA_DEBUG) BeMapEditor.mainWindow.append("Time slider: "+timespread+"\n");
        int style = BeMapEditor.settings.NORMAL;
        if(trackID == GLOBAL) style = BeMapEditor.settings.getGlobalStyle();
        
        if(pointList.size()>0){
          //iterate through point list
          for (DataPoint dp : pointList) {
              //for every point dp, do
              //TODO: only draw points in the time spread
              if(!BeMapEditor.mainWindow.timeSliderActive()){
              drawPoint(dp.lat(),dp.lon(),dp.getSensor(BeMapEditor.mainWindow.getSensorNumber()),BeMapEditor.mainWindow.getSensorNumber(),style);
              }
              //else if (DATA_DEBUG) BeMapEditor.mainWindow.append("Point ignored due to time slider\n");
          }
        }
    }
    
    public void addDataToGraph(XYSeries temp,XYSeries hum,XYSeries gaz1,XYSeries gaz2,XYSeries acc){
        
        
          for (DataPoint dp : pointList) {
              {
              temp.add(dp.getDateObject().getTime(),dp.s4()/100.0);
              hum.add(dp.getDateObject().getTime(),dp.s3()/100.0);
              gaz1.add(dp.getDateObject().getTime(),dp.s1());
              gaz2.add(dp.getDateObject().getTime(),dp.s2());
              acc.add(dp.getDateObject().getTime(),dp.s5());
              }
              //else if (DATA_DEBUG) BeMapEditor.mainWindow.append("Point ignored due to time slider\n");
          }
        
    }
    
    public String exportCSV(){
        String send = "";
        
        Iterator<DataPoint> it = pointList.iterator();
        while(it.hasNext())
        {
            DataPoint p = it.next();
            send += p.getDataPointCSV();
        }
        
        return send;
    }
    
    /**
     * Creates a JSONArray containing all the points of the data layer
     * @return JSONArray containing JSONObjects (datapoints)
     * @throws JSONException 
     */
    public JSONArray exportJSONList() throws JSONException{
   
        //this will be the track array of one single track.
        JSONArray jsonArray = new JSONArray();
        
        Iterator<DataPoint> it = pointList.iterator();
        while(it.hasNext())
        {
            DataPoint p = it.next();
            jsonArray.put(p.getDataPointJSON());
        }

        return jsonArray;
    }
    
    /**
     * Creates a JSONArray containing all the points of the data layer
     * @return JSONArray containing JSONObjects (datapoints)
     * @throws JSONException 
     */
    public JSONArray exportJSONListServer() throws JSONException{
   
        //this will be the track array of one single track.
        JSONArray jsonArray = new JSONArray();
        
        Iterator<DataPoint> it = pointList.iterator();
        while(it.hasNext())
        {
            DataPoint p = it.next();
            if(!p.isOnServer()) jsonArray.put(p.getDataPointJSON());
        }

        return jsonArray;
    }
    
    /**
     * Imports an entire JSONArray of points, overwrites the current list and draws
     * the points on the map if the boolean draw is set true.
     * @return returns the number of points stored.
     * @param gpsData JSONArray containing JSONObjects (points)
     * @param draw draw points on the map if true
     * @throws JSONException 
     */
    public int importJSONList(JSONArray gpsData) throws JSONException{

        
        //attention: value for s4 corresponds to a temperature. it should be drawn in a blue-orange color range.
        
        
        int pointsNumber = gpsData.length();
        
          for(int j = 0; j < pointsNumber; j++)
          {
           JSONObject innerObj = new JSONObject(gpsData.getJSONObject(j).toString());
            //Iterate through the elements of the array j.
            double lat = innerObj.getDouble("lat");
            double lon = innerObj.getDouble("lon");
            long date = innerObj.getLong("date");
            long time = innerObj.getLong("time");
            int trackID = innerObj.getInt("track");
            int s1 = innerObj.getInt("s1");
            int s2 = innerObj.getInt("s2");
            int s3 = innerObj.getInt("s3");
            int s4 = innerObj.getInt("s4"); //temperature
            int s5 = innerObj.getInt("s5"); //acc
            boolean sent = innerObj.getBoolean("sent");
            
            storePoint(trackID,lat,lon,date,time,s1,s2,s3,s4,s5,sent);
            
          }
        return pointsNumber;

    }
    
    /**
     * Imports an entire JSONArray of points, overwrites the current list and draws
     * the points on the map if the boolean draw is set true.
     * @return returns the number of points stored.
     * @param gpsData JSONArray containing JSONObjects (points)
     * @param draw draw points on the map if true
     * @throws JSONException 
     */
    public int importJSONListServer(JSONArray gpsData) throws JSONException{

        
        //attention: value for s4 corresponds to a temperature. it should be drawn in a blue-orange color range.
        
        
        int pointsNumber = gpsData.length();
        
          for(int j = 0; j < pointsNumber; j++)
          {
           JSONObject innerObj = new JSONObject(gpsData.getJSONObject(j).toString());
            //Iterate through the elements of the array j.
            double lat = innerObj.getDouble("lat");
            double lon = innerObj.getDouble("lon");
            long date = innerObj.getLong("date");
            long time = innerObj.getLong("time");
            int trackID = innerObj.getInt("track");
            int s1 = innerObj.getInt("s1");
            int s2 = innerObj.getInt("s2");
            int s3 = innerObj.getInt("s3");
            int s4 = innerObj.getInt("s4"); //temperature
            int s5 = innerObj.getInt("s5"); //acc
            
            storePoint(trackID,lat,lon,date,time,s1,s2,s3,s4,s5,true);
            
          }
        return pointsNumber;

    }
    
    /** 
     * Plots the points on the map.  
     * @param lat latitude in decimal degrees format
     * @param lon longitude in decimal degrees format
     * @param value is a value between 0 and 255 that defines the color from green to red. (0 to 100 for humidity and -100 to +100 for temperature)
     * @param type is the type of the value (sensor number), defines the color range.
     */
    public void drawPoint(double lat, double lon, int value, int type, int style){
        JMapViewer map = BeMapEditor.mainWindow.getMap();
        
        if (value == -1) type = DataPoint.TYPE_GREY;
        
        if(style == BeMapEditor.settings.NORMAL){
            Color col = DataPoint.chooseColor(value,type,255);
            MapMarkerDot point = new MapMarkerDot(col,lat,lon);
            point.setBackColor(col);
        
            map.addMapMarker(point);
            map.setMapMarkerVisible(true);
        }
        else if(style == BeMapEditor.settings.CLOUD){
            Color col = DataPoint.chooseColor(value,type,BeMapEditor.settings.getTransparency());
            MapPolygon poly = getPolygon(lat,lon,col);
        
            map.addMapPolygon(poly);
            map.setMapPolygonsVisible(true);
        }
        else BeMapEditor.mainWindow.append("\nIntern error: No style selected!");
        
        }
    
    private MapPolygon getPolygon(double lat, double lon, Color col){
        List<Coordinate> coords = new ArrayList<>();
        int dalpha = 360 / NB_POINTS;
       
        for(int i=0; i<NB_POINTS;i++){
            int alpha = i*dalpha;
            double dx = GLOBAL_R * Math.cos(alpha);
            double dy = GLOBAL_R * Math.sin(alpha);
            Coordinate c = new Coordinate(lat+dy,lon+dx);
            coords.add(c);
        }
        Layer global = new Layer("Global");
        Style style = new Style();
        style.setBackColor(col);
        style.setColor(col);
        style.setStroke(new BasicStroke(0));
        MapPolygon poly = new MapPolygonImpl(global,"",coords,style);
        return poly;
        
    }
    
    
    
    /**
     * Removes all the MapMarkers on the map.
     */
    public void removeMapMarkers(){
        BeMapEditor.mainWindow.getMap().removeAllMapMarkers();
        BeMapEditor.mainWindow.getMap().removeAllMapPolygons();
    }
    
    /**
     * Find the nearest point for the point clicked on the map and return
     * it as a JSON String, or send an empty String if the next point is further
     * than MAX_DEGRE.
     * @param pointClicked Screen coordinates of the clicked point
     * @return JSON String (empty if no point / JSONObject of the point)
     * @throws JSONException 
     */
    public String getNearestPoint(Point pointClicked) throws JSONException{
        String returnString = "";
        
        
        JMapViewer map = BeMapEditor.mainWindow.getMap();
        Coordinate clicked = map.getPosition(pointClicked);
                
        DataPoint nearest = new DataPoint();
        Iterator<DataPoint> it = pointList.iterator();
        
        while(it.hasNext()){
            //if windowcords_dist < MAPMARKER --> take point
            //else 
            DataPoint p = it.next();
            double dist = calculateDistance(p.coord(),clicked); //distance between clicked point and point
            if(dist < MAX_DEGRE && !nearest.isDefined()){
                //nearest has to be set at the first time
                nearest = p;
            }
            else if(dist < MAX_DEGRE && dist < calculateDistance(nearest.coord(),p.coord())){
                //point is nearer than the previous one
                nearest = p;
            }
        }
        
        if(nearest.isDefined()){
            //generate JSON point
            returnString = nearest.getDataPointJSON().toString();
        }
        
        return returnString;
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
    /**
     * Iterates through the entire DataPoint array list in order to find the youngest
     * point (Slow, shouldn't be used in loops)
     * @return the date of the youngest point in yyMMddhh
     */
    public long getYoungestDate() {
        Iterator<DataPoint> it = pointList.iterator();
        DataPoint youngest = new DataPoint();
        
        while(it.hasNext()){
            DataPoint p = it.next();
           // if(!youngest.isDefined() || p.getDateObject() > youngest.getDateObject) youngest = p;
        }
        return 0;
    }
    
    /**
     * Iterates through the entire DataPoint array list in order to find the oldest
     * point (Slow, shouldn't be used in loops)
     * @return the date of the oldest point in yyMMddhh
     */
    public long getOldestDate() {
        Iterator<DataPoint> it = pointList.iterator();
        DataPoint oldest = new DataPoint();
        
        while(it.hasNext()){
            DataPoint p = it.next();
            //if(!oldest.isDefined() || p.dateTime < oldest.dateTime) oldest = p;
        }
        return 0;
    }
    
    public void createModel() {
        if(model.isRendered()) BeMapEditor.mainWindow.append("\nModel already existing");
        else{
            try {
                model.render(pointList);
            } catch (Exception ex) {
                Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
   
}

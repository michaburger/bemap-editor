

package jcan2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.openstreetmap.gui.jmapviewer.Coordinate;


/**
 * Data structure that contains information about one single point.
 * @author Micha Burger
 */
public class DataPoint {

    int id;
    double lat;
    double lon;
    long date; //ddMMyy
    long time; //hhmmsscc
    long dateTime; //special value only used for the time slider
    int s1; //sensor values
    int s2;
    int s3;
    int s4;
    int s5;
    boolean set = false; //defines if a point has been defined
    boolean onServer = false; //defines if the point has already been sent to the server
    
    public void DataPoint(){
    
    }

    //getter (returns a point as a JSON object)
    /**
     * Returns the point as a JSON Object containing id, lat, lon, date, time, s1, s2, s3, s4.
     * @return JSONObject of the point
     * @throws JSONException 
     */
    public JSONObject getDataPointJSON() throws JSONException {
        //create a new JSONObject, fill it and return it
        JSONObject point = new JSONObject();
        point.put("id",id);
        point.put("lat",lat);
        point.put("lon",lon);
        point.put("date",date);
        point.put("time",time);
        point.put("s1",s1);
        point.put("s2",s2);
        point.put("s3",s3);
        point.put("s4",s4);
        point.put("s5", 0); //envoyer 0 en attendant
        point.put("sent",onServer);
        return point;
    }
    
    public JSONObject getDataPointJSONForServer()throws JSONException {
        //create a new JSONObject, fill it and return it
        JSONObject point = new JSONObject();
        point.put("id",id);
        point.put("lat",lat);
        point.put("lon",lon);
        point.put("date",date);
        point.put("time",time);
        point.put("s1",s1);
        point.put("s2",s2);
        point.put("s3",s3);
        point.put("s4",s4);
        point.put("s5", 0); //envoyer 0 en attendant
        return point;
    }
    
     //setter 
    /**
     * Sets the values for a data point.
     * @param lat Latitude in decimal degrees
     * @param lon Longitude in decimal degrees
     * @param id Identifier
     * @param date Date in ddMMyy
     * @param time Time in hhmmsscc
     * @param s1 CO
     * @param s2 NO2
     * @param s3 Humidity
     * @param s4 Temperature
     * @param s5 Accelerometer
     */
    public void setDataPoint(double lat, double lon, int id, long date, long time,int s1, int s2, int s3, int s4, int s5, boolean sent){
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.date = date;
        this.time = time;
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
        this.s4 = s4;
        this.s5 = s5;
        onServer = sent;
        
        dateTime = getDateTime(date,time);
        set = true;
    }
    
    public void removeServerFlag(){
        onServer = true;
    }
    
    /**
     * Getter for the coordinates of the point as a Coordinates object
     * @return Coordinate of the point
     */
    public Coordinate coord(){
        Coordinate c = new Coordinate(lat,lon);
        return c;
    }
    
    /**
     * Used to determinate if the point is empty or contains information
     * @return true if the point contains values
     */
    public boolean isDefined(){return set;}
    
    //returns longitude of the given point
    /**
     * Longitude getter
     * @return Longitude in decimal degrees
     */
    public double lon(){
        return lon;
    }
    
    public int id(){return id;}
    
    //returns latitude of the given point
    /**
     * Latitude getter
     * @return Latitude in decimal degrees
     */
    public double lat(){
        return lat;
    }
    
    public int seconds(){
        int hour = (int) (time / 1000000);
        int min = (int) ((time /  10000) - hour * 100);
        int sec = (int) ((time / 100) - min * 100 - hour * 10000);
        int seconds = hour * 3600 + min * 60 + sec;
        return seconds;
    }
    
    public int hour(){
        return (int) (time / 1000000);
    }
    
    /**
     * For the time slider, returns the hour and the date in a long.
     * @param date date in the format ddMMyy
     * @param time time in the format hhmmsscc
     * @return datetime in the format yyMMddhh
     */
    private long getDateTime(long date, long time){
        int day = (int) date / 10000;
        int month = (int) date / 100 - day * 100;
        int year = (int) date - day * 10000 - month * 100;
        int hour = (int) time / 1000000;
        
        return year * 1000000 + month * 10000 + day * 100 + hour;
    }
    
    
    /**
     * Date getter
     * @return Date in ddMMyy
     */
    public long date(){return date;}
    /**
     * Time getter
     * @return Time in hhmmsscc
     */
    public long time(){return time;}
    public int s1(){return s1;}
    public int s2(){return s2;}
    public int s3(){return s3;}
    public int s4(){return s4;}
    public int s5(){return s5;}
    
    /**
     * Get the value of a specific sensor
     * @param sensorNb Number of the sensor from 1 to 4
     * @return sensor value or 0 if invalid sensor number
     */
    public int getSensor(int sensorNb){
        switch(sensorNb){
            case 1: return s1;
                    
            case 2: return s2;
                    
            case 3: return s3;
                    
            case 4: return s4;
                
            case 5: return s5;
                    
        }
        return 0;
    }


}
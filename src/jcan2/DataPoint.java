

package jcan2;

import java.awt.Color;
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

    private int id;
    private double lat;
    private double lon;
    private long date; //ddMMyy
    private long time; //hhmmsscc
    long dateTime; //special value only used for the time slider
    private int s1; //sensor values
    private int s2;
    private int s3;
    private int s4;
    private int s5;
    private boolean set = false; //defines if a point has been defined
    private boolean onServer = false; //defines if the point has already been sent to the server
    
    static final int TYPE_POLL1 = 1; //type definition for the drawn color ranges
    static final int TYPE_POLL2 = 2;
    static final int TYPE_HUM = 3;
    static final int TYPE_TEMP = 4;
    static final int TYPE_ACC = 5;
    static final int TYPE_GREY = -1; //pollution placeholder for drawing a grey point
    static final int TEMPERATURE_OFFSET = -5;
    
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
    
    public boolean isOnServer(){
        return onServer;
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
    
    public static Color chooseColor(int value, int type, int opacity){
        int r=0,g=0,b=0;
        switch(type) {
            case TYPE_GREY: r=g=b=128;
                            break;
            case TYPE_HUM:  g = 127;
                            if(value<=50){
                             r = 255;
                             b = 5*value+5;
                            }
                            else{
                             r = 255-(5*(value-49));
                             b = 255;
                            }
                            if(value < 0 || value > 100) r=g=b=128;
                            break;
                            
            case TYPE_TEMP:   value += TEMPERATURE_OFFSET;
                              if (value < -10) value = -10;
                              if (value > 50) value = 50;
                              if (value < 20){
                                  r = (int)(85 + 8.5*value);
                                  g = (int)(133.33333 + 3.33333 * value);
                              }
                              else{
                                  r = 255;
                                  g = (int)(266.6667 - 3.33333 * value);
                              }
                              b = (int)(212.5 - 4.25 * value);
                              break;
            case TYPE_POLL1:  if (value < 150) value = 150; //rescale
                              if (value > 200) value = 200;
                              value = (int) ((int) (value - 150)*5.1);
                              
                             
                              if (value<=127){
                                 g = 200;
                                 r = 2*value*4/5;
                              }
                              else{
                                 g = (512 - 2*value -2)*4/5;
                                 r = 200;
                              }
                              if(value<0||value>255) r=g=b=128; //no value
                              break;
        
            case TYPE_POLL2:  if (value < 130) value = 130; //rescale
                              if (value > 180) value = 180;
                              value = (int) ((int) (value - 130)*5.1);
                             
                              if (value<=127){
                                 g = 200;
                                 r = 2*value*4/5;
                              }
                              else{
                                 g = (512 - 2*value -2)*4/5;
                                 r = 200;
                              }
                              if(value<0||value>255) r=g=b=128; //no value
                              break;
            case TYPE_ACC:  if (value < 0) {
                                r=g=b=128;
                                break;
                            }
                            if (value > 4) value = 4;
                            r=255;
                            switch (value) {
                                case 0: g=b=200;
                                        break;
                                case 1: g=b=150;
                                        break;
                                case 2: g=b=100;
                                        break;
                                case 3: g=b=50;
                                        break;
                                case 4: g=b=0;
                                        break;
                            }
                            
                            break;
        }
        
        Color pointColor = new Color(r,g,b,opacity);
        return pointColor;
    } 


}
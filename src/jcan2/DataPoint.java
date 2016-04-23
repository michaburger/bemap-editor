

package jcan2;

import java.awt.Color;
import java.util.Calendar;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import org.openstreetmap.gui.jmapviewer.Coordinate;


/**
 * Data structure that contains information about one single point.
 * @author Micha Burger
 */
public class DataPoint {

    private long id;
    private int trackID;
    private double lat;
    private double lon;
    private long date; //ddMMyy
    private long time; //hhmmsscc
    private int co; //sensor values
    private int no2;
    private int hum;
    private int temp;
    private int vib;
    private boolean set = false; //defines if a point has been defined
    private boolean onServer = false; //defines if the point has already been sent to the server
    
    static final int TYPE_POLL1 = 1; //type definition for the drawn color ranges
    static final int TYPE_POLL2 = 2;
    static final int TYPE_HUM = 3;
    static final int TYPE_TEMP = 4;
    static final int TYPE_ACC = 5;
    static final int TYPE_GREY = -1; //pollution placeholder for drawing a grey point
    static final int TEMPERATURE_OFFSET = -7;
    
    public void DataPoint(){
    
    }
    
    public int getTrackID(){return trackID;}

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
        point.put("track",trackID);
        point.put("s1",co);
        point.put("s2",no2);
        point.put("s3",hum);
        point.put("s4",temp);
        point.put("s5", vib); //envoyer 0 en attendant
        point.put("sent",onServer);
        return point;
    }
    
    public String getDataPointCSV(){
        String dataPoint = "";
        String separator = ",";
        
        String usr = ""+BeMapEditor.mainWindow.getUsr();
        
        dataPoint += id + separator;
        dataPoint += usr + separator;
        dataPoint += trackID + separator;
        dataPoint += getCSVDateFormat() + separator;
        dataPoint += lat + separator;
        dataPoint += lon + separator;
        dataPoint += temp/100.0 + separator;
        dataPoint += hum/100.0 + separator;
        dataPoint += co + separator;
        dataPoint += no2 + separator;
        dataPoint += vib + separator;
        dataPoint += convertCOppm() + separator;
        dataPoint += convertNOppm() + separator;
        
        dataPoint += "\n";
        
        return dataPoint;
    }
    
    public JSONObject getDataPointJSONForServer()throws JSONException {
        //create a new JSONObject, fill it and return it
        JSONObject point = new JSONObject();
        point.put("id",id);
        point.put("lat",lat);
        point.put("lon",lon);
        point.put("date",date);
        point.put("time",time);
        point.put("track",trackID);
        point.put("s1",co);
        point.put("s2",no2);
        point.put("s3",hum);
        point.put("s4",temp);
        point.put("s5", vib); //envoyer 0 en attendant
        return point;
    }
    
     //setter 
    /**
     * Sets the values for a data point.
     * @param trackID track identifier
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
    public void setDataPoint(int trackID, double lat, double lon, int id, long date, long time,int s1, int s2, int s3, int s4, int s5, boolean sent){
        this.id = id;
        this.trackID = trackID;
        this.lat = lat;
        this.lon = lon;
        this.date = date;
        this.time = time;
        this.co = s1;
        this.no2 = s2;
        this.hum = s3;
        this.temp = s4;
        this.vib = s5;
        onServer = sent;

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
    
    public long id(){return id;}
    
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
    public Date getDateObject(){
        int day = (int) date / 10000;
        int month = (int) date / 100 - day * 100;
        int year = (int) date - day * 10000 - month * 100;
        int hour = (int) time / 1000000;
        int min = (int) ((time /  10000) - hour * 100);
        int sec = (int) ((time / 100) - min * 100 - hour * 10000);
        Date d = new Date();
        d.setYear(year);
        d.setMonth(month);
        d.setDate(day);
        d.setHours(hour);
        d.setMinutes(min);
        d.setSeconds(sec);
        System.out.println(d);
        
        return d;
    }
    
    public String getCSVDateFormat(){
        String format = "";
        
        int day = (int) date / 10000;
        int month = (int) date / 100 - day * 100;
        int year = (int) date - day * 10000 - month * 100;
        int hour = (int) time / 1000000;
        int min = (int) ((time /  10000) - hour * 100);
        int sec = (int) ((time / 100) - min * 100 - hour * 10000);
        
        format += year + "-" + month + "-" + day + " " + hour + ":"
                + min + ":" + sec;
        
        return format;
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
    public int s1(){return co;}
    public int s2(){return no2;}
    public double s3(){return hum;}
    public double s4(){return temp;}
    public int s5(){return vib;}
    
    /**
     * Get the value of a specific sensor
     * @param sensorNb Number of the sensor from 1 to 4
     * @return sensor value or 0 if invalid sensor number
     */
    public int getSensor(int sensorNb){
        switch(sensorNb){
            case 1: return co;
                    
            case 2: return no2;
                    
            case 3: return hum;
                    
            case 4: return temp;
                
            case 5: return vib;
                    
        }
        return 0;
    }
    
    public double convertCOppm(){
        return Math.pow(10,(0.64-1.21*Math.log10((1024.0-co)/co)));
    }
    
    public double convertNOppm(){
        return Math.pow(10,(0.809+1.031*Math.log10((1024.0-no2)/no2)));
    }
    
    public static Color chooseColor(int value, int type, int opacity){
        int r=0,g=0,b=0;
        value = (value /4)-1;
        switch(type) {
            case TYPE_GREY: r=g=b=128;
                            break;
            case TYPE_HUM:  value = value / 100;
                            g = 127;
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
                            
            case TYPE_TEMP:   value = value / 100;
                              value += TEMPERATURE_OFFSET;
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


package bemap;

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
    private int deviceUsrID;
    private int trackID;
    private double lat;
    private double lon;
    private long date; //ddMMyy
    private long time; //hhmmsscc
    private int co; //sensor values
    private int no2;
    private int hum; //to divide by 100.0!
    private int temp; //to divide by 100.0!
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
    
    static final int SENSOR_CO = 1;
    static final int SENSOR_NO = 2;
    static final int SENSOR_HUM = 3;
    static final int SENSOR_TEMP = 4;
    static final int SENSOR_VIB = 5;
    
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
        point.put("usr",deviceUsrID);
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
    
    public int getUsr(){return deviceUsrID;}
    
    public void setUsr(int usr){this.deviceUsrID = usr;}
    
    public String getDataPointCSV(){
        String dataPoint = "";
        String separator = ",";
        long alt_placeholder = 777;
        
        dataPoint += getCSVDateFormat() + separator;
        dataPoint += deviceUsrID + separator;
        dataPoint += trackID + separator;
        dataPoint += lon + separator;
        dataPoint += lat + separator;
        dataPoint += alt_placeholder + separator;
        dataPoint += vib + separator;
        dataPoint += convertCOppm() + separator;
        dataPoint += convertNOppm() + separator;
        dataPoint += co + separator;
        dataPoint += no2 + separator;
        dataPoint += temp/100.0 + separator;
        dataPoint += hum/100.0 + separator;
        dataPoint += "\n";
        
        return dataPoint;
    }
    
    public JSONObject getDataPointJSONForServer()throws JSONException {
        //create a new JSONObject, fill it and return it
        JSONObject point = new JSONObject();
        point.put("id",id);
        point.put("usr",deviceUsrID);
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
     * @param co CO
     * @param no NO2
     * @param hum Humidity
     * @param temp Temperature
     * @param vib Accelerometer
     */
    public void setDataPoint(int usr, int trackID, double lat, double lon, int id, long date, long time,int co, int no, int hum, int temp, int vib, boolean sent){
        this.id = id;
        this.deviceUsrID = usr;
        this.trackID = trackID;
        this.lat = lat;
        this.lon = lon;
        this.date = date;
        this.time = time;
        this.co = co;
        this.no2 = no;
        this.hum = hum;
        this.temp = temp;
        this.vib = vib;
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
    public int co(){return co;}
    public int no2(){return no2;}
    public double hum(){return hum/100.0;}
    public double temp(){return temp/100.0;}
    public int vib(){return vib;}
    
    /**
     * Get the value of a specific sensor
     * @param sensorNb Number of the sensor from 1 to 4
     * @return sensor value or 0 if invalid sensor number
     */
    public int getSensor(int sensorNb){
        switch(sensorNb){
            case SENSOR_CO: double minCo = (double) BeMapEditor.trackOrganiser.getData().getCoGreenLevel();
                            double maxCo = (double) BeMapEditor.trackOrganiser.getData().getCoRedLevel();
                            double ppmCo = convertCOppm();
                            //BeMapEditor.mainWindow.append("\nppmCo: "+ppmCo);
                            //BeMapEditor.mainWindow.append("\nminCo: "+minCo);
                            //BeMapEditor.mainWindow.append("\nmaxCo: "+maxCo);
                            if(ppmCo <= minCo) return 0;
                            else if (ppmCo >= maxCo) return 255;
                            int value = (int) (255*((ppmCo-minCo)/(maxCo-minCo)));
                            //BeMapEditor.mainWindow.append("\nColor: "+maxCo);
                            return value;
                    
            case SENSOR_NO: double minNo = BeMapEditor.trackOrganiser.getData().getNoGreenLevel();
                            double maxNo = BeMapEditor.trackOrganiser.getData().getNoRedLevel();
                            double ppmNo = convertNOppm();
                            if(ppmNo <= minNo) return 0;
                            else if (ppmNo >= maxNo) return 255;
                            return (int) (255*((ppmNo-minNo)/(maxNo-minNo))); 
                    
            case SENSOR_HUM: return hum;
                    
            case SENSOR_TEMP: return temp;
                
            case SENSOR_VIB: return vib;
                    
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
        //value = (value /4)-1;
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
            case TYPE_POLL2:  if (value < 150) value = 150; //rescale
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
        /*
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
*/
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
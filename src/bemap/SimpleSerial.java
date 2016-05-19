/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bemap;


import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Micha
 */
public class SimpleSerial {
    private String serialPortName = "Error - no Port found";
    private static final boolean SERIAL_DEBUG = false;
    private static final int MAX_DATA_PER_IMPORT = 20;
    private static final int WAIT_MS = 50; //time to wait for the device to respond
    private static final int WAIT_MS_POINTS = WAIT_MS + 10*MAX_DATA_PER_IMPORT; //time to wait for the device to respond
    //Test: 100ms = 15 points
    //security factor 200% --> time_ms = 15 ms per point
    private boolean deviceConnected = false;
    private String currentPortName;
    
    public SimpleSerial(){
        super();
    }
    
public String getPortName(){
    return serialPortName;
}
    
public int searchDevicePort() throws InterruptedException{
    String[] portNames = SerialPortList.getPortNames();
    if(SERIAL_DEBUG) BeMapEditor.mainWindow.append("\nFound ports: " + Arrays.toString(portNames));
    int portNumber = -1;
        for(int i = 0; i < portNames.length; i++){
            
            currentPortName = portNames[i];
            
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(new PortSearchTask());
            
        try {
            String returnPort = future.get(3,TimeUnit.SECONDS);
            if(!"NULL".equals(returnPort)){
                portNumber = i;
                i = portNames.length; //quit loop
            }
            
        } catch (ExecutionException ex) {
            Logger.getLogger(SimpleSerial.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TimeoutException ex) {
            Logger.getLogger(SimpleSerial.class.getName()).log(Level.SEVERE, null, ex);
            if(SERIAL_DEBUG) BeMapEditor.mainWindow.append("\nOpening port "+currentPortName+" has timed out");
        }
        
        executor.shutdownNow();
        }
    if(portNumber < 0) {
        deviceConnected = false;
        return 0;
    } 
    else {
        serialPortName = portNames[portNumber];
        deviceConnected = true;
        if(SERIAL_DEBUG) BeMapEditor.mainWindow.append("\nDevice detected on port "+serialPortName);
        return 1; //success
    }
}

public boolean deviceConnected(){
    return deviceConnected;
}

public JSONObject getRealTimeData() throws InterruptedException, JSONException{
    if(!"Error - no Port found".equals(serialPortName))
    {
        try {
            SerialPort serialPort = new SerialPort(serialPortName);
            
            
            serialPort.openPort();//Open serial port
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
            
            serialPort.writeBytes("$ORRTV*11\n".getBytes());//Write data to port
            Thread.sleep(WAIT_MS*5);
            String realTime = serialPort.readString();
            serialPort.closePort();
            int temp = 0;
            JSONObject output = new JSONObject();
            
            if(SERIAL_DEBUG) BeMapEditor.mainWindow.append("\n"+realTime);
            
            //decompose the received data
            String[] lines = realTime.split("\n");
            
            for (int i=0; i<lines.length; i++){
                String[] seperated = lines[i].split(",");
                if(SERIAL_DEBUG) BeMapEditor.mainWindow.append(seperated[0]);
                
                if("$BMRTV".equals(seperated[0])){
                    output.put("err",0);
                    output.put("temp", Integer.parseInt(seperated[8])/100.0); //temp data
                    output.put("hum", Integer.parseInt(seperated[7])/100.0);
                    output.put("gaz1", Integer.parseInt(seperated[5]));
                    output.put("gaz2", Integer.parseInt(seperated[6]));
                    output.put("ax", Double.parseDouble(seperated[9]));
                    output.put("ay", Double.parseDouble(seperated[10]));
                    output.put("az", Double.parseDouble(seperated[11]));
                    
                    return output;
                }
                
            }
            //error handling
            output.put("err",1);
            return output;
            
        } catch (SerialPortException ex) {
            Logger.getLogger(SimpleSerial.class.getName()).log(Level.SEVERE, null, ex);
        }
            
            
            }
      return null;  
}
     
public String writeData(String dataToWrite){
    if("Error - no Port found".equals(serialPortName)) return "ERROR";
    else{
        try {
            SerialPort serialPort = new SerialPort(serialPortName);
            
            serialPort.openPort();//Open serial port
            serialPort.setParams(SerialPort.BAUDRATE_115200,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
            
            serialPort.writeBytes(dataToWrite.getBytes());//Write data to port
            Thread.sleep(WAIT_MS*2);
            String answer = serialPort.readString();
            serialPort.closePort();
            return answer;
        } catch (SerialPortException ex) {
            Logger.getLogger(SimpleSerial.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SimpleSerial.class.getName()).log(Level.SEVERE, null, ex);
        }
                
    }    
    return "ERROR";
}
    


//returns the number of points stored or -1 if error
public int importDataFromDevice(JProgressBar progressBar, JTextArea status) throws InterruptedException, JSONException{
    if("Error - no Port found".equals(serialPortName)) return -1;
    else{
        SerialPort serialPort = new SerialPort(serialPortName);
            
         
            try {
                serialPort.openPort();//Open serial port
                serialPort.setParams(SerialPort.BAUDRATE_115200, 
                                    SerialPort.DATABITS_8,
                                    SerialPort.STOPBITS_1,
                                    SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
                
                serialPort.writeBytes("$ORUSR*11\n".getBytes());//Write data to port
                Thread.sleep(WAIT_MS);
                String userString = serialPort.readString();
                int usr = decodeUserID(userString);
                if(usr > 0){
                    BeMapEditor.mainWindow.setUsr(usr);
                    status.append("\nUser ID: "+usr);
                }
                else if (usr == -1){
                    //code for getting usr id from server
                    //$ORUSR,25*11\n
                    status.append("\nError: No user ID set!");
                    BeMapEditor.mainWindow.setUsr(-1);
                }
                else {
                    status.append("\nError: No User ID found! Import aborted.");
                    return -1;
                }
                
                //import settings
                serialPort.writeBytes("$ORCFG*11\n".getBytes());//Write data to port
                Thread.sleep(WAIT_MS);
                String configString = serialPort.readString();
                if(decodeSettings(configString)==1 && SERIAL_DEBUG) {
                    BeMapEditor.mainWindow.append("\nSettings decoded");
                }
                else BeMapEditor.mainWindow.append("\nError decoding settings");
                
                //Start importing points
                serialPort.writeBytes("$ORNUM*11\n".getBytes());
                Thread.sleep(WAIT_MS);
                String numberString = serialPort.readString();
                int totalNumber = decodeNbPoints(numberString);
                if(totalNumber > 0) status.append("\nNumber of points to import: "+totalNumber);
                else if (totalNumber == 0) {
                    status.append("\nNo points stored to import! ");
                    return 0;
                }
                else{
                    status.append("\nError: Number of Points");
                    return -1;
                }
                
                //prepare track to import
                BeMapEditor.trackOrganiser.createNewTrack("Import");
                
                
                int nbRequests = (totalNumber/(MAX_DATA_PER_IMPORT))+1;
                int rest = totalNumber - (nbRequests-1) * MAX_DATA_PER_IMPORT; //nb of points for the last request
                if(SERIAL_DEBUG) BeMapEditor.mainWindow.append("\nNumber of requests necessary: "+ nbRequests);
                if(SERIAL_DEBUG) BeMapEditor.mainWindow.append("\nPoints resting for last request: " + rest);
            
                //init progress bar
                progressBar.setMaximum(nbRequests);
                
                for(int i=0; i< (nbRequests-1); i++){
                    //import serie of points
                    int offset = i*MAX_DATA_PER_IMPORT;
                    if(importSerieOfPoints(serialPort,MAX_DATA_PER_IMPORT,offset)<0) return 0;
                    //actualize progress bar
                    progressBar.setValue(i+1);
                }
                int final_offset = (nbRequests-1)*MAX_DATA_PER_IMPORT;
                if(importSerieOfPoints(serialPort,rest,final_offset)<0) return 0; //import the rest of the points
                progressBar.setValue(nbRequests);
                
                status.append("\n"+totalNumber+" Points successfully imported");
                
                serialPort.writeString("$ORMEM*11\n");
                Thread.sleep(WAIT_MS);
                decodeMemoryState(serialPort.readString());
                
                serialPort.closePort();//Close serial port
                
                BeMapEditor.trackOrganiser.postImportTreatment(progressBar);
                return 1;
            }
            catch (SerialPortException ex) {
                return 0;//do not print error
            }
    }

}

public int sendDeleteRequest(){
    if("Error - no Port found".equals(serialPortName)) return -1;
    else{
        SerialPort serialPort = new SerialPort(serialPortName);
            
            try {
                serialPort.openPort();//Open serial port
                serialPort.setParams(SerialPort.BAUDRATE_9600, 
                                    SerialPort.DATABITS_8,
                                    SerialPort.STOPBITS_1,
                                    SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
                serialPort.writeString("$ORCLR*11\n");
                serialPort.closePort();
                return 1;
            }
            catch (SerialPortException ex) {
                return 0;//do not print error
            } 
    }
}

public int updateMemoryState() throws InterruptedException{
    if("Error - no Port found".equals(serialPortName)) return -1;
    else{
        SerialPort serialPort = new SerialPort(serialPortName);
            
            try {
                serialPort.openPort();//Open serial port
                serialPort.setParams(SerialPort.BAUDRATE_9600, 
                                    SerialPort.DATABITS_8,
                                    SerialPort.STOPBITS_1,
                                    SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
                serialPort.writeString("$ORMEM*11\n");
                Thread.sleep(WAIT_MS);
                decodeMemoryState(serialPort.readString());
                serialPort.closePort();
                return 1;
            }
            catch (SerialPortException ex) {
                return 0;//do not print error
            } 
    }
}
/**
 * BMCFG - config
 * @param memoryState 
 */
private void decodeMemoryState(String memoryState){
    //decompose the received data
        String[] lines = memoryState.split("\n");
        
        for (int i=0; i<lines.length; i++){
            if(SERIAL_DEBUG) BeMapEditor.mainWindow.append(lines[i]+"\n");
            String[] seperated = lines[i].split(",");
            if(SERIAL_DEBUG) BeMapEditor.mainWindow.append("Received: "+lines[i]+"\n");
            
            if("$BMMEM".equals(seperated[0])){
                int a = Integer.parseInt(seperated[1]);
                int b = Integer.parseInt(seperated[2]);
                int state = a / b;
                if(SERIAL_DEBUG) BeMapEditor.mainWindow.append("Memory state: "+state+"\n");
                BeMapEditor.mainWindow.setBar(state);
            }
            else if("$BMERR".equals(seperated[0])){
                if(SERIAL_DEBUG) BeMapEditor.mainWindow.append("Memory state error\n");
            }
            else if(SERIAL_DEBUG) BeMapEditor.mainWindow.append("Memory state error\n");
           
        }
}

private int importSerieOfPoints(SerialPort serialPort, int nb,int offset) throws InterruptedException {
        try {
            String send = "$ORGET,"+offset+","+nb+"*11\n";
            if(SERIAL_DEBUG) BeMapEditor.mainWindow.append("\nSent request: "+send);
            serialPort.writeBytes(send.getBytes());
            Thread.sleep(WAIT_MS_POINTS);
            String answer = serialPort.readString();
            if(SERIAL_DEBUG) BeMapEditor.mainWindow.append("\nAnswer: "+answer);
            
            if(BeMapEditor.trackOrganiser.getData().treatData(answer)==nb) return 1;
            else return 0;
            
        } catch (SerialPortException ex) {
            Logger.getLogger(SimpleSerial.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
}

private int decodeNbPoints(String numberString){
    //decompose the received data
        String[] lines = numberString.split("\n");
        int nbPoints = -1;
        
        for (int i=0; i<lines.length; i++){
            String[] seperated = lines[i].split(",");
            
            if("$BMNUM".equals(seperated[0])){
                nbPoints = Integer.parseInt(seperated[1]);
                return nbPoints;
            }
            
        }
        return nbPoints;
          
}

//returns usr id from string or -1 if error
private int decodeUserID(String userString){
    //decompose the received data
        String[] lines = userString.split("\n");
        
        for (int i=0; i<lines.length; i++){
            if(SERIAL_DEBUG) BeMapEditor.mainWindow.append(lines[i]+"\n");
            String[] seperated = lines[i].split(",");
            
            if("$BMUSR".equals(seperated[0])){
                return Integer.parseInt(seperated[1]);
            }
            else if("$BMERR".equals(seperated[0])){
                return -1;
            }
           
        }
    return -1;
    
    
}

private int decodeSettings(String config){
    //decompose the received data
        String[] lines = config.split("\n");
        
        for (int i=0; i<lines.length; i++){
            if(SERIAL_DEBUG) BeMapEditor.mainWindow.append(lines[i]+"\n");
            String[] seperated = lines[i].split(",");
            
            if("$BMCFG".equals(seperated[0])){
               
               /**
                * seperated[1]: Fw-version
                * seperated[2]: battery %
                * seperated[3]: memory %
                * seperated[4]: sleeptime
                * seperated[5]: tracking time
                * seperated[6]: sensor gaz
                * seperated[7]: temp/hum
                * seperated[8]: timeout fixed lost
                * seperated[9]: preheat time
                * seperated[10]: accl- bounce threshold
                * seperated[11]: accl- bounce time
                * seperated[12]: accl- inut threshold
                * seperated[13]: accl Act threshold
                * seperated[14]: accl inut time
                * */
               int firmwareVersion = Integer.parseInt(seperated[1]);
               int batteryState = Integer.parseInt(seperated[2]);
               int memoryState = Integer.parseInt(seperated[3]);
               int sleepTime = Integer.parseInt(seperated[4]);
               int trackingTime = Integer.parseInt(seperated[5]);
               int gasInterval = Integer.parseInt(seperated[6]);
               int tempHumInterval = Integer.parseInt(seperated[7]);
               int timeoutFixLost = Integer.parseInt(seperated[8]);
               int preheatTime = Integer.parseInt(seperated[9]);
               int accBounceThr = Integer.parseInt(seperated[10]);
               int accBounceTime = Integer.parseInt(seperated[11]);
               int AccInutThr = Integer.parseInt(seperated[12]);
               int AccActTime = Integer.parseInt(seperated[13]);
               int AccInutTime = Integer.parseInt(seperated[14]);
               
               
               BeMapEditor.settings.setConfig(firmwareVersion,batteryState,
                       memoryState,sleepTime,trackingTime,gasInterval,tempHumInterval,
                       timeoutFixLost,preheatTime,accBounceThr,accBounceTime,
                       AccInutThr,AccActTime,AccInutTime);
               
            }
            else if("$BMERR".equals(seperated[0])){
                return -1;
            }
           
        }
    return -1;

}

class PortSearchTask implements Callable<String> {
    @Override
    public String call() throws Exception {
        String port = "NULL";
        SerialPort serialPort = new SerialPort(currentPortName);
            if(SERIAL_DEBUG) BeMapEditor.mainWindow.append("\nOpening port "+currentPortName);
            
            
            try {
                serialPort.openPort();//Open serial port
                serialPort.setParams(SerialPort.BAUDRATE_9600, 
                                    SerialPort.DATABITS_8,
                                    SerialPort.STOPBITS_1,
                                    SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
                
                serialPort.writeBytes("$ORUSR*11\n".getBytes());//Write data to port
                Thread.sleep(WAIT_MS);
                byte[] buffer = serialPort.readBytes(1);//Read 1 bytes from serial port
                if(buffer[0] == 36) port = currentPortName; //success on this port when device answers with $
                else if (SERIAL_DEBUG) BeMapEditor.mainWindow.append("\nError: Port found but device doesn't answer with $");
                serialPort.closePort();//Close serial port
            }
            catch (SerialPortException ex) {
                //do not print error
                if(SERIAL_DEBUG) BeMapEditor.mainWindow.append("\nOpening port "+currentPortName+" failed");
            }
        
        return port;
    }
}
    
    
}

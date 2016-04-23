/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcan2;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Micha
 */
public class ServiceRoutine {
    private boolean importRunning = false;
    private boolean portScanning = false;
    private static final boolean ROUTINE_DEBUG = false;
    Timer serviceTimer = new Timer();
    SimpleSerial serial = new SimpleSerial();
    private static final int MAX_IMPORT_TRYS = 3;
    
    public ServiceRoutine(){
        serviceTimer.scheduleAtFixedRate(new execute(), 5000, 5000);
        serviceTimer.scheduleAtFixedRate(new export(), 2*60*1000, 2*60*1000);
    }
    
    /**
     * When disabled, the window will open again.
     */
    public void disableImportRunning(){
        importRunning = false;
    }
    
    public SimpleSerial getSerialConnection(){
        return serial;
    }
    
    
class export extends TimerTask {
    public void run(){
            BeMapEditor.mainWindow.exportToServer();
        }
    }
    

    
class execute extends TimerTask {
    public void run(){
        if(ROUTINE_DEBUG) BeMapEditor.mainWindow.append("ServiceRoutine called \n");
        if(!portScanning){
        portScanning = true;
        try {
            if(!importRunning && serial.searchDevicePort()==1) {
                importRunning = true;
                statusWindow importStatus = new statusWindow();
                importStatus = new statusWindow();
                importStatus.setVisible(true);
                importStatus.updateStatus("Device found on port: " + serial.getPortName());
                int returnStatus = -1;
                
                importStatus.disableButtons(); //disable buttons in window
                int errorCounter = 0;
                while (errorCounter < MAX_IMPORT_TRYS){
                    returnStatus = serial.importDataFromDevice(importStatus.getProgressBar(), importStatus.getStatusArea());
                    //importStatus.updateStatus("\nReturn: " + returnStatus);
                    if(returnStatus==0){
                        importStatus.enableButtons();
                        errorCounter = MAX_IMPORT_TRYS;
                    }
                    else if(returnStatus>0){
                        importStatus.enableButtons();
                        BeMapEditor.mainWindow.updateMap();
                        BeMapEditor.mainWindow.getMap().setDisplayToFitMapMarkers();
                        errorCounter = MAX_IMPORT_TRYS;
                    }
                   
                    errorCounter++;
                }
                if(errorCounter == MAX_IMPORT_TRYS) importStatus.updateStatus("\nSerial error: No points imported");
                importStatus.enableButtons();
                
                
            }
            portScanning = false;
            
        } catch (InterruptedException ex) {
            Logger.getLogger(ServiceRoutine.class.getName()).log(Level.SEVERE, null, ex);
        }
        portScanning = false;
        }
    }
    
}
    
}

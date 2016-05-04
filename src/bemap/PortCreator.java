/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bemap;

import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

/**
 * Creates the serial ports for the communication with the device.
 * @author Micha
 */
public abstract class PortCreator {
    
    private PortCreator() {} //No instance allowed
    
    public static SerialPort openPort(String portName){
        int baudrate = 9600;
        int databits = SerialPort.DATABITS_8;
        int stopbits = SerialPort.STOPBITS_1;
        int parity = SerialPort.PARITY_NONE;
        char flowControl = 'n';
        
        return openPort(portName, baudrate, databits, stopbits, parity, flowControl);
    }
    
    public static SerialPort openPort(String portName, int baudrate, int databits, int stopbits, int parity, char flowControl) {
        SerialPort serialPort = null;
        
        try{
            CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(portName);
            serialPort = (SerialPort)portId.open("MyAppl", 1000);
            serialPort.setSerialPortParams(baudrate, databits, stopbits, parity);
            int flowControlMode = SerialPort.FLOWCONTROL_NONE;
            switch (flowControl) {
                case 'h':
                case 'H':
                    flowControlMode = SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT;
                    break;
                case 's':
                case 'S': flowControlMode = SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT;
                    break;
                }
            serialPort.setFlowControlMode(flowControlMode);
        }
        catch (NoSuchPortException ex){
            return null;
        }
        catch (PortInUseException ex){
            return null;
        }
        catch (UnsupportedCommOperationException ex){
            return null;
        }
        return serialPort;
        
    }
    
}

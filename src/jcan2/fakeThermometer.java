/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcan2;

/**
 *
 * @author Micha
 */
public class fakeThermometer {
    double currentTemp;
    /**
     *
     */
    public fakeThermometer(){
        currentTemp = 20;
    }
    
    public double getNext(){
        double change = Math.random() - 0.5;
        double newTemp = currentTemp + change;
        if(newTemp > 39) newTemp = 39;
        if(newTemp < 14) newTemp = 14;
        currentTemp = newTemp;
        return newTemp;
    }
    
}


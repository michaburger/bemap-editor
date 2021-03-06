/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcan2;


import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.json.JSONException;
import org.json.JSONObject;


/**
 *
 * @author Micha
 */
public class realTimeWindow extends javax.swing.JFrame {
    int graphIndex = 0;
    XYSeries temp = new XYSeries("Temperature °C");
    XYSeries hum = new XYSeries("Humidity %");
    XYSeries gaz1 = new XYSeries("CO level offset -150");
    XYSeries gaz2 = new XYSeries("NO2 level offset -150");
    XYSeries ax = new XYSeries("X");
    XYSeries ay = new XYSeries("Y");
    XYSeries az = new XYSeries("Z");
    XYSeriesCollection dataset = new XYSeriesCollection();
    XYSeriesCollection accDataset = new XYSeriesCollection();
    
    private final int INTERVAL = 300;
    
    private final int MAX_VALUES = 100;
    
    // Generate the graph
        JFreeChart chart = ChartFactory.createXYLineChart(
        "BeMap Realtime Sensor Values", // Title
        "#", // x-axis Label
        "°C – % – ppm ", // y-axis Label
        dataset, // Dataset
        PlotOrientation.VERTICAL, // Plot Orientation
        true, // Show Legend
        true, // Use tooltips
        false // Configure chart to generate URLs?
        );
    // Generate the graph
        JFreeChart accChart = ChartFactory.createXYLineChart(
        "BeMap Accelerometer  Values", // Title
        "#", // x-axis Label
        "G", // y-axis Label
        accDataset, // Dataset
        PlotOrientation.VERTICAL, // Plot Orientation
        true, // Show Legend
        true, // Use tooltips
        false // Configure chart to generate URLs?
        );
        
    ChartPanel chartPanel = new ChartPanel(chart);
    ChartPanel accChartPanel = new ChartPanel(accChart);
    fakeThermometer fakeThermometer = new fakeThermometer();
    Timer timer = new Timer();
    
    /**
     * Creates new form realTimeWindow
     */
    public realTimeWindow() {
        initComponents();
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(realTimeWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(realTimeWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(realTimeWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(realTimeWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        // Add the series to your data set
        dataset.addSeries(temp);
        dataset.addSeries(hum);
        dataset.addSeries(gaz1);
        dataset.addSeries(gaz2);
        
        accDataset.addSeries(ax);
        accDataset.addSeries(ay);
        accDataset.addSeries(az);
        
        graphPanel.setLayout(new java.awt.BorderLayout());
        graphPanel.add(chartPanel,BorderLayout.CENTER);
        graphPanel.validate();
        graphPanel.setSize(600,400);
        
        accPanel.setLayout(new java.awt.BorderLayout());
        accPanel.add(accChartPanel,BorderLayout.CENTER);
        accPanel.validate();
        accPanel.setSize(300,300);
        
        timer.schedule(new GetNewValues(), 0, INTERVAL);
        
    }
    
    public void addToGraph(double tempval, double humval, int gazval1, int gazval2, double vax, double vay, double vaz){
        temp.add(graphIndex,tempval);
        hum.add(graphIndex,humval);
        gaz1.add(graphIndex,gazval1);
        gaz2.add(graphIndex,gazval2);
        ax.add(graphIndex,vax);
        ay.add(graphIndex,vay);
        az.add(graphIndex,vaz);
        chartPanel.repaint();
        accChartPanel.repaint();
        graphIndex++;
        
    }
    
    
   class GetNewValues extends TimerTask {
    public void run() {
        try {
            //addToGraph(fakeThermometer.getNext());
            JSONObject realTimeValues = BeMapEditor.serviceRoutine.serial.getRealTimeData();
            BeMapEditor.mainWindow.append(realTimeValues.toString());
            if(realTimeValues.getInt("err")==0){
            addToGraph(realTimeValues.getDouble("temp"),realTimeValues.getDouble("hum"),
                    realTimeValues.getInt("gaz1")-150,realTimeValues.getInt("gaz2")-150,
                    realTimeValues.getDouble("ax"),realTimeValues.getDouble("ay"),realTimeValues.getDouble("az"));
            }
            } catch (InterruptedException ex) {
            Logger.getLogger(realTimeWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(realTimeWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
 }
    
  

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        graphPanel = new javax.swing.JPanel();
        accPanel = new javax.swing.JPanel();

        jTextField1.setText("jTextField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        graphPanel.setPreferredSize(new java.awt.Dimension(600, 400));

        javax.swing.GroupLayout graphPanelLayout = new javax.swing.GroupLayout(graphPanel);
        graphPanel.setLayout(graphPanelLayout);
        graphPanelLayout.setHorizontalGroup(
            graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );
        graphPanelLayout.setVerticalGroup(
            graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 408, Short.MAX_VALUE)
        );

        accPanel.setPreferredSize(new java.awt.Dimension(300, 300));
        accPanel.setRequestFocusEnabled(false);

        javax.swing.GroupLayout accPanelLayout = new javax.swing.GroupLayout(accPanel);
        accPanel.setLayout(accPanelLayout);
        accPanelLayout.setHorizontalGroup(
            accPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 457, Short.MAX_VALUE)
        );
        accPanelLayout.setVerticalGroup(
            accPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(graphPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(accPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(graphPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 408, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(accPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 12, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel accPanel;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}

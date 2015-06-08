/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcan2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Micha
 */
public class TrackOrganiser extends javax.swing.JFrame {

    
    private static final boolean TRACK_DEBUG = true;
    public static final int GLOBAL = 0;
    public static final int PUBLIC = 1;
    private final ArrayList<Data> trackList = new ArrayList<>(); //contains all the tracks
    private Data currentTrack;
    private int idCounter = 0;
    /**
     * Creates new form TrackOrganiser
     */
    public TrackOrganiser() {
        initComponents();
        //getContentPane().setBackground( Color.blue );
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(trackInfoPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(trackInfoPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(trackInfoPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(trackInfoPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
    
    public void createNewTrack(String name){
        currentTrack = new Data(idCounter,name); //create first data layer remove
        idCounter++;
        trackList.add(currentTrack);
        trackChooser.addItem(getDisplayName(currentTrack));
        updateTrackChooser();
    }
    
    public void createNewTrack(String name, JSONArray gpsData) throws JSONException{
        currentTrack = new Data(idCounter,name); //create first data layer remove
        idCounter++;
        trackList.add(currentTrack);
        trackChooser.addItem(getDisplayName(currentTrack));
        currentTrack.importJSONList(gpsData);
        
        updateTrackChooser();
    }
    
    public void deleteOnServerFlag(){
        trackList.get(PUBLIC).removeSendToServerFlag();
    }
    
    public void addPublicPoints(JSONArray gpsData) throws JSONException{
        trackList.get(PUBLIC).importJSONList(gpsData);
    }
    
    public void replaceGlobalData(JSONArray gpsData) throws JSONException{
        trackList.get(GLOBAL).deleteAllPoints();
        trackList.get(GLOBAL).importJSONList(gpsData);
    }
    
    private String getDisplayName(Data d){
        if(d.getID()>1){
        return ((d.getID()-1) + " - " + d.getLayerName());
        }
        //no numbers for global tracks
        else return "-- " + d.getLayerName() + " --";
    }
    
    
    
    public void setCurrentTrack(Data currentTrack){
        this.currentTrack = currentTrack;
        BeMapEditor.mainWindow.setTrackField(currentTrack.getLayerName());
        updateTrackChooser();
    
    }
    
    public void setGlobal(){
        setCurrentTrack(trackList.get(GLOBAL));
    }

    /**
     * Returns the current Data layer (sometimes called track) currently treated
     * by MainWindow.
     * @return Data layer
     */
    public Data getData(){
        return currentTrack;
        //int track = trackChooser.getSelectedIndex();
        //return trackList.get(track);
    }
    
    /**
     * Updates the track chooser combobox, has to be called every time a data layer
     * (sometimes called track) is added or removed.
     */
    private void updateTrackChooser(){
        int index = trackChooser.getSelectedIndex();
        trackChooser.removeAllItems();
        mergeComboBox.removeAllItems();
        //add them again with the correct names.
        Iterator<Data> it = trackList.iterator();
        while(it.hasNext())
        {
            Data d = it.next();
            trackChooser.addItem(getDisplayName(d));
            mergeComboBox.addItem(getDisplayName(d));
        }
        //set the correct track layer on the chooser
        trackChooser.setSelectedIndex(index);
        trackNameField.setText(currentTrack.getLayerName());
    }
    
    public void clearTrackList(){
        trackList.clear();
    }
    
    
    public void deleteCurrentTrack(){
        if(currentTrack.getID()==0||currentTrack.getID()==1){
            BeMapEditor.mainWindow.append("\nError: Public tracks cannot be deleted!");
        }
        else{
            trackList.remove(trackChooser.getSelectedIndex());
            updateTrackChooser();
            BeMapEditor.mainWindow.updateMap();
        }
    }
    
    public void renameTrack(){
        if(currentTrack.getID()==0||currentTrack.getID()==1){
            BeMapEditor.mainWindow.append("\nError: The public tracks cannot be deleted!");
        }
        else{
            String newName = trackNameField.getText();
            currentTrack.setLayerName(newName);
            trackNameField.setText(newName);
            updateTrackChooser();
        }
    }
    
    /**
     * Prepares a String containing all the data.
     * @return JSONString
     * @throws JSONException 
     */
    public JSONObject prepareJSONtoExport() throws JSONException{

        //this will be the object that contains all the data layers.
        JSONObject trackArr = new JSONObject();
        
        //loop through all data layers and add them into a JSONArray
        Iterator<Data> it = trackList.iterator();
        while(it.hasNext())
        {
            if(TRACK_DEBUG) BeMapEditor.mainWindow.append("\nNew Layer");
            Data d = it.next();
            //don't export the global layer
            if(d.getID()!=0){
                trackArr.put(d.getLayerName(),d.exportJSONList());
            }
        }
        
        JSONObject usrObj = new JSONObject();
        usrObj.put(BeMapEditor.mainWindow.getUsrString(), trackArr);
        if(TRACK_DEBUG) BeMapEditor.mainWindow.append(usrObj.toString());
        return usrObj;
    }
    
    /**
     * Prepares a String containing all the data, for export to server (only 
     * data from the public layer)
     * @return JSONString
     * @throws JSONException 
     */
    public JSONObject JSONExportPublic() throws JSONException{

        //this will be the object that contains all the data layers.
        JSONObject trackArr = new JSONObject();

        Data layer = trackList.get(PUBLIC);
        trackArr.put(layer.getLayerName(),layer.exportJSONList());
        
        JSONObject usrObj = new JSONObject();
        usrObj.put(BeMapEditor.mainWindow.getUsrString(), trackArr);
        if(TRACK_DEBUG) BeMapEditor.mainWindow.append(usrObj.toString());
        return usrObj;
    }
    
    private void duplicateTrack() throws JSONException{
        JSONArray currentArray = currentTrack.exportJSONList();
        createNewTrack(currentTrack.getLayerName()+" Copy",currentArray);
    }
    
    private void mergeTracks() throws JSONException{
        Data track1 = trackList.get(mergeComboBox.getSelectedIndex());
        if(track1 == currentTrack){
            BeMapEditor.mainWindow.append("\nError: Same tracks cannot be merged");
        }
        else if(track1.getID()==0||currentTrack.getID()==0){
            BeMapEditor.mainWindow.append("\nError: You cannot merge the public tracks");
        }
        else if(track1.getID()==1){
            //just copy the points to public points
            track1.importJSONList(currentTrack.exportJSONList());
            updateTrackChooser();
            BeMapEditor.mainWindow.updateMap();
            
        }
        else if(currentTrack.getID()==1){
            currentTrack.importJSONList(track1.exportJSONList());
            updateTrackChooser();
            BeMapEditor.mainWindow.updateMap();
        }
        else{
        //merge this track to the current track
        currentTrack.importJSONList(track1.exportJSONList());
        currentTrack.setLayerName(currentTrack.getLayerName()+" / "+track1.getLayerName());
        trackList.remove(mergeComboBox.getSelectedIndex());
        updateTrackChooser();
        BeMapEditor.mainWindow.updateMap();
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

        jLabel1 = new javax.swing.JLabel();
        trackChooser = new javax.swing.JComboBox();
        newTrackButton = new javax.swing.JButton();
        trackNameField = new javax.swing.JTextField();
        renameTrackButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        mergeComboBox = new javax.swing.JComboBox();
        mergeButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("BeMap Track Organiser");

        jLabel1.setText("Organise your cycling tracks");

        trackChooser.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                trackChooserMouseReleased(evt);
            }
        });
        trackChooser.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                trackChooserPopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
        });
        trackChooser.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                trackChooserComponentShown(evt);
            }
        });

        newTrackButton.setText("new");
        newTrackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newTrackButtonActionPerformed(evt);
            }
        });

        trackNameField.setText("Track");

        renameTrackButton.setText("rename");
        renameTrackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameTrackButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        mergeButton.setText("merge");
        mergeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mergeButtonActionPerformed(evt);
            }
        });

        closeButton.setText("done");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        jButton1.setText("copy");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 2, 10)); // NOI18N
        jLabel2.setText("Hint: merge to \"my public points\" to share with the community!");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(131, 131, 131)
                .addComponent(closeButton)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mergeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(mergeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 19, 19))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(54, 54, 54)
                                .addComponent(jLabel1))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(trackNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(trackChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jButton1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(newTrackButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(renameTrackButton)))))
                        .addGap(0, 22, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(trackChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton1))
                    .addComponent(deleteButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newTrackButton)
                    .addComponent(trackNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(renameTrackButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mergeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mergeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                .addComponent(closeButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        deleteCurrentTrack();
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void trackChooserMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_trackChooserMouseReleased
        
    }//GEN-LAST:event_trackChooserMouseReleased

    private void trackChooserComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_trackChooserComponentShown
       
    }//GEN-LAST:event_trackChooserComponentShown

    private void trackChooserPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_trackChooserPopupMenuWillBecomeInvisible
        setCurrentTrack(trackList.get(trackChooser.getSelectedIndex()));
        BeMapEditor.mainWindow.updateMap();
    }//GEN-LAST:event_trackChooserPopupMenuWillBecomeInvisible

    private void renameTrackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameTrackButtonActionPerformed
        renameTrack();
    }//GEN-LAST:event_renameTrackButtonActionPerformed

    private void newTrackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newTrackButtonActionPerformed
        createNewTrack(trackNameField.getText());
    }//GEN-LAST:event_newTrackButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            duplicateTrack();
        } catch (JSONException ex) {
            Logger.getLogger(TrackOrganiser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void mergeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mergeButtonActionPerformed
        try {
            mergeTracks();
        } catch (JSONException ex) {
            Logger.getLogger(TrackOrganiser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_mergeButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton mergeButton;
    private javax.swing.JComboBox mergeComboBox;
    private javax.swing.JButton newTrackButton;
    private javax.swing.JButton renameTrackButton;
    private javax.swing.JComboBox trackChooser;
    private javax.swing.JTextField trackNameField;
    // End of variables declaration//GEN-END:variables
}

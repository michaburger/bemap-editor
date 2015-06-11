
package jcan2;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

/**
 * This class is responsible for the GUI (Graphical User Interface) and contains 
 * a lot of functions related to the function of the buttons, sliders etc...
 * @author Micha Burger
 */
public class MainWindow extends javax.swing.JFrame {

    ServerCommunication http = new ServerCommunication();
    private final boolean WINDOW_DEBUG = false;
    private final String DEFAULT_FILENAME = "data";
    private final String DEFAULT_EXTENSION = ".bemap";
    private final String DEFAULT_FILENAME_COMPLETE = DEFAULT_FILENAME + DEFAULT_EXTENSION;
    private final String ERROR_MSG_NO_ID_FOUND = "Error: No user ID found. Please connect your device to"
                    + "import points or import existing data from a .bemap"
                    + "file.\n";
    private final String ERROR_MSG_FILE_EXISTS = "Error: File already exists. Please choose another name.\n";
    String portName = "";
    private long usr = -1;
    private boolean dragged = false;
    private SelectField selectRectangle = new SelectField();
    private int timeSpread = 0;
    private final int TYPE_GREY = -1;
    private Coordinate lastClicked = new Coordinate(0,0);
    private final String PUBLIC_DATA = "My public points";
    

    /**
     * Constructor of MainWindow.
     */
    public MainWindow() {
        initComponents();
        //create the first track to be pointed by currentTrack
        BeMapEditor.trackOrganiser = new TrackOrganiser();
        BeMapEditor.trackOrganiser.createNewTrack("Global Data");
        BeMapEditor.trackOrganiser.createNewTrack(PUBLIC_DATA);
        sensorSlider.setMaximum(5); 
        sensorSlider.setPaintTicks(true);
        selectPointsMenu.setSelected(true);
        
    }
    
    public String getPublicString(){
        return PUBLIC_DATA;
    }
    
    public void setTrackField(String track){
        trackField.setText(track);
    }
    
    private Data getTrack(){ return BeMapEditor.trackOrganiser.getData();}
    
    /**
     * Sets the maximum value of the memory state bar.
     * @param n maximum
     */
    public void setBarMax(int n){memoryBar.setMaximum(n);}
    /**
     * Sets the current value of the memory state bar.
     * @param n current value between 0 and the maximum previousely set.
     */
    public void setBar(int n){memoryBar.setValue(n);}
    /**
     * Changes the current Data layer (sometimes called track) showed and treated by MainWindow (there is 
     * always only ONE data layer treated by mainWindow at the time!
     * @param currentTrack Data layer (also called track sometimes)
     */
    
    public int getBarVal(){return memoryBar.getValue();}
    public int getBarMax(){return memoryBar.getMaximum();}
    
    /**
     * Getter for the user id
     * @return user id
     */
    public long getUsr(){
        return usr;
    }
    
    /**
     * Returns the difference in hours between the oldest and youngest point currently
     * shown on the map.
     * @return int in hours
     */
    public int getTimeSpread(){return timeSpread;}
    /**
     * Gets the current value of the time slider in yyMMddhh (slow because it uses
     * getOldestDate() from Data, mustn't be used in loops!)
     * @return current time filter value in yyMMddhh
     */
    public long getTimeSliderValue(){
        return (getTrack().getOldestDate() + timeSlider.getValue());
    }
    
    /**
     * Returns true when the time filter function is active
     * @return true when time filter function activated
     */
    public boolean timeSliderActive(){return activateTimeSliderBar.getState();}
    
    /**
     * Returns the number of the sensor, whose values have to be shown on the map
     * @return Sensor number from 1 to 4
     */
    public int getSensorNumber(){
        return sensorSlider.getValue();
    }
    
    /**
     * Sets the user ID, this function has necessarily to be called at program launch,
     * because data export isn't possible without user ID!
     * @param usr User ID
     */
    public void setUsr(long usr){
        this.usr = usr;
    }
    
    /**
     * Generates a String like "usr3"
     * @return String containing "usr" and the ID.
     */
    public String getUsrString(){
        return "usr" + Objects.toString(usr,null);
    }


    /**
     * Adds a String to the status area on the main Window.
     * @param output Text to add to the status area.
     */
    public void append(String output){
        statusArea.append(output);
    }


    /**
     * Map getter
     * @return Map on mainWindow
     */
    public JMapViewer getMap(){return map;}
    

    
    /**
     * Checks if a file already exists on the computer
     * @param path The entire filepath including the filename.
     * @return 
     */
    public boolean fileExists(String path){
        File f = new File(path);
        if(f.exists()) return true;
        else return false;
    }
    
    /**
     * Checks if the file DEFAULT_FILENAME_COMPLETE.bemap already exists in
     * the given path
     * @return 
     */
    public boolean fileExists(){
        File f = new File(DEFAULT_FILENAME_COMPLETE);
        if(f.exists()) return true;
        else return false;
    }
    
    
    
    /**
     * Returns the coordinates of the top left and bottom right corner of the map,
     * used for importing the right points from the server.
     * @return JSONString with 2 data objects
     * @throws JSONException 
     */
    private String getCorners() throws JSONException{
        int w = map.getWidth();
        int l = map.getHeight();
       
        //get the 4 corner points
        Coordinate c[] = new Coordinate[2];
        
        c[0] = map.getPosition(0,0);
        c[1] = map.getPosition(w,l);
        
        JSONArray coordinates = new JSONArray();
        
        //make a JSONObject to export
        for(int i=0; i<2; i++){
            JSONObject point = new JSONObject();
            point.put("lon",c[i].getLon());
            point.put("lat",c[i].getLat());
            coordinates.put(point);
        }
        JSONObject corners = new JSONObject();
        corners.put("corners", coordinates);
        
        return corners.toString();
    }
    
    private void importMultipleLayersWithoutUser(String jsonString) throws JSONException{
        BeMapEditor.trackOrganiser.clearTrackList();
        
        //get the tracks of the first user.
        JSONObject tracks = new JSONObject(jsonString);
        String[] trackNames = JSONObject.getNames(tracks);
        
        //for every data layer, import points
        for(int i=0; i<trackNames.length; i++) {
          //get the array for the track i.
          JSONArray gpsData = new JSONArray(tracks.getJSONArray(trackNames[i]).toString());
          
          //do not create a new layer for the public points, but add them to the existing
          if(PUBLIC_DATA.equals(trackNames[i])){
              BeMapEditor.trackOrganiser.addPublicPoints(gpsData);
          }
          else{
            //for every data layer, create a layer with the correct name and store the points in it.
            BeMapEditor.trackOrganiser.createNewTrack(trackNames[i],gpsData);
          }
        }
    }
    
    /**
     * Imports a String containing a JSONObject with multiple tracks
     * @param jsonString JSONObjects containing multiple tracks
     * @throws JSONException 
     */
    private void importMultipleLayers(String jsonString) throws JSONException{
        
        JSONObject imported = new JSONObject(jsonString);
        String[] usrNames = JSONObject.getNames(imported);
        
        //import user ID
        String usrArray[] = usrNames[0].split("usr");
        usr = Long.parseLong(usrArray[1],10); //parse decimal value
        //only import data of the first user.
        if(usrNames.length>1) BeMapEditor.mainWindow.append("Warning: invalid file type (multiple users). Only the first user has been imported.\n");
        
        //get the tracks of the first user.
        JSONObject tracks = imported.getJSONObject(usrNames[0]);
        String[] trackNames = JSONObject.getNames(tracks);
        
        //for every data layer, import points
        for(int i=0; i<trackNames.length; i++) {
          //get the array for the track i.
          JSONArray gpsData = new JSONArray(tracks.getJSONArray(trackNames[i]).toString());
          
          //do not create a new layer for the public points, but add them to the existing
          if(PUBLIC_DATA.equals(trackNames[i])){
              BeMapEditor.trackOrganiser.addPublicPoints(gpsData);
          }
          else{
            //for every data layer, create a layer with the correct name and store the points in it.
            BeMapEditor.trackOrganiser.createNewTrack(trackNames[i],gpsData);
          }
          
        }
    }
 
/**
 * Initialize the time slider (get the oldest and newest point of the track and 
 * update the slider).
 */
 private void initTimeSlider(){
     long oldest = getTrack().getOldestDate();
     long youngest = getTrack().getYoungestDate();
     int maximum = (int) (youngest - oldest);
     timeSpread = maximum;
     timeSlider.setMinimum(0);
     timeSlider.setMaximum(maximum);
 }
 /**
  * Gets the time in the format yyMMddhh and puts it to the time field in the
  * format dd.MM.20yy hh:00.
  */
 private void adaptTimeField() {
     long time = getTimeSliderValue();
     int year = (int) time / 1000000;
     int month = (int) time / 10000 - year * 100;
     int day = (int) time / 100 - year * 10000 - month * 100;
     int hour = (int) time - year * 1000000 - month * 10000 - day * 100;
     String yearS = "20" + String.format("%02d", year);
     String monthS = String.format("%02d", month);
     String dayS = String.format("%02d", day);
     String hourS = String.format("%02d", hour)+":00";
     timeField.setText(dayS+"."+monthS+"."+yearS+" "+hourS);
    }

 /**
  * Opens a JFileChooser and the user can choose the file he wants to import,
 *  the file is only imported when a valid path has been chosen.
  */
    private void openFileChooserToImportFile(){
        JFileChooser chooser = new JFileChooser();
        
        //only show .bemap files
        FileNameExtensionFilter filter = new FileNameExtensionFilter("BEMAP file", "bemap");
        chooser.setFileFilter(filter);
        
        chooser.setDialogTitle("Choose a file with readable text and .bemap extension");
        if (chooser.showOpenDialog(null) == JFileChooser.CANCEL_OPTION)
            statusArea.append("Error: File choosing aborted\n");
        File file = new File(chooser.getCurrentDirectory(), chooser.getSelectedFile().getName());

        if(WINDOW_DEBUG) append(file.toString());
        //import from selected file
        importFromFile(file.toString());

    }
    
    /**
     * Get a file path with a file Chooser
     * @return file path
     */
    private String fileChooserGetPath(){
        String path = "";
        
        JFileChooser chooser = new JFileChooser();
        if(WINDOW_DEBUG) append("File chooser created\n");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(WINDOW_DEBUG) append("Directories only set\n");
        
        chooser.setDialogTitle("Choose path for exporting your file");
        if (chooser.showOpenDialog(null) == JFileChooser.CANCEL_OPTION)
            statusArea.append("Error: Path choosing aborted\n");
        path = chooser.getSelectedFile().getAbsolutePath().toString() + "/";
        if(WINDOW_DEBUG) append("path chosen: " + path);
        
        return path;
    
    }
    
    
    
    
    /**
     * Exports all the current data (including all the tracks) to a file.
     * @param pathAndName Filepath including filename.
     */
    private void exportToFile(String pathAndName){
        PrintWriter writer= null;
        
        try {
            writer = new PrintWriter(pathAndName, "UTF-8");
            writer.print(BeMapEditor.trackOrganiser.prepareJSONtoExport());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            statusArea.append("Error: Unsupported Encoding Exception\n");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            statusArea.append("Error: File not found\n");
        } catch (JSONException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        statusArea.append("Output file "+pathAndName+" exported\n");
        writer.close();
    }
    
    public void exportToFilePublic(String pathAndName){
        PrintWriter writer= null;
        
        try {
            writer = new PrintWriter(pathAndName, "UTF-8");
            writer.print(BeMapEditor.trackOrganiser.JSONExportPublic());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            statusArea.append("Error: Unsupported Encoding Exception\n");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            statusArea.append("Error: File not found\n");
        } catch (JSONException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        statusArea.append("Output file "+pathAndName+" exported\n");
        writer.close();
    }

    /**
     * Import a file and overwrite all the currently stored data.
     * @param filename Filepath including filename.
     */
    private void importFromFile(String filename){
        String jsonData = "";
        String line = "";
        
        try {
            InputStream inputStream = new FileInputStream(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputStreamReader);
            
            while ((line = br.readLine()) != null){
                jsonData += line + "\n";
            }
            
            if(WINDOW_DEBUG) BeMapEditor.mainWindow.append(jsonData);
            importMultipleLayers(jsonData);
            append("File "+filename + " successfully imported\n");
            
            
        } catch (FileNotFoundException ex) {
            append("No file found to import\n");
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    /**
     * Imports a file from the default filepath.
     */
    public void importFromFile(){
        String jsonData = "";
        String line = "";
        
        try {
            InputStream inputStream = new FileInputStream(DEFAULT_FILENAME_COMPLETE);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputStreamReader);
            
            while ((line = br.readLine()) != null){
                jsonData += line + "\n";
            }
            
            if(WINDOW_DEBUG) BeMapEditor.mainWindow.append(jsonData);
            importMultipleLayers(jsonData);
            map.setDisplayToFitMapMarkers();
            append("File "+DEFAULT_FILENAME_COMPLETE + " successfully imported\n");
            
            
        } catch (FileNotFoundException ex) {
            append("No file found to import\n");
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Saves all the current data to the default file.
     */
    private void save(){
        if(usr == -1){
            //no user has been defined
            append(ERROR_MSG_NO_ID_FOUND);
        }
        else{
          exportToFile("data.bemap"); //export data.bemap to the same path
          append("Data successfully saved\n");
        }
    }
    
    /**
     * Exports all the current data to a new file chosen with a fileChooser.
     */
    private void saveAs(){
         if(WINDOW_DEBUG) append("Export button pressed\nUser ID = "+usr+"\n");
       //get a filename in the chosen directory that is not yet used, beginning
       //with data
       if(usr == -1){
            //no user has been defined
            append(ERROR_MSG_NO_ID_FOUND);
       }
       else{ 
        String path = fileChooserGetPath();
        if(!fileExists(path + BeMapEditor.settings.getExportName()+DEFAULT_EXTENSION)){
            exportToFile(path + BeMapEditor.settings.getExportName()+DEFAULT_EXTENSION);
        }
        else append(ERROR_MSG_FILE_EXISTS);
       }
    }
    
    
    /**
     * Imports all the points stored on the server (only in the current map section).
     */
    public void importFromServer(){
        String json = "";
        String globalPoints = "";
        try {
            json = getCorners();
            if(WINDOW_DEBUG) append("Corner points sent: "+ json + "\n");
        } catch (JSONException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        //export the JSON to server
        try {
            
            if(!"".equals(json)) globalPoints = http.getFromBeMapServer(json);
            else append("Corner points error\n");
        } catch (Exception ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(!"".equals(globalPoints)){
            //make a new layer for the import
            
            try {
                //add data to the import layer
                JSONObject imported = new JSONObject(globalPoints);
                String[] track = JSONObject.getNames(imported);
                JSONArray points = imported.getJSONArray(track[0]);
                if(WINDOW_DEBUG) append("track[0]: "+ points.toString());
                BeMapEditor.trackOrganiser.replaceGlobalData(points);
                BeMapEditor.trackOrganiser.setGlobal();
                updateMap(); //update points
                append("\n Server import successful");
                
            } catch (JSONException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else append("Server error, please check your internet connection!\n");
    }
    
    private void deletePoints(){
        if(selectRectangle.isActive()){
            int number = getTrack().deleteSelected(selectRectangle.upperLeft(),selectRectangle.lowerRight());
            selectRectangle.disable();
            append("\n"+number+" Points successfully deleted");
            updateMap();
        }
        else append("\nYou have to select points to delete!");
    }
    
    public void exportToServer(){
        try {
            exportToFilePublic("export.json");
            int response = http.sendJSONFileToBeMapServer("export.json");
            if(response == 200){
                BeMapEditor.trackOrganiser.deleteOnServerFlag();
                System.out.println("Server flags deleted");
            }
        } catch (Exception ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Gets the JSON file about the corresponding road segment (last point clicked) 
     * from the google servers and show it in the status area.
     */
    private void getRoadSegment(){
        double lat = lastClicked.getLat();
        double lon = lastClicked.getLon();
        try {
            OsmAPI api = new OsmAPI();
            api.getOSMSegments(lat, lon);
        } catch (Exception ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    public void updateMap(){
        getTrack().removeMapMarkers();
        getTrack().drawAllPoints();
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem2 = new javax.swing.JCheckBoxMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        map = new org.openstreetmap.gui.jmapviewer.JMapViewer();
        jScrollPane1 = new javax.swing.JScrollPane();
        statusArea = new javax.swing.JTextArea();
        jLabel8 = new javax.swing.JLabel();
        memoryBar = new javax.swing.JProgressBar();
        jLabel9 = new javax.swing.JLabel();
        saveButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        sensorSlider = new javax.swing.JSlider();
        timeSlider = new javax.swing.JSlider();
        jLabel6 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        timeField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        trackField = new javax.swing.JTextField();
        manageTracksButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        beMapMenu = new javax.swing.JMenu();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        aboutBar = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        settingsMenu = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JPopupMenu.Separator();
        quitBar = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        fileMenu = new javax.swing.JMenu();
        openBar = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        saveBar = new javax.swing.JMenuItem();
        saveAsBar = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        importServerBar = new javax.swing.JMenuItem();
        exportServerBar = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        activateTimeSliderBar = new javax.swing.JCheckBoxMenuItem();
        jSeparator12 = new javax.swing.JPopupMenu.Separator();
        enableImportMenu = new javax.swing.JMenuItem();
        realTimeImportMenu = new javax.swing.JMenuItem();
        showTrackMenu = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        editMenu = new javax.swing.JMenu();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        OpenTrackOrganiserMenu = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JPopupMenu.Separator();
        deletePointsMenu = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        deleteDataDeviceBar = new javax.swing.JMenuItem();
        mapMenu = new javax.swing.JMenu();
        centerMapBar = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        drawPointsMenu = new javax.swing.JCheckBoxMenuItem();
        showInfoMenu = new javax.swing.JCheckBoxMenuItem();
        selectPointsMenu = new javax.swing.JCheckBoxMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        roadSegmentBar = new javax.swing.JMenuItem();
        renderMenu = new javax.swing.JMenuItem();
        showPointsBar = new javax.swing.JMenuItem();

        jMenu1.setText("jMenu1");

        jMenu2.setText("jMenu2");

        jMenuItem1.setText("jMenuItem1");

        jMenuItem2.setText("jMenuItem2");

        jMenuItem3.setText("jMenuItem3");

        jMenuItem4.setText("jMenuItem4");

        jMenuItem8.setText("jMenuItem8");

        jMenuItem7.setText("jMenuItem7");

        jMenu3.setText("jMenu3");

        jMenuItem9.setText("jMenuItem9");

        jMenuItem10.setText("jMenuItem10");

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("jCheckBoxMenuItem1");

        jCheckBoxMenuItem2.setSelected(true);
        jCheckBoxMenuItem2.setText("jCheckBoxMenuItem2");

        jMenuItem5.setText("jMenuItem5");

        jMenuItem6.setText("jMenuItem6");

        jMenuItem12.setText("jMenuItem12");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        map.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                mapMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mapMouseClicked(evt);
            }
        });
        map.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                mapMouseDragged(evt);
            }
        });

        statusArea.setEditable(false);
        statusArea.setColumns(20);
        statusArea.setLineWrap(true);
        statusArea.setRows(5);
        jScrollPane1.setViewportView(statusArea);

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/logo_pack/logo/small.png"))); // NOI18N

        jLabel9.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 0, 0));
        jLabel9.setText("Attention: Save your data before quitting! ");

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel2.setText("memory state:");

        sensorSlider.setMajorTickSpacing(1);
        sensorSlider.setMinimum(1);
        sensorSlider.setSnapToTicks(true);
        sensorSlider.setToolTipText("");
        sensorSlider.setValue(1);
        sensorSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                sensorSliderMouseReleased(evt);
            }
        });

        timeSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                timeSliderMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                timeSliderMouseReleased(evt);
            }
        });

        jLabel6.setText("Sensor slider");

        jLabel11.setText("Time slider hour:");

        timeField.setEditable(false);
        timeField.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N

        jLabel1.setText("track shown: ");

        trackField.setEditable(false);

        manageTracksButton.setText("manage tracks");
        manageTracksButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageTracksButtonActionPerformed(evt);
            }
        });

        beMapMenu.setText("beMap");
        beMapMenu.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        beMapMenu.add(jSeparator3);

        aboutBar.setText("About us");
        aboutBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutBarActionPerformed(evt);
            }
        });
        beMapMenu.add(aboutBar);
        beMapMenu.add(jSeparator1);

        settingsMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_COMMA, java.awt.event.InputEvent.CTRL_MASK));
        settingsMenu.setText("Settings");
        settingsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsMenuActionPerformed(evt);
            }
        });
        beMapMenu.add(settingsMenu);
        beMapMenu.add(jSeparator15);

        quitBar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        quitBar.setText("Quit");
        quitBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitBarActionPerformed(evt);
            }
        });
        beMapMenu.add(quitBar);
        beMapMenu.add(jSeparator2);

        jMenuBar1.add(beMapMenu);

        fileMenu.setText("File");

        openBar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openBar.setText("Open...");
        openBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openBarActionPerformed(evt);
            }
        });
        fileMenu.add(openBar);
        fileMenu.add(jSeparator5);

        saveBar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveBar.setText("Save");
        saveBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBarActionPerformed(evt);
            }
        });
        fileMenu.add(saveBar);

        saveAsBar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveAsBar.setText("Save as...");
        saveAsBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsBarActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsBar);
        fileMenu.add(jSeparator7);

        importServerBar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        importServerBar.setText("Import from Server");
        importServerBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importServerBarActionPerformed(evt);
            }
        });
        fileMenu.add(importServerBar);

        exportServerBar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        exportServerBar.setText("Export to Server");
        exportServerBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportServerBarActionPerformed(evt);
            }
        });
        fileMenu.add(exportServerBar);
        fileMenu.add(jSeparator8);

        activateTimeSliderBar.setText("activate Time Slider");
        fileMenu.add(activateTimeSliderBar);
        fileMenu.add(jSeparator12);

        enableImportMenu.setText("re-enable import");
        fileMenu.add(enableImportMenu);

        realTimeImportMenu.setText("realtime import");
        realTimeImportMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                realTimeImportMenuActionPerformed(evt);
            }
        });
        fileMenu.add(realTimeImportMenu);

        showTrackMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        showTrackMenu.setText("Show Track Information");
        showTrackMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showTrackMenuActionPerformed(evt);
            }
        });
        fileMenu.add(showTrackMenu);
        fileMenu.add(jSeparator6);

        jMenuBar1.add(fileMenu);

        editMenu.setText("Edit");
        editMenu.add(jSeparator9);

        OpenTrackOrganiserMenu.setText("Open Track Organiser");
        OpenTrackOrganiserMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenTrackOrganiserMenuActionPerformed(evt);
            }
        });
        editMenu.add(OpenTrackOrganiserMenu);
        editMenu.add(jSeparator14);

        deletePointsMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_BACK_SPACE, java.awt.event.InputEvent.CTRL_MASK));
        deletePointsMenu.setText("Delete selected Points");
        deletePointsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deletePointsMenuActionPerformed(evt);
            }
        });
        editMenu.add(deletePointsMenu);

        jMenuItem11.setText("Clear Status");
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        editMenu.add(jMenuItem11);
        editMenu.add(jSeparator11);

        deleteDataDeviceBar.setText("Delete Data on Device");
        deleteDataDeviceBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteDataDeviceBarActionPerformed(evt);
            }
        });
        editMenu.add(deleteDataDeviceBar);

        jMenuBar1.add(editMenu);

        mapMenu.setText("Map");

        centerMapBar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        centerMapBar.setText("Center on Points");
        centerMapBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                centerMapBarActionPerformed(evt);
            }
        });
        mapMenu.add(centerMapBar);
        mapMenu.add(jSeparator13);

        drawPointsMenu.setText("Draw Points");
        drawPointsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drawPointsMenuActionPerformed(evt);
            }
        });
        mapMenu.add(drawPointsMenu);

        showInfoMenu.setText("Show Info");
        showInfoMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showInfoMenuActionPerformed(evt);
            }
        });
        mapMenu.add(showInfoMenu);

        selectPointsMenu.setSelected(true);
        selectPointsMenu.setText("Select Points");
        selectPointsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectPointsMenuActionPerformed(evt);
            }
        });
        mapMenu.add(selectPointsMenu);
        mapMenu.add(jSeparator10);

        roadSegmentBar.setText("Get Road Segment");
        roadSegmentBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                roadSegmentBarActionPerformed(evt);
            }
        });
        mapMenu.add(roadSegmentBar);

        renderMenu.setText("Render Model");
        renderMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renderMenuActionPerformed(evt);
            }
        });
        mapMenu.add(renderMenu);

        showPointsBar.setText("Show Points (Debug)");
        showPointsBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPointsBarActionPerformed(evt);
            }
        });
        mapMenu.add(showPointsBar);

        jMenuBar1.add(mapMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(trackField, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(manageTracksButton)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(saveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9)
                        .addGap(138, 138, 138)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sensorSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17)
                        .addComponent(timeSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(memoryBar, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(timeField, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(1, 1, 1)))
                        .addContainerGap())
                    .addComponent(map, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(trackField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(manageTracksButton))
                        .addGap(11, 11, 11)))
                .addComponent(map, javax.swing.GroupLayout.PREFERRED_SIZE, 437, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sensorSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(timeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(saveButton)
                            .addComponent(jLabel9)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(memoryBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(7, 7, 7)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Defines the actions when the map is clicked
     * @param evt Java Event to retreive the screen coordinates of the clicked point
     */
    private void mapMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mapMouseClicked
        if(evt.getClickCount() == 1 && evt.getButton() == MouseEvent.BUTTON1){

         Point p = evt.getPoint();
         //transform the clicked point from screen coordinates to GPS coordinates
         Coordinate c = map.getPosition(p);
         
         //adapt the last clicked coordinate
         lastClicked = c;

         //if the drawPoints checkbox is selected
         if (drawPointsMenu.getState()){
             //store and draw the point
             getTrack().drawPoint(c.getLat(),c.getLon(),TYPE_GREY,TYPE_GREY,BeMapEditor.settings.NORMAL);
             getTrack().storePoint(c.getLat(),c.getLon(),TYPE_GREY);
         }
         if (showInfoMenu.getState()){
             String nearestPoint;
             try {
                 nearestPoint = getTrack().getNearestPoint(p);
                 if("".equals(nearestPoint)) append("No point found in surrounding area\n");
             else{
                 //Show point informations
                 JSONObject pointInfo = new JSONObject(nearestPoint);
                 append("**************************\n");
                 append("Latitude: " + pointInfo.getDouble("lat")+ "\n");
                 append("Longitude: " + pointInfo.getDouble("lon")+ "\n");
                 long date = pointInfo.getLong("date");
                 int day = (int) date / 10000;
                 int month = (int) date /100 - day * 100;
                 int year = 2000 + (int) date - day * 10000 - month * 100;
                 append("Date: "+day+"."+month+"."+year);
                 long time = pointInfo.getLong("time");
                 //17252300
                 int hour = (int) time / 1000000;
                 int min = (int) time / 10000 - hour * 100;
                 int sec = (int) time / 100 - hour * 10000 - min * 100;
                 int hdr = (int) time - hour * 1000000 - min * 10000 - sec * 100;
                 //format correctly
                 String minS = String.format("%02d",min);
                 String secS = String.format("%02d",sec);
                 String hdrS = String.format("%02d",hdr);
                 append(" Time: "+hour+":"+minS+":"+secS+"."+hdrS+"\n");
                 append("CO: "+ pointInfo.getInt("s1"));
                 append(" NO2: "+ pointInfo.getInt("s2"));
                 append(" Humidity: "+ pointInfo.getInt("s3"));
                 append(" Temperature: "+ pointInfo.getInt("s4"));
                 append(" Road vibrations: "+ pointInfo.getInt("s5")+"\n");
             }
             } catch (JSONException ex) {
                 Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
    }

    //else zoom
    else if (evt.getClickCount() == 2 && evt.getButton() == MouseEvent.BUTTON1) {
        map.zoomIn(evt.getPoint());
    }
    }//GEN-LAST:event_mapMouseClicked

    
    
   
    
    private void aboutBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutBarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_aboutBarActionPerformed

    private void quitBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitBarActionPerformed
        System.exit(0);
    }//GEN-LAST:event_quitBarActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        save();
        exportToServer();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void openBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openBarActionPerformed
        openFileChooserToImportFile();
    }//GEN-LAST:event_openBarActionPerformed

    private void centerMapBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_centerMapBarActionPerformed
        map.setDisplayToFitMapMarkers();
    }//GEN-LAST:event_centerMapBarActionPerformed

    private void exportServerBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportServerBarActionPerformed
        exportToServer();
    }//GEN-LAST:event_exportServerBarActionPerformed

    private void saveBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBarActionPerformed
        save();
    }//GEN-LAST:event_saveBarActionPerformed

    private void saveAsBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsBarActionPerformed
        saveAs();
    }//GEN-LAST:event_saveAsBarActionPerformed

    private void importServerBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importServerBarActionPerformed
        importFromServer();
    }//GEN-LAST:event_importServerBarActionPerformed

    private void drawPointsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drawPointsMenuActionPerformed
        
    }//GEN-LAST:event_drawPointsMenuActionPerformed

    private void roadSegmentBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_roadSegmentBarActionPerformed
        getRoadSegment();
    }//GEN-LAST:event_roadSegmentBarActionPerformed

    private void showPointsBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPointsBarActionPerformed
        try {
            append(getTrack().exportJSONList().toString());
        } catch (JSONException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_showPointsBarActionPerformed

    private void showInfoMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showInfoMenuActionPerformed
       
    }//GEN-LAST:event_showInfoMenuActionPerformed

    private void sensorSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sensorSliderMouseReleased
        if(WINDOW_DEBUG) append(getSensorNumber() + "\n");
        updateMap();
    }//GEN-LAST:event_sensorSliderMouseReleased

    private void timeSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_timeSliderMouseReleased

        updateMap();
        adaptTimeField();
    }//GEN-LAST:event_timeSliderMouseReleased

    private void timeSliderMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_timeSliderMousePressed
        initTimeSlider();
    }//GEN-LAST:event_timeSliderMousePressed

    private void realTimeImportMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_realTimeImportMenuActionPerformed

        if(BeMapEditor.serviceRoutine.serial.deviceConnected()){
            BeMapEditor.realTimeWindow = new realTimeWindow();
            BeMapEditor.realTimeWindow.setVisible(true);
        }
        else append("Error: No device connected!\n");
        
    }//GEN-LAST:event_realTimeImportMenuActionPerformed

    private void showTrackMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showTrackMenuActionPerformed

        BeMapEditor.trackInfoPanel = new trackInfoPanel();
        BeMapEditor.trackInfoPanel.setVisible(true);
    }//GEN-LAST:event_showTrackMenuActionPerformed

    private void mapMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mapMouseDragged

        //only get the first point
        if(!dragged){
            selectRectangle.disable(); //disable old
            Point p = evt.getPoint();
            Coordinate c = map.getPosition(p);
            selectRectangle.setFirstCorner(c);
            dragged = true;
        }
        else{
            Point p = evt.getPoint();
            Coordinate c = map.getPosition(p);
            selectRectangle.setSecondCorner(c);
        }
        
        
        
    }//GEN-LAST:event_mapMouseDragged

    private void mapMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mapMouseReleased
        if(dragged){
            dragged = false;
            Point p = evt.getPoint();
            Coordinate c = map.getPosition(p);
            selectRectangle.setSecondCorner(c);
        }
    }//GEN-LAST:event_mapMouseReleased

    private void selectPointsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectPointsMenuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_selectPointsMenuActionPerformed

    private void deleteDataDeviceBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteDataDeviceBarActionPerformed
        if(BeMapEditor.serviceRoutine.serial.sendDeleteRequest()==1){
            append("\nMemory successfully deleted");
        }
        else append("\nError: Points not deleted. Did you connect a device?");
    }//GEN-LAST:event_deleteDataDeviceBarActionPerformed

    //clear status
    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        statusArea.setText("");
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void OpenTrackOrganiserMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenTrackOrganiserMenuActionPerformed

        BeMapEditor.trackOrganiser.setVisible(true);
    }//GEN-LAST:event_OpenTrackOrganiserMenuActionPerformed

    private void deletePointsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deletePointsMenuActionPerformed
        deletePoints();
    }//GEN-LAST:event_deletePointsMenuActionPerformed

    private void manageTracksButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageTracksButtonActionPerformed
        BeMapEditor.trackOrganiser.setVisible(true);
    }//GEN-LAST:event_manageTracksButtonActionPerformed

    private void settingsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsMenuActionPerformed
        BeMapEditor.settings.setVisible(true);
    }//GEN-LAST:event_settingsMenuActionPerformed

    private void renderMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renderMenuActionPerformed
        try {
            getTrack().createModel();
        } catch (Exception ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_renderMenuActionPerformed

    
    
    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem OpenTrackOrganiserMenu;
    private javax.swing.JMenuItem aboutBar;
    private javax.swing.JCheckBoxMenuItem activateTimeSliderBar;
    private javax.swing.JMenu beMapMenu;
    private javax.swing.JMenuItem centerMapBar;
    private javax.swing.JMenuItem deleteDataDeviceBar;
    private javax.swing.JMenuItem deletePointsMenu;
    private javax.swing.JCheckBoxMenuItem drawPointsMenu;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem enableImportMenu;
    private javax.swing.JMenuItem exportServerBar;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem importServerBar;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator14;
    private javax.swing.JPopupMenu.Separator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JButton manageTracksButton;
    private org.openstreetmap.gui.jmapviewer.JMapViewer map;
    private javax.swing.JMenu mapMenu;
    private javax.swing.JProgressBar memoryBar;
    private javax.swing.JMenuItem openBar;
    private javax.swing.JMenuItem quitBar;
    private javax.swing.JMenuItem realTimeImportMenu;
    private javax.swing.JMenuItem renderMenu;
    private javax.swing.JMenuItem roadSegmentBar;
    private javax.swing.JMenuItem saveAsBar;
    private javax.swing.JMenuItem saveBar;
    private javax.swing.JButton saveButton;
    private javax.swing.JCheckBoxMenuItem selectPointsMenu;
    private javax.swing.JSlider sensorSlider;
    private javax.swing.JMenuItem settingsMenu;
    private javax.swing.JCheckBoxMenuItem showInfoMenu;
    private javax.swing.JMenuItem showPointsBar;
    private javax.swing.JMenuItem showTrackMenu;
    private javax.swing.JTextArea statusArea;
    private javax.swing.JTextField timeField;
    private javax.swing.JSlider timeSlider;
    private javax.swing.JTextField trackField;
    // End of variables declaration//GEN-END:variables

    
}

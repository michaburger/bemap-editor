
package jcan2;



/**
 * Opens a window and imports the data in the .bemap file that
 * is in the same directory than the software.
 * @author Micha Burger
 * 
 */
public class BeMapEditor {

    public static int MAPMARKER_R = 5;
    public static MainWindow mainWindow;
    public static realTimeWindow realTimeWindow;
    public static trackInfoPanel trackInfoPanel;
    public static TrackOrganiser trackOrganiser;
    public static ServiceRoutine serviceRoutine;
    public static Settings settings;
    
    
    /**
 * Opens a window and imports the data in the .bemap file that
 * is in the same directory than the software.
 * @author Micha Burger
 */
    public static void main(String args[]) {
        
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                mainWindow = new MainWindow();
                mainWindow.setVisible(true);
                //import from existing file
                if(mainWindow.fileExists()){
                mainWindow.importFromFile();
                }
                else{
                    mainWindow.append("No data found. Please import an existing .bemap file or "
                    + "connect your device!\n");
                }
                serviceRoutine = new ServiceRoutine();
                settings = new Settings();
                BeMapEditor.trackOrganiser.setGlobal();
            }
        });
    }
    
}

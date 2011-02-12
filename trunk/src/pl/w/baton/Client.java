/*
 * Rafal Zaczynski (rafal.zaczynski@gmail.com)
 * 
 * Created on 09-02-2011
 */
package pl.w.baton;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;




/**
 * 
 * @author rafal.zaczynski@gmail.com
 */
public class Client extends JFrame {

    private static Client INSTANCE;
    private boolean showAlert = true;
    
    
    final static class AboutA extends AbstractAction {
        private int type;
        private AboutA( String label, int type ) {
            super( label );
            this.type = type;
        }
        public void actionPerformed( ActionEvent e ) {    
            AboutBox about = new AboutBox( INSTANCE, "Demo", type );
            Dimension size = new Dimension( 420, 300 );
            about.setSize(size);
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            about.setLocation( ( screen.width - about.getWidth() ) / 2,
                    ( screen.height - about.getHeight() ) / 2 );
            about.setVisible(true);
            
            if ( Client.INSTANCE.showAlert ) {
                JOptionPane.showMessageDialog( about, "Double click the main graphical component to start animation." );
                Client.INSTANCE.showAlert = false;
            }
        }
    };
    
    
    private static final void createAndShowGUI() throws Exception {
        Client client = INSTANCE = new Client();
        client.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        
        JButton demoB = new JButton( new AboutA( "3D Sphere bounce", 0 ) );
        client.getContentPane().add( demoB, BorderLayout.NORTH );

        demoB = new JButton( new AboutA( "3D Tunnel", 1 ) );
        client.getContentPane().add( demoB, BorderLayout.CENTER );

        demoB = new JButton( new AboutA( "Pipes / Text Float", 2 ) );
        client.getContentPane().add( demoB, BorderLayout.SOUTH );
        
        client.pack();
        
        final String lf = UIManager.getSystemLookAndFeelClassName(); 
        UIManager.setLookAndFeel( lf );
        SwingUtilities.updateComponentTreeUI( client ); 
        
        client.setVisible( true );
    }
    
    
    public static void main( String[] args ) throws Exception {
        createAndShowGUI();
    }    
    
}

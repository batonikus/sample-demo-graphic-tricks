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
    
    final static Action ACTION_ABOUT = new AbstractAction( "Call demo" ) {
        public void actionPerformed( ActionEvent e ) {    
            AboutBox about = new AboutBox( INSTANCE, "Demo", "baton" );
            Dimension size = new Dimension( 420, 300 );
            about.setSize(size);
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            about.setLocation( ( screen.width - about.getWidth() ) / 2,
                    ( screen.height - about.getHeight() ) / 2 );
            about.setVisible(true); 
        }
    };
    
    private static final void createAndShowGUI() throws Exception {
        Client client = INSTANCE = new Client();
        client.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        final JButton demoB = new JButton( ACTION_ABOUT );
        demoB.setSize( 100, 30 );
        client.getContentPane().add( demoB, BorderLayout.CENTER );
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

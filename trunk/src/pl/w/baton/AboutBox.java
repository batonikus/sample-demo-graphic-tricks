/*
 * Rafal Zaczynski (rafal.zaczynski@gmail.com)
 * 
 * Created on 2005-09-27
 */
package pl.w.baton;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import pl.w.baton.tricks.*;


/**
 * 
 * @author rafal.zaczynski@gmail.com
 */
public class AboutBox extends JDialog {

    private AnimatedComponent renderer;
    private String userInfo;
    private int type;
    
    
    
    
    protected AboutBox( Frame owner, String title, int type ) {
        super( owner, title );
        this.userInfo = "Double click the upper component to start anim";
        this.type = type;
        setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );

        buildGui();
    }

    public void dispose() {
        super.dispose();
    }
    
    
    private void buildGui() {

        switch ( this.type ) {
            case 0:
                renderer = new FacedSphere();
                setResizable( true );
                break;
            case 1:
                renderer = new Tunnel( 2, 4 );
                setResizable( true );
                break;
            default:
                renderer = new AboutC( 3, 2 );
                setResizable( false );
                break;
        }
        JComponent jRenderer = (JComponent)renderer;
        jRenderer.setDoubleBuffered( true );
        getRootPane().setWindowDecorationStyle( JRootPane.INFORMATION_DIALOG );
        JComponent pane = (JComponent)getContentPane();
        pane.add( jRenderer );
        
        // buttons panel
        JPanel btPanel = new JPanel( new BorderLayout() );
        JPanel mainBtPanel = new JPanel( new FlowLayout( FlowLayout.TRAILING ) );
        mainBtPanel.setBorder( BorderFactory.createEmptyBorder( 4, 4, 4, 4 ) );
        JButton ok = new JButton( new AbstractAction() {
            public void actionPerformed( ActionEvent ae ) {
                AboutBox.this.setVisible( false );
            }
        });
        ok.setText( "OK" );
        mainBtPanel.add( ok );

//        JPanel additionalBtPanel = new JPanel( new FlowLayout( FlowLayout.LEADING ) );
//        additionalBtPanel.setBorder( BorderFactory.createEmptyBorder( 4, 4, 4, 4 ) );
//        additionalBtPanel.add( new JButton( releaseNotesAction ) );
//        
//        btPanel.add( additionalBtPanel, BorderLayout.WEST );
        btPanel.add( mainBtPanel, BorderLayout.EAST );

        
        
        
        Insets insets = new Insets( 2, 2, 2, 2 );
        // labels panel
        JPanel lbPanel = new JPanel( new GridBagLayout() );
        lbPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
        lbPanel.add( new JLabel( "SDGT Demo" ),
                new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        insets, 0, 0 ) );
        lbPanel.add( new JLabel( "rafal.zaczynski@gmail.com" ),
                new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0,
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        insets, 0, 0 ) );
        JLabel version = new JLabel();
        version.setText( "Version 1.0" );
        lbPanel.add( version, new GridBagConstraints( 0, 2, 1, 1, 1.0, 1.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                insets, 0, 0 ) );
        lbPanel.add( new JLabel( "SDGT - Sample Demo Graphic Trick" ),
                new GridBagConstraints( 0, 3, 1, 1, 1.0, 1.0,
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        insets, 0, 0 ) );
        JLabel uI = new JLabel( userInfo );
        uI.setBorder( BorderFactory.createEmptyBorder( 0, 10, 0, 0 ) );
        lbPanel.add( uI,
                new GridBagConstraints( 0, 4, 1, 1, 1.0, 1.0,
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        insets, 0, 0 ) );

        
        
        JPanel combiner = new JPanel( new GridBagLayout() );
        combiner.add( lbPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                insets, 0, 0 ) );
        combiner.add( btPanel, new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                insets, 0, 0 ) );
        
        pane.add( combiner, BorderLayout.SOUTH );
    }
    
    
    public void setVisible( boolean v ) {
        super.setVisible( v );
        if ( v ) {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    renderer.start();
                }
            });
        } else {
            renderer.stop();
        }
    }
    
    
    
}

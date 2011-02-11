/*
 * Rafal Zaczynski (rafal.zaczynski@gmail.com)
 * 
 * Created on 2005-10-05
 */
package pl.w.baton.tricks;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;



/**
 * 
 * @author rafal.zaczynski@gmail.com
 */
public class Tunnel extends JPanel implements AnimatedComponent {

    private static final int SLEEP_TIMEOUT = 10;

    
    
    private static final int _P0 = 1 << 0;
    private static final int _P1 = 1 << 1;
    private static final int _P2 = 1 << 2;
    private static final int _P3 = 1 << 3;
    private static final int _P4 = 1 << 4;
    private static final int _P5 = 1 << 5;
    private static final int _P6 = 1 << 6;
    private static final int _P7 = 1 << 7;
    private static final int _P8 = 1 << 8;
    private static final int _P9 = 1 << 9;
    private static final int _PA = 1 << 10;
    private static final int _PB = 1 << 11;
    private static final int _PC = 1 << 12;
    private static final int _PD = 1 << 13;
    private static final int _PE = 1 << 14;
    private static final int _PF = 1 << 15;
    
    private int _LOGO_POINT_SIZE = 4;
    private int _LOGO_Y_RESOLUTION = 16;
    
    
    private static final int X_AXIS = 0;
    private static final int Y_AXIS = 1;
    private static final int Z_AXIS = 2;
    
    private static final int _RED = 0;
    private static final int _GREEN = 1;
    private static final int _BLUE = 2;
    
    // 
    private int width, height;
    private Color prevBg;
    private int POV = 1;
    
    private static double[] CAM = new double[] { 0.0, 0.0, 0.0 };
    
    
    private static final double[] _SINETABLE = new double[ 360 ];
    
    
    // local vars for logo reder
    // we have 36 columns
    private final int[] LOGO_COLS = new int[] {
            _PE | _PF,
            _PE,
            _PD,
            _PD,
            _PC | _P1,
            _PB | _PA | _P9 | _P1 | _P0,
            _P8 | _P7 | _P6 | _P5 | _P4 | _P3 | _P2 | _P1 | _P0,
            _P5 | _P4 | _P3 | _P2 | _P1 | _P0,
            _P1 | _P0,
            _P0 | _PA | _PB, //J+R
            _P2 | _P9 | _PA | _PB,
            _P1 | _P2 | _P5 | _P6 | _P7 | _P8 | _P9 | _PA,
            _P0 | _P1 | _P2 | _P3 | _P4 | _P5,
            _P0 | _P1 | _P3 | _P4 | _P5 | _P6,
            _P0 | _P1 | _P3 | _P5 | _P6 | _P7,
            _P0 | _P1 | _P2 | _P3 | _P6 | _P7,
            _P0 | _P1 | _P2 | _P7 | _P8,
            _P1 | _P8,
            _P7 | _P8 | _P9,
            _P5 | _P6 | _P7 | _P8 | _P9,
            _P4 | _P5 | _P6 | _P9,
            _P3 | _P4 | _P6 | _P8 | _P9,
            _P3 | _P4 | _P5 | _P8,
            _P8,
            _PA,
            _P2 | _P9,
            _P1 | _P2 | _P7 | _P8 | _P9,
            _P1 | _P2 | _P3 | _P4 | _P5 | _P6 | _P7,
            _P0 | _P1 | _P2 | _P3 | _P4 | _P5,
            _P2 | _P3 | _P4 | _P5 | _P6 | _P7 | _P8,
            _P5 | _P6 | _P7,
            _P4 | _P5,
            _P2 | _P3 | _P4 | _P5,
            _P1 | _P2 | _P3 | _P4 | _P5 | _P6 | _P7,
            _P1 | _P2 | _P3 | _P6 | _P7 | _P8 | _P9,
            _P8 | _P9
    };
    private int _LOGO_SCALE;
    
    
    // wave variables 
    private int _ALPHA_STEP = 1;

    private int rotationAxis = Y_AXIS;
    private int rotationAlpha = 0;
    private int[] colorComponents = new int[] { 1, 0, 0 };
    private int colorRotator = 1;
    
    private double[][] _ALL_POINTS;
    private double[][] _ALL_ANIM_POINTS;
    
    
    
    private double[][] _RINGS;
    private final int _TUNNEL_ALPHA = 20;
    private final int _TUNNEL_RADIUS = _LOGO_Y_RESOLUTION >> 1;
    private final int _TUNNEL_RING_SPACE = 2;
    private final double _TUNNEL_WAVE_ALPHA = _TUNNEL_RING_SPACE * 7.5;
    private final double _TUNNEL_JUMPER_STRENGTH = 4;
    private final double _TUNNEL_RING_SPACE_STEP = 0.1;
    
    private double ringOffset = 0;
    private double ringWaveOffset = 90;
    
    
    
    private boolean RUNNING = false;
    private Thread worker;
    
    private boolean initialized = false;
    
    
    public Tunnel( int scale, int pointSize ) {
        _LOGO_SCALE = scale;
        _LOGO_POINT_SIZE = pointSize;
        CAM[ Z_AXIS ] = -20.0 * scale;
        setOpaque( true );
        addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent me ) {
                if ( me.getButton() == MouseEvent.BUTTON1 ) {
                    if ( me.getClickCount() > 1 ) { // double click run/stop
                        if ( !RUNNING ) {
                            runPipes();
                        } else {
                            stop();
                        }
                    } else { // single click - change rotation axis
                        if ( RUNNING ) {
                            rotationAxis = ( ( rotationAxis + 1 ) % ( Z_AXIS + 1 ) );
                        }
                    }
                } else {
                    colorRotator = ( colorRotator + 1 ) % 8;
                    colorComponents[ _RED ] = ( colorRotator & ( 1 << _RED ) ) >> _RED;
                    colorComponents[ _GREEN ] = ( colorRotator & ( 1 << _GREEN ) ) >> _GREEN;
                    colorComponents[ _BLUE ] = ( colorRotator & ( 1 << _BLUE ) ) >> _BLUE;
                }
            }
        });
        addMouseWheelListener( new MouseWheelListener() {
            public void mouseWheelMoved( MouseWheelEvent e ) {
                double camZ = CAM[ Z_AXIS ] - ( e.getScrollAmount() * ( Math.signum( e.getWheelRotation() ) ) );
                if ( camZ > - ( LOGO_COLS.length / 2 ) ) {
                    camZ = - ( LOGO_COLS.length / 2 );
                }
                if ( camZ < - ( 3 * _LOGO_SCALE * LOGO_COLS.length ) ) {
                    camZ = - ( 3 * _LOGO_SCALE * LOGO_COLS.length );
                }
                CAM[ Z_AXIS ] = camZ;
            }
        });
        
        for ( int i=0; i<360; i++ ) {
            _SINETABLE[ i ] = Math.sin( ( i * Math.PI ) / 180 );
        }
        
        prepareAllPoints();
    }
    
    
    
    
    protected void init() {
        if ( initialized ) {
            return;
        }
        
        width = ( getWidth() >> 1 );
        height = ( getHeight() >> 1 );
        POV = Math.min( width, height );

        addComponentListener( new ComponentAdapter() {
            public void componentResized( ComponentEvent ce ) {
                updateSize( ce.getComponent() );
            }
            public void componentShown( ComponentEvent ce ) {
                updateSize( ce.getComponent() );
            }
            private void updateSize( Component c ) {
                width = ( c.getWidth() >> 1 );
                height = ( c.getHeight() >> 1 );
                POV = Math.min( width, height );
                repaint();
            }
        } );
        
        initialized = true;
        
        repaint();
    }    
    
    
    
    
    
    
    
    
    private void update() {
        updateMovement();
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                repaint();
            }
        });
    }
    


    
    private final void updateMovement() {
        
        int iMax = _ALL_POINTS.length;
        for ( int i=0; i<iMax; i++ ) {
            _rotate( rotationAxis, _ALPHA_STEP, _ALL_ANIM_POINTS[ i ] );
        }
        
        // tunnel
        ringOffset += _TUNNEL_RING_SPACE_STEP;
        if  ( ringOffset >= _TUNNEL_RING_SPACE ) {
            ringOffset = 0;
            ringWaveOffset++;
        }
    }
    
    
    
    
    
    
    
    
    private double _sin( double alpha ) {
        return _SINETABLE[ (int)( Math.abs( alpha ) ) % 360 ];
    }
    
    private double _cos( double alpha ) {
        return _SINETABLE[ (int)( Math.abs( alpha ) + 90 ) % 360 ];
    }
    
    
    
    private void _rotate( int rotationAxis, double alpha, double[] coords ) {
        double sin = _sin( alpha );
        double cos = _cos( alpha );
        
        int FIRST_AXIS_INDEX = 0;
        int SECOND_AXIS_INDEX = 0;
        if ( rotationAxis == X_AXIS ) {
            FIRST_AXIS_INDEX = Y_AXIS;
            SECOND_AXIS_INDEX = Z_AXIS;
        } else if ( rotationAxis == Y_AXIS ) {
            FIRST_AXIS_INDEX = X_AXIS;
            SECOND_AXIS_INDEX = Z_AXIS;
        } else {
            FIRST_AXIS_INDEX = X_AXIS;
            SECOND_AXIS_INDEX = Y_AXIS;
        }

        double oldV = coords[ FIRST_AXIS_INDEX ];
        coords[ FIRST_AXIS_INDEX ] = coords[ FIRST_AXIS_INDEX ] * cos - coords[ SECOND_AXIS_INDEX ] * sin;
        coords[ SECOND_AXIS_INDEX ] = oldV * sin + coords[ SECOND_AXIS_INDEX ] * cos;
    }
    

    
    
    
    
    public void paintComponent( Graphics g ) {
        super.paintComponent( g );
        if ( RUNNING ) {
            paintTunnelComponent( g );
        }
        paintLogoComponent( g );
    }
    
    

    
    private void prepareAllPoints() {

        int x = - ( LOGO_COLS.length >> 1 ) * _LOGO_SCALE;
        int y = - ( _LOGO_Y_RESOLUTION >> 1 ) * _LOGO_SCALE;

        Set<double[]> allPoints =  new HashSet<double[]>();
        for ( int i = 0; i<LOGO_COLS.length; i++ ) {

            y = - ( _LOGO_Y_RESOLUTION >> 1 ) * _LOGO_SCALE;
            int _COL = LOGO_COLS[ i ];
           
            while ( _COL > 0 ) {

                if ( ( _COL & 0x1 ) > 0 ) {
                    double[] coords = new double[ 3 ];
                    coords[ X_AXIS ] = x;
                    coords[ Y_AXIS ] = y;
                    coords[ Z_AXIS ] = 0;
                    
                    allPoints.add( coords );
                }
                y += _LOGO_SCALE;
                
                _COL >>= 1;
            }
            
            x += _LOGO_SCALE;
        }
        
        // now copy points to array
        int p = 0;
        _ALL_POINTS = new double[ allPoints.size() ][];
        _ALL_ANIM_POINTS = new double[ allPoints.size() ][];
        final Iterator<double[]> it = allPoints.iterator();
        while ( it.hasNext() ) {
            _ALL_POINTS[ p ] = (double[])it.next();
            _ALL_ANIM_POINTS[ p ] = new double[ 3 ];
            System.arraycopy( _ALL_POINTS[ p ], 0, _ALL_ANIM_POINTS[ p ], 0, _ALL_POINTS[ p ].length );
            p++;
        }
        
    }
    
    
    private void paintLogoComponent( Graphics g ) {
        g.setColor( Color.RED );
        int iMax = _ALL_POINTS.length;
        for ( int i=0; i<iMax; i++ ) {
            paintLogoPoint( g, 
                    _ALL_ANIM_POINTS[ i ][ X_AXIS ], 
                    _ALL_ANIM_POINTS[ i ][ Y_AXIS ], 
                    _ALL_ANIM_POINTS[ i ][ Z_AXIS ] 
            );
        }
    }
    
        
    private void paintTunnelComponent( Graphics g ) {
        
        // now draw tunnel
        int zMax = (int) -CAM[ Z_AXIS ] / _TUNNEL_RING_SPACE;
        double x = 0;
        double y = 0;
        double prevX = 0;
        double prevY = 0;
        int alpha = 0;
        int steps = 360 / _TUNNEL_ALPHA;

        double dX = 0;
        double dY = 0;
        double dX0 = 0;
        double dY0 = 0;
        
        int colStrength = 0;
        
        for ( int aS=0; aS<=steps; aS++ ) {
            x = _TUNNEL_RADIUS * _sin( alpha );
            y = _TUNNEL_RADIUS * _cos( alpha );
            if ( aS > 0 ) {
                for ( int z=0; z<zMax; z++ ) {
                    
                    if ( ( -z * _TUNNEL_RING_SPACE ) - CAM[ Z_AXIS ] < LOGO_COLS.length ) {

                        colStrength = (int) ( 255 * ( ( ( -z * _TUNNEL_RING_SPACE ) - CAM[ Z_AXIS ] ) / LOGO_COLS.length ) );
                        g.setColor( new Color( colStrength, colStrength, colStrength ) );
                        
                        dX = _TUNNEL_JUMPER_STRENGTH * _sin( ( z - ringWaveOffset ) * _TUNNEL_WAVE_ALPHA );
                        dY = _TUNNEL_JUMPER_STRENGTH * _cos( ( z - ringWaveOffset ) * _TUNNEL_WAVE_ALPHA );
                        if ( z > 0 ) {
                            dX0 = _TUNNEL_JUMPER_STRENGTH * _sin( ( z - 1 - ringWaveOffset ) * _TUNNEL_WAVE_ALPHA );
                            dY0 = _TUNNEL_JUMPER_STRENGTH * _cos( ( z - 1 - ringWaveOffset ) * _TUNNEL_WAVE_ALPHA );
                        }

                        drawLine( g, x + dX, y + dY, -z * _TUNNEL_RING_SPACE - ringOffset, prevX + dX, prevY + dY, -z * _TUNNEL_RING_SPACE - ringOffset );
                        if ( z > 0 ) {
                            drawLine( g, x + dX, y + dY, -z * _TUNNEL_RING_SPACE - ringOffset, x + dX0, y + dY0, -(z - 1) * _TUNNEL_RING_SPACE - ringOffset );
                        }
                    }
                }
            }
            alpha += _TUNNEL_ALPHA;
            prevX = x;
            prevY = y;
        }
    }
    
    
    
    
    
    private void paintLogoPoint( Graphics g, double x, double y, double z ) {
        
        int _LOGO_POINT_SIZE = (int) ( this._LOGO_POINT_SIZE * ( 1 - ( z / ( _LOGO_SCALE * LOGO_COLS.length >> 1 ) ) ) );
        int _COL_STRENGTH = Math.min( (int) ( 127 * ( 1 - ( z / ( _LOGO_SCALE * LOGO_COLS.length >> 1 ) ) ) ), 255 );
        g.setColor( new Color( 
                ( colorComponents[ _RED ] * _COL_STRENGTH ) & 0xFF, 
                ( colorComponents[ _GREEN ] * _COL_STRENGTH ) & 0xFF, 
                ( colorComponents[ _BLUE ] * _COL_STRENGTH ) & 0xFF 
        ) );
        
        paintPoint( g, x, y, z, _LOGO_POINT_SIZE );
    }
    

    
    private void paintPoint( Graphics g, double x, double y, double z, int pointSize ) {
        int sX = (int)( ( x - CAM[ X_AXIS ] ) / ( z - CAM[ Z_AXIS ] ) * POV + width );
        int sY = (int)( ( y - CAM[ Y_AXIS ] ) / ( z - CAM[ Z_AXIS ] ) * POV + height );

        if ( pointSize > 1 ) {
            g.fillRect( sX - ( pointSize >> 1 ), sY - ( pointSize >> 1 ), pointSize, pointSize );
        } else {
            g.drawLine( sX, sY, sX, sY );
        }
    }    
    

    
    private void drawLine( Graphics g, double x1, double y1, double z1, double x2, double y2, double z2 ) {
        int sX1 = (int)( ( x1 - CAM[ X_AXIS ] ) / ( z1 - CAM[ Z_AXIS ] ) * POV + width );
        int sY1 = (int)( ( y1 - CAM[ Y_AXIS ] ) / ( z1 - CAM[ Z_AXIS ] ) * POV + height );
        int sX2 = (int)( ( x2 - CAM[ X_AXIS ] ) / ( z2 - CAM[ Z_AXIS ] ) * POV + width );
        int sY2 = (int)( ( y2 - CAM[ Y_AXIS ] ) / ( z2 - CAM[ Z_AXIS ] ) * POV + height );
        
        g.drawLine( sX1, sY1, sX2, sY2 );
    }
    
    
    
    
    
    
    
    
    final Object locker = new Object();
    
    
    public final void start() {
        init();
    }
    
    
    private final void runPipes() {
        if ( RUNNING ) {
            return;
        }
        prevBg = getBackground();
        setBackground( Color.WHITE );

        RUNNING = true;
        worker = new Thread( new Runnable() { 
            public void run() {
                try {
                    while ( RUNNING ) {
                        update();
                        synchronized (locker) {
                            locker.wait( SLEEP_TIMEOUT );
                        }
                    }
                } catch ( InterruptedException ie ) {
                    ie.printStackTrace();
                }
            }
        });
        worker.setDaemon( true );
        worker.start();
    }
    
    public final void stop() {
        setBackground( prevBg );
        RUNNING = false;
        update();
    }
    
    
        
}

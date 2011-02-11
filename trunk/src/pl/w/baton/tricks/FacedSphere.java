/*
 * Rafal Zaczynski (rafal.zaczynski@gmail.com)
 * 
 * Created on 2006-03-14
 */
package pl.w.baton.tricks;

import java.awt.*;
import java.awt.event.*;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;



/**
 * 
 * @author rafal.zaczynski@gmail.com
 */
public class FacedSphere extends JPanel implements AnimatedComponent {

    private static final int SLEEP_TIMEOUT = 15;

    
    
    private static final int X_AXIS = 0;
    private static final int Y_AXIS = 1;
    private static final int Z_AXIS = 2;
    
    private static double _CAM_SCROLL = 0.3;
    private static double[] CAM = new double[] { 0.4, 0.4, -18.0 };
    private static double[] LIGHT = new double[] { 1.4, -3.4, -8.0 };
    
    
    private static final double[] _SINETABLE = new double[ 360 ];


    private static final int _AMBIENT_LIGHT_POWER = 4;
    private static final int _AMBIENT_LIGHT_STRENGTH = 1 << _AMBIENT_LIGHT_POWER;
    
    
    private double _BALL_RADIUS = 3;
    private int ALPHA_STEP = 12;
    
    private int _ALPHA_COUNT = 180 / ALPHA_STEP;
    private int _BETA_COUNT = 360 / ALPHA_STEP;

    
    private int[][] _PLANES_COLORS = new int[][] { { 1, 1, 1 }, { 1, 0, 0 } };
    
    
    private int width, height;
    private int POV = 1;
    private double[][][] ballP;
    private double[][][] ballAnimP;
    
    
    
    private boolean RUNNING = false;
    private Thread worker;
    
    private boolean initialized = false;

    
    private int _ROTATION_STEP = 2;
    private int currentRotation = 0;
    

    private static final double MULTIPLICATOR_MAX = 1.2;
    private static final double MULTIPLICATOR_MIN = 0.8;
    private static final double MULTIPLICATOR_STEP = 0.025;
    
    private double xMul = 1.0;
    private double yMul = 1.0;
    
    private double xMulStep = MULTIPLICATOR_STEP;
    private double yMulStep = -MULTIPLICATOR_STEP;
    
    
    private double yTranslation = 0;
    private double yTranslationAlpha = 30;
    private double yTranslationStep = - 90 / 45; // step in alpha on scope (0, 90): divide 90 by number of steps
    private double _Y_TRANSLATION_SCALE = 5; // jump from (-S, S)
    
    
    
    public FacedSphere() {
        for ( int i=0; i<360; i++ ) {
            _SINETABLE[ i ] = Math.sin( ( i * Math.PI ) / 180 );
        }
        //setOpaque( false );
        setBackground( Color.BLACK );
        setFocusable( true );
        calculateBall();
        
        addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent me ) {
                if ( me.getButton() == MouseEvent.BUTTON1 ) {
                    if ( me.getClickCount() > 1 ) { // double click run/stop
                        if ( !RUNNING ) {
                            runPipes();
                        } else {
                            stop();
                        }
                    }
                }
            }
        });
        
        // checkered colors changes under R, G and B keys (with or without Shift)
        addKeyListener( new KeyAdapter() {
            public void keyPressed( KeyEvent e ) {
                int component = -1;
                if ( e.getKeyCode() == KeyEvent.VK_R ) { // RED component
                    component = 0;
                } else if ( e.getKeyCode() == KeyEvent.VK_G ) { // GREEN component
                    component = 1;
                } else if ( e.getKeyCode() == KeyEvent.VK_B ) { // BLUE component
                    component = 2;
                }
                
                if ( component >= 0 ) {
                    if ( e.isShiftDown() ) {
                        _PLANES_COLORS[ 0 ][ component ] = ( _PLANES_COLORS[ 0 ][ component ] + 1 ) % 2;
                    } else {
                        _PLANES_COLORS[ 1 ][ component ] = ( _PLANES_COLORS[ 1 ][ component ] + 1 ) % 2;
                    }
                    if ( !RUNNING ) {
                        repaint();
                    }
                }
            }
        });

        addMouseWheelListener( new MouseWheelListener() {
            public void mouseWheelMoved( MouseWheelEvent e ) {
                if ( e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL ) {
                    CAM[ Z_AXIS ] += e.getWheelRotation() * _CAM_SCROLL;
                    
                    if ( !RUNNING ) {
                        repaint();
                    }
                }
            }
        });
        
        
    }

    
    protected void init() {
        if ( initialized ) {
            return;
        }
        
        width = ( getWidth() >> 1 );
        height = ( getHeight() >> 1 );
        POV = Math.min( width, height ) << 1;

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
    


    
    private double ySqeezeTranslation = 0.0;
    
    private final void updateMovement() {
        
        currentRotation = ( _ROTATION_STEP % 360 );
        
        for ( int i=0; i<_ALPHA_COUNT; i++ ) {
            for ( int j=0; j<_BETA_COUNT; j++ ) {
                _rotate( Y_AXIS, _ROTATION_STEP, ballAnimP[ i ][ j ] );
            }
        }
        
        
        // only if we are not squeezing update jumping
        if ( yMul == 1.0 ) {
            // update jumping
            yTranslationAlpha += yTranslationStep;
            if ( yTranslationAlpha <= 0 ) {
                yTranslationStep *= -1;
            } else if ( yTranslationAlpha >= 90 ) {
                yTranslationStep *= -1;
            }
            yTranslation = ( _sin( yTranslationAlpha ) * _Y_TRANSLATION_SCALE * 2 ) - _Y_TRANSLATION_SCALE;
            
        }
        
        // if we are at the bottom of bouncing trigger squeezing
        if ( yTranslationAlpha <= 0 ) {
            if ( xMul > MULTIPLICATOR_MAX || xMul < MULTIPLICATOR_MIN ) {
                xMulStep *= -1;
            }
            xMul += xMulStep;
            if ( yMul > MULTIPLICATOR_MAX || yMul < MULTIPLICATOR_MIN ) {
                yMulStep *= -1;
            }
            yMul += yMulStep;
            
            //as well move ball slightly on half of squeezing
            ySqeezeTranslation = - ( 1 - yMul ) * _BALL_RADIUS;

            //System.out.print( ySqeezeTranslation + ", " );
            
            if ( yMul == 1.0 ) { //reset sqeezing to default on last squeeze step
                xMulStep = MULTIPLICATOR_STEP;
                yMulStep = -MULTIPLICATOR_STEP;
                
                ySqeezeTranslation = 0.0;
            }        
        }
        

        
//        int iMax = _ALL_POINTS.length;
//        for ( int i=0; i<iMax; i++ ) {
//            _rotate( rotationAxis, _ALPHA_STEP, _ALL_ANIM_POINTS[ i ] );
//        }
        
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
    
    
    private void paintPoint( Graphics g, double[] p, int pointSize ) {
        int sX = (int)( ( p[ X_AXIS ] - CAM[ X_AXIS ] ) / ( p[ Z_AXIS ] - CAM[ Z_AXIS ] ) * POV + width );
        int sY = (int)( ( p[ Y_AXIS ] - CAM[ Y_AXIS ] ) / ( p[ Z_AXIS ] - CAM[ Z_AXIS ] ) * POV + height );

        if ( pointSize > 1 ) {
            g.fillRect( sX - ( pointSize >> 1 ), sY - ( pointSize >> 1 ), pointSize, pointSize );
        } else {
            g.drawLine( sX, sY, sX, sY );
        }
    }    
    

    
    private void drawLine( Graphics g, double[] p1, double[] p2 ) {
        int sX1 = (int)( ( p1[ X_AXIS ] - CAM[ X_AXIS ] ) / ( p1[ Z_AXIS ] - CAM[ Z_AXIS ] ) * POV + width );
        int sY1 = (int)( ( p1[ Y_AXIS ] - CAM[ Y_AXIS ] ) / ( p1[ Z_AXIS ] - CAM[ Z_AXIS ] ) * POV + height );
        int sX2 = (int)( ( p2[ X_AXIS ] - CAM[ X_AXIS ] ) / ( p2[ Z_AXIS ] - CAM[ Z_AXIS ] ) * POV + width );
        int sY2 = (int)( ( p2[ Y_AXIS ] - CAM[ Y_AXIS ] ) / ( p2[ Z_AXIS ] - CAM[ Z_AXIS ] ) * POV + height );
        
        g.drawLine( sX1, sY1, sX2, sY2 );
    }

    private void drawPlane( Graphics g, double[][] p ) {
        int points = p.length;
        int[] xPoints = new int[ points ];
        int[] yPoints = new int[ points ];
        
        for ( int i=0; i<p.length; i++ ) {
//            xPoints[ i ] = (int)( ( p[ i ][ X_AXIS ] * xMul - CAM[ X_AXIS ] ) 
//                    / ( p[ i ][ Z_AXIS ] * xMul - CAM[ Z_AXIS ] ) * POV + width );
//            yPoints[ i ] = (int)( ( ( ( p[ i ][ Y_AXIS ] * yMul ) - yTranslation ) - CAM[ Y_AXIS ] ) 
//                    / ( p[ i ][ Z_AXIS ] * xMul - CAM[ Z_AXIS ] ) * POV + height );
            xPoints[ i ] = (int)( ( p[ i ][ X_AXIS ] - CAM[ X_AXIS ] ) 
                    / ( p[ i ][ Z_AXIS ] - CAM[ Z_AXIS ] ) * POV + width );
            yPoints[ i ] = (int)( ( p[ i ][ Y_AXIS ] - CAM[ Y_AXIS ] ) 
                    / ( p[ i ][ Z_AXIS ] - CAM[ Z_AXIS ] ) * POV + height );
        }
        
        g.fillPolygon( xPoints, yPoints, points );
    }



    private double[] getLocallyModifiedCoordinates( double[] coords ) {
        double[] _localCoords = new double[ 3 ];
        _localCoords[ X_AXIS ] = coords[ X_AXIS ] * xMul;
        _localCoords[ Y_AXIS ] = coords[ Y_AXIS ] * yMul - yTranslation - ySqeezeTranslation;
        _localCoords[ Z_AXIS ] = coords[ Z_AXIS ];
        return _localCoords;
    }

    
    private void normalize( double[] v ) {
        // normalize vector
        double len = 0;
        for ( int i=0; i<3; i++ ) {
            len += v[ i ] * v[ i ];
        }
        len = Math.sqrt( len );
        for ( int i=0; i<3; i++ ) {
            v[ i ] /= len;
        }
    }
    
    
    private double[] toVector( double[] a, double[] b ) {
        double[] v = new double[ 3 ];
        v[ X_AXIS ] = b[ X_AXIS ] - a[ X_AXIS ];
        v[ Y_AXIS ] = b[ Y_AXIS ] - a[ Y_AXIS ];
        v[ Z_AXIS ] = b[ Z_AXIS ] - a[ Z_AXIS ];
        
        normalize( v );
        
        return v;
    }
    
    private double dot( double[] u, double[] v ) {
        return u[ X_AXIS ] * v[ X_AXIS ] + u[ Y_AXIS ] * v[ Y_AXIS ] + u[ Z_AXIS ] * v[ Z_AXIS ];
    }

    private double[] cross( double[] a, double[] b ) {
        double[] c = new double[ 3 ];
        c[ X_AXIS ] = a[ Y_AXIS ]* b[ Z_AXIS ] - a[ Z_AXIS ]* b[ Y_AXIS ];
        c[ Y_AXIS ] = a[ Z_AXIS ]* b[ X_AXIS ] - a[ X_AXIS ]* b[ Z_AXIS ];
        c[ Z_AXIS ] = a[ X_AXIS ]* b[ Y_AXIS ] - a[ Y_AXIS ]* b[ X_AXIS ];

        return c;
    }
    
    
    private void drawVisiblePoly( Graphics g, int colMod, double[] a, double[] b, double[] c, double[] d ) {
        a = getLocallyModifiedCoordinates( a );
        b = getLocallyModifiedCoordinates( b );
        c = getLocallyModifiedCoordinates( c );
        d = getLocallyModifiedCoordinates( d );
        // make vectors out of points in correct order
        double[] u = toVector( b, a );
        double[] v = toVector( b, c );
        double[] planeNorm = cross( u, v );
        double dot = dot( planeNorm, toVector( b, CAM ) );
        if ( dot > 0 ) {
            
            double lightDot = dot( planeNorm, toVector( b, LIGHT ) );
            if ( lightDot < 0 ) {
                lightDot = 0;
            }
            int colIntensity = (int)( lightDot * ( 255 - _AMBIENT_LIGHT_STRENGTH ) ) + ( _AMBIENT_LIGHT_STRENGTH );
            //int colIntensity = (int)( dot * 223 ) + ( 32 );

            g.setColor( new Color( colIntensity * _PLANES_COLORS[ colMod ][ 0 ], 
                    colIntensity * _PLANES_COLORS[ colMod ][ 1 ], 
                    colIntensity * _PLANES_COLORS[ colMod ][ 2 ] ) );
            
            drawPlane( g, new double[][] { a, b, c, d } );
        }
    }
    
    
    
    
    /**
     * 
     */
    public void paintComponent( Graphics g ) {
        super.paintComponent( g );

        int _COLORS_MOD = _PLANES_COLORS.length;
        for ( int i=0; i<_ALPHA_COUNT; i++ ) {
            for ( int j=0; j<=_BETA_COUNT; j++ ) {

                if ( j > 0 ) {
                    if ( i > 0 ) {
                        drawVisiblePoly( g, (i+j)%_COLORS_MOD, 
                                ballAnimP[ i % _ALPHA_COUNT ][ j % _BETA_COUNT ], 
                                ballAnimP[ (i-1) % _ALPHA_COUNT ][ j % _BETA_COUNT ], 
                                ballAnimP[ (i-1) % _ALPHA_COUNT ][ (j-1) % _BETA_COUNT ],
                                ballAnimP[ i % _ALPHA_COUNT ][ (j-1) % _BETA_COUNT ] );
                    }
                }
            }
        }
    
    }
    
    
    
    public void calculateBall() {

        double[] p = new double[ 3 ];
        p[ Y_AXIS ] = _BALL_RADIUS;
        _rotate( X_AXIS, ALPHA_STEP / 2, p );
        
        int _COLORS_MOD = _PLANES_COLORS.length;

        ballP = new double[ _ALPHA_COUNT ][ _BETA_COUNT ][ 3 ];
        ballAnimP = new double[ _ALPHA_COUNT ][ _BETA_COUNT ][ 3 ];
        
        double[][] prevPoints = new double[ _BETA_COUNT + 1 ][ 3 ];
        double[][] prevPointsBuff = null;
        for ( int i=0; i<_ALPHA_COUNT; i++ ) {

            prevPointsBuff = new double[ _BETA_COUNT + 1 ][ 3 ];

            double[] q = new double[ 3 ];
            double[] prevQ = new double[ 3 ];
            System.arraycopy( p, 0, q, 0, 3 );
            for ( int j=0; j<_BETA_COUNT; j++ ) {

                System.arraycopy( q, 0, ballP[ i ][ j ], 0, 3 );
                System.arraycopy( q, 0, ballAnimP[ i ][ j ], 0, 3 );
                    
                if ( j > 0 ) {
                    if ( i > 0 ) {
//                        drawVisiblePoly( g, (i+j)%_COLORS_MOD, 
//                                prevQ, q, prevPoints[ j ], prevPoints[ j - 1 ] );
                    }
                    System.arraycopy( prevQ, 0, prevPointsBuff[ j - 1 ], 0, 3 );
                }
                System.arraycopy( q, 0, prevQ, 0, 3 );
                
                _rotate( Y_AXIS, ALPHA_STEP, q );
            }
            System.arraycopy( q, 0, prevPointsBuff[ _BETA_COUNT ], 0, 3 );
            prevPoints = prevPointsBuff;
            
            _rotate( X_AXIS, ALPHA_STEP, p );
            
        }
    }
    
    
    
    
    
    final Object locker = new Object();
    
    
    public final void start() {
        init();
    }
    
    
    private final void runPipes() {
        if ( RUNNING ) {
            return;
        }
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
        RUNNING = false;
        //waveStrength = 0;
        update();
    }
    
    
    
    
}

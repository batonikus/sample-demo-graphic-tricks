/*
 * Rafal Zaczynski (rafal.zaczynski@gmail.com)
 * 
 * Created on 2005-10-05
 */
package pl.w.baton.tricks;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/**
 * 
 * @author rafal.zaczynski@gmail.com
 */
public class AboutC extends JPanel implements AnimatedComponent {

    private static final int SLEEP_TIMEOUT = 15;
    private static final int WAVE_2_PIPES_RATIO = 1;
    
    private static final int STEP_ALPHA = 3;
    private static final int PIPE_SIZE = 5;
    private static final int PIPES_COUNT = 3;
    private static final int PIPES_ALPHA_DELAY = 35; // degrees between pipes
    private final int[][] PIPES_RGB = new int[][] { 
            { 1, 0, 0 },
            { 1, 1, 0 },
            { 0, 0, 1 } 
        };

    private static final Color TRANSPARENT_COLOR = new Color( 0, 0, 0, 0 );

    
    
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
    
    
    
    
    // local vars for pipes rendering
    private int alpha = 0;
    private int r = 0;
    private int width, height;
    private byte rMultiplicator = 1;
    private byte rBase = 0;
    private int[] alt = new int[ PIPES_COUNT ];
    private int[] pipeColors = new int[ PIPE_SIZE ];
    
    
    
    private static final double[] _SINETABLE = new double[ 360 ];
    
    
    // local vars for logo reder
    
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
    private int COL_2_COL_ANGLE = 5;
    private int WAVE_STRENGTH = 14;
    private int WAVE_STEP_ALPHA = 2;

    private int waveAlpha = 0;
    private double waveStrength = 0.0; // the increment start ratio is 0.1 until WAVE_STRENGTH

    
    
    private boolean RUNNING = false;
    private Thread worker;
    
//    private Timer worker;
    
    private boolean initialized = false;
    private int ratioCounter = 0;
    
    
    public AboutC( int scale, int pointSize ) {
        _LOGO_SCALE = scale;
        _LOGO_POINT_SIZE = pointSize;
        setOpaque( true );
        addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent me ) {
                if ( me.getClickCount() > 1 ) {
                    if ( !RUNNING ) {
                        runPipes();
                    } else {
                        stop();
                    }
                }
            }
        });
        
        for ( int i=0; i<360; i++ ) {
            _SINETABLE[ i ] = Math.sin( ( i * Math.PI ) / 180 );
        }
    }
    
    
    
    
    protected void init() {
        if ( initialized ) {
            return;
        }
        
        width = getWidth();
        height = getHeight();

        initPipes();
        
        addComponentListener( new ComponentAdapter() {
            public void componentResized( ComponentEvent ce ) {
                updateSize( ce.getComponent() );
            }
            public void componentShown( ComponentEvent ce ) {
                updateSize( ce.getComponent() );
            }
            private void updateSize( Component c ) {
                width = c.getWidth();
                height = c.getHeight();
                r = ( height >> 1 ) - PIPE_SIZE;
            }
        } );
        
        initialized = true;
        
        repaint();
    }    
    
    
    
    private final void initPipes() {
        r = ( height >> 1 ) - PIPE_SIZE;
        
        //calculate pipes shading gradient (only 1/2 Pi)
        int r = PIPE_SIZE;
        for ( int i = 0; i<PIPE_SIZE; i++ ) {
            pipeColors[ i ] = (int)( ( ((i<<8)+128) / (float)r) ); //sin(alpha)
        }
        
    }
    
    
    
    
    
    private void update() {
        ratioCounter++;
        updatePipes();
        if ( ratioCounter >= WAVE_2_PIPES_RATIO ) {
            updateWave();
            ratioCounter = 0;
        }
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                repaint();
            }
        });
    }
    


    
    private final void updateWave() {
        waveAlpha += WAVE_STEP_ALPHA;
        if ( waveAlpha >= 360 ) waveAlpha = 0;
        if ( waveStrength < WAVE_STRENGTH ) waveStrength += 0.1;
    }
    
    
    private final void updatePipes() {
        alpha += STEP_ALPHA;
        if ( alpha >= 180 ) { // draw pipes in reverse order
            rMultiplicator = -1;
            rBase = 2;
        } else {
            rMultiplicator = 1;
            rBase = 0;
        }
        if ( alpha >= 360 ) alpha = 0;
        
        //recalculate new altitude
        int tempAlpha = alpha;
        for ( int i=0; i<PIPES_COUNT; i++ ) {
            alt[ i ] = (int)( ( height >> 1 ) - r * _sin( tempAlpha ) );
            tempAlpha += PIPES_ALPHA_DELAY;
        }
    }    
    
    
    
    
    
    
    private double _sin( double alpha ) {
        return _SINETABLE[ (int)( alpha ) % 360 ];
    }
    
    
    
    
    
    public void paintComponent( Graphics g ) {
        super.paintComponent( g );
        paintLogoComponent( g );
        if ( initialized && RUNNING ) {
            paintPipesComponent( g );
        }
    }
    
    
    public void paintPipesComponent( Graphics g ) {
        int pRGB = 0;
        int pI = 0;
        for ( int p=0; p<PIPES_COUNT; p++ ) {
            pI = rBase + rMultiplicator * p;
            for ( int i = 0; i<PIPE_SIZE; i++ ) {
                pRGB = 250 - pipeColors[ i ];
                g.setColor( new Color( 255 * PIPES_RGB[ pI ][ 0 ], 255 * PIPES_RGB[ pI ][ 1 ], 255 * PIPES_RGB[ pI ][ 2 ], pRGB ) );
                g.drawLine( 0, alt[ pI ]-i, width, alt[ pI ]-i );
                g.drawLine( 0, alt[ pI ]+i+1, width, alt[ pI ]+i+1 );
            }
        }
    }
    
    
    
    private void paintLogoComponent( Graphics g ) {
        
        g.setColor( Color.RED );
        
        int x = ( ( width - LOGO_COLS.length * _LOGO_SCALE ) >> 1 );

        int y = ( ( height - _LOGO_Y_RESOLUTION * _LOGO_SCALE ) >> 1 );
        int tempAlpha = waveAlpha;
        int tmpY = y;

        for ( int i = 0; i<LOGO_COLS.length; i++ ) {
            
            if ( RUNNING ) {
                //apply wave calculations
                tmpY = (int)( y - waveStrength * _sin( tempAlpha ) );
                tempAlpha += COL_2_COL_ANGLE;
            }

            paintLogoColumn( g, LOGO_COLS[ i ], x, tmpY );
            
            x += _LOGO_SCALE;
        }
    }    
    
    
    
    private void paintLogoColumn( Graphics g, int _COL, int x, int y ) {
        
        while ( _COL > 0 ) {

            if ( ( _COL & 0x1 ) > 0 ) {
                paintLogoPoint( g, x, y );
            }
            y += _LOGO_SCALE;
            
            _COL >>= 1;
        }
    }
    
    
    private void paintLogoPoint( Graphics g, int x, int y ) {
        g.fillRect( x - (_LOGO_POINT_SIZE >> 1), y - (_LOGO_POINT_SIZE >> 1), _LOGO_POINT_SIZE, _LOGO_POINT_SIZE );
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
        waveStrength = 0;
        update();
    }
    
    
        
}

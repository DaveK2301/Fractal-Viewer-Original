/** Creates a Mandlebrot or Julia set Fractal
 *  of variable zoom, location, color, power
 *  and picture size (restrained to square)
 * 
 * @author David Kaplan
 * @version 8-04 
 * @copyright 2001-2004 David Kaplan
 */


import java.awt.*;
import java.awt.image.*;
import javax.swing.*;


public class Fractal extends Component
{
    protected Image myImage; 
    protected int nPix ; // num of pixels in square
    protected int width, height;
    protected double aspectR;
    protected double zoom; // how many times to magnify (must be >0)
    protected double xCenter; // center x of the fractal 
    protected double yCenter;  // center y of the fractal
    protected double uLx,uLy; // the upper left corner coordinates
    protected int numIter; // total num of times to iterate
    protected int [] colorSet; // the color set to use, SUPPLIED BY HANDLER
    protected int [] preMixColors; // pre-mixed colorset map for speed
    protected int numColors; // num of colors in the set 
    protected double xZoomFactor, yZoomFactor, zoomFactor; // magnification of 4x4 unit square 
    protected int colRepeats; // number of times to cycle through colors
    protected int colMode; // math shifts applied to colorset/ somewhat tested
    protected int [] iterationData; // stores iteration data for speed regen.
    protected double [] xCoords; // pre-mapped for speed
    protected double [] yCoords; //  "    "     "    "
    protected int [] pix; // pixel array for Image (ARGB flat-array)
    protected int [] temp; 
    protected boolean useSpeedPass; // turn on SpeedPass two pass quess mode
    protected int power; // must be >= 2 to draw a fractal
    protected boolean isMandlebrot; // mand or julia set
    protected double juliaX, juliaY; // real and imaginary parts of Julia const.
    protected FractalViewer v;
    public static final int NUM_COLOR_MODES = 8;
    public static final int PIXEL_ITERATION_BASED = 0;
    public static final int PIXEL_MAGNITUDE_BASED = 1;
    public static final int PIXEL_COS_BASED = 2;
    public static final int PIXEL_SIN_BASED = 3;    
    public static final int PIXEL_MAG_SHIFT_ANGLE_BASED = 4;  
    public static final int PIXEL_ITER_SHIFT_ANGLE_BASED = 5;
    public static final int PIXEL_ANGLE_BASED = 6;
    public static final int PIXEL_MAG_SHIFT_COS_BASED = 7;
    public static final int PIXEL_MAG_SHIFT_SIN_BASED = 8;
    public static final int PIXEL_ITER_SHIFT_COS_BASED = 9;
    public static final int PIXEL_ITER_SHIFT_SIN_BASED = 10;
    public static final int INNERCOLOR_BLACK = 0;
    public static final int INNERCOLOR_ANGLE_BASED = 1;
    protected int pixelColorScheme;
    protected int innerPixelColorScheme;

    /**
     * This constructor of the Fractal Class uses the x and y
     * of the dead center of the fractal for orientation
     * in the complex plane. This is for handlers that click
     * on the point in the center of the area the the user 
     * wants to zoom in on.
     *
     * @param xOff X offset from the Uleft corner of the container 
     * @param yOff Y offset from the Uleft corner of the container 
     * @param numPixels the # of pixels (>0), width and height of the image 
     * @param xAtCenter The X coord. of the dead center of the fractal     
     * @param yAtCenter The Y coord. of the dead center of the fractal
     * @param fractZoom - the zoom factor, MUST BE > O!    
     * @param colors the color set array to use- int [ARGB] Must not be null!
     * @param numIterates self explanatory, must be > 0!
     * @param colorRepeats number of times to repeat the colorSet
     * @param colorMode different math shifts of the colorSet to highlight pics
     * @param power The power to raise the complex# to (>=2).
     * @param isMandlebrot true for Mandlebrot, false for Julia set.
     * @param useSpeedPass true for use 2 pass "guessing mode"
     *
     */

/*****************************************************************************/
    public Fractal( int w, int h, double xAtCenter,
    double yAtCenter, double fractZoom, int [] colors, int numIterates,
    int colorRepeats, int colorMode, int power, boolean isMandlebrot,
    double juliaX, double juliaY, boolean useSpeedPass, FractalViewer v)
/*****************************************************************************/
    {
        super();

        //nPix = numPixels; 
        width = w;
        height = h;
        aspectR = (double)width/(double)height;
        setBounds(0, 0,width, height);
        zoom = fractZoom; 
        xCenter = xAtCenter;
        yCenter = yAtCenter;
        if (aspectR >= 1) {
            uLx = xCenter - 2.0/zoom; // transform center to upper left corner           
            uLy = yCenter + 2.0/(zoom*aspectR);
            //System.out.println("init Fractal= uLx= "+ uLx);
            //System.out.println("init Fractal= uLy= "+ uLy);
            //     "       "     "   "     "    "
        } else {
            uLx = xCenter - (2.0*aspectR)/zoom; // transform center to upper left corner
            uLy = yCenter + 2.0/zoom; //     "       "     "   "     "    "
            //System.out.println("init Fractal= uLx= "+ uLx);
            //System.out.println("init Fractal= uLy= "+ uLy);
        }
            
        numIter = numIterates;        
        numColors = colors.length;
        colRepeats = colorRepeats;
        colorSet = colors;
        colMode = colorMode;
        preMixColors = mixColorSet ();          
        yCoords = new double [height];
        xCoords = new double [width];
        this.power = power;
        this.isMandlebrot = isMandlebrot;
        this.juliaX = juliaX;
        this.juliaY = juliaY;
        this.useSpeedPass = useSpeedPass;
        this.v = v;
        iterationData = new int [height * width]; // store iterations
        pixelColorScheme = PIXEL_ITERATION_BASED;
        innerPixelColorScheme = INNERCOLOR_BLACK ;
    }

    /**
     * The mix colorSet method takes the color-array
     * and applies functions to shift and repeat the
     * the array, based on iterations, colorMode and 
     * colorRepeats
     *
     * @returns a premixed set optimized for the iteration values.
     */

    protected int[] mixColorSet()
    {    
        int [] mixedSet = new int [numIter]; 
                                   
        switch (colMode)
        {    
            case 2: // 1/4 sine cycle
                for (int i=0; i < numIter; i++)
                {     
                    mixedSet [i] = colorSet [
                    (int)((Math.sin((i/(double)numIter)*1.57079)
                    *numColors*colRepeats))%
                    numColors]; 
                }
                break;

            case 3: // hyperbolic 8x
                for (int i=0; i < numIter; i++)  
                {  
                    mixedSet[i]=colorSet [((int)((-1.0/
                    ((8.0*((double)i/numIter))+1)+1)*numColors
                    *colRepeats))%numColors ];
                }                    
                break;

            case 4: // hyperbolic 16x
                for (int i=0; i < numIter; i++)
                {
                    mixedSet[i]=colorSet [((int)((-1.0/
                    ((16.0*((double)i/numIter))+1)+1)*numColors
                    *colRepeats))%numColors ];              
                }
                break;
            
            case 5: // y =squareroot (x)
                for (int i=0; i < numIter; i++)
                {     
                    mixedSet[i]= colorSet [(int)(Math.sqrt((double)i/numIter)
                    *numColors*colRepeats)%numColors];
                }            
                break;
          
            case 6: // 1/2 sine cycle
                for (int i=0; i < numIter; i++)
                {     
                    mixedSet [i] = colorSet [
                    (int)((Math.sin(i/(double)numIter*3.1415926)
                    *numColors*colRepeats))%
                    numColors]; 
                }            
                break;

            case 7: // full sine cycle
                for (int i=0; i < numIter; i++)
                {     
                    double x = (double) i/numIter *6.283;
                    mixedSet [i] = colorSet [
                    (int)( (Math.sin (x)+1)*0.5
                    *numColors*colRepeats )%
                    numColors]; 
                }            
                break;

            case 8: // full cosine cycle
                for (int i=0; i < numIter; i++)
                {     
                    double x = (double) i/numIter *6.283;
                    mixedSet [i] = colorSet [
                    (int)( (Math.cos(x)+1)*0.5
                    *numColors*colRepeats )%
                    numColors]; 
                }            
                break; 

            default: //straight colors, use 1 as passed param for this one
                
                for (int i=0; i<numIter; i++)
                {
                    mixedSet[i]= colorSet[(int)((double)i/numIter
                    *numColors*colRepeats)%numColors];               
                }
        }  
     return mixedSet;             
     }   

/*****************************************************************************/


    public void redraw ( int numPixels,double xAtCenter, double yAtCenter, double fractZoom, int numIterates, boolean useSpeedPass)
    {
        height = numPixels;
        width = numPixels;
        setBounds( 0, 0,width, height);
        zoom = fractZoom; 
        xCenter = xAtCenter;
        yCenter = yAtCenter;
        uLx = xCenter - 2.0/zoom; // transform center to upper left corner
        uLy = yCenter + 2.0/zoom; //     "       "     "   "     "    "
        numIter = numIterates;               
        yCoords = new double [nPix];
        xCoords = new double [nPix];
        this.useSpeedPass = useSpeedPass;
        iterationData = new int [nPix * nPix]; // store iterations
        generateImage();
        v.notifyMe();
    }


    


    /**
     * The generateImage method does the work of computing the pixels
     * and creating a single instance of MemoryImageSource
     * to use the pixel array. This is the time consuming
     * part of the code
     */
    

    public void generateImage()

    {
        double startT = (double)(System.currentTimeMillis())/1000.0;
        JFrame progress = null;
        boolean isParent = false;
        double maxnormsq = 0;
        int limit;
        // max escape magnitude tied to power =  (2 ^ power) + 2;
        double maxescapemag=1;
        for ( int i = 0 ; i < power; i ++ )  {
            maxescapemag *= 2;
        }
        maxescapemag += 2;
        maxescapemag = maxescapemag * maxescapemag;
        if ( v.getFrame() instanceof JFrame ) 
        {
            progress = (JFrame) v.getFrame();
            isParent = true;
        } else {
            progress = new JFrame();
            progress.setBounds (10,10,80,20);
            progress.setSize (400, 20 );
            progress.getContentPane().add( new bBox (0,0,380 , 10 , Color.white ));
            progress.setVisible ( true );
            progress.show();
            progress.validate();
            progress.repaint();
        }

        pix = new int [width * height] ; // the pixel array
        int index = 0; // index counter for the pixel array
        if (aspectR >=1){
            xZoomFactor = 4.0/(width*zoom); // magnifcation factor of 4x4 square 
            yZoomFactor = 4.0/(height*zoom*aspectR);
        } else {
            xZoomFactor = 4.0*aspectR/(width*zoom); // magnifcation factor of 4x4 square 
            yZoomFactor = 4.0/(height*zoom);
        }
        int i = 0;
           
        // pre map x and y coords for speed and later regen.
        for (i = 0; i < width; i++)
        {
            xCoords [i] = (i*xZoomFactor)+uLx;
        }

        for (i = 0; i < height; i++)
        {
            yCoords [i] =-(i*yZoomFactor)+uLy;
        }
        // the y scan raster
        if (!useSpeedPass)
        {
            // no SpeedPass: calculate every pixel
            for (int y = 0; y < height; y++) {
                // the x scan raster
                for (int x = 0; x < width; x++) {
                    double zx = 0;
                    double zy = 0;
                    double incrX = xCoords[x];
                    double incrY = yCoords[y];
                    double tempzx = 0;
                    double origZx;
                    double origZy;

                    if (!isMandlebrot) 
                    {    
                        zx = xCoords[x];
                        zy = yCoords[y];

                        incrX = juliaX;
                        incrY = juliaY;
                    }                    

                    // the iteration loop                
                    
                    for (i=0 ; i < numIter; i++) {            
                        origZx = zx;
                        origZy = zy;
                        for (int j=0; j < power-1; j++) {
                            tempzx = (zx*origZx)- (zy*origZy); //real part
                            zy = zx*origZy + zy*origZx; // imag
                            zx = tempzx;
                        }
                        zx += incrX;
                        zy += incrY;
                        if (zx*zx + zy*zy > 4) break; // the "escape value"
                    }
 
                    
                    // here is switches for various pixel coloring schemes
                    if (i<numIter) {
                        double normsq;
                        int ratio_ind;
                        double radius;
                        double angle;
                        double cosine;
                        switch ( pixelColorScheme ) {

                            case PIXEL_ITERATION_BASED:
                                iterationData [index] = i; // store iter. val for regen.     
                                pix[index++]=preMixColors[i];    
                                break;
                                case PIXEL_MAGNITUDE_BASED:
                                    // hit escape value at iteration i
                                    // check original x and y werent outside of circle radius 2
                                    
                                    if ( xCoords[x]*xCoords[x] + yCoords[y]*yCoords[y] > 4 ) {
                                        normsq = maxescapemag;
                                    }
                                    else {
                                        normsq = zx*zx + zy*zy;
                                    }
                                    if ( normsq > maxnormsq ) {
                                        maxnormsq = normsq;
                                    }
                                    // since numIter is tied to num preset colors for now
                                    // use it to set pixel coloring
                                    ratio_ind = (int)((normsq/maxescapemag)*(numIter-1));
                                    iterationData [index] = ratio_ind; // store iter. val for regen.
                                    pix[index++]=preMixColors[ratio_ind];
                                    break;
  
                                case PIXEL_COS_BASED:
                                    cosine=0;
                                    normsq = zx*zx + zy*zy;                                    
                                    radius = Math.sqrt(normsq);
                                    if ( radius != 0 ) cosine = xCoords[x]/radius;
                                    ratio_ind = (int)( ((cosine+1)/2)*(numIter-1) );
                                    iterationData [index] = ratio_ind; // store iter. val for regen.
                                    pix[index++]=preMixColors[ratio_ind];
                                    break;

                                case PIXEL_SIN_BASED:
                                    double sine=0;
                                    normsq = zx*zx + zy*zy;                                    
                                    radius = Math.sqrt(normsq);
                                    if ( radius != 0 ) sine = yCoords[y]/radius;
                                    ratio_ind = (int)(((sine+1)/2)*(numIter-1));
                                    iterationData [index] = ratio_ind; // store iter. val for regen.
                                    pix[index++]=preMixColors[ratio_ind];
                                    break;

                                    case PIXEL_MAG_SHIFT_ANGLE_BASED:
                                    // hit escape value at iteration i
                                    // check original x and y werent outside of circle radius 2
                                    
                                    if ( xCoords[x]*xCoords[x] + yCoords[y]*yCoords[y] > 4 ) {
                                        normsq = maxescapemag;
                                    }
                                    else {
                                        normsq = zx*zx + zy*zy;
                                    }
                                    if ( normsq > maxnormsq ) {
                                        maxnormsq = normsq;
                                    }
                                    cosine=0;                                    
                                    radius = Math.sqrt(normsq);
                                    if ( radius != 0 ) cosine = xCoords[x]/radius;
                                    angle = Math.acos(cosine);
                                    if (yCoords[y] < 0 )angle = 2*Math.PI-angle;
                                    
                                    // since numIter is tied to num preset colors for now
                                    // use it to set pixel coloring
                                    ratio_ind =(int)((normsq/maxescapemag)*(numIter-1));
                                    // now rotate mag based on angle
                                    ratio_ind += (int)( ( angle/(2*Math.PI) )*(numIter-1) );
                                    if (ratio_ind>(numIter-1)) ratio_ind -= (numIter-1);
                                    iterationData [index] = ratio_ind; // store iter. val for regen.
                                    pix[index++]=preMixColors[ratio_ind];
                                    break;
                                    
                                    case PIXEL_ITER_SHIFT_ANGLE_BASED:
                                    // hit escape value at iteration i
                                    // check original x and y werent outside of circle radius 2
                                    
                                    if ( xCoords[x]*xCoords[x] + yCoords[y]*yCoords[y] > 4 ) {
                                        normsq = maxescapemag;
                                    }
                                    else {
                                        normsq = zx*zx + zy*zy;
                                    }
                                    if ( normsq > maxnormsq ) {
                                        maxnormsq = normsq;
                                    }
                                    cosine=0;                                    
                                    radius = Math.sqrt(normsq);
                                    if ( radius != 0 ) cosine = xCoords[x]/radius;
                                    angle = Math.acos(cosine);
                                    if (yCoords[y] < 0 )angle = 2*Math.PI-angle;
                                    
                                    // since numIter is tied to num preset colors for now
                                    // use it to set pixel coloring
                                    ratio_ind =i;
                                    // now rotate mag based on angle
                                    ratio_ind += (int)( ( angle/(2*Math.PI) )*(numIter-1) );
                                    if (ratio_ind>(numIter-1)) ratio_ind -= (numIter-1);
                                    iterationData [index] = ratio_ind; // store iter. val for regen.
                                    pix[index++]=preMixColors[ratio_ind];
                                    break;
                                    
                                    case PIXEL_ANGLE_BASED:
                                    // hit escape value at iteration i
                                    // check original x and y werent outside of circle radius 2
                                    
                                    if ( xCoords[x]*xCoords[x] + yCoords[y]*yCoords[y] > 4 ) {
                                        normsq = maxescapemag;
                                    }
                                    else {
                                        normsq = zx*zx + zy*zy;
                                    }
                                    if ( normsq > maxnormsq ) {
                                        maxnormsq = normsq;
                                    }
                                    cosine=0;                                    
                                    radius = Math.sqrt(normsq);
                                    if ( radius != 0 ) cosine = xCoords[x]/radius;
                                    angle = Math.acos(cosine);
                                    if (yCoords[y] < 0 )angle = 2*Math.PI-angle;
                                    
                                    // since numIter is tied to num preset colors for now
                                    // use it to set pixel coloring
                                    ratio_ind = (int)( ( angle/(2*Math.PI) )*(numIter-1) );
                                    iterationData [index] = ratio_ind; // store iter. val for regen.
                                    pix[index++]=preMixColors[ratio_ind];
                                    break;
                                    
                                    
                                    case PIXEL_MAG_SHIFT_COS_BASED:
                                    //case 7:
                                    // hit escape value at iteration i
                                    // check original x and y werent outside of circle radius 2
                                    
                                    if ( xCoords[x]*xCoords[x] + yCoords[y]*yCoords[y] > 4 ) {
                                        normsq = maxescapemag;
                                    }
                                    else {
                                        normsq = zx*zx + zy*zy;
                                    }
                                    if ( normsq > maxnormsq ) {
                                        maxnormsq = normsq;
                                    }
                                    cosine=0;                                    
                                    radius = Math.sqrt(normsq);
                                    if ( radius != 0 ) cosine = xCoords[x]/radius;
                                    //angle = Math.acos(cosine);
                                    //if (yCoords[y] < 0 )angle = 2*Math.PI-angle;
                                    
                                    // since numIter is tied to num preset colors for now
                                    // use it to set pixel coloring
                                    ratio_ind =(int)((normsq/maxescapemag)*(numIter-1));
                                    // now rotate mag based on angle
                                    ratio_ind += (int)( ( (cosine+1)/2 )*(numIter-1) );
                                    if (ratio_ind>(numIter-1)) ratio_ind -= (numIter-1);
                                    iterationData [index] = ratio_ind; // store iter. val for regen.
                                    pix[index++]=preMixColors[ratio_ind];
                                    break;
                                    
                                    case PIXEL_MAG_SHIFT_SIN_BASED:

                                    // hit escape value at iteration i
                                    // check original x and y werent outside of circle radius 2
                                    
                                    if ( xCoords[x]*xCoords[x] + yCoords[y]*yCoords[y] > 4 ) {
                                        normsq = maxescapemag;
                                    }
                                    else {
                                        normsq = zx*zx + zy*zy;
                                    }
                                    if ( normsq > maxnormsq ) {
                                        maxnormsq = normsq;
                                    }
                                    sine=0;                                    
                                    radius = Math.sqrt(normsq);
                                    if ( radius != 0 ) sine = yCoords[y]/radius;
                                    //angle = Math.acos(cosine);
                                    //if (yCoords[y] < 0 )angle = 2*Math.PI-angle;
                                    
                                    // since numIter is tied to num preset colors for now
                                    // use it to set pixel coloring
                                    ratio_ind =(int)((normsq/maxescapemag)*(numIter-1));
                                    // now rotate mag based on angle
                                    ratio_ind += (int)( ( (sine+1)/2 )*(numIter-1) );
                                    if (ratio_ind>(numIter-1)) ratio_ind -= (numIter-1);
                                    iterationData [index] = ratio_ind; // store iter. val for regen.
                                    pix[index++]=preMixColors[ratio_ind];
                                    break;
                                    
                                    case PIXEL_ITER_SHIFT_COS_BASED:
                                    // hit escape value at iteration i
                                    // check original x and y werent outside of circle radius 2
                                    
                                    if ( xCoords[x]*xCoords[x] + yCoords[y]*yCoords[y] > 4 ) {
                                        normsq = maxescapemag;
                                    }
                                    else {
                                        normsq = zx*zx + zy*zy;
                                    }
                                    if ( normsq > maxnormsq ) {
                                        maxnormsq = normsq;
                                    }
                                    cosine=0;                                    
                                    radius = Math.sqrt(normsq);
                                    if ( radius != 0 ) cosine = xCoords[x]/radius;
                                    //angle = Math.acos(cosine);
                                    //if (yCoords[y] < 0 )angle = 2*Math.PI-angle;
                                    
                                    // since numIter is tied to num preset colors for now
                                    // use it to set pixel coloring
                                    ratio_ind =i;
                                    // now rotate iters based on angle
                                    ratio_ind += (int)( ( (cosine+1)/2 )*(numIter-1) );
                                    if (ratio_ind>(numIter-1)) ratio_ind -= (numIter-1);
                                    iterationData [index] = ratio_ind; // store iter. val for regen.
                                    pix[index++]=preMixColors[ratio_ind];
                                    break;
                                    
                                    case PIXEL_ITER_SHIFT_SIN_BASED:
                                    // hit escape value at iteration i
                                    // check original x and y werent outside of circle radius 2
                                    
                                    if ( xCoords[x]*xCoords[x] + yCoords[y]*yCoords[y] > 4 ) {
                                        normsq = maxescapemag;
                                    }
                                    else {
                                        normsq = zx*zx + zy*zy;
                                    }
                                    if ( normsq > maxnormsq ) {
                                        maxnormsq = normsq;
                                    }
                                    sine=0;                                    
                                    radius = Math.sqrt(normsq);
                                    if ( radius != 0 ) sine = yCoords[y]/radius;
                                    //angle = Math.acos(cosine);
                                    //if (yCoords[y] < 0 )angle = 2*Math.PI-angle;
                                    
                                    // since numIter is tied to num preset colors for now
                                    // use it to set pixel coloring
                                    ratio_ind = i;
                                    // now rotate iters based on angle
                                    ratio_ind += (int)( ( (sine+1)/2 )*(numIter-1) );
                                    if (ratio_ind>(numIter-1)) ratio_ind -= (numIter-1);
                                    iterationData [index] = ratio_ind; // store iter. val for regen.
                                    pix[index++]=preMixColors[ratio_ind];
                                    break;
                                    
                                    
                        }
                        // never escaped, is part of the inside fractal set
                    }
                    else  {
                        double normsq, cosine, radius, angle;
                        int ratio_ind;
                        switch ( innerPixelColorScheme ) {
                            case INNERCOLOR_BLACK:
                                iterationData [index] = i; // store iter. val for regen.
                                pix[index++] =255 << 24; // color black
                                break;
                                
                                case INNERCOLOR_ANGLE_BASED:
                                    // hit escape value at iteration i
                                    // check original x and y werent outside of circle radius 2
                                    
                                    if ( xCoords[x]*xCoords[x] + yCoords[y]*yCoords[y] > 4 ) {
                                        normsq = maxescapemag;
                                    }
                                    else {
                                        normsq = zx*zx + zy*zy;
                                    }
                                    if ( normsq > maxnormsq ) {
                                        maxnormsq = normsq;
                                    }
                                    cosine=0;                                    
                                    radius = Math.sqrt(normsq);
                                    if ( radius != 0 ) cosine = xCoords[x]/radius;
                                    angle = Math.acos(cosine);
                                    if (yCoords[y] < 0 )angle = 2*Math.PI-angle;                                    
                                    ratio_ind = (int)( ( angle/(2*Math.PI) )*(numIter-1) );
                                    iterationData [index] = ratio_ind; // store iter. val for regen.
                                    pix[index++]=preMixColors[ratio_ind];
                                    break;                                
                        }
                    }

                    
                    
                }                      
            int percent = (int)((y+1)/(double)height*100+0.5); 
            progress.setTitle("Calculating image: " + percent + "% complete "); 
            progress.repaint();            
            }
        }
        else
        {
        boolean evenPixels = true;
            if (width%2==0)  
            {
                evenPixels = true;
            }else evenPixels = false;

            // Pass-One:
            // draw every-other pixel, alternately
            // staggering every-other row            

            boolean evenScan = true;
            for (int y = 0; y < height; y++ ) 
            {
                // the x scan raster                
                int temp = 0;
                if (!evenScan) temp= 1;
                for (int x = temp; x < width; x+=2) {
                    double zx = 0;
                    double zy = 0;
                    double incrX = xCoords[x];
                    double incrY = yCoords[y];
                    double tempzx = 0;
                    double origZx;
                    double origZy;

                    if (!isMandlebrot) 
                    {    
                        zx = xCoords[x];
                        zy = yCoords[y];
                        incrX = juliaX;
                        incrY = juliaY;
                    }                    
                    // the iteration loop                
                    
                    for (i=0 ; i < numIter; i++) {            
                        origZx = zx;
                        origZy = zy;
                        for (int j=0; j < power-1; j++) {
                            tempzx = (zx*origZx)- (zy*origZy); //real part
                            zy = zx*origZy + zy*origZx; // imag
                            zx = tempzx;
                        }
                        zx += incrX;
                        zy += incrY;
                        if (zx*zx + zy*zy > 4) break; // the "escape value"
                    }
                    iterationData [index] = i; // store iter. val for regen.    
                    if (i < numIter) {  // hit escape value at iteration i     
                        pix[index]=preMixColors[i];    
  
                    }
                    // never escaped, is part of the inside fractal set
                    else 
                    {
                        pix[index] =255 << 24; // color black                        
                    }    
                    index += 2;
                }
            if (evenScan && evenPixels) index++;
            if (!evenScan && evenPixels) index--;                                                     
            evenScan  = (!evenScan);
            int percent = (int)((y+1)/(double)height*100+0.5); 
            progress.setTitle("Calculating Image--Pass One: "+percent+"%  complete ");             
            }
        progress.setTitle("Pass Two: 0% complete");
        v.notifyMe();
        index = width +2;

        // Pass Two:
        // draw every -other pixel checking 4 immediate (North, East,
        // South, West) neighbors for equality. If so, steal
        // value and skip iterations

            evenScan = false;
            for (int y = 1; y < height-1; y++ ) 
            {
                // the x scan raster                
                int temp = 1;
                if (!evenScan) temp= 2;

                for (int x = temp; x < width-1; x+=2) {
                    double zx = 0;
                    double zy = 0;
                    double incrX = xCoords[x];
                    double incrY = yCoords[y];
                    double tempzx = 0;
                    double origZx;
                    double origZy;

                    if (!isMandlebrot) 
                    {    
                        zx = xCoords[x];
                        zy = yCoords[y];
                        incrX = juliaX;
                        incrY = juliaY;
                    }                                   
                    // the iteration loop                
                    int val = pix[index+1];
                    if ( (val==pix[index-1]) && 
                    (val == pix[index+width]) &&
                    (val == pix[index-width]) )
                    {
                        pix[index] = val;
                        iterationData[index] = iterationData[index+1];
                    }
                    else
                    {    
                        for (i=0 ; i < numIter; i++)
                        {            

                            origZx = zx;
                            origZy = zy;
                            for (int j=0; j < power-1; j++) {
                                tempzx = (zx*origZx)- (zy*origZy); //real part
                                zy = zx*origZy + zy*origZx; // imag
                                zx = tempzx;
                            }
                            zx += incrX;
                            zy += incrY;
                            if (zx*zx + zy*zy > 4) break;
                        }
                        iterationData [index] = i; // store iter. val for regen.    
                        if (i < numIter)
                        {  // hit escape value at iteration i     
                            pix[index]=preMixColors[i];    
                        }
                        // never escaped, is part of the inside fractal set
                        else 
                        {
                            pix[index] =255 << 24; // color black                        
                        }    
                    }
                    index += 2;
                }
            if (evenScan)
            {
                index = (y+1)*width +2; 
            } else index = (y+1)*width +1; // different wrap around for odd/ even
                
            evenScan  = (!evenScan);
            int percent = (int)((y+1)/(double)height*100+0.5); 
            progress.setTitle("Pass Two: " + percent + "% complete ");             
            }

        }
        if ( !isParent ) 
            progress.dispose();
        else
            progress.setTitle(v.getFrameTitle());
        myImage = createImage(new MemoryImageSource(width,height, pix, 0,width));        
        v.notifyMe();
        System.gc();
        double finishT = (double)(System.currentTimeMillis())/1000.0; 
        //System.out.println("Fractal generateImage() elasped time: " + ( finishT - startT ) );
        //System.out.println ( "maxnormsq : " + maxnormsq );
        //System.out.println ( "maxescapemag : " + maxescapemag );
    }

    /**
     * The changeColors method allows the pre-mapped iteration
     * bands to switch colorSet, mode and repeats. Long Live
     * the chameleon!
     *
     * @param cSet - the new (or the same) colorSet
     * @param cMode the new color mode to apply
     * @param numReps the new color reps to apply        
     *
     */

    public void changeColors (int[] cSet,int cMode,int numReps)
    {
        colorSet = cSet;         
        colMode = cMode; 
        numColors = colorSet.length;
        colRepeats = numReps;
        preMixColors = mixColorSet(); // re-mix the pre-mix
        int totalPix = (width*height); 
        /*int []*/ pix = new int [totalPix]; // new pixel array       
        for (int i = 0; i < totalPix; i++)
        {

        if (iterationData[i] < numIter) pix[i] =  // iteration hit escape val.
        preMixColors [ iterationData [i] ];       // recolor pixel
        else pix [i] = 255 << 24;                 // iteration didn't escape
        }
        // re-create the image
        myImage = createImage(new MemoryImageSource(width,height, pix, 0,width));
        System.gc();
    }



    /**
     * The cycleColorsUp method rotates the color array to the right,
     * wrapping around.
     *
     * @param c how many colors to shift
     *
     */

    public void cycleColorsUp (int c)
    {
        int nCyc =c;
        temp= new int [nCyc];
        for (int i = 0; i < nCyc; i++) {
            temp [i] = preMixColors [i];
        }
        for (int i = 0; i < numIter-nCyc ; i++) {
            preMixColors [i] = preMixColors [(i+nCyc)];         
       }
        for (int i = 0; i < nCyc; i++) {
            preMixColors [numIter/*-1*/-nCyc+i] = temp [i];
        }
        int totalPix = (width*height);
        /*int []*/ pix = new int [totalPix];       
        for (int i = 0; i < totalPix; i++)
        {
        if (iterationData[i] < numIter) pix[i] = 
        preMixColors [ iterationData [i] ];
        else pix [i] = 255 << 24;    
        }
        // regenerate image    
        myImage = createImage(new MemoryImageSource(width,height, pix, 0,width));        
        this.repaint();
        v.notifyMe();
        System.gc();
    }

    public void cycleColorsDown (int c)
    {
        int nCyc =  c;
        temp= new int [nCyc];
        for (int i = 0; i < nCyc ; i++) {
            temp [i] = preMixColors [numIter-nCyc+i-1];
        }
        for (int i = numIter-1 ; i > nCyc-1 ; i--) {
            preMixColors [i] = preMixColors [(i-nCyc)];         
       }
        for (int i = 0; i < nCyc; i++) {
            preMixColors [i] = temp [i];
        }
        int totalPix = (width*height);
        /*int []*/ pix = new int [totalPix];       
        for (int i = 0; i < totalPix; i++)
        {
            if (iterationData[i] < numIter) pix[i] = 
            preMixColors [ iterationData [i] ];
            else pix [i] = 255 << 24;    
        }
        // regenerate image    
        myImage = createImage(new MemoryImageSource(width,height, pix, 0,width));        
        this.repaint();
        v.notifyMe();
        System.gc();
    }
    public Image getImage()
    {
        return myImage;
    }
    public void setPower ( int p, int w, int h) {
        power = p;
        zoom = 1; 
        xCenter = 0;
        yCenter = 0;
        redraw(w, h, 0,0,numIter,1);
    }
    public void setIterations( int iters ) {
        numIter = iters;
        preMixColors = mixColorSet();
        redraw(width,height,xCenter,yCenter,numIter,zoom);
    }
    public void setJulia (double juliaX, double juliaY, double centerX, 
                          double centerY, double zoomNew, int widthNew,
                          int heightNew, int newIterates) 
    {
        isMandlebrot = false;
        this.juliaX = juliaX;
        this.juliaY = juliaY;
        redraw ( widthNew, heightNew, centerX, centerY, newIterates,
                 zoomNew);
    }   
    public void setMand ( double centerX, double centerY, double zoomNew,
                          int widthNew, int heightNew, int newIterates) 
    {
        isMandlebrot = true;
        preMixColors = mixColorSet ();
        redraw ( widthNew, heightNew,centerX, centerY, newIterates,
                 zoomNew);
    }   
    public void setFractSize( int s ) {
        redraw(nPix,xCenter,yCenter,zoom,numIter,useSpeedPass);
        
    }

    public void redraw ( int w, int h,double xAtCenter,
    double yAtCenter, int numIterates,double fractZoom)
    { 
        width = w;
        height = h;
        aspectR = (double)width/(double)height;
        setBounds(0, 0 ,width, height);
        zoom = fractZoom; 
        xCenter = xAtCenter;
        yCenter = yAtCenter;
        if (aspectR >= 1) {
            uLx = xCenter - 2.0/zoom; // transform center to upper left corner           
            uLy = yCenter + 2.0/(zoom*aspectR);
            //System.out.println("redraw Fractal= uLx= "+ uLx);
            //System.out.println("redraw Fractal= uLy= "+ uLy);
            //     "       "     "   "     "    "
        } else {
            uLx = xCenter - (2.0*aspectR)/zoom; // transform center to upper left corner
            uLy = yCenter + 2.0/zoom; //     "       "     "   "     "    "
            //System.out.println("redraw Fractal= uLx= "+ uLx);
            //System.out.println("redraw Fractal= uLy= "+ uLy);
        }
        if ( numIter != numIterates) {            
            numIter = numIterates;
            preMixColors = mixColorSet ();            
        }
        yCoords = new double [height];
        xCoords = new double [width];
        //System.out.println("redraw Fractal= zoom= "+ zoom);
        iterationData = new int [height * width]; // store
        generateImage();
         v.notifyMe();
    }
    

    public void setSpeedPass (boolean useSpeedPass ) {
        this.useSpeedPass = useSpeedPass;
    }

    public void loadGradient ( int [] colors ) {
        colorSet = colors;
    }
    
    public void setPixelScheme ( int mode ) {
        pixelColorScheme = mode;
        redraw(width,height,xCenter,yCenter,numIter,zoom);
    }
    
    /**
     * Toggles the inner colors between black
     * and angle-based schemes.
     */
    public void toggleInnerColors() {
    	if (innerPixelColorScheme == INNERCOLOR_BLACK) {
    		innerPixelColorScheme = INNERCOLOR_ANGLE_BASED;
    	} else {
    		innerPixelColorScheme = INNERCOLOR_BLACK;
    	}
    	redraw(width,height,xCenter,yCenter,numIter,zoom);
    }
    public void paint( Graphics g )
    {
        g.drawImage( myImage, 0, 0, Color.gray, this );
    }
    public int[] getPix() {
        return pix;
    }
}

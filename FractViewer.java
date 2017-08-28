import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*; // for encoding/saving Png's :) Thanx Java

/**
 * FractViewer: a combination controller/viewer application for visualizing
 * fractal data as gradient color graphs.
 *
 * @author David Kaplan
 * @version 1_4991
 * @copyright 2004-2017 David Kaplan
 */


public class FractViewer extends JFrame implements FractalViewer,
     ActionListener, MouseListener, MouseMotionListener 
{
    // JFrame parameters
    public static final String APP_TITLE = "Fractal_Viewer_1_4991 by Dave Kaplan";
    public static final int DEFAULT_WIDTH_SQUARE = 512;     //init size of Fractal Panel
    public static final int DEFAULT_HEIGHT_SQUARE = 512;    //  ''   ''
    public static final int DEFAULT_ITERS = 256;           
    public static final int BUTTON_PANEL_HEIGHT = 480;
    public static final int BUTTON_PANEL_WIDTH = 120;    
    public static final int ASPECT_MODE_SQUARE = 1;         //aspect ratio modes for
    public static final int ASPECT_MODE_LANDSCAPE = 2;      //zoom box
    public static final int ASPECT_MODE_LETTERBOX = 3;
    public static final int ASPECT_MODE_PORTRAIT = 4;
    public static final int ASPECT_MODE_FREEASPECT = 5;
    public static final int ASPECT_USERDEF = 6;
 
    //Fractviewer params
    private int frameWidth;            //width with Fractal,buttonPanel+insets
    private int frameHeight;           //height "      "       "    "  +  "
    private Container cp;              // ContentPane of this JFrame
    private bBox boundBox;             // zoom box
    private int rootX, rootY;          // corner MousePressed coords. of bBox 
    private Fractal myFractal;         // My Fractal

    // boolean toggles for fixed/free aspect, zoom (or not)/ julia pick mode /
    // corner or center zoom/Mandlebrot or JuliaSet/
    // speed pass on or off/
    // oldZoom remembers previous zoom state if some buttons are cancelled
    private boolean aspectFixed, zoom, selectJuliaMode, 
                    cornerZoom, isMandlebrot, useSpeedPass, oldZoom;

    // Fractal parameters:
    // fixed width, height, store size in fixed aspect mode, fPower is the exponent
    // the complex # is raised to
    private int width,height, fixedWidth, fixedHeight, numIter,
                colorRepeats, colorMode, fPower, palette;

    // aspect is the 'current' aspect, same as fixedWidth/fixedHeight in fixed aspect mode
    private double aspect, fx, fy, fz, juliaX, juliaY;

    // the INT_RGB gradient, supplied by the gradient manager's array list
    // (easy to add new gradient 'plugins' with minimal coding)
    private int [] colorSet;


            
    // Buttons
    private Button zoomButton, juliaButton, mandButton, powerButton , colorButton,
    sizeButton, cycleUpButton, cycleDownButton, iterationButton, paletteButton,
    speedButton, saveButton, aspectButton, zoomTypeButton, saveAnimButton, customDuotoneButton,
    customGradientButton, saveParamsButton, pixelSchemeButton, innerColorButton;

    // Panels
    private Panel buttonPanel;
    private JPanel fractalPanel;          //panel the Fractal rests on

    // Gradient Manager
    private GradientManager grManager;    //supplier class for colorSets

    /**
     * Construct a fractal viewer
     */

    public FractViewer() 
    {
        // initialise window components
        super();
        try {
            setDefaultCloseOperation (EXIT_ON_CLOSE);
        } catch ( SecurityException e ) {}      //catch exception for Applet context
        /*addWindowListener(new WindowAdapter()
            { public void windowClosing(WindowEvent e) {System.exit(0);} } );*/        

        // set up Components
        selectJuliaMode = false;
        oldZoom = false;
        buttonPanel = new Panel();
        fractalPanel = new JPanel();
        buttonPanel.setLayout (new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        fractalPanel.setLayout ( null);
        zoomButton = new Button ("Toggle Zoom: Off");
        zoomButton.addActionListener ( this );
        zoomTypeButton = new Button ("Zoom Type: Corner");
        zoomTypeButton.addActionListener ( this );
        juliaButton = new Button ("Switch To Julia");
        juliaButton.addActionListener ( this );
        mandButton = new Button ("Reset Mandlebrot");
        mandButton.addActionListener ( this );
        powerButton = new Button ("Set Power Z^?");
        powerButton.addActionListener ( this );
        colorButton = new Button ("Color Mode");
        colorButton.addActionListener ( this );
        sizeButton = new Button ("Fractal Size");
        sizeButton.addActionListener ( this );
        cycleUpButton = new Button ("Cycle Colors Up");
        cycleUpButton.addActionListener ( this );
        cycleDownButton = new Button ("Cycle Colors Down");
        cycleDownButton.addActionListener ( this );        
        iterationButton = new Button ("Set # of Iterations");
        iterationButton.addActionListener ( this );
        paletteButton = new Button ("Palette");
        paletteButton.addActionListener ( this ); 
        speedButton = new Button ("Speed Pass: 0ff");
        speedButton.addActionListener ( this );
        saveButton = new Button ("Save Fractal");
        saveButton.addActionListener ( this );
        aspectButton = new Button ("Aspect Mode");
        aspectButton.addActionListener ( this );
        saveAnimButton = new Button ("Save Animation");
        saveAnimButton.addActionListener ( this );
        customDuotoneButton = new Button ("Custom Duotone");
        customDuotoneButton.addActionListener ( this );
        customGradientButton = new Button ("Custom Gradient");
        customGradientButton.addActionListener ( this );
        saveParamsButton = new Button ("Save Params");
        saveParamsButton.addActionListener ( this );
        pixelSchemeButton = new Button ("Pixel Scheme");
        pixelSchemeButton.addActionListener ( this );
        innerColorButton = new Button ("Inner color toggle");
        innerColorButton.addActionListener ( this );
        
        // zoom box
        boundBox = new bBox ( 0, 0, 0, 0, Color.white );   //invisible box
        //fix aspect to square,default size, zoom box from corner
        aspectFixed = true;                                
        cornerZoom = true; 
        fixedWidth = DEFAULT_WIDTH_SQUARE;
        fixedHeight = DEFAULT_HEIGHT_SQUARE;
        //default initialization of Fractal parameters
        // Mandlebrot centered on origin, zoom 1
        fz = 1;
        width = fixedWidth;
        height = fixedHeight;
        aspect = (double)width/(double)height;
        fx = 0;    //origin of Gaussian plane
        fy = 0;    // ''    ''  ''        ''
        numIter = 256;
        colorRepeats = 2; // repeat colors twice through iteration bands
        colorMode = 1;  //no 'tweaks' on the colorset
        fPower = 2;     // classic squared
        juliaX = 0;     // no Julia yet
        juliaY = 0;
        isMandlebrot = true;
        useSpeedPass = false;
        palette = 1;    //full spectrum

        // set fractalPanel to fit Fractal, set up ContentPane
        cp = getContentPane(); 
        fractalPanel.setSize (new Dimension(width, height) );
        fractalPanel.setLayout( null );   
        cp.setLayout ( null );
        cp.setBackground ( Color.cyan );
        setBounds (10, 10 , DEFAULT_WIDTH_SQUARE , 40 );
        setVisible(true);
 
        // add Buttons to Panel
        buttonPanel.add ( zoomButton );
        buttonPanel.add ( zoomTypeButton );
        buttonPanel.add ( juliaButton );
        buttonPanel.add ( mandButton );
        buttonPanel.add ( powerButton );
        buttonPanel.add ( colorButton );
        buttonPanel.add ( sizeButton );
        buttonPanel.add ( speedButton );
        buttonPanel.add ( iterationButton );
        buttonPanel.add ( paletteButton );
        buttonPanel.add ( customDuotoneButton );
        buttonPanel.add ( customGradientButton );
        buttonPanel.add ( saveButton );
        buttonPanel.add ( saveParamsButton );
        buttonPanel.add ( saveAnimButton );
        buttonPanel.add (aspectButton );        
        buttonPanel.add ( cycleUpButton );
        buttonPanel.add ( cycleDownButton );
        buttonPanel.add ( pixelSchemeButton );
        buttonPanel.add ( innerColorButton );



        // get a color set to send to Fractal
        grManager = new GradientManager();
        colorSet = grManager.getGradient(palette);

        // create Fractal and finish initiallization
        drawFractal();    
        setTitle (APP_TITLE);
        validate();
        cp.add (fractalPanel);
        cp.add (buttonPanel);
        Insets i = getInsets();
        //button panel on the left of Fractal panel
        fractalPanel.setBounds ( 20 + BUTTON_PANEL_WIDTH, 10, width, height );
        buttonPanel.setBounds ( 10, 10, BUTTON_PANEL_WIDTH,
            BUTTON_PANEL_HEIGHT );
        frameWidth = width + i.left + i.right + BUTTON_PANEL_WIDTH + 30;
        if (height < BUTTON_PANEL_HEIGHT ){
            frameHeight = BUTTON_PANEL_HEIGHT + i.top + i.bottom + 20;
        } else
            frameHeight = height + i.top + i.bottom+ 20;
        setBounds (0, 0 , frameWidth ,frameHeight );
        setSize( frameWidth, frameHeight );
        validate();
        repaint();
    }

   private void saveParams()
   {
      PrintWriter fractWriter;
      // get/path/filename
      String filename="";
      boolean cancelled = false;
      try {
          filename = 
          JOptionPane.showInputDialog(this,
          "Enter full-file-path\\filename\n'.params' added automatically",
           APP_TITLE,3);
           if ( filename == null ) cancelled = true;
       } catch (Exception ex) {}
       if (!cancelled) {
                   try {
            fractWriter = 
                new PrintWriter(new FileWriter(filename));

         String temp = "FractView 1_498 parameters:" +
         "\nwidth:\n" + width + "\nheight:\n" + height  +"\nfx:\n" + fx +
         "\nfy:\n" + fy + "\nfz:\n" + fz + "\niterations:\n"+ numIter+ 
         "\ncolorrepeats:\n" + colorRepeats + "\ncolormode:\n"+ colorMode +
         "\nfpower:\n" + fPower + "\nismandlebrot:\n" + isMandlebrot +
         "\njuliaX:\n" + juliaX + "\njuliaY:\n" + juliaY;

         fractWriter.println(temp);
         fractWriter.close();
           } catch (Exception ex) {}

        }    
    }
    //drawFractal() Creates and places the Fractal for the first time
    //adds event listeners, sets window icon. Can't remove and replace easily
    //with action listeners, so Fractal redraws itself from here on in
    private void drawFractal()
    {
        myFractal = new Fractal ( width, height, fx, fy, fz, colorSet, numIter,
                                colorRepeats, colorMode, fPower, 
                                isMandlebrot, juliaX, juliaY, 
                                useSpeedPass, this );         
        myFractal.generateImage();
        fractalPanel.add( myFractal, BorderLayout.CENTER);
        setIconImage(myFractal.getImage());
        setTitle (APP_TITLE);
        myFractal.addMouseListener ( this );
        myFractal.addMouseMotionListener ( this );
    }
   
    //redrawFractal used for zooming or changing iterations when only
    //a few params change
     private void redrawFractal()
    {
        myFractal.redraw(width, height, fx, fy, numIter, fz); 
        fractalPanel.setSize (new Dimension(width, height) );
        fractalPanel.add( myFractal, BorderLayout.CENTER);
        setIconImage(myFractal.getImage());
        setTitle (APP_TITLE);
    }

    /**
     * getFrame() lets the Fractal update the window title with 'percent done'
     * will switch to a progress bar dialog soon
     * @returns the JFrame that this window is
     */
    
    public Object getFrame()
    {
        return this;
    }

    // all button events
    public void actionPerformed ( ActionEvent e ){
        if ( e.getSource() == zoomButton  && !selectJuliaMode ){
            //toggle zoom mode off and on
            if ( !zoom ){
                zoomButton.setLabel( "Toggle Zoom: On" );
            } else  zoomButton.setLabel ( "Toggle Zoom: Off");
            zoom = !zoom;            
        } else 
        //***********
        if ( e.getSource() == zoomTypeButton ){
            //toggle corner to center zoom or back
            if ( !cornerZoom ){
                zoomTypeButton.setLabel( "Zoom Type: Corner" );
            } else 
            { 
                zoomTypeButton.setLabel ( "Zoom Type: Center");
            }
            cornerZoom = !cornerZoom;            
        } else 
        //***********
        if ( e.getSource() == juliaButton ) {
             if (!selectJuliaMode) //pick a point on the Mandlebrot set to get Julia constant 
             {
                juliaButton.setLabel("Cancel Julia Pick"); //give user a cancel out
                oldZoom = zoom; //remember zoom state while zoom is disabled
                zoom = false;   //disable zoom
                zoomButton.setLabel ( "Toggle Zoom: Off");
                // Sniper Time!!
                Cursor crossHair = new Cursor(Cursor.CROSSHAIR_CURSOR); 
                myFractal.setCursor(crossHair);
                selectJuliaMode = true;
             } else 
             {
                 //user cancelled Julia Pick
                 selectJuliaMode = false;
                 juliaButton.setLabel( "Switch to Julia" );
                 zoom = oldZoom; //restore old zoom state
                 if (zoom)  zoomButton.setLabel ( "Toggle Zoom: On");
                 // restore cursor to arrow
                 Cursor defCursor = new Cursor(Cursor.DEFAULT_CURSOR);  
                 myFractal.setCursor(defCursor);      //take off the crosshairs
             }
         } else
         //***********
         if ( e.getSource() == mandButton ) {
            //restore Mandlebrot set to default, colors reset
            //fix aspect to square,default size, zoom box from corner
            
            // first things first: check for juliaSelect state and if so restore zoom state
            if ( selectJuliaMode ) {

                 //reset cancelled Julia Pick
                 selectJuliaMode = false;
                 juliaButton.setLabel( "Switch to Julia" );
                 zoom = oldZoom; //restore old zoom state
                 if (zoom)  zoomButton.setLabel ( "Toggle Zoom: On");
             }
            
            aspectFixed = true;                                
            cornerZoom = true;
            zoomTypeButton.setLabel( "Zoom Type: Corner" );
            fixedWidth = DEFAULT_WIDTH_SQUARE;
            fixedHeight = DEFAULT_HEIGHT_SQUARE;
            //default initialization of Fractal parameters
            // Mandlebrot centered on origin, zoom 1
            fz = 1;
            width = fixedWidth;
            height = fixedHeight;
            aspect = (double)width/(double)height;
            fx = 0;    //origin of Gaussian plane
            fy = 0;    // ''    ''  ''        ''
            numIter = DEFAULT_ITERS;
            colorRepeats = 1; // no color repeats
            colorMode = 1;  //no 'tweaks' on the colorset
            //fPower = 2;     // dont reset power
            juliaX = 0;     // no Julia yet
            juliaY = 0;
            isMandlebrot = true;
            useSpeedPass = false;
            palette = 1;    //full spectrum
            juliaButton.setEnabled(true);
            isMandlebrot = true;             
            myFractal.setMand ( 0, 0, fz, width, height, numIter);
            repack();
         } else
         //***********
         if ( e.getSource() == speedButton ) {
             //toggle speed pass (only faster where large areas of adjacent pixels
             //are the same color) Could be slower in some cases. For Next Zoom
             useSpeedPass = !useSpeedPass;
             if (useSpeedPass) 
                speedButton.setLabel("Speed Pass: On");
             else
                speedButton.setLabel("Speed Pass: Off");
             myFractal.setSpeedPass( useSpeedPass );
         } else   
         //***********
         if ( e.getSource() == powerButton ) {
             //set power to raise to ^
             int p=0;
             boolean cancelled = false;
             do {
                 try {                         

                     String result = JOptionPane.showInputDialog(this,
                     "Enter Fractal Power (Integer 2-100)",
                     APP_TITLE,3);                         
                     if ( result == null ) cancelled = true;
                     p = Integer.parseInt(result);
                 } catch (Exception ex) {}
             } while ((p<2 || p>100) && !cancelled);
             if (!cancelled) {
                fx = 0;
                fy = 0;
                fz = 1;
                fPower = p;
                juliaX = 0;
                juliaY = 0;
                //always start from Mandlebrot set
                isMandlebrot = true;
                selectJuliaMode = false;
                juliaButton.setEnabled(true);
                height= DEFAULT_HEIGHT_SQUARE;
                width = DEFAULT_WIDTH_SQUARE;
                myFractal.setPower( p, height, width );
             }
         } else
         //**********
         if ( e.getSource() == iterationButton ) {
             //set Iterations
             int iters=0;
             boolean cancelled = false;
             do {
                 try {
                     String result = 
                     JOptionPane.showInputDialog(this,"Enter # of Iterations (Int. 1-100000)",
                        APP_TITLE,3);
                     if ( result == null ) cancelled = true;
                     iters = Integer.parseInt((String)result);
                 } catch (Exception ex) {}
             } while ((iters < 1 || iters > 100000) && !cancelled);                      
             if (!cancelled) {
                 numIter = iters;
                 myFractal.setIterations(iters);
                 repaint();
             }
         } else
         //*********
         if ( e.getSource() == pixelSchemeButton ) {
             //set pixel drawing scheme
             int ps=0;
             String temp = "Enter Pixel Color Scheme:\n" +
                     "ITERATION BASED = 0\n"  + "PIXEL_MAGNITUDE_BASED = 1\n" +
                     "PIXEL COS BASED = 2\n" + "PIXEL_SIN_BASED = 3\n" + 
                     "PIXEL MAG SHIFT ANGLE BASED = 4\n"+ "PIXEL ITER SHIFT ANGLE BASED = 5\n" +  
                     "PIXEL_ANGLE_BASED = 6\n" + "PIXEL MAG SHIFT COS BASED = 7\n" +
                     "PIXEL MAG SHIFT SIN BASED = 8\n" + "PIXEL_ITER_SHIFT_COS_BASED = 9\n" +
                     "PIXEL_ITER_SHIFT_SIN_BASED = 10";
             boolean cancelled = false;
             do {
                 try {
                     String result = 
                     JOptionPane.showInputDialog(this,temp,
                        APP_TITLE,3);
                     if ( result == null ) cancelled = true;
                     ps = Integer.parseInt((String)result);
                 } catch (Exception ex) {}
             } while (ps < 0 && !cancelled);                      
             if (!cancelled) {
                 //numIter = iters;
                 myFractal.setPixelScheme(ps);
                 repaint();
             }
         } else
         //*********
         if ( e.getSource() == innerColorButton ) {
                 myFractal.toggleInnerColors();
                 repaint();
         } else
         //***********
         if ( e.getSource() == sizeButton ) {
            //resize the current Fractal view, preserving aspect
            double m=0;
            String result= "";
            boolean cancelled = false;
            do 
            {
               try 
               {                         
                   String text = "Current Size = "+ width +" x " + height +
                   "\nEnter multiplier (0.1-10000):" ;
                   result = JOptionPane.showInputDialog(this,
                   text, APP_TITLE,3);                         
                   if ( result == null ) cancelled = true;
                   m =(new Double(result)).doubleValue();

               } catch (Exception ex) {}
            } while ( (m<0.1 || m>10000 ) && !cancelled);
            if (!cancelled) {
                height = (int)(height*m);
                width = (int)(width*m);
                if ( aspectFixed ) {
                    fixedHeight = height; //set as new fixed aspect
                    fixedWidth = width;
                }
                redrawFractal();
                repack();
            }

         } else
         //*********
         if ( e.getSource() == aspectButton ) {
             // gosub to zoom box aspect ratio settings
             setAspect();
         } else
         //**********           
         if ( e.getSource() == colorButton ) {
             boolean cancelled = false; 
             int mode=0;
             do {
                 try {
                     //REM**this whole function needs to be handled as a pugin collection
                     //and something like the Gradient Manger (or maybe the Gradient 
                     //manager itself
                     String temp = "Enter Color Mode Dwell Shift (iteration band coloring)" +
                     " :\n#1. none (linear)\n" +
                     "#2. 1/4 sine cycle\n#3. hyperbolic section 1/8x\n" +
                     "#4. hyperbolic section 1/16x\n#5. square root\n#6. 1/2 sine cycle\n" +
                     "#7. full sine cycle\n#8.  full cosine cycle" ;
                     String result = JOptionPane.showInputDialog (this, temp, APP_TITLE, 3);
                     if (result == null) cancelled = true;
                     mode = Integer.parseInt(result);                     
                 } catch (Exception ex) {}
             } while ((mode < 1 || mode > 8) && !cancelled);                      
             colorMode = mode;
             if (!cancelled) {
                 int repeats = 0;
                 do {
                     try {
                         String result = JOptionPane.showInputDialog(this,
                         "Enter # of color repeats (1 -"+ numIter+ ")\n" +
                         "(Number of times the colors repeat over dwell bands)", APP_TITLE,3);
                         if (result == null) cancelled = true;  
                         repeats = Integer.parseInt(result);
                     } catch (Exception ex) {}
                 } while ( (repeats<1 || repeats>numIter) && !cancelled );
                 if (!cancelled) {
                    colorRepeats = repeats;
                    myFractal.changeColors( colorSet, colorMode, colorRepeats);
                 }
             }    
             
          } else
          if ( e.getSource() == cycleUpButton ) {
            //roll color array two steps to the right, makes colors move "up" in the fractal
            cycleUp ( 1, 1 );
         } else
         if ( e.getSource() == cycleDownButton ) {
            //roll color array two steps left
            cycleDown ( 1, 1 );
         } else
         if ( e.getSource() == paletteButton ) {
             //pick a colorset from the Gradient Managers list
             boolean cancelled = false;
             int temp = 0;
             do {
                 try {
                     String result = 
                     JOptionPane.showInputDialog(this,
                        "Enter Palette#:\n" + grManager.getDescriptions(),
                        APP_TITLE,3);
                     if ( result == null ) cancelled = true;
                     temp = Integer.parseInt((String)result);
                 } catch (Exception ex) {}
             } while ( ( temp < 1 || temp > grManager.getSize() ) && !cancelled);                      
             if (!cancelled) {
                 palette = temp;
                 colorSet = grManager.getGradient(palette);
                 myFractal.changeColors( colorSet, 1, 1 );                                          
                 repaint();
             }   
         } 
         
        else
         if ( e.getSource() == saveAnimButton ) {
             // save image, need to add params and iteration array too
             boolean cancelled = false;
             boolean cyclePos = true;
             String filename = "";
             int colorStep = 0;
             int totalSteps = 0;
             do {
                 // get/path/filename
                 try {
                     filename = 
                     JOptionPane.showInputDialog(this,
                        "Enter full-file-path\\filename\n'.png' added automatically",
                        APP_TITLE,3);
                     if ( filename == null ) cancelled = true;
                 } catch (Exception ex) {}
             } while (!cancelled && (filename == null || filename == "") );                      

 
             if (!cancelled) {
                     try {
                        // get the number of steps the color cycles each frame
                        String temp = "Enter Number of Steps per frame (negative numbers cycle down):";
                        String result = JOptionPane.showInputDialog (this, temp, APP_TITLE, 3);
                        if (result == null) cancelled = true;
                        colorStep = Integer.parseInt(result);                     
                    } catch (Exception ex) {}
             if (colorStep < 0 ) {
                 // if color negative cycle down
                 colorStep = Math.abs(colorStep);
                 cyclePos = false;
             }
             if (colorStep == 0 ) cancelled = true;
             if (!cancelled) {
                     try {
                         String result = JOptionPane.showInputDialog(this,
                         "Enter total number of frames in animation:", APP_TITLE,3);
                         if (result == null) cancelled = true;  
                         totalSteps = Integer.parseInt(result);
                     } catch (Exception ex) {}
             if (totalSteps==0) cancelled=true;
             
             if (!cancelled) {
                 for ( int i = 0 ; i < totalSteps; i++ ) {
                     if ( cyclePos ) {
                         //roll color array two steps to the right, makes colors move "up" in the fractal
                         cycleUp ( colorStep, 1 );
                        } 
                        else {
                            cycleDown ( colorStep, 1 );
                        }
                        try {
                            //create image buffer for outstream to disk
                            BufferedImage bf = new BufferedImage(width, height,
                            BufferedImage.TYPE_INT_RGB);
                            //load raw pixel array from Fractal into buffer 
                            bf.setRGB(0,0,width,height,myFractal.getPix(),0, width ); 
                            File FractPic = new File(filename + Integer.toString(i) +".png");
                            ImageIO.write(bf, "png", FractPic );  //thanks for the encoder Java
                        } catch ( Exception ex ) {System.out.println(ex); }//add something here :0     
                  }
          
             }
        }
    }
    }
    else
         if ( e.getSource() == customDuotoneButton ) {
             // gosub to zoom box aspect ratio settings
             grManager.addCustomDuotone(this);
    }else 
         if ( e.getSource() == customGradientButton ) {
             // gosub to zoom box aspect ratio settings
             grManager.addCustomGradient(this);
    }else 
    if ( e.getSource() == saveParamsButton ) {
             // gosub to zoom box aspect ratio settings
             saveParams();
    }else 
         if ( e.getSource() == saveButton ) {
             // save image, need to add params and iteration array too
             boolean cancelled = false;
             String temp = "";
             do {
                 try {
                     String result = 
                     JOptionPane.showInputDialog(this,
                        "Enter full-file-path\\filename\n'.png' added automatically",
                        APP_TITLE,3);
                     if ( result == null ) cancelled = true;
                     temp = result;
                     System.out.println(temp);
                 } catch (Exception ex) {}
             } while (!cancelled && (temp == null || temp == "") );                      
             if (!cancelled) {                     
                 try {
                     //create image buffer for outstream to disk
                     BufferedImage bf = new BufferedImage(width, height,
                        BufferedImage.TYPE_INT_RGB);
                     //load raw pixel array from Fractal into buffer 
                     bf.setRGB(0,0,width,height,myFractal.getPix(),0, width ); 
                     File FractPic = new File(temp + ".png");
                     ImageIO.write(bf, "png", FractPic );  //thanks for the encoder Java
                 } catch ( Exception ex ) {System.out.println(ex); }//add something here :0     
             }             
         }
}
    // set up for animating color cycling, fixed to one frame at a time at button press
    // until issues can be resolved (image.animate() method in AWT looks promising)
    private void cycleUp ( int step, int num ) {
        for ( int i = 0; i < num ; i++ ) {                
            myFractal.cycleColorsUp ( step );
            repaint();
        }
    }  
    
    private void cycleDown ( int step, int num ) {
        for ( int i = 0; i < num ; i++ ) {                                        
            myFractal.cycleColorsDown ( step );             
            repaint(); 
        }
    } 
    
    //"almost" defaults all params need to iron out control structure with Fractal
    private void resetFractal(){
         fx = 0;
         fy = 0;
         fz = 1;
         juliaX = 0;
         juliaY = 0;
         isMandlebrot = true;
         height = DEFAULT_HEIGHT_SQUARE;
         width = DEFAULT_WIDTH_SQUARE;
         numIter = DEFAULT_ITERS;
         redrawFractal(); 
    }

    private void getHelp(){} //!!LOL NO HELP HERE> ALL ARE FORESAKEN

    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseMoved (MouseEvent e) {}

    //catch the mouse
    public void mousePressed( MouseEvent e )
    {
        //only zoom mode
        if (zoom) {
            rootX = e.getX();                           //store mousedown x,y in instance var
            rootY = e.getY();
            boundBox.setLocation( new Point (rootX, rootY) ); //set box
            boundBox.setSize( new Dimension (0, 0) );         //invisible
            fractalPanel.add ( boundBox, 0 );  
            fractalPanel.repaint();
            repaint();           
        } 
    }
        
    public void mouseDragged( MouseEvent e ) 
    {
        //no zoom, no boom
        if (zoom) {
            // feeling anamorphic today??
            if (aspectFixed) {
                // no? ok :)
                // whats our fixed aspect?
                double presetAspect = (double)fixedWidth/(double)fixedHeight;
                int newX = e.getX();
                int newY = e.getY();
                // keep newX, newY in window
                if (newX < 1) newX = 1;
                if (newX > width-1) newX = width-1 ;
                if (newY < 1) newY = 1;
                if (newY > height-1 ) newY = height-1 ; 
                //this catches the mouse boundary across origin but not aspect forced issues
                //NEED A GOOD SCHEMA FOR ASPECT/CONTRAINT CLIPPING ISSUES CENTER ZOOM
                if (!cornerZoom)
                {
                    if ( (rootX-newX+1) > (width-rootX) ) newX = 2*rootX - width + 1;
                    if ( newX > 2*rootX - 1 ) newX = 2*rootX - 1;
                    if ( (rootY-newY+1) > (height-rootY) ) newY = 2*rootY - height + 1;
                    if (newY > 2*rootY -1 ) newY = 2*rootY - 1;
                }
                // box must always stay between the tip of the mouse arrow and the root point
                int newSize = Math.max( Math.abs(rootX - newX),
                                    Math.abs(rootY - newY) );
                boundBox.setSize( new Dimension (0, 0) );
                //zoom is determined by the largest side of the parent image,
                //set to the largest side of child image (had to pick some method)
                if (presetAspect>=1) {

                    //"landscape" type aspect, or square
                    if (newX < rootX)                 //moved to the left of rootX?? 
                        newX = rootX-newSize;         //keep box bewteen cursor and root
                    else
                        newX = rootX;                 //rootX is the uppper left corner
                    if (newY < rootY)                 //moved above rootY??
                        newY = rootY-(int)(newSize/presetAspect);  //like above
                    else
                    newY = rootY;                     //rootY is the ULy
                    //force aspect ratio
                    boundBox.setSize( new Dimension
                                     (newSize, (int)(newSize/presetAspect) ) );
                } else                                
                {
                    //analogous to above for "portrait" type aspect    
                    if (newX < rootX) 
                        newX = rootX-(int)(newSize*presetAspect);
                    else
                        newX = rootX;
                    if (newY < rootY) 
                        newY = rootY-newSize;
                    else
                        newY = rootY;
                    boundBox.setSize( new Dimension 
                                      ( (int)(newSize*presetAspect), newSize) );
                }                    
                if (!cornerZoom) {
                    // alterations for center zoom
                    // just quadruple corner zoom box (double size) centered on root pt.
                    if (presetAspect>=1) {
                        if (newX == rootX) newX -= newSize;
                        if (newY == rootY) newY -= (int)(newSize/presetAspect);
                        boundBox.setSize ( new Dimension
                                      ( 2*newSize, 2*(int)(newSize/presetAspect) ) );
                    } else {
                        if (newX == rootX) newX -= (int)(newSize*presetAspect);
                        if (newY == rootY) newY -= newSize;
                        boundBox.setSize ( new Dimension
                                      ( (int)(2*newSize*presetAspect),2*newSize) );                    
                    }          
                }
                boundBox.setLocation( new Point (newX, newY) );                    
                // clip zoom box if necessary
                clipZoomBox(newX,newY,presetAspect);
                fractalPanel.repaint();
            } else 

            // oh, we are anamorphic today
            {           
                // like above but no fixing
                int newX = e.getX();
                int newY = e.getY();
                if (newX < 1) newX = 1;
                if (newX > width-1) newX = width-1;
                if (newY < 1) newY = 1;
                if (newY > height-1) newY = height-1;
                if (!cornerZoom)
                {
                    if ( (rootX-newX+1) > (width-rootX) ) newX = 2*rootX - width + 1;
                    if ( newX > 2*rootX - 1 ) newX = 2*rootX - 1;
                    if ( (rootY-newY+1) > (height-rootY) ) newY = 2*rootY - height + 1;
                    if (newY > 2*rootY -1 ) newY = 2*rootY - 1;
                }
                int newWidth = Math.abs (rootX - newX );
                int newHeight = Math.abs (rootY - newY );
                boundBox.setSize( new Dimension (0, 0) );

                //swap for rootX/Y if neccessary
                if (newX > rootX)  newX = rootX;
                if (newY > rootY)  newY = rootY;

                if (!cornerZoom) {
                    // alterations for center zoom
                    if (newX == rootX) newX -= newWidth;
                    if (newY == rootY) newY -= newHeight;
                    newWidth += newWidth;
                    newHeight += newHeight;                    
                }
                boundBox.setLocation( new Point (newX, newY) );
                boundBox.setSize( new Dimension (newWidth, newHeight) );
                fractalPanel.repaint();
            }

        }
    }
    
    public void mouseReleased( MouseEvent e )
    {        
        if(selectJuliaMode) 
        //are we picking a julia set or zooming??
        {
            int setX = e.getX();
            int setY = e.getY();
            //System.out.println( "X = " + setX + " Y = " + setY );
            Cursor defCursor = new Cursor(Cursor.DEFAULT_CURSOR);  
            myFractal.setCursor(defCursor);                     //take off the crosshairs
            juliaButton.setEnabled(false);                      //disable to Mand reset
            juliaButton.setLabel( "Switch to Julia" );          //fix "cancel" text  
            isMandlebrot = false;
            //have to adjust for aspect
            if (aspect>=1) 
            {                
                juliaX = fx + ( setX - (width/2.0) ) * (4.0/width) / fz;            
                //System.out.println("JuliaX = "+ juliaX );
                juliaY = fy - ( setY - (height/2.0) ) * (4.0/width) / fz;
                //System.out.println("JuliaY = " + juliaY );
            } else 
            {
                juliaX = fx + ( setX - (width/2.0) ) * (4.0/height) / fz;
                //System.out.println("JuliaX = "+ juliaX );
                juliaY = fy - ( setY - (height/2.0) ) * (4.0/height) / fz;
                //System.out.println("JuliaY = "+ juliaY );
            }                
            height = DEFAULT_HEIGHT_SQUARE;
            width = DEFAULT_WIDTH_SQUARE;
            fx = 0;
            fy = 0;
            fz = 1;
            myFractal.setJulia( juliaX, juliaY, 0, 0, 1, DEFAULT_HEIGHT_SQUARE,
                DEFAULT_WIDTH_SQUARE, DEFAULT_ITERS);
            selectJuliaMode = false;
            zoom = oldZoom;                       //restore zoom state
            if (zoom)  zoomButton.setLabel ( "Toggle Zoom: On"); 
            repack();                                
        }
        else 
        {
            //Zoom Time!!
            if (zoom && boundBox.getSize().width > 2 &&
                boundBox.getSize().height > 2) 
            // if you can't see a pixel of color why zoom??
            {
                //update fractal parameters
                int boxWidth = boundBox.getSize().width;
                int boxHeight = boundBox.getSize().height;
                //System.out.println("Box width = " + boxWidth);
                //System.out.println("Box height = " + boxHeight);
                double bbCenterX = boundBox.getLocation().x + (boxWidth/2.0);
                double bbCenterY = boundBox.getLocation().y + (boxHeight/2.0);
                //System.out.println("boundBox corner X = " + boundBox.getLocation().x);
                //System.out.println("boundBox corner Y = " + boundBox.getLocation().y); 
                //System.out.println("boundBox centerX = " + bbCenterX);
                //System.out.println("boundBox centerY = " + bbCenterY);            
                double newAspect;
                if (aspectFixed) 
                    newAspect = (double)fixedWidth/(double)fixedHeight;
                else
                    //free zoom: zoom box determines aspect
                    newAspect = (double)boxWidth/(double)boxHeight;
                if (aspect>=1) 
                {                
                    //parent zoom fixed to width
                    //use width for transform
                    fx += ( bbCenterX - (width/2.0) ) * (4.0/width) / fz;            
                    fy -= ( bbCenterY - (height/2.0) ) * (4.0/width) / fz;
                    //use widest side of child image to fix child zoom
                    if (newAspect>=1)                 
                        fz *=(double) width /(double)(boxWidth) ;                                    
                    else
                        fz *= (double)width/(double)(boxHeight);                   
                } else 
                {
                    //parent aspect <1
                    //parent zoom fixed to height
                    //use height for transform
                    fx += ( bbCenterX - (width/2.0) ) * (4.0/height) / fz;
                    fy -= ( bbCenterY - (height/2.0) ) * (4.0/height) / fz;
                    //use widest side of child image to fix child zoom
                    if (newAspect>=1)
                        fz *= (double)height /(double) (boxWidth);
                    else 
                        fz *=  (double)height / (double)(boxHeight);   
                } 
                //System.out.println("FractView - fx = "+ fx );
                //System.out.println("Fractview : fy = " + fy ); 
                aspect = newAspect;
                if (aspectFixed) {
                    height = fixedHeight;
                    width = fixedWidth;
                    redrawFractal();
                    repack();
                } else 
                {
                    // ***********************************
                        if (boxWidth>=boxHeight) {
                            width = DEFAULT_HEIGHT_SQUARE;
                            height = (int)(DEFAULT_HEIGHT_SQUARE*boxHeight/boxWidth);
                        }
                        else
                        {
                            height = DEFAULT_HEIGHT_SQUARE;
                            width = (int)(DEFAULT_HEIGHT_SQUARE*boxWidth/boxHeight);                            
                        }
                        redrawFractal();
                        repack();
                    // ***********************************
                    // below replaces above and lets user custum select a multiplier * box size
                    /*double m=0;
                    String result= "";
                    boolean cancelled = false;
                    do 
                    {
                        try 
                        {                         
                            String text = "Zoom Box Size = "+ boxWidth +" x " + boxHeight + 
                            "\nEnter multiplier(0.1-10000):" ;
                            result = JOptionPane.showInputDialog(this,
                                       text, APP_TITLE,3);                         
                            if ( result == null ) cancelled = true;
                            m =(new Double(result)).doubleValue();
                        } catch (Exception ex) {}
                    } while ( (m<0.1 || m>10000 ) && !cancelled);
                    if (!cancelled) {
                        height = (int)(boxHeight*m);
                        width = (int)(boxWidth*m);
                        redrawFractal();
                        repack();
                    }*/
                } 
            }
        }
    }               
        
    private void repack(){

        //resize frame for changed fractal size
        fractalPanel.remove( boundBox);
        fractalPanel.remove( myFractal );           
        fractalPanel.setSize (new Dimension(width, height) );       
        Insets i = getInsets();
        fractalPanel.setBounds ( 20 + BUTTON_PANEL_WIDTH, 10, width, height );
        buttonPanel.setBounds ( 10, 10, BUTTON_PANEL_WIDTH,
            BUTTON_PANEL_HEIGHT );
        frameWidth = width + i.left + i.right + BUTTON_PANEL_WIDTH + 30;
        if (height < BUTTON_PANEL_HEIGHT )
           frameHeight = BUTTON_PANEL_HEIGHT + i.top + i.bottom + 20;
        else
           frameHeight = height + i.top + i.bottom+ 20;
        setBounds (getX(), getY() , frameWidth ,frameHeight );
        setSize( frameWidth, frameHeight );
        fractalPanel.add(myFractal);
        myFractal.repaint();
        validate();
        repaint();    
    }
    private void setAspect() {
        //ask user for aspect mode
        int a=1;
        boolean cancelled = false;
        do {
            try {                         
                String temp = "Enter Zoom Box Aspect Mode:\n" +
                              "#1: Fixed Square(400x400)\n#2: Fixed Landscape (400x300)\n" +
                              "#3: Fixed Letterbox (640x360)\n#4: Fixed Portrait (300x400)\n" +
                              "#5: Free Aspect (zoom box defines)\n#6: User Defined";
                String result = JOptionPane.showInputDialog(this, temp,
                                                            APP_TITLE,3);                         
                if ( result == null ) cancelled = true;
                a = Integer.parseInt(result);
            } catch (Exception ex) {}
        } while ((a<1 || a>6) && !cancelled);
        if (!cancelled) {
            switch (a) {
                case (ASPECT_MODE_SQUARE): {
                    fixedWidth = 400;
                    fixedHeight = 400;
                    aspectFixed = true;                    
                    break;
                }
                case (ASPECT_MODE_LANDSCAPE): {
                    fixedWidth = 400;
                    fixedHeight = 300;
                    aspectFixed = true;
                    break;
                }
                case (ASPECT_MODE_LETTERBOX): {
                    fixedWidth = 640;
                    fixedHeight = 360;
                    aspectFixed = true;
                    break;
                }                            
                case (ASPECT_MODE_PORTRAIT): {
                    fixedWidth = 300;
                    fixedHeight = 400;
                    aspectFixed = true;
                    break;
                }
                case (ASPECT_MODE_FREEASPECT): {
                    aspectFixed = false;
                    break;
                }
                case (ASPECT_USERDEF): {
                    int newWidth = 0;
                    int newHeight = 0;
                    do {
                        try {                         
                            String temp = "Enter New Fixed Width (pixels, 3-1,000,000)";
                            String result = JOptionPane.showInputDialog(this, temp,
                                                                        APP_TITLE,3);                         
                            if ( result == null ) cancelled = true;
                            newWidth = Integer.parseInt(result);
                        } catch (Exception ex) {}
                    } while ((newWidth<3 || newWidth>1000000) && !cancelled);
                    if (!cancelled) {
                        do {
                            try {                         
                                String temp = "Enter New Fixed Height (pixels, 3-1,000,000)";
                                String result = JOptionPane.showInputDialog(this, temp,
                                                                            APP_TITLE,3);                         
                                if ( result == null ) cancelled = true;
                                newHeight = Integer.parseInt(result);
                            } catch (Exception ex) {}
                        } while ((newHeight<3 || newHeight>1000000) && !cancelled);                       
                    }
                    if (!cancelled) {
                                fixedHeight = newHeight;
                                fixedWidth = newWidth;
                                aspectFixed = true;
                    }
                }
            }
        }
        //System.out.println ("fixedHeight = " + fixedHeight);
        //System.out.println ("fixedWidth = " + fixedWidth);
        //System.out.println ("aspectFixed = " + aspectFixed );
    }

    private void clipZoomBox(int newY, int newX, double presetAspect)
    {
        /*THIS IS BROKEN, VERY LITTLE TIME IN IT SO FAR
        //CONTRAINTS IN MOUSE_DRAGGED KEEP EVERYTHING WITHIN REASONBLE LIMITS
        //BUT SOME OF BOX MAY BE OFF THE COMPONENT AND INVISIBLE FROM
        //ASPECT FORCING

        //ok did some get forced off the window? need to trim it
        //four possible cases. newX and newY couldn't have caused it
        //so aspect forcing did. Now fix it
        int tempX = (int)boundBox.getBounds().getX();
        int tempY = (int)boundBox.getBounds().getY();
        int tempW = (int)boundBox.getSize().width;
        int tempH = (int)boundBox.getSize().height;
        // is x < 1
        if ( tempX < 1 ) 
        {
            // two cases, corner or center zoom
            // height has forced it 
            if (cornerZoom) 
            {
                //not too bad here
                if ( newY == rootY ) 
                {
                    //mouse is below root
                    boundBox.setBounds(1,rootY,rootX-1,(int)((rootX-1)/presetAspect));
                } 
                else
                {
                    //mouse is above root
                    boundBox.setBounds(1,rootY-(int)((rootX-1)/presetAspect)
                                        ,rootX-1,(int)((rootX-1)/presetAspect));
                }                            
            }
            else
            {
                //center zoom OH NO
                boundBox.setBounds (1, rootY-(int)((rootX-1)/presetAspect)
                                        ,2*(rootX-1),(int)(2*(rootX-1)/presetAspect));
            }                        
        }
        // ok thats it for x < 0 
        // now see if something got pushed off the right
        if ( tempX + tempW > width -1 ) 
        {
            // two cases, corner or center zoom
            // height has forced it 
            if (cornerZoom) 
            {
                //not too bad here
                if ( newY == rootY ) 
                {
                    //mouse is below root
                    boundBox.setBounds(rootX,rootY,(width-rootX-1),
                        (int)((width-rootX-1)/presetAspect));
                } 
                else
                {
                    //mouse is above root
                    boundBox.setBounds(rootX,rootY-(int)((width-rootX-1)/presetAspect)
                                        ,width-rootX-1,(int)((width-rootX-1)/presetAspect));
                }                            
            }
            else
            {
                //center zoom
                boundBox.setBounds (2*(rootX-width-1), 
                        rootY-(int)((width-rootX-1)/presetAspect),
                             2*(width-rootX-1),(int)(2*(width-rootX-1)/presetAspect));
            }
        }
        repaint();
        */
    }

    public void notifyMe() {
        cp.repaint();
        //try {
        //    Thread.sleep( 50 );
        //} catch ( Exception e ) {}
    }

    /**
     * getTitle lets Fractal reset proper title after showing % progress on Frame title
     *
     * @returns APP_TITLE the title of this application
     */

    public String getFrameTitle() {
        return APP_TITLE;
    }


    public static void main(String args[])   
    {
        FractViewer me = new FractViewer();
    }
}

import java.util.*;
import java.awt.*;
/**
 * Creates a custom color gradient, which shifts between different
 * color points.
 * 
 * @author David Kaplan
 * @copyright 2004 David Kaplan
 */
public class CustomGradient implements GradientCreator
{
    int r1,g1,b1,r2,g2,b2,rdif,gdif,bdif;
    int totalcolors=0;
    boolean mirror;
    ArrayList myparams;
    public String description = "CustomGradient ";
    public CustomGradient(ArrayList params, boolean mirror_grad, String name) {
        myparams = params;
        mirror = mirror_grad;
        description = description + name;
        
    
    }
    //while ( myparams.hasNext() ) {}
    public int [] getGradient() {
        int[] colorSet;
        // count the steps , array list in order: color, steps, color etc )
        //System.out.println("creating iterator" );
        Iterator it = myparams.iterator();
        Object o = it.next();
        Color c = (Color) o;
        //System.out.println("read in first color" );
        while ( it.hasNext () ) {

            o = it.next(); 
            //System.out.println("read in int as object" );
            String s = o.toString();
            //int steps = Integer.parseInt( o) ;
            int steps = Integer.parseInt( s );
            //System.out.println("steps" + steps );
            totalcolors += (steps-1);
            o = it.next();
            c = (Color) o;
        }
        totalcolors++; //the first interval has start and end color
        // the rest of the gradient steps overlap one color with the seg before
        //System.out.println("totalcolors" + totalcolors );      
        
        if (!mirror) {
            colorSet = new int [totalcolors];
        }
        else {
            colorSet = new int [2*totalcolors-1];
        }
        int arrayIndex = 0;
        Iterator it2 = myparams.iterator();
        Color firstcolor = (Color) it2.next() ;
        r1= firstcolor.getRed();
        g1= firstcolor.getGreen();
        b1= firstcolor.getBlue();
        while ( it2.hasNext () ) {
            o = it2.next(); 
            String s = o.toString();
            int steps = Integer.parseInt( s );
            //System.out.println("Taking steps to next color: " + steps );
            o = it2.next();
            Color secondcolor = (Color) o;
            r1= firstcolor.getRed();
            g1= firstcolor.getGreen();
            b1= firstcolor.getBlue();
            r2= secondcolor.getRed();
            g2= secondcolor.getGreen();
            b2= secondcolor.getBlue();
            rdif=r2-r1;
            gdif=g2-g1;
            bdif=b2-b1;
            //colorSet [0] =255<<24|r1<<16|g1<<8|b1;
            //arrayIndex ++;
            for (int i=0;i<steps;i++) {
           colorSet [arrayIndex] =(255 << 24) | ( (int) ( r1+rdif*i/(steps-1) ) << 16 ) |
           ( (int)( g1+gdif*i/(steps-1) ) << 8 ) | (int)(b1+bdif*i/(steps-1) );

           arrayIndex++;
           }
        firstcolor = secondcolor;
        arrayIndex--; // gets the overlap to avoid double colors
        }
      //System.out.println("Array Index ended at: " +arrayIndex);
      /** rdif=rd2-rd1;
        gdif=gn2-gn1;
        bdif=bL2-bL1; /*
        
       /** for (int i=0;i<totalcolors;i++) {
           colorSet [i] =(255 << 24) | ( (int) ( rd1+rdif*i/(steps-1) ) << 16 ) |
           ( (int)( gn1+gdif*i/(steps-1) ) << 8 ) | (int)(bL1+bdif*i/(steps-1) ); 
 */
           if (mirror) {
           for (int i=0;i<totalcolors-1;i++) {
            colorSet [i+totalcolors] = colorSet [totalcolors-i-2];
           }
        }

        return colorSet;  
    }
    public String getDescription() {
        return description;
    }
}

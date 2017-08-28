import java.awt.*;
/**
 * Creates a custom two-tone gradient.
 * 
 * @author David Kaplan
 * @copyright 2004 David Kaplan
 */
public class CustomDuotone implements GradientCreator
{
    int rd1,gn1,bL1,rd2,gn2,bL2,rdif,gdif,bdif,steps;
    boolean mirror;
    public String description = "CustomDuotone ";
    public CustomDuotone(Color color1,Color color2,int num_steps, boolean mirror_grad, String name) {
        rd1=color1.getRed();
        gn1=color1.getGreen();
        bL1=color1.getBlue();
        rd2=color2.getRed();
        gn2=color2.getGreen();
        bL2=color2.getBlue();
        steps=num_steps;
        mirror = mirror_grad;
        description = description + name;
        
    }
    public int [] getGradient() {
        int[] colorSet;
        if (!mirror) {
            colorSet = new int [steps];
        }
        else {
            colorSet = new int [2*steps-1];
        }
        rdif=rd2-rd1;
        gdif=gn2-gn1;
        bdif=bL2-bL1;
        
        for (int i=0;i<steps;i++) {
           colorSet [i] =(255 << 24) | ( (int) ( rd1+rdif*i/(steps-1) ) << 16 ) |
           ( (int)( gn1+gdif*i/(steps-1) ) << 8 ) | (int)(bL1+bdif*i/(steps-1) ); 
        }
        if (mirror) {
           for (int i=0;i<steps-1;i++) {
            colorSet [i+steps] = colorSet [steps-i-2];
           }
        }

        return colorSet;  
    }
    public String getDescription() {
        return description;
    }
}

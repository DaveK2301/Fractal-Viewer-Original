
/**
 * A grayscale gradient with 512 shades which cycles from black to 
 * white and then back.
 * 
 * @author David Kaplan
 * @copyright 2004 David Kaplan
 * @version 1_4991
 */


public class GrayscaleTwo implements GradientCreator
{

    public static final String DESCRIPTION = "GrayscaleTwo: Full Cycle Grayscale"; 

    public int [] getGradient() {
        int []colorSet = new int [512];
        for (int i=0; i<256; i++)
        {
            colorSet[i] =(255<<24)|(i<<16)|(i<<8)|i;
        }
        for (int i=255; i>=0; i--)
        {
            colorSet[511-i] =(255<<24)|(i<<16)|(i<<8)|i;
        }                
        return colorSet;
    }
    public String getDescription() {
        return DESCRIPTION;
    }
}

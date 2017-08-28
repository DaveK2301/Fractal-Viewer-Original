
/**
 * A grayscale gradient of 256 shades.
 * 
 * @author David Kaplan
 * @copyright 2004 David Kaplan
 */
public class GrayscaleOne implements GradientCreator
{
    public static final String DESCRIPTION = "GrayscaleOne: Half Cycle Grayscale";

	public int [] getGradient() {
        int [] colorSet = new int [256];
        for (int i=0; i<256; i++)
        {
           colorSet[i] =(255<<24)|(i<<16)|(i<<8)|i;
        }
		return colorSet;
	}
    public String getDescription() {
        return DESCRIPTION;
    }
}

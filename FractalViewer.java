import javax.swing.*;

/**
 * FractalViewer - supports neccessary communcation links
 * between a Fractal and it's viewing frame. Specifically,
 * by providing an instance of a JFrame for the fractal 
 * being computed to communicate progress on. If Fractal
 * detects that is has a non-Frame owner (any other Object type is permitted)
 * Fractal will throw it's own JFrame title bar to report real time 
 * calculations forced through Host API & GUI frame. Otherwise it uses 
 * the provided frame for real time progress report. NEED PROGRESS BAR DIALOG 
 * 
 * @author Dave Kaplan
 * @version 1.00
 * @copyright 2004 David Kaplan
 */

public interface FractalViewer 
{
    /**
     * getFrame() return the frame the fractal is being drawn on.
     * if null the fractal will throw up its own notification
     * window. This is to alert the user that computations are in
     * progress and that they should wait. In addition, the cycleUp
     * and cycleDown in Fractal only work if getFrame() returns a JFrame
     * 
     * @return The frame the Fractal is to be viewed upon.
     */
    
    public Object getFrame();

    public String getFrameTitle();
    
    public void notifyMe();
    
}

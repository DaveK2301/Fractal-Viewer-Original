
import java.awt.*;
import javax.swing.*;

/**
 * bBox is a rectangle
 * 
 * @author David Kaplan
 * @version 10-28-02
 * @copyright 2004 David Kaplan
 */
public class bBox extends JComponent
{

    /**
     * Constructor for objects of class Rectangle
     *
     * @param x The upper left x of this Rectangle.
     * @param y The upper left y of this Rectangle.
     * @param width The width of this Rectangle.
     * @param height The height of this Rectangle.
     * @param c The color of this Rectangle.
     */
    private int x, y, width, height; // location, size info
    private Color myColor; 

    public bBox( int x, int y, int width, int height, Color c)
    {
        super ();
        // initialize JComponent params
        setBounds ( x, y, width, height );
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        myColor = c;
    }


    /**
     * getLocation() 
     *
     * @return Upper left Point of this reactangle
     */

    public Point getLocation () {
        return new Point ( x, y );
    }

   
    public void setLocation ( Point p ) {
        this.x = p.x;
        this.y = p.y;
        setBounds ( x, y, width, height );
    }
    


    /**
     * setWidth() Set the width of the bounding box of this shape. 
     *
     * @param w The width of the bounding box of this shape.
     */

    public void setSize ( Dimension d ){
        width = d.width;
        height = d.height;
        setBounds ( x, y, width, height );
    }    
    

    /**
     * getWidth() Get the width of the bounding box of this shape. 
     *
     * @return  The width of the bounding box of this shape.
     */
    public Dimension getSize() {
        return new Dimension ( width , height );
    }

    /**
     * getColor() Get the color of this shape. 
     *
     * @return The color of this shape.
     */

    public Color getColor()
    {
        return myColor;
    }


    /**
     * setColor() Set the color of this shape. 
     *
     * @param The color to draw this shape in.
     */

    public void setColor( Color c)
    {
        myColor = c;
    }            

    /**
     * paintComponent overrides paint() in Component,
     * draws this shape onto JComponent.
     */   

    public void paintComponent ( Graphics g ) 
    {
        g.setColor ( myColor );
        g.drawRect ( 0, 0, width -1, height -1 ); // outline        
    }    
}

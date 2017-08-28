import javax.swing.colorchooser.*;
import java.util.*;
//import java.io.*;
import java.awt.*;
import javax.swing.*;
/**
 * Manages all the gradients for fractal viewer.
 * 
 * @author David Kaplan
 * @copyright 2004 David KaplanT 
 */
public class GradientManager
{
    private ArrayList myGradients;

    public GradientManager()
    {
        myGradients = new ArrayList();
        myGradients.add(new RainbowGradient() );
        myGradients.add(new GrayscaleOne() );
        myGradients.add(new GrayscaleTwo() );
    }
    public int[] getGradient ( int paletteNo ) {
        Iterator it = myGradients.iterator();
        Object o= null;
        for (int i=0; i < paletteNo; i++ )
        {
           o = it.next();
        }
        int [] palette = ( (GradientCreator)o ).getGradient();
        return palette;
    }

    public int getSize() {
        return myGradients.size();
    }
    public String getDescriptions() {
        String s= "";
        int counter = 1;
        Iterator it = myGradients.iterator();
        while ( it.hasNext() ) {
            GradientCreator g = (GradientCreator) (it.next()); 
            s = s + counter + ": " + g.getDescription() + "\n" ;
            counter++;
        }            
        return s;
    }
    
    public void addCustomDuotone(FractViewer owner) {
    Color color1= null;
    Color color2= null;
    int steps;
    steps = 0;
    Color pick1, pick2;
    JFrame myOwner = (JFrame) owner;
    boolean mirror= false;
    String name = "";
    boolean cancelled = false;
    // dialog for params:
                 do {
                 // get/path/filename
                 try {
                     String result = 
                     JOptionPane.showInputDialog(myOwner,
                        "Enter name for gradient (will be added to palettes):",
                        "Gradient Creation",3);
                     if ( result == null ) cancelled = true;
                     name = result;
                 } catch (Exception ex) {}
             } while (!cancelled && (name == null || name == "") );                      

        
         if (!cancelled) {
             color1 = JColorChooser.showDialog(
                     myOwner,
                     "Pick Gradient Start Color",
                     Color.WHITE);
             if (color1==null) {
                cancelled = true;
             } 
         }
         
             if (!cancelled) {
                color2= JColorChooser.showDialog(
                     myOwner,
                     "Pick Gradient End Color",
                     Color.WHITE);
             if (color2==null) {
                cancelled = true;
             } 
         }
      

          if (!cancelled) {
             int temp = -1;
                 do {
                 try {
                     String result = 
                     JOptionPane.showInputDialog(myOwner,
                        "Mirror? 0=false, 1=true:",
                        "Gradient Creation",3);
                     if ( result == null ) {
                         cancelled = true;
                     }
                     else { 
                       temp =  Integer.parseInt(result);
                    }
                 } catch (Exception ex) {}
             } while (!cancelled && temp !=0 && temp !=1  );
             if (temp == 1 ) mirror = true;
          }
    
                 if (!cancelled) {
                 do {
                 try {
                     String result = 
                     JOptionPane.showInputDialog(myOwner,
                        "Enter Number of steps in Gradient:",
                        "Gradient Creation",3);
                     if ( result == null ) {
                         cancelled = true;
                     }
                     else { 
                       steps =  Integer.parseInt(result);
                    }
                 } catch (Exception ex) {}
             } while (!cancelled && steps < 2 );
          }
          
          if (!cancelled) {
            // k got all the params made the gradient and add it to the list
            // should be able to save custom gradients
            CustomDuotone myD = new CustomDuotone(color1, color2,steps,mirror, name);
            myGradients.add ( myD );
        }
        }
        
    public void addCustomGradient(FractViewer owner) {
    
    ArrayList gradList= new ArrayList();
    JFrame myOwner = (JFrame) owner;
    boolean mirror= false;
    String name = "";
    boolean cancelled = false;
    // dialog for params:
        do {
           // get/path/filename
           try {
               String result = 
               JOptionPane.showInputDialog(myOwner,
                        "Enter name for gradient (will be added to palettes):",
                        "Gradient Creation",3);
               if ( result == null ) /*cancelled = true*/ return;
               name = result;
             } catch (Exception ex) {}
         } while (name == null || name == "");                      

         if (!cancelled) {
             Color addColor= JColorChooser.showDialog(
                     myOwner,
                     "Pick Gradient Start Color",
                     Color.WHITE);
             if (addColor==null) {
                cancelled = true;
             } 
             else {
                   gradList.add(addColor);
              }
         }

         int steps=0;
         Color nextcolor;
         do {
            // add steps to next color
                 try {
                     String result = 
                     JOptionPane.showInputDialog(myOwner,
                        "Enter Number of steps to next color, O to quit adding colors, cancel to cancel gradient",
                        "Gradient Creation",3);
                     if ( result == null ) {
                         cancelled = true;
                     }
                     else { 
                       steps =  Integer.parseInt(result);
                    }
                 } catch (Exception ex) {}
                 
                          if (!cancelled && steps > 0) {
             nextcolor = JColorChooser.showDialog(
                     myOwner,
                     "Pick Next Gradient Color, cancel to cancel gradient",
                     Color.WHITE);
             if (nextcolor==null) {
                cancelled = true;
             } 
             else {
                   if (steps > 0 ) {
                      gradList.add(steps);
                      gradList.add(nextcolor);

                   }
              }  
            }
          } while (!cancelled && steps > 0);
       

          if (!cancelled) {
             int temp = -1;
                 do {
                    try {
                         String result = 
                         JOptionPane.showInputDialog(myOwner,
                        "Mirror? 0=false, 1=true:",
                        "Gradient Creation",3);
                        if ( result == null ) {
                            cancelled = true;
                        }
                        else { 
                            temp =  Integer.parseInt(result);
                        }
                 } catch (Exception ex) {}
             } while (!cancelled && temp !=0 && temp !=1  );
             if (temp == 1 ) mirror = true;
          }
    
          // k got all the params made the gradient and add it to the list
          // should be able to save custom gradients
          CustomGradient mygrad = new CustomGradient(gradList,mirror, name);
          //System.out.println("Created new custom gradient");
          myGradients.add ( mygrad );
    }
 }
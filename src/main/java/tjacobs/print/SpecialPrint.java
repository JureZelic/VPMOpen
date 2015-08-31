/*
* Created on May 24, 2005 by @author Tom Jacobs
* You are free to use or modify this code, but please do not change the package. 
*/
package tjacobs.print;

import java.awt.Dimension;
import java.awt.Graphics;

public interface SpecialPrint
{
    public Dimension getPrintSize();
    public void printerPaint(Graphics g);
}
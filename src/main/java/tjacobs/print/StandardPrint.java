/*
* Created on May 24, 2005 by @author Tom Jacobs
* You are free to use or modify this code, but please do not change the package. 
*/
package tjacobs.print;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;

public class StandardPrint implements Printable, Pageable
{
    Component c;
    SpecialPrint sp;
    PageFormat mFormat;
    public StandardPrint(Component c)
    {
        this.c = c;
        if (c instanceof SpecialPrint)
        {
            sp = (SpecialPrint)c;
        }
    }
    
    public StandardPrint(SpecialPrint sp)
    {
        this.sp = sp;
    }
       
    public void start() throws PrinterException
    {
        PrinterJob job = PrinterJob.getPrinterJob();
        if (mFormat == null)
        {
            mFormat = job.defaultPage();
        }
        job.setPageable(this);
        if (job.printDialog())
        {
            job.print();
        }
    }

    public void setPageFormat (PageFormat pf)
    {
        mFormat = pf;
    }

    public void printStandardComponent (Pageable p) throws PrinterException
    {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPageable(p);
        job.print();
    }

    private Dimension getJobSize()
    {
        if (sp != null)
        {
            return sp.getPrintSize();
        }
        else
        {
            return c.getSize();
        }
    }

    public static Image preview (int width, int height, StandardPrint sp, int pageNo)
    {
        BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        return preview (im, sp, pageNo);
    }

    public static Image preview (Image im, StandardPrint sp, int pageNo)
    {
        Graphics2D g = (Graphics2D) im.getGraphics();
        PageFormat pf = sp.getPageFormat(pageNo);
        int width = im.getWidth(null);
        int height = im.getHeight(null);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        double hratio = height / pf.getHeight();
        double wratio = width / pf.getWidth();
        g.scale(hratio, wratio);
        sp.print(g, pf, pageNo);
        g.dispose();
        return im;
    }

    public int print(Graphics gr, PageFormat format, int pageNo)
    {
        mFormat = format;
        Graphics2D g = (Graphics2D) gr;
        g.translate((int)format.getImageableX(), (int)format.getImageableY());
        Dimension size = getJobSize();
        if (pageNo > getNumberOfPages())
        {
            return Printable.NO_SUCH_PAGE;
        }
        int horizontal = getNumHorizontalPages();
        int vertical = getNumVerticalPages();
        int horizontalOffset = (int) ((pageNo % horizontal) * format.getImageableWidth());
        int verticalOffset = (int) ((pageNo / vertical) * format.getImageableHeight());
        double ratio = getScreenRatio();
        g.scale(1 / ratio, 1 / ratio);
        g.translate(-horizontal, -vertical);
        if (sp != null)
        {
            sp.printerPaint(g);
        }
        else
        {
            c.paint(g);
        }
        g.translate(horizontal, vertical);
        g.scale(ratio, ratio);
        g.translate((int)-format.getImageableX(), (int)-format.getImageableY());
        return Printable.PAGE_EXISTS;
    }

    public int getNumHorizontalPages()
    {
        Dimension size = getJobSize();
        int imWidth = (int)mFormat.getImageableWidth();
        int pWidth = 1 + (int)(size.width / getScreenRatio() / imWidth) - (imWidth == size.width ? 1 : 0);
        return pWidth;
    }

    private double getScreenRatio ()
    {
        double res = Toolkit.getDefaultToolkit().getScreenResolution();
        double ratio = res / 72.0;
        return ratio;
    }

    public int getNumVerticalPages()
    {
        Dimension size = getJobSize();
        int imHeight = (int)mFormat.getImageableHeight();
        int pHeight = (int) (1 + (size.height / getScreenRatio() / imHeight)) - (imHeight == size.height ? 1 : 0);
        return pHeight;
    }

    public int getNumberOfPages()
    {
        return getNumHorizontalPages() * getNumVerticalPages();
    }

    public Printable getPrintable(int i)
    {
        return this;
    }

    public PageFormat getPageFormat(int page)
    {
        if (mFormat == null)
        {
            PrinterJob job = PrinterJob.getPrinterJob();
            mFormat = job.defaultPage();
        }
        return mFormat;
    }
}
//********************************************************************************
//
//    DiveCalc - Ideal Mix Calculator
//
//    Copyright (C) 2006-2008  Jurij Zelic - vpm.open@gmail.com
//
//    This program is free software; you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation; either version 2 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program; if not, write to the Free Software
//    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
//********************************************************************************
//    Revision history:
//        2006: J. Zelic - First Version
//  Feb.  2007: J. Zelic - 1.2.0 ICD warning 
//  Mar.  2007: J. Zelic - 1.2.1 "f02 bigger than 100%" bug crrection
//
//********************************************************************************
package vpm;

// GUI imports
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

//import java.math.*;

public class DiveCalc extends JFrame implements ActionListener
{
private double mod=70.0;
private double ppO2=1.4;
private double ead=35.0;
private boolean o2nrc=false;
private double fO2=0, fN2=0, fHe=0;
private double depthToPressFactor=10.1972; // 10 if meters 33.45, if feets 
private double depthFactor=1; // 1 if meters, 3.3 if feets
private String unitsString="m";
private JCheckBox o2nrcbox;
private JTextField fO2text, fN2text, fHetext;
private JTextField modtext, ppO2text, eadtext;
private JPanel outputpanell, inputpanell, basicpannel, o2nrcpanel;
private JButton okbutton, calculatebutton;

// class constructor
public DiveCalc()
{
    super("Dive Calculator");
            
    int xPos = this.getToolkit().getScreenSize().width/2-75;
    int yPos = this.getToolkit().getScreenSize().height/2-200; 
          
    this.setLocation(xPos, yPos);  
    
    basicpannel = new JPanel();
    basicpannel.setLayout(new GridLayout(4,1));
    add(basicpannel);

    // input fields
    inputpanell = new JPanel();
    basicpannel.add(inputpanell);
    
    inputpanell.add(new Label("  mod:"));    
    modtext = new JTextField(4);
    modtext.setEditable(true);
    modtext.setText(Double.toString(mod));
    modtext.addActionListener( this );
    inputpanell.add(modtext);

    inputpanell.add(new Label("  ppO2:"));        
    ppO2text = new JTextField(4);
    ppO2text.setEditable(true);
    ppO2text.setText(Double.toString(ppO2));
    ppO2text.addActionListener( this );
    inputpanell.add(ppO2text);

    inputpanell.add(new Label("  end:"));        
    eadtext = new JTextField(4);
    eadtext.setEditable(true);
    eadtext.setText(Double.toString(ead));
    eadtext.addActionListener( this );
    inputpanell.add(eadtext);

    // o2nrcpanel button
    o2nrcpanel = new JPanel();
    basicpannel.add(o2nrcpanel);
        
    ItemListener handler = new CheckBoxHandler();
    o2nrcbox=new JCheckBox("O2 narcotic", o2nrc);
    o2nrcbox.setMnemonic(KeyEvent.VK_O);
    o2nrcbox.addItemListener(handler);
    o2nrcpanel.add(o2nrcbox);

    calculate();

    // output fields
    outputpanell = new JPanel();
    basicpannel.add(outputpanell);  
    
    outputpanell.add(new Label("fO2:"));         
    fO2text = new JTextField(4);
    fO2text.setEditable(false);
    outputpanell.add(fO2text);

    outputpanell.add(new Label("  fHe:"));        
    fHetext = new JTextField(4);
    fHetext.setEditable(false);
    outputpanell.add(fHetext);

    outputpanell.add(new Label("  fN2:"));        
    fN2text = new JTextField(4);
    fN2text.setEditable(false);
    outputpanell.add(fN2text);
    
    // buttons
    JPanel buttonpannel=new JPanel();
    
    calculatebutton = new JButton();
    calculatebutton.setText("Calc");
    calculatebutton.setPreferredSize(new Dimension(80, 30));
    calculatebutton.addActionListener(this);
    buttonpannel.add(calculatebutton);
    
    okbutton = new JButton();
    okbutton.setText("OK");
    okbutton.setPreferredSize(new Dimension(80, 30));
    okbutton.addActionListener(this);
    buttonpannel.add(okbutton);
        
    basicpannel.add(buttonpannel); 
      
    updateValues();
    
    //
    pack();
    setSize(350, 200);
}

public void setMod(double newMod)
{
    if ((newMod>=6.0*depthFactor) & (newMod<=150.0*depthFactor))
    {
        mod=Math.rint(newMod*10)/10;
        calculate();
        updateValues();
    }
    modtext.setText(Double.toString(mod));
}

public double getMod()
{
    return mod;
}
    
public void setPpO2(double newPpO2)
{
    if ((newPpO2>=0.5) & (newPpO2<=1.6))
    {
        ppO2=Math.rint(newPpO2*100)/100;
        calculate();
        updateValues();
    }
    ppO2text.setText(Double.toString(ppO2));
}

public double getPpO2()
{
    return ppO2;
}

public void setEAD(double newEad)
{
    if ((newEad>=6.0*depthFactor) & (newEad<=50.0*depthFactor))
    {
        ead=Math.rint(newEad*10)/10;
        calculate();
        updateValues();
    }
    eadtext.setText(Double.toString(ead));
}

public double getEAD()
{
    return ead;
}

public void setO2nrc(boolean newO2nrc)
{
    o2nrc=newO2nrc;
    calculate();
    updateValues();
}

public boolean getO2nrc()
{
    return o2nrc;
}

public void setMeters(boolean meters)
{
    if (meters)
    {
        depthToPressFactor=10; 
        depthFactor=1;
        unitsString="m";
    }
    else
    {
        depthToPressFactor=100/3;
        depthFactor=3.3;
        unitsString="ft";
    }
    calculate();
    updateValues();
}

public boolean getMeters()
{
    if (depthFactor==1)
        return true;
    else
        return false;
}

public double getF02()
{
    return fO2;
}

public double getFN2()
{
    return fN2;
}

public double getFHe()
{
    return fHe;
}

private class CheckBoxHandler implements ItemListener
{
    public void itemStateChanged (ItemEvent e)
    {

        if (e.getSource() == o2nrcbox)
        {
            if (e.getStateChange() == ItemEvent.SELECTED)
                o2nrc=true;
            else
                o2nrc=false;
            calculate();
            updateValues();
    }
    }
}


public void actionPerformed(ActionEvent evt)
{
    String str=eadtext.getText();
    if (str.equals("  ----")==false)
        setEAD(Double.valueOf(str).doubleValue());
    setMod(Double.valueOf(modtext.getText()).doubleValue());
    setPpO2(Double.valueOf(ppO2text.getText()).doubleValue());
     
    calculate();
    updateValues();
    
    if (evt.getSource() == okbutton)
    	  this.setVisible(false);
}

private void calculate()
{
    double pres, nrcPres, nitroxead;

    pres=mod/depthToPressFactor+1;
    fO2=ppO2/pres;
    if (fO2>1)
        fO2=1;

    if (o2nrc)
    {
        nrcPres=ead/depthToPressFactor+1;
        fN2=nrcPres/pres-fO2;
    }
    else
    {
        nrcPres=(ead/depthToPressFactor+1)*0.79;
        fN2=nrcPres/pres;
    }    
        
    fO2=Math.floor(fO2*1000)/10;
    fN2=Math.rint(fN2*1000)/10;
    fHe=Math.rint((100-fO2-fN2)*10)/10;
    
    if (fHe<1)
    {
    	fN2=Math.rint((100-fO2)*10)/10;
    	fHe=0;

        if (o2nrc)
           nitroxead=mod;
        else
           nitroxead=Math.rint(((mod/depthToPressFactor+1)*fN2/79-1)*depthToPressFactor*10)/10;

        if (nitroxead>=0)        
            eadtext.setText(Double.toString(nitroxead));
        else
            eadtext.setText("  ----");
        eadtext.setEditable(false);   
    }
    else
    {
        eadtext.setEditable(true);
        eadtext.setText(Double.toString(ead));
    }

}

private void updateValues()
{
    fO2text.setText(Double.toString(fO2)+"%");
    fN2text.setText(Double.toString(fN2)+"%");
    fHetext.setText(Double.toString(fHe)+"%");
    o2nrcbox.setSelected(o2nrc);
}

}
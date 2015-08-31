//********************************************************************************
//
//    ConfigWindow - Handles the Configuration Window
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
//  Mar.  2007: j. Zelic - 1.2.1 LINUX adaptation
//  May.  2007: J. Zelic - 1.3.2 6m/20ft last deco stop
//
//********************************************************************************
package vpm;

// GUI imports
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ConfigWindow extends JFrame implements ActionListener
{
    public int conservatism=4;
    public String ascentRate="9.0", descentRate="18.0";
    public double rmvBottom=25, rmvDeco=18;
    public boolean lastStop6m20ft=false;
    public boolean icdWarning=false;
    final private double rmvMax=50, rmvMin=0.5;
    public double oxiWindow=3.0;
    final private double oxiWindowTextMax=10, oxiWindowTextMin=0;

    private double unitFactor=1;
    private double descentRateMin=5*unitFactor,
                   descentRateMax=50*unitFactor,
                   ascentRateMin=2*unitFactor,
                   ascentRateMax=20*unitFactor;     

    private JPanel panel, conservatismPannell, descentRatePanell, ascentRatePanell;
    private Checkbox cb0, cb1, cb2, cb3, cb4, cb5, cb6;
    private JCheckBox icdbox, lastStopbox;
    private JTextField descentText, ascentText, rmvBottomText, rmvDecoText, oxiWindowText;
    private JButton okbutton;

    ItemListener handler = new CheckBoxHandler();

//***************************************************************
//
//  CLASS CONSTRUCTOR - FORMAT CONFIG WINDOW
//
//***************************************************************
    public ConfigWindow()
    {
        super("Configuration");

        CheckboxGroup cbg;

        JPanel configpannel = new JPanel();
        configpannel.setLayout(new GridLayout(8,1));
        add(configpannel);

        // add konzervativism
        conservatismPannell = new JPanel();
        //conservatismPannell.setPreferredSize(new Dimension(330, 33));
        configpannel.add(conservatismPannell);

        conservatismPannell.add(new Label("Conservatism: "));
        cbg = new CheckboxGroup();

        conservatismPannell.add(cb0=new Checkbox("0", cbg, (conservatism==0)));
        cb0.addItemListener(handler);           
        conservatismPannell.add(cb1=new Checkbox("1", cbg, (conservatism==1)));
        cb1.addItemListener(handler);             
        conservatismPannell.add(cb2=new Checkbox("2", cbg, (conservatism==2)));
        cb2.addItemListener(handler);          
        conservatismPannell.add(cb3=new Checkbox("3", cbg, (conservatism==3)));
        cb3.addItemListener(handler);           
        conservatismPannell.add(cb4=new Checkbox("4", cbg, (conservatism==4)));
        cb4.addItemListener(handler);
        conservatismPannell.add(cb5=new Checkbox("5", cbg, (conservatism==5)));
        cb5.addItemListener(handler);                  
        conservatismPannell.add(cb6=new Checkbox("6", cbg, (conservatism==6)));
        cb6.addItemListener(handler);  
        
        // add descentRate
        descentRatePanell = new JPanel();
        configpannel.add(descentRatePanell);
        
        descentRatePanell.add(new Label("Descent Rate:"));        
        descentText = new JTextField(4);
        descentText.setEditable(true);
        descentText.setText(descentRate);
        descentText.addActionListener( this );
        descentRatePanell.add(descentText);
        
        // add ascentRate
        descentRatePanell.add(new Label("  Ascent Rate:"));
        ascentText = new JTextField(4);
        ascentText.setEditable(true);
        ascentText.setText(ascentRate);
        ascentText.addActionListener( this );
        descentRatePanell.add(ascentText);

        //RMV
        panel=new JPanel();
        configpannel.add(panel);
        panel.add(new Label("  RMV Bottom:"));
        rmvBottomText = new JTextField(4);
        rmvBottomText.setEditable(true);
        rmvBottomText.setText(""+rmvBottom);
        rmvBottomText.addActionListener( this );
        panel.add(rmvBottomText);

        panel.add(new Label("     RMV Deco:"));
        rmvDecoText = new JTextField(4);
        rmvDecoText.setEditable(true);
        rmvDecoText.setText(""+rmvDeco);
        rmvDecoText.addActionListener( this );
        panel.add(rmvDecoText);

        // oxiWindowText
        panel=new JPanel();
        configpannel.add(panel);
        panel.add(new Label("Minimum Gass Switch Stop Time"));
        oxiWindowText = new JTextField(2);
        oxiWindowText.setEditable(true);
        oxiWindowText.setText(""+Math.round(oxiWindow));
        oxiWindowText.addActionListener( this );
        panel.add(oxiWindowText);
        panel.add(new Label("min"));        

        panel=new JPanel();
        configpannel.add(panel);
        panel.add(new Label("Last Decompression Stop on 6m/20ft"));
        lastStopbox=new JCheckBox("",lastStop6m20ft);
        lastStopbox.addItemListener(handler);
        panel.add(lastStopbox);
    
        panel=new JPanel();
        configpannel.add(panel);
        panel.add(new Label("Isobaric Counter Diffusion (ICD) Warnings"));
        icdbox=new JCheckBox("",icdWarning);
        icdbox.addItemListener(handler);
        panel.add(icdbox);
    
        panel=new JPanel();
        configpannel.add(panel);

        panel=new JPanel();
        configpannel.add(panel);
        okbutton = new JButton();
        okbutton.setText("OK");
        okbutton.setPreferredSize(new Dimension(80, 30));
        okbutton.addActionListener(this);
        panel.add(okbutton);
                  
        pack();
        setSize(370, 350);
    }

//***************************************************************
//
//     SET PARAMETERS METHODES
//
//***************************************************************
public void setConservatism(int c)
{
	  conservatism=c;
	  switch (conservatism)
	  {
	  case 0:
	      cb0.setState(true);
	      break;
	  case 1:
	      cb1.setState(true);
	      break;
	  case 2:
	      cb2.setState(true);
	      break;
	  case 3:
	      cb3.setState(true);
	      break;
	  case 4:
	      cb4.setState(true);
	      break;
	  case 5:
	      cb5.setState(true);
	      break;	      	      
	  default:
	      cb6.setState(true);
	  }
}
public void setAscentRate(String rate)
{
	  ascentRate=rate;
	  ascentText.setText(ascentRate);
}
public void setDescentRate(String rate)
{
	  descentRate=rate;
	  descentText.setText(descentRate);
}
public void setRmvBottom(double rmv)
{
	  rmvBottom=rmv;
	  rmvBottomText.setText(""+rmvBottom);
}
public void setRmvDeco(double rmv)
{
	  rmvDeco=rmv;
	  rmvDecoText.setText(""+rmvDeco);
}
public void setOxiWindow(double Time)
{
	  oxiWindow=Time;
	  oxiWindowText.setText(""+Math.round(oxiWindow));
}

public void setlastStop6m20ft(boolean lststp)
{
	  lastStop6m20ft=lststp;
	  lastStopbox.setSelected(lastStop6m20ft);
}

public void setIcdWarning(boolean icdw)
{
	  icdWarning=icdw;
	  icdbox.setSelected(icdWarning);
}
//***************************************************************
//
//     CONSERVATISM CHECKBOX HANDLER
//
//***************************************************************
private class CheckBoxHandler implements ItemListener
{
    public void itemStateChanged (ItemEvent e)
    {
        if (e.getSource() == cb0)
            if (e.getStateChange() == ItemEvent.SELECTED)
                conservatism=0;
        if (e.getSource() == cb1)
            if (e.getStateChange() == ItemEvent.SELECTED)
                conservatism=1;
        if (e.getSource() == cb2)
            if (e.getStateChange() == ItemEvent.SELECTED)
                conservatism=2;
        if (e.getSource() == cb3)
            if (e.getStateChange() == ItemEvent.SELECTED)
                conservatism=3;
        if (e.getSource() == cb4)
            if (e.getStateChange() == ItemEvent.SELECTED)
                conservatism=4;
        if (e.getSource() == cb5)
            if (e.getStateChange() == ItemEvent.SELECTED)
                conservatism=5;
        if (e.getSource() == cb6)
            if (e.getStateChange() == ItemEvent.SELECTED)
                conservatism=6; 
        if (e.getSource() == lastStopbox)
        {
            if (e.getStateChange() == ItemEvent.SELECTED)
                lastStop6m20ft=true;
            else
                lastStop6m20ft=false;
        }                                     
        if (e.getSource() == icdbox)
        {
            if (e.getStateChange() == ItemEvent.SELECTED)
                icdWarning=true;
            else
                icdWarning=false;
        }                                               
    }
}

//***************************************************************
//
//     UPDATE TEXT FIELDS AND CHECK INPUT VALUES
//
//***************************************************************
private boolean updateTextFields()
{
    double tmp;

    tmp=Double.valueOf(descentText.getText()).doubleValue();
    if ((tmp<descentRateMin) || (tmp>descentRateMax))
    {
        MsgBox errorBox=new MsgBox();
        errorBox.clearMessage();
        errorBox.addMessageString("Descent Rate Must be "+Double.toString(descentRateMin)+
            " to "+Double.toString(descentRateMax));
        errorBox.setVisible(true);
        return false;
    }
    else
    {
        tmp=Math.rint(tmp*10)/10;
        descentRate=Double.toString(tmp);
    }
    descentText.setText(descentRate);

    tmp=Double.valueOf(ascentText.getText()).doubleValue();
    if ((tmp<ascentRateMin) || (tmp>ascentRateMax))
    {
        MsgBox errorBox=new MsgBox();
        errorBox.clearMessage();
        errorBox.addMessageString("Ascent Rate Must be "+Double.toString(ascentRateMin)+
            " to "+Double.toString(ascentRateMax));
        errorBox.setVisible(true);
        return false;
    }
    else
    {
        tmp=Math.rint(tmp*10)/10;
        ascentRate=Double.toString(tmp);
    }
    ascentText.setText(ascentRate);

    tmp=Double.valueOf(rmvBottomText.getText()).doubleValue();
    if ((tmp<rmvMin) || (tmp>rmvMax))
    {
        MsgBox errorBox=new MsgBox();
        errorBox.clearMessage();
        errorBox.addMessageString("RMV Must be "+Double.toString(rmvMin)+
            " to "+Double.toString(rmvMax));
        errorBox.setVisible(true);
        return false;
    }
    else
    {
        tmp=Math.rint(tmp*10)/10;
        rmvBottom=tmp;
    }
    rmvBottomText.setText(Double.toString(rmvBottom));

    tmp=Double.valueOf(rmvDecoText.getText()).doubleValue();
    if ((tmp<rmvMin) || (tmp>rmvMax))
    {
        MsgBox errorBox=new MsgBox();
        errorBox.clearMessage();
        errorBox.addMessageString("RMV Must be "+Double.toString(rmvMin)+
            " to "+Double.toString(rmvMax));
        errorBox.setVisible(true);
        return false;
    }
    else
    {
        tmp=Math.rint(tmp*10)/10;
        rmvDeco=tmp;
    }
    rmvDecoText.setText(Double.toString(rmvDeco));

    tmp=Double.valueOf(oxiWindowText.getText()).doubleValue();
    if ((tmp<oxiWindowTextMin) || (tmp>oxiWindowTextMax))
    {
        MsgBox errorBox=new MsgBox();
        errorBox.clearMessage();
        errorBox.addMessageString("Deco Switch Time Must be "+Double.toString(oxiWindowTextMin)+
            " to "+Double.toString(oxiWindowTextMax));
        errorBox.setVisible(true);
        return false;
    }
    else
    {
        tmp=Math.rint(tmp);
        oxiWindow=tmp;
    }
    oxiWindowText.setText(""+Math.round(oxiWindow));

    return true;

}

//***************************************************************
//
//     OK BUTTON AND TEXTBOX HANDLER
//
//***************************************************************
public void actionPerformed(ActionEvent evt)
{
    if (evt.getSource() == descentText)
        updateTextFields();
    if (evt.getSource() == ascentText)
        updateTextFields();
    if (evt.getSource() == rmvBottomText)
        updateTextFields();
    if (evt.getSource() == rmvDecoText)
        updateTextFields();
    if (evt.getSource() == oxiWindowText)
        updateTextFields();
    if (evt.getSource() == okbutton)
        if (updateTextFields())
            setVisible(false);
}

}
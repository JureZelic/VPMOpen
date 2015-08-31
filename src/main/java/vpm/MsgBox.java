//********************************************************************************
//
//    MsgBox - Error, Warning and Info Box
//
//    Copyright (C) 2006  Jurij Zelic - vpm.open@gmail.com
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
//
//********************************************************************************
package vpm;

// GUI imports
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MsgBox extends JFrame
{
    private String str="";
    private JLabel labela;
    
    public MsgBox()
    {
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/Att.gif")));
        this.setTitle("Error");
        
        int xPos = (this.getToolkit().getScreenSize().width-250)/2;
        int yPos = (this.getToolkit().getScreenSize().height-150)/2;
         
        this.setLocation(xPos, yPos);
        this.setResizable(false);
        
        JPanel basicpannel = new JPanel();
        basicpannel.setLayout(new GridLayout(2,1));
        add(basicpannel);
        
        labela=new JLabel("");
        labela.setHorizontalAlignment(SwingConstants.CENTER);
        basicpannel.add(labela);

        JPanel okbuttonpannel = new JPanel();
        basicpannel.add(okbuttonpannel);
        
        JButton okbutton = new JButton();
        okbutton.setText("OK");
        okbutton.setPreferredSize(new Dimension(80, 30));
        okbuttonpannel.add(okbutton);
        okbutton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
            }
        });                                       // Ends addActionListener
    	
    	pack();
        setSize(250, 150);
    }
    
    public void clearMessage()
    {
    	str="";
    	labela.setText("");
    }

    public void addMessageString(String addition)
    {
    	str+=addition;
    	labela.setText(str);
    }
    
    public void setErrorType()
    {
    	setTitle("Error");    	
    }

    public void setWarningType()
    {
    	setTitle("Warning");    	
    }

    public void setInfoType()
    {
    	setTitle("Info");    	
    }
}
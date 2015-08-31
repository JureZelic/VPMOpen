//********************************************************************************
//
//    About - About box class
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
//        2015: J. Zelic - maven compiling
//********************************************************************************
package vpm;

// GUI imports
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class About extends JFrame implements ActionListener
{
	  private JTextArea outputText;
	  private JPanel basicpannel;
	  private final int wwidh=400, whighth=300;
	  
    public About(String picture)
    {
    	  setTitle("About");
    	  setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

    	  int xPos = (this.getToolkit().getScreenSize().width-wwidh)/2;
        int yPos = (this.getToolkit().getScreenSize().height-whighth)/2;
    	  this.setLocation(xPos, yPos);  
    	    	
      	basicpannel = new JPanel();
      	basicpannel.setLayout(new BorderLayout());
      	add(basicpannel);
      	
      	JPanel iconpanell = new JPanel();
      	basicpannel.add(iconpanell,BorderLayout.WEST);
      	JPanel textpanell = new JPanel();
      	textpanell.setLayout(new BorderLayout());
      	basicpannel.add(textpanell,BorderLayout.CENTER);
      	
      	JLabel icon = new JLabel("",new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(picture))),JLabel.CENTER);
      	iconpanell.add(icon);
      	
        outputText = new JTextArea();
        outputText.setBorder(BorderFactory.createTitledBorder(""));
        outputText.setFont(new Font("SansSerif", Font.PLAIN, 11));
        outputText.setLineWrap(true);
        outputText.setWrapStyleWord(true);
        outputText.setTabSize(3);
        outputText.setEditable(false);
        
        textpanell.add(outputText,BorderLayout.CENTER);
 
    	  pack();
        setSize(wwidh, whighth);   	
    }
    public void setText(String tx)
    {
    	  outputText.setText(tx);
    }
    
    public void makeOKButton()
    {
        JPanel okpanell = new JPanel();
        basicpannel.add(okpanell,BorderLayout.SOUTH);

        JButton okbutton = new JButton();
        okbutton.setText("OK");
        okbutton.setPreferredSize(new Dimension(80, 30));
        okbutton.addActionListener( this );
        okpanell.add(okbutton);
    }
    
    public void actionPerformed(ActionEvent evt)
    {
	      setVisible(false); 
    }	  
}
//********************************************************************************
//
//    Vpm - Main Class of the program
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
//  Revision history:
//        2006: J. Zelic - First Version
//  Dec.  2006: J. Zelic - 1.1.0 added ppO2 printouts and warnings 
//  Feb.  2007: J. Zelic - 1.2.0 changed ppO2 printout
//                               ICD warning
//  Mar.  2007: j. Zelic - 1.2.1 changed MOD for 21/35 deco mix, LINUX adaptation
//  Mar.  2007: J. Zelic - 1.3.0 repeated dives
//  Apr.  2007: J. Zelic - 1.3.1 The size in main bar buttons - for Linux
//                               Improved accuracy on bottom mix gas consumption
//  May.  2007: J. Zelic - 1.3.2 6m/20ft last deco stop
//  May.  2007: J. Zelic - 1.4.0 multilevel algorithm - first stage 
//  Jan.  2015: J. Zelic - 1.4.3 maven compiling
//********************************************************************************
package vpm;

// GUI imports
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// Print Imports
import java.awt.print.*;
import tjacobs.print.*;
// IO inports
import java.io.*;

public class Vpm
{
    final String version="1.4.2";
    final int wwidh=700, whighth=500;
    final int MAX_DIVEPONTS=8;
    final int MAX_DECOMIX=4;
    final int MAX_DIVES=10; // Must be defined same as in BakerVpmB.java

    private boolean metric=true;
    private String depthUnit="m";
    private String volmeUnit="ltr";
    private double minPPO2=0.16;
    private double maxPPO2deco=1.6;
    private double maxPPO2bottom=1.4;
    private double decoStepSize=3.0;
    private double customDecoMixF02=80;
    private double customDecoMixFHe=0;
    private double customDecoMixMOD=10;
    
    private double solubilityN2=0.067;
    private double solubilityHe=0.015;   
    private int xPos, yPos; /* start position of windows */
    
    private JFrame editFrame, editDecoFrame, editSurfaceIntervalFrame;
    private JTextArea outputText;
    private DiveCalc divecalc;

    private ConfigWindow configWindow;

    JTextField profilepointText[]=new JTextField[MAX_DIVEPONTS];
    JTextField profilepointMix[]=new JTextField[MAX_DIVEPONTS];
    JButton profilePointEditButton[] = new JButton[MAX_DIVEPONTS];
    JButton profilePointDelButton[] = new JButton[MAX_DIVEPONTS];
    JButton profilePointInsButton[] = new JButton[MAX_DIVEPONTS];

    double profilePointDepth[][]= new double[MAX_DIVEPONTS][MAX_DIVES];
    double profilePointRT[][]= new double[MAX_DIVEPONTS][MAX_DIVES];
    double profilePointMixO2[][]= new double[MAX_DIVEPONTS][MAX_DIVES];
    double profilePointMixHe[][]= new double[MAX_DIVEPONTS][MAX_DIVES];
    
    int surfaceIntervals[]=new int[MAX_DIVES];

    JTextField decomixText[]=new JTextField[MAX_DECOMIX];
    JTextField decoModText[]=new JTextField[MAX_DECOMIX];
    JButton decomixEditButton[] = new JButton[MAX_DECOMIX];
    JButton decomixDelButton[] = new JButton[MAX_DECOMIX];
    JTextField customfO2Text, customfHeText, custommodText; 
    JTextField hoursText, minutesText;
    JButton okProfileButton;
    
    JTextField surfaceintervalText;
    JButton surfaceintervalEditButton;
    
    JButton mainBarPpreviousbutton, editFramePpreviousbutton;
    JButton mainBarNextbutton, editFrameNextbutton;   

    double decomixO2[][]= new double[MAX_DECOMIX][MAX_DIVES];
    double decomixHe[][]= new double[MAX_DECOMIX][MAX_DIVES];
    double fromdepth[][]= new double[MAX_DECOMIX][MAX_DIVES];

    BakerVpmB bakerB;
    DiveTools diveTools;
    private int currentDive=0;

 //***************************************************************
 //
 //     LITLE TOOLS
 //
 //***************************************************************
     //*****************************************
    // Method:   round
    // Input:    number to be rounded, round value
    // Output:   rounded value
    //*****************************************
    private double round(double a, double b)
    {
    	  if (a<=0)
    	      return 0;
    	  return(Math.rint(a/b)*b);
    }

     //*****************************************
    // Method:   toString
    // Input:    number to be converted, before decimal point, after decimal point
    // Output:   convert number to string
    //*****************************************
    private String toString(double val, int up, int down)
    {
    	  int i, count, dotindex;
    	  
    	  count=1;
    	  for (i=0;i<down;i++)
    	      count*=10;
    	          
    	  String rv=""+Math.rint(val*count)/count;

        count=rv.length();        // chars in string    	  
    	  dotindex=rv.indexOf("."); // last char before dot
    	  if (dotindex==-1)         // if no dot in string
    	  {
    	  	  dotindex=count;
    	  	  if (down>0)
    	  	  {
    	  	      rv+=".";
    	  	      count++;
    	  	  }
        }    	  

    	  for(i=dotindex+down+1;i>count;i--)
    	  	  rv+="0";  
    	  for(;dotindex<up;dotindex++)
    	  	  rv=" "+rv;
 	  
    	  return rv;
    }   
    
    //*****************************************
    // Method:   gasmixToString
    // Input:    fraction of Oxygen and fraction of Helium
    // Output:   String Representaion of a mix
    //*****************************************
    private String gasmixToString(double fO2, double fHe)
    {
    	  String rv="";
    	  
    	  if (fO2==1)
    	      return ("Oxygen");
    	  if ( (fO2>=.209) && (fO2<=.21) && (fHe==0) )
    	      return ("Air");
    	  if (fHe==0)
    	      rv+="Nx";
    	  else
    	      rv+="Tx";
    	  if (Math.rint(100*fO2)==100*fO2)
    	      rv+=Math.round(100*fO2);
    	  else
    	      rv+=Math.rint(1000*fO2)/10;
    	  if (fHe==0)
    	      return rv;
    	  if (Math.rint(100*fHe)==100*fHe) // no decimal point
    	      rv+="/"+Math.round(100*fHe);
    	  else
    	      rv+="/"+Math.rint(1000*fHe)/10;
    	  return rv;
    	  
    }
 //***************************************************************
 //
 //     CALCULATE DIVE TABLE AND PRINT IT OUT
 //
 //***************************************************************
    private void makeADive()
    {
        int i,j, rowsNumber=17;
        int prevmix=0, mix=0; 
        int noBottomMix[]=new int[MAX_DIVES]; // number ob bottom mixes
        double rmv=0, rate, prevsegmentTime=0, segmentTime, prevsegmentDepth=0, segmentDepth=0, prevsegmentfO2=0, segmentfO2=0, prevsegmentfHe=0, segmentfHe=0;
        double otu=0, tmpotu, cns=0, tmpcns, gas=0, tmpgas;
        double rmvBottom, rmvDeco;
        double ppO21, ppO22, ppO23, ppO2min, ppO2max;
        int dive_num, rv;
        
        bakerB.newMission();
        outputText.setText(""); // clear the output
        outputText.setBorder(BorderFactory.createTitledBorder("Model:   VPM-B"));
        
        // list of all the diferent mixes used in this dive
        double diveMixO2[][]=new double[bakerB.MAX_BOTTOM_MIXES+MAX_DECOMIX][bakerB.MAX_DIVES];
        double diveMixHe[][]=new double[bakerB.MAX_BOTTOM_MIXES+MAX_DECOMIX][bakerB.MAX_DIVES];
        double diveGas[]=new double[bakerB.MAX_BOTTOM_MIXES+MAX_DECOMIX];

        // set parameters
        bakerB.conservatism=configWindow.conservatism;
        bakerB.deco_gas_switch_time=configWindow.oxiWindow;
        rmvBottom=configWindow.rmvBottom;
        rmvDeco=configWindow.rmvDeco;
        bakerB.lastStop6m20ft=configWindow.lastStop6m20ft;

        for(i=0;i<bakerB.MAX_BOTTOM_MIXES+MAX_DECOMIX;i++) // clear the table
        {
            for(j=0;j<bakerB.MAX_DIVES;j++)
                diveMixO2[i][j]=-1;
            diveGas[i]=0;
        }
 
        for(dive_num=0; dive_num<MAX_DIVES;dive_num++)
        {

            noBottomMix[dive_num]=0;
            	  
            for(i=0;i<MAX_DIVEPONTS;i++)
            {
                // add profile point
                if (profilePointDepth[i][dive_num]<0) // no more points to add 
                {
                	   if (i==0) /* if no dive points in this dive */
                	   {
                	   	    mix=bakerB.addBottomMix(0.21,0);
                	   	    bakerB.addProfilePoint(0,0,Double.valueOf(configWindow.descentRate).doubleValue(),mix);
                	   }    
                     break;
                }
            
                // add mix to mix table
                for (j=0;true;j++)
                {
                    if(j==bakerB.MAX_BOTTOM_MIXES)
                    {
                        outputText.append("Max. number of bottom mixes excieded - "+j);
                        return;
                    }
                        if (diveMixO2[j][dive_num]==-1) // found empty spot
                    {
                        mix=bakerB.addBottomMix(profilePointMixO2[i][dive_num]/100,profilePointMixHe[i][dive_num]/100);
                        if (mix<0)
                        {
                            outputText.append("Internal ERROR 1: mix="+mix+"divepoint="+i+"\n");
                            return;
                        }
                            diveMixO2[mix][dive_num]=profilePointMixO2[i][dive_num];
                            diveMixHe[mix][dive_num]=profilePointMixHe[i][dive_num];
                        break;
                    }
                        if((profilePointMixO2[i][dive_num]==diveMixO2[j][dive_num]) && (profilePointMixHe[i][dive_num]==diveMixHe[j][dive_num])) 
                         // mix like this already added
                    {
                        mix=j;
                        break;
                    }
                }
                if (mix>noBottomMix[dive_num]) 
                     noBottomMix[dive_num]=mix;
            
                if (i==0)                   // if first point rate eq descent rate 
                    rate=Double.valueOf(configWindow.descentRate).doubleValue();
                else if (profilePointDepth[i][dive_num]>profilePointDepth[i-1][dive_num]) // descent
                    rate=Double.valueOf(configWindow.descentRate).doubleValue(); 
                else                                                  // ascent
                    rate=-Double.valueOf(configWindow.ascentRate).doubleValue();
            
                if(bakerB.addProfilePoint(profilePointDepth[i][dive_num],profilePointRT[i][dive_num],rate,mix)<0)
                {
                    outputText.append("Internal ERROR 2\n");
                    return;
                }
            }
            
            for(i=0;i<MAX_DECOMIX;i++)
            {
                if (fromdepth[i][dive_num]<0)
                     break;
                int ret=bakerB.addDecoMix(decomixO2[i][dive_num]/100,decomixHe[i][dive_num]/100,fromdepth[i][dive_num]);         
                if (ret<0)
                {
                    outputText.append("Internal ERROR 0: "+gasmixToString(decomixO2[i][dive_num]/100,decomixHe[i][dive_num]/100));
                    return;
                }
                    diveMixO2[noBottomMix[dive_num]+1+i][dive_num]=decomixO2[i][dive_num];
                    diveMixHe[noBottomMix[dive_num]+1+i][dive_num]=decomixHe[i][dive_num];
            }

 
           
            if (dive_num<MAX_DIVES-1) /* not on the last dive */
                if(bakerB.addSurfaceInterval(surfaceIntervals[dive_num+1])<0)
                    outputText.append("Internal ERROR 0.1: "+dive_num);
            
        } /* dive_num */
         
        bakerB.finalAscentSpeed=-Double.valueOf(configWindow.ascentRate).doubleValue();
        
        rv=bakerB.calculate(); // HORAY
       	  
        if(rv<-1)
        {  
            MsgBox errorBox=new MsgBox();
            errorBox.clearMessage();
            errorBox.addMessageString("Dive #"+((-rv)/100+1)+" Dive Point "+
            ((-rv)%100+1)+" to deep\n");
            errorBox.setVisible(true); 
            return;           
        }
        else if(rv<0) 
        {       	  
            outputText.append("Internal ERROR 3\n");
            return;
        }
        double temp;

        outputText.append(
            "Warning:  This program is intended for informational purposes only. "+  
            "The author accepts no responsibility for the results of diving "+
            "schedules generated by this program.  Divers who "+
            "choose to use this deco schedule do so at their own risk!\n");
         outputText.append("\n");

        for (dive_num=0;dive_num<currentDive;dive_num++)
        {
        	  prevsegmentDepth=0;
            prevsegmentTime=0;
        	  for(i=0;i<50;i++)
            {
                if (bakerB.getProfilePoint(i,1,dive_num)==-1)
                    break;
                mix=bakerB.getProfileGasIndex(i,dive_num);
                if (i>0)
                    prevmix=bakerB.getProfileGasIndex(i-1,dive_num);
                else
                    prevmix=mix;                
                
                segmentDepth=bakerB.getProfilePoint(i,1,dive_num);
                segmentTime=bakerB.getProfilePoint(i,2,dive_num);
                segmentfO2=bakerB.getProfilePoint(i,4,dive_num);
                segmentfHe=bakerB.getProfilePoint(i,5,dive_num);

                if (i==0)
                {
                    prevsegmentfO2=segmentfO2;
                    prevsegmentfHe=segmentfHe;
                }                    
                tmpcns=diveTools.getSegmentCNS(prevsegmentDepth,segmentDepth,prevsegmentfO2, segmentfO2,
                    -Double.valueOf(configWindow.ascentRate).doubleValue(),
                    Double.valueOf(configWindow.descentRate).doubleValue(),
                    segmentTime-prevsegmentTime);
                if (tmpcns<0)
                {
                    outputText.append("Internal ERROR 4\n");
                    return;
                }
                cns+=tmpcns;
                prevsegmentDepth=segmentDepth;
                prevsegmentTime=segmentTime;
                prevsegmentfO2=segmentfO2;
                prevsegmentfHe=segmentfHe;                                    
            }
            tmpcns=diveTools.getSegmentCNS(segmentDepth,0,prevsegmentfO2,segmentfO2,
                -Double.valueOf(configWindow.ascentRate).doubleValue(),
                Double.valueOf(configWindow.descentRate).doubleValue(),
                2*segmentDepth/Double.valueOf(configWindow.ascentRate).doubleValue());
            if (tmpcns<0)
            {
                outputText.append("Internal ERROR 4.1\n");
                return;
            }
            cns+=tmpcns;            
        	  cns=cns/Math.exp((surfaceIntervals[dive_num+1]/90)*Math.log(2));      // JURE
        } //next dive
        
        dive_num=currentDive; // dive to print
        
        if (dive_num>0)
        {            
            outputText.append("Surface Interval: "+surfaceIntervals[dive_num]/60+"h "+surfaceIntervals[dive_num]%60+"min\n");
            outputText.append("CNS before the dive: "+Math.rint(cns*1000)/10+"%\n");        
        }
        outputText.append("Conservatism "+Integer.toString(configWindow.conservatism)+"\n");
        temp=Math.rint(bakerB.otputStartOfDecoDepth[dive_num]*10)/10;
        outputText.append("Leading compartment enters the decompression zone at "+Double.toString(temp)+depthUnit+"\n\n");
        outputText.append("  Depth\tRun Time\tStop Time\tMix\t    ppO2\n");
        outputText.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

        outputText.setBorder(BorderFactory.createTitledBorder("Dive #"+(dive_num+1)));
        
        prevsegmentDepth=0;
        prevsegmentTime=0;
        
        for(i=0;i<50;i++)
        {
            if (bakerB.getProfilePoint(i,1,dive_num)==-1)
                break;

            mix=bakerB.getProfileGasIndex(i,dive_num);
            if (i>0)
                prevmix=bakerB.getProfileGasIndex(i-1,dive_num);
            else
                prevmix=mix;                

            segmentDepth=bakerB.getProfilePoint(i,1,dive_num);
            temp=Math.rint(segmentDepth*10)/10;
            outputText.append("  "+Double.toString(temp)+" "+depthUnit+"\t");
            segmentTime=bakerB.getProfilePoint(i,2,dive_num);
            temp=Math.rint(segmentTime*10)/10;
            outputText.append(Double.toString(temp)+" min\t");
            temp=Math.rint(bakerB.getProfilePoint(i,3,dive_num)*10)/10;
            outputText.append(Double.toString(temp)+" min\t");
            segmentfO2=bakerB.getProfilePoint(i,4,dive_num);
            segmentfHe=bakerB.getProfilePoint(i,5,dive_num);
            outputText.append(gasmixToString(segmentfO2,segmentfHe)+"\t");
            if (i==0)
            {
                prevsegmentfO2=segmentfO2;
                prevsegmentfHe=segmentfHe;
            }

            // checking ppO2
            ppO21=diveTools.depth2press(prevsegmentDepth)*prevsegmentfO2;
            ppO22=diveTools.depth2press(segmentDepth)*prevsegmentfO2;
            ppO23=diveTools.depth2press(segmentDepth)*segmentfO2;
            ppO2min=Math.min(Math.min(ppO21,ppO22),ppO23);
            ppO2max=Math.max(Math.max(ppO21,ppO22),ppO23);            
                  
            outputText.append("    "+toString(ppO21,1,2)+" - "+toString(ppO22,1,2)+" - "+toString(ppO23,1,2)+"   ");
            if (ppO2min < minPPO2)
                outputText.append("Segment ppO2 low! ");            
            if ((mix>noBottomMix[dive_num]) && (ppO23 > maxPPO2deco))
                outputText.append("Segment ppO2 high! ");
            else if ((prevmix>noBottomMix[dive_num]) && (Math.max(ppO21,ppO22) > maxPPO2deco)) 
                outputText.append("Segment ppO2 high! ");
            else if ((mix<=noBottomMix[dive_num]) && (ppO2max > maxPPO2bottom))
                outputText.append("Segment ppO2 high! ");
            
            // checking ICD
            double prevsegmentfN2=1-prevsegmentfO2-prevsegmentfHe;
            double segmentfN2=1-segmentfO2-segmentfHe;
            if ((configWindow.icdWarning) && (i>=bakerB.firstDecoProfilePoint))    // ICD flag set and deco zone
              if (((prevsegmentfHe!=0)||(segmentfHe!=0))&&((prevsegmentfN2!=0)||(segmentfN2!=0)))         // 
                if( (prevsegmentfO2!=segmentfO2) || (prevsegmentfHe!=segmentfHe))  // change of gasses
                {
                    if ((segmentfHe*solubilityHe+segmentfN2*solubilityN2)>
                        (prevsegmentfHe*solubilityHe+prevsegmentfN2*solubilityN2))
                        outputText.append("ICD Warning!");
                }
                                          
            tmpotu=diveTools.getSegmentOTU(prevsegmentDepth,segmentDepth,prevsegmentfO2,segmentfO2,
                -Double.valueOf(configWindow.ascentRate).doubleValue(),
                Double.valueOf(configWindow.descentRate).doubleValue(),
                segmentTime-prevsegmentTime);

            if (tmpotu<=-1)
            {
                outputText.append("Internal ERROR 3.1\n");
                return;
            }
            otu+=tmpotu;

            tmpcns=diveTools.getSegmentCNS(prevsegmentDepth,segmentDepth,prevsegmentfO2, segmentfO2,
                -Double.valueOf(configWindow.ascentRate).doubleValue(),
                Double.valueOf(configWindow.descentRate).doubleValue(),
                segmentTime-prevsegmentTime);
            if (tmpcns<0)
            {
                outputText.append("Internal ERROR 4\n");
                return;
            }
            cns+=tmpcns;
  
            if (i>bakerB.firstDecoProfilePoint)
                rmv=rmvDeco;
            else
                rmv=rmvBottom;
   
            diveGas[prevmix]+=
                diveTools.getSegmentGas(prevsegmentDepth,segmentDepth,rmv,
                -Double.valueOf(configWindow.ascentRate).doubleValue(),
                Double.valueOf(configWindow.descentRate).doubleValue(),
                segmentTime-prevsegmentTime,1);
            diveGas[mix]+=
                diveTools.getSegmentGas(prevsegmentDepth,segmentDepth,rmv,
                -Double.valueOf(configWindow.ascentRate).doubleValue(),
                Double.valueOf(configWindow.descentRate).doubleValue(),
                segmentTime-prevsegmentTime,2);

            prevsegmentDepth=segmentDepth;
            prevsegmentTime=segmentTime;
            prevsegmentfO2=segmentfO2;
            prevsegmentfHe=segmentfHe;
            outputText.append("\n");
            rowsNumber++;
        }
        // last segment
        tmpotu=diveTools.getSegmentOTU(segmentDepth,0,prevsegmentfO2,segmentfO2,
            -Double.valueOf(configWindow.ascentRate).doubleValue(),
            Double.valueOf(configWindow.descentRate).doubleValue(),
            2*segmentDepth/Double.valueOf(configWindow.ascentRate).doubleValue());           
        if (tmpotu<0)
        {
            outputText.append("Internal ERROR 3.2\n");
            return;
        }
        otu+=tmpotu;
        outputText.append("\nOTU of this dive: "+Math.round(otu+0.49)+"\n");

        tmpcns=diveTools.getSegmentCNS(segmentDepth,0,prevsegmentfO2,segmentfO2,
            -Double.valueOf(configWindow.ascentRate).doubleValue(),
            Double.valueOf(configWindow.descentRate).doubleValue(),
            2*segmentDepth/Double.valueOf(configWindow.ascentRate).doubleValue());
        outputText.append("CNS total: "+Math.rint(cns*1000)/10+"%\n");
        if (tmpcns<0)
        {
            outputText.append("Internal ERROR 4.1\n");
            return;
        }
        cns+=tmpcns;
        
        diveGas[mix]+=
            diveTools.getSegmentGas(segmentDepth,0,rmv,
            -Double.valueOf(configWindow.ascentRate).doubleValue(),
            Double.valueOf(configWindow.descentRate).doubleValue(),
            2*segmentDepth/Double.valueOf(configWindow.ascentRate).doubleValue(),3);

        outputText.append("\n");
        for(i=0;i<bakerB.MAX_BOTTOM_MIXES+MAX_DECOMIX;i++)
        {
            if (diveGas[i]==0)
                continue;
            outputText.append(Math.rint(diveGas[i]*10)/10 +" " + volmeUnit + " " +
                gasmixToString(diveMixO2[i][dive_num]/100,diveMixHe[i][dive_num]/100) + "\n");
            rowsNumber++;
        }

        outputText.setRows(rowsNumber);
    }

 //***************************************************************
 //
 //     LOAD SAVE FUNCTIONS
 //
 //***************************************************************
     //*****************************************
    // Method:   loadDefaults
    // Input:    /
    // Output:   /
    //*****************************************
    private void loadDefaults()
    {
        int i,j;
        
        for (i=0;i<MAX_DIVEPONTS;i++)
            for (j=0;j<MAX_DIVES;j++)          
                profilePointDepth[i][j]=-1;              

        for (i=0;i<MAX_DECOMIX;i++)
            for (j=0;j<MAX_DIVES;j++)
                fromdepth[i][j]=-1;        
        
        surfaceIntervals[0]=-1;
        for (j=1;j<MAX_DIVES;j++)
            surfaceIntervals[j]=180;
    }
    
    //*****************************************
    // Method:   loadConfiguration
    // Input:    /
    // Output:   /
    //*****************************************    
    private void loadConfiguration()
    {
    	  String tmpstr="";
        int i,j;
        
        loadDefaults();

        String slash=System.getProperty("file.separator","/");
        String userDir=System.getProperty("user.home","."+slash);


        // backward compatibility
        File configDir = new File(userDir+slash+"VPMOpen");
        if (!configDir.isDirectory())
        {
            configDir.mkdir();
            configDir.setReadable(true);
            configDir.setWritable(true);
            TextFile oldConfigFile=new TextFile ("vpm.config");
            TextFile newConfigFile=new TextFile(userDir+slash+"VPMOpen"+slash+"vpm.config");
            try
            {
                FileInputStream is = new FileInputStream(oldConfigFile);
                FileOutputStream os = new FileOutputStream(newConfigFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
                oldConfigFile.delete();
                oldConfigFile.close();
            } catch (IOException e) {/* expected */}
            newConfigFile.close();

        }
        // backward compatibility - END
        
        TextFile cnffile=new TextFile(userDir+slash+"VPMOpen"+slash+"vpm.config");
        if(cnffile.open(cnffile.FILE_READ)==false)
            return;
        
        // version line
        cnffile.readLine(); 

        // read divepoints
        for (j=0;j<MAX_DIVES;j++)
        {
            for (i=0;i<=MAX_DIVEPONTS;i++)
            {
            	  tmpstr=cnffile.readWord();
            	  if (tmpstr.equals("."))
            	      break;
                profilePointRT[i][j]=Double.valueOf(tmpstr).doubleValue();
                tmpstr=cnffile.readWord(); 
                profilePointMixO2[i][j]=Double.valueOf(tmpstr).doubleValue();
                tmpstr=cnffile.readWord(); 
                profilePointMixHe[i][j]=Double.valueOf(tmpstr).doubleValue();
                tmpstr=cnffile.readWord(); 
                profilePointDepth[i][j]=Double.valueOf(tmpstr).doubleValue();
            }

            for (;i<MAX_DIVEPONTS;i++)     
                profilePointDepth[i][j]=-1;
            
            // custom mix
            if (j==0)
            {
                tmpstr=cnffile.readWord();
                customDecoMixF02=Double.valueOf(tmpstr).doubleValue();
                tmpstr=cnffile.readWord();
                customDecoMixFHe=Double.valueOf(tmpstr).doubleValue();
                tmpstr=cnffile.readWord();
                customDecoMixMOD=Double.valueOf(tmpstr).doubleValue();
                customfO2Text.setText(""+customDecoMixF02);      
                customfHeText.setText(""+customDecoMixFHe);                         
                custommodText.setText(""+customDecoMixMOD);
            }
            
            // read decomixes            
            for (i=0;i<=MAX_DECOMIX;i++)
            {
            	  tmpstr=cnffile.readWord();
            	  if (tmpstr.equals(".") || tmpstr.equals(","))
            	      break;
                decomixO2[i][j]=Double.valueOf(tmpstr).doubleValue();
                tmpstr=cnffile.readWord(); 
                decomixHe[i][j]=Double.valueOf(tmpstr).doubleValue();
                tmpstr=cnffile.readWord(); 
                fromdepth[i][j]=Double.valueOf(tmpstr).doubleValue();       	      
            }

            if (tmpstr.equals("."))
            {
                break;
            }
            else
            {
            	  tmpstr=cnffile.readWord();
            	  surfaceIntervals[j+1]=Integer.valueOf(tmpstr).intValue();
            }            
        }        
           
        // read configuration
        tmpstr=cnffile.readWord();
        configWindow.setConservatism((int)Long.valueOf(tmpstr).longValue());
        tmpstr=cnffile.readWord();
        configWindow.setDescentRate(tmpstr); 
        tmpstr=cnffile.readWord();
        configWindow.setAscentRate(tmpstr);
        tmpstr=cnffile.readWord();
        configWindow.setRmvBottom(Double.valueOf(tmpstr).doubleValue());
        tmpstr=cnffile.readWord();
        configWindow.setRmvDeco(Double.valueOf(tmpstr).doubleValue());
        tmpstr=cnffile.readWord();
        configWindow.setOxiWindow(Double.valueOf(tmpstr).doubleValue());
        
        tmpstr=cnffile.readWord(); // read dot
        
        // read dive calculator
        tmpstr=cnffile.readWord();
        divecalc.setMod(Double.valueOf(tmpstr).doubleValue());
        tmpstr=cnffile.readWord();
        divecalc.setPpO2(Double.valueOf(tmpstr).doubleValue());
        tmpstr=cnffile.readWord();
        divecalc.setEAD(Double.valueOf(tmpstr).doubleValue());
        tmpstr=cnffile.readWord();
        divecalc.setO2nrc(tmpstr.equals("1"));
        
        tmpstr=cnffile.readWord(); // read dot
        
        // read ICD warning flag
        tmpstr=cnffile.readWord();
        configWindow.setIcdWarning(tmpstr.equals("1"));        

        // read last stop flag
        tmpstr=cnffile.readWord();
        configWindow.setlastStop6m20ft(tmpstr.equals("1"));   
               
        cnffile.close();
    }
    
    //*****************************************
    // Method:   saveConfiguration
    // Input:    /
    // Output:   /
    //*****************************************
    private void saveConfiguration()
    {
    	  int i,j;

        String slash=System.getProperty("file.separator","/");
        String userDir=System.getProperty("user.home","."+slash);

        TextFile cnffile=new TextFile(userDir+slash+"VPMOpen"+slash+"vpm.config");
        if(cnffile.open(cnffile.FILE_WRITE)==false)
            if(cnffile.create(cnffile.FILE_WRITE)==false)
                return;
            
        cnffile.writeStr("VPMOpen version "+version+"\n");

        // save dive points
        for (j=0;j<MAX_DIVES;j++)
        {
            for(i=0;i<MAX_DIVEPONTS;i++)
            {
                // add profile point
                if (profilePointDepth[i][j]<0) // no more points to add
                     break;
                cnffile.writeStr(profilePointRT[i][j]    + " " +
                                 profilePointMixO2[i][j] + " " +
                                 profilePointMixHe[i][j] + " " + 
                                 profilePointDepth[i][j] + "\n"); 
            } 
            cnffile.writeStr(".\n");
            
            // save custom deco mix
            if (j==0)
                cnffile.writeStr(customDecoMixF02 + " " + 
                                 customDecoMixFHe + " " + 
                                 customDecoMixMOD + "\n"); 
            //save deco mixes        
            for(i=0;i<MAX_DECOMIX;i++)
            {
                if (fromdepth[i][j]<0)
                     break;
                cnffile.writeStr(decomixO2[i][j] + " " +  
                                 decomixHe[i][j] + " " +  
                                 fromdepth[i][j] + "\n"); 
            }
            if (j<MAX_DIVES-1)
            	  cnffile.writeStr(", "+surfaceIntervals[j+1]+"\n");
            else
                cnffile.writeStr(".\n");
        }
        
        // save configuration
        cnffile.writeStr(configWindow.conservatism + " " + 
                         configWindow.descentRate  + " " + 
                         configWindow.ascentRate   + " " + 
                         configWindow.rmvBottom    + " " + 
                         configWindow.rmvDeco      + " " + 
                         configWindow.oxiWindow    + "\n"); 
        cnffile.writeStr(".\n");        
        
        // save dive calculator
        cnffile.writeStr(divecalc.getMod()            + " " +  
                         divecalc.getPpO2()           + " " +  
                         divecalc.getEAD()            + " " +  
                         ((divecalc.getO2nrc())?1:0) + "\n");           
        cnffile.writeStr(".\n");
        
        // save ICD warning flag
        cnffile.writeStr(((configWindow.icdWarning)?1:0) + " ");
        
        // save last stop flag
        cnffile.writeStr(((configWindow.lastStop6m20ft)?1:0) + "\n");

        cnffile.close();
    }
    
    private void printScreen()
    {    
        Component componentToPrint = outputText;
        StandardPrint sp = new StandardPrint(componentToPrint);
        try
        {
            sp.start();
        } catch (PrinterException exception)                      
        {                                                         
            System.err.println("Printing error: " + exception);   
        }                                                         
    }
 //***************************************************************
 //
 //     EDIT PROFILE STUFF
 //
 //***************************************************************
    private JFrame editprofilePointFrame;
    private JTextField depthText, timeText, fO2Text, fHeText;
    private int editDivePointNumber; // which button was pressed
    private int divePointAction; // was it edit (0) or insert (1)

    //*****************************************
    // Method:   makeeditprofilePointFrame
    // Input:    /
    // Output:   /
    //*****************************************
    private void makeeditprofilePointFrame()
    {
        JPanel editPannel, centralpannel, southpannel, pannel;
        JButton editinserProfilePointButton;
 
        editprofilePointFrame=new JFrame();
        editprofilePointFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        editprofilePointFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/VPMOpen.gif")));
        editprofilePointFrame.setLocation(xPos+40, yPos+70); 
        editprofilePointFrame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e) //the name must be exactly that
            {
                updateprofilePointFields();
            }
        });
        
        centralpannel= new JPanel();
        editprofilePointFrame.add(centralpannel,BorderLayout.CENTER);
        southpannel= new JPanel();
        editprofilePointFrame.add(southpannel,BorderLayout.SOUTH);

        editPannel = new JPanel();
        editPannel.setLayout(new GridLayout(4,2));
        centralpannel.add(editPannel);

        depthText = new JTextField(4);
        depthText.setEditable(true);
        timeText = new JTextField(4);
        timeText.setEditable(true); 
        fO2Text = new JTextField(4);
        fO2Text.setEditable(true);
        fHeText = new JTextField(4);
        fHeText.setEditable(true);

        editPannel.add(new Label("    Depth:"));
        pannel= new JPanel();
        pannel.add(depthText);
        editPannel.add(pannel);
        editPannel.add(new Label(" Seg. Time:"));
        pannel= new JPanel();
        pannel.add(timeText);
        editPannel.add(pannel);
        editPannel.add(new Label("  Mix-fO2:"));
        pannel= new JPanel();
        pannel.add(fO2Text);
        editPannel.add(pannel); 
        editPannel.add(new Label("  Mix-fHe:"));
        pannel= new JPanel();
        pannel.add(fHeText);
        editPannel.add(pannel);

        editinserProfilePointButton=new JButton("OK");
        editinserProfilePointButton.setPreferredSize(new Dimension(80, 30));
        editinserProfilePointButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                double depth, time, fO2, fHe;

                depth=Double.valueOf(depthText.getText()).doubleValue();
                time=Double.valueOf(timeText.getText()).doubleValue();
                fO2=Double.valueOf(fO2Text.getText()).doubleValue();
                fHe=Double.valueOf(fHeText.getText()).doubleValue();

                depth=Math.rint(depth*10)/10;
                time=Math.rint(time*10)/10;
                fO2=Math.rint(fO2*10)/10;
                fHe=Math.rint(fHe*10)/10;
 
                if (divePointAction==0)
                {   // edit
                	  double prev_depth, prev_time;
                	  if (editDivePointNumber==0)
                	  {
                	  	  prev_depth=0;prev_time=0;
                	  }
                	  else
                	  {
                	  }
                    editprofilePoint(editDivePointNumber,depth, time, fO2, fHe);                   	  
                }
                else
                {    //insert
                    insertprofilePoint(editDivePointNumber,depth, time, fO2, fHe);
                }  
                editprofilePointFrame.setVisible(false);
                updateprofilePointFields();
            }
        });                                       // Ends addActionListener

        southpannel.add(editinserProfilePointButton);
            
        editprofilePointFrame.pack();
        editprofilePointFrame.setSize(200, 200);
        editprofilePointFrame.setVisible(false);
    }

    //*****************************************
    // Method:   filleditBottomTextButtons
    // Input:    0-Edit button was pressed
    //           1-Insert button was pressed
    // Output:   /
    //*****************************************
    private void filleditBottomTextButtons(int bottonPressed)
    {
        if (bottonPressed==0) // edit 
        {
            if (profilePointDepth[editDivePointNumber][currentDive]!=-1) // change the contents of a field
            {                                               // take the old values
                depthText.setText(""+profilePointDepth[editDivePointNumber][currentDive]);
                timeText.setText(""+profilePointRT[editDivePointNumber][currentDive]);
                fO2Text.setText(""+profilePointMixO2[editDivePointNumber][currentDive]);
                fHeText.setText(""+profilePointMixHe[editDivePointNumber][currentDive]);
            }
            else if (editDivePointNumber==0) // the first divepoint to enter
            {                                // take the values from calculator
                depthText.setText(""+divecalc.getMod());
                timeText.setText("");
                fO2Text.setText(""+divecalc.getF02());
                fHeText.setText(""+divecalc.getFHe());        
            }
            else // edit new field
            {    // take the mix fields from previous point
                depthText.setText("");
                timeText.setText("");
                fO2Text.setText(""+profilePointMixO2[editDivePointNumber-1][currentDive]);
                fHeText.setText(""+profilePointMixHe[editDivePointNumber-1][currentDive]);
            }
        }
        else  // insert
        {     // simply take the data from existent point
                depthText.setText("");
                timeText.setText("");
                fO2Text.setText(""+profilePointMixO2[editDivePointNumber][currentDive]);
                fHeText.setText(""+profilePointMixHe[editDivePointNumber][currentDive]);
        }
    }

    //*****************************************
    // Method:   deleteprofilePoint
    // Input:    index of a point to delete
    // Output:   /
    //*****************************************
    private void deleteprofilePoint(int which)
    {
        int i;

        if (which==MAX_DIVEPONTS-1) // delete last point in the table
            profilePointDepth[which][currentDive]=-1;
        else if (profilePointDepth[which+1][currentDive]==-1) // delete last ocupied point
            profilePointDepth[which][currentDive]=-1;
        else
        {
            for (i=which;i<MAX_DIVEPONTS-1;i++)
            {
                profilePointDepth[i][currentDive]=profilePointDepth[i+1][currentDive];
                profilePointRT[i][currentDive]=profilePointRT[i+1][currentDive];
                profilePointMixO2[i][currentDive]=profilePointMixO2[i+1][currentDive];
                profilePointMixHe[i][currentDive]=profilePointMixHe[i+1][currentDive];            	
            }
            profilePointDepth[MAX_DIVEPONTS-1][currentDive]=-1;     
        }
    }

    //*****************************************
    // Method:   insertprofilePoint
    // Input:    index of a point to insert before
    //           dive point datta: depth, runtime, mix datta
    // Output:   /
    //*****************************************
    private void insertprofilePoint(int which, double depth, double rtime, double ppO2, double ppHe)
    {
        int i;
        double prev_depth=0, prev_time=0;
        boolean changed=false;
        
        if (which!=0)
            prev_depth=profilePointDepth[which-1][currentDive];
        
        
        // test the values - against previous point
        if (depth>prev_depth) // descent
        {
        	  // is rtime big enough?
        	  if ( depth > (prev_depth + Double.valueOf(configWindow.descentRate).doubleValue()*(rtime-prev_time)) )
        	  { // error - need longer rtime
        	  	  rtime=(depth-prev_depth)/Double.valueOf(configWindow.descentRate).doubleValue();
                rtime=Math.rint(rtime*10+.5)/10;
                changed=true;      	  
        	  }
        }
        else  // ascent
        {
        	  if ( rtime<1 )
        	  {  
        	      rtime=1;
        	      changed=true;
        	  }        	
        }
        
        if (changed)
        {
            MsgBox errorBox=new MsgBox();
            errorBox.clearMessage();
            errorBox.addMessageString("Run Time corected to "+rtime);
            errorBox.setVisible(true);
        }  
               
        // Insert it in the table
        for (i=MAX_DIVEPONTS-2;i>=which;i--)
        {
            profilePointDepth[i+1][currentDive]=profilePointDepth[i][currentDive];
            profilePointRT[i+1][currentDive]=profilePointRT[i][currentDive];
            profilePointMixO2[i+1][currentDive]=profilePointMixO2[i][currentDive];
            profilePointMixHe[i+1][currentDive]=profilePointMixHe[i][currentDive];        	
        }
        profilePointDepth[which][currentDive]=depth;
        profilePointRT[which][currentDive]=rtime;
        profilePointMixO2[which][currentDive]=ppO2;
        profilePointMixHe[which][currentDive]=ppHe;
    }

    //*****************************************
    // Method:   editprofilePoint
    // Input:    index of a point to insert before
    //           dive point datta: depth, runtime, mix datta
    // Output:   /
    //*****************************************
    private void editprofilePoint(int which, double depth, double rtime, double ppO2, double ppHe)
    {
        double prev_depth=0, prev_time=0;
        boolean changed=false;
        
        if (which!=0)
            prev_depth=profilePointDepth[which-1][currentDive];
        

        // test the values - against previous point
        if (depth>prev_depth) // descent
        {
        	  // is rtime big enough?
        	  if ( depth > (prev_depth + Double.valueOf(configWindow.descentRate).doubleValue()*(rtime-prev_time)) )
        	  { // error - need longer rtime
        	  	  rtime=(depth-prev_depth)/Double.valueOf(configWindow.descentRate).doubleValue();
                rtime=Math.rint(rtime*10+.5)/10;
                changed=true;       	  
        	  }
        }
        else  // ascent
        {
        	  if ( rtime<1 )
        	  {  
        	      rtime=1;
        	      changed=true;
        	  }        	
        }

         
        if (changed)
        {
            MsgBox errorBox=new MsgBox();
            errorBox.clearMessage();
            errorBox.addMessageString("Run Time corected to "+rtime);
            errorBox.setVisible(true);
        }  
                       
        // Insert it in the table
        if ((profilePointDepth[which][currentDive]<0) && (which<(MAX_DIVEPONTS-1)))
            profilePointDepth[which+1][currentDive]=-1; // make new last
        profilePointDepth[which][currentDive]=depth;
        profilePointRT[which][currentDive]=rtime;
        profilePointMixO2[which][currentDive]=ppO2;
        profilePointMixHe[which][currentDive]=ppHe;
    }

    //*****************************************
    // Method:   frezzeprofilePointButtons
    // Input:    /
    // Output:   /
    //*****************************************
    private void frezzeprofilePointButtons()
    {
        int i;

        for (i=0;i<MAX_DIVEPONTS;i++)
        {
            profilePointEditButton[i].setBackground(Color.white);
            profilePointDelButton[i].setBackground(Color.white);
            profilePointInsButton[i].setBackground(Color.white);
            profilePointEditButton[i].setEnabled(false);
            profilePointDelButton[i].setEnabled(false);
            profilePointInsButton[i].setEnabled(false);
        }
        okProfileButton.setEnabled(false);
    }

    //*****************************************
    // Method:   updateprofilePointFields
    // Input:    /
    // Output:   /
    //*****************************************
    private void updateprofilePointFields()
    {
        int i;
        String str;

        for (i=0;i<MAX_DIVEPONTS;i++)
        {
            if (profilePointDepth[i][currentDive]<0)
                break;
            str=Double.toString(profilePointDepth[i][currentDive])+depthUnit+"/"+
                Double.toString(profilePointRT[i][currentDive])+"min";
            profilepointText[i].setText(str);
            
            str=gasmixToString(profilePointMixO2[i][currentDive]/100,profilePointMixHe[i][currentDive]/100);
            profilepointMix[i].setText(str);
            profilePointEditButton[i].setBackground(Color.green);
            profilePointDelButton[i].setBackground(Color.red);
            profilePointInsButton[i].setBackground(Color.yellow);
            profilePointEditButton[i].setEnabled(true);
            profilePointDelButton[i].setEnabled(true);
            profilePointInsButton[i].setEnabled(true);
        }

        if (i<MAX_DIVEPONTS) // not all the fields are full
        {
            profilepointText[i].setText("");
            profilepointMix[i].setText("");
            profilePointEditButton[i].setBackground(Color.green);
            profilePointDelButton[i].setBackground(Color.white);
            profilePointInsButton[i].setBackground(Color.white);
            profilePointEditButton[i].setEnabled(true);
            profilePointDelButton[i].setEnabled(false);
            profilePointInsButton[i].setEnabled(false);
            i++;            
            for (;i<MAX_DIVEPONTS;i++)
            {
                profilepointText[i].setText("");
                profilepointMix[i].setText("");
                profilePointEditButton[i].setBackground(Color.white);
                profilePointDelButton[i].setBackground(Color.white);
                profilePointInsButton[i].setBackground(Color.white);
                profilePointEditButton[i].setEnabled(false);
                profilePointDelButton[i].setEnabled(false);
                profilePointInsButton[i].setEnabled(false);
            }
        }
        else // all the fields are full
        {
            for (i=0;i<MAX_DIVEPONTS;i++) // no more insert posible
            {
                profilePointInsButton[i].setBackground(Color.white);
                profilePointInsButton[i].setEnabled(false);
            }
        }
        okProfileButton.setEnabled(true);

    }
 //***************************************************************
 //
 //    EDIT SURFACE INTERVAL
 //
 //***************************************************************
    //*****************************************
    // Method:   makesurfaceIntervalFrame
    // Input:    /
    // Output:   /
    //*****************************************
    private void makesurfaceIntervalFrame()
    {
        JPanel buttonpannel;
        JButton addCustomButton;

        editSurfaceIntervalFrame= new JFrame("Surface Interval");
        editSurfaceIntervalFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        editSurfaceIntervalFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/VPMOpen.gif")));
        editSurfaceIntervalFrame.setLocation(xPos+40, yPos+50); 
        editSurfaceIntervalFrame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e) //the name must be exactly that
            {
                    updatesurfaceIntervalFields();
            }
        });
        JPanel editPannel = new JPanel();
        editPannel.setLayout(new GridLayout(3,1));
        editSurfaceIntervalFrame.add(editPannel);

        // hours
        buttonpannel=new JPanel();
        buttonpannel.add(new Label("   Hours:"));
        hoursText = new JTextField(2);
        hoursText.setText("");
        buttonpannel.add(hoursText);
        editPannel.add(buttonpannel);
        // minutes
        buttonpannel=new JPanel();
        buttonpannel.add(new Label("Minutes:"));
        minutesText = new JTextField(2);
        minutesText.setText("");
        buttonpannel.add(minutesText);
        editPannel.add(buttonpannel);

        // OK button 
        JPanel okpanel= new JPanel();
        editPannel.add(okpanel);
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            	  float min=0, hour=0;
            	  String str;
            	  
            	  str=hoursText.getText();
            	  if (str.length()>0)
            	      hour=Float.valueOf(str).floatValue();
            	  str=minutesText.getText();
            	  if (str.length()>0)
            	      min=Float.valueOf(str).floatValue();
            	  min+=60*hour;
            	  if (min>=0)
            	  {
            	      surfaceIntervals[currentDive]=Math.round(min);
            	      updatesurfaceIntervalFields();                
                    editSurfaceIntervalFrame.setVisible(false);
                }
                else
                {                	
                    MsgBox errorBox=new MsgBox();
                    errorBox.clearMessage();
                    errorBox.addMessageString("Time can not be negative");
                    errorBox.setVisible(true);
                }
            }
        });                                       // Ends addActionListener
        okButton.setPreferredSize(new Dimension(80, 30));
        okpanel.add(okButton);

        editSurfaceIntervalFrame.pack();
        editSurfaceIntervalFrame.setSize(150, 150);
        editSurfaceIntervalFrame.setVisible(false);
    }

    //*****************************************
    // Method:   filleditsurfaceIntervalTextButtons
    // Input:    /
    // Output:   /
    //*****************************************
    private void filleditsurfaceIntervalTextButtons()
    {
    	  hoursText.setText(""+surfaceIntervals[currentDive]/60);
    	  minutesText.setText(""+surfaceIntervals[currentDive]%60);
    }
    
    //*****************************************
    // Method:   updatesurfaceIntervalFields
    // Input:    /
    // Output:   /
    //*****************************************
    private void updatesurfaceIntervalFields()
    {
    	  if (currentDive==0)
    	  {
    	  	  surfaceintervalEditButton.setBackground(Color.white);
    	  	  surfaceintervalEditButton.setEnabled(false);
    	  	  surfaceintervalText.setText("  > 48 h");
    	  }
    	  else
    	  {
    	  	  surfaceintervalEditButton.setBackground(Color.green);
    	  	  surfaceintervalEditButton.setEnabled(true);
    	  	  surfaceintervalText.setText(""+surfaceIntervals[currentDive]/60+"h "+surfaceIntervals[currentDive]%60+"min\n");    	  	
    	  }
    }
     //*****************************************
    // Method:   frezzesurfaceIntervalButtons
    // Input:    /
    // Output:   /
    //*****************************************
    private void frezzesurfaceIntervalButtons()
    {
    	 surfaceintervalEditButton.setBackground(Color.white);
    	 surfaceintervalEditButton.setEnabled(false);    	
    }    
 //***************************************************************
 //
 //    EDIT DECO MIX STUFF
 //
 //***************************************************************
    private final int PREDEFINED_DECO_MIX_NUM=4;
    private int editDecoMixNumber; // which mix do we edit
    private JButton addmixbutton[]=new JButton[PREDEFINED_DECO_MIX_NUM];

    private double predefinedDecoMixO2[]= {100, 50, 35, 21};
    private double predefinedDecoMixHe[]= {  0,  0, 25, 35};
    private double predefinedDecoMixMOD[]={  6, 21, 36, 57};

    //*****************************************
    // Method:   sortDecoFields
    // Input:    /
    // Output:   /
    //*****************************************
    private void sortDecoFields()
    {
    // sorts deco fields from the deepest to the shallowest
        int i;
        double tmpdecomixO2, tmpdecomixHe, tmpfromdepth;

        for (i=1;i<MAX_DECOMIX;i++)
        {
            if (fromdepth[i][currentDive]>fromdepth[i-1][currentDive]) // if true switch them
            {
                 tmpdecomixO2=decomixO2[i][currentDive];
                 tmpdecomixHe=decomixHe[i][currentDive];
                 tmpfromdepth=fromdepth[i][currentDive];
                 decomixO2[i][currentDive]=decomixO2[i-1][currentDive];
                 decomixHe[i][currentDive]=decomixHe[i-1][currentDive];
                 fromdepth[i][currentDive]=fromdepth[i-1][currentDive];
                 decomixO2[i-1][currentDive]=tmpdecomixO2;
                 decomixHe[i-1][currentDive]=tmpdecomixHe;
                 fromdepth[i-1][currentDive]=tmpfromdepth;
                 i=0;
                 continue;                 // start from start again
            }
        }
    }

    //*****************************************
    // Method:   updateDecoFields
    // Input:    /
    // Output:   /
    //*****************************************
    private void updateDecoFields()
    {
    // updates deco fields buttons and text boxes
        int i;
        String str;

        for (i=0;i<MAX_DECOMIX;i++)
        {
            if (fromdepth[i][currentDive]<0)
                break;

            str=gasmixToString(decomixO2[i][currentDive]/100,decomixHe[i][currentDive]/100);
            decomixText[i].setText(str);
            str="From "+ Double.toString(fromdepth[i][currentDive])+depthUnit;
            decoModText[i].setText(str);

            decomixEditButton[i].setBackground(Color.green);
            decomixDelButton[i].setBackground(Color.red);
            decomixEditButton[i].setEnabled(true);
            decomixDelButton[i].setEnabled(true);
        }
        if (i<MAX_DECOMIX) // not all the fields are full
        {
            decomixEditButton[i].setBackground(Color.green);
            decomixDelButton[i].setBackground(Color.white);
            decomixEditButton[i].setEnabled(true);
            decomixDelButton[i].setEnabled(false);
            decomixText[i].setText("");
            decoModText[i].setText("");
            i++;
            for (;i<MAX_DECOMIX;i++)
            {
                decomixEditButton[i].setBackground(Color.white);
                decomixDelButton[i].setBackground(Color.white);
                decomixEditButton[i].setEnabled(false);
                decomixDelButton[i].setEnabled(false);
                decomixText[i].setText("");
                decoModText[i].setText("");
            }  
        }
        okProfileButton.setEnabled(true);
    }

    //*****************************************
    // Method:   frezzeDecoButtons
    // Input:    /
    // Output:   /
    //*****************************************
    private void frezzeDecoButtons()
    {
        int i;

        for (i=0;i<MAX_DECOMIX;i++)
        {
            decomixEditButton[i].setBackground(Color.white);
            decomixDelButton[i].setBackground(Color.white);
            decomixEditButton[i].setEnabled(false);
            decomixDelButton[i].setEnabled(false);
        }
        okProfileButton.setEnabled(false); 
    }

    //*****************************************
    // Method:   deleteDecoMiX
    // Input:    button that has been pressed
    // Output:   /
    //*****************************************
    private void deleteDecoMiX(ActionEvent e)
    {
        int i;

        for(i=0;i<MAX_DECOMIX;i++)
            if(e.getSource()==decomixDelButton[i])
                break;                          // i eq index of button pressed now

        fromdepth[i][currentDive]=-1;
        sortDecoFields();
        updateDecoFields();
    }

    //*****************************************
    // Method:   editDecoMiX
    // Input:    button that has been pressed
    // Output:   /
    //*****************************************
    private void editDecoMiX(ActionEvent e)
    {
        int i;

        for(i=0;i<MAX_DECOMIX;i++)
            if(e.getSource()==decomixEditButton[i])
                break;                            // i eq index of button pressed now

        editDecoMixNumber=i; // which mix do we edit

        frezzeDecoButtons();

    }

    //*****************************************
    // Method:   makeEditDecoFrame
    // Input:    /
    // Output:   /
    //*****************************************
    private void makeEditDecoFrame()
    {
        int i;
        JPanel buttonpannel;
        JButton addCustomButton;

        editDecoFrame= new JFrame("Edit Deco");
        editDecoFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        editDecoFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/VPMOpen.gif")));
        editDecoFrame.setLocation(xPos+40, yPos+120); 
        editDecoFrame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e) //the name must be exactly that
            {
                    updateDecoFields();
            }
        });
        JPanel editPannel = new JPanel();
        editPannel.setLayout(new GridLayout(PREDEFINED_DECO_MIX_NUM+3,1));
        editDecoFrame.add(editPannel);

        //add defoult mix buttons
        for (i=0;i<PREDEFINED_DECO_MIX_NUM;i++)
        {
            buttonpannel=new JPanel();
            addmixbutton[i] = new JButton(gasmixToString(predefinedDecoMixO2[i]/100,predefinedDecoMixHe[i]/100));
            addmixbutton[i].setPreferredSize(new Dimension(120, 30));
            addmixbutton[i].addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    int j,k;

                    for(j=0;j<MAX_DECOMIX;j++)
                        if(e.getSource()==addmixbutton[j])
                            break;                           // j eq index defoult mix button pressed
                    
                    for(k=0;k<MAX_DECOMIX;k++)               // check if mix for this mod exists
                    {
                        if (k==editDecoMixNumber)            // do not check the mix edited
                            continue;

                        if (fromdepth[k][currentDive]==predefinedDecoMixMOD[j]) // error
                         {
                             MsgBox errorBox=new MsgBox();
                             errorBox.clearMessage();
                             errorBox.addMessageString("Deco mix for "+fromdepth[k][currentDive]+depthUnit+" already added");
                             errorBox.setVisible(true);
                             k=-1;
                             break;                            
                         }
                    }

                    if (k!=-1) // no error
                    {   
                        decomixO2[editDecoMixNumber][currentDive]= predefinedDecoMixO2[j];
                        decomixHe[editDecoMixNumber][currentDive]= predefinedDecoMixHe[j];
                        fromdepth[editDecoMixNumber][currentDive]= predefinedDecoMixMOD[j];
                    }
                    sortDecoFields();
                    updateDecoFields();
                    editDecoFrame.setVisible(false);
                }
            });                                       // Ends addActionListener
            buttonpannel.add(addmixbutton[i]);
            editPannel.add(buttonpannel);
        }

        // custom mix
        buttonpannel=new JPanel(); // make empty spot
        editPannel.add(buttonpannel);

        buttonpannel=new JPanel();
        buttonpannel.add(new Label("fO2:"));
        customfO2Text = new JTextField(4);
        customfO2Text.setText(""+customDecoMixF02);
        buttonpannel.add(customfO2Text);
        buttonpannel.add(new Label("    fHe:"));
        customfHeText = new JTextField(4);
        customfHeText.setText(""+customDecoMixFHe);
        buttonpannel.add(customfHeText);
        buttonpannel.add(new Label("MOD:"));
        custommodText = new JTextField(4);
        custommodText.setText(""+customDecoMixMOD);
        buttonpannel.add(custommodText);
        editPannel.add(buttonpannel);

        buttonpannel=new JPanel();
        addCustomButton=new JButton("Custom Mix");
        addCustomButton.setPreferredSize(new Dimension(120, 30));
        addCustomButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int k;

                double mod=Double.valueOf(custommodText.getText()).doubleValue();
                double fO2=Double.valueOf(customfO2Text.getText()).doubleValue();
                double fHe=Double.valueOf(customfHeText.getText()).doubleValue();
                mod=Math.rint(mod*10)/10;
                fO2=Math.rint(fO2*10)/10;
                fHe=Math.rint(fHe*10)/10;
                    
                for(k=0;k<MAX_DECOMIX;k++)
                {
                    if (k==editDecoMixNumber)            // do not check the mix edited
                        continue;

                    if (fromdepth[k][currentDive]==mod) // error - mix for mod already exists
                     {
                         MsgBox errorBox=new MsgBox();
                         errorBox.clearMessage();
                         errorBox.addMessageString("Deco mix for "+fromdepth[k][currentDive]+depthUnit+" already added");
                         errorBox.setVisible(true);
                         k=-1;
                         break;                            
                     }
                }

                if ((fO2<2) || (fHe<0) || ((fHe+fO2)>100))
                {
                     MsgBox errorBox=new MsgBox();
                     errorBox.clearMessage();
                     errorBox.addMessageString("Deco mix not breathable");
                     errorBox.setVisible(true);
                     k=-1;
                }

                if (k!=-1) // no error
                {   
                    decomixO2[editDecoMixNumber][currentDive] = customDecoMixF02 = fO2;
                    decomixHe[editDecoMixNumber][currentDive] = customDecoMixFHe = fHe;
                    fromdepth[editDecoMixNumber][currentDive] = customDecoMixMOD = mod;
                }
                // error or not
                customfO2Text.setText(""+customDecoMixF02);
                customfHeText.setText(""+customDecoMixFHe);
                custommodText.setText(""+customDecoMixMOD);

                sortDecoFields();
                updateDecoFields();
                editDecoFrame.setVisible(false);
            }
        });                                       // Ends addActionListener
        buttonpannel.add(addCustomButton);
        editPannel.add(buttonpannel);

        editDecoFrame.pack();
        editDecoFrame.setSize(350, 300);
        editFrame.setVisible(false);
    }

 //***************************************************************
 //
 //     MAKE MAIN WINDOW
 //
 //***************************************************************
    private void createAndShowGUI()
    {
        JPanel basicpannel, configpannel, editpannel, buttonsbar, profile, deco;
        JScrollPane output;
        JButton configbutton, editbutton, calculatebutton, printbutton, divecalcbutton, aboutbutton, exitbutton;

        //JFrame.setDefaultLookAndFeelDecorated(true);

        final About aboutWindow=new About("/img/VPMOpen.gif");
        aboutWindow.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/VPMOpen.gif")));
        aboutWindow.setText("\n\n VPMOpen version "+version+"\n\n"+
            "\tCopyright "+ (char)169 +" 2006-2015 Jurij Zeli"+(char)269+
            "\n\n\tvpm.open@gmail.com"+
            "\n\thttp://webspace.webring.com/people/fv/vpmopen/");
        aboutWindow.setVisible(true);
        
        //Create and set up the window.
        final JFrame dlframe = new JFrame();
        dlframe.setTitle("VPMOpen     -     Dive #"+(currentDive+1));
        dlframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dlframe.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/VPMOpen.gif")));
        
        xPos = (dlframe.getToolkit().getScreenSize().width-wwidh)/2;
        yPos = (dlframe.getToolkit().getScreenSize().height-whighth)/2;
        
        basicpannel = new JPanel();
        basicpannel.setLayout(new BorderLayout());
        dlframe.add(basicpannel);

        //Create Baker
        bakerB=new BakerVpmB();
        //Create diveTools;
        diveTools=new DiveTools();

        //make buttons bar
        buttonsbar = new JPanel();
        buttonsbar.setBorder(BorderFactory.createLineBorder(Color.gray,2));
        //buttonsbar.setBorder(BorderFactory.createTitledBorder(""));
        basicpannel.add(buttonsbar, BorderLayout.NORTH);

        //output TextArea
        outputText = new JTextArea();
        outputText.setFont(new Font("SansSerif", Font.PLAIN, 11));
        outputText.setForeground(Color.BLACK);
        outputText.setLineWrap(true);
        outputText.setWrapStyleWord(true);
        outputText.setTabSize(8);
        outputText.setEditable(false);
        outputText.setBorder(BorderFactory.createTitledBorder("Welcome"));
        outputText.setPreferredSize(new Dimension(wwidh-20, whighth-80));
        outputText.append("VPMOpen - Version "+version+", Copyright "+ (char)169 +"  2006-2013 Jurij Zeli"+ (char)269 +"\n\n"+
                          "This program comes with ABSOLUTELY NO WARRANTY.\n\n"+
                          "This is free software, and you are welcome to redistribute it "+
                          "under the terms of GNU General Public License.\n"+
                          "Please send Your comments, suggestions and improvements to vpm.open@gmail.com\n\n"+
                          "This program uses VPM code by Erik C. Baker.\n"+
                          "Porting to Java, user interface and Enhanced Multilevel Algorithm has been made by J. Zeli"+ (char)269 +".\n\n");
        
        //make dive output window
        output = new JScrollPane(outputText);
        output.setBorder(BorderFactory.createLineBorder(Color.gray,2));
        //output.setBorder(BorderFactory.createTitledBorder(""));        
        basicpannel.add(output, BorderLayout.CENTER);

        
        //make config button and frame
        configbutton = new JButton();
        configbutton.setText("Config");
        configbutton.setToolTipText("Settings and Configuration");
        configbutton.setPreferredSize(new Dimension(78, 30));
        configbutton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                configWindow.setVisible(true);
            }
        });                                       // Ends addActionListener

        configWindow=new ConfigWindow();
        configWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        configWindow.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/VPMOpen.gif")));
        configWindow.setLocation(xPos+20, yPos+20); 

        //make previous and next button and frame
        mainBarPpreviousbutton = new JButton();
        mainBarNextbutton = new JButton();
        
        mainBarPpreviousbutton.setText("<");
        mainBarPpreviousbutton.setToolTipText("Previous Dive");
        mainBarPpreviousbutton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                currentDive--;
                mainBarNextbutton.setEnabled(true);
                editFrameNextbutton.setEnabled(true);
                if (currentDive<=0)
                {
                    mainBarPpreviousbutton.setEnabled(false);
                    editFramePpreviousbutton.setEnabled(false);
                }
                dlframe.setTitle("VPMOpen     -     Dive #"+(currentDive+1));
                editFrame.setTitle("Edit Dive #"+(currentDive+1));
                updatesurfaceIntervalFields();
                updateprofilePointFields();
                updateDecoFields();
            }
        });                      
        mainBarPpreviousbutton.setPreferredSize(new Dimension(50, 30));
        mainBarPpreviousbutton.setEnabled(false);
        
        mainBarNextbutton.setText(">");
        mainBarNextbutton.setToolTipText("Next Dive");
        mainBarNextbutton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                currentDive++;
                mainBarPpreviousbutton.setEnabled(true);
                editFramePpreviousbutton.setEnabled(true);
                if (currentDive>=MAX_DIVES-1)
                {
                    mainBarNextbutton.setEnabled(false);
                    editFrameNextbutton.setEnabled(false);
                }
                dlframe.setTitle("VPMOpen     -     Dive #"+(currentDive+1));
                editFrame.setTitle("Edit Dive #"+(currentDive+1));
                updatesurfaceIntervalFields();
                updateprofilePointFields();
                updateDecoFields();
            }
        });                      
        mainBarNextbutton.setPreferredSize(new Dimension(50, 30));  
              
        //make edit button and frame
        editbutton = new JButton();
        editbutton.setText("Edit");
        editbutton.setToolTipText("Edit Dive Profile and Deco Mix");

        editFrame = new JFrame();
        editFrame.setTitle("Edit Dive #"+(currentDive+1));
        editFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        editFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/VPMOpen.gif")));
        editFrame.setLocation(xPos+20, yPos+20); 
        editbutton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                sortDecoFields();
                updatesurfaceIntervalFields();
                updateDecoFields();
                updateprofilePointFields();
                editFrame.setVisible(true);
            }
        });                                       // Ends addActionListener

        //make edit profile panel
        profile = new JPanel();
        profile.setLayout(new GridLayout(MAX_DIVEPONTS+MAX_DECOMIX+3,1));
        editFrame.add(profile, BorderLayout.CENTER);        //make edit button and frame

        editFrame.pack();
        editFrame.setSize(280, 460);
        editFrame.setVisible(false);
        editbutton.setPreferredSize(new Dimension(75, 30));
        
        // Surface interval
        JPanel surfaceintervalPanel= new JPanel();
        profile.add(surfaceintervalPanel);
        surfaceintervalPanel.add(new Label("Surface Interval:"));
        surfaceintervalText=new JTextField(6);
        surfaceintervalText.setEditable(false);
        surfaceintervalPanel.add(surfaceintervalText);
        surfaceintervalEditButton= new JButton("");
        surfaceintervalEditButton.setToolTipText("Edit Surface Interval");
        surfaceintervalEditButton.setPreferredSize(new Dimension(15, 15));
        surfaceintervalEditButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                filleditsurfaceIntervalTextButtons();
                frezzesurfaceIntervalButtons();
                editSurfaceIntervalFrame.setVisible(true);
            }
        });                                       //                       
        surfaceintervalPanel.add(surfaceintervalEditButton); 
        
        int i;
        // add divepoints
        profile.add(new Label("Dive Points:"));
        for (i=0;i<MAX_DIVEPONTS;i++)
        {
            JPanel profilePointPanel= new JPanel();
            profile.add(profilePointPanel);

            profilepointText[i]=new JTextField(10);
            profilepointText[i].setEditable(false);
            profilePointPanel.add(profilepointText[i]);

            profilepointMix[i]=new JTextField(6);
            profilepointMix[i].setEditable(false);
            profilePointPanel.add(profilepointMix[i]);

            profilePointEditButton[i] = new JButton("");
            profilePointEditButton[i].setToolTipText("Edit Dive Point");
            profilePointEditButton[i].setPreferredSize(new Dimension(15, 15));
            profilePointEditButton[i].addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    int i;

                    for(i=0;i<MAX_DIVEPONTS;i++)
                        if(e.getSource()==profilePointEditButton[i])
                            break;                            // i eq index of button pressed

                   editDivePointNumber=i;
                   divePointAction=0;
                   frezzeprofilePointButtons();
                   filleditBottomTextButtons(0);
                   editprofilePointFrame.setVisible(true);
                   editprofilePointFrame.setTitle("Edit Dive Point");
                }
            });                                       // Ends addActionListener
            profilePointPanel.add(profilePointEditButton[i]);

            profilePointDelButton[i] = new JButton("");
            profilePointDelButton[i].setToolTipText("Delete Dive Point");
            profilePointDelButton[i].setPreferredSize(new Dimension(15, 15));
            profilePointDelButton[i].addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    int i;

                    for(i=0;i<MAX_DIVEPONTS;i++)
                        if(e.getSource()==profilePointDelButton[i])
                            break;                            // i eq index of button pressed

                    deleteprofilePoint(i);
                    updateprofilePointFields();
                }
            });                                       // Ends addActionListener
            profilePointPanel.add(profilePointDelButton[i]);

            profilePointInsButton[i] = new JButton("");
            profilePointInsButton[i].setToolTipText("Insert New Dive Point");
            profilePointInsButton[i].setPreferredSize(new Dimension(15, 15));
            profilePointInsButton[i].addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    int i;

                    for(i=0;i<MAX_DIVEPONTS;i++)
                        if(e.getSource()==profilePointInsButton[i])
                            break;                            // i eq index of button pressed
                   
                   editDivePointNumber=i;
                   divePointAction=1;
                   frezzeprofilePointButtons();
                   filleditBottomTextButtons(1);
                   editprofilePointFrame.setVisible(true);
                   editprofilePointFrame.setTitle("Insert Dive Point");
                }
            });                                       // Ends addActionListener
            profilePointPanel.add(profilePointInsButton[i]);
        }

        // add decomix 
        profile.add(new Label("Deco Mix:"));
        for (i=0;i<MAX_DECOMIX;i++)
        {
            JPanel decomixPanel= new JPanel();
            profile.add(decomixPanel);

            decomixText[i]=new JTextField(6);
            decomixText[i].setEditable(false);
            decomixPanel.add(decomixText[i]);

            decoModText[i]=new JTextField(10);
            decoModText[i].setEditable(false);
            decomixPanel.add(decoModText[i]);

            decomixEditButton[i] = new JButton("");
            decomixEditButton[i].setToolTipText("Edit Deco Mix");
            decomixEditButton[i].setPreferredSize(new Dimension(15, 15));            
            decomixEditButton[i].addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    editDecoMiX(e);
                    editDecoFrame.setVisible(true);
                }
            });                                       // Ends addActionListener
            decomixPanel.add(decomixEditButton[i]);

            decomixDelButton[i] = new JButton("");
            decomixDelButton[i].setToolTipText("Delete Deco Mix");
            decomixDelButton[i].setPreferredSize(new Dimension(15, 15));
            decomixDelButton[i].addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    deleteDecoMiX(e);
                }
            });                                       // Ends addActionListener
            decomixPanel.add(decomixDelButton[i]);
        }

        // OK button of edit window
        JPanel okpanel= new JPanel();
        editFrame.add(okpanel, BorderLayout.SOUTH);
        okProfileButton = new JButton("OK");
        okProfileButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {                
                editFrame.setVisible(false);
            }
        });                                       // Ends addActionListener
        okProfileButton.setPreferredSize(new Dimension(80, 30));
        
        editFramePpreviousbutton = new JButton();
        editFrameNextbutton = new JButton();
        
        editFramePpreviousbutton.setText("<");
        editFramePpreviousbutton.setToolTipText("Previous Dive");
        editFramePpreviousbutton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                currentDive--;
                mainBarNextbutton.setEnabled(true);
                editFrameNextbutton.setEnabled(true);
                if (currentDive<=0)
                {
                    mainBarPpreviousbutton.setEnabled(false);
                    editFramePpreviousbutton.setEnabled(false);
                }
                dlframe.setTitle("VPMOpen     -     Dive #"+(currentDive+1));
                editFrame.setTitle("Edit Dive #"+(currentDive+1));
                updatesurfaceIntervalFields();
                updateprofilePointFields();
                updateDecoFields();
            }
        });                      
        editFramePpreviousbutton.setPreferredSize(new Dimension(50, 30));
        editFramePpreviousbutton.setEnabled(false);
        
        editFrameNextbutton.setText(">");
        editFrameNextbutton.setToolTipText("Next Dive");
        editFrameNextbutton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                currentDive++;
                mainBarPpreviousbutton.setEnabled(true);
                editFramePpreviousbutton.setEnabled(true);
                if (currentDive>=MAX_DIVES-1)
                {
                    mainBarNextbutton.setEnabled(false);
                    editFrameNextbutton.setEnabled(false);
                }
                dlframe.setTitle("VPMOpen     -     Dive #"+(currentDive+1));
                editFrame.setTitle("Edit Dive #"+(currentDive+1));
                updatesurfaceIntervalFields();
                updateprofilePointFields();
                updateDecoFields();
            }
        });                      
        editFrameNextbutton.setPreferredSize(new Dimension(50, 30));
                       
        okpanel.add(editFramePpreviousbutton);
        okpanel.add(okProfileButton);
        okpanel.add(editFrameNextbutton);

        updatesurfaceIntervalFields();
        updateprofilePointFields();
        updateDecoFields();

        makeeditprofilePointFrame();
        makesurfaceIntervalFrame();
        makeEditDecoFrame();     

        // other buttons
        calculatebutton = new JButton("Dive");
        calculatebutton .setToolTipText("Calculate Dive Profile");
        calculatebutton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {                
                makeADive();
            }
        });                                       // Ends addActionListener
        calculatebutton.setPreferredSize(new Dimension(75, 30));
        calculatebutton.setEnabled(true);        //unfreze the button
        printbutton = new JButton("Print");
        printbutton.setToolTipText("Print Dive Profile");
        printbutton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {                
               printScreen();
            }

        }); // Ends addActionListener
        printbutton.setPreferredSize(new Dimension(75, 30));
        printbutton.setEnabled(true);        //unfreze the button
        aboutbutton = new JButton("About");
        aboutbutton.setToolTipText("About the Program");
        aboutbutton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {                
                aboutWindow.setVisible(true);
            }
        });                                       // Ends addActionListener
        aboutbutton.setPreferredSize(new Dimension(75, 30));
        divecalcbutton = new JButton("Calc");
        divecalcbutton.setToolTipText("Optimal Mix Calculator");
        divecalcbutton.setPreferredSize(new Dimension(75, 30));
        divecalc = new DiveCalc();
        divecalc.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        divecalc.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/VPMOpen.gif")));
        divecalcbutton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {                
                divecalc.setVisible(true);
            }
        });                                       // Ends addActionListener
        exitbutton = new JButton("Exit");
        exitbutton.setToolTipText("Exit the Program");
        exitbutton.setPreferredSize(new Dimension(75, 30));
        exitbutton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            	  saveConfiguration();
                System.exit(0);
            }
        });                                       // Ends addActionListener
        buttonsbar.add(configbutton);
        buttonsbar.add(mainBarPpreviousbutton);
        buttonsbar.add(editbutton);
        buttonsbar.add(calculatebutton);
        buttonsbar.add(mainBarNextbutton);
        buttonsbar.add(printbutton);
        buttonsbar.add(divecalcbutton);
        buttonsbar.add(aboutbutton);
        buttonsbar.add(exitbutton);

        //Display the main window        
        dlframe.setLocation(xPos, yPos);    
        dlframe.pack();
        dlframe.setSize(wwidh, whighth);
        dlframe.setVisible(true);
        
        aboutWindow.setVisible(false);
        aboutWindow.makeOKButton();

        // load data
        loadConfiguration();
    }

 //***************************************************************
 //
 //     MAIN METHOD
 //
 //***************************************************************
    public static void main(String[] args)
    {
        Vpm application = new Vpm();
    }
    
    public Vpm()
    {
    	    createAndShowGUI();
    }

}

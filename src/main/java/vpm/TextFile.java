//********************************************************************************
//
//    TextFile - Simple Text file read write program
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

import java.io.*;

public class TextFile extends File
{
    public int FILE_READ=1;
    public int FILE_WRITE=2;
    private boolean readFlag=false, writeFlag=false;
    FileReader readFD;
    FileWriter writeFD;
    String fileWithPath;
    
    public TextFile(String file)
    {
        super(file);
        fileWithPath=file;
    }

    public boolean open(int flags)
    {
    	  if (exists()==false)
    	      return false;
    	  if (isFile()==false)
    	      return false;
    	  if (( (flags&FILE_READ)>0 ) && (canRead()==false))
    	      return false;
    	  if (( (flags&FILE_WRITE)>0 ) && (canWrite()==false))
    	      return false;
    	      
    	  if ((flags&FILE_READ)>0)
    	  { 
    	  	  try 
    	  	  {
    	          readFD=new FileReader(fileWithPath);
    	          readFlag=true;
    	      }                                       
            catch (IOException e)
            {                   
                e.printStackTrace();                    
            }                                  
    	  }
    	  if ((flags&FILE_WRITE)>0)
    	  {
    	  	  try 
    	  	  {    	  	
    	          writeFD=new FileWriter(fileWithPath);
    	          writeFlag=true;
    	      }
            catch (IOException e)
            {                   
                e.printStackTrace();                    
            }                    
    	  }
    	      
    	  return true;
    }

    public boolean create(int flags)
    {
        boolean rv=false;

    	        try
    	{         
            rv=createNewFile();
    	}
        catch (IOException e)
        {                   
            e.printStackTrace();                    
        }
        if (rv)
            rv=open(flags);

        return rv;                   
    }

    public boolean close()
    {
    	  boolean rv=false;
    	  
    	  if (readFlag)
    	  {
    	  	  try 
    	  	  { 
    	  	      rv=true;
    	          readFlag=false;
    	          readFD.close();
    	      }
            catch (IOException e)
            {                   
                e.printStackTrace();                    
            }
    	  }
    	  if (writeFlag)
    	  {
    	  	  try 
    	  	  { 
    	  	      rv=true;
    	          writeFlag=false;
                  writeFD.flush();
    	          writeFD.close();
    	      }
            catch (IOException e)
            {                   
                e.printStackTrace();                    
            }
    	  }
    	  return rv;
    }
        
    public void writeStr(String text)
    {
    	  int len;
    	  
    	  if (!writeFlag)
    	      return;
    	      
    	  len=text.length();
    	  try 
    	  {     	  
    	      writeFD.write(text,0,len);
    	  }
        catch (IOException e)
        {                   
            e.printStackTrace();                    
        }    	      
    }

    public String readLine()
    {
    	  String rv="";
    	  char ch;

    	  if (!readFlag)
    	      return rv;
    	          	  
    	  for(;;)
    	  {
            ch=readByte();
            if (ch==(char)13) 
            //     ctrl
                continue; //there should be LF next - except this is MAC :-)
            else if (ch==(char)10)
                break;    //end of line
            else if (ch==(char)0)
                break;    // end of file
            rv+=ch;
        }
    	  
    	  return rv;
    }
    public String readWord()
    {
    	  String rv="";
    	  char ch;

    	  if (!readFlag)
    	      return rv;
    	          	  
    	  for(;;)
    	  {
            ch=readByte();
            if (ch==(char)13) 
            //     ctrl
                continue; //there should be LF next
            else if ( (ch==(char)10) || (ch==(char)0) || (ch==(char)9) || (ch==(char)32) )
            //             LF               EOF             TAB              space
                break;    //end of word
            rv+=ch;
        }
    	  
    	  return rv;
    }

    public char readByte()
    {
    	  int intchar=0;
    	  char ch=(char)0;
    	  
    	  if (!readFlag)
    	      return ch;
    	  try 
    	  {
        	  intchar=readFD.read();
    	  }                                       
        catch (IOException e)
        {                   
            e.printStackTrace();                    
        }           
    	  if (intchar>=0) // not EOF
    	      ch=(char)intchar;       	  
    	  return ch;
    }

}
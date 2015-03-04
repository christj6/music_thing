//////////////////////////////////////////////
//                Midi2Text
//
// Converts a MIDi file to a tab-delimeted
// text file. useful for importing into
// a spreadsheet for statistical analysis.
//
// (c) 2005 Andrew R. Brown
// 
// This application is built using the jMusic
// library,m and hence this code is distibuted
// under the GPL license (see below).
//
//////////////////////////////////////////////

/*
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or any
later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

*/

import jm.JMC;
import jm.music.data.*;
import jm.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Enumeration;


public class Midi2Text
{
    //private JFrame window;
    //private JButton convert;

    public static void main(String[] args) 
    { 
		// new Midi2Text();
		//convert.setEnabled(false);
	    convert(args[0]);
	    //convert.setEnabled(true);
    }

    public static void convert(String filename) 
    {
		Score s = new Score();
		Read.midi(s, filename);
		// open text file
		try 
		{
			String[] temp = filename.split("\\."); // change filename from example.mid to example.txt
		    FileWriter textFile = new FileWriter(temp[0] + ".txt");
		    // textFile.write("Start Time" + "\t" + "Pitch" + "\t" + "Duration" + "\t" + "Dynamic" + "\n");
		    // read note data and convert
		    // get data values
		    Enumeration enum1 = s.getPartList().elements(); // changed "enum" to "enum1" to avoid the keyword error
		    while(enum1.hasMoreElements())
		    {
				Part part = (Part) enum1.nextElement();
				Enumeration enum2 = part.getPhraseList().elements();
				while(enum2.hasMoreElements())
				{
				    Phrase phrase = (Phrase) enum2.nextElement();
				    double startTime = phrase.getStartTime(); 
				    Enumeration enum3 = phrase.getNoteList().elements();
				    while(enum3.hasMoreElements())
				    {
						Note note = (Note) enum3.nextElement();

						if (note.getPitch() != JMC.REST) {
						    // start time
						    // textFile.write(Double.toString(startTime) + "\t");
						    // pitch
						    textFile.write(Integer.toString(note.getPitch()) + "\t");
						    // duration
						    // textFile.write(Double.toString(note.getDuration()) + "\t");
						    // velocity
						    // textFile.write(Integer.toString(note.getDynamic()) + "\n");
						}

						startTime += note.getDuration();
				    }
				}
		    }

		    textFile.close();
		} 
		catch(IOException e) 
		{
		    System.err.println(e);
		}    
    }

}

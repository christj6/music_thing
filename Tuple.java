
/*
    Tuple.java
    
    This class stores a possible position for a left hand finger to go on the fretboard.
    It is comprised of a string # and a fret #. 

    String values range from 1 to 6. Fret values range from 0 to 20.

    If the fret # is 0, the tuple doesn't require a finger to be played, since it's an open note.
*/

import jm.JMC;
import jm.music.data.*;
import jm.music.tools.*;
import jm.midi.*;
import jm.util.*;

import java.io.*;
import java.util.*;

public class Tuple implements Comparable<Tuple>
{
	private int stringNum;
	private int fretNum;

	// constants
    public static final int LOW_E_STRING = 40; // string #6, aka lowest possible note
    public static final int A_STRING = 45;
    public static final int D_STRING = 50;
    public static final int G_STRING = 55;
    public static final int B_STRING = 59;
    public static final int HIGH_E_STRING = 64; // pitch of string #1

    public static final int HIGHEST_POSSIBLE_NOTE = 84;

    public Tuple()
    {
    	// empty constructor
    }

    // takes in string and fret #s (in that order) and creates a Tuple object
	public Tuple(int stringNum, int fretNum) 
	{
		this.stringNum = stringNum;
		this.fretNum = fretNum;
	}

	// setters and getters for main attributes
	public int getStringNum()
	{
		return stringNum;
	}

	public int getFretNum()
	{
		return fretNum;
	}

	public void setStringNum(int stringNum)
	{
		this.stringNum = stringNum;
	}

	public void setFretNum(int fretNum)
	{
		this.fretNum = fretNum;
	}

	// takes in string and fret number, returns pitch value
	public int getPitch (int stringNum, int fretNum)
	{
		switch(stringNum)
		{
			case 1:
				return HIGH_E_STRING + fretNum;
			case 2:
				return B_STRING + fretNum;
			case 3:
				return G_STRING + fretNum;
			case 4:
				return D_STRING + fretNum;
			case 5:
				return A_STRING + fretNum;
			case 6:
				return LOW_E_STRING + fretNum;
			default:
				return -1;
		}
	}

	// sort positions based on where they are on the fretboard
	public int compareTo(Tuple other) 
    {
        if (this.getFretNum() > other.getFretNum()) 
        {
            return 1;
        } 
        else if (this.getFretNum() < other.getFretNum()) 
        {
            return -1;
        }
        return 0;
    }

    // outputs a Tuple to the command line in the form of
    // "string x, fret y"
    public String toString()
    {
    	String output = "";
    	if (stringNum == -1 && fretNum == -1)
    	{
    		output += "nothing\n";
    	}
    	else
    	{
    		output += "string " + stringNum + ", fret " + fretNum + "\n";
    	}
    	return output;
    }
}

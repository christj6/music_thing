
// Tuple class for left hand finger position on guitar
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
	public static final int maximumPolyphony = 6; // 6 strings, can be strummed simultaneously

    public static final int lowEString = 40; // string #6, aka lowest possible note
    public static final int aString = 45;
    public static final int dString = 50;
    public static final int gString = 55;
    public static final int bString = 59;
    public static final int highEString = 64; // pitch of string #1

    public static final int highestPossibleNote = 84;

    public Tuple()
    {
    	// empty constructor
    }

	public Tuple(int stringNum, int fretNum) 
	{
		this.stringNum = stringNum; // maybe have something where if the string number is something weird, that means the position is part of a barre chord
		this.fretNum = fretNum;
	}

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

	// modify a tuple so it goes up or down some # of strings
	public void shift (int shift)
	{
		int originalPitch = this.getPitch(this.stringNum, this.fretNum);

		int newStringNum = this.stringNum + shift;

		if (newStringNum <= 6 && newStringNum >= 0)
		{
			int newFretNum = -1;

			for (int i = 0; i < (highestPossibleNote - highEString); i++)
			{
				if (this.getPitch(newStringNum, i) == originalPitch)
				{
					newFretNum = i;

					i = highestPossibleNote - highEString; // trip loop when fret # is found
				}
			}

			if (newFretNum > -1) // if the fret # is not found, don't modify the fretNum or stringNum
			{
				this.setStringNum(newStringNum);
				this.setFretNum(newFretNum);
			}
		}
	}

	// takes in string and fret number, returns pitch value
	public int getPitch (int stringNum, int fretNum)
	{
		switch(stringNum)
		{
			case 1:
				return highEString + fretNum;
			case 2:
				return bString + fretNum;
			case 3:
				return gString + fretNum;
			case 4:
				return dString + fretNum;
			case 5:
				return aString + fretNum;
			case 6:
				return lowEString + fretNum;
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

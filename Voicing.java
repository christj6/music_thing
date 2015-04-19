
import jm.JMC;
import jm.music.data.*;
import jm.music.tools.*;
import jm.midi.*;
import jm.util.*;

import java.io.*;
import java.util.*;

// function will take LinkedList of Note objects as input, create a Voicing object if the chord is valid (can be played by a human)
public class Voicing
{
	// up to 4 notes, each associated with their own left (1 = index, 2 = middle, 3, 4) and right (p = thumb, i = index, m, a) hand fingers
	// also associated with a position on the fretboard (some notes can appear in more than one location on the fretboard)
	// if the note is open, no left-hand fingering is required.
	// Once a string is used for a note (fretted or open), no other note can use that string. If there exist no other strings for that note, the entire voicing is disqualified.
	// If we are estimating the distance between two positions (based on average of left hand finger positions?), don't include the 0 fret for open strings
	
	// attributes
	private Tuple[] lhFingers = new Tuple[] {new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1)};
	private Tuple[] fretboard = new Tuple[] {new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1)};
	private int[] rhFingers = {-1, -1, -1, -1}; // p, i, m, a -- # refers to string plucked
	private Double[] expirations = {-1.0, -1.0, -1.0, -1.0, -1.0, -1.0};

	private int[] stringsUsed = {0, 0, 0, 0, 0, 0}; // 

	private LinkedList<Note> chord;
	private Double avgDistance = 0.0;
	// private int value; // will be changed frequently when computing transitions for each possible chord voicing


	// constants
	public static final int maximumPolyphony = 6; // 6 strings, can be strummed simultaneously

    public static final int lowEString = 40; // string #6, aka lowest possible note
    public static final int aString = 45;
    public static final int dString = 50;
    public static final int gString = 55;
    public static final int bString = 59;
    public static final int highEString = 64; // pitch of string #1

    public static final int highestPossibleNote = 84;

	public Voicing()
	{
		// empty constructor
	}

	// main constructor
	public Voicing(Tuple[] fretboard, Tuple[] lhFingers, LinkedList<Note> chord)
	{
		this.chord = chord;
		this.fretboard = fretboard;
		this.lhFingers = lhFingers;		
	}

	// setters
	public void assignLhFinger (int fingerNum, int stringNum, int fretNum)
	{
		
	}

	public void assignRhFingers()
	{
		// only call this after left hand fingers have been assigned
		for (int i = 0; i < stringsUsed.length; i++)
		{
			if (stringsUsed[i] == 1)
			{
				for (int j = 0; j < rhFingers.length; j++)
				{
					if (rhFingers[j] == -1)
					{
						rhFingers[j] = i+1;
						j = rhFingers.length;
					}
				}
			}
		}
	}

	public void resetLhFingers()
	{
		for (int i = 0; i < lhFingers.length; i++)
		{
			lhFingers[i] = new Tuple(-1, -1);
		}
	}

	public void resetRhFingers()
	{
		for (int i = 0; i < stringsUsed.length; i++)
		{
			stringsUsed[i] = 0;
		}

		for (int i = 0; i < rhFingers.length; i++)
		{
			rhFingers[i] = -1;
		}
	}

	// takes in a set of Tuples representing left hand finger positions, determines if it's possible for the hand to make that shape
    public boolean chordTester()
    {
    	List<Integer> occupiedFrets = new ArrayList<Integer>();

        // first round of checks
        for (int i = 0; i < lhFingers.length; i++)
        {
            int currentFret = lhFingers[i].getFretNum();

            if (currentFret > 0) // watch for open strings (0) and unassigned fingers (-1)
            {
            	occupiedFrets.add(currentFret);
            }
        }

        for (int i = 0; i < occupiedFrets.size() - 1; i++)
        {
        	int currentFret = occupiedFrets.get(i);
        	int nextFret = occupiedFrets.get(i+1);

        	if (nextFret < currentFret)
        	{
        		return false;
        	}
        }

        // check that each string gets only one note each
        /*
        int[] noteCount = new int[] {0, 0, 0, 0, 0, 0};

        for (int i = 0; i < lhFingers.length; i++)
        {
            int currentString = lhFingers[i].getStringNum(); // stringNum goes from 1 to 6, unless unset, in which case it's -1 or 0

            if (currentString >= 1 && currentString <= 6)
            {
                noteCount[currentString-1]++;

                if (noteCount[currentString-1] > 1)
                {
                    return false;
                }
            }
        }
        */



        return true;
    }

    // calc avg distance of fingers from tail of guitar
    public void avgDistance()
    {
    	Double sum = 0.0;
    	int stringsUsed = 0;

    	for (int i = 0; i < fretboard.length; i++)
    	{
    		if (fretboard[i].getFretNum() > 0)
    		{
    			sum += fretboard[i].getFretNum();
    			stringsUsed++;
    		}
    	}

    	if (stringsUsed > 0)
    	{
    		this.avgDistance = sum / stringsUsed;
    	}
    }

    // output object as string, for debug purposes
    public String toString()
    {
    	String output = "";
    	output += "left index holds " + lhFingers[0].toString();
    	output += "left middle holds " + lhFingers[1].toString();
    	output += "left ring holds " + lhFingers[2].toString();
    	output += "left pinkie holds " + lhFingers[3].toString();

    	output += "right thumb plucks string " + rhFingers[0] + "\n";
    	output += "right index plucks string " + rhFingers[1] + "\n";
    	output += "right middle plucks string " + rhFingers[2] + "\n";
    	output += "right ring plucks string " + rhFingers[3] + "\n";

    	this.avgDistance();
    	// output += "avg distance of fingers from tail: " + this.avgDistance;

    	return output;
    }

	// some sort of compare method
}

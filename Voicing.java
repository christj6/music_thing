
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
	// private LinkedList<Note> chord;
	// private int numberOfNotes;

	private Tuple[] lhFingers = new Tuple[] {new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1)};
	private int[] rhFingers = {-1, -1, -1, -1}; // p, i, m, a -- # refers to string plucked
	private Double[] expirations = {-1.0, -1.0, -1.0, -1.0};


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

	public Voicing(Tuple[] lhFingers)
	{		
		this.lhFingers = lhFingers;
	}

	// constructor
	public Voicing(Tuple[] lhFingers, Double[] expirations, int[] rhFingers)
	{		
		this.lhFingers = lhFingers;
		this.expirations = expirations;
		this.rhFingers = rhFingers;
	}

	// setters
	public void mapFinger (int fingerNum, int stringNum, int fretNum, Double expiration)
	{
		if (fingerNum > -1 && fingerNum < 4)
		{
			if (fretNum == 0)
			{
				lhFingers[fingerNum].setStringNum(-1);
				lhFingers[fingerNum].setFretNum(-1);

				// map first available finger on right hand
				for (int i = 0; i < rhFingers.length; i++)
				{
					if (rhFingers[i] == -1)
					{
						rhFingers[i] = stringNum; // assign right finger, leave left finger at -1
						i = rhFingers.length;
					}
				}

			}
			if (lhFingers[fingerNum].getStringNum() == -1 && lhFingers[fingerNum].getFretNum() == -1)
			{
				// expiration doesn't matter if the finger is unassigned, so we don't consider the expiration value
				lhFingers[fingerNum].setStringNum(stringNum);
				lhFingers[fingerNum].setFretNum(fretNum);

				// assign first available right hand finger
				for (int i = 0; i < rhFingers.length; i++)
				{
					if (rhFingers[i] == -1)
					{
						rhFingers[i] = stringNum;
						i = rhFingers.length;
					}
				}
			}
			else
			{
				// finger's already assigned
				if (expiration > expirations[fingerNum])
				{
					// finger's previous position expired, it can be reassigned
					lhFingers[fingerNum].setStringNum(stringNum);
					lhFingers[fingerNum].setFretNum(fretNum);

					// assign first available right hand finger
					for (int i = 0; i < rhFingers.length; i++)
					{
						if (rhFingers[i] == -1)
						{
							rhFingers[i] = stringNum;
							i = rhFingers.length;
						}
					}
				}
				else
				{
					// can't be reassigned yet, without value penalty
				}
			}
		}
		else
		{
			// finger outside range
		}
	}

	// takes in a set of Tuples representing left hand finger positions, determines if it's possible for the hand to make that shape
    public boolean chordTester()
    {

        // first round of checks
        for (int i = 0; i < lhFingers.length-1; i++)
        {
            int currentFret = lhFingers[i].getFretNum();
            int nextFret = lhFingers[i+1].getFretNum();

            // System.out.println(currentFret + " vs " + nextFret);

            if (currentFret > 0 && nextFret > 0) // watch for open strings (0) and unassigned fingers (-1)
            {
                // check that index is leftmost, and subsequent fingers are at or past their predecessor
                if (nextFret < currentFret)
                {
                    return false; 
                }

                // check that gaps between adjacent fingers are not too big
                if ((nextFret - currentFret) > 2) // if the guitarist's hand is huge, consider making this number bigger
                {
                    return false;
                }
            }
        }

        // check that each string gets only one note each
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



        return true;
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

    	return output;
    }

	// some sort of compare method
}

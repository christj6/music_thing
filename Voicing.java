
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

	public Voicing(Tuple[] lhFingers)
	{		
		this.lhFingers = lhFingers;
	}

	// constructor
	public Voicing(Tuple[] lhFingers, Double[] expirations, int[] rhFingers)
	{		
		this.lhFingers = lhFingers;
		// this.expirations = expirations;
		this.rhFingers = rhFingers;
	}

	// main constructor
	public Voicing(LinkedList<Note> chord)
	{
		this.chord = chord;

		int currentFinger = 0; // 0 refers to index finger
        int currentNote = 0; // 0 refers to bottom note of chord

		for (int i = 0; i < (highestPossibleNote - highEString); i++) // frets
        {
            for (int j = 0; j < 6; j++) // strings
            {
                Tuple currentPosition = new Tuple(j+1, i);

                if (currentFinger < 4 && currentNote < chord.size())
                {
                    if (currentPosition.getPitch(j+1, i) == chord.get(currentNote).getPitch())
                    {
                        this.assignLhFinger(currentFinger, j+1, i);

                        if (i != 0)
                        {
                            currentFinger++;
                        }

                        currentNote++;
                    }
                }
            }
        }

        this.assignRhFingers();
	}

	// setters
	public void assignLhFinger (int fingerNum, int stringNum, int fretNum)
	{
		if (fretboard[stringNum - 1].getStringNum() == -1)
		{
			fretboard[stringNum - 1] = new Tuple(stringNum, fretNum);
		}

		if (fingerNum > -1 && fingerNum < 4)
		{
			if (fretNum == 0 && stringsUsed[stringNum - 1] == 0)
			{
				stringsUsed[stringNum - 1] = 1;
			}
			else if (lhFingers[fingerNum].getStringNum() == -1 && lhFingers[fingerNum].getFretNum() == -1 && stringsUsed[stringNum - 1] == 0)
			{
				// expiration doesn't matter if the finger is unassigned, so we don't consider the expiration value
				lhFingers[fingerNum].setStringNum(stringNum);
				lhFingers[fingerNum].setFretNum(fretNum);

				stringsUsed[stringNum - 1] = 1;
			}
		}
		else
		{
			// finger outside range
		}
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

	public void shiftUp()
	{

		int maxMoveUp = 0;
		// int maxMoveDown = 0;

		boolean changeMax = false;

		for (int i = 0; i < stringsUsed.length; i++)
		{
			if (stringsUsed[i] == 0)
			{
				if (changeMax == true)
				{
					maxMoveUp++;
				}
				else if (changeMax == false)
				{
					// maxMoveDown++;
				}
			}
			else if (stringsUsed[i] == 1)
			{
				changeMax = true;
			}
		}

		if (maxMoveUp > 0)
		{
			// gather list of tuples -- both the lhFinger ones and the open string ones
			List<Tuple> tuples = new ArrayList<Tuple>();
			for (int i = 0; i < lhFingers.length; i++)
			{
				tuples.add(lhFingers[i]);
			}

			// find the open string notes. Currently they don't require a lh finger, but moving them up a string will change that.
			for (int i = 0; i < stringsUsed.length; i++)
			{
				if (stringsUsed[i] == 1)
				{
					// check if there is already a tuple in tuples on that string -- if not, add the open string to tuples
					boolean foundInPrevTuples = false;

					for (int j = 0; j < lhFingers.length; j++)
					{
						if (lhFingers[j].getStringNum() == i+1)
						{
							// do nothing
							j = lhFingers.length;
							foundInPrevTuples = true;
						}
					}

					if (foundInPrevTuples == false)
					{
						Tuple open = new Tuple(i+1, 0);
						tuples.add(open);
					}
				}
			}

			Collections.sort(tuples);
			for (int i = 0; i < tuples.size(); i++)
			{
				tuples.get(i).shift(-1);
			}

			this.resetLhFingers();
			this.resetRhFingers();

			int currentFinger = 0;

			for (int i = 0; i < 6; i++)
			{
				for (int j = 0; j < tuples.size(); j++)
				{
					Tuple current = tuples.get(j);

					if (current.getStringNum() == i+1)
					{
						if (current.getFretNum() == 0)
						{
							stringsUsed[i] = 1;
							tuples.remove(j);
							j = tuples.size();
						}
						else
						{
							this.assignLhFinger(currentFinger, current.getStringNum(), current.getFretNum());
							currentFinger++;
						}
					}
				}
			}

			this.assignRhFingers();
		}
		else
		{
			// either shift is 0 (do nothing) or shift is out of bounds (do nothing)
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

    // calc avg distance of fingers from tail of guitar
    public void avgDistance()
    {
    	Double sum = 0.0;
    	int stringsUsed = 0;

    	for (int i = 0; i < fretboard.length; i++)
    	{
    		if (fretboard[i].getFretNum() > -1)
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

    	// this.avgDistance();
    	// output += "avg distance of fingers from tail: " + this.avgDistance;

    	return output;
    }

	// some sort of compare method
}


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

	private LinkedList<Note> chord;

	// private boolean barre = false; // if index finger needs to hold multiple strings with the same fret #, it's probably a barre chord
	// private boolean strummed = false; // if more than 4 right-hand fingers are needed to play the chord, it's probably strummed

	private Double weight;
	private Voicing parent; // stores the parent of the voicing in a traversal
	private Double totalScore; // used for the dynamic programming aspect of the traversal -- http://www.seas.gwu.edu/~simhaweb/cs151/lectures/module12/align.html


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
	public Voicing(Tuple[] fretboard, Tuple[] lhFingers, int[] rhFingers, LinkedList<Note> chord)
	{
		this.chord = chord;
		this.fretboard = fretboard;
		this.lhFingers = lhFingers;	
		this.rhFingers = rhFingers;

		this.setWeight();
	}


    public void setParent(Voicing parent)
    {
    	this.parent = parent;
    }

    public Voicing getParent()
    {
    	return parent;
    }

    public void setWeight()
    {
    	// do something with weight
    	Double avgDistance = this.avgDistance();

        //-----------------------------------------------------------
    	Double stdDev = 0.0; // how far out the fingers are from the average location, fret wise
    	int fingers = 0;

    	for (int i = 0; i < lhFingers.length; i++)
    	{
    		Tuple current = lhFingers[i];

    		if (current.getFretNum() > 0)
    		{
    			Double value = current.getFretNum() - avgDistance;
    			stdDev += Math.pow(value, 2);
    			fingers++;
    		}
    	}

    	if (fingers > 0)
    	{
    		stdDev /= fingers;
    	}

        //-----------------------------------------------------------
        Double contourLength = 0.0; // sum of distance between each finger, including string and fret

        List<Integer> used = new ArrayList<Integer>();

        for (int i = 0; i < lhFingers.length; i++)
        {
            if (lhFingers[i].getFretNum() > 0)
            {
                used.add(i);
            }
        }

        for (int i = 0; i < used.size() - 1; i++)
        {
            int currentFret = lhFingers[i].getFretNum();
            int currentString = lhFingers[i].getStringNum();

            int nextFret = lhFingers[i+1].getFretNum();
            int nextString = lhFingers[i+1].getStringNum();

            Double fretDiff = Math.abs((double)nextFret - currentFret);
            Double stringDiff = Math.abs((double)nextString - currentString);

            Double temp = 0.0;
            temp += Math.pow(fretDiff, 2.0);
            temp += Math.pow(stringDiff, 2.0);

            contourLength += Math.pow(temp, 0.5);
        }

        //------------------------------------------------------------

    	this.weight = stdDev + contourLength + 1.0;
    }

    public Double getWeight()
    {
    	return weight;
    }

    // score as in value, not to be confused with JMusic's Score data structure for storing parts/phrases/notes
    public void setTotalScore(Double totalScore)
    {
    	this.totalScore = totalScore;
    }

    public Double getTotalScore()
    {
    	return totalScore;
    }

    // calc avg distance of fingers from tail of guitar
    public Double avgDistance()
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
    		return sum / stringsUsed;
    	}
    	else
    	{
    		return 999.0;
    	}
    }

    public Double distance(Voicing other)
    {
    	//
    	Double distance = Math.abs(this.avgDistance() - other.avgDistance());

    	return distance;
    }

	// takes in a set of Tuples representing left hand finger positions, determines if it's possible for the hand to make that shape
    public boolean chordTester()
    {
    	List<Integer> occupiedFrets = new ArrayList<Integer>();

        //------------------------------------------------------------------------------------------------------
        // check the left hand fingers:

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
        		return false; // make sure that LH index is leftmost (if not index, middle -- if not middle, ring)
        	}
        }

        //------------------------------------------------------------------------------------------------------
        // check the right hand fingers:

        // non -1 repeats?
        for (int i = 0; i < rhFingers.length; i++)
        {
            if (rhFingers[i] > 0)
            {
                for (int j = 0; j < rhFingers.length; j++)
                {
                    if (i != j)
                    {
                        if (rhFingers[j] == rhFingers[i])
                        {
                            return false;
                        }
                    }
                }
            }
        }

        // are the right hand fingers in order? thumb on bottom (near string 6), index above thumb, etc?
        List<Integer> rh = new ArrayList<Integer>();

        for (int i = 0; i < rhFingers.length; i++)
        {
            if (rhFingers[i] > 0)
            {
                rh.add(rhFingers[i]);
            }
        }

        for (int i = 0; i < rh.size() - 1; i++)
    	{
    		if (rh.get(i) < rh.get(i+1))
    		{
    			return false;
    		}
    	}

        int numberOfNotes = 0;
        for (int i = 0; i < fretboard.length; i++)
        {
            if (fretboard[i].getFretNum() != -1)
            {
                numberOfNotes++;
            }
        }

        if (numberOfNotes != rh.size())
        {
            return false; // discrepancy between # of notes the guitar needs plucked, and the number of right-hand fingers needed to be engaged
        }

        // are the right hand fingers plucking strings that correspond with left hand Tuples or required open notes?
        /*
        for (int i = 0; i < fretboard.length; i++)
        {
        	int currentString = fretboard[i].getStringNum();

        	if (currentString != -1) // find string that is actually used
        	{
        		boolean rhFound = false;

        		for (int j = 0; j < rhFingers.length; j++)
        		{
        			if (rhFingers[j] == currentString) // see if there is any rh finger that uses that string
        			{
        				rhFound = true;
        			}
        		}

        		if (!rhFound)
        		{
        			return false;
        		}
        	}
        }
        */
        
        for (int i = 0; i < rhFingers.length; i++)
        {
        	if (rhFingers[i] > 0)
            {
                int currentString = fretboard[rhFingers[i] - 1].getStringNum();

                if (rhFingers[i] != currentString)
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

    	// output += "avg distance of fingers from tail: " + this.avgDistance;

    	return output;
    }

	// some sort of compare method
}

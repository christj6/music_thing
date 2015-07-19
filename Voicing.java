
/*
    Voicing.java

    This class stores 1 way in which a guitarist might play a given chord.

    Comprised of a fretboard (represents what each string on the guitar would play if it were currently plucked),
    a left hand (represents where each left hand finger is on the fretboard), 
    and a right hand (represents which string each right hand finger plucks)

    Also features a weight, a pointer to a parent Voicing, and a totalScore.

    Has a method (chordTester) so a Voicing can test itself and see if it's valid.
*/

import jm.JMC;
import jm.music.data.*;
import jm.music.tools.*;
import jm.midi.*;
import jm.util.*;

import java.io.*;
import java.util.*;

public class Voicing
{
	// attributes
	private Tuple[] lhFingers = new Tuple[] {new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1)};
	private Tuple[] fretboard = new Tuple[] {new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1)};
	private int[] rhFingers = {-1, -1, -1, -1}; // p, i, m, a -- # refers to string plucked

	private LinkedList<Note> chord; // stored, in case we need access to the notes in the future.

	private Double weight = 0.0; // how difficult/undesirable is this particular voicing to play
	private Voicing parent; // stores the parent of the voicing in a traversal
    private Voicing child;
	private Double totalScore = 0.0; // used for the dynamic programming aspect of the traversal -- http://www.seas.gwu.edu/~simhaweb/cs151/lectures/module12/align.html

	// constants
    public static final int LOW_E_STRING = 40; // string #6, aka lowest possible note
    public static final int A_STRING = 45;
    public static final int D_STRING = 50;
    public static final int G_STRING = 55;
    public static final int B_STRING = 59;
    public static final int HIGH_E_STRING = 64; // pitch of string #1

    public static final int HIGHEST_POSSIBLE_NOTE = 84;

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

    // makes a Voicing point to its parent within Test.java's grid structure
    // This, in tandem with the totalScore attribute, allows the dynamic programming algorithm to occur
    public void setParent(Voicing parent)
    {
    	this.parent = parent;
    }

    // allows the backtracking of the optimal path to occur
    public Voicing getParent()
    {
    	return parent;
    }

    public void setChild(Voicing child)
    {
        this.child = child;
    }

    public Voicing getChild()
    {
        return child;
    }

    // establishes the weight (relative difficulty) of playing a Voicing in and of itself. Neighboring voicings are not involved yet.
    public void setWeight()
    {
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

    public Tuple[] getFretboard()
    {
        return fretboard;
    }

    // calculates the average distance of nonzero-fret holding fingers from tail of guitar
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
    		return Double.MAX_VALUE;
    	}
    }

    // calculates the distance between two Voicings according to how much the left hand would have to move
    public Double distance(Voicing other)
    {
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

        // non -1 repeats? There can be multiple -1s in a rhFinger array, since multiple fingers can be unassigned. But repeats of any other number implies that a single finger is plucking multiple strings simultaneously.
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

    // output object as string to command line
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
}

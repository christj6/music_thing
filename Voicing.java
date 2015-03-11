
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
	private LinkedList<Note> chord;
	private int numberOfNotes;
	private Tuple[] positions;


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

	// constructor
	public Voicing(LinkedList<Note> notes)
	{		
		chord = notes;
		numberOfNotes = chord.size();
		positions = new Tuple[numberOfNotes];

		for (int i = 0; i < positions.length; i++)
		{
			// do thing here
		}
	}

	// setters

	// getters

	// takes in pitch value & string, returns fret value
	public int getFret (int stringNum, int pitch)
	{
		switch(stringNum)
		{
			case 1:
				return pitch - highEString;
			case 2:
				return pitch - bString;
			case 3:
				return pitch - gString;
			case 4:
				return pitch - dString;
			case 5:
				return pitch - aString;
			case 6:
				return pitch - lowEString;
			default:
				return -1;
		}
		//return null;
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

	// some sort of compare method
}

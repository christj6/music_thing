
import jm.JMC;
import jm.music.data.*;
import jm.music.tools.*;
import jm.midi.*;
import jm.util.*;

// function will take LinkedList of Note objects as input, create a Voicing object if the chord is valid (can be played by a human)
public class Voicing
{
	// up to 4 notes, each associated with their own left (1 = index, 2 = middle, 3, 4) and right (p = thumb, i = index, m, a) hand fingers
	// also associated with a position on the fretboard (some notes can appear in more than one location on the fretboard)
	// if the note is open, no left-hand fingering is required.
	// Once a string is used for a note (fretted or open), no other note can use that string. If there exist no other strings for that note, the entire voicing is disqualified.
	// If we are estimating the distance between two positions (based on average of left hand finger positions?), don't include the 0 fret for open strings
	
	// attributes
	private Note[] chord;

	// constants
	public static final int maximumPolyphony = 6; // 6 strings, can be strummed simultaneously


	// constructor
	public Voicing()
	{
		// this.setAttribute(attr);
		for (int i = 0; i < maximumPolyphony; i++)
		{
			chord[i] = new Note();
		}
	}

	// setters

	// getters

	// some sort of compare method
}

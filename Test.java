

// Jack Christiansen

// using Windows cmd:
// compile with
// javac -classpath C:\Users\Jack\jmusic\jmusic.jar Test.java

// run with
// java -cp C:\Users\Jack\jmusic\jmusic.jar;. Test 

// to import file (this example called "file.mid"):
// java -cp C:\Users\Jack\jmusic\jmusic.jar;. Test file.mid

// to add to the git:
// delete the folder, git clone https://github.com/christj6/music_thing
// cd music_thing
// git add .
// git commit -m "some message"
// git push origin master

import jm.JMC;
import jm.music.data.*;
import jm.music.tools.*;
import jm.midi.*;
import jm.util.*;

import java.io.*;
import java.util.*;

 
public class Test implements JMC 
{

    // constants
    public static final int lowEString = 40;
    public static final int aString = 45;
    public static final int dString = 50;
    public static final int gString = 55;
    public static final int bString = 59;
    public static final int highEString = 64;

    public static final int highestPossibleNote = 84;

    public static void main(String[] args)
    {
        String midiFileName = args[0]; // might be changed later on to the name of a modified midi file
        System.out.println("Importing " + midiFileName);

        Score score = new Score();

        Read.midi(score, midiFileName); // check if args[0] is a valid midi file before you try reading it

        Mod.quantise(score, 0.25); // institutes barlines everywhere -- seems to work better than the Tools -> Quantize Timing
        // 0.25 corresponds with quarter notes

        //for each chord in the midi file, extract the chord:

        // for each note in the chord, map the note to a left-hand finger (1 = index, 2 = middle, 3, 4) 
        // and a right-hand finger (p = thumb, i = index, m, a)
        // and also a location on the fretboard (for example, a4 can be played on fret 0 of the a-string or fret 5 of the e-string)
        
        // for standard tuning guitar, the lowest pitch is 40 (e3), and the highest pitch is 84 (c7)
        // idea: transpose the song up/down in such a way to allow in the most notes with minimal truncation (might not work)
        if (score.getHighestPitch() > highestPossibleNote)
        {
            System.out.println("Alert: some note(s) too high for standard-tuned guitar.");
        }

        if (score.getLowestPitch() < lowEString)
        {
            System.out.println("Alert: some note(s) too low for standard-tuned guitar.");
        }

        // idea: maximize the occurrences of open strings -- transpose the piece up/down by x semitones until the max # of notes occur on E, A, D, G, b, e strings

        Double[] times = new Double[3]; // specific value doesn't matter, we reassign it anyway
        times = sortedUniqueStartTimes(score);

        /*
        for (int i = 0; i < times.length; i++)
        {
            System.out.println(Double.toString(times[i])); // testing to make sure the times are stored correctly
        }
        */

        // create array of linked lists of note objects
        //
        LinkedList<Note>[] chordSequence = new LinkedList[times.length]; // this uses unsafe/unchecked operations, apparently
        for (int i = 0; i < chordSequence.length; i++)
        {
            chordSequence[i] = new LinkedList<Note>();
        }

        // System.out.println(chordSequence.length);

        // 2nd time around
        Enumeration enum1 = score.getPartList().elements();
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

                    if (note.getPitch() != JMC.REST) 
                    {
                        // retrieve start time of note
                        // use it to perform binary search on start time array
                        // retrieve correct index
                        // add note to the linked list stored in the correct index
                        // something like: chordSequence[index].enqueue(note)

                        int index = Arrays.binarySearch(times, startTime);

                        System.out.println("index: " + index + ", pitch: " + note.getPitch());

                        chordSequence[index].add(note);

                    }

                    startTime += note.getDuration();
                }
            }
        }

        // do something to process the notes here

        // when we're done messing with the notes, we will probably iterate through all the notes in the chordSequence array,
        // and for each chord, we'll create a new Phrase with the right start time and add the notes to that phrase.
        // All the phrases will be added to a Part, which will then be added to a new score. Render a midi file from that.

        Score newArrangement = convertStructureToScore(chordSequence, times);

        
        // View.notate(newArrangement); // null pointer exception if the piece is too large

        // System.out.println(isTransitionReasonable(chordSequence[0], chordSequence[1]));

        //System.out.println("note array: " + score.getNoteArray());


        //View.notate(score); // make the sheet music appear on the screen in cpn
        // MuseScore's way better though, just import midi files with that


    }

    public static Double[] sortedUniqueStartTimes(Score score)
    {
        Set<Double> uniqueStartTimes = new HashSet<Double>();

        // this section adapted from http://explodingart.com/jmusic/applications/Midi2text.java
        Enumeration enum1 = score.getPartList().elements(); // changed "enum" to "enum1" to avoid the keyword error
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

                    if (note.getPitch() != JMC.REST) 
                    {
                        /*
                        // start time
                        System.out.println("Start time: " + Double.toString(startTime));
                        // pitch
                        System.out.println("Pitch: " + Integer.toString(note.getPitch()));
                        // duration
                        System.out.println("Duration: " + Double.toString(note.getDuration()));
                        // velocity
                        System.out.println("Velocity: " + Integer.toString(note.getDynamic()));
                        System.out.println("-------------------------------------------------");
                        */

                        // gather start times
                        uniqueStartTimes.add(startTime);
                    }

                    startTime += note.getDuration();
                }
            }
        }

        // filter out start time array so it just has unique entries
        // sort start time array
        Double[] times = uniqueStartTimes.toArray(new Double[uniqueStartTimes.size()]);
        Arrays.sort(times);

        return times;
    }

    // takes in an array of linked lists of Notes as an input, outputs an equivalent score
    public static Score convertStructureToScore(LinkedList<Note>[] structure, Double[] times)
    {
        int maxLinesNeeded = 6; // find length of longest linked list in structure, or just go with 6 because 6 strings

        Score newArrangement = new Score();
        


        for (int i = 0; i < maxLinesNeeded; i++)
        {
            Part part = new Part();

            for (int j = 0; j < structure.length; j++)
            {

                if (structure[j].peek() != null)
                {
                    Phrase phrase = new Phrase(times[j]);

                    // System.out.println("pitch: " + structure[j].peek().getPitch());

                    phrase.add(structure[j].remove()); // take one off the top of each linked list

                    part.add(phrase); 
                }


            }

            newArrangement.add(part);
        }

        return newArrangement;
    }

    // takes in a note/chord (1-n # of notes) as input, determines if it's playable or not according to the rules
    // If the chord is playable, the function will return a Voicing object. If not, it'll return null.
    public static boolean isChordPlayable(LinkedList<Note> notes) 
    {
        // for each note in the chord:
        // extract the note's pitch value
        // find pitch on fretboard
        // for example, pitch 50 (D4) yields a candidate array [-1, -1, -1, 0, 5, 10] --> [50 - 64, 50 - 59, 50 - 55, 50 - 50, 50 - 45, 50 - 40], disregard negative entries
        // since D4 can be played in 3 ways: play D-string open, play A-string 5th fret, or play E-string 10th fret.
        // D4 cannot be played on any other string, so those slots in the array have -1.

        


        // for a 3-note chord (for example, C4, E4, G4), the candidate arrays would look like:
        // G: [x, x, 0, 5, 10, 15]
        // E: [x, x, x, 2, 7,  12]
        // C: [x, x, x, x, 3,  8]
        // we can iterate through the top array until we find an entry that is not -1. If not, we go to next string. 
        // When we find a valid fret #, we can assign the first finger (index) to that fret/string. Unless it's 0 (open string).
        // Then we must go to next string to find next note (can't play 2 notes on the same string). 
        // When we find the next valid fret #, the # must be greater than or equal to the fret # that the index finger is assigned to.
        // We will assign the next finger (middle) to that and continue on.

        // how about we keep track of the smallest fret # candidate for each of the notes?
        // so, in that C major chord example, we iterate through the candidate arrays until G holds 0 on G-string, E holds 2 on D-string,
        // and C holds 3 on A-string. Each one grabs the first valid fret #. Whichever one is the smallest (that is not zero) gets the index finger.
        // This ensures that the index finger goes as far left on the fretboard as possible.



        return true; // still need to figure out how the Voicing object will be set up/structured.
    }

    // takes in a pitch value (for example, 40) as input, outputs a 6 integer array such as [-1, -1, -1, -1, -1, 0]
    public static int[] candidateArray (int pitch)
    {
        // int pitch = 40; // whatever the first pitch is

        int[] candidates = {highEString, bString, gString, dString, aString, lowEString}; // 6 strings in a guitar

        for (int i = 0; i < candidates.length; i++)
        {
            candidates[i] = pitch - candidates[i];

            if (candidates[i] < 0 || candidates[i] > (highestPossibleNote - highEString))
            {
                candidates[i] = -1; // set it to -1 if it's an invalid fret # (either less than 0, or greater than 20)
            }
        }

        return candidates;
    }

    // takes in two different chord voicings as input, determines if it's reasonable for the guitarist to move their hands from position A to position B
    // if yes, it returns true. If not, it returns false
    public static boolean isTransitionReasonable(LinkedList<Note> first, LinkedList<Note> second)
    {
        // put some more code here
        return true;
    }

    // given two pitch values (for example, 60 and 72), this function determines if the two pitches are the same note but some # of octaves apart.
    // I don't know where it will come in handy just yet.
    public static boolean octavesApart (int pitch1, int pitch2)
    {
        if ((Math.abs(pitch1 - pitch2) % 12) == 0) // not sure if I need absolute value here
        {
            return true;
        }
        else
        {
            return false;
        }

        // alternative approach: take in an array of pitch values as input, take the sum of them, see if the sum mod 12 is 0
    }
}

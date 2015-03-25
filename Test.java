

// Jack Christiansen

// using Windows cmd:
// compile with
// javac -cp C:\Users\Jack\jmusic\jmusic.jar *.java

// run with
// java -cp C:\Users\Jack\jmusic\jmusic.jar;. Test file.mid output.mid

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

    public static final int highestPossibleNote = 84; // assumes the guitar has 20 frets

    // fingers
    // left hand (fretboard)
    int leftHandPositions[] = {-1, -1, -1, -1}; // {index, middle, ring, pinkie} // identifies which fret each finger holds onto. -1 is unassigned.
    Double leftHandExpirations[] = {0.0, 0.0, 0.0, 0.0}; // time signifies when it's safe for the finger to be reassigned

    // right hand (strings)
    int rightHandPositions[] = {-1, -1, -1, -1}; // {p, i, m, a}

    public static void main(String[] args)
    {
        String inputFile = args[0];
        String outputFile = args[1];
        System.out.println("Importing " + inputFile);

        Score score = new Score();

        Read.midi(score, inputFile); // check if args[0] is a valid midi file before you try reading it

        // Mod.quantise(score, 4); // institutes barlines everywhere -- seems to work better than the Tools -> Quantize Timing
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

        int transposeValue = bestTransposition(score);

        System.out.println("transpose: " + transposeValue);





        // idea: maximize the occurrences of open strings -- transpose the piece up/down by x semitones until the max # of notes occur on E, A, D, G, b, e strings

        Double[] times = new Double[3]; // specific value doesn't matter, we reassign it anyway
        times = sortedUniqueStartTimes(score);

        LinkedList<Note>[] chordSequence = new LinkedList[times.length]; // this uses unsafe/unchecked operations, apparently
        chordSequence = chordSequenceArray(score, times);

        // do something to process the notes here (mess with chordSequence)
        LinkedList<Note>[] newSequence = new LinkedList[times.length];
        newSequence = processNotes(chordSequence, times);

        // when we're done messing with the notes, we will probably iterate through all the notes in the chordSequence array,
        // and for each chord, we'll create a new Phrase with the right start time and add the notes to that phrase.
        // All the phrases will be added to a Part, which will then be added to a new score. Render a midi file from that.

        Score newArrangement = convertStructureToScore(newSequence, times);

        Write.midi(newArrangement, outputFile);
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
                        // gather start times
                        uniqueStartTimes.add(startTime);
                    }
                    

                    // uniqueStartTimes.add(startTime); // includes rests

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

    // idea: go through each note and make sure the duration is equal to times[i+1] - times[i]
    public static LinkedList<Note>[] chordSequenceArray(Score score, Double[] times)
    {
        LinkedList<Note>[] chordSequence = new LinkedList[times.length]; // this uses unsafe/unchecked operations, apparently

        for (int i = 0; i < chordSequence.length; i++)
        {
            chordSequence[i] = new LinkedList<Note>();
        }

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

                        int index = Arrays.binarySearch(times, startTime);

                        chordSequence[index].add(note);
                    }

                    startTime += note.getDuration();
                }
            }
        }

        // Collections.sort(chordSequence, new MyComparator());

        return chordSequence;
    }

    // process each chord to see if a human can play it
    // if not, modify the chord in some way?
    // process transitions sequentially, see if a human can move their hand that quickly
    // if not, do something else?
    // return the modified structure
    public static LinkedList<Note>[] processNotes (LinkedList<Note>[] structure, Double[] times)
    {
        // do stuff here

        //System.out.println();

        assignFingers(structure[0], times[0]); // put a loop and and i here

        return structure;
    }

    // takes in a note/chord (1-n # of notes) as input, determines if it's playable or not according to the rules
    public static void assignFingers(LinkedList<Note> notes, Double start) 
    {
        // for each note in the chord:
        // extract the note's pitch value
        // find pitch on fretboard
        // for example, pitch 50 (D4) yields a candidate array [-1, -1, -1, 0, 5, 10] --> [50 - 64, 50 - 59, 50 - 55, 50 - 50, 50 - 45, 50 - 40], disregard negative entries
        // since D4 can be played in 3 ways: play D-string open, play A-string 5th fret, or play E-string 10th fret.
        // D4 cannot be played on any other string, so those slots in the array have -1.

        List<Note> list = new ArrayList<>();
        list.addAll(notes);

        Collections.sort(list, new MyComparator());

        for (int i = 0; i < list.size(); i++)
        {
            Note current = list.get(i);
            System.out.println("pitch: " + current.getPitch());
        }

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

    public static int bestTransposition (Score score)
    {
        if (score.getHighestPitch() > highestPossibleNote || score.getLowestPitch() < lowEString)
        {
            return 0; // don't bother transposing
        }

        int maxMoveUp = highestPossibleNote - score.getHighestPitch(); // the most # of semitones the piece can go up.
        int maxMoveDown = score.getLowestPitch() - lowEString; // the most # of semitones the piece can go down

        int openNotes = countOpenNotes(score); // call on original, unmodified score
        //System.out.println("i: " + 0 + " openNotes: " + openNotes);

        int maxOpenNotes = openNotes; // not compared to anything else yet, is current max
        int transposeValue = 0;

        for (int i = 1; i <= maxMoveUp; i++)
        {
            Score copy = score;
            Mod.transpose(copy, i);
            openNotes = countOpenNotes(copy);

            //System.out.println("i: " + i + " openNotes: " + openNotes);

            if (openNotes > maxOpenNotes)
            {
                maxOpenNotes = openNotes;
                transposeValue = i;
            }

        }

        for (int i = 1; i <= maxMoveDown; i++)
        {
            Score copy = score;
            Mod.transpose(copy, -i);
            openNotes = countOpenNotes(copy);

            //System.out.println("i: " + -i + " openNotes: " + openNotes);

            if (openNotes > maxOpenNotes)
            {
                maxOpenNotes = openNotes;
                transposeValue = -i;
            }
        }

        return transposeValue;
    }

    public static int countOpenNotes(Score score)
    {
        int openNotes = 0;

        Enumeration enum1 = score.getPartList().elements();

        while(enum1.hasMoreElements())
        {
            Part part = (Part) enum1.nextElement();
            Enumeration enum2 = part.getPhraseList().elements();

            while(enum2.hasMoreElements())
            {
                Phrase phrase = (Phrase) enum2.nextElement();
                Enumeration enum3 = phrase.getNoteList().elements();

                while(enum3.hasMoreElements())
                {
                    Note note = (Note) enum3.nextElement();

                    if (isOpenNote(note.getPitch()) == true) 
                    {
                        openNotes++;
                    }
                }
            }
        }

        return openNotes;
    }

    // given a pitch value, determines if it's one of the open notes on the guitar
    public static boolean isOpenNote(int pitchValue)
    {
        if (pitchValue == lowEString)
        {
            return true;
        }
        else if (pitchValue == aString)
        {
            return true;
        }
        else if (pitchValue == dString)
        {
            return true;
        }
        else if (pitchValue == gString)
        {
            return true;
        }
        else if (pitchValue == bString)
        {
            return true;
        }
        else if (pitchValue == highEString)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    // takes in an array of linked lists of Notes as an input, outputs an equivalent score
    public static Score convertStructureToScore(LinkedList<Note>[] structure, Double[] times)
    {
        int maxLinesNeeded = 6; // find length of longest linked list in structure, or just go with 6 because guitars usually have 6 strings

        Score newArrangement = new Score();

        for (int i = 0; i < maxLinesNeeded; i++)
        {
            Part part = new Part();

            for (int j = 0; j < structure.length; j++)
            {
                if (structure[j].peek() != null)
                {
                    Phrase phrase = new Phrase(times[j]);

                    Note note = structure[j].remove(); // take one off the top of each linked list

                    // should remove this if statement, move it to the processNotes function.
                    // it's not the responsibility of the convert function to determine which notes should or should not be played
                    if (note.getPitch() >= lowEString && note.getPitch() <= highestPossibleNote)
                    {
                        phrase.add(note); 
                        part.add(phrase); 
                    }
                    // else ignore the note, the guitar can't play it anyway
                }
            }

            newArrangement.add(part);
        }

        return newArrangement;
    }
}

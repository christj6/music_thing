

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

    // score
    private Score score = new Score();
    // private Set<Double> uniqueStartTimes = new HashSet<Double>();
    // private Double[] times;
    // private LinkedList<Note>[] chordSequence;
    private List<Double> times = new ArrayList<Double>();
    private List<LinkedList<Note>> chordSequence = new ArrayList<LinkedList<Note>>();

    // fingers
    // left hand (fretboard)
    // private int leftHandPositions[] = {-1, -1, -1, -1}; // {index, middle, ring, pinkie} // identifies which fret each finger holds onto. -1 is unassigned.
    // private Double leftHandExpirations[] = {0.0, 0.0, 0.0, 0.0}; // time signifies when it's safe for the finger to be reassigned

    // right hand (strings)
    private int rightHandPositions[] = {-1, -1, -1, -1}; // {p, i, m, a}

    // sets up everything 
    public Test(String inputFile, String outputFile)
    {
        // System.out.println("Importing " + inputFile);

        Read.midi(score, inputFile); // check if args[0] is a valid midi file before you try reading it

        // int transposeValue = bestTransposition(score);

        // System.out.println("transpose: " + transposeValue);
        // Mod.quantise(score, transposeValue);

        // Write.midi(score, outputFile);
        
        this.times = sortedUniqueStartTimes();

        this.chordSequence = chordSequenceArray();

        List<LinkedList<Note>> newSequence = processNotes(chordSequence, times);

        Write.midi(convertStructureToScore(newSequence, times), outputFile);

        // testing chord shape tester
        // Voicing cMajorChordOpen = new Tuple[] {new Tuple(2, 1), new Tuple(4, 2), new Tuple(5, 3), new Tuple(-1, -1)}; // index is on string 2, fret 1; middle on string 4, fret 2; ring on string 5, fret 3; pinkie unassigned
        Voicing cMajorChordOpen = new Voicing(new Tuple[] {new Tuple(2, 1), new Tuple(4, 2), new Tuple(5, 3), new Tuple(-1, -1)});
        System.out.println(cMajorChordOpen);
        System.out.println("testing... " + cMajorChordOpen.chordTester());
    }

    public List<Double> sortedUniqueStartTimes()
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

        List<Double> times = new ArrayList<>();
        times.addAll(uniqueStartTimes);
        Collections.sort(times);

        return times;
    }

    
    // idea: go through each note and make sure the duration is equal to times[i+1] - times[i]
    public List<LinkedList<Note>> chordSequenceArray()
    {

        List<LinkedList<Note>> chordSequence = new ArrayList<LinkedList<Note>>();

        for (int i = 0; i < times.size(); i++)
        {
            LinkedList<Note> chord = new LinkedList<Note>();
            chordSequence.add(chord);
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

                        int index = Collections.binarySearch(times, startTime);

                        chordSequence.get(index).add(note);
                    }

                    startTime += note.getDuration();
                }
            }
        }

        return chordSequence;
    }
    

    // process each chord to see if a human can play it
    // if not, modify the chord in some way?
    // process transitions sequentially, see if a human can move their hand that quickly
    // if not, do something else?
    // return the modified structure
    public List<LinkedList<Note>> processNotes (List<LinkedList<Note>> structure, List<Double> times)
    {
        /*
        Tuple[] emptyTuples = new Tuple[4];
        for (int i = 0; i < emptyTuples.length; i++)
        {
            emptyTuples[i] = new Tuple();
        }

        Double[] negativeEndTimes = new Double[4];
        for (int i = 0; i < negativeEndTimes.length; i++)
        {
            negativeEndTimes[i] = -1.0;
        }
        */

        Voicing voic = new Voicing();
        // System.out.println(voic);

        // System.out.println(assignFingers(emptyTuples, negativeEndTimes, 0));



        // based on the outcome of that function call, mess with structure??

        // create an array of lists of Voicing objects
        // for each voicing object, find the best-scoring next voicing to go to
        // do this for each spot in the array

        return structure;
    }


    public void assignFingers() 
    {
        /*
        List<Note> list = new ArrayList<>();
        List<Tuple>[] possiblePositions = new ArrayList[6]; // 6 long, for 6 strings

        list.addAll(chordSequence.get(currentIndex));

        Collections.sort(list, new MyComparator());

        // populate the 6 arraylists with positions based on which string they occupy
        for (int i = 0; i < list.size(); i++)
        {
            Note current = list.get(i);
            // System.out.println("pitch: " + current.getPitch());
            List<Tuple> positions = retrievePositionArray(current.getPitch());

            for (int j = 0; j < positions.size(); j++)
            {
                int stringNum = positions.get(j).getStringNum();

                if (stringNum > 0 && stringNum <= 6)
                {
                    possiblePositions[stringNum-1].add(positions.get(j));
                }
            }
        }
        */

    }

    

    // returns list of possible string/fret combos to play a given pitch
    public List<Tuple> retrievePositionArray (int pitch)
    {
        List<Tuple> positions = new ArrayList<Tuple>();

        for (int i = 1; i <= 6; i++)
        {
            for (int j = 0; j <= (highestPossibleNote - highEString); j++)
            {
                Tuple position = new Tuple(i, j);

                if (position.getPitch(i, j) == pitch)
                {
                    positions.add(position);
                }

            }
        }

        Collections.sort(positions);

        return positions;
    }

    public int bestTransposition (Score score)
    {
        if (score.getHighestPitch() > highestPossibleNote || score.getLowestPitch() < lowEString)
        {
            return 0; // don't bother transposing
        }

        int maxMoveUp = highestPossibleNote - score.getHighestPitch(); // the most # of semitones the piece can go up.
        int maxMoveDown = score.getLowestPitch() - lowEString; // the most # of semitones the piece can go down

        int openNotes = countOpenNotes(score); // call on original, unmodified score
        // System.out.println("i: " + 0 + " openNotes: " + openNotes);

        int maxOpenNotes = openNotes; // not compared to anything else yet, is current max
        int transposeValue = 0;

        for (int i = 1; i <= maxMoveUp; i++)
        {
            Score copy = score;
            Mod.transpose(copy, i);
            openNotes = countOpenNotes(copy);

            // System.out.println("i: " + i + " openNotes: " + openNotes);

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

            // System.out.println("i: " + -i + " openNotes: " + openNotes);

            if (openNotes > maxOpenNotes)
            {
                maxOpenNotes = openNotes;
                transposeValue = -i;
            }
        }

        return transposeValue;
    }

    // don't just check for any open notes, check for notes that are sustained for an extended period of time
    public int countOpenNotes(Score score)
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
                        // check the duration of the note, relative to the other notes (long sustaining bass note?)
                        openNotes++;
                    }
                }
            }
        }

        return openNotes;
    }

    // given a pitch value, determines if it's one of the open notes on the guitar
    public boolean isOpenNote(int pitchValue)
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
    public Score convertStructureToScore(List<LinkedList<Note>> structure, List<Double> times)
    {
        int maxLinesNeeded = 6; // find length of longest linked list in structure, or just go with 6 because guitars usually have 6 strings

        Score newArrangement = new Score();

        for (int i = 0; i < maxLinesNeeded; i++)
        {
            Part part = new Part();

            for (int j = 0; j < structure.size(); j++)
            {
                if (structure.get(j).peek() != null)
                {
                    Phrase phrase = new Phrase(times.get(j));

                    Note note = structure.get(j).remove(); // take one off the top of each linked list

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

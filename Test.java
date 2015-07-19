
/*
    Test.java
    
    This class takes in a MIDI file as an input, and determines the best way to play it on guitar, if it can be played. Otherwise it aborts.
*/

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

    // open string values
    public static final int LOW_E_STRING = 40;
    public static final int A_STRING = 45;
    public static final int D_STRING = 50;
    public static final int G_STRING = 55;
    public static final int B_STRING = 59;
    public static final int HIGH_E_STRING = 64;

    public static final int HIGHEST_POSSIBLE_NOTE = 84; // assumes the guitar has 20 frets

    public static final int MAX_FINGER_GAP = 2;

    // attributes
    private Score score = new Score(); // Before JMusic can do anything with a MIDI file, it first must convert it into a Score object

    private List<Double> times = new ArrayList<Double>(); // stores the unique note start times in a MIDI file
    private List<LinkedList<Note>> chordSequence = new ArrayList<LinkedList<Note>>(); // stores a chord for each time value

    // takes in 2 arguments. Can potentially output a midi file
    public Test(String inputFile, String outputFile)
    {
        System.out.println("Importing " + inputFile);
        Read.midi(score, inputFile);
        
        this.times = sortedUniqueStartTimes();
        this.chordSequence = chordSequenceArray();

        processNotes(chordSequence, times);

        /*
        List<LinkedList<Note>> newSequence = processNotes(chordSequence, times);

        if (newSequence != null)
        {
            Write.midi(convertStructureToScore(newSequence, times), outputFile); // an instance of how to call the convert method on a structure
        }
        */
    }

    // takes in only 1 argument -- filename. Use this for processing a file.
    public Test(String inputFile)
    {
        System.out.println("Importing " + inputFile);
        Read.midi(score, inputFile);
        
        this.times = sortedUniqueStartTimes();
        this.chordSequence = chordSequenceArray();

        processNotes(chordSequence, times);
    }

    // iterates through the midi file, returns an arrayList of unique start times for every note event in the midi file
    public List<Double> sortedUniqueStartTimes()
    {
        Set<Double> uniqueStartTimes = new HashSet<Double>();

        // this section adapted from http://explodingart.com/jmusic/applications/Midi2text.java
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
                        // gather start times
                        uniqueStartTimes.add(startTime);
                    }

                    startTime += note.getDuration();
                }
            }
        }

        List<Double> times = new ArrayList<>();
        times.addAll(uniqueStartTimes);
        Collections.sort(times); // allows us to perform binary searches on the times arraylist later

        return times;
    }

    // iterates through the midi file, storing each note according to its start time. Also returns an arrayList.
    // "Chords" (sets of notes played simultaneously) are stored within this arrayList as linked lists.
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

                        int index = Collections.binarySearch(times, startTime); // finds the index associated with the current start time

                        chordSequence.get(index).add(note);
                    }

                    startTime += note.getDuration();
                }
            }
        }

        return chordSequence;
    }
    
    // traverses a structure, determines if it can be played on guitar, outputs the optimal path to the command line
    public void processNotes (List<LinkedList<Note>> structure, List<Double> times)
    {
        List<List<Voicing>> grid = new ArrayList<List<Voicing>>(); // stores the voicing candidates for each time value. This is the structure that gets traversed at the end.

        for (int i = 0; i < structure.size(); i++)
        {
            List<Voicing> current = retrieveVoicingArray(structure.get(i));

            System.out.println(i + ": " + current.size());

            if (current.size() < 1)
            {
                return; // piece cannot be played. Continuing any further will cause the program to crash

                // perhaps it would be possible to modify structure.get(i) -- ie, the chord -- so that it can actually be played on guitar
            }
            else
            {
                grid.add(current);
            }
        }
        //---------------------------------------------------------

        for (int i = 0; i < grid.get(0).size(); i++)
        {
            // initialize the first set of voicings with total scores equal to weights
            grid.get(0).get(i).setTotalScore(grid.get(0).get(i).getWeight());
        }

        for (int i = 1; i < grid.size(); i++)
        {
            for (int j = 0; j < grid.get(i).size(); j++)
            {
                Double currentMin = Double.MAX_VALUE;
                int currentMinIndex = -1;

                for (int m = 0; m < grid.get(i-1).size(); m++)
                {
                    Double transition = grid.get(i-1).get(m).getTotalScore() + grid.get(i).get(j).distance(grid.get(i-1).get(m));

                    if (transition < currentMin)
                    {
                        currentMin = transition;
                        currentMinIndex = m;
                    }
                }

                if (currentMinIndex > -1)
                {
                    grid.get(i).get(j).setParent(grid.get(i-1).get(currentMinIndex));
                    grid.get(i).get(j).setTotalScore(currentMin + grid.get(i).get(j).getWeight());
                }
            }
        }

        Double bestScore = Double.MAX_VALUE; // initialize to maximum value
        int bestIndex = -1;

        // System.out.println("Scores");

        for (int i = 0; i < grid.get(grid.size() - 1).size(); i++)
        {
            double candidate = grid.get(grid.size() - 1).get(i).getTotalScore();

            // System.out.println((i + 1) + ": " + candidate); // outputs the scores of the n best paths

            if (candidate < bestScore)
            {
                bestScore = candidate;
                bestIndex = i;
            }
        }

        if (bestIndex > -1)
        {
            Voicing best = grid.get(grid.size() - 1).get(bestIndex);

            System.out.println(best);

            while (best.getParent() != null) // backtracks through the optimal path
            {
                System.out.println(best.getParent());
                best = best.getParent();
            }
        }

        // output a tab?
        for (int i = 0; i < 6; i++) // 6 strings
        {
            switch(i)
            {
                case 0:
                    System.out.print("e");
                    break;
                case 1:
                    System.out.print("b");
                    break;
                case 2:
                    System.out.print("G");
                    break;
                case 3:
                    System.out.print("D");
                    break;
                case 4:
                    System.out.print("A");
                    break;
                case 5:
                    System.out.print("E");
                    break;
            }

            Voicing best = grid.get(grid.size() - 1).get(bestIndex);

            Tuple[] fretboard = best.getFretboard();

            // System.out.println(best);

            System.out.print("\t");
            if (fretboard[i].getFretNum() > 9)
            {
                System.out.print(fretboard[i].getFretNum());
                System.out.print("\t");
            }
            else if (fretboard[i].getFretNum() < 10 && fretboard[i].getFretNum() > -1)
            {
                System.out.print(fretboard[i].getFretNum());
                System.out.print("\t");
            }
            else if (fretboard[i].getFretNum()==-1)
            {
                System.out.print("\t");
            }
            //System.out.print("-"); // extra dash for readability            

            while (best.getParent() != null) // backtracks through the optimal path
            {
                fretboard = best.getFretboard();

                // System.out.println(best.getParent());
                if (fretboard[i].getFretNum() > 9)
                {
                    System.out.print(fretboard[i].getFretNum());
                    System.out.print("\t");
                }
                else if (fretboard[i].getFretNum() < 10 && fretboard[i].getFretNum() > -1)
                {
                    System.out.print(fretboard[i].getFretNum());
                    System.out.print("\t");
                }
                else if (fretboard[i].getFretNum()==-1)
                {
                    System.out.print("\t");
                }
                //System.out.print("-"); // extra dash for readability

                best = best.getParent();
            }

            System.out.println(""); // newline
        }

        //-------------------------------------

        // suggestion for the future: instead of outputting the optimal path to the console, have the function return the optimal path as an arraylist of voicings
        // that might provide more versatility for interacting with the optimal path
    }

    // given a chord (linked list of Notes), returns all possible voicings of that chord in an arraylist
    public List<Voicing> retrieveVoicingArray(LinkedList<Note> chordInput)
    {
        // make sure pitch values in Note are sorted
        List<Note> sorted = new ArrayList<Note>();
        for (int i = 0; i < chordInput.size(); i++)
        {
            sorted.add(chordInput.get(i));
        }

        Collections.sort(sorted, new MyComparator());

        LinkedList<Note> chord = new LinkedList<Note>();

        for (int i = 0; i < sorted.size(); i++)
        {
            chord.add(sorted.get(i));
        }

        // begin collecting voicings
        List<Voicing> voicings = new ArrayList<Voicing>();

        List<Tuple> positions = new ArrayList<Tuple>();

        for (int i = 0; i < chord.size(); i++)
        {
            int currentPitch = chord.get(i).getPitch();

            List<Tuple> temp = retrievePositionArray(currentPitch);

            for (int j = 0; j < temp.size(); j++)
            {
                positions.add(temp.get(j));
            }
        }

        Tuple[] elements = new Tuple[positions.size()];
        elements = positions.toArray(elements);

        // this section traverses all possible combinations of the provided Tuples associated with a given chord
        int combination[] = new int[chord.size()];
        int r = 0;      
        int index = 0;
         
        while (r >= 0)
        {
            if (index <= elements.length + r - chord.size())
            {
                combination[r] = index;
                     
                if(r == chord.size() - 1)
                {
                    // this section filters the invalid voicings
                    //--------------------------------------------------------------------------
                    Tuple[] tempList = new Tuple[chord.size()];
                   
                    int tempIndex = 0;

                    for (int i = 0; i < combination.length; i++)
                    {
                        tempList[tempIndex] = elements[combination[i]];
                        tempIndex++;
                    }

                    //--------------------------------------------------------------------------
                    boolean noStringPlaysMultipleNotes = true; // innocent until proven guilty
                    int[] stringOccurrences = new int[] {0, 0, 0, 0, 0, 0};

                    for (int i = 0; i < tempList.length; i++)
                    {
                        if (tempList[i].getStringNum() > 0 && tempList[i].getStringNum() <= 6)
                        {
                            stringOccurrences[tempList[i].getStringNum() - 1]++;

                            if (stringOccurrences[tempList[i].getStringNum() - 1] > 1)
                            {
                                // disregard combination
                                noStringPlaysMultipleNotes = false;
                            }
                            // if at any point this value exceeds 1, delete the combination,
                            // because this combination attempts to play multiple notes on one string, which is not possible
                        }
                    }

                    //--------------------------------------------------------------------------
                    boolean rightPitchValues = true;

                    List<Integer> tempPitches = new ArrayList<Integer>();

                    for (int i = 0; i < tempList.length; i++)
                    {
                        int currentPitch = tempList[i].getPitch(tempList[i].getStringNum(), tempList[i].getFretNum());

                        if (currentPitch > -1)
                        {
                            tempPitches.add(currentPitch);
                        }
                    }

                    Collections.sort(tempPitches);

                    for (int i = 0; i < tempPitches.size(); i++)
                    {
                        if (tempPitches.get(i) != chord.get(i).getPitch())
                        {
                            rightPitchValues = false;
                        }
                    }

                    //--------------------------------------------------------------------------
                    boolean needsNoMoreThanFourFingers = true;
                    int fingersNeeded = 0;

                    for (int i = 0; i < tempList.length; i++)
                    {
                        int pitchValue = tempList[i].getPitch(tempList[i].getStringNum(), tempList[i].getFretNum());

                        if (tempList[i].getFretNum() > 0)
                        {
                            fingersNeeded++;
                        }
                    }

                    if (fingersNeeded > 4)
                    {
                        needsNoMoreThanFourFingers = false; // what about barre chords?
                    }

                    //--------------------------------------------------------------------------
                    boolean gapsDoNotExceedMax = true;

                    int[] fretNumbers = new int[tempList.length];

                    for (int i = 0; i < tempList.length; i++)
                    {
                        fretNumbers[i] = tempList[i].getFretNum();
                    }

                    Arrays.sort(fretNumbers);

                    for (int i = 0; i < fretNumbers.length - 1; i++)
                    {
                        if (fretNumbers[i] > 0 && fretNumbers[i+1] > 0 && (Math.abs(fretNumbers[i] - fretNumbers[i+1]) > MAX_FINGER_GAP))
                        {
                            gapsDoNotExceedMax = false;
                        }
                    }

                    //--------------------------------------------------------------------------
                    if (rightPitchValues == true && noStringPlaysMultipleNotes == true && needsNoMoreThanFourFingers == true && gapsDoNotExceedMax == true)
                    {
                        // now we are dealing with a Voicing that passed all the tests.
                        // We will set it to a fretboard array (6 tuples) and assign the fingers in every permutation possible
                        Tuple[] tempFretboard = new Tuple[] {new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1)};
                        for (int i = 0; i < tempFretboard.length; i++)
                        {
                            for (int j = 0; j < tempList.length; j++)
                            {
                                if (tempList[j].getStringNum() == i+1)
                                {
                                    tempFretboard[i] = tempList[j];
                                }
                            }
                        }

                        // tempFretboard is set, now determine combinations of finger placements
                        Tuple[] lhFingers = new Tuple[] {new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1), new Tuple(-1, -1)};
                        int currentFingerIndex = 0;

                        for (int i = 0; i < tempFretboard.length; i++)
                        {
                            int pitchValue = tempFretboard[i].getPitch(tempFretboard[i].getStringNum(), tempFretboard[i].getFretNum());

                            if (tempFretboard[i].getFretNum() > 0 && pitchValue > -1 && currentFingerIndex < 4)
                            {
                                lhFingers[currentFingerIndex] = tempFretboard[i];

                                currentFingerIndex++;
                            }
                        }

                        // lhFingers is populated
                        List<Tuple[]> lhFingerCombinations = lhFingerCombinations(lhFingers);

                        List<Integer[]> rhFingerCombinations = rhFingerCombinations();

                        for (int i = 0; i < lhFingerCombinations.size(); i++)
                        {
                            for (int j = 0; j < rhFingerCombinations.size(); j++)
                            {
                                // sorting the fingers with Arrays.sort(###) destroys the finger arrangement by moving fingers around

                                int[] rh = new int[4]; // convert the Int[] into a int[] array so it can be passed as a parameter

                                for (int m = 0; m < 4; m++)
                                {
                                    rh[m] = rhFingerCombinations.get(j)[m];
                                }

                                Voicing voic = new Voicing(tempFretboard, lhFingerCombinations.get(i), rh, chord);

                                if (voic.chordTester() == true)
                                {
                                    voicings.add(voic);
                                }
                            }
                        }
                    }

                    index++;                
                }
                else
                {
                    index = combination[r]+1;
                    r++;                                        
                }
            }
            else
            {
                r--;

                if(r > 0)
                {
                    index = combination[r]+1;
                }
                else
                {
                    index = combination[0]+1;
                }   
            }           
        }

        return voicings;
    }

    // returns an arraylist of all possible permutations of left hand fingers, given the set of fretboard positions (Tuples) to hold down
    public List<Tuple[]> lhFingerCombinations(Tuple[] elements)
    {
        List<Tuple[]> results = new ArrayList<Tuple[]>();

        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                for (int k = 0; k < 4; k++)
                {
                    for (int m = 0; m < 4; m++)
                    {
                        if (i != j && i != k && i != m && j != k && j != m && k != m)
                        {
                            Tuple[] tempCombo = new Tuple[4];

                            tempCombo[0] = elements[i]; // left index, 1
                            tempCombo[1] = elements[j]; // left middle, 2 
                            tempCombo[2] = elements[k]; // left ring, 3
                            tempCombo[3] = elements[m]; // left pinkie, 4

                            results.add(tempCombo);
                        }
                    }
                }
            }
        }

        return results;
    }

    // returns an arraylist of 4-int arrays (all possible right hand finger permutations, including those where fingers are unused. (-1)s are placeholders for unused slots.) 
    public List<Integer[]> rhFingerCombinations()
    {
        List<Integer[]> results = new ArrayList<Integer[]>();

        for (int i = -1; i <= 6; i++)
        {
            for (int j = -1; j <= 6; j++)
            {
                for (int k = -1; k <= 6; k++)
                {
                    for (int m = -1; m <= 6; m++)
                    {
                        if (i == 0 || j == 0 || k == 0 || m == 0)
                        {
                            // skip, there is no 0th string
                        }
                        else
                        {
                            Integer[] tempCombo = new Integer[4];

                            tempCombo[0] = i; // right thumb, p
                            tempCombo[1] = j; // right index, i
                            tempCombo[2] = k; // right middle, m
                            tempCombo[3] = m; // right ring, a

                            results.add(tempCombo);
                        }
                    }
                }
            }
        }

        return results;
    }

    // takes in a midi pitch value, returns list of possible string/fret combos to play a given pitch
    public List<Tuple> retrievePositionArray (int pitch)
    {
        List<Tuple> positions = new ArrayList<Tuple>();

        for (int i = 1; i <= 6; i++)
        {
            for (int j = 0; j <= (HIGHEST_POSSIBLE_NOTE - HIGH_E_STRING); j++)
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

    // takes in an array of linked lists of Notes as an input, outputs an equivalent score
    // does not work well on unquantized midi files
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

                    phrase.add(note); 
                    part.add(phrase);
                }
            }

            newArrangement.add(part);
        }

        return newArrangement;
    }
}

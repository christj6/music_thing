

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
    public static void main(String[] args)
    {
        String midiFileName = args[0]; // might be changed later on to the name of a modified midi file
        System.out.println("Importing " + midiFileName);

        Score score = new Score();

        Read.midi(score, midiFileName); // check if args[0] is a valid midi file before you try reading it

        Mod.quantise(score, 0.25); // institutes barlines everywhere -- seems to work better than the Tools -> Quantize Timing
        // 0.25 corresponds with quarter notes

        
        //runJFugueFile(midiFileName); // 

        try // 
        {
            String directory = "C:\\Users\\Jack\\jmusic\\jmusic.jar";

            Process pro = Runtime.getRuntime().exec("javac -classpath " + directory + " Midi2Text.java"); // compiles file
            //System.out.println(pro.getInputStream());
            //System.out.println(pro.getErrorStream());
            pro.waitFor();
            pro = Runtime.getRuntime().exec("java -cp " + directory + ";. Midi2Text " + midiFileName); // runs file
            //System.out.println(pro.getInputStream());
            //System.out.println(pro.getErrorStream());
            pro.waitFor();

        } catch (Exception e) {
          e.printStackTrace();
        }



        //for each chord in the midi file, extract the chord:

        // for each note in the chord, map the note to a left-hand finger (1 = index, 2 = middle, 3, 4) 
        // and a right-hand finger (p = thumb, i = index, m, a)
        // and also a location on the fretboard (for example, a4 can be played on fret 0 of the a-string or fret 5 of the e-string)
        
        //System.out.println("highest pitch: " + score.getHighestPitch() + ", lowest pitch: " + score.getLowestPitch());
        // for standard tuning guitar, the lowest pitch is 40 (e3), and the highest pitch is 84 (c7)
        // idea: transpose the song up/down in such a way to allow in the most notes with minimal truncation (might not work)
        if (score.getHighestPitch() > 84)
        {
            System.out.println("Alert: some note(s) too high for standard-tuned guitar.");
        }
        if (score.getLowestPitch() < 40)
        {
            System.out.println("Alert: some note(s) too low for standard-tuned guitar.");
        }

        // idea: maximize the occurrences of open strings -- transpose the piece up/down by x semitones until the max # of notes occur on E, A, D, G, b, e strings

        
        System.out.println("pitch array: ");
        Part part = score.getPart(0);
        Phrase phrase = part.getPhrase(0);
        int i, j, k = 0;

        int notesInCurrentChord = 0;


        for (k = 0; k < 40; k++)
        {
            notesInCurrentChord = 0;
            //System.out.println("Chord #" + (k+1));
            //int[] whichNotes = new int[4];

            for (i = 0; i < score.getSize(); i++)
            {
                //System.out.println("i: " + i);

                part = score.getPart(i);

                for (j = 0; j < part.getSize(); j++)
                {
                    //System.out.println("j: " + j);

                    phrase = part.getPhrase(j);

                    int[] pitches = phrase.getPitchArray();

                    /*
                    for (int k = 0; k < phrase.getSize(); k++)
                    {
                        System.out.println("k: " + k);

                        System.out.println("pitch: " + pitches[k]);
                    }
                    */
                    if (k < pitches.length && notesInCurrentChord < 4)
                    {
                        // System.out.println("pitch: " + pitches[k]); // outputs pitches for each chord, kinda inefficient

                        notesInCurrentChord++; // takes another left-hand finger, UNLESS it can be played with an open string
                        // takes another right-hand finger to pluck the chord, UNLESS the chord is being strummed instead of plucked
                    }
                }
            }

            if (notesInCurrentChord > 0)
            {
                System.out.println("Chord #" + (k+1));

                for (int z = 0; z < notesInCurrentChord; z++)
                {
                    // print each note in chord?
                }
            }
        }

        

        //System.out.println("note array: " + score.getNoteArray());


        //View.notate(score); // make the sheet music appear on the screen in cpn
        // MuseScore's way better though, just import midi files with that


    }

    public static void runJFugueFile(String filename)
    {
        // alternative idea: use a java process to compile/run a JFugue program, take in the midi file. export it into a txt file storing the music string with numbers
        // javac -classpath C:\Users\Jack\Documents\spring_2015\jfugue-4.0.3.jar MusicString.java
        // java -cp C:\Users\Jack\Documents\spring_2015\jfugue-4.0.3.jar;. MusicString 

        // on second thought -- musicString format is pretty gross (more information than we need, weird formatting, hard to extract information)

        // if we can extract a .jfugue file, that might be more useful -- Nevermind, same thing happens
        try // 
        {
            String directory = "C:\\Users\\Jack\\Documents\\spring_2015\\jfugue-4.0.3.jar";

            Process pro = Runtime.getRuntime().exec("javac -classpath " + directory + " MusicString.java"); // compiles file
            //System.out.println(pro.getInputStream());
            //System.out.println(pro.getErrorStream());
            pro.waitFor();
            pro = Runtime.getRuntime().exec("java -cp " + directory + ";. MusicString " + filename); // runs file
            //System.out.println(pro.getInputStream());
            //System.out.println(pro.getErrorStream());
            pro.waitFor();

        } catch (Exception e) {
          e.printStackTrace();
        }
    }
}

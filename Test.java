

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

        
        //System.out.println("pitch array: ");
        //Part part = score.getPart(0);
        //Phrase phrase = part.getPhrase(0);
        
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
                        // start time
                        System.out.println("Start time: " + Double.toString(startTime));
                        // pitch
                        System.out.println("Pitch: " + Integer.toString(note.getPitch()));
                        // duration
                        System.out.println("Duration: " + Double.toString(note.getDuration()));
                        // velocity
                        System.out.println("Velocity: " + Integer.toString(note.getDynamic()));
                        System.out.println("-------------------------------------------------");

                        // add the note to the bucket thing you were talking about
                    }

                    startTime += note.getDuration();
                }
            }
        }

        

        //System.out.println("note array: " + score.getNoteArray());


        //View.notate(score); // make the sheet music appear on the screen in cpn
        // MuseScore's way better though, just import midi files with that


    }

    public static boolean isChordPlayable(Note[] notes)
    {
        // put some more code here
        return true;
    }

    public static boolean isTransitionReasonable(Note[] first, Note[] second)
    {
        // put some more code here
        return true;
    }
}

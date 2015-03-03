

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

import java.util.Arrays;

 
public class Test implements JMC 
{
    public static void main(String[] args)
    {
        //System.out.println("Java works.");
        //Score s = new Score(new Part(new Phrase(new Note(C4, MINIM))));
        //Write.midi(s, "Test.mid");

        System.out.println("Importing " + args[0]);

        Score score = new Score();

        Read.midi(score, args[0]);

        Mod.quantise(score, 0.25); // institutes barlines everywhere -- seems to work better than the Tools -> Quantize Timing
        // 0.25 corresponds with quarter notes



        //for each chord in the midi file, extract the chord:

        // for each note in the chord, map the note to a left-hand finger (1 = index, 2 = middle, 3, 4) 
        // and a right-hand finger (p = thumb, i = index, m, a)
        // and also a location on the fretboard (for example, a4 can be played on fret 0 of the a-string or fret 5 of the e-string)
        
        System.out.println("highest pitch: " + score.getHighestPitch() + ", lowest pitch: " + score.getLowestPitch());
        // for standard tuning guitar, the lowest pitch is 40 (e3), and the highest pitch is 84 (c7)
        // idea: transpose the song up/down in such a way to allow in the most notes with minimal truncation (might not work)

        // idea: maximize the occurrences of open strings -- transpose the piece up/down by x semitones until the max # of notes occur on E, A, D, G, b, e strings

        
        System.out.println("pitch array: ");
        Part part;
        Phrase phrase;
        int i, j, k = 0;

        for (i = 0; i < score.getSize(); i++)
        {
            System.out.println("i: " + i);

            part = score.getPart(i);

            for (j = 0; j < part.getSize(); j++)
            {
                System.out.println("j: " + j);

                phrase = part.getPhrase(j);

                int[] pitches = phrase.getPitchArray();

                /*
                for (int k = 0; k < phrase.getSize(); k++)
                {
                    System.out.println("k: " + k);

                    System.out.println("pitch: " + pitches[k]);
                }
                */
                System.out.println("pitch: " + pitches[k]); // right now, this successfully outputs the notes in the first chord
                // determine how to move k along so we can grab each chord in the piece
            }
        }

        //System.out.println("note array: " + score.getNoteArray());


        //View.notate(score); // make the sheet music appear on the screen in cpn
        // MuseScore's way better though, just import midi files with that


    }

    public static void func()
    {
        // do something
    }
}

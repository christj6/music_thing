

// Jack Christiansen

// using Windows cmd:
// compile with
// javac -classpath C:\Users\Jack\jmusic\jmusic.jar Test.java

// run with
// java -cp C:\Users\Jack\jmusic\jmusic.jar;. Test 

// to import file (this example called "file.mid"):
// java -cp C:\Users\Jack\jmusic\jmusic.jar;. Test file.mid

// I always forget this part, so I put it here
// git add .
// git commit -m "some message"
// git push origin master

import jm.JMC;
import jm.music.data.*;
import jm.music.tools.*;
import jm.midi.*;
import jm.util.*;
 
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

        //Part part = score.getPart(0);

        //Phrase phrase = part.getPhrase(0);

        Mod.quantise(score, 0.25); // institutes barlines everywhere -- seems to work better than the Tools -> Quantize Timing
        // 0.25 corresponds with quarter notes

        Mod.transpose(score, 4); // moves notes up by 4 semitones -- http://explodingart.com/jmusic/jmtutorial/Methods.html

        // somewhere here, mess around with file?
        
        View.notate(score); // make the sheet music appear on the screen in cpn
        // MuseScore's way better though, just import midi files with that

        // attempted importing a midi file with wildly varing dynamics (file_w_dynamics.mid), to see if cpn or MuseScore would pick up on it, but it did not
                
        //Play.midi(score, false);
    }
}
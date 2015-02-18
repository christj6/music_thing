

// Jack Christiansen

// using Windows cmd:
// compile with
// javac -classpath C:\Users\Jack\jmusic\jmusic.jar Test.java

// run with
// java -cp C:\Users\Jack\jmusic\jmusic.jar;. Test 

// to import file (this example called "file.mid"):
// java -cp C:\Users\Jack\jmusic\jmusic.jar;. Test file.mid

import jm.JMC;
import jm.music.data.*;
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

        Part part = score.getPart(0);

        Phrase phrase = part.getPhrase(0);
        // after using Tools -> Quantize Timing, the file.mid worked perfectly for the first 12 measures, but then it 'ran out of' barlines. However, it also corrected the tempo, and midi playback was unaffected by the missing barlines.

        // somewhere here, mess around with file?
        
        View.notate(score); // make the sheet music appear on the screen in cpn
                
        //Play.midi(score, false);
    }
}

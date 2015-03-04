

// Jack Christiansen

// using Windows cmd:
// compile with
// javac -classpath \jfugue-4.0.3.jar Test.java

// run with
// java -cp \jfugue-4.0.3.jar;. Test 

import java.io.*;
import java.util.*;

import org.jfugue.*;

public class MusicString {

   public static void main(String args[]) throws IOException
   {
      // args[0] is the filename -- simple.mid or range.mid, something like that

   	/*
      String notes = "[48]+[52]+[55] [50]+[53]+[57] [48]+[52]+[55]";
      Player player = new Player();
      Pattern pattern = new Pattern(notes);
      player.play(pattern);
      */

      String output = "";

    try
    {
    	Player player = new Player();
		Pattern pattern = player.loadMidi(new File(args[0]));

		output = pattern.getMusicString();

		

		//player.play(pattern);
    } catch (Exception e) {
      e.printStackTrace();
    }

    //System.out.println(output);

    writeFile("musicString.txt", output);



      //System.exit(0); // If using Java 1.4 or lower
   }

   public static void writeFile(String filename, String data) throws IOException
	{
		FileWriter fileWriter = new FileWriter(filename);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(data);
        bufferedWriter.close();
	}
 }

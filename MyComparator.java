
import jm.JMC;
import jm.music.data.*;
import jm.music.tools.*;
import jm.midi.*;
import jm.util.*;

import java.io.*;
import java.util.*;

public class MyComparator implements Comparator<Note>
    {
        // http://stackoverflow.com/questions/18985209/sorting-arraylist-using-mycomparator-class
        @Override
        public int compare(Note first, Note second) 
        {
            if (first.getPitch() > second.getPitch()) 
            {
                return -1;
            } 
            else if (first.getPitch() < second.getPitch()) 
            {
                return 1;
            }
            return 0;
        }
    }

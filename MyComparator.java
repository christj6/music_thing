
/*
    MyComparator.java
    
    Allows Note objects to be sorted by their pitch value.
*/

import jm.JMC;
import jm.music.data.*;
import jm.music.tools.*;
import jm.midi.*;
import jm.util.*;

import java.io.*;
import java.util.*;

// allows Note objects to be sorted by pitch value
public class MyComparator implements Comparator<Note>
{
    @Override
    public int compare(Note first, Note second) 
    {
        if (first.getPitch() > second.getPitch()) 
        {
            return 1;
        } 
        else if (first.getPitch() < second.getPitch()) 
        {
            return -1;
        }
        return 0;
    }
}

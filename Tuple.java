
// Tuple class for left hand finger position on guitar

public class Tuple implements Comparable<Tuple>
{
	private int stringNum;
	private int fretNum;

	// constants
	public static final int maximumPolyphony = 6; // 6 strings, can be strummed simultaneously

    public static final int lowEString = 40; // string #6, aka lowest possible note
    public static final int aString = 45;
    public static final int dString = 50;
    public static final int gString = 55;
    public static final int bString = 59;
    public static final int highEString = 64; // pitch of string #1

    public static final int highestPossibleNote = 84;

    public Tuple()
    {
    	// empty constructor
    }

	public Tuple(int stringNum, int fretNum) 
	{
		this.stringNum = stringNum;
		this.fretNum = fretNum;
	}

	public int getStringNum()
	{
		return stringNum;
	}

	public int getFretNum()
	{
		return fretNum;
	}

	public void getStringNum(int stringNum)
	{
		this.stringNum = stringNum;
	}

	public void getFretNum(int fretNum)
	{
		this.fretNum = fretNum;
	}

	// takes in string and fret number, returns pitch value
	public int getPitch (int stringNum, int fretNum)
	{
		switch(stringNum)
		{
			case 1:
				return highEString + fretNum;
			case 2:
				return bString + fretNum;
			case 3:
				return gString + fretNum;
			case 4:
				return dString + fretNum;
			case 5:
				return aString + fretNum;
			case 6:
				return lowEString + fretNum;
			default:
				return -1;
		}
	}

	public int compareTo(Tuple other) 
    {
        if (this.getFretNum() > other.getFretNum()) 
        {
            return 1;
        } 
        else if (this.getFretNum() < other.getFretNum()) 
        {
            return -1;
        }
        return 0;
    }
}


// Tuple class for left hand finger position on guitar
public class Tuple 
{
	private int stringNum;
	private int fretNum;

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
}

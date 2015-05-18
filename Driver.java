
/*
    Driver.java
    
    Allows the user to interact with the program by inputting a midi file of their choice.
*/

public class Driver
{
	public static void main(String[] args)
	{
		if (args.length == 1)
		{
			Test mus = new Test(args[0]);
		}
		else if (args.length == 2)
		{
			Test mus = new Test(args[0], args[1]);
		}
	}
}

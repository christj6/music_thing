
// Jack Christiansen

// using Windows cmd:
// compile with
// javac -cp C:\Users\Jack\jmusic\jmusic.jar *.java

// run with
// java -cp C:\Users\Jack\jmusic\jmusic.jar;. Driver file.mid output.mid
// whatever the 2nd file is will be overwritten

// to add to the git:
// delete the folder from local
// git clone https://github.com/christj6/music_thing
// cd music_thing
// git add .
// git commit -m "some message"
// git push origin master

public class Driver
{
	public static void main(String[] args)
	{
		Test mus = new Test(args[0], args[1]);
	}
}

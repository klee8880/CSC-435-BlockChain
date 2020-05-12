import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BlockChain {
	
	private static String hashType = "SHA-256";
	
	static MessageDigest encoder;

	public static void main (String [] args) {

		//Start up encoder
		try {
			encoder = MessageDigest.getInstance(hashType);
		} 
		//If encoding variable fails immediately close program after printing error code.
		catch (NoSuchAlgorithmException exp) {
			exp.printStackTrace();
			return;
		}
		
		//Run the process
		new BlockChain().run(args);

	}
	
	void run(String [] args) {
		
		//TODO: Instantiate variables
		int port = 0;
		int unverifiedPort = 4710;
		int blockChainPort = 4810;
		Boolean solved = false;
		
		//Parse Arguments
		if (args.length < 1) port = 0; //Leave port as 0 for default
	    else {
	    	try {
	    		port = Integer.parseInt(args[0]);
	    	}
	    	//If integer cannot be parsed terminate process.
	    	catch (NumberFormatException ex) {
	    		System.out.println("Passed arguments is not a number value... Process terminated.");
	    		return;
	    	}
	    }
		
		//Mutate ports
		unverifiedPort += port;
		blockChainPort += port;
		
		//TODO: Read the data block
		
		//TODO: Verify digital signature
		
		//TODO: Notify other processes to start trying to resolve hash
		
		//TODO: Start randomizing blocks till winning hash is found by this process or others.
		//Check if already solved
		//Mark solved if solution is found
		
		//TODO: If process 0, write results to disk.
	}
}


class DataBlock {
	//Properties
	String startHash;
	String date;
	String movie;
	String genre;
	String screeningDate;
	int rating; //Rating out of 100
	String randomSeed = "";
	String endHash = "";
	
	//Getters and Setters
	public String getStartHash() {return startHash;}
	public void setStartHash(String startHash) {this.startHash = startHash;}
	
	public String getDate() {return date;}
	public void setDate(String date) {this.date = date;}
	
	public String getMovie() {return movie;}
	public void setMovie(String movie) {this.movie = movie;}
	
	public String getGenre() {return genre;}
	public void setGenre(String genre) {this.genre = genre;}
	
	public String getScreeningDate() {return screeningDate;}
	public void setScreeningDate(String screeningDate) {this.screeningDate = screeningDate;}
	
	public int getRating() {return rating;}
	public void setRating(int rating) {this.rating = rating;}
	
	public String getRandomSeed() {return randomSeed;}
	public void setRandomSeed(String randomSeed) {this.randomSeed = randomSeed;}
	
	public String getEndHash() {return endHash;}
	public void setEndHash(String endHash) {this.endHash = endHash;}

	/* (non-Javadoc)
	 * Concatenate the string of values into a single string
	 */
	@Override
	public String toString() {
		return new StringBuilder()
				.append(startHash)
				.append(date)
				.append(movie)
				.append(genre)
				.append(screeningDate)
				.append(rating)
				.append(randomSeed)
				.append(endHash)
				.toString();
	}
}

//Listens for solutions from secondary processes.
class SolutionListener {
	//Properties
	private int port;

	//Getters and Setters
	public int getPort() {return port;}
	public void setPort(int port) {this.port = port;}
	
}
























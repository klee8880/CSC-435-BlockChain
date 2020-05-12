import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class DataBlock {
	String startHash;
	String date;
	String movie;
	String genre;
	String screeningDate;
	int rating; //Rating out of 100
	String randomSeed;
	String endHash;
	
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

public class BlockChain {
	
	static MessageDigest encoder;

	private static void main (String [] args) {

		//TODO: Instantiate variables
		try {
			encoder = MessageDigest.getInstance("SHA-256");
		} 
		//If encoding variable immediately close program after printing error code.
		catch (NoSuchAlgorithmException exp) {
			exp.printStackTrace();
			return;
		}
		
		//Run the process
		new BlockChain().run(args);

	}
	
	void run(String [] args) {
		
		//TODO: Parse Arguments
		
		//TODO: Read the data block
		
		//TODO: Notify other processes to start trying to resolve hash
		
		//TODO: Start randomizing blocks till winning hash is found by this process or others.
	}
}























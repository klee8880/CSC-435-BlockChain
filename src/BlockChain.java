/*--------------------------------------------------------

1. Name / Date: Kevin Lee 5/12/2020

2. Java version used, if not the official version for the class:

build 1.8.0_161

3. Precise command-line compilation examples / instructions:

> javac BlockChain.java

4. Precise examples / instructions to run this program:

In separate shell windows or computers:

> java BlockChain #

All acceptable commands are displayed on the various consoles.

5. List of files needed for running the program.

 a. 

5. Notes:

----------------------------------------------------------*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.*;

/**
 * @author klee8
 * Data block to be solved
 */
class DataBlock {
	//Properties
	private String startHash;
	private String data;
	private String randomSeed = "";
	private String endHash = "";
	
	//Constructors
	public DataBlock(String startHash, String data) {
		super();
		this.startHash = startHash;
		this.data = data;
	}
	
	//Getters and Setters
	public String getStartHash() {return startHash;}
	public void setStartHash(String startHash) {this.startHash = startHash;}
	
	public String getData() {return data;}
	public void setData(String data) {this.data = data;}
	
	public String getRandomSeed() {return randomSeed;}
	public void setRandomSeed(String randomSeed) {this.randomSeed = randomSeed;}
	
	public String getEndHash() {return endHash;}
	public void setEndHash(String endHash) {this.endHash = endHash;}
	
	//Methods
	
	/* (non-Javadoc)
	 * Concatenate the string of values into a single descriptive string.
	 * Description string can also be used in hashing methods
	 */
	@Override
	public String toString() {
		return new StringBuilder()
				.append(startHash).append('\n')
				.append(data).append('\n')
				.append(randomSeed).append('\n')
				.append(endHash)
				.toString();
	}
}

/**
 * @author klee8
 *
 */
public class BlockChain {
	
	//Statics
	private static String hashType = "SHA-256";

	//Properties
	Boolean solved = false;
	
	public static void main (String [] args) {
		
		//Run the process
		new BlockChain().run(args);

	}
	
	void run(String [] args) {
		
		//TODO: Instantiate variables
		int port = 0;
		int unverifiedPort = 4710;
		int blockChainPort = 4930;
		int keyPort = 4710;
		String input = "";
		
		BufferedReader inStream =  new BufferedReader(new InputStreamReader(System.in));
		
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
		keyPort += port;
		
		//Splash Screen
		System.out.println("Now running Kevin Lee's Block Chain Application v.01");
		System.out.println("Running as process #" + port );
		
		//TODO: Spawn secondary processes Threads.
		
		
		do {
		//Prompt user for data.
		System.out.println("Please input a new phrase to commit to the block chain or QUIT to end the program.");
		
		//Read in data to be encoded.
		try {
			input = inStream.readLine();
		} catch (IOException e) {
			System.out.println("Error occured reading input See below:");
			e.printStackTrace();
		}
		
		//Break loop on quit command.
		if (input.toUpperCase().equals("QUIT")) {
			break;
		}
		
		//TODO: Verify digital signature.
		
		//TODO: Notify other processes to start trying to resolve hash.
		
		//TODO: Attempt to resolve the hash locally.
		
		}while (true);
		
		//TODO: inform other processes of signoff.
		
		//End of process
		System.out.println("Block Chain process ended.");
		
	}
}


//------SOLUTION RECEIVER STRUCTURE------
/**SolutionListener
 * @author klee8
 *
 */
class SolutionListener extends Thread{
	//Properties
	private int port;
	
	public SolutionListener(int port) {
		super();
		this.port = port;
	}
	
	@Override
	public void run() {
		System.out.println("Solution Listener Thread Spawned...");
		
		//Open Port
		
		//Listen for connection.
		
		//If solution is found inform the parent thread to stop searching for a solution and accept yours.
	}
	
}


//------SOLUTION REQUEST STRUCTURE------
/**RequestListener
 * @author klee8
 *
 */
class RequestListener extends Thread{
	//Properties
	private int port;
	
	public RequestListener(int port) {
		super();
		this.port = port;
	}
	
	@Override
	public void run() {
		System.out.println("Request Listener Thread Spawned...");
		
		//Open Port
		
		//Listen for connection.
		
		//Requests made to this spawn Solver threads that attempt to resolve the block
	}
	
}


/**Solver
 * @author klee8
 * Takes input problem and solves for the given hash problem.
 * Repeatedly randomizes the given data block until the concatenated block's hash code meets the given criteria.
 */
class Solver extends Thread{
	//Properties
	private int port;
	private MessageDigest encoder;
	private String hashType;
	
	//Constructor
	public Solver(int port, String hashType) {
		super();
		this.port = port;
		this.hashType = hashType;
	}

	//Methods
	@Override
	public void run() {
		
		//Start up encoder
		try {
			encoder = MessageDigest.getInstance(hashType);
		} 
		//If encoding variable fails immediately kill process after printing error code.
		catch (NoSuchAlgorithmException exp) {
			exp.printStackTrace();
			return;
		}
		
		System.out.println("Solving for New Block...");
		
		//TODO: Randomize Block Seed
		
		//TODO: Hash new block
		
		//TODO: Check New Hash for requirements
	}
	
}
























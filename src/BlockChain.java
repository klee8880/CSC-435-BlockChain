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
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Queue;
import java.util.Random;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author klee8
 * Data block to be solved
 * Contains signagure information, starting & ending hashes, & the data to be encoded.
 */
class DataBlock{
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
	public void setRandomSeed(String randomString) {this.randomSeed = randomString;}
	
	public String getEndHash() {return endHash;}
	public void setEndHash(String endHash) {this.endHash = endHash;}
	
	//Methods
	
	/**
	 * Returns a string used for hashing.
	 * Excludes any existing end hash from the block
	 * @return
	 */
	public String hashString() {
		return new StringBuilder()
				.append(startHash)
				.append(data)
				.append(randomSeed)
				.toString();
	}
	
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
 * Process state class that holds all the current process state values and constants.
 * Used as a reference by all functions to check in and update shared resources.
 * Only one instance of this class can exist. Shared among threads
 */
class ProcessState{
	//Singleton
	private static ProcessState instance = null;
	
	//Port Properties
	public int portNum;
	public int numPorts;
	public int unverifiedPort = 4710;
	public int blockChainPort = 4930;
	
	//Hashing properties
	public String hashType = "SHA-256";
	public int passPhrase = 1000;
	private boolean solving = false;	
	private int currentBlock = 0;
	private String currentHash = "00000000000000000000";
	
	//Constructors
	private ProcessState() {
		super();
	}

	
	//Getters and Setters
	public boolean getSolving() {return solving;}
	public void setSolving(Boolean solving) {this.solving = solving;}
	
	public int getCurrentBlock() {return currentBlock;}
	public void setCurrentBlock(int currentBlock) {this.currentBlock = currentBlock;}
	
	public String getCurrentHash() {return currentHash;}
	public void setCurrentHash(String currentHash) {this.currentHash = currentHash;}

	//Singleton requester
	protected static ProcessState getInstance() {
		if (instance == null) instance = new ProcessState();
		return instance;
	}
	
}

/**
 * @author klee8
 * Beginning of Block chain program spawns secondary processes and maintains queue for 
 */
public class BlockChain {

	//Properties
	Boolean solved = false;
	
	public static void main (String [] args) {
		//Get the singleton process state shared between processes. 
		ProcessState state = ProcessState.getInstance();
		
		String input = "";
		DataBlock block;
		BufferedReader inStream =  new BufferedReader(new InputStreamReader(System.in));
		
		//Parse Arguments
		//Get this ports # designation
		if (args.length < 1) state.portNum = 0; //Leave port as 0 for default
	    else {
	    	try {
	    		state.portNum = Integer.parseInt(args[0]);
	    	}
	    	//If integer cannot be parsed terminate process.
	    	catch (NumberFormatException ex) {
	    		System.out.println("Passed argument 1 is not a number value... Process terminated.");
	    		return;
	    	}
	    }
		
		//Get number of total ports
		if (args.length < 2) state.numPorts = 3;//Default to 3 if no argument provided.		
		else {
	    	try {
	    		state.numPorts = Integer.parseInt(args[0]);
	    	}
	    	//If integer cannot be parsed terminate process.
	    	catch (NumberFormatException ex) {
	    		System.out.println("Passed argument 2 is not a number value... Process terminated.");
	    		return;
	    	}
	    }
		
		//Mutate ports
		state.unverifiedPort += state.portNum;
		state.blockChainPort += state.portNum;
		
		//Splash Screen
		System.out.println("Now running Kevin Lee's Block Chain Application v.01");
		System.out.println("Running as process #" + state.portNum);
		
		//Spawn secondary processes Threads.
		new SolutionListener(state).start();
		System.out.println("Solution Listener Thread Spawned...");
		new RequestListener(state).start();
		System.out.println("Request Listener Thread Spawned...");
		
		//Wait a moment
		
		
		//UI Loop
		while (true) {
			//Prompt user for data.
			System.out.println("Please input a new phrase to commit to the block chain or QUIT to end the program.");
			System.out.print("Input: ");
			
			//Read in data to be encoded.
			try {
				input = inStream.readLine();
			} catch (IOException e) {
				System.out.println("Error occured reading input See below:");
				e.printStackTrace();
			}
			
			//Break loop on quit command.
			if (input.toUpperCase().equals("QUIT")) break;
			
			//TODO: Check if there is already a block being solved
			
			//Build the data block
			block = new DataBlock(state.getCurrentHash(), input);
			
			//TODO: Add Digital Signature
			
			//Build the request string.
			StringBuilder request = new StringBuilder("newBlock/")
					.append(state.getCurrentBlock()).append('/')
					.append(block.getStartHash()).append('/')
					.append(block.getData());
			
			//Contact ports to start solving process
			contactPorts(state.unverifiedPort,0,state.numPorts, request.toString());
		
		}
		
		//TODO: inform other processes of signoff?
		
		//End of process
		System.out.println("Block Chain process ended.");
		
	}
	
	private static void contactPorts(int port, int firstIndex, int secIndex, String message) {
		
		Socket sock;
		PrintStream toStream;
		
		while (firstIndex < secIndex) {
			
			//Contact all remote sockets and ask for a solution
			try {
				//Acquire next port
				sock = new Socket("localhost", port + firstIndex);
				toStream = new PrintStream(sock.getOutputStream());
				
				toStream.println(message);
				toStream.flush();
				
			} catch (IOException e) {
				System.out.println("Connection not found.");
			}
			
			firstIndex++;
		}
		
	}
}


//------SOLUTION RECEIVER STRUCTURE------
/**SolutionListener
 * @author klee8
 *
 */
class SolutionListener extends Thread{
	//Properties
	private ProcessState state;
	
	//Constructor
	public SolutionListener(ProcessState state) {
		this.state = state;
	}

	@Override
	public void run() {
		
		//Open Port
		
		//Listen for connection.
		
		//If solution is found inform the parent thread to stop searching for a solution and accept yours.
	}
	
}


//------NEW REQUEST STRUCTURE------
/**RequestListener
 * @author klee8
 *
 */
class RequestListener extends Thread{
	//Static Vars
	private static final int queueLength = 6;
	
	//Properties
	private ProcessState state;
	
	//Constructor
	public RequestListener(ProcessState state) {
		this.state = state;
	}

	@Override
	public void run() {
		Socket sock;

		//Open Port
		try {
			@SuppressWarnings("resource")
			ServerSocket servsock = new ServerSocket(state.unverifiedPort, queueLength);
			
			//Wait for a connection and handle
			while (true) {
				
				//accept new connection
				sock = servsock.accept();
				System.out.println("New request received");
				
				//Spawn new process
				new Solver(sock, state).start();
					
			}
		} catch (IOException ioe) {ioe.printStackTrace();}
		
	}
	
}


/**Solver
 * @author klee8
 * Takes input problem and solves for the given hash problem.
 * Repeatedly randomizes the given data block until the concatenated block's hash code meets the given criteria.
 */
class Solver extends Thread{
	
	//Properties
	private Socket sock;
	private ProcessState state;
	private DataBlock block;
	
	//Constructor
	public Solver(Socket sock, ProcessState state) {
		super();
		this.sock = sock;
		this.state = state;
	}

	//Methods
	@Override
	public void run() {
		BufferedReader in = null;
		
		try {
			//Establish port readers
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				
			//Collect data.
			String [] parsed = in.readLine().split("/");
			
			//Break if incorrect command
			if (!parsed[0].equals("newBlock") || parsed.length != 4) {
				return;
			}
			
			//Establish local copy of the block
			DataBlock block = new DataBlock(parsed[2], parsed[3]);
			
			//TODO: If already processing a request push to queue instead starting thread.
			
			//variables
			String randomString;
			StringBuffer hash;
			Random randomizer = new Random();
			byte byteData[];
			
			while(true) {
				//TODO: Add time to block.
				
				//Randomize Block Seed
				randomString = Integer.toHexString(randomizer.nextInt(1000000));
				block.setRandomSeed(randomString);
				
				//Hash new block.
				hash = new StringBuffer();
				
				MessageDigest digester = MessageDigest.getInstance(state.hashType);
				digester.update(block.hashString().getBytes());
				byteData = digester.digest();
				
				//TODO: Check New Hash for requirements, break loop if acceptable solution found
				//Parse last 2 bytes into an integer 
				hash.append("0x");
				for (int i = byteData.length - 4; i < byteData.length; i++) {
					hash.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
				}
				
				System.out.println(Long.decode(hash.toString()));
				
				if (Long.decode(hash.toString()) < 40000) {break;}
				
				//TODO: Check if someone else has already solved the problem.
			}
			
			hash = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				hash.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
			block.setEndHash(hash.toString());
			
			//TODO: Inform indexed processes of discovered solution.
		
		} catch (Exception ex) {ex.printStackTrace();}
	}	
}
























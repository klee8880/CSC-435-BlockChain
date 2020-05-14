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
import java.util.Random;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author klee8
 * Data block to be solved
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
 */
class ProcessState{
	public static String hashType = "SHA-256";
	public static int passPhrase = 1000;
	public static int unverifiedPort = 4710;
	public static int blockChainPort = 4930;
	private Boolean solving = false;	
	private int currentBlock = 0;
	
	//Constructors
	public ProcessState(Boolean solving, int currentBlock) {
		super();
		this.solving = solving;
		this.currentBlock = currentBlock;
	}
	public ProcessState() {
		super();
	}

	//Getters and Setters
	public boolean getSolving() {return solving;}
	public void setSolving(Boolean solving) {this.solving = solving;}
	
	public int getCurrentBlock() {return currentBlock;}
	public void setCurrentBlock(int currentBlock) {this.currentBlock = currentBlock;}
	
}

/**
 * @author klee8
 * Beginning of Block chain program spawns secondary processes and maintains queue for 
 */
public class BlockChain {

	//Properties
	Boolean solved = false;
	
	public static void main (String [] args) {
		
		int numPorts = 3;
		int port = 0;
		String input = "";
		DataBlock block;
		String startHash = "000000000000";
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
		int unverifiedPort = ProcessState.unverifiedPort + port;
		int blockChainPort = ProcessState.blockChainPort + port;
		
		//Splash Screen
		System.out.println("Now running Kevin Lee's Block Chain Application v.01");
		System.out.println("Running as process #" + port );
		
		//TODO: Spawn secondary processes Threads.
		new SolutionListener(blockChainPort, numPorts).start();
		new RequestListener(unverifiedPort, numPorts).start();
		
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
			
			//Build the data block
			block = new DataBlock(startHash, input);
			
			//TODO: Verify digital signature.
			
			
			//Stringify block to JSON
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String jsonString = gson.toJson(block);
			
			//Contact ports to start solving process
			contactPorts(unverifiedPort,0,0, jsonString);
		
		}
		
		//TODO: inform other processes of signoff?
		
		//End of process
		System.out.println("Block Chain process ended.");
		
	}
	
	private static void contactPorts(int port, int firstIndex, int secIndex, String message) {
		
		Socket sock;
		PrintStream toStream;
		
		while (firstIndex <= secIndex) {
			
			//Contact all remote sockets and ask for a solution
			try {
				//Acquire next port
				sock = new Socket("localhost", port + firstIndex);
				toStream = new PrintStream(sock.getOutputStream());
				
				toStream.print(message);
				
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
	private int port;
	private int numPorts;
	
	public SolutionListener(int port, int numPorts) {
		super();
		this.port = port;
		this.numPorts = numPorts;
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
	//Static Vars
	private static final int queueLength = 6;
	
	//Properties
	private int port;
	private int numPorts;
	
	public RequestListener(int port, int numPorts) {
		super();
		this.port = port;
		this.numPorts = numPorts;
	}
	
	@Override
	public void run() {
		Socket sock;
		
		System.out.println("Request Listener Thread Spawned...");

		//Open Port
		try {
			ServerSocket servsock = new ServerSocket(port, queueLength);
			
			//Wait for a connection and handle
			while (true) {
				
				//accept new connection
				sock = servsock.accept();
				System.out.println("New request received");
				
				//TODO: already processing a request push to queue instead starting thread.
				
				//Spawn new process
					
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
	private int fullIndex;
	private String hashType;
	private int passPhrase;
	private DataBlock block;

	//Constructor
	public Solver(Socket sock, int fullIndex, String hashType, int passPhrase, DataBlock block) {
		super();
		this.sock = sock;
		this.fullIndex = fullIndex;
		this.hashType = hashType;
		this.passPhrase = passPhrase;
		this.block = block;
	}
	
	//Methods
	@Override
	public void run() {
		//variables
		String randomString;
		
		//Start randomizer
		Random randomizer = new Random();
		
		System.out.println("Solving for New Block...");
		//TODO: Add time to block.
		
		//Randomize Block Seed
		randomString = Integer.toHexString(randomizer.nextInt(18354124));
		block.setRandomSeed(randomString);
		
		//Hash new block.
		
		//TODO: Check New Hash for requirements.
		
		//TODO: Inform indexed processes of discovered solution.
	}	
}
























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
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author klee8
 * Data block to be solved
 * Contains signature information, starting & ending hashes, & the data to be encoded.
 */
class DataBlock{
	//Used by other processes to know how many lines to look for in a stream.
	public static final int blockLines = 6;
	
	//Properties
	private String startHash = "";
	private String data;
	private String randomSeed = "";
	private String endHash = "";
	
	//Constructors
	public DataBlock(String startHash, String data) {
		super();
		this.startHash = startHash;
		this.data = data;
	}
	public DataBlock(String data) {
		super();
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
	
	//Other Properties
	private Queue<DataBlock> unsolved;
	private ArrayList<DataBlock> ledger;
	
	//Port Properties
	public int portNum;
	public int numPorts;
	public int unverifiedPort = 4710;
	
	//Hashing properties
	public String hashType = "SHA-256";
	public int passPhrase = 1000;
	private boolean solving = false;	
	private int blockIndex = 0;
	private String currentHash = "00000000000000000000";
	
	//Constructors
	private ProcessState() {
		super();
		this.unsolved = new LinkedList <DataBlock> ();
		this.ledger = new ArrayList <DataBlock> ();
	}

	
	//Getters and Setters
	public boolean getSolving() {return solving;}
	public void setSolving(Boolean solving) {this.solving = solving;}
	
	public int getCurrentBlock() {return blockIndex;}
	public void setCurrentBlock(int currentBlock) {this.blockIndex = currentBlock;}
	
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
		
		//Mutate port(s)
		state.unverifiedPort += state.portNum;
		
		//Splash Screen
		System.out.println("Now running Kevin Lee's Block Chain Application v.01");
		System.out.println("Running as process #" + state.portNum + '\n');
		
		//Spawn secondary processes Thread(s).
		new RequestListener(state).start();
		System.out.println("Request Listener Thread Spawned...");
		
		//UI Loop
		while (true) {
			//Prompt user for data.
			System.out.println("\nAccepted Inputs (WIP):");
			System.out.println("C - Display each block with credit for the process that solves it");
			System.out.println("R - Read a new file to record (R <File Name>)");
			System.out.println("V - Verify Blocks");
			System.out.println("L - Retreive and display Lines");
			System.out.print("Input: ");
			
			//Read in data to be encoded.
			try {
				input = inStream.readLine();
			} catch (IOException e) {
				System.out.println("Error occured reading input See below:");
				e.printStackTrace();
			}
			
			//Handle Inputs
			switch(input) {
			case "C":
				break;
			case "R":
				readFile(state, inStream);
				break;
			case "V":
				break;
			case "L":
				break;
			default:
				break;
			}
		
		}
		
	}
	
	private static void readFile(ProcessState state, BufferedReader in) {
		DataBlock block;
		String input = "";
		
		//TODO: Change to actually read a file.
		
		System.out.println();
		System.out.print("Line to input:");
		
		try {
			input = in.readLine();
		} catch (IOException e) {e.printStackTrace();}
		
		//Build the data block
		block = new DataBlock(input);
		
		//TODO: Add Digital Signature
		
		//Set up JSON builder
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		System.out.println(gson.toJson(block));
		
		//Contact ports to start solving process
		contactPorts(state.unverifiedPort,0,state.numPorts, gson.toJson(block));
	}
	
	public static void contactPorts(int port, int firstIndex, int secIndex, String message) {
		
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

class BlockAdder extends Thread{
	
	//Properties
	Socket sock;
	String [] arguements;
	
	//Constructor
	public BlockAdder(Socket sock, String[] arguements) {
		super();
		this.sock = sock;
		this.arguements = arguements;
	}

	@Override
	public void run() {
		
		//Read in arguments
		
		//Read in JSON Block
		
		//Check that the hash is valid
		
		//Update state of program
		
		//Start hashing next block if there are blocks in the queue
		
	}
}

/**RequestListener
 * @author klee8
 * Listen for new connections and handle for any valid request string.
 */
class RequestListener extends Thread{
	//Static Vars
	private static final int queueLength = 6;
	
	//Properties
	private ProcessState state;
	
	//Constructor
	public RequestListener(ProcessState state) {
		super();
		this.state = state;
	}

	@Override
	public void run() {
		//Open Port
		try {
			@SuppressWarnings("resource")
			ServerSocket servsock = new ServerSocket(state.unverifiedPort, queueLength);
			
			//Wait for a connection and handle
			while (true) {
				
				//accept new connection
				Socket sock = servsock.accept();
				System.out.println("New request received");
				
				//Read & Parse Request.
				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				StringBuilder json = new StringBuilder();
				
				for (int i = 0; i < DataBlock.blockLines; i++) {
					json.append(in.readLine());
				}
				
				System.out.println(json.toString());
				
				DataBlock block = new Gson().fromJson(json.toString(), DataBlock.class);
				
				//Start new thread.
				new Solver(block, state).start();
				
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
	private DataBlock block;
	private ProcessState state;
	
	//Constructor
	public Solver(DataBlock block, ProcessState state) {
		super();
		this.block = block;
		this.state = state;
	}

	//Methods
	@Override
	public void run() {
		
		try {
			
			System.out.println(block.toString());
			
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
				
				//Check New Hash for requirements, break loop if acceptable solution found
				//Parse last 2 bytes into an integer 
				hash.append("0x");
				for (int i = byteData.length - 4; i < byteData.length; i++) {
					hash.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
				}
				
				if (Long.decode(hash.toString()) < 5000) {break;}
				
				//TODO: Check if someone else has already solved the problem.
				
			}
			
			hash = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				hash.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
			block.setEndHash(hash.toString());
			
			System.out.println("Solution found: " + block.getEndHash());
			
			//Generate a message string that notifies other processes that of a completed block
			//Includes all the information from 
			StringBuilder message = new StringBuilder("newSolution/")
					.append(state.getCurrentBlock())	.append('/')
					.append(block.getStartHash())		.append('/')
					.append(block.getData())			.append('/')
					.append(block.getRandomSeed())		.append('/')
					.append(block.getEndHash());
			
			//TODO: Inform indexed processes of discovered solution.
			BlockChain.contactPorts(state.portNum, 0, 0, message.toString());
		
		} catch (Exception ex) {ex.printStackTrace();}
	}	
}
























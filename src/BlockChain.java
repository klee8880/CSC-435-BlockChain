/*--------------------------------------------------------

1. Name / Date: Kevin Lee 5/12/2020

2. Java version used, if not the official version for the class:

build 1.8.0_161

3. Precise command-line compilation examples / instructions:

> javac -cp "gson-2.8.2.jar" *.java run twice in a directory with the supporting gson jar file

4. Precise examples / instructions to run this program:

In separate shell windows or computers:

> java -cp "gson-2.8.2.jar"; BlockChain a b
	a == this process # (Optional: Defaults to 0)
	b == total processes (Optional: Defaults to 3)
	
Start all three programs first then press enter in all three to send information to each other.

All acceptable commands are displayed on the various consoles.

5. List of files needed for running the program.

 a. 

5. Notes:

----------------------------------------------------------*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author klee8
 * Data block to be solved
 * Contains signature information, starting & ending hashes, & the data to be encoded.
 */
class DataBlock implements Comparable<DataBlock>{
	//Used by other processes to know how many lines to look for in a stream.
	public static final int requirement = 4000;
	
	//Properties
	private String startHash = "";
	private String data;
	private String solver = "DEFAULT";
	private String randomSeed = "";
	private String endHash = "";
	
	//Constructors
	public DataBlock(String startHash, String data) {
		super();
		this.startHash = startHash;
		this.data = data;
		//Generate Public/Private Keys
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
	
	public String getSolver() {return solver;}
	public void setSolver(String solver) {this.solver = solver;}
	
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
				.append(solver)
				.append(randomSeed)
				.toString();
	}
	
	/* 
	 * Concatenate the string of values into a single descriptive string.
	 * Description string can also be used in hashing methods
	 */
	@Override
	public String toString() {
		return new StringBuilder()
				.append(startHash).append('\n')
				.append(data).append('\n')
				.append(solver).append('\n')
				.append(randomSeed).append('\n')
				.append(endHash)
				.toString();
	}

	/**toJSON
	 * Data block object to a single line of JSON.
	 * @return JSON String
	 */
	public String toJson() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(this).replace("\n", "");
	}

	/**
	 * @param hashType - the hash protocol to use
	 * Generate a new hash given the current blocks written information
	 * DOES NOT guarantee that the hash meets the requirements.
	 * @return the ending Hash that was updated to the object
	 * @throws NoSuchAlgorithmException
	 */
	public String generateHash(String hashType) throws NoSuchAlgorithmException {
		//Get instance of digester
		MessageDigest digester = MessageDigest.getInstance(hashType);
		//Hash using the provided protocol
		digester.update(hashString().getBytes());
		byte[] byteData = digester.digest();
		
		//Convert byte array into a string
		StringBuilder hash = new StringBuilder();
		for (int i = 0; i < byteData.length; i++) {
			hash.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}
		
		return hash.toString();
	}
	
	/**Check that the hash meets the set requirement for the block.
	 * @return boolean representing hash check result
	 */
	public boolean checkHash() {
		StringBuilder sb = new StringBuilder("0x").append(endHash.substring(endHash.length() - 8));

		//Return false if requirement is not met
		if (Long.decode(sb.toString()) < DataBlock.requirement) return true;
		else return false;
		
	}
	
	//TODO: implement compareTo function
	/* compareTo
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DataBlock o) {
		return this.data.compareTo(o.data);
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
	
	//Semaphore used to lock hashing process a block is being processed
	public Semaphore hashingLock = new Semaphore(1);
	
	//Other Properties
	public PriorityBlockingQueue<DataBlock> unsolved;
	public ArrayList<DataBlock> ledger;
	
	//Key Properties
	private final String keyType = "RSA";
	public final KeyPair localKeys = setKeys(); //Set the Keys. Separated from code block for simplicity. No user input needed.
	public final Map<Integer, PublicKey> keyList = new Hashtable<Integer, PublicKey>();;
	
	//Port Properties
	public int portNum;
	public int totalPorts;
	public final int unverifiedPort = 4820;
	public final int verifiedPort = 4930;
	public final int keyPort = 4710;
	
	//Hashing properties
	public final String hashType = "SHA-256";
	public Semaphore solvingLock = new Semaphore(1);
	private int blockIndex;
	
	//Constructors
	//Private constructor so it can't be built outside of the getInstance method
	private ProcessState() throws Exception {
		super();
		this.unsolved = new PriorityBlockingQueue <DataBlock> ();
		this.ledger = new ArrayList <DataBlock> ();
		
		//Set up first block
		DataBlock block = new DataBlock("");
		block.setEndHash("00000000000000000000");
		block.setData("ROOT BLOCK: DUMMY DATA");
		
		//Add this data block as the first seed block for the 
		ledger.add(block);
	}

	
	//Getters and Setters
	public int getCurrentBlock() {return blockIndex;}
	public void setCurrentBlock(int currentBlock) {this.blockIndex = currentBlock;}
	
	public String getCurrentHash() {
		return this.ledger.get(ledger.size() - 1).getEndHash();
	}

	/**setKeys
	 * Set the public & private keys for this process
	 * @throws NoSuchAlgorithmException 
	 * @throws NoSuchProviderException 
	 */
	private KeyPair setKeys() throws Exception {
		//Randomize seed
		long seed = new Random().nextLong();
		
		//Key Pair generation
		KeyPairGenerator generator = KeyPairGenerator.getInstance(keyType);
		SecureRandom rng = SecureRandom.getInstance("SHA1PRNG","SUN");
		rng.setSeed(seed);
		generator.initialize(1024, rng);
		
		return generator.generateKeyPair();
	}
	
	//Singleton requester
	protected static ProcessState getInstance() throws Exception {
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
	
	public static void main (String [] args) throws Exception {
		//Get the singleton process state shared between processes. 
		ProcessState state;
		try {
			state = ProcessState.getInstance();
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}
		
		String[] input = {""};
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
		if (args.length < 2) state.totalPorts = 3;//Default to 3 if no argument provided.		
		else {
	    	try {
	    		state.totalPorts = Integer.parseInt(args[0]);
	    	}
	    	//If integer cannot be parsed terminate process.
	    	catch (NumberFormatException ex) {
	    		System.out.println("Passed argument 2 is not a number value... Process terminated.");
	    		return;
	    	}
	    }
		
		//Spawn semaphore for later
		Semaphore gate = new Semaphore(1);
		gate.acquire();
		
		//Spawn thread for waiting for key port notifications
		new KeyListener(state, gate).start();
		System.out.println("New Process Listener Thread Spawned...");
		
		//Prompt the user to start the program. Begins by transmitting it's public key to everyone.
		System.out.println("Press (Enter) to begin Processing");
		try {System.in.read();} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		
		//Notify other processes of your key
		distributeKeys(state);
		
		//Wait for the key listener to collect the specified # of keys.
		gate.acquire();
		
		//Splash Screen
		System.out.println("Now running Kevin Lee's Block Chain Application v.05");
		System.out.println("Running as process #" + state.portNum + '\n');
		
		//Spawn secondary processes Thread(s).
		new RequestListener(state).start();
		System.out.println("Requested Block Listener Thread Spawned...");
		new CompleteListener(state).start();
		System.out.println("Complete Block Listener Thread Spawned...");
		
		//UI Loop
		while (true) {
			//Prompt user for data.
			System.out.println("\nAccepted Inputs:");
			System.out.println("C - Display each block with credit for the process that solves it");
			System.out.println("R - Read a new file to record (R <File Name>)");
			System.out.println("V - Verify Blocks");
			System.out.println("L - List committed information");
			System.out.print("Input: ");
			
			//Read in data to be encoded.
			try {
				
				input = inStream.readLine().split(" ");
				//Handle Inputs
				switch(input[0].toUpperCase()) {
				case "C": viewLedger(state); break;
				case "R": readFile(state, input); break;
				case "V": verifyLedger(state.ledger); break;
				case "L": listLedger(state); break;
				default: System.out.println("\nUnsupported Command Provided..."); break;
				}
				
			} catch (IOException e) {
				System.out.println("Error occured reading input See below:");
				e.printStackTrace();
			}
		}
		
	}
	
	private static void readFile(ProcessState state, String [] args) throws IOException{
		//Check the number of input arguments.
		if (args.length < 2) throw new IOException("invallid # of elements");
		
		//Spawn file reader
		try (BufferedReader reader = new BufferedReader(new FileReader(args[1]))){
			
			//build string command
			StringBuilder sb = new StringBuilder("newBlocks ").append(state.portNum).append(' ');
			StringBuilder jsonBlocks = new StringBuilder();
			
			int count = 0;
			//read in blocks from file
			String nextJSON = reader.readLine();
			while (nextJSON != null) {
				jsonBlocks.append(nextJSON).append('\n');
				
				//TODO: Add Digital Signature
				
				count++;
				
				nextJSON = reader.readLine();
			}
			
			//Finish building message string
			sb.append(count).append('\n');
			sb.append(jsonBlocks);
			
			System.out.println(sb.toString());
			
			//Contact ports to start solving for blocks
			contactPorts(state.unverifiedPort,0,state.totalPorts, sb.toString());
		}
	}
	
	private static void viewLedger(ProcessState state) {
		//For each entry in the ledger print a line
		System.out.println();
		System.out.println("Number of Blocks: " + state.ledger.size());
		
		//For each loop to print out each line of the ledger
		for (DataBlock block: state.ledger) {
			System.out.println();
			System.out.println(block.getSolver() + ", " + block.toJson());
		}
	}
	
	private static void listLedger(ProcessState state) {
		//For each entry in the ledger print a line
		System.out.println();
		System.out.println("Number of Blocks: " + state.ledger.size());
		
		//For each loop to print out each line of the ledger
		for (DataBlock block: state.ledger) {
			System.out.println();
			System.out.println(block.getSolver() + ", " + block.getData());
		}
	}
	
	private static void verifyLedger(List <DataBlock> ledger) {
		DataBlock previous = null;
		int verified = ledger.size();
		
		for (DataBlock block: ledger) {
			//If first block update previous and move to next block.
			if (previous == null) {
				previous = block;
				continue;
			}
			else {
				//If the block does not match the previous decrement counter
				if (!block.getStartHash().equals(previous.getEndHash()))
					verified--;
				
				previous = block;
			}
		}
		
		System.out.println("Blocks verified: " + verified + " of " + ledger.size());
		
	}
	
	/**distributeKeys
	 * Distribute this processes' public key to all specified ports so they have record of how to handle incoming blocks
	 * @param state
	 */
	private static void distributeKeys(ProcessState state) {
		//Encode the message
		StringBuilder sb = new StringBuilder().append(state.portNum).append(' ');
		//Convert Key to string
		byte[] byteKey = state.localKeys.getPublic().getEncoded();
		String stringKey = Base64.getEncoder().encodeToString(byteKey);
		sb.append(stringKey);
		
		System.out.println(sb.toString());
		contactPorts(state.keyPort, 0, state.totalPorts, sb.toString());
	}
	
	/**contactPorts
	 * Contact all ports in the selection with the given message.
	 * @param port
	 * @param firstIndex
	 * @param secIndex
	 * @param message
	 */
	public static void contactPorts(int port, int firstIndex, int secIndex, String message) {
		
		Socket sock;
		PrintStream toStream;
		
		while (firstIndex < secIndex) {
			
			//Contact all remote sockets and ask for a solution
			try {
				//Acquire next port
				System.out.println("Contacting: " + (port + firstIndex));
				sock = new Socket("localhost", port + firstIndex);
				toStream = new PrintStream(sock.getOutputStream());
				
				toStream.println(message);
				toStream.flush();
				
			} catch (IOException e) {}
			
			firstIndex++;
		}
		
	}
}

class KeyListener extends Thread{
	//Static Vars
	private static final int queueLength = 6;
	
	//Properties
	private ProcessState state;
	private Semaphore completionGate;
	private int processCount = 0;
	private KeyFactory decoder;
	
	public KeyListener(ProcessState state, Semaphore completionGate) throws NoSuchAlgorithmException {
		super();
		this.state = state;
		this.completionGate = completionGate;
		decoder = KeyFactory.getInstance("RSA");
	}

	@Override
	public void run(){
		//Open Port
		try {
			@SuppressWarnings("resource")
			ServerSocket servsock = new ServerSocket(state.keyPort + state.portNum, queueLength);
			
			//Wait for a connection and handle
			while (true) {
				//process#/publicKey
				//accept new connection
				Socket sock = servsock.accept();
				System.out.println("New process signing in");
				
				//Process request
				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				String[] args =  in.readLine().split(" ");
				//Check correct # of arguments
				if (args.length < 2) return;
				
				//Parse process #
				int portNum = Integer.parseInt(args[0]);
				//Parse Key
				byte[] publicBytes = Base64.getDecoder().decode(args[1]);
				X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(publicBytes);
				PublicKey RestoredKey = decoder.generatePublic(pubSpec);
				
				//break if this key is already contained
				if (state.keyList.containsKey(portNum)) return;
				//Add new key if needed
				state.keyList.put(portNum, RestoredKey);
				//Increment saved keys
				processCount++;
				System.out.println("Total Processes: " + processCount);
				
				//If all keys are accounted for release main's gate and let it continue
				if (processCount >= state.totalPorts) {
					System.out.println("All processes accounted for");
					completionGate.release();
				}
			}
		} catch (Exception ioe) {ioe.printStackTrace();}
	}
	
}

/**
 * @author klee8
 * Listener to wait for completed block notifications.
 */
class CompleteListener extends Thread{
	//Static Vars
	private static final int queueLength = 6;
	
	//Properties
	private ProcessState state;
	
	//Constructor
	public CompleteListener(ProcessState state) {
		super();
		this.state = state;
	}	
	
	@Override
	public void run(){
		//Open Port
		try {
			@SuppressWarnings("resource")
			ServerSocket servsock = new ServerSocket(state.verifiedPort + state.portNum, queueLength);
			
			//Wait for a connection and handle
			while (true) {
				
				//accept new connection
				Socket sock = servsock.accept();
				System.out.println("New completed block received");
				
				//Read & Parse Request.
				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				DataBlock block = new Gson().fromJson(in.readLine(), DataBlock.class);
				
				//Start new thread.
				new BlockAdder(state, block).start();
				
				sock.close();
			}
		} catch (IOException ioe) {ioe.printStackTrace();}
	}
}

class BlockAdder extends Thread{
	
	//Properties
	ProcessState state;
	DataBlock block;
	private static Semaphore gate = new Semaphore(1);

	//Constructor
	public BlockAdder(ProcessState state, DataBlock block) {
		super();
		this.state = state;
		this.block = block;
	}

	@Override
	public void run() {
		
		try {
			//Acquire semaphore locks
			state.hashingLock.acquire();// Stop processing of next block
			gate.acquire();//Stop processing of any other completed blocks

			//Check if the hash is valid
			if (!block.checkHash()) {
				System.out.println("FAILURE: Block's end hash does not match requirement");
				state.hashingLock.release();
				return;
			}
			System.out.println("SUCCESS: Hash requirments met");
			
			//Rehash block and compare 
			String newHash = block.generateHash(state.hashType);
			
			//Check if this hash matches the starting block
			if (!block.getEndHash().equals(newHash)) {
				System.out.println("FAILURE: Hash does not match block");
				state.hashingLock.release();
				return;
			}
			System.out.println("SUCCESS: Hash matches provided");
			
			//Check that the beginning hash matches the ending hash on the ledger
			String previousHash = state.ledger.get(state.ledger.size() - 1).getEndHash();
			if (!block.getStartHash().equals(previousHash)) {
				System.out.println("FAILURE: Hash does not fit the ledger.");
				state.hashingLock.release();
				return;
			}
			System.out.println("SUCCESS: Hash matches the current ledger");
			
			//TODO: Check if the block is still on the queue
			boolean found = false;
			for (DataBlock b: state.unsolved) {
				if (b.getData().equals(block.getData())) {
					state.unsolved.remove(b);
					found = true;
				}
			}
			
			if (!found) {
				System.out.println("Failure: Block not in ledger");
				return;
			}
			else {
				System.out.println("SUCCESS: Block located");
			}
			
			//Update the ledger
			state.ledger.add(block);
			
			//Rewrite data file if you are process 0
			if (state.portNum == 0) writeJSONFile("BlockchainLedger.json", state.ledger);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			//release semaphore lock no matter what to avoid process locking out.
			state.hashingLock.release();
			gate.release();
		}
		

	}
	
	/**writeJSONFile
	 * commits the given array to hardDisk by writing it to a file.
	 * @param name - Name of the file
	 * @param array - the array of objects you wish to write
	 */
	private void writeJSONFile(String name, ArrayList <DataBlock> array) {
		
		//Open file
		try(FileWriter out = new FileWriter(name)){
			//Write file
			 Gson gson = new GsonBuilder().setPrettyPrinting().create();
			
			for(DataBlock block: array) {
				gson.toJson(block, out);
			}
			
			//Close the file
			out.close();
			
		} catch (IOException e) {e.printStackTrace();}
		
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
			ServerSocket servsock = new ServerSocket(state.unverifiedPort + state.portNum, queueLength);
			
			//Wait for a connection and handle
			while (true) {
				
				//accept new connection
				Socket sock = servsock.accept();
				System.out.println("New request received");
				
				//Start new thread.
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
	
	//Constructor
	public Solver(Socket sock, ProcessState state) {
		super();
		this.sock = sock;
		this.state = state;
	}

	//Methods
	@Override
	public void run() {
		
		try {
			//Read the socket connection (if there is one)
			if (sock != null) {
				ArrayList<DataBlock> newBlocks = readRequest(sock);
				sock.close();
						
				//Add the newest blocks to the queue
				for (DataBlock block: newBlocks) {
					//Add identifying information to block for the current processes
					block.setSolver("Process " + state.portNum);
					//Add to queue
					state.unsolved.add(block);
				}
			}

			solveBlocks(); //Recursively solve blocks
		} catch (Exception ex) {ex.printStackTrace();}
	}
	
	/**Recursively solve blocks of data until there is nothing in the queue.
	 * 
	 */
	private void solveBlocks() {
		//If there is nothing in the queue terminate
		if (state.unsolved.isEmpty()) return;
		//If already processing a request just end here and let the other process handle it.
		if (!state.solvingLock.tryAcquire()) return;
		
		System.out.println("Beginning solving cycle...");
		
		try {
			//variables
			Random randomizer = new Random();
			long attempt = 1;
			DataBlock block;
			
			while(true) {
				
				//Acquire semaphore lock
				state.hashingLock.acquire();
				
				//acquire the correct block from the list.
				block = state.unsolved.peek();
				
				if (block == null) return;
				
				//Update start hash in case newest locked block has changed
				block.setStartHash(state.getCurrentHash());
				
				//TODO: Add time to block.
				
				//Randomize Block Seed
				String randomString = Integer.toHexString(randomizer.nextInt(1000000));
				block.setRandomSeed(randomString);
				
				//Generate a new hash
				block.setEndHash(block.generateHash(state.hashType));
				
				//Check if the hash is valid.
				if (block.checkHash()) break;
				
				//Release semaphore lock
				state.hashingLock.release();
				
				attempt++;
			}
			
			//Remove the now solved block
			//state.unsolved.remove(block);
			
			//Print to console
			System.out.println("Solution found on attempt #(" + attempt + "): " + block.getEndHash());
			
			//Inform indexed processes of discovered solution.
			BlockChain.contactPorts(state.verifiedPort, 0, state.totalPorts, block.toJson());
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {//Release locks even when exceptions happen
			//Release hold on the semaphores
			state.hashingLock.release();
			state.solvingLock.release();
		}
		
		//Recurse
		solveBlocks();
	
	}
	
	/**readRequest
	 * @param sock
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	private ArrayList <DataBlock> readRequest(Socket sock) throws IllegalArgumentException, IOException{
		//Parameters
		ArrayList<DataBlock> blocks = new ArrayList<DataBlock> ();
		BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		Gson gson = new Gson();

		//Read first string or arguments
		String [] args = in.readLine().split(" ");
		
		//Read remaining blocks into JSON
		for (int i = 0; i < Integer.parseInt(args[2]); i++)
			blocks.add(gson.fromJson(in.readLine(), DataBlock.class));
		
		//Console print lines
		System.out.println("Received Blocks: ");
		for (int i = 0; i < blocks.size(); i++)
			System.out.println(blocks.get(i).toJson());
		
		//TODO: Validate signatures
		
		return blocks;
	}
}
























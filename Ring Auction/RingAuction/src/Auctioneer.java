import java.net.*;
import java.io.*;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Auctioneer{

    private Random rnd;

    private ServerSocket in_ss;

    private Socket in_soc;
    private Socket out_soc;
     

    String	localhost = "127.0.0.1";
    String 	n_host_name;
    
    int 	in_port;
    int 	out_port;

    int         token;
 
       
     public Auctioneer (int inpor, int outpor){

	rnd = new Random();
	in_port = inpor;
	out_port = outpor;
	RandomAccessFile current_bid;
	int the_bid = rnd.nextInt(100);  //>>>  THis is used to set the initial price

    	System.out.println("Auctioneer: " +in_port+ " of distributed lottery is active ....");

	// AUCTIONEER:
	// creates the file bid.txt containing the initial price (the_bid)
	// generates and forwards the token to the next node in the ring
	// waits to receive the token back and concludes the auction

	try {
	    // >>>  Create and instantiates the file named bid.txt with the initial price (the_bid)
		File bid_Record = new File("bid.txt");
		if(bid_Record.createNewFile())
			System.out.println("File: bid.txt successfully created");
		else
			System.out.println("File: bid.txt already exists");

		BufferedWriter writer = new BufferedWriter(new FileWriter("bid.txt"));
		writer.write("The starting price is: " + the_bid);
		writer.flush();
	}
	catch (IOException e) {System.out.println("Exception when opening the file "+ e);}

	//Saving bid on current_bid
	try
	{
		current_bid = new RandomAccessFile("current_bid.txt", "rw");
		current_bid.seek(0);
		current_bid.writeInt(the_bid);
		current_bid.close();
	}catch(IOException e){System.out.println("Error: Unable to write on current_bid " + e);}

	System.out.println("Auctioneer: " +in_port+ " -  STARTING AUCTION  with price = "+the_bid);

	
	try{Thread.sleep(1000);} catch (Exception e){}
	
	
	try{
	    // >>>
	    // **** Forward the the token through a socket that connects to the out_port;
		//attempting connection to output socket
		while(out_soc == null)
		{
			try
			{
				out_soc = new Socket(n_host_name, outpor);
			}
			catch (IOException e) {System.out.println("Error: Auctioneer Connection " + e);}
		}

		//Parsing token through socket
		DataOutputStream out;
		try
		{
			out = new DataOutputStream(out_soc.getOutputStream());
			out.write(token);
			out.flush();
		}catch (IOException e){System.out.println("Error: Auctioneer token unable to send " + e);}


	    System.out.println("Auctioneer: " +in_port+ " :: sent token to "+out_port);
	    try{Thread.sleep(1000);} catch (Exception e){}
	    
	    // >>>
	    // **** Wait for the token and receive it from the in_port through a suitable socket-based server  mechanism;

		in_ss = new ServerSocket(inpor);
	    in_soc = in_ss.accept();

		while(in_soc.getInputStream().available() == 0)
		{
			System.out.println("waiting");
			wait();
		}
		InputStream in = in_soc.getInputStream();
		BufferedReader bin = new BufferedReader(new InputStreamReader(in));
	    
	    System.out.println("Auctioneer: " +in_port+ " :: received token back");
	    try{Thread.sleep(1000);} catch (Exception e){}
	}

	//>>>  Select a more specific exception type where possible/appropriate !
	catch (Exception e) {
	    System.out.println(e);
	    System.exit(1);	
	}
     }

    public static void main (String args[]){
	
	String n_host_name = ""; 
	int n_port;
	int iter;
	
	if (args.length != 2 ) {
	    System.out.print("Usage: Auctioneer [port number] [forward port number]");
	    System.exit(1);
	}
	
	// get the IP address and the port number of the node
 	try{ 
	    InetAddress n_inet_address =  InetAddress.getLocalHost() ;
	    n_host_name = n_inet_address.getHostName();
	    System.out.println ("node hostname is " +n_host_name+":"+n_inet_address);
    	}
    	catch (java.net.UnknownHostException e){
	    System.out.println(e);
	    System.exit(1);
    	} 
	
    	Auctioneer a = new Auctioneer(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }
    
    
    
    
}


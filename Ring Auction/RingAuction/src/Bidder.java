import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Bidder{

    private Random rnd;

    private ServerSocket in_ss;

    private Socket in_soc;
    private Socket out_soc;
     

    String	localhost = "127.0.0.1";
    String 	n_host_name;
    
    int 	in_port;
    int 	out_port;

    int         token;
 
       

    // BIDDER:
    // receives the token by a socket communication through in_port;
    // performs its auction algorithm: 
    //   - reads from bid.txt, 
    //   - decides whether to bit or not, 
    //   - in case of bidding, updates bid;
    // forwards the lottery token by a socket communication through out_port 



    public Bidder (int inpor, int outpor){
	
	rnd = new Random();
	in_port = inpor;
	out_port = outpor;

	RandomAccessFile current_bid;
	int the_bid = 0;
	
    	System.out.println("Bidder  : " +in_port+ " of distributed lottry is active ....");

	try {
		in_ss = new ServerSocket(in_port);
	// >>>
	// **** Wait for the token and receive it from a socket listening on the in_port;
		in_soc = in_ss.accept();
		InputStreamReader in = new InputStreamReader(in_soc.getInputStream());
		BufferedReader bf = new BufferedReader(in);

		while(in_soc.getInputStream().available()==0)
		{
			System.out.println("waiting");
			wait();
		}
	    
	// >>>
	// **** Perform the auction algorithm (with suitable delay/sleep of 1 sec);

		//updating the_bid
		try
		{
			current_bid = new RandomAccessFile("current_bid.txt", "rw");
			current_bid.seek(0);
			the_bid = current_bid.readInt();
		}catch(IOException e){System.out.println("Error: Unable to write on current_bid " + e);}

		//chosing to bid or not
		String str;
		if(rnd.nextInt(2) == 1)
		{
			the_bid = the_bid + 10;
			str = "Node: " + in_port + " my bid is " + (the_bid);
		}
		else
			str = "Node: no bid";

		//updating the_bid
		try
		{
			current_bid = new RandomAccessFile("current_bid.txt", "rw");
			current_bid.seek(0);
			current_bid.writeInt(the_bid);
			current_bid.close();
		}catch(IOException e){System.out.println("Error: Unable to write on current_bid " + e);}

		//updating current_bid
		try
		{
			BufferedWriter write = new BufferedWriter(new FileWriter("bid.txt", true));
			write.newLine();
			write.write(str);
			write.flush();
		}catch(IOException e){System.out.println("Error: Bidder unable to write " + e);}

		try{Thread.sleep(1000);}catch(Exception e){};

		System.out.println("Node: " + in_port + " - received token. My bid is " + the_bid);

	// >>>
	// **** Forward the  token through the  out_port;
		while(out_soc == null)
		{
			try
			{
				out_soc = new Socket(n_host_name, outpor);
			}
			catch (IOException e){System.out.println("Error: Auctioneer Connection " + e); wait(5000);}
		}

		DataOutputStream out;
		try
		{
			out = new DataOutputStream(out_soc.getOutputStream());
			out.write(token);
			out.flush();
		}catch (IOException e){System.out.println("Error: Auctioneer token unable to send " + e);}


		System.out.println("Bidder  : " +in_port+ " - forwarded token to "+out_port);
	
	}
	catch (Exception e) {
	    System.out.println(e);
	    System.exit(1);	
	}
    }

    
        
    public static void main (String args[]){
	
	String n_host_name = ""; 
	int n_port;
	
	// receive own port and next port in the ring at launch time
	if (args.length != 2) {
	    System.out.print("Usage: Bidder [port number] [forward port number]");
	    System.exit(1);
	}
	
	// get the IP address of the node  - might be useful on multi-computer runs 
 	try{ 
	    InetAddress n_inet_address =  InetAddress.getLocalHost() ;
	    n_host_name = n_inet_address.getHostName();
	    System.out.println ("node hostname is " +n_host_name+":"+n_inet_address);
    	}
    	catch (java.net.UnknownHostException e){
	    System.out.println(e);
	    System.exit(1);
    	} 
	
    	Bidder b = new Bidder(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }
    
    
}


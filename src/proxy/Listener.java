/** 
* Listener
*
* port listener class for Swing Listener 
*
*/

package proxy ;

// imports
import java.util.ArrayList;
import proxy.Handler;
import static proxy.SwingGUI.println;
import java.net.Socket;
import java.net.ServerSocket;


public class Listener implements Runnable
// implements Runnable because it is started as a thread by the gui
{
	// class variables
	static int handlerID = 0;
	
    // instance variables 
    int port;
    Blocker blocker;
    ArrayList<Thread> handlers; // all handlers opened by the listener
    ServerSocket incomingSocket; // initialises a socket using the port
    
    /**
     * Listener()
     * constructs a new listener instance
     * @param prt - port number that the Listener opens a socket to
     * @param b - Blocker instance to be passed on to the Handler threads
     */
    // constructor
    public Listener (int prt, Blocker b)
    {
        this.port = prt;
        this.blocker = b;
        // set up array list for threaded handlers
        handlers = new ArrayList<>();
        
        // set up incomingSocket with given port
        try
        {
        	incomingSocket = new ServerSocket (this.port) ;
        }
        catch (Exception e)
        {
        	println("Exception in Listener.Listener() - could not set up Server Socket");
        }
        
    }
    
    /**
     * run()
     * runs the Listener thread which starts a new Handler thread for each request received on its given socket
     */
    public void run()
    {   	
    	while(true)
		{
			try 
			{
				// wait for connections on existing server socket 
				Socket skt = incomingSocket.accept();
				
				// Construct new handler thread
				Thread t = new Thread(new Handler(skt, handlerID++, this.blocker));
				
				// store threads in array list as created
				this.handlers.add(t);
				
				t.start();	
			} 
			catch (Exception e) 
			{	
				e.printStackTrace();			 
				println("Exception in Listener.run() - could not start new handler");
				return;
			} 

		}
    }
  
}

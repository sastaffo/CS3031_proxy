/** 
* Listener
*
* port listener class for Swing Listener 
*
*/

package proxy ;

import java.util.ArrayList;
import proxy.Handler;
import static proxy.SwingGUI.println;

// import java.io.IOException;

import java.net.Socket;
// import java.net.SocketException;
import java.net.ServerSocket;

import static proxy.SwingGUI.println;

// import swinglistener.Shandler;

public class Listener implements Runnable
// implements Runnable because it is started as a thread by the gui
{
    // class variables 
    int port; // the port on which to listen
    
    ArrayList<Thread> handlers;
    
    ServerSocket incomingSocket;
    
    
    // constructor
    public Listener (int prt)
    {
        this.port = prt;
        // set up array list for threaded handlers
        handlers = new ArrayList<>();
        
        // set up incomingSocket with given port
        try
        {
        	incomingSocket = new ServerSocket (this.port) ;
        }
        catch (Exception e)
        {
        	println(0, "Exception in Listener.Listener() - could not set up Server Socket");
        }
        
    }
    
    public void run()
    {   	
    	while(true)
		{
			try 
			{
				// wait for connections on existing server socket 
				Socket skt = incomingSocket.accept();
				
				// Construct new handler thread
				Thread sh = new Thread(new Handler(skt));
				
				// store threads in array list as created
				this.handlers.add(sh);
				
				sh.start();	
			} 
			catch (Exception e) 
			{	
				e.printStackTrace();			 
				println(0, "Exception in Listener.listen() - could not start new handler");
				return;
			} 

		}
    }
  
}

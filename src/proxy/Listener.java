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

public class Listener
             implements Runnable // because it is started as a thread by the gui
{
    // class variables 
    int port = 0 ; // the port on which to listen
    
    static ArrayList<Thread> handlers;
    
    ServerSocket incomingSocket;
    
  
    public Listener ( int prt)
    {
        this.port = prt ; // pick up port parameter and pass to class variable
        proxy.SwingGUI.println(9," ");
        // set up array list
        handlers = new ArrayList<>();
        // set up incomingsocket serversocket
        try
        {
        	incomingSocket = new ServerSocket (port) ;
        }
        catch (Exception e)
        {
        	println(0, "Exception setting up Server Socket");
        }
        
    } // Listener ()
    
    public void listen(int port)
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
				handlers.add(sh);
				
				sh.start();	
			} 
			catch (Exception e) 
			{				 
				println(0, "Exception strating new handler");
			} 

		}
	}
    
    public void run()
    {   	
    	listen(port);
    }
  
} // class Listener

/** 
* Handler
*
* connection handler class for SwingGUI
* 
* initiated from Listener
*
*/

package proxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
// import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.ArrayList;
import java.util.stream.Stream;

import static proxy.SwingGUI.println;

public class Handler implements Runnable
// implements Runnable because it is started as a thread by Listener
{
	// class constants
	public static final String HTTP = "http://";
	public static final String GET = "GET";
	public static final String HEAD = "HEAD";
	public static final String POST = "POST";
	
	// class variables 
	Socket incomingSocket;
	
	BufferedReader incomingBReader;
	// HTTP Request:
	String incomingRequest ;
	String host;
	String userAgent;
	String accept;
	String acceptLang;
	String acceptEncoding;
	String connection;
	String upgradeInsecureRequests;
	
	HttpURLConnection huc;
	boolean isCacheable = false;

	public Handler(Socket skt)
	{
		println(6, "In Handler . Handler ()  " );
		incomingSocket = skt;
		
		try
		{
			incomingSocket.setSoTimeout(5000);
			incomingBReader = new BufferedReader(new InputStreamReader(incomingSocket.getInputStream())) ;
			// incomingBWriter = new BufferedWriter(new OutputStreamWriter(incomingSocket.getOutputStream()));
		} 
		catch (Exception e) 
		{
			println(0, "Exception in Handler . Handler () - socketTO and BReader setup " );
		}
	} // Handler()
	

	public void run()
	{
		println(6, "In Handler . run ()  " );
		try
		{
			this.incomingRequest = this.incomingBReader.readLine();
			println(0, "---> 0 ->  " + this.incomingRequest );
			this.host = this.incomingBReader.readLine(); // TODO : implement blocking
			println(0, "---> 1 ->  " + this.host );
			this.userAgent = this.incomingBReader.readLine();
			println(0, "---> 2 ->  " + this.userAgent );
			this.accept = this.incomingBReader.readLine();
			println(0, "---> 3 ->  " + this.accept );
			this.acceptLang = this.incomingBReader.readLine();
			println(0, "---> 4 ->  " + this.acceptLang );
			this.acceptEncoding = this.incomingBReader.readLine();
			println(0, "---> 5 ->  " + this.acceptEncoding );
			this.connection = this.incomingBReader.readLine();
			println(0, "---> 6 ->  " + this.connection );
			this.upgradeInsecureRequests = this.incomingBReader.readLine();
			println(0, "---> 7 ->  " + this.upgradeInsecureRequests );
		}
		catch (Exception e)
		{
			println(0, "Exception in Handler . run () -  BReader readline " );
			return;
		}
		String[] inreq = this.incomingRequest.split(" ");
		String requestType = inreq[0];
		String urlS = inreq[1];
		
		// if url doesn't contain http/https, prepend 'http://'
		// assumes not secure
		if (!urlS.startsWith("http"))
			urlS = HTTP + urlS;
		
		if (requestType.equals(GET) && Cache.isCacheable(urlS))
		{
			this.isCacheable = true;
			/*
			if in-cache
				send HEAD to see if cached file is up to date
				if up to date
					send cached to client
				// else, treat as if not
			*/
			
		}
		else {
			if (this.isCacheable)
			{
				// add to cache
			}
		}
		
	} // run()
	
} // class Handler

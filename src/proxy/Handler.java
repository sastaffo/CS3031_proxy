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
import java.io.IOException;
// import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
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
	
	BufferedReader requestBReader;
	BufferedWriter requestBWriter;
	// HTTP Request:
	String incomingRequest;
	String requestType;
	String host;
	String userAgent;
	
	int responseCode;
	BufferedReader responseBReader;
	BufferedWriter responseBWriter;
	
	URL url;
	HttpURLConnection connex;

	public Handler(Socket skt)
	{
		println(6, "In Handler.Handler()  " );
		this.incomingSocket = skt;
		
		
		try
		{
			this.incomingSocket.setSoTimeout(5000);
			this.requestBReader = new BufferedReader(new InputStreamReader(
							this.incomingSocket.getInputStream())) ;
			this.requestBWriter = new BufferedWriter(new OutputStreamWriter(
							skt.getOutputStream()));
			this.responseBWriter = new BufferedWriter(new OutputStreamWriter(
					skt.getOutputStream()));
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			println(0, "Exception in Handler.Handler() - socketTO and BReader setup " );
		}
	} // END Handler()
	

	public void run()
	{
		println(6, "In Handler.run()  " );
		try
		{
			this.incomingRequest = this.requestBReader.readLine();
			println(0, "---> 0 ->  " + this.incomingRequest );
		}
		catch (Exception e)
		{
			e.printStackTrace();
			println(0, "Exception in Handler.run() -  BReader readline " );
			return;
		}
		// loops through and processes all lines of the buffer
		while(true)
		{
			String tmp;
			try {
				tmp = this.requestBReader.readLine();
			}
			catch (Exception e)
			// an exception will be thrown when the buffer reader has no lines left to read
			// then it will exit the loop
			{
				break;
			}
			
			if (tmp.startsWith("Host:"))
			{
				this.host = tmp.substring(6);
				println(0, "---> " + " ->  HOST = [" + this.host + "]");
			}
			else if (tmp.startsWith("User-Agent:"))
			{
				this.userAgent = tmp.substring(12);
				println(0, "---> " + " ->  USER-AGENT = [" + this.userAgent + "]");
			}
			else {
				println(0, "---> " + " ->  ignore: " + tmp);
				continue;
			}
			
		}
		println(0, "exit for loop");
		String[] inreq = this.incomingRequest.split(" ");
		this.requestType = inreq[0];
		String urlS = inreq[1];
		
		// if url doesn't contain http/https
		// prepend 'http://' (assumes not secure)
		if (!urlS.startsWith("http"))
			urlS = HTTP + urlS;
		
		if (this.requestType.equals(GET) && Cache.isCacheable(urlS))
		{
			/*
			if in-cache
				send HEAD to see if cached file is up to date
				if up to date
					send cached to client
					{continue}
				//else, treat as if not cached
			//else
			GET page
			add to cache
			send to client
			*/
			
		}
		else // non-cacheable page or non-GET request
		{
			try
			{
				// creates url from url given in client request
				this.url = new URL(urlS);
				// opens connection with url
				this.connex = (HttpURLConnection) this.url.openConnection();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				println(0, "Exception in Handler.run() - setting up connection to noncacheable page" );
				return;
			}
			
			// sets request type as taken from client
			try
			{
				this.connex.setRequestMethod(this.requestType);
			}
			catch (ProtocolException e1)
			{
				e1.printStackTrace();
				println(0, "Exception in Handler.run() - invalid request type: " + this.requestType );
				return;
			}
			
			// adds User-Agent field taken from client
			this.connex.setRequestProperty("User-Agent", this.userAgent);
			
			// with HttpURLConnection, the first time the program encounters a 'response' method
			// it fetches the response
			try {
				this.responseCode = connex.getResponseCode();
				this.responseBReader = new BufferedReader(new InputStreamReader(connex.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
				println(0, "Exception in Handler.run() - cannot retrieve response" );
				return;
			}
			String l;
			try {
				while((l = this.responseBReader.readLine()) != null)
					this.responseBWriter.write(l);
			} catch (IOException e) {
				e.printStackTrace();
				println(0, "Exception in Handler.run() - cannot write response to buffer" );
				return;
			}
		}
		
	} // END run()
	
} // END class Handler

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
	public static final int TIMEOUT = 1000;
	public static final String HTTP = "http://";
	public static final String GET = "GET";
	
	
	// instance variables 
	int id;
	Socket incomingSocket;
	
	BufferedReader requestBReader;
	BufferedWriter requestBWriter;
	// HTTP Request:
	String incomingRequest;
	String requestType;
	String host;
	String userAgent;
	
	int responseCode;
	InputStreamReader isreader;
	OutputStreamWriter oswriter;
	BufferedReader responseBReader;
	BufferedWriter responseBWriter;
	
	URL url;
	HttpURLConnection connex;

	public Handler(Socket skt, int id)
	{
		this.id = id;
		println("In Handler.Handler() = " + this.id);
		this.incomingSocket = skt;
		
		
		try
		{
			this.incomingSocket.setSoTimeout(TIMEOUT);
			this.requestBReader = new BufferedReader(new InputStreamReader(
							this.incomingSocket.getInputStream())) ;
			this.requestBWriter = new BufferedWriter(new OutputStreamWriter(
							skt.getOutputStream()));
			this.oswriter = new OutputStreamWriter(
					skt.getOutputStream());
			this.responseBWriter = new BufferedWriter(this.oswriter);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			println("" + this.id + " > Exception in Handler.Handler() - socketTO and BReader setup " );
		}
	} // END Handler()
	

	public void run()
	{
		println("" + this.id + " > In Handler.run()  " );
		try
		{
			this.incomingRequest = this.requestBReader.readLine();
			println("" + this.id + " > ---> incoming request [" + this.incomingRequest + "]");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			println("" + this.id + " > Exception in Handler.run() -  BReader readline " );
			return;
		}
		// loops through and processes all lines of the buffer
		while(true)
		{
			String tmp;
			// an exception will be thrown when the buffer reader has no lines left to read
			// then it will exit the loop
			try {
				tmp = this.requestBReader.readLine();
			}
			catch (Exception e) { break; }
			
			
			if (tmp.startsWith("Host:"))
			{
				this.host = tmp.substring(6);
				println("" + this.id + " > HOST = [" + this.host + "]");
			}
			else if (tmp.startsWith("User-Agent:"))
			{
				this.userAgent = tmp.substring(12);
				println("" + this.id + " > USER-AGENT = [" + this.userAgent + "]");
			}
			else {
				//println("" + this.id + " > ignore: " + tmp);
				continue;
			}
			
		}
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
				println("" + this.id + " > Exception in Handler.run() - setting up connection to noncacheable page" );
				return;
			}
			
			// sets request type as taken from client
			try
			{
				//if (this.requestType.equals("CONNECT"))
					//this.requestType = "GET";
				this.connex.setRequestMethod(this.requestType);
			}
			catch (ProtocolException e1)
			{
				e1.printStackTrace();
				println("" + this.id + " > Exception in Handler.run() - invalid request type: [" + this.requestType + "]");
				return;
			}
			
			// adds User-Agent field taken from client
			this.connex.setRequestProperty("User-Agent", this.userAgent);
			
			// with HttpURLConnection, the first time the program encounters a 'response' method
			// it fetches the response
			
			try {
				this.responseCode = connex.getResponseCode();
				this.isreader = new InputStreamReader(connex.getInputStream());
				//this.responseBReader = new BufferedReader(this.isreader);
			} catch (IOException e) {
				e.printStackTrace();
				println("" + this.id + " > Exception in Handler.run() - cannot retrieve response" );
				return;
			}
			
			// writes contents of input buffer to output buffer
			Integer i;
			try {
				while((i = this.isreader.read()) != null)
					this.oswriter.write(i);
			} catch (IOException e) {
				e.printStackTrace();
				println("" + this.id + " > Exception in Handler.run() - cannot write response to output stream" );
				//return;
			}
			try {
				this.responseBWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
				println("" + this.id + " > Exception in Handler.run() - cannot flush buffer" );
				//return;
			}
			println("" + this.id + " > buffer flushed" );
			

			try {
				this.isreader.close();
			} catch (IOException e) {
				e.printStackTrace();
				println("" + this.id + " > Exception in Handler.run() - cannot close isreader" );
				//return;
			}
			println("" + this.id + " > isreader closed" );
			try {
				this.responseBWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
				println("" + this.id + " > Exception in Handler.run() - cannot close responseBWriter" );
				//return;
			}
			println("" + this.id + " > responseBWriter closed");
			//this.connex.disconnect();
			//println("connex disconnected");
			
			try {
				this.oswriter.close();
			} catch (IOException e) {
				e.printStackTrace();
				println("" + this.id + " > Exception in Handler.run() - cannot close oswriter" );
				//return;
			}
			println("" + this.id + " > oswriter closed");
		}
	}
}

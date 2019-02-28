/** 
* Handler
*
* connection handler class for SwingGUI
* 
* initiated from Listener
*
*/

package proxy;

// imports
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

import javax.imageio.ImageIO;

import static proxy.SwingGUI.println;

public class Handler implements Runnable
// implements Runnable because it is started as a thread by Listener
{
	// class constants
	public static final int LF = 10; // line feed - end of line character in ascii
	public static final int TIMEOUT = 2000;
	public static final String HTTP = "http://";
	public static final String GET = "GET";
	public static final String CONNECT = "CONNECT";
	
	
	public static final String VERSION = "HTTP/1.0";
	public static final String RESPONSE_CODE_OK = "200 OK";
	public static final String RESPONSE_CODE_NF = "404 NOT FOUND";
	public static final String PROXY_AGENT = "Proxy-Agent: ProxyServer/1.0";
	
	
	// instance variables 
	int id;
	Blocker blocker;
	Socket incomingSocket;
	
	// reader of request from client to proxy
	BufferedReader clientproxyBReader;

	// reader of response from server to proxy
	BufferedReader serverproxyBReader;

	// writer of response from proxy to client
	BufferedWriter proxyclientBWriter;
	
	
	// HTTP Request:
	String incomingRequest;
	String requestType;
	String host;
	String userAgent;
	String contentLength;
	
	int responseCode;
	
	URL url;
	HttpURLConnection connex;

	/**
	 * Handler
	 * Creates a new Handler object
	 * @param skt - Client's socket
	 * @param id - handler's id number
	 * @param b - Blocker, contains list of blocked domains
	 */
	public Handler(Socket skt, int id, Blocker b)
	{
		this.id = id;
		this.blocker = b;
		println("In Handler.Handler() = " + this.id);
		this.incomingSocket = skt;
		
		
		try
		{
			this.incomingSocket.setSoTimeout(TIMEOUT);
			this.clientproxyBReader = new BufferedReader(new InputStreamReader(
					this.incomingSocket.getInputStream()));
			this.proxyclientBWriter = new BufferedWriter(new OutputStreamWriter(
					this.incomingSocket.getOutputStream()));
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			println("" + this.id + " > Exception in Handler.Handler() - socketTO and i/o stream reader/writer setup " );
		}
	} // END Handler()
	

	/**
	 * run
	 * runs the handler thread to process request and send server's response to client
	 */
	@Override
	public void run()
	{
		println("" + this.id + " > In Handler.run()  " );
		
		this.processRequest();
		if (this.incomingRequest == null)
		{
			return;
		}
		// before going further, checks if the host has been blocked by proxy user.
		if (this.blocker.isBlocked(this.host))
		{
			println("" + this.id + " > There was an attempt to access blocked host: [" + this.host + "]" );
			return;
		}
		
		// dissection of incoming request string
		String[] inreq = this.incomingRequest.split(" ");
		this.requestType = inreq[0];
		String urlS = inreq[1];
		
		// checks if https request
		if (this.requestType.equals(CONNECT))
		{
			this.secureRequest(urlS);
			return;
		}
		
		// if url doesn't contain http
		// prepend 'http://'
		if (!urlS.startsWith("http"))
			urlS = HTTP + urlS;
		
		
		if (this.requestType.equals(GET) && Cache.isCacheable(urlS))
		{
			this.cacheablePage(urlS);
			
		}
		else // non-cacheable page or non-GET request
		{
			this.nonCacheablePage(urlS);
		}

	}
	
	/**
	 * secureRequest()
	 * handles https connections
	 * @param urlString - String of the url
	 */
	private void secureRequest(String urlString)
	{
		
	}
	
	/**
	 * cacheablePage()
	 * handles pages that are cacheable
	 * @param urlString - String of the url
	 */
	private void cacheablePage(String urlString)
	{
		/*
		if in-cache
			send HEAD to see if cached file is up to date
			if up to date
				send cached to client
				{continue}
			//else, treat as if not cached
		//else, not in cache
		GET page
		add to cache
		send to client
		*/
	}
		
	/**
	 * nonCacheablePage()
	 * handles pages that are not cacheable
	 * @param urlString - String of the url
	 */
	private void nonCacheablePage(String urlString)
	{
		try
		{
			// creates url from url given in client request
			this.url = new URL(urlString);
			// opens connection with url
			this.connex = (HttpURLConnection) this.url.openConnection();
			this.connex.setUseCaches(false);
			this.connex.setDoOutput(true);
			//this.connex.connect();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			println("" + this.id + " > Exception in Handler.run() - setting up connection to noncacheable page" );
			return;
		}
		// adds User-Agent field taken from client
		if (this.userAgent != null)
			this.connex.setRequestProperty("User-Agent", this.userAgent);
		if (this.contentLength!=null)
			this.connex.setRequestProperty("Content-Length", this.contentLength);
		this.connex.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		
		// sets request type as taken from client
		try
		{
			this.connex.setRequestMethod(this.requestType);
		}
		catch (IllegalStateException e)
		{
			println("" + this.id + " > Exception in Handler.run() - IllegalStateException");
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			println("" + this.id + " > Exception in Handler.run() - invalid request type: ["
					   + this.requestType + "]");
			return;
		}
		
		
		
		// with HttpURLConnection, the first time the program encounters a 'response' method
		// it fetches the response
		
		try
		{
			//this.connex.
			this.responseCode = connex.getResponseCode();
			
			
		} catch (IOException e)
		{
			e.printStackTrace();
			println("" + this.id + " > Exception in Handler.run() - cannot retrieve response" );
			return;
		}
		
		// writes contents of input buffer to output buffer
		String i;
		try
		{
			if (Cache.isImage(urlString))
			{
				BufferedImage image = ImageIO.read(this.url);
				if (image == null)
				{
					this.proxyclientBWriter.write(httpResponseCode(RESPONSE_CODE_NF));
					this.proxyclientBWriter.flush();
					return;
				}
				
				this.proxyclientBWriter.write(httpResponseCode(RESPONSE_CODE_OK));
				this.proxyclientBWriter.flush();
				ImageIO.write(image, "image", this.incomingSocket.getOutputStream());
			}
			else
			{
				this.serverproxyBReader = new BufferedReader(new InputStreamReader(connex.getInputStream()));
				
				this.proxyclientBWriter.write(httpResponseCode(RESPONSE_CODE_OK));
				
				while((i = this.serverproxyBReader.readLine()) != null)
				{
					this.proxyclientBWriter.write(i);
				}
				this.proxyclientBWriter.flush();
				this.serverproxyBReader.close();
			}
			
		} catch (IOException e)
		{
			e.printStackTrace();
			println("" + this.id + " > Exception in Handler.run() - cannot write response to output stream" );
			return;
		}
		return;
	}
	
	/**
	 * processRequest()
	 * split incoming request request into different segments and adds them to instance variables
	 */
	private void processRequest()
	{
		try
		{
			// the first line will contain the http request: the method and URL
			this.incomingRequest = this.clientproxyBReader.readLine();
			println("" + this.id + " > ---> incoming request [" + this.incomingRequest + "]");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			println("" + this.id + " > Exception in Handler.run() -  ISReader readline " );
			return;
		}
		// loops through and processes all lines of the buffer
		int x = 0;
		boolean readingLines = true;
		while(readingLines)
		{
			String tmp = "";
			// an exception will be thrown when the buffer reader has no lines left to read
			// then it will exit the loop
			try {
				tmp = this.clientproxyBReader.readLine();
			}
			catch (Exception e) {
				println("" + this.id + " > exiting while(true) - request parsing complete [exception]" );
				readingLines=false;
			}
			
			if (tmp == null || tmp.equals(""))
			{
				println("" + this.id + " > exiting while(true) - request parsing complete [null/empty string]" );
				readingLines=false;
			}
			else if (tmp.startsWith("Host:"))
			{
				this.host = tmp.substring(6);
				println("" + this.id + " > HOST = [" + this.host + "]");
			}
			else if (tmp.startsWith("User-Agent:"))
			{
				this.userAgent = tmp.substring(12);
				println("" + this.id + " > USER-AGENT = [" + this.userAgent + "]");
			}
			else if (tmp.startsWith("Content-Length:"))
			{
				this.contentLength = tmp.substring(15);
			}
			else {
				String t2;
				if (tmp.length() > 20)
					t2 = tmp.substring(0, 20);
				else
					t2 = tmp;
				println("" + this.id + " > "+ (x++) +" > ignore: " + t2);
				continue;
			}
			
		}
		println("" + this.id + " > exited while" );
	}
	
	/**
	 * httpResponseCode()
	 * takes in a code and returns a formatted http response message to send to the client
	 * @param code - String of http response code
	 * @return String formatted http response message
	 */
	private static String httpResponseCode(String code)
	{
		return (VERSION + " " + code + "\n" + PROXY_AGENT + "\n\r\n");
	}
}

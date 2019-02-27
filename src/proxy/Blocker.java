package proxy;

import static proxy.SwingGUI.println;

import java.util.ArrayList;

// This class does not error check on the user-entered domain names
// It assumes that the user will always enter correctly formed domain names,
// eg www.google.com
public class Blocker
{
	ArrayList<String> blockedHosts;
	
	public Blocker() 
	{
		this.blockedHosts = new ArrayList<String>();
	}
	
	public void block(String host)
	{
		if (!this.blockedHosts.contains(host))
		{
			this.add(host);
			println("Blocked: [" + host + "]");
			return;
		}
		println("[" + host + "] is already blocked");
	}
	
	public void unblock(String host)
	{
		if (this.blockedHosts.contains(host))
		{
			this.blockedHosts.remove(host);
			println("Block removed on: [" + host + "]");
			return;
		}
		println("Cannot unblock: [" + host + "] - not present in list of blocked sites");
	}
	
	public void listBlockedSites()
	{
		if (this.blockedHosts.size() == 0)
		{
			println("No sites are blocked");
			return;
		}
		println("List of blocked sites:");
		int i=0;
		for (String s : this.blockedHosts)
		{
			println("" + i++ + " > " + s);
		}
	}
	
	// checks if host of request made has been blocked
	public boolean isBlocked(String host)
	{
		for (String h : this.blockedHosts)
		{
			if (h.equals(host))
				return true;
		}
		return false;
	}

	
	// version of insertion sort adding 1 element into a sorted ArrayList
	// https://www.dreamincode.net/forums/topic/257550-insertion-sort-with-strings/
	void add(String name) {
	   for(int i = 0; i < this.blockedHosts.size(); ++i) {
	      String str = this.blockedHosts.get(i);
	      if(name.compareTo(str) <= 0) {
	         this.blockedHosts.add(i, name);
	         return;   // done
	      }
	   }
	   // not found a greater one in the arrayList
	   this.blockedHosts.add(name);  // so append to the end
	}

}

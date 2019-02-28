/**
 * Blocker
 * contains an ArrayList of blocked domains that can be added to or removed
 */
package proxy;

// imports
import static proxy.SwingGUI.println;
import java.util.ArrayList;

public class Blocker
{
	ArrayList<String> blockedHosts;
	
	/**
	 * Blocker()
	 * constructs a new blocker with an empty ArrayList of strings to track blocked domains
	 */
	public Blocker() 
	{
		this.blockedHosts = new ArrayList<String>();
	}
	
	/**
	 * block()
	 * adds a new blocked domain into the arraylist, in alphabetical order
	 * does not error check on the user-entered domain names, assumes that the user will always enter correctly formed domain names
	 * @param host - String, the domain of the site we want to block
	 */
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
	
	/**
	 * unblock()
	 * takes a domain and removes it from the arraylist so we will no longer block that site
	 * @param host - String, the domain of the site we no longer want to block
	 */
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
	
	/**
	 * listBlockedSites()
	 * prints a list of all blocked domains to the management console
	 */
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
	/**
	 * isBlocked()
	 * checks if host of request made has been blocked
	 * @param host - String, the domain that we want to check
	 * @return boolean - true if the domain has been blocked, false if otherwise
	 */
	public boolean isBlocked(String host)
	{
		for (String h : this.blockedHosts)
		{
			if (h.equals(host))
				return true;
		}
		return false;
	}

	/**
	 * add()
	 * version of insertion sort adding 1 element into a sorted ArrayList
	 * https://www.dreamincode.net/forums/topic/257550-insertion-sort-with-strings/
	 * private function that adds the newest domain into the list in alphabetical order
	 * @param name - String, the new domain to add to the arraylist
	 */
	private void add(String name) {
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

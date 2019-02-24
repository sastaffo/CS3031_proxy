package proxy;

import java.util.ArrayList;
import java.util.Comparator;

public class Blocker {
	ArrayList<String> blockedHosts;
	Comparator<String> c;
	
	public Blocker() 
	{
		this.blockedHosts = new ArrayList<String>();
	}
	
	public void block(String host)
	{
		if (!this.blockedHosts.contains(host))
		{
			this.blockedHosts.add(host);
		}
	}
	
	public void unblock(String host)
	{
		
	}
	
	// checks if host of request made has been blocked
	public static boolean isBlocked(String host)
	{
		return false;
	}
	
	public void sort()
	{
		
	}
	// quickSort algorithm adapted from: https://stackoverflow.com/a/33971385
	public ArrayList<String> quickSort(ArrayList<String> list)
	{
	    if (list.isEmpty()) 
	        return list; // start with recursion base case
	    
	    ArrayList<String> smaller = new ArrayList<String>(); 	// all Strings smaller than pivot
	    ArrayList<String> greater = new ArrayList<String>(); 	// all Strings greater than pivot
	    String pivot = list.get(0);  // first Vehicle in list, used as pivot
	    int i;
	    String j;     // Variable used for String in the loop
	    for (i=1;i<list.size();i++)
	    {
	        j=list.get(i);
	        if (j.compareTo(pivot)<0)
	            smaller.add(j);
	        else
	            greater.add(j);
	    }
	    
	    // sort both halves recursively
	    smaller=quickSort(smaller);
	    greater=quickSort(greater);
	    
	    // add initial pivot to the end of the (now sorted) smaller Strings
	    smaller.add(pivot);  
	    // add the (now sorted) greater Strings to the smaller ones
	    // (now smaller is essentially your sorted list)
	    smaller.addAll(greater);     
	    return smaller;
	}
}

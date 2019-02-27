package proxy;

import java.util.ArrayList;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;
// adapted from
// https://crunchify.com/how-to-create-a-simple-in-memory-cache-in-java-lightweight-cache/
@SuppressWarnings("hiding")
public class Cache<String, File>
{	
	static java.lang.String[] imageExtensions = {".png", ".jpg", ".gif"};
	static java.lang.String[] fileExtensions = {".html", ".htm", ".txt", ".pdf", ".css"};
	private long lifetime;
	private LRUMap<String,CacheObject> cachemap;
	
	// nested class
	protected class CacheObject {
		public long lastAccessed;
		public File value;
		protected CacheObject(File val)
		{
			this.lastAccessed = System.currentTimeMillis();
			this.value = val;
		}
	}
	// constructor
	public Cache(int lifetimems, long timerInterval, int maxItems)
	{
		this.lifetime = lifetimems * 1000;
		this.cachemap = new LRUMap<String,CacheObject>(maxItems);
		if ((this.lifetime > 0) && (timerInterval > 0))
		{
			Thread t = new Thread(new Runnable()
			{
				public void run() {
					while(true){
						try {
							Thread.sleep(timerInterval * 1000);
						} catch (Exception e) { /*no action*/}
						cleanup();
					}
				}
			});
			t.setDaemon(true);
			t.start();
		}
	}
	
	public void put(String key, File val)
	{
		synchronized (this.cachemap)
		{
			this.cachemap.put(key, new CacheObject(val));
		}
	}
	
	protected File get(String key)
	{
		synchronized (this.cachemap)
		{
			CacheObject co = (CacheObject) this.cachemap.get(key);
			
			if (co == null)
				return null;
			else
			{
				co.lastAccessed = System.currentTimeMillis();
				return co.value;
			}
		}
		
	}
	
	public void remove(String key)
	{
		synchronized (this.cachemap)
		{
			this.cachemap.remove(key);
		}
	}
	
	public int size()
	{
		synchronized (this.cachemap)
		{
			return this.cachemap.size();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void cleanup()
	{
		long now = System.currentTimeMillis();
		ArrayList<String> cleanupList;
		synchronized (this.cachemap)
		{
			MapIterator itr = this.cachemap.mapIterator();
			cleanupList = new ArrayList<String>((this.size()/2)+1);
			String k = null;
			CacheObject c = null;
			
			// loops through all cache objects to check when they were last accessed
			while (itr.hasNext())
			{
				k = (String) itr.next();
				c = (CacheObject) itr.getValue();
				// if there is still time before the cache entry times out
				if (c != null && (now > (this.lifetime + c.lastAccessed)))
					cleanupList.add(k);
			}
		}
		// runs through all keys on the list compiled in the previous loop
		// and removes them from the cache
		for (String k : cleanupList)
		{
			synchronized (this.cachemap)
			{
				this.remove(k);
			}
			Thread.yield();
		}
	}
	
	
	
	// analyses url string to determine if can be cached
	// eg is a text file or image
	public static boolean isCacheable(java.lang.String url)
	{
		/*
		url = url.toLowerCase();
		
		if (!url.contains("?")) {
			for (int i=0; i<imageExtensions.length; i++)
			{
				if (url.contains(imageExtensions[i]))
					return true;
			}
			for (int i=0; i<fileExtensions.length; i++)
			{
				if (url.contains(fileExtensions[i]))
					return true;
			}
		}
		*/
		return false;
	}
	
	public static boolean isImage(java.lang.String url)
	{
		url = url.toLowerCase();
		for (int i=0; i<imageExtensions.length; i++)
		{
			if (url.contains(imageExtensions[i]))
				return true;
		}
		return false;
	}
}

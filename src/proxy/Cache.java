/**
 * Cache
 * an LRU cache that maps Strings to Files.
 * adapted from https://crunchify.com/how-to-create-a-simple-in-memory-cache-in-java-lightweight-cache/
 */

package proxy;

// imports
import java.util.ArrayList;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;

@SuppressWarnings("hiding")
public class Cache<String, File>
{	
	static java.lang.String[] imageExtensions = {".png", ".jpeg", ".jpg", ".gif", ".pdf", ".tiff"};
	static java.lang.String[] fileExtensions = {".html", ".htm", ".txt", ".css", ".js"};
	private long lifetime;
	private LRUMap<String,CacheObject> cachemap;
	
	// nested class
	protected class CacheObject {
		public long lastAccessed;
		public File value;
		/**
		 * CacheObject()
		 * constructs a new CacheObject instance which holds the given File 
		 * and records when it was last accessed in the cache
		 * @param val - File, the object we want to cache
		 */
		protected CacheObject(File val)
		{
			this.lastAccessed = System.currentTimeMillis();
			this.value = val;
		}
	}
	/**
	 * Cache()
	 * constructs a new Cache instance which maps Strings to CacheObjects, an class that holds the cached Files.
	 * @param lifetimems - int, the length of time that a cached item will be kept after their last time accessed
	 * @param timerInterval - determines how long the cache-threads will sleep for after being started.
	 * @param maxItems - int, max size of cache
	 */
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
	
	/**
	 * put()
	 * creates a new entry into the cache
	 * @param key - String, the file name of the file we want to cache
	 * @param val - File, the file we want to cache
	 */
	public void put(String key, File val)
	{
		synchronized (this.cachemap)
		{
			this.cachemap.put(key, new CacheObject(val));
		}
	}
	/**
	 * get()
	 * takes in a String key and returns its corresponding value
	 * also updates the lastAccessed time of the file in the cache
	 * @param key - String, the key used to find the desired File.
	 * @return File - the value (of type File) that corresponds to the given key
	 */
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
	
	/**
	 * remove()
	 * removes a key-value pair from the cache when given a key.
	 * @param key - String, the file name of the file that we want to remove from the cache
	 */
	public void remove(String key)
	{
		synchronized (this.cachemap)
		{
			this.cachemap.remove(key);
		}
	}
	
	/**
	 * size()
	 * determines the size of the cache
	 * @return int - size of cache
	 */
	public int size()
	{
		synchronized (this.cachemap)
		{
			return this.cachemap.size();
		}
	}
	
	/**
	 * cleanup()
	 * loops through all items in the cache and marks any that have not been accessed in this.lifetime or longer
	 * the marked items are removed from the cache
	 */
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
	
	
	/**
	 * isCacheable()
	 * compares url string to common text and image file extensions to determine if can be cached 
	 * @param url - String, uses file extension to determine if url points to a cacheable page
	 * @return boolean -  true if page is cacheable, false otherwise
	 */
	public static boolean isCacheable(java.lang.String url)
	{
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
		return false;
	}
	
	/**
	 * isImage()
	 * compares url to image file extensions to determine if url points to an image 
	 * @param url - String, uses the file extension to determine if the url points to an image
	 * @return boolean - true if url points to an image, false otherwise
	 */
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

package proxy;

import java.util.ArrayList;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;
// adapted from
// https://crunchify.com/how-to-create-a-simple-in-memory-cache-in-java-lightweight-cache/
public class Cache<K, V>
{	
	private long lifetime;
	private LRUMap<K,CacheObject> cachemap;
	
	// nested class
	protected class CacheObject {
		public long lastAccessed;
		public V value;
		protected CacheObject(V val)
		{
			this.lastAccessed = System.currentTimeMillis();
			this.value = val;
		}
	}
	// constructor
	public Cache(int lifetimems, long timerInterval, int maxItems)
	{
		this.lifetime = lifetimems * 1000;
		this.cachemap = new LRUMap<K,CacheObject>(maxItems);
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
	
	@SuppressWarnings("unchecked")
	public void put(K key, V val)
	{
		synchronized (this.cachemap)
		{
			this.cachemap.put(key, new CacheObject(val));
		}
	}
	
	@SuppressWarnings("unchecked")
	protected V get(K key)
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
	
	public void remove(K key)
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
		ArrayList<K> cleanupList = null;
		synchronized (this.cachemap)
		{
			MapIterator itr = this.cachemap.mapIterator();
			cleanupList = new ArrayList<K>((this.size()/2)+1);
			K k = null;
			CacheObject c = null;
			
			// loops through all cache objects to check when they were last accessed
			while (itr.hasNext())
			{
				k = (K) itr.next();
				c = (CacheObject) itr.getValue();
				// if there is still time before the cache entry times out
				if (c != null && (now > (this.lifetime + c.lastAccessed)))
					cleanupList.add(k);
			}
		}
		// runs through all keys on the list compiled in the previous loop
		// and removes them from the cache
		for (K k : cleanupList)
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
	public static boolean isCacheable(String url)
	{
		return false;
	}
}

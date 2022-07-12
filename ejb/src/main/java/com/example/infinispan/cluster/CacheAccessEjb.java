package com.example.infinispan.cluster;

import java.util.Map.Entry;
import java.util.Random;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.infinispan.Cache;
import org.infinispan.util.concurrent.TimeoutException;
import org.infinispan.util.concurrent.locks.LockManager;
import org.jboss.logging.Logger;

@Stateless
public class CacheAccessEjb {

    private static final Logger LOG = Logger.getLogger(CacheAccessEjb.class);

    @EJB
    CacheManager cacheManager;

    private Cache<String, String> cache;

    /**
     * Gets the cache from the cache manager and returns all keys and values found therein.
     */
    public String getCacheContents() {
        LOG.info("cache()...");
        long start = System.currentTimeMillis();
        cache = cacheManager.getCache(CacheManager.CACHE_NAME);
        cacheManager.refreshEntry(cache);
        String result = "";
        for (Entry<String, String> e : cache.entrySet()) {
            result += e.getKey() + "=" + e.getValue() + "\n";
        }
        long end = System.currentTimeMillis();
        LOG.info("DONE in " + (end - start) + "ms");
        return result;
    }

    /**
     * Gets the default cache and puts an entry under the given key into it.
     * <p/>
     * Please note that the transaction time has been artificially elongated by
     * sleeping a random time between 500ms - 1500ms.
     */
    public String put(String key, String value){
        cache = cacheManager.getCache("default");
        Random random = new Random();
        int sleepMillis = random.nextInt(1000) + 500;
        long start = System.currentTimeMillis();
        
        String result = "";
        
        try {
            result = cache.put(key, value);
        }
        catch(TimeoutException toe){
            // this exception is never thrown/caught, as the problem arises during commit
            LOG.info("Caught timeout exception: ", toe);
            LockManager lockManager = cache.getAdvancedCache().getLockManager();
            LOG.info("Number of locks held: " + lockManager.getNumberOfLocksHeld());
            LOG.info("lock info: " + lockManager.printLockInfo());
            
            throw(toe);
        }
        
        try {
            // artificially elongate the transaction processing time
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        LOG.info("POST done in " + (end - start) + "ms");
        return result;
    }

    /**
     * Provides information about existing locks, which are always 0, even when the problem
     * is currently happening.
     */
    public String getLockInfo(){
        cache = cacheManager.getCache(CacheManager.CACHE_NAME);
        LockManager lockManager = cache.getAdvancedCache().getLockManager();
        LOG.info("Number of locks held: " + lockManager.getNumberOfLocksHeld());
        LOG.info("lock info: " + lockManager.printLockInfo());
        return "" + lockManager.getNumberOfLocksHeld() + ": " + lockManager.printLockInfo();
    }

}

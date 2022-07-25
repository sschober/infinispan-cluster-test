package com.example.infinispan.cluster;

import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.logging.Logger;

/**
 * Singleton bean instantiation an infinispan embedded cache manager on each 
 * cluster node during post construct.
 */
@Singleton
@Startup
public class CacheManager {

    private EmbeddedCacheManager cacheManager;
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static final Logger LOG = Logger.getLogger(CacheManager.class);

    private String serverName = System.getProperty("weblogic.Name");

    public static String CACHE_NAME = "default";
    /**
     * initializes infinispan cache manager an puts the current time under 
     * the current server name as key into the cache.
     */
    @PostConstruct
    public void init(){
        try {
            LOG.info("CacheManager()...");
            long start = System.currentTimeMillis();
            cacheManager = new DefaultCacheManager(CacheManager.class.getResourceAsStream("/infinispan.xml"));
            long end = System.currentTimeMillis();
            LOG.info("DONE in " + (end - start) + "ms");
            if(null == serverName){
                serverName = InetAddress.getLocalHost().getHostName();
            }
            refreshEntry(cacheManager.getCache(CACHE_NAME));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshEntry(Cache<String,String> cache){
        cache.put(serverName, dateFormat.format(new Date()));
    }

    public Cache<String,String> getCache(String cacheName){
        return cacheManager.getCache(cacheName);
    }
}

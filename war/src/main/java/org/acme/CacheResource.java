package org.acme;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.example.infinispan.cluster.CacheAccessEjb;

@Path("/cache")
@RequestScoped
public class CacheResource {

    @Inject
    CacheAccessEjb ejb;


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getContents() {
        return ejb.getCacheContents();
    }

    public static class CacheEntry {
        public String key;
        public String value;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String put(CacheEntry entry){
        return ejb.put(entry.key, entry.value);
    }

    @GET
    @Path("/lock")
    public String getLockInfo(){
        return ejb.getLockInfo();
    }
}
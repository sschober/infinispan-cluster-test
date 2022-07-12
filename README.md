# Infinispan Cluster Test 

This demonstartor application consists of

1. An EJB module, containing an EJB singleton which starts an Infinispan
embedded cache manager, and a stateless session bean, which provides access to a cache therein (configured via)
`infinispa.xml`). Please, that the transaction time was elongated artificially by us by random sleeps in the 
business methods.

1. A web archive contains a rest endpoint, which provides HTTP access to the EJB mentioned above.

1. A shell client script, which calls the rest endpoint in an endless loop making random requests

## Cache configuration

We use a `replicated-cache` with isolation level `REPEATABLE_READ`, with `FULL_XA` transactions:

```
<replicated-cache-configuration name="repl-cache" mode="SYNC" statistics="false" remote-timeout="15000">
    <locking acquire-timeout="15000" isolation="REPEATABLE_READ" />
    <transaction mode="FULL_XA" stop-timeout="1000" transaction-manager-lookup="org.infinispan.transaction.lookup.GenericTransactionManagerLookup" locking="OPTIMISTIC"/>
    <memory storage="HEAP" />
    <encoding media-type="application/x-java-serialized-object" />
</replicated-cache-configuration>
```

## Deployment Setting

The demonstrator needs to be deployed on two separate application servers (we tested on weblogic 12.1.4) 
on two separate VMs (we could not reproduce the problem on a single vm).

## Problem

When one node crashes/is powered off (by powering off the VM hosting the corresponding JVM), we see the following exceptions arise on the other node:

```
Caused by: org.infinispan.util.concurrent.TimeoutException: ISPN000299: Unable to acquire lock after 15 seconds for key WrappedByteArray[ACED0005\t0001\9 (8 bytes)] and requestor GlobalTransaction{id=496, addr=HOST1-43695, remote=false, xid=Xid{formatId=48801, globalTransactionId=01EF088AA6508A806BC5,branchQualifier=6F72672E696E66696E697370616E2E7472616E73616374696F6E2E78612E5472616E73616374696F6E586141646170746572}, internalId=562954248389104}. Lock is held by GlobalTransaction{id=337, addr=BAGOSACP3x06-43695, remote=false, xid=Xid{formatId=48801, globalTransactionId=0150088AA6508A806BC5,branchQualifier=6F72672E696E66696E697370616E2E7472616E73616374696F6E2E78612E5472616E73616374696F6E586141646170746572}, internalId=281479271678289} (pending)
    at org.infinispan.util.concurrent.locks.impl.DefaultPendingLockManager.timeout(DefaultPendingLockManager.java:148)
    at org.infinispan.util.concurrent.locks.impl.DefaultPendingLockManager.access$200(DefaultPendingLockManager.java:49)
    ... 9 more
```

This leads to the corresponding keys being inaccessible for writing.
Thise excpetions do _not_ vanish once a configured JTA time out expires.

We tried this with several infinispan versions: `8.2.7`, `12.1.7` and the latest `13.0.10` and it
happens with each one.

## Steps to reproduce

1. Deploy the application to applications server instances on two two nodes
1. Set the parameters at application server start: 
   - `jgroups.tcp.address` to the respective bind address
   - `jgroups.tcp.port` to the respective bind port
   - `jgroups.tcpping.initial_hosts` populate it to the participating nodes
1. Start the application servers
1. Start the client scripts multiple times (preferably against both nodes)
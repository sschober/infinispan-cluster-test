#!/bin/bash

# demonstrator client script
#
# Call like this:
#
#    ./clienst <hostname> <host port>
#
ENDPOINT_HOST=$1
ENDPOINT_PORT=$2

while true; 
do 
    MES=$(perl -e 'print int rand(10)'); 
    VALUE=$(echo $$ $(hostname) $(date +'%F %T:%N'));
    DATA=$(echo "{ \"key\" : \"$MES\", \"value\" : \"$VALUE\" }");
    echo $DATA;
    curl -X POST http://$ENDPOINT_HOST:$ENDPOINT_PORT/infinispan-cluster-test-war/resources/cache\
        -H 'Content-Type: application/json' \
        -d "$DATA";
    sleep $(( $MES / 10 )); 
done
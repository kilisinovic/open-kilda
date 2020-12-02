##### WIP #####
Expansion for openkilda netconf speaker - NENAD(Network Node Administrator).

There are three components to setup for this(in parenthesies is folder which contains source code for that part):
- Storm topology(storm)
- Sysrepo database(device3)
- Netconf speaker(netconfspeaker)

Information on how these components fit together can be found in Documentation folder.

To be able to compile and build components it's needed to first build the code in "serialization" and "netconf-java"
 folders and install them to local maven repository. Netconf-java library is taken form https://github.com/Juniper/netconf-java
and is modified to suit our needs.

Kafka topics which need to be configured are:
- "node-administrator-request"
- "node-administrator-response"
- "netconfspeaker-request"
- "netconfspeaker-response"
Storm spout collects requests from kafka topic "node-administrator-request" and response
 is expected to be sent to "node-administrator-response" topic.
Request is processed and validated inside storm after which is generated request for device
 node which is then passed to kafka topic "netconfspeaker-request". After netconf speaker
applies the request to device, the response is returned to "netconfspeaker-response" topic
in order to be further processed by storm.
